package com.ruoyi.rentalbusiness.service;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.exception.ServiceException;

/** JDBC Map 字段转换回归测试。 */
public class RentalBusinessServiceValueConversionTest
{
    /**
     * 验证 MySQL 驱动将 {@code TINYINT(1)} 返回为布尔值时，能稳定转换为 0 或 1。
     */
    @Test
    public void shouldConvertBooleanJdbcValueToInteger()
    {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("disabled", Boolean.FALSE);
        row.put("enabled", Boolean.TRUE);

        Assert.assertEquals(Integer.valueOf(0), RentalBusinessService.valueNullable(row, "disabled"));
        Assert.assertEquals(Integer.valueOf(1), RentalBusinessService.valueNullable(row, "enabled"));
    }

    /**
     * 验证数值和布尔字符串使用同一转换规则，避免不同 JDBC 返回类型导致行为不一致。
     */
    @Test
    public void shouldConvertNumericAndBooleanTextToInteger()
    {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("number", Long.valueOf(7L));
        row.put("numericText", "12");
        row.put("falseText", "false");

        Assert.assertEquals(Integer.valueOf(7), RentalBusinessService.valueNullable(row, "number"));
        Assert.assertEquals(Integer.valueOf(12), RentalBusinessService.valueNullable(row, "numericText"));
        Assert.assertEquals(Integer.valueOf(0), RentalBusinessService.valueNullable(row, "falseText"));
    }

    /**
     * 验证字段值为空时返回空值，无效文本则转换为明确的业务异常。
     */
    @Test
    public void shouldHandleNullAndRejectInvalidIntegerText()
    {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("blank", "  ");
        row.put("invalid", "unknown");

        Assert.assertNull(RentalBusinessService.valueNullable(row, "missing"));
        Assert.assertNull(RentalBusinessService.valueNullable(row, "blank"));
        try
        {
            RentalBusinessService.valueNullable(row, "invalid");
            Assert.fail("无效整数文本必须抛出业务异常");
        }
        catch (ServiceException exception)
        {
            Assert.assertTrue(exception.getMessage().contains("invalid"));
        }
    }
}
