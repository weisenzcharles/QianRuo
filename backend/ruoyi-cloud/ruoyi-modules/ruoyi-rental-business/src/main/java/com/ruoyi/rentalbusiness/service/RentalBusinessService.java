package com.ruoyi.rentalbusiness.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.enums.NumericCode;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.RemoteMerchantProductClassService;
import com.ruoyi.system.api.RemoteBrandModelService;
import com.ruoyi.system.api.model.BrandModel;
import com.ruoyi.rentalbusiness.mapper.RentalBusinessMapper;
import com.ruoyi.rentalbusiness.model.RentalBusinessCommand;
import com.ruoyi.rentalbusiness.model.RentalBusinessComponentCommand;
import com.ruoyi.rentalbusiness.model.RentalBusinessComponentVO;
import com.ruoyi.rentalbusiness.model.RentalBusinessVO;
import com.ruoyi.rentalbusiness.model.RentalInventoryCheckItemVO;
import com.ruoyi.rentalbusiness.model.RentalInventoryCheckVO;

/**
 * 租赁主数据与库存应用服务。
 *
 * <p>该服务是六类领域共用的业务校验边界：归属字段全部来自登录上下文，写操作
 * 统一要求商户已审核，仓库操作统一校验仓库范围，套餐只通过版本化 BOM 发布，
 * 库存统计统一按商户商品和库存单元计算，避免套餐组件重复统计。</p>
 */
@Service
public class RentalBusinessService
{
    private final RentalBusinessMapper mapper;

    private final RemoteMerchantProductClassService merchantProductClassService;

    private final RemoteBrandModelService brandModelService;

    private final CodeGeneratorClient codeGeneratorClient;

    private final MerchantBusinessAuditRecorder auditRecorder;

    /** 注入租赁主数据 Mapper。 */
    @org.springframework.beans.factory.annotation.Autowired
    public RentalBusinessService(RentalBusinessMapper mapper,
            RemoteMerchantProductClassService merchantProductClassService, RemoteBrandModelService brandModelService,
            CodeGeneratorClient codeGeneratorClient, MerchantBusinessAuditRecorder auditRecorder)
    {
        this.mapper = mapper;
        this.merchantProductClassService = merchantProductClassService;
        this.brandModelService = brandModelService;
        this.codeGeneratorClient = codeGeneratorClient;
        this.auditRecorder = auditRecorder;
    }

    /** 保留旧构造方式，供现有单元测试使用；测试未提供审计记录器时不影响主业务校验。 */
    public RentalBusinessService(RentalBusinessMapper mapper,
            RemoteMerchantProductClassService merchantProductClassService, RemoteBrandModelService brandModelService,
            CodeGeneratorClient codeGeneratorClient)
    {
        this(mapper, merchantProductClassService, brandModelService, codeGeneratorClient, null);
    }

    /** 查询当前商户可见仓库。 */
    public List<RentalBusinessVO> listWarehouses(String keyword, Integer status)
    {
        Scope scope = scope();
        return mapList(mapper.selectWarehouses(scope.tenantId, scope.memberId, trim(keyword), status,
                scope.warehouseIds, scope.allWarehouseAccess), this::toView);
    }

    /**
     * 查询指定商户的仓库内部视图，供平台服务和商户身份服务通过内部契约调用。
     *
     * @param tenantId 可信租户唯一编号
     * @param memberId 可信商户唯一编号
     * @param status 仓库状态，为空时不限制状态
     * @return 仓库内部视图集合
     */
    public List<Map<String, Object>> listWarehousesForInternal(Long tenantId, Long memberId, Integer status)
    {
        requireInternalScope(tenantId, memberId);
        return mapper.selectWarehousesForInternal(tenantId, memberId, status);
    }

    /**
     * 统计指定商户启用仓库中命中唯一编号集合的数量，供平台端保存仓库权限时做完整归属校验。
     *
     * @param tenantId 可信租户唯一编号
     * @param memberId 可信商户唯一编号
     * @param warehouseIds 待校验仓库唯一编号集合
     * @return 命中的启用仓库数量
     */
    public int countEnabledWarehousesForInternal(Long tenantId, Long memberId, List<Long> warehouseIds)
    {
        requireInternalScope(tenantId, memberId);
        if (warehouseIds == null || warehouseIds.isEmpty())
        {
            return 0;
        }
        return mapper.countEnabledWarehouses(tenantId, memberId, warehouseIds);
    }

    /**
     * 判断标准型号是否已经被租赁业务数据引用，供平台端删除标准型号前执行跨服务校验。
     *
     * @param modelId 平台标准型号唯一编号
     * @return 已被商品、库存或订单引用返回 true
     */
    public boolean hasModelReferencesForInternal(Long modelId)
    {
        if (modelId == null || modelId <= 0)
        {
            throw new ServiceException("标准型号唯一编号不能为空");
        }
        return mapper.countModelReferences(modelId) > 0;
    }

    /** 查询当前商户仓库详情并校验用户仓库范围。 */
    public RentalBusinessVO getWarehouse(Long warehouseId)
    {
        Scope scope = scope();
        MerchantContextUtils.checkWarehouseAccess(warehouseId);
        return requireView(mapper.selectWarehouse(scope.tenantId, scope.memberId, warehouseId), "仓库不存在或已删除");
    }

    /** 新增仓库并由平台统一编码服务生成仓库编码。 */
    @Transactional(rollbackFor = Exception.class)
    public RentalBusinessVO addWarehouse(RentalBusinessCommand command)
    {
        writeGuard();
        Scope scope = scope();
        String name = required(command == null ? null : command.name, "仓库名称不能为空");
        String code = codeGeneratorClient.next("warehouse");
        mapper.insertWarehouse(scope.tenantId, scope.memberId, code, name, trim(command.contactName),
                trim(command.contactPhone), trim(command.province), trim(command.city), trim(command.district),
                trim(command.address), command.longitude, command.latitude, trim(command.pickupRuleJson),
                number(command.sortNo), status(command.status), booleanValue(command.returnEnabled, 1), booleanValue(command.allianceShareEnabled, 0),
                trim(command.remark), operator());
        RentalBusinessVO created = getWarehouse(mapper.selectLastInsertId());
        audit("仓库管理", "CREATE", "WAREHOUSE", created.id, created.name, "创建仓库", null, created);
        return created;
    }

    /** 修改仓库资料；系统编码一经生成即保持不变。 */
    @Transactional(rollbackFor = Exception.class)
    public RentalBusinessVO updateWarehouse(RentalBusinessCommand command)
    {
        writeGuard();
        Scope scope = scope();
        Long warehouseId = requiredId(command == null ? null : command.id, "仓库唯一编号不能为空");
        RentalBusinessVO existing = requireWarehouse(scope, warehouseId, true);
        String code = existing.code;
        String name = required(command.name, "仓库名称不能为空");
        mapper.updateWarehouse(scope.tenantId, scope.memberId, warehouseId, code, name, trim(command.contactName),
                trim(command.contactPhone), trim(command.province), trim(command.city), trim(command.district),
                trim(command.address), command.longitude, command.latitude, trim(command.pickupRuleJson),
                number(command.sortNo), status(command.status), booleanValue(command.returnEnabled, 1), booleanValue(command.allianceShareEnabled, 0),
                trim(command.remark), operator());
        RentalBusinessVO updated = getWarehouse(warehouseId);
        audit("仓库管理", "UPDATE", "WAREHOUSE", warehouseId, updated.name, "编辑仓库", existing, updated);
        return updated;
    }

    /** 软删除仓库，存在库存、SN、配件或订单引用时拒绝删除。 */
    @Transactional(rollbackFor = Exception.class)
    public int deleteWarehouse(Long warehouseId)
    {
        writeGuard();
        Scope scope = scope();
        RentalBusinessVO existing = requireWarehouse(scope, warehouseId, true);
        if (mapper.countWarehouseReferences(scope.tenantId, scope.memberId, warehouseId) > 0)
        {
            throw new ServiceException("仓库仍被库存、SN、配件或订单引用，不能删除");
        }
        int rows = mapper.softDeleteWarehouse(scope.tenantId, scope.memberId, warehouseId, operator());
        audit("仓库管理", "DELETE", "WAREHOUSE", warehouseId, existing.name, "删除仓库", existing, null);
        return rows;
    }

    /** 查询当前商户商品。 */
    public List<RentalBusinessVO> listProducts(String keyword, Integer productType, Integer stockMode,
            Long classId, Integer status)
    {
        Scope scope = scope();
        List<Long> allowedClassIds = selectedClassIds();
        return mapList(mapper.selectProducts(scope.tenantId, scope.memberId, trim(keyword), productType,
                stockMode, classId, status, allowedClassIds), this::toView);
    }

    /** 查询商品详情。 */
    public RentalBusinessVO getProduct(Long productId)
    {
        Scope scope = scope();
        RentalBusinessVO view = requireView(mapper.selectProduct(scope.tenantId, scope.memberId, productId), "商品不存在或已删除");
        assertAllowedClass(view.classId);
        if (RentalCode.ProductType.ACCESSORY.getCode().equals(view.productType) && view.customFlag != null
                && view.customFlag == 1)
        {
            view.compatibleModelIds = mapper.selectAccessoryModelIds(scope.tenantId, scope.memberId, productId);
        }
        return view;
    }

    /** 新增商品，校验商品类型、库存模式和型号分类，并按商品类型使用统一编码规则。 */
    @Transactional(rollbackFor = Exception.class)
    public RentalBusinessVO addProduct(RentalBusinessCommand command)
    {
        writeGuard();
        Scope scope = scope();
        validateProduct(command, null);
        String codeType = RentalCode.ProductType.ACCESSORY.getCode().equals(command.productType)
                ? "accessory" : "product";
        String code = codeGeneratorClient.next(codeType);
        List<Long> compatibleModelIds = normalizeIds(command.compatibleModelIds);
        if (RentalCode.ProductType.ACCESSORY.getCode().equals(command.productType))
        {
            requireCompatibleModels(command, compatibleModelIds);
            command.modelId = compatibleModelIds.get(0);
        }
        mapper.insertProduct(scope.tenantId, scope.memberId, command.classId, command.modelId, code,
                required(command.productName, "商品名称不能为空"), command.productType, command.stockMode, trim(command.brand),
                trim(command.modelName), trim(command.memorySpec), trim(command.description), command.purchaseCost, command.salePrice,
                status(command.status), trim(command.remark), operator());
        Long productId = mapper.selectLastInsertId();
        if (RentalCode.ProductType.ACCESSORY.getCode().equals(command.productType))
        {
            mapper.insertMerchantAccessory(scope.tenantId, scope.memberId, productId, code,
                    required(command.productName, "配件名称不能为空"), command.classId, trim(command.brand),
                    trim(command.memorySpec), null, trim(command.description), status(command.status),
                    trim(command.remark), operator());
            mapper.insertAccessoryModelIds(scope.tenantId, scope.memberId, productId, compatibleModelIds, operator());
        }
        RentalBusinessVO created = getProduct(productId);
        if (RentalCode.ProductType.ACCESSORY.getCode().equals(command.productType)) created.compatibleModelIds = compatibleModelIds;
        audit("商品管理", "CREATE", "PRODUCT", created.id, created.productName, "创建商品", null, created);
        return created;
    }

    /** 修改商品，已有库存事实后禁止切换库存模式、商品类型或分类型号。 */
    @Transactional(rollbackFor = Exception.class)
    public RentalBusinessVO updateProduct(RentalBusinessCommand command)
    {
        writeGuard();
        Scope scope = scope();
        Long productId = requiredId(command == null ? null : command.id, "商品唯一编号不能为空");
        RentalBusinessVO existing = getProduct(productId);
        validateProduct(command, existing);
        List<Long> compatibleModelIds = normalizeIds(command.compatibleModelIds);
        if (RentalCode.ProductType.ACCESSORY.getCode().equals(command.productType))
        {
            requireCompatibleModels(command, compatibleModelIds);
            command.modelId = compatibleModelIds.get(0);
        }
        int referenceCount = mapper.countProductReferences(scope.tenantId, scope.memberId, productId);
        if (referenceCount > 0
                && (!same(existing.stockMode, command.stockMode) || !same(existing.productType, command.productType)
                || !sameId(existing.classId, command.classId)
                || !sameId(existing.modelId, command.modelId)))
        {
            throw new ServiceException("商品已有库存、套餐或订单引用，不能修改分类、型号、商品类型或库存模式");
        }
        mapper.updateProduct(scope.tenantId, scope.memberId, productId, command.classId, command.modelId,
                existing.productCode, required(command.productName, "商品名称不能为空"),
                command.productType, command.stockMode, trim(command.brand), trim(command.modelName), trim(command.memorySpec),
                trim(command.description), command.purchaseCost, command.salePrice, status(command.status), trim(command.remark), operator());
        RentalBusinessVO updated = getProduct(productId);
        if (RentalCode.ProductType.ACCESSORY.getCode().equals(command.productType))
        {
            mapper.updateMerchantAccessory(scope.tenantId, scope.memberId, productId,
                    required(command.productName, "配件名称不能为空"), command.classId, trim(command.brand),
                    trim(command.memorySpec), null, trim(command.description), status(command.status),
                    trim(command.remark), operator());
            mapper.deleteAccessoryModelIds(scope.tenantId, scope.memberId, productId, operator());
            mapper.insertAccessoryModelIds(scope.tenantId, scope.memberId, productId, compatibleModelIds, operator());
            updated.compatibleModelIds = compatibleModelIds;
        }
        audit("商品管理", "UPDATE", "PRODUCT", productId, updated.productName, "编辑商品", existing, updated);
        return updated;
    }

    /** 软删除商品，存在 SN、配件、库存余额、套餐或订单引用时拒绝删除。 */
    @Transactional(rollbackFor = Exception.class)
    public int deleteProduct(Long productId)
    {
        writeGuard();
        Scope scope = scope();
        RentalBusinessVO existing = getProduct(productId);
        if (mapper.countProductReferences(scope.tenantId, scope.memberId, productId) > 0)
        {
            throw new ServiceException("商品仍被库存、SN、配件、套餐或订单引用，不能删除");
        }
        int rows = mapper.softDeleteProduct(scope.tenantId, scope.memberId, productId, operator());
        if (RentalCode.ProductType.ACCESSORY.getCode().equals(existing.productType) && existing.customFlag != null
                && existing.customFlag == 1)
        {
            mapper.softDeleteMerchantAccessory(scope.tenantId, scope.memberId, productId, operator());
        }
        audit("商品管理", "DELETE", "PRODUCT", productId, existing.productName, "删除商品", existing, null);
        return rows;
    }

    /** 查询当前用户仓库范围内的 SN。 */
    public List<RentalBusinessVO> listSns(String keyword, Long productId, Long warehouseId, Integer assetStatus)
    {
        Scope scope = scope();
        if (warehouseId != null)
        {
            MerchantContextUtils.checkWarehouseAccess(warehouseId);
        }
        return mapList(mapper.selectSns(scope.tenantId, scope.memberId, trim(keyword), productId, warehouseId,
                assetStatus, scope.warehouseIds, scope.allWarehouseAccess, selectedClassIds()), this::toView);
    }

    /** 查询 SN 详情。 */
    public RentalBusinessVO getSn(Long snId)
    {
        Scope scope = scope();
        RentalBusinessVO view = requireView(mapper.selectSn(scope.tenantId, scope.memberId, snId), "SN不存在或已删除");
        MerchantContextUtils.checkWarehouseAccess(view.warehouseId);
        getProduct(view.productId);
        return view;
    }

    /** 新增 SN，商品必须是逐件库存且仓库必须属于当前用户权限范围。 */
    @Transactional(rollbackFor = Exception.class)
    public RentalBusinessVO addSn(RentalBusinessCommand command)
    {
        writeGuard();
        Scope scope = scope();
        validateSn(command, null);
        mapper.insertSn(scope.tenantId, scope.memberId, command.productId, command.warehouseId,
                required(command.snCode, "SN编码不能为空"), trim(command.barcode), command.assetStatus,
                trim(command.conditionLevel), operator(), trim(command.remark));
        RentalBusinessVO created = getSn(mapper.selectLastInsertId());
        audit("SN管理", "CREATE", "SN", created.id, created.snCode, "创建SN", null, created);
        return created;
    }

    /** 修改 SN，已租用或预占的 SN 不允许直接改变仓库和状态。 */
    @Transactional(rollbackFor = Exception.class)
    public RentalBusinessVO updateSn(RentalBusinessCommand command)
    {
        writeGuard();
        Long snId = requiredId(command == null ? null : command.id, "SN唯一编号不能为空");
        RentalBusinessVO existing = getSn(snId);
        validateSn(command, existing);
        if (mapper.countSnCode(scope().tenantId, scope().memberId, required(command.snCode, "SN编码不能为空"), snId) > 0)
        {
            throw new ServiceException("SN编码已存在");
        }
        if ((RentalCode.InventoryStatus.RENTED.getCode().equals(existing.assetStatus)
                || RentalCode.InventoryStatus.RESERVED.getCode().equals(existing.assetStatus))
                && (!same(existing.assetStatus, command.assetStatus) || !sameId(existing.warehouseId, command.warehouseId)))
        {
            throw new ServiceException("已租用或已预占的SN不能直接修改仓库或状态");
        }
        Scope scope = scope();
        mapper.updateSn(scope.tenantId, scope.memberId, snId, command.warehouseId, command.snCode, trim(command.barcode),
                command.assetStatus, trim(command.conditionLevel), operator(), trim(command.remark));
        RentalBusinessVO updated = getSn(snId);
        audit("SN管理", "UPDATE", "SN", snId, updated.snCode, "编辑SN", existing, updated);
        return updated;
    }

    /** 软删除 SN，已租用或预占状态必须先完成业务归还或释放。 */
    @Transactional(rollbackFor = Exception.class)
    public int deleteSn(Long snId)
    {
        writeGuard();
        RentalBusinessVO existing = getSn(snId);
        if (RentalCode.InventoryStatus.RENTED.getCode().equals(existing.assetStatus)
                || RentalCode.InventoryStatus.RESERVED.getCode().equals(existing.assetStatus))
        {
            throw new ServiceException("已租用或已预占的SN不能删除");
        }
        Scope scope = scope();
        int rows = mapper.softDeleteSn(scope.tenantId, scope.memberId, snId, operator());
        audit("SN管理", "DELETE", "SN", snId, existing.snCode, "删除SN", existing, null);
        return rows;
    }

    /** 查询当前用户仓库范围内的配件实例。 */
    public List<RentalBusinessVO> listAccessories(String keyword, Long productId, Long warehouseId, Integer itemStatus)
    {
        Scope scope = scope();
        if (warehouseId != null)
        {
            MerchantContextUtils.checkWarehouseAccess(warehouseId);
        }
        return mapList(mapper.selectAccessories(scope.tenantId, scope.memberId, trim(keyword), productId, warehouseId,
                itemStatus, scope.warehouseIds, scope.allWarehouseAccess, selectedClassIds()), this::toView);
    }

    /** 查询配件实例详情。 */
    public RentalBusinessVO getAccessory(Long accessoryItemId)
    {
        Scope scope = scope();
        RentalBusinessVO view = requireView(mapper.selectAccessory(scope.tenantId, scope.memberId, accessoryItemId), "配件不存在或已删除");
        if (view.warehouseId != null)
        {
            MerchantContextUtils.checkWarehouseAccess(view.warehouseId);
        }
        getProduct(view.productId);
        return view;
    }

    /** 新增逐件配件实例；总数型配件通过商品和库存余额维护，实例编码由平台统一生成。 */
    @Transactional(rollbackFor = Exception.class)
    public RentalBusinessVO addAccessory(RentalBusinessCommand command)
    {
        writeGuard();
        validateAccessory(command, null);
        Scope scope = scope();
        mapper.insertAccessory(scope.tenantId, scope.memberId, command.productId, command.warehouseId,
                codeGeneratorClient.next("accessory_item"), RentalCode.StockMode.SERIAL.getCode(), command.itemStatus,
                operator(), trim(command.remark));
        RentalBusinessVO created = getAccessory(mapper.selectLastInsertId());
        audit("配件管理", "CREATE", "ACCESSORY", created.id, created.itemCode, "创建配件", null, created);
        return created;
    }

    /** 修改配件实例。 */
    @Transactional(rollbackFor = Exception.class)
    public RentalBusinessVO updateAccessory(RentalBusinessCommand command)
    {
        writeGuard();
        Long itemId = requiredId(command == null ? null : command.id, "配件唯一编号不能为空");
        RentalBusinessVO existing = getAccessory(itemId);
        validateAccessory(command, existing);
        Scope scope = scope();
        if ((RentalCode.InventoryStatus.RENTED.getCode().equals(existing.itemStatus)
                || RentalCode.InventoryStatus.RESERVED.getCode().equals(existing.itemStatus))
                && (!same(existing.itemStatus, command.itemStatus) || !sameId(existing.warehouseId, command.warehouseId)))
        {
            throw new ServiceException("已租用或已预占的配件不能直接修改仓库或状态");
        }
        mapper.updateAccessory(scope.tenantId, scope.memberId, itemId, command.warehouseId, existing.itemCode,
                command.itemStatus, operator(), trim(command.remark));
        RentalBusinessVO updated = getAccessory(itemId);
        audit("配件管理", "UPDATE", "ACCESSORY", itemId, updated.itemCode, "编辑配件", existing, updated);
        return updated;
    }

    /** 软删除配件实例。 */
    @Transactional(rollbackFor = Exception.class)
    public int deleteAccessory(Long itemId)
    {
        writeGuard();
        RentalBusinessVO existing = getAccessory(itemId);
        if (RentalCode.InventoryStatus.RENTED.getCode().equals(existing.itemStatus)
                || RentalCode.InventoryStatus.RESERVED.getCode().equals(existing.itemStatus))
        {
            throw new ServiceException("已租用或已预占的配件不能删除");
        }
        Scope scope = scope();
        int rows = mapper.softDeleteAccessory(scope.tenantId, scope.memberId, itemId, operator());
        audit("配件管理", "DELETE", "ACCESSORY", itemId, existing.itemCode, "删除配件", existing, null);
        return rows;
    }

    /** 查询当前商户仓库范围内的库存汇总。 */
    public List<RentalBusinessVO> listInventory(String keyword, Long warehouseId, Integer productType)
    {
        Scope scope = scope();
        if (warehouseId != null)
        {
            MerchantContextUtils.checkWarehouseAccess(warehouseId);
        }
        List<RentalBusinessVO> result = mapList(mapper.selectInventory(scope.tenantId, scope.memberId, trim(keyword), warehouseId,
                productType, scope.warehouseIds, scope.allWarehouseAccess, selectedClassIds()), this::toView);
        for (RentalBusinessVO item : result)
        {
            item.availableQty = Math.max(0, number(item.onHandQty) - number(item.reservedQty));
        }
        return result;
    }

    /** 查询指定商品在指定仓库的库存明细，详情也必须通过经营分类和仓库范围校验。 */
    public RentalBusinessVO getInventory(Long productId, Long warehouseId)
    {
        RentalBusinessVO product = getProduct(requiredId(productId, "商品唯一编号不能为空"));
        requireWarehouse(scope(), requiredId(warehouseId, "仓库唯一编号不能为空"), false);
        return listInventory(product.productCode, warehouseId, product.productType).stream()
                .filter(item -> sameId(item.productId, product.id)).findFirst()
                .orElseThrow(() -> new ServiceException("库存记录不存在或已删除"));
    }

    /** 软删除零库存余额；库存流水和历史订单保持不变。 */
    @Transactional(rollbackFor = Exception.class)
    public int deleteInventory(Long productId, Long warehouseId)
    {
        writeGuard();
        Scope scope = scope();
        RentalBusinessVO product = getProduct(requiredId(productId, "商品唯一编号不能为空"));
        requireWarehouse(scope, requiredId(warehouseId, "仓库唯一编号不能为空"), false);
        Map<String, Object> balance = mapper.selectBalanceForUpdate(scope.tenantId, scope.memberId,
                product.id, warehouseId, RentalCode.InventoryStatus.AVAILABLE.getCode());
        if (balance == null || balance.isEmpty())
        {
            throw new ServiceException("库存余额不存在或已删除");
        }
        if (value(balance, "onHandQty", 0) != 0)
        {
            throw new ServiceException("只有零库存余额才允许删除");
        }
        int rows = mapper.softDeleteInventoryBalance(scope.tenantId, scope.memberId, product.id, warehouseId, operator());
        audit("库存管理", "DELETE", "INVENTORY", product.id + "@" + warehouseId,
                product.productName, "删除库存余额", balance, null);
        return rows;
    }

    /** 调整数量型商品库存，所有变更都追加库存流水并禁止库存变为负数。 */
    @Transactional(rollbackFor = Exception.class)
    public RentalBusinessVO adjustInventory(RentalBusinessCommand command)
    {
        writeGuard();
        if (command == null)
        {
            throw new ServiceException("库存调整数据不能为空");
        }
        Scope scope = scope();
        Long productId = requiredId(command == null ? null : command.productId, "商品唯一编号不能为空");
        Long warehouseId = requiredId(command == null ? null : command.warehouseId, "仓库唯一编号不能为空");
        MerchantContextUtils.checkWarehouseAccess(warehouseId);
        RentalBusinessVO product = getProduct(productId);
        if (!RentalCode.StockMode.QUANTITY.getCode().equals(product.stockMode))
        {
            throw new ServiceException("逐件商品必须通过SN或配件实例入库，不能直接调整数量库存");
        }
        int changeQty = command.changeQty == null ? 0 : command.changeQty;
        if (changeQty == 0)
        {
            throw new ServiceException("库存调整数量不能为零");
        }
        Map<String, Object> balance = mapper.selectBalanceForUpdate(scope.tenantId, scope.memberId, productId, warehouseId,
                RentalCode.InventoryStatus.AVAILABLE.getCode());
        int current = value(balance, "onHandQty", 0);
        long targetLong = (long) current + changeQty;
        if (targetLong < 0 || targetLong > Integer.MAX_VALUE)
        {
            throw new ServiceException(targetLong < 0 ? "库存不足，调整后库存不能小于零" : "库存数量超过系统允许范围");
        }
        int target = (int) targetLong;
        if (balance == null || balance.isEmpty())
        {
            mapper.insertBalance(scope.tenantId, scope.memberId, productId, warehouseId,
                    RentalCode.InventoryStatus.AVAILABLE.getCode(), target,
                    operator(), trim(command.reason));
        }
        else
        {
            mapper.updateBalance(scope.tenantId, scope.memberId, valueLong(balance, "id"), target, operator(), trim(command.reason));
        }
        mapper.insertInventoryLedger(scope.tenantId, scope.memberId, warehouseId, productId,
                code(command.changeType, RentalCode.InventoryLedgerChangeType.values(),
                        RentalCode.InventoryLedgerChangeType.MANUAL_ADJUST.getCode(), "库存变更类型不合法"),
                changeQty, trim(command.reason), operator());
        RentalBusinessVO updated = listInventory(product.productCode, warehouseId, product.productType).stream()
                .filter(item -> sameId(item.productId, productId)).findFirst().orElse(product);
        audit("库存管理", "UPDATE", "INVENTORY", productId + "@" + warehouseId, product.productName,
                "调整库存数量", balance, updated);
        return updated;
    }

    /** 校验商品类型、库存模式和设备型号必填规则。 */
    private void validateProduct(RentalBusinessCommand command, RentalBusinessVO existing)
    {
        if (command == null)
        {
            throw new ServiceException("商品数据不能为空");
        }
        command.productName = required(command.productName, "商品名称不能为空");
        if (!contains(RentalCode.ProductType.values(), command.productType))
        {
            throw new ServiceException("商品类型不合法");
        }
        if (!contains(RentalCode.StockMode.values(), command.stockMode))
        {
            throw new ServiceException("库存模式不合法");
        }
        if (command.classId == null)
        {
            throw new ServiceException("商品必须选择产品分类");
        }
        assertAllowedClass(command.classId);
        if (RentalCode.ProductType.DEVICE.getCode().equals(command.productType) && command.modelId == null)
        {
            throw new ServiceException("设备商品必须选择标准型号");
        }
        if (command.purchaseCost != null && command.purchaseCost.signum() < 0)
        {
            throw new ServiceException("采购成本不能小于零");
        }
        if (command.salePrice != null && command.salePrice.signum() < 0)
        {
            throw new ServiceException("销售价格不能小于零");
        }
        BrandModel standardModel = command.modelId == null ? null : findActiveModel(command.modelId, command.classId);
        normalizeProductModel(command, standardModel);
        Scope scope = scope();
        if (scope.tenantId == null || scope.memberId == null)
        {
            throw new ServiceException("商户上下文不完整");
        }
    }

    /** 校验自定义配件至少绑定一个有效兼容型号，并确保型号属于当前分类。 */
    private void requireCompatibleModels(RentalBusinessCommand command, List<Long> modelIds)
    {
        if (modelIds.isEmpty()) throw new ServiceException("至少选择一个兼容产品型号");
        for (Long modelId : modelIds) findActiveModel(modelId, command.classId);
    }

    /**
     * 使用平台标准型号补齐商品展示字段。
     *
     * <p>商品仍然保留自身的品牌、型号和规格快照，便于历史订单展示；当请求
     * 绑定标准型号且对应字段为空时，使用平台当前有效型号补齐，避免前端必须
     * 重复输入标准主数据。已经填写的商品规格允许保留，用于表达同型号的商品
     * 变体，例如不同存储规格或销售包装。</p>
     *
     * @param command 待校验商品命令
     * @param model 已经完成启用状态和分类归属校验的标准型号
     */
    private void normalizeProductModel(RentalBusinessCommand command, BrandModel model)
    {
        if (command.modelId == null || model == null)
        {
            return;
        }
        if (StringUtils.isEmpty(trim(command.brand)))
        {
            command.brand = model.getBrand();
        }
        if (StringUtils.isEmpty(trim(command.modelName)))
        {
            command.modelName = model.getModelName();
        }
        if (StringUtils.isEmpty(trim(command.memorySpec)))
        {
            command.memorySpec = model.getMemorySpec();
        }
    }

    /** 校验 SN 商品和仓库归属。 */
    private void validateSn(RentalBusinessCommand command, RentalBusinessVO existing)
    {
        if (command == null)
        {
            throw new ServiceException("SN数据不能为空");
        }
        Long productId = requiredId(command.productId, "商品唯一编号不能为空");
        RentalBusinessVO product = getProduct(productId);
        if (!RentalCode.StockMode.SERIAL.getCode().equals(product.stockMode))
        {
            throw new ServiceException("数量型商品不能创建SN");
        }
        Long warehouseId = requiredId(command.warehouseId, "仓库唯一编号不能为空");
        requireWarehouse(scope(), warehouseId, true);
        command.assetStatus = code(command.assetStatus, RentalCode.InventoryStatus.values(),
                existing == null ? RentalCode.InventoryStatus.AVAILABLE.getCode() : existing.assetStatus, "SN状态不合法");
    }

    /** 校验逐件配件商品和仓库归属。 */
    private void validateAccessory(RentalBusinessCommand command, RentalBusinessVO existing)
    {
        if (command == null)
        {
            throw new ServiceException("配件数据不能为空");
        }
        RentalBusinessVO product = getProduct(requiredId(command.productId, "商品唯一编号不能为空"));
        if (!RentalCode.ProductType.ACCESSORY.getCode().equals(product.productType)
                || !RentalCode.StockMode.SERIAL.getCode().equals(product.stockMode))
        {
            throw new ServiceException("只有逐件管理的配件商品可以创建配件实例");
        }
        requireWarehouse(scope(), requiredId(command.warehouseId, "仓库唯一编号不能为空"), true);
        command.itemStatus = code(command.itemStatus, RentalCode.InventoryStatus.values(),
                existing == null ? RentalCode.InventoryStatus.AVAILABLE.getCode() : existing.itemStatus, "配件状态不合法");
    }

    /** 校验套餐至少包含一个主商品且同一版本不重复引用商品。 */
    private void validatePackage(RentalBusinessCommand command, Long packageId)
    {
        if (command == null)
        {
            throw new ServiceException("套餐数据不能为空");
        }
        required(command.packageName, "套餐名称不能为空");
        if (command.components == null || command.components.isEmpty())
        {
            throw new ServiceException("套餐至少需要一个BOM组件");
        }
        Set<Long> products = new HashSet<Long>();
        int mainCount = 0;
        for (RentalBusinessComponentCommand component : command.components)
        {
            if (component == null || component.productId == null || component.requiredQty == null || component.requiredQty <= 0)
            {
                throw new ServiceException("套餐组件必须选择商品且需求数量大于零");
            }
            if (!products.add(component.productId))
            {
                throw new ServiceException("同一套餐版本不能重复引用同一个商品");
            }
            RentalBusinessVO product = getProduct(component.productId);
            Integer role = code(component.componentRole, RentalCode.PackageComponentRole.values(),
                    RentalCode.PackageComponentRole.ACCESSORY.getCode(), "套餐组件角色不合法");
            if (RentalCode.PackageComponentRole.MAIN.getCode().equals(role))
            {
                mainCount++;
                if (!RentalCode.ProductType.DEVICE.getCode().equals(product.productType))
                {
                    throw new ServiceException("套餐主组件必须来自设备商品");
                }
            }
            component.componentRole = role;
            component.allocationPolicy = code(component.allocationPolicy, RentalCode.AllocationPolicy.values(),
                    RentalCode.AllocationPolicy.ANY.getCode(), "套餐组件分配策略不合法");
            component.sortNo = number(component.sortNo);
        }
        if (mainCount != 1)
        {
            throw new ServiceException("套餐必须且只能有一个主设备组件");
        }
    }

    /** 发布套餐新版本并写入不可变 BOM。 */
    private void publishPackageVersion(Scope scope, Long packageId, List<RentalBusinessComponentCommand> components)
    {
        mapper.retireActivePackageVersion(scope.tenantId, scope.memberId, packageId, operator());
        int versionNo = mapper.nextPackageVersionNo(scope.tenantId, scope.memberId, packageId);
        String bomHash = bomHash(components);
        mapper.insertPackageVersion(scope.tenantId, scope.memberId, packageId, versionNo,
                RentalCode.PackageVersionStatus.ACTIVE.getCode(), bomHash, operator());
        Long versionId = mapper.selectLastInsertId();
        mapper.insertPackageComponents(scope.tenantId, scope.memberId, versionId, components, operator());
    }

    /** 将套餐详情补齐当前生效版本和组件名称。 */
    private void fillPackageVersion(Scope scope, RentalBusinessVO packageView)
    {
        Map<String, Object> version = mapper.selectActivePackageVersion(scope.tenantId, scope.memberId, packageView.id);
        if (version == null || version.isEmpty())
        {
            return;
        }
        packageView.packageVersionId = valueLong(version, "packageVersionId");
        packageView.versionNo = value(version, "versionNo", 0);
        packageView.versionStatus = valueNullable(version, "versionStatus");
        for (Map<String, Object> row : mapper.selectPackageComponents(scope.tenantId, scope.memberId, packageView.packageVersionId))
        {
            RentalBusinessComponentVO component = new RentalBusinessComponentVO();
            component.componentId = valueLong(row, "componentId");
            component.productId = valueLong(row, "productId");
            component.componentRole = valueNullable(row, "componentRole");
            component.requiredQty = value(row, "requiredQty", 0);
            component.allocationPolicy = valueNullable(row, "allocationPolicy");
            component.sortNo = value(row, "sortNo", 0);
            packageView.components.add(component);
        }
    }

    /** 校验仓库存在、启用并属于当前用户可访问范围。 */
    private RentalBusinessVO requireWarehouse(Scope scope, Long warehouseId, boolean requireEnabled)
    {
        MerchantContextUtils.checkWarehouseAccess(warehouseId);
        RentalBusinessVO warehouse = requireView(mapper.selectWarehouse(scope.tenantId, scope.memberId, warehouseId), "仓库不存在或已删除");
        if (requireEnabled && warehouse.status != null && warehouse.status != 0)
        {
            throw new ServiceException("仓库已停用，不能进行业务操作");
        }
        return warehouse;
    }

    /** 返回当前可信商户作用域。 */
    private Scope scope()
    {
        return new Scope(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                MerchantContextUtils.getWarehouseIds(), MerchantContextUtils.hasAllWarehouseAccess());
    }

    /** 校验写操作需要已审核商户。 */
    private void writeGuard()
    {
        MerchantContextUtils.requireMerchantApproved();
    }

    /** 查询当前商户经营分类，失败或未配置时禁止展示商品数据。 */
    private List<Long> selectedClassIds()
    {
        R<List<Long>> response = merchantProductClassService.selectedIds(SecurityConstants.INNER);
        if (response == null || R.FAIL == response.getCode())
        {
            throw new ServiceException(response == null ? "商户经营分类服务无响应" : response.getMsg());
        }
        return response.getData() == null ? Collections.emptyList() : response.getData();
    }

    /** 校验商品分类属于当前商户经营范围。 */
    private void assertAllowedClass(Long classId)
    {
        R<Boolean> response = merchantProductClassService.assertAllowed(classId, SecurityConstants.INNER);
        if (response == null || R.FAIL == response.getCode() || !Boolean.TRUE.equals(response.getData()))
        {
            throw new ServiceException(response == null ? "商户经营分类服务无响应"
                    : StringUtils.defaultIfEmpty(response.getMsg(), "当前商户未经营该产品分类"));
        }
    }

    /**
     * 从平台内部服务查找有效标准型号并校验其分类归属。
     *
     * @param modelId 标准型号唯一编号
     * @param classId 商品分类唯一编号
     * @return 匹配的有效标准型号
     */
    private BrandModel findActiveModel(Long modelId, Long classId)
    {
        R<List<BrandModel>> response = brandModelService.listActive(SecurityConstants.INNER);
        if (response == null || R.FAIL == response.getCode() || response.getData() == null)
        {
            throw new ServiceException(response == null ? "平台标准品牌型号服务无响应"
                    : StringUtils.defaultIfEmpty(response.getMsg(), "平台标准品牌型号服务暂不可用"));
        }
        BrandModel model = response.getData().stream().filter(item -> sameId(item.getModelId(), modelId)).findFirst().orElse(null);
        if (model == null)
        {
            throw new ServiceException("标准型号不存在、已停用或不允许经营");
        }
        if (!sameId(model.getClassId(), classId))
        {
            throw new ServiceException("商品分类与标准型号所属分类不一致");
        }
        return model;
    }

    /** 返回当前操作用户名称。 */
    private Long operator()
    {
        return SecurityUtils.getUserId();
    }

    /**
     * 写入商户业务审计；审计服务未注入时仅用于兼容单元测试，不影响主业务处理。
     *
     * @param module 业务模块
     * @param operationType 操作类型
     * @param targetType 业务对象类型
     * @param targetId 业务对象唯一编号
     * @param targetName 业务对象名称
     * @param description 业务描述
     * @param before 变更前对象
     * @param after 变更后对象
     */
    private void audit(String module, String operationType, String targetType, Object targetId,
            String targetName, String description, Object before, Object after)
    {
        if (auditRecorder != null)
        {
            auditRecorder.record(module, operationType, targetType, targetId == null ? null : String.valueOf(targetId),
                    targetName, description, before, after);
        }
    }

    /** 将 Map 集合转换为统一展示对象。 */
    private List<RentalBusinessVO> mapList(List<Map<String, Object>> rows, java.util.function.Function<Map<String, Object>, RentalBusinessVO> mapperFunction)
    {
        List<RentalBusinessVO> result = new ArrayList<RentalBusinessVO>();
        for (Map<String, Object> row : rows)
        {
            result.add(mapperFunction.apply(row));
        }
        return result;
    }

    /** 将数据库行转换为统一展示对象。 */
    private RentalBusinessVO toView(Map<String, Object> row)
    {
        RentalBusinessVO view = new RentalBusinessVO();
        view.id = valueLong(row, "id");
        view.warehouseId = valueLong(row, "warehouseId");
        view.productId = valueLong(row, "productId");
        view.packageId = valueLong(row, "packageId");
        view.classId = valueLong(row, "classId");
        view.modelId = valueLong(row, "modelId");
        view.code = text(row, "code");
        view.name = text(row, "name");
        view.productType = valueNullable(row, "productType");
        view.customFlag = valueNullable(row, "customFlag");
        view.merchantAccessoryId = valueLong(row, "merchantAccessoryId");
        view.productCode = text(row, "productCode");
        view.productName = text(row, "productName");
        view.brand = text(row, "brand");
        view.modelName = text(row, "modelName");
        view.memorySpec = text(row, "memorySpec");
        view.description = text(row, "description");
        String compatibleModelIds = text(row, "compatibleModelIds");
        if (!StringUtils.isEmpty(compatibleModelIds))
        {
            view.compatibleModelIds = new ArrayList<Long>();
            for (String value : compatibleModelIds.split(","))
            {
                try { view.compatibleModelIds.add(Long.valueOf(value)); }
                catch (NumberFormatException ignored) { /* 查询结果只包含数据库型号编号。 */ }
            }
        }
        view.stockMode = valueNullable(row, "stockMode");
        view.snCode = text(row, "snCode");
        view.barcode = text(row, "barcode");
        view.itemCode = text(row, "itemCode");
        view.assetStatus = valueNullable(row, "assetStatus");
        view.itemStatus = valueNullable(row, "itemStatus");
        view.conditionLevel = text(row, "conditionLevel");
        view.warehouseName = text(row, "warehouseName");
        view.warehouseCity = text(row, "warehouseCity");
        view.province = text(row, "province");
        view.city = text(row, "city");
        view.district = text(row, "district");
        view.address = text(row, "address");
        view.contactName = text(row, "contactName");
        view.contactPhone = text(row, "contactPhone");
        view.longitude = decimal(row, "longitude");
        view.latitude = decimal(row, "latitude");
        view.pickupRuleJson = text(row, "pickupRuleJson");
        view.packageCode = text(row, "packageCode");
        view.packageName = text(row, "packageName");
        view.platformCode = text(row, "platformCode");
        view.versionStatus = valueNullable(row, "versionStatus");
        view.status = valueNullable(row, "status");
        view.enabled = valueNullable(row, "enabled");
        view.sortNo = valueNullable(row, "sortNo");
        view.versionNo = valueNullable(row, "versionNo");
        view.onHandQty = valueNullable(row, "onHandQty");
        view.reservedQty = valueNullable(row, "reservedQty");
        view.requiredQty = valueNullable(row, "requiredQty");
        view.returnEnabled = valueNullable(row, "returnEnabled");
        view.allianceShareEnabled = valueNullable(row, "allianceShareEnabled");
        view.purchaseCost = decimal(row, "purchaseCost");
        view.salePrice = decimal(row, "salePrice");
        view.remark = text(row, "remark");
        return view;
    }

    /** 将数据库 Map 解析为非空业务对象。 */
    private RentalBusinessVO requireView(Map<String, Object> row, String message)
    {
        if (row == null || row.isEmpty())
        {
            throw new ServiceException(message);
        }
        return toView(row);
    }

    /** 生成规范化 BOM 哈希，保证同一组件集合只产生一个可审计摘要。 */
    private String bomHash(List<RentalBusinessComponentCommand> components)
    {
        List<RentalBusinessComponentCommand> ordered = new ArrayList<RentalBusinessComponentCommand>(components);
        ordered.sort(Comparator.comparing(component -> String.valueOf(component.productId)));
        String canonical = ordered.stream().map(item -> item.productId + ":" + item.componentRole + ":"
                + item.requiredQty + ":" + item.allocationPolicy + ":" + item.sortNo).collect(Collectors.joining("|"));
        try
        {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte item : digest)
            {
                result.append(String.format("%02x", item));
            }
            return result.toString();
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new ServiceException("无法生成套餐BOM摘要：" + exception.getMessage());
        }
    }

    /** 规范化唯一编号集合。 */
    private List<Long> normalizeIds(List<Long> source)
    {
        return source == null ? Collections.emptyList() : new ArrayList<Long>(new LinkedHashSet<Long>(source));
    }

    /** 以固定默认值读取状态。 */
    private int status(Integer value)
    {
        if (value != null && value != 0 && value != 1)
        {
            throw new ServiceException("状态只能是启用或停用");
        }
        return value == null ? 0 : value;
    }

    /** 以固定默认值读取布尔标志。 */
    private int flag(Integer value)
    {
        return value == null ? 1 : (value == 0 ? 0 : 1);
    }

    /** 读取可选布尔字段并应用业务默认值。 */
    private int booleanValue(Integer value, int defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value != 0 && value != 1)
        {
            throw new ServiceException("布尔字段只能是 0 或 1");
        }
        return value;
    }

    /** 以固定默认值读取启用状态。 */
    private int enabled(Integer value)
    {
        if (value != null && value != 0 && value != 1)
        {
            throw new ServiceException("启用状态只能是启用或停用");
        }
        return value == null ? 1 : value;
    }

    /** 以固定默认值读取排序值。 */
    private int number(Integer value)
    {
        if (value != null && value < 0)
        {
            throw new ServiceException("排序值不能小于零");
        }
        return value == null ? 0 : value;
    }

    /** 读取必填文本并清除首尾空白。 */
    private String required(String value, String message)
    {
        String normalized = trim(value);
        if (StringUtils.isEmpty(normalized))
        {
            throw new ServiceException(message);
        }
        return normalized;
    }

    /** 读取必填唯一编号。 */
    private Long requiredId(Long value, String message)
    {
        if (value == null || value <= 0)
        {
            throw new ServiceException(message);
        }
        return value;
    }

    /** 返回空安全文本。 */
    private String trim(String value)
    {
        return value == null ? null : value.trim();
    }

    /** 校验数值编码是否属于指定枚举。 */
    private <E extends Enum<E> & NumericCode> boolean contains(E[] values, Integer value)
    {
        if (value == null)
        {
            return false;
        }
        for (E item : values)
        {
            if (item.getCode().equals(value))
            {
                return true;
            }
        }
        return false;
    }

    /** 读取并校验数值编码，可在缺省时使用领域默认值。 */
    private <E extends Enum<E> & NumericCode> Integer code(Integer value, E[] values, Integer fallback, String message)
    {
        Integer actual = value == null ? fallback : value;
        if (!contains(values, actual))
        {
            throw new ServiceException(message);
        }
        return actual;
    }

    /**
     * 校验内部接口收到的租户和商户范围，避免空唯一编号导致跨商户查询或数据库全表条件退化。
     *
     * @param tenantId 可信租户唯一编号
     * @param memberId 可信商户唯一编号
     */
    private void requireInternalScope(Long tenantId, Long memberId)
    {
        if (tenantId == null || tenantId <= 0 || memberId == null || memberId <= 0)
        {
            throw new ServiceException("租赁业务内部调用缺少有效商户范围");
        }
    }

    /** 比较数值枚举编码。 */
    private boolean same(Integer left, Integer right)
    {
        return left == null ? right == null : left.equals(right);
    }

    /** 比较唯一编号。 */
    private boolean sameId(Long left, Long right)
    {
        return left == null ? right == null : left.equals(right);
    }

    /** 从 Map 读取文本字段。 */
    private String text(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    /** 从 Map 读取整数并提供默认值。 */
    private int value(Map<String, Object> row, String key, int fallback)
    {
        Integer value = valueNullable(row, key);
        return value == null ? fallback : value;
    }

    /**
     * 从 JDBC Map 中读取可空整数，统一兼容数值、字符串以及 MySQL
     * 驱动对 {@code TINYINT(1)} 返回的布尔值。
     *
     * @param row JDBC 返回的字段 Map
     * @param key 待读取的字段别名
     * @return 字段对应的整数，字段不存在或值为空时返回 {@code null}
     */
    static Integer valueNullable(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        if (value instanceof Boolean)
        {
            return ((Boolean) value) ? 1 : 0;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty())
        {
            return null;
        }
        if ("true".equalsIgnoreCase(text))
        {
            return 1;
        }
        if ("false".equalsIgnoreCase(text))
        {
            return 0;
        }
        try
        {
            return Integer.valueOf(text);
        }
        catch (NumberFormatException exception)
        {
            throw new ServiceException("数据库字段 " + key + " 不是有效整数");
        }
    }

    /** 从 Map 读取长整数。 */
    private Long valueLong(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        if (value == null)
        {
            return null;
        }
        return value instanceof Number ? ((Number) value).longValue() : Long.valueOf(String.valueOf(value));
    }

    /** 从 Map 读取金额。 */
    private java.math.BigDecimal decimal(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? null : value instanceof java.math.BigDecimal
                ? (java.math.BigDecimal) value : new java.math.BigDecimal(String.valueOf(value));
    }

    /** 当前商户与仓库范围快照。 */
    private static final class Scope
    {
        private final Long tenantId;
        private final Long memberId;
        private final Set<Long> warehouseIds;
        private final boolean allWarehouseAccess;

        /** 创建不可变商户范围快照。 */
        private Scope(Long tenantId, Long memberId, Set<Long> warehouseIds, boolean allWarehouseAccess)
        {
            this.tenantId = tenantId;
            this.memberId = memberId;
            this.warehouseIds = warehouseIds;
            this.allWarehouseAccess = allWarehouseAccess;
        }
    }
}
