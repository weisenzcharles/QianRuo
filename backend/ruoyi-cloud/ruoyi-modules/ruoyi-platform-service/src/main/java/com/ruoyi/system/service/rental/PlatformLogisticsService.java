package com.ruoyi.system.service.rental;

import org.springframework.stereotype.Service;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.system.api.model.PlatformLogisticsDetailView;
import com.ruoyi.system.api.model.PlatformLogisticsPageView;

/** 平台物流治理编排服务。 */
@Service
public class PlatformLogisticsService
{
    private final RentalBusinessGateway rentalBusinessGateway;

    public PlatformLogisticsService(RentalBusinessGateway rentalBusinessGateway)
    {
        this.rentalBusinessGateway = rentalBusinessGateway;
    }

    /** 分页查询跨商户物流台账。 */
    public TableDataInfo list(String keyword, String merchantKeyword, Integer direction, Integer routeStatus,
            Boolean abnormalOnly, Integer pageNum, Integer pageSize)
    {
        PlatformLogisticsPageView page = rentalBusinessGateway.listPlatformLogistics(keyword, merchantKeyword,
                direction, routeStatus, abnormalOnly, pageNum, pageSize);
        return new TableDataInfo(page.getRows(), page.getTotal());
    }

    /** 查询物流详情和已缓存路线。 */
    public PlatformLogisticsDetailView detail(Long logisticsId)
    {
        return rentalBusinessGateway.getPlatformLogisticsDetail(logisticsId);
    }

    /** 刷新未签收物流路线，复用统一六小时刷新规则。 */
    public int refresh()
    {
        return rentalBusinessGateway.refreshLogisticsRoutes();
    }

    /** 手工刷新平台选中的单条物流路线。 */
    public int refresh(Long logisticsId)
    {
        return rentalBusinessGateway.refreshPlatformLogistics(logisticsId);
    }
}
