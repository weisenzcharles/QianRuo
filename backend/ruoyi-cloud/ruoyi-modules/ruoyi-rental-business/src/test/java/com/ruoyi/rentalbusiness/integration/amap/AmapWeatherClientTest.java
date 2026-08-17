package com.ruoyi.rentalbusiness.integration.amap;

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
}
