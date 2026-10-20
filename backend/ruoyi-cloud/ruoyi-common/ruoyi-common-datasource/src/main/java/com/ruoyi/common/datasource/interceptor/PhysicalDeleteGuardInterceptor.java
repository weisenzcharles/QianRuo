package com.ruoyi.common.datasource.interceptor;

import java.sql.Connection;
import java.util.Locale;
import java.util.Properties;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.exception.ServiceException;

/**
 * MyBatis 物理删除保护拦截器。
 *
 * <p>项目统一使用 {@code is_delete} 软删除，任何运行时 {@code DELETE} 或 {@code TRUNCATE} SQL 都会被拒绝。</p>
 */
@Component
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class })
})
public class PhysicalDeleteGuardInterceptor implements Interceptor
{
    /**
     * 在 JDBC 预编译前检查最终 SQL，发现物理删除语句时立即中止执行。
     *
     * @param invocation MyBatis 方法调用上下文
     * @return 原始 StatementHandler 执行结果
     * @throws Throwable 原始执行异常或物理删除保护异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = normalize(boundSql.getSql());
        if (sql.startsWith("delete ") || sql.startsWith("truncate "))
        {
            throw new ServiceException("检测到物理删除SQL，所有数据必须通过is_delete字段软删除");
        }
        return invocation.proceed();
    }

    /**
     * 使用 MyBatis 标准插件代理目标对象。
     *
     * @param target 待代理目标
     * @return 插件代理对象
     */
    @Override
    public Object plugin(Object target)
    {
        return Plugin.wrap(target, this);
    }

    /**
     * 接收 MyBatis 插件配置；当前保护规则固定，不读取外部开关以防止被绕过。
     *
     * @param properties MyBatis 插件配置
     */
    @Override
    public void setProperties(Properties properties)
    {
        // 软删除保护为系统强制规则，不提供关闭配置。
    }

    /**
     * 去除 SQL 首尾空白、合并连续空白并转换为小写，保证关键字判断稳定。
     *
     * @param sql MyBatis 最终 SQL
     * @return 规范后的 SQL
     */
    private String normalize(String sql)
    {
        return sql == null ? "" : sql.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
