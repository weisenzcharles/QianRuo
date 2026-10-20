package com.ruoyi.rentalbusiness.integration.amap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.redis.service.RedisService;
import org.junit.Assert;
import org.junit.Test;

/** 高德天气客户端 Key 轮换规则测试。 */
public class AmapWeatherClientTest
{
    /**
     * 验证每 5000 次真实请求切换一次 Key，并在最后一个 Key 后按顺序循环。
     */
    @Test
    public void keyIndexForCall_shouldRotateEveryFiveThousandCalls()
    {
        Assert.assertEquals(0, AmapWeatherClient.keyIndexForCall(1L, 3));
        Assert.assertEquals(0, AmapWeatherClient.keyIndexForCall(5000L, 3));
        Assert.assertEquals(1, AmapWeatherClient.keyIndexForCall(5001L, 3));
        Assert.assertEquals(1, AmapWeatherClient.keyIndexForCall(10000L, 3));
        Assert.assertEquals(2, AmapWeatherClient.keyIndexForCall(10001L, 3));
        Assert.assertEquals(0, AmapWeatherClient.keyIndexForCall(15001L, 3));
    }

    /** 验证无效请求序号或空 Key 配置不会静默选择错误的 Key。 */
    @Test(expected = IllegalArgumentException.class)
    public void keyIndexForCall_shouldRejectInvalidArguments()
    {
        AmapWeatherClient.keyIndexForCall(0L, 2);
    }

    /** 缓存未命中时只返回待刷新结果，订单列表不能触发外部天气网络请求。 */
    @Test(timeout = 5000L)
    public void queryCached_cacheMiss_shouldReturnWithoutRemoteRequest()
    {
        AmapWeatherClient client = new AmapWeatherClient(null, new EmptyRedisService(), new ObjectMapper());

        AmapWeatherSnapshot result = client.queryCached("110100");

        Assert.assertFalse(result.available);
        Assert.assertEquals("天气待刷新", result.message);
    }

    /** 缓存命中时返回已经准备好的天气快照。 */
    @Test
    public void queryCached_cacheHit_shouldReturnCachedSnapshot()
    {
        AmapWeatherClient client = new AmapWeatherClient(null,
                new CachedRedisService("{\"available\":true,\"adcode\":\"110100\",\"weather\":\"晴\"}"),
                new ObjectMapper());

        AmapWeatherSnapshot result = client.queryCached("110100");

        Assert.assertTrue(result.available);
        Assert.assertEquals("晴", result.weather);
    }

    private static class EmptyRedisService extends RedisService
    {
        @Override
        public <T> T getCacheObject(String key)
        {
            return null;
        }
    }

    private static class CachedRedisService extends RedisService
    {
        private final String value;

        private CachedRedisService(String value)
        {
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCacheObject(String key)
        {
            return (T) value;
        }
    }
}
