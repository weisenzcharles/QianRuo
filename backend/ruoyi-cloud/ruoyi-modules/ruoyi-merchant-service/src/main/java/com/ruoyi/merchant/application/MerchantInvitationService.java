package com.ruoyi.merchant.application;

import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.system.api.RemoteMerchantPromotionService;
import com.ruoyi.system.api.model.MerchantInvitationOverviewVO;

/** 商户端我的邀请应用服务。 */
@Service
public class MerchantInvitationService
{
    private final RemoteMerchantPromotionService remoteService;
    /** @param remoteService 平台推广内部契约 */
    public MerchantInvitationService(RemoteMerchantPromotionService remoteService) { this.remoteService = remoteService; }

    /**
     * 使用认证上下文查询当前商户邀请概览，不接受客户端传入商户归属。
     *
     * @return 当前商户邀请概览
     */
    public MerchantInvitationOverviewVO overview()
    {
        R<MerchantInvitationOverviewVO> response = remoteService.overview(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), SecurityConstants.INNER);
        if (response == null || R.FAIL == response.getCode() || response.getData() == null)
        {
            throw new ServiceException(response == null ? "平台推广服务无响应" : response.getMsg());
        }
        return response.getData();
    }
}
