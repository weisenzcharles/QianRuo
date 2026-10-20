package com.ruoyi.rentalbusiness.task;

import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.rentalbusiness.domain.RegionWeatherRecord;

/** 发货日前一日天气风险决策测试。 */
public class RegionWeatherRefreshTaskTest
{
    @Test
    public void weatherRiskFlag_任一地点为风险天气_订单标记风险()
    {
        RegionWeatherRecord departure = record(RentalCode.YesNo.NO.getCode());
        RegionWeatherRecord arrival = record(RentalCode.YesNo.YES.getCode());

        Assert.assertEquals(RentalCode.YesNo.YES.getCode(),
                RegionWeatherRefreshTask.weatherRiskFlag(departure, arrival));
    }

    @Test
    public void weatherRiskFlag_查询失败_不覆盖为风险天气()
    {
        RegionWeatherRecord failed = record(RentalCode.YesNo.YES.getCode());
        failed.queryStatus = RentalCode.WeatherQueryStatus.FAILED.getCode();

        Assert.assertEquals(RentalCode.YesNo.NO.getCode(), RegionWeatherRefreshTask.weatherRiskFlag(failed, null));
    }

    private RegionWeatherRecord record(Integer riskFlag)
    {
        RegionWeatherRecord record = new RegionWeatherRecord();
        record.queryStatus = RentalCode.WeatherQueryStatus.SUCCESS.getCode();
        record.riskFlag = riskFlag;
        return record;
    }
}
