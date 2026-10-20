package com.ruoyi.merchant.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.mapper.MerchantInvoiceApplicationMapper;
import com.ruoyi.merchant.model.MerchantFinanceDirection;
import com.ruoyi.merchant.model.MerchantInvoiceApplicationCreateCommand;
import com.ruoyi.merchant.model.MerchantInvoiceApplicationStatus;
import com.ruoyi.merchant.model.MerchantInvoiceApplicationType;
import com.ruoyi.merchant.model.MerchantInvoiceApplicationVO;

/** 商户开票申请服务。申请只绑定成功收入流水，平台审核后追加发票或红冲记录。 */
@Service
public class MerchantInvoiceApplicationService
{
    private static final DateTimeFormatter APPLICATION_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final MerchantInvoiceApplicationMapper mapper;

    public MerchantInvoiceApplicationService(MerchantInvoiceApplicationMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 查询当前商户的开票申请。 */
    public List<MerchantInvoiceApplicationVO> list(Integer status, Integer applicationType)
    {
        MerchantInvoiceApplicationStatus validStatus = MerchantInvoiceApplicationStatus.fromCode(status);
        if (status != null && validStatus == null) throw new ServiceException("不支持的发票申请状态");
        MerchantInvoiceApplicationType validType = MerchantInvoiceApplicationType.fromCode(applicationType);
        if (applicationType != null && validType == null) throw new ServiceException("不支持的发票申请类型");
        List<MerchantInvoiceApplicationVO> rows = mapper.selectList(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), status, applicationType);
        return rows == null ? Collections.emptyList() : rows;
    }

    /** 查询单笔开票申请。 */
    public MerchantInvoiceApplicationVO get(String applicationNo)
    {
        if (applicationNo == null || applicationNo.isBlank()) return null;
        return mapper.selectByNo(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                applicationNo.trim());
    }

    /** 提交开票或补票申请，金额和流水归属以服务端锁定读取的流水为准。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantInvoiceApplicationVO create(MerchantInvoiceApplicationCreateCommand command)
    {
        validate(command);
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        String idempotencyKey = command.getIdempotencyKey() == null || command.getIdempotencyKey().isBlank()
                ? UUID.randomUUID().toString() : command.getIdempotencyKey().trim();
        if (idempotencyKey.length() > 128) throw new ServiceException("幂等请求标识不能超过128个字符");

        MapValue ledger = new MapValue(mapper.selectLedgerForUpdate(tenantId, memberId, command.getLedgerNo().trim()));
        if (ledger.value == null) throw new ServiceException("财务流水不存在或无权申请开票");
        if (!Integer.valueOf(1).equals(ledger.integer("ledgerStatus"))
                || !Integer.valueOf(MerchantFinanceDirection.INCOME.getCode()).equals(ledger.integer("direction")))
            throw new ServiceException("只有成功收入流水可以申请开票");
        BigDecimal amount = ledger.decimal("amount");
        if (amount.signum() <= 0) throw new ServiceException("流水金额必须大于零");
        if (command.getAmount() != null && amount.compareTo(command.getAmount()) != 0)
            throw new ServiceException("申请金额与财务流水不一致");
        if (mapper.selectValidInvoice(tenantId, memberId, ledger.longValue("ledgerId"), ledger.string("refType"),
                ledger.longValue("refId")) != null)
            throw new ServiceException("该业务已经存在有效发票，不能重复开票");
        if (mapper.countPendingApplication(tenantId, memberId, ledger.longValue("ledgerId"),
                command.getApplicationType()) > 0)
            throw new ServiceException("该流水已有待处理的开票申请");

        String applicationNo = "INV-" + LocalDateTime.now().format(APPLICATION_TIME) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        try
        {
            mapper.insertApplication(tenantId, memberId, ledger.longValue("ledgerId"), applicationNo, idempotencyKey,
                    command, amount, ledger.string("refType"), ledger.longValue("refId"), SecurityUtils.getUserId());
        }
        catch (DuplicateKeyException exception)
        {
            MerchantInvoiceApplicationVO existing = mapper.selectByIdempotencyKey(tenantId, memberId, idempotencyKey);
            if (existing != null) return existing;
            throw new ServiceException("该开票申请已提交，请刷新后重试");
        }
        MerchantInvoiceApplicationVO result = mapper.selectByNo(tenantId, memberId, applicationNo);
        if (result == null) throw new ServiceException("开票申请保存失败");
        return result;
    }

    private void validate(MerchantInvoiceApplicationCreateCommand command)
    {
        if (command == null || command.getLedgerNo() == null || command.getLedgerNo().isBlank())
            throw new ServiceException("请选择需要开票的流水");
        MerchantInvoiceApplicationType type = MerchantInvoiceApplicationType.fromCode(command.getApplicationType());
        if (type == null || type == MerchantInvoiceApplicationType.RED_FLUSH)
            throw new ServiceException("商户端只能提交开票或补票申请");
        if (command.getInvoiceType() == null || (command.getInvoiceType() != 1 && command.getInvoiceType() != 2))
            throw new ServiceException("发票类型不合法");
        if (command.getInvoiceTitle() == null || command.getInvoiceTitle().trim().isEmpty()
                || command.getInvoiceTitle().trim().length() > 256)
            throw new ServiceException("请填写有效的发票抬头");
        if (command.getInvoiceType() == 1 && (command.getTaxNo() == null || command.getTaxNo().trim().isEmpty()))
            throw new ServiceException("企业发票必须填写纳税人识别号");
        if (command.getInvoiceEmail() != null && command.getInvoiceEmail().length() > 128)
            throw new ServiceException("发票邮箱不能超过128个字符");
    }

    /** 将 MyBatis 动态结果集中转换为可读值，避免业务代码散落强制类型转换。 */
    private static final class MapValue
    {
        private final java.util.Map<String, Object> value;

        private MapValue(java.util.Map<String, Object> value) { this.value = value; }
        private Integer integer(String key) { return value.get(key) == null ? null : ((Number) value.get(key)).intValue(); }
        private Long longValue(String key) { return value.get(key) == null ? null : ((Number) value.get(key)).longValue(); }
        private BigDecimal decimal(String key) { return value.get(key) == null ? BigDecimal.ZERO : new BigDecimal(value.get(key).toString()); }
        private String string(String key) { return value.get(key) == null ? null : value.get(key).toString(); }
    }
}
