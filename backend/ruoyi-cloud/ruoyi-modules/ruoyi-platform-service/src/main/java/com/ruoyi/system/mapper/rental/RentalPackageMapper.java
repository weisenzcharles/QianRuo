package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.rental.RentalPackage;
import com.ruoyi.system.domain.rental.RentalPackageItem;
import com.ruoyi.system.model.rental.PlatformPackageQuery;
import com.ruoyi.system.api.model.PlatformPackage;

/** 平台官方套餐数据访问组件。 */
public interface RentalPackageMapper
{
    List<RentalPackage> selectList(PlatformPackageQuery query);
    RentalPackage selectById(@Param("packageId") Long packageId);
    List<RentalPackageItem> selectItems(@Param("packageVersionId") Long packageVersionId);
    int countByCode(@Param("packageCode") String packageCode, @Param("excludePackageId") Long excludePackageId);
    Integer selectNextVersionNo(@Param("packageId") Long packageId);
    int insert(RentalPackage item);
    int update(RentalPackage item);
    int insertVersion(RentalPackage item);
    int insertItem(RentalPackageItem item);
    int retireVersions(@Param("packageId") Long packageId, @Param("operator") Long operator);
    int activateLatest(@Param("packageId") Long packageId, @Param("operator") Long operator);
    int softDeleteItems(@Param("packageId") Long packageId, @Param("operator") Long operator);
    int softDeleteVersions(@Param("packageId") Long packageId, @Param("operator") Long operator);
    int softDelete(@Param("packageId") Long packageId, @Param("operator") Long operator);
    List<PlatformPackage> selectActiveForInternal();
}
