package com.ruoyi.common.core.enums;

/**
 * 租赁业务数字编码。
 *
 * <p>本类集中维护跨服务持久化的状态、类型和标志位编码。新增取值必须先在这里定义，再同步
 * 数据库迁移、前端选项和接口契约；禁止在业务代码中直接写数字或旧的英文状态字符串。</p>
 */
public final class RentalCode
{
    private RentalCode()
    {
    }

    /** 通用启停状态。 */
    public enum EnableStatus implements NumericCode
    {
        ENABLED(0, "正常"),
        DISABLED(1, "停用");

        private final Integer code;
        private final String label;

        EnableStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 通用是否标志。 */
    public enum YesNo implements NumericCode
    {
        NO(0, "否"),
        YES(1, "是");

        private final Integer code;
        private final String label;

        YesNo(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 商户主体类型。 */
    public enum MemberType implements NumericCode
    {
        PERSONAL(1, "个人"),
        ENTERPRISE(2, "企业");

        private final Integer code;
        private final String label;

        MemberType(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 商户会员类型。 */
    public enum MembershipType implements NumericCode
    {
        PERSONAL_MEMBER(1, "个人会员"),
        ORDINARY_ENTERPRISE_MEMBER(2, "普通企业会员"),
        SETTLED_MEMBER(3, "入驻会员");

        private final Integer code;
        private final String label;

        MembershipType(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 商户认证状态。 */
    public enum AuthenticationStatus implements NumericCode
    {
        UNVERIFIED(0, "未认证"),
        PAYMENT_PENDING(1, "待支付"),
        PENDING(2, "待审核"),
        VERIFIED(3, "已认证"),
        REJECTED(4, "已驳回");

        private final Integer code;
        private final String label;

        AuthenticationStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 注册流程状态。 */
    public enum RegistrationStatus implements NumericCode
    {
        AWAITING_PAYMENT(0, "待支付"),
        COMPLETED(1, "已完成");

        private final Integer code;
        private final String label;

        RegistrationStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 注册方式。 */
    public enum RegistrationMode implements NumericCode
    {
        PAID(1, "付费注册"),
        INVITE(2, "邀请码注册");

        private final Integer code;
        private final String label;

        RegistrationMode(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 认证主体类型。 */
    public enum CertificationType implements NumericCode
    {
        PERSONAL(1, "个人"),
        ENTERPRISE(2, "企业");

        private final Integer code;
        private final String label;

        CertificationType(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 认证审核状态。 */
    public enum CertificationStatus implements NumericCode
    {
        PENDING(0, "待审核"),
        APPROVED(1, "已通过"),
        REJECTED(2, "已驳回"),
        EXPIRED(3, "已过期"),
        SUPPLEMENT_REQUIRED(4, "待补件");

        private final Integer code;
        private final String label;

        CertificationStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 认证材料类型。 */
    public enum CertificationMaterialType implements NumericCode
    {
        LICENSE(1, "营业执照"),
        LEGAL_ID_FRONT(2, "身份证人像面"),
        LEGAL_ID_BACK(3, "身份证国徽面"),
        CONTRACT(4, "合同"),
        OTHER(9, "其他");

        private final Integer code;
        private final String label;

        CertificationMaterialType(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 接单资格状态。 */
    public enum QualificationStatus implements NumericCode
    {
        DISABLED(0, "未开通"),
        PENDING(1, "待审核"),
        ENABLED(2, "已开通"),
        REJECTED(3, "已驳回");

        private final Integer code;
        private final String label;

        QualificationStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 商户用户仓库范围。 */
    public enum WarehouseScope implements NumericCode
    {
        ALL(1, "全部仓库"),
        SPECIFIED(2, "指定仓库");

        private final Integer code;
        private final String label;

        WarehouseScope(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 商户用户敏感数据访问范围。 */
    public enum SensitiveScope implements NumericCode
    {
        MASKED(1, "脱敏查看"),
        DETAIL(2, "明细查看"),
        EXPORT(3, "导出查看");

        private final Integer code;
        private final String label;

        SensitiveScope(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 邀请记录状态。 */
    public enum InvitationStatus implements NumericCode
    {
        REGISTERED(1, "已注册"),
        INVALID(2, "已失效");

        private final Integer code;
        private final String label;

        InvitationStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 支付状态。 */
    public enum PaymentStatus implements NumericCode
    {
        UNPAID(0, "未支付"),
        PAID(1, "已支付"),
        FAILED(2, "支付失败"),
        CLOSED(3, "已关闭"),
        REFUNDED(4, "已退款");

        private final Integer code;
        private final String label;

        PaymentStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 订单渠道类型。 */
    public enum OrderChannelType implements NumericCode
    {
        ALLIANCE(1, "联盟渠道"),
        CUSTOM(2, "商户自定义渠道");

        private final Integer code;
        private final String label;

        OrderChannelType(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 订单渠道归属范围。 */
    public enum OrderChannelScope implements NumericCode
    {
        PLATFORM(1, "平台"),
        MERCHANT(2, "商户");

        private final Integer code;
        private final String label;

        OrderChannelScope(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 订单履约状态。 */
    public enum OrderStatus implements NumericCode
    {
        PENDING_SHIP(10, "待发货"),
        PREPARED(15, "已备货"),
        SHIPPING(20, "运输中"),
        SHIP_IN_TRANSIT(25, "已发货"),
        SHIPPED(30, "已发货"),
        RENTING(40, "租赁中"),
        RETURNING(45, "归还中"),
        COMPLETED(50, "已完成"),
        OVERDUE_RETURN(60, "归还逾期"),
        OVERDUE(65, "已逾期"),
        AFTER_SALES(70, "售后中"),
        CANCELLED(80, "已取消");

        private final Integer code;
        private final String label;

        OrderStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 订单时效状态。 */
    public enum TimeEffectStatus implements NumericCode
    {
        NORMAL(0, "正常"),
        OVERDUE(1, "已逾期");

        private final Integer code;
        private final String label;

        TimeEffectStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 商品类型。 */
    public enum ProductType implements NumericCode
    {
        DEVICE(1, "主商品"),
        ACCESSORY(2, "配件");

        private final Integer code;
        private final String label;

        ProductType(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 商品库存管理模式。 */
    public enum StockMode implements NumericCode
    {
        SERIAL(1, "逐件库存"),
        QUANTITY(2, "数量库存");

        private final Integer code;
        private final String label;

        StockMode(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 套餐 BOM 组件角色。 */
    public enum PackageComponentRole implements NumericCode
    {
        MAIN(1, "主商品"),
        ACCESSORY(2, "配件"),
        CONSUMABLE(3, "耗材"),
        NON_MAIN_PRODUCT(4, "非主产品");

        private final Integer code;
        private final String label;

        PackageComponentRole(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 套餐组件资源分配策略。 */
    public enum AllocationPolicy implements NumericCode
    {
        ANY(1, "任意可用资源"),
        FIFO(2, "先进先出"),
        FEFO(3, "先到期先出"),
        MANUAL(4, "人工指定");

        private final Integer code;
        private final String label;

        AllocationPolicy(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 库存资源状态。 */
    public enum InventoryStatus implements NumericCode
    {
        AVAILABLE(1, "可用"),
        RESERVED(2, "已预占"),
        RENTED(3, "租赁中"),
        REPAIRING(4, "维修中"),
        LOST(5, "遗失"),
        RETIRED(6, "已报废");

        private final Integer code;
        private final String label;

        InventoryStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 库存预占状态。 */
    public enum ReservationStatus implements NumericCode
    {
        RESERVED(1, "已预占"),
        LOCKED(2, "已锁定"),
        RELEASED(3, "已释放"),
        CONSUMED(4, "已消耗"),
        EXPIRED(5, "已过期");

        private final Integer code;
        private final String label;

        ReservationStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 库存流水变更类型。 */
    public enum InventoryLedgerChangeType implements NumericCode
    {
        MANUAL_ADJUST(1, "人工调整"),
        STATE_TRANSITION(2, "状态流转"),
        RESERVE(3, "库存预占"),
        CONSUME(4, "预占消耗"),
        RELEASE(5, "预占释放"),
        EXPIRE(6, "预占过期");

        private final Integer code;
        private final String label;

        InventoryLedgerChangeType(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 文件配额流水变更类型。 */
    public enum FileQuotaChangeType implements NumericCode
    {
        IMPORT(1, "导入"),
        EXPORT(2, "导出"),
        UPLOAD(3, "上传"),
        DELETE(4, "删除");

        private final Integer code;
        private final String label;

        FileQuotaChangeType(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 订单物流方向。 */
    public enum DeliveryDirection implements NumericCode
    {
        SHIP(1, "发货"),
        RETURN(2, "归还"),
        TRANSFER(3, "调货");

        private final Integer code;
        private final String label;

        DeliveryDirection(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 平台兼容关系类型。 */
    public enum CompatibilityType implements NumericCode
    {
        FULL(1, "完全兼容"),
        CONDITIONAL(2, "条件兼容");

        private final Integer code;
        private final String label;

        CompatibilityType(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 幂等请求处理状态。 */
    public enum IdempotentRequestStatus implements NumericCode
    {
        PROCESSING(1, "处理中"),
        SUCCEEDED(2, "已完成"),
        FAILED(3, "处理失败");

        private final Integer code;
        private final String label;

        IdempotentRequestStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 套餐版本状态。 */
    public enum PackageVersionStatus implements NumericCode
    {
        DRAFT(0, "草稿"),
        ACTIVE(1, "启用"),
        RETIRED(2, "已停用");

        private final Integer code;
        private final String label;

        PackageVersionStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 协议状态。 */
    public enum ProtocolStatus implements NumericCode
    {
        DRAFT(0, "草稿"),
        PUBLISHED(1, "已发布"),
        RETIRED(2, "已停用");

        private final Integer code;
        private final String label;

        ProtocolStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 地址解析状态。 */
    public enum AddressParseStatus implements NumericCode
    {
        FAILED(0, "解析失败"),
        SUCCESS(1, "解析成功"),
        PARTIAL(2, "部分解析");

        private final Integer code;
        private final String label;

        AddressParseStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 天气查询状态。 */
    public enum WeatherQueryStatus implements NumericCode
    {
        FAILED(0, "查询失败"),
        SUCCESS(1, "查询成功");

        private final Integer code;
        private final String label;

        WeatherQueryStatus(Integer code, String label)
        {
            this.code = code;
            this.label = label;
        }

        @Override
        public Integer getCode()
        {
            return code;
        }

        @Override
        public String getLabel()
        {
            return label;
        }
    }

    /** 资产图片类型。 */
    public enum AssetPhotoType implements NumericCode
    {
        DEVICE(1, "设备图片"),
        ACCESSORY(2, "配件图片"),
        PACKAGE(3, "套餐图片"),
        OTHER(9, "其他图片");

        private final Integer code;
        private final String label;

        AssetPhotoType(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 物流数据来源。 */
    public enum LogisticsSourceType implements NumericCode
    {
        SYSTEM(1, "系统创建"),
        MANUAL(2, "人工录入"),
        EXTERNAL(3, "外部同步");

        private final Integer code;
        private final String label;

        LogisticsSourceType(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 物流运单与节点状态。 */
    public enum LogisticsRouteStatus implements NumericCode
    {
        PENDING(0, "待处理"),
        IN_TRANSIT(1, "运输中"),
        DELIVERED(2, "已签收"),
        ABNORMAL(3, "异常"),
        CLOSED(4, "已关闭");

        private final Integer code;
        private final String label;

        LogisticsRouteStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 订单媒体类型。 */
    public enum MediaType implements NumericCode
    {
        SHIP_VIDEO(1, "发货视频"),
        RECEIVE_VIDEO(2, "收货视频"),
        UNBOX_VIDEO(3, "开箱视频"),
        CHECK_PHOTO(4, "验机图片"),
        PRODUCT_PHOTO(5, "商品图片");

        private final Integer code;
        private final String label;

        MediaType(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 媒体与设备匹配状态。 */
    public enum MediaMatchStatus implements NumericCode
    {
        PENDING(0, "待匹配"),
        MATCHED(1, "已匹配"),
        UNMATCHED(2, "未匹配");

        private final Integer code;
        private final String label;

        MediaMatchStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 媒体上传主体。 */
    public enum MediaUploaderType implements NumericCode
    {
        STAFF(1, "员工"),
        CUSTOMER(2, "客户"),
        SYSTEM(3, "系统");

        private final Integer code;
        private final String label;

        MediaUploaderType(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 售后工单类型。 */
    public enum AfterSalesCaseType implements NumericCode
    {
        DAMAGE(1, "损坏"),
        LOST(2, "遗失"),
        COMPENSATION(3, "赔付"),
        OTHER(9, "其他");

        private final Integer code;
        private final String label;

        AfterSalesCaseType(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 售后工单状态。 */
    public enum AfterSalesCaseStatus implements NumericCode
    {
        PROCESSING(0, "处理中"),
        COMPLETED(1, "已完成"),
        CLOSED(2, "已关闭");

        private final Integer code;
        private final String label;

        AfterSalesCaseStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 免押码状态。 */
    public enum DepositFreeCodeStatus implements NumericCode
    {
        CREATED(0, "已创建"),
        APPLIED(1, "已申请"),
        SUCCESS(2, "已生效"),
        EXPIRED(3, "已过期"),
        FAILED(4, "处理失败"),
        CLOSED(5, "已关闭");

        private final Integer code;
        private final String label;

        DepositFreeCodeStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 调拨单状态。 */
    public enum TransferStatus implements NumericCode
    {
        PENDING(0, "待处理"),
        IN_TRANSIT(1, "调拨中"),
        COMPLETED(2, "已完成"),
        CANCELLED(3, "已取消");

        private final Integer code;
        private final String label;

        TransferStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 盘点单状态。 */
    public enum StocktakingStatus implements NumericCode
    {
        PROCESSING(0, "盘点中"),
        COMPLETED(1, "已完成"),
        CLOSED(2, "已关闭");

        private final Integer code;
        private final String label;

        StocktakingStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 盘点差异结果。 */
    public enum StocktakingResult implements NumericCode
    {
        NORMAL(0, "正常"),
        ABNORMAL(1, "异常");

        private final Integer code;
        private final String label;

        StocktakingResult(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 资金流水方向。 */
    public enum FinanceDirection implements NumericCode
    {
        INCOME(1, "收入"),
        EXPENSE(2, "支出"),
        FREEZE(3, "冻结"),
        UNFREEZE(4, "解冻"),
        REVERSE(5, "冲正");

        private final Integer code;
        private final String label;

        FinanceDirection(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 发票状态。 */
    public enum InvoiceStatus implements NumericCode
    {
        PENDING(0, "待开具"),
        ISSUED(1, "已开具"),
        REJECTED(2, "已驳回");

        private final Integer code;
        private final String label;

        InvoiceStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 通用资金流水状态。 */
    public enum LedgerStatus implements NumericCode
    {
        PROCESSING(0, "处理中"),
        SUCCESS(1, "成功"),
        FAILED(2, "失败"),
        CLOSED(3, "已关闭");

        private final Integer code;
        private final String label;

        LedgerStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 服务订阅状态。 */
    public enum SubscriptionStatus implements NumericCode
    {
        ACTIVE(1, "生效中"),
        EXPIRED(2, "已过期"),
        CANCELLED(3, "已取消");

        private final Integer code;
        private final String label;

        SubscriptionStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 推广邀请有效状态。 */
    public enum PromotionEffectiveStatus implements NumericCode
    {
        PENDING(0, "待生效"),
        EFFECTIVE(1, "已生效"),
        INVALID(2, "已失效");

        private final Integer code;
        private final String label;

        PromotionEffectiveStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 租商联盟状态。 */
    public enum AllianceStatus implements NumericCode
    {
        ACTIVE(1, "启用"),
        DISABLED(2, "停用");

        private final Integer code;
        private final String label;

        AllianceStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 商户公告发布范围。 */
    public enum MerchantNoticeScope implements NumericCode
    {
        ALL_MERCHANTS(1, "全部商户"),
        SPECIFIED_MERCHANTS(2, "指定商户");

        private final Integer code;
        private final String label;

        MerchantNoticeScope(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 租商联盟成员状态。 */
    public enum AllianceMemberStatus implements NumericCode
    {
        ACTIVE(1, "正常"),
        DISABLED(2, "停用"),
        QUIT(3, "已退出");

        private final Integer code;
        private final String label;

        AllianceMemberStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 联盟纠纷类型。 */
    public enum AllianceDisputeType implements NumericCode
    {
        GENERAL(0, "通用"),
        DAMAGE(1, "损坏"),
        LOSS(2, "丢失"),
        DELAYED_FULFILLMENT(3, "延迟履约"),
        FEE(4, "费用"),
        OTHER(9, "其他");

        private final Integer code;
        private final String label;

        AllianceDisputeType(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }

        public static AllianceDisputeType fromCode(Integer code)
        {
            for (AllianceDisputeType type : values())
            {
                if (type.code.equals(code)) return type;
            }
            return null;
        }
    }

    /** 联盟订单发货方式。 */
    public enum ShippingMode implements NumericCode
    {
        EXPRESS(1, "快递"),
        SELF_PICKUP(2, "自提"),
        DELIVERY(3, "配送");

        private final Integer code;
        private final String label;

        ShippingMode(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 联盟订单状态。 */
    public enum AllianceOrderStatus implements NumericCode
    {
        PENDING(0, "待接单"),
        ACCEPTED(1, "已接单"),
        SHIPPING(2, "配送中"),
        COMPLETED(3, "已完成"),
        CANCELLED(4, "已取消");

        private final Integer code;
        private final String label;

        AllianceOrderStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 租商联盟结算状态。 */
    public enum AllianceSettlementStatus implements NumericCode
    {
        PENDING_CONFIRM(0, "待确认"),
        CONFIRMED(1, "已确认"),
        DISPUTED(2, "争议中"),
        CLOSED(3, "已关闭");

        private final Integer code;
        private final String label;

        AllianceSettlementStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 商户市场板块类型。 */
    public enum MarketBoardType implements NumericCode
    {
        SALE(1, "出售"),
        RENT(2, "出租"),
        BUY(3, "求购");

        private final Integer code;
        private final String label;

        MarketBoardType(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 商户市场帖子状态。 */
    public enum MarketPostStatus implements NumericCode
    {
        DRAFT(0, "草稿"),
        PUBLISHED(1, "已发布"),
        CLOSED(2, "已关闭"),
        OFF_SHELF(3, "已下架");

        private final Integer code;
        private final String label;

        MarketPostStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 商户市场举报处理状态。 */
    public enum MarketReportStatus implements NumericCode
    {
        PENDING(0, "待处理"),
        RESOLVED(1, "已处理"),
        REJECTED(2, "已驳回");

        private final Integer code;
        private final String label;

        MarketReportStatus(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 免押资金流水方向。 */
    public enum DepositLedgerDirection implements NumericCode
    {
        PAY(1, "缴纳"),
        DEDUCT(2, "扣除"),
        REFUND(3, "退回");

        private final Integer code;
        private final String label;

        DepositLedgerDirection(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 商户业务审计操作主体。 */
    public enum AuditActorType implements NumericCode
    {
        MERCHANT_USER(1, "商户用户"),
        PLATFORM_USER(2, "平台用户"),
        SYSTEM(3, "系统");

        private final Integer code;
        private final String label;

        AuditActorType(Integer code, String label) { this.code = code; this.label = label; }
        @Override public Integer getCode() { return code; }
        @Override public String getLabel() { return label; }
    }

    /** 返回状态编码对应显示名称。 */
    public static <E extends Enum<E> & NumericCode> String labelOf(E[] values, Integer code, String fallback)
    {
        if (code == null)
        {
            return fallback;
        }
        for (E value : values)
        {
            if (value.getCode().equals(code))
            {
                return value.getLabel();
            }
        }
        return fallback;
    }
}
