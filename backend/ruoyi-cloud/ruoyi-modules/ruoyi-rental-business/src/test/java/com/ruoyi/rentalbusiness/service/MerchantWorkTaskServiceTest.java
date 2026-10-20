package com.ruoyi.rentalbusiness.service;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.merchant.api.RemoteMerchantUserService;
import com.ruoyi.merchant.api.model.MerchantUserSummary;
import com.ruoyi.rentalbusiness.enums.MerchantWorkReviewResult;
import com.ruoyi.rentalbusiness.enums.MerchantWorkTaskSourceType;
import com.ruoyi.rentalbusiness.enums.MerchantWorkTaskType;
import com.ruoyi.rentalbusiness.mapper.MerchantWorkTaskMapper;
import com.ruoyi.rentalbusiness.model.MerchantWorkTaskCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantWorkTaskReviewCommand;
import com.ruoyi.rentalbusiness.model.MerchantWorkTaskSubmissionCommand;
import com.ruoyi.system.api.model.LoginUser;

/** 商户工作台任务状态流转测试。 */
public class MerchantWorkTaskServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 个人任务必须自动指派给当前登录用户，不能由请求体指定其他执行人。 */
    @Test
    public void create_personalTask_assignsCurrentUser()
    {
        setMerchantContext(9001L);
        AtomicInteger assignedUser = new AtomicInteger();
        MerchantWorkTaskMapper mapper = mapper((method, args) -> {
            if ("selectUserOption".equals(method))
                return Map.of("userId", 9001L);
            if ("insertTask".equals(method))
                return 1;
            if ("selectLastInsertId".equals(method))
                return 77L;
            if ("insertAssignment".equals(method)) {
                assignedUser.set(((Long) args[3]).intValue());
                return 1;
            }
            return null;
        });
        MerchantWorkTaskCreateCommand command = new MerchantWorkTaskCreateCommand();
        command.title = "检查待归还设备";
        command.content = "完成设备外观和配件核验";
        command.sourceType = MerchantWorkTaskSourceType.PERSONAL;
        command.taskType = MerchantWorkTaskType.ORDER;
        command.assigneeUserId = 9010L;

        MerchantUserSummary summary = new MerchantUserSummary();
        summary.setUserId(9001L);
        summary.setNickname("发货员");
        RemoteMerchantUserService userService = remoteUserService((method, args) ->
        {
            if ("getActiveSummaries".equals(method))
                return R.ok(List.of(summary));
            return null;
        });
        Map<String, Object> result = new MerchantWorkTaskService(mapper, userService).create(command);

        require(Long.valueOf(77L).equals(result.get("taskId")), "create must return the new task id");
        require(assignedUser.get() == 9001, "personal task must assign the current user");
    }

    /** 执行人提交后应追加提交记录，并以条件更新任务和指派状态。 */
    @Test
    public void submit_pendingAssignment_movesTaskToReview()
    {
        setMerchantContext(9001L);
        AtomicInteger submissionCount = new AtomicInteger();
        AtomicInteger assignmentUpdateCount = new AtomicInteger();
        AtomicInteger taskUpdateCount = new AtomicInteger();
        MerchantWorkTaskMapper mapper = mapper((method, args) -> {
            if ("selectTask".equals(method))
                return Map.of("taskId", 77L, "taskStatus", 0, "creatorUserId", 9001L);
            if ("selectAssignmentForUpdate".equals(method))
                return Map.of("assignmentId", 31L, "assignmentStatus", 0);
            if ("insertSubmission".equals(method)) {
                submissionCount.incrementAndGet();
                return 1;
            }
            if ("updateAssignmentStatus".equals(method)) {
                assignmentUpdateCount.incrementAndGet();
                return 1;
            }
            if ("updateTaskStatus".equals(method)) {
                taskUpdateCount.incrementAndGet();
                return 1;
            }
            return null;
        });
        MerchantWorkTaskSubmissionCommand command = new MerchantWorkTaskSubmissionCommand();
        command.completionNote = "已完成设备核验";

        new MerchantWorkTaskService(mapper, null).submit(77L, command);

        require(submissionCount.get() == 1, "submit must append one submission");
        require(assignmentUpdateCount.get() == 1, "submit must update the assignment status once");
        require(taskUpdateCount.get() == 1, "submit must update the task status once");
    }

    /** 质检评分超出0至100分时必须拒绝，不能写入质检记录。 */
    @Test
    public void review_scoreOutOfRange_rejectsRequest()
    {
        setMerchantContext(9001L);
        AtomicInteger reviewCount = new AtomicInteger();
        MerchantWorkTaskMapper mapper = mapper((method, args) -> {
            if ("insertReview".equals(method)) {
                reviewCount.incrementAndGet();
                return 1;
            }
            return null;
        });
        MerchantWorkTaskReviewCommand command = new MerchantWorkTaskReviewCommand();
        command.score = 101;
        command.reviewResult = MerchantWorkReviewResult.PASSED;

        try
        {
            new MerchantWorkTaskService(mapper, null).review(77L, command);
            throw new AssertionError("review must reject score above 100");
        }
        catch (ServiceException exception)
        {
            require(exception.getMessage().contains("0至100"), "review must explain the score range");
        }
        require(reviewCount.get() == 0, "invalid review must not write a review record");
    }

    /** 日报应按执行人汇总，并移除仅用于计算整体分数的内部字段。 */
    @Test
    public void dailyReport_aggregatesRowsAndSummary()
    {
        setMerchantContext(9001L);
        MerchantWorkTaskMapper mapper = mapper((method, args) -> {
            if ("selectDailyReport".equals(method))
            {
                return List.of(Map.of("userName", "发货员", "assignedCount", 2L, "completedCount", 1L,
                        "pendingCount", 1L, "overdueCount", 0L, "scoreCount", 1L, "scoreTotal", 90,
                        "averageScore", 90));
            }
            return null;
        });

        Map<String, Object> result = new MerchantWorkTaskService(mapper, null)
                .dailyReport(LocalDate.now(), null);
        Map<String, Object> summary = (Map<String, Object>) result.get("summary");
        List<Map<String, Object>> rows = (List<Map<String, Object>>) result.get("rows");

        require(Long.valueOf(2L).equals(summary.get("assignedCount")), "report must aggregate assigned count");
        require(Long.valueOf(1L).equals(summary.get("completedCount")), "report must aggregate completed count");
        require("90.00".equals(String.valueOf(summary.get("averageScore"))), "report must calculate score");
        require(rows.size() == 1 && !rows.get(0).containsKey("scoreTotal"),
                "report rows must not expose calculation-only fields");
    }

    private void setMerchantContext(Long userId)
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(userId);
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setTenantId(10001L);
        loginUser.setMemberId(27L);
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        loginUser.setWarehouseScopeType(RentalCode.WarehouseScope.ALL.getCode());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
        SecurityContextHolder.setUserId(String.valueOf(userId));
    }

    private MerchantWorkTaskMapper mapper(MapperCall mapperCall)
    {
        return (MerchantWorkTaskMapper) Proxy.newProxyInstance(MerchantWorkTaskMapper.class.getClassLoader(),
                new Class<?>[] {MerchantWorkTaskMapper.class},
                (proxy, method, args) -> mapperCall.invoke(method.getName(), args));
    }

    private RemoteMerchantUserService remoteUserService(RemoteCall remoteCall)
    {
        return (RemoteMerchantUserService) Proxy.newProxyInstance(RemoteMerchantUserService.class.getClassLoader(),
                new Class<?>[] {RemoteMerchantUserService.class},
                (proxy, method, args) -> remoteCall.invoke(method.getName(), args));
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
            throw new AssertionError(message);
    }

    @FunctionalInterface
    private interface MapperCall
    {
        Object invoke(String method, Object[] args);
    }

    @FunctionalInterface
    private interface RemoteCall
    {
        Object invoke(String method, Object[] args);
    }
}
