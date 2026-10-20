# AGENTS.md — QianRuo 租赁平台项目 Agent 指令文件

> 本文件为 Codex / AI Agent 在本项目中的行为约束与开发规范。
> Agent 在执行任何代码生成、修改、数据库操作、部署动作前，**必须**先阅读并遵循本文件。

---

## 1. 项目概览

| 项目维度 | 说明 |
|---|---|
| 后端框架 | RuoYi-Cloud（Java 17、Spring Boot 4、Spring Cloud 微服务架构、MyBatis XML） |
| 前端框架 | Vue 3 + Vite + Element Plus + Pinia + Axios（JavaScript） |
| 微服务模块 | gateway、auth、platform-service、merchant-service、rental-business 等 |
| 数据库 | MySQL 8.4（ry-cloud、ry_rental、ry_business） |
| 缓存 | Redis |
| 注册中心/配置中心 | Nacos v3 |
| 本地部署 | Docker Compose |
| 生产部署 | Kubernetes（K8s） |
| 服务间通信 | Feign + Sentinel 熔断限流 |

### 1.1 后端目录结构约定

```
backend/ruoyi-cloud/
├── ruoyi-api/                       # Feign 接口定义模块
│   ├── ruoyi-api-platform/          # 平台端 API
│   └── ruoyi-api-merchant/          # 商户端 API
├── ruoyi-common/                    # 公共模块
├── ruoyi-gateway/                   # 网关服务
├── ruoyi-auth/                      # 认证服务
├── ruoyi-modules/
│   ├── ruoyi-platform-service/      # 平台端服务
│   ├── ruoyi-merchant-service/      # 商户端服务
│   ├── ruoyi-rental-business/      # 租赁执行业务服务
│   ├── ruoyi-file/                  # 文件服务
│   ├── ruoyi-gen/                   # 代码生成服务
│   └── ruoyi-job/                   # 定时任务服务
└── ruoyi-visual/ruoyi-monitor/      # 可视化监控
```

### 1.2 前端目录结构约定

```
frontend/
├── admin-web/              # 正式平台端
│   ├── src/api/            # 平台端 API 请求封装
│   ├── src/views/          # 平台端页面
│   └── package.json
└── merchant-web/           # 正式商户端
    ├── src/api/            # 商户端 API 请求封装
    ├── src/views/          # 商户端页面
    └── package.json
```

`backend/ruoyi-cloud/ruoyi-ui` 是上游/历史前端目录，不作为当前 Docker 发布的正式前端。平台端和商户端必须分别构建，不能通过隐藏菜单或默认值互相兼容。

---

## 2. 数据库设计规范

### 2.1 主键规范

- 新建或重命名的 Java、TypeScript、C# 类禁止使用 `Info` 作为类名或后缀；应使用能表达职责的 `Model`、`DTO`、`VO`、`Entity` 等命名。已有历史类只有在当前任务涉及其契约时才迁移，避免无关的跨模块破坏。
- 主键命名必须遵循当前领域表的既有约定。租赁业务使用 `member_id`、`merchant_user_id`、`class_id`、`brand_id`、`model_id`、`product_id`、`warehouse_id`、`order_id` 等有业务含义的主键；关系表按现有设计使用复合主键。
- 新增表可以使用 `id BIGINT AUTO_INCREMENT`，但不能为了统一而把已有领域主键改成 `id`，也不能新增无业务意义的代理主键。
- 技术字段名保持现有关系契约：主键继续使用现有 `id`、`*_id`、`Id` 命名，外键和其他关联字段也不得改成 `unique_no`、`uniqueNo` 或其他新字段。
- 主键、外键和关联字段的注释、Javadoc、校验提示、Excel 表头及页面展示文案统一使用“唯一编号”。例如 `brand_id` 写作“品牌唯一编号”、`class_id` 写作“分类唯一编号”、`create_by` 写作“创建者用户唯一编号”；不改变字段名、JSON 属性名、Mapper 列名或接口参数名。
- `order_no`、`member_code` 等业务编码，以及链路标识、UUID、外部系统 ID 等非主键/外键标识保留原业务术语，不强行改成“唯一编号”。

```sql
-- 通用表可以使用 id；领域表应使用与业务一致的主键名称
CREATE TABLE biz_example (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '唯一编号',
    -- ...
    PRIMARY KEY (id)
);

-- 例如租赁订单使用 order_id，不能强制改名为 id
CREATE TABLE ry_rental_order (
    order_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单唯一编号',
    PRIMARY KEY (order_id)
);
```

### 2.2 审计字段规范

- 所有业务表**必须**包含以下审计字段：

| 字段名 | 类型 | 说明 | 关联 |
|---|---|---|---|
| `create_by` | `BIGINT` | 创建者用户唯一编号 | 平台表关联 `sys_user.user_id`；商户业务表关联 `ry_rental_merchant_user.merchant_user_id` |
| `create_time` | `DATETIME` | 创建时间 | 默认 `CURRENT_TIMESTAMP` |
| `update_by` | `BIGINT` | 更新者用户唯一编号 | 平台表关联 `sys_user.user_id`；商户业务表关联 `ry_rental_merchant_user.merchant_user_id` |
| `update_time` | `DATETIME` | 更新时间 | 更新时自动填充 |
| `remove_by` | `BIGINT` | 删除者用户唯一编号 | 平台表关联 `sys_user.user_id`；商户业务表关联 `ry_rental_merchant_user.merchant_user_id` |
| `remark` | `VARCHAR(500)` | 备注 | 可选，默认 NULL |

> **关键约束**：平台表的审计字段使用 `BIGINT` 逻辑关联 `ry-cloud.sys_user.user_id`；商户控制面和执行面表使用 `BIGINT` 逻辑关联 `ry_rental.ry_rental_merchant_user.merchant_user_id`。审计字段**不存储用户名字符串**。修改仍为历史 `VARCHAR` 的表时，必须先新增版本化迁移，不能只修改实体或 Mapper；历史无法可靠匹配用户的值统一置为 `NULL`，不得伪造用户编号。

```sql
CREATE TABLE biz_example (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '唯一编号',
    -- 业务字段...
    create_by    BIGINT       DEFAULT NULL COMMENT '创建者（按表归属关联平台或商户用户）',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by    BIGINT       DEFAULT NULL COMMENT '更新者（按表归属关联平台或商户用户）',
    update_time  DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务表示例';
```

### 2.3 其他数据库规范

- 表名使用小写蛇形命名（snake_case），租赁域表统一使用 `ry_rental_` 前缀；表所在数据库由业务边界决定。
- 字段名使用小写蛇形命名。
- 每个表和每个字段**必须**添加 `COMMENT`。
- 字符集统一 `utf8mb4`，排序规则按当前数据库配置使用 `utf8mb4_unicode_ci`。
- 布尔值使用 `TINYINT(1)`，`0` 为否，`1` 为是。
- 金额字段使用 `DECIMAL(18,2)`，禁止使用 `FLOAT` 或 `DOUBLE`。
- 时间字段使用 `DATETIME`，不使用 `TIMESTAMP`（避免 2038 问题及时区问题）。
- 枚举值字段使用 `TINYINT`、`INT` 或受控 `VARCHAR`，并在代码中定义对应的枚举类/集中映射。
- 业务删除统一使用数据库列 `is_delete` 软删除标记，`0` 表示正常、`1` 表示已删除；Java/JSON 属性统一使用 `isDelete`，访问器使用 `getIsDelete`/`setIsDelete`，不得重新使用 `isdeleted`、`Isdeleted` 或 `Isdelete`。
- 外键关联在应用层维护，**不在数据库层创建物理外键**。

当前数据库边界如下：`ry-cloud` 保存平台系统数据，`ry_rental` 保存商户控制面和平台治理主数据，`ry_business` 保存商品、SN、配件、套餐、仓库、库存、订单、渠道、物流和天气等租赁执行数据。`platform-service` 不直接访问 `ry_business`，`merchant-service` 只访问 `ry_rental`，`rental-business` 只访问 `ry_business`。

行政区划和天气使用 `ry_business.ry_rental_region`、`ry_business.ry_rental_region_weather`；旧的 `ry_region`、`ry_region_weather` 已弃用，不得重新引入。

新环境基础结构放在 `database/mysql/`，基础字典/种子数据放在 `database/seed/`；已有环境的结构和数据变更只能新增 `database/migrations/VYYYYMMDD_NNN__description.sql`，并登记到 `scripts/deploy.sh` 的迁移数组。迁移执行记录保存在 `ry-config.qianruo_schema_migration`，已执行脚本禁止修改。测试订单等测试数据必须与正式迁移分离，普通部署不得重复写入或覆盖历史业务数据。

### 2.4 MyBatis 映射规范

- Mapper XML 中 `resultMap` 的主键映射必须使用实际领域主键，例如 `<id property="orderId" column="order_id"/>`；只有主键确实为 `id` 时才使用 `id`。
- 审计字段在 `insert` 时自动填充 `create_by`、`create_time`；`update` 时自动填充 `update_by`、`update_time`。
- 使用 RuoYi 框架的 `BaseEntity` 作为实体类父类，审计字段已包含其中，业务实体类继承即可。

```java
// 业务实体类示例
public class BizExample extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    // 业务字段...
}
```

> 注意：当前公共 `BaseEntity` 的 `createBy`、`updateBy` 使用 `Long`，对应数据库 `BIGINT`。实体中的 `removeBy` 若由业务表显式定义，也必须使用 `Long`；不能用用户名、手机号或其他字符串代替用户唯一编号。需要与商户用户关联的执行面表必须以迁移脚本和 Mapper 的真实字段为准。

---

## 3. 页面设计规范

### 3.1 核心原则

**优先遵循当前使用框架（RuoYi-Cloud + Vue 3 + Vite + Element Plus）的设计规范。**

- 不引入框架未集成的第三方 UI 库，除非用户明确要求。
- 页面布局、组件用法、交互模式参照 RuoYi 官方模板页面。
- 新增页面参照同类型已有页面的结构，保持风格一致。
- 正式业务表单、配置和列表页面的主体默认占满右侧内容区宽度；筛选区、工具栏、表格、树形选择器和表单不得使用会使内容仅停留在左侧的固定宽度或 `max-width`。仅在用户明确要求的阅读型详情、弹窗等场景收窄内容区。

### 3.2 列表页面规范

参照 RuoYi 标准 CRUD 页面结构：

```
┌─ 搜索区域（el-form + el-row/el-col）
│   ├── 查询条件输入框/下拉框
│   ├── 搜索 / 重置 按钮
│
├─ 工具栏区域
│   ├── 新增 / 修改 / 删除 / 导出 按钮（按权限控制）
│
├─ 表格区域（el-table）
│   ├── 多选列
│   ├── 数据列（支持排序、格式化）
│   ├── 操作列（修改/删除按钮）
│   └── 分页组件（el-pagination）
│
└─ 弹窗区域（el-dialog）
    ├── 新增/修改表单（el-form + el-input/el-select...）
    └── 表单校验规则
```

### 3.3 前端代码规范

- **API 封装**：所有后端接口请求统一放在对应正式前端的 `frontend/admin-web/src/api/` 或 `frontend/merchant-web/src/api/` 下，按模块分文件。
- **路由配置**：新增页面需在对应前端的 `src/router/` 中注册路由，动态路由通过菜单管理配置。
- **权限控制**：使用 `v-hasPermi` 指令控制按钮/操作权限。
- **状态管理**：全局状态使用 Pinia，页面局部状态使用组件 `data()`。
- **请求封装**：统一使用 `@/utils/request` 封装的 axios 实例。
- **字典数据**：使用 RuoYi 的字典管理（`useDict`）处理枚举值展示。

平台端请求使用 `X-Client-Type: platform`、`X-App-Code: rental-admin-web` 和 `Admin-Token`；商户端请求使用 `X-Client-Type: merchant`、`X-App-Code: rental-merchant-web` 和 `Merchant-Token`。`admin` 不能作为终端类型，也不能作为 `platform` 的兼容别名。平台端浏览器接口使用 `/admin/**`，商户端浏览器接口统一使用 `/merchant/**`，内部服务接口使用 `/internal/**`，三者不能混用。

**商户端用户可见信息规则**：商户端页面、表格、弹窗、提示、空状态、导出表头及操作记录中不得直接展示技术主键、外键或其他 `id` 类字段。商户关联对象统一显示商户名称，订单统一显示订单编码（如 `order_no`），库存所有者统一显示商户名称；分类、品牌、仓库、套餐、配件等关联对象也必须显示对应名称。`member_id`、`order_id`、`alliance_order_id` 等 ID 仅用于接口参数、权限校验和内部关联，不能作为用户可读文本、表格列或下拉选项标签。新增接口返回列表或详情时，应同时提供页面所需的名称/编码字段，避免前端把 ID 直接渲染给用户。

**接口响应字段最小化规则**：每个接口必须使用与调用场景匹配的专用 DTO/VO，只返回调用方实际需要的字段。列表、详情、汇总、下拉选项和内部 Feign 契约应分别定义响应模型，不得直接透传 Entity、数据库查询行、SQL 中仅用于内部组装的字段、调试字段或“以后可能使用”的字段。为后续操作保留的主键只能作为接口内部参数使用，不能因此增加用户可读字段；可选字段没有值时按接口约定省略或返回稳定的空结构。新增或删除响应字段前必须同步检查前端实际引用、权限边界、序列化契约和回归测试。

**唯一编号表单控件规则**：表单中涉及唯一编号、主键、外键或关联 ID 的字段，直接使用下拉框；只有层级关系或数据量确有需要时，才使用级联选择器、树形选择器或其他可搜索的名称选择控件。选项展示业务名称或业务编码，禁止使用 `el-input`、`el-input-number` 等控件让用户直接输入或看到 ID。提交时仍传递原有 ID 字段，接口和数据库字段名保持现有关系契约不变。

带筛选条件的正式列表页面沿用当前项目的 `el-form`、`el-row.mb8`、右上角工具区、隐藏搜索和刷新布局；隐藏搜索时必须隐藏整个筛选容器。商户订单中心同时支持卡片模式和精简模式，订单操作按钮必须绑定真实路由或业务事件。

弹窗表单中，排序、状态等紧凑型辅助字段需要使用同一 `el-row` 横向排列；该行统一放在备注等长文本字段之后、底部操作按钮之前，避免将相关字段拆成连续的独立纵向区域。状态字段继续使用 `el-switch`。

```javascript
// API 封装示例 — src/api/biz/example.js
import request from '@/utils/request'

// 查询列表
export function listExample(query) {
  return request({
    url: '/biz/example/list',
    method: 'get',
    params: query
  })
}

// 新增
export function addExample(data) {
  return request({
    url: '/biz/example',
    method: 'post',
    data: data
  })
}
```

```vue
<!-- 页面示例 — src/views/biz/example/index.vue -->
<template>
  <div class="app-container">
    <!-- 搜索 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch">
      <!-- 查询条件 -->
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
          v-hasPermi="['biz:example:add']">新增</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="唯一编号" align="center" prop="id" />
      <!-- 业务列 -->
      <el-table-column label="操作" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['biz:example:edit']">修改</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total"
      v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize"
      @pagination="getList" />

    <!-- 弹窗表单 -->
    <el-dialog :title="title" v-model="open" width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <!-- 表单字段 -->
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>
```

### 3.4 组件命名规范

| 类型 | 规范 | 示例 |
|---|---|---|
| 页面文件 | 小写蛇形，`index.vue` | `views/biz/example/index.vue` |
| 组件文件 | 大驼峰 | `components/UserSelect.vue` |
| API 文件 | 小写蛇形 | `api/biz/example.js` |
| 路由 name | 大驼峰 | `name: 'BizExample'` |
| CSS class | 中划线分隔 | `.example-list-page` |

---

## 4. 部署规范

### 4.1 前端自动部署到 Docker

**执行前端代码修改后，Agent 必须自动触发受影响前端的构建并部署到 Docker。**

**按变更范围部署**：默认只构建、推送和重启本次实际修改的服务或前端项目。仅在用户明确要求全量发布、共享基础镜像/配置变更影响多个服务，或定向发布无法保证一致性时，才执行全量 `./scripts/deploy.sh local`。执行前需说明扩大的部署范围及原因。

#### 部署流程

```
前端代码修改完成
    ↓
执行 npm build（生产构建）
    ↓
构建产物复制到 Docker 挂载目录 / 镜像构建上下文
    ↓
docker compose 重启前端容器（或重建镜像）
    ↓
验证服务可访问性
```

#### 具体操作

```bash
# 1. 仅在需要全量发布时使用统一入口；常规变更应使用受影响项目的定向构建与容器重启
./scripts/deploy.sh local

# 2. 验证：平台端 8088，商户端 8089
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8088/
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8089/
```

#### 部署检查清单

Agent 在完成前端部署后，需确认以下事项：

- [ ] 构建无报错（`npm run build:prod` 退出码为 0）
- [ ] Docker 容器状态为 `running`
- [ ] 页面可正常访问（HTTP 状态码 200）
- [ ] 浏览器无控制台报错（如条件允许）
- [ ] 修改的功能点已生效

### 4.2 后端部署规范

```bash
# 1. Maven 编译打包（必须 clean，避免增量编译问题）
mvn -f backend/ruoyi-cloud/pom.xml clean package

# 2. 通过统一脚本校验 JAR、构建全部服务镜像
./scripts/deploy.sh build

# 3. 启动/迁移/重启本地服务
./scripts/deploy.sh local
```

> **重要**：后端编译必须使用 `mvn clean package`，禁止跳过 `clean`。
> ECJ 增量编译器在跳过重编译时可能将失败类的错误 stub 打入 jar/镜像，导致运行时 `NoSuchMethodError` 等异常。

### 4.3 Docker Compose 配置规范

- 前端容器使用 Nginx 作为 Web 服务器。
- 后端容器基于 OpenJDK 镜像。
- 所有容器通过 Docker Compose 网络互联。
- 数据卷挂载用于持久化日志、配置等。
- Nacos 配置中心管理各服务配置，敏感信息通过环境变量注入。

```yaml
# deploy/docker-compose.local.yml 使用的前端服务名称
services:
  rental-admin-web:
    container_name: qianruo-admin-web
    ports:
      - "8088:80"
  rental-merchant-web:
    container_name: qianruo-merchant-web
    ports:
      - "8089:80"
```

### 4.4 Docker 镜像命名和版本

- 所有环境统一使用 `REGISTRY/IMAGE_NAMESPACE/<service-name>:<hex-version>` 命名镜像；`<service-name>` 必须使用部署脚本中的固定服务名，例如 `platform-service`、`merchant-service`、`rental-business`、`rental-admin-web`、`rental-merchant-web`。
- `<hex-version>` 使用 Git 提交 SHA（至少 12 位十六进制字符，生产发布优先使用完整 SHA）或镜像内容生成的等价不可变摘要，采用类似 MD5 的可追溯格式；禁止使用 `latest`、随机时间或无法关联源码的标签。
- 发布后应记录实际镜像摘要 `@sha256:<digest>`，回滚和 Kubernetes 部署优先按不可变标签或摘要执行，不允许同一标签覆盖不同镜像内容。

---

## 5. 后端代码规范

### 5.1 包结构规范

```
com.ruoyi.<module>
├── controller/         # 控制层
├── service/            # 服务接口
│   └── impl/           # 服务实现
├── mapper/             # MyBatis Mapper 接口
├── domain/             # 实体类
├── dto/                # 数据传输对象
├── vo/                 # 视图对象
├── config/             # 配置类
├── enums/              # 枚举类
└── utils/              # 工具类
```

### 5.2 Controller 规范

- 使用 `@RestController` + `@RequestMapping`。
- 浏览器接口必须遵循终端命名空间：平台端 `/admin/**`、商户端 `/merchant/**`、服务间 `/internal/**`。平台网关会去掉 `/admin` 后转发到平台 Controller 当前使用的 `/system/**` 或 `/rental/**` 模块路径；商户 Controller 使用 `/merchant/**`。租赁执行接口由网关路由到 `rental-business`，不能由商户端调用平台 Controller。
- 标准接口集（RESTful 风格）：

| 操作 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 分页查询 | `GET` | `/list` | 列表页数据 |
| 详情 | `GET` | `/{id}` | **不加动词路径，由路由承载语义** |
| 新增 | `POST` | `/` | 请求体传参 |
| 修改 | `PUT` | `/` | 请求体传参 |
| 删除 | `DELETE` | `/{ids}` | 支持批量，逗号分隔 |

- 详情接口**不使用** `getInfo`、`detail`、`queryById` 等动词路径，直接 `GET /{id}`。
- 使用 `@PreAuthorize` + `@ss.hasPermi()` 做权限控制。
- 返回 `AjaxResult` 统一响应格式。

```java
@RestController
@RequestMapping("/biz/example")
public class BizExampleController extends BaseController {

    @Autowired
    private IBizExampleService bizExampleService;

    @PreAuthorize("@ss.hasPermi('biz:example:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizExample bizExample) {
        startPage();
        List<BizExample> list = bizExampleService.selectBizExampleList(bizExample);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('biz:example:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(bizExampleService.selectBizExampleById(id));
    }

    @PreAuthorize("@ss.hasPermi('biz:example:add')")
    @PostMapping
    public AjaxResult add(@RequestBody BizExample bizExample) {
        return toAjax(bizExampleService.insertBizExample(bizExample));
    }

    @PreAuthorize("@ss.hasPermi('biz:example:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody BizExample bizExample) {
        return toAjax(bizExampleService.updateBizExample(bizExample));
    }

    @PreAuthorize("@ss.hasPermi('biz:example:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(bizExampleService.deleteBizExampleByIds(ids));
    }
}
```

### 5.3 Service 规范

- 接口命名以 `I` 开头，如 `IBizExampleService`。
- 实现类命名以 `Impl` 结尾，如 `BizExampleServiceImpl`。
- 使用 `@Service` 注解。
- 事务使用 `@Transactional`，只标在写操作上。

### 5.4 Mapper 规范

- XML 文件放在 `resources/mapper/<module>/` 目录下。
- 使用 MyBatis 注解或 XML 映射，复杂 SQL 使用 XML。
- 分页使用 RuoYi 的 `startPage()` + `PageHelper`。

---

## 6. Git 提交规范

### 6.1 分支策略

| 分支 | 用途 |
|---|---|
| `main` | 生产分支，保护分支 |
| `dev` | 开发集成分支 |
| `feature/<功能名>` | 功能开发分支 |
| `fix/<问题名>` | 修复分支 |
| `hotfix/<紧急修复>` | 生产紧急修复 |

### 6.2 提交信息格式

```
<类型>: <简要描述>

<详细说明（可选）>
```

类型：

| 类型 | 说明 |
|---|---|
| `feat` | 新功能 |
| `fix` | 修复 Bug |
| `refactor` | 重构（无功能变化） |
| `style` | 代码格式调整（无逻辑变化） |
| `docs` | 文档变更 |
| `test` | 测试相关 |
| `chore` | 构建/工具链变更 |
| `perf` | 性能优化 |

示例：
```
feat: 新增租赁订单管理模块 CRUD 接口

- 新增 rental_order 表及实体类
- 实现 Controller/Service/Mapper 三层
- 前端新增订单管理页面
- 完成权限标识配置
```

---

## 7. Agent 行为约束

### 7.1 执行前检查

Agent 在执行任何代码变更前，必须：

1. **阅读本文件**，了解项目规范与约束。
2. **确认变更范围**，仅修改用户要求的文件，不做无关变更。
3. **遵循框架规范**，优先参照已有代码风格，不引入未经确认的新依赖。

### 7.2 变更后检查

Agent 在完成代码变更后，必须：

1. **后端变更**：执行 `mvn -f backend/ruoyi-cloud/pom.xml clean package` 确认编译通过。
2. **前端变更**：在变更的正式前端执行 `npm --prefix frontend/<admin-web|merchant-web> run build:prod` 确认构建通过，并仅部署对应 Docker 前端容器；不得因单个项目改动默认重建无关项目。
3. **数据库变更**：新环境 DDL 放入 `database/mysql/`；已有环境变更生成版本化迁移，放入 `database/migrations/` 并登记到 `scripts/deploy.sh`。
4. **配置变更**：更新 Nacos 配置或 `application.yml`，并记录变更内容。

### 7.3 禁止事项

- 禁止跳过 `clean` 执行 Maven 编译。
- 禁止在数据库层创建物理外键。
- 禁止使用 `FLOAT`/`DOUBLE` 存储金额。
- 禁止使用 `TIMESTAMP` 类型存储时间。
- 禁止为了统一而把当前领域主键改成 `id`；新表主键必须与领域命名和现有关系设计一致。
- 商户执行面中已完成迁移的 `create_by`/`update_by` 必须使用 `BIGINT` 关联 `merchant_user_id`；历史字段转换必须通过版本化迁移，不能只改代码。
- 禁止前端修改后不部署到 Docker；禁止单个项目改动时无理由全量重建或推送无关项目。
- 禁止引入未经确认的第三方 UI 库。
- 禁止提交带有 `console.log` 的调试代码。

### 7.4 Nacos 配置注意事项

- Nacos v3 启动需配置 Base64 编码的 `NACOS_AUTH_TOKEN`。
- 各微服务配置统一在 Nacos 配置中心管理，`application.yml` 中仅保留引导配置。
- 敏感信息（数据库密码、密钥等）通过环境变量注入，不硬编码。

---

## 8. 常见任务模板

### 8.1 新增业务模块完整流程

当需要新增一个业务模块时，Agent 应按以下顺序执行：

1. **数据库**：新环境基础 DDL 放入 `database/mysql/`；已有环境变更新增 `database/migrations/VYYYYMMDD_NNN__description.sql`，并登记到 `scripts/deploy.sh`
2. **后端实体**：创建 Domain 类（是否继承 `BaseEntity` 按现有模块约定，主键使用实际领域命名）
3. **后端 Mapper**：创建 Mapper 接口 + XML 映射文件
4. **后端 Service**：创建 Service 接口 + 实现类
5. **后端 Controller**：创建 Controller（标准 CRUD 接口 + 权限控制）
6. **菜单配置**：通过对应数据库迁移配置平台/商户菜单和权限
7. **前端 API**：在对应正式前端创建 `frontend/<admin-web|merchant-web>/src/api/<module>/<entity>.js`
8. **前端页面**：在对应正式前端创建 `frontend/<admin-web|merchant-web>/src/views/<module>/<entity>/index.vue`
9. **前端构建部署**：在对应的正式前端执行 `npm run build:prod` → 自动部署到 Docker
10. **后端编译部署**：执行 `mvn -f backend/ruoyi-cloud/pom.xml clean package` → Docker 重建镜像
11. **验证**：确认功能可用

### 8.2 前端修改部署模板

```bash
# Agent 执行前端修改后的标准流程
npm --prefix frontend/admin-web run build:prod && \
npm --prefix frontend/merchant-web run build:prod && \
./scripts/deploy.sh local && \
echo "前端部署完成" && \
curl -s -o /dev/null -w "平台端 HTTP状态码: %{http_code}\n" http://localhost:8088/ && \
curl -s -o /dev/null -w "商户端 HTTP状态码: %{http_code}\n" http://localhost:8089/
```

---

_本文件随项目演进持续更新。Agent 发现规范缺失或过时时应主动提醒用户更新。_
