# 迁若租赁平台一键部署

本目录提供面向云容器服务的部署脚本与 Kubernetes 模板，适用于火山引擎容器服务等标准 Kubernetes 集群。

## 部署内容

- MySQL、Redis、Nacos 基础服务
- RuoYi-Cloud 网关、认证、系统、代码生成、任务、文件、监控服务
- 独立租赁核心业务服务 `rental-business`
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
- `MYSQL_ROOT_PASSWORD`、`NACOS_AUTH_*`：必须配置环境专用密钥；Nacos token 是解码后至少 32 字节的 Base64 值，部署脚本不会生成或回退到共享默认密钥。
- `PLATFORM_DB_USERNAME`、`PLATFORM_DB_PASSWORD`：平台服务专用数据库账号；脚本会自动创建并授予 `ry-cloud` 和平台治理所需的 `ry_rental` 数据权限。
- `MERCHANT_DB_USERNAME`、`MERCHANT_DB_PASSWORD`：商户服务专用数据库账号；只授予 `ry_rental` 数据权限。两个服务账号密码至少 12 位，不能包含单引号或反斜杠。
- `BUSINESS_DB_USERNAME`、`BUSINESS_DB_PASSWORD`：租赁核心业务服务专用数据库账号；只授予 `ry_business` 数据权限。密码至少 12 位，不能包含单引号或反斜杠。
- `JAVA_BASE_IMAGE`：后端 Java 17 基础镜像，默认使用微软 MCR，避免依赖 Docker Hub。
- `NGINX_BASE_IMAGE`：前端非 Alpine Nginx 基础镜像，默认通过 DaoCloud 国内镜像加速地址拉取。
- `MYSQL_IMAGE`、`REDIS_IMAGE`、`NACOS_IMAGE`：本地 Docker 基础镜像；Docker Hub 拉取失败时可换成国内镜像源或私有仓库镜像。
- `MIGRATE_DB`：正常部署时是否执行数据库升级，默认 `true`；全新初始化由 `INIT_DB` 流程创建最新表结构。

生产 Kubernetes 清单会创建 `qianruo-database-backup` CronJob，每日上海时间 02:30 使用 MySQL 镜像生成全库一致性压缩备份，写入 `backup-data` PVC，并将成功、失败、容量和保留截止时间写入平台运维台账；默认保留 7 天。备份卷可用空间低于系统配置 `platform.operations.backup.min-available-percent` 时自动生成存储告警，恢复后自动解除。同时创建 `qianruo-monthly-compliance-package` CronJob，在每月 1 日 03:30 归档审计、资金、支付、提现和发票表，默认保留 1095 天。备份 PVC 不是 OSS 副本，OSS 复制仍需按正式环境的对象存储策略另行配置。

## 时区约定

项目统一使用 `Asia/Shanghai`。部署脚本默认给 Java 服务注入  
`-Duser.timezone=Asia/Shanghai`，Docker Compose 和 Kubernetes 模板也会设置  
`TZ=Asia/Shanghai`，MySQL 连接串使用 `serverTimezone=Asia%2FShanghai`。  
新增服务或改造部署模板时必须沿用这一组配置，避免后台列表、操作日志、用户资料等时间字段出现 8 小时偏移。

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
7. 运行一次性数据库授权 Job，业务服务不使用 MySQL root 账号。
8. 对已有数据库只执行尚未记录的升级迁移。

### 部署到阿里云 ACR/ACK

阿里云发布使用专用封装脚本，底层仍调用同一个 `scripts/deploy.sh`，因此构建、镜像命名、数据库迁移和 Kubernetes 清单保持一致：

```bash
cp deploy/.env.aliyun.example deploy/.env.aliyun
# 编辑 deploy/.env.aliyun，填写 ACR、ACK kubeconfig/context 和运行时密钥
./scripts/deploy-aliyun.sh all
```

脚本约定：

- `ALIYUN_REGISTRY` 填 ACR registry 域名，不要包含协议头；`ALIYUN_IMAGE_NAMESPACE` 填仓库命名空间。
- 未填写 `IMAGE_TAG` 时自动使用当前完整 Git commit SHA；禁止 `latest`，便于回滚和追溯。
- `ALIYUN_KUBECONFIG` 可填写 ACK 控制台下载的 kubeconfig，也可以只填写 `ALIYUN_KUBE_CONTEXT` 使用本机已有配置。
- `ALIYUN_STORAGE_CLASS_NAME` 默认使用 `alicloud-disk-topology`；如果集群名称不同，先执行 `kubectl get storageclass` 查询后覆盖该值，否则 MySQL、Redis、文件服务的 PVC 可能无法绑定。
- ACR 登录可提前执行 `docker login`，也可同时配置 `ALIYUN_REGISTRY_USERNAME` 和 `ALIYUN_REGISTRY_PASSWORD` 由脚本通过标准输入登录；密码不会写入命令参数或日志。
- 私有 ACR 如果 ACK 节点没有拉取权限，配置 ACR 用户名/密码即可，脚本默认创建并绑定 `qianruo-acr-pull` 拉取密钥；也可以通过 `ALIYUN_IMAGE_PULL_SECRET_NAME` 自定义名称。
- 只渲染清单不发布可执行 `./scripts/deploy-aliyun.sh render`；检查参数但不执行云端动作可执行 `./scripts/deploy-aliyun.sh render --dry-run`。

分阶段发布：

```bash
./scripts/deploy-aliyun.sh build
./scripts/deploy-aliyun.sh push
./scripts/deploy-aliyun.sh deploy
./scripts/deploy-aliyun.sh status
```

首次新环境初始化数据库仍需明确设置 `INIT_DB=true` 并确认；生产升级保持 `INIT_DB=false`、`MIGRATE_DB=true`。

### 部署到本地 Mac Docker Desktop

本地 Docker 不需要 Kubernetes，直接使用 Docker Compose：

```bash
./scripts/deploy.sh local
```

`local` 会在保留现有数据卷的情况下只执行尚未记录的迁移，不会重建数据库。也可以单独执行：

```bash
./scripts/deploy.sh local-migrate-db
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
JAVA_BASE_IMAGE=mcr.microsoft.com/openjdk/jdk:17-ubuntu
NGINX_BASE_IMAGE=m.daocloud.io/docker.io/library/nginx:1.27
MYSQL_IMAGE=你的镜像仓库/mysql:8.4
REDIS_IMAGE=m.daocloud.io/docker.io/library/redis:7.4
NACOS_IMAGE=你的镜像仓库/nacos/nacos-server:v3.0.2
```

Redis 默认使用 Debian 系列的 Redis 7.4 国内加速镜像，不使用 Alpine。  
该版本与项目此前 `redis:7-alpine` 产生的持久化数据格式兼容，降级到  
Redis 7.2 会因无法读取 RDB v12 而启动失败。  
本地 Compose 项目固定命名为 `deploy`，所有服务会在 Docker Desktop 的 `deploy` 项目下统一展示。  
本地 Compose 使用 `pull_policy: missing`，Kubernetes 使用  
`imagePullPolicy: IfNotPresent`；节点已有同名同标签镜像时不会重复拉取。  
发布新版本时应更新 `IMAGE_TAG`，避免复用旧应用镜像。

本地执行 `./scripts/deploy.sh local` 时会强制替换应用服务容器，并在启动后检查  
`/merchant/user/getInfo` 是否由独立 `merchant-service` 和精确网关路由正确接管。  
商户身份服务使用端口 `9210`。构建产物缺少商户 Controller 或固定商户网关路由时，  
部署会立即终止，避免继续运行旧镜像。

租赁核心业务服务使用端口 `9220`、Nacos 服务名 `rental-business` 和独立数据库  
`ry_business`，提供 `/actuator/health`、内部探活接口  
`/internal/rental-business/ping` 及商户订单列表接口 `/merchant/orders`。  
订单请求通过网关精确路由到 `rental-business`，订单、商品、套餐、客户、仓库、物流和  
批注查询表由该服务维护，不得重新写入 `platform-service` 或 `merchant-service`。

默认部署未包含 Sentinel Dashboard，因此网关不会连接  
`127.0.0.1:8718`，并关闭 Sentinel 健康检查以避免周期性无效告警。  
需要接入控制台时，为网关设置 `SENTINEL_DASHBOARD`、  
`SENTINEL_EAGER=true` 和 `SENTINEL_HEALTH_ENABLED=true`。

后端镜像构建默认从微软容器镜像仓库拉取 Java 17，不再使用  
`eclipse-temurin:17-jre`。如果 MCR 在当前网络中也不可达，可将  
`JAVA_BASE_IMAGE` 改为火山引擎镜像仓库中同步好的 Java 17 镜像；镜像内需提供 `java` 命令。  
前端默认使用 Debian 系列的 Nginx 1.27，不再使用 `nginx:1.27-alpine`。  
如果 DaoCloud 镜像地址不可达，可将 `NGINX_BASE_IMAGE` 改为私有仓库中同步好的 Nginx 镜像。

如果只有 MySQL 拉取失败，只改这一项即可：

```bash
MYSQL_IMAGE=你的镜像仓库/mysql:8.4
```

项目默认使用当前维护中的 MySQL 8.4。若你本机或私有仓库已经有历史 MySQL 5.7 镜像，也可以手动设置为 `mysql:5.7`，但不建议作为新环境默认值。

本地端口：

- 平台后台：<http://localhost:8088>
- 商家端：<http://localhost:8089>
- 网关：<http://localhost:8080>
- Nacos 控制台：<http://localhost:18080/>
- Nacos API：<http://localhost:8848>

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

已有环境只升级表结构或业务结构时不要执行初始化命令，使用：

```bash
./scripts/deploy.sh migrate-db
```

当前历史迁移已按原部署顺序合并至 `database/migrations/V20261019_002__consolidated_project_baseline.sql`。新库通过 `init-db` 初始化时会执行该文件，并只在迁移账本中记录合并基线版本；历史段落标记保留在 SQL 注释中供审阅。

切换中的已有数据库必须已执行到 `V20261019_001__platform_merchant_menu_management`，且账本校验和匹配。部署脚本只登记合并基线，不会重放历史 SQL，并保留原账本记录。账本缺失、版本较旧或校验和不匹配时会中止；本版本不再支持从更早数据库状态增量升级。当前本地 Docker 账本已核验到该截止版本；阿里云环境需在切换前单独核验。

合并基线发布后不得修改。未来数据库变更仍应新增版本化迁移文件并登记到 `DATABASE_MIGRATIONS`，不能把新变更追加到已执行基线。

仓库不携带商户、客户、订单或库存测试数据脚本。需要联调数据时，请在隔离的开发数据库中通过应用功能或专用测试夹具创建，不要把真实业务数据提交到仓库。

已有 Nacos 配置会自动切换为 `/admin/** -> platform-service` 和 `/merchant/** -> merchant-service`，迁移完成后脚本会刷新 Nacos、网关和租赁业务服务运行态。

平台和商户服务的 Nacos 独立配置如果缺失或内容为空，迁移会自动补齐最低运行配置。两个服务也在各自的 `bootstrap.yml` 中保留数据源和 MyBatis mapper 扫描兜底，因此配置中心的单条空记录不会再导致 `Invalid bound statement` 启动失败；数据库密码仍只通过环境变量或 Secret 注入，不写入 Nacos。

本次升级后，旧登录令牌不包含客户端类型，平台端和商户端用户需要重新登录一次；新令牌会固定记录 `admin` 或 `merchant`，分别使用独立缓存前缀，并阻止跨端接口访问。

## 常用命令

```bash
./scripts/deploy.sh build
./scripts/deploy.sh push
./scripts/deploy.sh deploy
./scripts/deploy.sh render
./scripts/deploy.sh status
./scripts/deploy.sh migrate-db
./scripts/deploy.sh local
./scripts/deploy.sh local-migrate-db
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
