package com.scau.dormrepair.domain.view;

import com.scau.dormrepair.domain.enums.WorkOrderPriority;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import java.time.LocalDateTime;

/**
 * 工单工作台列表项。
 */
public class ActiveWorkOrderView {

    private Long id;
    private String workOrderNo;
    private String requestNo;
    private Long workerId;
    private WorkOrderPriority priority;
    private WorkOrderStatus status;
    private LocalDateTime assignedAt;

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

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public WorkOrderPriority getPriority() {
        return priority;
    }

    public void setPriority(WorkOrderPriority priority) {
        this.priority = priority;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
