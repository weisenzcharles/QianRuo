package com.ruoyi.rentalbusiness.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.api.RemoteMerchantUserService;
import com.ruoyi.merchant.api.model.MerchantUserSummary;
import com.ruoyi.merchant.api.model.MerchantUserSummaryQuery;
import com.ruoyi.rentalbusiness.enums.MerchantWorkAssignmentStatus;
import com.ruoyi.rentalbusiness.enums.MerchantWorkReviewResult;
import com.ruoyi.rentalbusiness.enums.MerchantWorkTaskSourceType;
import com.ruoyi.rentalbusiness.enums.MerchantWorkTaskStatus;
import com.ruoyi.rentalbusiness.enums.MerchantWorkTaskType;
import com.ruoyi.rentalbusiness.mapper.MerchantWorkTaskMapper;
import com.ruoyi.rentalbusiness.model.MerchantWorkTaskCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantWorkTaskReviewCommand;
import com.ruoyi.rentalbusiness.model.MerchantWorkTaskSubmissionCommand;

/** 商户通用工作台应用服务。任务提交和质检记录只追加，不覆盖历史事实。 */
@Service
public class MerchantWorkTaskService
{
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final Logger log = LoggerFactory.getLogger(MerchantWorkTaskService.class);

    private final MerchantWorkTaskMapper mapper;
    private final RemoteMerchantUserService remoteMerchantUserService;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public MerchantWorkTaskService(MerchantWorkTaskMapper mapper, RemoteMerchantUserService remoteMerchantUserService,
            PlatformTransactionManager transactionManager)
    {
        this.mapper = mapper;
        this.remoteMerchantUserService = remoteMerchantUserService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /** 供不启动 Spring 容器的单元测试使用；生产环境由三参数构造器注入事务管理器。 */
    public MerchantWorkTaskService(MerchantWorkTaskMapper mapper, RemoteMerchantUserService remoteMerchantUserService)
    {
        this.mapper = mapper;
        this.remoteMerchantUserService = remoteMerchantUserService;
        this.transactionTemplate = null;
    }

    /** 查询当前用户可见的任务列表。 */
    public List<Map<String, Object>> list(Long warehouseId, Integer status, String keyword)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (warehouseId != null)
        {
            MerchantContextUtils.checkWarehouseAccess(warehouseId);
        }
        if (status != null && MerchantWorkTaskStatus.fromCode(status) == null)
        {
            throw new ServiceException("任务状态不合法");
        }
        List<Map<String, Object>> rows = mapper.selectTaskList(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), SecurityUtils.getUserId(), warehouseId, status, trim(keyword),
                MerchantContextUtils.getWarehouseIds(), MerchantContextUtils.hasAllWarehouseAccess());
        return decorateTasks(rows);
    }

    /** 查询当前用户可见的工作台汇总。 */
    public Map<String, Object> summary()
    {
        MerchantContextUtils.requireMerchantApproved();
        Map<String, Object> result = mapper.selectSummary(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), SecurityUtils.getUserId(), MerchantContextUtils.getWarehouseIds(),
                MerchantContextUtils.hasAllWarehouseAccess());
        return result == null ? new LinkedHashMap<>() : result;
    }

    /**
     * 查询指定日期的工作日报。日报只统计当天新增指派的任务，质检分数来自当天产生的质检记录。
     *
     * @param reportDate 统计日期，为空时使用业务时区当天
     * @param warehouseId 关联仓库，为空时统计当前用户可见的全部仓库
     * @return 按执行人汇总的日报和整体汇总
     */
    public Map<String, Object> dailyReport(LocalDate reportDate, Long warehouseId)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (warehouseId != null)
        {
            MerchantContextUtils.checkWarehouseAccess(warehouseId);
        }
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate actualDate = reportDate == null ? today : reportDate;
        if (actualDate.isAfter(today))
        {
            throw new ServiceException("日报日期不能晚于今天");
        }
        List<Map<String, Object>> sourceRows = mapper.selectDailyReport(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), SecurityUtils.getUserId(), actualDate, warehouseId,
                MerchantContextUtils.getWarehouseIds(), MerchantContextUtils.hasAllWarehouseAccess());
        List<Map<String, Object>> rows = new ArrayList<>();
        long assignedCount = 0L;
        long completedCount = 0L;
        long pendingCount = 0L;
        long overdueCount = 0L;
        long reviewCount = 0L;
        BigDecimal scoreTotal = BigDecimal.ZERO;
        if (sourceRows != null)
        {
            Map<Long, String> userNames = resolveUserNames(collectUserIds(sourceRows, "userId"));
            for (Map<String, Object> sourceRow : sourceRows)
            {
                Map<String, Object> row = new LinkedHashMap<>(sourceRow);
                row.put("userName", resolveDisplayName(row.get("userId"), userNames, row.get("userName"), "未命名用户"));
                row.remove("userId");
                assignedCount += numberValue(row.get("assignedCount"));
                completedCount += numberValue(row.get("completedCount"));
                pendingCount += numberValue(row.get("pendingCount"));
                overdueCount += numberValue(row.get("overdueCount"));
                reviewCount += numberValue(row.get("scoreCount"));
                scoreTotal = scoreTotal.add(decimalValue(row.get("scoreTotal")));
                row.remove("scoreCount");
                row.remove("scoreTotal");
                rows.add(row);
            }
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("assignedCount", assignedCount);
        summary.put("completedCount", completedCount);
        summary.put("pendingCount", pendingCount);
        summary.put("overdueCount", overdueCount);
        summary.put("reviewCount", reviewCount);
        summary.put("averageScore", reviewCount == 0 ? BigDecimal.ZERO
                : scoreTotal.divide(BigDecimal.valueOf(reviewCount), 2, RoundingMode.HALF_UP));
        summary.put("completionRate", assignedCount == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(completedCount).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(assignedCount), 2, RoundingMode.HALF_UP));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reportDate", actualDate.toString());
        result.put("summary", summary);
        result.put("rows", rows);
        return result;
    }

    /** 查询商户内可被指派的有效用户。 */
    public List<Map<String, Object>> userOptions()
    {
        MerchantContextUtils.requireMerchantApproved();
        if (remoteMerchantUserService == null)
        {
            throw new ServiceException("商户用户服务不可用");
        }
        MerchantUserSummaryQuery query = new MerchantUserSummaryQuery();
        query.setTenantId(MerchantContextUtils.getTenantId());
        query.setMerchantId(MerchantContextUtils.getMemberId());
        R<List<MerchantUserSummary>> response = remoteMerchantUserService.getActiveSummaries(query,
                SecurityConstants.INNER, Constants.CLIENT_TYPE_MERCHANT);
        if (response == null || !R.isSuccess(response))
        {
            throw new ServiceException("获取可指派用户失败");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        List<MerchantUserSummary> summaries = response.getData();
        if (summaries != null)
        {
            for (MerchantUserSummary summary : summaries)
            {
                if (summary == null || summary.getUserId() == null)
                {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("userId", summary.getUserId());
                item.put("userName", resolveSummaryName(summary));
                item.put("loginName", summary.getUsername());
                result.add(item);
            }
        }
        return result;
    }

    /** 查询任务、指派、提交和质检时间线。 */
    public Map<String, Object> detail(Long taskId)
    {
        MerchantContextUtils.requireMerchantApproved();
        Map<String, Object> task = requireTask(taskId);
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        List<Map<String, Object>> assignments = mapper.selectAssignments(tenantId, memberId, taskId);
        List<Map<String, Object>> submissions = mapper.selectSubmissions(tenantId, memberId, taskId);
        List<Map<String, Object>> reviews = mapper.selectReviews(tenantId, memberId, taskId);
        Map<Long, String> userNames = resolveUserNames(collectUserIds(task, assignments, submissions, reviews));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("task", decorateTask(task, userNames));
        result.put("assignments", decorateAssignments(assignments, userNames));
        result.put("submissions", decorateSubmissions(submissions, userNames));
        result.put("reviews", decorateReviews(reviews, userNames));
        return result;
    }

    /** 创建任务并在同一事务中创建可选的执行人指派。 */
    public Map<String, Object> create(MerchantWorkTaskCreateCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null)
        {
            throw new ServiceException("任务内容不能为空");
        }
        String title = trim(command.title);
        String content = trim(command.content);
        if (title == null || title.length() > 128)
        {
            throw new ServiceException("任务标题不能为空且不能超过128个字符");
        }
        if (content == null || content.length() > 2000)
        {
            throw new ServiceException("任务内容不能为空且不能超过2000个字符");
        }
        MerchantWorkTaskSourceType sourceType = command.sourceType == null
                ? MerchantWorkTaskSourceType.MANAGEMENT : command.sourceType;
        MerchantWorkTaskType taskType = command.taskType == null ? MerchantWorkTaskType.OTHER : command.taskType;
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Long operatorId = SecurityUtils.getUserId();
        if (command.warehouseId != null)
        {
            MerchantContextUtils.checkWarehouseAccess(command.warehouseId);
        }
        Long assigneeUserId = command.assigneeUserId;
        if (sourceType == MerchantWorkTaskSourceType.PERSONAL)
        {
            assigneeUserId = operatorId;
        }
        if (assigneeUserId != null && !isActiveMerchantUser(tenantId, memberId, assigneeUserId))
        {
            throw new ServiceException("执行人不存在或已停用");
        }
        Long taskAssigneeUserId = assigneeUserId;
        if (transactionTemplate == null)
        {
            return persistTask(command, tenantId, memberId, operatorId, title, content, sourceType, taskType,
                    taskAssigneeUserId);
        }
        return transactionTemplate.execute(status -> persistTask(command, tenantId, memberId, operatorId, title,
                content, sourceType, taskType, taskAssigneeUserId));
    }

    /** 只在本地事务中写入任务及指派，远程用户校验已在事务外完成。 */
    private Map<String, Object> persistTask(MerchantWorkTaskCreateCommand command, Long tenantId, Long memberId,
            Long operatorId, String title, String content, MerchantWorkTaskSourceType sourceType,
            MerchantWorkTaskType taskType, Long assigneeUserId)
    {
        if (mapper.insertTask(tenantId, memberId, command.warehouseId, title, content, sourceType.getCode(),
                taskType.getCode(), command.dueTime, operatorId, trim(command.remark)) != 1)
        {
            throw new ServiceException("创建任务失败");
        }
        Long taskId = mapper.selectLastInsertId();
        if (taskId == null)
        {
            throw new ServiceException("创建任务失败");
        }
        if (assigneeUserId != null && mapper.insertAssignment(tenantId, memberId, taskId, assigneeUserId,
                operatorId) != 1)
        {
            throw new ServiceException("创建任务指派失败");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", taskId);
        return result;
    }

    /** 当前执行人提交完成说明；原提交记录保留。 */
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long taskId, MerchantWorkTaskSubmissionCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        String completionNote = command == null ? null : trim(command.completionNote);
        if (completionNote == null || completionNote.length() > 2000)
        {
            throw new ServiceException("完成说明不能为空且不能超过2000个字符");
        }
        String evidenceJson = command == null ? null : trim(command.evidenceJson);
        if (evidenceJson != null && evidenceJson.length() > 10000)
        {
            throw new ServiceException("完成凭证内容不能超过10000个字符");
        }
        Map<String, Object> task = requireTask(taskId);
        Integer taskStatus = intValue(task.get("taskStatus"));
        if (taskStatus == null || taskStatus == MerchantWorkTaskStatus.COMPLETED.getCode()
                || taskStatus == MerchantWorkTaskStatus.CLOSED.getCode()
                || taskStatus == MerchantWorkTaskStatus.WAITING_REVIEW.getCode())
        {
            throw new ServiceException("当前任务状态不允许提交");
        }
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> assignment = mapper.selectAssignmentForUpdate(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), taskId, userId);
        if (assignment == null)
        {
            throw new ServiceException("当前用户不是该任务执行人");
        }
        Integer assignmentStatus = intValue(assignment.get("assignmentStatus"));
        if (assignmentStatus == null || assignmentStatus == MerchantWorkAssignmentStatus.COMPLETED.getCode()
                || assignmentStatus == MerchantWorkAssignmentStatus.SUBMITTED.getCode())
        {
            throw new ServiceException("当前指派状态不允许提交");
        }
        Long assignmentId = longValue(assignment.get("assignmentId"));
        if (mapper.insertSubmission(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), taskId,
                assignmentId, userId, completionNote, evidenceJson) != 1)
        {
            throw new ServiceException("保存任务提交失败");
        }
        if (mapper.updateAssignmentStatus(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                assignmentId, assignmentStatus, MerchantWorkAssignmentStatus.SUBMITTED.getCode(), userId) != 1)
        {
            throw new ServiceException("更新任务指派状态失败");
        }
        if (mapper.updateTaskStatus(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), taskId,
                taskStatus, MerchantWorkTaskStatus.WAITING_REVIEW.getCode(), userId) != 1)
        {
            throw new ServiceException("更新任务状态失败");
        }
    }

    /** 创建任务质检记录并根据结果推进任务状态。 */
    @Transactional(rollbackFor = Exception.class)
    public void review(Long taskId, MerchantWorkTaskReviewCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.score == null || command.score < 0 || command.score > 100
                || command.reviewResult == null)
        {
            throw new ServiceException("质检评分必须在0至100分之间，并选择质检结果");
        }
        String reviewComment = trim(command.reviewComment);
        if (reviewComment != null && reviewComment.length() > 2000)
        {
            throw new ServiceException("质检意见不能超过2000个字符");
        }
        Map<String, Object> task = requireTask(taskId);
        Long reviewerId = SecurityUtils.getUserId();
        if (!reviewerId.equals(longValue(task.get("creatorUserId"))) && !MerchantContextUtils.hasAllWarehouseAccess())
        {
            throw new ServiceException("只有任务创建人或商户管理员可以质检");
        }
        if (!Integer.valueOf(MerchantWorkTaskStatus.WAITING_REVIEW.getCode()).equals(intValue(task.get("taskStatus"))))
        {
            throw new ServiceException("当前任务不在待审核状态");
        }
        Map<String, Object> submission = mapper.selectLatestSubmission(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), taskId);
        if (submission == null)
        {
            throw new ServiceException("当前任务没有可质检的提交记录");
        }
        Long submissionId = longValue(submission.get("submissionId"));
        Long assignmentId = longValue(submission.get("assignmentId"));
        if (mapper.insertReview(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), taskId,
                submissionId, reviewerId, command.score, command.reviewResult.getCode(), reviewComment) != 1)
        {
            throw new ServiceException("保存质检结果失败");
        }
        int assignmentTarget = command.reviewResult == MerchantWorkReviewResult.PASSED
                ? MerchantWorkAssignmentStatus.COMPLETED.getCode() : MerchantWorkAssignmentStatus.REJECTED.getCode();
        int taskTarget = command.reviewResult == MerchantWorkReviewResult.PASSED
                ? MerchantWorkTaskStatus.COMPLETED.getCode() : MerchantWorkTaskStatus.PROCESSING.getCode();
        if (mapper.updateAssignmentStatus(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                assignmentId, MerchantWorkAssignmentStatus.SUBMITTED.getCode(), assignmentTarget, reviewerId) != 1)
        {
            throw new ServiceException("更新指派质检状态失败");
        }
        if (mapper.updateTaskStatus(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), taskId,
                MerchantWorkTaskStatus.WAITING_REVIEW.getCode(), taskTarget, reviewerId) != 1)
        {
            throw new ServiceException("更新任务质检状态失败");
        }
    }

    /** 标记当前执行人的任务已阅。 */
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long taskId)
    {
        MerchantContextUtils.requireMerchantApproved();
        requireTask(taskId);
        if (mapper.markAssignmentRead(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), taskId,
                SecurityUtils.getUserId()) != 1)
        {
            throw new ServiceException("当前用户不是该任务执行人");
        }
    }

    private Map<String, Object> requireTask(Long taskId)
    {
        if (taskId == null)
        {
            throw new ServiceException("任务唯一编号不能为空");
        }
        Map<String, Object> task = mapper.selectTask(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), SecurityUtils.getUserId(), taskId,
                MerchantContextUtils.getWarehouseIds(), MerchantContextUtils.hasAllWarehouseAccess());
        if (task == null)
        {
            throw new ServiceException("任务不存在或无权访问");
        }
        return task;
    }

    private List<Map<String, Object>> decorateTasks(List<Map<String, Object>> rows)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<Long, String> userNames = resolveUserNames(collectUserIds(rows, "creatorUserId", "assigneeUserId"));
        if (rows != null)
        {
            for (Map<String, Object> row : rows)
            {
                result.add(decorateTask(row, userNames));
            }
        }
        return result;
    }

    private Map<String, Object> decorateTask(Map<String, Object> row, Map<Long, String> userNames)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        if (row == null)
        {
            return result;
        }
        result.putAll(row);
        result.put("creatorName", resolveDisplayName(row.get("creatorUserId"), userNames, row.get("creatorName"), "系统"));
        if (row.get("assigneeUserId") != null)
        {
            result.put("assigneeName", resolveDisplayName(row.get("assigneeUserId"), userNames,
                    row.get("assigneeName"), "未命名用户"));
        }
        else
        {
            result.put("assigneeName", null);
        }
        Integer status = intValue(row.get("taskStatus"));
        boolean overdue = Integer.valueOf(1).equals(intValue(row.get("overdueFlag")));
        MerchantWorkTaskStatus statusValue = overdue && status != null && status < MerchantWorkTaskStatus.COMPLETED.getCode()
                ? MerchantWorkTaskStatus.OVERDUE : MerchantWorkTaskStatus.fromCode(status);
        MerchantWorkTaskSourceType sourceValue = MerchantWorkTaskSourceType.fromCode(intValue(row.get("sourceType")));
        MerchantWorkTaskType typeValue = MerchantWorkTaskType.fromCode(intValue(row.get("taskType")));
        result.put("taskStatusLabel", statusValue == null ? "未知状态" : statusValue.getLabel());
        result.put("sourceTypeLabel", sourceValue == null ? "未知来源" : sourceValue.getLabel());
        result.put("taskTypeLabel", typeValue == null ? "未知类型" : typeValue.getLabel());
        result.put("overdueFlag", overdue ? 1 : 0);
        Integer assignmentStatus = intValue(row.get("assignmentStatus"));
        MerchantWorkAssignmentStatus assignmentValue = MerchantWorkAssignmentStatus.fromCode(assignmentStatus);
        result.put("assignmentStatusLabel", assignmentValue == null ? "未指派" : assignmentValue.getLabel());
        return result;
    }

    private List<Map<String, Object>> decorateAssignments(List<Map<String, Object>> rows, Map<Long, String> userNames)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        if (rows != null)
        {
            for (Map<String, Object> row : rows)
            {
                Map<String, Object> item = new LinkedHashMap<>(row);
                item.put("assigneeName", resolveDisplayName(row.get("assigneeUserId"), userNames,
                        row.get("assigneeName"), "未命名用户"));
                item.put("assignedByName", resolveDisplayName(row.get("assignedBy"), userNames,
                        row.get("assignedByName"), "系统"));
                MerchantWorkAssignmentStatus status = MerchantWorkAssignmentStatus.fromCode(intValue(row.get("assignmentStatus")));
                item.put("assignmentStatusLabel", status == null ? "未知状态" : status.getLabel());
                result.add(item);
            }
        }
        return result;
    }

    private List<Map<String, Object>> decorateSubmissions(List<Map<String, Object>> rows, Map<Long, String> userNames)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        if (rows != null)
        {
            for (Map<String, Object> row : rows)
            {
                Map<String, Object> item = new LinkedHashMap<>(row);
                item.put("submitterName", resolveDisplayName(row.get("submitterUserId"), userNames,
                        row.get("submitterName"), "系统"));
                result.add(item);
            }
        }
        return result;
    }

    private List<Map<String, Object>> decorateReviews(List<Map<String, Object>> rows, Map<Long, String> userNames)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        if (rows != null)
        {
            for (Map<String, Object> row : rows)
            {
                Map<String, Object> item = new LinkedHashMap<>(row);
                item.put("reviewerName", resolveDisplayName(row.get("reviewerUserId"), userNames,
                        row.get("reviewerName"), "系统"));
                MerchantWorkReviewResult review = MerchantWorkReviewResult.fromCode(intValue(row.get("reviewResult")));
                item.put("reviewResultLabel", review == null ? "未知结果" : review.getLabel());
                result.add(item);
            }
        }
        return result;
    }

    /** 批量解析工作台结果中的商户用户名称，业务服务不直接跨库读取商户用户表。 */
    private Map<Long, String> resolveUserNames(Set<Long> userIds)
    {
        Map<Long, String> names = new LinkedHashMap<>();
        if (userIds.isEmpty() || remoteMerchantUserService == null)
        {
            return names;
        }
        try
        {
            MerchantUserSummaryQuery query = new MerchantUserSummaryQuery();
            query.setTenantId(MerchantContextUtils.getTenantId());
            query.setMerchantId(MerchantContextUtils.getMemberId());
            query.setUserIds(userIds);
            R<List<MerchantUserSummary>> response = remoteMerchantUserService.getSummaries(query,
                    SecurityConstants.INNER, Constants.CLIENT_TYPE_MERCHANT);
            if (response == null || !R.isSuccess(response))
            {
                log.warn("工作台商户用户名称解析失败: code={}, message={}", response == null ? null : response.getCode(),
                        response == null ? null : response.getMsg());
                return names;
            }
            List<MerchantUserSummary> summaries = response.getData();
            if (summaries != null)
            {
                for (MerchantUserSummary summary : summaries)
                {
                    if (summary != null && summary.getUserId() != null)
                    {
                        String name = resolveSummaryName(summary);
                        if (name != null)
                        {
                            names.put(summary.getUserId(), name);
                        }
                    }
                }
            }
        }
        catch (RuntimeException exception)
        {
            log.warn("工作台商户用户名称解析失败，降级显示用户唯一编号", exception);
        }
        return names;
    }

    /** 校验任务指派人属于当前商户且账号仍启用。 */
    private boolean isActiveMerchantUser(Long tenantId, Long memberId, Long userId)
    {
        if (remoteMerchantUserService == null)
        {
            return false;
        }
        MerchantUserSummaryQuery query = new MerchantUserSummaryQuery();
        query.setTenantId(tenantId);
        query.setMerchantId(memberId);
        query.setUserIds(new LinkedHashSet<Long>(Set.of(userId)));
        R<List<MerchantUserSummary>> response = remoteMerchantUserService.getActiveSummaries(query,
                SecurityConstants.INNER, Constants.CLIENT_TYPE_MERCHANT);
        if (response == null || !R.isSuccess(response))
        {
            throw new ServiceException("校验执行人失败，请稍后重试");
        }
        List<MerchantUserSummary> summaries = response.getData();
        if (summaries != null)
        {
            for (MerchantUserSummary summary : summaries)
            {
                if (summary != null && userId.equals(summary.getUserId()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<Long> collectUserIds(List<Map<String, Object>> rows, String... fields)
    {
        Set<Long> userIds = new LinkedHashSet<Long>();
        if (rows != null)
        {
            for (Map<String, Object> row : rows)
            {
                if (row != null)
                {
                    for (String field : fields)
                    {
                        addUserId(userIds, row.get(field));
                    }
                }
            }
        }
        return userIds;
    }

    private Set<Long> collectUserIds(Map<String, Object> task, List<Map<String, Object>> assignments,
            List<Map<String, Object>> submissions, List<Map<String, Object>> reviews)
    {
        Set<Long> userIds = new LinkedHashSet<Long>();
        if (task != null)
        {
            addUserId(userIds, task.get("creatorUserId"));
            addUserId(userIds, task.get("assigneeUserId"));
        }
        addUserIds(userIds, assignments, "assigneeUserId", "assignedBy");
        addUserIds(userIds, submissions, "submitterUserId");
        addUserIds(userIds, reviews, "reviewerUserId");
        return userIds;
    }

    private void addUserIds(Set<Long> userIds, List<Map<String, Object>> rows, String... fields)
    {
        userIds.addAll(collectUserIds(rows, fields));
    }

    private void addUserId(Set<Long> userIds, Object value)
    {
        Long userId = longValue(value);
        if (userId != null)
        {
            userIds.add(userId);
        }
    }

    private String resolveDisplayName(Object userIdValue, Map<Long, String> userNames, Object existingName,
            String defaultName)
    {
        Long userId = longValue(userIdValue);
        String name = userId == null ? null : userNames.get(userId);
        if (name == null && existingName != null)
        {
            name = String.valueOf(existingName);
        }
        return name == null || name.trim().isEmpty() ? defaultName : name;
    }

    private String resolveSummaryName(MerchantUserSummary summary)
    {
        if (summary == null)
        {
            return null;
        }
        String name = summary.getNickname();
        if (name == null || name.trim().isEmpty())
        {
            name = summary.getUsername();
        }
        return name == null || name.trim().isEmpty() ? null : name;
    }

    private String trim(String value)
    {
        if (value == null)
        {
            return null;
        }
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }

    private Integer intValue(Object value)
    {
        return value == null ? null : Integer.valueOf(String.valueOf(value));
    }

    private Long longValue(Object value)
    {
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

    private long numberValue(Object value)
    {
        return value == null ? 0L : Long.parseLong(String.valueOf(value));
    }

    private BigDecimal decimalValue(Object value)
    {
        return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
    }
}
