# RuoYi-Cloud 平台端与商户端隔离开发规则

## 1. 适用范围

本规则适用于本仓库中所有基于 RuoYi-Cloud 开发的平台端、商户端及公共业务模块，包括但不限于：

- Java Controller、Service、Domain Service、Mapper；
- MyBatis Mapper XML、SQL、数据库迁移脚本；
- DTO、Query、Command、VO、Entity；
- 网关路由、认证、授权、数据权限；
- 单元测试、集成测试和接口测试。

Codex 在分析、设计、生成或修改相关代码时，必须遵守本文件。若现有代码与本规则冲突，不得继续复制错误模式；应指出冲突，并在当前任务授权范围内修正或给出迁移建议。

## 2. 总体原则

平台端和商户端必须拆分为两个独立微服务：

- `platform-service`：只承载平台端业务和平台端接口；
- `merchant-service`：只承载商户端业务和商户端接口。

两个服务必须能够独立构建、注册、部署、扩容和下线，不得退化为同一服务中的两个包。可以通过公共依赖模块共享无身份、无数据访问、无终端语义的基础代码，但必须隔离对外入口、应用服务、认证身份、权限和数据访问边界。

隔离不能仅依赖：

- 两个前端项目；
- 不同菜单；
- 页面按钮隐藏；
- 不同角色名称；
- 两套 Controller；
- 前端传入的 `merchantId`；
- 仅使用 RuoYi 的部门数据权限；
- 拆成不同微服务但共享无约束的数据访问；
- 在网关中将两个前端路由到同一个业务服务。

任何来自客户端的用户 ID、商户 ID、角色、数据范围或终端类型都不可信，必须从服务端认证上下文或服务端数据关系中确定。

## 3. 接口与目录隔离

### 3.1 必须使用独立接口前缀

- 平台端接口统一使用 `/platform/**`。
- 商户端接口统一使用 `/merchant/**`。
- 禁止由同一个公开接口根据当前角色执行平台或商户两套逻辑。
- 禁止在共享 Controller 中使用 `if (isAdmin)`、`if (isMerchant)` 切换业务边界。

示例：

```text
GET  /platform/orders
POST /platform/orders/{id}/audit

GET  /merchant/orders
POST /merchant/orders/{id}/ship
```

### 3.2 Controller 必须归属不同微服务

推荐的 RuoYi-Cloud 模块结构：

```text
ruoyi-cloud
├── ruoyi-gateway
├── ruoyi-auth
├── ruoyi-api
│   ├── platform-api
│   └── merchant-api
├── ruoyi-common
└── ruoyi-modules
    ├── platform-service
    │   └── src/main/java/com/example/platform
    │       ├── controller
    │       ├── application
    │       ├── domain
    │       ├── mapper
    │       └── model
    └── merchant-service
        └── src/main/java/com/example/merchant
            ├── controller
            ├── application
            ├── domain
            ├── mapper
            └── model
```

命名规则：

- 新建或重命名的 Java 类禁止使用 `Info` 作为类名或后缀；应使用能表达职责的 `Model`、`DTO`、`VO`、`Entity` 等命名。已有历史类只有在当前任务涉及其契约时才迁移，避免无关的跨模块破坏。
- 平台 Controller：`Platform{Business}Controller` 或项目已统一使用的 `Admin{Business}Controller`。
- 商户 Controller：`Merchant{Business}Controller`。
- 平台应用服务：`Platform{Business}Service` 或 `Admin{Business}Service`。
- 商户应用服务：`Merchant{Business}Service`。

强制规则：

- 平台 Controller 只能存在于 `platform-service`。
- 商户 Controller 只能存在于 `merchant-service`。
- `platform-service` 禁止暴露 `/merchant/**`。
- `merchant-service` 禁止暴露 `/platform/**`。
- 禁止在公共模块、API 契约模块或 `ruoyi-common` 中放置 Controller。
- 禁止为了减少类数量而让两个服务共享 Controller 或应用 Service。

## 4. 应用服务隔离

- 平台端和商户端必须在各自微服务中实现独立的应用服务。
- Controller 只能调用与自身终端匹配的应用服务。
- 平台应用服务可处理全局查询、审核、封禁、强制操作等平台能力。
- 商户应用服务只能操作当前商户有权访问的数据。
- 可通过公共依赖模块复用纯工具、通用枚举、异常定义和无身份语义的纯计算逻辑。
- 禁止在公共依赖模块中共享 Mapper、Repository、Controller、带事务的 Service 或能够绕开服务边界的数据访问代码。
- 跨服务业务协作必须通过明确的内部 API、Feign 契约或事件完成，禁止一个服务直接调用另一个服务的 Mapper。
- 不得把平台与商户权限差异集中成一个公共 Service 中的大量角色判断。

允许抽取为纯公共能力：

```java
MoneyUtils.calculateAmount(...);
OrderStatusRule.canChange(from, to);
```

禁止模式：

```java
public List<Order> list(OrderQuery query) {
    if (SecurityUtils.isAdmin()) {
        return mapper.selectAll(query);
    }
    return mapper.selectByMerchant(query);
}
```

## 5. DTO、Query、Command 和 VO 隔离

- 平台端与商户端请求对象必须分开定义。
- 平台端与商户端返回对象在字段权限不一致时必须分开定义。
- 商户端请求 DTO、Query、Command 中禁止出现可由客户端指定的数据归属字段，包括：
  - `merchantId`；
  - `tenantId`；
  - 数据所有者用户 ID；
  - 用于扩大数据范围的部门 ID；
  - 角色、权限、终端类型。
- 若框架绑定、历史兼容等原因导致商户请求对象含有上述字段，服务端必须忽略并覆盖，且应优先重构删除。
- 禁止直接将 Entity 作为 Controller 请求参数或响应结果。
- Bean 拷贝后必须显式覆盖服务端控制字段，不能让客户端字段进入持久化对象。

推荐命名：

```text
AdminOrderQuery
AdminOrderAuditCommand
AdminOrderDetailVO

MerchantOrderQuery
MerchantOrderShipCommand
MerchantOrderDetailVO
```

## 6. 身份与认证上下文

### 6.1 终端类型

登录身份必须区分终端类型，例如：

```java
public enum LoginClientType {
    ADMIN,
    MERCHANT
}
```

认证上下文至少应能可靠获得：

- `userId`；
- `clientType`；
- 商户账号对应的 `merchantId`；
- 当前账号有效状态；
- 服务端解析后的权限集合。

### 6.2 身份来源

- `merchantId` 必须由认证服务根据账号绑定关系生成，并写入可信登录上下文。
- 禁止相信登录请求、Header、Query、Body 中客户端自行提交的 `merchantId`。
- 从 Token 读取身份时，必须使用项目统一的 Token 校验和登录缓存机制。
- 禁止仅 Base64 解码 JWT 后直接信任其中内容。
- 商户账号未绑定有效商户时，应拒绝访问商户业务，而不是退化为无商户条件查询。

### 6.3 终端校验

- `/platform/**` 只能由 `PLATFORM` 终端身份访问。
- `/merchant/**` 只能由 `MERCHANT` 终端身份访问。
- 终端类型校验与权限码校验必须同时存在，不能互相替代。
- 网关可以进行第一层校验，业务服务仍必须执行最终授权和数据边界校验。
- 禁止仅依赖网关保证内部服务安全。

## 7. 权限体系

平台端与商户端必须使用不同的权限命名空间。

示例：

```text
platform:order:list
platform:order:audit
platform:merchant:disable

merchant:order:list
merchant:order:ship
merchant:product:add
```

规则：

- 平台权限统一以 `platform:` 开头。
- 商户权限统一以 `merchant:` 开头。
- 禁止两个终端复用同一个权限标识表达不同业务含义。
- 每个非公开 Controller 方法必须配置明确的权限校验。
- 超级管理员能力只能存在于平台端，不能绕过商户数据边界进入商户接口。
- 菜单权限、按钮权限不能替代后端接口权限。

## 8. 商户数据隔离

### 8.1 强制商户条件

商户端所有商户归属数据的查询、统计、更新、删除和状态变更，SQL 必须包含服务端取得的 `merchant_id` 条件。

查询：

```sql
SELECT *
FROM biz_order
WHERE id = #{orderId}
  AND merchant_id = #{merchantId}
  AND deleted = 0;
```

更新：

```sql
UPDATE biz_order
SET status = #{status}
WHERE id = #{orderId}
  AND merchant_id = #{merchantId};
```

逻辑删除：

```sql
UPDATE biz_order
SET deleted = 1
WHERE id = #{orderId}
  AND merchant_id = #{merchantId};
```

禁止：

```sql
SELECT * FROM biz_order WHERE id = #{orderId};
UPDATE biz_order SET status = #{status} WHERE id = #{orderId};
```

### 8.2 写入归属

- 新增数据时，`merchant_id` 必须由服务端登录上下文设置。
- 禁止从请求对象复制 `merchantId` 到 Entity。
- 批量新增、导入、复制、异步任务和消息消费同样必须确定可信的商户归属。
- 无法确定商户归属时必须失败，禁止使用 `null`、`0` 或默认商户继续执行。

### 8.3 查询与修改策略

- 优先使用“业务主键 + `merchant_id`”一次完成带所有权约束的查询或修改。
- 不得仅依赖先查询、再在 Java 中判断归属的方式。
- 更新或删除操作后必须检查影响行数；影响行为 `0` 时按“不存在或无权访问”处理。
- 商户端不得通过任意 ID 查询平台全局对象后返回敏感字段。
- 对关联表、明细表、附件表操作时，必须验证其根对象属于当前商户。

### 8.4 防止信息泄漏

对商户端的越权 ID，默认返回统一的“资源不存在”或项目约定错误，不应泄漏：

- 资源确实存在；
- 资源属于哪个商户；
- 其他商户名称；
- 内部审核信息；
- 平台备注及敏感字段。

## 9. 部门权限与商户权限

- `merchant_id` 表示数据属于哪个商户，是强制业务边界。
- `dept_id` 表示平台或商户内部组织的数据范围，是边界内的二级权限。
- 必须先限制 `merchant_id`，再应用 RuoYi 的部门或角色数据权限。
- 禁止只使用 `@DataScope`、`dept_id` 或角色配置代替商户隔离。
- 部门树、角色数据范围发生错误时，不得突破商户边界。

## 10. Mapper 与 SQL 规则

- 商户侧 Mapper 方法名应明确表达商户约束，例如：
  - `selectByIdAndMerchantId`；
  - `updateStatusByIdAndMerchantId`；
  - `selectMerchantPage`。
- 禁止在商户应用服务中调用无商户约束的 `selectById`、`updateById`、`deleteById`，除非目标是经审查确认的全局公共数据。
- MyBatis XML 中的商户条件不得依赖前端可控的开关，例如 `includeMerchantScope=false`。
- 动态 SQL 不得因为 `merchantId == null` 而省略商户条件；应在进入 Mapper 前拒绝空值。
- 分页查询、导出、统计、聚合、子查询、联表查询同样必须带商户边界。
- 代码审查时必须追踪 Controller 到最终 SQL，不能只检查方法注解。

## 11. 数据库设计

- 所有具有商户归属的业务主表必须包含非空 `merchant_id`。
- 明细表如果不能安全、稳定地通过主表约束归属，应冗余 `merchant_id` 并保证一致性。
- 常用商户查询应建立以 `merchant_id` 开头的联合索引。

示例：

```sql
KEY idx_merchant_status_time (
    merchant_id,
    status,
    create_time
)
```

- 唯一约束应考虑商户维度。例如商户内订单编号唯一时使用：

```sql
UNIQUE KEY uk_merchant_order_no (merchant_id, order_no)
```

- 是否分库、分 Schema 属于更高等级的部署隔离，不能替代代码和 SQL 中的数据边界。

## 12. 网关与微服务边界

### 12.1 服务职责

`platform-service` 必须：

- 只接收平台端请求；
- 只识别平台终端身份；
- 提供全局管理、审核、配置、封禁和平台运营能力；
- 不实现商户自助接口；
- 不伪装成商户调用商户公开接口；
- 跨商户操作保留完整审计记录。

`merchant-service` 必须：

- 只接收商户端请求；
- 只识别商户终端身份；
- 所有商户业务强制使用当前登录上下文中的 `merchantId`；
- 不提供全局查询、跨商户管理或平台审核能力；
- 不接受平台端通过外部公开接口绕过内部调用边界。

### 12.2 网关路由

网关必须配置固定、互斥的路由：

```text
/platform/**     -> platform-service
/merchant/**  -> merchant-service
```

禁止：

- `/platform/**` 转发到 `merchant-service`；
- `/merchant/**` 转发到 `platform-service`；
- 同一路径根据 Header、角色或请求参数动态选择两个服务；
- 允许客户端直接访问服务注册中心中的内部地址；
- 将内部 Feign 接口暴露为无需内部身份校验的公网接口。

网关应执行第一层终端校验，但两个业务服务仍必须独立完成认证、权限和数据边界校验。

### 12.3 服务注册与配置

- 两个服务必须使用不同的服务名，例如 `platform-service`、`merchant-service`。
- 两个服务必须具有独立的配置文件、端口、日志标识、监控指标和发布单元。
- 禁止两个模块打包成同一个可执行 JAR。
- 配置中心中的数据源、Redis Key 前缀、MQ Consumer Group 和限流规则必须能按服务区分。
- 服务间调用地址必须使用注册中心服务名或项目统一的内部调用机制，不得硬编码实例 IP。

### 12.4 内部 API 与 Feign

- 跨服务调用必须定义最小化的内部 API 契约。
- 内部 API 与外部 Controller 必须使用不同路径或不同安全策略。
- Feign DTO 只暴露调用所需字段，禁止直接复用 Entity。
- 所有 Controller、Feign 和事件响应都必须遵循最小响应原则：按调用场景定义专用 DTO/VO，只返回调用方实际消费的字段；禁止透传 Entity、Mapper 查询行、内部拼装字段、调试字段或预留字段。
- 列表、详情、汇总、选项接口不得共用一个“全字段”响应对象；需要后续操作的主键仅作为接口参数保留，不能在页面展示，也不能据此附带无关名称或审计字段。
- 响应字段没有值时按契约省略或返回稳定空结构；新增、删除或重命名响应字段必须同步检查调用方引用、权限边界、序列化契约和自动化测试。
- 内部调用必须使用可信服务身份，并防止外部请求伪造内部调用标识。
- 不得仅凭客户端可设置的 `from-source`、`X-Internal-Request` 等 Header 判断内部调用。
- 下游服务必须重新校验资源归属和操作合法性，不能盲目信任上游传入的 `merchantId`。
- 避免同步双向调用；若形成 `platform-service -> merchant-service -> platform-service`，必须重新设计边界。

### 12.5 数据所有权

- 每张业务表必须明确由哪个服务拥有。
- 数据表所有者负责最终写入规则、状态变化和一致性校验。
- 非所有者服务原则上通过内部 API、领域事件或只读模型获取数据。
- 禁止两个服务随意直连并写入对方拥有的业务表。
- 如果迁移阶段暂时共享数据库，必须：
  - 明确表级所有权；
  - 使用不同数据库账号；
  - 按最小权限授权；
  - 禁止跨服务直接修改对方表；
  - 为移除共享数据库建立明确迁移计划。
- `merchant-service` 即使拥有独立数据库，也必须继续执行 `merchant_id` 行级隔离。
- 拆分微服务不能替代商户数据隔离。

### 12.6 公共模块边界

公共模块仅允许包含：

- 通用响应结构；
- 异常和错误码基础定义；
- 纯工具类；
- 通用注解；
- API 契约和最小 DTO；
- 无数据库访问的纯规则代码。

公共模块禁止包含：

- Controller；
- Mapper 或 Mapper XML；
- Repository；
- 数据源配置；
- 同时服务平台端和商户端的应用 Service；
- 可以查询全部商户数据的通用方法；
- 平台和商户共享的可执行启动类。

## 13. 异步任务、消息和缓存

- 定时任务、MQ 消费者、批处理、导入导出必须显式携带或解析可信的 `merchantId`。
- 消息体中的 `merchantId` 必须由可信生产者生成；消费者仍需校验业务对象归属。
- 商户相关缓存 Key 必须包含 `merchantId`。

示例：

```text
merchant:{merchantId}:order:{orderId}
```

- 禁止不同商户共享不含商户维度的业务缓存 Key。
- 清理缓存时不得使用可能误删其他商户缓存的宽泛 Key。

## 14. 日志与审计

敏感操作日志应记录：

- 操作用户 ID；
- 终端类型；
- 商户 ID（商户端）；
- 业务对象 ID；
- 操作类型；
- 成功或失败结果；
- 必要的请求链路 ID。

禁止在日志中输出：

- 完整 Token；
- 密码；
- 密钥；
- 无脱敏的个人敏感信息。

平台跨商户操作、审核、封禁、强制退款等必须保留审计记录。

## 15. 测试要求

每个商户业务至少覆盖以下场景：

1. 商户 A 可以查询自己的数据。
2. 商户 A 不能查询商户 B 的数据。
3. 商户 A 不能修改商户 B 的数据。
4. 商户 A 不能删除商户 B 的数据。
5. 修改 URL、Query、Body、Header 中的 `merchantId` 不能越权。
6. 商户 Token 不能访问 `/platform/**`。
7. 平台 Token 不能以商户身份直接访问 `/merchant/**`。
8. 缺失或无效 `merchantId` 时请求失败，不能查询全部。
9. 分页、统计、导出和批量接口不能混入其他商户数据。
10. 关联资源、附件和明细不能通过 IDOR 越权访问。
11. 更新和删除越权对象时影响行数为 `0`，且不泄漏对象存在性。
12. 缓存命中时仍不会返回其他商户数据。
13. `/platform/**` 只路由到 `platform-service`。
14. `/merchant/**` 只路由到 `merchant-service`。
15. `platform-service` 不存在商户公开 Controller。
16. `merchant-service` 不存在平台公开 Controller。
17. 外部请求不能伪造内部 Feign 调用身份。
18. 两个服务不能直接修改对方拥有的业务表。

新增商户接口但未添加越权测试，视为任务未完成。

## 16. Codex 实施流程

执行涉及平台端或商户端的开发任务时，Codex 必须：

1. 先确认接口属于 `platform-service`、`merchant-service` 还是无数据访问的公共能力。
2. 将平台代码放入 `platform-service`，商户代码放入 `merchant-service`，不得放入同一可执行模块。
3. 检查现有网关路径、服务注册名、登录用户模型、权限命名和模块结构。
4. 明确相关数据表的服务所有权。
5. 沿调用链检查 Controller、DTO、Service、Mapper 和最终 SQL。
6. 明确 `merchantId` 的可信来源。
7. 检查查询、写入、更新、删除、统计、导出、缓存和异步流程。
8. 检查跨服务调用是否使用最小内部 API，是否形成双向调用。
9. 优先遵循仓库现有风格；若现有风格违反本规则，以安全边界为准。
10. 添加或更新授权、路由、服务边界与跨商户越权测试。
11. 分别构建和测试两个微服务。
12. 在交付说明中明确：
   - 代码所属微服务；
   - 平台端和商户端入口；
   - 网关路由目标；
   - 权限标识；
   - `merchantId` 来源；
   - 数据表所有权；
   - 跨服务调用方式；
   - SQL 数据边界；
   - 已验证的越权场景。

如果无法确定某个接口的服务归属、数据表所有权，或现有认证上下文不能可靠提供 `merchantId`，必须停止实现相关数据操作并向用户确认，不得自行放宽权限或把代码放进公共模块。

## 17. Codex 代码审查清单

在完成任务前逐项检查：

- [ ] 平台与商户 Controller 是否分开？
- [ ] 平台 Controller 是否只位于 `platform-service`？
- [ ] 商户 Controller 是否只位于 `merchant-service`？
- [ ] 两个服务是否为独立构建和部署单元？
- [ ] URL 是否分别使用 `/platform/**`、`/merchant/**`？
- [ ] 网关是否将两个路径固定路由到正确服务？
- [ ] 应用服务是否分开？
- [ ] 是否明确了相关业务表的服务所有权？
- [ ] 是否避免直接写入另一个服务拥有的表？
- [ ] 跨服务调用是否通过最小内部 API 或事件完成？
- [ ] 是否避免同步双向 Feign 调用？
- [ ] 外部请求是否无法伪造内部服务身份？
- [ ] DTO/Query/Command 是否按终端分开？
- [ ] 商户请求对象是否移除了客户端可控的 `merchantId`？
- [ ] `merchantId` 是否只来自可信登录上下文？
- [ ] 是否同时校验终端类型和权限码？
- [ ] 商户查询、统计、导出 SQL 是否包含 `merchant_id`？
- [ ] 商户更新、删除 SQL 是否包含 `merchant_id`？
- [ ] 新增数据的 `merchant_id` 是否由服务端写入？
- [ ] 关联对象是否验证了根对象归属？
- [ ] 更新或删除是否检查影响行数？
- [ ] 缓存 Key 是否包含 `merchantId`？
- [ ] 异步任务和消息是否保留可信商户上下文？
- [ ] 是否避免仅用 `dept_id` 或 `@DataScope` 代替商户隔离？
- [ ] 是否添加跨商户越权测试？
- [ ] 是否避免向商户泄漏资源存在性和平台敏感字段？

任何一项与当前改动相关但未满足时，不得声明任务完成。

## 18. 禁止生成的高风险模式

Codex 不得新增或推荐以下代码：

```java
// 禁止：信任前端商户 ID
Long merchantId = request.getMerchantId();
```

```java
// 禁止：商户侧直接按主键查询
Order order = orderMapper.selectById(orderId);
```

```java
// 禁止：merchantId 为空时取消隔离
if (merchantId != null) {
    wrapper.eq("merchant_id", merchantId);
}
```

```java
// 禁止：同一接口按角色切换业务边界
if (SecurityUtils.isAdmin()) {
    return listAll();
} else {
    return listMerchant();
}
```

```java
// 禁止：Bean 拷贝客户端归属字段后直接入库
BeanUtils.copyProperties(request, entity);
mapper.insert(entity);
```

发现类似既有代码时，应将其标记为越权风险，并优先改为服务端身份注入和带 `merchant_id` 条件的数据访问。
