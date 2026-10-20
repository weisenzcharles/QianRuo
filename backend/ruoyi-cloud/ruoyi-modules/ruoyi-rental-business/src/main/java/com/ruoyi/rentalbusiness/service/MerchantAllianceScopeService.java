package com.ruoyi.rentalbusiness.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.api.RemoteMerchantAllianceService;
import com.ruoyi.merchant.api.model.MerchantAllianceOption;
import com.ruoyi.merchant.api.model.MerchantAllianceOptionQuery;
import com.ruoyi.rentalbusiness.mapper.MerchantAllianceScopeMapper;
import com.ruoyi.rentalbusiness.model.MerchantAllianceScopeCommand;
import com.ruoyi.system.api.model.PlatformAllianceScopeView;

/** 商户联盟共享范围应用服务，执行库只维护当前商户拥有的范围授权。 */
@Service
public class MerchantAllianceScopeService
{
    private final MerchantAllianceScopeMapper mapper;
    private final RemoteMerchantAllianceService remoteAllianceService;

    public MerchantAllianceScopeService(MerchantAllianceScopeMapper mapper,
            RemoteMerchantAllianceService remoteAllianceService)
    {
        this.mapper = mapper;
        this.remoteAllianceService = remoteAllianceService;
    }

    /** 平台只读查询指定联盟成员的共享仓库和商品范围。 */
    public List<PlatformAllianceScopeView> listForPlatform(Long tenantId, Long allianceId, Long ownerMemberId)
    {
        return mapper.selectPlatformScopes(tenantId, allianceId, ownerMemberId);
    }

    /** 查询当前商户配置的共享仓库和共享商品范围。 */
    public Map<String, Object> list()
    {
        Long allianceId = allianceId();
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("allianceId", allianceId);
        result.put("warehouses", mapper.selectWarehouseScopes(tenantId, allianceId, memberId));
        result.put("products", mapper.selectProductScopes(tenantId, allianceId, memberId));
        return result;
    }

    /** 新增或更新当前商户的共享仓库范围。 */
    @Transactional(rollbackFor = Exception.class)
    public void saveWarehouse(MerchantAllianceScopeCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.warehouseId == null)
            throw new ServiceException("共享仓库不能为空");
        validateFlag(command.acceptOrderEnabled, command.shareStatus);
        if (mapper.upsertWarehouseScope(MerchantContextUtils.getTenantId(), allianceId(),
                MerchantContextUtils.getMemberId(), command.warehouseId, defaultFlag(command.acceptOrderEnabled),
                defaultFlag(command.shareStatus), command.remark, SecurityUtils.getUserId()) != 1)
            throw new ServiceException("仓库不存在、未启用或不属于当前商户");
    }

    /** 新增或更新当前商户的共享商品范围。 */
    @Transactional(rollbackFor = Exception.class)
    public void saveProduct(MerchantAllianceScopeCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.productId == null)
            throw new ServiceException("共享商品不能为空");
        validateFlag(command.acceptOrderEnabled, command.shareStatus);
        BigDecimal ratio = command.shareQuantityRatio == null ? BigDecimal.ONE : command.shareQuantityRatio;
        if (ratio.compareTo(BigDecimal.ZERO) <= 0 || ratio.compareTo(BigDecimal.ONE) > 0)
            throw new ServiceException("共享库存比例必须大于0且不能超过1");
        if (command.shareQuantityLimit != null && command.shareQuantityLimit < 0)
            throw new ServiceException("共享库存上限不能小于0");
        if (mapper.upsertProductScope(MerchantContextUtils.getTenantId(), allianceId(),
                MerchantContextUtils.getMemberId(), command.productId, ratio, command.shareQuantityLimit,
                defaultFlag(command.acceptOrderEnabled), defaultFlag(command.shareStatus), command.remark,
                SecurityUtils.getUserId()) != 1)
            throw new ServiceException("商品不存在、未启用或不属于当前商户");
    }

    /** 删除当前商户的共享仓库范围。 */
    @Transactional(rollbackFor = Exception.class)
    public void deleteWarehouse(Long scopeId)
    {
        delete(scopeId, true);
    }

    /** 删除当前商户的共享商品范围。 */
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long scopeId)
    {
        delete(scopeId, false);
    }

    private void delete(Long scopeId, boolean warehouse)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (scopeId == null)
            throw new ServiceException("共享范围不能为空");
        int changed = warehouse
                ? mapper.deleteWarehouseScope(MerchantContextUtils.getTenantId(), allianceId(),
                        MerchantContextUtils.getMemberId(), scopeId, SecurityUtils.getUserId())
                : mapper.deleteProductScope(MerchantContextUtils.getTenantId(), allianceId(),
                        MerchantContextUtils.getMemberId(), scopeId, SecurityUtils.getUserId());
        if (changed != 1)
            throw new ServiceException("共享范围不存在或无权操作");
    }

    private Long allianceId()
    {
        R<List<MerchantAllianceOption>> response = remoteAllianceService.listOptions(
                new MerchantAllianceOptionQuery(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId()),
                SecurityConstants.INNER, Constants.CLIENT_TYPE_MERCHANT);
        if (response != null && response.getData() != null)
            for (MerchantAllianceOption option : response.getData())
                if (option != null && option.getAllianceId() != null)
                    return option.getAllianceId();
        throw new ServiceException("当前商户未加入有效租商联盟");
    }

    private void validateFlag(Integer acceptOrderEnabled, Integer shareStatus)
    {
        if (!isFlag(acceptOrderEnabled) || !isFlag(shareStatus))
            throw new ServiceException("共享状态参数不合法");
    }

    private boolean isFlag(Integer value)
    {
        return value == null || RentalCode.EnableStatus.ENABLED.getCode().equals(value)
                || RentalCode.EnableStatus.DISABLED.getCode().equals(value);
    }

    private int defaultFlag(Integer value)
    {
        return value == null ? RentalCode.EnableStatus.ENABLED.getCode() : value;
    }
}
