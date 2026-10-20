package com.ruoyi.common.core.utils;

import java.security.SecureRandom;

/**
 * 统一邀请码生成工具。
 *
 * <p>字符集主动排除数字零、数字一和易混淆字母，数据库唯一索引负责最终防重，
 * 调用方在极低概率冲突时应重新生成并再次写入。</p>
 */
public final class InviteCodeUtils
{
    private static final char[] CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DEFAULT_LENGTH = 8;

    /** 禁止实例化纯工具类。 */
    private InviteCodeUtils()
    {
    }

    /**
     * 生成系统默认长度的邀请码。
     *
     * @return 八位大写邀请码
     */
    public static String generate()
    {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * 生成指定长度的邀请码。
     *
     * @param length 邀请码长度，必须大于零
     * @return 使用安全随机源生成的邀请码
     * @throws IllegalArgumentException 长度不合法时抛出
     */
    public static String generate(int length)
    {
        if (length <= 0)
        {
            throw new IllegalArgumentException("邀请码长度必须大于0");
        }
        StringBuilder result = new StringBuilder(length);
        for (int index = 0; index < length; index++)
        {
            result.append(CHARACTERS[RANDOM.nextInt(CHARACTERS.length)]);
        }
        return result.toString();
    }
}
