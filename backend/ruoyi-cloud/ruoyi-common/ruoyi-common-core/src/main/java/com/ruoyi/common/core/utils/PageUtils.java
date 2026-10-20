package com.ruoyi.common.core.utils;

import com.github.pagehelper.PageHelper;
import com.ruoyi.common.core.utils.sql.SqlUtil;
import com.ruoyi.common.core.web.page.PageDomain;
import com.ruoyi.common.core.web.page.TableSupport;

/**
 * 分页工具类
 * 
 * @author ruoyi
 */
public class PageUtils extends PageHelper
{
    /**
     * 设置请求分页数据
     */
    public static void startPage()
    {
        startPage(true);
    }

    /**
     * 设置请求分页数据，并允许调用方关闭 PageHelper 自动总数解析。
     *
     * <p>复杂列表可以自行执行独立的 count SQL，避免 PageHelper 对带有 JSON_TABLE
     * 或大量相关子查询的展示 SQL 生成总数 SQL 时消耗过长。</p>
     *
     * @param count 是否由 PageHelper 自动执行总数查询
     */
    public static void startPage(boolean count)
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy)
                .setReasonable(reasonable)
                .setCount(count);
    }

    /**
     * 清理分页的线程变量
     */
    public static void clearPage()
    {
        PageHelper.clearPage();
    }
}
