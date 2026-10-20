package com.ruoyi.rentalbusiness.model;

import java.util.Date;

/** 订单履约媒体视图，不向商户端返回媒体内部唯一编号。 */
public class MerchantOrderMediaVO
{
    public Long mediaId;
    public Long orderId;
    public Long snId;
    public String orderNo;
    public String mediaType;
    public String mediaTypeLabel;
    public String detectedSn;
    public Integer matchStatus;
    public String matchStatusLabel;
    public String fileUrl;
    public String fileName;
    public Long fileSize;
    public String mimeType;
    public Date createTime;
}
