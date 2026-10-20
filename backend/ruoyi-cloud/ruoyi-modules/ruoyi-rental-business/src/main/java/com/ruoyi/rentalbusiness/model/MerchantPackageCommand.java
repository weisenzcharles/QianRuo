package com.ruoyi.rentalbusiness.model;

import java.util.ArrayList;
import java.util.List;

/** 新架构商户套餐复制命令。 */
public class MerchantPackageCommand
{
    public Long id;
    public Long platformPackageId;
    public Long classId;
    public Long modelId;
    public String packageName;
    public Integer sortNo;
    public Integer status;
    /** 商户套餐自定义标识；服务端始终按商户套餐写入。 */
    public Integer customFlag;
    public String remark;
    public List<MerchantPackageItemCommand> items = new ArrayList<MerchantPackageItemCommand>();
}
