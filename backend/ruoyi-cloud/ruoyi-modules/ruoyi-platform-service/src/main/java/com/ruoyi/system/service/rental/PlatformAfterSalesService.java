package com.ruoyi.system.service.rental;

import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.model.PlatformAfterSalesDetailView;
import com.ruoyi.system.api.model.PlatformAfterSalesEventCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesEvidenceCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesPageView;

/** 平台售后治理编排服务，负责平台权限下的跨服务查询和处理。 */
@Service
public class PlatformAfterSalesService
{
    private final RentalBusinessGateway rentalBusinessGateway;

    public PlatformAfterSalesService(RentalBusinessGateway rentalBusinessGateway)
    {
        this.rentalBusinessGateway = rentalBusinessGateway;
    }

    /** 分页查询售后案件。 */
    public TableDataInfo list(Integer caseStatus, String keyword, String merchantKeyword, Integer pageNum,
            Integer pageSize)
    {
        PlatformAfterSalesPageView page = rentalBusinessGateway.listPlatformAfterSales(caseStatus, keyword,
                merchantKeyword, pageNum, pageSize);
        return new TableDataInfo(page.getRows(), page.getTotal());
    }

    /** 查询售后案件详情。 */
    public PlatformAfterSalesDetailView detail(Long caseId)
    {
        return rentalBusinessGateway.getPlatformAfterSalesDetail(caseId);
    }

    /** 追加售后处理事件，操作人取平台当前登录用户。 */
    public void appendEvent(Long caseId, PlatformAfterSalesEventCommand command)
    {
        if (command == null || StringUtils.isBlank(command.getContent()))
        {
            throw new ServiceException("处理说明不能为空");
        }
        rentalBusinessGateway.appendPlatformAfterSalesEvent(caseId, command, SecurityUtils.getUserId(),
                SecurityUtils.getUsername());
    }

    /** 追加平台售后证据，操作人取平台当前登录用户。 */
    public void addEvidence(Long caseId, PlatformAfterSalesEvidenceCommand command)
    {
        if (command == null || StringUtils.isBlank(command.getFileUrl()))
        {
            throw new ServiceException("请先上传证据文件");
        }
        rentalBusinessGateway.appendPlatformAfterSalesEvidence(caseId, command, SecurityUtils.getUserId(),
                SecurityUtils.getUsername());
    }
}
