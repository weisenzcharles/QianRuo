package com.ruoyi.system.rental;

import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.mapper.rental.PlatformMarketMapper;
import com.ruoyi.system.model.rental.PlatformMarketPostVO;
import com.ruoyi.system.model.rental.PlatformMarketQuery;
import com.ruoyi.system.model.rental.PlatformMarketReportReviewCommand;
import com.ruoyi.system.model.rental.PlatformMarketReportVO;
import com.ruoyi.system.service.rental.PlatformMarketService;

/** 平台周转市场状态中文化和举报处理测试。 */
public class PlatformMarketServiceTest
{
    private StubMarketMapper mapper;
    private PlatformMarketService service;

    @Before
    public void setUp()
    {
        SecurityContextHolder.setUserId("1");
        mapper = new StubMarketMapper();
        service = new PlatformMarketService(mapper);
    }

    @After
    public void tearDown()
    {
        SecurityContextHolder.remove();
    }

    @Test
    public void postsAddsChineseStatusLabels()
    {
        PlatformMarketPostVO post = new PlatformMarketPostVO();
        post.setBoardType(Integer.valueOf(2));
        post.setPostStatus(Integer.valueOf(1));
        mapper.posts = Collections.singletonList(post);

        PlatformMarketPostVO result = service.posts(new PlatformMarketQuery()).get(0);

        Assert.assertEquals("出租", result.getBoardTypeName());
        Assert.assertEquals("已发布", result.getPostStatusName());
    }

    @Test
    public void reviewReportCanOfflinePost()
    {
        PlatformMarketReportReviewCommand command = new PlatformMarketReportReviewCommand();
        command.setReportStatus(Integer.valueOf(1));
        command.setReviewResult("确认虚假信息");
        command.setOfflinePost(Boolean.TRUE);

        service.reviewReport(Long.valueOf(7L), command);

        Assert.assertEquals(Integer.valueOf(1), mapper.reviewedStatus);
        Assert.assertEquals(Integer.valueOf(3), mapper.postStatus);
        Assert.assertEquals(Long.valueOf(1L), mapper.operatorId);
    }

    @Test
    public void reviewReportRejectsProcessedReport()
    {
        mapper.report.setReportStatus(Integer.valueOf(1));
        PlatformMarketReportReviewCommand command = new PlatformMarketReportReviewCommand();
        command.setReportStatus(Integer.valueOf(2));
        command.setReviewResult("证据不足");

        try
        {
            service.reviewReport(Long.valueOf(7L), command);
            Assert.fail("processed report should not be reviewed again");
        }
        catch (ServiceException expected)
        {
            Assert.assertEquals("当前举报已经处理", expected.getMessage());
        }
    }

    private static class StubMarketMapper implements PlatformMarketMapper
    {
        private List<PlatformMarketPostVO> posts = Collections.emptyList();
        private final PlatformMarketReportVO report = report();
        private Integer reviewedStatus;
        private Integer postStatus;
        private Long operatorId;

        private static PlatformMarketReportVO report()
        {
            PlatformMarketReportVO row = new PlatformMarketReportVO();
            row.setReportId(Long.valueOf(7L));
            row.setPostId(Long.valueOf(8L));
            row.setReportStatus(Integer.valueOf(0));
            return row;
        }

        @Override public List<PlatformMarketPostVO> selectPosts(PlatformMarketQuery query) { return posts; }
        @Override public PlatformMarketPostVO selectPost(Long postId) { return post(); }
        @Override public List<PlatformMarketReportVO> selectReports(PlatformMarketQuery query) { return Collections.emptyList(); }
        @Override public PlatformMarketReportVO selectReport(Long reportId) { return report; }
        @Override public int reviewReport(Long reportId, Integer reportStatus, String reviewResult, Long reviewerId)
        { reviewedStatus = reportStatus; operatorId = reviewerId; return 1; }
        @Override public int updatePostStatus(Long postId, Integer status, Long operator)
        { postStatus = status; operatorId = operator; return 1; }
        @Override public int updatePostRank(Long postId, Integer downRankFlag, Long operator) { return 1; }

        private PlatformMarketPostVO post()
        {
            PlatformMarketPostVO row = new PlatformMarketPostVO();
            row.setPostId(Long.valueOf(8L));
            row.setPostStatus(Integer.valueOf(1));
            return row;
        }
    }
}
