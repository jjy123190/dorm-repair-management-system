package com.scau.dormrepair.domain.entity;

import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
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

@Entity
@Table(
        name = "repair_requests",
        indexes = {
                @Index(name = "idx_repair_request_no", columnList = "requestNo", unique = true),
                @Index(name = "idx_repair_request_status", columnList = "status")
        }
)
public class RepairRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String requestNo;

    @Column(nullable = false, length = 64)
    private String studentName;

    @Column(nullable = false, length = 32)
    private String contactPhone;

    @Column(nullable = false, length = 32)
    private String buildingNo;

    @Column(nullable = false, length = 32)
    private String roomNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FaultCategory faultCategory;

    @Lob
    @Column(nullable = false)
    private String description;

    @Lob
    private String imageUrls;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RepairRequestStatus status;

    private Long reviewerId;

    private Long workerId;

    @Column(nullable = false)
    private Integer urgeCount = 0;

    @Column(nullable = false)
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

    public String getBuildingNo() {
        return buildingNo;
    }

    public void setBuildingNo(String buildingNo) {
        this.buildingNo = buildingNo;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
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

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
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
