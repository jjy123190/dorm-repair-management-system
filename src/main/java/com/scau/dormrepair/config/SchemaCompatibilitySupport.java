package com.scau.dormrepair.config;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 启动时顺手修掉旧库和当前桌面端口径最容易撞上的表结构差异。
 */
public final class SchemaCompatibilitySupport {

    private static final String[] DORM_AREAS = {
            "泰山区",
            "华山区",
            "启林区",
            "黑山区",
            "燕山区"
    };

    private SchemaCompatibilitySupport() {
    }

    public static void repair(HikariDataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String catalog = connection.getCatalog();
            if (catalog == null || catalog.isBlank()) {
                return;
            }

            repairRequestTable(connection, catalog);
            seedDormBuildings(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("数据库表结构兼容处理失败: " + exception.getMessage(), exception);
        }
    }

    private static void repairRequestTable(Connection connection, String catalog) throws SQLException {
        // 新版学生报修先选宿舍区，再选楼栋，所以 repair_requests 需要额外保留宿舍区快照。
        ensureColumnExists(connection, catalog, "repair_requests", "dorm_area_snapshot", "VARCHAR(64) NULL");
        ensureColumnExists(connection, catalog, "repair_requests", "building_no_snapshot", "VARCHAR(32) NULL");
        ensureColumnExists(connection, catalog, "repair_requests", "room_no_snapshot", "VARCHAR(32) NULL");

        // 旧字段保留兼容即可，但必须放宽为可空，避免新插入被历史结构卡死。
        relaxNullableColumnIfPresent(connection, catalog, "repair_requests", "building_no", "VARCHAR(32) NULL");
        relaxNullableColumnIfPresent(connection, catalog, "repair_requests", "room_no", "VARCHAR(32) NULL");
        relaxNullableColumnIfPresent(connection, catalog, "repair_requests", "image_urls", "TINYTEXT NULL");
    }

    private static void seedDormBuildings(Connection connection) throws SQLException {
        String insertSql = """
                INSERT INTO dorm_buildings (campus_name, building_no, building_name)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE building_name = VALUES(building_name)
                """;

        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            for (String dormArea : DORM_AREAS) {
                for (int buildingIndex = 1; buildingIndex <= 15; buildingIndex++) {
                    String buildingNo = buildingIndex + "栋";
                    statement.setString(1, dormArea);
                    statement.setString(2, buildingNo);
                    statement.setString(3, dormArea + " " + buildingNo);
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        }
    }

    private static void ensureColumnExists(
            Connection connection,
            String catalog,
            String tableName,
            String columnName,
            String columnDefinition
    ) throws SQLException {
        if (findColumn(connection.getMetaData(), catalog, tableName, columnName).present()) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition
            );
        }
    }

    private static void relaxNullableColumnIfPresent(
            Connection connection,
            String catalog,
            String tableName,
            String columnName,
            String columnDefinition
    ) throws SQLException {
        ColumnState columnState = findColumn(connection.getMetaData(), catalog, tableName, columnName);
        if (!columnState.present() || columnState.nullable()) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " " + columnDefinition
            );
        }
    }

    private static ColumnState findColumn(
            DatabaseMetaData metadata,
            String catalog,
            String tableName,
            String columnName
    ) throws SQLException {
        try (ResultSet resultSet = metadata.getColumns(catalog, null, tableName, columnName)) {
            if (!resultSet.next()) {
                return new ColumnState(false, true);
            }
            return new ColumnState(true, resultSet.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
        }
    }

    private record ColumnState(boolean present, boolean nullable) {
    }
}
