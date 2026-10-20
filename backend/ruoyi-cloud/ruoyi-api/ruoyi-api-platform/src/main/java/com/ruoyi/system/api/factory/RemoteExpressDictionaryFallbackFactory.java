package com.ruoyi.system.api.factory;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteExpressDictionaryService;
import com.ruoyi.system.api.domain.SysDictData;

/** 平台快递公司字典服务降级处理。 */
@Component
public class RemoteExpressDictionaryFallbackFactory implements FallbackFactory<RemoteExpressDictionaryService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteExpressDictionaryFallbackFactory.class);

    @Override
    public RemoteExpressDictionaryService create(Throwable cause)
    {
        log.error("平台快递公司字典服务调用失败", cause);
        return (dictType, source) -> R.ok(Collections.<SysDictData>emptyList());
    }
}
