package com.ruoyi.rentalbusiness.service;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.merchant.api.model.MerchantOrderPaymentCompletionCommand;
import com.ruoyi.merchant.api.model.MerchantOrderPaymentTarget;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderMapper;

/** 租赁业务订单支付状态服务，只维护订单事实，不处理支付宝协议。 */
@Service
public class MerchantOrderPaymentService
{
    private final MerchantOrderMapper mapper;

    public MerchantOrderPaymentService(MerchantOrderMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 查询当前商户订单可用于创建支付单的目标信息。 */
    public MerchantOrderPaymentTarget getTarget(Long tenantId, Long memberId, Long orderId)
    {
        Map<String, Object> row = mapper.selectMerchantOrderPaymentTarget(tenantId, memberId, orderId);
        if (row == null)
        {
            return null;
        }
        MerchantOrderPaymentTarget target = new MerchantOrderPaymentTarget();
        target.setOrderId(number(row.get("orderId")));
        target.setOrderNo(string(row.get("orderNo")));
        target.setAmount(decimal(row.get("amount")));
        target.setPayStatus(integer(row.get("payStatus")));
        target.setOrderStatus(integer(row.get("orderStatus")));
        target.setSubject("租赁订单 " + target.getOrderNo() + " 支付");
        return target;
    }

    /** 幂等推进订单付款状态，重复支付宝通知视为成功。 */
    public boolean complete(MerchantOrderPaymentCompletionCommand command)
    {
        if (command == null || command.getTenantId() == null || command.getMemberId() == null
                || command.getOrderId() == null)
        {
            return false;
        }
        MerchantOrderPaymentTarget target = getTarget(command.getTenantId(), command.getMemberId(), command.getOrderId());
        if (target == null || RentalCode.OrderStatus.CANCELLED.getCode().equals(target.getOrderStatus()))
        {
            return false;
        }
        if (RentalCode.PaymentStatus.PAID.getCode().equals(target.getPayStatus()))
        {
            return true;
        }
        if (!RentalCode.PaymentStatus.UNPAID.getCode().equals(target.getPayStatus()))
        {
            return false;
        }
        int changed = mapper.updateMerchantOrderPayStatus(command.getTenantId(), command.getMemberId(),
                command.getOrderId(), RentalCode.PaymentStatus.UNPAID.getCode(), RentalCode.PaymentStatus.PAID.getCode(),
                RentalCode.OrderStatus.CANCELLED.getCode());
        if (changed == 1)
        {
            return true;
        }
        MerchantOrderPaymentTarget latest = getTarget(command.getTenantId(), command.getMemberId(), command.getOrderId());
        return latest != null && RentalCode.PaymentStatus.PAID.getCode().equals(latest.getPayStatus());
    }

    private Long number(Object value)
    {
        return value == null ? null : Long.valueOf(value.toString());
    }

    private Integer integer(Object value)
    {
        return value == null ? null : Integer.valueOf(value.toString());
    }

    private BigDecimal decimal(Object value)
    {
        return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
    }

    private String string(Object value)
    {
        return value == null ? null : value.toString();
    }
}
