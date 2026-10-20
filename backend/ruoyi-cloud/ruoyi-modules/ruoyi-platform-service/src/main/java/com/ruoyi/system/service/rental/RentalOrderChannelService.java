package com.ruoyi.system.service.rental;

import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.system.model.rental.PlatformOrderChannelCommand;
import com.ruoyi.system.model.rental.PlatformOrderChannelVO;

/** 平台订单渠道应用服务。 */
@Service
public class RentalOrderChannelService
{
    private final RentalBusinessGateway rentalBusinessGateway;

    /** 注入租赁业务网关。 */
    public RentalOrderChannelService(RentalBusinessGateway rentalBusinessGateway)
    {
        this.rentalBusinessGateway = rentalBusinessGateway;
    }

    /** 查询平台联盟订单渠道。 */
    public List<PlatformOrderChannelVO> list(String keyword, Integer status)
    {
        return rentalBusinessGateway.listPlatformOrderChannels(keyword, status);
    }

    /** 新增平台联盟订单渠道。 */
    public PlatformOrderChannelVO add(PlatformOrderChannelCommand command)
    {
        return rentalBusinessGateway.addPlatformOrderChannel(toApiCommand(command));
    }

    /** 修改平台联盟订单渠道。 */
    public PlatformOrderChannelVO update(PlatformOrderChannelCommand command)
    {
        return rentalBusinessGateway.updatePlatformOrderChannel(toApiCommand(command));
    }

    /** 软删除平台联盟订单渠道。 */
    public boolean delete(Long channelId)
    {
        return rentalBusinessGateway.deletePlatformOrderChannel(channelId);
    }

    private com.ruoyi.system.api.model.PlatformOrderChannelCommand toApiCommand(PlatformOrderChannelCommand source)
    {
        com.ruoyi.system.api.model.PlatformOrderChannelCommand target =
                new com.ruoyi.system.api.model.PlatformOrderChannelCommand();
        if (source != null)
        {
            target.setChannelId(source.getChannelId());
            target.setChannelName(source.getChannelName());
            target.setChannelType(source.getChannelType());
            target.setStatus(source.getStatus());
            target.setSortNo(source.getSortNo());
            target.setReason(source.getReason());
        }
        return target;
    }
}
