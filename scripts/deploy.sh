#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ENV_FILE:-$ROOT_DIR/deploy/.env}"
GENERATED_DIR="$ROOT_DIR/deploy/.generated"
GENERATED_MANIFEST="$GENERATED_DIR/qianruo.yaml"
GENERATED_INGRESS="$GENERATED_DIR/ingress.yaml"
LOCAL_COMPOSE_FILE="$ROOT_DIR/deploy/docker-compose.local.yml"

if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

REGISTRY="${REGISTRY:-}"
IMAGE_NAMESPACE="${IMAGE_NAMESPACE:-qianruo}"
IMAGE_TAG="${IMAGE_TAG:-$(git -C "$ROOT_DIR" rev-parse --short HEAD 2>/dev/null || echo latest)}"
K8S_NAMESPACE="${K8S_NAMESPACE:-qianruo}"
KUBE_CONTEXT="${KUBE_CONTEXT:-}"
ADMIN_HOST="${ADMIN_HOST:-}"
MERCHANT_HOST="${MERCHANT_HOST:-}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-}"
NACOS_AUTH_TOKEN="${NACOS_AUTH_TOKEN:-}"
NACOS_AUTH_IDENTITY_KEY="${NACOS_AUTH_IDENTITY_KEY:-serverIdentity}"
NACOS_AUTH_IDENTITY_VALUE="${NACOS_AUTH_IDENTITY_VALUE:-}"
BUILD_BACKEND="${BUILD_BACKEND:-true}"
BUILD_FRONTEND="${BUILD_FRONTEND:-true}"
BUILD_IMAGES="${BUILD_IMAGES:-true}"
PUSH_IMAGES="${PUSH_IMAGES:-true}"
APPLY_MANIFESTS="${APPLY_MANIFESTS:-true}"
INIT_DB="${INIT_DB:-false}"
JAVA_OPTS="${JAVA_OPTS:--Xms256m -Xmx512m}"
MYSQL_IMAGE="${MYSQL_IMAGE:-mysql:8.4}"
REDIS_IMAGE="${REDIS_IMAGE:-redis:7-alpine}"
NACOS_IMAGE="${NACOS_IMAGE:-nacos/nacos-server:v3.0.2}"

export REGISTRY IMAGE_NAMESPACE IMAGE_TAG K8S_NAMESPACE ADMIN_HOST MERCHANT_HOST
export MYSQL_ROOT_PASSWORD NACOS_AUTH_TOKEN NACOS_AUTH_IDENTITY_KEY NACOS_AUTH_IDENTITY_VALUE JAVA_OPTS
export MYSQL_IMAGE REDIS_IMAGE NACOS_IMAGE

BACKEND_IMAGES=(
  "ruoyi-gateway:backend/ruoyi-cloud/ruoyi-gateway/target/ruoyi-gateway.jar:ruoyi-gateway"
  "ruoyi-auth:backend/ruoyi-cloud/ruoyi-auth/target/ruoyi-auth.jar:ruoyi-auth"
  "ruoyi-modules-system:backend/ruoyi-cloud/ruoyi-modules/ruoyi-system/target/ruoyi-modules-system.jar:ruoyi-system"
  "ruoyi-modules-gen:backend/ruoyi-cloud/ruoyi-modules/ruoyi-gen/target/ruoyi-modules-gen.jar:ruoyi-gen"
  "ruoyi-modules-job:backend/ruoyi-cloud/ruoyi-modules/ruoyi-job/target/ruoyi-modules-job.jar:ruoyi-job"
  "ruoyi-modules-file:backend/ruoyi-cloud/ruoyi-modules/ruoyi-file/target/ruoyi-modules-file.jar:ruoyi-file"
  "ruoyi-visual-monitor:backend/ruoyi-cloud/ruoyi-visual/ruoyi-monitor/target/ruoyi-visual-monitor.jar:ruoyi-monitor"
)

FRONTEND_IMAGES=(
  "rental-admin-web:frontend/admin-web/dist:deploy/nginx/admin.conf"
  "rental-merchant-web:frontend/merchant-web/dist:deploy/nginx/merchant.conf"
)

usage() {
  cat <<'EOF'
Usage: ./scripts/deploy.sh [all|build|push|deploy|render|init-db|status|local|local-reset|local-pull-base|local-init-db|local-down|local-status]

Commands:
  all      Build backend/frontend, build images, push images, render and apply Kubernetes manifests.
  build    Build backend/frontend artifacts and Docker images.
  push     Push all application images.
  deploy   Render and apply Kubernetes manifests.
  render   Render Kubernetes manifests into deploy/.generated.
  init-db  Initialize MySQL with RuoYi and rental SQL. Requires CONFIRM_INIT_DB=yes.
  status   Show Kubernetes workload status.
  local    Build images and start the full stack in local Docker Desktop.
  local-reset
           Reset local Docker volumes, initialize database and start the full stack.
  local-pull-base
           Pull MySQL, Redis and Nacos base images for local Docker Desktop.
  local-init-db
           Initialize the local Docker MySQL. Requires CONFIRM_INIT_DB=yes.
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
  for key in MYSQL_ROOT_PASSWORD NACOS_AUTH_TOKEN NACOS_AUTH_IDENTITY_KEY NACOS_AUTH_IDENTITY_VALUE; do
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
  MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-qianruo_local_password}"
  NACOS_AUTH_TOKEN="${NACOS_AUTH_TOKEN:-cWlhbnJ1by1sb2NhbC1uYWNvcy10b2tlbi1zZWNyZXQtMjAyNg==}"
  NACOS_AUTH_IDENTITY_KEY="${NACOS_AUTH_IDENTITY_KEY:-serverIdentity}"
  NACOS_AUTH_IDENTITY_VALUE="${NACOS_AUTH_IDENTITY_VALUE:-qianruo-local-identity}"
  MYSQL_IMAGE="${MYSQL_IMAGE:-mysql:8.4}"
  REDIS_IMAGE="${REDIS_IMAGE:-redis:7-alpine}"
  NACOS_IMAGE="${NACOS_IMAGE:-nacos/nacos-server:v3.0.2}"
  PUSH_IMAGES=false
  APPLY_MANIFESTS=false
  export REGISTRY IMAGE_NAMESPACE MYSQL_ROOT_PASSWORD NACOS_AUTH_TOKEN NACOS_AUTH_IDENTITY_KEY NACOS_AUTH_IDENTITY_VALUE
  export MYSQL_IMAGE REDIS_IMAGE NACOS_IMAGE
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
    NACOS_AUTH_TOKEN="cWlhbnJ1by1sb2NhbC1uYWNvcy10b2tlbi1zZWNyZXQtMjAyNg=="
    export NACOS_AUTH_TOKEN
    echo "Local NACOS_AUTH_TOKEN was invalid. Using generated local-only Base64 token."
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
  mvn -f "$ROOT_DIR/backend/ruoyi-cloud/pom.xml" -DskipTests package
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
  log "Building backend Docker images"
  for item in "${BACKEND_IMAGES[@]}"; do
    IFS=: read -r image_name jar_file app_name <<<"$item"
    if [[ ! -f "$ROOT_DIR/$jar_file" ]]; then
      echo "Missing JAR: $jar_file. Run ./scripts/deploy.sh build after backend packaging succeeds." >&2
      exit 1
    fi
    docker build \
      -f "$ROOT_DIR/deploy/docker/backend.Dockerfile" \
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
    docker build \
      -f "$ROOT_DIR/deploy/docker/frontend.Dockerfile" \
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
  kubectl apply -f "$GENERATED_MANIFEST"
  if [[ -f "$GENERATED_INGRESS" ]]; then
    kubectl apply -f "$GENERATED_INGRESS"
  fi
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
  if [[ -n "$database" ]]; then
    kubectl -n "$K8S_NAMESPACE" exec -i deployment/ruoyi-mysql -- \
      sh -c "mysql -uroot -p\"\$MYSQL_ROOT_PASSWORD\" --default-character-set=utf8mb4 --database='$database'" < "$ROOT_DIR/$sql_file"
  else
    kubectl -n "$K8S_NAMESPACE" exec -i deployment/ruoyi-mysql -- \
      sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4' < "$ROOT_DIR/$sql_file"
  fi
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
}

show_status() {
  need_cmd kubectl
  use_kube_context
  kubectl -n "$K8S_NAMESPACE" get deploy,svc,pod,ingress
}

compose() {
  need_cmd docker
  docker compose -f "$LOCAL_COMPOSE_FILE" "$@"
}

local_base_images() {
  printf '%s=%s\n' \
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
   MYSQL_IMAGE=your-registry/mysql:8.4
   REDIS_IMAGE=your-registry/redis:7-alpine
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
  compose up -d ruoyi-nacos
  local_wait_nacos

  log "Starting local application services"
  compose up -d ruoyi-gateway ruoyi-auth ruoyi-system ruoyi-gen ruoyi-job ruoyi-file ruoyi-monitor rental-admin-web rental-merchant-web
  local_wait_system_service
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
  if [[ -n "$database" ]]; then
    compose exec -T ruoyi-mysql \
      sh -c "mysql -uroot -p\"\$MYSQL_ROOT_PASSWORD\" --default-character-set=utf8mb4 --database='$database'" < "$ROOT_DIR/$sql_file"
  else
    compose exec -T ruoyi-mysql \
      sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4' < "$ROOT_DIR/$sql_file"
  fi
}

local_init_db() {
  prepare_local_env
  if [[ "${CONFIRM_INIT_DB:-}" != "yes" ]]; then
    echo "Database initialization may recreate tables. Re-run with CONFIRM_INIT_DB=yes when you are ready." >&2
    exit 1
  fi
  log "Waiting for local MySQL"
  for _ in {1..60}; do
    if compose exec -T ruoyi-mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "SELECT 1" >/dev/null' >/dev/null 2>&1; then
      break
    fi
    sleep 2
  done
  if ! compose exec -T ruoyi-mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "SELECT 1" >/dev/null' >/dev/null 2>&1; then
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
}

local_wait_nacos() {
  log "Waiting for local Nacos"
  for _ in {1..90}; do
    if python3 - <<'PY' >/dev/null 2>&1
import socket

for port in (8848, 9848):
    with socket.create_connection(("127.0.0.1", port), timeout=2):
        pass
PY
    then
      return 0
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
  log "Waiting for local admin API"
  for _ in {1..90}; do
    if docker exec qianruo-admin-web sh -c 'wget -q -O - http://127.0.0.1/prod-api/code >/dev/null' >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  echo "Timed out waiting for local admin API. Recent logs:" >&2
  docker logs qianruo-admin-web --tail 80 >&2 || true
  docker logs qianruo-gateway --tail 120 >&2 || true
  exit 1
}

local_wait_system_service() {
  log "Waiting for local system service"
  for _ in {1..120}; do
    if docker logs qianruo-system 2>&1 | grep -q "ruoyi-system .*register finished\\|系统模块启动成功"; then
      return 0
    fi
    if ! docker inspect qianruo-system --format '{{.State.Running}}' 2>/dev/null | grep -q true; then
      echo "System service is not running. Recent logs:" >&2
      docker logs qianruo-system --tail 120 >&2 || true
      exit 1
    fi
    sleep 2
  done
  echo "Timed out waiting for system service registration. Recent logs:" >&2
  docker logs qianruo-system --tail 160 >&2 || true
  exit 1
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
      kubectl -n "$K8S_NAMESPACE" rollout restart deployment/ruoyi-nacos deployment/ruoyi-gateway deployment/ruoyi-auth deployment/ruoyi-system deployment/ruoyi-gen deployment/ruoyi-job deployment/ruoyi-file deployment/ruoyi-monitor
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
