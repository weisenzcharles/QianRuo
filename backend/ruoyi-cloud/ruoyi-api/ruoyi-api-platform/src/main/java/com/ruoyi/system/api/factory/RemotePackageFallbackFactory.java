package com.ruoyi.system.api.factory;

import java.util.List;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemotePackageService;
import com.ruoyi.system.api.model.PlatformPackage;

/** 平台套餐内部服务降级处理。 */
@Component
public class RemotePackageFallbackFactory implements FallbackFactory<RemotePackageService>
{
    @Override
    public RemotePackageService create(Throwable cause)
    {
        return source -> R.fail("平台套餐服务暂不可用");
    }
}
