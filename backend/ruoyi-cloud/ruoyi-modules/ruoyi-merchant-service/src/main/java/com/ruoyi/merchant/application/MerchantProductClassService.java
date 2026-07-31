package com.ruoyi.merchant.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.mapper.MerchantProductClassMapper;
import com.ruoyi.system.api.RemoteProductClassService;
import com.ruoyi.system.api.model.ProductClass;

/** 商户经营产品类目应用服务。 */
@Service
public class MerchantProductClassService
{
    private final MerchantProductClassMapper productClassMapper;

    private final RemoteProductClassService remoteProductClassService;

    /**
     * 注入商户经营类目数据访问和平台类目内部服务。
     *
     * @param productClassMapper 商户经营类目数据访问
     * @param remoteProductClassService 平台产品类目内部服务
     */
    public MerchantProductClassService(MerchantProductClassMapper productClassMapper,
            RemoteProductClassService remoteProductClassService)
    {
        this.productClassMapper = productClassMapper;
        this.remoteProductClassService = remoteProductClassService;
    }

    /**
     * 查询平台全部正常类目并构建选择树，供当前商户配置经营范围。
     *
     * @return 平台正常类目树
     */
    public List<ProductClass> listOptions()
    {
        return buildTree(loadActiveClasses());
    }

    /**
     * 查询当前登录商户已经保存的经营类目主键。
     *
     * @return 当前商户经营类目主键集合
     */
    public List<Long> listSelectedIds()
    {
        return productClassMapper.selectClassIds(tenantId(), merchantId());
    }

    /**
     * 查询当前商户业务可见类目树，只保留已选择类目及用于展示层级的祖先节点。
     *
     * @return 当前商户业务可见类目树
     */
    public List<ProductClass> listBusinessTree()
    {
        Set<Long> selectedIds = new LinkedHashSet<Long>(listSelectedIds());
        if (selectedIds.isEmpty())
        {
            return Collections.emptyList();
        }
        List<ProductClass> tree = buildTree(loadActiveClasses());
        return retainSelectedBranches(tree, selectedIds);
    }

    /**
     * 保存当前登录商户的经营类目，先校验所有类目均来自平台当前有效集合，再执行软删除和恢复。
     *
     * @param requestedClassIds 客户端提交的平台类目主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveSelections(Collection<Long> requestedClassIds)
    {
        List<Long> classIds = normalizeIds(requestedClassIds);
        Set<Long> activeIds = new LinkedHashSet<Long>();
        for (ProductClass item : loadActiveClasses())
        {
            activeIds.add(item.getClassId());
        }
        if (!activeIds.containsAll(classIds))
        {
            throw new ServiceException("经营类目包含不存在或已停用的平台类目");
        }

        Long tenantId = tenantId();
        Long merchantId = merchantId();
        String operator = SecurityUtils.getUsername();
        productClassMapper.softDeleteAll(tenantId, merchantId, operator);
        if (!classIds.isEmpty())
        {
            productClassMapper.upsertSelections(tenantId, merchantId, classIds, operator);
        }
    }

    /**
     * 校验指定类目是否属于当前登录商户经营范围，商品、套餐等业务写接口必须调用该方法。
     *
     * @param classId 平台产品类目主键
     */
    public void assertAllowed(Long classId)
    {
        if (classId == null || productClassMapper.countSelected(tenantId(), merchantId(), classId) == 0)
        {
            throw new ServiceException("当前商户未经营该产品类目");
        }
    }

    /**
     * 调用平台内部契约加载正常类目，并把远程失败转换为明确业务异常。
     *
     * @return 平台正常类目扁平集合
     */
    private List<ProductClass> loadActiveClasses()
    {
        R<List<ProductClass>> response = remoteProductClassService.listActive(SecurityConstants.INNER);
        if (response == null || R.FAIL == response.getCode())
        {
            throw new ServiceException(response == null ? "平台产品类目服务无响应" : response.getMsg());
        }
        return response.getData() == null ? Collections.emptyList() : response.getData();
    }

    /**
     * 去除空主键和重复主键，同时保持客户端原始选择顺序。
     *
     * @param source 客户端提交的类目主键集合
     * @return 规范后的唯一类目主键集合
     */
    private List<Long> normalizeIds(Collection<Long> source)
    {
        Set<Long> uniqueIds = new LinkedHashSet<Long>();
        if (source != null)
        {
            for (Long classId : source)
            {
                if (classId != null)
                {
                    uniqueIds.add(classId);
                }
            }
        }
        return new ArrayList<Long>(uniqueIds);
    }

    /**
     * 根据父主键把平台扁平类目集合组装成多级树。
     *
     * @param source 平台正常类目扁平集合
     * @return 多级类目树
     */
    private List<ProductClass> buildTree(List<ProductClass> source)
    {
        Map<Long, ProductClass> index = new LinkedHashMap<Long, ProductClass>();
        for (ProductClass item : source)
        {
            item.setChildren(new ArrayList<ProductClass>());
            index.put(item.getClassId(), item);
        }
        List<ProductClass> roots = new ArrayList<ProductClass>();
        for (ProductClass item : source)
        {
            ProductClass parent = index.get(item.getParentId());
            if (parent == null)
            {
                roots.add(item);
            }
            else
            {
                parent.getChildren().add(item);
            }
        }
        return roots;
    }

    /**
     * 递归裁剪平台类目树，仅保留商户已选择节点及其展示所需祖先节点。
     *
     * @param nodes 当前层类目节点
     * @param selectedIds 当前商户选择的类目主键
     * @return 裁剪后的业务可见类目树
     */
    private List<ProductClass> retainSelectedBranches(List<ProductClass> nodes, Set<Long> selectedIds)
    {
        List<ProductClass> retained = new ArrayList<ProductClass>();
        for (ProductClass node : nodes)
        {
            List<ProductClass> children = retainSelectedBranches(node.getChildren(), selectedIds);
            if (selectedIds.contains(node.getClassId()) || !children.isEmpty())
            {
                node.setChildren(children);
                retained.add(node);
            }
        }
        return retained;
    }

    /**
     * 从可信登录上下文获取当前租户主键，禁止客户端传入租户归属。
     *
     * @return 当前租户主键
     */
    private Long tenantId()
    {
        return MerchantContextUtils.getTenantId();
    }

    /**
     * 从可信登录上下文获取当前商户主键，禁止客户端传入商户归属。
     *
     * @return 当前商户主键
     */
    private Long merchantId()
    {
        return MerchantContextUtils.getMemberId();
    }
}
