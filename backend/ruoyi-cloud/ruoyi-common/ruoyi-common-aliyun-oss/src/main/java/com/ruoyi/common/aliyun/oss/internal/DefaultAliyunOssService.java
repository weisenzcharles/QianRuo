package com.ruoyi.common.aliyun.oss.internal;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.ruoyi.common.aliyun.oss.AliyunOssException;
import com.ruoyi.common.aliyun.oss.AliyunOssService;
import com.ruoyi.common.aliyun.oss.AliyunOssUsageSnapshot;
import com.ruoyi.common.aliyun.oss.config.AliyunOssProperties;

/**
 * OSS SDK 的项目内适配器。
 */
public class DefaultAliyunOssService implements AliyunOssService
{
    private final OSS client;
    private final AliyunOssProperties properties;

    public DefaultAliyunOssService(OSS client, AliyunOssProperties properties)
    {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public String upload(String objectKey, InputStream content, long contentLength, String contentType)
    {
        String safeKey = validateObjectKey(objectKey);
        if (content == null || contentLength < 0)
        {
            throw new IllegalArgumentException("OSS content and a non-negative content length are required");
        }

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType(contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType);
        try
        {
            client.putObject(new PutObjectRequest(properties.getBucketName(), safeKey, content, metadata));
            return properties.resolvePublicBaseUrl() + "/" + encodeObjectKey(safeKey);
        }
        catch (Exception exception)
        {
            throw new AliyunOssException("阿里云 OSS 上传失败", exception);
        }
    }

    @Override
    public void deleteByUrl(String fileUrl)
    {
        String objectKey = objectKeyFromUrl(fileUrl);
        try
        {
            client.deleteObject(properties.getBucketName(), objectKey);
        }
        catch (Exception exception)
        {
            throw new AliyunOssException("阿里云 OSS 删除失败", exception);
        }
    }

    @Override
    public AliyunOssUsageSnapshot usage()
    {
        try
        {
            com.aliyun.oss.model.BucketStat stat = client.getBucketStat(properties.getBucketName());
            long storageSize = stat == null || stat.getStorageSize() == null ? 0L : stat.getStorageSize();
            long objectCount = stat == null || stat.getObjectCount() == null ? 0L : stat.getObjectCount();
            if (storageSize < 0L || objectCount < 0L)
            {
                throw new IllegalStateException("OSS Bucket 使用量返回值不合法");
            }
            return new AliyunOssUsageSnapshot(storageSize, objectCount);
        }
        catch (Exception exception)
        {
            throw new AliyunOssException("阿里云 OSS 使用量查询失败", exception);
        }
    }

    static String validateObjectKey(String objectKey)
    {
        if (objectKey == null || objectKey.isBlank() || objectKey.startsWith("/")
                || objectKey.contains("\\") || objectKey.contains("?") || objectKey.contains("#"))
        {
            throw new IllegalArgumentException("Invalid OSS object key");
        }
        for (String segment : objectKey.split("/", -1))
        {
            if (segment.isBlank() || ".".equals(segment) || "..".equals(segment))
            {
                throw new IllegalArgumentException("Invalid OSS object key");
            }
        }
        return objectKey;
    }

    private String objectKeyFromUrl(String fileUrl)
    {
        if (fileUrl == null || fileUrl.isBlank())
        {
            throw new IllegalArgumentException("File URL is required");
        }

        URI baseUri = URI.create(properties.resolvePublicBaseUrl());
        URI fileUri = URI.create(fileUrl);
        if (!sameOrigin(baseUri, fileUri) || fileUri.getRawQuery() != null || fileUri.getRawFragment() != null
                || fileUri.getRawUserInfo() != null)
        {
            throw new IllegalArgumentException("File URL does not belong to the configured OSS public domain");
        }

        String basePath = trimTrailingSlash(baseUri.getRawPath());
        String filePath = fileUri.getRawPath();
        String requiredPrefix = basePath + "/";
        if (filePath == null || !filePath.startsWith(requiredPrefix))
        {
            throw new IllegalArgumentException("File URL is outside the configured OSS path");
        }

        String encodedKey = filePath.substring(requiredPrefix.length());
        String objectKey = Arrays.stream(encodedKey.split("/", -1))
                .map(segment -> URLDecoder.decode(segment.replace("+", "%2B"), StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));
        return validateObjectKey(objectKey);
    }

    private static boolean sameOrigin(URI left, URI right)
    {
        return left.getScheme() != null && left.getScheme().equalsIgnoreCase(right.getScheme())
                && left.getHost() != null && left.getHost().equalsIgnoreCase(right.getHost())
                && left.getPort() == right.getPort();
    }

    private static String encodeObjectKey(String objectKey)
    {
        return Arrays.stream(objectKey.split("/", -1))
                .map(segment -> URLEncoder.encode(segment, StandardCharsets.UTF_8)
                        .replace("+", "%20").replace("%7E", "~"))
                .collect(Collectors.joining("/"));
    }

    private static String trimTrailingSlash(String path)
    {
        if (path == null || path.isEmpty() || "/".equals(path))
        {
            return "";
        }
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }
}
