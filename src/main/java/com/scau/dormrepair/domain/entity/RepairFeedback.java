package com.scau.dormrepair.domain.entity;

/**
 * 维修评价实体。
 */
public class RepairFeedback extends BaseTimeEntity {

    private Long id;
    private Long repairRequestId;
    private Integer rating;
    private String feedbackComment;
    private Boolean anonymousFlag;

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

    public Boolean getAnonymousFlag() {
        return anonymousFlag;
    }

    public void setAnonymousFlag(Boolean anonymousFlag) {
        this.anonymousFlag = anonymousFlag;
    }
}
