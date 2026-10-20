package com.ruoyi.merchant.domain;

/** 商户店铺偏好设置持久化记录。 */
public class MerchantStoreSettingRecord
{
    private Long settingId;
    private String settingKey;
    private String settingValueJson;
    private Integer version;

    public Long getSettingId()
    {
        return settingId;
    }

    public void setSettingId(Long settingId)
    {
        this.settingId = settingId;
    }

    public String getSettingKey()
    {
        return settingKey;
    }

    public void setSettingKey(String settingKey)
    {
        this.settingKey = settingKey;
    }

    public String getSettingValueJson()
    {
        return settingValueJson;
    }

    public void setSettingValueJson(String settingValueJson)
    {
        this.settingValueJson = settingValueJson;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }
}
