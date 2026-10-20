package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.List;

/** 商户补交认证材料的内部服务请求。商户编号和操作人由商户服务从登录上下文补齐。 */
public class MerchantCertificationSupplementCommand implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long memberId;
    private Long operatorId;
    private String operatorName;
    private List<MerchantCertificationMaterialCommand> materials;

    /** @return 商户唯一编号。 */
    public Long getMemberId()
    {
        return memberId;
    }

    /** @param memberId 设置商户唯一编号。 */
    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    /** @return 商户用户唯一编号。 */
    public Long getOperatorId()
    {
        return operatorId;
    }

    /** @param operatorId 设置商户用户唯一编号。 */
    public void setOperatorId(Long operatorId)
    {
        this.operatorId = operatorId;
    }

    /** @return 商户用户名称。 */
    public String getOperatorName()
    {
        return operatorName;
    }

    /** @param operatorName 设置商户用户名称。 */
    public void setOperatorName(String operatorName)
    {
        this.operatorName = operatorName;
    }

    /** @return 补交材料列表。 */
    public List<MerchantCertificationMaterialCommand> getMaterials()
    {
        return materials;
    }

    /** @param materials 设置补交材料列表。 */
    public void setMaterials(List<MerchantCertificationMaterialCommand> materials)
    {
        this.materials = materials;
    }
}
