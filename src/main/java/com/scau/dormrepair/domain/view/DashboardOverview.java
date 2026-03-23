package com.scau.dormrepair.domain.view;

/**
 * 首页概览统计对象。
 */
public class DashboardOverview {

    private Long totalRequests;
    private Long pendingReviewRequests;
    private Long activeWorkOrders;
    private Long completedThisMonth;

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
}
