package com.scau.dormrepair.config;

import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public record AppProperties(
        String name,
        String title,
        DatabaseProperties database,
        UiProperties ui
) {

    public static AppProperties load() {
        try (InputStream inputStream = AppProperties.class.getClassLoader().getResourceAsStream("application.yml")) {
            if (inputStream == null) {
                throw new IllegalStateException("application.yml not found");
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
                            doubleValue(uiMap, "min-height"),
                            doubleValue(uiMap, "design-width"),
                            doubleValue(uiMap, "design-height")
                    )
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to read application.yml: " + exception.getMessage(), exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapValue(Map<String, Object> root, String key) {
        Object value = root.get(key);
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalStateException("Missing or invalid config section: " + key);
        }
        return (Map<String, Object>) map;
    }

    private static String stringValue(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value == null) {
            throw new IllegalStateException("Missing config value: " + key);
        }
        return String.valueOf(value);
    }

    private static double doubleValue(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value == null) {
            throw new IllegalStateException("Missing config value: " + key);
        }
        return Double.parseDouble(String.valueOf(value));
    }

    public record DatabaseProperties(
            String url,
            String username,
            String password,
            String driverClassName
    ) {
    }

    public record UiProperties(
            double width,
            double height,
            double minWidth,
            double minHeight,
            double designWidth,
            double designHeight
    ) {
    }
}