package com.ruoyi.common.core.constant;

/**
 * 缓存常量信息
 * 
 * @author ruoyi
 */
public class CacheConstants
{
    /**
     * 登录会话缓存有效期，默认10080分钟（7天）。平台端和商户端共用该会话周期。
     */
    public final static long EXPIRATION = 10080;

    /**
     * 缓存刷新时间，默认120（分钟）
     */
    public final static long REFRESH_TIME = 120;

    /**
     * 密码最大错误次数
     */
    public final static int PASSWORD_MAX_RETRY_COUNT = 5;

    /**
     * 密码锁定时间，默认10（分钟）
     */
    public final static long PASSWORD_LOCK_TIME = 10;

    /**
     * 权限缓存前缀
     */
    public final static String LOGIN_TOKEN_KEY = "login_tokens:";

    /** 平台登录令牌缓存前缀 */
    public final static String PLATFORM_LOGIN_TOKEN_KEY = LOGIN_TOKEN_KEY + "platform:";

    /** 商户登录令牌缓存前缀 */
    public final static String MERCHANT_LOGIN_TOKEN_KEY = LOGIN_TOKEN_KEY + "merchant:";

    /** 用户登录令牌索引，用于权限变化后失效指定用户的全部会话 */
    public final static String LOGIN_USER_TOKEN_KEY = "login_user_tokens:";

    public static String loginTokenKey(String clientType, String token)
    {
        if (Constants.CLIENT_TYPE_PLATFORM.equals(clientType))
        {
            return PLATFORM_LOGIN_TOKEN_KEY + token;
        }
        if (Constants.CLIENT_TYPE_MERCHANT.equals(clientType))
        {
            return MERCHANT_LOGIN_TOKEN_KEY + token;
        }
        throw new IllegalArgumentException("Unsupported login client type: " + clientType);
    }

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 全局配置缓存前缀，平台端和商户业务服务共用系统配置缓存命名空间。
     */
    public static final String GLOBAL_CONFIG_CACHE_KEY = SYS_CONFIG_KEY;

    /**
     * 全局配置缓存默认有效期，统一使用七天，配置更新时重新写入会自动刷新有效期。
     */
    public static final long GLOBAL_CONFIG_CACHE_TTL_DAYS = 7L;

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    /**
     * 登录IP黑名单 cache key
     */
    public static final String SYS_LOGIN_BLACKIPLIST = SYS_CONFIG_KEY + "sys.login.blackIPList";
}
