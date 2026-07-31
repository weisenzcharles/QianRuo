package com.ruoyi.system.rental;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import com.ruoyi.system.domain.rental.RentalProductClass;
import com.ruoyi.system.mapper.rental.RentalProductClassMapper;
import com.ruoyi.system.service.rental.RentalProductClassService;

/** 验证平台多级产品类目的层级维护规则。 */
public class RentalProductClassServiceTest
{
    /** 验证移动父类目时会同步替换所有后代的祖先路径并修正层级。 */
    @Test
    public void movingClassUpdatesDescendantHierarchy()
    {
        CapturingProductClassMapper mapper = new CapturingProductClassMapper();
        mapper.classes.put(2L, productClass(2L, 1L, "0,1", 2));
        mapper.classes.put(5L, productClass(5L, 4L, "0,3,4", 3));
        RentalProductClassService service = new RentalProductClassService(mapper);
        RentalProductClass updated = productClass(2L, 5L, null, null);
        updated.setClassCode("camera");
        updated.setClassName("相机");
        updated.setStatus("0");

        int affectedRows = service.update(updated, "platform-user");

        require(affectedRows == 1, "current class update must succeed");
        require("0,3,4,5".equals(mapper.updatedClass.getAncestors()),
                "current class ancestors must be rebuilt from the new parent");
        require(Integer.valueOf(4).equals(mapper.updatedClass.getClassLevel()),
                "current class level must follow the new parent");
        require("0,1,2".equals(mapper.oldAncestorPrefix),
                "descendant update must use the old hierarchy prefix");
        require("0,3,4,5,2".equals(mapper.newAncestorPrefix),
                "descendant update must use the new hierarchy prefix");
        require(mapper.levelOffset == 2, "descendant levels must apply the calculated offset");
    }

    /**
     * 构造测试使用的平台类目实体。
     *
     * @param classId 类目主键
     * @param parentId 父类目主键
     * @param ancestors 祖先路径
     * @param classLevel 类目层级
     * @return 测试类目实体
     */
    private RentalProductClass productClass(Long classId, Long parentId, String ancestors, Integer classLevel)
    {
        RentalProductClass productClass = new RentalProductClass();
        productClass.setClassId(classId);
        productClass.setParentId(parentId);
        productClass.setAncestors(ancestors);
        productClass.setClassLevel(classLevel);
        productClass.setClassCode("class-" + classId);
        productClass.setClassName("类目" + classId);
        productClass.setStatus("0");
        productClass.setSortNo(0);
        return productClass;
    }

    /**
     * 执行无第三方断言库依赖的测试断言。
     *
     * @param condition 期望成立的条件
     * @param message 断言失败提示
     */
    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }

    /** 用于模拟类目查询并捕获层级更新参数的数据访问测试替身。 */
    private static class CapturingProductClassMapper implements RentalProductClassMapper
    {
        private final Map<Long, RentalProductClass> classes = new HashMap<Long, RentalProductClass>();

        private RentalProductClass updatedClass;

        private String oldAncestorPrefix;

        private String newAncestorPrefix;

        private int levelOffset;

        /** 返回空类目列表，本测试不依赖条件查询。 */
        @Override
        public List<RentalProductClass> selectList(RentalProductClass query)
        {
            return Collections.emptyList();
        }

        /** 返回空有效类目列表，本测试不依赖内部发布查询。 */
        @Override
        public List<RentalProductClass> selectActiveList()
        {
            return Collections.emptyList();
        }

        /** 按主键从内存映射中返回测试类目。 */
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

        /** 模拟当前类目不存在直接子节点限制。 */
        @Override
        public int countChildren(Long classId)
        {
            return 0;
        }

        /** 模拟新增类目成功并返回一条受影响记录。 */
        @Override
        public int insert(RentalProductClass productClass)
        {
            return 1;
        }

        /** 捕获当前类目更新后的实体并模拟更新成功。 */
        @Override
        public int update(RentalProductClass productClass)
        {
            updatedClass = productClass;
            return 1;
        }

        /** 捕获后代层级批量更新使用的新旧路径前缀和层级差值。 */
        @Override
        public int updateDescendantHierarchy(String oldAncestorPrefix, String newAncestorPrefix,
                int levelOffset, String operator)
        {
            this.oldAncestorPrefix = oldAncestorPrefix;
            this.newAncestorPrefix = newAncestorPrefix;
            this.levelOffset = levelOffset;
            return 1;
        }

        /** 模拟类目软删除成功并返回一条受影响记录。 */
        @Override
        public int softDelete(Long classId, String operator)
        {
            return 1;
        }
    }
}
