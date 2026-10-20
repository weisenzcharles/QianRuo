# 阿里云 OSS 与短信公共能力

## OSS 文件上传

项目现有平台端和商户端上传接口继续通过 `ruoyi-file` 与 `RemoteFileService` / `RemoteMerchantFileService` 调用，接口路径和响应契约不变。正式环境 `ruoyi-file` 使用 `FILE_STORAGE_PROVIDER=aliyun-oss`，OSS 参数由平台端系统配置读取，不再从容器环境变量读取密钥。

启用 OSS 时设置：

```properties
FILE_STORAGE_PROVIDER=aliyun-oss
```

平台端“系统参数配置”中维护以下参数：

- `global.aliyun.access-key-id`：短信与 OSS 共用的 AccessKey ID。
- `global.aliyun.access-key-secret`：短信与 OSS 共用的 AccessKey Secret。
- `global.aliyun.oss.enabled`：OSS 开关。
- `global.aliyun.oss.endpoint`：OSS Endpoint，例如 `https://oss-cn-hangzhou.aliyuncs.com`。
- `global.aliyun.oss.bucket-name`：Bucket 名称。
- `global.aliyun.oss.public-base-url`：可选的自定义域名或 CDN 根地址。
- `global.aliyun.oss.object-prefix`：对象根目录，默认为 `qianruo`。

未配置 `global.aliyun.oss.public-base-url` 时，服务返回 `https://{bucket}.{endpoint-host}/{object-key}`。对象访问权限由 OSS Bucket Policy 或 CDN 配置控制，应用不会给对象设置公共读 ACL。自定义域名或 CDN 应配置为可读取同一个 Bucket。

上传接口可传递 `businessType`，对象键统一为 `<object-prefix>/<businessType>/<时间戳>.<扩展名>`，不再创建年月日目录，也不保留原始文件名。当前业务目录包括 `merchant`、`merchant-registration`、`merchant-user`、`platform-user`、`order`、`product`、`package`、`inventory`、`logistics` 和 `after-sales`；未指定时进入 `common`。服务端只接受白名单业务类型，避免调用方任意写入对象路径。

公共 API 位于 `ruoyi-common-aliyun-oss`：`AliyunOssService.upload` 接收对象键、流、长度和 MIME 类型，`deleteByUrl` 只接受当前配置公开域名和路径下的 URL。`ruoyi-file` 是当前 OSS 上传/删除的集中入口；业务模块不应绕过配额、鉴权和审计逻辑直接上传。

平台系统配置中的凭据只通过平台内部配置接口供 `ruoyi-file` 读取，短时间缓存后使用，不注入 Pod 环境变量、Secret、镜像或普通 ConfigMap。RAM 授权应限制到目标 Bucket 所需的上传、删除权限；生产优先使用 RAM 角色/临时凭证。

## 短信发送

阿里云短信配置统一在平台端“系统参数配置”中维护，配置键如下：

- `global.aliyun.sms.enabled`
- `global.aliyun.sms.endpoint`
- `global.aliyun.sms.sign-name`
- `global.aliyun.sms.login-template-code`
- `global.aliyun.access-key-id`
- `global.aliyun.access-key-secret`

AccessKey 在参数列表和导出中只显示配置状态，编辑详情不回显原值，操作日志也排除参数值。平台系统参数中的凭据仍由数据库保存；生产数据库应限制访问权限和备份权限。

`ruoyi-common-aliyun-sms` 提供 `AliyunSmsService.sendTemplateSms`，商户端手机号登录使用该能力发送四位验证码。短信发送接口只对可用的商户账号尝试发送，但无论手机号是否注册均返回相同提示，避免账号枚举。发送和登录均受网关图形验证码保护；短信验证码保存在 Redis 中，有效期 5 分钟、同手机号 60 秒冷却、单手机号每日最多 10 次、单 IP 每日最多 50 次，连续输错 5 次后失效，成功校验后立即消费。

启用商户短信登录前，平台端需配置 `global.aliyun.sms.enabled=true`、有效的 AccessKey、短信签名名称及已审核模板编码。模板变量必须为 `code`，RAM 账号只授予短信发送所需权限。签名或模板尚未审核、短信服务未启用、配置缺失时，不会降级为固定验证码或任意验证码。

```java
aliyunSmsService.sendTemplateSms(phoneNumber, signName, templateCode, Map.of("code", verificationCode));
```

## SDK 与验证

OSS 使用阿里云 Java OSS SDK V1，短信使用官方 `dysmsapi20170525` Java SDK。短信系统参数默认关闭且凭据为空；单元测试不调用真实阿里云服务。
