package com.ruoyi.rentalbusiness.integration.express;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

/** 顺丰丰桥轨迹查询协议测试。 */
public class ShunfengExpressClientTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 丰桥公共参数必须使用表单、毫秒时间戳、中文语言码和手机号后四位。 */
    @Test
    public void query_shouldSendStandardFormRequestAndParseNestedResponse() throws Exception
    {
        String trackingNo = "SFTEST000001";
        String businessData = objectMapper.writeValueAsString(Map.of(
                "success", true,
                "errorCode", "S0000",
                "msgData", Map.of("routeResps", List.of(Map.of(
                        "mailNo", trackingNo,
                        "routes", List.of(Map.of(
                                "acceptAddress", "深圳市",
                                "acceptTime", "2026-09-15 10:00:00",
                                "remark", "顺丰速运 已收取快件",
                                "opCode", "50")))))));
        ShunfengExpressClient client = new ShunfengExpressClient(null, objectMapper);
        ExpressTrackResult result = client.parse(trackingNo, objectMapper.writeValueAsString(Map.of(
                "apiResultCode", "A1000",
                "apiResultData", businessData)));

        Assert.assertTrue(result.available());
        Assert.assertEquals(1, result.nodes().size());
        Assert.assertEquals("50", result.nodes().get(0).status());
        Assert.assertEquals("深圳市", result.nodes().get(0).location());

        ExpressTrackQuery query = new ExpressTrackQuery(ExpressProvider.SF, trackingNo, null, "13800138000")
                .normalized();
        String msgData = ShunfengExpressClient.buildMessage(objectMapper, query);
        Map<String, String> form = ShunfengExpressClient.buildForm("partner-id", "check-word", msgData,
                1_757_920_000_000L);
        Assert.assertEquals("partner-id", form.get("partnerID"));
        Assert.assertEquals("EXP_RECE_SEARCH_ROUTES", form.get("serviceCode"));
        Assert.assertFalse(form.get("requestID").contains("-"));
        Assert.assertEquals("1757920000000", form.get("timestamp"));
        Assert.assertEquals(Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5")
                .digest((form.get("msgData") + form.get("timestamp") + "check-word")
                        .getBytes(StandardCharsets.UTF_8))), form.get("msgDigest"));

        JsonNode message = objectMapper.readTree(form.get("msgData"));
        Assert.assertEquals("0", message.path("language").asText());
        Assert.assertEquals("8000", message.path("checkPhoneNo").asText());
    }

    /** 顺丰业务层失败时应把真实错误消息返回给前端，而不是固定显示查询失败。 */
    @Test
    public void query_shouldExposeBusinessErrorMessage() throws Exception
    {
        String businessData = objectMapper.writeValueAsString(Map.of(
                "success", false,
                "errorCode", "S1001",
                "errorMsg", "运单不属于当前接入编码"));
        ShunfengExpressClient client = new ShunfengExpressClient(null, objectMapper);
        ExpressTrackResult result = client.parse("SFTEST000001", objectMapper.writeValueAsString(Map.of(
                "apiResultCode", "A1000",
                "apiResultData", businessData)));

        Assert.assertFalse(result.available());
        Assert.assertEquals("运单不属于当前接入编码", result.message());
    }
}
