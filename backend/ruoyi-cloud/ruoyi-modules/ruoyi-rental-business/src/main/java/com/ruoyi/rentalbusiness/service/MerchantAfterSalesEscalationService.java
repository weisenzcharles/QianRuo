package com.ruoyi.rentalbusiness.service;

import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.rentalbusiness.enums.MerchantAfterSalesCaseStatus;
import com.ruoyi.rentalbusiness.mapper.MerchantAfterSalesMapper;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesCaseVO;
import com.ruoyi.system.api.RemotePlatformGovernanceService;
import com.ruoyi.system.api.model.PlatformAfterSalesTicketCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesTicketView;

/** 商户售后申请平台介入服务，负责商户隔离和平台工单关联落库。 */
@Service
public class MerchantAfterSalesEscalationService
{
    private final MerchantAfterSalesMapper mapper;
    private final RemotePlatformGovernanceService platformGovernanceService;

    public MerchantAfterSalesEscalationService(MerchantAfterSalesMapper mapper,
            RemotePlatformGovernanceService platformGovernanceService)
    {
        this.mapper = mapper;
        this.platformGovernanceService = platformGovernanceService;
    }

    /** 创建或复用当前商户售后单对应的平台客服工单。 */
    public PlatformAfterSalesTicketView escalate(String caseNo)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (StringUtils.isBlank(caseNo))
        {
            throw new ServiceException("售后单号不能为空");
        }
        MerchantAfterSalesCaseVO caseRow = mapper.selectByCaseNoForUpdate(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), caseNo.trim());
        if (caseRow == null)
        {
            throw new ServiceException("售后单不存在或无权查看");
        }
        if (!MerchantAfterSalesCaseStatus.PROCESSING.getCode().equals(caseRow.caseStatus)
                && StringUtils.isBlank(caseRow.platformTicketNo))
        {
            throw new ServiceException("已完成或已关闭的售后单不能申请平台介入");
        }

        PlatformAfterSalesTicketCommand command = new PlatformAfterSalesTicketCommand();
        command.setMemberId(MerchantContextUtils.getMemberId());
        command.setCaseId(caseRow.caseId);
        command.setOrderId(caseRow.orderId);
        command.setCaseNo(caseRow.caseNo);
        command.setOrderNo(caseRow.orderNo);
        command.setCaseTypeName(caseRow.caseTypeLabel);
        command.setProblemDesc(caseRow.problemDesc);
        command.setCompensationAmount(caseRow.compensationAmount);
        command.setRemark(caseRow.remark);

        R<PlatformAfterSalesTicketView> response = platformGovernanceService.createAfterSalesTicket(command,
                SecurityConstants.INNER);
        if (response == null || !R.isSuccess(response) || response.getData() == null)
        {
            throw new ServiceException(response == null ? "平台工单服务暂不可用" : response.getMsg());
        }
        PlatformAfterSalesTicketView ticket = response.getData();
        if (StringUtils.isBlank(ticket.getTicketNo())
                || mapper.updatePlatformTicketNo(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                        caseRow.caseId, ticket.getTicketNo(), SecurityUtils.getUserId()) != 1)
        {
            throw new ServiceException("平台工单已创建，但售后关联保存失败，请重试");
        }
        return ticket;
    }
}
