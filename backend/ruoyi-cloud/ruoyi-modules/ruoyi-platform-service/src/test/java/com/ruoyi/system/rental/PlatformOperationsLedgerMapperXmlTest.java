package com.ruoyi.system.rental;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;
import com.ruoyi.system.model.rental.PlatformBackupRecordQuery;
import com.ruoyi.system.model.rental.PlatformCapacityQuery;

/** 验证平台运维台账 Mapper SQL 可以被 MyBatis 正确解析和绑定。 */
public class PlatformOperationsLedgerMapperXmlTest
{
    @Test
    public void ledgerStatementsParseAndBind() throws Exception
    {
        String resource = "mapper/rental/PlatformOperationsLedgerMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("query", new PlatformBackupRecordQuery());
        params.put("backupKey", "backups/test.sql.gz");
        params.put("backupNo", "BK202609280001");
        params.put("backupType", Integer.valueOf(1));
        params.put("backupStatus", Integer.valueOf(1));
        params.put("fileName", "test.sql.gz");
        params.put("fileSizeBytes", Long.valueOf(1024L));
        params.put("startedTime", "2026-09-28 10:00:00");
        params.put("completedTime", "2026-09-28 10:01:00");
        params.put("retentionUntil", "2026-10-05 10:01:00");
        params.put("failureReason", null);
        params.put("operator", Long.valueOf(1L));
        params.put("scopeType", Integer.valueOf(1));
        params.put("scopeKey", "ry-rental-db");
        params.put("scopeName", "租赁数据库");
        params.put("capacityBytes", Long.valueOf(1000L));
        params.put("usedBytes", Long.valueOf(500L));
        params.put("availableBytes", Long.valueOf(500L));
        params.put("changedBytes", Long.valueOf(100L));
        params.put("changeType", Integer.valueOf(2));
        params.put("snapshotSource", "测试监控");
        params.put("measuredTime", "2026-09-28 10:00:00");
        for (String statement : new String[] { "selectBackupRecords", "selectBackupByKey", "insertBackupRecord",
                "selectLatestSuccessfulBackup", "countBackupsByStatus", "selectCapacityLedgers",
                "selectLatestCapacity", "selectCapacitySummary", "insertCapacityLedger" })
        {
            if (statement.contains("Capacity"))
                params.put("query", new PlatformCapacityQuery());
            configuration.getMappedStatement("com.ruoyi.system.mapper.rental.PlatformOperationsLedgerMapper." + statement)
                    .getBoundSql(params);
        }
        params.put("query", new PlatformBackupRecordQuery());
        params.put("scopeType", Integer.valueOf(1));
        params.put("scopeKey", "ry-rental-db");
        params.put("alertEventKeys", Collections.singletonList("unused"));
    }
}
