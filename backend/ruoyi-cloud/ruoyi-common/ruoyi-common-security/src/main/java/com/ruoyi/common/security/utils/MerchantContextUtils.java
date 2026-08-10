package com.ruoyi.common.security.utils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.api.model.LoginUser;

/**
 * 商户请求上下文。租户、商户和仓库范围只读取服务端登录态，禁止信任请求参数。
 *
 * @author qianruo
 */
public final class MerchantContextUtils
{
    private MerchantContextUtils()
    {
    }

    public static boolean isMerchantUser()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return StringUtils.isNotNull(loginUser)
                && Constants.CLIENT_TYPE_MERCHANT.equals(loginUser.getClientType());
    }

    public static Long getTenantId()
    {
        return requireMerchantContext().getTenantId();
    }

    public static Long getMemberId()
    {
        return requireMerchantContext().getMemberId();
    }

    /**
     * 获取当前商户主体认证状态，供商户服务统一判断是否已经开放业务功能。
     *
     * @return 商户认证状态
     */
    public static String getMerchantAuthStatus()
    {
        return requireMerchantContext().getMerchantAuthStatus();
    }

    /**
     * 判断当前商户是否已通过平台认证，未通过时只能使用默认只读预览功能。
     *
     * @return 认证通过返回 true，否则返回 false
     */
    public static boolean isMerchantApproved()
    {
        return "verified".equals(getMerchantAuthStatus());
    }

    /**
     * 强制要求当前商户已经通过平台认证，用于保护新增、修改、删除、导出和授权等业务操作。
     *
     * @throws ServiceException 当前商户尚未通过认证时抛出
     */
    public static void requireMerchantApproved()
    {
        if (!isMerchantApproved())
        {
            throw new ServiceException("商户资料尚未审核通过，当前功能暂不可用");
        }
    }

    public static String getMemberRole()
    {
        return requireMerchantContext().getMemberRole();
    }

    public static String getWarehouseScopeType()
    {
        return requireMerchantContext().getWarehouseScopeType();
    }

    public static Set<Long> getWarehouseIds()
    {
        Set<Long> warehouseIds = requireMerchantContext().getWarehouseIds();
        if (warehouseIds == null || warehouseIds.isEmpty())
        {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new LinkedHashSet<Long>(warehouseIds));
    }

    public static boolean hasAllWarehouseAccess()
    {
        return Constants.WAREHOUSE_SCOPE_ALL.equals(getWarehouseScopeType());
    }

    public static boolean hasWarehouseAccess(Long warehouseId)
    {
        if (warehouseId == null)
        {
            return false;
        }
        LoginUser loginUser = requireMerchantContext();
        return Constants.WAREHOUSE_SCOPE_ALL.equals(loginUser.getWarehouseScopeType())
                || loginUser.getWarehouseIds() != null && loginUser.getWarehouseIds().contains(warehouseId);
    }

    public static void checkWarehouseAccess(Long warehouseId)
    {
        if (!hasWarehouseAccess(warehouseId))
        {
            throw new ServiceException("无权访问该仓库");
        }
    }

    private static LoginUser requireMerchantContext()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNull(loginUser)
                || !Constants.CLIENT_TYPE_MERCHANT.equals(loginUser.getClientType()))
        {
            throw new ServiceException("当前请求不是商户用户上下文");
        }
        if (loginUser.getTenantId() == null || loginUser.getMemberId() == null
                || StringUtils.isEmpty(loginUser.getWarehouseScopeType()))
        {
            throw new ServiceException("商户登录上下文不完整，请重新登录");
        }
        return loginUser;
    }
}
