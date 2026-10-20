package com.ruoyi.merchant.application;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.system.api.domain.SysDictData;

/** 商户操作日志字段字典缓存兼容测试。 */
public class MerchantAuditFieldLabelServiceTest
{
    @Test
    public void containsRequiredLabels_旧缓存缺字段_要求重新加载()
    {
        Assert.assertFalse(MerchantAuditFieldLabelService.containsRequiredLabels(
                Arrays.asList(dict("phoneDisplay"), dict("emailDisplay"))));
    }

    @Test
    public void containsRequiredLabels_新字段齐全_继续使用缓存()
    {
        Assert.assertTrue(MerchantAuditFieldLabelService.containsRequiredLabels(
                Arrays.asList(dict("phoneDisplay"), dict("emailDisplay"), dict("warehouseNames"))));
    }

    private SysDictData dict(String value)
    {
        SysDictData data = new SysDictData();
        data.setDictValue(value);
        return data;
    }
}
