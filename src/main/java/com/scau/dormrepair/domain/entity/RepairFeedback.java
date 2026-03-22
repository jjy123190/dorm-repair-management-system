package com.scau.dormrepair.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * 评价反馈实体。
 */
@Entity
@Table(name = "repair_feedbacks")
public class RepairFeedback extends BaseTimeEntity {

    /**
     * 主键 ID。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的报修单 ID。
     */
    @Column(nullable = false, unique = true)
    private Long repairRequestId;

    /**
     * 学生评分，范围 1-5。
     */
    @Column(nullable = false)
    private Integer rating;

    /**
     * 学生填写的评价内容。
     */
    @Lob
    private String feedbackComment;

    /**
     * 是否匿名评价。
     */
    @Column(nullable = false)
    private Boolean anonymous;

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

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getFeedbackComment() {
        return feedbackComment;
    }

    public void setFeedbackComment(String feedbackComment) {
        this.feedbackComment = feedbackComment;
    }

    public Boolean getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        this.anonymous = anonymous;
    }
}
