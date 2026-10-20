# 迁若租赁商家端

本目录为租赁系统商家端前端工程，基于 RuoYi-Cloud-Vue3-TypeScript 二次开发。

## 定位

- 商家端只面向租赁商家及其员工。
- 商家只能管理本商户主体下的数据，包括订单、仓库、商品、SN、套餐、库存、物流、售后、联盟协作、周转市场和个人中心。
- 平台内部管理后台位于 `frontend/admin-web`，不要在商家端放平台运营、平台财务、平台风控等内部菜单。

## 本地启动

```bash
npm install
npm run dev
```

默认开发端口：`81`

## 端隔离约定

- 应用名：`rental-merchant-web`
- 页面标题：`迁若租赁商家端`
- 请求头：`X-Client-Type: merchant`
- 请求头：`X-App-Code: rental-merchant-web`
- Token Cookie：`Merchant-Token`

后端必须根据商户、租户、仓库权限做服务端数据隔离，不能只依赖前端菜单隐藏。
