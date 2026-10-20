package com.ruoyi.rentalbusiness.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.rentalbusiness.mapper.PlatformMerchantBusinessMapper;
import com.ruoyi.system.api.model.PlatformMerchantBusinessView;
import com.ruoyi.system.api.model.PlatformMerchantInventoryView;
import com.ruoyi.system.api.model.PlatformMerchantGovernanceView;
import com.ruoyi.system.api.model.PlatformMerchantPackageView;
import com.ruoyi.system.api.model.PlatformMerchantProductView;
import com.ruoyi.system.api.model.PlatformMerchantWarehouseView;

/** 验证平台商户经营明细聚合查询的范围校验和空集合处理。 */
public class PlatformMerchantBusinessQueryServiceTest
{
    /** 非法租户或商户范围必须在访问数据库前被拒绝。 */
    @Test(expected = ServiceException.class)
    public void get_invalidScope_rejectsBeforeQuery()
    {
        CapturingMapper mapper = new CapturingMapper();
        new PlatformMerchantBusinessQueryService(mapper).get(Long.valueOf(0L), Long.valueOf(1L));
        Assert.assertFalse(mapper.called);
    }

    /** 四类明细均通过同一可信范围查询，并将空结果稳定转换为空集合。 */
    @Test
    public void get_validScope_returnsAllBusinessSections()
    {
        CapturingMapper mapper = new CapturingMapper();
        PlatformMerchantWarehouseView warehouse = new PlatformMerchantWarehouseView();
        PlatformMerchantProductView product = new PlatformMerchantProductView();
        PlatformMerchantPackageView packageView = new PlatformMerchantPackageView();
        PlatformMerchantInventoryView inventory = new PlatformMerchantInventoryView();
        mapper.warehouses = Collections.singletonList(warehouse);
        mapper.products = Collections.singletonList(product);
        mapper.packages = Collections.singletonList(packageView);
        mapper.inventories = Collections.singletonList(inventory);

        PlatformMerchantBusinessView result = new PlatformMerchantBusinessQueryService(mapper)
                .get(Long.valueOf(2L), Long.valueOf(3L));

        Assert.assertEquals(1, result.getWarehouses().size());
        Assert.assertEquals(1, result.getProducts().size());
        Assert.assertEquals(1, result.getPackages().size());
        Assert.assertEquals(1, result.getInventories().size());
        Assert.assertEquals(Arrays.asList(Integer.valueOf(200), Integer.valueOf(200), Integer.valueOf(200),
                Integer.valueOf(200)), mapper.limits);
    }

    /** 治理汇总使用同一租户和商户范围，并在无数据时返回稳定的零值对象。 */
    @Test
    public void getGovernance_validScope_returnsScopedSummary()
    {
        CapturingMapper mapper = new CapturingMapper();
        PlatformMerchantGovernanceView governance = new PlatformMerchantGovernanceView();
        governance.setOpenAfterSalesCaseCount(2L);
        mapper.governance = governance;

        PlatformMerchantGovernanceView result = new PlatformMerchantBusinessQueryService(mapper)
                .getGovernance(Long.valueOf(2L), Long.valueOf(3L));

        Assert.assertSame(governance, result);
        Assert.assertEquals(Long.valueOf(2L), mapper.governanceTenantId);
        Assert.assertEquals(Long.valueOf(3L), mapper.governanceMemberId);
    }

    private static final class CapturingMapper implements PlatformMerchantBusinessMapper
    {
        private boolean called;
        private List<Integer> limits = new java.util.ArrayList<Integer>();
        private List<PlatformMerchantWarehouseView> warehouses = Collections.emptyList();
        private List<PlatformMerchantProductView> products = Collections.emptyList();
        private List<PlatformMerchantPackageView> packages = Collections.emptyList();
        private List<PlatformMerchantInventoryView> inventories = Collections.emptyList();
        private PlatformMerchantGovernanceView governance;
        private Long governanceTenantId;
        private Long governanceMemberId;

        @Override
        public List<PlatformMerchantWarehouseView> selectWarehouses(Long tenantId, Long memberId, int limit)
        {
            called = true;
            limits.add(Integer.valueOf(limit));
            return warehouses;
        }

        @Override
        public List<PlatformMerchantProductView> selectProducts(Long tenantId, Long memberId, int limit)
        {
            limits.add(Integer.valueOf(limit));
            return products;
        }

        @Override
        public List<PlatformMerchantPackageView> selectPackages(Long tenantId, Long memberId, int limit)
        {
            limits.add(Integer.valueOf(limit));
            return packages;
        }

        @Override
        public List<PlatformMerchantInventoryView> selectInventories(Long tenantId, Long memberId, int limit)
        {
            limits.add(Integer.valueOf(limit));
            return inventories;
        }

        @Override
        public PlatformMerchantGovernanceView selectGovernance(Long tenantId, Long memberId)
        {
            governanceTenantId = tenantId;
            governanceMemberId = memberId;
            return governance;
        }
    }
}
