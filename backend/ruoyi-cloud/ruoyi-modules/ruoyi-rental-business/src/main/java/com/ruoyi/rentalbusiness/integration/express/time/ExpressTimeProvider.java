package com.ruoyi.rentalbusiness.integration.express.time;

import com.ruoyi.rentalbusiness.enums.ExpressTimeProviderCode;
import com.ruoyi.rentalbusiness.model.ExpressTimeProviderRequest;
import com.ruoyi.rentalbusiness.model.ExpressTimeProviderResult;

/** 快递时效供应商统一契约，与快递轨迹查询客户端保持独立。 */
public interface ExpressTimeProvider
{
    /** 返回供应商编码。 */
    ExpressTimeProviderCode provider();

    /** 查询寄件地到收件地的预估时效。 */
    ExpressTimeProviderResult estimate(ExpressTimeProviderRequest request);
}
