package com.ruoyi.merchant.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.merchant.model.MerchantPaymentRecord;

/** 商户支付单和财务入账数据访问。 */
public interface MerchantPaymentMapper
{
    /** 新增待支付单。 */
    int insertPaymentOrder(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("paymentNo") String paymentNo, @Param("bizType") String bizType, @Param("bizId") Long bizId,
            @Param("bizNo") String bizNo, @Param("subject") String subject, @Param("amount") BigDecimal amount,
            @Param("expireTime") LocalDateTime expireTime, @Param("operatorId") Long operatorId,
            @Param("pendingStatus") Integer pendingStatus);

    /** 按支付单号锁定当前支付单，支付宝通知处理必须使用该方法保证幂等。 */
    MerchantPaymentRecord selectByPaymentNoForUpdate(@Param("paymentNo") String paymentNo);

    /** 按当前商户范围查询支付单。 */
    MerchantPaymentRecord selectByPaymentNo(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("paymentNo") String paymentNo);

    /** 按业务单锁定最近支付单，避免同一订单并发创建多张支付单。 */
    MerchantPaymentRecord selectLatestByBusinessForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("bizType") String bizType, @Param("bizId") Long bizId);

    /** 按流水号锁定赔付流水，保证跨服务重试不会重复入账。 */
    Integer countFinanceLedgerByNoForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("ledgerNo") String ledgerNo);

    /** 关闭已经过期的未支付单。 */
    int closeExpiredPayment(@Param("paymentId") Long paymentId, @Param("unpaidStatus") Integer unpaidStatus,
            @Param("closedStatus") Integer closedStatus);

    /** 仅将未支付单推进为已支付。 */
    int markPaid(@Param("paymentId") Long paymentId, @Param("alipayTradeNo") String alipayTradeNo,
            @Param("notifyId") String notifyId, @Param("pendingStatus") Integer pendingStatus,
            @Param("paidStatus") Integer paidStatus);

    /** 读取当前商户可用余额的最新值，入账事务内计算 balance_after。 */
    BigDecimal selectAvailableBalanceForUpdate(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("incomeCode") Integer incomeCode, @Param("expenseCode") Integer expenseCode,
            @Param("unfreezeCode") Integer unfreezeCode, @Param("reverseCode") Integer reverseCode,
            @Param("successStatus") Integer successStatus, @Param("accountType") Integer accountType);

    /** 新增与支付单一一对应的不可变财务流水。 */
    int insertFinanceLedger(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("ledgerNo") String ledgerNo, @Param("bizType") String bizType, @Param("amount") BigDecimal amount,
            @Param("balanceAfter") BigDecimal balanceAfter, @Param("refId") Long refId,
            @Param("refType") String refType, @Param("payChannel") String payChannel,
            @Param("payOrderNo") String payOrderNo, @Param("direction") Integer direction,
            @Param("ledgerStatus") Integer ledgerStatus, @Param("operatorId") Long operatorId,
            @Param("remark") String remark, @Param("accountType") Integer accountType);

    /** 在账务流水提交成功后更新商户通用余额投影。 */
    int upsertAccountBalance(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("accountType") Integer accountType, @Param("amount") BigDecimal amount,
            @Param("operatorId") Long operatorId);
}
