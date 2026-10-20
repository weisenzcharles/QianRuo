package com.ruoyi.rentalbusiness.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ruoyi.common.core.enums.RentalCode;

/** 扫码入库来源。 */
public enum MerchantVerificationSource
{
    RENTAL_RETURN("rental-return", "租赁归还", RentalCode.InventoryStatus.RENTED.getCode()),
    ALLIANCE_RETURN("alliance-return", "联盟调货入库", RentalCode.InventoryStatus.RENTED.getCode()),
    REPAIR_RETURN("repair-return", "维修归还", RentalCode.InventoryStatus.REPAIRING.getCode()),
    NEW_STOCK("new-stock", "采购入库", null);

    private final String code;
    private final String label;
    private final Integer expectedStatus;

    MerchantVerificationSource(String code, String label, Integer expectedStatus)
    {
        this.code = code;
        this.label = label;
        this.expectedStatus = expectedStatus;
    }

    @JsonCreator
    public static MerchantVerificationSource fromCode(String code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantVerificationSource value : values())
        {
            if (value.code.equalsIgnoreCase(code.trim()))
            {
                return value;
            }
        }
        return null;
    }

    @JsonValue
    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    public Integer getExpectedStatus()
    {
        return expectedStatus;
    }
}
