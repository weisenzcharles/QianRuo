package com.ruoyi.system.controller.internal;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.system.api.RemoteProductClassService;
import com.ruoyi.system.api.model.ProductClass;
import com.ruoyi.system.service.rental.RentalProductClassService;

/** 商户服务读取平台产品分类的内部入口。 */
@RestController
@RequestMapping(RemoteProductClassService.INTERNAL_PREFIX)
public class InternalProductClassController
{
    private final RentalProductClassService productClassService;

    /**
     * 注入平台产品分类业务服务。
     *
     * @param productClassService 平台产品分类业务服务
     */
    public InternalProductClassController(RentalProductClassService productClassService)
    {
        this.productClassService = productClassService;
    }

    /**
     * 向可信内部服务返回平台全部正常分类。
     *
     * @return 平台正常分类扁平集合
     */
    @InnerAuth
    @GetMapping
    public R<List<ProductClass>> listActive()
    {
        return R.ok(productClassService.listActiveForInternal());
    }
}
