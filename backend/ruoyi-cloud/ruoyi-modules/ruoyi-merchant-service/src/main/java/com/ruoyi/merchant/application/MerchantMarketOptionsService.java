package com.ruoyi.merchant.application;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.merchant.model.MerchantMarketBoardType;
import com.ruoyi.merchant.model.MerchantMarketConditionLevel;
import com.ruoyi.merchant.model.MerchantMarketModelOption;
import com.ruoyi.merchant.model.MerchantMarketOptionsVO;
import com.ruoyi.merchant.model.MerchantMarketPriceRange;
import com.ruoyi.merchant.model.MerchantMarketSelectOption;
import com.ruoyi.system.api.RemoteBrandModelService;
import com.ruoyi.system.api.model.BrandModel;

/** 周转市场受控选项服务，统一约束页面选项和服务端写入值。 */
@Service
public class MerchantMarketOptionsService
{
    private final RemoteBrandModelService remoteBrandModelService;

    public MerchantMarketOptionsService(RemoteBrandModelService remoteBrandModelService)
    {
        this.remoteBrandModelService = remoteBrandModelService;
    }

    /** 查询当前商户发帖所需的标准型号、成色和价格区间。 */
    public MerchantMarketOptionsVO options()
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantMarketOptionsVO result = new MerchantMarketOptionsVO();
        result.setModels(loadActiveModels().stream().map(this::toModelOption).collect(Collectors.toList()));
        result.setConditionLevels(Arrays.stream(MerchantMarketConditionLevel.values())
                .map(item -> new MerchantMarketSelectOption(item.getCode(), item.getLabel()))
                .collect(Collectors.toList()));
        result.setPriceRanges(Arrays.stream(MerchantMarketPriceRange.values())
                .map(item -> new MerchantMarketSelectOption(item.getCode(), item.getLabel()))
                .collect(Collectors.toList()));
        return result;
    }

    /** 校验并归一化一个标准型号，避免客户端绕过下拉框写入自由文本。 */
    public MerchantMarketModelOption requireModel(String category, String brand, String modelName, String memorySpec)
    {
        String normalizedCategory = required(category, "品类");
        String normalizedBrand = required(brand, "品牌");
        String normalizedModelName = required(modelName, "型号");
        String normalizedMemorySpec = trim(memorySpec);
        for (BrandModel model : loadActiveModels())
        {
            if (Objects.equals(normalizedCategory, trim(model.getClassName()))
                    && Objects.equals(normalizedBrand, trim(model.getBrand()))
                    && Objects.equals(normalizedModelName, trim(model.getModelName()))
                    && Objects.equals(normalizedMemorySpec, trim(model.getMemorySpec())))
            {
                return toModelOption(model);
            }
        }
        throw new ServiceException("品类、品牌、型号和规格必须选择平台有效标准型号");
    }

    /** 将成色编码或历史中文值归一化为平台固定编码。 */
    public String normalizeCondition(String value)
    {
        MerchantMarketConditionLevel condition = MerchantMarketConditionLevel.fromValue(value);
        if (condition == null)
        {
            throw new ServiceException("请选择有效的设备成色");
        }
        return condition.getCode();
    }

    /** 出售/求购只允许固定价格区间，出租必须为空。 */
    public String normalizePrice(MerchantMarketBoardType boardType, String value)
    {
        String normalized = trim(value);
        if (boardType == MerchantMarketBoardType.RENT)
        {
            if (normalized != null)
            {
                throw new ServiceException("出租板块不允许填写价格区间");
            }
            return null;
        }
        MerchantMarketPriceRange priceRange = MerchantMarketPriceRange.fromValue(normalized);
        if (priceRange == null)
        {
            throw new ServiceException("出售和求购板块必须选择系统价格区间");
        }
        return priceRange.getCode();
    }

    public String conditionLabel(String value)
    {
        MerchantMarketConditionLevel condition = MerchantMarketConditionLevel.fromValue(value);
        return condition == null ? value : condition.getLabel();
    }

    public String priceLabel(MerchantMarketBoardType boardType, String value)
    {
        if (boardType == MerchantMarketBoardType.RENT || trim(value) == null)
        {
            return null;
        }
        return MerchantMarketPriceRange.labelOf(value);
    }

    private List<BrandModel> loadActiveModels()
    {
        R<List<BrandModel>> response = remoteBrandModelService.listActive(SecurityConstants.INNER);
        if (response == null || R.FAIL == response.getCode())
        {
            throw new ServiceException(response == null ? "平台标准型号服务无响应" : response.getMsg());
        }
        return response.getData() == null ? Collections.emptyList() : response.getData();
    }

    private MerchantMarketModelOption toModelOption(BrandModel model)
    {
        MerchantMarketModelOption option = new MerchantMarketModelOption();
        option.setCategory(trim(model.getClassName()));
        option.setBrand(trim(model.getBrand()));
        option.setModelName(trim(model.getModelName()));
        option.setMemorySpec(trim(model.getMemorySpec()));
        return option;
    }

    private String required(String value, String field)
    {
        String normalized = trim(value);
        if (normalized == null)
        {
            throw new ServiceException(field + "不能为空");
        }
        return normalized;
    }

    private String trim(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            return null;
        }
        return value.trim();
    }
}
