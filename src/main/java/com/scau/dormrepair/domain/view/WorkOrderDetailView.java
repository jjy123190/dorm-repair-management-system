package com.scau.dormrepair.domain.view;

import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.TimeoutLevel;
import com.scau.dormrepair.domain.enums.WorkOrderPriority;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Work-order tracking detail view.
 */
public class WorkOrderDetailView {

    private Long id;
    private Long repairRequestId;
    private String workOrderNo;
    private String requestNo;
    private Long workerId;
    private String workerName;
    private WorkOrderPriority priority;
    private WorkOrderStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private String studentName;
    private String locationText;
    private FaultCategory faultCategory;
    private String assignmentNote;
    private String completionNote;
    private Integer feedbackRating;
    private String feedbackComment;
    private Boolean feedbackAnonymousFlag;
    private List<String> completionImageUrls = new ArrayList<>();
    private List<WorkOrderRecordView> records = new ArrayList<>();
    private TimeoutLevel timeoutLevel;
    private String timeoutLabel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRepairRequestId() {
        return repairRequestId;
    }

    public void setRepairRequestId(Long repairRequestId) {
        this.repairRequestId = repairRequestId;
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

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
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

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
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

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public FaultCategory getFaultCategory() {
        return faultCategory;
    }

    public void setFaultCategory(FaultCategory faultCategory) {
        this.faultCategory = faultCategory;
    }

    public String getAssignmentNote() {
        return assignmentNote;
    }

    public void setAssignmentNote(String assignmentNote) {
        this.assignmentNote = assignmentNote;
    }

    public String getCompletionNote() {
        return completionNote;
    }

    public void setCompletionNote(String completionNote) {
        this.completionNote = completionNote;
    }

    public Integer getFeedbackRating() {
        return feedbackRating;
    }

    public void setFeedbackRating(Integer feedbackRating) {
        this.feedbackRating = feedbackRating;
    }

    public String getFeedbackComment() {
        return feedbackComment;
    }

    public void setFeedbackComment(String feedbackComment) {
        this.feedbackComment = feedbackComment;
    }

    public Boolean getFeedbackAnonymousFlag() {
        return feedbackAnonymousFlag;
    }

    public void setFeedbackAnonymousFlag(Boolean feedbackAnonymousFlag) {
        this.feedbackAnonymousFlag = feedbackAnonymousFlag;
    }

    public List<String> getCompletionImageUrls() {
        return completionImageUrls;
    }

    public void setCompletionImageUrls(List<String> completionImageUrls) {
        this.completionImageUrls = completionImageUrls == null ? new ArrayList<>() : new ArrayList<>(completionImageUrls);
    }

    public List<WorkOrderRecordView> getRecords() {
        return records;
    }

    public void setRecords(List<WorkOrderRecordView> records) {
        this.records = records == null ? new ArrayList<>() : new ArrayList<>(records);
    }

    public TimeoutLevel getTimeoutLevel() {
        return timeoutLevel;
    }

    public void setTimeoutLevel(TimeoutLevel timeoutLevel) {
        this.timeoutLevel = timeoutLevel;
    }

    public String getTimeoutLabel() {
        return timeoutLabel;
    }

    public void setTimeoutLabel(String timeoutLabel) {
        this.timeoutLabel = timeoutLabel;
    }

    public boolean hasFeedback() {
        return feedbackRating != null;
    }
}