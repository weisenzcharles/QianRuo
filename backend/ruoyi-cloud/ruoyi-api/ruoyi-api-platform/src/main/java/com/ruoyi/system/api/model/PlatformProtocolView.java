package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.List;

/** 平台协议版本内部返回对象。 */
public class PlatformProtocolView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long protocolId;
    private String protocolKey;
    private Integer versionNo;
    private String protocolTitle;
    private String protocolContent;
    private String contentHash;
    private Integer protocolStatus;
    private String effectiveTime;
    private List<Integer> scopeTypes;
    private List<String> scopeLabels;

    /** @return 协议版本唯一编号 */
    public Long getProtocolId() { return protocolId; }
    /** @param protocolId 协议版本唯一编号 */
    public void setProtocolId(Long protocolId) { this.protocolId = protocolId; }
    /** @return 协议业务键 */
    public String getProtocolKey() { return protocolKey; }
    /** @param protocolKey 协议业务键 */
    public void setProtocolKey(String protocolKey) { this.protocolKey = protocolKey; }
    /** @return 版本号 */
    public Integer getVersionNo() { return versionNo; }
    /** @param versionNo 版本号 */
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    /** @return 协议标题 */
    public String getProtocolTitle() { return protocolTitle; }
    /** @param protocolTitle 协议标题 */
    public void setProtocolTitle(String protocolTitle) { this.protocolTitle = protocolTitle; }
    /** @return 协议正文 */
    public String getProtocolContent() { return protocolContent; }
    /** @param protocolContent 协议正文 */
    public void setProtocolContent(String protocolContent) { this.protocolContent = protocolContent; }
    /** @return 正文哈希 */
    public String getContentHash() { return contentHash; }
    /** @param contentHash 正文哈希 */
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    /** @return 协议状态 */
    public Integer getProtocolStatus() { return protocolStatus; }
    /** @param protocolStatus 协议状态 */
    public void setProtocolStatus(Integer protocolStatus) { this.protocolStatus = protocolStatus; }
    /** @return 生效时间 */
    public String getEffectiveTime() { return effectiveTime; }
    /** @param effectiveTime 生效时间 */
    public void setEffectiveTime(String effectiveTime) { this.effectiveTime = effectiveTime; }
    /** @return 协议适用入口编码列表 */
    public List<Integer> getScopeTypes() { return scopeTypes; }
    /** @param scopeTypes 协议适用入口编码列表 */
    public void setScopeTypes(List<Integer> scopeTypes) { this.scopeTypes = scopeTypes; }
    /** @return 协议适用入口中文名称 */
    public List<String> getScopeLabels() { return scopeLabels; }
    /** @param scopeLabels 协议适用入口中文名称 */
    public void setScopeLabels(List<String> scopeLabels) { this.scopeLabels = scopeLabels; }
}
