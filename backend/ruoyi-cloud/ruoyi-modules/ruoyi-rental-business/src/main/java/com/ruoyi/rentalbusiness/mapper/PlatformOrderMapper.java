package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.rentalbusiness.domain.MerchantOrderRow;
import com.ruoyi.rentalbusiness.model.MerchantOrderEditLogVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderNoteVO;
import com.ruoyi.system.api.model.PlatformOrderItemView;
import com.ruoyi.system.api.model.PlatformOrderReturnInspectionView;
import com.ruoyi.system.api.model.PlatformOrderView;

/** 平台订单只读查询数据访问。 */
public interface PlatformOrderMapper
{
    /** 统计指定筛选范围内的未删除订单。 */
    long countPlatformOrders(@Param("memberIds") List<Long> memberIds, @Param("keyword") String keyword,
            @Param("orderStatus") Integer orderStatus, @Param("beginTime") String beginTime,
            @Param("endTime") String endTime);

    /** 按创建时间倒序分页查询指定筛选范围内的订单。 */
    List<PlatformOrderView> selectPlatformOrders(@Param("memberIds") List<Long> memberIds,
            @Param("keyword") String keyword, @Param("orderStatus") Integer orderStatus,
            @Param("beginTime") String beginTime, @Param("endTime") String endTime, @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    /** 查询平台订单详情基础行，平台权限已在上层 Controller 校验。 */
    MerchantOrderRow selectPlatformOrderDetail(@Param("orderId") Long orderId);

    /** 查询订单全部明细，套餐快照展示字段由应用服务补齐。 */
    List<PlatformOrderItemView> selectPlatformOrderItems(@Param("orderId") Long orderId);

    /** 查询订单最近一次归还验机记录。 */
    PlatformOrderReturnInspectionView selectPlatformOrderReturnInspection(@Param("orderId") Long orderId);

    /** 查询平台订单批注历史。 */
    List<MerchantOrderNoteVO> selectPlatformOrderNotes(@Param("orderId") Long orderId);

    /** 查询平台订单字段变更历史。 */
    List<MerchantOrderEditLogVO> selectPlatformOrderEditLogs(@Param("orderId") Long orderId);
}
