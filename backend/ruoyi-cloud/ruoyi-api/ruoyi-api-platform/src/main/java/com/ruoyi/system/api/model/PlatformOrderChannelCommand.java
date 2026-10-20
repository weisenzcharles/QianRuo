package com.ruoyi.system.api.model;

import java.io.Serializable;

/**
 * 平台维护联盟订单渠道的内部调用命令。
 *
 * <p>该命令只包含租赁业务服务写入所需字段，不携带租户或商户归属字段；平台渠道固定归属
 * 平台范围，由租赁业务服务根据接口边界写入。</p>
 */
public class PlatformOrderChannelCommand implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 待修改的平台渠道唯一编号。 */
    private Long channelId;

    /** 渠道名称。 */
    private String channelName;

    /** 渠道类型，平台端必须为 alliance。 */
    private Integer channelType;

    /** 状态：0 启用，1 停用。 */
    private Integer status;

    /** 排序号。 */
    private Integer sortNo;

    /** 操作原因或备注。 */
    private String reason;

    /** @return 平台渠道唯一编号 */
    public Long getChannelId()
    {
        return channelId;
    }

    /** @param channelId 平台渠道唯一编号 */
    public void setChannelId(Long channelId)
    {
        this.channelId = channelId;
    }

    /** @return 渠道名称 */
    public String getChannelName()
    {
        return channelName;
    }

    /** @param channelName 渠道名称 */
    public void setChannelName(String channelName)
    {
        this.channelName = channelName;
    }

    /** @return 渠道类型 */
    public Integer getChannelType()
    {
        return channelType;
    }

    /** @param channelType 渠道类型 */
    public void setChannelType(Integer channelType)
    {
        this.channelType = channelType;
    }

    /** @return 状态 */
    public Integer getStatus()
    {
        return status;
    }

    /** @param status 状态 */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /** @return 排序号 */
    public Integer getSortNo()
    {
        return sortNo;
    }

    /** @param sortNo 排序号 */
    public void setSortNo(Integer sortNo)
    {
        this.sortNo = sortNo;
    }

    /** @return 操作原因 */
    public String getReason()
    {
        return reason;
    }

    /** @param reason 操作原因 */
    public void setReason(String reason)
    {
        this.reason = reason;
    }
}
