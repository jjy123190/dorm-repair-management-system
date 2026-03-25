package com.scau.dormrepair.domain.entity;

import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import java.time.LocalDateTime;

/**
 * 报修申请主实体。
 * 这张表承载学生提交信息，以及管理员分派和维修过程中的状态流转。
 */
public class RepairRequest extends BaseTimeEntity {

    private Long id;
    private String requestNo;
    private Long studentId;
    private String studentName;
    private String contactPhone;
    private Long dormRoomId;
    private String dormAreaSnapshot;
    private String buildingNoSnapshot;
    private String roomNoSnapshot;
    private FaultCategory faultCategory;
    private String description;
    private RepairRequestStatus status;
    private Long reviewerId;
    private Long workerId;
    private Integer urgeCount;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
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

    public Long getDormRoomId() {
        return dormRoomId;
    }

    public void setDormRoomId(Long dormRoomId) {
        this.dormRoomId = dormRoomId;
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

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
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
}
