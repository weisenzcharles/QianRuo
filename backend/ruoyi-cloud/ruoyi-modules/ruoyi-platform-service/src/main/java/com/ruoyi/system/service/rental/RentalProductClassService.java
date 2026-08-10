package com.ruoyi.system.service.rental;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.api.model.ProductClass;
import com.ruoyi.system.domain.rental.RentalProductClass;
import com.ruoyi.system.mapper.rental.RentalProductClassMapper;
import com.ruoyi.system.service.SysCodeRuleService;

/** 平台产品类目业务服务。 */
@Service
public class RentalProductClassService
{
    /** 当前产品类目允许的最大层级，避免错误配置形成不可维护的深树。 */
    private static final int MAX_CLASS_LEVEL = 8;

    private final RentalProductClassMapper productClassMapper;

    private final SysCodeRuleService codeRuleService;

    /**
     * 注入平台产品类目数据访问组件。
     *
     * @param productClassMapper 平台产品类目数据访问组件
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
     * @param productClassMapper 平台产品类目数据访问组件
     */
    public RentalProductClassService(RentalProductClassMapper productClassMapper)
    {
        this(productClassMapper, null);
    }

    /**
     * 查询平台类目并构建多级树，查询条件只影响返回节点，不修改数据库层级关系。
     *
     * @param query 类目名称和状态查询条件
     * @return 平台产品类目树
     */
    public List<RentalProductClass> listTree(RentalProductClass query)
    {
        return buildDomainTree(productClassMapper.selectList(query));
    }

    /**
     * 查询指定类目详情，不存在或已删除时抛出统一业务异常。
     *
     * @param classId 类目主键
     * @return 平台产品类目详情
     */
    public RentalProductClass get(Long classId)
    {
        return requireClass(classId);
    }

    /**
     * 新增平台产品类目，由服务端计算祖先路径和层级并校验同级名称、全局编码唯一性。
     *
     * @param productClass 待新增类目
     * @param operator 当前平台操作人员
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int add(RentalProductClass productClass, String operator)
    {
        normalize(productClass);
        productClass.setClassCode(requireCodeRuleService().nextCode("product_class"));
        prepareHierarchy(productClass);
        validateUnique(productClass);
        productClass.setCreateBy(operator);
        return productClassMapper.insert(productClass);
    }

    /**
     * 修改平台产品类目，禁止把节点移动到自身或自己的后代节点下，确保祖先链无环。
     *
     * @param productClass 待修改类目
     * @param operator 当前平台操作人员
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int update(RentalProductClass productClass, String operator)
    {
        RentalProductClass existing = requireClass(productClass.getClassId());
        normalize(productClass);
        productClass.setClassCode(existing.getClassCode());
        if (productClass.getClassId().equals(productClass.getParentId()))
        {
            throw new ServiceException("上级类目不能选择当前类目自身");
        }
        prepareHierarchy(productClass);
        if (("," + productClass.getAncestors() + ",").contains("," + productClass.getClassId() + ","))
        {
            throw new ServiceException("上级类目不能选择当前类目的下级类目");
        }
        if ("0".equals(existing.getStatus()) && "1".equals(productClass.getStatus())
                && productClassMapper.countChildren(productClass.getClassId()) > 0)
        {
            throw new ServiceException("存在未删除的下级类目，不能停用当前类目");
        }
        validateUnique(productClass);
        productClass.setUpdateBy(operator);
        int affectedRows = productClassMapper.update(productClass);
        updateDescendantHierarchy(existing, productClass, operator, affectedRows);
        return affectedRows;
    }

    /**
     * 软删除平台类目，存在未删除子类目时拒绝删除以保证类目树完整。
     *
     * @param classId 类目主键
     * @param operator 当前平台操作人员
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long classId, String operator)
    {
        requireClass(classId);
        if (productClassMapper.countChildren(classId) > 0)
        {
            throw new ServiceException("存在未删除的下级类目，不能删除当前类目");
        }
        return productClassMapper.softDelete(classId, operator);
    }

    /**
     * 向内部服务发布平台正常类目的扁平只读集合，避免外部服务依赖平台数据库实体。
     *
     * @return 平台正常类目内部传输集合
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
     * 规范类目输入，统一根节点、文本空白、默认状态和默认排序。
     *
     * @param productClass 待规范的类目
     */
    private void normalize(RentalProductClass productClass)
    {
        if (productClass == null)
        {
            throw new ServiceException("类目数据不能为空");
        }
        productClass.setParentId(productClass.getParentId() == null ? 0L : productClass.getParentId());
        productClass.setClassCode(StringUtils.trim(productClass.getClassCode()));
        productClass.setClassName(StringUtils.trim(productClass.getClassName()));
        productClass.setStatus(StringUtils.isBlank(productClass.getStatus()) ? "0" : productClass.getStatus());
        productClass.setSortNo(productClass.getSortNo() == null ? 0 : productClass.getSortNo());
    }

    /**
     * 根据父类目生成祖先路径和层级，并校验父类目可用及最大层级限制。
     *
     * @param productClass 待保存类目
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
        if (!"0".equals(parent.getStatus()))
        {
            throw new ServiceException("上级类目已停用");
        }
        int level = parent.getClassLevel() + 1;
        if (level > MAX_CLASS_LEVEL)
        {
            throw new ServiceException("产品类目最多支持" + MAX_CLASS_LEVEL + "级");
        }
        productClass.setAncestors(parent.getAncestors() + "," + parent.getClassId());
        productClass.setClassLevel(level);
    }

    /**
     * 校验有效类目编码全局唯一、类目名称在同一父节点下唯一。
     *
     * @param productClass 待校验类目
     */
    private void validateUnique(RentalProductClass productClass)
    {
        if (productClassMapper.countByCode(productClass.getClassCode(), productClass.getClassId()) > 0)
        {
            throw new ServiceException("类目编码已存在");
        }
        if (productClassMapper.countByName(productClass.getParentId(), productClass.getClassName(),
                productClass.getClassId()) > 0)
        {
            throw new ServiceException("同一上级类目下的类目名称已存在");
        }
    }

    /**
     * 当前类目的父节点或层级变化后，批量替换所有后代节点的祖先路径并修正层级。
     *
     * @param existing 修改前的类目数据
     * @param productClass 修改后的类目数据
     * @param operator 当前平台操作人员
     * @param affectedRows 当前类目更新受影响行数
     */
    private void updateDescendantHierarchy(RentalProductClass existing, RentalProductClass productClass,
            String operator, int affectedRows)
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
     * 查询必须存在且未删除的类目。
     *
     * @param classId 类目主键
     * @return 平台产品类目
     */
    private RentalProductClass requireClass(Long classId)
    {
        if (classId == null)
        {
            throw new ServiceException("类目ID不能为空");
        }
        RentalProductClass productClass = productClassMapper.selectById(classId);
        if (productClass == null)
        {
            throw new ServiceException("产品类目不存在或已删除");
        }
        return productClass;
    }

    /**
     * 获取平台统一编码服务；仅在测试未覆盖新增类目时允许为空，生产调用必须由 Spring 注入。
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
     * 按父主键把扁平类目集合组装为稳定有序的多级树。
     *
     * @param source 扁平类目集合
     * @return 多级类目树
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
