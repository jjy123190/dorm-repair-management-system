package com.scau.dormrepair.config;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class SchemaCompatibilitySupport {

    private static final String BUILDING_SUFFIX = "\u680b";
    private static final String DEFAULT_PASSWORD_HASH =
            "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";
    private static final SeedAccount[] SEED_ACCOUNTS = {
            new SeedAccount("student01", "\u5f20\u4e09", "13800138001", "STUDENT"),
            new SeedAccount("student02", "\u674e\u6653\u96e8", "13800138002", "STUDENT"),
            new SeedAccount("student03", "\u76f8\u9022\u7684", "13800138003", "STUDENT"),
            new SeedAccount("admin01", "\u674e\u8001\u5e08", "13800138101", "ADMIN"),
            new SeedAccount("admin02", "\u9648\u8001\u5e08", "13800138102", "ADMIN"),
            new SeedAccount("worker01", "\u738b\u5e08\u5085", "13800138201", "WORKER"),
            new SeedAccount("worker02", "\u5468\u5e08\u5085", "13800138202", "WORKER"),
            new SeedAccount("worker03", "\u9648\u5e08\u5085", "13800138203", "WORKER")
    };
    private static final String[] DORM_AREAS = {
            "\u6cf0\u5c71\u533a",
            "\u534e\u5c71\u533a",
            "\u542f\u6797\u533a",
            "\u96c1\u5c71\u533a",
            "\u71d5\u5c71\u533a"
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
            repairDormBuildingTable(connection, catalog);
            repairDormRoomTable(connection, catalog);
            repairWorkOrderTable(connection, catalog);
            ensureWorkOrderCompletionImagesTable(connection, catalog);
            ensureAuditLogsTable(connection, catalog);
            seedDormBuildings(connection);
            seedUserAccounts(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("\u6570\u636e\u5e93\u8868\u7ed3\u6784\u517c\u5bb9\u5904\u7406\u5931\u8d25: " + exception.getMessage(), exception);
        }
    }

    private static void repairRequestTable(Connection connection, String catalog) throws SQLException {
        ensureColumnExists(connection, catalog, "repair_requests", "dorm_area_snapshot", "VARCHAR(64) NULL");
        ensureColumnExists(connection, catalog, "repair_requests", "building_no_snapshot", "VARCHAR(32) NULL");
        ensureColumnExists(connection, catalog, "repair_requests", "room_no_snapshot", "VARCHAR(32) NULL");

        relaxNullableColumnIfPresent(connection, catalog, "repair_requests", "building_no", "VARCHAR(32) NULL");
        relaxNullableColumnIfPresent(connection, catalog, "repair_requests", "room_no", "VARCHAR(32) NULL");
        relaxNullableColumnIfPresent(connection, catalog, "repair_requests", "image_urls", "TINYTEXT NULL");
    }

    private static void repairDormBuildingTable(Connection connection, String catalog) throws SQLException {
        ensureColumnExists(connection, catalog, "dorm_buildings", "campus_name", "VARCHAR(64) NOT NULL");
        ensureColumnExists(connection, catalog, "dorm_buildings", "building_no", "VARCHAR(32) NOT NULL");
        ensureColumnExists(connection, catalog, "dorm_buildings", "building_name", "VARCHAR(64) NULL");

        String placeholders = Arrays.stream(DORM_AREAS)
                .map(area -> "?")
                .collect(Collectors.joining(", "));
        String deleteSql = "DELETE FROM dorm_buildings WHERE campus_name NOT IN (" + placeholders + ")";
        try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
            bindDormAreas(statement);
            statement.executeUpdate();
        }
    }

    private static void repairDormRoomTable(Connection connection, String catalog) throws SQLException {
        relaxNullableColumnIfPresent(connection, catalog, "dorm_rooms", "building_no", "VARCHAR(32) NULL");
        relaxNullableColumnIfPresent(connection, catalog, "dorm_rooms", "campus_name", "VARCHAR(64) NULL");
    }

    private static void repairWorkOrderTable(Connection connection, String catalog) throws SQLException {
        ensureColumnExists(connection, catalog, "work_orders", "completion_note", "VARCHAR(1000) NULL");
    }

    private static void ensureWorkOrderCompletionImagesTable(Connection connection, String catalog) throws SQLException {
        if (tableExists(connection.getMetaData(), catalog, "work_order_completion_images")) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE work_order_completion_images (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        work_order_id BIGINT NOT NULL,
                        image_url VARCHAR(255) NOT NULL,
                        sort_no INT NOT NULL DEFAULT 1,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        KEY idx_work_order_completion_images_work_order_id (work_order_id),
                        CONSTRAINT fk_work_order_completion_images_work_order FOREIGN KEY (work_order_id) REFERENCES work_orders(id)
                    )
                    """);
        }
    }

    private static void ensureAuditLogsTable(Connection connection, String catalog) throws SQLException {
        if (tableExists(connection.getMetaData(), catalog, "audit_logs")) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE audit_logs (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        operator_id BIGINT NULL,
                        action_type VARCHAR(64) NOT NULL,
                        target_type VARCHAR(64) NOT NULL,
                        target_id VARCHAR(64) NULL,
                        target_label VARCHAR(255) NULL,
                        old_value TEXT NULL,
                        new_value TEXT NULL,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        KEY idx_audit_logs_operator_id (operator_id),
                        KEY idx_audit_logs_created_at (created_at),
                        CONSTRAINT fk_audit_logs_operator FOREIGN KEY (operator_id) REFERENCES user_accounts(id)
                    )
                    """);
        }
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
                    String buildingNo = buildingIndex + BUILDING_SUFFIX;
                    statement.setString(1, dormArea);
                    statement.setString(2, buildingNo);
                    statement.setString(3, dormArea + " " + buildingNo);
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        }
    }

    private static void seedUserAccounts(Connection connection) throws SQLException {
        String insertSql = """
                INSERT INTO user_accounts (username, password_hash, display_name, phone, role_code, enabled)
                VALUES (?, ?, ?, ?, ?, 1)
                ON DUPLICATE KEY UPDATE
                    password_hash = VALUES(password_hash),
                    display_name = VALUES(display_name),
                    phone = VALUES(phone),
                    role_code = VALUES(role_code),
                    enabled = VALUES(enabled)
                """;

        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            for (SeedAccount seedAccount : SEED_ACCOUNTS) {
                statement.setString(1, seedAccount.username());
                statement.setString(2, DEFAULT_PASSWORD_HASH);
                statement.setString(3, seedAccount.displayName());
                statement.setString(4, seedAccount.phone());
                statement.setString(5, seedAccount.roleCode());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void bindDormAreas(PreparedStatement statement) throws SQLException {
        for (int index = 0; index < DORM_AREAS.length; index++) {
            statement.setString(index + 1, DORM_AREAS[index]);
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
            statement.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
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
            statement.execute("ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " " + columnDefinition);
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

    private static boolean tableExists(DatabaseMetaData metadata, String catalog, String tableName) throws SQLException {
        try (ResultSet resultSet = metadata.getTables(catalog, null, tableName, null)) {
            return resultSet.next();
        }
    }

    private record ColumnState(boolean present, boolean nullable) {
    }

    private record SeedAccount(String username, String displayName, String phone, String roleCode) {
    }
}