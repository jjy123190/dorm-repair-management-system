package com.scau.dormrepair.config;

import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 * 读取桌面端运行配置。
 * 当前统一从 resources 下的 application.yml 读取，便于小组成员直接修改数据库和窗口参数。
 */
public record AppProperties(
        String name,
        String title,
        DatabaseProperties database,
        UiProperties ui
) {

    public static AppProperties load() {
        try (InputStream inputStream = AppProperties.class.getClassLoader().getResourceAsStream("application.yml")) {
            if (inputStream == null) {
                throw new IllegalStateException("未找到 application.yml 配置文件");
            }

            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(inputStream);
            Map<String, Object> appMap = mapValue(root, "app");
            Map<String, Object> databaseMap = mapValue(root, "database");
            Map<String, Object> uiMap = mapValue(root, "ui");

            return new AppProperties(
                    stringValue(appMap, "name"),
                    stringValue(appMap, "title"),
                    new DatabaseProperties(
                            stringValue(databaseMap, "url"),
                            stringValue(databaseMap, "username"),
                            stringValue(databaseMap, "password"),
                            stringValue(databaseMap, "driver-class-name")
                    ),
                    new UiProperties(
                            doubleValue(uiMap, "width"),
                            doubleValue(uiMap, "height"),
                            doubleValue(uiMap, "min-width"),
                            doubleValue(uiMap, "min-height")
                    )
            );
        } catch (Exception exception) {
            throw new IllegalStateException("读取 application.yml 失败: " + exception.getMessage(), exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapValue(Map<String, Object> root, String key) {
        Object value = root.get(key);
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalStateException("配置节点缺失或格式错误: " + key);
        }
        return (Map<String, Object>) map;
    }

    private static String stringValue(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value == null) {
            throw new IllegalStateException("缺少配置项: " + key);
        }
        return String.valueOf(value);
    }

    private static double doubleValue(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value == null) {
            throw new IllegalStateException("缺少配置项: " + key);
        }
        return Double.parseDouble(String.valueOf(value));
    }

    /**
     * 数据库连接参数。
     */
    public record DatabaseProperties(
            String url,
            String username,
            String password,
            String driverClassName
    ) {
    }

    /**
     * 桌面端窗口参数。
     */
    public record UiProperties(
            double width,
            double height,
            double minWidth,
            double minHeight
    ) {
    }
}
