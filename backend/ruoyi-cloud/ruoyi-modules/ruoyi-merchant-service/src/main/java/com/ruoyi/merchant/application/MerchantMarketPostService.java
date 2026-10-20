package com.ruoyi.merchant.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.mapper.MerchantMarketPostMapper;
import com.ruoyi.merchant.api.model.MerchantMarketInventoryUnitView;
import com.ruoyi.merchant.model.MerchantMarketBoardType;
import com.ruoyi.merchant.model.MerchantMarketModelOption;
import com.ruoyi.merchant.model.MerchantMarketPostCommand;
import com.ruoyi.merchant.model.MerchantMarketPostQuery;
import com.ruoyi.merchant.model.MerchantMarketPostRecord;
import com.ruoyi.merchant.model.MerchantMarketPostStatus;
import com.ruoyi.merchant.model.MerchantMarketPostVO;

/** 商户周转市场帖子应用服务。帖子只能操作当前商户的数据，状态由服务端统一流转。 */
@Service
public class MerchantMarketPostService
{
    private static final DateTimeFormatter POST_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final int MAX_TEXT_LENGTH = 128;

    private final MerchantMarketPostMapper mapper;
    private final MerchantBusinessAuditRecorder auditRecorder;
    private final MerchantMarketOptionsService optionsService;
    private final MerchantMarketInventoryService inventoryService;
    private final MerchantMarketDepositService depositService;

    public MerchantMarketPostService(MerchantMarketPostMapper mapper, MerchantBusinessAuditRecorder auditRecorder,
            MerchantMarketOptionsService optionsService, MerchantMarketInventoryService inventoryService,
            MerchantMarketDepositService depositService)
    {
        this.mapper = mapper;
        this.auditRecorder = auditRecorder;
        this.optionsService = optionsService;
        this.inventoryService = inventoryService;
        this.depositService = depositService;
    }

    /** 查询当前商户的市场帖子列表。 */
    public List<MerchantMarketPostVO> list(MerchantMarketPostQuery query)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantMarketPostQuery safeQuery = query == null ? new MerchantMarketPostQuery() : query;
        List<MerchantMarketPostRecord> records = mapper.selectList(tenantId(), memberId(), trim(safeQuery.getKeyword()),
                code(safeQuery.getBoardType()), code(safeQuery.getPostStatus()), trim(safeQuery.getCity()));
        if (records == null || records.isEmpty())
        {
            return Collections.emptyList();
        }
        return records.stream().map(record -> toView(record, false)).collect(Collectors.toList());
    }

    /** 查询当前商户的一条市场帖子。 */
    public MerchantMarketPostVO get(String postNo)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantMarketPostRecord record = mapper.selectByPostNo(tenantId(), memberId(), normalizePostNo(postNo));
        return record == null ? null : toView(record, true);
    }

    /** 新增草稿帖子。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantMarketPostVO create(MerchantMarketPostCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        List<MerchantMarketInventoryUnitView> selectedUnits = normalize(command);
        Long operatorId = SecurityUtils.getUserId();
        String postNo = createPostNo();
        if (mapper.insert(tenantId(), memberId(), postNo, command.getBoardType().getCode(), text(command.getCategory()),
                text(command.getBrand()), text(command.getModelName()), text(command.getMemorySpec()),
                text(command.getConditionLevel()), command.getQuantity(), text(command.getPriceRangeCode()),
                text(command.getCity()), text(command.getServiceTags()), text(command.getRemark()), operatorId) != 1)
        {
            throw new ServiceException("市场帖子保存失败");
        }
        MerchantMarketPostRecord created = mapper.selectByPostNo(tenantId(), memberId(), postNo);
        if (created == null)
        {
            throw new ServiceException("市场帖子保存后读取失败");
        }
        replaceInventoryBindings(created.getPostId(), selectedUnits);
        MerchantMarketPostVO after = toView(created, true);
        auditRecorder.record("周转市场", "CREATE", "MARKET_POST", postNo, displayName(after),
                "创建周转市场草稿", null, after);
        return after;
    }

    /** 修改草稿或已下架帖子。已发布和已关闭帖子不允许原地覆盖。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantMarketPostVO update(String postNo, MerchantMarketPostCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        List<MerchantMarketInventoryUnitView> selectedUnits = normalize(command);
        String normalizedPostNo = normalizePostNo(postNo);
        MerchantMarketPostRecord before = requireForUpdate(normalizedPostNo);
        MerchantMarketPostStatus status = status(before.getPostStatus());
        if (!EnumSet.of(MerchantMarketPostStatus.DRAFT, MerchantMarketPostStatus.OFF_SHELF).contains(status))
        {
            throw new ServiceException("只有草稿或已下架帖子可以修改");
        }
        if (mapper.updateDraft(tenantId(), memberId(), normalizedPostNo, command.getBoardType().getCode(),
                text(command.getCategory()), text(command.getBrand()), text(command.getModelName()),
                text(command.getMemorySpec()), text(command.getConditionLevel()), command.getQuantity(),
                text(command.getPriceRangeCode()), text(command.getCity()), text(command.getServiceTags()),
                text(command.getRemark()), SecurityUtils.getUserId()) != 1)
        {
            throw new ServiceException("市场帖子修改失败");
        }
        replaceInventoryBindings(before.getPostId(), selectedUnits);
        MerchantMarketPostVO after = get(normalizedPostNo);
        auditRecorder.record("周转市场", "UPDATE", "MARKET_POST", normalizedPostNo, displayName(after),
                "修改周转市场帖子", before, after);
        return after;
    }

    /** 发布草稿或重新发布已下架帖子。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantMarketPostVO publish(String postNo)
    {
        return changeStatus(postNo, MerchantMarketPostStatus.PUBLISHED,
                EnumSet.of(MerchantMarketPostStatus.DRAFT, MerchantMarketPostStatus.OFF_SHELF), "发布周转市场帖子");
    }

    /** 将已发布帖子下架。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantMarketPostVO offShelf(String postNo)
    {
        return changeStatus(postNo, MerchantMarketPostStatus.OFF_SHELF,
                EnumSet.of(MerchantMarketPostStatus.PUBLISHED), "下架周转市场帖子");
    }

    /** 关闭帖子，关闭后不再允许重新发布。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantMarketPostVO close(String postNo)
    {
        return changeStatus(postNo, MerchantMarketPostStatus.CLOSED,
                EnumSet.of(MerchantMarketPostStatus.DRAFT, MerchantMarketPostStatus.PUBLISHED,
                        MerchantMarketPostStatus.OFF_SHELF), "关闭周转市场帖子");
    }

    private MerchantMarketPostVO changeStatus(String postNo, MerchantMarketPostStatus target,
            EnumSet<MerchantMarketPostStatus> allowed, String description)
    {
        MerchantContextUtils.requireMerchantApproved();
        String normalizedPostNo = normalizePostNo(postNo);
        MerchantMarketPostRecord before = requireForUpdate(normalizedPostNo);
        MerchantMarketPostStatus current = status(before.getPostStatus());
        if (!allowed.contains(current))
        {
            throw new ServiceException("当前帖子状态不允许执行该操作");
        }
        if (target == MerchantMarketPostStatus.PUBLISHED)
        {
            validatePublishedInventory(before);
            depositService.freeze(tenantId(), memberId(), before.getPostId(), before.getPostNo());
        }
        else if (current == MerchantMarketPostStatus.PUBLISHED
                && EnumSet.of(MerchantMarketPostStatus.OFF_SHELF, MerchantMarketPostStatus.CLOSED).contains(target))
        {
            depositService.release(tenantId(), memberId(), before.getPostId(), before.getPostNo());
        }
        if (mapper.updateStatus(tenantId(), memberId(), normalizedPostNo, target.getCode(), SecurityUtils.getUserId()) != 1)
        {
            throw new ServiceException("市场帖子状态更新失败");
        }
        MerchantMarketPostVO after = get(normalizedPostNo);
        auditRecorder.record("周转市场", "STATUS_CHANGE", "MARKET_POST", normalizedPostNo, displayName(after),
                description, before, after);
        return after;
    }

    private MerchantMarketPostRecord requireForUpdate(String postNo)
    {
        MerchantMarketPostRecord record = mapper.selectByPostNoForUpdate(tenantId(), memberId(), postNo);
        if (record == null)
        {
            throw new ServiceException("市场帖子不存在或无权操作");
        }
        return record;
    }

    private List<MerchantMarketInventoryUnitView> normalize(MerchantMarketPostCommand command)
    {
        if (command == null || command.getBoardType() == null || blank(command.getCity()) || command.getQuantity() == null
                || command.getQuantity() < 1 || command.getQuantity() > 100000)
        {
            throw new ServiceException("板块、城市和数量不能为空");
        }
        MerchantMarketModelOption model = optionsService.requireModel(command.getCategory(), command.getBrand(),
                command.getModelName(), command.getMemorySpec());
        command.setCategory(model.getCategory());
        command.setBrand(model.getBrand());
        command.setModelName(model.getModelName());
        command.setMemorySpec(model.getMemorySpec());
        command.setConditionLevel(optionsService.normalizeCondition(command.getConditionLevel()));
        command.setPriceRangeCode(optionsService.normalizePrice(command.getBoardType(), command.getPriceRangeCode()));
        command.setServiceTags(null);
        command.setRemark(null);
        validateLength(command.getCity(), "城市");
        return validateInventorySelection(command);
    }

    private List<MerchantMarketInventoryUnitView> validateInventorySelection(MerchantMarketPostCommand command)
    {
        List<Long> unitIds = command.getInventoryUnitIds();
        if (unitIds == null || unitIds.isEmpty())
        {
            if (command.getBoardType() == MerchantMarketBoardType.BUY)
            {
                return Collections.emptyList();
            }
            return Collections.emptyList();
        }
        if (command.getBoardType() == MerchantMarketBoardType.BUY)
        {
            throw new ServiceException("求购帖子不能绑定自有库存设备");
        }
        if (command.getQuantity() == null || unitIds.size() != command.getQuantity())
        {
            throw new ServiceException("绑定设备数量必须与帖子数量一致");
        }
        List<MerchantMarketInventoryUnitView> selected = inventoryService.listEligible(command.getBrand(),
                command.getModelName(), command.getMemorySpec(), unitIds);
        if (selected.size() != unitIds.size())
        {
            throw new ServiceException("所选设备必须是当前商户已认证且可用的实物库存");
        }
        return selected;
    }

    private void validatePublishedInventory(MerchantMarketPostRecord record)
    {
        MerchantMarketBoardType boardType = MerchantMarketBoardType.fromCode(record.getBoardType());
        if (boardType == MerchantMarketBoardType.BUY)
        {
            return;
        }
        List<Long> unitIds = mapper.selectInventoryUnitIds(record.getPostId());
        if (unitIds == null || unitIds.size() != record.getQuantity())
        {
            throw new ServiceException("出售或出租帖子发布前必须绑定与数量一致的已认证实物库存");
        }
        List<MerchantMarketInventoryUnitView> eligible = inventoryService.listEligible(record.getBrand(),
                record.getModelName(), record.getMemorySpec(), unitIds);
        if (eligible.size() != unitIds.size())
        {
            throw new ServiceException("绑定的实物库存已不可用，请重新选择后再发布");
        }
    }

    private void replaceInventoryBindings(Long postId, List<MerchantMarketInventoryUnitView> selectedUnits)
    {
        mapper.removeInventoryBindings(tenantId(), memberId(), postId, SecurityUtils.getUserId());
        if (selectedUnits == null) return;
        for (MerchantMarketInventoryUnitView unit : selectedUnits)
        {
            if (mapper.insertInventoryBinding(tenantId(), memberId(), postId, unit.getUnitId(), unit.getUnitCode(),
                    unit.getSerialNumber(), SecurityUtils.getUserId()) != 1)
            {
                throw new ServiceException("市场帖子实物库存绑定失败");
            }
        }
    }

    private MerchantMarketPostVO toView(MerchantMarketPostRecord record, boolean includeInventory)
    {
        MerchantMarketPostVO view = new MerchantMarketPostVO();
        view.setPostNo(record.getPostNo());
        view.setBoardType(record.getBoardType());
        MerchantMarketBoardType boardType = MerchantMarketBoardType.fromCode(record.getBoardType());
        view.setBoardTypeName(boardType == null ? "未知板块" : boardType.getLabel());
        view.setCategory(record.getCategory());
        view.setBrand(record.getBrand());
        view.setModelName(record.getModelName());
        view.setMemorySpec(record.getMemorySpec());
        view.setConditionLevel(optionsService.conditionLabel(record.getConditionLevel()));
        view.setQuantity(record.getQuantity());
        view.setPriceRangeCode(optionsService.priceLabel(boardType, record.getPriceRangeCode()));
        view.setCity(record.getCity());
        if (includeInventory)
        {
            view.setInventoryUnitIds(mapper.selectInventoryUnitIds(record.getPostId()));
        }
        view.setServiceTags(record.getServiceTags());
        view.setRecent30dTurnover(record.getRecent30dTurnover());
        view.setIdleQty(record.getIdleQty());
        view.setRentedQty(record.getRentedQty());
        view.setLongIdleQty(record.getLongIdleQty());
        view.setPostStatus(record.getPostStatus());
        MerchantMarketPostStatus status = MerchantMarketPostStatus.fromCode(record.getPostStatus());
        view.setPostStatusName(status == null ? "未知状态" : status.getLabel());
        view.setDownRankFlag(record.getDownRankFlag());
        view.setCreateTime(record.getCreateTime());
        view.setUpdateTime(record.getUpdateTime());
        view.setRemark(record.getRemark());
        return view;
    }

    private String createPostNo()
    {
        return "MKT" + LocalDateTime.now().format(POST_TIME)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    private MerchantMarketPostStatus status(Integer code)
    {
        MerchantMarketPostStatus status = MerchantMarketPostStatus.fromCode(code);
        if (status == null)
        {
            throw new ServiceException("市场帖子状态不合法");
        }
        return status;
    }

    private Integer code(MerchantMarketBoardType type) { return type == null ? null : type.getCode(); }
    private Integer code(MerchantMarketPostStatus status) { return status == null ? null : status.getCode(); }
    private Long tenantId() { return MerchantContextUtils.getTenantId(); }
    private Long memberId() { return MerchantContextUtils.getMemberId(); }
    private boolean blank(String value) { return value == null || value.trim().isEmpty(); }
    private String trim(String value) { return value == null ? null : value.trim(); }
    private String text(String value) { return trim(value); }
    private String normalizePostNo(String postNo)
    {
        String normalized = trim(postNo);
        if (blank(normalized) || normalized.length() > 64)
        {
            throw new ServiceException("帖子编号不合法");
        }
        return normalized;
    }

    private void validateLength(String value, String field) { validateLength(value, field, MAX_TEXT_LENGTH); }
    private void validateLength(String value, String field, int maxLength)
    {
        if (value != null && value.trim().length() > maxLength)
        {
            throw new ServiceException(field + "不能超过" + maxLength + "个字符");
        }
    }

    private String displayName(MerchantMarketPostVO post)
    {
        if (post == null)
        {
            return "市场帖子";
        }
        return (post.getBrand() == null ? "" : post.getBrand() + " ")
                + (post.getModelName() == null ? post.getPostNo() : post.getModelName());
    }
}
