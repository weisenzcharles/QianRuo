package com.ruoyi.merchant.application;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.api.model.MerchantAllianceOption;
import com.ruoyi.merchant.mapper.MerchantAllianceMapper;

/** 商户租商联盟关系管理。联盟关系和成员配置只在商户服务的 ry_rental 中维护。 */
@Service
public class MerchantAllianceService
{
    private final MerchantAllianceMapper mapper;

    public MerchantAllianceService(MerchantAllianceMapper mapper)
    {
        this.mapper = mapper;
    }

    public Map<String, Object> current()
    {
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("memberId", memberId);
        Map<String, Object> alliance = mapper.selectCurrent(tenantId, memberId);
        result.put("alliance", alliance);
        result.put("members", alliance == null ? Collections.emptyList() : mapper.selectMembers(tenantId, memberId));
        return result;
    }

    /** 返回可加入当前联盟的商户名称选项，内部 memberId 仅用于提交关联。 */
    public List<MerchantAllianceOption> memberOptions()
    {
        return mapper.selectMemberOptions(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> create(String name)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (name == null || name.trim().isEmpty() || name.trim().length() > 128)
            throw new ServiceException("联盟名称不能为空且不能超过128个字符");
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        if (mapper.selectCurrent(tenantId, memberId) != null)
            throw new ServiceException("当前商户已经加入租商联盟");
        mapper.insertAlliance(tenantId, memberId, name.trim(), SecurityUtils.getUserId());
        Long allianceId = mapper.selectLatestAllianceId(tenantId, memberId);
        mapper.insertMember(tenantId, allianceId, memberId, SecurityUtils.getUserId());
        return current();
    }

    @Transactional(rollbackFor = Exception.class)
    public void addMember(Long memberId)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (memberId == null || memberId.equals(MerchantContextUtils.getMemberId()))
            throw new ServiceException("请选择其他有效商户");
        Long tenantId = MerchantContextUtils.getTenantId();
        Map<String, Object> alliance = mapper.selectCurrent(tenantId, MerchantContextUtils.getMemberId());
        if (alliance == null || !MerchantContextUtils.getMemberId().equals(((Number) alliance.get("ownerMemberId")).longValue()))
            throw new ServiceException("只有联盟发起商户可以添加成员");
        try
        {
            if (mapper.insertMember(tenantId, ((Number) alliance.get("allianceId")).longValue(), memberId,
                    SecurityUtils.getUserId()) != 1)
                throw new ServiceException("商户不存在或未通过审核");
        }
        catch (DuplicateKeyException exception)
        {
            throw new ServiceException("该商户已经在联盟中");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMember(Long memberId, Integer shareStockEnabled, Integer acceptOrderLimit)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (acceptOrderLimit != null && (acceptOrderLimit < 0 || acceptOrderLimit > 100000))
            throw new ServiceException("承接订单上限必须在0到100000之间");
        Map<String, Object> alliance = mapper.selectCurrent(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId());
        if (alliance == null) throw new ServiceException("当前商户未加入租商联盟");
        int changed = mapper.updateMember(MerchantContextUtils.getTenantId(),
                ((Number) alliance.get("allianceId")).longValue(), memberId, shareStockEnabled, acceptOrderLimit,
                MerchantContextUtils.getMemberId());
        if (changed != 1) throw new ServiceException("无权修改该联盟成员");
    }

    /** 原子预占联盟接单名额，避免并发接受订单突破成员上限。 */
    @Transactional(rollbackFor = Exception.class)
    public void reserveOrderSlot(Long tenantId, Long allianceId, Long memberId, Long allianceOrderId, Long operatorId)
    {
        if (tenantId == null || allianceId == null || memberId == null || allianceOrderId == null)
            throw new ServiceException("联盟接单参数不完整");
        int inserted = mapper.insertOrderSlot(tenantId, allianceId, memberId, allianceOrderId, operatorId);
        if (inserted == 0 && !mapper.existsActiveOrderSlot(tenantId, memberId, allianceOrderId))
            throw new ServiceException("联盟接单名额预占重复或已失效");
        if (inserted == 1 && mapper.incrementOrderSlot(tenantId, allianceId, memberId) != 1)
            throw new ServiceException("当前商户已达到接单上限");
    }

    /** 释放已结束协助订单的接单名额，重复调用不会重复扣减。 */
    @Transactional(rollbackFor = Exception.class)
    public void releaseOrderSlot(Long tenantId, Long memberId, Long allianceOrderId, Long operatorId)
    {
        if (mapper.releaseOrderSlot(tenantId, memberId, allianceOrderId, operatorId) == 1
                && mapper.decrementOrderSlot(tenantId, memberId) != 1)
            throw new ServiceException("联盟接单名额释放失败");
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long memberId)
    {
        MerchantContextUtils.requireMerchantApproved();
        Map<String, Object> alliance = mapper.selectCurrent(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId());
        if (alliance == null) throw new ServiceException("当前商户未加入租商联盟");
        if (mapper.removeMember(MerchantContextUtils.getTenantId(), ((Number) alliance.get("allianceId")).longValue(),
                memberId, MerchantContextUtils.getMemberId()) != 1)
            throw new ServiceException("无权移除该联盟成员");
    }

    public List<MerchantAllianceOption> sharedMembers(Long tenantId, Long memberId)
    {
        return mapper.selectSharedMembers(tenantId, memberId);
    }
}
