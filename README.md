# 迁渃租赁平台

迁渃租赁平台是一个面向租赁商家的订单、库存、仓库、物流、售后、财务、联盟协作和库存周转市场系统。项目基于 RuoYi-Cloud 与 RuoYi-Cloud-Vue3-TypeScript 二次开发，采用后端微服务 + 双前端工程的组织方式。

当前仓库包含后端基础工程、平台后台前端、商家端前端、租赁业务数据库脚本和产品设计文档。

## 项目定位

- 商家端：服务租赁商家及员工，覆盖订单录入、SN 绑定、仓库库存、发货归还、验机售后、套餐商品、联盟协作、周转市场和个人中心。
- 平台后台：服务平台内部运营、客服、财务、风控和系统管理员，覆盖会员认证、平台财务、总库存、订单统计、市场风控、推广佣金、系统配置和日志监控。
- 后端服务：基于 RuoYi-Cloud 微服务体系，提供网关、认证、系统基础、文件、任务调度、监控和后续租赁业务服务能力。
- 数据库：独立维护租赁业务库 `ry_rental`，不直接修改 RuoYi-Cloud 原生系统库表。

## 技术栈

### 后端

- Java 17
- Spring Boot 4.0.3
- Spring Cloud 2025.1.0
- Spring Cloud Alibaba 2025.1.0.0
- RuoYi-Cloud 3.6.8
- Nacos、Redis、Sentinel、Seata
- MyBatis / MyBatis Plus 风格的 RuoYi 分层结构

### 前端

- Vue 3.5
- Vite 6
- Element Plus 2
- Pinia
- Vue Router 4
- Axios
- ECharts

### 数据与文档

- MySQL / MariaDB 兼容 SQL
- Flyway/Liquibase 风格迁移脚本预留
- Markdown 与 DOCX 设计文档

## 目录结构

```text
.
├── backend/
│   └── ruoyi-cloud/              # RuoYi-Cloud 后端微服务工程
│       ├── ruoyi-gateway/        # API 网关
│       ├── ruoyi-auth/           # 认证中心
│       ├── ruoyi-api/            # 远程接口定义
│       ├── ruoyi-common/         # 通用能力
│       ├── ruoyi-modules/        # system/file/gen/job 等基础模块
│       ├── ruoyi-visual/         # 监控模块
│       ├── docker/               # Docker 部署参考
│       └── sql/                  # RuoYi 原生 SQL
├── frontend/
│   ├── admin-web/                # 迁渃租赁平台后台
│   └── merchant-web/             # 迁渃租赁商家端
├── database/
│   ├── mysql/                    # 租赁业务库初始化 DDL
│   ├── migrations/               # 迁移编号与执行说明
│   └── seed/                     # 业务字典种子数据
└── docs/
    └── design/                   # 产品设计、规格书、开发计划
```

## 核心业务范围

- 订单管理：录单、复制解析预留、库存时效判断、SN 绑定、发货视频、物流单号、转寄、归还验机、售后、逾期、完结和取消追回。
- 仓库库存：多仓库存、预计库存、物流库存、库存预占、调货、盘点、扫码出入库、库存流水和库存快照。
- 商品中心：品牌型号、商品、设备 SN、配件、套餐、二维码/条码、导入导出和收益统计字段。
- 物流与小程序：快递下单、路由同步、时效检查、客户归还入口、订单查询、开箱视频上传。
- 财务计费：会员余额、快递扣费、增值服务、佣金、保证金、发票和不可变流水。
- 多仓联盟：共享库存、联盟协助订单、接单上限、转派/拒绝和公众池。
- 库存周转市场：标准化发帖、筛选、私信、关注、举报、保证金和后台风控。
- 平台运营：会员统计、认证审核、平台财务、总库存、订单统计、小程序授权、市场风控和推广佣金。

## 双端隔离约定

### 平台后台

- 工程目录：`frontend/admin-web`
- 应用名：`rental-admin-web`
- 页面标题：`迁渃租赁平台后台`
- 默认端口：`80`
- 请求头：`X-Client-Type: platform`
- 请求头：`X-App-Code: rental-admin-web`
- Token Cookie：`Admin-Token`

平台后台只面向平台内部人员，后端必须继续细分运营、财务、风控、客服、系统管理员等权限。跨商户查看敏感数据必须记录审计日志。

### 商家端

- 工程目录：`frontend/merchant-web`
- 应用名：`rental-merchant-web`
- 页面标题：`迁渃租赁商家端`
- 默认端口：`81`
- 请求头：`X-Client-Type: merchant`
- 请求头：`X-App-Code: rental-merchant-web`
- Token Cookie：`Merchant-Token`

商家端只允许访问本商户主体下的数据。后端必须按商户、租户、仓库权限做服务端隔离，不能只依赖前端菜单隐藏。

## 本地启动

### 1. 基础环境

建议准备：

- JDK 17+
- Maven 3.9+
- Node.js 20+
- MySQL 8+
- Redis
- Nacos

如需完整微服务治理能力，再按 RuoYi-Cloud 配置 Sentinel、Seata、监控等组件。

### 2. 初始化数据库

租赁业务库脚本位于 `database/`：

```bash
mysql -uroot -p < database/mysql/00_create_database.sql
mysql -uroot -p ry_rental < database/mysql/01_rental_schema.sql
mysql -uroot -p ry_rental < database/seed/01_rental_dict_seed.sql
```

如果项目后续接入 Flyway 或 Liquibase，可参考：

```text
database/migrations/V20260721_001__rental_base_schema.sql
```

### 3. 启动后端

进入后端工程：

```bash
cd backend/ruoyi-cloud
mvn clean install
```

常见启动顺序：

1. 启动 Nacos、Redis、MySQL。
2. 启动 `ruoyi-gateway`。
3. 启动 `ruoyi-auth`。
4. 启动 `ruoyi-modules/ruoyi-system`。
5. 按需启动 `ruoyi-file`、`ruoyi-job`、`ruoyi-gen`、`ruoyi-visual/ruoyi-monitor`。

具体端口与注册配置以各模块 `bootstrap.yml` 和 Nacos 配置为准。

### 4. 启动平台后台

```bash
cd frontend/admin-web
npm install
npm run dev
```

默认开发端口：`80`。

### 5. 启动商家端

```bash
cd frontend/merchant-web
npm install
npm run dev
```

默认开发端口：`81`。

## 云端一键部署

项目已提供面向 Kubernetes/云容器服务的一键部署脚本，可用于火山引擎容器服务等环境：

```bash
cp deploy/.env.example deploy/.env
./scripts/deploy.sh all
```

部署前请先在 `deploy/.env` 中配置镜像仓库、Kubernetes 命名空间、域名和密钥。详细说明见 `deploy/README.md`。

本地 Mac Docker Desktop 可直接启动整套服务：

```bash
./scripts/deploy.sh local
```

## 安全与数据基线

- 所有业务表应包含 `tenant_id` / `member_id` 等隔离字段。
- 所有 API 必须做服务端权限校验。
- 客户姓名、手机号、地址、支付宝手机号、芝麻分、微信二维码等敏感信息默认脱敏。
- 订单核心字段、SN 状态、库存扣减、仓库迁移、权限修改、敏感信息查看和财务变更必须记录审计日志。
- 资金、余额、扣费、佣金、保证金和发票必须通过不可变流水记录，禁止直接覆盖原流水。
- 文件上传必须校验格式、MIME、大小、数量和访问权限，客户可见文件应使用临时授权 URL。
- 库存预占、SN 绑定、完结回补、调货迁移等动作必须事务化并支持幂等。

## 设计文档

- `docs/design/租赁项目设计文档.md`：项目总体定位与核心模块。
- `docs/design/租赁项目详细功能规格书.md`：业务规则、字段、权限、安全、状态机和验收基线。
- `docs/design/租赁系统RuoYi-Cloud开发计划.md`：工程拆分、开发阶段和模块任务清单。
- `docs/design/租赁项目原始文档抽取汇总.md`：原始需求资料抽取结果。

## 开发阶段建议

当前工程适合按以下顺序推进：

1. 工程落地准备：确认 JDK、Node、MySQL、Redis、Nacos 和基础服务可启动。
2. 双前端与权限骨架：完成平台后台和商家端菜单、路由、登录态、请求头和权限隔离。
3. 基础资料与库存骨架：商品、仓库、SN、配件、套餐、文件上传和库存流水。
4. 订单主流程 MVP：录单、库存检测、SN 绑定、发货、归还验机、完结和取消追回。
5. 仓库作业、物流、小程序、财务、联盟、市场和平台报表逐步完善。

## 许可证

本项目基于 RuoYi-Cloud 二次开发。仓库内 RuoYi 原始工程保留其 MIT License 约定，业务二次开发部分请以项目实际授权策略为准。
