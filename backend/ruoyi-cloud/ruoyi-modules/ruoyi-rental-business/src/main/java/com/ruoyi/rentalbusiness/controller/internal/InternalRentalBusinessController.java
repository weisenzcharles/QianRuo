package com.ruoyi.rentalbusiness.controller.internal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;

/**
 * 租赁业务服务内部探活接口。
 *
 * 该接口仅用于网关、部署脚本或其他服务验证服务注册与内部鉴权是否正常，不承载
 * 商品、订单等业务能力，也不允许浏览器端直接调用。
 */
@RestController
@RequestMapping("/internal/rental-business")
public class InternalRentalBusinessController
{
    /**
     * 返回固定服务标识，供内部调用方确认请求已经到达 rental-business。
     *
     * @return 租赁业务服务标识
     */
    @InnerAuth
    @GetMapping("/ping")
    public R<String> ping()
    {
        return R.ok("rental-business");
    }
}
