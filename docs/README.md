# 项目文档

本目录只维护可随源码公开的 Markdown 资料。需求与设计以这里的文档为准；实现状态以代码、迁移、测试和[开发进度快照](./development-progress.md)核对。原始需求文件、个人工作目录、业务数据和生成的 Word/PDF/页面截图不作为公开文档依赖。

## 项目与进度

- [开发进度与后续计划](./development-progress.md)：当前实现状态、未完成项和本地验证范围。
- [RuoYi-Cloud 开发计划](./design/ruoyi-cloud-development-plan.md)：阶段目标、技术边界和验收门禁。
- [第 1 阶段完成状态](./design/phase-1-completion-status.md)：2026-08-14 身份与权限阶段的历史验收记录，不代表当前整体状态。
- [1.0 需求基线与实施差异](./design/requirements-baseline-and-gaps.md)：产品需求、当前能力和差异整理。
- [平台端商户管理能力实施计划](./design/platform-merchant-management-plan.md)
- [平台与商户微服务拆分任务](./design/platform-merchant-service-split.md)

## 架构与业务设计

- [项目架构图](./design/architecture.md)
- [详细功能规格书](./design/rental-platform-functional-specification.md)
- [项目总体设计](./design/rental-platform-design.md)
- [数据库边界设计](./design/rental-database-boundaries.md)
- [商品与套餐库存数据结构](./design/product-package-inventory-data-model.md)
- [库存概览与剩余库存规则](./design/inventory-availability-rules.md)
- [商户订单流程与操作状态](./design/merchant-order-workflow-and-status.md)
- [多仓联盟设计](./design/multi-warehouse-alliance-design.md)
- [商户注册等级与邀请推广](./design/merchant-membership-and-referrals.md)
- [主键与关联字段术语规范](./design/primary-key-and-relation-terms.md)
- [行政区划与仓库地址设计](./design/administrative-regions-and-warehouse-addresses.md)
- [订单收件人行政区划与全局天气设计](./design/order-recipient-regions-and-weather.md)
- [统一业务编码规则](./design/business-code-conventions.md)
- [产品类目、接口审计与软删除设计](./design/product-categories-audit-and-soft-delete.md)

## 接口、集成与部署

- [项目路由清单](./route-catalog.md)
- [阿里云 OSS 与短信集成](./integrations/aliyun-oss-sms.md)
- [部署指南](../deploy/README.md)
- [后端工程说明](../backend/ruoyi-cloud/README.md)
- [平台端工程说明](../frontend/admin-web/README.md)
- [商户端工程说明](../frontend/merchant-web/README.md)

## 发布边界

数据库目录仅保留初始化结构、必要种子数据和版本化迁移；不得把数据库备份、真实商户/客户/订单/库存数据、环境文件、凭据或证书加入公开仓库。部署文档给出通用配置方式，不包含任何环境的真实密钥。
