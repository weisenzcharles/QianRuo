package com.ruoyi.merchant.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.enums.FileBusinessType;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.file.FileTypeUtils;
import com.ruoyi.common.core.utils.file.MimeTypeUtils;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.common.security.auth.AuthUtil;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.RemoteMerchantFileService;
import com.ruoyi.merchant.api.model.MerchantFileInfo;
import com.ruoyi.merchant.domain.MerchantStoreSettingRecord;
import com.ruoyi.merchant.mapper.MerchantStoreSettingsMapper;
import com.ruoyi.merchant.model.MerchantDeliveryMethod;
import com.ruoyi.merchant.model.MerchantInventoryAlertType;
import com.ruoyi.merchant.model.MerchantOrderViewMode;
import com.ruoyi.merchant.model.MerchantShipTimeSlot;
import com.ruoyi.merchant.model.MerchantStoreProfileUpdateCommand;
import com.ruoyi.merchant.model.MerchantStoreProfileVO;
import com.ruoyi.merchant.model.MerchantStoreSettingsPageVO;
import com.ruoyi.merchant.model.MerchantStoreSettingsUpdateCommand;
import com.ruoyi.merchant.model.MerchantStoreSettingsVO;

/** 商户店铺设置应用服务。基础资料和偏好设置分开保存，避免把展示配置混入订单或库存表。 */
@Service
public class MerchantStoreSettingsService
{
    private static final Logger log = LoggerFactory.getLogger(MerchantStoreSettingsService.class);
    private static final String SETTINGS_KEY = "store_preferences";
    private static final String CACHE_KEY_PREFIX = "merchant:store:settings:";
    private static final long CACHE_TTL_DAYS = 7L;
    private static final Set<Integer> DISPLAY_DAY_OPTIONS = new LinkedHashSet<Integer>(Arrays.asList(3, 5, 7, 15, 30));
    private static final Set<String> ORDER_COPY_FIELDS = new LinkedHashSet<String>(Arrays.asList(
            "customer", "recipient", "package", "lease", "warehouse", "amount", "remark"));

    @Autowired
    private MerchantStoreSettingsMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RemoteMerchantFileService remoteFileService;

    @Autowired
    private MerchantBusinessAuditRecorder auditRecorder;

    @Autowired
    private MerchantVipLevelService merchantVipLevelService;

    /** 查询当前商户店铺设置页面所需的最小聚合数据。 */
    public MerchantStoreSettingsPageVO get()
    {
        MerchantStoreProfileVO profile = requireProfile();
        MerchantStoreSettingsPageVO page = new MerchantStoreSettingsPageVO();
        page.setProfile(profile);
        page.setSettings(loadSettings());
        page.setCanEdit(isAdministrator());
        return page;
    }

    /** 修改商户可维护的店铺基础资料。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantStoreProfileVO updateProfile(MerchantStoreProfileUpdateCommand command)
    {
        requireAdministrator();
        MerchantContextUtils.requireMerchantApproved();
        validateProfile(command);
        MerchantStoreProfileVO before = requireProfile();
        int rows = mapper.updateStoreProfile(tenantId(), memberId(), command.getNickname().trim(),
                trimToNull(command.getContactName()), trimToNull(command.getServicePhone()),
                trimToNull(command.getContactEmail()), trimToNull(command.getAlipayRealName()),
                trimToNull(command.getAlipayAccount()), trimToNull(command.getStoreDescription()),
                Boolean.TRUE.equals(command.getExternalAcceptEnabled()) ? 1 : 0, SecurityUtils.getUserId());
        if (rows <= 0)
        {
            throw new ServiceException("店铺资料不存在或已删除");
        }
        MerchantStoreProfileVO after = requireProfile();
        auditRecorder.record("店铺设置", "UPDATE", "MERCHANT_STORE", String.valueOf(memberId()),
                after.getNickname(), "修改商户基础资料", before, after);
        return after;
    }

    /** 修改当前商户的订单、库存、配送和展示偏好。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantStoreSettingsVO updateSettings(MerchantStoreSettingsUpdateCommand command)
    {
        requireAdministrator();
        MerchantContextUtils.requireMerchantApproved();
        MerchantStoreSettingsVO before = loadSettings();
        MerchantStoreSettingsVO after = normalize(command, before);
        String json = writeJson(after);
        mapper.upsertSetting(tenantId(), memberId(), SETTINGS_KEY, json, SecurityUtils.getUserId());
        redisService.setCacheObject(cacheKey(), after, CACHE_TTL_DAYS, TimeUnit.DAYS);
        auditRecorder.record("店铺设置", "UPDATE", "MERCHANT_STORE", String.valueOf(memberId()),
                requireProfile().getNickname(), "修改商户经营偏好", before, after);
        return after;
    }

    /** 上传并保存店铺 Logo。文件地址由文件服务生成，前端不能直接伪造保存地址。 */
    @Transactional(rollbackFor = Exception.class)
    public String uploadLogo(MultipartFile file)
    {
        requireAdministrator();
        MerchantContextUtils.requireMerchantApproved();
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("请选择店铺 Logo");
        }
        if (file.getSize() > 5L * 1024L * 1024L)
        {
            throw new ServiceException("店铺 Logo 不能超过5MB");
        }
        String extension = FileTypeUtils.getExtension(file);
        if (!StringUtils.equalsAnyIgnoreCase(extension, MimeTypeUtils.IMAGE_EXTENSION))
        {
            throw new ServiceException("店铺 Logo 仅支持图片格式");
        }
        MerchantStoreProfileVO before = requireProfile();
        R<MerchantFileInfo> result = remoteFileService.upload(file, FileBusinessType.MERCHANT.getCode());
        if (result == null || result.getData() == null || StringUtils.isEmpty(result.getData().getUrl()))
        {
            throw new ServiceException("文件服务异常，请稍后重试");
        }
        String url = result.getData().getUrl();
        if (mapper.updateStoreLogo(tenantId(), memberId(), url, SecurityUtils.getUserId()) <= 0)
        {
            throw new ServiceException("保存店铺 Logo 失败");
        }
        deleteOldFile(before.getStoreLogoUrl(), url);
        auditRecorder.record("店铺设置", "UPDATE", "MERCHANT_STORE", String.valueOf(memberId()),
                before.getNickname(), "修改店铺 Logo", before.getStoreLogoUrl(), url);
        return url;
    }

    /** 删除店铺 Logo，同时清理商户主体上的地址。 */
    @Transactional(rollbackFor = Exception.class)
    public void removeLogo()
    {
        requireAdministrator();
        MerchantContextUtils.requireMerchantApproved();
        MerchantStoreProfileVO before = requireProfile();
        if (StringUtils.isEmpty(before.getStoreLogoUrl()))
        {
            return;
        }
        if (mapper.updateStoreLogo(tenantId(), memberId(), null, SecurityUtils.getUserId()) <= 0)
        {
            throw new ServiceException("删除店铺 Logo 失败");
        }
        deleteOldFile(before.getStoreLogoUrl(), null);
    }

    private MerchantStoreProfileVO requireProfile()
    {
        MerchantStoreProfileVO profile = mapper.selectStoreProfile(tenantId(), memberId());
        if (profile == null)
        {
            throw new ServiceException("商户不存在或已删除");
        }
        profile.setVipLevelName(merchantVipLevelService.resolve(profile.getVipLevel(), profile.getVipLevelName()));
        return profile;
    }

    private MerchantStoreSettingsVO loadSettings()
    {
        MerchantStoreSettingsVO cached = redisService.getCacheObject(cacheKey());
        if (cached != null)
        {
            return normalize(cached);
        }
        MerchantStoreSettingRecord record = mapper.selectSetting(tenantId(), memberId(), SETTINGS_KEY);
        MerchantStoreSettingsVO settings = MerchantStoreSettingsVO.defaults();
        if (record != null && StringUtils.isNotEmpty(record.getSettingValueJson()))
        {
            try
            {
                settings = normalize(objectMapper.readValue(record.getSettingValueJson(), MerchantStoreSettingsVO.class));
            }
            catch (JsonProcessingException e)
            {
                log.warn("商户店铺设置 JSON 解析失败，使用默认设置，tenantId={}, memberId={}", tenantId(), memberId(), e);
            }
        }
        redisService.setCacheObject(cacheKey(), settings, CACHE_TTL_DAYS, TimeUnit.DAYS);
        return settings;
    }

    private MerchantStoreSettingsVO normalize(MerchantStoreSettingsVO settings)
    {
        MerchantStoreSettingsVO normalized = settings == null ? MerchantStoreSettingsVO.defaults() : settings;
        if (!isSupportedDisplayDays(normalized.getInboundOutboundTrendDays())) normalized.setInboundOutboundTrendDays(30);
        if (!isSupportedDisplayDays(normalized.getInventoryOverviewDays())) normalized.setInventoryOverviewDays(30);
        if (normalized.getInventoryAlertType() == null) normalized.setInventoryAlertType(MerchantInventoryAlertType.COUNT);
        if (normalized.getInventoryAlertValue() == null) normalized.setInventoryAlertValue(2);
        if (normalized.getProtectedQuantity() == null) normalized.setProtectedQuantity(0);
        if (normalized.getOrderCopyFields() == null) normalized.setOrderCopyFields(new ArrayList<String>());
        if (normalized.getOrderViewMode() == null) normalized.setOrderViewMode(MerchantOrderViewMode.CARD);
        if (normalized.getDefaultDeliveryMethod() == null) normalized.setDefaultDeliveryMethod(MerchantDeliveryMethod.EXPRESS);
        if (normalized.getDefaultShipTimeSlot() == null) normalized.setDefaultShipTimeSlot(MerchantShipTimeSlot.MORNING);
        if (normalized.getContactVisible() == null) normalized.setContactVisible(Boolean.TRUE);
        if (normalized.getDepositFreeEnabled() == null) normalized.setDepositFreeEnabled(Boolean.FALSE);
        if (normalized.getAllianceStoreVisible() == null) normalized.setAllianceStoreVisible(Boolean.TRUE);
        if (normalized.getVideoAutoDeleteEnabled() == null) normalized.setVideoAutoDeleteEnabled(Boolean.FALSE);
        if (normalized.getVideoRetentionDays() == null) normalized.setVideoRetentionDays(15);
        return normalized;
    }

    private MerchantStoreSettingsVO normalize(MerchantStoreSettingsUpdateCommand command,
            MerchantStoreSettingsVO current)
    {
        MerchantStoreSettingsVO after = new MerchantStoreSettingsVO();
        after.setInboundOutboundTrendDays(command.getInboundOutboundTrendDays() == null ? current.getInboundOutboundTrendDays() : command.getInboundOutboundTrendDays());
        after.setInventoryOverviewDays(command.getInventoryOverviewDays() == null ? current.getInventoryOverviewDays() : command.getInventoryOverviewDays());
        after.setInventoryAlertType(command.getInventoryAlertType() == null ? current.getInventoryAlertType() : command.getInventoryAlertType());
        after.setInventoryAlertValue(command.getInventoryAlertValue() == null ? current.getInventoryAlertValue() : command.getInventoryAlertValue());
        after.setProtectedQuantity(command.getProtectedQuantity() == null ? current.getProtectedQuantity() : command.getProtectedQuantity());
        after.setOrderCopyFields(command.getOrderCopyFields() == null ? current.getOrderCopyFields() : command.getOrderCopyFields());
        after.setOrderViewMode(command.getOrderViewMode() == null ? current.getOrderViewMode() : command.getOrderViewMode());
        after.setDefaultDeliveryMethod(command.getDefaultDeliveryMethod() == null ? current.getDefaultDeliveryMethod() : command.getDefaultDeliveryMethod());
        after.setDefaultShipTimeSlot(command.getDefaultShipTimeSlot() == null ? current.getDefaultShipTimeSlot() : command.getDefaultShipTimeSlot());
        after.setContactVisible(command.getContactVisible() == null ? current.getContactVisible() : command.getContactVisible());
        after.setDepositFreeEnabled(command.getDepositFreeEnabled() == null ? current.getDepositFreeEnabled() : command.getDepositFreeEnabled());
        after.setAllianceStoreVisible(command.getAllianceStoreVisible() == null ? current.getAllianceStoreVisible() : command.getAllianceStoreVisible());
        after.setVideoAutoDeleteEnabled(command.getVideoAutoDeleteEnabled() == null ? current.getVideoAutoDeleteEnabled() : command.getVideoAutoDeleteEnabled());
        after.setVideoRetentionDays(command.getVideoRetentionDays() == null ? current.getVideoRetentionDays() : command.getVideoRetentionDays());
        after.setOrderCopyFields(new ArrayList<String>(new LinkedHashSet<String>(after.getOrderCopyFields())));
        after.getOrderCopyFields().removeIf(item -> !ORDER_COPY_FIELDS.contains(item));
        if (!isSupportedDisplayDays(after.getInboundOutboundTrendDays()))
        {
            throw new ServiceException("收发趋势默认显示天数仅支持3、5、7、15、30天");
        }
        if (!isSupportedDisplayDays(after.getInventoryOverviewDays()))
        {
            throw new ServiceException("库存概况默认显示天数仅支持3、5、7、15、30天");
        }
        if (after.getProtectedQuantity() < 0 || after.getProtectedQuantity() > 1000000)
        {
            throw new ServiceException("库存保护数量必须在0到1000000之间");
        }
        if (after.getInventoryAlertType() == MerchantInventoryAlertType.PERCENT
                && (after.getInventoryAlertValue() < 0 || after.getInventoryAlertValue() > 100))
        {
            throw new ServiceException("库存提醒百分比必须在0到100之间");
        }
        if (after.getInventoryAlertType() == MerchantInventoryAlertType.COUNT
                && (after.getInventoryAlertValue() < 0 || after.getInventoryAlertValue() > 1000000))
        {
            throw new ServiceException("库存提醒数量必须在0到1000000之间");
        }
        if (after.getVideoRetentionDays() < 1 || after.getVideoRetentionDays() > 3650)
        {
            throw new ServiceException("视频保留天数必须在1到3650之间");
        }
        return after;
    }

    static boolean isSupportedDisplayDays(Integer days)
    {
        return DISPLAY_DAY_OPTIONS.contains(days);
    }

    private String writeJson(MerchantStoreSettingsVO settings)
    {
        try
        {
            return objectMapper.writeValueAsString(settings);
        }
        catch (JsonProcessingException e)
        {
            log.error("商户店铺设置 JSON 序列化失败，tenantId={}, memberId={}", tenantId(), memberId(), e);
            throw new ServiceException("保存商户店铺设置失败");
        }
    }

    private void validateProfile(MerchantStoreProfileUpdateCommand command)
    {
        if (command == null || StringUtils.isBlank(command.getNickname()))
        {
            throw new ServiceException("店铺名称不能为空");
        }
        if (command.getNickname().trim().length() > 64)
        {
            throw new ServiceException("店铺名称不能超过64个字符");
        }
        validateLength(command.getContactName(), 64, "联系人");
        validateLength(command.getServicePhone(), 32, "客服电话");
        validateLength(command.getContactEmail(), 128, "联系邮箱");
        validateLength(command.getAlipayRealName(), 64, "支付宝实名");
        validateLength(command.getAlipayAccount(), 128, "支付宝账号");
        validateLength(command.getStoreDescription(), 1000, "店铺描述");
        if (StringUtils.isNotBlank(command.getContactEmail())
                && !command.getContactEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
        {
            throw new ServiceException("联系邮箱格式不正确");
        }
    }

    private void validateLength(String value, int max, String label)
    {
        if (value != null && value.trim().length() > max)
        {
            throw new ServiceException(label + "不能超过" + max + "个字符");
        }
    }

    private boolean isAdministrator()
    {
        return "owner".equals(MerchantContextUtils.getMemberRole()) || AuthUtil.hasRole("merchant_admin");
    }

    private void requireAdministrator()
    {
        if (!isAdministrator())
        {
            throw new ServiceException("当前操作没有权限");
        }
    }

    private void deleteOldFile(String oldUrl, String newUrl)
    {
        if (StringUtils.isNotEmpty(oldUrl) && !StringUtils.equals(oldUrl, newUrl))
        {
            remoteFileService.delete(oldUrl);
        }
    }

    private String trimToNull(String value)
    {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private Long tenantId()
    {
        return MerchantContextUtils.getTenantId();
    }

    private Long memberId()
    {
        return MerchantContextUtils.getMemberId();
    }

    private String cacheKey()
    {
        return CACHE_KEY_PREFIX + tenantId() + ":" + memberId();
    }
}
