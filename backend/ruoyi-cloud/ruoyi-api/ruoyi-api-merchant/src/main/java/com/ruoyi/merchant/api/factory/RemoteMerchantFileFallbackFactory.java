package com.ruoyi.merchant.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.RemoteMerchantFileService;
import com.ruoyi.merchant.api.model.MerchantFileInfo;

/** 商户文件服务降级处理。 */
@Component
public class RemoteMerchantFileFallbackFactory implements FallbackFactory<RemoteMerchantFileService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteMerchantFileFallbackFactory.class);

    @Override
    public RemoteMerchantFileService create(Throwable cause)
    {
        log.error("商户文件服务调用失败: {}", cause.getMessage());
        return new RemoteMerchantFileService()
        {
            @Override
            public R<MerchantFileInfo> upload(MultipartFile file, String businessType)
            {
                return R.fail("上传文件失败:" + cause.getMessage());
            }

            @Override
            public R<Boolean> delete(String fileUrl)
            {
                return R.fail("删除文件失败:" + cause.getMessage());
            }
        };
    }
}
