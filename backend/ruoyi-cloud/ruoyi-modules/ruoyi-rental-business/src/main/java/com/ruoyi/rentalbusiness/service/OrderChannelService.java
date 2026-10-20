package com.ruoyi.rentalbusiness.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.rentalbusiness.mapper.RentalWorkflowMapper;
import com.ruoyi.rentalbusiness.model.OrderChannelCommand;
import com.ruoyi.rentalbusiness.model.OrderChannelVO;

/**
 * 订单渠道应用服务。
 *
 * <p>平台联盟渠道与商户自定义渠道共用一个只读查询模型，但写入边界完全分离：平台只能维护
 * platform/alliance，商户只能维护 merchant/custom。套餐和订单的转移都在同一事务中完成关系更新、
 * 版本校验和不可变转移记录，重复请求由幂等键返回成功而不会重复扣减或重复写入。</p>
 */
@Service
public class OrderChannelService
{
    private final RentalWorkflowMapper mapper;
    private final CodeGeneratorClient codeGeneratorClient;
    private final MerchantBusinessAuditRecorder auditRecorder;

    /** 注入渠道数据访问对象和统一编码客户端。 */
    @org.springframework.beans.factory.annotation.Autowired
    public OrderChannelService(RentalWorkflowMapper mapper, CodeGeneratorClient codeGeneratorClient,
            MerchantBusinessAuditRecorder auditRecorder)
    {
        this.mapper = mapper;
        this.codeGeneratorClient = codeGeneratorClient;
        this.auditRecorder = auditRecorder;
    }

    /** 保留历史构造方式，供不需要审计记录器的测试使用。 */
    public OrderChannelService(RentalWorkflowMapper mapper, CodeGeneratorClient codeGeneratorClient)
    {
        this(mapper, codeGeneratorClient, null);
    }

    /** 查询平台联盟渠道和当前商户自定义渠道。 */
    public List<OrderChannelVO> listChannels(String keyword, Integer status)
    {
        Scope scope = merchantScope();
        return mapChannels(mapper.selectVisibleChannels(scope.tenantId, scope.memberId, trim(keyword), status));
    }

    /** 查询平台管理员可维护的联盟渠道。 */
    public List<OrderChannelVO> listPlatformChannels(String keyword, Integer status)
    {
        assertPlatformClient();
        return mapChannels(mapper.selectPlatformChannels(trim(keyword), status));
    }

    /** 新增平台联盟渠道或商户自定义渠道。 */
    @Transactional(rollbackFor = Exception.class)
    public OrderChannelVO addChannel(OrderChannelCommand command)
    {
        if (ClientContextHolder.isPlatformClient())
        {
            assertPlatformClient();
            validateName(command);
            if (command.channelType != null
                    && !RentalCode.OrderChannelType.ALLIANCE.getCode().equals(command.channelType))
            {
                throw new ServiceException("平台端只能新增联盟订单渠道");
            }
            String code = codeGeneratorClient.next("order_channel");
            mapper.insertChannel(0L, 0L, code, command.channelName.trim(),
                    RentalCode.OrderChannelType.ALLIANCE.getCode(), RentalCode.OrderChannelScope.PLATFORM.getCode(),
                    status(command.status), number(command.sortNo), operatorId(), command.reason);
            return findPlatformChannel(code);
        }
        MerchantContextUtils.requireMerchantApproved();
        Scope scope = merchantScope();
        validateName(command);
        if (command.channelType != null && !RentalCode.OrderChannelType.CUSTOM.getCode().equals(command.channelType))
        {
            throw new ServiceException("商户端只能新增自定义订单渠道");
        }
        String code = codeGeneratorClient.next("order_channel");
        mapper.insertChannel(scope.tenantId, scope.memberId, code, command.channelName.trim(),
                RentalCode.OrderChannelType.CUSTOM.getCode(), RentalCode.OrderChannelScope.MERCHANT.getCode(),
                status(command.status), number(command.sortNo), operatorId(), command.reason);
        OrderChannelVO created = findChannel(scope.tenantId, scope.memberId, code, false);
        audit("订单渠道", "CREATE", "ORDER_CHANNEL", created.channelId, created.channelName, "创建订单渠道", null, created);
        return created;
    }

    /** 修改商户自定义渠道；平台联盟渠道的变更由平台接口单独负责。 */
    @Transactional(rollbackFor = Exception.class)
    public OrderChannelVO updateChannel(OrderChannelCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        Scope scope = merchantScope();
        Long channelId = required(command == null ? null : command.channelId, "订单渠道唯一编号不能为空");
        Map<String, Object> existing = mapper.selectChannel(scope.tenantId, scope.memberId, channelId, true);
        if (existing == null)
        {
            throw new ServiceException("只能修改当前商户自定义渠道");
        }
        validateName(command);
        mapper.updateChannel(scope.tenantId, scope.memberId, channelId, command.channelName.trim(), status(command.status),
                number(command.sortNo), operatorId(), command.reason);
        OrderChannelVO updated = findChannelById(scope.tenantId, scope.memberId, channelId, true);
        audit("订单渠道", "UPDATE", "ORDER_CHANNEL", channelId, updated.channelName, "编辑订单渠道", existing, updated);
        return updated;
    }

    /** 修改平台联盟渠道名称、状态和排序。 */
    @Transactional(rollbackFor = Exception.class)
    public OrderChannelVO updatePlatformChannel(OrderChannelCommand command)
    {
        assertPlatformClient();
        Long channelId = required(command == null ? null : command.channelId, "订单渠道唯一编号不能为空");
        Map<String, Object> existing = mapper.selectPlatformChannel(channelId);
        if (existing == null || !RentalCode.OrderChannelScope.PLATFORM.getCode()
                .equals(value(existing, "scopeType", -1)))
        {
            throw new ServiceException("平台联盟渠道不存在");
        }
        validateName(command);
        mapper.updatePlatformChannel(channelId, command.channelName.trim(), status(command.status), number(command.sortNo),
                operatorId(), command.reason);
        return findPlatformChannelById(channelId);
    }

    /** 软删除平台联盟渠道，仍被订单或套餐引用时拒绝删除。 */
    @Transactional(rollbackFor = Exception.class)
    public int deletePlatformChannel(Long channelId)
    {
        assertPlatformClient();
        if (mapper.selectPlatformChannel(channelId) == null)
        {
            throw new ServiceException("平台联盟渠道不存在");
        }
        if (mapper.countPlatformChannelReferences(channelId) > 0)
        {
            throw new ServiceException("平台联盟渠道仍被商户套餐或订单使用，不能删除");
        }
        return mapper.softDeletePlatformChannel(channelId, operatorId());
    }

    /** 软删除商户自定义渠道，仍被套餐或订单使用时拒绝删除。 */
    @Transactional(rollbackFor = Exception.class)
    public int deleteChannel(Long channelId)
    {
        MerchantContextUtils.requireMerchantApproved();
        Scope scope = merchantScope();
        if (mapper.selectChannel(scope.tenantId, scope.memberId, channelId, true) == null)
        {
            throw new ServiceException("只能删除当前商户自定义渠道");
        }
        if (mapper.countChannelReferences(scope.tenantId, scope.memberId, channelId) > 0)
        {
            throw new ServiceException("订单渠道仍被套餐使用，不能删除");
        }
        Map<String, Object> existing = mapper.selectChannel(scope.tenantId, scope.memberId, channelId, true);
        int rows = mapper.softDeleteChannel(scope.tenantId, scope.memberId, channelId, operatorId());
        audit("订单渠道", "DELETE", "ORDER_CHANNEL", channelId, stringValue(existing, "channelName"),
                "删除订单渠道", existing, null);
        return rows;
    }

    /** 查询当前商户套餐的渠道绑定。 */
    public List<OrderChannelVO> listPackageChannels(Long packageId)
    {
        Scope scope = merchantScope();
        requirePackage(scope, packageId);
        return mapChannels(mapper.selectPackageChannels(scope.tenantId, scope.memberId, packageId));
    }

    /** 将套餐默认渠道转移到目标渠道，并记录转移历史。 */
    @Transactional(rollbackFor = Exception.class)
    public OrderChannelVO transferPackageChannel(OrderChannelCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        Scope scope = merchantScope();
        Long packageId = required(command == null ? null : command.packageId, "套餐唯一编号不能为空");
        Long targetId = required(command == null ? null : command.toChannelId, "目标渠道唯一编号不能为空");
        String key = required(command == null ? null : command.idempotencyKey, "幂等键不能为空");
        requirePackage(scope, packageId);
        Map<String, Object> target = requireChannel(scope, targetId);
        if (!RentalCode.EnableStatus.ENABLED.getCode().equals(value(target, "status", -1)))
        {
            throw new ServiceException("停用渠道不能作为套餐目标渠道");
        }
        Map<String, Object> previous = mapper.selectPackageTransferByKey(scope.memberId, key);
        if (previous != null)
        {
            return findChannelById(scope.tenantId, scope.memberId, longValue(previous, "toChannelId"), false);
        }
        Map<String, Object> current = mapper.selectDefaultPackageChannel(scope.tenantId, scope.memberId, packageId);
        Long fromChannelId = current == null ? null : longValue(current, "channelId");
        if (fromChannelId != null && fromChannelId.equals(targetId))
        {
            mapper.insertPackageTransfer(scope.tenantId, scope.memberId, packageId, fromChannelId, targetId, key,
                    trim(command.reason), operatorId(), operatorName());
            OrderChannelVO unchanged = findChannelById(scope.tenantId, scope.memberId, targetId, false);
            audit("套餐渠道", "UPDATE", "PACKAGE_CHANNEL", packageId, unchanged.channelName, "转移套餐订单渠道", current, unchanged);
            return unchanged;
        }
        mapper.clearPackageDefault(scope.tenantId, scope.memberId, packageId, operatorId());
        mapper.insertPackageChannel(scope.tenantId, scope.memberId, packageId, targetId, 1, operatorId());
        try
        {
            mapper.insertPackageTransfer(scope.tenantId, scope.memberId, packageId, fromChannelId, targetId, key,
                    trim(command.reason), operatorId(), operatorName());
        }
        catch (DuplicateKeyException exception)
        {
            return findChannelById(scope.tenantId, scope.memberId, targetId, false);
        }
        OrderChannelVO updated = findChannelById(scope.tenantId, scope.memberId, targetId, false);
        audit("套餐渠道", "UPDATE", "PACKAGE_CHANNEL", packageId, updated.channelName, "转移套餐订单渠道", current, updated);
        return updated;
    }

    /** 批量将订单转移到目标渠道，单个订单以版本号锁定并分别记录历史。 */
    @Transactional(rollbackFor = Exception.class)
    public int transferOrders(OrderChannelCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        Scope scope = merchantScope();
        Long targetId = required(command == null ? null : command.toChannelId, "目标渠道唯一编号不能为空");
        String key = required(command == null ? null : command.idempotencyKey, "幂等键不能为空");
        Map<String, Object> target = requireChannel(scope, targetId);
        if (!RentalCode.EnableStatus.ENABLED.getCode().equals(value(target, "status", -1)))
        {
            throw new ServiceException("停用渠道不能作为订单目标渠道");
        }
        if (command.orderIds == null || command.orderIds.isEmpty())
        {
            throw new ServiceException("至少选择一个订单");
        }
        int changed = 0;
        for (Long orderId : command.orderIds)
        {
            Map<String, Object> order = mapper.selectOrder(scope.tenantId, scope.memberId, required(orderId, "订单唯一编号不能为空"));
            if (order == null)
            {
                throw new ServiceException("订单不存在或已删除");
            }
            if (mapper.selectOrderTransferByKey(scope.memberId, orderId, key) != null)
            {
                continue;
            }
            Long oldChannelId = order.get("channelId") == null ? null : longValue(order, "channelId");
            if (targetId.equals(oldChannelId))
            {
                continue;
            }
            int rows = mapper.updateOrderChannel(scope.tenantId, scope.memberId, orderId, targetId,
                    value(order, "version", 0), operatorId());
            if (rows != 1)
            {
                throw new ServiceException("订单已被其他操作修改，请刷新后重试");
            }
            try
            {
                mapper.insertOrderTransfer(scope.tenantId, scope.memberId, orderId, oldChannelId,
                        targetId, key, trim(command.reason), operatorId(), operatorName());
            }
            catch (DuplicateKeyException exception)
            {
                continue;
            }
            changed++;
            audit("订单渠道", "UPDATE", "ORDER_CHANNEL", orderId, stringValue(order, "orderNo"),
                    "转移订单渠道", order, target);
        }
        return changed;
    }

    /** 将一批数据库行转换为渠道展示对象。 */
    private List<OrderChannelVO> mapChannels(List<Map<String, Object>> rows)
    {
        if (rows == null || rows.isEmpty())
        {
            return Collections.emptyList();
        }
        List<OrderChannelVO> result = new ArrayList<OrderChannelVO>(rows.size());
        for (Map<String, Object> row : rows)
        {
            OrderChannelVO view = new OrderChannelVO();
            view.channelId = longValue(row, "channelId");
            view.packageId = longValue(row, "packageId");
            view.packageName = stringValue(row, "packageName");
            view.channelCode = stringValue(row, "channelCode");
            view.channelName = stringValue(row, "channelName");
            view.channelType = value(row, "channelType", 0);
            view.scopeType = value(row, "scopeType", 0);
            view.status = value(row, "status", 0);
            view.sortNo = value(row, "sortNo", 0);
            view.isDefault = value(row, "isDefault", RentalCode.YesNo.NO.getCode());
            result.add(view);
        }
        return result;
    }

    /** 根据渠道编码读取渠道展示对象。 */
    private OrderChannelVO findChannel(Long tenantId, Long memberId, String code, boolean ownOnly)
    {
        Map<String, Object> row = mapper.selectVisibleChannels(tenantId, memberId, code, null).stream()
                .filter(item -> code.equals(stringValue(item, "channelCode"))).findFirst().orElse(null);
        if (row == null)
        {
            throw new ServiceException("订单渠道保存失败");
        }
        return mapChannels(Collections.singletonList(row)).get(0);
    }

    /** 按编码读取平台渠道，包含停用状态。 */
    private OrderChannelVO findPlatformChannel(String code)
    {
        Map<String, Object> row = mapper.selectPlatformChannels(code, null).stream()
                .filter(item -> code.equals(stringValue(item, "channelCode"))).findFirst().orElse(null);
        if (row == null)
        {
            throw new ServiceException("订单渠道保存失败");
        }
        return mapChannels(Collections.singletonList(row)).get(0);
    }

    /** 按渠道唯一编号读取渠道展示对象。 */
    private OrderChannelVO findChannelById(Long tenantId, Long memberId, Long channelId, boolean ownOnly)
    {
        return mapChannels(Collections.singletonList(requireChannel(tenantId, memberId, channelId, ownOnly))).get(0);
    }

    /** 按唯一编号读取平台渠道，包含停用状态。 */
    private OrderChannelVO findPlatformChannelById(Long channelId)
    {
        Map<String, Object> row = mapper.selectPlatformChannel(channelId);
        if (row == null)
        {
            throw new ServiceException("平台联盟渠道不存在");
        }
        return mapChannels(Collections.singletonList(row)).get(0);
    }

    /** 校验套餐归属。 */
    private void requirePackage(Scope scope, Long packageId)
    {
        if (mapper.selectPackage(scope.tenantId, scope.memberId, packageId) == null)
        {
            throw new ServiceException("套餐不存在或已删除");
        }
    }

    /** 校验当前商户可使用的渠道。 */
    private Map<String, Object> requireChannel(Scope scope, Long channelId)
    {
        return requireChannel(scope.tenantId, scope.memberId, channelId, false);
    }

    /** 按指定归属规则校验渠道。 */
    private Map<String, Object> requireChannel(Long tenantId, Long memberId, Long channelId, boolean ownOnly)
    {
        Map<String, Object> row = mapper.selectChannel(tenantId, memberId, channelId, ownOnly);
        if (row == null)
        {
            throw new ServiceException("订单渠道不存在、已停用或无权使用");
        }
        return row;
    }

    /** 获取并校验商户登录上下文。 */
    private Scope merchantScope()
    {
        return new Scope(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId());
    }

    /** 严格校验平台客户端。 */
    private void assertPlatformClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("当前接口仅允许平台端访问");
        }
    }

    /** 校验渠道名称。 */
    private void validateName(OrderChannelCommand command)
    {
        if (command == null || StringUtils.isBlank(command.channelName))
        {
            throw new ServiceException("订单渠道名称不能为空");
        }
    }

    /** 将输入状态转换成数据库状态。 */
    private int status(Integer value)
    {
        return RentalCode.EnableStatus.DISABLED.getCode().equals(value)
                ? RentalCode.EnableStatus.DISABLED.getCode() : RentalCode.EnableStatus.ENABLED.getCode();
    }

    /** 将空排序转换为零。 */
    private int number(Integer value)
    {
        return value == null ? 0 : value;
    }

    /**
     * 获取当前商户操作人的用户唯一编号。
     *
     * <p>订单域审计字段只允许关联商户用户表。平台端维护全局渠道时没有商户用户身份，因此返回
     * {@code null}，平台操作人的追踪由平台操作日志负责，禁止把平台用户唯一编号写入商户用户字段。</p>
     */
    private Long operatorId()
    {
        return ClientContextHolder.isPlatformClient() ? null : SecurityUtils.getUserId();
    }

    /**
     * 获取不可变转移记录中的操作人名称快照。
     *
     * <p>名称只用于历史展示，关联和权限判断始终使用 {@link #operatorId()} 返回的商户用户唯一编号。</p>
     */
    private String operatorName()
    {
        return StringUtils.defaultIfEmpty(SecurityUtils.getUsername(), "system");
    }

    /**
     * 写入当前商户订单渠道业务审计，平台端调用不会写入商户审计表。
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
    private void audit(String module, String operationType, String targetType, Long targetId,
            String targetName, String description, Object before, Object after)
    {
        if (auditRecorder != null && MerchantContextUtils.isMerchantUser())
        {
            auditRecorder.record(module, operationType, targetType, String.valueOf(targetId), targetName,
                    description, before, after);
        }
    }

    /** 校验非空字符串。 */
    private String required(String value, String message)
    {
        if (StringUtils.isBlank(value))
        {
            throw new ServiceException(message);
        }
        return value.trim();
    }

    /** 校验非空唯一编号。 */
    private Long required(Long value, String message)
    {
        if (value == null || value <= 0)
        {
            throw new ServiceException(message);
        }
        return value;
    }

    /** 去除可选原因两端空白。 */
    private String trim(String value)
    {
        return value == null ? null : value.trim();
    }

    /** 从数据库 Map 中读取字符串。 */
    private String stringValue(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    /** 从数据库 Map 中读取长整型。 */
    private Long longValue(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? null : ((Number) value).longValue();
    }

    /** 从数据库 Map 中读取整型并提供默认值。 */
    private int value(Map<String, Object> row, String key, int fallback)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? fallback : ((Number) value).intValue();
    }

    /** 商户租赁服务数据范围。 */
    private static final class Scope
    {
        private final Long tenantId;
        private final Long memberId;

        private Scope(Long tenantId, Long memberId)
        {
            this.tenantId = tenantId;
            this.memberId = memberId;
        }
    }
}
