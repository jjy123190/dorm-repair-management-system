package com.scau.dormrepair.domain.entity;

import com.scau.dormrepair.domain.enums.WorkOrderPriority;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 工单实体。
 */
@Entity
@Table(
        name = "work_orders",
        indexes = {
                @Index(name = "idx_work_order_no", columnList = "workOrderNo", unique = true),
                @Index(name = "idx_work_order_request_id", columnList = "repairRequestId", unique = true)
        }
)
public class WorkOrder extends BaseTimeEntity {

    /**
     * 主键 ID。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 工单号。
     */
    @Column(nullable = false, unique = true, length = 64)
    private String workOrderNo;

    /**
     * 对应的报修单 ID。
     */
    @Column(nullable = false, unique = true)
    private Long repairRequestId;

    /**
     * 派单管理员 ID。
     */
    @Column(nullable = false)
    private Long adminId;

    /**
     * 当前负责的维修人员 ID。
     */
    @Column(nullable = false)
    private Long workerId;

    /**
     * 工单状态。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WorkOrderStatus status;

    /**
     * 工单优先级。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WorkOrderPriority priority;

    /**
     * 派单备注。
     */
    @Lob
    private String assignmentNote;

    /**
     * 派单时间。
     */
    @Column(nullable = false)
    private LocalDateTime assignedAt;

    /**
     * 维修人员接单时间。
     */
    private LocalDateTime acceptedAt;

    /**
     * 工单完成时间。
     */
    private LocalDateTime completedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    public Long getRepairRequestId() {
        return repairRequestId;
    }

    public void setRepairRequestId(Long repairRequestId) {
        this.repairRequestId = repairRequestId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrderStatus status) {
        this.status = status;
    }

    public WorkOrderPriority getPriority() {
        return priority;
    }

    public void setPriority(WorkOrderPriority priority) {
        this.priority = priority;
    }

    public String getAssignmentNote() {
        return assignmentNote;
    }

    public void setAssignmentNote(String assignmentNote) {
        this.assignmentNote = assignmentNote;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
