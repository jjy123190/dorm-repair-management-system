package com.scau.dormrepair.config;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 启动时顺手把旧版本库里最容易撞到的结构差异修正掉。
 * 现在这套桌面端已经改用 snapshot 字段，老库里残留的 building_no / room_no 如果还是 NOT NULL，
 * 插入报修时就会直接因为“旧字段没给值”而失败。
 */
public final class SchemaCompatibilitySupport {

    private SchemaCompatibilitySupport() {
    }

    public static void repair(HikariDataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String catalog = connection.getCatalog();
            if (catalog == null || catalog.isBlank()) {
                return;
            }

            repairRequestTable(connection, catalog);
        } catch (SQLException exception) {
            throw new IllegalStateException("数据库表结构兼容处理失败: " + exception.getMessage(), exception);
        }
    }

    private static void repairRequestTable(Connection connection, String catalog) throws SQLException {
        // 先保证桌面端当前依赖的 snapshot 字段存在，避免老库只有 building_no / room_no 时直接崩。
        ensureColumnExists(connection, catalog, "repair_requests", "building_no_snapshot", "VARCHAR(32) NULL");
        ensureColumnExists(connection, catalog, "repair_requests", "room_no_snapshot", "VARCHAR(32) NULL");

        // 旧字段如果还保留着，就把历史值回填给 snapshot，后续列表和详情仍然能正常显示。
        copyLegacySnapshotIfNeeded(connection, catalog, "building_no_snapshot", "building_no");
        copyLegacySnapshotIfNeeded(connection, catalog, "room_no_snapshot", "room_no");

        // 老字段继续留着也没关系，但必须放宽为可空，不然新插入语句会被旧字段卡死。
        relaxNullableColumnIfPresent(connection, catalog, "repair_requests", "building_no", "VARCHAR(32) NULL");
        relaxNullableColumnIfPresent(connection, catalog, "repair_requests", "room_no", "VARCHAR(32) NULL");
        relaxNullableColumnIfPresent(connection, catalog, "repair_requests", "image_urls", "TINYTEXT NULL");
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

    private static void copyLegacySnapshotIfNeeded(
            Connection connection,
            String catalog,
            String snapshotColumn,
            String legacyColumn
    ) throws SQLException {
        ColumnState snapshot = findColumn(connection.getMetaData(), catalog, "repair_requests", snapshotColumn);
        ColumnState legacy = findColumn(connection.getMetaData(), catalog, "repair_requests", legacyColumn);
        if (!snapshot.present() || !legacy.present()) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "UPDATE repair_requests " +
                            "SET " + snapshotColumn + " = " + legacyColumn + " " +
                            "WHERE (" + snapshotColumn + " IS NULL OR " + snapshotColumn + " = '') " +
                            "AND " + legacyColumn + " IS NOT NULL"
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
