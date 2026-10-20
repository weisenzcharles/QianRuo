package com.ruoyi.merchant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresLogin;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.RemoteMerchantCertificationService;
import com.ruoyi.system.api.RemotePlatformGovernanceService;
import com.ruoyi.system.api.model.MerchantCreditAppealCommand;
import com.ruoyi.system.api.model.MerchantCreditAppealView;
import com.ruoyi.system.api.model.MerchantCreditRiskView;
import com.ruoyi.system.api.model.MerchantCertificationSupplementCommand;
import com.ruoyi.system.api.model.MerchantCertificationView;

/** 商户认证资料查询入口。商户端只能读取当前登录主体，不允许提交或修改审核结果。 */
@RestController
@RequestMapping("/merchant/certification")
public class MerchantCertificationController
{
    @Autowired
    private RemoteMerchantCertificationService certificationService;

    @Autowired
    private RemotePlatformGovernanceService governanceService;

    /**
     * 查询当前商户完整认证材料、认证历史、会员类型和接单资格历史。
     * 商户唯一编号只从服务端登录上下文获取，忽略任何请求参数。
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

    /**
     * 提交平台要求的认证补件。商户编号和操作人从当前登录上下文获取，忽略客户端同名字段。
     *
     * @param command 补交材料地址
     * @return 更新后的认证资料
     */
    @RequiresLogin
    @PostMapping("/supplement")
    public AjaxResult submitSupplement(@RequestBody MerchantCertificationSupplementCommand command)
    {
        if (command == null)
        {
            return AjaxResult.error("补件资料不能为空");
        }
        command.setMemberId(MerchantContextUtils.getMemberId());
        command.setOperatorId(SecurityUtils.getUserId());
        command.setOperatorName(SecurityUtils.getUsername());
        R<MerchantCertificationView> result = certificationService.submitSupplement(
                command.getMemberId(), command, SecurityConstants.INNER);
        if (result == null || R.FAIL == result.getCode())
        {
            return AjaxResult.error(result == null ? "商户认证资料服务无响应" : result.getMsg());
        }
        return AjaxResult.success(result.getData());
    }

    /** 查询当前商户信用摘要和可申诉流水。 */
    @RequiresLogin
    @GetMapping("/credit-risk")
    public AjaxResult creditRisk()
    {
        R<MerchantCreditRiskView> result = governanceService.getMerchantCreditRisk(
                MerchantContextUtils.getMemberId(), SecurityConstants.INNER);
        if (result == null || R.FAIL == result.getCode())
        {
            return AjaxResult.error(result == null ? "商户信用服务无响应" : result.getMsg());
        }
        return AjaxResult.success(result.getData());
    }

    /** 提交当前商户信用流水申诉，平台审核后通过追加冲正流水处理。 */
    @RequiresLogin
    @PostMapping("/credit-appeals")
    public AjaxResult submitCreditAppeal(@RequestBody MerchantCreditAppealCommand command)
    {
        R<MerchantCreditAppealView> result = governanceService.createMerchantCreditAppeal(
                MerchantContextUtils.getMemberId(), command, SecurityConstants.INNER);
        if (result == null || R.FAIL == result.getCode())
        {
            return AjaxResult.error(result == null ? "信用申诉服务无响应" : result.getMsg());
        }
        return AjaxResult.success(result.getData());
    }
}
