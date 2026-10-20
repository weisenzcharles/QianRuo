package com.ruoyi.merchant.config;

import java.sql.Connection;
import java.util.Properties;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logs merchant-service SQL templates without logging bound parameter values.
 */
@Component
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class })
})
public class MerchantSqlLoggingInterceptor implements Interceptor
{
    private static final Logger SQL_LOG = LoggerFactory.getLogger("com.ruoyi.merchant.sql");

    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        if (SQL_LOG.isDebugEnabled())
        {
            SQL_LOG.debug("{}", boundSql.getSql().replaceAll("\\s+", " ").trim());
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target)
    {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties)
    {
        // SQL logging is controlled by the merchant-service logger level.
    }
}
