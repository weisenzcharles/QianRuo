package com.ruoyi.system.service.rental;

import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.InviteCodeUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.api.model.MerchantInvitationOverviewVO;
import com.ruoyi.system.domain.rental.MerchantLevel;
import com.ruoyi.system.mapper.rental.MerchantPromotionMapper;

/** 商户等级与邀请推广应用服务。 */
@Service
public class MerchantPromotionService
{
    private static final int CODE_RETRY_LIMIT = 5;
    private final MerchantPromotionMapper mapper;

    /**
     * 创建商户等级与推广服务。
     *
     * @param mapper 推广数据访问接口
     */
    public MerchantPromotionService(MerchantPromotionMapper mapper)
    {
        this.mapper = mapper;
    }

    /** @return 平台配置的全部有效等级 */
    public List<MerchantLevel> listLevels() { return mapper.selectLevelList(); }

    /** @param levelId 等级唯一编号 @return 等级详情 */
    public MerchantLevel getLevel(Long levelId) { return mapper.selectLevelById(levelId); }

    /**
     * 新增商户等级，等级编码只在创建时生成并保持稳定。
     *
     * @param level 待新增等级
     * @param operator 平台操作人
     * @return 影响行数
     */
    public int addLevel(MerchantLevel level, Long operator)
    {
        level.setLevelCode("level_" + System.currentTimeMillis());
        level.setStatus(level.getStatus() == null
                ? RentalCode.EnableStatus.ENABLED.getCode() : level.getStatus());
        level.setCreateBy(operator);
        return mapper.insertLevel(level);
    }

    /**
     * 修改商户等级配置，不允许变更稳定等级编码。
     *
     * @param level 等级配置
     * @param operator 平台操作人
     * @return 影响行数
     */
    public int updateLevel(MerchantLevel level, Long operator)
    {
        if (level.getLevelId() == null || mapper.selectLevelById(level.getLevelId()) == null)
        {
            throw new ServiceException("商户等级不存在");
        }
        level.setUpdateBy(operator);
        return mapper.updateLevel(level);
    }

    /**
     * 软删除未被商户使用的等级。
     *
     * @param levelId 等级唯一编号
     * @param operator 平台操作人
     * @return 影响行数
     */
    public int removeLevel(Long levelId, Long operator)
    {
        if (mapper.countMembersByLevel(levelId) > 0)
        {
            throw new ServiceException("当前等级已被商户使用，不能删除");
        }
        return mapper.softDeleteLevel(levelId, operator);
    }

    /** @return 平台查看的邀请码和邀请记录 */
    public List<Map<String, Object>> listPromotions() { return mapper.selectPromotionList(); }

    /**
     * 查询商户邀请概览；首次访问时为商户生成唯一邀请码。
     *
     * @param tenantId 租户唯一编号
     * @param memberId 商户唯一编号
     * @return 邀请码、等级名额和邀请记录
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantInvitationOverviewVO overview(Long tenantId, Long memberId)
    {
        Map<String, Object> data = mapper.selectInvitationOverview(tenantId, memberId);
        if (data == null)
        {
            throw new ServiceException("商户等级未配置，请联系平台管理员");
        }
        if (data.get("inviteCode") == null)
        {
            createInviteCode(tenantId, memberId);
            data = mapper.selectInvitationOverview(tenantId, memberId);
        }
        MerchantInvitationOverviewVO result = new MerchantInvitationOverviewVO();
        result.setInviteCode(stringValue(data.get("inviteCode")));
        result.setLevelName(stringValue(data.get("levelName")));
        result.setInviteLimit(intValue(data.get("inviteLimit")));
        result.setUsedCount(intValue(data.get("usedCount")));
        result.setRemainingCount(Math.max(0, result.getInviteLimit() - result.getUsedCount()));
        result.setInvitations(mapper.selectInvitations(tenantId, memberId));
        return result;
    }

    /**
     * 为商户生成唯一邀请码，唯一索引冲突时在固定次数内重试。
     *
     * @param tenantId 租户唯一编号
     * @param memberId 商户唯一编号
     */
    private void createInviteCode(Long tenantId, Long memberId)
    {
        for (int attempt = 0; attempt < CODE_RETRY_LIMIT; attempt++)
        {
            try
            {
                // 邀请码由首次查询按需生成，没有可归属的人工操作人，审计用户编号保持为空。
                mapper.insertInviteCode(tenantId, memberId, InviteCodeUtils.generate(), null);
                return;
            }
            catch (DuplicateKeyException exception)
            {
                if (mapper.selectInvitationOverview(tenantId, memberId).get("inviteCode") != null)
                {
                    return;
                }
            }
        }
        throw new ServiceException("邀请码生成失败，请稍后重试");
    }

    /** @param value 数据库数值 @return 非空整数 */
    private int intValue(Object value) { return value == null ? 0 : ((Number) value).intValue(); }

    /** @param value 数据库值 @return 字符串 */
    private String stringValue(Object value) { return value == null ? "" : String.valueOf(value); }
}
