package com.ruoyi.file.service;

import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.enums.FileBusinessType;

/**
 * 文件上传接口
 * 
 * @author ruoyi
 */
public interface ISysFileService
{
    /**
     * 文件上传接口
     * 
     * @param file 上传的文件
     * @return 访问地址
     * @throws Exception
     */
    default String uploadFile(MultipartFile file) throws Exception
    {
        return uploadFile(file, FileBusinessType.COMMON.getCode());
    }

    /**
     * 按业务目录上传文件。
     *
     * @param file 文件
     * @param businessType 文件业务类型
     * @return 访问地址
     * @throws Exception 上传失败
     */
    String uploadFile(MultipartFile file, String businessType) throws Exception;

    /**
     * 文件删除接口
     * 
     * @param fileUrl 文件访问URL
     * @throws Exception
     */
    public void deleteFile(String fileUrl) throws Exception;
}
