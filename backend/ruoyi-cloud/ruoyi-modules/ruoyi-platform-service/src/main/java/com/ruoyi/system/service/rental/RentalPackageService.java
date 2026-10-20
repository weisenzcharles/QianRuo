package com.ruoyi.system.service.rental;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.domain.rental.RentalPackage;
import com.ruoyi.system.domain.rental.RentalPackageItem;
import com.ruoyi.system.mapper.rental.RentalPackageMapper;
import com.ruoyi.system.model.rental.PlatformPackageCommand;
import com.ruoyi.system.model.rental.PlatformPackageQuery;
import com.ruoyi.system.api.model.PlatformPackage;
import com.ruoyi.system.service.SysCodeRuleService;

/** 平台官方套餐业务服务。 */
@Service
public class RentalPackageService
{
    private final RentalPackageMapper packageMapper;
    private final RentalProductClassService productClassService;
    private final RentalBrandModelService brandModelService;
    private final SysCodeRuleService codeRuleService;

    public RentalPackageService(RentalPackageMapper packageMapper, RentalProductClassService productClassService,
            RentalBrandModelService brandModelService, SysCodeRuleService codeRuleService)
    {
        this.packageMapper = packageMapper;
        this.productClassService = productClassService;
        this.brandModelService = brandModelService;
        this.codeRuleService = codeRuleService;
    }

    public List<RentalPackage> list(PlatformPackageQuery query) { return packageMapper.selectList(query); }

    /** 返回已启用且存在已发布版本的套餐，供商户服务复制。 */
    public List<PlatformPackage> listActiveForInternal()
    {
        return packageMapper.selectActiveForInternal();
    }

    public RentalPackage get(Long packageId)
    {
        RentalPackage item = packageMapper.selectById(packageId);
        if (item == null) throw new ServiceException("套餐不存在或已删除");
        item.setItems(packageMapper.selectItems(item.getPackageVersionId()));
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public int add(PlatformPackageCommand command, Long operator)
    {
        RentalPackage item = normalize(command);
        validate(item);
        item.setPackageCode(codeRuleService.nextCode("package"));
        packageMapper.insert(item);
        createVersion(item, operator, 1);
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(PlatformPackageCommand command, Long operator)
    {
        RentalPackage item = normalize(command);
        RentalPackage existing = get(item.getPackageId());
        item.setPackageCode(existing.getPackageCode());
        validate(item);
        try
        {
            int affected = packageMapper.update(item);
            createVersion(item, operator, packageMapper.selectNextVersionNo(item.getPackageId()));
            return affected;
        }
        catch (DuplicateKeyException exception) { throw new ServiceException("套餐编码已存在"); }
    }

    @Transactional(rollbackFor = Exception.class)
    public int publish(Long packageId, Long operator)
    {
        get(packageId);
        packageMapper.retireVersions(packageId, operator);
        return packageMapper.activateLatest(packageId, operator);
    }

    @Transactional(rollbackFor = Exception.class)
    public int delete(Long packageId, Long operator)
    {
        get(packageId);
        packageMapper.softDeleteItems(packageId, operator);
        packageMapper.softDeleteVersions(packageId, operator);
        return packageMapper.softDelete(packageId, operator);
    }

    private RentalPackage normalize(PlatformPackageCommand command)
    {
        if (command == null) throw new ServiceException("套餐数据不能为空");
        RentalPackage item = new RentalPackage();
        item.setPackageId(command.getPackageId()); item.setPackageCode(StringUtils.trim(command.getPackageCode()));
        item.setPackageName(StringUtils.trim(command.getPackageName())); item.setClassId(command.getClassId());
        item.setModelId(command.getModelId()); item.setStatus(command.getStatus() == null ? 0 : command.getStatus());
        item.setSortNo(command.getSortNo() == null ? 0 : command.getSortNo());
        item.setRemark(StringUtils.trim(command.getRemark())); item.setItems(command.getItems());
        return item;
    }

    private void validate(RentalPackage item)
    {
        productClassService.get(item.getClassId());
        brandModelService.get(item.getModelId());
        if (packageMapper.countByCode(item.getPackageCode(), item.getPackageId()) > 0) throw new ServiceException("套餐编码已存在");
        if (item.getItems() == null || item.getItems().isEmpty()) throw new ServiceException("套餐至少需要一个明细");
        int mainCount = 0;
        Set<Long> productModelIds = new HashSet<Long>();
        Integer mainRole = RentalCode.PackageComponentRole.MAIN.getCode();
        Integer accessoryRole = RentalCode.PackageComponentRole.ACCESSORY.getCode();
        Integer nonMainProductRole = RentalCode.PackageComponentRole.NON_MAIN_PRODUCT.getCode();
        for (RentalPackageItem detail : item.getItems())
        {
            if (detail == null || detail.getQuantity() == null || detail.getQuantity() <= 0) throw new ServiceException("套餐明细数量必须大于0");
            if (mainRole.equals(detail.getItemRole()))
            {
                mainCount++;
                detail.setModelId(item.getModelId());
                detail.setAccessoryId(null);
            }
            else if (accessoryRole.equals(detail.getItemRole()))
            {
                if (detail.getAccessoryId() == null || detail.getAccessoryId() <= 0) throw new ServiceException("配件唯一编号不合法");
                detail.setModelId(null);
            }
            else if (nonMainProductRole.equals(detail.getItemRole()))
            {
                if (detail.getModelId() == null || detail.getModelId() <= 0) throw new ServiceException("非主产品型号唯一编号不合法");
                if (item.getModelId().equals(detail.getModelId())) throw new ServiceException("非主产品不能与主产品型号相同");
                if (!productModelIds.add(detail.getModelId())) throw new ServiceException("套餐不能重复添加相同的非主产品型号");
                brandModelService.get(detail.getModelId());
                detail.setAccessoryId(null);
            }
            else throw new ServiceException("套餐明细类型不合法");
        }
        if (mainCount != 1) throw new ServiceException("套餐必须且只能包含一个主产品型号明细");
    }

    private void createVersion(RentalPackage item, Long operator, Integer versionNo)
    {
        item.setVersionNo(versionNo == null ? 1 : versionNo); item.setVersionStatus(0); item.setCreateBy(operator);
        item.setBomHash(hash(item.getItems())); packageMapper.insertVersion(item);
        for (RentalPackageItem source : item.getItems()) { source.setPackageVersionId(item.getPackageVersionId()); source.setCreateBy(operator); packageMapper.insertItem(source); }
    }

    private String hash(List<RentalPackageItem> items)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            StringBuilder text = new StringBuilder();
            for (RentalPackageItem item : items) text.append(item.getItemRole()).append(':').append(item.getModelId()).append(':').append(item.getAccessoryId()).append(':').append(item.getQuantity()).append(';');
            byte[] bytes = digest.digest(text.toString().getBytes(StandardCharsets.UTF_8)); StringBuilder result = new StringBuilder();
            for (byte value : bytes) result.append(String.format("%02x", value)); return result.toString();
        }
        catch (NoSuchAlgorithmException exception) { throw new ServiceException("套餐明细摘要生成失败"); }
    }
}
