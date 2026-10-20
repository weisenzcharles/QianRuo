package com.ruoyi.system.model.rental;

/** 平台订单渠道外部接口展示对象。 */
public class PlatformOrderChannelVO
{
    private Long channelId;
    private String channelCode;
    private String channelName;
    private Integer channelType;
    private Integer scopeType;
    private Integer status;
    private Integer sortNo;

    /** @return 渠道唯一编号 */
    public Long getChannelId() { return channelId; }
    /** @param channelId 渠道唯一编号 */
    public void setChannelId(Long channelId) { this.channelId = channelId; }
    /** @return 渠道编码 */
    public String getChannelCode() { return channelCode; }
    /** @param channelCode 渠道编码 */
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }
    /** @return 渠道名称 */
    public String getChannelName() { return channelName; }
    /** @param channelName 渠道名称 */
    public void setChannelName(String channelName) { this.channelName = channelName; }
    /** @return 渠道类型 */
    public Integer getChannelType() { return channelType; }
    /** @param channelType 渠道类型 */
    public void setChannelType(Integer channelType) { this.channelType = channelType; }
    /** @return 归属范围 */
    public Integer getScopeType() { return scopeType; }
    /** @param scopeType 归属范围 */
    public void setScopeType(Integer scopeType) { this.scopeType = scopeType; }
    /** @return 状态 */
    public Integer getStatus() { return status; }
    /** @param status 状态 */
    public void setStatus(Integer status) { this.status = status; }
    /** @return 排序号 */
    public Integer getSortNo() { return sortNo; }
    /** @param sortNo 排序号 */
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
}
