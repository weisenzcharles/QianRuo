package com.ruoyi.rentalbusiness.integration.express;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

/** 快递100预计到达时间和目的地参数契约测试。 */
public class Kuaidi100ClientTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 归还件查询必须请求预计到达字段，并解析快递100返回的到达日期。 */
    @Test
    public void queryParamAndResponse_shouldKeepEstimatedArrivalDate() throws Exception
    {
        ExpressTrackQuery query = new ExpressTrackQuery(ExpressProvider.KUAIDI100,
                "JDVD10902242353", "jd", null, "浙江省杭州市余杭区归还仓").normalized();
        JsonNode request = objectMapper.readTree(Kuaidi100Client.buildParam(objectMapper, query));
        Assert.assertEquals("jd", request.path("com").asText());
        Assert.assertEquals("8", request.path("resultv2").asText());
        Assert.assertEquals("浙江省杭州市余杭区归还仓", request.path("to").asText());

        String body = objectMapper.writeValueAsString(Map.of(
                "status", "200",
                "state", "1",
                "arrivalTime", "2026-10-12 06",
                "data", List.of(Map.of(
                        "ftime", "2026-10-10 03:48:23",
                        "context", "快件运输中",
                        "location", "杭州市"))));
        ExpressTrackResult result = new Kuaidi100Client(null, objectMapper)
                .parse(query.trackingNo(), body);

        Assert.assertTrue(result.available());
        Assert.assertEquals(LocalDateTime.of(2026, 10, 12, 6, 0), result.estimatedArriveTime());
        Assert.assertEquals(1, result.nodes().size());
    }
}
