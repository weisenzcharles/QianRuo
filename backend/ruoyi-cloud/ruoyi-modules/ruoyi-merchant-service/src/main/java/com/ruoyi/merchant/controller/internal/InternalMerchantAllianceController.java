package com.ruoyi.merchant.controller.internal;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.github.pagehelper.PageHelper;
import com.ruoyi.merchant.api.model.MerchantAllianceOption;
import com.ruoyi.merchant.api.model.MerchantAllianceOptionQuery;
import com.ruoyi.merchant.mapper.MerchantAllianceMapper;
import com.ruoyi.merchant.application.MerchantAllianceService;

/** 商户联盟内部查询入口，仅供租赁业务服务调用。 */
@RestController
@RequestMapping("/internal/merchant/alliances")
public class InternalMerchantAllianceController extends BaseController
{
    private final MerchantAllianceMapper mapper;
    private final MerchantAllianceService service;

    public InternalMerchantAllianceController(MerchantAllianceMapper mapper, MerchantAllianceService service)
    {
        this.mapper = mapper;
        this.service = service;
    }

    /** 查询当前商户所属联盟中的其他商户，不返回联系方式等敏感信息。 */
    @PostMapping("/options")
    public R<List<MerchantAllianceOption>> options(@RequestBody MerchantAllianceOptionQuery query,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType)
    {
        if (!SecurityConstants.INNER.equals(source) || !Constants.CLIENT_TYPE_MERCHANT.equals(clientType)
                || query == null || query.getTenantId() == null || query.getMemberId() == null)
        {
            return R.fail("无效的联盟查询请求");
        }
        return R.ok(mapper.selectOptions(query.getTenantId(), query.getMemberId()));
    }

    /** 查询当前商户自身的有效联盟关系，供租赁业务服务校验接收资格。 */
    @PostMapping("/current")
    public R<java.util.Map<String, Object>> current(@RequestBody MerchantAllianceOptionQuery query,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType)
    {
        if (!SecurityConstants.INNER.equals(source) || !Constants.CLIENT_TYPE_MERCHANT.equals(clientType)
                || query == null || query.getTenantId() == null || query.getMemberId() == null)
            return R.fail("无效的联盟关系查询请求");
        return R.ok(mapper.selectCurrent(query.getTenantId(), query.getMemberId()));
    }

    /** 查询已允许共享库存的联盟商户，供租赁业务服务汇总库存。 */
    @PostMapping("/shared-members")
    public R<List<MerchantAllianceOption>> sharedMembers(@RequestBody MerchantAllianceOptionQuery query,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType)
    {
        if (!SecurityConstants.INNER.equals(source) || !Constants.CLIENT_TYPE_MERCHANT.equals(clientType)
                || query == null || query.getTenantId() == null || query.getMemberId() == null)
            return R.fail("无效的共享库存查询请求");
        return R.ok(service.sharedMembers(query.getTenantId(), query.getMemberId()));
    }

    /** 平台分页查询所有租商联盟，不返回商户敏感资料。 */
    @PostMapping("/admin/list")
    public R<TableDataInfo> listForPlatform(@RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType)
    {
        if (!SecurityConstants.INNER.equals(source) || !Constants.CLIENT_TYPE_PLATFORM.equals(clientType))
            return R.fail("无效的平台联盟查询请求");
        PageHelper.startPage(pageNum == null || pageNum < 1 ? 1 : pageNum,
                pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100));
        return R.ok(getDataTable(mapper.selectAdminAlliances(keyword, status)));
    }

    /** 平台查询指定联盟成员。 */
    @PostMapping("/admin/members")
    public R<List<MerchantAllianceOption>> membersForPlatform(@RequestParam("allianceId") Long allianceId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType)
    {
        if (!SecurityConstants.INNER.equals(source) || !Constants.CLIENT_TYPE_PLATFORM.equals(clientType)
                || allianceId == null)
            return R.fail("无效的平台联盟成员查询请求");
        return R.ok(mapper.selectAdminMembers(allianceId));
    }

    /** 平台更新联盟或成员状态；商户服务负责在治理库内执行状态校验和更新。 */
    @PostMapping("/admin/status")
    public R<Void> updateStatusForPlatform(@RequestParam("allianceId") Long allianceId,
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam("status") Integer status,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType)
    {
        boolean validStatus = memberId == null
                ? status != null && (RentalCode.AllianceStatus.ACTIVE.getCode().equals(status)
                        || RentalCode.AllianceStatus.DISABLED.getCode().equals(status))
                : status != null && (RentalCode.AllianceMemberStatus.ACTIVE.getCode().equals(status)
                        || RentalCode.AllianceMemberStatus.DISABLED.getCode().equals(status));
        if (!SecurityConstants.INNER.equals(source) || !Constants.CLIENT_TYPE_PLATFORM.equals(clientType)
                || allianceId == null || !validStatus)
            return R.fail("无效的联盟状态更新请求");
        if (memberId == null) {
            if (mapper.updateAdminAllianceStatus(allianceId, status) != 1)
                return R.fail("联盟不存在或已删除");
        } else if (mapper.updateAdminMemberStatus(allianceId, memberId, status) != 1) {
            return R.fail("联盟成员不存在或状态未变化");
        }
        return R.ok();
    }

    /** 租赁业务服务调用的接单名额预占接口。 */
    @PostMapping("/order-slots/reserve")
    public R<Void> reserveOrderSlot(@RequestParam("tenantId") Long tenantId,
            @RequestParam("allianceId") Long allianceId, @RequestParam("memberId") Long memberId,
            @RequestParam("allianceOrderId") Long allianceOrderId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType)
    {
        if (!SecurityConstants.INNER.equals(source) || !Constants.CLIENT_TYPE_MERCHANT.equals(clientType))
            return R.fail("无效的接单名额请求");
        service.reserveOrderSlot(tenantId, allianceId, memberId, allianceOrderId, null);
        return R.ok();
    }

    /** 租赁业务服务调用的接单名额释放接口。 */
    @PostMapping("/order-slots/release")
    public R<Void> releaseOrderSlot(@RequestParam("tenantId") Long tenantId,
            @RequestParam("memberId") Long memberId, @RequestParam("allianceOrderId") Long allianceOrderId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType)
    {
        if (!SecurityConstants.INNER.equals(source) || !Constants.CLIENT_TYPE_MERCHANT.equals(clientType))
            return R.fail("无效的接单名额请求");
        service.releaseOrderSlot(tenantId, memberId, allianceOrderId, null);
        return R.ok();
    }
}
