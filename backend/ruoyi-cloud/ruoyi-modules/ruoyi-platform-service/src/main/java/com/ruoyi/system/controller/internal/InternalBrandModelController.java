package com.ruoyi.system.controller.internal;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.system.api.RemoteBrandModelService;
import com.ruoyi.system.api.model.BrandModel;
import com.ruoyi.system.service.rental.RentalBrandModelService;

/** 商户服务读取平台标准品牌型号的内部入口。 */
@RestController
@RequestMapping(RemoteBrandModelService.INTERNAL_PREFIX)
public class InternalBrandModelController
{
    private final RentalBrandModelService brandModelService;

    /**
     * 注入标准品牌型号业务服务。
     *
     * @param brandModelService 标准品牌型号业务服务
     */
    public InternalBrandModelController(RentalBrandModelService brandModelService)
    {
        this.brandModelService = brandModelService;
    }

    /**
     * 向可信商户服务发布平台当前有效的标准品牌型号。
     *
     * @return 平台标准品牌型号集合
     */
    @InnerAuth
    @GetMapping
    public R<List<BrandModel>> listActive()
    {
        return R.ok(brandModelService.listActiveForInternal());
    }
}
