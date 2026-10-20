package com.ruoyi.merchant.api.factory;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.RemoteMerchantUserService;
import com.ruoyi.merchant.api.model.MerchantLoginIdentity;
import com.ruoyi.merchant.api.model.MerchantLoginRecordCommand;
import com.ruoyi.merchant.api.model.MerchantOwnerProvisionCommand;
import com.ruoyi.merchant.api.model.MerchantUserSummaryQuery;
import com.ruoyi.merchant.api.model.MerchantUserSummary;

/** 商户身份服务降级处理。 */
@Component
public class RemoteMerchantUserFallbackFactory implements FallbackFactory<RemoteMerchantUserService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteMerchantUserFallbackFactory.class);

    /**
     * 创建商户身份接口的降级实现，确保认证和平台开户调用失败时返回明确错误而不是空身份。
     *
     * @param cause Feign 调用失败原因
     * @return 商户身份接口降级实现
     */
    @Override
    public RemoteMerchantUserService create(Throwable cause)
    {
        log.error("商户身份服务调用失败: {}", cause.getMessage());
        return new RemoteMerchantUserService()
        {
            @Override
            public R<MerchantLoginIdentity> getByUsername(String username, String source, String clientType)
            {
                return R.fail("获取商户用户失败:" + cause.getMessage());
            }

            @Override
            public R<MerchantLoginIdentity> getByPhone(String phone, String source, String clientType)
            {
                return R.fail("获取商户手机号用户失败:" + cause.getMessage());
            }

            @Override
            public R<List<MerchantUserSummary>> getSummaries(MerchantUserSummaryQuery query, String source,
                    String clientType)
            {
                return R.fail("获取商户用户摘要失败:" + cause.getMessage());
            }

            @Override
            public R<List<MerchantUserSummary>> getActiveSummaries(MerchantUserSummaryQuery query, String source,
                    String clientType)
            {
                return R.fail("获取可指派商户用户失败:" + cause.getMessage());
            }

            @Override
            public R<Boolean> recordLogin(MerchantLoginRecordCommand command, String source, String clientType)
            {
                return R.fail("记录商户登录信息失败:" + cause.getMessage());
            }

            @Override
            public R<Boolean> provisionOwner(MerchantOwnerProvisionCommand command, String source, String clientType)
            {
                return R.fail("开通商户管理员失败:" + cause.getMessage());
            }
        };
    }
}
