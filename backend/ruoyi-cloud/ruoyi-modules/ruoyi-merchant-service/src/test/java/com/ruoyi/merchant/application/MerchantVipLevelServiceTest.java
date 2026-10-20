package com.ruoyi.merchant.application;

import java.util.Arrays;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.system.api.domain.SysDictData;

/** 会员等级展示必须使用启用的系统字典数据。 */
public class MerchantVipLevelServiceTest
{
    @Test
    public void toLabelMap_shouldMapEnabledLevelCodesToChineseLabels()
    {
        SysDictData junior = new SysDictData();
        junior.setDictValue("junior");
        junior.setDictLabel("初级会员");
        junior.setStatus(0);

        SysDictData disabled = new SysDictData();
        disabled.setDictValue("senior");
        disabled.setDictLabel("高级会员");
        disabled.setStatus(1);

        Map<String, String> labels = MerchantVipLevelService.toLabelMap(Arrays.asList(junior, disabled));

        Assert.assertEquals("初级会员", labels.get("junior"));
        Assert.assertFalse(labels.containsKey("senior"));
    }
}
