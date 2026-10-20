package com.ruoyi.rentalbusiness.model;

import java.util.List;

/** 平台协议版本新增、修改和发布命令。 */
public class ProtocolCommand
{
    public Long protocolId;
    public String protocolKey;
    public String protocolTitle;
    public String protocolContent;
    public Integer protocolStatus;
    public List<Integer> scopeTypes;
    public String remark;
}
