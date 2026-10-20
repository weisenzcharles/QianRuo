package com.ruoyi.merchant.api;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.factory.RemoteMerchantProductClassFallbackFactory;

/** 商户经营产品分类内部契约。 */
@FeignClient(contextId = "remoteMerchantProductClassService", value = MerchantServiceConstants.SERVICE_NAME,
        fallbackFactory = RemoteMerchantProductClassFallbackFactory.class)
public interface RemoteMerchantProductClassService
{
    /**
     * 查询当前登录商户已选择的产品分类唯一编号，供业务服务过滤商品数据。
     *
     * @param source 内部调用标识
     * @return 当前商户经营分类唯一编号集合
     */
    @GetMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/class/selected")
    R<List<Long>> selectedIds(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 校验当前登录商户是否经营指定平台产品分类。
     *
     * @param classId 平台产品分类唯一编号
     * @param source 内部调用标识
     * @return 校验结果
     */
    @GetMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/class/assert/{classId}")
    R<Boolean> assertAllowed(@PathVariable("classId") Long classId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
