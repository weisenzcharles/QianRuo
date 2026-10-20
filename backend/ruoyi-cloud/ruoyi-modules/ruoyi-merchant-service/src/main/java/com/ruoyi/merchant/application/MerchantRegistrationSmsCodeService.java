package com.ruoyi.merchant.application;

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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import com.ruoyi.common.aliyun.sms.AliyunSmsException;
import com.ruoyi.common.aliyun.sms.AliyunSmsService;
import com.ruoyi.common.aliyun.sms.AliyunSmsServiceFactory;
import com.ruoyi.common.core.constant.GlobalConfigConstants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.ip.IpUtils;
import com.ruoyi.system.api.RemoteGlobalConfigService;

/** 商户公开注册短信验证码的发送、频控和一次性核销。 */
@Service
public class MerchantRegistrationSmsCodeService
{
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_TTL_SECONDS = 300;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int MAX_DAILY_PHONE_SENDS = 10;
    private static final int MAX_DAILY_IP_SENDS = 50;
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final String KEY_PREFIX = "merchant:registration:sms:";
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DAILY_KEY_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DefaultRedisScript<Long> VERIFY_CODE_SCRIPT = createVerifyCodeScript();

    private final StringRedisTemplate redisTemplate;
    private final RemoteGlobalConfigService globalConfigService;

    public MerchantRegistrationSmsCodeService(StringRedisTemplate redisTemplate,
            RemoteGlobalConfigService globalConfigService)
    {
        this.redisTemplate = redisTemplate;
        this.globalConfigService = globalConfigService;
    }

    /** 发送注册验证码，供应商配置和频控异常会直接返回业务错误。 */
    public void sendCode(String rawPhone)
    {
        String phone = normalizePhone(rawPhone);
        reserveSend(phone, IpUtils.getIpAddr());
        String phoneHash = sha256(phone);
        String codeKey = codeKey(phoneHash);
        String attemptsKey = attemptsKey(phoneHash);
        try
        {
            SmsConfiguration configuration = loadSmsConfiguration();
            String code = String.format(Locale.ROOT, "%04d", RANDOM.nextInt(10_000));
            redisTemplate.opsForValue().set(codeKey, code, CODE_TTL_SECONDS, TimeUnit.SECONDS);
            redisTemplate.delete(attemptsKey);
            AliyunSmsService smsService = AliyunSmsServiceFactory.create(configuration.endpoint(),
                    configuration.accessKeyId(), configuration.accessKeySecret());
            smsService.sendTemplateSms(phone, configuration.signName(), configuration.templateCode(),
                    Map.of("code", code));
        }
        catch (AliyunSmsException exception)
        {
            clearCode(phone, codeKey, attemptsKey);
            throw new ServiceException("短信验证码发送失败，请稍后重试");
        }
        catch (RuntimeException exception)
        {
            clearCode(phone, codeKey, attemptsKey);
            throw exception;
        }
    }

    /** 原子校验并消费验证码，避免同一验证码并发重复使用。 */
    public void verifyAndConsume(String rawPhone, String code)
    {
        String phone = normalizePhone(rawPhone);
        if (code == null || !code.matches("\\d{4}"))
        {
            throw new ServiceException("请输入四位验证码");
        }
        Long result = redisTemplate.execute(VERIFY_CODE_SCRIPT,
                List.of(codeKey(sha256(phone)), attemptsKey(sha256(phone))), code,
                String.valueOf(CODE_TTL_SECONDS), String.valueOf(MAX_VERIFY_ATTEMPTS));
        validateVerificationResult(result);
    }

    static String normalizePhone(String rawPhone)
    {
        String phone = rawPhone == null ? "" : rawPhone.trim();
        if (!PHONE_PATTERN.matcher(phone).matches())
        {
            throw new ServiceException("手机号格式不正确");
        }
        return phone;
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

    private void reserveSend(String phone, String ipAddress)
    {
        String phoneHash = sha256(phone);
        String cooldownKey = cooldownKey(phoneHash);
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(cooldownKey, "1",
                RESEND_COOLDOWN_SECONDS, TimeUnit.SECONDS);
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

    private void enforceDailyLimit(String key, int maximum, String message)
    {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null)
        {
            throw new ServiceException("短信发送频控暂不可用，请稍后重试");
        }
        if (count == 1L && !Boolean.TRUE.equals(redisTemplate.expire(key, 2, TimeUnit.DAYS)))
        {
            redisTemplate.delete(key);
            throw new ServiceException("短信发送频控暂不可用，请稍后重试");
        }
        if (count > maximum)
        {
            throw new ServiceException(message);
        }
    }

    private SmsConfiguration loadSmsConfiguration()
    {
        if (!"true".equalsIgnoreCase(getGlobalConfig(GlobalConfigConstants.ALIYUN_SMS_ENABLED)))
        {
            throw new ServiceException("商户注册短信验证码暂未启用");
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

    private void clearCode(String phone, String codeKey, String attemptsKey)
    {
        redisTemplate.delete(List.of(codeKey, attemptsKey, cooldownKey(sha256(phone))));
    }

    private static String codeKey(String phoneHash) { return KEY_PREFIX + "code:" + phoneHash; }

    private static String attemptsKey(String phoneHash) { return KEY_PREFIX + "attempts:" + phoneHash; }

    private static String cooldownKey(String phoneHash) { return KEY_PREFIX + "cooldown:" + phoneHash; }

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

    private static boolean isBlank(String value) { return value == null || value.isBlank(); }

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
