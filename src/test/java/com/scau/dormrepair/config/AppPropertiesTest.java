package com.scau.dormrepair.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 配置读取最小测试。
 */
class AppPropertiesTest {

    @Test
    void shouldLoadApplicationYaml() {
        AppProperties appProperties = AppProperties.load();
        assertNotNull(appProperties);
        assertEquals("dorm-repair-management-system", appProperties.name());
        assertEquals("宿舍报修与工单管理系统", appProperties.title());
        assertTrue(appProperties.database().url().contains("dorm_repair_db"));
        assertEquals("root", appProperties.database().username());
        assertEquals(1440, appProperties.ui().width());
        assertEquals(900, appProperties.ui().height());
        assertEquals(1280, appProperties.ui().minWidth());
        assertEquals(820, appProperties.ui().minHeight());
    }
}
