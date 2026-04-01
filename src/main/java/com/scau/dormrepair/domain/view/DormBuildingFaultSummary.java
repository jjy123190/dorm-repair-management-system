package com.scau.dormrepair.domain.view;

/**
 * Dorm-area/building drill-down summary.
 */
public class DormBuildingFaultSummary {

    private String dormArea;
    private String buildingNo;
    private Long totalRequests;

    public String getDormArea() {
        return dormArea;
    }

    public void setDormArea(String dormArea) {
        this.dormArea = dormArea;
    }

    public String getBuildingNo() {
        return buildingNo;
    }

    public void setBuildingNo(String buildingNo) {
        this.buildingNo = buildingNo;
    }

    public Long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(Long totalRequests) {
        this.totalRequests = totalRequests;
    }
}