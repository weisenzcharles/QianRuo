package com.ruoyi.system.service.rental;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.github.pagehelper.Page;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.domain.rental.RentalBrand;
import com.ruoyi.system.domain.rental.RentalProductClass;
import com.ruoyi.system.mapper.rental.RentalBrandMapper;
import com.ruoyi.system.model.rental.PlatformBrandCommand;
import com.ruoyi.system.model.rental.PlatformBrandQuery;
import com.ruoyi.system.model.rental.PlatformBrandVO;

/** 平台品牌主数据业务服务。 */
@Service
public class RentalBrandService
{
    private final RentalBrandMapper brandMapper;

    private final RentalProductClassService productClassService;

    /**
     * 注入品牌主数据访问组件。
     *
     * @param brandMapper 品牌主数据访问组件
     * @param productClassService 产品分类校验服务
     */
    public RentalBrandService(RentalBrandMapper brandMapper, RentalProductClassService productClassService)
    {
        this.brandMapper = brandMapper;
        this.productClassService = productClassService;
    }

    /**
     * 查询平台品牌列表。
     *
     * @param query 品牌名称和状态条件
     * @return 品牌展示对象集合
     */
    public List<PlatformBrandVO> list(PlatformBrandQuery query)
    {
        PlatformBrandQuery normalizedQuery = normalizeQuery(query);
        List<RentalBrand> source = brandMapper.selectList(normalizedQuery);
        List<PlatformBrandVO> result = createPageResult(source);
        for (RentalBrand brand : source)
        {
            result.add(toView(brand));
        }
        return result;
    }

    /**
     * 查询品牌详情。
     *
     * @param brandId 品牌唯一编号
     * @return 品牌详情
     */
    public PlatformBrandVO get(Long brandId)
    {
        return toView(requireBrand(brandId));
    }

    /**
     * 查询可用于产品型号的有效品牌实体。
     *
     * @param brandId 品牌唯一编号
     * @return 正常且未删除的品牌实体
     */
    public RentalBrand requireActive(Long brandId)
    {
        RentalBrand brand = requireBrand(brandId);
        if (!RentalCode.EnableStatus.ENABLED.getCode().equals(brand.getStatus()))
        {
            throw new ServiceException("品牌已停用，不能维护产品型号");
        }
        return brand;
    }

    /**
     * 校验品牌已启用且绑定指定产品分类，供型号录入和商品选择复用。
     *
     * @param brandId 品牌唯一编号
     * @param classId 产品分类唯一编号
     * @return 可用的品牌实体
     */
    public RentalBrand requireActiveForClass(Long brandId, Long classId)
    {
        RentalBrand brand = requireActive(brandId);
        Long normalizedClassId = normalizeClassId(classId);
        if (brandMapper.countClassRelation(brandId, normalizedClassId) == 0)
        {
            throw new ServiceException("品牌未绑定当前产品分类，不能维护产品型号");
        }
        return brand;
    }

    /**
     * 新增平台品牌。
     *
     * @param command 品牌写入命令
     * @param operator 平台操作人员
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int add(PlatformBrandCommand command, Long operator)
    {
        RentalBrand brand = toEntity(command);
        List<Long> classIdList = normalizeClassIdList(command.getClassIdList());
        validateUnique(brand);
        brand.setCreateBy(operator);
        try
        {
            int affectedRows = brandMapper.insert(brand);
            if (affectedRows == 0 || brand.getBrandId() == null)
            {
                throw new ServiceException("品牌新增失败");
            }
            for (Long classId : classIdList)
            {
                brandMapper.insertClassRelation(brand.getBrandId(), classId, operator);
            }
            return affectedRows;
        }
        catch (DuplicateKeyException exception)
        {
            throw duplicateBrandException();
        }
    }

    /**
     * 修改品牌。型号只保存 brand_id，查询时实时关联品牌主数据，因此品牌改名会自然反映到型号展示。
     *
     * @param command 品牌写入命令
     * @param operator 平台操作人员
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int update(PlatformBrandCommand command, Long operator)
    {
        RentalBrand existing = requireBrand(command == null ? null : command.getBrandId());
        RentalBrand brand = toEntity(command);
        List<Long> classIdList = normalizeClassIdList(command.getClassIdList());
        validateRemovedRelations(existing, classIdList);
        validateUnique(brand);
        brand.setUpdateBy(operator);
        try
        {
            int affectedRows = brandMapper.update(brand);
            if (affectedRows == 0)
            {
                throw new ServiceException("品牌不存在或已删除");
            }
            Set<Long> newClassIds = new LinkedHashSet<Long>(classIdList);
            for (Long oldClassId : existing.getClassIdList())
            {
                if (!newClassIds.contains(oldClassId))
                {
                    brandMapper.softDeleteClassRelation(brand.getBrandId(), oldClassId, operator);
                }
            }
            for (Long classId : classIdList)
            {
                brandMapper.insertClassRelation(brand.getBrandId(), classId, operator);
            }
            return affectedRows;
        }
        catch (DuplicateKeyException exception)
        {
            throw duplicateBrandException();
        }
    }

    /**
     * 软删除没有标准型号引用的平台品牌。
     *
     * @param brandId 品牌唯一编号
     * @param operator 平台操作人员
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long brandId, Long operator)
    {
        requireBrand(brandId);
        if (brandMapper.countModelReferences(brandId) > 0)
        {
            throw new ServiceException("品牌已被产品型号引用，请先停用或处理相关型号");
        }
        int affectedRows = brandMapper.softDelete(brandId, operator);
        if (affectedRows == 0)
        {
            throw new ServiceException("品牌不存在或已删除");
        }
        brandMapper.softDeleteRelations(brandId, operator);
        return affectedRows;
    }

    /** 将品牌写入命令规范化为持久化实体。 */
    private RentalBrand toEntity(PlatformBrandCommand command)
    {
        if (command == null)
        {
            throw new ServiceException("品牌数据不能为空");
        }
        RentalBrand brand = new RentalBrand();
        brand.setBrandId(command.getBrandId());
        brand.setBrandName(StringUtils.trim(command.getBrandName()));
        brand.setSortNo(command.getSortNo() == null ? 0 : command.getSortNo());
        brand.setStatus(command.getStatus() == null
                ? RentalCode.EnableStatus.ENABLED.getCode() : command.getStatus());
        brand.setRemark(emptyToNull(StringUtils.trim(command.getRemark())));
        if (StringUtils.isBlank(brand.getBrandName()))
        {
            throw new ServiceException("品牌名称不能为空");
        }
        if (brand.getSortNo() < 0)
        {
            throw new ServiceException("显示顺序不能小于0");
        }
        assertLength(brand.getBrandName(), 64, "品牌名称长度不能超过64个字符");
        assertLength(brand.getRemark(), 500, "备注长度不能超过500个字符");
        if (!RentalCode.EnableStatus.ENABLED.getCode().equals(brand.getStatus())
                && !RentalCode.EnableStatus.DISABLED.getCode().equals(brand.getStatus()))
        {
            throw new ServiceException("品牌状态不合法");
        }
        return brand;
    }

    /** 规范品牌列表查询条件。 */
    private PlatformBrandQuery normalizeQuery(PlatformBrandQuery query)
    {
        PlatformBrandQuery result = query == null ? new PlatformBrandQuery() : query;
        result.setBrandName(emptyToNull(StringUtils.trim(result.getBrandName())));
        return result;
    }

    /** 校验品牌名称的有效唯一性。 */
    private void validateUnique(RentalBrand brand)
    {
        if (brandMapper.countByName(brand.getBrandName(), brand.getBrandId()) > 0)
        {
            throw duplicateBrandException();
        }
    }

    /** 查询必须存在且未软删除的品牌，并加载全部有效分类关系。 */
    private RentalBrand requireBrand(Long brandId)
    {
        if (brandId == null)
        {
            throw new ServiceException("品牌唯一编号不能为空");
        }
        RentalBrand brand = brandMapper.selectById(brandId);
        if (brand == null)
        {
            throw new ServiceException("品牌不存在或已删除");
        }
        brand.setClassIdList(brandMapper.selectClassIds(brandId));
        return brand;
    }

    /** 转换为接口展示对象，避免 Controller 暴露实体和软删除字段。 */
    private PlatformBrandVO toView(RentalBrand brand)
    {
        PlatformBrandVO view = new PlatformBrandVO();
        view.setBrandId(brand.getBrandId());
        view.setBrandName(brand.getBrandName());
        view.setClassIdList(brand.getClassIdList());
        view.setClassName(brand.getClassName());
        view.setSortNo(brand.getSortNo());
        view.setStatus(brand.getStatus());
        view.setModelCount(brand.getModelCount());
        view.setCreateBy(brand.getCreateBy());
        view.setCreateTime(brand.getCreateTime());
        view.setUpdateBy(brand.getUpdateBy());
        view.setUpdateTime(brand.getUpdateTime());
        view.setRemark(brand.getRemark());
        return view;
    }

    /**
     * 创建与查询结果同样带有 PageHelper 元数据的展示列表。
     *
     * <p>品牌列表需要从实体转换为 VO。直接使用 ArrayList 会丢失 PageHelper
     * 保存的总条数，导致分页组件把当前页条数误认为全部数据量。</p>
     *
     * @param source PageHelper 返回的实体列表
     * @return 保留分页元数据的 VO 列表，非分页调用仍返回普通列表
     */
    private List<PlatformBrandVO> createPageResult(List<RentalBrand> source)
    {
        if (!(source instanceof Page<?>))
        {
            return new ArrayList<PlatformBrandVO>(source.size());
        }
        Page<?> sourcePage = (Page<?>) source;
        Page<PlatformBrandVO> result = new Page<PlatformBrandVO>();
        result.setCount(sourcePage.isCount());
        result.setReasonable(sourcePage.getReasonable());
        result.setPageSizeZero(sourcePage.getPageSizeZero());
        result.setPageNum(sourcePage.getPageNum());
        result.setPageSize(sourcePage.getPageSize());
        result.setPages(sourcePage.getPages());
        result.setStartRow(sourcePage.getStartRow());
        result.setEndRow(sourcePage.getEndRow());
        result.setTotal(sourcePage.getTotal());
        return result;
    }

    private String emptyToNull(String value)
    {
        return StringUtils.isBlank(value) ? null : value;
    }

    private void assertLength(String value, int maxLength, String message)
    {
        if (value != null && value.length() > maxLength)
        {
            throw new ServiceException(message);
        }
    }

    private ServiceException duplicateBrandException()
    {
        return new ServiceException("品牌名称已存在");
    }

    /** 校验分类存在、未删除、已启用且为二级分类。 */
    private Long normalizeClassId(Long classId)
    {
        if (classId == null)
        {
            throw new ServiceException("所属产品分类不能为空");
        }
        if (classId <= 0)
        {
            throw new ServiceException("所属产品分类唯一编号不合法");
        }
        RentalProductClass productClass = productClassService.get(classId);
        if (!Integer.valueOf(2).equals(productClass.getClassLevel()))
        {
            throw new ServiceException("品牌所属分类只能选择二级分类");
        }
        if (!RentalCode.EnableStatus.ENABLED.getCode().equals(productClass.getStatus()))
        {
            throw new ServiceException("所属产品分类已停用，不能绑定品牌");
        }
        return classId;
    }

    /** 规范品牌经营范围，并保证关系表每条记录对应一个有效二级分类。 */
    private List<Long> normalizeClassIdList(List<Long> classIdList)
    {
        if (classIdList == null || classIdList.isEmpty())
        {
            throw new ServiceException("至少选择一个所属二级产品分类");
        }
        Set<Long> uniqueClassIds = new LinkedHashSet<Long>();
        for (Long classId : classIdList)
        {
            uniqueClassIds.add(normalizeClassId(classId));
        }
        return new ArrayList<Long>(uniqueClassIds);
    }

    /** 修改品牌经营范围前，禁止移除仍被型号使用的分类关系。 */
    private void validateRemovedRelations(RentalBrand existing, List<Long> newClassIdList)
    {
        Set<Long> newClassIds = new LinkedHashSet<Long>(newClassIdList);
        List<Long> existingClassIds = existing.getClassIdList();
        if (existingClassIds == null)
        {
            return;
        }
        for (Long oldClassId : existingClassIds)
        {
            if (!newClassIds.contains(oldClassId)
                    && brandMapper.countModelReferencesByClass(existing.getBrandId(), oldClassId) > 0)
            {
                throw new ServiceException("品牌在该产品分类下已有型号，不能移除分类关系");
            }
        }
    }
}
