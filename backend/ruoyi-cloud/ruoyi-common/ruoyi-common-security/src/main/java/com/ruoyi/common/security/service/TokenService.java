package com.ruoyi.common.security.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.CacheConstants;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.utils.JwtUtils;
import com.ruoyi.common.core.utils.ServletUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.ip.IpUtils;
import com.ruoyi.common.core.utils.uuid.IdUtils;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.model.LoginUser;

/**
 * token验证处理
 * 
 * @author ruoyi
 */
@Component
public class TokenService
{
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    private RedisService redisService;

    protected static final long MILLIS_SECOND = 1000;

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private final static long TOKEN_EXPIRE_TIME = CacheConstants.EXPIRATION;

    private final static Long TOKEN_REFRESH_THRESHOLD_MINUTES = CacheConstants.REFRESH_TIME * MILLIS_MINUTE;

    /**
     * 创建令牌
     */
    public Map<String, Object> createToken(LoginUser loginUser)
    {
        String token = IdUtils.fastUUID();
        Long userId = loginUser.getSysUser().getUserId();
        String userName = loginUser.getSysUser().getUserName();
        loginUser.setToken(token);
        loginUser.setUserid(userId);
        loginUser.setUsername(userName);
        loginUser.setIpaddr(IpUtils.getIpAddr());
        refreshToken(loginUser);
        bindUserToken(loginUser);

        // Jwt存储信息
        Map<String, Object> claimsMap = new HashMap<String, Object>();
        claimsMap.put(SecurityConstants.USER_KEY, token);
        claimsMap.put(SecurityConstants.DETAILS_USER_ID, userId);
        claimsMap.put(SecurityConstants.DETAILS_USERNAME, userName);
        claimsMap.put(SecurityConstants.LOGIN_CLIENT_TYPE, loginUser.getClientType());

        // 接口返回信息
        Map<String, Object> rspMap = new HashMap<String, Object>();
        rspMap.put("access_token", JwtUtils.createToken(claimsMap));
        rspMap.put("expires_in", TOKEN_EXPIRE_TIME);
        return rspMap;
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser()
    {
        return getLoginUser(ServletUtils.getRequest());
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser(HttpServletRequest request)
    {
        // 获取请求携带的令牌
        String token = SecurityUtils.getToken(request);
        return getLoginUser(token);
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser(String token)
    {
        LoginUser user = null;
        try
        {
            if (StringUtils.isNotEmpty(token))
            {
                String userkey = JwtUtils.getUserKey(token);
                String clientType = JwtUtils.getClientType(token);
                user = redisService.getCacheObject(getTokenKey(clientType, userkey));
                return user;
            }
        }
        catch (Exception e)
        {
            log.error("获取用户信息异常'{}'", e.getMessage());
        }
        return user;
    }

    /**
     * 设置用户身份信息
     */
    public void setLoginUser(LoginUser loginUser)
    {
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken()))
        {
            refreshToken(loginUser);
        }
    }

    /**
     * 删除用户缓存信息
     */
    public void delLoginUser(String token)
    {
        if (StringUtils.isNotEmpty(token))
        {
            String userkey = JwtUtils.getUserKey(token);
            String clientType = JwtUtils.getClientType(token);
            if (!StringUtils.equalsAny(clientType, Constants.CLIENT_TYPE_PLATFORM, Constants.CLIENT_TYPE_MERCHANT))
            {
                // 兼容本次升级前的令牌注销；旧令牌不能再访问业务接口。
                redisService.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + userkey);
                return;
            }
            String tokenKey = getTokenKey(clientType, userkey);
            LoginUser loginUser = redisService.getCacheObject(tokenKey);
            redisService.deleteObject(tokenKey);
            if (StringUtils.isNotNull(loginUser))
            {
                redisService.removeCacheSetValue(getUserTokenKey(loginUser.getClientType(), loginUser.getUserid()),
                        userkey);
            }
        }
    }

    /**
     * 失效指定用户全部登录会话，权限范围变化后必须重新登录。
     */
    public void delLoginUsers(String clientType, Long userId)
    {
        if (StringUtils.isEmpty(clientType) || userId == null)
        {
            return;
        }
        String indexKey = getUserTokenKey(clientType, userId);
        Set<String> tokens = redisService.getCacheSet(indexKey);
        if (tokens != null && !tokens.isEmpty())
        {
            for (String token : tokens)
            {
                redisService.deleteObject(getTokenKey(clientType, token));
            }
        }
        redisService.deleteObject(indexKey);
    }

    /**
     * 验证令牌有效期，相差不足120分钟，自动刷新缓存
     *
     * @param loginUser
     */
    public void verifyToken(LoginUser loginUser)
    {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= TOKEN_REFRESH_THRESHOLD_MINUTES)
        {
            refreshToken(loginUser);
        }
    }

    /**
     * 刷新令牌有效期
     *
     * @param loginUser 登录信息
     */
    public void refreshToken(LoginUser loginUser)
    {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + TOKEN_EXPIRE_TIME * MILLIS_MINUTE);
        // 根据uuid将loginUser缓存
        String userKey = getTokenKey(loginUser.getClientType(), loginUser.getToken());
        redisService.setCacheObject(userKey, loginUser, TOKEN_EXPIRE_TIME, TimeUnit.MINUTES);
        if (StringUtils.isNotEmpty(loginUser.getClientType()) && loginUser.getUserid() != null)
        {
            redisService.expire(getUserTokenKey(loginUser.getClientType(), loginUser.getUserid()),
                    TOKEN_EXPIRE_TIME, TimeUnit.MINUTES);
        }
    }

    private void bindUserToken(LoginUser loginUser)
    {
        String indexKey = getUserTokenKey(loginUser.getClientType(), loginUser.getUserid());
        redisService.setCacheSet(indexKey, Collections.singleton(loginUser.getToken()));
        redisService.expire(indexKey, TOKEN_EXPIRE_TIME, TimeUnit.MINUTES);
    }

    private String getTokenKey(String clientType, String token)
    {
        return CacheConstants.loginTokenKey(clientType, token);
    }

    private String getUserTokenKey(String clientType, Long userId)
    {
        return CacheConstants.LOGIN_USER_TOKEN_KEY + clientType + ":" + userId;
    }
}
