package com.ruoyi.rentalbusiness.service;

import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.api.RemoteCodeRuleService;

/**
 * 租赁业务服务的统一编码客户端。
 *
 * <p>本类是业务服务与平台编码分配器之间的唯一适配边界。业务应用服务不得直接
 * 使用时间、随机数、数据库唯一编号或客户端提交值生成仓库、商品、套餐等业务编码。</p>
 */
@Service
public class CodeGeneratorClient
{
    private final RemoteCodeRuleService remoteCodeRuleService;

    /**
     * 注入平台统一编码内部契约。
     *
     * @param remoteCodeRuleService 平台统一编码内部契约
     */
    public CodeGeneratorClient(RemoteCodeRuleService remoteCodeRuleService)
    {
        this.remoteCodeRuleService = remoteCodeRuleService;
    }

    /**
     * 分配一个平台规则驱动的下一个业务编码。
     *
     * @param codeType 编码类型键
     * @return 唯一业务编码
     */
    public String next(String codeType)
    {
        R<String> response = remoteCodeRuleService.nextCode(codeType, SecurityConstants.INNER);
        if (response == null || R.FAIL == response.getCode() || StringUtils.isBlank(response.getData()))
        {
            throw new ServiceException(response == null ? "平台统一编码服务无响应"
                    : StringUtils.defaultIfEmpty(response.getMsg(), "平台统一编码服务暂不可用"));
        }
        return response.getData();
    }
}
