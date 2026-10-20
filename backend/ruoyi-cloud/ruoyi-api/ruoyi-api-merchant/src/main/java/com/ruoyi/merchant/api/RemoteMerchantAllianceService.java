package com.ruoyi.merchant.api;

import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.merchant.api.factory.RemoteMerchantAllianceFallbackFactory;
import com.ruoyi.merchant.api.model.MerchantAllianceOption;
import com.ruoyi.merchant.api.model.MerchantAllianceOptionQuery;

/** 商户联盟关系内部服务契约。 */
@FeignClient(contextId = "remoteMerchantAllianceService", value = MerchantServiceConstants.SERVICE_NAME,
        fallbackFactory = RemoteMerchantAllianceFallbackFactory.class)
public interface RemoteMerchantAllianceService
{
    /** 平台查询联盟分页数据。 */
    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/alliances/admin/list")
    R<TableDataInfo> listForPlatform(@RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);

    /** 平台查询指定联盟成员。 */
    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/alliances/admin/members")
    R<List<MerchantAllianceOption>> membersForPlatform(@RequestParam("allianceId") Long allianceId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);

    /** 平台更新联盟或成员状态。 */
    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/alliances/admin/status")
    R<Void> updateStatusForPlatform(@RequestParam("allianceId") Long allianceId,
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam("status") Integer status,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);

    /** 查询当前商户所属联盟中的其他有效商户。 */
    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/alliances/options")
    R<List<MerchantAllianceOption>> listOptions(@RequestBody MerchantAllianceOptionQuery query,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);

    /** 查询当前商户自身的有效联盟关系。 */
    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/alliances/current")
    R<Map<String, Object>> current(@RequestBody MerchantAllianceOptionQuery query,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);

    /** 查询允许当前商户读取共享库存的联盟成员。 */
    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/alliances/shared-members")
    R<List<MerchantAllianceOption>> listSharedMembers(@RequestBody MerchantAllianceOptionQuery query,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);

    /** 为已接受的联盟订单原子预占接单名额，重复请求按幂等成功处理。 */
    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/alliances/order-slots/reserve")
    R<Void> reserveOrderSlot(@RequestParam("tenantId") Long tenantId, @RequestParam("allianceId") Long allianceId,
            @RequestParam("memberId") Long memberId, @RequestParam("allianceOrderId") Long allianceOrderId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);

    /** 释放已结束联盟订单占用的接单名额，重复请求安全。 */
    @PostMapping(MerchantServiceConstants.INTERNAL_PREFIX + "/alliances/order-slots/release")
    R<Void> releaseOrderSlot(@RequestParam("tenantId") Long tenantId, @RequestParam("memberId") Long memberId,
            @RequestParam("allianceOrderId") Long allianceOrderId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source,
            @RequestHeader(Constants.CLIENT_TYPE_HEADER) String clientType);
}
