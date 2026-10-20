package com.ruoyi.rentalbusiness.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.ibatis.annotations.Param;

/** 商户通用工作台数据访问。所有查询均带租户、商户和当前用户可见范围。 */
public interface MerchantWorkTaskMapper
{
    Long selectLastInsertId();

    List<Map<String, Object>> selectTaskList(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("currentUserId") Long currentUserId, @Param("warehouseId") Long warehouseId,
            @Param("status") Integer status, @Param("keyword") String keyword,
            @Param("warehouseIds") Set<Long> warehouseIds, @Param("allWarehouseAccess") boolean allWarehouseAccess);

    Map<String, Object> selectSummary(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("currentUserId") Long currentUserId, @Param("warehouseIds") Set<Long> warehouseIds,
            @Param("allWarehouseAccess") boolean allWarehouseAccess);

    List<Map<String, Object>> selectDailyReport(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("currentUserId") Long currentUserId, @Param("reportDate") LocalDate reportDate,
            @Param("warehouseId") Long warehouseId, @Param("warehouseIds") Set<Long> warehouseIds,
            @Param("allWarehouseAccess") boolean allWarehouseAccess);

    Map<String, Object> selectTask(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("currentUserId") Long currentUserId, @Param("taskId") Long taskId,
            @Param("warehouseIds") Set<Long> warehouseIds, @Param("allWarehouseAccess") boolean allWarehouseAccess);

    List<Map<String, Object>> selectAssignments(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("taskId") Long taskId);

    List<Map<String, Object>> selectSubmissions(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("taskId") Long taskId);

    List<Map<String, Object>> selectReviews(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("taskId") Long taskId);

    Map<String, Object> selectAssignmentForUpdate(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("taskId") Long taskId, @Param("userId") Long userId);

    Map<String, Object> selectLatestSubmission(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("taskId") Long taskId);

    int insertTask(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId, @Param("title") String title, @Param("content") String content,
            @Param("sourceType") Integer sourceType, @Param("taskType") Integer taskType,
            @Param("dueTime") LocalDateTime dueTime, @Param("operatorId") Long operatorId,
            @Param("remark") String remark);

    int insertAssignment(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("taskId") Long taskId, @Param("assigneeUserId") Long assigneeUserId,
            @Param("assignedBy") Long assignedBy);

    int insertSubmission(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("taskId") Long taskId, @Param("assignmentId") Long assignmentId,
            @Param("submitterUserId") Long submitterUserId, @Param("completionNote") String completionNote,
            @Param("evidenceJson") String evidenceJson);

    int insertReview(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("taskId") Long taskId, @Param("submissionId") Long submissionId,
            @Param("reviewerUserId") Long reviewerUserId, @Param("score") Integer score,
            @Param("reviewResult") Integer reviewResult, @Param("reviewComment") String reviewComment);

    int updateTaskStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("taskId") Long taskId, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("operatorId") Long operatorId);

    int updateAssignmentStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("assignmentId") Long assignmentId, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("operatorId") Long operatorId);

    int markAssignmentRead(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("taskId") Long taskId, @Param("userId") Long userId);
}
