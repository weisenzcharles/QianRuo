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
