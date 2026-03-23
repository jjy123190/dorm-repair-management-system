package com.scau.dormrepair.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertEquals("root", appProperties.database().username());
    }
}
