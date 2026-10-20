package com.ruoyi.rentalbusiness.integration.express;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.GlobalConfigConstants;
import com.ruoyi.rentalbusiness.config.GlobalConfigService;

/** 快递100即时查询客户端。 */
@Service
public class Kuaidi100Client implements ExpressTrackingClient
{
    private static final Logger log = LoggerFactory.getLogger(Kuaidi100Client.class);
    private final GlobalConfigService config;
    private final ObjectMapper objectMapper;

    public Kuaidi100Client(GlobalConfigService config, ObjectMapper objectMapper)
    {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Override
    public ExpressProvider provider()
    {
        return ExpressProvider.KUAIDI100;
    }

    @Override
    public ExpressTrackResult query(ExpressTrackQuery input)
    {
        ExpressTrackQuery query = input.normalized();
        String customer = config.get(GlobalConfigConstants.EXPRESS_KUAIDI100_CUSTOMER);
        String key = config.get(GlobalConfigConstants.EXPRESS_KUAIDI100_KEY);
        if (customer.isBlank() || key.isBlank())
        {
            return ExpressTrackResult.unavailable(provider(), query.trackingNo(), "未配置快递100接口凭证");
        }
        try
        {
            String param = buildParam(objectMapper, query);
            Map<String, String> form = new HashMap<>();
            form.put("customer", customer);
            form.put("param", param);
            form.put("sign", ExpressHttpSupport.md5(param + key + customer).toUpperCase());
            String endpoint = config.get(GlobalConfigConstants.EXPRESS_KUAIDI100_TRACKING_URL,
                    "https://poll.kuaidi100.com/poll/query.do");
            var response = ExpressHttpSupport.postForm(ExpressHttpSupport.client(), endpoint, form);
            if (!ExpressHttpSupport.success(response))
            {
                return ExpressTrackResult.unavailable(provider(), query.trackingNo(), "快递100轨迹服务暂不可用");
            }
            return parse(query.trackingNo(), response.body());
        }
        catch (Exception exception)
        {
            log.warn("查询快递100轨迹失败，trackingNo={}, message={}", query.trackingNo(), exception.getMessage());
            return ExpressTrackResult.unavailable(provider(), query.trackingNo(), "快递100轨迹查询失败");
        }
    }

    static String buildParam(ObjectMapper objectMapper, ExpressTrackQuery query) throws Exception
    {
        Map<String, String> param = new HashMap<>();
        param.put("com", query.shipperCode() == null ? "auto" : query.shipperCode());
        param.put("num", query.trackingNo());
        // resultv2=8 返回预计送达时间；to 用于提高目的地和时效识别准确率。
        param.put("resultv2", "8");
        if (query.destinationAddress() != null && !query.destinationAddress().isBlank())
        {
            param.put("to", query.destinationAddress());
        }
        return objectMapper.writeValueAsString(param);
    }

    ExpressTrackResult parse(String trackingNo, String body) throws Exception
    {
        JsonNode root = objectMapper.readTree(body);
        if (!"200".equals(root.path("status").asText()))
        {
            String message = root.path("message").asText();
            return ExpressTrackResult.unavailable(provider(), trackingNo,
                    message.isBlank() ? "快递100轨迹查询失败" : message);
        }
        List<ExpressTrackNode> nodes = new ArrayList<>();
        for (JsonNode item : root.path("data"))
        {
            nodes.add(new ExpressTrackNode(parseTime(item.path("ftime").asText(null)),
                    root.path("state").asText(null), item.path("context").asText(""),
                    item.path("location").asText(null)));
        }
        return new ExpressTrackResult(true, provider(), trackingNo, root.path("state").asText(), nodes, null,
                parseEstimatedArriveTime(root));
    }

    private LocalDateTime parseEstimatedArriveTime(JsonNode root)
    {
        String value = root.path("arrivalTime").asText(null);
        if (value == null || value.isBlank())
        {
            value = root.path("lastResult").path("arrivalTime").asText(null);
        }
        if (value == null || value.isBlank())
        {
            return null;
        }
        for (DateTimeFormatter formatter : List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH"),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        {
            try
            {
                return LocalDateTime.parse(value.trim(), formatter);
            }
            catch (DateTimeParseException exception)
            {
                // 供应商可能只返回到小时，继续尝试其他官方返回格式。
            }
        }
        return null;
    }

    private OffsetDateTime parseTime(String value)
    {
        try
        {
            return value == null ? null : OffsetDateTime.parse(value.replace(" ", "T") + "+08:00",
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.ofHours(8)));
        }
        catch (Exception exception)
        {
            return null;
        }
    }
}
