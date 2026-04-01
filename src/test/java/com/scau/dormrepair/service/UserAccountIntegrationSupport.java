package com.scau.dormrepair.service;

import com.scau.dormrepair.common.MyBatisExecutor;
import com.scau.dormrepair.config.AppProperties;
import com.scau.dormrepair.config.DatabaseConfig;
import com.scau.dormrepair.config.SchemaCompatibilitySupport;
import com.scau.dormrepair.domain.entity.RepairRequest;
import com.scau.dormrepair.domain.entity.WorkOrder;
import com.scau.dormrepair.mapper.RepairRequestMapper;
import com.scau.dormrepair.mapper.WorkOrderMapper;
import com.scau.dormrepair.service.impl.AuditLogServiceImpl;
import com.scau.dormrepair.service.impl.DashboardServiceImpl;
import com.scau.dormrepair.service.impl.DormCatalogServiceImpl;
import com.scau.dormrepair.service.impl.RepairRequestServiceImpl;
import com.scau.dormrepair.service.impl.StatisticsServiceImpl;
import com.scau.dormrepair.service.impl.UserAccountServiceImpl;
import com.scau.dormrepair.service.impl.WorkOrderServiceImpl;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

public abstract class UserAccountIntegrationSupport {

    protected static final String TEST_PREFIX = "ct_";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMddHHmmssSSS");

    protected static HikariDataSource dataSource;
    protected static MyBatisExecutor executor;
    protected static UserAccountService userAccountService;
    protected static RepairRequestService repairRequestService;
    protected static WorkOrderService workOrderService;
    protected static DashboardService dashboardService;
    protected static StatisticsService statisticsService;
    protected static AuditLogService auditLogService;
    protected static DormCatalogService dormCatalogService;

    @BeforeAll
    static void setUpInfrastructure() {
        AppProperties properties = AppProperties.load();
        dataSource = DatabaseConfig.createDataSource(properties);
        SchemaCompatibilitySupport.repair(dataSource);
        SqlSessionFactory sqlSessionFactory = DatabaseConfig.createSqlSessionFactory(dataSource);
        executor = new MyBatisExecutor(sqlSessionFactory);
        userAccountService = new UserAccountServiceImpl(executor);
        repairRequestService = new RepairRequestServiceImpl(executor);
        workOrderService = new WorkOrderServiceImpl(executor);
        dashboardService = new DashboardServiceImpl(executor);
        statisticsService = new StatisticsServiceImpl(executor);
        auditLogService = new AuditLogServiceImpl(executor);
        dormCatalogService = new DormCatalogServiceImpl(executor);
    }

    @AfterEach
    void cleanTestData() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            List<Long> accountIds = selectIdsByParams(
                    connection,
                    "SELECT id FROM user_accounts WHERE username LIKE ?",
                    TEST_PREFIX + "%"
            );
            List<Long> buildingIds = selectIdsByParams(
                    connection,
                    "SELECT id FROM dorm_buildings WHERE campus_name LIKE ? OR building_no LIKE ? OR building_name LIKE ?",
                    TEST_PREFIX + "%",
                    TEST_PREFIX + "%",
                    TEST_PREFIX + "%"
            );
            List<Long> roomIds = mergeIds(
                    selectIdsByParams(connection, "SELECT id FROM dorm_rooms WHERE room_no LIKE ?", TEST_PREFIX + "%"),
                    selectIdsByLongs(connection, "SELECT id FROM dorm_rooms WHERE building_id IN (%s)", buildingIds)
            );

            List<Long> repairRequestIds = mergeIds(
                    selectIdsByLongs(connection, "SELECT id FROM repair_requests WHERE student_id IN (%s)", accountIds),
                    selectIdsByLongs(connection, "SELECT id FROM repair_requests WHERE dorm_room_id IN (%s)", roomIds)
            );
            List<Long> workOrderIds = mergeIds(
                    selectIdsByLongs(connection, "SELECT id FROM work_orders WHERE repair_request_id IN (%s)", repairRequestIds),
                    selectIdsByLongs(connection, "SELECT id FROM work_orders WHERE admin_id IN (%s)", accountIds),
                    selectIdsByLongs(connection, "SELECT id FROM work_orders WHERE worker_id IN (%s)", accountIds)
            );

            deleteByIds(connection, "work_order_completion_images", "work_order_id", workOrderIds);
            deleteByIds(connection, "work_order_records", "work_order_id", workOrderIds);
            deleteByIds(connection, "repair_feedbacks", "repair_request_id", repairRequestIds);
            deleteByIds(connection, "repair_request_images", "repair_request_id", repairRequestIds);
            deleteByIds(connection, "work_orders", "id", workOrderIds);
            deleteByIds(connection, "repair_requests", "id", repairRequestIds);
            deleteByIds(connection, "audit_logs", "operator_id", accountIds);
            deleteByIds(connection, "dorm_rooms", "id", roomIds);
            deleteByIds(connection, "dorm_buildings", "id", buildingIds);
            deleteByIds(connection, "user_accounts", "id", accountIds);
        }
    }

    @AfterAll
    static void closeInfrastructure() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    protected static String uniqueUsername(String suffix) {
        String normalizedSuffix = suffix == null ? "acct" : suffix.replaceAll("[^A-Za-z0-9_]", "");
        if (normalizedSuffix.isBlank()) {
            normalizedSuffix = "acct";
        }
        if (normalizedSuffix.length() > 12) {
            normalizedSuffix = normalizedSuffix.substring(0, 12);
        }
        return TEST_PREFIX + normalizedSuffix + "_" + FORMATTER.format(LocalDateTime.now());
    }

    protected void updateAccountDisplayName(Long accountId, String displayName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE user_accounts SET display_name = ? WHERE id = ?"
             )) {
            statement.setString(1, displayName);
            statement.setLong(2, accountId);
            statement.executeUpdate();
        }
    }

    protected RepairRequest loadRepairRequest(Long requestId) {
        return executor.executeRead(session -> session.getMapper(RepairRequestMapper.class).selectById(requestId));
    }

    protected WorkOrder loadWorkOrder(Long workOrderId) {
        return executor.executeRead(session -> session.getMapper(WorkOrderMapper.class).selectById(workOrderId));
    }

    protected int countWorkOrderRecords(Long workOrderId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM work_order_records WHERE work_order_id = ?"
             )) {
            statement.setLong(1, workOrderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0;
                }
                return resultSet.getInt(1);
            }
        }
    }

    private List<Long> selectIdsByParams(Connection connection, String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < params.length; index++) {
                Object value = params[index];
                if (value instanceof Long longValue) {
                    statement.setLong(index + 1, longValue);
                } else {
                    statement.setString(index + 1, String.valueOf(value));
                }
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Long> ids = new ArrayList<>();
                while (resultSet.next()) {
                    ids.add(resultSet.getLong(1));
                }
                return ids;
            }
        }
    }

    private List<Long> selectIdsByLongs(Connection connection, String sqlTemplate, List<Long> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String sql = String.format(sqlTemplate, placeholders(ids.size()));
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < ids.size(); index++) {
                statement.setLong(index + 1, ids.get(index));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Long> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(resultSet.getLong(1));
                }
                return results;
            }
        }
    }

    private void deleteByIds(Connection connection, String table, String column, List<Long> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        String sql = "DELETE FROM " + table + " WHERE " + column + " IN (" + placeholders(ids.size()) + ")";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < ids.size(); index++) {
                statement.setLong(index + 1, ids.get(index));
            }
            statement.executeUpdate();
        }
    }

    @SafeVarargs
    private final List<Long> mergeIds(List<Long>... groups) {
        Set<Long> merged = new LinkedHashSet<>();
        for (List<Long> group : groups) {
            if (group != null) {
                merged.addAll(group);
            }
        }
        return new ArrayList<>(merged);
    }

    private String placeholders(int count) {
        return String.join(",", java.util.Collections.nCopies(count, "?"));
    }
}