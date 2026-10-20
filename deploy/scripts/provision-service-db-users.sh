#!/bin/sh
set -eu

validate_identifier()
{
    name="$1"
    value="$2"
    case "$value" in
        ""|*[!A-Za-z0-9_]*)
            echo "$name must contain only letters, numbers and underscores" >&2
            exit 1
            ;;
    esac
}

validate_password()
{
    name="$1"
    value="$2"
    if [ "${#value}" -lt 12 ]; then
        echo "$name must contain at least 12 characters" >&2
        exit 1
    fi
    case "$value" in
        *"'"*|*"\\"*)
            echo "$name must not contain single quotes or backslashes" >&2
            exit 1
            ;;
    esac
}

: "${MYSQL_HOST:?MYSQL_HOST is required}"
: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD is required}"
: "${PLATFORM_DB_USERNAME:?PLATFORM_DB_USERNAME is required}"
: "${PLATFORM_DB_PASSWORD:?PLATFORM_DB_PASSWORD is required}"
: "${MERCHANT_DB_USERNAME:?MERCHANT_DB_USERNAME is required}"
: "${MERCHANT_DB_PASSWORD:?MERCHANT_DB_PASSWORD is required}"
: "${BUSINESS_DB_USERNAME:?BUSINESS_DB_USERNAME is required}"
: "${BUSINESS_DB_PASSWORD:?BUSINESS_DB_PASSWORD is required}"

validate_identifier PLATFORM_DB_USERNAME "$PLATFORM_DB_USERNAME"
validate_identifier MERCHANT_DB_USERNAME "$MERCHANT_DB_USERNAME"
validate_identifier BUSINESS_DB_USERNAME "$BUSINESS_DB_USERNAME"
validate_password PLATFORM_DB_PASSWORD "$PLATFORM_DB_PASSWORD"
validate_password MERCHANT_DB_PASSWORD "$MERCHANT_DB_PASSWORD"
validate_password BUSINESS_DB_PASSWORD "$BUSINESS_DB_PASSWORD"

export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"

attempt=0
until mysqladmin --protocol=tcp -h "$MYSQL_HOST" -uroot ping --silent >/dev/null 2>&1; do
    attempt=$((attempt + 1))
    if [ "$attempt" -ge 60 ]; then
        echo "Timed out waiting for MySQL at $MYSQL_HOST" >&2
        exit 1
    fi
    sleep 2
done

mysql --protocol=tcp -h "$MYSQL_HOST" -uroot <<SQL
CREATE USER IF NOT EXISTS '$PLATFORM_DB_USERNAME'@'%' IDENTIFIED BY '$PLATFORM_DB_PASSWORD';
ALTER USER '$PLATFORM_DB_USERNAME'@'%' IDENTIFIED BY '$PLATFORM_DB_PASSWORD';
REVOKE ALL PRIVILEGES, GRANT OPTION FROM '$PLATFORM_DB_USERNAME'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON \`ry-cloud\`.* TO '$PLATFORM_DB_USERNAME'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON \`ry_rental\`.* TO '$PLATFORM_DB_USERNAME'@'%';

CREATE USER IF NOT EXISTS '$MERCHANT_DB_USERNAME'@'%' IDENTIFIED BY '$MERCHANT_DB_PASSWORD';
ALTER USER '$MERCHANT_DB_USERNAME'@'%' IDENTIFIED BY '$MERCHANT_DB_PASSWORD';
REVOKE ALL PRIVILEGES, GRANT OPTION FROM '$MERCHANT_DB_USERNAME'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON \`ry_rental\`.* TO '$MERCHANT_DB_USERNAME'@'%';

CREATE USER IF NOT EXISTS '$BUSINESS_DB_USERNAME'@'%' IDENTIFIED BY '$BUSINESS_DB_PASSWORD';
ALTER USER '$BUSINESS_DB_USERNAME'@'%' IDENTIFIED BY '$BUSINESS_DB_PASSWORD';
REVOKE ALL PRIVILEGES, GRANT OPTION FROM '$BUSINESS_DB_USERNAME'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON \`ry_business\`.* TO '$BUSINESS_DB_USERNAME'@'%';
FLUSH PRIVILEGES;
SQL

echo "Provisioned isolated platform and merchant database users"
