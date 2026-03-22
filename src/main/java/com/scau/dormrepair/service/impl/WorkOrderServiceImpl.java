package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.domain.entity.RepairFeedback;
import com.scau.dormrepair.domain.entity.RepairRecord;
import com.scau.dormrepair.domain.entity.RepairRequest;
import com.scau.dormrepair.domain.entity.WorkOrder;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import com.scau.dormrepair.exception.ResourceNotFoundException;
import com.scau.dormrepair.repository.RepairFeedbackRepository;
import com.scau.dormrepair.repository.RepairRecordRepository;
import com.scau.dormrepair.repository.RepairRequestRepository;
import com.scau.dormrepair.repository.WorkOrderRepository;
import com.scau.dormrepair.service.WorkOrderService;
import com.scau.dormrepair.web.dto.AssignWorkOrderCommand;
import com.scau.dormrepair.web.dto.SubmitRepairFeedbackCommand;
import com.scau.dormrepair.web.dto.UpdateWorkOrderStatusCommand;
import com.scau.dormrepair.web.dto.WorkOrderResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkOrderServiceImpl implements WorkOrderService {

    private static final DateTimeFormatter NUMBER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final WorkOrderRepository workOrderRepository;
    private final RepairRequestRepository repairRequestRepository;
    private final RepairRecordRepository repairRecordRepository;
    private final RepairFeedbackRepository repairFeedbackRepository;

    public WorkOrderServiceImpl(
            WorkOrderRepository workOrderRepository,
            RepairRequestRepository repairRequestRepository,
            RepairRecordRepository repairRecordRepository,
            RepairFeedbackRepository repairFeedbackRepository
    ) {
        this.workOrderRepository = workOrderRepository;
        this.repairRequestRepository = repairRequestRepository;
        this.repairRecordRepository = repairRecordRepository;
        this.repairFeedbackRepository = repairFeedbackRepository;
    }

    @Override
    @Transactional
    public WorkOrderResponse assign(AssignWorkOrderCommand command) {
        RepairRequest repairRequest = repairRequestRepository.findById(command.repairRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("未找到报修单，ID=" + command.repairRequestId()));

        if (repairRequest.getStatus() == RepairRequestStatus.COMPLETED) {
            throw new IllegalArgumentException("已完成的报修单不能再次派单");
        }

        WorkOrder workOrder = workOrderRepository.findByRepairRequestId(command.repairRequestId())
                .orElseGet(WorkOrder::new);
        if (workOrder.getId() == null) {
            workOrder.setWorkOrderNo(generateWorkOrderNo());
            workOrder.setRepairRequestId(command.repairRequestId());
        }

        workOrder.setAdminId(command.adminId());
        workOrder.setWorkerId(command.workerId());
        workOrder.setPriority(command.priority());
        workOrder.setAssignmentNote(command.assignmentNote());
        workOrder.setStatus(WorkOrderStatus.ASSIGNED);
        workOrder.setAssignedAt(LocalDateTime.now());

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);

        repairRequest.setReviewerId(command.adminId());
        repairRequest.setWorkerId(command.workerId());
        repairRequest.setStatus(RepairRequestStatus.ASSIGNED);
        repairRequestRepository.save(repairRequest);

        createRecord(savedWorkOrder.getId(), command.adminId(), WorkOrderStatus.ASSIGNED, defaultNote(command.assignmentNote(), "管理员完成派单"));
        return toResponse(savedWorkOrder);
    }

    @Override
    @Transactional
    public WorkOrderResponse updateStatus(Long workOrderId, UpdateWorkOrderStatusCommand command) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到工单，ID=" + workOrderId));
        RepairRequest repairRequest = repairRequestRepository.findById(workOrder.getRepairRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("未找到工单关联的报修单，ID=" + workOrder.getRepairRequestId()));

        workOrder.setStatus(command.status());
        if (command.status() == WorkOrderStatus.ACCEPTED && workOrder.getAcceptedAt() == null) {
            workOrder.setAcceptedAt(LocalDateTime.now());
        }
        if (command.status() == WorkOrderStatus.COMPLETED) {
            LocalDateTime completedAt = LocalDateTime.now();
            workOrder.setCompletedAt(completedAt);
            repairRequest.setCompletedAt(completedAt);
        }

        repairRequest.setStatus(mapRequestStatus(command.status()));
        workOrderRepository.save(workOrder);
        repairRequestRepository.save(repairRequest);
        createRecord(workOrderId, command.operatorId(), command.status(), defaultNote(command.note(), "工单状态更新为 " + command.status().name()));

        return toResponse(workOrder);
    }

    @Override
    @Transactional
    public void submitFeedback(SubmitRepairFeedbackCommand command) {
        RepairRequest repairRequest = repairRequestRepository.findById(command.repairRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("未找到报修单，ID=" + command.repairRequestId()));

        if (repairRequest.getStatus() != RepairRequestStatus.COMPLETED) {
            throw new IllegalArgumentException("仅已完成的报修单允许提交评价");
        }
        if (repairFeedbackRepository.existsByRepairRequestId(command.repairRequestId())) {
            throw new IllegalArgumentException("该报修单已提交过评价");
        }

        RepairFeedback feedback = new RepairFeedback();
        feedback.setRepairRequestId(command.repairRequestId());
        feedback.setRating(command.rating());
        feedback.setFeedbackComment(command.feedbackComment());
        feedback.setAnonymous(Boolean.TRUE.equals(command.anonymous()));
        repairFeedbackRepository.save(feedback);
    }

    private void createRecord(Long workOrderId, Long operatorId, WorkOrderStatus status, String note) {
        RepairRecord repairRecord = new RepairRecord();
        repairRecord.setWorkOrderId(workOrderId);
        repairRecord.setOperatorId(operatorId);
        repairRecord.setStatus(status);
        repairRecord.setRecordNote(note);
        repairRecordRepository.save(repairRecord);
    }

    private RepairRequestStatus mapRequestStatus(WorkOrderStatus status) {
        return switch (status) {
            case ASSIGNED -> RepairRequestStatus.ASSIGNED;
            case ACCEPTED, IN_PROGRESS, WAITING_PARTS -> RepairRequestStatus.IN_PROGRESS;
            case COMPLETED -> RepairRequestStatus.COMPLETED;
            case REJECTED -> RepairRequestStatus.REJECTED;
            case CANCELLED -> RepairRequestStatus.CANCELLED;
        };
    }

    private String generateWorkOrderNo() {
        return "WO" + NUMBER_TIME_FORMATTER.format(LocalDateTime.now())
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    private String defaultNote(String input, String fallback) {
        return (input == null || input.isBlank()) ? fallback : input;
    }

    private WorkOrderResponse toResponse(WorkOrder workOrder) {
        return new WorkOrderResponse(
                workOrder.getId(),
                workOrder.getWorkOrderNo(),
                workOrder.getRepairRequestId(),
                workOrder.getAdminId(),
                workOrder.getWorkerId(),
                workOrder.getStatus(),
                workOrder.getPriority(),
                workOrder.getAssignmentNote(),
                workOrder.getAssignedAt(),
                workOrder.getAcceptedAt(),
                workOrder.getCompletedAt()
        );
    }
}
