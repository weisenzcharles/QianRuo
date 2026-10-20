package com.ruoyi.merchant.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.merchant.domain.MerchantStoreSettingRecord;
import com.ruoyi.merchant.model.MerchantStoreProfileVO;

/** 商户店铺设置数据访问。所有查询都以登录上下文中的租户和商户范围为准。 */
public interface MerchantStoreSettingsMapper
{
    /** 查询当前商户基础资料和只读认证摘要。 */
    MerchantStoreProfileVO selectStoreProfile(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId);

    /** 修改当前商户可由店铺管理员维护的基础资料。 */
    int updateStoreProfile(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("nickname") String nickname, @Param("contactName") String contactName,
            @Param("servicePhone") String servicePhone, @Param("contactEmail") String contactEmail,
            @Param("alipayRealName") String alipayRealName, @Param("alipayAccount") String alipayAccount,
            @Param("storeDescription") String storeDescription,
            @Param("externalAcceptEnabled") Integer externalAcceptEnabled,
            @Param("operator") Long operator);

    /** 修改店铺 Logo 地址。文件上传成功后立即落库。 */
    int updateStoreLogo(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("storeLogoUrl") String storeLogoUrl, @Param("operator") Long operator);

    /** 查询商户偏好设置记录。 */
    MerchantStoreSettingRecord selectSetting(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("settingKey") String settingKey);

    /** 插入或更新商户偏好设置。 */
    int upsertSetting(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("settingKey") String settingKey, @Param("settingValueJson") String settingValueJson,
            @Param("operator") Long operator);
}
