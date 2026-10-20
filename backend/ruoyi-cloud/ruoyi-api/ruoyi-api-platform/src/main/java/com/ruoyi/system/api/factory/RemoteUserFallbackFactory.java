package com.ruoyi.system.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteUserService;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;

/**
 * 用户服务降级处理
 *
 * @author ruoyi
 */
@Component
public class RemoteUserFallbackFactory implements FallbackFactory<RemoteUserService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteUserFallbackFactory.class);

    private static String getErrorMessage(Throwable throwable)
    {
        StringBuilder message = new StringBuilder(throwable.getMessage());
        Throwable cause = throwable.getCause();
        int depth = 0;
        while (cause != null && depth < 3)
        {
            if (cause.getMessage() != null && message.indexOf(cause.getMessage()) < 0)
            {
                message.append("; ").append(cause.getMessage());
            }
            cause = cause.getCause();
            depth++;
        }
        return message.toString();
    }

    @Override
    public RemoteUserService create(Throwable throwable)
    {
        log.error("用户服务调用失败", throwable);
        String errorMessage = getErrorMessage(throwable);
        return new RemoteUserService()
        {
            @Override
            public R<LoginUser> getAdminUserInfo(String username, String source)
            {
                return R.fail("获取平台用户失败:" + errorMessage);
            }

            @Override
            public R<Boolean> registerUserInfo(SysUser sysUser, String source)
            {
                return R.fail("注册用户失败:" + errorMessage);
            }

            @Override
            public R<Boolean> recordAdminLogin(SysUser sysUser, String source)
            {
                return R.fail("记录平台用户登录信息失败:" + errorMessage);
            }

        };
    }
}
