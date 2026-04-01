package com.scau.dormrepair.domain.view;

import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.domain.enums.TimeoutLevel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Student-side repair detail view.
 */
public class StudentRepairDetailView {

    private Long id;
    private Long studentId;
    private String requestNo;
    private String studentName;
    private String contactPhone;
    private String dormAreaSnapshot;
    private String buildingNoSnapshot;
    private String roomNoSnapshot;
    private FaultCategory faultCategory;
    private String description;
    private RepairRequestStatus status;
    private Integer urgeCount;
    private Long workerId;
    private String workerName;
    private String workOrderNo;
    private String assignmentNote;
    private LocalDateTime submittedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private String completionNote;
    private Integer feedbackRating;
    private String feedbackComment;
    private Boolean feedbackAnonymousFlag;
    private List<String> imageUrls = new ArrayList<>();
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

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getDormAreaSnapshot() {
        return dormAreaSnapshot;
    }

    public void setDormAreaSnapshot(String dormAreaSnapshot) {
        this.dormAreaSnapshot = dormAreaSnapshot;
    }

    public String getBuildingNoSnapshot() {
        return buildingNoSnapshot;
    }

    public void setBuildingNoSnapshot(String buildingNoSnapshot) {
        this.buildingNoSnapshot = buildingNoSnapshot;
    }

    public String getRoomNoSnapshot() {
        return roomNoSnapshot;
    }

    public void setRoomNoSnapshot(String roomNoSnapshot) {
        this.roomNoSnapshot = roomNoSnapshot;
    }

    public FaultCategory getFaultCategory() {
        return faultCategory;
    }

    public void setFaultCategory(FaultCategory faultCategory) {
        this.faultCategory = faultCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RepairRequestStatus getStatus() {
        return status;
    }

    public void setStatus(RepairRequestStatus status) {
        this.status = status;
    }

    public Integer getUrgeCount() {
        return urgeCount;
    }

    public void setUrgeCount(Integer urgeCount) {
        this.urgeCount = urgeCount;
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

    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    public String getAssignmentNote() {
        return assignmentNote;
    }

    public void setAssignmentNote(String assignmentNote) {
        this.assignmentNote = assignmentNote;
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

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
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

    public String getLocationText() {
        String area = dormAreaSnapshot == null ? "" : dormAreaSnapshot.trim();
        String building = buildingNoSnapshot == null ? "" : buildingNoSnapshot.trim();
        String room = roomNoSnapshot == null ? "" : roomNoSnapshot.trim();
        if (area.isEmpty()) {
            return building + "-" + room;
        }
        return area + " " + building + "-" + room;
    }

    public boolean hasFeedback() {
        return feedbackRating != null;
    }
}