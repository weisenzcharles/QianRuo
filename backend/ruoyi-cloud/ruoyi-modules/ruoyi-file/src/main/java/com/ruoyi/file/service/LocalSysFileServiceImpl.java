package com.ruoyi.file.service;

import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.enums.FileBusinessType;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.file.FileUtils;
import com.ruoyi.common.core.utils.file.MimeTypeUtils;
import com.ruoyi.file.utils.FileUploadUtils;

/**
 * 本地文件存储
 * 
 * @author ruoyi
 */
@Service
@ConditionalOnProperty(prefix = "file", name = "storage-provider", havingValue = "local", matchIfMissing = true)
public class LocalSysFileServiceImpl implements ISysFileService
{
    /**
     * 资源映射路径 前缀
     */
    @Value("${file.prefix}")
    public String localFilePrefix;

    /**
     * 域名或本机访问地址
     */
    @Value("${file.domain}")
    public String domain;
    
    /**
     * 上传文件存储在本地的根路径
     */
    @Value("${file.path}")
    private String localFilePath;

    /**
     * 本地文件上传接口
     * 
     * @param file 上传的文件
     * @return 访问地址
     * @throws Exception
     */
    @Override
    public String uploadFile(MultipartFile file, String businessType) throws Exception
    {
        FileBusinessType type = businessType == null || businessType.isBlank()
                ? FileBusinessType.COMMON
                : FileBusinessType.fromCode(businessType);
        String name = FileUploadUtils.upload(localFilePath + File.separator + type.getCode(), file,
                MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION,
                FileUploadUtils.getMaxSize(type));
        String url = domain + localFilePrefix + "/" + type.getCode() + name;
        return url;
    }

    /**
     * 本地文件删除接口
     * 
     * @param fileUrl 文件访问URL
     * @throws Exception
     */
    @Override
    public void deleteFile(String fileUrl) throws Exception
    {
        String localFile = StringUtils.substringAfter(fileUrl, localFilePrefix);
        FileUtils.deleteFile(localFilePath + localFile);
    }
}
