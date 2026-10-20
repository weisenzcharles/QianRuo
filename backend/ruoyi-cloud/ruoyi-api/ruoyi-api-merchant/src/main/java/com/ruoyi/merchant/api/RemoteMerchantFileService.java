package com.ruoyi.merchant.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.factory.RemoteMerchantFileFallbackFactory;
import com.ruoyi.merchant.api.model.MerchantFileInfo;

/** 商户服务使用的最小文件服务契约。 */
@FeignClient(contextId = "remoteMerchantFileService", value = ServiceNameConstants.FILE_SERVICE,
        fallbackFactory = RemoteMerchantFileFallbackFactory.class)
public interface RemoteMerchantFileService
{
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    R<MerchantFileInfo> upload(@RequestPart("file") MultipartFile file,
            @RequestParam(value = "businessType", required = false) String businessType);

    @DeleteMapping(value = "/delete", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    R<Boolean> delete(@RequestParam("fileUrl") String fileUrl);
}
