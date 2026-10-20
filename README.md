# 迁若租赁平台

面向租赁商家的业务平台，提供订单履约、商品与库存、仓库、物流、售后、财务和平台治理能力。项目基于 RuoYi-Cloud 二次开发，由 Java 微服务和独立的平台端、商户端前端组成。

> 项目计划、当前完成情况和本地验证范围见[开发进度](./docs/development-progress.md)。详细文档入口见[文档目录](./docs/README.md)。

## 项目计划与开发进度

截至 2026-10-19，项目基础工程和双端应用已建立；订单、库存、物流、工作台、财务及运营分析已有多项实现，整体仍在持续开发和验收中。页面或接口存在不等于完整业务闭环已验收。

| 领域 | 当前进度 | 后续重点 |
| --- | --- | --- |
| 订单履约、售后、物流、财务 | 核心订单、订单详情、物流查询、售后及账户基础能力已实现，闭环仍在完善 | 完成纠纷/赔付、物流下单与取消、支付回调及财务端到端验收 |
| 工作台与平台治理 | 工单、工作台、知识内容、订单详情及部分回收能力已实现 | 补齐跨角色验收和更多业务对象的回收治理 |
| 库存 | 预测、告警和外部库存同步基础已实现 | 完善高级预测模型、异常场景和真实环境验收 |
| 周转市场、经营分析、运营风控 | 市场治理与经营分析首版已实现 | 完善指标口径、风险覆盖和持续运行验收 |

详细状态、下一步顺序和验证说明以[开发进度文档](./docs/development-progress.md)为准；整体阶段任务见[RuoYi-Cloud 开发计划](./docs/design/ruoyi-cloud-development-plan.md)。

## 功能与架构

- **平台端**：商户与认证治理、订单和库存监管、财务、客服工单、运营分析、系统配置与运维。
- **商户端**：工作台、订单、商品、库存、仓库、物流、售后、员工协作和财务。
- **后端**：RuoYi-Cloud 微服务，包括网关、认证、平台服务、商户服务、租赁业务、文件和任务服务。
- **数据边界**：`ry-cloud` 保存 RuoYi 平台系统数据；`ry_rental` 保存商户控制面和平台治理数据；`ry_business` 保存租赁执行面数据。服务边界及迁移规则见[数据库边界设计](./docs/design/rental-database-boundaries.md)。

### 技术栈

- Java 17、Spring Boot、Spring Cloud、Nacos、Redis、MyBatis
- Vue 3、Vite、Element Plus、Pinia、Vue Router、Axios、ECharts
- MySQL 8、Docker Compose；Kubernetes 部署配置

### 目录

```text
backend/ruoyi-cloud/     后端微服务
frontend/admin-web/      平台端
frontend/merchant-web/   商户端
database/mysql/          新环境数据库结构
database/seed/           初始化字典数据
database/migrations/     当前数据库合并基线（新库初始化）
deploy/                  本地及 Kubernetes 部署配置
docs/                    项目设计、计划、进度和运维文档
scripts/                 构建、迁移和部署入口
```

仓库中的数据库脚本保留初始化结构、必要种子数据和一个合并后的数据库基线，不包含商户、客户、订单、库存测试数据或数据库备份。该基线面向新环境；已有数据库切换时必须先达到部署指南中的迁移截止版本。

## 本地运行

需要 Docker Desktop、Git 和可用的本地资源。首次运行前复制环境模板，并在本地 `.env` 中设置仅用于开发的密码和密钥：

```bash
cp deploy/.env.example deploy/.env
./scripts/deploy.sh local
```

平台端和商户端默认入口分别为 [http://localhost:8088](http://localhost:8088) 和 [http://localhost:8089](http://localhost:8089)。脚本行为、环境变量和 Kubernetes 部署说明见[部署指南](./deploy/README.md)。不要将本地 `.env`、密钥、生产配置或业务数据提交到仓库。

## 文档

- [文档目录](./docs/README.md)
- [项目架构图](./docs/design/architecture.md)
- [详细功能规格](./docs/design/rental-platform-functional-specification.md)
- [数据库边界设计](./docs/design/rental-database-boundaries.md)
- [项目路由清单](./docs/route-catalog.md)
- [阿里云 OSS 与短信集成说明](./docs/integrations/aliyun-oss-sms.md)
- [部署指南](./deploy/README.md)

仓库以 Markdown 作为公开维护的需求与设计文档格式。原始需求材料、生成的 Word/PDF/页面截图和本地文档处理产物不作为项目运行或开发的依赖。

## 安全与许可证

请通过环境变量或密钥管理服务配置凭据；不要在源码、文档、数据库种子和提交记录中放入密码、令牌、证书、真实客户资料或生产数据。安全问题请勿公开提交可利用的细节，使用仓库维护者提供的私下渠道报告。

本项目采用 MIT License，详见根目录 [`LICENSE`](./LICENSE)。RuoYi-Cloud 及其他第三方依赖保留各自的许可证与版权声明；再分发时应一并遵守。
