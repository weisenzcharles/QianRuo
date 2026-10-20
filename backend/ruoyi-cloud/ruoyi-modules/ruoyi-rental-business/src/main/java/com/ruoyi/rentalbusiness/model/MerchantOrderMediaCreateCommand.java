package com.ruoyi.rentalbusiness.model;

/** 订单履约媒体登记命令，文件本体由统一文件服务上传。 */
public class MerchantOrderMediaCreateCommand
{
    /** 服务端生成的媒体唯一编号，批量登记时由数据库回填。 */
    public Long mediaId;
    /** 媒体类型编码，例如 ship_video。 */
    public String mediaType;
    /** 媒体类型数据库编码，由服务端根据 mediaType 映射。 */
    public Integer mediaTypeCode;
    /** 文件服务返回的访问地址或对象 Key。 */
    public String fileUrl;
    public String fileName;
    public Long fileSize;
    public String mimeType;
    /** 视频识别出的 SN；未识别时允许为空，进入待人工处理队列。 */
    public String detectedSn;
    public String remark;
}
