package com.ruoyi.rentalbusiness.model;

import java.util.Date;

/** 售后证据视图，不返回内部主键。 */
public class MerchantAfterSalesEvidenceVO
{
    public Integer evidenceType;
    public String evidenceTypeLabel;
    public String fileUrl;
    public String fileName;
    public String fileMime;
    public String description;
    public String operatorName;
    public Date createTime;
}
