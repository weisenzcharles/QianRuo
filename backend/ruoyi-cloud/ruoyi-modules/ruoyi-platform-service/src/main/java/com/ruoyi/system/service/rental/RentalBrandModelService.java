package com.ruoyi.system.service.rental;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.api.model.BrandModel;
import com.ruoyi.system.domain.rental.RentalBrandModel;
import com.ruoyi.system.domain.rental.RentalProductClass;
import com.ruoyi.system.mapper.rental.RentalBrandModelMapper;

/** 平台标准品牌型号业务服务。 */
@Service
public class RentalBrandModelService
{
    private final RentalBrandModelMapper brandModelMapper;

    private final RentalProductClassService productClassService;

    /**
     * 注入标准品牌型号数据访问组件和产品类目校验服务。
     *
     * @param brandModelMapper 标准品牌型号数据访问组件
     * @param productClassService 产品类目业务服务
     */
    public RentalBrandModelService(RentalBrandModelMapper brandModelMapper,
            RentalProductClassService productClassService)
    {
        this.brandModelMapper = brandModelMapper;
        this.productClassService = productClassService;
    }

    /**
     * 查询平台标准品牌型号列表，过滤条件全部由平台管理端使用。
     *
     * @param query 品牌、型号、类目和状态查询条件
     * @return 标准品牌型号列表
     */
    public List<RentalBrandModel> list(RentalBrandModel query)
    {
        return brandModelMapper.selectList(query);
    }

    /**
     * 查询指定标准型号详情，不存在或已软删除时返回业务异常。
     *
     * @param modelId 标准型号主键
     * @return 标准型号详情
     */
    public RentalBrandModel get(Long modelId)
    {
        return requireModel(modelId);
    }

    /**
     * 新增标准品牌型号，服务端校验所属类目、文本、状态和业务唯一性。
     *
     * @param model 待新增标准型号
     * @param operator 当前平台操作人员
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int add(RentalBrandModel model, String operator)
    {
        normalize(model);
        assertActiveClass(model.getClassId());
        validateUnique(model);
        model.setCreateBy(operator);
        return brandModelMapper.insert(model);
    }

    /**
     * 修改标准品牌型号，禁止修改到停用或不存在的产品类目。
     *
     * @param model 待修改标准型号
     * @param operator 当前平台操作人员
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int update(RentalBrandModel model, String operator)
    {
        requireModel(model == null ? null : model.getModelId());
        normalize(model);
        assertActiveClass(model.getClassId());
        validateUnique(model);
        model.setUpdateBy(operator);
        return brandModelMapper.update(model);
    }

    /**
     * 软删除标准型号；已经被商品或商户常用型号引用的记录改为停用，保留历史关联完整性。
     *
     * @param modelId 标准型号主键
     * @param operator 当前平台操作人员
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long modelId, String operator)
    {
        requireModel(modelId);
        if (brandModelMapper.countReferences(modelId) > 0)
        {
            throw new ServiceException("标准型号已被业务数据引用，请先停用后再处理");
        }
        return brandModelMapper.softDelete(modelId, operator);
    }

    /**
     * 向商户服务发布平台当前正常的标准品牌型号，转换为跨服务传输对象并隐藏审计字段。
     *
     * @return 平台正常标准品牌型号集合
     */
    public List<BrandModel> listActiveForInternal()
    {
        List<RentalBrandModel> source = brandModelMapper.selectActiveList();
        if (source == null || source.isEmpty())
        {
            return Collections.emptyList();
        }
        List<BrandModel> result = new ArrayList<BrandModel>(source.size());
        for (RentalBrandModel item : source)
        {
            BrandModel target = new BrandModel();
            target.setModelId(item.getModelId());
            target.setClassId(item.getClassId());
            target.setClassName(item.getClassName());
            target.setBrand(item.getBrand());
            target.setModelName(item.getModelName());
            target.setMemorySpec(item.getMemorySpec());
            target.setOfficialFlag(item.getOfficialFlag());
            target.setStatus(item.getStatus());
            result.add(target);
        }
        return result;
    }

    /**
     * 规范品牌、型号和规格文本，并设置稳定的默认状态。
     *
     * @param model 待规范标准型号
     */
    private void normalize(RentalBrandModel model)
    {
        if (model == null)
        {
            throw new ServiceException("品牌型号数据不能为空");
        }
        model.setBrand(StringUtils.trim(model.getBrand()));
        model.setModelName(StringUtils.trim(model.getModelName()));
        model.setMemorySpec(StringUtils.trim(model.getMemorySpec()));
        model.setOfficialFlag(StringUtils.isBlank(model.getOfficialFlag()) ? "1" : model.getOfficialFlag());
        model.setStatus(StringUtils.isBlank(model.getStatus()) ? "0" : model.getStatus());
        if (StringUtils.isBlank(model.getBrand()) || StringUtils.isBlank(model.getModelName()))
        {
            throw new ServiceException("品牌和型号不能为空");
        }
        if (!"0".equals(model.getStatus()) && !"1".equals(model.getStatus()))
        {
            throw new ServiceException("型号状态不合法");
        }
    }

    /**
     * 校验产品类目存在且处于正常状态，防止标准型号挂到不可经营的类目。
     *
     * @param classId 产品类目主键
     */
    private void assertActiveClass(Long classId)
    {
        RentalProductClass productClass = productClassService.get(classId);
        if (!"0".equals(productClass.getStatus()))
        {
            throw new ServiceException("产品类目已停用，不能维护标准型号");
        }
    }

    /**
     * 校验同一类目下品牌、型号和规格组合唯一，避免商户选择时出现重复标准项。
     *
     * @param model 待校验标准型号
     */
    private void validateUnique(RentalBrandModel model)
    {
        if (brandModelMapper.countByIdentity(model.getClassId(), model.getBrand(), model.getModelName(),
                model.getMemorySpec(), model.getModelId()) > 0)
        {
            throw new ServiceException("同一产品类目下的品牌、型号和规格已存在");
        }
    }

    /**
     * 查询必须存在且未软删除的标准型号。
     *
     * @param modelId 标准型号主键
     * @return 标准型号实体
     */
    private RentalBrandModel requireModel(Long modelId)
    {
        if (modelId == null)
        {
            throw new ServiceException("型号ID不能为空");
        }
        RentalBrandModel model = brandModelMapper.selectById(modelId);
        if (model == null)
        {
            throw new ServiceException("标准型号不存在或已删除");
        }
        return model;
    }
}
