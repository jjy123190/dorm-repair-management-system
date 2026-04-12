package com.scau.dormrepair.common;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DemoDataSqlSeedTest {

    @Test
    void shouldKeepSeedScriptInSyncWithStableDemoAccounts() throws IOException {
        String sql = Files.readString(Path.of("sql", "mysql", "02_seed_demo_data.sql"));

        assertTrue(sql.contains("(1001, '1001', NULL, '张三'"));
        assertTrue(sql.contains("(1002, '1002', NULL, '李晓雨'"));
        assertTrue(sql.contains("(1003, '1003', NULL, '相逢的'"));
        assertTrue(sql.contains("(2001, '2001', NULL, '李老师'"));
        assertTrue(sql.contains("(2002, '2002', NULL, '陈老师'"));
        assertTrue(sql.contains("(3001, '3001', NULL, '王师傅'"));
        assertTrue(sql.contains("(3002, '3002', NULL, '周师傅'"));
        assertTrue(sql.contains("(3003, '3003', NULL, '陈师傅'"));
        assertTrue(sql.contains("'RR-DEMO-5001'"));
        assertTrue(sql.contains("'WO-DEMO-6001'"));
    }
}
