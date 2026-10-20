package com.ruoyi.system.api;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.domain.SysDictData;
import com.ruoyi.system.api.factory.RemoteExpressDictionaryFallbackFactory;

/** 租赁业务服务读取平台快递公司字典的内部契约。 */
@FeignClient(contextId = "remoteExpressDictionaryService", value = ServiceNameConstants.PLATFORM_SERVICE,
        fallbackFactory = RemoteExpressDictionaryFallbackFactory.class)
public interface RemoteExpressDictionaryService
{
    String INTERNAL_PREFIX = "/internal/platform/dictionaries";

    /** 查询平台当前启用的快递公司字典数据。 */
    @GetMapping(INTERNAL_PREFIX + "/{dictType}")
    R<List<SysDictData>> list(@PathVariable("dictType") String dictType,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
