package com.ruoyi.merchant.controller.internal;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.merchant.api.MerchantServiceConstants;
import com.ruoyi.merchant.application.MerchantProductClassService;

/** 租赁业务服务使用的商户经营类目内部入口。 */
@RestController
@RequestMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/class")
public class InternalMerchantProductClassController
{
    private final MerchantProductClassService productClassService;

    /**
     * 注入商户经营类目服务。
     *
     * @param productClassService 商户经营类目服务
     */
    public InternalMerchantProductClassController(MerchantProductClassService productClassService)
    {
        this.productClassService = productClassService;
    }

    /**
     * 返回当前商户已选择的类目主键，供租赁业务服务过滤商品列表。
     *
     * @return 当前商户经营类目主键集合
     */
    @InnerAuth
    @GetMapping("/selected")
    public R<List<Long>> selectedIds()
    {
        return R.ok(productClassService.listSelectedIds());
    }

    /**
     * 校验当前请求中的商户是否选择了指定类目。
     *
     * @param classId 平台产品类目主键
     * @return 校验通过返回 true，未选择时由应用服务抛出业务异常
     */
    @InnerAuth
    @GetMapping("/assert/{classId}")
    public R<Boolean> assertAllowed(@PathVariable Long classId)
    {
        productClassService.assertAllowed(classId);
        return R.ok(Boolean.TRUE);
    }
}
