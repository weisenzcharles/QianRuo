package com.ruoyi.system.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteCodeRuleService;

/** 平台统一编码分配服务降级处理。 */
@Component
public class RemoteCodeRuleFallbackFactory implements FallbackFactory<RemoteCodeRuleService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteCodeRuleFallbackFactory.class);

    /**
     * 创建失败实现，编码服务不可用时禁止业务服务自行拼接编码，避免出现重复编码。
     *
     * @param cause 远程调用失败原因
     * @return 统一编码服务降级实现
     */
    @Override
    public RemoteCodeRuleService create(Throwable cause)
    {
        log.error("平台统一编码服务调用失败", cause);
        return new RemoteCodeRuleService()
        {
            /**
             * 返回明确失败结果，调用方会把失败转换为业务异常并回滚当前写入事务。
             *
             * @param codeType 编码类型键
             * @param source 内部服务调用标识
             * @return 编码服务不可用结果
             */
            @Override
            public R<String> nextCode(String codeType, String source)
            {
                return R.fail("平台统一编码服务暂不可用，当前操作未执行");
            }
        };
    }
}
