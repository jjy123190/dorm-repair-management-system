package com.scau.dormrepair.domain.view;

public class DashboardOverview {

    private Long totalRequests;
    private Long pendingReviewRequests;
    private Long activeWorkOrders;
    private Long completedThisMonth;
    private Long timeoutWarningCount;
    private Long timeoutOverdueCount;
    private Long pendingConfirmationCount;
    private Long reworkInProgressCount;

    public Long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(Long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public Long getPendingReviewRequests() {
        return pendingReviewRequests;
    }

    public void setPendingReviewRequests(Long pendingReviewRequests) {
        this.pendingReviewRequests = pendingReviewRequests;
    }

    public Long getActiveWorkOrders() {
        return activeWorkOrders;
    }

    public void setActiveWorkOrders(Long activeWorkOrders) {
        this.activeWorkOrders = activeWorkOrders;
    }

    public Long getCompletedThisMonth() {
        return completedThisMonth;
    }

    public void setCompletedThisMonth(Long completedThisMonth) {
        this.completedThisMonth = completedThisMonth;
    }

    public Long getTimeoutWarningCount() {
        return timeoutWarningCount;
    }

    public void setTimeoutWarningCount(Long timeoutWarningCount) {
        this.timeoutWarningCount = timeoutWarningCount;
    }

    public Long getTimeoutOverdueCount() {
        return timeoutOverdueCount;
    }

    public void setTimeoutOverdueCount(Long timeoutOverdueCount) {
        this.timeoutOverdueCount = timeoutOverdueCount;
    }

    public Long getPendingConfirmationCount() {
        return pendingConfirmationCount;
    }

    public void setPendingConfirmationCount(Long pendingConfirmationCount) {
        this.pendingConfirmationCount = pendingConfirmationCount;
    }

    public Long getReworkInProgressCount() {
        return reworkInProgressCount;
    }

    public void setReworkInProgressCount(Long reworkInProgressCount) {
        this.reworkInProgressCount = reworkInProgressCount;
    }
}