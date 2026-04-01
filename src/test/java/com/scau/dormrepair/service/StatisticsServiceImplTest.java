package com.scau.dormrepair.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.scau.dormrepair.domain.command.AssignWorkOrderCommand;
import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.view.FaultCategorySummary;
import com.scau.dormrepair.domain.view.MonthlyRepairSummary;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StatisticsServiceImplTest extends UserAccountIntegrationSupport {

    @Test
    void shouldAggregateMonthlyAndCategorySummary() throws SQLException {
        Long studentId = userAccountService.registerStudent(
                uniqueUsername("stats_student"),
                "student123",
                "student123",
                "stats_student",
                "13855552001"
        );

        Long completedRequestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "stats_student",
                "13855552001",
                null,
                "Taishan",
                "3-BLD",
                "201",
                FaultCategory.ELECTRICITY,
                "Lamp is broken.",
                List.of()
        ));
        repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "stats_student",
                "13855552001",
                null,
                "Taishan",
                "3-BLD",
                "202",
                FaultCategory.WATER_PIPE,
                "Water pipe is leaking.",
                List.of()
        ));
        markRepairRequestCompleted(completedRequestId);

        List<MonthlyRepairSummary> monthlyRows = statisticsService.listMonthlySummary(6);
        assertFalse(monthlyRows.isEmpty());
        assertTrue(monthlyRows.stream().anyMatch(item -> safeLong(item.getTotalRequests()) >= 2));
        assertTrue(monthlyRows.stream().anyMatch(item -> safeLong(item.getCompletedRequests()) >= 1));

        List<FaultCategorySummary> categoryRows = statisticsService.listFaultCategorySummary(6);
        assertFalse(categoryRows.isEmpty());
        assertTrue(findCategoryTotal(categoryRows, FaultCategory.ELECTRICITY) >= 1L);
        assertTrue(findCategoryTotal(categoryRows, FaultCategory.WATER_PIPE) >= 1L);
    }

    @Test
    void shouldUseNaturalMonthWindowForMonthlyAndCategorySummary() throws SQLException {
        Long studentId = userAccountService.registerStudent(
                uniqueUsername("stats_boundary"),
                "student123",
                "student123",
                "stats_boundary",
                "13855552031"
        );

        Long currentMonthRequestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "stats_boundary",
                "13855552031",
                null,
                "Taishan",
                "4-BLD",
                "301",
                FaultCategory.ELECTRICITY,
                "Current-month repair request.",
                List.of()
        ));
        Long oldestIncludedRequestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "stats_boundary",
                "13855552031",
                null,
                "Taishan",
                "4-BLD",
                "302",
                FaultCategory.WATER_PIPE,
                "Oldest included natural month request.",
                List.of()
        ));
        Long excludedRequestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "stats_boundary",
                "13855552031",
                null,
                "Taishan",
                "4-BLD",
                "303",
                FaultCategory.NETWORK,
                "Excluded seventh month request.",
                List.of()
        ));

        LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);
        LocalDateTime currentMonthTime = currentMonthStart.plusDays(2).atTime(10, 0);
        LocalDateTime oldestIncludedTime = currentMonthStart.minusMonths(5).plusDays(1).atTime(10, 0);
        LocalDateTime excludedTime = currentMonthStart.minusMonths(5).minusDays(1).atTime(10, 0);

        updateSubmittedAt(currentMonthRequestId, currentMonthTime);
        updateSubmittedAt(oldestIncludedRequestId, oldestIncludedTime);
        updateSubmittedAt(excludedRequestId, excludedTime);

        List<MonthlyRepairSummary> monthlyRows = statisticsService.listMonthlySummary(6);
        List<FaultCategorySummary> categoryRows = statisticsService.listFaultCategorySummary(6);

        String excludedMonthLabel = currentMonthStart.minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Set<String> expectedMonthLabels = expectedMonthLabels(currentMonthStart, 6);

        assertTrue(monthlyRows.stream().map(MonthlyRepairSummary::getMonthLabel).allMatch(expectedMonthLabels::contains));
        assertTrue(monthlyRows.stream().noneMatch(item -> excludedMonthLabel.equals(item.getMonthLabel())));

        long expectedTotal = countRepairRequestsSince(currentMonthStart.minusMonths(5));
        long monthlyTotal = monthlyRows.stream()
                .map(MonthlyRepairSummary::getTotalRequests)
                .mapToLong(this::safeLong)
                .sum();
        long categoryTotal = categoryRows.stream()
                .map(FaultCategorySummary::getTotalRequests)
                .mapToLong(this::safeLong)
                .sum();

        assertTrue(expectedTotal >= 2L);
        assertTrue(monthlyTotal >= 2L);
        assertTrue(categoryTotal >= 2L);
        assertTrue(monthlyTotal == expectedTotal && categoryTotal == expectedTotal);
    }

    private void markRepairRequestCompleted(Long requestId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE repair_requests SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP WHERE id = ?"
             )) {
            statement.setLong(1, requestId);
            statement.executeUpdate();
        }
    }

    private void updateSubmittedAt(Long requestId, LocalDateTime submittedAt) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE repair_requests SET submitted_at = ? WHERE id = ?"
             )) {
            statement.setTimestamp(1, Timestamp.valueOf(submittedAt));
            statement.setLong(2, requestId);
            statement.executeUpdate();
        }
    }

    private long countRepairRequestsSince(LocalDate startDate) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM repair_requests WHERE submitted_at >= ?"
             )) {
            statement.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0L;
                }
                return resultSet.getLong(1);
            }
        }
    }

    private Set<String> expectedMonthLabels(LocalDate currentMonthStart, int recentMonths) {
        Set<String> labels = new LinkedHashSet<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int offset = 0; offset < recentMonths; offset++) {
            labels.add(currentMonthStart.minusMonths(offset).format(formatter));
        }
        return labels;
    }

    private long findCategoryTotal(List<FaultCategorySummary> rows, FaultCategory category) {
        return rows.stream()
                .filter(item -> item.getFaultCategory() == category)
                .map(FaultCategorySummary::getTotalRequests)
                .mapToLong(this::safeLong)
                .sum();
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }
    @Test
    void shouldReturnDrillDownRequestDetailsForDormBuildingCategoryAndTimeoutStage() throws SQLException {
        Long adminId = userAccountService.createInternalAccount(
                uniqueUsername("stats_drill_admin"),
                "admin123",
                "admin123",
                "stats_drill_admin",
                "13855552061",
                com.scau.dormrepair.domain.enums.UserRole.ADMIN
        );
        Long workerId = userAccountService.createInternalAccount(
                uniqueUsername("stats_drill_worker"),
                "worker123",
                "worker123",
                "stats_drill_worker",
                "13855552062",
                com.scau.dormrepair.domain.enums.UserRole.WORKER
        );
        Long studentId = userAccountService.registerStudent(
                uniqueUsername("stats_drill_student"),
                "student123",
                "student123",
                "stats_drill_student",
                "13855552063"
        );

        Long requestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "stats_drill_student",
                "13855552063",
                null,
                "Taishan",
                "8-BLD",
                "512",
                FaultCategory.NETWORK,
                "Need drill-down details for statistics.",
                List.of()
        ));
        Long workOrderId = workOrderService.assign(new AssignWorkOrderCommand(
                requestId,
                adminId,
                workerId,
                com.scau.dormrepair.domain.enums.WorkOrderPriority.NORMAL,
                "Drill-down timeout sample."
        ));
        updateSubmittedAt(requestId, LocalDateTime.now().minusHours(30));
        updateAssignedAt(workOrderId, LocalDateTime.now().minusHours(12));

        List<RecentRepairRequestView> dormRows = statisticsService.listDormBuildingRequestDetails(6, "Taishan", "8-BLD", 20);
        List<RecentRepairRequestView> categoryRows = statisticsService.listFaultCategoryRequestDetails(6, FaultCategory.NETWORK, 20);
        List<RecentRepairRequestView> timeoutRows = statisticsService.listTimeoutStageRequestDetails("PENDING_ACCEPT", 20);

        assertTrue(dormRows.stream().anyMatch(item -> requestId.equals(item.getId())));
        assertTrue(categoryRows.stream().anyMatch(item -> requestId.equals(item.getId())));
        assertTrue(timeoutRows.stream().anyMatch(item -> requestId.equals(item.getId())));
    }

    private void updateAssignedAt(Long workOrderId, LocalDateTime assignedAt) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE work_orders SET assigned_at = ? WHERE id = ?"
             )) {
            statement.setTimestamp(1, Timestamp.valueOf(assignedAt));
            statement.setLong(2, workOrderId);
            statement.executeUpdate();
        }
    }
}