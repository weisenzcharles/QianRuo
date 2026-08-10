package com.ruoyi.merchant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresLogin;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.system.api.RemoteMerchantCertificationService;
import com.ruoyi.system.api.model.MerchantCertificationView;

/** 商户认证资料查询入口。商户端只能读取当前登录主体，不允许提交或修改审核结果。 */
@RestController
@RequestMapping("/merchant/certification")
public class MerchantCertificationController
{
    @Autowired
    private RemoteMerchantCertificationService certificationService;

    /**
     * 查询当前商户完整认证材料、认证历史、会员类型和接单资格历史。
     * 商户主键只从服务端登录上下文获取，忽略任何请求参数。
     *
     * @return 当前商户认证聚合资料
     */
    @RequiresLogin
    @GetMapping
    public AjaxResult get()
    {
        R<MerchantCertificationView> result = certificationService.getByMemberId(
                MerchantContextUtils.getMemberId(), SecurityConstants.INNER);
        if (result == null || R.FAIL == result.getCode())
        {
            return AjaxResult.error(result == null ? "商户认证资料服务无响应" : result.getMsg());
        }
        return AjaxResult.success(result.getData());
    }
}
