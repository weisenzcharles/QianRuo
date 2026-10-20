package com.ruoyi.rentalbusiness.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.rentalbusiness.domain.MerchantOrderRow;
import com.ruoyi.rentalbusiness.enums.MerchantOrderMediaMatchStatus;
import com.ruoyi.rentalbusiness.enums.MerchantOrderMediaType;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderMapper;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderMediaMapper;
import com.ruoyi.rentalbusiness.model.MerchantOrderMediaBatchCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderMediaCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderMediaVO;

/** 商户订单履约媒体应用服务，负责文件上传后的业务登记和状态校验。 */
@Service
public class MerchantOrderMediaService
{
    private static final long MAX_FILE_SIZE = 200L * 1024L * 1024L;
    private static final int MAX_BATCH_SIZE = 50;
    private static final List<String> VIDEO_EXTENSIONS = Collections.unmodifiableList(Arrays.asList("mp4", "avi", "rmvb"));
    private static final List<String> IMAGE_EXTENSIONS = Collections.unmodifiableList(Arrays.asList("jpg", "jpeg", "png", "webp"));
    private static final Pattern FILE_NAME_SN_PATTERN = Pattern.compile(
            "(?i)(?:sn|s[/_-]?n|序列号)[-_：: ]*([A-Za-z0-9][A-Za-z0-9_-]{3,127})");
    private static final List<Integer> UNMATCHED_STATUSES = Collections.unmodifiableList(Arrays.asList(
            MerchantOrderMediaMatchStatus.PENDING.getCode(), MerchantOrderMediaMatchStatus.UNMATCHED.getCode()));
    private static final List<Integer> OPEN_ORDER_STATUSES = Collections.unmodifiableList(Arrays.asList(
            RentalCode.OrderStatus.PENDING_SHIP.getCode(), RentalCode.OrderStatus.PREPARED.getCode(),
            RentalCode.OrderStatus.SHIPPING.getCode(), RentalCode.OrderStatus.SHIP_IN_TRANSIT.getCode(),
            RentalCode.OrderStatus.SHIPPED.getCode(), RentalCode.OrderStatus.RENTING.getCode(),
            RentalCode.OrderStatus.RETURNING.getCode(), RentalCode.OrderStatus.OVERDUE_RETURN.getCode(),
            RentalCode.OrderStatus.OVERDUE.getCode(), RentalCode.OrderStatus.AFTER_SALES.getCode()));

    private final MerchantOrderMapper orderMapper;
    private final MerchantOrderMediaMapper mediaMapper;
    private final MerchantBusinessAuditRecorder auditRecorder;

    @Autowired
    public MerchantOrderMediaService(MerchantOrderMapper orderMapper, MerchantOrderMediaMapper mediaMapper,
            MerchantBusinessAuditRecorder auditRecorder)
    {
        this.orderMapper = orderMapper;
        this.mediaMapper = mediaMapper;
        this.auditRecorder = auditRecorder;
    }

    /** 查询当前商户订单的履约媒体，供订单详情展示。 */
    @Transactional(readOnly = true)
    public List<MerchantOrderMediaVO> list(Long orderId)
    {
        requireVisibleOrder(orderId);
        List<MerchantOrderMediaVO> rows = mediaMapper.selectByOrder(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), orderId);
        if (rows == null || rows.isEmpty())
        {
            return Collections.emptyList();
        }
        for (MerchantOrderMediaVO row : rows)
        {
            decorate(row);
        }
        return new ArrayList<MerchantOrderMediaVO>(rows);
    }

    /** 登记统一文件服务已上传的订单履约媒体。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantOrderMediaVO add(Long orderId, MerchantOrderMediaCreateCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantOrderRow order = requireVisibleOrder(orderId);
        MerchantOrderMediaType type = validateCommand(command);
        validateOrderStatus(order.orderStatus, type);

        Long operatorId = SecurityUtils.getUserId();
        int inserted = mediaMapper.insert(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                orderId, command, operatorId);
        if (inserted != 1)
        {
            throw new ServiceException("订单履约凭证保存失败");
        }
        MerchantOrderMediaVO result = mediaMapper.selectLatest(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), orderId, String.valueOf(type.getDbCode()));
        if (result == null)
        {
            throw new ServiceException("订单履约凭证读取失败");
        }
        decorate(result);
        if (auditRecorder != null)
        {
            auditRecorder.record("订单履约", "CREATE", "ORDER_MEDIA", order.orderNo, order.orderNo,
                    "登记" + type.getLabel(), null, result);
        }
        return result;
    }

    /** 查询当前商户批量上传后仍待识别或待人工处理的发货视频。 */
    @Transactional(readOnly = true)
    public List<MerchantOrderMediaVO> listUnmatched()
    {
        MerchantContextUtils.requireMerchantApproved();
        List<MerchantOrderMediaVO> rows = mediaMapper.selectUnmatched(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), MerchantOrderMediaType.SHIP_VIDEO.getDbCode(), UNMATCHED_STATUSES);
        if (rows == null || rows.isEmpty())
        {
            return Collections.emptyList();
        }
        for (MerchantOrderMediaVO row : rows)
        {
            decorate(row);
        }
        return new ArrayList<MerchantOrderMediaVO>(rows);
    }

    /** 批量登记已上传视频，并优先按传入或文件名中的 SN 匹配未完成订单。 */
    @Transactional(rollbackFor = Exception.class)
    public List<MerchantOrderMediaVO> addBatch(MerchantOrderMediaBatchCreateCommand batch)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (batch == null || batch.items == null || batch.items.isEmpty())
        {
            throw new ServiceException("请至少选择一个履约视频");
        }
        if (batch.items.size() > MAX_BATCH_SIZE)
        {
            throw new ServiceException("单次最多上传50个履约视频");
        }
        MerchantOrderMediaType type = MerchantOrderMediaType.fromCode(batch.mediaType);
        if (!MerchantOrderMediaType.SHIP_VIDEO.equals(type))
        {
            throw new ServiceException("批量匹配目前只支持发货视频");
        }

        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Long operatorId = SecurityUtils.getUserId();
        List<MerchantOrderMediaVO> result = new ArrayList<MerchantOrderMediaVO>();
        for (MerchantOrderMediaCreateCommand item : batch.items)
        {
            if (item == null)
            {
                throw new ServiceException("履约视频参数不能为空");
            }
            item.mediaType = type.getCode();
            validateCommand(item);
            item.detectedSn = optional(item.detectedSn, 128);
            if (item.detectedSn == null)
            {
                item.detectedSn = inferSnFromFileName(item.fileName);
            }
            int inserted = mediaMapper.insert(tenantId, memberId, null, item, operatorId);
            if (inserted != 1 || item.mediaId == null)
            {
                throw new ServiceException("批量履约视频保存失败");
            }
            if (item.detectedSn != null)
            {
                matchMedia(tenantId, memberId, item.mediaId, item.detectedSn, operatorId);
            }
            MerchantOrderMediaVO row = mediaMapper.selectById(tenantId, memberId, item.mediaId);
            if (row == null)
            {
                throw new ServiceException("批量履约视频读取失败");
            }
            decorate(row);
            result.add(row);
        }
        if (auditRecorder != null)
        {
            auditRecorder.record("订单履约", "CREATE", "ORDER_MEDIA_BATCH", "BATCH", "BATCH",
                    "批量登记发货视频，共" + result.size() + "个", null, result);
        }
        return result;
    }

    /** 对待处理视频补录 SN 并重新匹配订单，失败时保留待人工处理状态。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantOrderMediaVO recognize(Long mediaId, String detectedSn)
    {
        MerchantContextUtils.requireMerchantApproved();
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        MerchantOrderMediaVO existing = mediaMapper.selectById(tenantId, memberId, mediaId);
        if (existing == null)
        {
            throw new ServiceException("履约视频不存在或已删除");
        }
        MerchantOrderMediaType type = MerchantOrderMediaType.fromCode(existing.mediaType);
        if (!MerchantOrderMediaType.SHIP_VIDEO.equals(type))
        {
            throw new ServiceException("当前媒体不是发货视频");
        }
        String normalizedSn = required(detectedSn, "请输入设备SN", 128);
        matchMedia(tenantId, memberId, mediaId, normalizedSn, SecurityUtils.getUserId());
        MerchantOrderMediaVO result = mediaMapper.selectById(tenantId, memberId, mediaId);
        decorate(result);
        return result;
    }

    private void matchMedia(Long tenantId, Long memberId, Long mediaId, String detectedSn, Long operatorId)
    {
        Map<String, Object> target = mediaMapper.selectOpenOrderBySnCode(tenantId, memberId, detectedSn,
                OPEN_ORDER_STATUSES);
        if (!isVisibleTarget(target))
        {
            mediaMapper.updateMatch(tenantId, memberId, mediaId, null, null, null, detectedSn,
                    MerchantOrderMediaMatchStatus.UNMATCHED.getCode(), operatorId);
            return;
        }
        mediaMapper.updateMatch(tenantId, memberId, mediaId, toLong(target.get("orderId")),
                toLong(target.get("unitId")), toLong(target.get("snId")), detectedSn,
                MerchantOrderMediaMatchStatus.MATCHED.getCode(), operatorId);
    }

    private boolean isVisibleTarget(Map<String, Object> target)
    {
        if (target == null || target.get("orderId") == null)
        {
            return false;
        }
        Long warehouseId = toLong(target.get("warehouseId"));
        return MerchantContextUtils.hasAllWarehouseAccess() || warehouseId == null
                || MerchantContextUtils.hasWarehouseAccess(warehouseId);
    }

    private void decorate(MerchantOrderMediaVO row)
    {
        if (row == null)
        {
            return;
        }
        MerchantOrderMediaType type = MerchantOrderMediaType.fromCode(row.mediaType);
        row.mediaTypeLabel = type == null ? "履约凭证" : type.getLabel();
        MerchantOrderMediaMatchStatus matchStatus = MerchantOrderMediaMatchStatus.fromCode(row.matchStatus);
        row.matchStatusLabel = matchStatus == null ? "待处理" : matchStatus.getLabel();
    }

    private String inferSnFromFileName(String fileName)
    {
        if (fileName == null)
        {
            return null;
        }
        Matcher matcher = FILE_NAME_SN_PATTERN.matcher(fileName);
        return matcher.find() ? matcher.group(1) : null;
    }

    private Long toLong(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        if (value == null)
        {
            return null;
        }
        try
        {
            return Long.valueOf(String.valueOf(value));
        }
        catch (NumberFormatException exception)
        {
            return null;
        }
    }

    private MerchantOrderRow requireVisibleOrder(Long orderId)
    {
        if (orderId == null)
        {
            throw new ServiceException("订单唯一编号不能为空");
        }
        MerchantOrderRow order = orderMapper.selectMerchantOrderById(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), orderId);
        if (order == null || order.orderId == null || (!MerchantContextUtils.hasAllWarehouseAccess()
                && order.warehouseId != null && !MerchantContextUtils.hasWarehouseAccess(order.warehouseId)))
        {
            throw new ServiceException("订单不存在或无权查看");
        }
        return order;
    }

    private MerchantOrderMediaType validateCommand(MerchantOrderMediaCreateCommand command)
    {
        if (command == null)
        {
            throw new ServiceException("履约凭证参数不能为空");
        }
        MerchantOrderMediaType type = MerchantOrderMediaType.fromCode(command.mediaType);
        if (type == null)
        {
            throw new ServiceException("履约凭证类型不合法");
        }
        command.mediaType = type.getCode();
        command.mediaTypeCode = type.getDbCode();
        command.fileUrl = required(command.fileUrl, "请先上传履约凭证", 255);
        command.fileName = optional(command.fileName, 255);
        command.mimeType = optional(command.mimeType, 128);
        command.remark = optional(command.remark, 500);
        validateFileFormat(type, command.fileName, command.fileUrl, command.mimeType);
        if (command.fileSize != null && (command.fileSize < 0 || command.fileSize > MAX_FILE_SIZE))
        {
            throw new ServiceException("履约凭证大小不能超过200MB");
        }
        if (command.fileSize == null)
        {
            command.fileSize = 0L;
        }
        return type;
    }

    private void validateFileFormat(MerchantOrderMediaType type, String fileName, String fileUrl, String mimeType)
    {
        boolean image = MerchantOrderMediaType.CHECK_PHOTO.equals(type);
        List<String> allowedExtensions = image ? IMAGE_EXTENSIONS : VIDEO_EXTENSIONS;
        String extension = fileExtension(fileName);
        if (extension == null)
        {
            extension = fileExtension(fileUrl);
        }
        if (extension == null || !allowedExtensions.contains(extension))
        {
            throw new ServiceException(image ? "验机照片仅支持jpg、jpeg、png或webp格式"
                    : "履约视频仅支持mp4、avi或rmvb格式");
        }
        if (mimeType == null || mimeType.isEmpty() || "application/octet-stream".equalsIgnoreCase(mimeType))
        {
            return;
        }
        String normalizedMime = mimeType.toLowerCase(Locale.ROOT);
        boolean validMime = image ? normalizedMime.startsWith("image/") : normalizedMime.startsWith("video/");
        if (!validMime)
        {
            throw new ServiceException(image ? "验机照片文件类型不正确" : "履约视频文件类型不正确");
        }
    }

    private String fileExtension(String value)
    {
        if (value == null || value.isBlank())
        {
            return null;
        }
        String path = value.trim().split("[?#]", 2)[0];
        int slash = path.lastIndexOf('/');
        int dot = path.lastIndexOf('.');
        if (dot <= slash || dot == path.length() - 1)
        {
            return null;
        }
        return path.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private void validateOrderStatus(Integer orderStatus, MerchantOrderMediaType type)
    {
        if (MerchantOrderMediaType.SHIP_VIDEO.equals(type)
                || MerchantOrderMediaType.UNBOX_VIDEO.equals(type))
        {
            if (!RentalCode.OrderStatus.PREPARED.getCode().equals(orderStatus))
            {
                throw new ServiceException("只有已备货订单可以上传发货凭证");
            }
            return;
        }
        if (!RentalCode.OrderStatus.RETURNING.getCode().equals(orderStatus)
                && !RentalCode.OrderStatus.OVERDUE_RETURN.getCode().equals(orderStatus)
                && !RentalCode.OrderStatus.AFTER_SALES.getCode().equals(orderStatus)
                && !RentalCode.OrderStatus.COMPLETED.getCode().equals(orderStatus))
        {
            throw new ServiceException("当前订单阶段不允许登记收货凭证");
        }
    }

    private String required(String value, String message, int maxLength)
    {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty())
        {
            throw new ServiceException(message);
        }
        if (normalized.length() > maxLength)
        {
            throw new ServiceException("履约凭证地址过长");
        }
        return normalized;
    }

    private String optional(String value, int maxLength)
    {
        if (value == null)
        {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty())
        {
            return null;
        }
        return normalized.length() > maxLength ? normalized.substring(0, maxLength) : normalized;
    }
}
