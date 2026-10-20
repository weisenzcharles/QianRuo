#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ENV_FILE:-$ROOT_DIR/deploy/.env}"
GENERATED_DIR="$ROOT_DIR/deploy/.generated"
GENERATED_MANIFEST="$GENERATED_DIR/qianruo.yaml"
GENERATED_INGRESS="$GENERATED_DIR/ingress.yaml"
LOCAL_COMPOSE_FILE="$ROOT_DIR/deploy/docker-compose.local.yml"
LOCAL_COMPOSE_PROJECT="deploy"

if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

REGISTRY="${REGISTRY:-}"
IMAGE_NAMESPACE="${IMAGE_NAMESPACE:-qianruo}"
IMAGE_TAG="${IMAGE_TAG:-$(git -C "$ROOT_DIR" rev-parse HEAD 2>/dev/null || echo latest)}"
K8S_NAMESPACE="${K8S_NAMESPACE:-qianruo}"
KUBE_CONTEXT="${KUBE_CONTEXT:-}"
STORAGE_CLASS_NAME="${STORAGE_CLASS_NAME:-alicloud-disk-topology}"
ADMIN_HOST="${ADMIN_HOST:-}"
MERCHANT_HOST="${MERCHANT_HOST:-}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-}"
PLATFORM_DB_USERNAME="${PLATFORM_DB_USERNAME:-qianruo_platform}"
PLATFORM_DB_PASSWORD="${PLATFORM_DB_PASSWORD:-}"
MERCHANT_DB_USERNAME="${MERCHANT_DB_USERNAME:-qianruo_merchant}"
MERCHANT_DB_PASSWORD="${MERCHANT_DB_PASSWORD:-}"
BUSINESS_DB_USERNAME="${BUSINESS_DB_USERNAME:-qianruo_business}"
BUSINESS_DB_PASSWORD="${BUSINESS_DB_PASSWORD:-}"
NACOS_AUTH_TOKEN="${NACOS_AUTH_TOKEN:-}"
NACOS_AUTH_IDENTITY_KEY="${NACOS_AUTH_IDENTITY_KEY:-}"
NACOS_AUTH_IDENTITY_VALUE="${NACOS_AUTH_IDENTITY_VALUE:-}"
BUILD_BACKEND="${BUILD_BACKEND:-true}"
BUILD_FRONTEND="${BUILD_FRONTEND:-true}"
BUILD_IMAGES="${BUILD_IMAGES:-true}"
PUSH_IMAGES="${PUSH_IMAGES:-true}"
APPLY_MANIFESTS="${APPLY_MANIFESTS:-true}"
INIT_DB="${INIT_DB:-false}"
MIGRATE_DB="${MIGRATE_DB:-true}"
JAVA_OPTS="${JAVA_OPTS:--Xms256m -Xmx512m -Duser.timezone=Asia/Shanghai}"
JAVA_BASE_IMAGE="${JAVA_BASE_IMAGE:-mcr.microsoft.com/openjdk/jdk:17-ubuntu}"
NGINX_BASE_IMAGE="${NGINX_BASE_IMAGE:-m.daocloud.io/docker.io/library/nginx:1.27}"
MYSQL_IMAGE="${MYSQL_IMAGE:-mysql:8.4}"
REDIS_IMAGE="${REDIS_IMAGE:-m.daocloud.io/docker.io/library/redis:7.4}"
NACOS_IMAGE="${NACOS_IMAGE:-nacos/nacos-server:v3.0.2}"
IMAGE_PLATFORM="${IMAGE_PLATFORM:-}"
FILE_STORAGE_PROVIDER="${FILE_STORAGE_PROVIDER:-aliyun-oss}"

export REGISTRY IMAGE_NAMESPACE IMAGE_TAG K8S_NAMESPACE KUBE_CONTEXT STORAGE_CLASS_NAME ADMIN_HOST MERCHANT_HOST LOCAL_COMPOSE_PROJECT
export MYSQL_ROOT_PASSWORD PLATFORM_DB_USERNAME PLATFORM_DB_PASSWORD MERCHANT_DB_USERNAME MERCHANT_DB_PASSWORD
export BUSINESS_DB_USERNAME BUSINESS_DB_PASSWORD
export NACOS_AUTH_TOKEN NACOS_AUTH_IDENTITY_KEY NACOS_AUTH_IDENTITY_VALUE JAVA_OPTS
export JAVA_BASE_IMAGE NGINX_BASE_IMAGE MYSQL_IMAGE REDIS_IMAGE NACOS_IMAGE IMAGE_PLATFORM
export FILE_STORAGE_PROVIDER

BACKEND_IMAGES=(
  "ruoyi-gateway:backend/ruoyi-cloud/ruoyi-gateway/target/ruoyi-gateway.jar:ruoyi-gateway"
  "ruoyi-auth:backend/ruoyi-cloud/ruoyi-auth/target/ruoyi-auth.jar:ruoyi-auth"
  "platform-service:backend/ruoyi-cloud/ruoyi-modules/ruoyi-platform-service/target/ruoyi-platform-service.jar:platform-service"
  "merchant-service:backend/ruoyi-cloud/ruoyi-modules/ruoyi-merchant-service/target/ruoyi-merchant-service.jar:merchant-service"
  "rental-business:backend/ruoyi-cloud/ruoyi-modules/ruoyi-rental-business/target/ruoyi-rental-business.jar:rental-business"
  "ruoyi-modules-gen:backend/ruoyi-cloud/ruoyi-modules/ruoyi-gen/target/ruoyi-modules-gen.jar:ruoyi-gen"
  "ruoyi-modules-job:backend/ruoyi-cloud/ruoyi-modules/ruoyi-job/target/ruoyi-modules-job.jar:ruoyi-job"
  "ruoyi-modules-file:backend/ruoyi-cloud/ruoyi-modules/ruoyi-file/target/ruoyi-modules-file.jar:ruoyi-file"
  "ruoyi-visual-monitor:backend/ruoyi-cloud/ruoyi-visual/ruoyi-monitor/target/ruoyi-visual-monitor.jar:ruoyi-monitor"
)

FRONTEND_IMAGES=(
  "rental-admin-web:frontend/admin-web/dist:deploy/nginx/admin.conf"
  "rental-merchant-web:frontend/merchant-web/dist:deploy/nginx/merchant.conf"
)

DATABASE_MIGRATIONS=(
  "database/migrations/V20261019_002__consolidated_project_baseline.sql"
)

MIGRATION_HISTORY_DATABASE="ry-config"
MIGRATION_HISTORY_TABLE="qianruo_schema_migration"
# 仅支持此版本及之后的数据库切换到合并基线；旧账本不再具备增量升级路径。
PRE_CONSOLIDATION_VERSION="V20261019_001__platform_merchant_menu_management"
PRE_CONSOLIDATION_CHECKSUM="67d391a146a4d1c52f059952fbb4d6c190b73742494e47eea400387506a2d132"

usage() {
  cat <<'EOF'
Usage: ./scripts/deploy.sh [all|build|push|deploy|render|init-db|migrate-db|status|local|local-reset|local-pull-base|local-init-db|local-migrate-db|local-down|local-status]

Commands:
  all      Build backend/frontend, build images, push images, render and apply Kubernetes manifests.
  build    Build backend/frontend artifacts and Docker images.
  push     Push all application images.
  deploy   Render and apply Kubernetes manifests.
  render   Render Kubernetes manifests into deploy/.generated.
  init-db  Initialize MySQL and apply the consolidated project baseline. Requires CONFIRM_INIT_DB=yes.
  migrate-db
           Apply only unapplied database migrations without recreating tables.
  status   Show Kubernetes workload status.
  local    Build images and start the full stack in local Docker Desktop.
  local-reset
           Reset local Docker volumes, initialize database and start the full stack.
  local-pull-base
           Pull Java, Nginx, MySQL, Redis and Nacos base images for local Docker Desktop.
  local-init-db
           Initialize the local Docker MySQL. Requires CONFIRM_INIT_DB=yes.
  local-migrate-db
           Apply only unapplied database migrations to the local Docker MySQL.
  local-down
           Stop local Docker services.
  local-status
           Show local Docker service status.
EOF
}

log() {
  printf '\n[%s] %s\n' "$(date '+%H:%M:%S')" "$*"
}

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing command: $1" >&2
    exit 1
  }
}

require_registry() {
  if [[ -z "$REGISTRY" || "$REGISTRY" == *"<region>"* ]]; then
    echo "Please set REGISTRY in deploy/.env, for example cr-<region>.volces.com." >&2
    exit 1
  fi
}

require_secret_vars() {
  local missing=0
  for key in MYSQL_ROOT_PASSWORD PLATFORM_DB_PASSWORD MERCHANT_DB_PASSWORD BUSINESS_DB_PASSWORD NACOS_AUTH_TOKEN NACOS_AUTH_IDENTITY_KEY NACOS_AUTH_IDENTITY_VALUE; do
    local value="${!key:-}"
    if [[ -z "$value" || "$value" == CHANGE_ME* ]]; then
      echo "Please set $key in deploy/.env." >&2
      missing=1
    fi
  done
  [[ "$missing" -eq 0 ]] || exit 1
}

prepare_local_env() {
  REGISTRY="${LOCAL_REGISTRY:-local}"
  IMAGE_NAMESPACE="${IMAGE_NAMESPACE:-qianruo}"
  MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-}"
  PLATFORM_DB_USERNAME="${PLATFORM_DB_USERNAME:-qianruo_platform}"
  PLATFORM_DB_PASSWORD="${PLATFORM_DB_PASSWORD:-}"
  MERCHANT_DB_USERNAME="${MERCHANT_DB_USERNAME:-qianruo_merchant}"
  MERCHANT_DB_PASSWORD="${MERCHANT_DB_PASSWORD:-}"
  BUSINESS_DB_USERNAME="${BUSINESS_DB_USERNAME:-qianruo_business}"
  BUSINESS_DB_PASSWORD="${BUSINESS_DB_PASSWORD:-}"
  NACOS_AUTH_TOKEN="${NACOS_AUTH_TOKEN:-}"
  NACOS_AUTH_IDENTITY_KEY="${NACOS_AUTH_IDENTITY_KEY:-}"
  NACOS_AUTH_IDENTITY_VALUE="${NACOS_AUTH_IDENTITY_VALUE:-}"
  MYSQL_IMAGE="${MYSQL_IMAGE:-mysql:8.4}"
  REDIS_IMAGE="${REDIS_IMAGE:-m.daocloud.io/docker.io/library/redis:7.4}"
  NACOS_IMAGE="${NACOS_IMAGE:-nacos/nacos-server:v3.0.2}"
  PUSH_IMAGES=false
  APPLY_MANIFESTS=false
  export REGISTRY IMAGE_NAMESPACE MYSQL_ROOT_PASSWORD PLATFORM_DB_USERNAME PLATFORM_DB_PASSWORD
  export MERCHANT_DB_USERNAME MERCHANT_DB_PASSWORD BUSINESS_DB_USERNAME BUSINESS_DB_PASSWORD
  export NACOS_AUTH_TOKEN NACOS_AUTH_IDENTITY_KEY NACOS_AUTH_IDENTITY_VALUE
  export MYSQL_IMAGE REDIS_IMAGE NACOS_IMAGE
  export FILE_STORAGE_PROVIDER
  require_secret_vars
  ensure_local_nacos_token
}

ensure_local_nacos_token() {
  local decoded_len
  decoded_len="$(python3 - "$NACOS_AUTH_TOKEN" <<'PY'
import base64
import sys

try:
    raw = base64.b64decode(sys.argv[1], validate=True)
except Exception:
    print(0)
else:
    print(len(raw))
PY
)"
  if [[ "$decoded_len" -lt 32 ]]; then
    echo "NACOS_AUTH_TOKEN must be Base64 encoding at least 32 bytes; configure it in deploy/.env." >&2
    exit 1
  fi
}

image_ref() {
  local image_name="$1"
  printf '%s/%s/%s:%s' "${REGISTRY%/}" "$IMAGE_NAMESPACE" "$image_name" "$IMAGE_TAG"
}

build_backend_artifacts() {
  [[ "$BUILD_BACKEND" == "true" ]] || return 0
  need_cmd mvn
  log "Packaging backend services"
  # Always clean first: incremental Maven output can leave newly added common classes
  # out of a dependent service JAR while still producing a seemingly valid image.
  mvn -f "$ROOT_DIR/backend/ruoyi-cloud/pom.xml" clean package
}

# 校验指定服务的 JAR 文件已经完成打包，避免 jar 命令对不存在的文件输出无关的 Java 异常堆栈。
require_backend_jar() {
  local service_name="$1"
  local jar_path="$2"

  if [[ ! -f "$jar_path" ]]; then
    echo "$service_name JAR is missing: ${jar_path#"$ROOT_DIR/"}. Run ./scripts/deploy.sh build after backend packaging succeeds." >&2
    exit 1
  fi
}

# 校验平台端、商户端和网关的关键类与资源均已写入最终 JAR，防止部署不完整的服务镜像。
verify_backend_artifacts() {
  local merchant_jar="$ROOT_DIR/backend/ruoyi-cloud/ruoyi-modules/ruoyi-merchant-service/target/ruoyi-merchant-service.jar"
  local business_jar="$ROOT_DIR/backend/ruoyi-cloud/ruoyi-modules/ruoyi-rental-business/target/ruoyi-rental-business.jar"
  local platform_jar="$ROOT_DIR/backend/ruoyi-cloud/ruoyi-modules/ruoyi-platform-service/target/ruoyi-platform-service.jar"
  local gateway_jar="$ROOT_DIR/backend/ruoyi-cloud/ruoyi-gateway/target/ruoyi-gateway.jar"
  need_cmd jar

  require_backend_jar "Platform service" "$platform_jar"
  require_backend_jar "Merchant service" "$merchant_jar"
  require_backend_jar "Rental business service" "$business_jar"
  require_backend_jar "Gateway" "$gateway_jar"

  if ! jar tf "$platform_jar" | grep 'com/ruoyi/system/PlatformServiceApplication.class' >/dev/null; then
    echo "Platform service JAR does not contain PlatformServiceApplication; refusing to deploy an incomplete image." >&2
    exit 1
  fi
  if ! jar tf "$platform_jar" | grep 'BOOT-INF/classes/mapper/system/SysConfigMapper.xml' >/dev/null; then
    echo "Platform service JAR does not contain MyBatis mapper XML files; refusing to deploy an incomplete image." >&2
    exit 1
  fi
  if ! jar tf "$merchant_jar" | grep 'com/ruoyi/merchant/controller/MerchantUserController.class' >/dev/null; then
    echo "Merchant service JAR does not contain MerchantUserController; refusing to deploy an incomplete image." >&2
    exit 1
  fi
  if ! jar tf "$merchant_jar" | grep 'BOOT-INF/classes/mapper/merchant/MerchantRoleMapper.xml' >/dev/null; then
    echo "Merchant service JAR does not contain its role mapper XML; refusing to deploy an incomplete image." >&2
    exit 1
  fi
  if ! jar tf "$merchant_jar" | grep 'BOOT-INF/classes/com/ruoyi/merchant/model/MerchantRoleVO.class' >/dev/null; then
    echo "Merchant service JAR does not contain MerchantRoleVO; refusing to deploy an incomplete image." >&2
    exit 1
  fi
  if ! jar tf "$business_jar" | grep 'com/ruoyi/rentalbusiness/RentalBusinessApplication.class' >/dev/null; then
    echo "Rental business JAR does not contain RentalBusinessApplication; refusing to deploy an incomplete image." >&2
    exit 1
  fi
  if ! jar tf "$gateway_jar" | grep 'com/ruoyi/gateway/config/MerchantRouteConfiguration.class' >/dev/null; then
    echo "Gateway JAR does not contain the fixed /merchant/** route; refusing to deploy an incomplete image." >&2
    exit 1
  fi
  if ! jar tf "$gateway_jar" | grep 'com/ruoyi/gateway/config/PlatformRouteConfiguration.class' >/dev/null; then
    echo "Gateway JAR does not contain the fixed /admin/** route; refusing to deploy an incomplete image." >&2
    exit 1
  fi
}

install_frontend_deps() {
  local app_dir="$1"
  if [[ ! -d "$app_dir/node_modules" ]]; then
    npm --prefix "$app_dir" install --package-lock=false
  fi
}

build_frontend_artifacts() {
  [[ "$BUILD_FRONTEND" == "true" ]] || return 0
  need_cmd npm
  log "Building admin frontend"
  install_frontend_deps "$ROOT_DIR/frontend/admin-web"
  npm --prefix "$ROOT_DIR/frontend/admin-web" run build:prod

  log "Building merchant frontend"
  install_frontend_deps "$ROOT_DIR/frontend/merchant-web"
  npm --prefix "$ROOT_DIR/frontend/merchant-web" run build:prod
}

build_images() {
  need_cmd docker
  require_registry
  verify_backend_artifacts
  log "Building backend Docker images"
  for item in "${BACKEND_IMAGES[@]}"; do
    IFS=: read -r image_name jar_file app_name <<<"$item"
    if [[ ! -f "$ROOT_DIR/$jar_file" ]]; then
      echo "Missing JAR: $jar_file. Run ./scripts/deploy.sh build after backend packaging succeeds." >&2
      exit 1
    fi
    docker build ${IMAGE_PLATFORM:+--platform "$IMAGE_PLATFORM"} \
      -f "$ROOT_DIR/deploy/docker/backend.Dockerfile" \
      --build-arg "JAVA_BASE_IMAGE=$JAVA_BASE_IMAGE" \
      --build-arg "JAR_FILE=$jar_file" \
      --build-arg "APP_NAME=$app_name" \
      -t "$(image_ref "$image_name")" \
      "$ROOT_DIR"
  done

  log "Building frontend Docker images"
  for item in "${FRONTEND_IMAGES[@]}"; do
    IFS=: read -r image_name dist_dir nginx_conf <<<"$item"
    if [[ ! -d "$ROOT_DIR/$dist_dir" ]]; then
      echo "Missing dist directory: $dist_dir. Run frontend build first." >&2
      exit 1
    fi
    docker build ${IMAGE_PLATFORM:+--platform "$IMAGE_PLATFORM"} \
      -f "$ROOT_DIR/deploy/docker/frontend.Dockerfile" \
      --build-arg "NGINX_BASE_IMAGE=$NGINX_BASE_IMAGE" \
      --build-arg "DIST_DIR=$dist_dir" \
      --build-arg "NGINX_CONF=$nginx_conf" \
      -t "$(image_ref "$image_name")" \
      "$ROOT_DIR"
  done
}

build_docker_images() {
  [[ "$BUILD_IMAGES" == "true" ]] || return 0
  build_images
}

push_images() {
  [[ "$PUSH_IMAGES" == "true" ]] || return 0
  need_cmd docker
  require_registry
  log "Pushing Docker images"
  for item in "${BACKEND_IMAGES[@]}" "${FRONTEND_IMAGES[@]}"; do
    IFS=: read -r image_name _ <<<"$item"
    docker push "$(image_ref "$image_name")"
  done
}

render_manifests() {
  require_registry
  require_secret_vars
  need_cmd python3
  log "Rendering Kubernetes manifests"
  python3 "$ROOT_DIR/scripts/render_template.py" \
    "$ROOT_DIR/deploy/k8s/qianruo.template.yaml" \
    "$GENERATED_MANIFEST"

  if [[ -n "$ADMIN_HOST" && -n "$MERCHANT_HOST" ]]; then
    python3 "$ROOT_DIR/scripts/render_template.py" \
      "$ROOT_DIR/deploy/k8s/ingress.template.yaml" \
      "$GENERATED_INGRESS"
  else
    rm -f "$GENERATED_INGRESS"
  fi
}

use_kube_context() {
  if [[ -n "$KUBE_CONTEXT" ]]; then
    kubectl config use-context "$KUBE_CONTEXT"
  fi
}

apply_manifests() {
  [[ "$APPLY_MANIFESTS" == "true" ]] || return 0
  need_cmd kubectl
  use_kube_context
  render_manifests
  log "Applying Kubernetes manifests to namespace $K8S_NAMESPACE"
  kubectl create namespace "$K8S_NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -
  kubectl -n "$K8S_NAMESPACE" create configmap qianruo-db-access-script \
    --from-file=provision-service-db-users.sh="$ROOT_DIR/deploy/scripts/provision-service-db-users.sh" \
    --dry-run=client -o yaml | kubectl apply -f -
  kubectl -n "$K8S_NAMESPACE" create configmap qianruo-operations-scripts \
    --from-file=platform-database-backup.sh="$ROOT_DIR/deploy/scripts/platform-database-backup.sh" \
    --from-file=platform-monthly-compliance-package.sh="$ROOT_DIR/deploy/scripts/platform-monthly-compliance-package.sh" \
    --dry-run=client -o yaml | kubectl apply -f -
  kubectl -n "$K8S_NAMESPACE" delete job qianruo-db-access --ignore-not-found
  kubectl apply -f "$GENERATED_MANIFEST"
  kubectl -n "$K8S_NAMESPACE" wait --for=condition=complete job/qianruo-db-access --timeout=180s
  kubectl -n "$K8S_NAMESPACE" rollout restart deployment/platform-service deployment/merchant-service deployment/rental-business
  # The frontend Nginx config is mounted through subPath, so ConfigMap changes
  # are not picked up until the frontend Pods are recreated.
  kubectl -n "$K8S_NAMESPACE" rollout restart deployment/rental-admin-web deployment/rental-merchant-web
  if [[ -f "$GENERATED_INGRESS" ]]; then
    kubectl apply -f "$GENERATED_INGRESS"
  fi
  kubectl -n "$K8S_NAMESPACE" delete deployment,service ruoyi-system --ignore-not-found
}

wait_deployment() {
  local deployment="$1"
  kubectl -n "$K8S_NAMESPACE" rollout status "deployment/$deployment" --timeout=180s
}

run_sql_file() {
  local sql_file="$1"
  local database="${2:-}"
  if [[ ! -f "$ROOT_DIR/$sql_file" ]]; then
    echo "Missing SQL file: $sql_file" >&2
    exit 1
  fi

  log "Running SQL: $sql_file"
  # 通过 Pod 内环境变量向 MySQL 客户端传递密码，避免密码出现在进程参数中并消除安全警告。
  if [[ -n "$database" ]]; then
    kubectl -n "$K8S_NAMESPACE" exec -i deployment/ruoyi-mysql -- \
      sh -c "MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" mysql -uroot --default-character-set=utf8mb4 --database='$database' >/dev/null" < "$ROOT_DIR/$sql_file"
  else
    kubectl -n "$K8S_NAMESPACE" exec -i deployment/ruoyi-mysql -- \
      sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot --default-character-set=utf8mb4 >/dev/null' < "$ROOT_DIR/$sql_file"
  fi
}

migration_checksum() {
  local sql_file="$1"
  if command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "$ROOT_DIR/$sql_file" | awk '{print $1}'
    return 0
  fi
  sha256sum "$ROOT_DIR/$sql_file" | awk '{print $1}'
}

migration_version() {
  basename "$1" .sql
}

migration_database() {
  local sql_file="$1"
  case "$(migration_version "$sql_file")" in
    V20261019_002__consolidated_project_baseline)
      printf '%s' "ry_rental"
      ;;
  esac
}

ensure_migration_history_table() {
  kubectl -n "$K8S_NAMESPACE" exec -i deployment/ruoyi-mysql -- \
    sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot --default-character-set=utf8mb4 --database=ry-config >/dev/null' <<'SQL'
CREATE TABLE IF NOT EXISTS `qianruo_schema_migration` (
    `migration_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '迁移记录唯一编号',
    `version` VARCHAR(128) NOT NULL COMMENT '迁移版本，使用SQL文件名（不含扩展名）',
    `checksum` CHAR(64) NOT NULL COMMENT '迁移脚本SHA-256',
    `applied_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行完成时间',
    PRIMARY KEY (`migration_id`),
    UNIQUE KEY `uk_qianruo_schema_migration_version` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='QianRuo数据库迁移执行记录';
SQL
}

migration_recorded_checksum() {
  local version="$1"
  kubectl -n "$K8S_NAMESPACE" exec -i deployment/ruoyi-mysql -- \
    sh -c "MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" mysql -uroot --batch --skip-column-names --default-character-set=utf8mb4 --database='$MIGRATION_HISTORY_DATABASE' -e \"SELECT checksum FROM $MIGRATION_HISTORY_TABLE WHERE version = '$version' LIMIT 1\""
}

record_migration() {
  local version="$1"
  local checksum="$2"
  kubectl -n "$K8S_NAMESPACE" exec -i deployment/ruoyi-mysql -- \
      sh -c "MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" mysql -uroot --default-character-set=utf8mb4 --database='$MIGRATION_HISTORY_DATABASE' -e \"INSERT INTO $MIGRATION_HISTORY_TABLE (version, checksum, applied_time) VALUES ('$version', '$checksum', NOW())\""
}

migration_history_count() {
  kubectl -n "$K8S_NAMESPACE" exec -i deployment/ruoyi-mysql -- \
    sh -c "MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" mysql -uroot --batch --skip-column-names --default-character-set=utf8mb4 --database='$MIGRATION_HISTORY_DATABASE' -e \"SELECT COUNT(*) FROM $MIGRATION_HISTORY_TABLE\""
}

migration_history_latest() {
  kubectl -n "$K8S_NAMESPACE" exec -i deployment/ruoyi-mysql -- \
    sh -c "MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" mysql -uroot --batch --skip-column-names --default-character-set=utf8mb4 --database='$MIGRATION_HISTORY_DATABASE' -e \"SELECT CONCAT(version, '|', checksum) FROM $MIGRATION_HISTORY_TABLE ORDER BY migration_id DESC LIMIT 1\""
}

ensure_nacos_database() {
  local database_exists
  database_exists="$(kubectl -n "$K8S_NAMESPACE" exec -i deployment/ruoyi-mysql -- \
    sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot --batch --skip-column-names --default-character-set=utf8mb4 --database=information_schema -e "SELECT COUNT(*) FROM schemata WHERE schema_name = '\''ry-config'\''"')"
  if [[ "$database_exists" == "1" ]]; then
    return 0
  fi

  log "Initializing missing Nacos database: ry-config"
  run_sql_file "backend/ruoyi-cloud/sql/ry_config_20260311.sql"
}

run_migration_file() {
  local sql_file="$1"
  local version
  local checksum
  local recorded_checksum

  version="$(migration_version "$sql_file")"
  checksum="$(migration_checksum "$sql_file")"
  recorded_checksum="$(migration_recorded_checksum "$version")"
  if [[ -n "$recorded_checksum" ]]; then
    if [[ "$recorded_checksum" != "$checksum" ]]; then
      echo "Migration checksum changed after execution: $sql_file. Create a new migration version instead of editing an applied file." >&2
      exit 1
    fi
    log "Skipping applied migration: $sql_file"
    return 0
  fi

  local database
  database="$(migration_database "$sql_file")"
  if [[ -n "$database" ]]; then
    run_sql_file "$sql_file" "$database"
  else
    run_sql_file "$sql_file"
  fi
  record_migration "$version" "$checksum"
}

apply_configured_migrations() {
  local migration
  local baseline_version
  local baseline_checksum
  local recorded_checksum
  local previous_record
  local history_count
  local fresh_init="${1:-false}"

  ensure_migration_history_table
  migration="${DATABASE_MIGRATIONS[0]}"
  baseline_version="$(migration_version "$migration")"
  baseline_checksum="$(migration_checksum "$migration")"
  recorded_checksum="$(migration_recorded_checksum "$baseline_version")"
  if [[ -z "$recorded_checksum" ]]; then
    history_count="$(migration_history_count)"
    if [[ "$history_count" == "0" ]]; then
      if [[ "$fresh_init" != "true" ]]; then
        echo "No migration history found. Run init-db for a fresh database; older database states are unsupported." >&2
        exit 1
      fi
    else
      previous_record="$(migration_history_latest)"
      if [[ "$previous_record" != "$PRE_CONSOLIDATION_VERSION|$PRE_CONSOLIDATION_CHECKSUM" ]]; then
        echo "Database is not at the consolidated migration cutover version ($PRE_CONSOLIDATION_VERSION). Upgrade it before using this release." >&2
        exit 1
      fi
      record_migration "$baseline_version" "$baseline_checksum"
      log "Recorded the consolidated baseline without replaying historical SQL"
    fi
  fi

  for migration in "${DATABASE_MIGRATIONS[@]}"; do
    run_migration_file "$migration"
  done
}

init_db() {
  need_cmd kubectl
  use_kube_context
  if [[ "${CONFIRM_INIT_DB:-}" != "yes" ]]; then
    echo "Database initialization may recreate tables. Re-run with CONFIRM_INIT_DB=yes when you are ready." >&2
    exit 1
  fi
  log "Waiting for MySQL"
  wait_deployment ruoyi-mysql
  run_sql_file "backend/ruoyi-cloud/sql/ry_config_20260311.sql"
  run_sql_file "backend/ruoyi-cloud/sql/ry_20260321.sql" "ry-cloud"
  run_sql_file "database/mysql/00_create_database.sql"
  run_sql_file "database/mysql/01_rental_schema.sql" "ry_rental"
  run_sql_file "database/seed/01_rental_dict_seed.sql" "ry-cloud"
  run_sql_file "database/seed/02_rental_utf8_repair.sql" "ry-cloud"
  apply_configured_migrations true
}

migrate_db() {
  need_cmd kubectl
  use_kube_context
  log "Waiting for MySQL before migrations"
  wait_deployment ruoyi-mysql
  ensure_nacos_database
  apply_configured_migrations
  log "Reloading Nacos, gateway, platform and scheduled-job configuration"
  kubectl -n "$K8S_NAMESPACE" rollout restart deployment/ruoyi-nacos deployment/ruoyi-gateway deployment/platform-service deployment/rental-business deployment/ruoyi-job
  wait_deployment ruoyi-nacos
  wait_deployment ruoyi-gateway
  wait_deployment platform-service
  wait_deployment rental-business
  wait_deployment ruoyi-job
}

show_status() {
  need_cmd kubectl
  use_kube_context
  kubectl -n "$K8S_NAMESPACE" get deploy,svc,pod,ingress
}

compose() {
  need_cmd docker
  docker compose --project-name "$LOCAL_COMPOSE_PROJECT" -f "$LOCAL_COMPOSE_FILE" "$@"
}

local_mysql_exec() {
  need_cmd docker
  docker exec -i qianruo-mysql "$@"
}

local_base_images() {
  printf '%s=%s\n' \
    "JAVA_BASE_IMAGE" "$JAVA_BASE_IMAGE" \
    "NGINX_BASE_IMAGE" "$NGINX_BASE_IMAGE" \
    "MYSQL_IMAGE" "$MYSQL_IMAGE" \
    "REDIS_IMAGE" "$REDIS_IMAGE" \
    "NACOS_IMAGE" "$NACOS_IMAGE"
}

local_pull_base_images() {
  prepare_local_env
  need_cmd docker
  log "Pulling local base images"
  local failed=0
  while IFS='=' read -r image_var image; do
    [[ -n "$image" ]] || continue
    if docker image inspect "$image" >/dev/null 2>&1; then
      echo "Already exists: $image"
      continue
    fi
    echo "Pulling: $image"
    if ! docker pull "$image"; then
      failed=1
      echo "Failed to pull: $image" >&2
      echo "Set $image_var to a reachable mirror image in deploy/.env." >&2
    fi
  done < <(local_base_images)

  if [[ "$failed" -ne 0 ]]; then
    cat >&2 <<EOF

Base image pull failed. This is usually a Docker Hub network/auth issue.
You can fix it in one of these ways:
1. Run: docker login
2. Configure Docker Desktop registry mirrors.
3. Put reachable images in deploy/.env:
   JAVA_BASE_IMAGE=mcr.microsoft.com/openjdk/jdk:17-ubuntu
   NGINX_BASE_IMAGE=m.daocloud.io/docker.io/library/nginx:1.27
   MYSQL_IMAGE=your-registry/mysql:8.4
   REDIS_IMAGE=m.daocloud.io/docker.io/library/redis:7.4
   NACOS_IMAGE=your-registry/nacos/nacos-server:v3.0.2
EOF
    exit 1
  fi
}

local_up() {
  prepare_local_env
  local_pull_base_images
  build_backend_artifacts
  build_frontend_artifacts
  build_docker_images
  log "Starting local infrastructure"
  compose up -d ruoyi-mysql ruoyi-redis
  if [[ "$INIT_DB" == "true" ]]; then
    local_init_db
  fi
  if [[ "$MIGRATE_DB" == "true" ]]; then
    local_migrate_db
  fi
  log "Provisioning isolated database users"
  local_mysql_exec /bin/sh /usr/local/bin/provision-service-db-users.sh
  if [[ "$INIT_DB" == "true" || "$MIGRATE_DB" == "true" ]]; then
    log "Restarting local Nacos to load migrated configuration"
    compose up -d --force-recreate ruoyi-nacos
  else
    compose up -d ruoyi-nacos
  fi
  local_wait_nacos

  # 分批启动：先起网关和认证服务，避免 9 个 JVM 同时冷启动争抢 CPU 导致 nacos 注册超时崩溃
  log "Starting local gateway and authentication services"
  compose up -d --force-recreate --remove-orphans ruoyi-gateway ruoyi-auth
  local_wait_auth_service

  log "Starting remaining local application services"
  compose up -d --force-recreate --remove-orphans platform-service merchant-service rental-business ruoyi-gen ruoyi-job ruoyi-file ruoyi-monitor rental-admin-web rental-merchant-web
  local_wait_platform_service
  local_wait_merchant_service
  local_wait_rental_business_service
  local_wait_job_service
  local_wait_file_service
  local_verify_merchant_routes
  local_wait_admin_api
  compose ps
  log "Admin web: http://localhost:8088"
  log "Merchant web: http://localhost:8089"
  log "Gateway: http://localhost:8080"
  log "Nacos console: http://localhost:18080/"
  log "Nacos API: http://localhost:8848"
}

local_reset() {
  prepare_local_env
  log "Resetting local Docker stack and volumes"
  compose down -v
  INIT_DB=true
  CONFIRM_INIT_DB=yes
  export INIT_DB CONFIRM_INIT_DB
  local_up
}

local_run_sql_file() {
  local sql_file="$1"
  local database="${2:-}"
  if [[ ! -f "$ROOT_DIR/$sql_file" ]]; then
    echo "Missing SQL file: $sql_file" >&2
    exit 1
  fi

  log "Running local SQL: $sql_file"
  # 通过容器内环境变量向 MySQL 客户端传递密码，避免密码出现在进程参数中并消除安全警告。
  if [[ -n "$database" ]]; then
    local_mysql_exec \
      sh -c "MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" mysql -uroot --default-character-set=utf8mb4 --database='$database' >/dev/null" < "$ROOT_DIR/$sql_file"
  else
    local_mysql_exec \
      sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot --default-character-set=utf8mb4 >/dev/null' < "$ROOT_DIR/$sql_file"
  fi
}

local_ensure_migration_history_table() {
  local_mysql_exec \
    sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot --default-character-set=utf8mb4 --database=ry-config >/dev/null' <<'SQL'
CREATE TABLE IF NOT EXISTS `qianruo_schema_migration` (
    `migration_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '迁移记录唯一编号',
    `version` VARCHAR(128) NOT NULL COMMENT '迁移版本，使用SQL文件名（不含扩展名）',
    `checksum` CHAR(64) NOT NULL COMMENT '迁移脚本SHA-256',
    `applied_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行完成时间',
    PRIMARY KEY (`migration_id`),
    UNIQUE KEY `uk_qianruo_schema_migration_version` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='QianRuo数据库迁移执行记录';
SQL
}

local_migration_recorded_checksum() {
  local version="$1"
  local_mysql_exec \
    sh -c "MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" mysql -uroot --batch --skip-column-names --default-character-set=utf8mb4 --database='$MIGRATION_HISTORY_DATABASE' -e \"SELECT checksum FROM $MIGRATION_HISTORY_TABLE WHERE version = '$version' LIMIT 1\""
}

local_record_migration() {
  local version="$1"
  local checksum="$2"
  local_mysql_exec \
      sh -c "MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" mysql -uroot --default-character-set=utf8mb4 --database='$MIGRATION_HISTORY_DATABASE' -e \"INSERT INTO $MIGRATION_HISTORY_TABLE (version, checksum, applied_time) VALUES ('$version', '$checksum', NOW())\""
}

local_migration_history_count() {
  local_mysql_exec \
    sh -c "MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" mysql -uroot --batch --skip-column-names --default-character-set=utf8mb4 --database='$MIGRATION_HISTORY_DATABASE' -e \"SELECT COUNT(*) FROM $MIGRATION_HISTORY_TABLE\""
}

local_migration_history_latest() {
  local_mysql_exec \
    sh -c "MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" mysql -uroot --batch --skip-column-names --default-character-set=utf8mb4 --database='$MIGRATION_HISTORY_DATABASE' -e \"SELECT CONCAT(version, '|', checksum) FROM $MIGRATION_HISTORY_TABLE ORDER BY migration_id DESC LIMIT 1\""
}

local_run_migration_file() {
  local sql_file="$1"
  local version
  local checksum
  local recorded_checksum

  version="$(migration_version "$sql_file")"
  checksum="$(migration_checksum "$sql_file")"
  recorded_checksum="$(local_migration_recorded_checksum "$version")"
  if [[ -n "$recorded_checksum" ]]; then
    if [[ "$recorded_checksum" != "$checksum" ]]; then
      echo "Migration checksum changed after execution: $sql_file. Create a new migration version instead of editing an applied file." >&2
      exit 1
    fi
    log "Skipping applied migration: $sql_file"
    return 0
  fi

  local database
  database="$(migration_database "$sql_file")"
  if [[ -n "$database" ]]; then
    local_run_sql_file "$sql_file" "$database"
  else
    local_run_sql_file "$sql_file"
  fi
  local_record_migration "$version" "$checksum"
}

local_apply_configured_migrations() {
  local migration
  local baseline_version
  local baseline_checksum
  local recorded_checksum
  local previous_record
  local history_count
  local fresh_init="${1:-false}"

  local_ensure_migration_history_table
  migration="${DATABASE_MIGRATIONS[0]}"
  baseline_version="$(migration_version "$migration")"
  baseline_checksum="$(migration_checksum "$migration")"
  recorded_checksum="$(local_migration_recorded_checksum "$baseline_version")"
  if [[ -z "$recorded_checksum" ]]; then
    history_count="$(local_migration_history_count)"
    if [[ "$history_count" == "0" ]]; then
      if [[ "$fresh_init" != "true" ]]; then
        echo "No migration history found. Run local-init-db for a fresh database; older database states are unsupported." >&2
        exit 1
      fi
    else
      previous_record="$(local_migration_history_latest)"
      if [[ "$previous_record" != "$PRE_CONSOLIDATION_VERSION|$PRE_CONSOLIDATION_CHECKSUM" ]]; then
        echo "Local database is not at the consolidated migration cutover version ($PRE_CONSOLIDATION_VERSION). Upgrade it before using this release." >&2
        exit 1
      fi
      local_record_migration "$baseline_version" "$baseline_checksum"
      log "Recorded the consolidated baseline without replaying historical SQL"
    fi
  fi

  for migration in "${DATABASE_MIGRATIONS[@]}"; do
    local_run_migration_file "$migration"
  done
}

local_init_db() {
  prepare_local_env
  if [[ "${CONFIRM_INIT_DB:-}" != "yes" ]]; then
    echo "Database initialization may recreate tables. Re-run with CONFIRM_INIT_DB=yes when you are ready." >&2
    exit 1
  fi
  log "Waiting for local MySQL"
  for _ in {1..60}; do
    if local_mysql_exec sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot -e "SELECT 1" >/dev/null' >/dev/null 2>&1; then
      break
    fi
    sleep 2
  done
  if ! local_mysql_exec sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot -e "SELECT 1" >/dev/null' >/dev/null 2>&1; then
    echo "Timed out waiting for local MySQL to accept root password." >&2
    docker logs qianruo-mysql --tail 80 >&2 || true
    exit 1
  fi
  local_run_sql_file "backend/ruoyi-cloud/sql/ry_config_20260311.sql"
  local_run_sql_file "backend/ruoyi-cloud/sql/ry_20260321.sql" "ry-cloud"
  local_run_sql_file "database/mysql/00_create_database.sql"
  local_run_sql_file "database/mysql/01_rental_schema.sql" "ry_rental"
  local_run_sql_file "database/seed/01_rental_dict_seed.sql" "ry-cloud"
  local_run_sql_file "database/seed/02_rental_utf8_repair.sql" "ry-cloud"
  local_apply_configured_migrations true
}

local_migrate_db() {
  prepare_local_env
  log "Waiting for local MySQL before migrations"
  for _ in {1..60}; do
    if local_mysql_exec sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot -e "SELECT 1" >/dev/null' >/dev/null 2>&1; then
      local_apply_configured_migrations
      local_prepare_local_nacos_config
      return 0
    fi
    sleep 2
  done
  echo "Timed out waiting for local MySQL before migrations." >&2
  exit 1
}

local_prepare_local_nacos_config() {
  # 本地 Docker 网络中的服务必须通过 Compose 服务名互相访问，不能继续使用宿主机开发环境遗留的 localhost 地址。
  # 这里在 Nacos 启动前统一修正配置中心中的 Redis 和 MySQL 地址，并重新计算 Nacos 配置摘要，
  # 确保 Nacos 能够识别配置发生变化，避免应用容器把 localhost 解析为自身而导致启动或缓存初始化阻塞。
  log "Normalizing local Nacos service endpoints"
  local_mysql_exec \
    sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot --default-character-set=utf8mb4 --database=ry-config >/dev/null' <<'SQL'
UPDATE config_info
SET content = REPLACE(
        REPLACE(content, 'host: localhost', 'host: ruoyi-redis'),
        'jdbc:mysql://localhost:3306/',
        'jdbc:mysql://ruoyi-mysql:3306/'
    )
WHERE content LIKE '%host: localhost%'
   OR content LIKE '%jdbc:mysql://localhost:3306/%';

UPDATE config_info
SET md5 = MD5(content),
    gmt_modified = NOW()
WHERE content LIKE '%ruoyi-redis%'
   OR content LIKE '%jdbc:mysql://ruoyi-mysql:3306/%';
SQL
}

local_reload_gateway_config() {
  if docker inspect qianruo-nacos --format '{{.State.Running}}' 2>/dev/null | grep -q true; then
    log "Reloading local Nacos configuration"
    compose restart ruoyi-nacos
    local_wait_nacos
  fi
  if docker inspect qianruo-gateway --format '{{.State.Running}}' 2>/dev/null | grep -q true; then
    log "Reloading local gateway configuration"
    compose restart ruoyi-gateway
  fi
  if docker inspect qianruo-platform-service --format '{{.State.Running}}' 2>/dev/null | grep -q true; then
    log "Reloading local platform configuration"
    compose restart platform-service
    local_wait_platform_service
  fi
  if docker inspect qianruo-merchant-service --format '{{.State.Running}}' 2>/dev/null | grep -q true; then
    log "Reloading local merchant configuration"
    compose restart merchant-service
    local_wait_merchant_service
  fi
  if docker inspect qianruo-rental-business --format '{{.State.Running}}' 2>/dev/null | grep -q true; then
    log "Reloading local rental business configuration"
    compose restart rental-business
    local_wait_rental_business_service
  fi
  if docker inspect qianruo-job --format '{{.State.Running}}' 2>/dev/null | grep -q true; then
    log "Reloading local scheduled-job configuration"
    compose restart ruoyi-job
    local_wait_job_service
  fi
  if docker inspect qianruo-gateway --format '{{.State.Running}}' 2>/dev/null | grep -q true; then
    local_verify_merchant_routes
  fi
}

local_wait_nacos() {
  need_cmd curl
  log "Waiting for local Nacos"
  for _ in {1..240}; do
    if curl --fail --silent --max-time 3 \
      http://127.0.0.1:18080/v3/console/health/readiness 2>/dev/null \
      | grep -Eq '"data"[[:space:]]*:[[:space:]]*"ok"'; then
      if python3 - <<'PY' >/dev/null 2>&1
import socket

for port in (8848, 9848):
    with socket.create_connection(("127.0.0.1", port), timeout=2):
        pass
PY
      then
        return 0
      fi
    fi
    if ! docker inspect qianruo-nacos --format '{{.State.Running}}' 2>/dev/null | grep -q true; then
      echo "Nacos is not running. Recent logs:" >&2
      docker logs qianruo-nacos --tail 80 >&2 || true
      echo "If this is your first local run, use: INIT_DB=true CONFIRM_INIT_DB=yes ./scripts/deploy.sh local" >&2
      exit 1
    fi
    sleep 2
  done
  echo "Timed out waiting for Nacos. Recent logs:" >&2
  docker logs qianruo-nacos --tail 80 >&2 || true
  exit 1
}

local_wait_admin_api() {
  need_cmd curl
  log "Waiting for local admin API"
  for _ in {1..240}; do
    if curl --fail --silent --max-time 3 http://127.0.0.1:8088/prod-api/code >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  echo "Timed out waiting for local admin API. Recent logs:" >&2
  docker logs qianruo-admin-web --tail 80 >&2 || true
  docker logs qianruo-gateway --tail 120 >&2 || true
  exit 1
}

local_wait_platform_service() {
  local_wait_application_service \
    "platform service" "qianruo-platform-service" "http://127.0.0.1:9201/actuator/health" \
    "http://qianruo-platform-service:9201/actuator/health"
}

local_wait_auth_service() {
  local_wait_application_service \
    "authentication service" "qianruo-auth" "http://127.0.0.1:9200/actuator/health" \
    "http://qianruo-auth:9200/actuator/health"
}

local_wait_merchant_service() {
  local_wait_application_service \
    "merchant service" "qianruo-merchant-service" "http://127.0.0.1:9210/actuator/health" \
    "http://qianruo-merchant-service:9210/actuator/health"
}

local_wait_rental_business_service() {
  local_wait_application_service \
    "rental business service" "qianruo-rental-business" "http://127.0.0.1:9220/actuator/health" \
    "http://qianruo-rental-business:9220/actuator/health"
}

local_wait_job_service() {
  local_wait_application_service \
    "scheduled job service" "qianruo-job" "http://127.0.0.1:9203/actuator/health"
}

local_wait_file_service() {
  local_wait_application_service \
    "file service" "qianruo-file" "http://127.0.0.1:9300/actuator/health"
}

local_wait_application_service() {
  need_cmd curl
  local service_name="$1"
  local container_name="$2"
  local health_url="$3"
  local internal_health_url="${4:-}"

  log "Waiting for local $service_name"
  for _ in {1..360}; do
    if curl --silent --max-time 3 "$health_url" 2>/dev/null \
      | grep '"status":"UP"' >/dev/null; then
      return 0
    fi
    # Docker Desktop may reset host-loopback forwarding for a JVM bound to IPv6.
    # Use the Compose network as a fallback so verification reflects service health,
    # rather than the host-side port forwarding implementation.
    if [[ -n "$internal_health_url" ]] \
      && docker run --rm --pull=never --network qianruo-local --entrypoint /usr/bin/curl \
        "$NGINX_BASE_IMAGE" --fail --silent --max-time 3 "$internal_health_url" 2>/dev/null \
        | grep '"status":"UP"' >/dev/null; then
      return 0
    fi
    if ! docker inspect "$container_name" --format '{{.State.Running}}' 2>/dev/null | grep -q true; then
      echo "$service_name is not running. Recent logs:" >&2
      docker logs "$container_name" --tail 160 >&2 || true
      exit 1
    fi
    sleep 2
  done
  echo "Timed out waiting for $service_name. Recent logs:" >&2
  docker logs "$container_name" --tail 200 >&2 || true
  exit 1
}

local_verify_merchant_routes() {
  need_cmd curl
  local direct_url="http://127.0.0.1:9210/merchant/user/getInfo"
  local gateway_url="http://127.0.0.1:8080/merchant/user/getInfo"
  local response_file
  response_file="$(mktemp)"

  log "Verifying merchant user routes"
  for _ in {1..30}; do
    curl --silent --show-error --max-time 3 \
      -H 'X-Client-Type: platform' "$direct_url" -o "$response_file" || true
    if grep -q '"code":403' "$response_file"; then
      break
    fi
    sleep 2
  done

  if ! grep -q '"code":403' "$response_file"; then
    rm -f "$response_file"
    echo "Merchant service namespace boundary is not active. Recent logs:" >&2
    docker logs qianruo-merchant-service --tail 160 >&2 || true
    exit 1
  fi

  for _ in {1..30}; do
    : > "$response_file"
    curl --silent --show-error --max-time 5 "$gateway_url" -o "$response_file" || true
    if grep -q '"code":401' "$response_file"; then
      break
    fi
    sleep 2
  done
  if ! grep -q '"code":401' "$response_file"; then
    rm -f "$response_file"
    echo "Gateway merchant namespace authentication is not active. Recent logs:" >&2
    docker logs qianruo-gateway --tail 160 >&2 || true
    exit 1
  fi
  rm -f "$response_file"
}

cmd="${1:-all}"
case "$cmd" in
  all)
    build_backend_artifacts
    build_frontend_artifacts
    build_docker_images
    push_images
    apply_manifests
    if [[ "$INIT_DB" == "true" ]]; then
      init_db
      kubectl -n "$K8S_NAMESPACE" rollout restart deployment/ruoyi-nacos deployment/ruoyi-gateway deployment/ruoyi-auth deployment/platform-service deployment/merchant-service deployment/rental-business deployment/ruoyi-gen deployment/ruoyi-job deployment/ruoyi-file deployment/ruoyi-monitor
    fi
    if [[ "$MIGRATE_DB" == "true" ]]; then
      migrate_db
    fi
    show_status
    ;;
  build)
    build_backend_artifacts
    build_frontend_artifacts
    build_docker_images
    ;;
  push)
    push_images
    ;;
  deploy)
    apply_manifests
    show_status
    ;;
  render)
    render_manifests
    ;;
  init-db)
    init_db
    ;;
  migrate-db)
    migrate_db
    ;;
  status)
    show_status
    ;;
  local)
    local_up
    ;;
  local-reset)
    local_reset
    ;;
  local-pull-base)
    local_pull_base_images
    ;;
  local-init-db)
    local_init_db
    ;;
  local-migrate-db)
    local_migrate_db
    local_reload_gateway_config
    ;;
  local-down)
    prepare_local_env
    compose down
    ;;
  local-status)
    prepare_local_env
    compose ps
    ;;
  -h|--help|help)
    usage
    ;;
  *)
    usage
    exit 2
    ;;
esac
