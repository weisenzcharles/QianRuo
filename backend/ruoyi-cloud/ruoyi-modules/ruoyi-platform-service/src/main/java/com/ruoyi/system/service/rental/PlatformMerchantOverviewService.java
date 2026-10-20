package com.ruoyi.system.service.rental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.system.api.model.PlatformMerchantOverviewStat;
import com.ruoyi.system.api.model.PlatformMerchantBusinessView;
import com.ruoyi.system.api.model.PlatformMerchantGovernanceView;
import com.ruoyi.system.mapper.rental.RentalMemberMapper;
import com.ruoyi.system.model.rental.PlatformMerchantBusinessScope;
import com.ruoyi.system.model.rental.PlatformMerchantOverviewBase;
import com.ruoyi.system.model.rental.PlatformMerchantOverviewVO;

/** 平台商户经营概况编排服务。 */
@Service
public class PlatformMerchantOverviewService
{
    private final RentalMemberMapper memberMapper;
    private final RentalBusinessGateway rentalBusinessGateway;

    public PlatformMerchantOverviewService(RentalMemberMapper memberMapper,
            RentalBusinessGateway rentalBusinessGateway)
    {
        this.memberMapper = memberMapper;
        this.rentalBusinessGateway = rentalBusinessGateway;
    }

    /** 按商户筛选条件分页查询基础资料，并聚合对应租赁业务统计。 */
    public TableDataInfo list(String memberCode, String nickname, String legalSubjectName, Integer memberType,
            Integer authStatus, Integer status, Integer pageNum, Integer pageSize)
    {
        int normalizedPageNum = pageNum == null || pageNum.intValue() < 1 ? 1 : pageNum.intValue();
        int normalizedPageSize = pageSize == null || pageSize.intValue() < 1 ? 10
                : Math.min(pageSize.intValue(), 100);
        String normalizedMemberCode = normalize(memberCode);
        String normalizedNickname = normalize(nickname);
        String normalizedLegalSubjectName = normalize(legalSubjectName);

        List<PlatformMerchantOverviewBase> bases;
        PageHelper.startPage(normalizedPageNum, normalizedPageSize);
        try
        {
            bases = memberMapper.selectRentalMemberOverviewPage(normalizedMemberCode, normalizedNickname,
                    normalizedLegalSubjectName, memberType, authStatus, status);
        }
        finally
        {
            PageHelper.clearPage();
        }
        long total = new PageInfo<PlatformMerchantOverviewBase>(bases).getTotal();
        if (bases.isEmpty())
        {
            return new TableDataInfo(new ArrayList<PlatformMerchantOverviewVO>(), total);
        }

        List<Long> memberIds = new ArrayList<Long>(bases.size());
        for (PlatformMerchantOverviewBase base : bases)
        {
            memberIds.add(base.getMemberId());
        }
        Map<Long, PlatformMerchantOverviewStat> statsByMember = new HashMap<Long, PlatformMerchantOverviewStat>();
        for (PlatformMerchantOverviewStat stat : rentalBusinessGateway.listPlatformMerchantOverviewStats(memberIds))
        {
            if (stat != null && stat.getMemberId() != null)
            {
                statsByMember.put(stat.getMemberId(), stat);
            }
        }

        List<PlatformMerchantOverviewVO> rows = new ArrayList<PlatformMerchantOverviewVO>(bases.size());
        for (PlatformMerchantOverviewBase base : bases)
        {
            PlatformMerchantOverviewVO row = new PlatformMerchantOverviewVO();
            row.setMerchantName(text(base.getNickname(), text(base.getLegalSubjectName(), "未命名商户")));
            row.setLegalSubjectName(text(base.getLegalSubjectName(), "-"));
            row.setMemberCode(text(base.getMemberCode(), "-"));
            row.setAuthStatus(base.getAuthStatus());
            row.setStatus(base.getStatus());
            row.setCreateTime(base.getCreateTime());
            applyStat(row, statsByMember.get(base.getMemberId()));
            rows.add(row);
        }
        return new TableDataInfo(rows, total);
    }

    /**
     * 按商户编码查询商户经营明细，编码只作为平台浏览器接口的业务定位符。
     *
     * @param memberCode 商户业务编码
     * @return 商户经营明细
     */
    public PlatformMerchantBusinessView getBusiness(String memberCode)
    {
        String normalizedCode = memberCode == null ? null : memberCode.trim();
        if (normalizedCode == null || normalizedCode.isEmpty())
        {
            throw new com.ruoyi.common.core.exception.ServiceException("商户编码不能为空");
        }
        PlatformMerchantBusinessScope scope = memberMapper.selectRentalMemberBusinessScopeByCode(normalizedCode);
        if (scope == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("商户不存在或已删除");
        }
        PlatformMerchantBusinessView result = rentalBusinessGateway.getPlatformMerchantBusiness(scope.getTenantId(),
                scope.getMemberId());
        result.setMerchantName(text(scope.getMerchantName(), "未命名商户"));
        result.setMemberCode(text(scope.getMemberCode(), normalizedCode));
        return result;
    }

    /** 按商户编码查询履约、物流和风险治理汇总。 */
    public PlatformMerchantGovernanceView getGovernance(String memberCode)
    {
        PlatformMerchantBusinessScope scope = resolveScope(memberCode);
        return rentalBusinessGateway.getPlatformMerchantGovernance(scope.getTenantId(), scope.getMemberId());
    }

    private PlatformMerchantBusinessScope resolveScope(String memberCode)
    {
        String normalizedCode = memberCode == null ? null : memberCode.trim();
        if (normalizedCode == null || normalizedCode.isEmpty())
        {
            throw new com.ruoyi.common.core.exception.ServiceException("商户编码不能为空");
        }
        PlatformMerchantBusinessScope scope = memberMapper.selectRentalMemberBusinessScopeByCode(normalizedCode);
        if (scope == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("商户不存在或已删除");
        }
        return scope;
    }

    /** 将跨服务统计复制到平台端展示对象，保持浏览器接口不携带内部商户编号。 */
    private void applyStat(PlatformMerchantOverviewVO row, PlatformMerchantOverviewStat stat)
    {
        if (stat == null)
        {
            return;
        }
        row.setWarehouseCount(stat.getWarehouseCount());
        row.setDeviceProductCount(stat.getDeviceProductCount());
        row.setAccessoryCount(stat.getAccessoryCount());
        row.setPackageCount(stat.getPackageCount());
        row.setTotalInventoryQuantity(stat.getTotalInventoryQuantity());
        row.setAvailableInventoryQuantity(stat.getAvailableInventoryQuantity());
        row.setReservedInventoryQuantity(stat.getReservedInventoryQuantity());
        row.setRentedInventoryQuantity(stat.getRentedInventoryQuantity());
        row.setRepairingInventoryQuantity(stat.getRepairingInventoryQuantity());
        row.setDamagedInventoryQuantity(stat.getDamagedInventoryQuantity());
        row.setPendingOrderCount(stat.getPendingOrderCount());
        row.setActiveOrderCount(stat.getActiveOrderCount());
        row.setCompletedOrderCount(stat.getCompletedOrderCount());
        row.setOverdueOrderCount(stat.getOverdueOrderCount());
    }

    private String text(String value, String defaultValue)
    {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    private String normalize(String value)
    {
        return value == null ? null : value.trim();
    }
}
