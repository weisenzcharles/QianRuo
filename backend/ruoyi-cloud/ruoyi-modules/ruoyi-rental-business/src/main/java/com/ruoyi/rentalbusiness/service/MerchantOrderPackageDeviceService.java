package com.ruoyi.rentalbusiness.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.rentalbusiness.mapper.MerchantInventoryMapper;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderMapper;
import com.ruoyi.rentalbusiness.model.MerchantOrderCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderPackageDeviceBindCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderPackageDeviceOptionVO;

/** 订单套餐逐件设备绑定服务。绑定结果只更新订单套餐 JSON 快照。 */
@Service
public class MerchantOrderPackageDeviceService
{
    private static final int MAIN_PRODUCT_ROLE = 1;
    private static final int NON_MAIN_PRODUCT_ROLE = 4;
    private static final int SERIAL_STOCK_MODE = 1;

    private final MerchantOrderService orderService;
    private final MerchantOrderMapper orderMapper;
    private final MerchantInventoryMapper inventoryMapper;
    private final ObjectMapper objectMapper;

    public MerchantOrderPackageDeviceService(MerchantOrderService orderService, MerchantOrderMapper orderMapper,
            MerchantInventoryMapper inventoryMapper, ObjectMapper objectMapper)
    {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.inventoryMapper = inventoryMapper;
        this.objectMapper = objectMapper;
    }

    /** 查询订单套餐明细可选择的逐件设备。 */
    @Transactional(readOnly = true)
    public List<MerchantOrderPackageDeviceOptionVO> list(Long orderId, Integer itemIndex)
    {
        MerchantContextUtils.requireMerchantApproved();
        PackageDeviceContext context = loadContext(orderId, itemIndex);
        List<Map<String, Object>> rows = inventoryMapper.selectOrderPackageDeviceOptions(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), orderId,
                context.productId, context.order.warehouseId);
        List<MerchantOrderPackageDeviceOptionVO> result = new ArrayList<>();
        if (rows == null)
        {
            return result;
        }
        Set<Long> alreadyBound = boundUnitIds(context.root, context.itemIndex);
        for (Map<String, Object> row : rows)
        {
            MerchantOrderPackageDeviceOptionVO option = toOption(row);
            if (option.unitId != null && !alreadyBound.contains(option.unitId))
            {
                result.add(option);
            }
        }
        return result;
    }

    /** 锁定并绑定逐件设备，防止两个订单同时绑定同一库存单元。 */
    @Transactional(rollbackFor = Exception.class)
    public void bind(Long orderId, MerchantOrderPackageDeviceBindCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null)
        {
            throw new ServiceException("设备绑定参数不能为空");
        }
        PackageDeviceContext context = loadContext(orderId, command.itemIndex);
        ensureOrderPackageEditable(context.order);
        List<Long> unitIds = normalizeUnitIds(command.unitIds);
        if (unitIds.size() != context.quantity)
        {
            throw new ServiceException("请选择 " + context.quantity + " 台设备");
        }
        Set<Long> alreadyBound = boundUnitIds(context.root, context.itemIndex);
        for (Long unitId : unitIds)
        {
            if (alreadyBound.contains(unitId))
            {
                throw new ServiceException("所选设备已绑定到当前套餐的其他明细");
            }
        }
        List<Map<String, Object>> rows = inventoryMapper.selectOrderPackageDeviceOptionsForUpdate(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), orderId,
                context.productId, context.order.warehouseId, unitIds);
        if (rows == null || rows.size() != unitIds.size())
        {
            throw new ServiceException("所选设备已被占用或不属于当前发货仓库，请刷新后重试");
        }
        Map<Long, Map<String, Object>> rowById = new java.util.LinkedHashMap<>();
        for (Map<String, Object> row : rows)
        {
            Long unitId = mapLong(row, "unitId");
            if (unitId != null)
            {
                rowById.put(unitId, row);
            }
        }
        if (rowById.size() != unitIds.size() || !rowById.keySet().containsAll(unitIds))
        {
            throw new ServiceException("所选设备已被占用或不属于当前发货仓库，请刷新后重试");
        }
        ObjectNode item = context.item;
        ArrayNode jsonUnitIds = objectMapper.createArrayNode();
        ArrayNode unitCodes = objectMapper.createArrayNode();
        ArrayNode serialNumbers = objectMapper.createArrayNode();
        for (Long unitId : unitIds)
        {
            Map<String, Object> row = rowById.get(unitId);
            String unitCode = text(row, "unitCode");
            String serialNumber = text(row, "serialNumber");
            jsonUnitIds.add(String.valueOf(unitId));
            unitCodes.add(unitCode == null ? "" : unitCode);
            serialNumbers.add(serialNumber == null ? unitCode : serialNumber);
        }
        item.set("unitIds", jsonUnitIds);
        item.set("unitCodes", unitCodes);
        item.set("serialNumbers", serialNumbers);
        item.put("unitId", String.valueOf(unitIds.get(0)));
        item.put("unitCode", text(rowById.get(unitIds.get(0)), "unitCode"));
        String firstSerialNumber = text(rowById.get(unitIds.get(0)), "serialNumber");
        item.put("serialNumber", firstSerialNumber == null
                ? text(rowById.get(unitIds.get(0)), "unitCode") : firstSerialNumber);
        try
        {
            String packageDetailsJson = objectMapper.writeValueAsString(context.root);
            int updated = orderMapper.updateOrderPackageDetails(MerchantContextUtils.getTenantId(),
                    MerchantContextUtils.getMemberId(), orderId, packageDetailsJson, SecurityUtils.getUserId());
            if (updated == 0)
            {
                throw new ServiceException("订单套餐明细保存失败，请刷新后重试");
            }
        }
        catch (JsonProcessingException exception)
        {
            throw new ServiceException("订单套餐明细生成失败");
        }
    }

    private PackageDeviceContext loadContext(Long orderId, Integer itemIndex)
    {
        if (orderId == null || itemIndex == null || itemIndex < 0)
        {
            throw new ServiceException("套餐明细参数不正确");
        }
        MerchantOrderCreateCommand order = orderService.get(orderId);
        ensureOrderPackageEditable(order);
        if (order.packageId == null || order.warehouseId == null)
        {
            throw new ServiceException("订单套餐或发货仓库未设置");
        }
        if (order.packageDetailsJson == null || order.packageDetailsJson.trim().isEmpty())
        {
            throw new ServiceException("订单尚未生成套餐明细");
        }
        JsonNode rootNode;
        try
        {
            rootNode = objectMapper.readTree(order.packageDetailsJson);
        }
        catch (JsonProcessingException exception)
        {
            throw new ServiceException("订单套餐详情格式不正确");
        }
        if (!(rootNode instanceof ObjectNode))
        {
            throw new ServiceException("订单尚未生成套餐明细");
        }
        ObjectNode root = (ObjectNode) rootNode;
        JsonNode itemsNode = root.path("items");
        if (!itemsNode.isArray() || itemIndex >= itemsNode.size()
                || !(itemsNode.get(itemIndex) instanceof ObjectNode))
        {
            throw new ServiceException("套餐明细不存在");
        }
        ObjectNode item = (ObjectNode) itemsNode.get(itemIndex);
        Long productId = longValue(item, "productId", "product_id");
        if (productId == null)
        {
            throw new ServiceException("该套餐明细未关联商品");
        }
        Map<String, Object> product = inventoryMapper.selectProduct(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), productId);
        if (product == null || !isSerialStockMode(value(item, "stockMode", "stock_mode") == null
                ? product.get("stockMode") : value(item, "stockMode", "stock_mode")))
        {
            throw new ServiceException("该套餐明细不是逐件库存设备，不能选择设备");
        }
        Integer itemRole = integerValue(item, "itemRole", "item_role");
        if (!Integer.valueOf(MAIN_PRODUCT_ROLE).equals(itemRole)
                && !Integer.valueOf(NON_MAIN_PRODUCT_ROLE).equals(itemRole))
        {
            throw new ServiceException("该套餐明细不支持选择设备");
        }
        Integer quantity = integerValue(item, "quantity");
        return new PackageDeviceContext(order, root, item, itemIndex, productId,
                quantity == null || quantity < 1 ? 1 : quantity);
    }

    private void ensureOrderPackageEditable(MerchantOrderCreateCommand order)
    {
        if (order != null && order.sourceOrderId == null
                && orderMapper.countAcceptedAllianceChildren(MerchantContextUtils.getTenantId(),
                        MerchantContextUtils.getMemberId(), order.orderId) > 0)
        {
            throw new ServiceException("原订单已有已接受的商户联盟订单，仅允许查看");
        }
    }

    private List<Long> normalizeUnitIds(List<Long> values)
    {
        if (values == null || values.isEmpty())
        {
            throw new ServiceException("请选择设备");
        }
        Set<Long> unique = new LinkedHashSet<>();
        for (Long value : values)
        {
            if (value == null || value <= 0 || !unique.add(value))
            {
                throw new ServiceException("设备选择不正确");
            }
        }
        return new ArrayList<>(unique);
    }

    private Set<Long> boundUnitIds(ObjectNode root, int exceptItemIndex)
    {
        Set<Long> result = new HashSet<>();
        JsonNode items = root.path("items");
        if (!items.isArray())
        {
            return result;
        }
        for (int index = 0; index < items.size(); index++)
        {
            if (index == exceptItemIndex || !items.get(index).isObject())
            {
                continue;
            }
            JsonNode item = items.get(index);
            JsonNode unitIds = item.path("unitIds");
            if (unitIds.isArray())
            {
                for (JsonNode unitId : unitIds)
                {
                    Long value = parseLong(unitId);
                    if (value != null)
                    {
                        result.add(value);
                    }
                }
            }
            Long unitId = longValue(item, "unitId");
            if (unitId != null)
            {
                result.add(unitId);
            }
        }
        return result;
    }

    private MerchantOrderPackageDeviceOptionVO toOption(Map<String, Object> row)
    {
        MerchantOrderPackageDeviceOptionVO option = new MerchantOrderPackageDeviceOptionVO();
        option.unitId = mapLong(row, "unitId");
        option.unitCode = text(row, "unitCode");
        option.serialNumber = text(row, "serialNumber");
        option.productName = text(row, "productName");
        option.modelName = text(row, "modelName");
        option.warehouseName = text(row, "warehouseName");
        return option;
    }

    private boolean isSerialStockMode(Object value)
    {
        if (value == null)
        {
            return false;
        }
        String text = String.valueOf(value).trim();
        return String.valueOf(SERIAL_STOCK_MODE).equals(text)
                || "sn".equalsIgnoreCase(text) || "serial".equalsIgnoreCase(text);
    }

    private Object value(JsonNode node, String... names)
    {
        if (node == null || !node.isObject())
        {
            return null;
        }
        for (String name : names)
        {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull() && !value.isMissingNode())
            {
                return value.isValueNode() ? value.asText() : value;
            }
        }
        return null;
    }

    private String text(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        if (value == null || String.valueOf(value).trim().isEmpty())
        {
            return null;
        }
        return String.valueOf(value).trim();
    }

    private Long mapLong(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        if (value == null)
        {
            return null;
        }
        return parseLong(String.valueOf(value));
    }

    private Integer integerValue(JsonNode node, String... names)
    {
        Long value = longValue(node, names);
        return value == null ? null : value.intValue();
    }

    private Long longValue(JsonNode node, String... names)
    {
        for (String name : names)
        {
            JsonNode value = node == null ? null : node.get(name);
            Long parsed = parseLong(value);
            if (parsed != null)
            {
                return parsed;
            }
        }
        return null;
    }

    private Long parseLong(JsonNode value)
    {
        if (value == null || value.isNull())
        {
            return null;
        }
        return parseLong(value.asText());
    }

    private Long parseLong(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            return null;
        }
        try
        {
            return Long.valueOf(value.trim());
        }
        catch (NumberFormatException exception)
        {
            return null;
        }
    }

    private static final class PackageDeviceContext
    {
        private final MerchantOrderCreateCommand order;
        private final ObjectNode root;
        private final ObjectNode item;
        private final int itemIndex;
        private final Long productId;
        private final int quantity;

        private PackageDeviceContext(MerchantOrderCreateCommand order, ObjectNode root, ObjectNode item,
                int itemIndex, Long productId, int quantity)
        {
            this.order = order;
            this.root = root;
            this.item = item;
            this.itemIndex = itemIndex;
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}
