package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 商户提交的单项认证材料。文件内容由统一文件服务上传后只提交文件地址。 */
public class MerchantCertificationMaterialCommand implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Integer materialType;
    private String fileUrl;

    /** @return 材料类型编码。 */
    public Integer getMaterialType()
    {
        return materialType;
    }

    /** @param materialType 设置材料类型编码。 */
    public void setMaterialType(Integer materialType)
    {
        this.materialType = materialType;
    }

    /** @return 文件访问地址。 */
    public String getFileUrl()
    {
        return fileUrl;
    }

    /** @param fileUrl 设置文件访问地址。 */
    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }
}
