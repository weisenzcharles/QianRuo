package com.ruoyi.file.service;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.aliyun.oss.AliyunOssService;
import com.ruoyi.common.core.enums.FileBusinessType;
import com.ruoyi.common.core.utils.file.MimeTypeUtils;
import com.ruoyi.file.config.PlatformAliyunOssConfigProvider;
import com.ruoyi.file.config.AliyunOssConfig;
import com.ruoyi.file.utils.FileUploadUtils;
import com.ruoyi.file.utils.FileObjectKeyUtils;

/**
 * 通过公共 OSS 客户端实现文件服务接口。
 */
@Service
@ConditionalOnProperty(prefix = "file", name = "storage-provider", havingValue = "aliyun-oss")
public class AliyunOssSysFileServiceImpl implements ISysFileService
{
    private final AliyunOssService aliyunOssService;
    private final PlatformAliyunOssConfigProvider configProvider;

    public AliyunOssSysFileServiceImpl(AliyunOssService aliyunOssService,
            PlatformAliyunOssConfigProvider configProvider)
    {
        this.aliyunOssService = aliyunOssService;
        this.configProvider = configProvider;
    }

    @Override
    public String uploadFile(MultipartFile file, String businessType) throws Exception
    {
        if (file.getOriginalFilename() == null
                || file.getOriginalFilename().length() > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH)
        {
            throw new IOException("Invalid upload file name");
        }
        FileBusinessType type = businessType == null || businessType.isBlank()
                ? FileBusinessType.COMMON
                : FileBusinessType.fromCode(businessType);
        FileUploadUtils.assertAllowed(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION,
                FileUploadUtils.getMaxSize(type));

        AliyunOssConfig config = configProvider.getConfig();
        String objectKey = FileObjectKeyUtils.build(
                type.getCode(),
                FileUploadUtils.extractFilename(file), config.objectPrefix());
        try (InputStream content = file.getInputStream())
        {
            return aliyunOssService.upload(objectKey, content, file.getSize(), file.getContentType());
        }
    }

    @Override
    public void deleteFile(String fileUrl)
    {
        aliyunOssService.deleteByUrl(fileUrl);
    }
}
