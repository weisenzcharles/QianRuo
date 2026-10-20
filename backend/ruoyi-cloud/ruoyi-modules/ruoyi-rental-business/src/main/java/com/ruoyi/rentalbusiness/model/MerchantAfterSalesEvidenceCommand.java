package com.ruoyi.rentalbusiness.model;

/** 售后证据上传命令，文件内容由统一文件服务上传后只提交对象地址。 */
public class MerchantAfterSalesEvidenceCommand
{
    public Integer evidenceType;
    public String fileUrl;
    public String fileName;
    public String fileMime;
    public String description;
}
