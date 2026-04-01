package com.scau.dormrepair.domain.view;

import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import java.time.LocalDateTime;

/**
 * Work-order timeline record view.
 */
public class WorkOrderRecordView {

    private Long id;
    private WorkOrderStatus status;
    private Long operatorId;
    private String operatorName;
    private UserRole operatorRole;
    private String recordNote;
    private LocalDateTime recordedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrderStatus status) {
        this.status = status;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public UserRole getOperatorRole() {
        return operatorRole;
    }

    public void setOperatorRole(UserRole operatorRole) {
        this.operatorRole = operatorRole;
    }

    public String getRecordNote() {
        return recordNote;
    }

    public void setRecordNote(String recordNote) {
        this.recordNote = recordNote;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
