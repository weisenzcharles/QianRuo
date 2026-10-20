package com.ruoyi.system.service.rental;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.api.model.ProductClass;
import com.ruoyi.system.domain.rental.RentalProductClass;
import com.ruoyi.system.mapper.rental.RentalProductClassMapper;
import com.ruoyi.system.service.SysCodeRuleService;

/** 平台产品分类业务服务。 */
@Service
public class RentalProductClassService
{
    /** 当前产品分类允许的最大层级，避免错误配置形成不可维护的深树。 */
    private static final int MAX_CLASS_LEVEL = 8;

    private final RentalProductClassMapper productClassMapper;

    private final SysCodeRuleService codeRuleService;

    /**
     * 注入平台产品分类数据访问组件。
     *
     * @param productClassMapper 平台产品分类数据访问组件
     */
    @Autowired
    public RentalProductClassService(RentalProductClassMapper productClassMapper, SysCodeRuleService codeRuleService)
    {
        this.productClassMapper = productClassMapper;
        this.codeRuleService = codeRuleService;
    }

    /**
     * 保留单参数构造方法供不加载 Spring 容器的层级规则单元测试使用；正式运行必须
     * 使用注入统一编码服务的构造方法。
     *
     * @param productClassMapper 平台产品分类数据访问组件
     */
    public RentalProductClassService(RentalProductClassMapper productClassMapper)
    {
        this(productClassMapper, null);
    }

    /**
     * 查询平台分类并构建多级树，查询条件只影响返回节点，不修改数据库层级关系。
     *
     * @param query 分类名称和状态查询条件
     * @return 平台产品分类树
     */
    public List<RentalProductClass> listTree(RentalProductClass query)
    {
        return buildDomainTree(productClassMapper.selectList(query));
    }

    /**
     * 查询指定分类详情，不存在或已删除时抛出统一业务异常。
     *
     * @param classId 分类唯一编号
     * @return 平台产品分类详情
     */
    public RentalProductClass get(Long classId)
    {
        return requireClass(classId);
    }

    /**
     * 新增平台产品分类，由服务端计算祖先路径和层级并校验同级名称、全局编码唯一性。
     *
     * @param productClass 待新增分类
     * @param operator 当前平台操作人员
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int add(RentalProductClass productClass, Long operator)
    {
        normalize(productClass);
        productClass.setClassCode(requireCodeRuleService().nextCode("product_class"));
        prepareHierarchy(productClass);
        validateUnique(productClass);
        productClass.setCreateBy(operator);
        return productClassMapper.insert(productClass);
    }

    /**
     * 修改平台产品分类，禁止把节点移动到自身或自己的后代节点下，确保祖先链无环。
     *
     * @param productClass 待修改分类
     * @param operator 当前平台操作人员
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int update(RentalProductClass productClass, Long operator)
    {
        RentalProductClass existing = requireClass(productClass.getClassId());
        normalize(productClass);
        productClass.setClassCode(existing.getClassCode());
        if (productClass.getClassId().equals(productClass.getParentId()))
        {
            throw new ServiceException("上级分类不能选择当前分类自身");
        }
        prepareHierarchy(productClass);
        if (("," + productClass.getAncestors() + ",").contains("," + productClass.getClassId() + ","))
        {
            throw new ServiceException("上级分类不能选择当前分类的下级分类");
        }
        if (RentalCode.EnableStatus.ENABLED.getCode().equals(existing.getStatus())
                && RentalCode.EnableStatus.DISABLED.getCode().equals(productClass.getStatus())
                && productClassMapper.countChildren(productClass.getClassId()) > 0)
        {
            throw new ServiceException("存在未删除的下级分类，不能停用当前分类");
        }
        validateUnique(productClass);
        productClass.setUpdateBy(operator);
        int affectedRows = productClassMapper.update(productClass);
        updateDescendantHierarchy(existing, productClass, operator, affectedRows);
        return affectedRows;
    }

    /**
     * 软删除平台分类，存在未删除子分类时拒绝删除以保证分类树完整。
     *
     * @param classId 分类唯一编号
     * @param operator 当前平台操作人员
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long classId, Long operator)
    {
        requireClass(classId);
        if (productClassMapper.countChildren(classId) > 0)
        {
            throw new ServiceException("存在未删除的下级分类，不能删除当前分类");
        }
        return productClassMapper.softDelete(classId, operator);
    }

    /**
     * 向内部服务发布平台正常分类的扁平只读集合，避免外部服务依赖平台数据库实体。
     *
     * @return 平台正常分类内部传输集合
     */
    public List<ProductClass> listActiveForInternal()
    {
        List<ProductClass> result = new ArrayList<ProductClass>();
        for (RentalProductClass item : productClassMapper.selectActiveList())
        {
            ProductClass info = new ProductClass();
            info.setClassId(item.getClassId());
            info.setParentId(item.getParentId());
            info.setClassCode(item.getClassCode());
            info.setClassName(item.getClassName());
            info.setClassLevel(item.getClassLevel());
            info.setSortNo(item.getSortNo());
            info.setStatus(item.getStatus());
            result.add(info);
        }
        return result;
    }

    /**
     * 规范分类输入，统一根节点、文本空白、默认状态和默认排序。
     *
     * @param productClass 待规范的分类
     */
    private void normalize(RentalProductClass productClass)
    {
        if (productClass == null)
        {
            throw new ServiceException("分类数据不能为空");
        }
        productClass.setParentId(productClass.getParentId() == null ? 0L : productClass.getParentId());
        productClass.setClassCode(StringUtils.trim(productClass.getClassCode()));
        productClass.setClassName(StringUtils.trim(productClass.getClassName()));
        productClass.setStatus(productClass.getStatus() == null
                ? RentalCode.EnableStatus.ENABLED.getCode() : productClass.getStatus());
        productClass.setSortNo(productClass.getSortNo() == null ? 0 : productClass.getSortNo());
    }

    /**
     * 根据父分类生成祖先路径和层级，并校验父分类可用及最大层级限制。
     *
     * @param productClass 待保存分类
     */
    private void prepareHierarchy(RentalProductClass productClass)
    {
        if (Long.valueOf(0L).equals(productClass.getParentId()))
        {
            productClass.setAncestors("0");
            productClass.setClassLevel(1);
            return;
        }
        RentalProductClass parent = requireClass(productClass.getParentId());
        if (!RentalCode.EnableStatus.ENABLED.getCode().equals(parent.getStatus()))
        {
            throw new ServiceException("上级分类已停用");
        }
        int level = parent.getClassLevel() + 1;
        if (level > MAX_CLASS_LEVEL)
        {
            throw new ServiceException("产品分类最多支持" + MAX_CLASS_LEVEL + "级");
        }
        productClass.setAncestors(parent.getAncestors() + "," + parent.getClassId());
        productClass.setClassLevel(level);
    }

    /**
     * 校验有效分类编码全局唯一、分类名称在同一父节点下唯一。
     *
     * @param productClass 待校验分类
     */
    private void validateUnique(RentalProductClass productClass)
    {
        if (productClassMapper.countByCode(productClass.getClassCode(), productClass.getClassId()) > 0)
        {
            throw new ServiceException("分类编码已存在");
        }
        if (productClassMapper.countByName(productClass.getParentId(), productClass.getClassName(),
                productClass.getClassId()) > 0)
        {
            throw new ServiceException("同一上级分类下的分类名称已存在");
        }
    }

    /**
     * 当前分类的父节点或层级变化后，批量替换所有后代节点的祖先路径并修正层级。
     *
     * @param existing 修改前的分类数据
     * @param productClass 修改后的分类数据
     * @param operator 当前平台操作人员
     * @param affectedRows 当前分类更新受影响行数
     */
    private void updateDescendantHierarchy(RentalProductClass existing, RentalProductClass productClass,
            Long operator, int affectedRows)
    {
        if (affectedRows == 0 || (existing.getParentId().equals(productClass.getParentId())
                && existing.getClassLevel().equals(productClass.getClassLevel())
                && existing.getAncestors().equals(productClass.getAncestors())))
        {
            return;
        }
        String oldPrefix = existing.getAncestors() + "," + existing.getClassId();
        String newPrefix = productClass.getAncestors() + "," + productClass.getClassId();
        int levelOffset = productClass.getClassLevel() - existing.getClassLevel();
        productClassMapper.updateDescendantHierarchy(oldPrefix, newPrefix, levelOffset, operator);
    }

    /**
     * 查询必须存在且未删除的分类。
     *
     * @param classId 分类唯一编号
     * @return 平台产品分类
     */
    private RentalProductClass requireClass(Long classId)
    {
        if (classId == null)
        {
            throw new ServiceException("分类唯一编号不能为空");
        }
        RentalProductClass productClass = productClassMapper.selectById(classId);
        if (productClass == null)
        {
            throw new ServiceException("产品分类不存在或已删除");
        }
        return productClass;
    }

    /**
     * 获取平台统一编码服务；仅在测试未覆盖新增分类时允许为空，生产调用必须由 Spring 注入。
     *
     * @return 平台统一编码服务
     */
    private SysCodeRuleService requireCodeRuleService()
    {
        if (codeRuleService == null)
        {
            throw new ServiceException("平台统一编码服务未配置");
        }
        return codeRuleService;
    }

    /**
     * 按父唯一编号把扁平分类集合组装为稳定有序的多级树。
     *
     * @param source 扁平分类集合
     * @return 多级分类树
     */
    private List<RentalProductClass> buildDomainTree(List<RentalProductClass> source)
    {
        Map<Long, RentalProductClass> index = new LinkedHashMap<Long, RentalProductClass>();
        for (RentalProductClass item : source)
        {
            item.setChildren(new ArrayList<RentalProductClass>());
            index.put(item.getClassId(), item);
        }
        List<RentalProductClass> roots = new ArrayList<RentalProductClass>();
        for (RentalProductClass item : source)
        {
            RentalProductClass parent = index.get(item.getParentId());
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
}
