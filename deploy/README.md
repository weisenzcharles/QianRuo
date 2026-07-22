# 迁渃租赁平台一键部署

本目录提供面向云容器服务的部署脚本与 Kubernetes 模板，适用于火山引擎容器服务等标准 Kubernetes 集群。

## 部署内容

- MySQL、Redis、Nacos 基础服务
- RuoYi-Cloud 网关、认证、系统、代码生成、任务、文件、监控服务
- 平台后台 `rental-admin-web`
- 商家端 `rental-merchant-web`
- 可选 Ingress 域名入口

## 前置准备

1. 本机安装并登录 Docker。
2. 本机安装 `kubectl`，并已配置云上集群 kubeconfig。
3. 准备容器镜像仓库，例如火山引擎镜像仓库命名空间。
4. 若镜像仓库是私有仓库，请在集群命名空间内配置 imagePullSecret，或在云控制台为集群授权拉取镜像。

## 配置

```bash
cp deploy/.env.example deploy/.env
```

编辑 `deploy/.env`：

- `REGISTRY`：镜像仓库地址，例如云厂商控制台显示的 registry 域名。
- `IMAGE_NAMESPACE`：镜像命名空间。
- `IMAGE_TAG`：镜像标签，建议使用版本号或 Git commit。
- `K8S_NAMESPACE`：Kubernetes 命名空间。
- `KUBE_CONTEXT`：kubectl context，可留空使用当前 context。
- `ADMIN_HOST`、`MERCHANT_HOST`：平台后台和商家端域名。两个都填写时会创建 Ingress。
- `MYSQL_ROOT_PASSWORD`、`NACOS_AUTH_*`：生产环境必须替换为强密码或随机密钥。
- `MYSQL_IMAGE`、`REDIS_IMAGE`、`NACOS_IMAGE`：本地 Docker 基础镜像；Docker Hub 拉取失败时可换成国内镜像源或私有仓库镜像。

## 一键部署

### 部署到云容器服务

```bash
./scripts/deploy.sh all
```

该命令会依次执行：

1. Maven 打包后端微服务。
2. npm 构建平台后台和商家端。
3. 构建 Docker 镜像。
4. 推送镜像到仓库。
5. 渲染 Kubernetes 模板。
6. 应用到云上 Kubernetes 集群。

### 部署到本地 Mac Docker Desktop

本地 Docker 不需要 Kubernetes，直接使用 Docker Compose：

```bash
./scripts/deploy.sh local
```

本地全新环境推荐直接一次性重置、初始化并启动：

```bash
./scripts/deploy.sh local-reset
```

该命令会删除本地 Docker Compose 卷，适合开发机首次部署或本地数据可清空的场景。

如果遇到 `failed to fetch anonymous token`、`EOF`、`timeout` 等 Docker Hub 拉取错误，先尝试：

```bash
docker login
./scripts/deploy.sh local-pull-base
```

如果仍然失败，在 `deploy/.env` 中把基础镜像替换为你可访问的镜像仓库：

```bash
MYSQL_IMAGE=你的镜像仓库/mysql:8.4
REDIS_IMAGE=你的镜像仓库/redis:7-alpine
NACOS_IMAGE=你的镜像仓库/nacos/nacos-server:v3.0.2
```

如果只有 MySQL 拉取失败，只改这一项即可：

```bash
MYSQL_IMAGE=你的镜像仓库/mysql:8.4
```

项目默认使用当前维护中的 MySQL 8.4。若你本机或私有仓库已经有历史 MySQL 5.7 镜像，也可以手动设置为 `mysql:5.7`，但不建议作为新环境默认值。

本地端口：

- 平台后台：http://localhost:8088
- 商家端：http://localhost:8089
- 网关：http://localhost:8080
- Nacos 控制台：http://localhost:18080/
- Nacos API：http://localhost:8848

首次本地初始化数据库：

```bash
CONFIRM_INIT_DB=yes ./scripts/deploy.sh local-init-db
```

首次本地部署并初始化。第一次在本机启动必须执行这条，因为 Nacos 需要 `ry-config` 配置库：

```bash
INIT_DB=true CONFIRM_INIT_DB=yes ./scripts/deploy.sh local
```

停止本地服务：

```bash
./scripts/deploy.sh local-down
```

## 首次初始化数据库

数据库初始化会创建或重建 RuoYi 配置库、系统库表和租赁业务库，已有数据环境不要直接执行。

新环境可以执行：

```bash
CONFIRM_INIT_DB=yes ./scripts/deploy.sh init-db
```

也可以在全量首次部署时打开：

```bash
INIT_DB=true CONFIRM_INIT_DB=yes ./scripts/deploy.sh all
```

初始化完成后，脚本会在 `all` 模式下重启 Nacos 与后端服务，让服务重新读取配置。

## 常用命令

```bash
./scripts/deploy.sh build
./scripts/deploy.sh push
./scripts/deploy.sh deploy
./scripts/deploy.sh render
./scripts/deploy.sh status
./scripts/deploy.sh local
./scripts/deploy.sh local-status
./scripts/deploy.sh local-reset
./scripts/deploy.sh local-down
```

## 文件说明

- `deploy/.env.example`：部署配置模板，不提交真实密钥。
- `deploy/docker/backend.Dockerfile`：后端微服务通用镜像。
- `deploy/docker/frontend.Dockerfile`：前端 Nginx 通用镜像。
- `deploy/docker-compose.local.yml`：本地 Docker Desktop 编排文件。
- `deploy/nginx/*.conf`：前端容器 Nginx 配置。
- `deploy/k8s/*.template.yaml`：Kubernetes 清单模板。
- `scripts/render_template.py`：模板变量渲染工具。
- `scripts/deploy.sh`：一键部署入口。

## 生产建议

- MySQL、Redis、Nacos 可替换为云厂商托管服务，减少状态组件运维成本。
- 为后端服务配置资源 requests/limits、健康检查和 HPA 后再进入生产流量。
- 生产环境建议将 Secret 改为云 KMS 或密钥管理服务注入。
- Ingress、证书、负载均衡和 WAF 建议在云控制台统一治理。
