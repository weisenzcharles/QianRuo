package com.ruoyi.system.rental;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.api.RemoteRentalBusinessService;
import com.ruoyi.system.api.model.PlatformWarehouseView;
import com.ruoyi.system.domain.rental.RentalBrandModel;
import com.ruoyi.system.domain.rental.RentalProductClass;
import com.ruoyi.system.mapper.rental.RentalBrandModelMapper;
import com.ruoyi.system.mapper.rental.RentalProductClassMapper;
import com.ruoyi.system.service.rental.RentalBusinessGateway;
import com.ruoyi.system.model.rental.PlatformBrandModelCommand;
import com.ruoyi.system.model.rental.PlatformBrandModelQuery;
import com.ruoyi.system.service.rental.RentalBrandModelService;
import com.ruoyi.system.service.rental.RentalProductClassService;

/** 验证平台标准型号录入的规范化、默认值和业务校验。 */
public class RentalBrandModelServiceTest
{
    /** 验证新增时会去除文本空白、清理空规格并设置稳定的官方标识和状态默认值。 */
    @Test
    public void addNormalizesStandardModelBeforePersisting()
    {
        CapturingBrandModelMapper brandModelMapper = new CapturingBrandModelMapper();
        RentalBrandModelService service = createService(brandModelMapper, activeClass(1L));
        PlatformBrandModelCommand command = command(1L, "  大疆  ", "  Pocket 3  ", "   ");

        int affectedRows = service.add(command, "platform-admin");

        require(affectedRows == 1, "standard model insert must affect one row");
        require("大疆".equals(brandModelMapper.inserted.getBrand()), "brand must be trimmed");
        require("Pocket 3".equals(brandModelMapper.inserted.getModelName()), "model name must be trimmed");
        require(brandModelMapper.inserted.getMemorySpec() == null, "blank specification must become null");
        require("1".equals(brandModelMapper.inserted.getOfficialFlag()), "official flag must default to one");
        require("0".equals(brandModelMapper.inserted.getStatus()), "status must default to active");
        require("platform-admin".equals(brandModelMapper.inserted.getCreateBy()),
                "create operator must come from the platform context");
    }

    /** 验证停用类目不能继续录入标准型号，避免商户侧出现不可经营的主数据。 */
    @Test
    public void addRejectsModelUnderDisabledProductClass()
    {
        CapturingBrandModelMapper brandModelMapper = new CapturingBrandModelMapper();
        RentalProductClass disabledClass = activeClass(2L);
        disabledClass.setStatus("1");
        RentalBrandModelService service = createService(brandModelMapper, disabledClass);

        expectServiceException(() -> service.add(command(2L, "大疆", "Pocket 4", null), "platform-admin"),
                "产品类目已停用");
        require(brandModelMapper.inserted == null, "disabled product class must not reach the insert mapper");
    }

    /** 验证重复的类目、品牌、型号和规格组合会在写入数据库前返回明确业务错误。 */
    @Test
    public void addRejectsDuplicateStandardModelIdentity()
    {
        CapturingBrandModelMapper brandModelMapper = new CapturingBrandModelMapper();
        brandModelMapper.duplicateCount = 1;
        RentalBrandModelService service = createService(brandModelMapper, activeClass(3L));

        expectServiceException(() -> service.add(command(3L, "大疆", "Pocket 3", "标准版"), "platform-admin"),
                "品牌、型号和规格已存在");
        require(brandModelMapper.inserted == null, "duplicate standard model must not be inserted");
    }

    /** 验证修改时会校验原记录、规范输入并记录平台操作人员。 */
    @Test
    public void updateNormalizesStandardModelAndRecordsOperator()
    {
        CapturingBrandModelMapper brandModelMapper = new CapturingBrandModelMapper();
        brandModelMapper.selected = persistedModel(8L, 1L, "大疆", "Pocket 3", null);
        RentalBrandModelService service = createService(brandModelMapper, activeClass(2L));
        PlatformBrandModelCommand command = command(2L, "  大疆  ", "  Pocket 4  ", "  全能套装  ");
        command.setModelId(8L);
        command.setOfficialFlag("0");
        command.setStatus("1");

        int affectedRows = service.update(command, "platform-editor");

        require(affectedRows == 1, "standard model update must affect one row");
        require(brandModelMapper.updated != null, "normalized standard model must reach update mapper");
        require(Long.valueOf(8L).equals(brandModelMapper.updated.getModelId()), "updated model id must be preserved");
        require(Long.valueOf(2L).equals(brandModelMapper.updated.getClassId()), "updated class id must be preserved");
        require("大疆".equals(brandModelMapper.updated.getBrand()), "updated brand must be trimmed");
        require("Pocket 4".equals(brandModelMapper.updated.getModelName()), "updated model name must be trimmed");
        require("全能套装".equals(brandModelMapper.updated.getMemorySpec()), "updated specification must be trimmed");
        require("platform-editor".equals(brandModelMapper.updated.getUpdateBy()),
                "update operator must come from the platform context");
    }

    /** 验证已经被商品或商户常用型号引用的标准型号不能删除。 */
    @Test
    public void deleteRejectsReferencedStandardModel()
    {
        CapturingBrandModelMapper brandModelMapper = new CapturingBrandModelMapper();
        brandModelMapper.selected = persistedModel(9L, 1L, "大疆", "Pocket 3", null);
        brandModelMapper.merchantCommonModelReferenceCount = 1;
        RentalBrandModelService service = createService(brandModelMapper, activeClass(1L));

        expectServiceException(() -> service.delete(9L, "platform-admin"), "已被业务数据引用");

        require(brandModelMapper.deletedModelId == null, "referenced standard model must not be soft deleted");
    }

    /** 验证未被引用的标准型号使用软删除并记录平台操作人员。 */
    @Test
    public void deleteSoftDeletesUnreferencedStandardModel()
    {
        CapturingBrandModelMapper brandModelMapper = new CapturingBrandModelMapper();
        brandModelMapper.selected = persistedModel(10L, 1L, "大疆", "Pocket 4", null);
        RentalBrandModelService service = createService(brandModelMapper, activeClass(1L));

        int affectedRows = service.delete(10L, "platform-admin");

        require(affectedRows == 1, "standard model soft delete must affect one row");
        require(Long.valueOf(10L).equals(brandModelMapper.deletedModelId),
                "soft delete must target the requested standard model");
        require("platform-admin".equals(brandModelMapper.deletedBy),
                "soft delete operator must come from the platform context");
    }

    /**
     * 使用内存 Mapper 构造标准型号应用服务，避免单元测试依赖 Spring 容器和真实数据库。
     *
     * @param brandModelMapper 捕获标准型号写入的数据访问替身
     * @param productClass 测试场景中的产品类目
     * @return 可直接执行录入规则的标准型号应用服务
     */
    private RentalBrandModelService createService(CapturingBrandModelMapper brandModelMapper,
            RentalProductClass productClass)
    {
        ProductClassMapperStub productClassMapper = new ProductClassMapperStub();
        productClassMapper.classes.put(productClass.getClassId(), productClass);
        return new RentalBrandModelService(brandModelMapper, new RentalProductClassService(productClassMapper),
                newRentalBusinessGateway());
    }

    /**
     * 构造不访问网络的租赁业务网关替身，默认表示业务库没有标准型号引用。
     *
     * @return 返回稳定成功结果的租赁业务应用网关
     */
    private RentalBusinessGateway newRentalBusinessGateway()
    {
        return new RentalBusinessGateway(new RemoteRentalBusinessService()
        {
            /** 测试场景不依赖租赁业务仓库列表。 */
            @Override
            public R<List<PlatformWarehouseView>> listWarehouses(Long tenantId, Long memberId, Integer status,
                    String source)
            {
                return R.ok(Collections.<PlatformWarehouseView>emptyList());
            }

            /** 测试场景不依赖租赁业务仓库范围统计。 */
            @Override
            public R<Integer> countEnabledWarehouses(Long tenantId, Long memberId, List<Long> warehouseIds,
                    String source)
            {
                return R.ok(Integer.valueOf(0));
            }

            /** 测试场景默认表示标准型号未被租赁业务引用。 */
            @Override
            public R<Boolean> hasModelReferences(Long modelId, String source)
            {
                return R.ok(Boolean.FALSE);
            }
        });
    }

    /**
     * 构造处于正常状态的产品类目，供标准型号类目校验使用。
     *
     * @param classId 产品类目主键
     * @return 可用于录入标准型号的正常产品类目
     */
    private RentalProductClass activeClass(Long classId)
    {
        RentalProductClass productClass = new RentalProductClass();
        productClass.setClassId(classId);
        productClass.setParentId(0L);
        productClass.setAncestors("0");
        productClass.setClassCode("CLASS-" + classId);
        productClass.setClassName("测试类目" + classId);
        productClass.setClassLevel(1);
        productClass.setSortNo(0);
        productClass.setStatus("0");
        return productClass;
    }

    /**
     * 构造平台标准型号写入命令。
     *
     * @param classId 产品类目主键
     * @param brand 品牌名称
     * @param modelName 型号名称
     * @param memorySpec 内存或规格
     * @return 标准型号写入命令
     */
    private PlatformBrandModelCommand command(Long classId, String brand, String modelName, String memorySpec)
    {
        PlatformBrandModelCommand command = new PlatformBrandModelCommand();
        command.setClassId(classId);
        command.setBrand(brand);
        command.setModelName(modelName);
        command.setMemorySpec(memorySpec);
        return command;
    }

    /**
     * 构造数据库中已经存在的标准型号，供修改和软删除规则测试使用。
     *
     * @param modelId 标准型号主键
     * @param classId 产品类目主键
     * @param brand 品牌名称
     * @param modelName 型号名称
     * @param memorySpec 内存或规格
     * @return 模拟从数据库读取的标准型号实体
     */
    private RentalBrandModel persistedModel(Long modelId, Long classId, String brand, String modelName,
            String memorySpec)
    {
        RentalBrandModel model = new RentalBrandModel();
        model.setModelId(modelId);
        model.setClassId(classId);
        model.setBrand(brand);
        model.setModelName(modelName);
        model.setMemorySpec(memorySpec);
        model.setOfficialFlag("1");
        model.setStatus("0");
        return model;
    }

    /**
     * 执行业务调用并断言返回指定业务错误，确保异常信息可直接反馈给平台操作人员。
     *
     * @param action 预期失败的标准型号业务调用
     * @param expectedMessage 期望包含的中文业务提示
     */
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

    /**
     * 执行无额外断言框架依赖的布尔断言。
     *
     * @param condition 期望成立的条件
     * @param message 断言失败时输出的说明
     */
    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }

    /** 捕获标准型号写入参数并模拟数据库响应的数据访问替身。 */
    private static class CapturingBrandModelMapper implements RentalBrandModelMapper
    {
        private RentalBrandModel inserted;
        private RentalBrandModel selected;
        private RentalBrandModel updated;
        private int duplicateCount;
        private int merchantCommonModelReferenceCount;
        private Long deletedModelId;
        private String deletedBy;

        /** 返回空列表，本测试不依赖平台型号查询。 */
        @Override
        public List<RentalBrandModel> selectList(PlatformBrandModelQuery query)
        {
            return Collections.emptyList();
        }

        /** 返回空列表，本测试不依赖内部标准型号发布。 */
        @Override
        public List<RentalBrandModel> selectActiveList()
        {
            return Collections.emptyList();
        }

        /** 返回测试场景预设的标准型号详情。 */
        @Override
        public RentalBrandModel selectById(Long modelId)
        {
            return selected;
        }

        /** 返回测试预设的业务身份重复数量。 */
        @Override
        public int countByIdentity(Long classId, String brand, String modelName, String memorySpec,
                Long excludeModelId)
        {
            return duplicateCount;
        }

        /** 返回测试场景预设的平台本地商户常用型号引用数量。 */
        @Override
        public int countMerchantCommonModelReferences(Long modelId)
        {
            return merchantCommonModelReferenceCount;
        }

        /** 捕获新增实体并模拟数据库成功写入一行。 */
        @Override
        public int insert(RentalBrandModel model)
        {
            inserted = model;
            model.setModelId(1L);
            return 1;
        }

        /** 捕获修改实体并模拟数据库成功更新一行。 */
        @Override
        public int update(RentalBrandModel model)
        {
            updated = model;
            return 1;
        }

        /** 捕获软删除主键和操作人员并模拟数据库成功更新一行。 */
        @Override
        public int softDelete(Long modelId, String operator)
        {
            deletedModelId = modelId;
            deletedBy = operator;
            return 1;
        }
    }

    /** 仅提供标准型号类目校验所需行为的产品类目数据访问替身。 */
    private static class ProductClassMapperStub implements RentalProductClassMapper
    {
        private final Map<Long, RentalProductClass> classes = new HashMap<Long, RentalProductClass>();

        /** 返回空列表，本测试不依赖产品类目树。 */
        @Override
        public List<RentalProductClass> selectList(RentalProductClass query)
        {
            return Collections.emptyList();
        }

        /** 返回空列表，本测试不依赖内部产品类目发布。 */
        @Override
        public List<RentalProductClass> selectActiveList()
        {
            return Collections.emptyList();
        }

        /** 按主键返回测试场景中预设的产品类目。 */
        @Override
        public RentalProductClass selectById(Long classId)
        {
            return classes.get(classId);
        }

        /** 模拟类目编码不存在重复记录。 */
        @Override
        public int countByCode(String classCode, Long excludeClassId)
        {
            return 0;
        }

        /** 模拟同级类目名称不存在重复记录。 */
        @Override
        public int countByName(Long parentId, String className, Long excludeClassId)
        {
            return 0;
        }

        /** 模拟产品类目没有直接下级节点。 */
        @Override
        public int countChildren(Long classId)
        {
            return 0;
        }

        /** 模拟产品类目新增成功。 */
        @Override
        public int insert(RentalProductClass productClass)
        {
            return 1;
        }

        /** 模拟产品类目修改成功。 */
        @Override
        public int update(RentalProductClass productClass)
        {
            return 1;
        }

        /** 模拟产品类目后代层级更新成功。 */
        @Override
        public int updateDescendantHierarchy(String oldAncestorPrefix, String newAncestorPrefix,
                int levelOffset, String operator)
        {
            return 1;
        }

        /** 模拟产品类目软删除成功。 */
        @Override
        public int softDelete(Long classId, String operator)
        {
            return 1;
        }
    }
}
