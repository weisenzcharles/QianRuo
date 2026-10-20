package com.ruoyi.system.rental;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.system.api.model.PlatformAccessoryOption;
import com.ruoyi.system.domain.rental.RentalAccessory;
import com.ruoyi.system.domain.rental.RentalProductClass;
import com.ruoyi.system.mapper.rental.RentalAccessoryMapper;
import com.ruoyi.system.mapper.rental.RentalProductClassMapper;
import com.ruoyi.system.model.rental.PlatformAccessoryCommand;
import com.ruoyi.system.model.rental.PlatformAccessoryQuery;
import com.ruoyi.system.service.rental.RentalAccessoryService;
import com.ruoyi.system.service.rental.RentalProductClassService;

/** 验证平台配件修改时兼容型号关系的软删除冲突处理。 */
public class RentalAccessoryServiceTest
{
    /** 验证更新会先清理冲突历史关系，且不会把不可变配件编码再次判重。 */
    @Test
    public void updateCleansConflictingDeletedModelsBeforeSoftDeletingCurrentRelations()
    {
        CapturingAccessoryMapper accessoryMapper = new CapturingAccessoryMapper();
        RentalAccessoryService service = new RentalAccessoryService(accessoryMapper,
                new RentalProductClassService(new ProductClassMapperStub()), null, null);

        int affectedRows = service.update(command(), 9L);

        require(affectedRows == 1, "accessory update must affect one row");
        require(Arrays.asList("selectById", "selectModelIds", "update", "deleteConflictingDeletedModels",
                "deleteModels", "insertModel").equals(accessoryMapper.events),
                "conflicting history must be cleaned before current models are soft deleted");
        require(accessoryMapper.countByCodeCalls == 0,
                "immutable accessory code must not be checked as a new code during update");
    }

    /** 构造修改配件命令。 */
    private PlatformAccessoryCommand command()
    {
        PlatformAccessoryCommand command = new PlatformAccessoryCommand();
        command.setAccessoryId(21L);
        command.setAccessoryName("相机电池");
        command.setClassId(2L);
        command.setModelIdList(Collections.singletonList(24L));
        command.setStatus(RentalCode.EnableStatus.ENABLED.getCode());
        return command;
    }

    /** 执行无额外断言框架依赖的布尔断言。 */
    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }

    /** 捕获配件更新顺序的 Mapper 替身。 */
    private static class CapturingAccessoryMapper implements RentalAccessoryMapper
    {
        private final List<String> events = new ArrayList<String>();
        private int countByCodeCalls;

        @Override
        public List<RentalAccessory> selectList(PlatformAccessoryQuery query)
        {
            return Collections.emptyList();
        }

        @Override
        public RentalAccessory selectById(Long accessoryId)
        {
            events.add("selectById");
            RentalAccessory accessory = new RentalAccessory();
            accessory.setAccessoryId(accessoryId);
            accessory.setAccessoryCode("ACC000000021");
            accessory.setAccessoryName("相机电池");
            accessory.setClassId(2L);
            accessory.setStatus(RentalCode.EnableStatus.ENABLED.getCode());
            return accessory;
        }

        @Override
        public List<Long> selectModelIds(Long accessoryId)
        {
            events.add("selectModelIds");
            return Collections.singletonList(24L);
        }

        @Override
        public List<PlatformAccessoryOption> selectActiveOptions()
        {
            return Collections.emptyList();
        }

        @Override
        public int countByCode(String accessoryCode, Long excludeAccessoryId)
        {
            countByCodeCalls++;
            return 0;
        }

        @Override
        public int insert(RentalAccessory accessory)
        {
            return 1;
        }

        @Override
        public int update(RentalAccessory accessory)
        {
            events.add("update");
            return 1;
        }

        @Override
        public int insertModel(Long accessoryId, Long modelId, Long operator)
        {
            events.add("insertModel");
            return 1;
        }

        @Override
        public int deleteConflictingDeletedModels(Long accessoryId)
        {
            events.add("deleteConflictingDeletedModels");
            return 1;
        }

        @Override
        public int deleteModels(Long accessoryId, Long operator)
        {
            events.add("deleteModels");
            return 1;
        }

        @Override
        public int softDelete(Long accessoryId, Long operator)
        {
            return 1;
        }
    }

    /** 仅提供二级分类校验所需行为的产品分类 Mapper 替身。 */
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
        public int updateDescendantHierarchy(String oldAncestorPrefix, String newAncestorPrefix,
                int levelOffset, Long operator)
        {
            return 1;
        }

        @Override
        public int softDelete(Long classId, Long operator)
        {
            return 1;
        }
    }
}
