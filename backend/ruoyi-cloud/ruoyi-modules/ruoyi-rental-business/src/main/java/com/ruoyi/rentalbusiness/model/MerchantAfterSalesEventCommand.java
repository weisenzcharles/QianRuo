package com.ruoyi.rentalbusiness.model;

/** 售后处理记录命令。事件类型由服务端固定为处理记录，避免客户端伪造状态事件。 */
public class MerchantAfterSalesEventCommand
{
    public String content;
}
