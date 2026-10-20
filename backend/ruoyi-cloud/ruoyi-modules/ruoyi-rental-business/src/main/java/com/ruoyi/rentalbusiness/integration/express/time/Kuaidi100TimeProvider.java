package com.ruoyi.rentalbusiness.integration.express.time;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.GlobalConfigConstants;
import com.ruoyi.rentalbusiness.config.GlobalConfigService;
import com.ruoyi.rentalbusiness.enums.ExpressTimeProviderCode;
import com.ruoyi.rentalbusiness.integration.express.ExpressHttpSupport;
import com.ruoyi.rentalbusiness.model.ExpressTimeProviderRequest;
import com.ruoyi.rentalbusiness.model.ExpressTimeProviderResult;

/** 快递100智能时效预估客户端，与快递100轨迹查询客户端分开维护。 */
@Service
public class Kuaidi100TimeProvider implements ExpressTimeProvider
{
    private static final Logger log = LoggerFactory.getLogger(Kuaidi100TimeProvider.class);

    private static final DateTimeFormatter ORDER_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final GlobalConfigService config;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    /** 注入平台全局配置和 JSON 客户端。 */
    public Kuaidi100TimeProvider(GlobalConfigService config, ObjectMapper objectMapper)
    {
        this.config = config;
        this.objectMapper = objectMapper;
        this.httpClient = ExpressHttpSupport.client();
    }

    @Override
    public ExpressTimeProviderCode provider()
    {
        return ExpressTimeProviderCode.KUAIDI100;
    }

    /** 调用快递100时效预估接口并转换为供应商统一响应。 */
    @Override
    public ExpressTimeProviderResult estimate(ExpressTimeProviderRequest request)
    {
        ExpressTimeProviderRequest normalized = request.normalized();
        String key;
        String secret;
        try
        {
            key = config.get(GlobalConfigConstants.EXPRESS_KUAIDI100_TIME_KEY);
            secret = config.get(GlobalConfigConstants.EXPRESS_KUAIDI100_TIME_SECRET);
        }
        catch (RuntimeException exception)
        {
            log.warn("读取快递100时效预估配置失败，message={}", exception.getMessage());
            return ExpressTimeProviderResult.unavailable("快递100时效预估配置暂不可用");
        }
        if (key == null || key.isBlank() || secret == null || secret.isBlank())
        {
            return ExpressTimeProviderResult.unavailable("未配置快递100时效预估接口凭证");
        }

        try
        {
            String param = buildParam(objectMapper, normalized);
            long timestamp = System.currentTimeMillis();
            Map<String, String> form = buildForm(key, secret, param, timestamp);
            String endpoint = config.get(GlobalConfigConstants.EXPRESS_KUAIDI100_TIME_URL,
                    "https://api.kuaidi100.com/label/order");
            var response = ExpressHttpSupport.postForm(httpClient, endpoint, form);
            if (!ExpressHttpSupport.success(response))
            {
                return ExpressTimeProviderResult.unavailable("快递100时效预估服务暂不可用");
            }
            return parseResponse(objectMapper, response.body());
        }
        catch (Exception exception)
        {
            log.warn("调用快递100时效预估失败，message={}", exception.getMessage());
            return ExpressTimeProviderResult.unavailable("快递100时效预估失败");
        }
    }

    /** 按快递100接口约定构造 param JSON。 */
    static String buildParam(ObjectMapper objectMapper, ExpressTimeProviderRequest request) throws Exception
    {
        ExpressTimeProviderRequest normalized = request.normalized();
        Map<String, String> param = new LinkedHashMap<>();
        param.put("from", normalized.fromAddress());
        param.put("to", normalized.toAddress());
        param.put("orderTime", ORDER_TIME_FORMAT.format(normalized.orderTime()));
        param.put("kuaidicom", normalized.providerCompanyCode());
        if (normalized.expressType() != null && !normalized.expressType().isBlank())
        {
            param.put("expType", normalized.expressType());
        }
        return objectMapper.writeValueAsString(param);
    }

    /** 按 param + t + key + secret 生成快递100要求的大写 MD5 签名。 */
    static Map<String, String> buildForm(String key, String secret, String param, long timestamp)
    {
        Map<String, String> form = new LinkedHashMap<>();
        String timestampText = String.valueOf(timestamp);
        form.put("method", "time");
        form.put("key", key);
        form.put("sign", ExpressHttpSupport.md5(param + timestampText + key + secret).toUpperCase(Locale.ROOT));
        form.put("t", timestampText);
        form.put("param", param);
        return form;
    }

    /** 解析快递100时效预估响应，仅提取后续入库需要的字段。 */
    static ExpressTimeProviderResult parseResponse(ObjectMapper objectMapper, String body) throws Exception
    {
        JsonNode root = objectMapper.readTree(body);
        if (!isSuccess(root))
        {
            return ExpressTimeProviderResult.unavailable(messageOf(root, "快递100时效预估失败"));
        }
        JsonNode data = root.path("data");
        if (!data.isObject())
        {
            return ExpressTimeProviderResult.unavailable("快递100返回的时效数据为空");
        }

        String providerCompanyCode = text(data, "kuaidicom");
        String fromName = text(data, "fromName");
        String toName = text(data, "toName");
        if (isBlank(providerCompanyCode) || isBlank(fromName) || isBlank(toName))
        {
            return ExpressTimeProviderResult.unavailable("快递100返回的线路信息不完整");
        }

        BigDecimal deliveryExpendTime = decimal(data, "deliveryExpendTime");
        BigDecimal totalTime = decimal(data, "totalTime");
        BigDecimal remainTime = decimal(data, "remainTime");
        if (deliveryExpendTime == null && totalTime == null && remainTime == null)
        {
            return ExpressTimeProviderResult.unavailable("快递100返回的时效数据不完整");
        }
        return new ExpressTimeProviderResult(true, providerCompanyCode, fromName, toName,
                text(data, "expType"), deliveryExpendTime, totalTime, remainTime, null);
    }

    private static boolean isSuccess(JsonNode root)
    {
        if (root == null || !root.isObject())
        {
            return false;
        }
        JsonNode success = root.get("success");
        if (success != null && (success.asBoolean(false) || "true".equalsIgnoreCase(success.asText())))
        {
            return true;
        }
        return "200".equals(root.path("code").asText()) && root.path("data").isObject();
    }

    private static BigDecimal decimal(JsonNode parent, String fieldName)
    {
        String value = text(parent, fieldName);
        if (isBlank(value))
        {
            return null;
        }
        try
        {
            BigDecimal result = new BigDecimal(value);
            if (result.signum() < 0)
            {
                throw new IllegalArgumentException(fieldName + "不能为负数");
            }
            return result;
        }
        catch (NumberFormatException exception)
        {
            throw new IllegalArgumentException("快递100返回的" + fieldName + "不是有效数字", exception);
        }
    }

    private static String text(JsonNode parent, String fieldName)
    {
        String value = parent == null ? null : parent.path(fieldName).asText(null);
        return value == null ? null : value.trim();
    }

    private static String messageOf(JsonNode root, String fallback)
    {
        String message = text(root, "message");
        return isBlank(message) ? fallback : message;
    }

    private static boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }
}
