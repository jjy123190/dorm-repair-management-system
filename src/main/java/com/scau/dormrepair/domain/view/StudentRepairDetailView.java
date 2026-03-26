package com.scau.dormrepair.domain.view;

import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生侧查看报修明细时使用的视图对象。
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
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
    private Integer feedbackRating;
    private String feedbackComment;
    private Boolean feedbackAnonymousFlag;
    private List<String> imageUrls = new ArrayList<>();

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

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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
