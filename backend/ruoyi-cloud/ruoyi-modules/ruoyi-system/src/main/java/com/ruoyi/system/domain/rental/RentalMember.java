package com.ruoyi.system.domain.rental;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.annotation.Excel.ColumnType;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 租赁商户/会员主体 ry_rental_member
 *
 * @author qianruo
 */
public class RentalMember extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 商户/会员ID */
    @Excel(name = "商户ID", cellType = ColumnType.NUMERIC)
    private Long memberId;

    /** 租户ID */
    @Excel(name = "租户ID", cellType = ColumnType.NUMERIC)
    private Long tenantId;

    /** 商户编码 */
    @Excel(name = "商户编码")
    private String memberCode;

    /** 商户类型：personal个人 enterprise企业 */
    @Excel(name = "商户类型", readConverterExp = "personal=个人,enterprise=企业")
    private String memberType;

    /** 认证状态 */
    @Excel(name = "认证状态", readConverterExp = "unverified=未认证,pending=待审核,verified=已认证,rejected=已驳回")
    private String authStatus;

    /** 主体名称 */
    @Excel(name = "主体名称")
    private String legalSubjectName;

    /** 营业执照/主体编号 */
    @Excel(name = "主体编号")
    private String licenseNo;

    /** 商户昵称 */
    @Excel(name = "商户昵称")
    private String nickname;

    /** 联系人 */
    @Excel(name = "联系人")
    private String contactName;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 微信二维码文件ID */
    private Long wechatQrFileId;

    /** 会员等级 */
    @Excel(name = "会员等级")
    private String vipLevel;

    /** 存储额度MB */
    @Excel(name = "存储额度MB", cellType = ColumnType.NUMERIC)
    private Long storageQuotaMb;

    /** 是否允许库存对外接单 */
    @Excel(name = "允许对外接单", readConverterExp = "0=否,1=是")
    private Integer externalAcceptEnabled;

    /** 状态：0正常 1停用 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志：0存在 2删除 */
    private String delFlag;

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    @NotNull(message = "租户ID不能为空")
    public Long getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }

    @NotBlank(message = "商户编码不能为空")
    @Size(min = 0, max = 64, message = "商户编码长度不能超过64个字符")
    public String getMemberCode()
    {
        return memberCode;
    }

    public void setMemberCode(String memberCode)
    {
        this.memberCode = memberCode;
    }

    @NotBlank(message = "商户类型不能为空")
    public String getMemberType()
    {
        return memberType;
    }

    public void setMemberType(String memberType)
    {
        this.memberType = memberType;
    }

    @NotBlank(message = "认证状态不能为空")
    public String getAuthStatus()
    {
        return authStatus;
    }

    public void setAuthStatus(String authStatus)
    {
        this.authStatus = authStatus;
    }

    @Size(min = 0, max = 128, message = "主体名称长度不能超过128个字符")
    public String getLegalSubjectName()
    {
        return legalSubjectName;
    }

    public void setLegalSubjectName(String legalSubjectName)
    {
        this.legalSubjectName = legalSubjectName;
    }

    @Size(min = 0, max = 64, message = "主体编号长度不能超过64个字符")
    public String getLicenseNo()
    {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo)
    {
        this.licenseNo = licenseNo;
    }

    @Size(min = 0, max = 64, message = "商户昵称长度不能超过64个字符")
    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    @Size(min = 0, max = 64, message = "联系人长度不能超过64个字符")
    public String getContactName()
    {
        return contactName;
    }

    public void setContactName(String contactName)
    {
        this.contactName = contactName;
    }

    @Size(min = 0, max = 32, message = "联系电话长度不能超过32个字符")
    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    public Long getWechatQrFileId()
    {
        return wechatQrFileId;
    }

    public void setWechatQrFileId(Long wechatQrFileId)
    {
        this.wechatQrFileId = wechatQrFileId;
    }

    @NotBlank(message = "会员等级不能为空")
    public String getVipLevel()
    {
        return vipLevel;
    }

    public void setVipLevel(String vipLevel)
    {
        this.vipLevel = vipLevel;
    }

    @NotNull(message = "存储额度不能为空")
    public Long getStorageQuotaMb()
    {
        return storageQuotaMb;
    }

    public void setStorageQuotaMb(Long storageQuotaMb)
    {
        this.storageQuotaMb = storageQuotaMb;
    }

    @NotNull(message = "对外接单状态不能为空")
    public Integer getExternalAcceptEnabled()
    {
        return externalAcceptEnabled;
    }

    public void setExternalAcceptEnabled(Integer externalAcceptEnabled)
    {
        this.externalAcceptEnabled = externalAcceptEnabled;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("memberId", getMemberId())
            .append("tenantId", getTenantId())
            .append("memberCode", getMemberCode())
            .append("memberType", getMemberType())
            .append("authStatus", getAuthStatus())
            .append("legalSubjectName", getLegalSubjectName())
            .append("licenseNo", getLicenseNo())
            .append("nickname", getNickname())
            .append("contactName", getContactName())
            .append("contactPhone", getContactPhone())
            .append("wechatQrFileId", getWechatQrFileId())
            .append("vipLevel", getVipLevel())
            .append("storageQuotaMb", getStorageQuotaMb())
            .append("externalAcceptEnabled", getExternalAcceptEnabled())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
