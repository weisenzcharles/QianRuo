package com.ruoyi.merchant.model;

/** 店铺设置页面聚合数据。 */
public class MerchantStoreSettingsPageVO
{
    private MerchantStoreProfileVO profile;
    private MerchantStoreSettingsVO settings;
    private Boolean canEdit;

    public MerchantStoreProfileVO getProfile() { return profile; }
    public void setProfile(MerchantStoreProfileVO profile) { this.profile = profile; }
    public MerchantStoreSettingsVO getSettings() { return settings; }
    public void setSettings(MerchantStoreSettingsVO settings) { this.settings = settings; }
    public Boolean getCanEdit() { return canEdit; }
    public void setCanEdit(Boolean canEdit) { this.canEdit = canEdit; }
}
