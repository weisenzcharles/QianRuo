package com.ruoyi.rentalbusiness.service;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.rentalbusiness.domain.MerchantOrderRow;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderMapper;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderMediaMapper;
import com.ruoyi.rentalbusiness.model.MerchantOrderMediaCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderMediaVO;
import com.ruoyi.system.api.model.LoginUser;

/** 订单履约媒体登记的订单范围和状态校验测试。 */
public class MerchantOrderMediaServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 已备货订单登记发货视频后应落库并返回中文媒体类型。 */
    @Test
    public void addShipVideo_whenOrderPrepared_savesMedia()
    {
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantOrderRow order = order(RentalCode.OrderStatus.PREPARED.getCode());
        AtomicBoolean inserted = new AtomicBoolean(false);
        MerchantOrderMediaCreateCommand saved = new MerchantOrderMediaCreateCommand();
        MerchantOrderMediaMapper mediaMapper = mediaMapper(inserted, saved);

        MerchantOrderMediaCreateCommand command = new MerchantOrderMediaCreateCommand();
        command.mediaType = " ship_video ";
        command.fileUrl = " https://file.example/order/video.mp4 ";
        command.fileName = " video.mp4 ";
        MerchantOrderMediaVO result = new MerchantOrderMediaService(orderMapper(order), mediaMapper, null)
                .add(order.orderId, command);

        require(inserted.get(), "prepared order media must be inserted");
        require("ship_video".equals(saved.mediaType), "media type must be normalized");
        require("发货视频".equals(result.mediaTypeLabel), "media type must expose Chinese label");
        require("https://file.example/order/video.mp4".equals(saved.fileUrl),
                "file URL must be trimmed before persistence");
    }

    /** 已发货订单不能伪造为已备货后再登记发货视频。 */
    @Test
    public void addShipVideo_whenOrderShipped_rejectsWithoutInsert()
    {
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        AtomicBoolean inserted = new AtomicBoolean(false);
        MerchantOrderMediaCreateCommand command = new MerchantOrderMediaCreateCommand();
        command.mediaType = "ship_video";
        command.fileUrl = "https://file.example/order/video.mp4";

        try
        {
            new MerchantOrderMediaService(orderMapper(order(RentalCode.OrderStatus.SHIPPED.getCode())),
                    mediaMapper(inserted, new MerchantOrderMediaCreateCommand()), null)
                    .add(7001L, command);
            throw new AssertionError("shipped order must reject ship video registration");
        }
        catch (ServiceException exception)
        {
            require("只有已备货订单可以上传发货凭证".equals(exception.getMessage()),
                    "unexpected shipped order error");
        }
        require(!inserted.get(), "rejected media must not be inserted");
    }

    /** 即使绕过前端，服务端也必须拒绝非白名单履约文件。 */
    @Test
    public void addShipVideo_whenExtensionInvalid_rejectsWithoutInsert()
    {
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        AtomicBoolean inserted = new AtomicBoolean(false);
        MerchantOrderMediaCreateCommand command = new MerchantOrderMediaCreateCommand();
        command.mediaType = "ship_video";
        command.fileUrl = "https://file.example/order/video.exe";
        try
        {
            new MerchantOrderMediaService(orderMapper(order(RentalCode.OrderStatus.PREPARED.getCode())),
                    mediaMapper(inserted, new MerchantOrderMediaCreateCommand()), null).add(7001L, command);
            throw new AssertionError("invalid media extension must be rejected");
        }
        catch (ServiceException exception)
        {
            require("履约视频仅支持mp4、avi或rmvb格式".equals(exception.getMessage()),
                    "unexpected invalid extension error");
        }
        require(!inserted.get(), "invalid media must not be inserted");
    }

    private MerchantOrderMapper orderMapper(MerchantOrderRow order)
    {
        return (MerchantOrderMapper) Proxy.newProxyInstance(MerchantOrderMapper.class.getClassLoader(),
                new Class<?>[] { MerchantOrderMapper.class },
                (proxy, method, args) -> "selectMerchantOrderById".equals(method.getName()) ? order : null);
    }

    private MerchantOrderMediaMapper mediaMapper(AtomicBoolean inserted,
            MerchantOrderMediaCreateCommand saved)
    {
        return (MerchantOrderMediaMapper) Proxy.newProxyInstance(MerchantOrderMediaMapper.class.getClassLoader(),
                new Class<?>[] { MerchantOrderMediaMapper.class },
                (proxy, method, args) -> {
                    if ("insert".equals(method.getName()))
                    {
                        inserted.set(true);
                        MerchantOrderMediaCreateCommand command = (MerchantOrderMediaCreateCommand) args[3];
                        saved.mediaType = command.mediaType;
                        saved.fileUrl = command.fileUrl;
                        saved.fileName = command.fileName;
                        return 1;
                    }
                    if ("selectLatest".equals(method.getName()))
                    {
                        MerchantOrderMediaVO result = new MerchantOrderMediaVO();
                        result.mediaType = "ship_video";
                        result.fileUrl = saved.fileUrl;
                        result.fileName = saved.fileName;
                        return result;
                    }
                    return Collections.emptyList();
                });
    }

    private MerchantOrderRow order(Integer status)
    {
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 7001L;
        order.orderNo = "OD0000007001";
        order.warehouseId = 101L;
        order.orderStatus = status;
        return order;
    }

    private LoginUser merchantLoginUser()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setTenantId(10001L);
        loginUser.setMemberId(27L);
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        loginUser.setWarehouseScopeType(RentalCode.WarehouseScope.SPECIFIED.getCode());
        loginUser.setWarehouseIds(Collections.singleton(101L));
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
