package com.ruoyi.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import com.ruoyi.common.aliyun.sms.AliyunSmsException;
import com.ruoyi.common.aliyun.sms.AliyunSmsService;
import com.ruoyi.common.aliyun.sms.AliyunSmsServiceFactory;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.GlobalConfigConstants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.SoftDeleteStatus;
import com.ruoyi.common.core.enums.UserStatus;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.ip.IpUtils;
import com.ruoyi.merchant.api.RemoteMerchantUserService;
import com.ruoyi.merchant.api.model.MerchantLoginIdentity;
import com.ruoyi.system.api.RemoteGlobalConfigService;

/** 商户登录短信验证码的发送、频控与一次性校验。 */
@Service
public class MerchantSmsLoginCodeService
{
    private static final Logger log = LoggerFactory.getLogger(MerchantSmsLoginCodeService.class);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_TTL_SECONDS = 300;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int MAX_DAILY_PHONE_SENDS = 10;
    private static final int MAX_DAILY_IP_SENDS = 50;
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final String KEY_PREFIX = "auth:sms-login:";
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DAILY_KEY_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DefaultRedisScript<Long> VERIFY_CODE_SCRIPT = createVerifyCodeScript();

    private final StringRedisTemplate redisTemplate;
    private final RemoteGlobalConfigService globalConfigService;
    private final RemoteMerchantUserService merchantUserService;

    /**
     * 注入短信验证码所需的存储和内部身份服务。
     *
     * @param redisTemplate Redis 字符串模板
     * @param globalConfigService 平台全局配置内部接口
     * @param merchantUserService 商户账号内部接口
     */
    public MerchantSmsLoginCodeService(StringRedisTemplate redisTemplate,
            RemoteGlobalConfigService globalConfigService, RemoteMerchantUserService merchantUserService)
    {
        this.redisTemplate = redisTemplate;
        this.globalConfigService = globalConfigService;
        this.merchantUserService = merchantUserService;
    }

    /**
     * 向已启用的商户账号发送登录验证码。响应不区分手机号是否注册，避免账号枚举。
     *
     * @param rawPhone 商户手机号
     */
    public void sendLoginCode(String rawPhone)
    {
        String phone = normalizePhone(rawPhone);
        reserveSend(phone, IpUtils.getIpAddr());
        SmsConfiguration configuration;
        try
        {
            configuration = loadSmsConfiguration();
        }
        catch (RuntimeException exception)
        {
            redisTemplate.delete(cooldownKey(phone));
            throw exception;
        }

        if (!isEligibleMerchantPhone(phone))
        {
            return;
        }

        String code = String.format(Locale.ROOT, "%04d", RANDOM.nextInt(10_000));
        String phoneHash = sha256(phone);
        String codeKey = codeKey(phoneHash);
        String attemptsKey = attemptsKey(phoneHash);
        redisTemplate.opsForValue().set(codeKey, code, CODE_TTL_SECONDS, TimeUnit.SECONDS);
        redisTemplate.delete(attemptsKey);

        try
        {
            AliyunSmsService smsService = AliyunSmsServiceFactory.create(configuration.endpoint(),
                    configuration.accessKeyId(), configuration.accessKeySecret());
            smsService.sendTemplateSms(phone, configuration.signName(), configuration.templateCode(),
                    Map.of("code", code));
        }
        catch (AliyunSmsException exception)
        {
            redisTemplate.delete(List.of(codeKey, attemptsKey));
            log.warn("商户登录验证码短信发送失败，供应商错误类型：{}", exception.getClass().getSimpleName());
        }
    }

    /**
     * 原子校验并消费验证码，避免同一验证码被并发重复使用。
     *
     * @param rawPhone 商户手机号
     * @param code 用户提交的验证码
     */
    public void verifyAndConsume(String rawPhone, String code)
    {
        String phone = normalizePhone(rawPhone);
        if (code == null || !code.matches("\\d{4}"))
        {
            throw new ServiceException("请输入四位验证码");
        }

        String phoneHash = sha256(phone);
        Long result = redisTemplate.execute(VERIFY_CODE_SCRIPT,
                List.of(codeKey(phoneHash), attemptsKey(phoneHash)), code, String.valueOf(CODE_TTL_SECONDS),
                String.valueOf(MAX_VERIFY_ATTEMPTS));
        validateVerificationResult(result);
    }

    static String normalizePhone(String rawPhone)
    {
        String phone = rawPhone == null ? "" : rawPhone.trim();
        if (!PHONE_PATTERN.matcher(phone).matches())
        {
            throw new ServiceException("请输入正确的手机号");
        }
        return phone;
    }

    static String maskPhone(String rawPhone)
    {
        String phone = rawPhone == null ? "" : rawPhone.trim();
        return PHONE_PATTERN.matcher(phone).matches()
                ? phone.substring(0, 3) + "****" + phone.substring(7)
                : "未知手机号";
    }

    static void validateVerificationResult(Long result)
    {
        if (result == null || result == -1L)
        {
            throw new ServiceException("验证码已过期或尚未发送");
        }
        if (result == -2L)
        {
            throw new ServiceException("验证码错误次数过多，请重新获取");
        }
        if (result == 0L)
        {
            throw new ServiceException("验证码错误");
        }
        if (result != 1L)
        {
            throw new ServiceException("验证码校验失败，请重新获取");
        }
    }

    private SmsConfiguration loadSmsConfiguration()
    {
        if (!"true".equalsIgnoreCase(getGlobalConfig(GlobalConfigConstants.ALIYUN_SMS_ENABLED)))
        {
            throw new ServiceException("商户短信验证码登录暂未启用");
        }

        String endpoint = getGlobalConfig(GlobalConfigConstants.ALIYUN_SMS_ENDPOINT);
        String signName = getGlobalConfig(GlobalConfigConstants.ALIYUN_SMS_SIGN_NAME);
        String templateCode = getGlobalConfig(GlobalConfigConstants.ALIYUN_SMS_LOGIN_TEMPLATE_CODE);
        String accessKeyId = getGlobalConfig(GlobalConfigConstants.ALIYUN_SMS_ACCESS_KEY_ID);
        String accessKeySecret = getGlobalConfig(GlobalConfigConstants.ALIYUN_SMS_ACCESS_KEY_SECRET);
        if (isBlank(endpoint) || endpoint.contains("://") || endpoint.contains("/") || isBlank(signName)
                || isBlank(templateCode) || isBlank(accessKeyId) || isBlank(accessKeySecret))
        {
            throw new ServiceException("平台短信配置不完整，请联系平台管理员");
        }
        return new SmsConfiguration(endpoint.trim(), signName.trim(), templateCode.trim(), accessKeyId.trim(),
                accessKeySecret.trim());
    }

    private String getGlobalConfig(String configKey)
    {
        R<String> result = globalConfigService.getValue(configKey, SecurityConstants.INNER);
        if (result == null || result.getCode() != R.SUCCESS)
        {
            throw new ServiceException("平台短信配置读取失败，请稍后重试");
        }
        return result.getData();
    }

    private void reserveSend(String phone, String ipAddress)
    {
        String phoneHash = sha256(phone);
        String cooldownKey = cooldownKey(phone);
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(cooldownKey, "1", RESEND_COOLDOWN_SECONDS,
                TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(acquired))
        {
            throw new ServiceException("请稍后再获取验证码");
        }

        String date = LocalDate.now(BUSINESS_ZONE).format(DAILY_KEY_DATE);
        try
        {
            enforceDailyLimit(KEY_PREFIX + "phone-day:" + date + ":" + phoneHash, MAX_DAILY_PHONE_SENDS,
                    "该手机号今日获取验证码次数已达上限");
            enforceDailyLimit(KEY_PREFIX + "ip-day:" + date + ":" + sha256(ipAddress), MAX_DAILY_IP_SENDS,
                    "当前网络今日获取验证码次数已达上限");
        }
        catch (RuntimeException exception)
        {
            redisTemplate.delete(cooldownKey);
            throw exception;
        }
    }

    private void enforceDailyLimit(String key, int maximum, String errorMessage)
    {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null)
        {
            throw new ServiceException("短信发送频控暂不可用，请稍后重试");
        }
        if (count == 1L)
        {
            if (!Boolean.TRUE.equals(redisTemplate.expire(key, 2, TimeUnit.DAYS)))
            {
                redisTemplate.delete(key);
                throw new ServiceException("短信发送频控暂不可用，请稍后重试");
            }
        }
        if (count > maximum)
        {
            throw new ServiceException(errorMessage);
        }
    }

    private boolean isEligibleMerchantPhone(String phone)
    {
        R<MerchantLoginIdentity> result = merchantUserService.getByPhone(phone, SecurityConstants.INNER,
                Constants.CLIENT_TYPE_MERCHANT);
        if (result == null || result.getCode() != R.SUCCESS || result.getData() == null)
        {
            return false;
        }
        MerchantLoginIdentity identity = result.getData();
        return !SoftDeleteStatus.DELETED.getCode().equals(identity.getIsDelete())
                && !UserStatus.DISABLE.getCode().equals(identity.getStatus());
    }

    private static String codeKey(String phoneHash)
    {
        return KEY_PREFIX + "code:" + phoneHash;
    }

    private static String attemptsKey(String phoneHash)
    {
        return KEY_PREFIX + "attempts:" + phoneHash;
    }

    private static String cooldownKey(String phone)
    {
        return KEY_PREFIX + "cooldown:" + sha256(phone);
    }

    private static String sha256(String value)
    {
        try
        {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }

    private static boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }

    private static DefaultRedisScript<Long> createVerifyCodeScript()
    {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText("local current = redis.call('GET', KEYS[1]) "
                + "if not current then return -1 end "
                + "if current == ARGV[1] then redis.call('DEL', KEYS[1], KEYS[2]); return 1 end "
                + "local attempts = redis.call('INCR', KEYS[2]) "
                + "if attempts == 1 then redis.call('EXPIRE', KEYS[2], tonumber(ARGV[2])) end "
                + "if attempts >= tonumber(ARGV[3]) then redis.call('DEL', KEYS[1], KEYS[2]); return -2 end "
                + "return 0");
        return script;
    }

    private record SmsConfiguration(String endpoint, String signName, String templateCode, String accessKeyId,
            String accessKeySecret)
    {
    }
}
