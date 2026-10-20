package com.ruoyi.rentalbusiness.model;

import java.util.List;

/** 协议版本和确认状态展示对象。 */
public class ProtocolVO
{
    public Long protocolId;
    public String protocolKey;
    public Integer versionNo;
    public String protocolTitle;
    public String protocolContent;
    public String contentHash;
    public Integer protocolStatus;
    public String effectiveTime;
    public List<Integer> scopeTypes;
    public List<String> scopeLabels;
    public Boolean accepted;
    public String acceptedTime;
}
