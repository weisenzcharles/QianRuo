#!/usr/bin/env bash
set -euo pipefail

# Alibaba Cloud ACR/ACK entrypoint. The canonical build, migration and manifest
# logic remains in deploy.sh so cloud and local releases cannot drift apart.
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MAIN_SCRIPT="$ROOT_DIR/scripts/deploy.sh"
CONFIG_FILE="${ALIYUN_ENV_FILE:-$ROOT_DIR/deploy/.env.aliyun}"
DRY_RUN="false"
SKIP_LOGIN="false"

usage() {
  cat <<'EOF'
Usage: ./scripts/deploy-aliyun.sh [all|build|push|deploy|render|migrate-db|status] [--dry-run] [--skip-login]

Commands:
  all       Build backend/frontend, push ACR images, apply ACK manifests and migrate DB.
  build     Build application artifacts and Docker images locally.
  push      Push already-built application images to ACR.
  deploy    Apply the rendered application manifests to ACK and migrate DB.
  render    Render ACK manifests without applying them.
  migrate-db Apply only unapplied database migrations to ACK.
  status    Show ACK workload status.

Configuration:
  Copy deploy/.env.aliyun.example to deploy/.env.aliyun and fill in the ACR,
  ACK and runtime values. Secrets are read from that file but never printed.
  ALIYUN_KUBECONFIG may point to the kubeconfig downloaded from the ACK console.
  ALIYUN_IMAGE_PULL_SECRET_NAME is optional; use it with the ACR username/password
  when ACK nodes do not already have permission to pull the private repository.

Examples:
  ./scripts/deploy-aliyun.sh all
  ./scripts/deploy-aliyun.sh deploy
  ./scripts/deploy-aliyun.sh render --dry-run
EOF
}

die() {
  printf '阿里云发布失败：%s\n' "$1" >&2
  exit 1
}

log() {
  printf '\n[%s] %s\n' "$(date '+%H:%M:%S')" "$*"
}

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || die "缺少命令：$1"
}

parse_args() {
  COMMAND="all"
  while (($#)); do
    case "$1" in
      all|build|push|deploy|render|migrate-db|status)
        COMMAND="$1"
        ;;
      --dry-run)
        DRY_RUN="true"
        ;;
      --skip-login)
        SKIP_LOGIN="true"
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        die "未知参数：$1"
        ;;
    esac
    shift
  done
}

load_config() {
  if [[ ! -f "$CONFIG_FILE" ]]; then
    die "配置文件不存在：${CONFIG_FILE#"$ROOT_DIR/"}。请先复制 deploy/.env.aliyun.example。"
  fi

  set -a
  # shellcheck disable=SC1090
  source "$CONFIG_FILE"
  set +a

  REGISTRY="${ALIYUN_REGISTRY:-${REGISTRY:-}}"
  IMAGE_NAMESPACE="${ALIYUN_IMAGE_NAMESPACE:-${IMAGE_NAMESPACE:-qianruo}}"
  K8S_NAMESPACE="${ALIYUN_K8S_NAMESPACE:-${K8S_NAMESPACE:-qianruo}}"
  KUBE_CONTEXT="${ALIYUN_KUBE_CONTEXT:-${KUBE_CONTEXT:-}}"
  STORAGE_CLASS_NAME="${ALIYUN_STORAGE_CLASS_NAME:-${STORAGE_CLASS_NAME:-alicloud-disk-topology}}"
  IMAGE_PLATFORM="${ALIYUN_IMAGE_PLATFORM:-${IMAGE_PLATFORM:-linux/amd64}}"
  ADMIN_HOST="${ALIYUN_ADMIN_HOST:-${ADMIN_HOST:-}}"
  MERCHANT_HOST="${ALIYUN_MERCHANT_HOST:-${MERCHANT_HOST:-}}"
  REGISTRY_USERNAME="${ALIYUN_REGISTRY_USERNAME:-}"
  REGISTRY_PASSWORD="${ALIYUN_REGISTRY_PASSWORD:-}"
  if [[ -n "$REGISTRY_USERNAME" || -n "$REGISTRY_PASSWORD" ]]; then
    IMAGE_PULL_SECRET_NAME="${ALIYUN_IMAGE_PULL_SECRET_NAME:-qianruo-acr-pull}"
  else
    IMAGE_PULL_SECRET_NAME="${ALIYUN_IMAGE_PULL_SECRET_NAME:-}"
  fi
  REGISTRY_SERVER="${ALIYUN_REGISTRY_SERVER:-$REGISTRY}"
  NACOS_IMAGE="${ALIYUN_NACOS_IMAGE:-${NACOS_IMAGE:-m.daocloud.io/docker.io/nacos/nacos-server:v3.0.2}}"
  DOCKER_LOGIN="${ALIYUN_DOCKER_LOGIN:-true}"
  ALIYUN_KUBECONFIG="${ALIYUN_KUBECONFIG:-}"

  [[ -n "$REGISTRY" && "$REGISTRY" != *"<"* && "$REGISTRY" != *">"* ]] || die "ALIYUN_REGISTRY 必须填写 ACR registry 域名"
  [[ "$REGISTRY" != http://* && "$REGISTRY" != https://* ]] || die "ALIYUN_REGISTRY 不要包含 http:// 或 https://"
  [[ "$IMAGE_NAMESPACE" != */* ]] || die "ALIYUN_IMAGE_NAMESPACE 只填写仓库命名空间，不要包含 registry 域名"
  [[ "$K8S_NAMESPACE" =~ ^[a-z0-9]([-a-z0-9]*[a-z0-9])?$ ]] || die "ALIYUN_K8S_NAMESPACE 不是合法 Kubernetes 命名空间"

  if [[ -z "${IMAGE_TAG:-}" || "${IMAGE_TAG:-}" == "latest" ]]; then
    IMAGE_TAG="$(git -C "$ROOT_DIR" rev-parse HEAD 2>/dev/null || true)"
  fi
  [[ "$IMAGE_TAG" =~ ^[0-9a-f]{12,64}$ ]] || die "IMAGE_TAG 必须是至少 12 位的 Git SHA，不允许使用 latest"

  if [[ -n "$ALIYUN_KUBECONFIG" ]]; then
    [[ -f "$ALIYUN_KUBECONFIG" ]] || die "ALIYUN_KUBECONFIG 文件不存在"
    export KUBECONFIG="$ALIYUN_KUBECONFIG"
  fi

  export ENV_FILE="$CONFIG_FILE"
  export REGISTRY IMAGE_NAMESPACE IMAGE_TAG K8S_NAMESPACE KUBE_CONTEXT STORAGE_CLASS_NAME ADMIN_HOST MERCHANT_HOST IMAGE_PLATFORM NACOS_IMAGE
}

prepare_tools() {
  case "$COMMAND" in
    all|build|push) need_cmd docker ;;
  esac
  case "$COMMAND" in
    all|deploy|render|migrate-db|status) need_cmd kubectl ;;
  esac
  need_cmd git
}

prepare_registry_login() {
  [[ "$COMMAND" == "all" || "$COMMAND" == "push" ]] || return 0
  [[ "$SKIP_LOGIN" == "true" || "$DOCKER_LOGIN" != "true" ]] && return 0
  if [[ -n "$REGISTRY_USERNAME" || -n "$REGISTRY_PASSWORD" ]]; then
    [[ -n "$REGISTRY_USERNAME" && -n "$REGISTRY_PASSWORD" ]] || die "ACR 用户名和密码必须同时配置"
    log "登录阿里云 ACR：$REGISTRY"
    printf '%s' "$REGISTRY_PASSWORD" | docker login "$REGISTRY" --username "$REGISTRY_USERNAME" --password-stdin >/dev/null
  else
    log "未提供 ACR 密码，使用本机已有 Docker 登录凭据"
  fi
}

prepare_pull_secret() {
  [[ "$COMMAND" == "all" || "$COMMAND" == "deploy" ]] || return 0
  [[ -n "$IMAGE_PULL_SECRET_NAME" ]] || return 0
  [[ "$DRY_RUN" == "true" ]] && return 0

  kubectl create namespace "$K8S_NAMESPACE" --dry-run=client -o yaml | kubectl apply -f - >/dev/null
  if [[ -n "$REGISTRY_USERNAME" || -n "$REGISTRY_PASSWORD" ]]; then
    [[ -n "$REGISTRY_USERNAME" && -n "$REGISTRY_PASSWORD" ]] || die "创建 ACR 拉取密钥时，用户名和密码必须同时配置"
    log "更新 ACK 镜像拉取密钥：$IMAGE_PULL_SECRET_NAME"
    kubectl -n "$K8S_NAMESPACE" create secret docker-registry "$IMAGE_PULL_SECRET_NAME" \
      --docker-server="$REGISTRY_SERVER" \
      --docker-username="$REGISTRY_USERNAME" \
      --docker-password="$REGISTRY_PASSWORD" \
      --dry-run=client -o yaml | kubectl apply -f - >/dev/null
  else
    log "使用 ACK 命名空间中已有镜像拉取密钥：$IMAGE_PULL_SECRET_NAME"
  fi
}

attach_pull_secret() {
  [[ "$COMMAND" == "all" || "$COMMAND" == "deploy" ]] || return 0
  [[ -n "$IMAGE_PULL_SECRET_NAME" && "$DRY_RUN" != "true" ]] || return 0
  local deployment
  for deployment in ruoyi-gateway ruoyi-auth platform-service merchant-service rental-business ruoyi-gen ruoyi-job ruoyi-file ruoyi-monitor rental-admin-web rental-merchant-web; do
    kubectl -n "$K8S_NAMESPACE" patch deployment "$deployment" --type strategic \
      -p "{\"spec\":{\"template\":{\"spec\":{\"imagePullSecrets\":[{\"name\":\"$IMAGE_PULL_SECRET_NAME\"}]}}}}" \
      >/dev/null
  done
}

ensure_public_frontends() {
  [[ "$COMMAND" == "all" || "$COMMAND" == "deploy" ]] || return 0
  [[ "$DRY_RUN" != "true" ]] || return 0
  kubectl -n "$K8S_NAMESPACE" patch service rental-admin-web --type merge \
    -p '{"spec":{"type":"LoadBalancer"}}' >/dev/null
  kubectl -n "$K8S_NAMESPACE" patch service rental-merchant-web --type merge \
    -p '{"spec":{"type":"LoadBalancer"}}' >/dev/null
}

run_main_script() {
  if [[ "$DRY_RUN" == "true" ]]; then
    printf 'DRY RUN: ENV_FILE=%s REGISTRY=%s IMAGE_NAMESPACE=%s IMAGE_TAG=%s K8S_NAMESPACE=%s ./scripts/deploy.sh %s\n' \
      "${CONFIG_FILE#"$ROOT_DIR/"}" "$REGISTRY" "$IMAGE_NAMESPACE" "$IMAGE_TAG" "$K8S_NAMESPACE" "$COMMAND"
    return 0
  fi
  "$MAIN_SCRIPT" "$COMMAND"
}

main() {
  parse_args "$@"
  if [[ "$COMMAND" == "render" && "$DRY_RUN" == "true" ]]; then
    CONFIG_FILE="${ALIYUN_ENV_FILE:-$ROOT_DIR/deploy/.env.aliyun.example}"
  fi
  load_config
  prepare_tools
  prepare_registry_login
  prepare_pull_secret
  run_main_script
  attach_pull_secret
  ensure_public_frontends
}

main "$@"
