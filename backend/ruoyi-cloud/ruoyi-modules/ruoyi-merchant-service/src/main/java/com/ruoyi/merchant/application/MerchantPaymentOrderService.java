package com.ruoyi.merchant.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.merchant.mapper.MerchantPaymentMapper;
import com.ruoyi.merchant.model.MerchantPaymentBizType;
import com.ruoyi.merchant.model.MerchantPaymentRecord;

/** 订单支付单本地创建服务，确保同一订单不会并发生成多张有效支付单。 */
@Service
public class MerchantPaymentOrderService
{
    private static final DateTimeFormatter PAYMENT_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final MerchantPaymentMapper mapper;

    public MerchantPaymentOrderService(MerchantPaymentMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 锁定当前订单最近支付单，复用有效待支付单，过期后关闭并创建新单。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantPaymentRecord createOrReuse(Long tenantId, Long memberId, Long orderId, String orderNo,
            String subject, BigDecimal amount, LocalDateTime expireTime, Long operatorId)
    {
        MerchantPaymentRecord existing = mapper.selectLatestByBusinessForUpdate(tenantId, memberId,
                MerchantPaymentBizType.ORDER.getCode(), orderId);
        if (existing != null)
        {
            if (RentalCode.PaymentStatus.PAID.getCode().equals(existing.getPaymentStatus()))
            {
                throw new ServiceException("订单当前无需支付");
            }
            if (RentalCode.PaymentStatus.UNPAID.getCode().equals(existing.getPaymentStatus()))
            {
                if (existing.getAmount() == null || existing.getAmount().compareTo(amount) != 0)
                {
                    throw new ServiceException("订单存在金额不一致的待支付单，请先完成或关闭原支付单");
                }
                if (existing.getExpireTime() == null || existing.getExpireTime().isAfter(LocalDateTime.now()))
                {
                    return existing;
                }
                mapper.closeExpiredPayment(existing.getPaymentId(), RentalCode.PaymentStatus.UNPAID.getCode(),
                        RentalCode.PaymentStatus.CLOSED.getCode());
            }
        }

        String paymentNo = "ORD" + LocalDateTime.now().format(PAYMENT_NO_TIME)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        mapper.insertPaymentOrder(tenantId, memberId, paymentNo, MerchantPaymentBizType.ORDER.getCode(), orderId,
                orderNo, subject, amount, expireTime, operatorId, RentalCode.PaymentStatus.UNPAID.getCode());
        MerchantPaymentRecord payment = mapper.selectByPaymentNo(tenantId, memberId, paymentNo);
        if (payment == null)
        {
            throw new ServiceException("支付单创建失败");
        }
        return payment;
    }
}
