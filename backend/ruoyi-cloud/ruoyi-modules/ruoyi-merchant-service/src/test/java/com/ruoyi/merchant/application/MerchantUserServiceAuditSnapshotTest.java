package com.ruoyi.merchant.application;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.merchant.domain.MerchantWarehouse;
import com.ruoyi.merchant.model.MerchantUserVO;

/** 商户用户操作日志展示快照测试。 */
public class MerchantUserServiceAuditSnapshotTest
{
    @Test
    public void auditDisplaySnapshot_指定角色和仓库_展示名称并脱敏联系方式()
    {
        MerchantUserVO user = new MerchantUserVO();
        user.setUserName("warehouse-operator");
        user.setNickName("仓库专员");
        user.setPhone("13800138000");
        user.setEmail("operator@example.invalid");
        user.setStatus(RentalCode.EnableStatus.ENABLED.getCode());
        user.setRoleNames("仓库管理员");
        user.setMemberRole("staff");
        user.setWarehouseScopeType(RentalCode.WarehouseScope.SPECIFIED.getCode());
        user.setWarehouseIds(new Long[] { 10L });

        MerchantWarehouse warehouse = new MerchantWarehouse();
        warehouse.setWarehouseId(10L);
        warehouse.setWarehouseName("北京仓");
        MerchantWarehouse otherWarehouse = new MerchantWarehouse();
        otherWarehouse.setWarehouseId(11L);
        otherWarehouse.setWarehouseName("杭州仓");
        List<MerchantWarehouse> warehouses = Arrays.asList(warehouse, otherWarehouse);

        Map<String, Object> snapshot = MerchantUserService.auditDisplaySnapshot(user, warehouses);

        Assert.assertEquals("北京仓", snapshot.get("warehouseNames"));
        Assert.assertEquals("仓库管理员", snapshot.get("roleNames"));
        Assert.assertEquals("正常", snapshot.get("status"));
        Assert.assertEquals("138****8000", snapshot.get("phoneDisplay"));
        Assert.assertEquals("o***@example.invalid", snapshot.get("emailDisplay"));
        Assert.assertFalse(snapshot.containsKey("userId"));
        Assert.assertFalse(snapshot.containsKey("warehouseIds"));
        Assert.assertFalse(snapshot.containsValue("13800138000"));
        Assert.assertFalse(snapshot.containsValue("operator@example.invalid"));

        user.setWarehouseIds(new Long[] { 11L });
        Map<String, Object> updatedSnapshot = MerchantUserService.auditDisplaySnapshot(user, warehouses);
        Assert.assertEquals("杭州仓", updatedSnapshot.get("warehouseNames"));
        Assert.assertNotEquals(snapshot.get("warehouseNames"), updatedSnapshot.get("warehouseNames"));
    }
}
