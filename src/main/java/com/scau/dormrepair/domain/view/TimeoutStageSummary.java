package com.scau.dormrepair.domain.view;

import java.math.BigDecimal;

/**
 * Timeout drill-down summary by stage.
 */
public class TimeoutStageSummary {

    private String stageLabel;
    private Long totalCount;
    private Long overdueCount;
    private BigDecimal overdueRate;

    public String getStageLabel() {
        return stageLabel;
    }

    public void setStageLabel(String stageLabel) {
        this.stageLabel = stageLabel;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(Long overdueCount) {
        this.overdueCount = overdueCount;
    }

    public BigDecimal getOverdueRate() {
        return overdueRate;
    }

    public void setOverdueRate(BigDecimal overdueRate) {
        this.overdueRate = overdueRate;
    }
}