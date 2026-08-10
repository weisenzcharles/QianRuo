package com.ruoyi.merchant.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.mapper.MerchantBrandModelMapper;
import com.ruoyi.merchant.mapper.MerchantProductClassMapper;
import com.ruoyi.merchant.model.MerchantBrandModelVO;
import com.ruoyi.system.api.RemoteBrandModelService;
import com.ruoyi.system.api.model.BrandModel;

/** 商户常用品牌型号应用服务。 */
@Service
public class MerchantBrandModelService
{
    private final MerchantBrandModelMapper brandModelMapper;

    private final MerchantProductClassMapper productClassMapper;

    private final RemoteBrandModelService remoteBrandModelService;

    /**
     * 注入商户常用型号关系、经营类目关系和平台标准型号内部服务。
     *
     * @param brandModelMapper 商户常用型号关系数据访问
     * @param productClassMapper 商户经营类目数据访问
     * @param remoteBrandModelService 平台标准型号内部服务
     */
    public MerchantBrandModelService(MerchantBrandModelMapper brandModelMapper,
            MerchantProductClassMapper productClassMapper, RemoteBrandModelService remoteBrandModelService)
    {
        this.brandModelMapper = brandModelMapper;
        this.productClassMapper = productClassMapper;
        this.remoteBrandModelService = remoteBrandModelService;
    }

    /**
     * 查询当前商户经营类目内的全部有效标准型号，并回显常用选择状态。
     *
     * @return 当前商户可选择的标准型号列表
     */
    public List<MerchantBrandModelVO> listOptions()
    {
        List<BrandModel> models = loadActiveModels();
        Set<Long> allowedClassIds = new LinkedHashSet<Long>(
                productClassMapper.selectClassIds(tenantId(), merchantId()));
        Set<Long> selectedIds = new LinkedHashSet<Long>(
                brandModelMapper.selectSelectedModelIds(tenantId(), merchantId()));
        List<MerchantBrandModelVO> result = new ArrayList<MerchantBrandModelVO>();
        for (BrandModel model : models)
        {
            if (!allowedClassIds.contains(model.getClassId()))
            {
                continue;
            }
            MerchantBrandModelVO item = new MerchantBrandModelVO();
            item.setModelId(model.getModelId());
            item.setClassId(model.getClassId());
            item.setClassName(model.getClassName());
            item.setBrand(model.getBrand());
            item.setModelName(model.getModelName());
            item.setMemorySpec(model.getMemorySpec());
            item.setSelected(selectedIds.contains(model.getModelId()));
            result.add(item);
        }
        return result;
    }

    /**
     * 查询当前商户已保存的常用型号主键，供其他商户业务页面快速回显。
     *
     * @return 常用型号主键集合
     */
    public List<Long> listSelectedIds()
    {
        return brandModelMapper.selectSelectedModelIds(tenantId(), merchantId());
    }

    /**
     * 保存当前商户常用型号，校验型号属于平台有效主数据且对应类目已经被当前商户选择。
     *
     * @param requestedModelIds 客户端提交的标准型号主键集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveSelections(Collection<Long> requestedModelIds)
    {
        MerchantContextUtils.requireMerchantApproved();
        List<Long> modelIds = normalizeIds(requestedModelIds);
        Set<Long> allowedClassIds = new LinkedHashSet<Long>(
                productClassMapper.selectClassIds(tenantId(), merchantId()));
        Set<Long> activeModelIds = new LinkedHashSet<Long>();
        for (BrandModel model : loadActiveModels())
        {
            if (allowedClassIds.contains(model.getClassId()))
            {
                activeModelIds.add(model.getModelId());
            }
        }
        if (!activeModelIds.containsAll(modelIds))
        {
            throw new ServiceException("常用型号包含未经营类目、已停用或不存在的标准型号");
        }
        Long tenantId = tenantId();
        Long merchantId = merchantId();
        String operator = SecurityUtils.getUsername();
        brandModelMapper.softDeleteAll(tenantId, merchantId, operator);
        if (!modelIds.isEmpty())
        {
            brandModelMapper.upsertSelections(tenantId, merchantId, modelIds, operator);
        }
    }

    /**
     * 调用平台内部契约加载有效标准型号，远程异常统一转换为明确业务错误。
     *
     * @return 平台有效标准型号集合
     */
    private List<BrandModel> loadActiveModels()
    {
        R<List<BrandModel>> response = remoteBrandModelService.listActive(SecurityConstants.INNER);
        if (response == null || R.FAIL == response.getCode())
        {
            throw new ServiceException(response == null ? "平台标准型号服务无响应" : response.getMsg());
        }
        return response.getData() == null ? Collections.emptyList() : response.getData();
    }

    /**
     * 去除空主键和重复主键，并保持客户端提交顺序用于常用型号排序。
     *
     * @param source 客户端提交的型号主键集合
     * @return 规范后的唯一主键集合
     */
    private List<Long> normalizeIds(Collection<Long> source)
    {
        Set<Long> uniqueIds = new LinkedHashSet<Long>();
        if (source != null)
        {
            for (Long modelId : source)
            {
                if (modelId != null)
                {
                    uniqueIds.add(modelId);
                }
            }
        }
        return new ArrayList<Long>(uniqueIds);
    }

    /**
     * 从可信商户登录上下文读取租户主键。
     *
     * @return 当前租户主键
     */
    private Long tenantId()
    {
        return MerchantContextUtils.getTenantId();
    }

    /**
     * 从可信商户登录上下文读取商户主键。
     *
     * @return 当前商户主键
     */
    private Long merchantId()
    {
        return MerchantContextUtils.getMemberId();
    }
}
