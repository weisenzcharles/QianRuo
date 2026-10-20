package com.ruoyi.merchant.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.merchant.model.MerchantKnowledgeArticleQuery;
import com.ruoyi.merchant.model.MerchantKnowledgeArticleVO;
import com.ruoyi.merchant.model.MerchantKnowledgeAttachmentCommand;
import com.ruoyi.merchant.model.MerchantKnowledgeAttachmentVO;
import com.ruoyi.merchant.model.MerchantKnowledgeCategoryVO;
import com.ruoyi.merchant.model.MerchantKnowledgeVersionVO;
import com.ruoyi.merchant.model.MerchantInternalNoticeRoleOptionVO;

/** 商户知识库数据访问组件，所有可写查询都显式带当前商户范围。 */
public interface MerchantKnowledgeMapper
{
    List<MerchantKnowledgeCategoryVO> selectCategories(@Param("memberId") Long memberId);

    List<MerchantInternalNoticeRoleOptionVO> selectRoleOptions(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId);

    int countValidRoles(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("roleIds") Long[] roleIds);

    MerchantKnowledgeCategoryVO selectCategory(@Param("memberId") Long memberId,
            @Param("categoryId") Long categoryId);

    MerchantKnowledgeCategoryVO selectOwnCategory(@Param("memberId") Long memberId,
            @Param("categoryId") Long categoryId);

    int countCategoryChildren(@Param("memberId") Long memberId, @Param("categoryId") Long categoryId);

    int countCategoryArticles(@Param("memberId") Long memberId, @Param("categoryId") Long categoryId);

    int insertCategory(@Param("memberId") Long memberId, @Param("parentId") Long parentId,
            @Param("categoryName") String categoryName, @Param("sortNo") Integer sortNo,
            @Param("status") Integer status, @Param("operator") Long operator);

    int updateCategory(@Param("memberId") Long memberId, @Param("categoryId") Long categoryId,
            @Param("parentId") Long parentId, @Param("categoryName") String categoryName,
            @Param("sortNo") Integer sortNo, @Param("status") Integer status,
            @Param("operator") Long operator);

    int deleteCategory(@Param("memberId") Long memberId, @Param("categoryId") Long categoryId,
            @Param("operator") Long operator);

    List<MerchantKnowledgeArticleVO> selectArticles(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("merchantUserId") Long merchantUserId,
            @Param("query") MerchantKnowledgeArticleQuery query);

    MerchantKnowledgeArticleVO selectArticle(@Param("memberId") Long memberId,
            @Param("tenantId") Long tenantId,
            @Param("merchantUserId") Long merchantUserId,
            @Param("articleId") Long articleId);

    MerchantKnowledgeArticleVO selectOwnArticleForUpdate(@Param("memberId") Long memberId,
            @Param("tenantId") Long tenantId,
            @Param("articleId") Long articleId);

    List<MerchantKnowledgeVersionVO> selectVersions(@Param("memberId") Long memberId,
            @Param("tenantId") Long tenantId,
            @Param("merchantUserId") Long merchantUserId,
            @Param("articleId") Long articleId);

    List<MerchantKnowledgeAttachmentVO> selectVersionAttachments(@Param("versionId") Long versionId);

    List<Long> selectArticleRoleIds(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("articleId") Long articleId);

    int insertArticle(@Param("memberId") Long memberId, @Param("categoryId") Long categoryId,
            @Param("title") String title, @Param("audienceType") Integer audienceType,
            @Param("pinned") Integer pinned, @Param("operator") Long operator);

    Long selectLastInsertId();

    int insertVersion(@Param("articleId") Long articleId, @Param("versionNo") Integer versionNo,
            @Param("content") String content, @Param("versionStatus") Integer versionStatus,
            @Param("operator") Long operator);

    Long selectVersionId(@Param("articleId") Long articleId, @Param("versionNo") Integer versionNo);

    int insertVersionAttachments(@Param("versionId") Long versionId,
            @Param("attachments") List<MerchantKnowledgeAttachmentCommand> attachments,
            @Param("operator") Long operator);

    int deleteArticleRoles(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("articleId") Long articleId, @Param("operator") Long operator);

    int insertArticleRoles(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("articleId") Long articleId, @Param("roleIds") Long[] roleIds,
            @Param("operator") Long operator);

    int updateArticle(@Param("memberId") Long memberId, @Param("articleId") Long articleId,
            @Param("categoryId") Long categoryId, @Param("title") String title,
            @Param("audienceType") Integer audienceType, @Param("pinned") Integer pinned,
            @Param("versionNo") Integer versionNo,
            @Param("operator") Long operator);

    int updateArticleStatus(@Param("memberId") Long memberId, @Param("articleId") Long articleId,
            @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus,
            @Param("operator") Long operator);

    int updateVersionStatus(@Param("memberId") Long memberId, @Param("articleId") Long articleId,
            @Param("versionStatus") Integer versionStatus, @Param("operator") Long operator);

    int deleteArticle(@Param("memberId") Long memberId, @Param("articleId") Long articleId,
            @Param("operator") Long operator);

    int insertReadRecord(@Param("articleId") Long articleId, @Param("readerUserId") Long readerUserId);
}
