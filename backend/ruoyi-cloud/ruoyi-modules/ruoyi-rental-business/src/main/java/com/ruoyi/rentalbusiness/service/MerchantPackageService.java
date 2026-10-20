package com.ruoyi.rentalbusiness.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.rentalbusiness.mapper.MerchantPackageMapper;
import com.ruoyi.rentalbusiness.model.MerchantPackageCommand;
import com.ruoyi.rentalbusiness.model.MerchantPackageItemCommand;
import com.ruoyi.system.api.RemotePackageService;
import com.ruoyi.system.api.model.PlatformPackage;
import com.ruoyi.system.api.model.PlatformPackageItem;
import com.ruoyi.system.api.RemoteAccessoryService;
import com.ruoyi.system.api.model.PlatformAccessoryOption;

/** 新架构商户套餐服务，平台套餐复制后形成商户自己的不可变版本。 */
@Service
public class MerchantPackageService
{
    private static final Logger log = LoggerFactory.getLogger(MerchantPackageService.class);

    private final MerchantPackageMapper mapper;
    private final RemotePackageService remotePackageService;
    private final RemoteAccessoryService remoteAccessoryService;
    private final CodeGeneratorClient codeGeneratorClient;

    public MerchantPackageService(MerchantPackageMapper mapper, RemotePackageService remotePackageService,
            RemoteAccessoryService remoteAccessoryService, CodeGeneratorClient codeGeneratorClient)
    {
        this.mapper = mapper;
        this.remotePackageService = remotePackageService;
        this.remoteAccessoryService = remoteAccessoryService;
        this.codeGeneratorClient = codeGeneratorClient;
    }

    /** 返回当前商户可用于指定主型号的自定义配件和平台官方配件。 */
    public List<Map<String, Object>> accessoryOptions(Long modelId)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (modelId == null || modelId <= 0)
        {
            return new ArrayList<Map<String, Object>>();
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> item : mapper.selectMerchantAccessoryOptions(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), modelId))
        {
            item.put("sourceType", "custom");
            result.add(item);
        }
        com.ruoyi.common.core.domain.R<List<PlatformAccessoryOption>> response =
                remoteAccessoryService.listActive(SecurityConstants.INNER);
        if (response == null || response.getCode() != 200 || response.getData() == null)
            throw new ServiceException("平台配件服务暂不可用");
        for (PlatformAccessoryOption item : response.getData())
        {
            if (modelId != null && (item.getModelIds() == null || !item.getModelIds().contains(modelId)))
            {
                continue;
            }
            Map<String, Object> option = new LinkedHashMap<String, Object>();
            option.put("accessoryId", item.getAccessoryId());
            option.put("accessoryCode", item.getAccessoryCode());
            option.put("accessoryName", item.getAccessoryName());
            option.put("classId", item.getClassId());
            option.put("brandId", item.getBrandId());
            option.put("specification", item.getSpecification());
            option.put("unit", item.getUnit());
            option.put("status", item.getStatus());
            option.put("modelIds", item.getModelIds());
            option.put("sourceType", "official");
            result.add(option);
        }
        return result;
    }

    /** 保留无参数调用兼容性；未选择主型号时不返回可选配件。 */
    public List<Map<String, Object>> accessoryOptions()
    {
        return accessoryOptions(null);
    }

    public List<Map<String, Object>> list(String keyword, Integer status)
    {
        MerchantContextUtils.requireMerchantApproved();
        return mapper.selectList(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                StringUtils.trim(keyword), status);
    }

    /** 查询平台当前可复制套餐，避免商户端直接访问平台服务。 */
    public List<PlatformPackage> platformOptions()
    {
        MerchantContextUtils.requireMerchantApproved();
        com.ruoyi.common.core.domain.R<List<PlatformPackage>> result = remotePackageService.listActive(SecurityConstants.INNER);
        if (result == null || result.getCode() != 200 || result.getData() == null) throw new ServiceException("平台套餐服务暂不可用");
        return result.getData();
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> copyPlatform(MerchantPackageCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.platformPackageId == null || command.platformPackageId <= 0)
        {
            throw new ServiceException("平台套餐唯一编号不能为空");
        }
        PlatformPackage source = findPlatformPackage(command.platformPackageId);
        Map<String, Object> product = mapper.selectProductByModel(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), source.getModelId());
        if (product == null) throw new ServiceException("当前商户尚未配置该平台型号商品");
        Long operator = SecurityUtils.getUserId();
        String name = StringUtils.trim(command.packageName);
        if (name == null || name.isEmpty()) name = source.getPackageName();
        List<MerchantPackageItemCommand> items = convert(source, product, source.getModelId());
        String code = codeGeneratorClient.next("package");
        mapper.insertPackage(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), source.getPackageId(),
                source.getClassId(), source.getModelId(), code, name, command.sortNo == null ? 0 : command.sortNo,
                operator, StringUtils.trim(command.remark));
        Long packageId = mapper.selectLastInsertId();
        mapper.insertVersion(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), packageId, 1,
                hash(items), operator);
        Long versionId = mapper.selectLastVersionId();
        for (MerchantPackageItemCommand item : items)
        {
            mapper.insertItem(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), versionId,
                    item.itemRole, item.productId, item.accessoryId, item.accessoryItemProductId, item.merchantAccessoryId, item.quantity,
                    item.requiredFlag, item.sortNo, operator);
        }
        return mapper.selectById(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), packageId);
    }

    /** 新增商户自定义套餐，主商品和配件必须来自当前商户商品目录。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> add(MerchantPackageCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        validateCommand(command, false);
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Long operator = SecurityUtils.getUserId();
        Map<String, Object> mainProduct = validateItems(command, tenantId, memberId);
        String code = codeGeneratorClient.next("package");
        mapper.insertCustomPackage(tenantId, memberId, command.classId, command.modelId, code,
                command.packageName.trim(), command.status == null ? 0 : command.status,
                command.sortNo == null ? 0 : command.sortNo, operator, StringUtils.trim(command.remark));
        Long packageId = mapper.selectLastInsertId();
        saveVersion(command.items, tenantId, memberId, packageId, mainProduct, operator);
        return mapper.selectById(tenantId, memberId, packageId);
    }

    /** 编辑商户套餐并发布新版本，历史订单仍读取原快照。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> update(MerchantPackageCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        validateCommand(command, true);
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Map<String, Object> existing = mapper.selectById(tenantId, memberId, command.id);
        if (existing == null) throw new ServiceException("套餐不存在或已删除");
        Map<String, Object> mainProduct = validateItems(command, tenantId, memberId);
        Long operator = SecurityUtils.getUserId();
        mapper.updatePackage(tenantId, memberId, command.id, command.classId, command.modelId,
                command.packageName.trim(),
                command.status == null ? 0 : command.status, command.sortNo == null ? 0 : command.sortNo,
                operator, StringUtils.trim(command.remark));
        Long activeVersionId = numberValue(mapper.selectVersionById(tenantId, memberId, command.id), "packageVersionId");
        if (activeVersionId != null) mapper.softDeletePackageItems(activeVersionId, operator);
        int versionNo = mapper.selectNextVersionNo(command.id);
        mapper.softDeletePackageVersions(tenantId, memberId, command.id, operator);
        saveVersion(command.items, tenantId, memberId, command.id, mainProduct, operator, versionNo);
        return mapper.selectById(tenantId, memberId, command.id);
    }

    public Map<String, Object> detail(Long packageId)
    {
        MerchantContextUtils.requireMerchantApproved();
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Map<String, Object> result = mapper.selectById(tenantId, memberId, packageId);
        if (result == null) throw new ServiceException("套餐不存在或已删除");
        Map<String, Object> version = mapper.selectVersionById(tenantId, memberId, packageId);
        List<Map<String, Object>> items = version == null ? new ArrayList<>()
                : mapper.selectItems(numberValue(version, "packageVersionId"));
        enrichOfficialAccessoryNames(items);
        result.put("items", items);
        return result;
    }

    /**
     * 套餐明细表只保存官方配件唯一编号，详情接口需要将其转换为平台当前名称。
     * 平台服务暂不可用时保留“官方配件”占位文本，不能阻断商户套餐详情打开。
     */
    private void enrichOfficialAccessoryNames(List<Map<String, Object>> items)
    {
        if (items == null || items.isEmpty() || remoteAccessoryService == null) return;
        Set<Long> accessoryIds = new LinkedHashSet<Long>();
        for (Map<String, Object> item : items)
        {
            if (item == null || !Long.valueOf(2L).equals(numberValue(item, "itemRole"))) continue;
            Long accessoryId = numberValue(item, "accessoryId");
            if (accessoryId != null) accessoryIds.add(accessoryId);
        }
        if (accessoryIds.isEmpty()) return;
        try
        {
            R<List<PlatformAccessoryOption>> response = remoteAccessoryService.listActive(SecurityConstants.INNER);
            if (response == null || response.getCode() != R.SUCCESS || response.getData() == null)
            {
                log.warn("套餐详情官方配件名称读取失败，保留已有名称，占位配件数量={}, code={}", accessoryIds.size(),
                        response == null ? null : response.getCode());
                return;
            }
            Map<Long, PlatformAccessoryOption> options = new LinkedHashMap<Long, PlatformAccessoryOption>();
            for (PlatformAccessoryOption option : response.getData())
            {
                if (option != null && option.getAccessoryId() != null && accessoryIds.contains(option.getAccessoryId()))
                    options.put(option.getAccessoryId(), option);
            }
            for (Map<String, Object> item : items)
            {
                if (item == null || !Long.valueOf(2L).equals(numberValue(item, "itemRole"))) continue;
                PlatformAccessoryOption option = options.get(numberValue(item, "accessoryId"));
                if (option == null) continue;
                item.put("itemName", option.getAccessoryName());
                item.put("itemCode", option.getAccessoryCode());
                item.put("itemSpecification", option.getSpecification());
                item.put("itemUnit", option.getUnit());
            }
        }
        catch (RuntimeException exception)
        {
            log.warn("套餐详情官方配件名称读取异常，保留已有名称", exception);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int delete(Long packageId)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (mapper.selectById(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), packageId) == null)
            throw new ServiceException("套餐不存在或已删除");
        Long operator = SecurityUtils.getUserId();
        mapper.softDeletePackageVersions(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), packageId, operator);
        return mapper.softDeletePackage(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), packageId, operator);
    }

    private void validateCommand(MerchantPackageCommand command, boolean update)
    {
        if (command == null || (update && (command.id == null || command.id <= 0))) throw new ServiceException("套餐唯一编号不能为空");
        if (command.classId == null || command.classId <= 0 || command.modelId == null || command.modelId <= 0)
            throw new ServiceException("请选择产品分类和型号");
        if (StringUtils.isEmpty(StringUtils.trim(command.packageName))) throw new ServiceException("套餐名称不能为空");
        if (command.items == null || command.items.isEmpty()) throw new ServiceException("请至少添加一项套餐商品");
    }

    private Map<String, Object> validateItems(MerchantPackageCommand command, Long tenantId, Long memberId)
    {
        Map<String, Object> main = null;
        int mainCount = 0;
        Set<Long> productIds = new HashSet<Long>();
        Integer mainRole = RentalCode.PackageComponentRole.MAIN.getCode();
        Integer officialAccessoryRole = RentalCode.PackageComponentRole.ACCESSORY.getCode();
        Integer merchantAccessoryRole = RentalCode.PackageComponentRole.CONSUMABLE.getCode();
        Integer nonMainProductRole = RentalCode.PackageComponentRole.NON_MAIN_PRODUCT.getCode();

        // 主商品决定套餐的分类和型号，先解析它，再用解析结果校验所有配件的兼容性。
        for (MerchantPackageItemCommand item : command.items)
        {
            if (item == null || item.quantity == null || item.quantity <= 0) throw new ServiceException("套餐数量必须大于0");
            if (mainRole.equals(item.itemRole))
            {
                if (item.productId == null) throw new ServiceException("主商品不存在或不属于当前商户");
                Map<String, Object> product = mapper.selectProduct(tenantId, memberId, item.productId);
                if (product == null) throw new ServiceException("主商品不存在或不属于当前商户");
                main = product;
                mainCount++;
                productIds.add(item.productId);
            }
        }
        if (mainCount != 1 || main == null) throw new ServiceException("套餐必须且只能包含一个主商品");

        Long mainClassId = numberValue(main, "class_id");
        Long mainModelId = numberValue(main, "model_id");
        if (mainClassId == null || mainModelId == null) throw new ServiceException("主商品分类或型号不存在");
        command.classId = mainClassId;
        command.modelId = mainModelId;

        List<PlatformAccessoryOption> officialAccessories = null;
        for (MerchantPackageItemCommand item : command.items)
        {
            if (mainRole.equals(item.itemRole))
            {
                item.accessoryId = null;
                item.accessoryItemProductId = null;
                item.merchantAccessoryId = null;
                continue;
            }
            if (officialAccessoryRole.equals(item.itemRole))
            {
                if (officialAccessories == null) officialAccessories = officialAccessories();
                PlatformAccessoryOption accessory = officialAccessories.stream()
                        .filter(option -> option.getAccessoryId().equals(item.accessoryId)).findFirst().orElse(null);
                if (accessory == null || accessory.getModelIds() == null || !accessory.getModelIds().contains(mainModelId))
                    throw new ServiceException("官方配件不存在或与主商品型号不兼容");
                item.productId = null;
                item.accessoryItemProductId = null;
                item.merchantAccessoryId = null;
            }
            else if (merchantAccessoryRole.equals(item.itemRole))
            {
                Map<String, Object> accessory = item.merchantAccessoryId == null ? null
                        : mapper.selectMerchantAccessory(tenantId, memberId, item.merchantAccessoryId);
                if (accessory == null && item.accessoryItemProductId != null)
                {
                    accessory = mapper.selectAccessoryProduct(tenantId, memberId, item.accessoryItemProductId);
                    if (accessory != null)
                    {
                        Map<String, Object> migrated = mapper.selectMerchantAccessory(tenantId, memberId,
                                numberValue(accessory, "merchant_accessory_id"));
                        if (migrated != null) accessory = migrated;
                    }
                }
                if (accessory == null) throw new ServiceException("商户配件不存在或不属于当前商户");
                item.merchantAccessoryId = numberValue(accessory, "accessory_id");
                item.productId = null;
                item.accessoryId = null;
                if (item.merchantAccessoryId == null || mapper.selectMerchantAccessoryModelIds(tenantId, memberId,
                        item.merchantAccessoryId, mainModelId).isEmpty())
                    throw new ServiceException("配件与主商品型号不兼容");
            }
            else if (nonMainProductRole.equals(item.itemRole))
            {
                if (item.productId == null) throw new ServiceException("非主产品不存在或不属于当前商户");
                Map<String, Object> product = mapper.selectPackageProduct(tenantId, memberId, item.productId);
                if (product == null) throw new ServiceException("非主产品不存在或不属于当前商户");
                if (!productIds.add(item.productId)) throw new ServiceException("套餐不能重复添加相同的产品");
                item.accessoryId = null;
                item.accessoryItemProductId = null;
                item.merchantAccessoryId = null;
            }
            else throw new ServiceException("套餐明细角色不合法");
        }
        return main;
    }

    private List<PlatformAccessoryOption> officialAccessories()
    {
        com.ruoyi.common.core.domain.R<List<PlatformAccessoryOption>> response =
                remoteAccessoryService.listActive(SecurityConstants.INNER);
        if (response == null || response.getCode() != 200 || response.getData() == null)
            throw new ServiceException("平台配件服务暂不可用");
        return response.getData();
    }

    private void saveVersion(List<MerchantPackageItemCommand> items, Long tenantId, Long memberId, Long packageId,
            Map<String, Object> mainProduct, Long operator) { saveVersion(items, tenantId, memberId, packageId, mainProduct, operator, 1); }

    private void saveVersion(List<MerchantPackageItemCommand> items, Long tenantId, Long memberId, Long packageId,
            Map<String, Object> mainProduct, Long operator, int versionNo)
    {
        mapper.insertVersion(tenantId, memberId, packageId, versionNo, hash(items), operator);
        Long versionId = mapper.selectLastVersionId();
        for (MerchantPackageItemCommand item : items)
            mapper.insertItem(tenantId, memberId, versionId, item.itemRole, item.productId, item.accessoryId,
                    item.accessoryItemProductId, item.merchantAccessoryId, item.quantity, item.requiredFlag == null ? 1 : item.requiredFlag,
                    item.sortNo == null ? 0 : item.sortNo, operator);
    }

    private Long numberValue(Map<String, Object> value, String key)
    { return value == null || value.get(key) == null ? null : ((Number) value.get(key)).longValue(); }

    private PlatformPackage findPlatformPackage(Long packageId)
    {
        com.ruoyi.common.core.domain.R<List<PlatformPackage>> result = remotePackageService.listActive(SecurityConstants.INNER);
        if (result == null || result.getCode() != 200 || result.getData() == null) throw new ServiceException("平台套餐服务暂不可用");
        return result.getData().stream().filter(item -> packageId.equals(item.getPackageId())).findFirst()
                .orElseThrow(() -> new ServiceException("平台套餐不存在或未发布"));
    }

    private List<MerchantPackageItemCommand> convert(PlatformPackage source, Map<String, Object> product, Long modelId)
    {
        List<MerchantPackageItemCommand> result = new ArrayList<MerchantPackageItemCommand>();
        Integer mainRole = RentalCode.PackageComponentRole.MAIN.getCode();
        Integer officialAccessoryRole = RentalCode.PackageComponentRole.ACCESSORY.getCode();
        Integer merchantAccessoryRole = RentalCode.PackageComponentRole.CONSUMABLE.getCode();
        Integer nonMainProductRole = RentalCode.PackageComponentRole.NON_MAIN_PRODUCT.getCode();
        for (PlatformPackageItem sourceItem : source.getItems())
        {
            MerchantPackageItemCommand item = new MerchantPackageItemCommand();
            Integer sourceRole = sourceItem.getItemRole();
            if (mainRole.equals(sourceRole))
            {
                item.itemRole = mainRole;
                item.productId = ((Number) product.get("product_id")).longValue();
            }
            else if (nonMainProductRole.equals(sourceRole))
            {
                item.itemRole = nonMainProductRole;
                if (sourceItem.getModelId() == null) throw new ServiceException("平台套餐中的非主产品型号不存在");
                Map<String, Object> matchedProduct = mapper.selectPackageProductByModel(
                        MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), sourceItem.getModelId());
                if (matchedProduct == null) throw new ServiceException("请先配置平台套餐中的非主产品");
                item.productId = numberValue(matchedProduct, "product_id");
            }
            else if (officialAccessoryRole.equals(sourceRole) || merchantAccessoryRole.equals(sourceRole))
            {
                item.itemRole = merchantAccessoryRole;
                Map<String, Object> accessory = mapper.selectMerchantAccessoryByModel(
                        MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), modelId);
                if (accessory == null) throw new ServiceException("请先配置与平台套餐兼容的商户配件");
                item.merchantAccessoryId = numberValue(accessory, "accessory_id");
                item.accessoryItemProductId = numberValue(accessory, "product_id");
            }
            else
            {
                throw new ServiceException("平台套餐明细角色不合法");
            }
            item.quantity = sourceItem.getQuantity(); item.requiredFlag = sourceItem.getRequiredFlag(); item.sortNo = sourceItem.getSortNo();
            result.add(item);
        }
        return result;
    }

    private String hash(List<MerchantPackageItemCommand> items)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256"); StringBuilder value = new StringBuilder();
            for (MerchantPackageItemCommand item : items) value.append(item.itemRole).append(':').append(item.productId)
                    .append(':').append(item.accessoryId).append(':').append(item.merchantAccessoryId)
                    .append(':').append(item.quantity).append(';');
            StringBuilder result = new StringBuilder();
            for (byte item : digest.digest(value.toString().getBytes(StandardCharsets.UTF_8))) result.append(String.format("%02x", item));
            return result.toString();
        }
        catch (Exception exception) { throw new ServiceException("套餐明细摘要生成失败"); }
    }
}
