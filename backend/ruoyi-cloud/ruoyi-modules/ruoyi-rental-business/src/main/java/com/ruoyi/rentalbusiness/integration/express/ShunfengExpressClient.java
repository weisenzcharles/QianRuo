package com.ruoyi.rentalbusiness.integration.express;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.GlobalConfigConstants;
import com.ruoyi.rentalbusiness.config.GlobalConfigService;

/** 顺丰标准轨迹查询客户端。 */
@Service
public class ShunfengExpressClient implements ExpressTrackingClient
{
    private static final Logger log = LoggerFactory.getLogger(ShunfengExpressClient.class);
    private static final String METHOD = "EXP_RECE_SEARCH_ROUTES";
    private final GlobalConfigService config;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = ExpressHttpSupport.client();

    public ShunfengExpressClient(GlobalConfigService config, ObjectMapper objectMapper)
    {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Override
    public ExpressProvider provider()
    {
        return ExpressProvider.SF;
    }

    @Override
    public ExpressTrackResult query(ExpressTrackQuery input)
    {
        ExpressTrackQuery query = input.normalized();
        String partnerId = config.get(GlobalConfigConstants.EXPRESS_SF_PARTNER_ID);
        String partnerKey = config.get(GlobalConfigConstants.EXPRESS_SF_PARTNER_KEY);
        if (partnerId.isBlank() || partnerKey.isBlank())
        {
            return ExpressTrackResult.unavailable(provider(), query.trackingNo(), "未配置顺丰接口凭证");
        }
        try
        {
            String msgData = buildMessage(objectMapper, query);
            Map<String, String> form = buildForm(partnerId, partnerKey, msgData, System.currentTimeMillis());
            String endpoint = config.get(GlobalConfigConstants.EXPRESS_SF_TRACKING_URL,
                    "https://sfapi.sf-express.com/std/service");
            HttpResponse<String> response = ExpressHttpSupport.postForm(httpClient, endpoint, form);
            if (!ExpressHttpSupport.success(response))
            {
                log.warn("顺丰轨迹服务返回 HTTP 错误，trackingNo={}, statusCode={}",
                        maskTrackingNo(query.trackingNo()), response.statusCode());
                return ExpressTrackResult.unavailable(provider(), query.trackingNo(), "顺丰轨迹服务暂不可用");
            }
            return parse(query.trackingNo(), response.body());
        }
        catch (Exception exception)
        {
            log.warn("查询顺丰快递轨迹失败，trackingNo={}, message={}", maskTrackingNo(query.trackingNo()),
                    exception.getMessage());
            return ExpressTrackResult.unavailable(provider(), query.trackingNo(), "顺丰轨迹查询失败");
        }
    }

    ExpressTrackResult parse(String trackingNo, String body) throws Exception
    {
        JsonNode root = objectMapper.readTree(body);
        String apiResultCode = root.path("apiResultCode").asText();
        if (!"A1000".equals(apiResultCode))
        {
            return ExpressTrackResult.unavailable(provider(), trackingNo,
                    firstNonBlank(root.path("apiErrorMsg").asText(null),
                            root.path("apiResultMsg").asText(null), "顺丰返回查询失败"));
        }

        JsonNode resultData = parseNestedJson(root.path("apiResultData"));
        if (!resultData.isObject())
        {
            return ExpressTrackResult.unavailable(provider(), trackingNo, "顺丰返回数据格式异常");
        }
        if (!resultData.path("success").asBoolean(false))
        {
            return ExpressTrackResult.unavailable(provider(), trackingNo,
                    firstNonBlank(resultData.path("errorMsg").asText(null),
                            resultData.path("errorCode").asText(null), "顺丰业务查询失败"));
        }

        JsonNode messageData = parseNestedJson(resultData.path("msgData"));
        JsonNode routeList = messageData.path("routeResps");
        if (!routeList.isArray())
        {
            return ExpressTrackResult.unavailable(provider(), trackingNo, "顺丰返回物流轨迹格式异常");
        }
        List<ExpressTrackNode> nodes = new ArrayList<>();
        if (routeList.size() > 0)
        {
            for (JsonNode item : routeList.get(0).path("routes"))
            {
                OffsetDateTime time = parseTime(item.path("acceptTime").asText(null));
                String operationCode = firstNonBlank(item.path("opCode").asText(null),
                        item.path("opcode").asText(null));
                nodes.add(new ExpressTrackNode(time, operationCode,
                        item.path("remark").asText(""), item.path("acceptAddress").asText(null)));
            }
        }
        return new ExpressTrackResult(true, provider(), trackingNo, "success", nodes, null);
    }

    static String buildMessage(ObjectMapper objectMapper, ExpressTrackQuery query) throws Exception
    {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("language", "0");
        message.put("trackingType", 1);
        message.put("methodType", 1);
        message.put("trackingNumber", List.of(query.trackingNo()));
        String checkPhoneNo = normalizeCheckPhoneNo(query.receiverCode());
        if (!checkPhoneNo.isBlank())
        {
            message.put("checkPhoneNo", checkPhoneNo);
        }
        return objectMapper.writeValueAsString(message);
    }

    static Map<String, String> buildForm(String partnerId, String partnerKey, String msgData,
            long timestampMillis) throws Exception
    {
        String timestamp = String.valueOf(timestampMillis);
        String digest = java.util.Base64.getEncoder().encodeToString(
                java.security.MessageDigest.getInstance("MD5")
                        .digest((msgData + timestamp + partnerKey).getBytes(StandardCharsets.UTF_8)));
        Map<String, String> form = new LinkedHashMap<>();
        form.put("partnerID", partnerId);
        form.put("requestID", UUID.randomUUID().toString().replace("-", ""));
        form.put("serviceCode", METHOD);
        form.put("timestamp", timestamp);
        form.put("msgDigest", digest);
        form.put("msgData", msgData);
        return form;
    }

    private JsonNode parseNestedJson(JsonNode value) throws Exception
    {
        if (value == null || value.isMissingNode() || value.isNull())
        {
            return objectMapper.createObjectNode();
        }
        if (!value.isTextual())
        {
            return value;
        }
        JsonNode parsed = objectMapper.readTree(value.asText());
        return parsed == null ? objectMapper.createObjectNode() : parsed;
    }

    static String normalizeCheckPhoneNo(String value)
    {
        if (value == null || value.isBlank())
        {
            return "";
        }
        String digits = value.replaceAll("\\D", "");
        return digits.length() >= 4 ? digits.substring(digits.length() - 4) : "";
    }

    private String firstNonBlank(String... values)
    {
        for (String value : values)
        {
            if (value != null && !value.isBlank())
            {
                return value.trim();
            }
        }
        return "顺丰返回查询失败";
    }

    private String maskTrackingNo(String value)
    {
        if (value == null || value.length() <= 4)
        {
            return "****";
        }
        return value.substring(0, Math.min(2, value.length())) + "****"
                + value.substring(value.length() - 4);
    }

    private OffsetDateTime parseTime(String value)
    {
        try
        {
            if (value == null || value.isBlank())
            {
                return null;
            }
            String normalized = value.trim().replace(" ", "T");
            try
            {
                return OffsetDateTime.parse(normalized, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            }
            catch (Exception ignored)
            {
                return java.time.LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .atOffset(ZoneOffset.ofHours(8));
            }
        }
        catch (Exception exception)
        {
            return null;
        }
    }
}
