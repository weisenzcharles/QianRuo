package com.ruoyi.system.model.rental;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.ibatis.type.Alias;

/** 平台协议草稿外部接口写入命令。 */
@Alias("PlatformRentalProtocolCommand")
public class PlatformProtocolCommand
{
    private Long protocolId;

    @NotBlank(message = "协议键不能为空")
    @Size(max = 64, message = "协议键长度不能超过64个字符")
    private String protocolKey;

    @NotBlank(message = "协议标题不能为空")
    @Size(max = 128, message = "协议标题长度不能超过128个字符")
    private String protocolTitle;

    @NotBlank(message = "协议正文不能为空")
    private String protocolContent;

    private List<Integer> scopeTypes;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    /** @return 协议版本唯一编号 */
    public Long getProtocolId() { return protocolId; }
    /** @param protocolId 协议版本唯一编号 */
    public void setProtocolId(Long protocolId) { this.protocolId = protocolId; }
    /** @return 协议键 */
    public String getProtocolKey() { return protocolKey; }
    /** @param protocolKey 协议键 */
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
