package com.ruoyi.rentalbusiness.model;

import java.util.ArrayList;
import java.util.List;

/** 套餐库存可组装性校验结果。 */
public class RentalInventoryCheckVO
{
    public Long packageId;
    public Long warehouseId;
    public Integer quantity;
    public Boolean canReserve;
    public List<RentalInventoryCheckItemVO> items = new ArrayList<RentalInventoryCheckItemVO>();
}
