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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 工单服务实现。
 */
@Service
public class WorkOrderServiceImpl implements WorkOrderService {

    private static final DateTimeFormatter NUMBER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 工单数据访问对象。
     */
    private final WorkOrderRepository workOrderRepository;
    /**
     * 报修单数据访问对象。
     */
    private final RepairRequestRepository repairRequestRepository;
    /**
     * 工单流水数据访问对象。
     */
    private final RepairRecordRepository repairRecordRepository;
    /**
     * 评价数据访问对象。
     */
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

    /**
     * 管理员创建并派发工单。
     * @param command 派单数据
     * @return 创建后的工单信息
     */
    @Override
    @Transactional
    public WorkOrderResponse assign(AssignWorkOrderCommand command) {
        // 1. 先检查报修单是否存在。
        RepairRequest repairRequest = repairRequestRepository.findById(command.repairRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("未找到报修单，ID=" + command.repairRequestId()));

        if (repairRequest.getStatus() == RepairRequestStatus.COMPLETED) {
            throw new IllegalArgumentException("已完成的报修单不能再次派单");
        }

        // 2. 尝试按报修单找到已有工单，防止重复创建。
        WorkOrder workOrder = workOrderRepository.findByRepairRequestId(command.repairRequestId())
                .orElseGet(WorkOrder::new);
        if (workOrder.getId() == null) {
            // 一条报修单最多对应一张工单，第一次派单时才生成正式工单号。
            workOrder.setWorkOrderNo(generateWorkOrderNo());
            workOrder.setRepairRequestId(command.repairRequestId());
        }

        // 3. 写入派单基础数据。
        workOrder.setAdminId(command.adminId());
        workOrder.setWorkerId(command.workerId());
        workOrder.setPriority(command.priority());
        workOrder.setAssignmentNote(command.assignmentNote());
        workOrder.setStatus(WorkOrderStatus.ASSIGNED);
        workOrder.setAssignedAt(LocalDateTime.now());

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);

        // 4. 同步更新报修单的负责人和状态。
        repairRequest.setReviewerId(command.adminId());
        repairRequest.setWorkerId(command.workerId());
        repairRequest.setStatus(RepairRequestStatus.ASSIGNED);
        repairRequestRepository.save(repairRequest);

        // 派单动作本身也要写入记录表，后续才能回溯是谁在什么时间做了派单。
        createRecord(savedWorkOrder.getId(), command.adminId(), WorkOrderStatus.ASSIGNED, defaultNote(command.assignmentNote(), "管理员完成派单"));
        return toResponse(savedWorkOrder);
    }

    /**
     * 查询工单详情。
     * @param id 工单 ID
     * @return 工单详情
     */
    @Override
    @Transactional(readOnly = true)
    public WorkOrderResponse getById(Long id) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到工单，ID=" + id));
        return toResponse(workOrder);
    }

    /**
     * 分页查询工单。
     * @param status 可选状态
     * @param workerId 可选维修人员 ID
     * @param page 页码
     * @param size 每页条数
     * @return 工单分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public Page<WorkOrderResponse> page(WorkOrderStatus status, Long workerId, int page, int size) {
        // 支持按状态、维修人员组合筛选，给管理员列表和维修员列表共用。
        int safePage = Math.max(page - 1, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageRequest = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Order.desc("assignedAt"), Sort.Order.desc("id"))
        );

        Page<WorkOrder> workOrderPage;
        if (status != null && workerId != null) {
            workOrderPage = workOrderRepository.findAllByStatusAndWorkerId(status, workerId, pageRequest);
        } else if (status != null) {
            workOrderPage = workOrderRepository.findAllByStatus(status, pageRequest);
        } else if (workerId != null) {
            workOrderPage = workOrderRepository.findAllByWorkerId(workerId, pageRequest);
        } else {
            workOrderPage = workOrderRepository.findAll(pageRequest);
        }

        return workOrderPage.map(this::toResponse);
    }

    /**
     * 更新工单状态。
     * @param workOrderId 工单 ID
     * @param command 状态更新数据
     * @return 更新后的工单信息
     */
    @Override
    @Transactional
    public WorkOrderResponse updateStatus(Long workOrderId, UpdateWorkOrderStatusCommand command) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到工单，ID=" + workOrderId));
        RepairRequest repairRequest = repairRequestRepository.findById(workOrder.getRepairRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("未找到工单关联的报修单，ID=" + workOrder.getRepairRequestId()));

        // 1. 更新工单当前状态。
        workOrder.setStatus(command.status());
        if (command.status() == WorkOrderStatus.ACCEPTED && workOrder.getAcceptedAt() == null) {
            workOrder.setAcceptedAt(LocalDateTime.now());
        }
        if (command.status() == WorkOrderStatus.COMPLETED) {
            // 工单完成时，同步回写报修单的完成时间。
            LocalDateTime completedAt = LocalDateTime.now();
            workOrder.setCompletedAt(completedAt);
            repairRequest.setCompletedAt(completedAt);
        }

        // 工单状态和报修状态必须同步维护，否则列表和统计会对不上。
        repairRequest.setStatus(mapRequestStatus(command.status()));
        workOrderRepository.save(workOrder);
        repairRequestRepository.save(repairRequest);
        // 2. 把本次处理动作写进流水表。
        createRecord(workOrderId, command.operatorId(), command.status(), defaultNote(command.note(), "工单状态更新为 " + command.status().name()));

        return toResponse(workOrder);
    }

    /**
     * 提交学生评价。
     * @param command 评价数据
     */
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
        // 每次派单或状态更新都写一条流水，便于后续追溯。
        RepairRecord repairRecord = new RepairRecord();
        repairRecord.setWorkOrderId(workOrderId);
        repairRecord.setOperatorId(operatorId);
        repairRecord.setStatus(status);
        repairRecord.setRecordNote(note);
        repairRecordRepository.save(repairRecord);
    }

    private RepairRequestStatus mapRequestStatus(WorkOrderStatus status) {
        // 这里定义了“工单状态 -> 报修状态”的统一映射规则。
        return switch (status) {
            case ASSIGNED -> RepairRequestStatus.ASSIGNED;
            case ACCEPTED, IN_PROGRESS, WAITING_PARTS -> RepairRequestStatus.IN_PROGRESS;
            case COMPLETED -> RepairRequestStatus.COMPLETED;
            case REJECTED -> RepairRequestStatus.REJECTED;
            case CANCELLED -> RepairRequestStatus.CANCELLED;
        };
    }

    private String generateWorkOrderNo() {
        // 规则与报修单号一致：前缀 + 时间戳 + 随机串。
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
