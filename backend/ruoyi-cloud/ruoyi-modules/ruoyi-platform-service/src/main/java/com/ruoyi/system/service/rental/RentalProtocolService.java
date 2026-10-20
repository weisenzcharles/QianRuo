package com.ruoyi.system.service.rental;

import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.system.model.rental.PlatformProtocolCommand;
import com.ruoyi.system.model.rental.PlatformProtocolVO;

/** 平台协议版本应用服务。 */
@Service
public class RentalProtocolService
{
    private final RentalBusinessGateway rentalBusinessGateway;

    /** 注入租赁业务网关。 */
    public RentalProtocolService(RentalBusinessGateway rentalBusinessGateway)
    {
        this.rentalBusinessGateway = rentalBusinessGateway;
    }

    /** 查询平台协议版本。 */
    public List<PlatformProtocolVO> list(String protocolKey)
    {
        return rentalBusinessGateway.listPlatformProtocols(protocolKey);
    }

    /** 新增平台协议草稿。 */
    public PlatformProtocolVO add(PlatformProtocolCommand command)
    {
        return rentalBusinessGateway.addPlatformProtocol(toApiCommand(command));
    }

    /** 修改平台协议草稿。 */
    public PlatformProtocolVO update(PlatformProtocolCommand command)
    {
        return rentalBusinessGateway.updatePlatformProtocol(toApiCommand(command));
    }

    /** 发布平台协议版本。 */
    public PlatformProtocolVO publish(Long protocolId, String effectiveTime)
    {
        return rentalBusinessGateway.publishPlatformProtocol(protocolId, effectiveTime);
    }

    private com.ruoyi.system.api.model.PlatformProtocolCommand toApiCommand(PlatformProtocolCommand source)
    {
        com.ruoyi.system.api.model.PlatformProtocolCommand target =
                new com.ruoyi.system.api.model.PlatformProtocolCommand();
        if (source != null)
        {
            target.setProtocolId(source.getProtocolId());
            target.setProtocolKey(source.getProtocolKey());
            target.setProtocolTitle(source.getProtocolTitle());
            target.setProtocolContent(source.getProtocolContent());
            target.setScopeTypes(source.getScopeTypes());
            target.setRemark(source.getRemark());
        }
        return target;
    }
}
