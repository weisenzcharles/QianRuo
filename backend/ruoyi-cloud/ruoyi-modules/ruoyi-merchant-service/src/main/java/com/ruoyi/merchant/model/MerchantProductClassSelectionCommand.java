package com.ruoyi.merchant.model;

import java.util.ArrayList;
import java.util.List;

/** 商户经营类目保存命令。 */
public class MerchantProductClassSelectionCommand
{
    /** 商户选择的平台类目主键集合。 */
    private List<Long> classIds = new ArrayList<Long>();

    /**
     * 获取商户选择的平台类目主键集合。
     *
     * @return 类目主键集合
     */
    public List<Long> getClassIds()
    {
        return classIds;
    }

    /**
     * 设置商户选择的平台类目主键集合。
     *
     * @param classIds 类目主键集合
     */
    public void setClassIds(List<Long> classIds)
    {
        this.classIds = classIds;
    }
}
