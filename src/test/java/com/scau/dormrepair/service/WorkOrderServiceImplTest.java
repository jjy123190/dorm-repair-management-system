package com.scau.dormrepair.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.scau.dormrepair.domain.command.AssignWorkOrderCommand;
import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
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
import com.scau.dormrepair.domain.view.ActiveWorkOrderView;
import com.scau.dormrepair.domain.view.WorkOrderDetailView;
import com.scau.dormrepair.exception.BusinessException;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;

class WorkOrderServiceImplTest extends UserAccountIntegrationSupport {

    @Test
    void shouldAssignWorkOrderAndExposeItToWorkerQueue() throws SQLException {
        UserAccount admin = createInternalAccount("dispatch_admin", UserRole.ADMIN, "dispatch_admin", "13855552001", "admin123");
        UserAccount worker = createInternalAccount("dispatch_worker", UserRole.WORKER, "repair_worker", "13855552002", "worker123");
        Long studentId = userAccountService.registerStudent(
                uniqueUsername("dispatch_student"),
                "student123",
                "student123",
                "student_owner",
                "13855552003"
        );
        Long repairRequestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "student_owner",
                "13855552003",
                null,
                "Yanshan",
                "2-BLD",
                "305",
                FaultCategory.DOOR_WINDOW,
                "Door lock is loose and cannot close properly.",
                List.of()
        ));

        Long workOrderId = workOrderService.assign(new AssignWorkOrderCommand(
                repairRequestId,
                admin.getId(),
                worker.getId(),
                WorkOrderPriority.NORMAL,
                "Inspect this room before tonight."
        ));

        WorkOrder workOrder = loadWorkOrder(workOrderId);
        assertEquals(WorkOrderStatus.ASSIGNED, workOrder.getStatus());

        RepairRequest repairRequest = loadRepairRequest(repairRequestId);
        assertEquals(RepairRequestStatus.ASSIGNED, repairRequest.getStatus());
        assertEquals(1, countWorkOrderRecords(workOrderId));

        List<ActiveWorkOrderView> workerOrders = workOrderService.listWorkerActiveWorkOrders(worker.getId(), 10);
        ActiveWorkOrderView workerOrder = workerOrders.stream()
                .filter(item -> workOrderId.equals(item.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("student_owner", workerOrder.getStudentName());
        assertEquals("Yanshan 2-BLD-305", workerOrder.getLocationText());
        assertEquals(FaultCategory.DOOR_WINDOW, workerOrder.getFaultCategory());
        assertEquals("Inspect this room before tonight.", workerOrder.getAssignmentNote());

        List<WorkOrderDetailView> trackedOrders = workOrderService.listAdminTrackedWorkOrders(10);
        assertTrue(trackedOrders.stream().anyMatch(item -> workOrderId.equals(item.getId())));
    }

    @Test
    void shouldRejectUnauthorizedOrIllegalStatusTransitions() throws SQLException {
        UserAccount admin = createInternalAccount("status_admin", UserRole.ADMIN, "status_admin", "13855552011", "admin123");
        UserAccount ownerWorker = createInternalAccount("status_owner", UserRole.WORKER, "owner_worker", "13855552012", "worker123");
        UserAccount otherWorker = createInternalAccount("status_other", UserRole.WORKER, "other_worker", "13855552013", "worker123");
        Long studentId = userAccountService.registerStudent(
                uniqueUsername("status_student"),
                "student123",
                "student123",
                "status_student",
                "13855552014"
        );
        Long repairRequestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "status_student",
                "13855552014",
                null,
                "Taishan",
                "3-BLD",
                "410",
                FaultCategory.NETWORK,
                "Dorm network port has been offline for the whole day.",
                List.of()
        ));
        Long workOrderId = workOrderService.assign(new AssignWorkOrderCommand(
                repairRequestId,
                admin.getId(),
                ownerWorker.getId(),
                WorkOrderPriority.HIGH,
                "Prioritize the network diagnosis."
        ));

        BusinessException unauthorized = assertThrows(
                BusinessException.class,
                () -> workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(
                        workOrderId,
                        otherWorker.getId(),
                        WorkOrderStatus.ACCEPTED,
                        "Try to accept someone else's order"
                ))
        );
        assertEquals("当前账号无权更新这张工单。", unauthorized.getMessage());

        BusinessException viewUnauthorized = assertThrows(
                BusinessException.class,
                () -> workOrderService.getWorkerWorkOrderDetail(otherWorker.getId(), workOrderId)
        );
        assertEquals("当前账号无权查看这张工单。", viewUnauthorized.getMessage());

        BusinessException illegalJump = assertThrows(
                BusinessException.class,
                () -> workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(
                        workOrderId,
                        ownerWorker.getId(),
                        WorkOrderStatus.COMPLETED,
                        "Try to finish directly"
                ))
        );
        assertEquals("当前工单状态不允许直接流转到目标状态。", illegalJump.getMessage());

        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(workOrderId, ownerWorker.getId(), WorkOrderStatus.ACCEPTED, "Accepted."));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(workOrderId, ownerWorker.getId(), WorkOrderStatus.IN_PROGRESS, "On site now."));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(
                workOrderId,
                ownerWorker.getId(),
                WorkOrderStatus.WAITING_CONFIRMATION,
                "Issue fixed.",
                "Issue fixed.",
                List.of()
        ));

        WorkOrder waitingConfirmOrder = loadWorkOrder(workOrderId);
        assertEquals(WorkOrderStatus.WAITING_CONFIRMATION, waitingConfirmOrder.getStatus());
        assertNotNull(waitingConfirmOrder.getAcceptedAt());
        assertNotNull(waitingConfirmOrder.getCompletedAt());
        assertEquals("Issue fixed.", waitingConfirmOrder.getCompletionNote());

        RepairRequest pendingRequest = loadRepairRequest(repairRequestId);
        assertEquals(RepairRequestStatus.PENDING_CONFIRMATION, pendingRequest.getStatus());
        assertEquals(4, countWorkOrderRecords(workOrderId));

        repairRequestService.confirmStudentCompletion(studentId, repairRequestId);

        WorkOrder finishedWorkOrder = loadWorkOrder(workOrderId);
        assertEquals(WorkOrderStatus.COMPLETED, finishedWorkOrder.getStatus());
        assertNotNull(finishedWorkOrder.getAcceptedAt());
        assertNotNull(finishedWorkOrder.getCompletedAt());

        RepairRequest completedRequest = loadRepairRequest(repairRequestId);
        assertEquals(RepairRequestStatus.COMPLETED, completedRequest.getStatus());
        assertEquals(5, countWorkOrderRecords(workOrderId));

        BusinessException finishedOrderError = assertThrows(
                BusinessException.class,
                () -> workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(
                        workOrderId,
                        ownerWorker.getId(),
                        WorkOrderStatus.IN_PROGRESS,
                        "Try to roll back a finished order"
                ))
        );
        assertEquals("当前工单已结束，不能再回退或重复更新。", finishedOrderError.getMessage());
    }

    @Test
    void shouldExposeTrackedDetailTimelineAndFeedback() throws SQLException {
        UserAccount admin = createInternalAccount("track_admin", UserRole.ADMIN, "track_admin", "13855552031", "admin123");
        UserAccount worker = createInternalAccount("track_worker", UserRole.WORKER, "track_worker", "13855552032", "worker123");
        Long studentId = userAccountService.registerStudent(
                uniqueUsername("track_student"),
                "student123",
                "student123",
                "track_student",
                "13855552033"
        );
        Long requestId = repairRequestService.create(new CreateRepairRequestCommand(
                studentId,
                "track_student",
                "13855552033",
                null,
                "Taishan",
                "6-BLD",
                "118",
                FaultCategory.PUBLIC_AREA,
                "Public corridor lamp flickers every night.",
                List.of()
        ));
        Long workOrderId = workOrderService.assign(new AssignWorkOrderCommand(
                requestId,
                admin.getId(),
                worker.getId(),
                WorkOrderPriority.HIGH,
                "Handle this shared-area repair first."
        ));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(workOrderId, worker.getId(), WorkOrderStatus.ACCEPTED, "Accepted by worker."));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(workOrderId, worker.getId(), WorkOrderStatus.IN_PROGRESS, "Started repair work."));
        workOrderService.updateStatus(new UpdateWorkOrderStatusCommand(
                workOrderId,
                worker.getId(),
                WorkOrderStatus.WAITING_CONFIRMATION,
                "Issue solved on site.",
                "Issue solved on site.",
                List.of("pics/completion-proof-1.png")
        ));
        repairRequestService.confirmStudentCompletion(studentId, requestId);
        repairRequestService.submitFeedback(new SubmitRepairFeedbackCommand(
                studentId,
                requestId,
                4,
                "The repair was fast and effective.",
                true
        ));

        WorkOrderDetailView adminDetail = workOrderService.getAdminWorkOrderDetail(workOrderId);
        assertEquals(workOrderId, adminDetail.getId());
        assertEquals("track_student", adminDetail.getStudentName());
        assertEquals(4, adminDetail.getFeedbackRating());
        assertEquals("The repair was fast and effective.", adminDetail.getFeedbackComment());
        assertTrue(Boolean.TRUE.equals(adminDetail.getFeedbackAnonymousFlag()));
        assertEquals("Issue solved on site.", adminDetail.getCompletionNote());
        assertEquals(List.of("pics/completion-proof-1.png"), adminDetail.getCompletionImageUrls());
        assertEquals(5, adminDetail.getRecords().size());
        assertEquals(WorkOrderStatus.ASSIGNED, adminDetail.getRecords().get(0).getStatus());
        assertEquals(WorkOrderStatus.ACCEPTED, adminDetail.getRecords().get(1).getStatus());
        assertEquals(WorkOrderStatus.IN_PROGRESS, adminDetail.getRecords().get(2).getStatus());
        assertEquals(WorkOrderStatus.WAITING_CONFIRMATION, adminDetail.getRecords().get(3).getStatus());
        assertEquals(WorkOrderStatus.COMPLETED, adminDetail.getRecords().get(4).getStatus());
        assertEquals(UserRole.ADMIN, adminDetail.getRecords().get(0).getOperatorRole());
        assertEquals(UserRole.WORKER, adminDetail.getRecords().get(1).getOperatorRole());
        assertEquals(UserRole.STUDENT, adminDetail.getRecords().get(4).getOperatorRole());

        WorkOrderDetailView workerDetail = workOrderService.getWorkerWorkOrderDetail(worker.getId(), workOrderId);
        assertEquals(workOrderId, workerDetail.getId());
        assertEquals(5, workerDetail.getRecords().size());
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
}