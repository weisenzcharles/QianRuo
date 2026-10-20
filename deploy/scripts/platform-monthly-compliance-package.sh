#!/bin/sh

set -eu

: "${MYSQL_HOST:?MYSQL_HOST is required}"
: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD is required}"

MYSQL_PORT="${MYSQL_PORT:-3306}"
BACKUP_DIR="${BACKUP_DIR:-/var/lib/qianruo-backup}"
RETENTION_DAYS="${LOG_PACKAGE_RETENTION_DAYS:-1095}"
TZ="${TZ:-Asia/Shanghai}"
export TZ

case "$RETENTION_DAYS" in
    ''|*[!0-9]*)
        echo "LOG_PACKAGE_RETENTION_DAYS must be a non-negative integer" >&2
        exit 1
        ;;
esac

mkdir -p "$BACKUP_DIR"

mysql_command()
{
    MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --protocol=tcp -h "$MYSQL_HOST" -P "$MYSQL_PORT" -uroot \
        --default-character-set=utf8mb4 "$@"
}

period_key="$(mysql_command --batch --skip-column-names \
    -e "SELECT DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y%m')")"
if [ -z "$period_key" ]; then
    echo "无法确定待归档月份" >&2
    exit 1
fi

now="$(date '+%Y%m%d%H%M%S')"
started_time="$(date '+%Y-%m-%d %H:%M:%S')"
backup_no="LOG${period_key}"
file_name="monthly-compliance-${period_key}.sql.gz"
backup_key="operations/compliance/${file_name}"
backup_file="${BACKUP_DIR}/${file_name}"
temporary_file="${BACKUP_DIR}/.${file_name}.sql"
backup_record_created=0

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
                    failure_reason = '月度审计财务归档失败，退出码 ${exit_code}',
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

existing_status="$(mysql_command --database=ry_rental --batch --skip-column-names \
    -e "SELECT backup_status
        FROM ry_rental_platform_backup_record
        WHERE backup_no = '${backup_no}' AND is_delete = 0 LIMIT 1" 2>/dev/null || true)"
case "$existing_status" in
    1)
        echo "${period_key} 月归档已经成功，跳过重复执行"
        exit 0
        ;;
    0|2|3)
        mysql_command --database=ry_rental \
            -e "UPDATE ry_rental_platform_backup_record
                SET backup_status = 0,
                    file_size_bytes = 0,
                    started_time = '${started_time}',
                    completed_time = NULL,
                    retention_until = NULL,
                    failure_reason = NULL,
                    update_time = NOW()
                WHERE backup_no = '${backup_no}' AND is_delete = 0" >/dev/null
        backup_record_created=1
        ;;
    '')
        mysql_command --database=ry_rental \
            -e "INSERT INTO ry_rental_platform_backup_record
                    (backup_no, backup_type, backup_status, backup_key, file_name, file_size_bytes,
                     started_time, create_time, update_time, is_delete)
                VALUES
                    ('${backup_no}', 2, 0, '${backup_key}', '${file_name}', 0,
                     '${started_time}', NOW(), NOW(), 0)" >/dev/null
        backup_record_created=1
        ;;
    *)
        echo "月度归档记录状态不合法：${existing_status}" >&2
        exit 1
        ;;
esac

# 表清单固定在脚本中，实际导出前再从 information_schema 过滤掉未安装的可选表。
available_tables="$(mysql_command --batch --skip-column-names \
    -e "SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = 'ry_rental'
          AND table_name IN (
              'ry_rental_audit_log',
              'ry_rental_finance_account',
              'ry_rental_finance_ledger',
              'ry_rental_payment_order',
              'ry_rental_registration_payment',
              'ry_rental_finance_withdrawal',
              'ry_rental_invoice_application',
              'ry_rental_invoice_record',
              'ry_rental_invoice_red_flush'
          )
        ORDER BY table_name")"
if [ -z "$available_tables" ]; then
    echo "没有可归档的审计或财务表" >&2
    exit 1
fi

echo "开始归档 ${period_key} 月审计与财务数据：${backup_key}"
MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump --protocol=tcp -h "$MYSQL_HOST" -P "$MYSQL_PORT" -uroot \
    --single-transaction --routines --events --triggers --hex-blob \
    ry_rental $available_tables > "$temporary_file"
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

find "$BACKUP_DIR" -type f -name 'monthly-compliance-*.sql.gz' -mtime "+$RETENTION_DAYS" -delete
mysql_command --database=ry_rental \
    -e "UPDATE ry_rental_platform_backup_record
        SET backup_status = 3, update_time = NOW()
        WHERE backup_type = 2 AND backup_status = 1 AND retention_until < NOW() AND is_delete = 0" >/dev/null

echo "月度审计财务归档完成：${backup_key}，大小 ${file_size_bytes} 字节，保留 ${RETENTION_DAYS} 天"
