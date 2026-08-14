package com.ruoyi.rentalbusiness.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 商户订单列表查询条件。
 *
 * 请求对象只承载页面筛选条件，租户、商户和仓库范围由应用服务从可信登录
 * 上下文覆盖写入，客户端提交的任何归属字段都不会进入 SQL。
 */
public class MerchantOrderQuery extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String keyword;

    private String status;

    private Long channelId;

    private String shipDate;

    private boolean allWarehouseAccess;

    private Set<Long> warehouseIds = new LinkedHashSet<Long>();

    /**
     * 获取订单搜索关键词。
     *
     * @return 可匹配订单号、客户、渠道和商品信息的关键词
     */
    public String getKeyword()
    {
        return keyword;
    }

    /**
     * 设置订单搜索关键词。
     *
     * @param keyword 订单搜索关键词
     */
    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    /**
     * 获取订单状态筛选值。
     *
     * @return 前端状态标签或后端状态编码
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * 设置订单状态筛选值。
     *
     * @param status 前端状态标签或后端状态编码
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * 获取订单渠道筛选值。
     *
     * @return 订单渠道编码或名称
     */
    public Long getChannelId()
    {
        return channelId;
    }

    /**
     * 设置订单渠道筛选值。
     *
     * @param channel 订单渠道编码或名称
     */
    public void setChannelId(Long channelId)
    {
        this.channelId = channelId;
    }

    /**
     * 获取发货日期筛选值。
     *
     * @return yyyy-MM-dd 格式的发货日期
     */
    public String getShipDate()
    {
        return shipDate;
    }

    /**
     * 设置发货日期筛选值。
     *
     * @param shipDate yyyy-MM-dd 格式的发货日期
     */
    public void setShipDate(String shipDate)
    {
        this.shipDate = shipDate;
    }

    /**
     * 判断当前用户是否可以访问商户全部仓库。
     *
     * @return 全部仓库返回 true，指定仓库范围返回 false
     */
    public boolean isAllWarehouseAccess()
    {
        return allWarehouseAccess;
    }

    /**
     * 设置当前用户是否可以访问商户全部仓库。
     *
     * @param allWarehouseAccess 全部仓库访问标识
     */
    public void setAllWarehouseAccess(boolean allWarehouseAccess)
    {
        this.allWarehouseAccess = allWarehouseAccess;
    }

    /**
     * 获取服务端从登录上下文取得的仓库范围。
     *
     * @return 当前用户可访问的仓库主键集合
     */
    public Set<Long> getWarehouseIds()
    {
        return Collections.unmodifiableSet(warehouseIds);
    }

    /**
     * 设置服务端从登录上下文取得的仓库范围。
     *
     * @param warehouseIds 当前用户可访问的仓库主键集合
     */
    public void setWarehouseIds(Set<Long> warehouseIds)
    {
        this.warehouseIds = warehouseIds == null
                ? new LinkedHashSet<Long>()
                : new LinkedHashSet<Long>(warehouseIds);
    }
}
