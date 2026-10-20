package com.ruoyi.merchant.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

/** 商户菜单树节点。 */
public class MerchantTreeSelect
{
    private Long id;
    private String label;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<MerchantTreeSelect> children;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public List<MerchantTreeSelect> getChildren() { return children; }
    public void setChildren(List<MerchantTreeSelect> children) { this.children = children; }
}
