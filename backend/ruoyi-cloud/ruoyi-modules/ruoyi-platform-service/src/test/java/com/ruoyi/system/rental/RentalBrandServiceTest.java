package com.ruoyi.system.rental;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import org.junit.Test;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.domain.rental.RentalBrand;
import com.ruoyi.system.domain.rental.RentalProductClass;
import com.ruoyi.system.mapper.rental.RentalBrandMapper;
import com.ruoyi.system.mapper.rental.RentalProductClassMapper;
import com.ruoyi.system.model.rental.PlatformBrandCommand;
import com.ruoyi.system.model.rental.PlatformBrandQuery;
import com.ruoyi.system.model.rental.PlatformBrandVO;
import com.ruoyi.system.service.rental.RentalBrandService;
import com.ruoyi.system.service.rental.RentalProductClassService;

/** 验证平台品牌与产品分类关系的新增、校验和修改约束。 */
public class RentalBrandServiceTest
{
    /** 验证新增品牌会按每个分类一条关系记录落库。 */
    @Test
    public void addBindsBrandToOneClassRelation()
    {
        CapturingBrandMapper brandMapper = new CapturingBrandMapper();
        RentalBrandService service = createService(brandMapper);

        int affectedRows = service.add(command(1L), 1L);

        require(affectedRows == 1, "brand insert must affect one row");
        require(brandMapper.inserted != null, "brand must be inserted");
        require(Long.valueOf(100L).equals(brandMapper.inserted.getBrandId()), "generated brand id must be retained");
        require(Collections.singletonList(1L).equals(brandMapper.relatedClassIds),
                "brand relation must persist one class id per row");
    }

    /** 验证品牌可以绑定多个二级分类，关系表仍然按单行分类关系写入。 */
    @Test
    public void addBindsBrandToMultipleClassRelations()
    {
        CapturingBrandMapper brandMapper = new CapturingBrandMapper();
        RentalBrandService service = createService(brandMapper);
        PlatformBrandCommand command = command(1L);
        command.setClassIdList(Arrays.asList(1L, 2L));

        service.add(command, 1L);

        require(Arrays.asList(1L, 2L).equals(brandMapper.relatedClassIds),
                "each selected class must be persisted as an independent relation row");
    }

    /** 验证品牌没有经营分类时不能进入持久化层。 */
    @Test
    public void addRejectsBrandWithoutClassRelations()
    {
        CapturingBrandMapper brandMapper = new CapturingBrandMapper();
        RentalBrandService service = createService(brandMapper);

        expectServiceException(() -> service.add(command(null), 1L),
                "至少选择一个所属二级产品分类");
        require(brandMapper.inserted == null, "invalid brand must not be inserted");
    }

    /** 验证型号只能引用已启用且已绑定当前分类的品牌。 */
    @Test
    public void requireActiveForClassRejectsUnboundClass()
    {
        CapturingBrandMapper brandMapper = new CapturingBrandMapper();
        RentalBrandService service = createService(brandMapper);

        expectServiceException(() -> service.requireActiveForClass(100L, 2L),
                "品牌未绑定当前产品分类");
    }

    /** 验证已有型号引用时不能移除品牌与分类的关系。 */
    @Test
    public void updateRejectsRemovingClassReferencedByModel()
    {
        CapturingBrandMapper brandMapper = new CapturingBrandMapper();
        brandMapper.selected = persistedBrand(2L);
        brandMapper.referencedClassId = 2L;
        RentalBrandService service = createService(brandMapper);
        PlatformBrandCommand command = command(1L);
        command.setBrandId(100L);
        command.setClassIdList(Collections.singletonList(1L));

        expectServiceException(() -> service.update(command, 2L),
                "不能移除分类关系");
        require(brandMapper.updated == null, "referenced class relation must not be updated");
    }

    /** 验证品牌实体转展示对象后仍保留 PageHelper 的总条数，保证品牌分页与型号分页一致。 */
    @Test
    public void listPreservesPageHelperTotalAfterViewMapping()
    {
        CapturingBrandMapper brandMapper = new CapturingBrandMapper();
        Page<RentalBrand> source = new Page<RentalBrand>(2, 10);
        source.setTotal(23L);
        source.add(persistedBrand(1L));
        brandMapper.listResult = source;
        RentalBrandService service = createService(brandMapper);

        List<PlatformBrandVO> result = service.list(new PlatformBrandQuery());

        require(result instanceof Page<?>, "mapped brand list must retain page metadata");
        require(new PageInfo<PlatformBrandVO>(result).getTotal() == 23L,
                "mapped brand list must retain the full result count");
        require(new PageInfo<PlatformBrandVO>(result).getPageNum() == 2,
                "mapped brand list must retain the requested page number");
    }

    /** 构造使用内存 Mapper 替身的品牌业务服务。 */
    private RentalBrandService createService(CapturingBrandMapper brandMapper)
    {
        return new RentalBrandService(brandMapper,
                new RentalProductClassService(new ProductClassMapperStub()));
    }

    /** 构造品牌写入命令。 */
    private PlatformBrandCommand command(Long classId)
    {
        PlatformBrandCommand command = new PlatformBrandCommand();
        command.setBrandName("大疆");
        command.setClassIdList(classId == null ? null : Collections.singletonList(classId));
        command.setStatus(RentalCode.EnableStatus.ENABLED.getCode());
        return command;
    }

    /** 构造已有品牌及其分类关系快照。 */
    private RentalBrand persistedBrand(Long classId)
    {
        RentalBrand brand = new RentalBrand();
        brand.setBrandId(100L);
        brand.setBrandName("大疆");
        brand.setStatus(RentalCode.EnableStatus.ENABLED.getCode());
        brand.setClassIdList(classId == null ? null : Collections.singletonList(classId));
        return brand;
    }

    /** 断言业务异常包含面向操作人员的明确提示。 */
    private void expectServiceException(Runnable action, String expectedMessage)
    {
        try
        {
            action.run();
            throw new AssertionError("expected ServiceException");
        }
        catch (ServiceException exception)
        {
            require(exception.getMessage().contains(expectedMessage),
                    "unexpected service exception: " + exception.getMessage());
        }
    }

    /** 执行无额外断言框架依赖的布尔断言。 */
    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }

    /** 捕获品牌写入和品牌分类关系写入参数的 Mapper 替身。 */
    private static class CapturingBrandMapper implements RentalBrandMapper
    {
        private RentalBrand inserted;
        private RentalBrand selected;
        private RentalBrand updated;
        private final List<Long> relatedClassIds = new ArrayList<Long>();
        private Long referencedClassId;
        private List<RentalBrand> listResult = Collections.emptyList();

        @Override
        public List<RentalBrand> selectList(PlatformBrandQuery query)
        {
            return listResult;
        }

        @Override
        public RentalBrand selectById(Long brandId)
        {
            RentalBrand brand = selected == null && Long.valueOf(100L).equals(brandId) ? persistedDefaultBrand() : selected;
            return brand;
        }

        @Override
        public List<Long> selectClassIds(Long brandId)
        {
            RentalBrand brand = selectById(brandId);
            return brand == null || brand.getClassIdList() == null
                    ? Collections.<Long>emptyList() : brand.getClassIdList();
        }

        @Override
        public int countByName(String brandName, Long excludeBrandId)
        {
            return 0;
        }

        @Override
        public int countModelReferences(Long brandId)
        {
            return 0;
        }

        @Override
        public int countModelReferencesByClass(Long brandId, Long classId)
        {
            return Long.valueOf(classId).equals(referencedClassId) ? 1 : 0;
        }

        @Override
        public int countClassRelation(Long brandId, Long classId)
        {
            RentalBrand brand = selectById(brandId);
            return brand != null && brand.getClassIdList() != null && brand.getClassIdList().contains(classId) ? 1 : 0;
        }

        @Override
        public int insert(RentalBrand brand)
        {
            inserted = brand;
            brand.setBrandId(100L);
            return 1;
        }

        @Override
        public int insertClassRelation(Long brandId, Long classId, Long operator)
        {
            relatedClassIds.add(classId);
            return 1;
        }

        @Override
        public int softDeleteClassRelation(Long brandId, Long classId, Long operator)
        {
            return 1;
        }

        @Override
        public int softDeleteRelations(Long brandId, Long operator)
        {
            return 1;
        }

        @Override
        public int update(RentalBrand brand)
        {
            updated = brand;
            return 1;
        }

        @Override
        public int softDelete(Long brandId, Long operator)
        {
            return 1;
        }

        private RentalBrand persistedDefaultBrand()
        {
            return persistedBrandValue(1L);
        }

        private RentalBrand persistedBrandValue(Long classId)
        {
            RentalBrand brand = new RentalBrand();
            brand.setBrandId(100L);
            brand.setBrandName("大疆");
            brand.setStatus(RentalCode.EnableStatus.ENABLED.getCode());
            brand.setClassIdList(Collections.singletonList(classId));
            return brand;
        }
    }

    /** 只提供品牌分类状态校验所需行为的产品分类 Mapper 替身。 */
    private static class ProductClassMapperStub implements RentalProductClassMapper
    {
        @Override
        public List<RentalProductClass> selectList(RentalProductClass query)
        {
            return Collections.emptyList();
        }

        @Override
        public List<RentalProductClass> selectActiveList()
        {
            return Collections.emptyList();
        }

        @Override
        public RentalProductClass selectById(Long classId)
        {
            RentalProductClass productClass = new RentalProductClass();
            productClass.setClassId(classId);
            productClass.setClassLevel(2);
            productClass.setStatus(RentalCode.EnableStatus.ENABLED.getCode());
            return productClass;
        }

        @Override
        public int countByCode(String classCode, Long excludeClassId)
        {
            return 0;
        }

        @Override
        public int countByName(Long parentId, String className, Long excludeClassId)
        {
            return 0;
        }

        @Override
        public int countChildren(Long classId)
        {
            return 0;
        }

        @Override
        public int insert(RentalProductClass productClass)
        {
            return 1;
        }

        @Override
        public int update(RentalProductClass productClass)
        {
            return 1;
        }

        @Override
        public int updateDescendantHierarchy(String oldAncestorPrefix, String newAncestorPrefix, int levelOffset,
                Long operator)
        {
            return 0;
        }

        @Override
        public int softDelete(Long classId, Long operator)
        {
            return 1;
        }
    }
}
