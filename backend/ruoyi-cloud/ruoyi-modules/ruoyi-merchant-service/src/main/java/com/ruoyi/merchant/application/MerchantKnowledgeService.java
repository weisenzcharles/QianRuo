package com.ruoyi.merchant.application;

import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.mapper.MerchantKnowledgeMapper;
import com.ruoyi.merchant.model.MerchantKnowledgeArticleCommand;
import com.ruoyi.merchant.model.MerchantKnowledgeArticleQuery;
import com.ruoyi.merchant.model.MerchantKnowledgeArticleVO;
import com.ruoyi.merchant.model.MerchantKnowledgeAudienceType;
import com.ruoyi.merchant.model.MerchantKnowledgeAttachmentCommand;
import com.ruoyi.merchant.model.MerchantKnowledgeCategoryCommand;
import com.ruoyi.merchant.model.MerchantKnowledgeCategoryStatus;
import com.ruoyi.merchant.model.MerchantKnowledgeCategoryVO;
import com.ruoyi.merchant.model.MerchantKnowledgeDetailVO;
import com.ruoyi.merchant.model.MerchantKnowledgePinned;
import com.ruoyi.merchant.model.MerchantKnowledgeSourceType;
import com.ruoyi.merchant.model.MerchantKnowledgeStatus;
import com.ruoyi.merchant.model.MerchantKnowledgeVersionVO;

/**
 * 商户私有知识库应用服务。
 *
 * <p>商户文章和分类严格按当前登录商户隔离，平台文章只开放已发布内容；版本记录保留在共享知识库表中。</p>
 */
@Service
public class MerchantKnowledgeService
{
    private static final int MAX_CATEGORY_DEPTH = 32;
    private static final int MAX_TITLE_LENGTH = 160;
    private static final int MAX_CONTENT_LENGTH = 100000;
    private static final int MAX_ATTACHMENT_COUNT = 10;
    private static final int MAX_ATTACHMENT_NAME_LENGTH = 255;
    private static final int MAX_ATTACHMENT_URL_LENGTH = 1000;

    private final MerchantKnowledgeMapper mapper;

    public MerchantKnowledgeService(MerchantKnowledgeMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 查询平台公共分类和当前商户自建分类。 */
    public List<MerchantKnowledgeCategoryVO> categories()
    {
        requireApproved();
        List<MerchantKnowledgeCategoryVO> rows = mapper.selectCategories(memberId());
        if (rows == null)
        {
            return Collections.emptyList();
        }
        rows.forEach(this::decorateCategory);
        return rows;
    }

    /** 查询当前商户可作为知识文章受众的员工组。 */
    public List<com.ruoyi.merchant.model.MerchantInternalNoticeRoleOptionVO> roleOptions()
    {
        requireApproved();
        List<com.ruoyi.merchant.model.MerchantInternalNoticeRoleOptionVO> rows = mapper.selectRoleOptions(tenantId(), memberId());
        return rows == null ? Collections.emptyList() : rows;
    }

    /** 新增当前商户的知识分类。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantKnowledgeCategoryVO createCategory(MerchantKnowledgeCategoryCommand command)
    {
        requireApproved();
        NormalizedCategory normalized = normalizeCategory(command, null);
        try
        {
            if (mapper.insertCategory(memberId(), normalized.parentId(), normalized.name(), normalized.sortNo(),
                    normalized.status(), operatorId()) != 1)
            {
                throw new ServiceException("知识分类保存失败");
            }
        }
        catch (DuplicateKeyException exception)
        {
            throw new ServiceException("同级知识分类名称已存在");
        }
        Long categoryId = mapper.selectLastInsertId();
        MerchantKnowledgeCategoryVO result = categoryId == null ? null : mapper.selectOwnCategory(memberId(), categoryId);
        if (result == null)
        {
            throw new ServiceException("知识分类保存后读取失败");
        }
        decorateCategory(result);
        return result;
    }

    /** 修改当前商户的知识分类。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantKnowledgeCategoryVO updateCategory(MerchantKnowledgeCategoryCommand command)
    {
        requireApproved();
        if (command == null || command.getCategoryId() == null || command.getCategoryId() <= 0)
        {
            throw new ServiceException("知识分类唯一编号不能为空");
        }
        MerchantKnowledgeCategoryVO before = mapper.selectOwnCategory(memberId(), command.getCategoryId());
        if (before == null)
        {
            throw new ServiceException("知识分类不存在或无权修改");
        }
        NormalizedCategory normalized = normalizeCategory(command, command.getCategoryId());
        try
        {
            if (mapper.updateCategory(memberId(), command.getCategoryId(), normalized.parentId(), normalized.name(),
                    normalized.sortNo(), normalized.status(), operatorId()) != 1)
            {
                throw new ServiceException("知识分类修改失败");
            }
        }
        catch (DuplicateKeyException exception)
        {
            throw new ServiceException("同级知识分类名称已存在");
        }
        MerchantKnowledgeCategoryVO result = mapper.selectOwnCategory(memberId(), command.getCategoryId());
        if (result == null)
        {
            throw new ServiceException("知识分类修改后读取失败");
        }
        decorateCategory(result);
        return result;
    }

    /** 删除当前商户分类，分类下仍有子分类或文章时拒绝删除。 */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId)
    {
        requireApproved();
        MerchantKnowledgeCategoryVO category = mapper.selectOwnCategory(memberId(), categoryId);
        if (category == null)
        {
            throw new ServiceException("知识分类不存在或无权删除");
        }
        if (mapper.countCategoryChildren(memberId(), categoryId) > 0
                || mapper.countCategoryArticles(memberId(), categoryId) > 0)
        {
            throw new ServiceException("分类下仍有子分类或文章，不能删除");
        }
        if (mapper.deleteCategory(memberId(), categoryId, operatorId()) != 1)
        {
            throw new ServiceException("知识分类删除失败");
        }
    }

    /** 分页查询当前商户自己的文章和已发布的平台公共文章。 */
    public List<MerchantKnowledgeArticleVO> list(MerchantKnowledgeArticleQuery query)
    {
        requireApproved();
        MerchantKnowledgeArticleQuery safeQuery = query == null ? new MerchantKnowledgeArticleQuery() : query;
        safeQuery.setTitle(trim(safeQuery.getTitle()));
        validateArticleFilters(safeQuery);
        if (safeQuery.getCategoryId() != null && mapper.selectCategory(memberId(), safeQuery.getCategoryId()) == null)
        {
            throw new ServiceException("知识分类不存在或无权访问");
        }
        List<MerchantKnowledgeArticleVO> rows = mapper.selectArticles(tenantId(), memberId(), operatorId(), safeQuery);
        if (rows == null)
        {
            return Collections.emptyList();
        }
        rows.forEach(this::decorateArticle);
        return rows;
    }

    /** 查询文章详情及历史版本。平台文章只能读取已发布内容。 */
    public MerchantKnowledgeDetailVO detail(Long articleId)
    {
        requireApproved();
        MerchantKnowledgeArticleVO article = requireReadableArticle(articleId);
        article.setRoleIds(mapper.selectArticleRoleIds(tenantId(), memberId(), articleId));
        List<MerchantKnowledgeVersionVO> versions = mapper.selectVersions(memberId(), tenantId(), operatorId(), articleId);
        MerchantKnowledgeDetailVO result = new MerchantKnowledgeDetailVO();
        result.setArticle(article);
        result.setVersions(versions == null ? Collections.emptyList() : versions.stream()
                .peek(this::decorateVersion).collect(Collectors.toList()));
        result.getVersions().forEach(version -> version.setAttachments(
                emptyIfNull(mapper.selectVersionAttachments(version.getVersionId()))));
        return result;
    }

    /** 新增商户知识文章并生成第一版草稿。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantKnowledgeArticleVO create(MerchantKnowledgeArticleCommand command)
    {
        requireApproved();
        NormalizedArticle normalized = normalizeArticle(command);
        if (mapper.insertArticle(memberId(), normalized.categoryId(), normalized.title(), normalized.audienceType(),
                normalized.pinned(), operatorId()) != 1)
        {
            throw new ServiceException("知识文章创建失败");
        }
        Long articleId = mapper.selectLastInsertId();
        if (articleId == null || mapper.insertVersion(articleId, 1, normalized.content(),
                MerchantKnowledgeStatus.DRAFT.getCode(), operatorId()) != 1)
        {
            throw new ServiceException("知识文章版本写入失败");
        }
        syncAudienceRoles(articleId, normalized.audienceType(), normalized.roleIds());
        appendAttachments(articleId, 1, normalized.attachments());
        return requireOwnArticle(articleId);
    }

    /** 修改商户文章并追加一个新版本，历史版本不会被覆盖。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantKnowledgeArticleVO update(MerchantKnowledgeArticleCommand command)
    {
        requireApproved();
        if (command == null || command.getArticleId() == null || command.getArticleId() <= 0)
        {
            throw new ServiceException("知识文章唯一编号不能为空");
        }
        MerchantKnowledgeArticleVO before = requireOwnArticleForUpdate(command.getArticleId());
        NormalizedArticle normalized = normalizeArticle(command);
        int versionNo = before.getCurrentVersionNo() == null ? 1 : before.getCurrentVersionNo() + 1;
        if (mapper.updateArticle(memberId(), command.getArticleId(), normalized.categoryId(), normalized.title(),
                normalized.audienceType(), normalized.pinned(), versionNo, operatorId()) != 1
                || mapper.insertVersion(command.getArticleId(), versionNo, normalized.content(),
                        MerchantKnowledgeStatus.DRAFT.getCode(), operatorId()) != 1)
        {
            throw new ServiceException("知识文章修改失败");
        }
        syncAudienceRoles(command.getArticleId(), normalized.audienceType(), normalized.roleIds());
        appendAttachments(command.getArticleId(), versionNo, normalized.attachments());
        return requireOwnArticle(command.getArticleId());
    }

    /** 发布商户文章当前草稿版本。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantKnowledgeArticleVO publish(Long articleId)
    {
        return changeStatus(articleId, MerchantKnowledgeStatus.PUBLISHED,
                new MerchantKnowledgeStatus[] { MerchantKnowledgeStatus.DRAFT, MerchantKnowledgeStatus.OFFLINE });
    }

    /** 下架商户已发布文章。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantKnowledgeArticleVO offline(Long articleId)
    {
        return changeStatus(articleId, MerchantKnowledgeStatus.OFFLINE,
                new MerchantKnowledgeStatus[] { MerchantKnowledgeStatus.PUBLISHED });
    }

    /** 删除商户自己的文章。平台公共文章不能通过商户接口删除。 */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long articleId)
    {
        requireApproved();
        requireOwnArticleForUpdate(articleId);
        if (mapper.deleteArticle(memberId(), articleId, operatorId()) != 1)
        {
            throw new ServiceException("知识文章删除失败");
        }
    }

    /** 记录当前商户用户的最近阅读时间。 */
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long articleId)
    {
        requireApproved();
        requireReadableArticle(articleId);
        Long readerUserId = operatorId();
        if (readerUserId == null || mapper.insertReadRecord(articleId, readerUserId) != 1)
        {
            throw new ServiceException("知识文章阅读记录保存失败");
        }
    }

    private MerchantKnowledgeArticleVO changeStatus(Long articleId, MerchantKnowledgeStatus target,
            MerchantKnowledgeStatus[] allowed)
    {
        requireApproved();
        MerchantKnowledgeArticleVO before = requireOwnArticleForUpdate(articleId);
        MerchantKnowledgeStatus current = MerchantKnowledgeStatus.fromCode(before.getStatus());
        if (current == null || !contains(allowed, current))
        {
            throw new ServiceException("当前知识文章状态不允许执行该操作");
        }
        if (mapper.updateArticleStatus(memberId(), articleId, current.getCode(), target.getCode(), operatorId()) != 1
                || mapper.updateVersionStatus(memberId(), articleId, target == MerchantKnowledgeStatus.PUBLISHED
                        ? MerchantKnowledgeStatus.PUBLISHED.getCode() : MerchantKnowledgeStatus.OFFLINE.getCode(), operatorId()) != 1)
        {
            throw new ServiceException("知识文章状态更新失败");
        }
        return requireOwnArticle(articleId);
    }

    private NormalizedCategory normalizeCategory(MerchantKnowledgeCategoryCommand command, Long categoryId)
    {
        if (command == null)
        {
            throw new ServiceException("知识分类内容不能为空");
        }
        String name = trim(command.getCategoryName());
        if (StringUtils.isBlank(name) || name.length() > 80)
        {
            throw new ServiceException("知识分类名称不能为空且不能超过80个字符");
        }
        Integer status = command.getStatus() == null ? MerchantKnowledgeCategoryStatus.NORMAL.getCode() : command.getStatus();
        if (MerchantKnowledgeCategoryStatus.fromCode(status) == null)
        {
            throw new ServiceException("知识分类状态不合法");
        }
        int sortNo = command.getSortNo() == null ? 0 : command.getSortNo();
        if (sortNo < 0 || sortNo > 999999)
        {
            throw new ServiceException("知识分类排序号不合法");
        }
        Long parentId = command.getParentId() == null || command.getParentId() <= 0 ? 0L : command.getParentId();
        validateParent(parentId, categoryId);
        return new NormalizedCategory(parentId, name, sortNo, status);
    }

    private NormalizedArticle normalizeArticle(MerchantKnowledgeArticleCommand command)
    {
        if (command == null)
        {
            throw new ServiceException("知识文章内容不能为空");
        }
        if (command.getCategoryId() == null || command.getCategoryId() <= 0)
        {
            throw new ServiceException("请选择知识分类");
        }
        MerchantKnowledgeCategoryVO category = mapper.selectCategory(memberId(), command.getCategoryId());
        if (category == null || category.getStatus() == null
                || category.getStatus().intValue() != MerchantKnowledgeCategoryStatus.NORMAL.getCode())
        {
            throw new ServiceException("知识分类不存在、已停用或无权使用");
        }
        String title = trim(command.getTitle());
        String content = trim(command.getContent());
        if (StringUtils.isBlank(title) || title.length() > MAX_TITLE_LENGTH)
        {
            throw new ServiceException("知识文章标题不能为空且不能超过160个字符");
        }
        if (StringUtils.isBlank(content) || content.length() > MAX_CONTENT_LENGTH)
        {
            throw new ServiceException("知识文章正文不能为空且不能超过100000个字符");
        }
        Integer pinned = command.getPinned() == null ? MerchantKnowledgePinned.NO.getCode() : command.getPinned();
        if (MerchantKnowledgePinned.fromCode(pinned) == null)
        {
            throw new ServiceException("知识文章置顶标记不合法");
        }
        Integer audienceType = command.getAudienceType() == null
                ? MerchantKnowledgeAudienceType.ALL.getCode() : command.getAudienceType();
        if (MerchantKnowledgeAudienceType.fromCode(audienceType) == null)
        {
            throw new ServiceException("知识文章受众范围不合法");
        }
        List<Long> roleIds = normalizeRoleIds(command.getRoleIds());
        if (audienceType == MerchantKnowledgeAudienceType.SPECIFIED_ROLES.getCode())
        {
            if (roleIds.isEmpty())
            {
                throw new ServiceException("指定员工组时必须选择员工组");
            }
            if (mapper.countValidRoles(tenantId(), memberId(), roleIds.toArray(new Long[0])) != roleIds.size())
            {
                throw new ServiceException("员工组不存在、已停用或不属于当前商户");
            }
        }
        else
        {
            roleIds = Collections.emptyList();
        }
        return new NormalizedArticle(command.getCategoryId(), title, content, pinned, audienceType,
                roleIds, normalizeAttachments(command.getAttachments()));
    }

    private List<Long> normalizeRoleIds(List<Long> roleIds)
    {
        if (roleIds == null || roleIds.isEmpty())
        {
            return Collections.emptyList();
        }
        Set<Long> unique = new LinkedHashSet<>();
        for (Long roleId : roleIds)
        {
            if (roleId == null || roleId <= 0 || !unique.add(roleId))
            {
                if (roleId == null || roleId <= 0)
                {
                    throw new ServiceException("员工组唯一编号不合法");
                }
            }
        }
        if (unique.size() > 20)
        {
            throw new ServiceException("最多选择20个员工组");
        }
        return new ArrayList<>(unique);
    }

    private List<MerchantKnowledgeAttachmentCommand> normalizeAttachments(
            List<MerchantKnowledgeAttachmentCommand> attachments)
    {
        if (attachments == null || attachments.isEmpty())
        {
            return Collections.emptyList();
        }
        if (attachments.size() > MAX_ATTACHMENT_COUNT)
        {
            throw new ServiceException("知识文章最多添加10个附件");
        }
        Map<String, MerchantKnowledgeAttachmentCommand> unique = new LinkedHashMap<>();
        for (MerchantKnowledgeAttachmentCommand attachment : attachments)
        {
            if (attachment == null)
            {
                throw new ServiceException("知识附件不能为空");
            }
            String url = trim(attachment.getFileUrl());
            if (StringUtils.isBlank(url) || url.length() > MAX_ATTACHMENT_URL_LENGTH
                    || url.toLowerCase(Locale.ROOT).startsWith("javascript:")
                    || url.toLowerCase(Locale.ROOT).startsWith("data:"))
            {
                throw new ServiceException("知识附件地址不合法");
            }
            String name = trim(attachment.getAttachmentName());
            if (StringUtils.isBlank(name))
            {
                int queryIndex = url.indexOf('?');
                String path = queryIndex >= 0 ? url.substring(0, queryIndex) : url;
                int slashIndex = path.lastIndexOf('/');
                name = slashIndex >= 0 ? path.substring(slashIndex + 1) : path;
            }
            if (StringUtils.isBlank(name) || name.length() > MAX_ATTACHMENT_NAME_LENGTH)
            {
                throw new ServiceException("知识附件名称不能为空且不能超过255个字符");
            }
            Long fileSize = attachment.getFileSize();
            if (fileSize != null && (fileSize < 0 || fileSize > 20L * 1024 * 1024))
            {
                throw new ServiceException("知识附件大小不能超过20MB");
            }
            MerchantKnowledgeAttachmentCommand normalized = new MerchantKnowledgeAttachmentCommand();
            normalized.setAttachmentName(name);
            normalized.setFileUrl(url);
            normalized.setFileType(trim(attachment.getFileType()));
            normalized.setFileSize(fileSize);
            unique.putIfAbsent(url, normalized);
        }
        return new ArrayList<>(unique.values());
    }

    private void syncAudienceRoles(Long articleId, Integer audienceType, List<Long> roleIds)
    {
        mapper.deleteArticleRoles(tenantId(), memberId(), articleId, operatorId());
        if (audienceType == MerchantKnowledgeAudienceType.SPECIFIED_ROLES.getCode() && !roleIds.isEmpty()
                && mapper.insertArticleRoles(tenantId(), memberId(), articleId, roleIds.toArray(new Long[0]), operatorId()) != roleIds.size())
        {
            throw new ServiceException("知识文章员工组受众保存失败");
        }
    }

    private void appendAttachments(Long articleId, Integer versionNo,
            List<MerchantKnowledgeAttachmentCommand> attachments)
    {
        if (attachments.isEmpty())
        {
            return;
        }
        Long versionId = mapper.selectVersionId(articleId, versionNo);
        if (versionId == null || mapper.insertVersionAttachments(versionId, attachments, operatorId()) != attachments.size())
        {
            throw new ServiceException("知识文章附件保存失败");
        }
    }

    private void validateArticleFilters(MerchantKnowledgeArticleQuery query)
    {
        if (query.getStatus() != null && MerchantKnowledgeStatus.fromCode(query.getStatus()) == null)
        {
            throw new ServiceException("知识文章状态不合法");
        }
        if (query.getSourceType() != null && MerchantKnowledgeSourceType.fromCode(query.getSourceType()) == null)
        {
            throw new ServiceException("知识文章来源类型不合法");
        }
    }

    private void validateParent(Long parentId, Long categoryId)
    {
        if (parentId == 0L)
        {
            return;
        }
        MerchantKnowledgeCategoryVO parent = mapper.selectCategory(memberId(), parentId);
        if (parent == null || parent.getStatus() == null
                || parent.getStatus().intValue() != MerchantKnowledgeCategoryStatus.NORMAL.getCode())
        {
            throw new ServiceException("上级知识分类不存在、已停用或无权使用");
        }
        Long currentId = parentId;
        for (int depth = 0; depth < MAX_CATEGORY_DEPTH && currentId != null && currentId > 0; depth++)
        {
            if (currentId.equals(categoryId))
            {
                throw new ServiceException("知识分类不能移动到自己的子分类下");
            }
            MerchantKnowledgeCategoryVO current = mapper.selectCategory(memberId(), currentId);
            if (current == null || current.getParentId() == null || current.getParentId() == 0)
            {
                return;
            }
            currentId = current.getParentId();
        }
        if (currentId != null && currentId > 0)
        {
            throw new ServiceException("知识分类层级不能超过32级");
        }
    }

    private MerchantKnowledgeArticleVO requireReadableArticle(Long articleId)
    {
        if (articleId == null || articleId <= 0)
        {
            throw new ServiceException("知识文章唯一编号不能为空");
        }
        MerchantKnowledgeArticleVO article = mapper.selectArticle(memberId(), tenantId(), operatorId(), articleId);
        if (article == null)
        {
            throw new ServiceException("知识文章不存在或无权访问");
        }
        decorateArticle(article);
        return article;
    }

    private MerchantKnowledgeArticleVO requireOwnArticle(Long articleId)
    {
        MerchantKnowledgeArticleVO article = mapper.selectArticle(memberId(), tenantId(), operatorId(), articleId);
        if (article == null || !Integer.valueOf(MerchantKnowledgeSourceType.MERCHANT.getCode()).equals(article.getSourceType()))
        {
            throw new ServiceException("知识文章保存后读取失败");
        }
        decorateArticle(article);
        return article;
    }

    private MerchantKnowledgeArticleVO requireOwnArticleForUpdate(Long articleId)
    {
        if (articleId == null || articleId <= 0)
        {
            throw new ServiceException("知识文章唯一编号不能为空");
        }
        MerchantKnowledgeArticleVO article = mapper.selectOwnArticleForUpdate(memberId(), tenantId(), articleId);
        if (article == null)
        {
            throw new ServiceException("知识文章不存在或无权操作");
        }
        decorateArticle(article);
        return article;
    }

    private void decorateCategory(MerchantKnowledgeCategoryVO category)
    {
        MerchantKnowledgeCategoryStatus status = MerchantKnowledgeCategoryStatus.fromCode(category.getStatus());
        category.setStatusName(status == null ? "未知状态" : status.getLabel());
    }

    private void decorateArticle(MerchantKnowledgeArticleVO article)
    {
        MerchantKnowledgeStatus status = MerchantKnowledgeStatus.fromCode(article.getStatus());
        MerchantKnowledgeSourceType source = MerchantKnowledgeSourceType.fromCode(article.getSourceType());
        article.setStatusName(status == null ? "未知状态" : status.getLabel());
        article.setSourceTypeName(source == null ? "未知来源" : source.getLabel());
        MerchantKnowledgeAudienceType audience = MerchantKnowledgeAudienceType.fromCode(article.getAudienceType());
        article.setAudienceTypeName(audience == null ? "未知范围" : audience.getLabel());
        if (article.getReadCount() == null)
        {
            article.setReadCount(0L);
        }
    }

    private void decorateVersion(MerchantKnowledgeVersionVO version)
    {
        MerchantKnowledgeStatus status = MerchantKnowledgeStatus.fromCode(version.getVersionStatus());
        version.setVersionStatusName(status == null ? "未知状态" : status.getLabel());
    }

    private boolean contains(MerchantKnowledgeStatus[] values, MerchantKnowledgeStatus target)
    {
        for (MerchantKnowledgeStatus value : values)
        {
            if (value == target)
            {
                return true;
            }
        }
        return false;
    }

    private void requireApproved()
    {
        MerchantContextUtils.requireMerchantApproved();
    }

    private Long memberId()
    {
        return MerchantContextUtils.getMemberId();
    }

    private Long operatorId()
    {
        return SecurityUtils.getUserId();
    }

    private Long tenantId()
    {
        return MerchantContextUtils.getTenantId();
    }

    private <T> List<T> emptyIfNull(List<T> rows)
    {
        return rows == null ? Collections.emptyList() : rows;
    }

    private String trim(String value)
    {
        return value == null ? null : value.trim();
    }

    private record NormalizedCategory(Long parentId, String name, int sortNo, Integer status)
    {
    }

    private record NormalizedArticle(Long categoryId, String title, String content, Integer pinned,
            Integer audienceType, List<Long> roleIds, List<MerchantKnowledgeAttachmentCommand> attachments)
    {
    }
}
