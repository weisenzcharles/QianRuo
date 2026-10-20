package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.List;

/** 平台维护协议草稿的内部调用命令。 */
public class PlatformProtocolCommand implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long protocolId;
    private String protocolKey;
    private String protocolTitle;
    private String protocolContent;
    private List<Integer> scopeTypes;
    private String remark;

    /** @return 协议版本唯一编号 */
    public Long getProtocolId() { return protocolId; }
    /** @param protocolId 协议版本唯一编号 */
    public void setProtocolId(Long protocolId) { this.protocolId = protocolId; }
    /** @return 协议业务键 */
    public String getProtocolKey() { return protocolKey; }
    /** @param protocolKey 协议业务键 */
    public void setProtocolKey(String protocolKey) { this.protocolKey = protocolKey; }
    /** @return 协议标题 */
    public String getProtocolTitle() { return protocolTitle; }
    /** @param protocolTitle 协议标题 */
    public void setProtocolTitle(String protocolTitle) { this.protocolTitle = protocolTitle; }
    /** @return 协议正文 */
    public String getProtocolContent() { return protocolContent; }
    /** @param protocolContent 协议正文 */
    public void setProtocolContent(String protocolContent) { this.protocolContent = protocolContent; }
    /** @return 协议适用入口编码列表 */
    public List<Integer> getScopeTypes() { return scopeTypes; }
    /** @param scopeTypes 协议适用入口编码列表 */
    public void setScopeTypes(List<Integer> scopeTypes) { this.scopeTypes = scopeTypes; }
    /** @return 备注 */
    public String getRemark() { return remark; }
    /** @param remark 备注 */
    public void setRemark(String remark) { this.remark = remark; }
}
