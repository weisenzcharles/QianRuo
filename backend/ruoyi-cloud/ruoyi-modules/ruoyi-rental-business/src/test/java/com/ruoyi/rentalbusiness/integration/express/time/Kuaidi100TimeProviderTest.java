package com.ruoyi.rentalbusiness.integration.express.time;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.rentalbusiness.model.ExpressTimeProviderRequest;
import com.ruoyi.rentalbusiness.model.ExpressTimeProviderResult;

/** 快递100智能时效预估协议测试。 */
public class Kuaidi100TimeProviderTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** param 和签名必须符合快递100时效接口约定，且私密密钥不能进入 param。 */
    @Test
    public void buildForm_shouldUseExpectedSignatureAndRequestFields() throws Exception
    {
        ExpressTimeProviderRequest request = new ExpressTimeProviderRequest(
                "深圳市南山区测试路1号", "北京市海淀区测试路2号", "shunfeng", "标准快递",
                LocalDateTime.of(2026, 9, 16, 10, 20, 30));
        String param = Kuaidi100TimeProvider.buildParam(objectMapper, request);
        Map<String, String> form = Kuaidi100TimeProvider.buildForm("key", "secret", param, 1_757_980_830_000L);

        JsonNode paramJson = objectMapper.readTree(form.get("param"));
        Assert.assertEquals("深圳市南山区测试路1号", paramJson.path("from").asText());
        Assert.assertEquals("北京市海淀区测试路2号", paramJson.path("to").asText());
        Assert.assertEquals("2026-09-16 10:20:30", paramJson.path("orderTime").asText());
        Assert.assertEquals("shunfeng", paramJson.path("kuaidicom").asText());
        Assert.assertEquals("标准快递", paramJson.path("expType").asText());
        Assert.assertFalse(form.get("param").contains("secret"));
        Assert.assertEquals("time", form.get("method"));
        Assert.assertEquals("key", form.get("key"));
        Assert.assertEquals("1757980830000", form.get("t"));
        byte[] digest = MessageDigest.getInstance("MD5").digest(
                (param + form.get("t") + "key" + "secret").getBytes(StandardCharsets.UTF_8));
        String expectedSign = java.util.HexFormat.of().formatHex(digest).toUpperCase();
        Assert.assertEquals(expectedSign, form.get("sign"));
    }

    /** 成功响应只转换需要保存的字段，并保留供应商原始 kuaidicom 供服务层反向映射。 */
    @Test
    public void parseResponse_shouldMapEstimateFields() throws Exception
    {
        String body = "{\"success\":true,\"code\":\"200\",\"data\":{"
                + "\"kuaidicom\":\"shunfeng\",\"fromName\":\"深圳市南山区\","
                + "\"toName\":\"北京市海淀区\",\"deliveryExpendTime\":\"2.00\","
                + "\"expType\":\"标准快递\",\"totalTime\":48,\"remainTime\":24}}";

        ExpressTimeProviderResult result = Kuaidi100TimeProvider.parseResponse(objectMapper, body);

        Assert.assertTrue(result.available());
        Assert.assertEquals("shunfeng", result.providerCompanyCode());
        Assert.assertEquals("深圳市南山区", result.fromName());
        Assert.assertEquals("北京市海淀区", result.toName());
        Assert.assertEquals("标准快递", result.expressType());
        Assert.assertEquals(new BigDecimal("2.00"), result.deliveryExpendTime());
        Assert.assertEquals(new BigDecimal("48"), result.totalTime());
        Assert.assertEquals(new BigDecimal("24"), result.remainTime());
    }

    /** 供应商返回空时效时不得把不完整数据写入缓存或数据库。 */
    @Test
    public void parseResponse_shouldRejectIncompleteEstimate() throws Exception
    {
        String body = "{\"success\":true,\"data\":{\"kuaidicom\":\"shunfeng\","
                + "\"fromName\":\"深圳市南山区\",\"toName\":\"北京市海淀区\"}}";

        ExpressTimeProviderResult result = Kuaidi100TimeProvider.parseResponse(objectMapper, body);

        Assert.assertFalse(result.available());
        Assert.assertEquals("快递100返回的时效数据不完整", result.message());
    }
}
