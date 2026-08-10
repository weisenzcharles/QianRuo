# 迁渃租赁平台

迁渃租赁平台是一个面向租赁商家的订单、库存、仓库、物流、售后、财务、联盟协作和库存周转市场系统。项目基于 RuoYi-Cloud 与 RuoYi-Cloud-Vue3-TypeScript 二次开发，采用后端微服务 + 双前端工程的组织方式。

当前仓库包含后端基础工程、平台后台前端、商家端前端、租赁业务数据库脚本和产品设计文档。

## 项目定位

- 商家端：服务租赁商家及员工，覆盖订单录入、SN 绑定、仓库库存、发货归还、验机售后、套餐商品、联盟协作、周转市场和个人中心。
- 平台后台：服务平台内部运营、客服、财务、风控和系统管理员，覆盖会员认证、平台财务、总库存、订单统计、市场风控、推广佣金、系统配置和日志监控。
- 后端服务：基于 RuoYi-Cloud 微服务体系，提供网关、认证、平台治理、商户身份、独立租赁业务、文件、任务调度和监控服务。
- 数据库：平台和商户身份数据使用 `ry-cloud`/`ry_rental`，租赁核心业务服务独立使用 `ry_business`，不直接修改 RuoYi-Cloud 原生系统库表。

当前历史租赁 DDL 仍会在 `ry_rental` 中初始化部分业务表；`rental-business` 已完成独立服务和数据库边界，后续按领域迁移商品、库存、订单等核心表到 `ry_business`。

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
│       ├── ruoyi-modules/        # platform/merchant/rental-business/file/gen/job 等可执行模块
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
- 商户协作：全局工作台、任务、抽检评分、内部通知、私有知识库、操作记录和 30 天回收站。
- 平台治理：协议版本、完整认证、客服工单、合同证据、分账提现、开票红冲、风控和运维告警。
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

商户业务接口统一通过 `MerchantContextUtils` 读取服务端登录态中的租户、商户和仓库范围。查询必须追加 `tenantId + memberId` 条件，涉及具体仓库的操作必须调用 `checkWarehouseAccess(warehouseId)`；不得信任前端提交的租户或商户 ID。

商家端操作日志由 `merchant-service` 独立写入 `ry_rental_merchant_oper_log`。商户管理员默认拥有查看与导出权限，其他商户用户需通过角色权限分配；日志查询始终限制在当前登录态的 `tenantId + memberId` 范围内。

平台统一维护多级产品类目，商户只能在 `/merchant/class/**` 中选择经营范围。商户商品、型号和套餐业务只读取当前商户已选类目；平台有效类目通过内部 Feign 契约提供，商户服务不跨库调用平台 Mapper。

所有 Controller 接口由通用切面记录入库审计，应用表统一使用 `isdeleted` 软删除。MyBatis 运行时拒绝物理 `DELETE` 和 `TRUNCATE`，代码生成模板也默认生成软删除 SQL。

平台商户资料统一维护店铺、企业资质、法人身份、签约合同、认证审核和冻结状态。店铺主账号从商户用户绑定关系实时读取，不在商户主体表重复保存；审核时间、冻结时间及解冻清理由后端状态规则统一处理。

平台系统接口统一使用 `/admin/user/**` 等平台命名空间并由 `platform-service` 提供；商户系统接口必须统一使用 `/merchant/**`。商户用户信息接口为 `/merchant/user/getInfo`，禁止两个终端回退调用旧 `/system/**` 路径。

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

已有数据库升级到当前版本时执行幂等迁移：

```bash
./scripts/deploy.sh migrate-db
# 本地 Docker 环境使用：./scripts/deploy.sh local-migrate-db
```

部署脚本会按 `scripts/deploy.sh` 中 `DATABASE_MIGRATIONS` 的登记顺序执行全部幂等迁移，包括账号隔离、双端路由、仓库权限、商户日志、产品类目、全接口审计字段和统一 `isdeleted` 软删除字段。

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
4. 启动 `ruoyi-modules/ruoyi-platform-service`（运行时服务名仍为 `platform-service`）。
5. 启动 `ruoyi-modules/ruoyi-rental-business`（运行时服务名 `rental-business`，端口 `9220`）。
6. 按需启动 `ruoyi-file`、`ruoyi-job`、`ruoyi-gen`、`ruoyi-visual/ruoyi-monitor`。

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

- `docs/design/1.0需求基线与实施差异.md`：最新三个 1.0 Word 文档的统一术语、不可变规则、实现差异和新增表组。
- `docs/design/项目架构图.md`：当前运行架构、Maven 模块依赖、认证隔离、服务调用和全量数据关系图。
- `docs/design/商品与套餐库存数据结构设计.md`：共享资源池、版本化 BOM、套餐容量、订单预占和并发防超卖设计。
- `docs/design/产品类目、接口审计与软删除设计.md`：平台多级类目、商户经营范围、全接口审计和统一软删除规范。
- `docs/design/租赁项目设计文档.md`：项目总体定位与核心模块。
- `docs/design/租赁项目详细功能规格书.md`：业务规则、字段、权限、安全、状态机和验收基线。
- `docs/design/租赁系统RuoYi-Cloud开发计划.md`：工程拆分、开发阶段和模块任务清单。
- `docs/design/第1阶段完成状态.md`：双前端与权限骨架的当前完成情况。
- `docs/design/租赁项目原始文档抽取汇总.md`：原始需求资料抽取结果。

当前产品需求以 `docs/design` 下三个 `1.0-` 开头的 Word 文件为权威源；研发设计以 Markdown 文档为持续维护版本。旧版拆分 Word 文档仅用于历史追溯，发生冲突时不再覆盖最新 1.0 基线。
- `docs/项目路由清单.md`：网关、后端 API、平台端和商户端页面路由总表。

## 商户账号开通

平台后台新增商户时，系统会使用商户的 `联系电话` 自动创建并绑定商家管理员账号。商户账号写入 `ry_rental.ry_rental_merchant_user`，不混用平台内部用户表 `sys_user`；商户与账号关系通过 `ry_rental_user_member.merchant_user_id` 维护。

该账号首次进入商家端时可选择 `手机号验证码` 登录；当前开发阶段未接入短信服务，允许输入任意四位数字验证码。登录成功后进入后台独立的“设置初始密码”页面，完成 5 到 20 位登录密码设置前不能访问其他业务页面，但可以退出登录。初始密码页面不在登录页弹窗，也不与个人中心的“修改密码”页面混用。

平台用户和商户用户分别使用 `sys_user.must_change_password` 与 `ry_rental.ry_rental_merchant_user.must_change_password` 持久化初始化状态。`pwd_update_date` 仅记录密码更新时间和处理密码过期策略，不再用于推断用户是否设置过初始密码。平台和商户使用独立初始化接口，管理员重置密码后会重新要求对应用户设置密码。

## 商户权限隔离

商户端用户、角色、菜单和授权关系均拆分到租赁业务库，不再复用平台后台的 `sys_user`、`sys_role`、`sys_menu`、`sys_user_role`、`sys_role_menu`。

- 商户用户表：`ry_rental.ry_rental_merchant_user`
- 商户与用户绑定表：`ry_rental.ry_rental_user_member`
- 商户菜单模板表：`ry_rental.ry_rental_merchant_menu`
- 商户角色表：`ry_rental.ry_rental_merchant_role`
- 商户角色菜单表：`ry_rental.ry_rental_merchant_role_menu`
- 商户用户角色表：`ry_rental.ry_rental_merchant_user_role`
- 商户用户菜单表：`ry_rental.ry_rental_merchant_user_menu`

商户菜单使用全局模板记录维护，商户角色和授权关系按 `tenant_id + member_id` 隔离。商户管理员可在商家端 `角色权限` 页面新增、修改、启停、删除本商户角色，并为角色勾选可访问菜单；平台后台可在 `商户用户` 页面为单个商户用户配置最终菜单权限。商家端实际加载菜单以商户用户菜单表为准，平台后台的菜单和角色不会透出到商家端。

## 开发阶段建议

当前工程适合按以下顺序推进：

1. 工程落地准备：确认 JDK、Node、MySQL、Redis、Nacos 和基础服务可启动。
2. 双前端与权限骨架：完成平台后台和商家端菜单、路由、登录态、请求头和权限隔离。
3. 基础资料与库存骨架：商品、仓库、SN、配件、套餐、文件上传和库存流水。
4. 订单主流程 MVP：录单、库存检测、SN 绑定、发货、归还验机、完结和取消追回。
5. 仓库作业、物流、小程序、财务、联盟、市场和平台报表逐步完善。

## 许可证

本项目基于 RuoYi-Cloud 二次开发。仓库内 RuoYi 原始工程保留其 MIT License 约定，业务二次开发部分请以项目实际授权策略为准。
