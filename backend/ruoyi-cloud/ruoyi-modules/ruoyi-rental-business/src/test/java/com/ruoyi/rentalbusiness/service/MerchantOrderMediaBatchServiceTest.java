package com.ruoyi.rentalbusiness.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.lang.reflect.Proxy;
import org.junit.After;
import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderMapper;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderMediaMapper;
import com.ruoyi.rentalbusiness.model.MerchantOrderMediaBatchCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderMediaCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderMediaVO;
import com.ruoyi.system.api.model.LoginUser;

/** 批量发货视频登记和 SN 匹配测试。 */
public class MerchantOrderMediaBatchServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 已识别 SN 对应未完成订单时，视频应自动绑定订单和 SN。 */
    @Test
    public void addBatch_whenSnMatchesOpenOrder_marksMatched()
    {
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        AtomicInteger matchStatus = new AtomicInteger(0);
        AtomicLong matchedUnitId = new AtomicLong(0);
        MerchantOrderMediaMapper mediaMapper = mediaMapper(matchStatus, matchedUnitId, true);
        MerchantOrderMediaBatchCreateCommand batch = new MerchantOrderMediaBatchCreateCommand();
        batch.mediaType = "ship_video";
        MerchantOrderMediaCreateCommand item = new MerchantOrderMediaCreateCommand();
        item.fileUrl = "https://file.example/order/sn001.mp4";
        item.fileName = "ship-SN001.mp4";
        item.detectedSn = "SN001";
        batch.items = Collections.singletonList(item);

        MerchantOrderMediaService service = new MerchantOrderMediaService(orderMapper(), mediaMapper, null);
        MerchantOrderMediaVO result = service.addBatch(batch).get(0);

        require(matchStatus.get() == 1, "matching order must mark media as matched");
        require(matchedUnitId.get() == 69L, "matching order must retain inventory unit id");
        require(result.mediaId != null, "batch media must retain media id for retry");
        require("已匹配".equals(result.matchStatusLabel), "matched media must expose Chinese status");
        require("OD0000007001".equals(result.orderNo), "matched media must expose order number");
    }

    /** 文件名没有 SN 候选时，视频应进入待识别状态而不是误绑订单。 */
    @Test
    public void addBatch_whenNoSnCandidate_keepsPending()
    {
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        AtomicInteger matchStatus = new AtomicInteger(0);
        AtomicLong matchedUnitId = new AtomicLong(0);
        MerchantOrderMediaMapper mediaMapper = mediaMapper(matchStatus, matchedUnitId, false);
        MerchantOrderMediaBatchCreateCommand batch = new MerchantOrderMediaBatchCreateCommand();
        batch.mediaType = "ship_video";
        MerchantOrderMediaCreateCommand item = new MerchantOrderMediaCreateCommand();
        item.fileUrl = "https://file.example/order/ship-video.mp4";
        item.fileName = "ship-video.mp4";
        batch.items = Collections.singletonList(item);

        MerchantOrderMediaVO result = new MerchantOrderMediaService(orderMapper(), mediaMapper, null)
                .addBatch(batch).get(0);

        require(matchStatus.get() == 0, "video without SN candidate must remain pending");
        require("待识别".equals(result.matchStatusLabel), "pending media must expose Chinese status");
    }

    private MerchantOrderMediaMapper mediaMapper(AtomicInteger matchStatus, AtomicLong matchedUnitId,
            boolean hasTarget)
    {
        return (MerchantOrderMediaMapper) Proxy.newProxyInstance(MerchantOrderMediaMapper.class.getClassLoader(),
                new Class<?>[] { MerchantOrderMediaMapper.class }, (proxy, method, args) -> {
                    if ("insert".equals(method.getName()))
                    {
                        MerchantOrderMediaCreateCommand command = (MerchantOrderMediaCreateCommand) args[3];
                        command.mediaId = 9901L;
                        return 1;
                    }
                    if ("selectOpenOrderBySnCode".equals(method.getName()) && hasTarget)
                    {
                        Map<String, Object> target = new HashMap<String, Object>();
                        target.put("orderId", 7001L);
                        target.put("orderNo", "OD0000007001");
                        target.put("warehouseId", 101L);
                        target.put("unitId", 69L);
                        target.put("snId", 9001L);
                        return target;
                    }
                    if ("updateMatch".equals(method.getName()))
                    {
                        matchedUnitId.set(args[4] == null ? 0L : ((Number) args[4]).longValue());
                        matchStatus.set(((Number) args[7]).intValue());
                        return 1;
                    }
                    if ("selectById".equals(method.getName()))
                    {
                        MerchantOrderMediaVO result = new MerchantOrderMediaVO();
                        result.mediaId = 9901L;
                        result.mediaType = "1";
                        result.fileUrl = "https://file.example/order/sn001.mp4";
                        result.matchStatus = matchStatus.get() < 0 ? 0 : matchStatus.get();
                        result.orderNo = result.matchStatus == 1 ? "OD0000007001" : null;
                        return result;
                    }
                    if (method.getReturnType().equals(java.util.List.class))
                    {
                        return Collections.emptyList();
                    }
                    return null;
                });
    }

    private MerchantOrderMapper orderMapper()
    {
        return (MerchantOrderMapper) Proxy.newProxyInstance(MerchantOrderMapper.class.getClassLoader(),
                new Class<?>[] { MerchantOrderMapper.class }, (proxy, method, args) -> null);
    }

    private LoginUser merchantLoginUser()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setTenantId(10001L);
        loginUser.setMemberId(27L);
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        loginUser.setWarehouseScopeType(RentalCode.WarehouseScope.ALL.getCode());
        return loginUser;
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
