package com.ruoyi.file.utils;

import java.lang.reflect.Proxy;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.enums.FileBusinessType;
import com.ruoyi.common.core.exception.file.FileSizeLimitExceededException;
import com.ruoyi.common.core.utils.file.MimeTypeUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** 文件上传名称规则测试。 */
public class FileUploadUtilsTest
{
    @Test
    public void extractFilename_usesTimestampAndExtensionOnly()
    {
        MultipartFile file = (MultipartFile) Proxy.newProxyInstance(
                MultipartFile.class.getClassLoader(),
                new Class<?>[] { MultipartFile.class },
                (proxy, method, args) -> "getOriginalFilename".equals(method.getName()) ? "license.pdf" : null);

        assertTrue(FileUploadUtils.extractFilename(file).matches("\\d{14}A\\d{3}\\.pdf"));
    }

    @Test
    public void orderBusiness_allowsVideoUpTo200Mb() throws Exception
    {
        MultipartFile file = mockFile("shipment.MP4", 200L * 1024L * 1024L);

        FileUploadUtils.assertAllowed(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION,
                FileUploadUtils.getMaxSize(FileBusinessType.ORDER));

        assertEquals(200L * 1024L * 1024L, FileUploadUtils.getMaxSize(FileBusinessType.ORDER));
    }

    @Test(expected = FileSizeLimitExceededException.class)
    public void commonBusiness_rejectsFilesOver50Mb() throws Exception
    {
        MultipartFile file = mockFile("document.pdf", 51L * 1024L * 1024L);
        FileUploadUtils.assertAllowed(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    }

    private MultipartFile mockFile(String name, long size)
    {
        return (MultipartFile) Proxy.newProxyInstance(
                MultipartFile.class.getClassLoader(),
                new Class<?>[] { MultipartFile.class },
                (proxy, method, args) -> {
                    if ("getOriginalFilename".equals(method.getName()))
                    {
                        return name;
                    }
                    if ("getSize".equals(method.getName()))
                    {
                        return size;
                    }
                    return null;
                });
    }
}
