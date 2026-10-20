package com.ruoyi.system.api.factory;

import java.util.Collections;
import java.util.List;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteBrandModelService;
import com.ruoyi.system.api.model.BrandModel;

/** 平台标准品牌型号内部服务降级处理。 */
@Component
public class RemoteBrandModelFallbackFactory implements FallbackFactory<RemoteBrandModelService>
{
    /**
     * 创建平台型号内部服务的降级实现，避免平台不可用时商户端误展示过期或不完整主数据。
     *
     * @param cause 远程调用失败原因
     * @return 标准型号内部服务降级实现
     */
    @Override
    public RemoteBrandModelService create(Throwable cause)
    {
        return new RemoteBrandModelService()
        {
            /**
             * 返回明确失败结果，商户应用服务会把失败转换为业务异常。
             *
             * @param source 内部服务调用标识
             * @return 平台型号服务不可用结果
             */
            @Override
            public R<List<BrandModel>> listActive(String source)
            {
                return R.fail("平台标准品牌型号服务暂不可用");
            }
        };
    }
}
