# 迁渃租赁平台后台

本目录为租赁系统平台内部管理后台前端工程，基于 RuoYi-Cloud-Vue3-TypeScript 二次开发。

## 定位

- 平台后台只面向平台内部人员。
- 用于管理商户认证、会员统计、平台财务、票据、总库存、订单统计、市场风控、推广佣金、系统配置、日志监控等平台级功能。
- 商家业务操作端位于 `frontend/merchant-web`，不要在平台后台中混入商家日常作业入口。

## 本地启动

```bash
npm install
npm run dev
```

默认开发端口：`80`

## 端隔离约定

- 应用名：`rental-admin-web`
- 页面标题：`迁渃租赁平台后台`
- 请求头：`X-Client-Type: platform`
- 请求头：`X-App-Code: rental-admin-web`
- Token Cookie：`Admin-Token`

后端必须对平台内部角色继续细分运营、财务、风控、客服、系统管理员等权限；平台跨商户查看敏感数据必须记录审计日志。
