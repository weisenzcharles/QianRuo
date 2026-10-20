package com.ruoyi.rentalbusiness.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 商户订单批注历史响应对象。
 *
 * <p>操作人名称由商户身份内部服务解析，用户唯一编号仅供服务端批量查询使用，不向前端暴露。</p>
 */
public class MerchantOrderNoteVO
{
    public Long noteId;
    public String noteContent;
    public Integer noteType;
    @JsonIgnore
    public Long operatorId;
    public String operatorName;
    public String createTime;
}
