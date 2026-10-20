package com.ruoyi.system.model.rental;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/** 平台要求商户补交认证材料的命令。 */
public class RentalMemberSupplementCommand
{
    @NotEmpty(message = "请选择至少一项需要补交的材料")
    @Size(max = 5, message = "一次最多要求补交5项材料")
    private List<Integer> materialTypes;

    @NotBlank(message = "补件说明不能为空")
    @Size(max = 500, message = "补件说明长度不能超过500个字符")
    private String auditRemark;

    /** @return 需要补交的材料类型编码。 */
    public List<Integer> getMaterialTypes()
    {
        return materialTypes;
    }

    /** @param materialTypes 设置需要补交的材料类型编码。 */
    public void setMaterialTypes(List<Integer> materialTypes)
    {
        this.materialTypes = materialTypes;
    }

    /** @return 面向商户的补件说明。 */
    public String getAuditRemark()
    {
        return auditRemark;
    }

    /** @param auditRemark 设置面向商户的补件说明。 */
    public void setAuditRemark(String auditRemark)
    {
        this.auditRemark = auditRemark;
    }
}
