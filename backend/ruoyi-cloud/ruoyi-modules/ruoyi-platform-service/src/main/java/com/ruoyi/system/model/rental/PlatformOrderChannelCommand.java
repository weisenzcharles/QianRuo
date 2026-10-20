package com.ruoyi.system.model.rental;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.ibatis.type.Alias;

/** 平台订单渠道外部接口写入命令。 */
@Alias("PlatformRentalOrderChannelCommand")
public class PlatformOrderChannelCommand
{
    private Long channelId;

    @NotBlank(message = "渠道名称不能为空")
    @Size(max = 128, message = "渠道名称长度不能超过128个字符")
    private String channelName;

    private Integer channelType;
    private Integer status;
    private Integer sortNo;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String reason;

    /** @return 渠道唯一编号 */
    public Long getChannelId() { return channelId; }
    /** @param channelId 渠道唯一编号 */
    public void setChannelId(Long channelId) { this.channelId = channelId; }
    /** @return 渠道名称 */
    public String getChannelName() { return channelName; }
    /** @param channelName 渠道名称 */
    public void setChannelName(String channelName) { this.channelName = channelName; }
    /** @return 渠道类型 */
    public Integer getChannelType() { return channelType; }
    /** @param channelType 渠道类型 */
    public void setChannelType(Integer channelType) { this.channelType = channelType; }
    /** @return 状态 */
    public Integer getStatus() { return status; }
    /** @param status 状态 */
    public void setStatus(Integer status) { this.status = status; }
    /** @return 排序号 */
    public Integer getSortNo() { return sortNo; }
    /** @param sortNo 排序号 */
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    /** @return 备注 */
    public String getReason() { return reason; }
    /** @param reason 备注 */
    public void setReason(String reason) { this.reason = reason; }
}
