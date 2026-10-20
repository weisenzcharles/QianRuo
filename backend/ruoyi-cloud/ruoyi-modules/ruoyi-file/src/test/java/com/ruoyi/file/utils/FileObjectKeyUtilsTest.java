package com.ruoyi.file.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** 文件业务目录和对象键边界测试。 */
public class FileObjectKeyUtilsTest
{
    @Test
    public void build_addsBusinessDirectoryWithoutDatePath()
    {
        assertEquals("qianruo/merchant/20260922152045A001.png",
                FileObjectKeyUtils.build("merchant", "20260922152045A001.png", "qianruo"));
    }

    @Test
    public void build_blankBusinessTypeUsesCommonDirectory()
    {
        assertEquals("common/file.png", FileObjectKeyUtils.build(" ", "file.png", ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void build_unknownBusinessTypeRejected()
    {
        FileObjectKeyUtils.build("unknown", "file.png", "qianruo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void build_parentTraversalFileRejected()
    {
        FileObjectKeyUtils.build("order", "../private.txt", "qianruo");
    }

}
