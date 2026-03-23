package com.scau.dormrepair.domain.view;

import java.math.BigDecimal;

/**
 * 月度统计视图对象。
 */
public class MonthlyRepairSummary {

    private String monthLabel;
    private Long totalRequests;
    private Long completedRequests;
    private BigDecimal completionRate;

    public String getMonthLabel() {
        return monthLabel;
    }

    public void setMonthLabel(String monthLabel) {
        this.monthLabel = monthLabel;
    }

    public Long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(Long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public Long getCompletedRequests() {
        return completedRequests;
    }

    public void setCompletedRequests(Long completedRequests) {
        this.completedRequests = completedRequests;
    }

    public BigDecimal getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(BigDecimal completionRate) {
        this.completionRate = completionRate;
    }
}
