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

    public static final String SCOPE_TODAY_SHIPPING = "today-shipping";

    public static final String SCOPE_TODAY_RETURN = "today-return";

    public static final String SCOPE_TODAY_ARRIVAL = "today-arrival";

    public static final String SCOPE_TOMORROW_ARRIVAL = "tomorrow-arrival";

    public static final String SCOPE_TODAY_CREATED = "today-created";

    public static final String SCOPE_TODAY_COMPLETED = "today-completed";

    private String keyword;

    private Integer status;

    private Long channelId;

    private Long warehouseId;

    private boolean attentionOnly;

    private boolean shippingOnly;

    private String dateStart;

    private String dateEnd;

    private OrderDateType dateType;

    private OrderSortType sortType;

    private OrderSortDirection sortDirection;

    private String orderScope;

    private boolean allWarehouseAccess;

    private Set<Long> warehouseIds = new LinkedHashSet<Long>();

    /**
     * 获取订单搜索关键词。
     *
     * @return 可匹配订单编号、客户、渠道和商品信息的关键词
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
    public Integer getStatus()
    {
        return status;
    }

    /**
     * 设置订单状态筛选值。
     *
     * @param status 前端状态标签或后端状态编码
     */
    public void setStatus(Integer status)
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

    public Long getWarehouseId()
    {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId)
    {
        this.warehouseId = warehouseId;
    }

    public boolean isAttentionOnly()
    {
        return attentionOnly;
    }

    public void setAttentionOnly(boolean attentionOnly)
    {
        this.attentionOnly = attentionOnly;
    }

    public boolean isShippingOnly()
    {
        return shippingOnly;
    }

    public void setShippingOnly(boolean shippingOnly)
    {
        this.shippingOnly = shippingOnly;
    }

    /** 获取日期范围起始日，格式为 yyyy-MM-dd。 */
    public String getDateStart()
    {
        return dateStart;
    }

    /** 设置日期范围起始日。 */
    public void setDateStart(String dateStart)
    {
        this.dateStart = dateStart;
    }

    /** 获取日期范围结束日，格式为 yyyy-MM-dd。 */
    public String getDateEnd()
    {
        return dateEnd;
    }

    /** 设置日期范围结束日。 */
    public void setDateEnd(String dateEnd)
    {
        this.dateEnd = dateEnd;
    }

    /** 获取日期筛选类型。 */
    public OrderDateType getDateType()
    {
        return dateType;
    }

    /**
     * 设置日期筛选类型；非法值按未设置处理，由服务层使用发货日期作为默认类型。
     */
    public void setDateType(String dateType)
    {
        this.dateType = OrderDateType.fromCode(dateType);
    }

    /** 返回供 MyBatis 动态 SQL 使用的受控日期类型编码。 */
    public String getDateTypeCode()
    {
        return dateType == null ? null : dateType.getCode();
    }

    /** 获取排序类型。 */
    public OrderSortType getSortType()
    {
        return sortType;
    }

    /** 设置排序类型；非法值按未设置处理，由服务层使用创建时间作为默认类型。 */
    public void setSortType(String sortType)
    {
        this.sortType = OrderSortType.fromCode(sortType);
    }

    /** 返回供 MyBatis 动态 SQL 使用的受控排序类型编码。 */
    public String getSortTypeCode()
    {
        return sortType == null ? null : sortType.getCode();
    }

    /** 获取排序方向。 */
    public OrderSortDirection getSortDirection()
    {
        return sortDirection;
    }

    /** 设置排序方向；非法值按未设置处理，由服务层使用倒序作为默认方向。 */
    public void setSortDirection(String sortDirection)
    {
        this.sortDirection = OrderSortDirection.fromCode(sortDirection);
    }

    /** 返回供 MyBatis 动态 SQL 使用的受控排序方向编码。 */
    public String getSortDirectionCode()
    {
        return sortDirection == null ? null : sortDirection.getCode();
    }

    /**
     * 获取订单快捷视图筛选值。
     *
     * @return 服务端支持的订单快捷视图编码
     */
    public String getOrderScope()
    {
        return orderScope;
    }

    /**
     * 设置订单快捷视图筛选值。
     *
     * @param orderScope 订单快捷视图编码
     */
    public void setOrderScope(String orderScope)
    {
        this.orderScope = orderScope;
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
     * @return 当前用户可访问的仓库唯一编号集合
     */
    public Set<Long> getWarehouseIds()
    {
        return Collections.unmodifiableSet(warehouseIds);
    }

    /**
     * 设置服务端从登录上下文取得的仓库范围。
     *
     * @param warehouseIds 当前用户可访问的仓库唯一编号集合
     */
    public void setWarehouseIds(Set<Long> warehouseIds)
    {
        this.warehouseIds = warehouseIds == null
                ? new LinkedHashSet<Long>()
                : new LinkedHashSet<Long>(warehouseIds);
    }

    /** 订单列表可用的日期字段白名单。下单日期和创建日期均映射到订单创建时间。 */
    public enum OrderDateType
    {
        SHIP("ship"),
        RETURN("return"),
        ARRIVAL("arrival"),
        LEASE_START("lease_start"),
        ORDER("order"),
        CREATE("create");

        private final String code;

        OrderDateType(String code)
        {
            this.code = code;
        }

        public String getCode()
        {
            return code;
        }

        public static OrderDateType fromCode(String value)
        {
            if (value == null || value.trim().isEmpty())
            {
                return null;
            }
            for (OrderDateType item : values())
            {
                if (item.code.equals(value.trim()))
                {
                    return item;
                }
            }
            return null;
        }
    }

    /** 订单列表可用的排序字段白名单。 */
    public enum OrderSortType
    {
        STATUS("status"),
        SHIP("ship"),
        RETURN("return"),
        ARRIVAL("arrival"),
        LEASE_START("lease_start"),
        ORDER("order"),
        CREATE("create");

        private final String code;

        OrderSortType(String code)
        {
            this.code = code;
        }

        public String getCode()
        {
            return code;
        }

        public static OrderSortType fromCode(String value)
        {
            if (value == null || value.trim().isEmpty())
            {
                return null;
            }
            for (OrderSortType item : values())
            {
                if (item.code.equals(value.trim()))
                {
                    return item;
                }
            }
            return null;
        }
    }

    /** 订单列表排序方向白名单。 */
    public enum OrderSortDirection
    {
        ASC("asc"),
        DESC("desc");

        private final String code;

        OrderSortDirection(String code)
        {
            this.code = code;
        }

        public String getCode()
        {
            return code;
        }

        public static OrderSortDirection fromCode(String value)
        {
            if (value == null || value.trim().isEmpty())
            {
                return null;
            }
            for (OrderSortDirection item : values())
            {
                if (item.code.equals(value.trim()))
                {
                    return item;
                }
            }
            return null;
        }
    }
}
