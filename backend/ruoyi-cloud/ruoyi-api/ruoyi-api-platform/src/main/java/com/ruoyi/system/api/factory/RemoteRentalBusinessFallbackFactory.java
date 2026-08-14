package com.ruoyi.system.api.factory;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteRentalBusinessService;
import com.ruoyi.system.api.model.PlatformWarehouseView;

/** 租赁业务服务内部契约降级处理。 */
@Component
public class RemoteRentalBusinessFallbackFactory implements FallbackFactory<RemoteRentalBusinessService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteRentalBusinessFallbackFactory.class);

    /**
     * 创建失败即拒绝的降级实现，避免租赁业务服务不可用时平台误放行仓库范围或删除标准型号。
     *
     * @param cause 远程调用失败原因
     * @return 明确返回失败结果的降级实现
     */
    @Override
    public RemoteRentalBusinessService create(Throwable cause)
    {
        log.error("租赁业务服务内部调用失败: {}", cause == null ? "未知异常" : cause.getMessage(), cause);
        return new RemoteRentalBusinessService()
        {
            /** 远程仓库查询失败时返回失败结果，禁止调用方使用不完整列表。 */
            @Override
            public R<List<PlatformWarehouseView>> listWarehouses(Long tenantId, Long memberId, Integer status,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 远程仓库范围校验失败时返回失败结果，禁止调用方放行越权仓库。 */
            @Override
            public R<Integer> countEnabledWarehouses(Long tenantId, Long memberId, List<Long> warehouseIds,
                    String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }

            /** 远程型号引用校验失败时返回失败结果，禁止调用方删除标准型号。 */
            @Override
            public R<Boolean> hasModelReferences(Long modelId, String source)
            {
                return R.fail("租赁业务服务暂不可用");
            }
        };
    }
}
