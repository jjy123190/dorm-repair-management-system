package com.scau.dormrepair.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.scau.dormrepair.domain.command.AssignWorkOrderCommand;
import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
import com.scau.dormrepair.domain.command.UpdateWorkOrderStatusCommand;
import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.enums.WorkOrderPriority;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import com.scau.dormrepair.domain.view.DashboardOverview;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class DashboardServiceImplTest extends UserAccountIntegrationSupport {

    @Test
    void shouldLoadWorkerDashboardFromWorkerScopedData() {
        UserAccount admin = createInternalAccount("dashboard_admin", UserRole.ADMIN, "dashboard_admin", "13855553001", "admin123");
        UserAccount workerA = createInternalAccount("dashboard_worker_a", UserRole.WORKER, "dashboard_worker_a", "13855553002", "worker123");
        UserAccount workerB = createInternalAccount("dashboard_worker_b", UserRole.WORKER, "dashboard_worker_b", "13855553003", "worker123");
        Long studentA = registerStudent("dashboard_student_a", "13855553011");
        Long studentB = registerStudent("dashboard_student_b", "13855553012");

        Long requestA = createRepairRequest(studentA, "dashboard_student_a", "13855553011", "1-BLD", "301", FaultCategory.ELECTRICITY);
        Long requestB = createRepairRequest(studentB, "dashboard_student_b", "13855553012", "2-BLD", "302", FaultCategory.WATER_PIPE);

        Long workOrderA = workOrderService.assign(new AssignWorkOrderCommand(
                requestA,
                admin.getId(),
                workerA.getId(),
                WorkOrderPriority.NORMAL,
                "Handle worker A request first."
        ));
        workOrderService.assign(new AssignWorkOrderCommand(
                requestB,
                admin.getId(),
                workerB.getId(),
                WorkOrderPriority.NORMAL,
                "Handle worker B request next."
        ));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(
                workOrderA,
                workerA.getId(),
                WorkOrderStatus.ACCEPTED,
                "Worker A accepted the request."
        ));

        DashboardOverview workerOverview = dashboardService.loadOverview(UserRole.WORKER, workerA.getId());
        assertEquals(1L, safeLong(workerOverview.getTotalRequests()));
        assertEquals(0L, safeLong(workerOverview.getPendingReviewRequests()));
        assertEquals(1L, safeLong(workerOverview.getActiveWorkOrders()));

        List<RecentRepairRequestView> recentRows = dashboardService.listRecentRepairRequests(UserRole.WORKER, workerA.getId(), 10);
        assertEquals(1, recentRows.size());
        assertEquals(requestA, recentRows.get(0).getId());
        assertEquals("dashboard_student_a", recentRows.get(0).getStudentName());
    }

    @Test
    void shouldOrderAdminRecentRowsByLatestProcessingActivity() throws SQLException {
        UserAccount admin = createInternalAccount("recent_admin", UserRole.ADMIN, "recent_admin", "13855553021", "admin123");
        UserAccount worker = createInternalAccount("recent_worker", UserRole.WORKER, "recent_worker", "13855553022", "worker123");
        Long oldStudentId = registerStudent("recent_old_student", "13855553023");
        Long newStudentId = registerStudent("recent_new_student", "13855553024");

        Long oldRequestId = createRepairRequest(oldStudentId, "recent_old_student", "13855553023", "6-BLD", "118", FaultCategory.PUBLIC_AREA);
        Long workOrderId = workOrderService.assign(new AssignWorkOrderCommand(
                oldRequestId,
                admin.getId(),
                worker.getId(),
                WorkOrderPriority.NORMAL,
                "Older request but updated most recently."
        ));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(
                workOrderId,
                worker.getId(),
                WorkOrderStatus.ACCEPTED,
                "Accepted the older request."
        ));

        Long newerRequestId = createRepairRequest(newStudentId, "recent_new_student", "13855553024", "7-BLD", "402", FaultCategory.NETWORK);

        LocalDateTime newestActivityTime = LocalDateTime.of(2098, 12, 31, 10, 0);
        LocalDateTime newerSubmissionTime = LocalDateTime.of(2098, 12, 30, 10, 0);
        LocalDateTime olderSubmissionTime = LocalDateTime.of(2098, 12, 20, 10, 0);
        LocalDateTime olderAssignmentTime = LocalDateTime.of(2098, 12, 21, 10, 0);

        updateRepairRequestSubmittedAt(oldRequestId, olderSubmissionTime);
        updateRepairRequestSubmittedAt(newerRequestId, newerSubmissionTime);
        updateWorkOrderAssignedAt(workOrderId, olderAssignmentTime);
        updateWorkOrderRecordTimes(workOrderId, newestActivityTime);

        List<RecentRepairRequestView> recentRows = dashboardService.listRecentRepairRequests(UserRole.ADMIN, admin.getId(), 10);
        assertTrue(recentRows.stream().anyMatch(item -> oldRequestId.equals(item.getId())));
        assertTrue(recentRows.stream().anyMatch(item -> newerRequestId.equals(item.getId())));
        assertEquals(oldRequestId, recentRows.get(0).getId());
    }

    private Long registerStudent(String displayName, String phone) {
        return userAccountService.registerStudent(
                uniqueUsername(displayName),
                "student123",
                "student123",
                displayName,
                phone
        );
    }

    private Long createRepairRequest(
            Long studentId,
            String studentName,
            String phone,
            String buildingNo,
            String roomNo,
            FaultCategory category
    ) {
        return repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                studentName,
                phone,
                null,
                "Taishan",
                buildingNo,
                roomNo,
                category,
                "Dashboard scope verification.",
                List.of()
        ));
    }

    private UserAccount createInternalAccount(
            String suffix,
            UserRole role,
            String displayName,
            String phone,
            String password
    ) {
        String username = uniqueUsername(suffix);
        Long accountId = userAccountService.createInternalAccount(username, password, password, displayName, phone, role);
        return userAccountService.getById(accountId);
    }

    private void updateRepairRequestSubmittedAt(Long requestId, LocalDateTime submittedAt) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE repair_requests SET submitted_at = ? WHERE id = ?"
             )) {
            statement.setTimestamp(1, Timestamp.valueOf(submittedAt));
            statement.setLong(2, requestId);
            statement.executeUpdate();
        }
    }

    private void updateWorkOrderAssignedAt(Long workOrderId, LocalDateTime assignedAt) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE work_orders SET assigned_at = ? WHERE id = ?"
             )) {
            statement.setTimestamp(1, Timestamp.valueOf(assignedAt));
            statement.setLong(2, workOrderId);
            statement.executeUpdate();
        }
    }

    private void updateWorkOrderRecordTimes(Long workOrderId, LocalDateTime recordedAt) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE work_order_records SET created_at = ? WHERE work_order_id = ?"
             )) {
            statement.setTimestamp(1, Timestamp.valueOf(recordedAt));
            statement.setLong(2, workOrderId);
            statement.executeUpdate();
        }
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }
    @Test
    void shouldExposePendingConfirmationAndReworkCounts() {
        UserAccount admin = createInternalAccount("dashboard_counts_admin", UserRole.ADMIN, "dashboard_counts_admin", "13855553041", "admin123");
        UserAccount worker = createInternalAccount("dashboard_counts_worker", UserRole.WORKER, "dashboard_counts_worker", "13855553042", "worker123");
        Long studentId = registerStudent("dashboard_counts_student", "13855553043");

        Long pendingConfirmRequestId = createRepairRequest(studentId, "dashboard_counts_student", "13855553043", "9-BLD", "401", FaultCategory.NETWORK);
        Long pendingConfirmWorkOrderId = workOrderService.assign(new AssignWorkOrderCommand(
                pendingConfirmRequestId,
                admin.getId(),
                worker.getId(),
                WorkOrderPriority.NORMAL,
                "Push to pending confirmation."
        ));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(pendingConfirmWorkOrderId, worker.getId(), WorkOrderStatus.ACCEPTED, "Accepted."));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(pendingConfirmWorkOrderId, worker.getId(), WorkOrderStatus.IN_PROGRESS, "Started."));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(
                pendingConfirmWorkOrderId,
                worker.getId(),
                WorkOrderStatus.WAITING_CONFIRMATION,
                "Finished and waiting for confirmation.",
                "Finished and waiting for confirmation.",
                List.of()
        ));

        Long reworkRequestId = createRepairRequest(studentId, "dashboard_counts_student", "13855553043", "9-BLD", "402", FaultCategory.ELECTRICITY);
        Long reworkWorkOrderId = workOrderService.assign(new AssignWorkOrderCommand(
                reworkRequestId,
                admin.getId(),
                worker.getId(),
                WorkOrderPriority.HIGH,
                "Will reopen into rework."
        ));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(reworkWorkOrderId, worker.getId(), WorkOrderStatus.ACCEPTED, "Accepted."));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(reworkWorkOrderId, worker.getId(), WorkOrderStatus.IN_PROGRESS, "Started."));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(
                reworkWorkOrderId,
                worker.getId(),
                WorkOrderStatus.WAITING_CONFIRMATION,
                "First completion.",
                "First completion.",
                List.of()
        ));
        repairRequestService.requestStudentRework(studentId, reworkRequestId, "Need another pass.");

        DashboardOverview adminOverview = dashboardService.loadOverview(UserRole.ADMIN, admin.getId());
        DashboardOverview studentOverview = dashboardService.loadOverview(UserRole.STUDENT, studentId);
        DashboardOverview workerOverview = dashboardService.loadOverview(UserRole.WORKER, worker.getId());

        assertEquals(1L, safeLong(adminOverview.getPendingConfirmationCount()));
        assertEquals(1L, safeLong(adminOverview.getReworkInProgressCount()));
        assertEquals(1L, safeLong(studentOverview.getPendingConfirmationCount()));
        assertEquals(1L, safeLong(studentOverview.getReworkInProgressCount()));
        assertEquals(1L, safeLong(workerOverview.getPendingConfirmationCount()));
        assertEquals(1L, safeLong(workerOverview.getReworkInProgressCount()));
    }
}