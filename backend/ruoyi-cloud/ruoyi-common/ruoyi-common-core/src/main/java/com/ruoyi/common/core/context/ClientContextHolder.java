package com.ruoyi.common.core.context;

import jakarta.servlet.http.HttpServletRequest;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.utils.ServletUtils;
import com.ruoyi.common.core.utils.StringUtils;

/**
 * 前端客户端上下文
 *
 * @author qianruo
 */
public class ClientContextHolder
{
    /**
     * 获取当前请求客户端类型。缺失或未知值由调用方严格拒绝。
     */
    public static String getClientType()
    {
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null)
        {
            return "";
        }
        String clientType = ServletUtils.getHeader(request, Constants.CLIENT_TYPE_HEADER);
        if (StringUtils.isEmpty(clientType))
        {
            return "";
        }
        return clientType;
    }

    public static boolean isPlatformClient()
    {
        return Constants.CLIENT_TYPE_PLATFORM.equals(getClientType());
    }

    public static boolean isMerchantClient()
    {
        return Constants.CLIENT_TYPE_MERCHANT.equals(getClientType());
    }
}
