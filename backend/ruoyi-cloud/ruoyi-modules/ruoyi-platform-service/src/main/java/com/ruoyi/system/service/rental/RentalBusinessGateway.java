package com.ruoyi.system.service.rental;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.api.RemoteRentalBusinessService;
import com.ruoyi.system.api.model.PlatformWarehouseView;

/**
 * 平台服务访问租赁业务服务的应用网关。
 *
 * <p>平台控制器只依赖该网关，不直接依赖租赁业务 Mapper 或 {@code ry_business} 数据库。
 * 网关对远程失败统一抛出业务异常，避免把降级结果误当作空数据继续执行。</p>
 */
@Service
public class RentalBusinessGateway
{
    private final RemoteRentalBusinessService remoteRentalBusinessService;

    /** 注入租赁业务服务内部 Feign 契约。 */
    public RentalBusinessGateway(RemoteRentalBusinessService remoteRentalBusinessService)
    {
        this.remoteRentalBusinessService = remoteRentalBusinessService;
    }

    /**
     * 查询指定商户的启用仓库，供平台商户用户编辑和仓库权限校验使用。
     *
     * @param tenantId 可信租户主键
     * @param memberId 可信商户主键
     * @return 启用仓库集合
     */
    public List<PlatformWarehouseView> listEnabledWarehouses(Long tenantId, Long memberId)
    {
        R<List<PlatformWarehouseView>> response = remoteRentalBusinessService.listWarehouses(tenantId, memberId,
                Integer.valueOf(0), SecurityConstants.INNER);
        List<PlatformWarehouseView> data = require(response, "查询商户仓库");
        return data == null ? Collections.emptyList() : data;
    }

    /**
     * 校验仓库主键集合是否全部属于指定商户的启用仓库。
     *
     * @param tenantId 可信租户主键
     * @param memberId 可信商户主键
     * @param warehouseIds 待校验仓库主键集合
     * @return 命中的启用仓库数量
     */
    public int countEnabledWarehouses(Long tenantId, Long memberId, List<Long> warehouseIds)
    {
        if (warehouseIds == null || warehouseIds.isEmpty())
        {
            return 0;
        }
        R<Integer> response = remoteRentalBusinessService.countEnabledWarehouses(tenantId, memberId, warehouseIds,
                SecurityConstants.INNER);
        Integer data = require(response, "校验商户仓库范围");
        if (data == null)
        {
            throw new ServiceException("校验商户仓库范围返回空结果");
        }
        return data.intValue();
    }

    /**
     * 判断标准型号是否被租赁业务数据引用，供平台删除标准型号前执行保护校验。
     *
     * @param modelId 平台标准型号主键
     * @return 已被业务引用返回 true
     */
    public boolean hasModelReferences(Long modelId)
    {
        R<Boolean> response = remoteRentalBusinessService.hasModelReferences(modelId, SecurityConstants.INNER);
        Boolean data = require(response, "校验标准型号业务引用");
        if (data == null)
        {
            throw new ServiceException("校验标准型号业务引用返回空结果");
        }
        return data.booleanValue();
    }

    /**
     * 统一解包内部服务响应，任何非成功状态都必须阻断当前平台操作。
     *
     * @param response 远程服务响应
     * @param operation 当前操作名称
     * @param <T> 响应数据类型
     * @return 远程响应数据
     */
    private <T> T require(R<T> response, String operation)
    {
        if (response == null || !R.isSuccess(response))
        {
            String message = response == null ? "无响应" : response.getMsg();
            throw new ServiceException(operation + "失败：" + message);
        }
        return response.getData();
    }
}
