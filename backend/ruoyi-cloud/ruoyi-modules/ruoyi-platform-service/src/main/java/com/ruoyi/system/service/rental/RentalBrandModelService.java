package com.ruoyi.system.service.rental;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.api.model.BrandModel;
import com.ruoyi.system.domain.rental.RentalBrandModel;
import com.ruoyi.system.domain.rental.RentalProductClass;
import com.ruoyi.system.mapper.rental.RentalBrandModelMapper;
import com.ruoyi.system.model.rental.PlatformBrandModelCommand;
import com.ruoyi.system.model.rental.PlatformBrandModelQuery;
import com.ruoyi.system.model.rental.PlatformBrandModelVO;

/** 平台标准品牌型号业务服务。 */
@Service
public class RentalBrandModelService
{
    private final RentalBrandModelMapper brandModelMapper;

    private final RentalProductClassService productClassService;

    private final RentalBusinessGateway rentalBusinessGateway;

    /**
     * 注入标准品牌型号数据访问组件和产品类目校验服务。
     *
     * @param brandModelMapper 标准品牌型号数据访问组件
     * @param productClassService 产品类目业务服务
     * @param rentalBusinessGateway 租赁业务服务应用网关
     */
    public RentalBrandModelService(RentalBrandModelMapper brandModelMapper,
            RentalProductClassService productClassService, RentalBusinessGateway rentalBusinessGateway)
    {
        this.brandModelMapper = brandModelMapper;
        this.productClassService = productClassService;
        this.rentalBusinessGateway = rentalBusinessGateway;
    }

    /**
     * 查询平台标准品牌型号列表，过滤条件全部由平台管理端使用。
     *
     * @param query 品牌、型号、类目和状态查询条件
     * @return 与数据库实体隔离的标准品牌型号展示列表
     */
    public List<PlatformBrandModelVO> list(PlatformBrandModelQuery query)
    {
        PlatformBrandModelQuery normalizedQuery = normalizeQuery(query);
        List<RentalBrandModel> source = brandModelMapper.selectList(normalizedQuery);
        List<PlatformBrandModelVO> result = new ArrayList<PlatformBrandModelVO>(source.size());
        for (RentalBrandModel item : source)
        {
            result.add(toView(item));
        }
        return result;
    }

    /**
     * 查询指定标准型号详情，不存在或已软删除时返回业务异常。
     *
     * @param modelId 标准型号主键
     * @return 与数据库实体隔离的标准型号详情
     */
    public PlatformBrandModelVO get(Long modelId)
    {
        return toView(requireModel(modelId));
    }

    /**
     * 新增标准品牌型号，服务端校验所属类目、文本、状态和业务唯一性。
     *
     * @param command 待新增标准型号命令
     * @param operator 当前平台操作人员
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int add(PlatformBrandModelCommand command, String operator)
    {
        RentalBrandModel model = toEntity(command);
        assertActiveClass(model.getClassId());
        validateUnique(model);
        model.setCreateBy(operator);
        try
        {
            return brandModelMapper.insert(model);
        }
        catch (DuplicateKeyException exception)
        {
            throw duplicateModelException();
        }
    }

    /**
     * 修改标准品牌型号，禁止修改到停用或不存在的产品类目。
     *
     * @param command 待修改标准型号命令
     * @param operator 当前平台操作人员
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int update(PlatformBrandModelCommand command, String operator)
    {
        requireModel(command == null ? null : command.getModelId());
        RentalBrandModel model = toEntity(command);
        assertActiveClass(model.getClassId());
        validateUnique(model);
        model.setUpdateBy(operator);
        try
        {
            int affectedRows = brandModelMapper.update(model);
            if (affectedRows == 0)
            {
                throw new ServiceException("标准型号不存在或已删除");
            }
            return affectedRows;
        }
        catch (DuplicateKeyException exception)
        {
            throw duplicateModelException();
        }
    }

    /**
     * 软删除标准型号；已经被商品或商户常用型号引用时拒绝删除，保留历史关联完整性。
     *
     * @param modelId 标准型号主键
     * @param operator 当前平台操作人员
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long modelId, String operator)
    {
        requireModel(modelId);
        if (brandModelMapper.countMerchantCommonModelReferences(modelId) > 0
                || rentalBusinessGateway.hasModelReferences(modelId))
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
     * 将平台写入命令转换为持久化实体，并统一规范文本、状态和官方标识。
     *
     * @param command 平台标准型号写入命令
     * @return 已完成基础校验和规范化的持久化实体
     */
    private RentalBrandModel toEntity(PlatformBrandModelCommand command)
    {
        if (command == null)
        {
            throw new ServiceException("品牌型号数据不能为空");
        }
        RentalBrandModel model = new RentalBrandModel();
        model.setModelId(command.getModelId());
        model.setClassId(command.getClassId());
        model.setBrand(StringUtils.trim(command.getBrand()));
        model.setModelName(StringUtils.trim(command.getModelName()));
        model.setMemorySpec(emptyToNull(StringUtils.trim(command.getMemorySpec())));
        model.setOfficialFlag(StringUtils.isBlank(command.getOfficialFlag()) ? "1" : command.getOfficialFlag());
        model.setStatus(StringUtils.isBlank(command.getStatus()) ? "0" : command.getStatus());
        model.setRemark(emptyToNull(StringUtils.trim(command.getRemark())));
        if (StringUtils.isBlank(model.getBrand()) || StringUtils.isBlank(model.getModelName()))
        {
            throw new ServiceException("品牌和型号不能为空");
        }
        assertLength(model.getBrand(), 64, "品牌长度不能超过64个字符");
        assertLength(model.getModelName(), 128, "型号长度不能超过128个字符");
        assertLength(model.getMemorySpec(), 64, "内存或规格长度不能超过64个字符");
        assertLength(model.getRemark(), 500, "备注长度不能超过500个字符");
        if (!"0".equals(model.getOfficialFlag()) && !"1".equals(model.getOfficialFlag()))
        {
            throw new ServiceException("官方标准标识不合法");
        }
        if (!"0".equals(model.getStatus()) && !"1".equals(model.getStatus()))
        {
            throw new ServiceException("型号状态不合法");
        }
        return model;
    }

    /**
     * 规范平台列表查询文本，避免空白字符改变模糊查询结果。
     *
     * @param query 平台标准型号查询条件，可为空
     * @return 可安全传入 MyBatis 的查询对象
     */
    private PlatformBrandModelQuery normalizeQuery(PlatformBrandModelQuery query)
    {
        PlatformBrandModelQuery result = query == null ? new PlatformBrandModelQuery() : query;
        result.setBrand(emptyToNull(StringUtils.trim(result.getBrand())));
        result.setModelName(emptyToNull(StringUtils.trim(result.getModelName())));
        result.setStatus(emptyToNull(StringUtils.trim(result.getStatus())));
        return result;
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
     * 将标准型号持久化实体转换为平台展示对象，避免 Controller 暴露数据库实体和软删除字段。
     *
     * @param model 标准型号持久化实体
     * @return 平台标准型号列表或详情展示对象
     */
    private PlatformBrandModelVO toView(RentalBrandModel model)
    {
        PlatformBrandModelVO view = new PlatformBrandModelVO();
        view.setModelId(model.getModelId());
        view.setClassId(model.getClassId());
        view.setClassName(model.getClassName());
        view.setBrand(model.getBrand());
        view.setModelName(model.getModelName());
        view.setMemorySpec(model.getMemorySpec());
        view.setOfficialFlag(model.getOfficialFlag());
        view.setStatus(model.getStatus());
        view.setCreateBy(model.getCreateBy());
        view.setCreateTime(model.getCreateTime());
        view.setUpdateBy(model.getUpdateBy());
        view.setUpdateTime(model.getUpdateTime());
        view.setRemark(model.getRemark());
        return view;
    }

    /**
     * 校验文本长度，保证应用服务被内部代码直接调用时仍遵守数据库字段上限。
     *
     * @param value 待校验文本，可为空
     * @param maxLength 数据库允许的最大字符数
     * @param message 超长时返回的平台业务提示
     */
    private void assertLength(String value, int maxLength, String message)
    {
        if (value != null && value.length() > maxLength)
        {
            throw new ServiceException(message);
        }
    }

    /**
     * 将空字符串统一转换为空值，保证唯一性校验与数据库生成唯一键使用相同语义。
     *
     * @param value 已经去除首尾空白的文本
     * @return 空文本返回 null，其他文本保持不变
     */
    private String emptyToNull(String value)
    {
        return StringUtils.isBlank(value) ? null : value;
    }

    /**
     * 创建统一的标准型号重复业务异常，屏蔽数据库约束名称和 SQL 细节。
     *
     * @return 面向平台操作人员的重复型号业务异常
     */
    private ServiceException duplicateModelException()
    {
        return new ServiceException("同一产品类目下的品牌、型号和规格已存在");
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
