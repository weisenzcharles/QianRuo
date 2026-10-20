package com.ruoyi.system.service.rental;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.domain.rental.RentalAccessory;
import com.ruoyi.system.mapper.rental.RentalAccessoryMapper;
import com.ruoyi.system.model.rental.PlatformAccessoryCommand;
import com.ruoyi.system.model.rental.PlatformAccessoryQuery;
import com.ruoyi.system.api.model.PlatformAccessoryOption;
import com.ruoyi.system.service.SysCodeRuleService;

/** 平台配件主数据业务服务。 */
@Service
public class RentalAccessoryService
{
    private final RentalAccessoryMapper accessoryMapper;
    private final RentalProductClassService productClassService;
    private final RentalBrandService brandService;
    private final SysCodeRuleService codeRuleService;

    public RentalAccessoryService(RentalAccessoryMapper accessoryMapper,
            RentalProductClassService productClassService, RentalBrandService brandService,
            SysCodeRuleService codeRuleService)
    {
        this.accessoryMapper = accessoryMapper;
        this.productClassService = productClassService;
        this.brandService = brandService;
        this.codeRuleService = codeRuleService;
    }

    public List<RentalAccessory> list(PlatformAccessoryQuery query) { return accessoryMapper.selectList(query); }

    public List<PlatformAccessoryOption> listActiveOptions()
    {
        List<PlatformAccessoryOption> result = accessoryMapper.selectActiveOptions();
        for (PlatformAccessoryOption option : result)
            option.setModelIds(accessoryMapper.selectModelIds(option.getAccessoryId()));
        return result;
    }

    public RentalAccessory get(Long accessoryId)
    {
        RentalAccessory item = accessoryMapper.selectById(accessoryId);
        if (item == null) throw new ServiceException("配件不存在或已删除");
        item.setModelIdList(accessoryMapper.selectModelIds(accessoryId));
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public int add(PlatformAccessoryCommand command, Long operator)
    {
        RentalAccessory item = toEntity(command);
        validateReferences(item);
        item.setAccessoryCode(codeRuleService.nextCode("accessory"));
        item.setCreateBy(operator);
        try
        {
            int affected = accessoryMapper.insert(item);
            saveModels(item, operator);
            return affected;
        }
        catch (DuplicateKeyException exception) { throw translateDuplicateKey(exception); }
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(PlatformAccessoryCommand command, Long operator)
    {
        RentalAccessory item = toEntity(command);
        RentalAccessory existing = get(item.getAccessoryId());
        item.setAccessoryCode(existing.getAccessoryCode());
        validateReferences(item);
        item.setUpdateBy(operator);
        try
        {
            int affected = accessoryMapper.update(item);
            accessoryMapper.deleteConflictingDeletedModels(item.getAccessoryId());
            accessoryMapper.deleteModels(item.getAccessoryId(), operator);
            saveModels(item, operator);
            return affected;
        }
        catch (DuplicateKeyException exception) { throw translateDuplicateKey(exception); }
    }

    @Transactional(rollbackFor = Exception.class)
    public int delete(Long accessoryId, Long operator)
    {
        get(accessoryId);
        accessoryMapper.deleteConflictingDeletedModels(accessoryId);
        accessoryMapper.deleteModels(accessoryId, operator);
        return accessoryMapper.softDelete(accessoryId, operator);
    }

    private RentalAccessory toEntity(PlatformAccessoryCommand command)
    {
        if (command == null) throw new ServiceException("配件数据不能为空");
        RentalAccessory item = new RentalAccessory();
        item.setAccessoryId(command.getAccessoryId());
        item.setAccessoryCode(StringUtils.trim(command.getAccessoryCode()));
        item.setAccessoryName(StringUtils.trim(command.getAccessoryName()));
        item.setClassId(command.getClassId()); item.setBrandId(command.getBrandId());
        item.setModelIdList(new ArrayList<Long>(new LinkedHashSet<Long>(command.getModelIdList() == null
                ? new ArrayList<Long>() : command.getModelIdList())));
        item.setSpecification(StringUtils.trim(command.getSpecification()));
        item.setUnit(StringUtils.trim(command.getUnit()));
        item.setDescription(StringUtils.trim(command.getDescription()));
        item.setStatus(command.getStatus() == null ? RentalCode.EnableStatus.ENABLED.getCode() : command.getStatus());
        item.setRemark(StringUtils.trim(command.getRemark()));
        return item;
    }

    private void validateReferences(RentalAccessory item)
    {
        if (!Integer.valueOf(2).equals(productClassService.get(item.getClassId()).getClassLevel()))
        {
            throw new ServiceException("配件所属分类只能选择二级分类");
        }
        if (item.getBrandId() != null) brandService.requireActiveForClass(item.getBrandId(), item.getClassId());
        if (item.getModelIdList().isEmpty()) throw new ServiceException("至少选择一个兼容产品型号");
        for (Long modelId : item.getModelIdList()) if (modelId == null || modelId <= 0) throw new ServiceException("产品型号唯一编号不合法");
    }

    private void saveModels(RentalAccessory item, Long operator)
    {
        for (Long modelId : item.getModelIdList()) accessoryMapper.insertModel(item.getAccessoryId(), modelId, operator);
    }

    /**
     * 仅将配件编码唯一索引冲突转换为编码提示，避免兼容型号关系冲突被误报为编码重复。
     *
     * @param exception 数据库唯一索引异常
     * @return 面向操作人员的业务异常
     */
    private ServiceException translateDuplicateKey(DuplicateKeyException exception)
    {
        String message = exception.getMessage();
        if (message != null && message.contains("uk_rental_accessory_code"))
        {
            return new ServiceException("配件编码已存在");
        }
        if (message != null && message.contains("uk_rental_accessory_model"))
        {
            return new ServiceException("配件兼容型号关系保存失败，请刷新后重试");
        }
        return new ServiceException("配件保存失败，请刷新后重试");
    }
}
