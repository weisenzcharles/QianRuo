package com.ruoyi.merchant.application;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.merchant.mapper.MerchantKnowledgeMapper;
import com.ruoyi.merchant.model.MerchantKnowledgeArticleCommand;
import com.ruoyi.merchant.model.MerchantKnowledgeArticleVO;
import com.ruoyi.merchant.model.MerchantKnowledgeAudienceType;
import com.ruoyi.merchant.model.MerchantKnowledgeCategoryVO;
import com.ruoyi.merchant.model.MerchantKnowledgeStatus;
import com.ruoyi.system.api.model.LoginUser;

/** 商户知识文章归属和状态流转测试。 */
public class MerchantKnowledgeServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 商户只能从自己的分类创建文章，并且只能按草稿到发布流转。 */
    @Test
    public void createAndPublish_usesMerchantScopeAndChineseStatus()
    {
        setMerchantContext();
        AtomicReference<Integer> status = new AtomicReference<>(MerchantKnowledgeStatus.DRAFT.getCode());
        MerchantKnowledgeMapper mapper = mapper((method, args) -> {
            if ("selectCategory".equals(method))
            {
                MerchantKnowledgeCategoryVO category = new MerchantKnowledgeCategoryVO();
                category.setCategoryId(3L);
                category.setStatus(0);
                return category;
            }
            if ("insertArticle".equals(method) || "insertVersion".equals(method)
                    || "updateVersionStatus".equals(method) || "deleteArticleRoles".equals(method))
            {
                return 1;
            }
            if ("selectLastInsertId".equals(method))
            {
                return 12L;
            }
            if ("selectArticle".equals(method) || "selectOwnArticleForUpdate".equals(method))
            {
                MerchantKnowledgeArticleVO article = new MerchantKnowledgeArticleVO();
                article.setArticleId(12L);
                article.setCategoryId(3L);
                article.setSourceType(2);
                article.setStatus(status.get());
                article.setCurrentVersionNo(1);
                return article;
            }
            if ("updateArticleStatus".equals(method))
            {
                status.set((Integer) args[3]);
                return 1;
            }
            throw new UnsupportedOperationException(method);
        });

        MerchantKnowledgeService service = new MerchantKnowledgeService(mapper);
        MerchantKnowledgeArticleCommand command = new MerchantKnowledgeArticleCommand();
        command.setCategoryId(3L);
        command.setTitle("设备清洁流程");
        command.setContent("归还验机后完成清洁和入库登记");

        MerchantKnowledgeArticleVO created = service.create(command);
        Assert.assertEquals("草稿", created.getStatusName());
        MerchantKnowledgeArticleVO published = service.publish(12L);
        Assert.assertEquals(Integer.valueOf(MerchantKnowledgeStatus.PUBLISHED.getCode()), published.getStatus());
        Assert.assertEquals("已发布", published.getStatusName());
    }

    /** 指定员工组时，服务端必须拒绝不属于当前商户的角色，不能只依赖前端下拉框。 */
    @Test
    public void createSpecifiedAudience_invalidRole_isRejected()
    {
        setMerchantContext();
        MerchantKnowledgeMapper mapper = mapper((method, args) -> {
            if ("selectCategory".equals(method))
            {
                MerchantKnowledgeCategoryVO category = new MerchantKnowledgeCategoryVO();
                category.setCategoryId(3L);
                category.setStatus(0);
                return category;
            }
            if ("countValidRoles".equals(method))
                return 0;
            throw new UnsupportedOperationException(method);
        });

        MerchantKnowledgeService service = new MerchantKnowledgeService(mapper);
        MerchantKnowledgeArticleCommand command = new MerchantKnowledgeArticleCommand();
        command.setCategoryId(3L);
        command.setTitle("内部设备交接");
        command.setContent("仅允许指定员工组查看");
        command.setAudienceType(MerchantKnowledgeAudienceType.SPECIFIED_ROLES.getCode());
        command.setRoleIds(Arrays.asList(999L));

        try
        {
            service.create(command);
            Assert.fail("不属于当前商户的员工组不应创建成功");
        }
        catch (ServiceException expected)
        {
            Assert.assertEquals("员工组不存在、已停用或不属于当前商户", expected.getMessage());
        }
    }

    private void setMerchantContext()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(9001L);
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setTenantId(10001L);
        loginUser.setMemberId(27L);
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        loginUser.setWarehouseScopeType(RentalCode.WarehouseScope.ALL.getCode());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
        SecurityContextHolder.setUserId(String.valueOf(loginUser.getUserid()));
    }

    private MerchantKnowledgeMapper mapper(MapperCall mapperCall)
    {
        return (MerchantKnowledgeMapper) Proxy.newProxyInstance(
                MerchantKnowledgeMapper.class.getClassLoader(), new Class<?>[] { MerchantKnowledgeMapper.class },
                (proxy, method, args) -> mapperCall.invoke(method.getName(), args));
    }

    @FunctionalInterface
    private interface MapperCall
    {
        Object invoke(String method, Object[] args);
    }
}
