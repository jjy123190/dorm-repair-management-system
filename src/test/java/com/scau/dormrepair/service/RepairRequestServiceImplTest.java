package com.scau.dormrepair.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.scau.dormrepair.domain.command.AssignWorkOrderCommand;
import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
import com.scau.dormrepair.domain.command.SaveDormBuildingCommand;
import com.scau.dormrepair.domain.command.SaveDormRoomCommand;
import com.scau.dormrepair.domain.command.SubmitRepairFeedbackCommand;
import com.scau.dormrepair.domain.command.UpdateWorkOrderStatusCommand;
import com.scau.dormrepair.domain.entity.RepairRequest;
import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.entity.WorkOrder;
import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.enums.WorkOrderPriority;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import com.scau.dormrepair.domain.view.DashboardOverview;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.StudentRepairDetailView;
import com.scau.dormrepair.exception.BusinessException;
import com.scau.dormrepair.exception.ResourceNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;

class RepairRequestServiceImplTest extends UserAccountIntegrationSupport {

    @Test
    void shouldKeepStudentHistoryBoundToStudentIdAfterDisplayNameChange() throws SQLException {
        String username = uniqueUsername("history");
        String originalName = "history_student";
        String renamedDisplayName = "renamed_student";
        Long studentId = userAccountService.registerStudent(
                username,
                "student123",
                "student123",
                originalName,
                "13855551001"
        );

        Long firstRequestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                originalName,
                "13855551001",
                null,
                "Taishan",
                "1-BLD",
                "207",
                FaultCategory.ELECTRICITY,
                "Dorm light stays off and needs repair soon.",
                List.of()
        ));
        Long secondRequestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                originalName,
                "13855551001",
                null,
                "Taishan",
                "1-BLD",
                "208",
                FaultCategory.WATER_PIPE,
                "Water tap keeps leaking through the night.",
                List.of()
        ));

        updateAccountDisplayName(studentId, renamedDisplayName);

        List<RecentRepairRequestView> history = repairRequestService.listStudentSubmittedRequests(studentId, 20);
        assertEquals(2, history.size());
        assertTrue(history.stream().anyMatch(item -> firstRequestId.equals(item.getId())));
        assertTrue(history.stream().anyMatch(item -> secondRequestId.equals(item.getId())));

        StudentRepairDetailView detail = repairRequestService.getStudentRequestDetail(studentId, firstRequestId);
        assertEquals(firstRequestId, detail.getId());
        assertEquals(originalName, detail.getStudentName());

        int urgeCount = repairRequestService.urgeStudentRequest(studentId, firstRequestId);
        assertEquals(1, urgeCount);

        repairRequestService.cancelStudentRequest(studentId, secondRequestId);
        RepairRequest cancelledRequest = loadRepairRequest(secondRequestId);
        assertEquals(RepairRequestStatus.CANCELLED, cancelledRequest.getStatus());

        DashboardOverview overview = dashboardService.loadOverview(UserRole.STUDENT, studentId);
        assertEquals(2L, overview.getTotalRequests());
        assertEquals(1L, overview.getPendingReviewRequests());
        assertEquals(0L, overview.getActiveWorkOrders());

        List<RecentRepairRequestView> recent = dashboardService.listRecentRepairRequests(UserRole.STUDENT, studentId, 10);
        assertEquals(2, recent.size());
        assertTrue(recent.stream().anyMatch(item -> firstRequestId.equals(item.getId())));
        assertTrue(recent.stream().anyMatch(item -> secondRequestId.equals(item.getId())));
    }

    @Test
    void shouldRejectFeedbackSubmissionFromAnotherStudent() throws SQLException {
        UserAccount admin = createInternalAccount("feedback_admin", UserRole.ADMIN, "feedback_admin", "13855551011", "admin123");
        UserAccount worker = createInternalAccount("feedback_worker", UserRole.WORKER, "feedback_worker", "13855551012", "worker123");
        Long ownerStudentId = userAccountService.registerStudent(
                uniqueUsername("feedback_owner"),
                "student123",
                "student123",
                "owner_student",
                "13855551021"
        );
        Long otherStudentId = userAccountService.registerStudent(
                uniqueUsername("feedback_other"),
                "student123",
                "student123",
                "other_student",
                "13855551022"
        );
        Long requestId = repairRequestService.create(new CreateRepairRequestCommand(
                ownerStudentId,
                "owner_student",
                "13855551021",
                null,
                "Taishan",
                "2-BLD",
                "305",
                FaultCategory.NETWORK,
                "Dorm network port has no signal at all.",
                List.of()
        ));
        Long workOrderId = workOrderService.assign(new AssignWorkOrderCommand(
                requestId,
                admin.getId(),
                worker.getId(),
                WorkOrderPriority.NORMAL,
                "Check the wall network port."
        ));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(workOrderId, worker.getId(), WorkOrderStatus.ACCEPTED, "Accepted."));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(workOrderId, worker.getId(), WorkOrderStatus.IN_PROGRESS, "Diagnosed on site."));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(
                workOrderId,
                worker.getId(),
                WorkOrderStatus.WAITING_CONFIRMATION,
                "Recovered the network line.",
                "Recovered the network line.",
                List.of()
        ));
        repairRequestService.confirmStudentCompletion(ownerStudentId, requestId);

        assertThrows(
                ResourceNotFoundException.class,
                () -> repairRequestService.submitFeedback(new SubmitRepairFeedbackCommand(
                        otherStudentId,
                        requestId,
                        5,
                        "Not my repair request.",
                        false
                ))
        );

        repairRequestService.submitFeedback(new SubmitRepairFeedbackCommand(
                ownerStudentId,
                requestId,
                5,
                "Issue resolved well.",
                false
        ));

        StudentRepairDetailView detail = repairRequestService.getStudentRequestDetail(ownerStudentId, requestId);
        assertTrue(detail.hasFeedback());
        assertEquals(5, detail.getFeedbackRating());
        assertEquals(5, detail.getRecords().size());
        assertEquals(WorkOrderStatus.ASSIGNED, detail.getRecords().get(0).getStatus());
        assertEquals(WorkOrderStatus.ACCEPTED, detail.getRecords().get(1).getStatus());
        assertEquals(WorkOrderStatus.IN_PROGRESS, detail.getRecords().get(2).getStatus());
        assertEquals(WorkOrderStatus.WAITING_CONFIRMATION, detail.getRecords().get(3).getStatus());
        assertEquals(WorkOrderStatus.COMPLETED, detail.getRecords().get(4).getStatus());
    }

    @Test
    void shouldCancelAssignedRepairAndCloseLinkedWorkOrder() throws SQLException {
        UserAccount admin = createInternalAccount("cancel_admin", UserRole.ADMIN, "dispatch_admin", "13855551031", "admin123");
        UserAccount worker = createInternalAccount("cancel_worker", UserRole.WORKER, "dispatch_worker", "13855551032", "worker123");
        Long studentId = userAccountService.registerStudent(
                uniqueUsername("cancel_student"),
                "student123",
                "student123",
                "cancel_student",
                "13855551033"
        );
        Long requestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "cancel_student",
                "13855551033",
                null,
                "Taishan",
                "6-BLD",
                "118",
                FaultCategory.PUBLIC_AREA,
                "Public corridor lamp is broken and needs a visit.",
                List.of()
        ));
        Long workOrderId = workOrderService.assign(new AssignWorkOrderCommand(
                requestId,
                admin.getId(),
                worker.getId(),
                WorkOrderPriority.NORMAL,
                "Visit this room before noon."
        ));

        repairRequestService.cancelStudentRequest(studentId, requestId);

        RepairRequest repairRequest = loadRepairRequest(requestId);
        WorkOrder workOrder = loadWorkOrder(workOrderId);
        assertEquals(RepairRequestStatus.CANCELLED, repairRequest.getStatus());
        assertEquals(WorkOrderStatus.CANCELLED, workOrder.getStatus());
        assertEquals(2, countWorkOrderRecords(workOrderId));
        assertEquals(1, countWorkOrderRecordsByStatus(workOrderId, WorkOrderStatus.CANCELLED.name()));
    }

    @Test
    void shouldReopenSameRequestWhenStudentRequestsRework() throws SQLException {
        UserAccount admin = createInternalAccount("rework_admin", UserRole.ADMIN, "rework_admin", "13855551041", "admin123");
        UserAccount worker = createInternalAccount("rework_worker", UserRole.WORKER, "rework_worker", "13855551042", "worker123");
        Long studentId = userAccountService.registerStudent(
                uniqueUsername("rework_student"),
                "student123",
                "student123",
                "rework_student",
                "13855551043"
        );
        Long requestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "rework_student",
                "13855551043",
                null,
                "Taishan",
                "7-BLD",
                "302",
                FaultCategory.ELECTRICITY,
                "Power outlet still sparks at night.",
                List.of()
        ));
        Long workOrderId = workOrderService.assign(new AssignWorkOrderCommand(
                requestId,
                admin.getId(),
                worker.getId(),
                WorkOrderPriority.HIGH,
                "Check the socket and breaker."
        ));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(workOrderId, worker.getId(), WorkOrderStatus.ACCEPTED, "Accepted."));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(workOrderId, worker.getId(), WorkOrderStatus.IN_PROGRESS, "Started inspection."));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(
                workOrderId,
                worker.getId(),
                WorkOrderStatus.WAITING_CONFIRMATION,
                "Replaced the faulty socket.",
                "Replaced the faulty socket.",
                List.of()
        ));

        repairRequestService.requestStudentRework(studentId, requestId, "The socket still sparks occasionally.");

        RepairRequest repairRequest = loadRepairRequest(requestId);
        WorkOrder workOrder = loadWorkOrder(workOrderId);
        assertEquals(RepairRequestStatus.REWORK_IN_PROGRESS, repairRequest.getStatus());
        assertEquals(WorkOrderStatus.IN_PROGRESS, workOrder.getStatus());
        assertEquals(5, countWorkOrderRecords(workOrderId));

        StudentRepairDetailView detail = repairRequestService.getStudentRequestDetail(studentId, requestId);
        assertEquals(requestId, detail.getId());
        assertEquals(5, detail.getRecords().size());
        assertEquals(WorkOrderStatus.IN_PROGRESS, detail.getRecords().get(4).getStatus());
        assertTrue(detail.getRecords().get(4).getRecordNote().contains("返修"));
    }

    @Test
    void shouldRejectCreatingRepairRequestForInactiveDormRoom() {
        String campusName = uniqueUsername("area");
        String buildingNo = uniqueUsername("bld");
        Long buildingId = dormCatalogService.saveBuilding(new SaveDormBuildingCommand(
                null,
                campusName,
                buildingNo,
                campusName + " " + buildingNo
        ));
        String roomNo = uniqueRoomNo("room");
        Long roomId = dormCatalogService.saveRoom(new SaveDormRoomCommand(
                null,
                buildingId,
                roomNo,
                4,
                6,
                "INACTIVE"
        ));
        Long studentId = userAccountService.registerStudent(
                uniqueUsername("inactive_room_student"),
                "student123",
                "student123",
                "inactive_room_student",
                "13855551061"
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> repairRequestService.create(new CreateRepairRequestCommand(
                        studentId,
                        "inactive_room_student",
                        "13855551061",
                        roomId,
                        campusName,
                        buildingNo,
                        roomNo,
                        FaultCategory.ELECTRICITY,
                        "Dorm light is still broken and the room has been disabled.",
                        List.of()
                ))
        );
        assertEquals("所选房间已停用，请重新选择可报修房间。", exception.getMessage());
    }

    @Test
    void shouldAllowStudentToAppendImagesForOwnOpenRequest() throws SQLException {
        Long studentId = userAccountService.registerStudent(
                uniqueUsername("append_images_student"),
                "student123",
                "student123",
                "append_images_student",
                "13855551071"
        );

        Long requestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "append_images_student",
                "13855551071",
                null,
                "Taishan",
                "8-BLD",
                "416",
                FaultCategory.ELECTRICITY,
                "The ceiling light keeps flickering in the dorm.",
                List.of("pics/original-a.png", "pics/original-b.png")
        ));

        int totalImages = repairRequestService.appendStudentRequestImages(
                studentId,
                requestId,
                List.of("pics/extra-a.png", "pics/extra-b.png")
        );

        assertEquals(4, totalImages);
        assertEquals(4, countRepairRequestImages(requestId));

        StudentRepairDetailView detail = repairRequestService.getStudentRequestDetail(studentId, requestId);
        assertEquals(4, detail.getImageUrls().size());
        assertTrue(detail.getImageUrls().contains("pics/extra-a.png"));
        assertTrue(detail.getImageUrls().contains("pics/extra-b.png"));
    }

    @Test
    void shouldRejectAppendingImagesBeyondLimit() {
        Long studentId = userAccountService.registerStudent(
                uniqueUsername("append_limit_student"),
                "student123",
                "student123",
                "append_limit_student",
                "13855551072"
        );

        Long requestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "append_limit_student",
                "13855551072",
                null,
                "Taishan",
                "9-BLD",
                "512",
                FaultCategory.WATER_PIPE,
                "The water pipe keeps dripping and needs urgent repair.",
                List.of("pics/original-a.png", "pics/original-b.png", "pics/original-c.png")
        ));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> repairRequestService.appendStudentRequestImages(
                        studentId,
                        requestId,
                        List.of("pics/extra-a.png", "pics/extra-b.png", "pics/extra-c.png")
                )
        );

        assertEquals("当前报修最多保留 5 张图片，请减少后再提交。", exception.getMessage());
    }

    @Test
    void shouldRejectAppendingImagesToCompletedRequest() throws SQLException {
        UserAccount admin = createInternalAccount("append_completed_admin", UserRole.ADMIN, "append_completed_admin", "13855551074", "admin123");
        UserAccount worker = createInternalAccount("append_completed_worker", UserRole.WORKER, "append_completed_worker", "13855551075", "worker123");
        Long studentId = userAccountService.registerStudent(
                uniqueUsername("append_completed_student"),
                "student123",
                "student123",
                "append_completed_student",
                "13855551076"
        );

        Long requestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "append_completed_student",
                "13855551076",
                null,
                "Taishan",
                "11-BLD",
                "602",
                FaultCategory.ELECTRICITY,
                "The desk lamp outlet keeps tripping after repair.",
                List.of("pics/original-a.png")
        ));
        Long workOrderId = workOrderService.assign(new AssignWorkOrderCommand(
                requestId,
                admin.getId(),
                worker.getId(),
                WorkOrderPriority.NORMAL,
                "Check the outlet and confirm stability."
        ));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(workOrderId, worker.getId(), WorkOrderStatus.ACCEPTED, "Accepted."));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(workOrderId, worker.getId(), WorkOrderStatus.IN_PROGRESS, "Started fixing."));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(
                workOrderId,
                worker.getId(),
                WorkOrderStatus.WAITING_CONFIRMATION,
                "Finished fixing.",
                "Finished fixing.",
                List.of()
        ));
        repairRequestService.confirmStudentCompletion(studentId, requestId);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> repairRequestService.appendStudentRequestImages(
                        studentId,
                        requestId,
                        List.of("pics/extra-a.png")
                )
        );

        assertEquals("当前报修已结束，不能继续补充图片。", exception.getMessage());
    }

    @Test
    void shouldReturnMoreThanTwentyStudentRecordsWhenRequested() {
        Long studentId = userAccountService.registerStudent(
                uniqueUsername("history_many_student"),
                "student123",
                "student123",
                "history_many_student",
                "13855551073"
        );

        for (int index = 0; index < 25; index++) {
            repairRequestService.create(new CreateRepairRequestCommand(
                    studentId,
                    "history_many_student",
                    "13855551073",
                    null,
                    "Taishan",
                    "10-BLD",
                    "6" + String.format("%02d", index),
                    FaultCategory.OTHER,
                    "History case " + index + " needs to remain visible in the student list.",
                    List.of()
            ));
        }

        List<RecentRepairRequestView> history = repairRequestService.listStudentSubmittedRequests(studentId, 100);
        assertEquals(25, history.size());
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

    private String uniqueRoomNo(String suffix) {
        String raw = uniqueUsername(suffix).replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return raw.length() > 12 ? raw.substring(0, 12) : raw;
    }

    private int countWorkOrderRecordsByStatus(Long workOrderId, String status) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM work_order_records WHERE work_order_id = ? AND status = ?"
             )) {
            statement.setLong(1, workOrderId);
            statement.setString(2, status);
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0;
                }
                return resultSet.getInt(1);
            }
        }
    }

    private int countRepairRequestImages(Long requestId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM repair_request_images WHERE repair_request_id = ?"
             )) {
            statement.setLong(1, requestId);
            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return 0;
                }
                return resultSet.getInt(1);
            }
        }
    }
}
