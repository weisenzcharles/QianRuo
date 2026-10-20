package com.ruoyi.system.domain.rental;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.ruoyi.common.core.web.domain.BaseEntity;

/** 平台可配置的商户等级。 */
public class MerchantLevel extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long levelId;
    private String levelCode;
    private String levelName;
    private Integer levelOrder;
    private Integer monthlyOrderRequirement;
    private Integer inventoryRequirement;
    private Integer inviteLimit;
    private BigDecimal registrationFee;
    private Integer status;
    private Integer isDelete;

    /** @return 等级唯一编号 */
    public Long getLevelId() { return levelId; }
    /** @param levelId 等级唯一编号 */
    public void setLevelId(Long levelId) { this.levelId = levelId; }
    /** @return 稳定等级编码 */
    public String getLevelCode() { return levelCode; }
    /** @param levelCode 稳定等级编码 */
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    /** @return 平台可配置等级名称 */
    @NotBlank(message = "等级名称不能为空")
    @Size(max = 64, message = "等级名称不能超过64个字符")
    public String getLevelName() { return levelName; }
    /** @param levelName 等级名称 */
    public void setLevelName(String levelName) { this.levelName = levelName; }
    /** @return 等级排序 */
    @NotNull(message = "等级排序不能为空") @Min(value = 0, message = "等级排序不能小于0")
    public Integer getLevelOrder() { return levelOrder; }
    /** @param levelOrder 等级排序 */
    public void setLevelOrder(Integer levelOrder) { this.levelOrder = levelOrder; }
    /** @return 每月完成订单要求 */
    @NotNull(message = "月订单要求不能为空") @Min(value = 0, message = "月订单要求不能小于0")
    public Integer getMonthlyOrderRequirement() { return monthlyOrderRequirement; }
    /** @param monthlyOrderRequirement 每月完成订单要求 */
    public void setMonthlyOrderRequirement(Integer monthlyOrderRequirement) { this.monthlyOrderRequirement = monthlyOrderRequirement; }
    /** @return 库存数量要求 */
    @NotNull(message = "库存要求不能为空") @Min(value = 0, message = "库存要求不能小于0")
    public Integer getInventoryRequirement() { return inventoryRequirement; }
    /** @param inventoryRequirement 库存数量要求 */
    public void setInventoryRequirement(Integer inventoryRequirement) { this.inventoryRequirement = inventoryRequirement; }
    /** @return 可邀请人数上限 */
    @NotNull(message = "邀请人数不能为空") @Min(value = 0, message = "邀请人数不能小于0")
    public Integer getInviteLimit() { return inviteLimit; }
    /** @param inviteLimit 可邀请人数上限 */
    public void setInviteLimit(Integer inviteLimit) { this.inviteLimit = inviteLimit; }
    /** @return 付费注册金额 */
    @NotNull(message = "注册费用不能为空") @DecimalMin(value = "0.00", message = "注册费用不能小于0")
    public BigDecimal getRegistrationFee() { return registrationFee; }
    /** @param registrationFee 付费注册金额 */
    public void setRegistrationFee(BigDecimal registrationFee) { this.registrationFee = registrationFee; }
    /** @return 状态，0正常1停用 */
    public Integer getStatus() { return status; }
    /** @param status 状态 */
    public void setStatus(Integer status) { this.status = status; }
    /** @return 软删除标识 */
    public Integer getIsDelete() { return isDelete; }
    /** @param isDelete 软删除标识 */
    public void setIsDelete(Integer isDelete) { this.isDelete = isDelete; }
}
