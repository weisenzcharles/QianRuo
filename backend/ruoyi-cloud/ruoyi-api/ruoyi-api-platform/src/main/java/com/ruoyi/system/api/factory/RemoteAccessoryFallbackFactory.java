package com.ruoyi.system.api.factory;

import java.util.List;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteAccessoryService;
import com.ruoyi.system.api.model.PlatformAccessoryOption;

/** 平台官方配件内部服务降级处理。 */
@Component
public class RemoteAccessoryFallbackFactory implements FallbackFactory<RemoteAccessoryService>
{
    @Override
    public RemoteAccessoryService create(Throwable cause)
    {
        return source -> R.fail("平台配件服务暂不可用");
    }
}
