package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.rental.RentalAccessory;
import com.ruoyi.system.model.rental.PlatformAccessoryQuery;
import com.ruoyi.system.api.model.PlatformAccessoryOption;

/** 平台配件主数据访问组件。 */
public interface RentalAccessoryMapper
{
    List<RentalAccessory> selectList(PlatformAccessoryQuery query);
    RentalAccessory selectById(@Param("accessoryId") Long accessoryId);
    List<Long> selectModelIds(@Param("accessoryId") Long accessoryId);
    List<PlatformAccessoryOption> selectActiveOptions();
    int countByCode(@Param("accessoryCode") String accessoryCode, @Param("excludeAccessoryId") Long excludeAccessoryId);
    int insert(RentalAccessory accessory);
    int update(RentalAccessory accessory);
    int insertModel(@Param("accessoryId") Long accessoryId, @Param("modelId") Long modelId, @Param("operator") Long operator);
    /**
     * 清理与当前有效关系重复的历史软删除记录，避免软删除有效关系时撞上唯一索引。
     *
     * @param accessoryId 配件唯一编号
     * @return 清理的历史关系数量
     */
    int deleteConflictingDeletedModels(@Param("accessoryId") Long accessoryId);
    int deleteModels(@Param("accessoryId") Long accessoryId, @Param("operator") Long operator);
    int softDelete(@Param("accessoryId") Long accessoryId, @Param("operator") Long operator);
}
