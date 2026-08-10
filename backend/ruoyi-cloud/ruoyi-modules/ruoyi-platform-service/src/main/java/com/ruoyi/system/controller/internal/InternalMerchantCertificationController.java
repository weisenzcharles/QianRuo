package com.ruoyi.system.controller.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.system.api.RemoteMerchantCertificationService;
import com.ruoyi.system.api.model.MerchantCertificationView;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.mapper.rental.RentalMemberMapper;
import com.ruoyi.system.service.rental.RentalMemberCertificationService;

/** 商户服务读取平台认证主数据的内部入口。 */
@RestController
@RequestMapping(RemoteMerchantCertificationService.INTERNAL_PREFIX)
public class InternalMerchantCertificationController
{
    @Autowired
    private RentalMemberMapper memberMapper;

    @Autowired
    private RentalMemberCertificationService certificationService;

    /**
     * 仅允许内部服务按可信商户主键读取完整认证聚合资料，平台主键由商户登录上下文产生。
     *
     * @param memberId 商户主键
     * @return 商户认证、会员类型和接单资格资料
     */
    @InnerAuth
    @GetMapping("/member/{memberId}")
    public R<MerchantCertificationView> getByMemberId(@PathVariable Long memberId)
    {
        RentalMember member = memberMapper.selectRentalMemberById(memberId);
        if (member == null)
        {
            throw new ServiceException("商户不存在或已删除");
        }
        return R.ok(certificationService.toMerchantView(member));
    }
}
