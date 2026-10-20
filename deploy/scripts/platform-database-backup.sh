#!/bin/sh

set -eu

: "${MYSQL_HOST:?MYSQL_HOST is required}"
: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD is required}"

MYSQL_PORT="${MYSQL_PORT:-3306}"
BACKUP_DIR="${BACKUP_DIR:-/var/lib/qianruo-backup}"
RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-7}"
TZ="${TZ:-Asia/Shanghai}"
export TZ

case "$RETENTION_DAYS" in
    ''|*[!0-9]*)
        echo "BACKUP_RETENTION_DAYS must be a non-negative integer" >&2
        exit 1
        ;;
esac

mkdir -p "$BACKUP_DIR"

now="$(date '+%Y%m%d%H%M%S')"
started_time="$(date '+%Y-%m-%d %H:%M:%S')"
backup_no="BK${now}"
file_name="database-backup-${now}.sql.gz"
backup_key="operations/database/${file_name}"
backup_file="${BACKUP_DIR}/${file_name}"
temporary_file="${BACKUP_DIR}/.${file_name}.sql"
backup_record_created=0

mysql_command()
{
    MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --protocol=tcp -h "$MYSQL_HOST" -P "$MYSQL_PORT" -uroot \
        --default-character-set=utf8mb4 "$@"
}

record_failure()
{
    exit_code="$1"
    if [ "$backup_record_created" -eq 1 ]; then
        completed_time="$(date '+%Y-%m-%d %H:%M:%S')"
        set +e
        mysql_command --database=ry_rental \
            -e "UPDATE ry_rental_platform_backup_record
                SET backup_status = 2,
                    completed_time = '${completed_time}',
                    failure_reason = '备份脚本执行失败，退出码 ${exit_code}',
                    update_time = NOW()
                WHERE backup_no = '${backup_no}' AND is_delete = 0" >/dev/null 2>&1
        set -e
    fi
    rm -f "$temporary_file" "$backup_file"
    exit "$exit_code"
}

on_exit()
{
    exit_code="$1"
    trap - EXIT
    if [ "$exit_code" -ne 0 ]; then
        record_failure "$exit_code"
    fi
    exit 0
}

trap 'on_exit "$?"' EXIT

mysql_command --database=ry_rental \
    -e "INSERT INTO ry_rental_platform_backup_record
            (backup_no, backup_type, backup_status, backup_key, file_name, file_size_bytes,
             started_time, create_time, update_time, is_delete)
        VALUES
            ('${backup_no}', 1, 0, '${backup_key}', '${file_name}', 0,
             '${started_time}', NOW(), NOW(), 0)" >/dev/null
backup_record_created=1

echo "开始执行数据库备份：${backup_key}"
MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump --protocol=tcp -h "$MYSQL_HOST" -P "$MYSQL_PORT" -uroot \
    --all-databases --single-transaction --routines --events --triggers --hex-blob > "$temporary_file"
gzip -c "$temporary_file" > "$backup_file"
rm -f "$temporary_file"

file_size_bytes="$(wc -c < "$backup_file" | tr -d ' ')"
completed_time="$(date '+%Y-%m-%d %H:%M:%S')"
mysql_command --database=ry_rental \
    -e "UPDATE ry_rental_platform_backup_record
        SET backup_status = 1,
            file_size_bytes = ${file_size_bytes},
            completed_time = '${completed_time}',
            retention_until = DATE_ADD('${completed_time}', INTERVAL ${RETENTION_DAYS} DAY),
            failure_reason = NULL,
            update_time = NOW()
        WHERE backup_no = '${backup_no}' AND is_delete = 0" >/dev/null

find "$BACKUP_DIR" -type f -name 'database-backup-*.sql.gz' -mtime "+$RETENTION_DAYS" -delete
mysql_command --database=ry_rental \
    -e "UPDATE ry_rental_platform_backup_record
        SET backup_status = 3, update_time = NOW()
        WHERE backup_type = 1 AND backup_status = 1 AND retention_until < NOW() AND is_delete = 0" >/dev/null

record_backup_volume_capacity()
{
    capacity_snapshot="$(df -Pk "$BACKUP_DIR" 2>/dev/null | awk 'NR == 2 {printf "%.0f %.0f %.0f\\n", $2 * 1024, $3 * 1024, $4 * 1024}')"
    if [ -z "$capacity_snapshot" ]; then
        echo "无法读取备份卷容量，跳过容量快照" >&2
        return 0
    fi

    capacity_bytes="$(printf '%s' "$capacity_snapshot" | awk '{print $1}')"
    used_bytes="$(printf '%s' "$capacity_snapshot" | awk '{print $2}')"
    available_bytes="$(printf '%s' "$capacity_snapshot" | awk '{print $3}')"
    previous_used_bytes="$(mysql_command --database=ry_rental --batch --skip-column-names \
        -e "SELECT used_bytes
            FROM ry_rental_platform_storage_quota_ledger
            WHERE scope_type = 3 AND scope_key = 'backup-data' AND is_delete = 0
            ORDER BY measured_time DESC, storage_ledger_id DESC LIMIT 1" 2>/dev/null || true)"

    if [ -z "$previous_used_bytes" ]; then
        changed_bytes=0
        change_type=1
    elif [ "$used_bytes" -gt "$previous_used_bytes" ]; then
        changed_bytes=$((used_bytes - previous_used_bytes))
        change_type=2
    elif [ "$used_bytes" -lt "$previous_used_bytes" ]; then
        changed_bytes=$((used_bytes - previous_used_bytes))
        change_type=3
    else
        changed_bytes=0
        change_type=4
    fi

    measured_time="$(date '+%Y-%m-%d %H:%M:%S')"
    if ! mysql_command --database=ry_rental \
        -e "INSERT INTO ry_rental_platform_storage_quota_ledger
                (scope_type, scope_key, scope_name, capacity_bytes, used_bytes, available_bytes,
                 changed_bytes, change_type, snapshot_source, measured_time, create_time, update_time, is_delete)
            VALUES
                (3, 'backup-data', '数据库备份存储卷', ${capacity_bytes}, ${used_bytes}, ${available_bytes},
                 ${changed_bytes}, ${change_type}, 'qianruo-database-backup', '${measured_time}', NOW(), NOW(), 0)" >/dev/null; then
        return 1
    fi

    min_available_percent="$(mysql_command --batch --skip-column-names \
        -e "SELECT config_value FROM \`ry-cloud\`.sys_config
            WHERE config_key = 'platform.operations.backup.min-available-percent'
              AND is_delete = 0 LIMIT 1" 2>/dev/null || true)"
    case "$min_available_percent" in
        ''|*[!0-9]*) min_available_percent=10 ;;
    esac
    if [ "$min_available_percent" -gt 100 ]; then
        min_available_percent=10
    fi

    if [ "$capacity_bytes" -gt 0 ]; then
        available_percent=$((available_bytes * 100 / capacity_bytes))
        if [ "$available_percent" -lt "$min_available_percent" ]; then
            alert_level=2
            if [ "$available_percent" -le 5 ]; then
                alert_level=3
            fi
            if ! mysql_command --database=ry_rental \
                -e "INSERT INTO ry_rental_platform_operations_alert_event
                        (alert_event_key, alert_type, alert_level, title, detail, source_type, source_key,
                         occurrence_count, event_status, first_seen_time, last_seen_time,
                         create_time, update_time, is_delete)
                    VALUES
                        ('STORAGE:backup-data', 4, ${alert_level}, '备份存储空间不足',
                         '备份卷可用空间 ${available_percent}%，低于阈值 ${min_available_percent}%',
                         'backup-volume', 'backup-data', 1, 0, NOW(), NOW(), NOW(), NOW(), 0)
                    ON DUPLICATE KEY UPDATE
                        alert_level = VALUES(alert_level),
                        detail = VALUES(detail),
                        occurrence_count = occurrence_count + 1,
                        first_seen_time = CASE WHEN event_status = 2 THEN NOW() ELSE first_seen_time END,
                        last_seen_time = NOW(),
                        ack_by = CASE WHEN event_status = 2 THEN NULL ELSE ack_by END,
                        ack_time = CASE WHEN event_status = 2 THEN NULL ELSE ack_time END,
                        resolved_by = CASE WHEN event_status = 2 THEN NULL ELSE resolved_by END,
                        resolved_time = CASE WHEN event_status = 2 THEN NULL ELSE resolved_time END,
                        resolved_remark = CASE WHEN event_status = 2 THEN NULL ELSE resolved_remark END,
                        event_status = CASE WHEN event_status = 2 THEN 0 ELSE event_status END,
                        update_time = NOW(), is_delete = 0" >/dev/null; then
                echo "备份已完成，但存储不足告警写入失败" >&2
            fi
        elif ! mysql_command --database=ry_rental \
            -e "UPDATE ry_rental_platform_operations_alert_event
                SET event_status = 2, resolved_time = NOW(),
                    resolved_remark = '备份卷可用空间已恢复', update_time = NOW()
                WHERE alert_event_key = 'STORAGE:backup-data'
                  AND event_status IN (0, 1) AND is_delete = 0" >/dev/null; then
            echo "备份已完成，但存储恢复告警解除失败" >&2
        fi
    fi
}

if ! record_backup_volume_capacity; then
    echo "备份已完成，但容量快照写入失败" >&2
fi

echo "数据库备份完成：${backup_key}，大小 ${file_size_bytes} 字节，保留 ${RETENTION_DAYS} 天"
