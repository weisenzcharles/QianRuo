package com.ruoyi.rentalbusiness.model;

import java.util.List;

/** 批量登记订单履约媒体的请求。文件本体由统一文件服务先行上传。 */
public class MerchantOrderMediaBatchCreateCommand
{
    /** 批量媒体类型，目前订单批量匹配只开放发货视频。 */
    public String mediaType;
    /** 已上传文件列表，最多 50 个。 */
    public List<MerchantOrderMediaCreateCommand> items;
}
