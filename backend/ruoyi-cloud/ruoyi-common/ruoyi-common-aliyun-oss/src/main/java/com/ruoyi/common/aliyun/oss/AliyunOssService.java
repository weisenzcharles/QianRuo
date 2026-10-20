package com.ruoyi.common.aliyun.oss;

import java.io.InputStream;

/**
 * 项目内统一的阿里云 OSS 对象存储能力。
 */
public interface AliyunOssService
{
    /**
     * 上传对象并返回其配置的公开访问地址。
     * 调用方负责关闭输入流。
     *
     * @param objectKey 对象键
     * @param content 文件内容
     * @param contentLength 文件字节数
     * @param contentType MIME 类型
     * @return 文件访问地址
     */
    String upload(String objectKey, InputStream content, long contentLength, String contentType);

    /**
     * 删除当前配置 Bucket 下的历史文件地址。
     *
     * @param fileUrl 上传时返回的文件访问地址
     */
    void deleteByUrl(String fileUrl);

    /**
     * 查询当前 Bucket 的实际对象存储使用量。
     *
     * @return OSS 使用量快照
     */
    AliyunOssUsageSnapshot usage();
}
