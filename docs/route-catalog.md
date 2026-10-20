# 项目路由清单

> 更新时间：2026-09-29  
> 统计范围：网关路由、后端 Spring Controller、平台端 Vue Router、商户端 Vue Router、数据库动态菜单。  
> 本文路径均为客户端访问网关时使用的外部路径，不是微服务内部路径。

## 1. 路由约定

| 入口                                                                                                                                                                           | 目标服务               | 转发规则            | 用途                       |
| ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------ | --------------- | ------------------------ |
| `/auth/**`                                                                                                                                                                   | `ruoyi-auth`       | 去除 `/auth`      | 认证与令牌                    |
| `/merchant/orders`、`/merchant/orders/**`                                                                                                                                     | `rental-business`  | 保留原路径；优先于通用商户路由 | 商户订单列表及后续订单状态接口          |
| `/merchant/warehouse/**`、`/merchant/product/**`、`/merchant/sn/**`、`/merchant/accessory/**`、`/merchant/package/**`、`/merchant/inventory/**`、`/merchant/alliance-inventory/**` | `rental-business`  | 保留原路径；优先于通用商户路由 | 商户租赁主数据、库存和联盟共享库存接口      |
| `/merchant/alliances/**`                                                                                                                                                     | `merchant-service` | 保留原路径           | 联盟主体、成员关系和共享授权配置         |
| `/merchant/**`                                                                                                                                                               | `merchant-service` | 保留原路径           | 商户身份、上下文、菜单、角色及商户管理接口    |
| `/merchant/order-channel/**`、`/merchant/batch/**`、`/merchant/file-quota/**`、`/merchant/protocol/**`                                                                          | `rental-business`  | 保留原路径；优先于通用商户路由 | 商户渠道转移、批处理、文件配额和协议接口     |
| `/admin/**`                                                                                                                                                                  | `platform-service` | 去除 `/admin`     | 平台系统接口                   |
| `/code/**`                                                                                                                                                                   | `ruoyi-gen`        | 去除 `/code`      | 代码生成；`GET /code` 由网关自身处理 |
| `/schedule/**`                                                                                                                                                               | `ruoyi-job`        | 去除 `/schedule`  | 定时任务                     |
| `/file/**`                                                                                                                                                                   | `ruoyi-file`       | 去除 `/file`      | 文件服务                     |

`rental-business` 已作为独立服务注册，端口为 `9220`。订单和租赁主数据接口使用精确路由进入  
`rental-business`，不得把商品、SN、配件、套餐、仓库或库存 Controller 放回 `merchant-service`。

网关同时启用了服务发现自动路由，理论上还可形成 `/{service-id}/**` 路径。业务前端不得依赖自动路由，应只使用上表中的稳定入口。

平台请求使用 `X-Client-Type: platform`，商户请求使用 `X-Client-Type: merchant`。商户接口必须使用 `/merchant/**`；平台访问商户路径或商户访问平台路径会被拒绝。

## 2. 租赁核心业务服务

| 方法                    | 内部路径                                            | 说明                                    |
| --------------------- | ----------------------------------------------- | ------------------------------------- |
| `GET`                 | `/actuator/health`                              | `rental-business:9220` 服务健康检查，不要求终端标识 |
| `GET`                 | `/internal/rental-business/ping`                | 内部服务探活，仅允许携带统一内部来源标识的服务间调用            |
| `GET`                 | `/merchant/orders`                              | 商户订单工作台分页查询；租户、商户和仓库范围由服务端登录态确定       |
| `POST`                | `/merchant/orders/{orderId}/prepare`            | 订单备货；仅将待发货订单置为已备货，不覆盖其他订单字段           |
| `GET`                 | `/merchant/orders/{orderId}/notes`              | 查询当前商户指定订单的历史批注，按创建时间倒序返回并关联商户用户昵称    |
| `GET`                 | `/merchant/orders/{orderId}/edit-logs`          | 查询当前商户指定订单的字段编辑记录，按创建时间倒序返回并关联商户用户昵称  |
| `GET`                 | `/merchant/warehouse/list`                      | 仓库分页查询；按商户和用户仓库范围过滤                   |
| `GET/POST/PUT/DELETE` | `/merchant/warehouse[/{id}]`                    | 仓库详情、新增、修改和软删除                        |
| `GET`                 | `/merchant/product/list`                        | 商品分页查询；只展示当前商户已选择分类                   |
| `GET/POST/PUT/DELETE` | `/merchant/product[/{id}]`                      | 商品详情、新增、修改和软删除                        |
| `GET/POST/PUT/DELETE` | `/merchant/sn[/{id}]`                           | SN 列表、详情、新增、修改和软删除                    |
| `GET/POST/PUT/DELETE` | `/merchant/accessory[/{id}]`                    | 逐件配件列表、详情、新增、修改和软删除                   |
| `GET/POST/PUT/DELETE` | `/merchant/package[/{id}]`                      | 套餐版本化 BOM 的查询、发布和软删除                  |
| `GET`                 | `/merchant/inventory/list`                      | 按仓库、商品和型号汇总库存；不再使用资源池                 |
| `GET`                 | `/merchant/inventory/{productId}/{warehouseId}` | 查询指定商品在指定仓库的库存详情                      |
| `POST`                | `/merchant/inventory/adjust`                    | 调整数量型库存并追加库存流水                        |
| `DELETE`              | `/merchant/inventory/{productId}/{warehouseId}` | 仅软删除零库存且无预占的数量库存余额，库存流水保留             |
| `POST`                | `/merchant/inventory/check-package`             | 按套餐 BOM 校验指定仓库的商品和配件可用量               |

当前服务已提供订单列表、主数据/库存基础接口、订单渠道、渠道转移、批量导入导出和库存工作流接口；订单核销、售后和复杂履约状态仍按后续领域任务实现。

### 2.1 订单渠道与库存工作流

| 方法         | 路径                                                                 | 说明                   |
| ---------- | ------------------------------------------------------------------ | -------------------- |
| `GET`      | `/merchant/order-channel/list`                                     | 查询平台联盟渠道和当前商户自定义渠道   |
| `GET`      | `/merchant/order-channel/package/{packageId}`                      | 查询套餐渠道绑定             |
| `POST/PUT` | `/merchant/order-channel`                                          | 新增/修改商户自定义渠道         |
| `DELETE`   | `/merchant/order-channel/{channelId}`                              | 软删除商户自定义渠道           |
| `POST`     | `/merchant/order-channel/package/transfer`                         | 转移套餐默认渠道并记录历史        |
| `POST`     | `/merchant/order-channel/orders/transfer`                          | 按订单唯一编号批量转移渠道并执行版本校验 |
| `POST`     | `/merchant/inventory/workflow/reserve`                             | 按套餐 BOM 预占库存         |
| `GET`      | `/merchant/inventory/workflow/reservation/{reservationId}`         | 查询预占主表和预占明细          |
| `POST`     | `/merchant/inventory/workflow/reservation/{reservationId}/release` | 主动释放预占               |
| `POST`     | `/merchant/inventory/workflow/reservation/{reservationId}/consume` | 消耗预占并扣减真实库存          |
| `POST`     | `/merchant/inventory/workflow/reservation/{reservationId}/expire`  | 过期释放预占               |
| `POST`     | `/merchant/inventory/workflow/transition`                          | 数量库存或逐件库存状态迁移        |
| `GET`      | `/merchant/inventory/shared`                                       | 查询联盟授权仓库和商品的共享库存     |
| `GET`      | `/merchant/alliance-scopes`                                        | 查询当前商户配置的共享仓库和商品范围   |
| `POST`     | `/merchant/alliance-scopes/warehouses`                             | 保存联盟共享仓库范围           |
| `POST`     | `/merchant/alliance-scopes/products`                               | 保存联盟共享商品范围           |
| `DELETE`   | `/merchant/alliance-scopes/warehouses/{scopeId}`                   | 删除联盟共享仓库范围           |
| `DELETE`   | `/merchant/alliance-scopes/products/{scopeId}`                     | 删除联盟共享商品范围           |
| `GET`      | `/merchant/orders/alliance-options`                                | 查询当前商户可用的联盟转单目标      |
| `POST`     | `/merchant/orders/alliance`                                        | 创建联盟协助订单             |
| `GET`      | `/merchant/orders/alliance-tasks`                                  | 查询当前商户发起或收到的联盟协助订单   |
| `POST`     | `/merchant/orders/alliance-tasks/{allianceOrderId}/accept`         | 接受联盟协助订单并创建子订单       |
| `POST`     | `/merchant/orders/alliance-tasks/{allianceOrderId}/reject`         | 拒绝联盟协助订单             |

### 2.2 批量、配额和协议

平台租赁管理接口由 `platform-service` 提供 `/admin/rental/**` 外部入口，`rental-business` 只保留受信任的内部命令和查询接口；旧 `/platform/**` 网关路由和浏览器入口已删除，不提供兼容路径。

| 方法         | 路径                                                        | 说明                     |
| ---------- | --------------------------------------------------------- | ---------------------- |
| `POST`     | `/merchant/batch/{resource}/import`                       | 导入仓库、商品、SN、配件或套餐 Excel |
| `GET`      | `/merchant/batch/{resource}/export`                       | 导出当前商户范围内的主数据 Excel    |
| `GET`      | `/merchant/file-quota`                                    | 查询商户文件配额               |
| `GET`      | `/merchant/file-quota/ledger`                             | 查询文件操作审计流水             |
| `GET`      | `/merchant/protocol/list`                                 | 查询当前有效协议版本             |
| `POST`     | `/merchant/protocol/accept`                               | 确认协议版本并保存正文哈希          |
| `GET`      | `/admin/rental/order-channel/list`                        | 查询平台联盟订单渠道             |
| `POST/PUT` | `/admin/rental/order-channel`                             | 平台联盟渠道新增/修改            |
| `DELETE`   | `/admin/rental/order-channel/{channelId}`                 | 平台联盟渠道软删除              |
| `GET`      | `/admin/rental/order/list`                                | 分页查询平台可见的全部商户订单        |
| `GET`      | `/admin/rental/order/{orderId}`                           | 查询平台订单脱敏详情和处理记录        |
| `POST`     | `/admin/rental/order/{orderId}/customer`                  | 按独立权限查看完整客户信息并记录平台操作日志 |
| `GET`      | `/admin/rental/merchant-overview/list`                    | 分页查询平台商户经营概况           |
| `GET`      | `/admin/rental/merchant-overview/{memberCode}/business`   | 查询商户仓库、商品、套餐和库存只读明细    |
| `GET`      | `/admin/rental/merchant-overview/{memberCode}/governance` | 按独立权限查询商户履约、物流和风险治理汇总  |
| `GET`      | `/admin/rental/protocol/list`                             | 查询平台协议全部版本             |
| `POST/PUT` | `/admin/rental/protocol`                                  | 新增/修改协议草稿              |
| `POST`     | `/admin/rental/protocol/{protocolId}/publish`             | 发布协议版本并下线旧版本           |

## 3. 网关自身路由

| 方法    | 路径      | 说明      |
| ----- | ------- | ------- |
| `GET` | `/code` | 获取图形验证码 |

## 4. 认证服务

| 方法       | 外部路径                 | 说明                      |
| -------- | -------------------- | ----------------------- |
| `POST`   | `/auth/login`        | 账号密码登录；按客户端类型选择平台或商户用户表 |
| `POST`   | `/auth/smsLogin`     | 商户手机号验证码登录              |
| `DELETE` | `/auth/logout`       | 退出登录                    |
| `POST`   | `/auth/refresh`      | 刷新令牌                    |
| `POST`   | `/auth/register`     | 注册平台用户                  |
| `POST`   | `/auth/unlockscreen` | 解锁屏幕                    |

## 5. 平台系统接口

### 5.1 平台用户

| 方法       | 路径                              |
| -------- | ------------------------------- |
| `GET`    | `/admin/user/list`              |
| `POST`   | `/admin/user/export`            |
| `POST`   | `/admin/user/importData`        |
| `POST`   | `/admin/user/importTemplate`    |
| `GET`    | `/admin/user/info/{username}`   |
| `POST`   | `/admin/user/register`          |
| `PUT`    | `/admin/user/recordlogin`       |
| `GET`    | `/admin/user/getInfo`           |
| `GET`    | `/admin/user/`                  |
| `GET`    | `/admin/user/{userId}`          |
| `POST`   | `/admin/user`                   |
| `PUT`    | `/admin/user`                   |
| `DELETE` | `/admin/user/{userIds}`         |
| `PUT`    | `/admin/user/resetPwd`          |
| `PUT`    | `/admin/user/changeStatus`      |
| `GET`    | `/admin/user/authRole/{userId}` |
| `PUT`    | `/admin/user/authRole`          |
| `GET`    | `/admin/user/deptTree`          |

`/info/{username}`、`/register` 和 `/recordlogin` 主要供服务间内部调用。

### 5.2 平台个人中心

| 方法     | 路径                                       |
| ------ | ---------------------------------------- |
| `GET`  | `/admin/user/profile`                    |
| `PUT`  | `/admin/user/profile`                    |
| `PUT`  | `/admin/user/profile/updatePwd`          |
| `PUT`  | `/admin/user/profile/admin/initPassword` |
| `POST` | `/admin/user/profile/avatar`             |

### 5.3 角色

| 方法       | 路径                                     |
| -------- | -------------------------------------- |
| `GET`    | `/admin/role/list`                     |
| `POST`   | `/admin/role/export`                   |
| `GET`    | `/admin/role/{roleId}`                 |
| `POST`   | `/admin/role`                          |
| `PUT`    | `/admin/role`                          |
| `PUT`    | `/admin/role/dataScope`                |
| `PUT`    | `/admin/role/changeStatus`             |
| `DELETE` | `/admin/role/{roleIds}`                |
| `GET`    | `/admin/role/optionselect`             |
| `GET`    | `/admin/role/authUser/allocatedList`   |
| `GET`    | `/admin/role/authUser/unallocatedList` |
| `PUT`    | `/admin/role/authUser/cancel`          |
| `PUT`    | `/admin/role/authUser/cancelAll`       |
| `PUT`    | `/admin/role/authUser/selectAll`       |
| `GET`    | `/admin/role/deptTree/{roleId}`        |

### 5.4 菜单

| 方法       | 路径                                        |
| -------- | ----------------------------------------- |
| `GET`    | `/admin/menu/list`                        |
| `GET`    | `/admin/menu/{menuId}`                    |
| `GET`    | `/admin/menu/treeselect`                  |
| `GET`    | `/admin/menu/roleMenuTreeselect/{roleId}` |
| `POST`   | `/admin/menu`                             |
| `PUT`    | `/admin/menu`                             |
| `PUT`    | `/admin/menu/updateSort`                  |
| `DELETE` | `/admin/menu/{menuId}`                    |
| `GET`    | `/admin/menu/getRouters`                  |

### 5.5 部门与岗位

| 方法       | 路径                                  |
| -------- | ----------------------------------- |
| `GET`    | `/admin/dept/list`                  |
| `GET`    | `/admin/dept/list/exclude/{deptId}` |
| `GET`    | `/admin/dept/{deptId}`              |
| `POST`   | `/admin/dept`                       |
| `PUT`    | `/admin/dept`                       |
| `PUT`    | `/admin/dept/updateSort`            |
| `DELETE` | `/admin/dept/{deptId}`              |
| `GET`    | `/admin/post/list`                  |
| `POST`   | `/admin/post/export`                |
| `GET`    | `/admin/post/{postId}`              |
| `POST`   | `/admin/post`                       |
| `PUT`    | `/admin/post`                       |
| `DELETE` | `/admin/post/{postIds}`             |
| `GET`    | `/admin/post/optionselect`          |

### 5.6 字典与参数

| 方法       | 路径                                    |
| -------- | ------------------------------------- |
| `GET`    | `/admin/dict/type/list`               |
| `POST`   | `/admin/dict/type/export`             |
| `GET`    | `/admin/dict/type/{dictId}`           |
| `POST`   | `/admin/dict/type`                    |
| `PUT`    | `/admin/dict/type`                    |
| `DELETE` | `/admin/dict/type/{dictIds}`          |
| `DELETE` | `/admin/dict/type/refreshCache`       |
| `GET`    | `/admin/dict/type/optionselect`       |
| `GET`    | `/admin/dict/data/list`               |
| `POST`   | `/admin/dict/data/export`             |
| `GET`    | `/admin/dict/data/{dictCode}`         |
| `GET`    | `/admin/dict/data/type/{dictType}`    |
| `POST`   | `/admin/dict/data`                    |
| `PUT`    | `/admin/dict/data`                    |
| `DELETE` | `/admin/dict/data/{dictCodes}`        |
| `GET`    | `/admin/config/list`                  |
| `POST`   | `/admin/config/export`                |
| `GET`    | `/admin/config/{configId}`            |
| `GET`    | `/admin/config/configKey/{configKey}` |
| `POST`   | `/admin/config`                       |
| `PUT`    | `/admin/config`                       |
| `DELETE` | `/admin/config/{configIds}`           |
| `DELETE` | `/admin/config/refreshCache`          |

### 5.7 通知与日志

| 方法       | 路径                                    |
| -------- | ------------------------------------- |
| `GET`    | `/admin/notice/list`                  |
| `GET`    | `/admin/notice/{noticeId}`            |
| `POST`   | `/admin/notice`                       |
| `PUT`    | `/admin/notice`                       |
| `GET`    | `/admin/notice/listTop`               |
| `POST`   | `/admin/notice/markRead`              |
| `POST`   | `/admin/notice/markReadAll`           |
| `DELETE` | `/admin/notice/{noticeIds}`           |
| `GET`    | `/admin/operlog/list`                 |
| `POST`   | `/admin/operlog/export`               |
| `DELETE` | `/admin/operlog/{operIds}`            |
| `DELETE` | `/admin/operlog/clean`                |
| `POST`   | `/admin/operlog`                      |
| `GET`    | `/admin/logininfor/list`              |
| `POST`   | `/admin/logininfor/export`            |
| `DELETE` | `/admin/logininfor/{infoIds}`         |
| `DELETE` | `/admin/logininfor/clean`             |
| `GET`    | `/admin/logininfor/unlock/{userName}` |
| `POST`   | `/admin/logininfor`                   |


### 5.8 在线用户

| 方法       | 路径                        |
| -------- | ------------------------- |
| `GET`    | `/admin/online/list`      |
| `DELETE` | `/admin/online/{tokenId}` |

## 6. 平台租赁管理接口

### 6.1 商户主体

| 方法       | 路径                                      |
| -------- | --------------------------------------- |
| `GET`    | `/admin/rental/member/list`             |
| `POST`   | `/admin/rental/member/export`           |
| `GET`    | `/admin/rental/member/{memberId}`       |
| `POST`   | `/admin/rental/member`                  |
| `PUT`    | `/admin/rental/member`                  |
| `PUT`    | `/admin/rental/member/{memberId}/audit` |
| `DELETE` | `/admin/rental/member/{memberIds}`      |

### 6.2 商户用户

| 方法       | 路径                                                       | 说明                        |
| -------- | -------------------------------------------------------- | ------------------------- |
| `GET`    | `/admin/rental/merchantUser/list`                        | 按平台选择的商户查询用户              |
| `GET`    | `/admin/rental/merchantUser/{merchantUserId}`            | 查询商户用户详情                  |
| `GET`    | `/admin/rental/merchantUser/warehouseOptions/{memberId}` | 查询指定商户的仓库                 |
| `GET`    | `/admin/rental/merchantUser/roleOptions/{memberId}`      | 查询指定商户已有的运行角色，供平台管理商户用户使用 |
| `GET`    | `/admin/rental/merchantUser/menuTreeselect/{memberId}`   | 查询指定商户可用的菜单树，供平台管理商户用户使用  |
| `POST`   | `/admin/rental/merchantUser`                             | 新增商户用户                    |
| `PUT`    | `/admin/rental/merchantUser`                             | 修改商户用户                    |
| `PUT`    | `/admin/rental/merchantUser/changeStatus`                | 修改商户用户状态                  |
| `PUT`    | `/admin/rental/merchantUser/resetPwd`                    | 重置商户用户密码                  |
| `DELETE` | `/admin/rental/merchantUser/{merchantUserIds}`           | 软删除商户用户                   |

### 6.3 商户角色

平台端这里管理的是所有商户可使用的全局角色模板，不包含所属商户筛选，也不接收 `memberId`。商户端实际用户授权仍由当前商户上下文下的运行角色完成。

| 方法       | 路径                                                               | 说明            |
| -------- | ---------------------------------------------------------------- | ------------- |
| `GET`    | `/admin/rental/merchantRole/list`                                | 查询平台全局角色模板    |
| `GET`    | `/admin/rental/merchantRole/{roleTemplateId}`                    | 查询全局角色模板详情    |
| `POST`   | `/admin/rental/merchantRole`                                     | 新增全局角色模板      |
| `PUT`    | `/admin/rental/merchantRole`                                     | 修改全局角色模板及菜单权限 |
| `DELETE` | `/admin/rental/merchantRole/{roleTemplateIds}`                   | 软删除全局角色模板     |
| `GET`    | `/admin/rental/merchantRole/menuTreeselect`                      | 查询全局商户菜单树     |
| `GET`    | `/admin/rental/merchantRole/roleMenuTreeselect/{roleTemplateId}` | 查询角色模板菜单权限    |

### 6.4 平台产品分类

| 方法       | 路径                                   | 说明            |
| -------- | ------------------------------------ | ------------- |
| `GET`    | `/admin/rental/class/tree`           | 查询平台多级产品分类树   |
| `GET`    | `/admin/rental/class/{classId}`      | 查询平台产品分类详情    |
| `POST`   | `/admin/rental/class`                | 新增平台产品分类      |
| `PUT`    | `/admin/rental/class`                | 修改平台产品分类      |
| `DELETE` | `/admin/rental/class/{classId}`      | 软删除平台产品分类     |
| `GET`    | `/internal/platform/product-classes` | 向内部服务发布平台有效分类 |

### 6.5 平台标准品牌型号

| 方法       | 路径                                    | 说明               |
| -------- | ------------------------------------- | ---------------- |
| `GET`    | `/admin/rental/brand-model/list`      | 分页查询平台标准品牌型号     |
| `GET`    | `/admin/rental/brand-model/{modelId}` | 查询平台标准品牌型号详情     |
| `POST`   | `/admin/rental/brand-model`           | 新增平台标准品牌型号       |
| `PUT`    | `/admin/rental/brand-model`           | 修改平台标准品牌型号       |
| `DELETE` | `/admin/rental/brand-model/{modelId}` | 软删除未被引用的平台标准品牌型号 |
| `GET`    | `/internal/platform/brand-models`     | 向商户服务发布平台有效标准型号  |

### 6.6 平台品牌管理

| 方法       | 路径                              | 说明                |
| -------- | ------------------------------- | ----------------- |
| `GET`    | `/admin/rental/brand/list`      | 按名称、状态和产品分类查询平台品牌 |
| `GET`    | `/admin/rental/brand/{brandId}` | 查询平台品牌详情及绑定分类     |
| `POST`   | `/admin/rental/brand`           | 新增品牌并绑定一个或多个产品分类  |
| `PUT`    | `/admin/rental/brand`           | 修改品牌名称、状态、排序和分类关系 |
| `DELETE` | `/admin/rental/brand/{brandId}` | 软删除未被型号引用的平台品牌    |

## 7. 商户独立接口

### 7.1 商户公开入驻

以下接口不要求登录，但必须使用商户端请求头。验证码由商户服务调用平台系统配置中的阿里云短信服务发送，并在 Redis 中限频和一次性核销：

| 方法     | 路径                                     | 说明                |
| ------ | -------------------------------------- | ----------------- |
| `POST` | `/merchant/public/registration/code`   | 获取入驻查询或提交验证码      |
| `POST` | `/merchant/public/registration`        | 提交商户入驻申请，状态固定为待审核 |
| `POST` | `/merchant/public/registration/lookup` | 使用手机号和验证码查询已提交资料  |
| `PUT`  | `/merchant/public/registration`        | 修改被驳回的资料并重新提交     |

商户服务通过以下平台内部接口保存申请，外部客户端不能访问：

| 方法     | 内部路径                                                     | 说明       |
| ------ | -------------------------------------------------------- | -------- |
| `POST` | `/internal/platform/merchant-registration`               | 新建待审核申请  |
| `GET`  | `/internal/platform/merchant-registration/phone/{phone}` | 按手机号查询申请 |
| `PUT`  | `/internal/platform/merchant-registration`               | 驳回后重新提交  |

### 7.2 商户用户与上下文

| 方法     | 路径                                    | 说明                   |
| ------ | ------------------------------------- | -------------------- |
| `GET`  | `/merchant/user/getInfo`              | 商户端获取当前用户、角色、权限和密码状态 |
| `GET`  | `/merchant/user/profile`              | 获取商户个人资料             |
| `PUT`  | `/merchant/user/profile`              | 修改商户个人资料             |
| `PUT`  | `/merchant/user/profile/updatePwd`    | 修改密码                 |
| `PUT`  | `/merchant/user/profile/initPassword` | 首次登录设置初始密码           |
| `POST` | `/merchant/user/profile/avatar`       | 上传头像                 |
| `GET`  | `/merchant/context`                   | 获取租户、商户、会员和仓库上下文     |
| `GET`  | `/merchant/context/warehouses`        | 获取当前用户可访问仓库          |

认证服务通过服务发现直接调用以下内部契约，不经过公网网关：

| 方法     | 内部路径                                              | 说明                       |
| ------ | ------------------------------------------------- | ------------------------ |
| `GET`  | `/internal/merchant/users/by-username/{username}` | 按用户名取得独立商户身份 DTO         |
| `GET`  | `/internal/merchant/users/by-phone/{phone}`       | 按手机号取得独立商户身份 DTO         |
| `POST` | `/internal/merchant/users/summaries`              | 按租户、商户和用户唯一编号批量取得操作人展示摘要 |
| `PUT`  | `/internal/merchant/users/login-record`           | 记录商户登录时间和 IP             |
| `POST` | `/internal/merchant/users/owner`                  | 平台审核通过后幂等开通商户管理员         |

### 7.3 商户菜单与角色

平台端通过 `/rental/merchantMenu/**` 维护商户端全局菜单模板。平台配置的启用菜单是商户端的唯一显示来源：商户管理员（owner）实时拥有全部启用菜单，普通商户用户通过商户管理员配置的角色和用户菜单授权获得子集。平台停用或软删除菜单后，商户端下次刷新路由立即不再显示。

以下接口由 `merchant-service` 独立提供，菜单模板只读，角色及用户授权强制按登录态中的租户和商户上下文隔离：

| 方法       | 路径                                           | 说明             |
| -------- | -------------------------------------------- | -------------- |
| `GET`    | `/merchant/menu/getRouters`                  | 获取当前商户用户动态路由   |
| `GET`    | `/merchant/menu/list`                        | 获取商户菜单模板列表     |
| `GET`    | `/merchant/menu/{menuId}`                    | 获取菜单模板详情       |
| `GET`    | `/merchant/menu/treeselect`                  | 获取菜单授权树        |
| `GET`    | `/merchant/menu/roleMenuTreeselect/{roleId}` | 获取角色菜单授权树和已选节点 |
| `GET`    | `/merchant/role/list`                        | 查询当前商户角色       |
| `GET`    | `/merchant/role/{roleId}`                    | 查询当前商户角色详情     |
| `POST`   | `/merchant/role`                             | 新增当前商户角色       |
| `PUT`    | `/merchant/role`                             | 修改角色及菜单权限      |
| `PUT`    | `/merchant/role/changeStatus`                | 修改角色状态         |
| `DELETE` | `/merchant/role/{roleIds}`                   | 删除未分配用户的角色     |
| `GET`    | `/merchant/role/authUser/allocatedList`      | 查询角色已授权用户      |
| `GET`    | `/merchant/role/authUser/unallocatedList`    | 查询角色未授权用户      |
| `PUT`    | `/merchant/role/authUser/cancel`             | 取消单个用户授权       |
| `PUT`    | `/merchant/role/authUser/cancelAll`          | 批量取消用户授权       |
| `PUT`    | `/merchant/role/authUser/selectAll`          | 批量授予用户角色       |

### 7.4 商户业务审计日志

以下接口由 `merchant-service` 提供，权限分别为 `merchant:operlog:list` 和 `merchant:operlog:export`。接口读取商户业务审计表，租户和商户条件只取自服务端登录态，前端不能指定查询其他商户；请求方式、请求地址等平台级请求日志不在商户页面展示：

| 方法     | 路径                            | 说明                  |
| ------ | ----------------------------- | ------------------- |
| `GET`  | `/merchant/operlog/list`      | 分页查询当前商户所有用户的操作日志   |
| `GET`  | `/merchant/operlog/{auditId}` | 查询当前商户一条业务日志的前后变更详情 |
| `POST` | `/merchant/operlog/export`    | 导出当前商户操作日志          |

### 7.5 商户经营分类

| 方法    | 路径                         | 说明             |
| ----- | -------------------------- | -------------- |
| `GET` | `/merchant/class/options`  | 查询平台允许选择的有效分类树 |
| `GET` | `/merchant/class/selected` | 查询当前商户已选分类唯一编号 |
| `GET` | `/merchant/class/tree`     | 查询当前商户业务可见分类树  |
| `PUT` | `/merchant/class/selected` | 保存当前商户经营分类     |

### 7.6 商户常用品牌型号

| 方法    | 路径                               | 说明                      |
| ----- | -------------------------------- | ----------------------- |
| `GET` | `/merchant/brand-model/options`  | 查询当前商户经营分类内的标准型号并回显常用状态 |
| `GET` | `/merchant/brand-model/selected` | 查询当前商户已选常用型号唯一编号        |
| `PUT` | `/merchant/brand-model/selected` | 保存当前商户常用型号              |

### 7.7 商户订单工作台

订单和主数据业务由 `rental-business` 独立提供，网关对订单及仓库、商品、SN、配件、套餐、库存路径做精确转发；查询和写入必须同时满足权限、商户终端校验、租户/商户归属和仓库范围校验。

| 方法    | 路径                                     | 说明                              |
| ----- | -------------------------------------- | ------------------------------- |
| `GET` | `/merchant/orders`                     | 分页查询当前商户可见订单，支持关键词、状态、渠道和发货日期筛选 |
| `GET` | `/merchant/orders/{orderId}/notes`     | 查看当前商户订单的历史批注，最新批注优先            |
| `GET` | `/merchant/orders/{orderId}/edit-logs` | 查看当前商户订单的字段编辑历史，最新变更优先          |

参数、字典、通知、部门、岗位、平台端日志、在线用户、调度和代码生成均未作为商户能力开放。商户操作日志使用独立商户表和接口；未知 `/merchant/**` 路径由 `merchant-service` 返回未找到，不会回落到平台 Controller。

## 8. 定时任务服务

### 8.1 任务

| 方法       | 路径                           |
| -------- | ---------------------------- |
| `GET`    | `/schedule/job/list`         |
| `POST`   | `/schedule/job/export`       |
| `GET`    | `/schedule/job/{jobId}`      |
| `POST`   | `/schedule/job`              |
| `PUT`    | `/schedule/job`              |
| `PUT`    | `/schedule/job/changeStatus` |
| `PUT`    | `/schedule/job/run`          |
| `DELETE` | `/schedule/job/{jobIds}`     |

### 8.2 任务日志

| 方法       | 路径                              |
| -------- | ------------------------------- |
| `GET`    | `/schedule/job/log/list`        |
| `POST`   | `/schedule/job/log/export`      |
| `GET`    | `/schedule/job/log/{jobLogId}`  |
| `DELETE` | `/schedule/job/log/{jobLogIds}` |
| `DELETE` | `/schedule/job/log/clean`       |

### 8.3 市级天气任务内部调用

平台定时任务的目标为 `regionWeatherRefreshJob.refreshThreeDayWeather()`。该 Bean 位于  
`ruoyi-job`，通过内部 Feign 契约调用租赁业务服务；浏览器端不能直接调用以下内部接口。

| 方法     | 路径                                          | 说明                                                        |
| ------ | ------------------------------------------- | --------------------------------------------------------- |
| `POST` | `/internal/rental-business/weather/refresh` | 由平台调度服务触发全部市级行政区三日天气刷新，区县复用市级天气，必须携带 `from-source: inner` |

### 8.4 平台运维告警内部调用

平台调度任务每 5 分钟调用平台服务，同步最近 24 小时调度失败记录；浏览器端不能直接调用以下内部接口。

| 方法     | 路径                                                     | 说明                     |
| ------ | ------------------------------------------------------ | ---------------------- |
| `POST` | `/internal/platform/governance/operations/alerts/sync` | 生成、恢复平台运维告警事件，必须通过内部认证 |

## 9. 代码生成服务

| 方法       | 路径                               |
| -------- | -------------------------------- |
| `GET`    | `/code/gen/list`                 |
| `GET`    | `/code/gen/{tableId}`            |
| `GET`    | `/code/gen/db/list`              |
| `GET`    | `/code/gen/column/{tableId}`     |
| `POST`   | `/code/gen/importTable`          |
| `PUT`    | `/code/gen`                      |
| `DELETE` | `/code/gen/{tableIds}`           |
| `GET`    | `/code/gen/preview/{tableId}`    |
| `GET`    | `/code/gen/download/{tableName}` |
| `GET`    | `/code/gen/genCode/{tableName}`  |
| `GET`    | `/code/gen/synchDb/{tableName}`  |
| `GET`    | `/code/gen/batchGenCode`         |

## 10. 文件服务

| 方法       | 路径                                          |
| -------- | ------------------------------------------- |
| `POST`   | `/file/upload`                              |
| `POST`   | `/file/public/merchant-registration/upload` |
| `DELETE` | `/file/delete`                              |

## 11. 前端静态路由

平台端保留原有 `/register` 路由。商户端新增公开 `/register` 入驻页面，账号仅在平台审核通过后由系统开通。公共静态路由如下：

| 路径                          | 说明                  |
| --------------------------- | ------------------- |
| `/login`                    | 登录页                 |
| `/register`                 | 商户入驻申请、申请查询和驳回后修改重提 |
| `/index`                    | 首页                  |
| `/401`                      | 无权限页                |
| `/lock`                     | 锁屏页                 |
| `/redirect/:path(.*)`       | 页面重定向               |
| `/user/profile/:activeTab?` | 个人中心                |
| `/user/init-password`       | 首次登录设置初始密码          |
| `/:pathMatch(.*)*`          | 404 兜底              |

## 12. 前端权限附加路由

平台端保留平台用户、角色、字典、调度和代码生成附加路由。商户端只保留自己的角色用户授权路由：

| 路径                                 | 权限                   | 说明            |
| ---------------------------------- | -------------------- | ------------- |
| `/system/user-auth/role/:userId`   | `admin:user:edit`    | 平台端为用户分配角色    |
| `/system/role-auth/user/:roleId`   | `admin:role:edit`    | 平台端为角色分配用户    |
| `/system/dict-data/index/:dictId`  | `admin:dict:list`    | 平台字典数据        |
| `/monitor/job-log/index/:jobId`    | `monitor:job:list`   | 平台调度日志        |
| `/tool/gen-edit/index/:tableId`    | `tool:gen:edit`      | 平台修改代码生成配置    |
| `/merchant/role-auth/user/:roleId` | `merchant:role:edit` | 为当前商户角色分配商户用户 |

## 13. 平台端数据库动态页面路由

平台菜单由 `GET /admin/menu/getRouters` 根据 `sys_menu` 生成，主要页面路径如下：

| 页面路径                     | 页面                 |
| ------------------------ | ------------------ |
| `/system/user`           | 用户管理               |
| `/system/role`           | 角色管理               |
| `/system/menu`           | 菜单管理               |
| `/system/dept`           | 部门管理               |
| `/system/post`           | 岗位管理               |
| `/system/dict`           | 字典管理               |
| `/system/config`         | 参数设置               |
| `/system/notice`         | 通知公告               |
| `/system/log/operlog`    | 操作日志               |
| `/system/log/logininfor` | 登录日志               |
| `/monitor/online`        | 在线用户               |
| `/rental/codeRule/index` | 统一业务编码规则配置         |
| `/monitor/job`           | 定时任务               |
| `/tool/build`            | 表单构建               |
| `/tool/gen`              | 代码生成               |
| `/rental/member`         | 商户管理               |
| `/rental/merchant-user`  | 商户用户管理             |
| `/rental/merchant-role`  | 商户角色管理             |
| `/rental/product-class`  | 平台产品分类管理           |
| `/rental/brand`          | 平台品牌管理，新增品牌时绑定产品分类 |
| `/rental/brand-model`    | 平台标准品牌型号录入与管理      |


Nacos、Sentinel、Spring Boot Admin、Swagger 和若依官网为菜单中的外部链接，不属于 Vue Router 内部页面。

## 14. 商户端数据库动态页面路由

商户菜单由 `GET /merchant/menu/getRouters` 根据 `ry_rental.ry_rental_merchant_menu`、角色菜单和用户菜单授权关系生成：

| 页面路径                        | 组件                               | 页面           |
| --------------------------- | -------------------------------- | ------------ |
| `/merchant/home`            | `merchant/dashboard/index`       | 首页           |
| `/merchant/store`           | `ParentView`                     | 店铺           |
| `/merchant/product`         | `ParentView`                     | 商品管理         |
| `/merchant/product/class`   | `merchant/productClass/index`    | 经营分类         |
| `/merchant/product/my`      | `merchant/product/my/index`      | 我的商品         |
| `/merchant/product/publish` | `merchant/product/publish/index` | 发布商品         |
| `/merchant/brand-model`     | `merchant/brandModel/index`      | 品牌型号         |
| `/merchant/sn`              | `merchant/sn/index`              | SN 管理        |
| `/merchant/accessory`       | `merchant/accessory/index`       | 配件管理         |
| `/merchant/package`         | `merchant/package/index`         | 套餐管理         |
| `/merchant/warehouse`       | `merchant/warehouse/index`       | 仓库管理         |
| `/merchant/store/profile`   | `merchant/profile/index`         | 店铺管理         |
| `/merchant/user`            | `ParentView`                     | 用户管理         |
| `/merchant/user/account`    | `merchant/user/account/index`    | 账号管理         |
| `/merchant/user/operlog`    | `merchant/user/operlog/index`    | 操作日志（商户业务审计） |
| `/merchant/alliance`        | `merchant/alliance/index`        | 租商联盟         |
| `/merchant/trade`           | `ParentView`                     | 交易           |
| `/merchant/order`           | `merchant/order/index`           | 订单管理         |
| `/merchant/inventory`       | `merchant/inventory/index`       | 库存管理         |
| `/merchant/logistics`       | `merchant/logistics/index`       | 物流管理         |
| `/merchant/data`            | `merchant/data/index`            | 数据           |
| `/merchant/help`            | `merchant/help/index`            | 帮助           |

商户菜单表保存完整业务路径，商户前端加载动态路由时统一转换为绝对站内路径。商户页面统一在当前后台右侧内容区打开；HTTP/HTTPS 菜单由服务端转换为 `InnerLink` 本地路由并以内嵌页面展示，侧边栏、顶部菜单和菜单搜索均禁止新窗口打开，普通子菜单不会被误判为外链。

动态页面是否出现取决于商户用户最终菜单权限，不应仅根据前端是否存在组件判断可访问性。

### 14.1 商户注册、等级与推广补充路由

| 服务               | 方法                  | 路径                                                          | 权限                         |
| ---------------- | ------------------- | ----------------------------------------------------------- | -------------------------- |
| merchant-service | POST                | `/merchant/public/registration`                             | 公开                         |
| merchant-service | POST                | `/merchant/public/registration/payment/{paymentNo}/confirm` | 公开支付确认                     |
| merchant-service | GET                 | `/merchant/invitation/overview`                             | `merchant:invitation:list` |
| platform-service | GET/POST/PUT/DELETE | `/admin/platform/merchant-level/**`                         | `platform:merchantLevel:*` |
| platform-service | GET                 | `/admin/platform/promotion/list`                            | `platform:promotion:list`  |

## 15. 维护规则

新增或调整路由时应同步检查以下位置：

1. 网关配置：固定商户路由位于 `MerchantRouteConfiguration`，其他动态路由位于 `backend/ruoyi-cloud/sql/ry_config_20260311.sql`。
2. 后端 Controller：`backend/ruoyi-cloud/**/controller/**/*Controller.java`。
3. 平台前端：`frontend/admin-web/src/router/index.js` 与 `src/api/**`。
4. 商户前端：`frontend/merchant-web/src/router/index.js` 与 `src/api/**`。
5. 动态菜单：`backend/ruoyi-cloud/sql/ry_20260321.sql`、`database/seed/01_rental_dict_seed.sql`，以及合并基线 `database/migrations/V20261019_002__consolidated_project_baseline.sql` 中标记为 `V20260803_002__repair_merchant_menu_encoding_and_internal_routes.sql` 的历史段落；新增中文 SQL 必须显式使用 `SET NAMES utf8mb4`。
6. 商户命名空间：`MerchantRouteConfiguration` 中的独立服务路由，以及两个服务各自的终端边界过滤器。
