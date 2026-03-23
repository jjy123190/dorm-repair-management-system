package com.scau.dormrepair.domain.entity;

import com.scau.dormrepair.domain.enums.WorkOrderStatus;

/**
 * 工单处理记录实体。
 * 每次派单、接单、处理中、完成都应该落一条流水。
 */
public class WorkOrderRecord extends BaseTimeEntity {

    private Long id;
    private Long workOrderId;
    private Long operatorId;
    private WorkOrderStatus status;
    private String recordNote;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrderStatus status) {
        this.status = status;
    }

    public String getRecordNote() {
        return recordNote;
    }

    public void setRecordNote(String recordNote) {
        this.recordNote = recordNote;
    }
}
