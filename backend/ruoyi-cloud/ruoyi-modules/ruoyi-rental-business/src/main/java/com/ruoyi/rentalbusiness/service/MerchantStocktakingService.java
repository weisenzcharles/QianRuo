package com.ruoyi.rentalbusiness.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.rentalbusiness.enums.MerchantStocktakingMode;
import com.ruoyi.rentalbusiness.enums.MerchantStocktakingScope;
import com.ruoyi.rentalbusiness.enums.MerchantStocktakingScanResult;
import com.ruoyi.rentalbusiness.enums.MerchantStocktakingStatus;
import com.ruoyi.rentalbusiness.mapper.MerchantStocktakingMapper;
import com.ruoyi.rentalbusiness.model.MerchantStocktakingCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantStocktakingScanCommand;

/** 商户库存盘点工作流。盘点结果只记录差异，不直接调整库存事实。 */
@Service
public class MerchantStocktakingService
{
    private final MerchantStocktakingMapper mapper;
    private final MerchantStocktakingWriter writer;
    private final CodeGeneratorClient codeGeneratorClient;

    public MerchantStocktakingService(MerchantStocktakingMapper mapper, MerchantStocktakingWriter writer,
            CodeGeneratorClient codeGeneratorClient)
    {
        this.mapper = mapper;
        this.writer = writer;
        this.codeGeneratorClient = codeGeneratorClient;
    }

    /** 查询当前商户的盘点单。 */
    public List<Map<String, Object>> list(Long warehouseId, Integer statusCode, String modeCode, String keyword,
            LocalDate beginDate, LocalDate endDate)
    {
        MerchantContextUtils.requireMerchantApproved();
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        if (warehouseId != null)
        {
            MerchantContextUtils.checkWarehouseAccess(warehouseId);
        }
        MerchantStocktakingStatus status = MerchantStocktakingStatus.fromCode(statusCode);
        if (statusCode != null && status == null)
        {
            throw new ServiceException("盘点状态不合法");
        }
        MerchantStocktakingMode mode = MerchantStocktakingMode.fromCode(modeCode);
        if (modeCode != null && mode == null)
        {
            throw new ServiceException("盘点方式不合法");
        }
        if (beginDate != null && endDate != null && beginDate.isAfter(endDate))
        {
            throw new ServiceException("盘点开始日期不能晚于结束日期");
        }
        Set<Long> warehouseIds = MerchantContextUtils.getWarehouseIds();
        boolean allWarehouseAccess = MerchantContextUtils.hasAllWarehouseAccess();
        return mapper.selectStocktakingList(tenantId, memberId, warehouseId,
                status == null ? null : status.getCode(), mode == null ? null : mode.getDatabaseValue(), trim(keyword),
                beginDate, endDate, warehouseIds, allWarehouseAccess);
    }

    /** 查询盘点概况。 */
    public Map<String, Object> summary(Long warehouseId)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (warehouseId != null)
        {
            MerchantContextUtils.checkWarehouseAccess(warehouseId);
        }
        Map<String, Object> result = mapper.selectSummary(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), warehouseId, MerchantContextUtils.getWarehouseIds(),
                MerchantContextUtils.hasAllWarehouseAccess());
        return result == null ? new LinkedHashMap<>() : result;
    }

    /** 查询所选仓库中存在可用库存的分类或型号选项。 */
    public List<Map<String, Object>> scopeOptions(Long warehouseId, String scopeCode)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (warehouseId == null)
        {
            throw new ServiceException("请选择盘点仓库");
        }
        MerchantContextUtils.checkWarehouseAccess(warehouseId);
        MerchantStocktakingScope scope = MerchantStocktakingScope.fromCode(scopeCode);
        if (scope == null || scope == MerchantStocktakingScope.ALL)
        {
            throw new ServiceException("盘点范围不合法");
        }
        return mapper.selectScopeOptions(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), warehouseId, scope.getDatabaseValue());
    }

    /** 查询盘点单和明细。 */
    public Map<String, Object> detail(Long stocktakingId)
    {
        MerchantContextUtils.requireMerchantApproved();
        Map<String, Object> stocktaking = requireStocktaking(stocktakingId, false);
        Long warehouseId = number(stocktaking.get("warehouseId"));
        MerchantContextUtils.checkWarehouseAccess(warehouseId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stocktaking", stocktaking);
        result.put("items", mapper.selectItems(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), stocktakingId));
        return result;
    }

    /** 创建盘点单并冻结账面数量快照。 */
    public Map<String, Object> create(MerchantStocktakingCreateCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.warehouseId == null)
        {
            throw new ServiceException("请选择盘点仓库");
        }
        if (command.mode == null)
        {
            throw new ServiceException("盘点方式不合法");
        }
        MerchantContextUtils.checkWarehouseAccess(command.warehouseId);
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        MerchantStocktakingScope scope = command.scope == null ? MerchantStocktakingScope.ALL : command.scope;
        Long classId = scope == MerchantStocktakingScope.CLASS ? command.classId : null;
        Long modelId = scope == MerchantStocktakingScope.MODEL ? command.modelId : null;
        if (scope == MerchantStocktakingScope.CLASS && classId == null)
        {
            throw new ServiceException("请选择盘点分类");
        }
        if (scope == MerchantStocktakingScope.MODEL && modelId == null)
        {
            throw new ServiceException("请选择盘点型号");
        }
        String scopeLabel = scope == MerchantStocktakingScope.ALL ? "全部商品"
                : mapper.selectScopeLabel(tenantId, memberId, scope.getDatabaseValue(), classId, modelId);
        if (scopeLabel == null || scopeLabel.trim().isEmpty())
        {
            throw new ServiceException("所选盘点范围没有可用商品");
        }
        Integer expectedQuantity = mapper.selectExpectedQuantity(tenantId, memberId, command.warehouseId,
                command.mode.getDatabaseValue(), scope.getDatabaseValue(), classId, modelId);
        if (expectedQuantity == null || expectedQuantity <= 0)
        {
            throw new ServiceException("所选仓库和盘点范围没有可用库存");
        }
        String stocktakingNo = codeGeneratorClient.next("stocktaking");
        return writer.create(tenantId, memberId, command.warehouseId, stocktakingNo,
                command.stocktakingDate == null ? LocalDate.now() : command.stocktakingDate,
                command.mode.getDatabaseValue(), scope.getDatabaseValue(), classId, modelId, scopeLabel,
                expectedQuantity == null ? 0 : expectedQuantity,
                SecurityUtils.getUserId(), trim(command.remark));
    }

    /** 保存一条手工盘点实盘数量。 */
    @Transactional(rollbackFor = Exception.class)
    public void count(Long stocktakingId, Long itemId, Integer countedQuantity)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (itemId == null || countedQuantity == null || countedQuantity < 0)
        {
            throw new ServiceException("实盘数量必须是大于或等于零的整数");
        }
        Map<String, Object> stocktaking = requireStocktaking(stocktakingId, true);
        requireProcessing(stocktaking);
        if (!Integer.valueOf(MerchantStocktakingMode.MANUAL.getDatabaseValue()).equals(intNumber(stocktaking.get("mode"))))
        {
            throw new ServiceException("该盘点单不是手工盘点");
        }
        Map<String, Object> item = mapper.selectItemForUpdate(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), stocktakingId, itemId);
        if (item == null)
        {
            throw new ServiceException("盘点明细不存在或不属于当前盘点单");
        }
        if (mapper.updateCountedQuantity(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                stocktakingId, itemId, countedQuantity) != 1)
        {
            throw new ServiceException("保存实盘数量失败，请刷新后重试");
        }
    }

    /** 按当前盘点仓库校验并记录设备扫码结果，重复码返回重复状态而不重复计数。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> scan(Long stocktakingId, MerchantStocktakingScanCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || trim(command.code) == null)
        {
            throw new ServiceException("请扫描设备序列号或库存编码");
        }
        String code = trim(command.code);
        if (code.length() > 128)
        {
            throw new ServiceException("扫码内容不能超过128个字符");
        }
        Map<String, Object> stocktaking = requireStocktaking(stocktakingId, true);
        requireProcessing(stocktaking);
        if (!Integer.valueOf(MerchantStocktakingMode.SCAN.getDatabaseValue()).equals(intNumber(stocktaking.get("mode"))))
        {
            throw new ServiceException("该盘点单不是扫码盘点");
        }
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Long warehouseId = number(stocktaking.get("warehouseId"));
        MerchantContextUtils.checkWarehouseAccess(warehouseId);
        if (mapper.countDuplicateScan(tenantId, memberId, stocktakingId, code) > 0)
        {
            Map<String, Object> duplicate = new LinkedHashMap<>();
            duplicate.put("duplicate", true);
            duplicate.put("scanCode", code);
            duplicate.put("message", "该设备已在本盘点单中扫描");
            return duplicate;
        }

        Map<String, Object> unit = mapper.selectUnitByCode(tenantId, memberId, code);
        Integer resultCode = MerchantStocktakingScanResult.NORMAL.getCode();
        String remark = null;
        Long unitId = null;
        Integer unitStatus = null;
        String productName = null;
        String modelName = null;
        Long unitWarehouseId = null;
        Long operatorId = SecurityUtils.getUserId();
        if (unit == null)
        {
            resultCode = MerchantStocktakingScanResult.ABNORMAL.getCode();
            remark = "未找到对应的库存设备";
        }
        else
        {
            unitId = number(unit.get("unitId"));
            unitStatus = intNumber(unit.get("unitStatus"));
            unitWarehouseId = number(unit.get("warehouseId"));
            productName = string(unit.get("productName"));
            modelName = string(unit.get("modelName"));
            if (!warehouseId.equals(unitWarehouseId))
            {
                resultCode = MerchantStocktakingScanResult.ABNORMAL.getCode();
                remark = "设备当前所属仓库为" + stringOr(unit.get("warehouseName"), "其他仓库");
            }
            else if (isNotAvailableStatus(unitStatus))
            {
                resultCode = MerchantStocktakingScanResult.ABNORMAL.getCode();
                remark = "设备当前状态为" + inventoryStatusText(unitStatus);
            }
        }
        if (mapper.insertScanItem(tenantId, memberId, stocktakingId, unitId, code, unitStatus,
                resultCode, unitId == null ? null : "inventory_unit", unitId,
                productName, modelName, remark) != 1)
        {
            throw new ServiceException("保存扫码结果失败");
        }
        if (resultCode == MerchantStocktakingScanResult.ABNORMAL.getCode())
        {
            mapper.incrementAbnormalCount(tenantId, memberId, stocktakingId, operatorId);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("duplicate", false);
        result.put("scanCode", code);
        result.put("productName", productName);
        result.put("modelName", modelName);
        result.put("warehouseName", unit == null ? null : unit.get("warehouseName"));
        result.put("systemStatus", inventoryStatusText(unitStatus));
        result.put("scanResult", resultCode);
        result.put("scanResultText", MerchantStocktakingScanResult.ABNORMAL.getCode() == resultCode ? "异常" : "正常");
        result.put("message", remark == null ? "设备已计入本次盘点" : remark);
        return result;
    }

    /** 完成当前盘点；手工盘点必须先录入每项实盘数量。 */
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long stocktakingId)
    {
        MerchantContextUtils.requireMerchantApproved();
        Map<String, Object> stocktaking = requireStocktaking(stocktakingId, true);
        requireProcessing(stocktaking);
        boolean manual = Integer.valueOf(MerchantStocktakingMode.MANUAL.getDatabaseValue())
                .equals(intNumber(stocktaking.get("mode")));
        if (manual && mapper.countUncountedItems(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), stocktakingId) > 0)
        {
            throw new ServiceException("请先填写全部商品的实盘数量");
        }
        MerchantStocktakingStatus target = manual && mapper.countDifferentItems(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), stocktakingId) > 0
                        ? MerchantStocktakingStatus.REVIEW_REQUIRED
                        : MerchantStocktakingStatus.COMPLETED;
        updateStatus(stocktakingId, MerchantStocktakingStatus.PROCESSING, target);
    }

    /** 关闭未完成的盘点单，不更改库存数量。 */
    @Transactional(rollbackFor = Exception.class)
    public void close(Long stocktakingId)
    {
        MerchantContextUtils.requireMerchantApproved();
        Map<String, Object> stocktaking = requireStocktaking(stocktakingId, true);
        MerchantStocktakingStatus current = MerchantStocktakingStatus.fromCode(intNumber(stocktaking.get("status")));
        if (current != MerchantStocktakingStatus.PROCESSING && current != MerchantStocktakingStatus.REVIEW_REQUIRED)
        {
            throw new ServiceException("只有未完成的盘点单可以关闭");
        }
        updateStatus(stocktakingId, current, MerchantStocktakingStatus.CLOSED);
    }

    /** 确认人工复核后的差异并归档盘点结果。 */
    @Transactional(rollbackFor = Exception.class)
    public void review(Long stocktakingId)
    {
        MerchantContextUtils.requireMerchantApproved();
        Map<String, Object> stocktaking = requireStocktaking(stocktakingId, true);
        if (MerchantStocktakingStatus.REVIEW_REQUIRED.getCode() != intNumber(stocktaking.get("status")))
        {
            throw new ServiceException("该盘点单当前不需要复核");
        }
        if (mapper.countUncountedItems(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), stocktakingId) > 0)
        {
            throw new ServiceException("请先填写全部商品的实盘数量");
        }
        updateStatus(stocktakingId, MerchantStocktakingStatus.REVIEW_REQUIRED,
                MerchantStocktakingStatus.COMPLETED);
    }

    private void updateStatus(Long stocktakingId, MerchantStocktakingStatus from, MerchantStocktakingStatus target)
    {
        if (mapper.updateStatus(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), stocktakingId,
                from.getCode(), target.getCode(), SecurityUtils.getUserId()) != 1)
        {
            throw new ServiceException("盘点状态已变化，请刷新后重试");
        }
    }

    private Map<String, Object> requireStocktaking(Long stocktakingId, boolean lock)
    {
        if (stocktakingId == null)
        {
            throw new ServiceException("盘点单不能为空");
        }
        Map<String, Object> stocktaking = lock
                ? mapper.selectStocktakingForUpdate(MerchantContextUtils.getTenantId(),
                        MerchantContextUtils.getMemberId(), stocktakingId)
                : mapper.selectStocktaking(MerchantContextUtils.getTenantId(),
                        MerchantContextUtils.getMemberId(), stocktakingId);
        if (stocktaking == null)
        {
            throw new ServiceException("盘点单不存在或无权访问");
        }
        MerchantContextUtils.checkWarehouseAccess(number(stocktaking.get("warehouseId")));
        return stocktaking;
    }

    private static void requireProcessing(Map<String, Object> stocktaking)
    {
        if (!Integer.valueOf(MerchantStocktakingStatus.PROCESSING.getCode()).equals(intNumber(stocktaking.get("status"))))
        {
            throw new ServiceException("盘点单已结束，不能继续修改");
        }
    }

    private static boolean isNotAvailableStatus(Integer status)
    {
        return !RentalCode.InventoryStatus.AVAILABLE.getCode().equals(status);
    }

    private static String inventoryStatusText(Integer status)
    {
        if (status == null)
        {
            return "未识别";
        }
        for (RentalCode.InventoryStatus value : RentalCode.InventoryStatus.values())
        {
            if (value.getCode().equals(status))
            {
                return value.getLabel();
            }
        }
        return "未知状态";
    }

    private static Long number(Object value)
    {
        return value instanceof Number ? ((Number) value).longValue() : null;
    }

    private static Integer intNumber(Object value)
    {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    private static String string(Object value)
    {
        return value == null ? null : value.toString();
    }

    private static String stringOr(Object value, String fallback)
    {
        String text = string(value);
        return text == null || text.trim().isEmpty() ? fallback : text;
    }

    private static String trim(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
