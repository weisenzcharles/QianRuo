package com.ruoyi.common.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/** 统一邀请码生成工具测试。 */
public class InviteCodeUtilsTest
{
    /** 验证默认邀请码长度、字符集和连续生成差异。 */
    @Test
    public void shouldGenerateReadableUniqueLookingCodes()
    {
        String first = InviteCodeUtils.generate();
        String second = InviteCodeUtils.generate();
        assertEquals(8, first.length());
        assertTrue(first.matches("[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{8}"));
        assertNotEquals(first, second);
    }

    /** 验证非法长度会被明确拒绝。 */
    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectInvalidLength()
    {
        InviteCodeUtils.generate(0);
    }
}
