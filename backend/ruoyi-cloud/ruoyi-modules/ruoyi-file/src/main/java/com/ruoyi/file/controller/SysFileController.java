package com.ruoyi.file.controller;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.FileBusinessType;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.file.FileUtils;
import com.ruoyi.common.core.utils.file.FileTypeUtils;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.common.aliyun.oss.AliyunOssService;
import com.ruoyi.common.aliyun.oss.AliyunOssUsageSnapshot;
import com.ruoyi.file.service.ISysFileService;
import com.ruoyi.system.api.model.PlatformObjectStorageUsage;
import com.ruoyi.system.api.domain.SysFile;

/**
 * 文件请求处理
 * 
 * @author ruoyi
 */
@RestController
public class SysFileController
{
    private static final long REGISTRATION_FILE_MAX_SIZE = 10L * 1024L * 1024L;

    private static final Set<String> REGISTRATION_FILE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf");

    private static final Logger log = LoggerFactory.getLogger(SysFileController.class);

    @Autowired
    private ISysFileService sysFileService;

    @Autowired(required = false)
    private AliyunOssService aliyunOssService;

    /**
     * 文件上传请求
     */
    @PostMapping("upload")
    public R<SysFile> upload(MultipartFile file,
            @RequestParam(value = "businessType", required = false) String businessType)
    {
        try
        {
            // 上传并返回访问地址
            String url = sysFileService.uploadFile(file, businessType);
            SysFile sysFile = new SysFile();
            sysFile.setName(FileUtils.getName(url));
            sysFile.setUrl(url);
            return R.ok(sysFile);
        }
        catch (Exception e)
        {
            log.error("上传文件失败", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 上传商户入驻所需的图片或 PDF 资料。接口限制手机号、四位验证码、文件大小和扩展名，
     * 短信服务接入后应将四位数字格式校验替换为服务端验证码核销。
     *
     * @param file 待上传的商户资质文件
     * @param phone 商户申请手机号
     * @param code 当前四位验证码
     * @return 上传成功后的文件名称和访问地址
     */
    @PostMapping("public/merchant-registration/upload")
    public R<SysFile> uploadMerchantRegistrationFile(MultipartFile file, String phone, String code)
    {
        try
        {
            if (StringUtils.isBlank(phone) || !phone.matches("^1[3-9]\\d{9}$"))
            {
                return R.fail("手机号格式不正确");
            }
            if (StringUtils.isBlank(code) || !code.matches("^\\d{4}$"))
            {
                return R.fail("请输入四位数字验证码");
            }
            if (file == null || file.isEmpty())
            {
                return R.fail("上传文件不能为空");
            }
            if (file.getSize() > REGISTRATION_FILE_MAX_SIZE)
            {
                return R.fail("入驻资料文件不能超过10MB");
            }
            String extension = FileTypeUtils.getExtension(file).toLowerCase();
            if (!REGISTRATION_FILE_EXTENSIONS.contains(extension))
            {
                return R.fail("入驻资料仅支持 jpg、jpeg、png、pdf 格式");
            }
            String url = sysFileService.uploadFile(file, FileBusinessType.MERCHANT_REGISTRATION.getCode());
            SysFile sysFile = new SysFile();
            sysFile.setName(FileUtils.getName(url));
            sysFile.setUrl(url);
            return R.ok(sysFile);
        }
        catch (Exception e)
        {
            log.error("上传商户入驻资料失败", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 文件删除请求
     */
    @DeleteMapping("delete")
    public R<Boolean> delete(String fileUrl)
    {
        try
        {
            if (!FileUtils.validateFilePath(fileUrl))
            {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许删除。 ", fileUrl));
            }
            sysFileService.deleteFile(fileUrl);
            return R.ok();
        }
        catch (Exception e)
        {
            log.error("删除文件失败", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 为平台容量采集提供 OSS 实际使用量。总容量由平台系统配置维护，文件服务不返回凭据。
     */
    @InnerAuth
    @PostMapping("internal/storage/usage")
    public R<PlatformObjectStorageUsage> usage()
    {
        PlatformObjectStorageUsage result = new PlatformObjectStorageUsage();
        if (aliyunOssService == null)
        {
            result.setEnabled(false);
            return R.ok(result);
        }
        try
        {
            AliyunOssUsageSnapshot snapshot = aliyunOssService.usage();
            result.setEnabled(true);
            result.setStorageSizeBytes(snapshot.storageSizeBytes());
            result.setObjectCount(snapshot.objectCount());
            return R.ok(result);
        }
        catch (Exception exception)
        {
            log.error("查询 OSS 使用量失败", exception);
            return R.fail("查询对象存储使用量失败");
        }
    }
}
