package com.ruoyi.merchant.api.factory;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.RemoteRentalBusinessService;
import com.ruoyi.merchant.api.model.MerchantFinanceSettlementView;
import com.ruoyi.merchant.api.model.MerchantMarketInventoryQuery;
import com.ruoyi.merchant.api.model.MerchantMarketInventoryUnitView;
import com.ruoyi.merchant.api.model.MerchantOrderPaymentCompletionCommand;
import com.ruoyi.merchant.api.model.MerchantOrderPaymentTarget;
import com.ruoyi.merchant.api.model.MerchantWarehouseView;

/** 租赁业务服务仓库契约降级处理。 */
@Component
public class RemoteRentalBusinessFallbackFactory implements FallbackFactory<RemoteRentalBusinessService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteRentalBusinessFallbackFactory.class);

    /**
     * 创建失败即拒绝的降级实现，避免租赁业务服务不可用时商户端展示不完整仓库范围。
     *
     * @param cause 远程调用失败原因
     * @return 明确返回失败结果的降级实现
     */
    @Override
    public RemoteRentalBusinessService create(Throwable cause)
    {
        log.error("商户端调用租赁业务服务失败: {}", cause == null ? "未知异常" : cause.getMessage(), cause);
        return new RemoteRentalBusinessService()
        {
            /** 远程仓库查询失败时返回失败结果，不以空列表伪装成功。 */
            @Override
            public R<List<MerchantWarehouseView>> listWarehouses(Long tenantId, Long memberId, Integer status,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<List<MerchantFinanceSettlementView>> listFinanceSettlements(Long tenantId, Long memberId,
                    Integer status, String beginTime, String endTime, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<MerchantOrderPaymentTarget> getOrderPaymentTarget(Long tenantId, Long memberId, Long orderId,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<Boolean> completeOrderPayment(MerchantOrderPaymentCompletionCommand command, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            @Override
            public R<List<MerchantMarketInventoryUnitView>> listMarketEligibleUnits(
                    MerchantMarketInventoryQuery query, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }
        };
    }
}
