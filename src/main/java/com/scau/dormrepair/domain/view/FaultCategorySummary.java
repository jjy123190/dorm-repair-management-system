package com.scau.dormrepair.domain.view;

import com.scau.dormrepair.domain.enums.FaultCategory;
import java.math.BigDecimal;

public class FaultCategorySummary {

    private FaultCategory faultCategory;
    private Long totalRequests;
    private BigDecimal percentage;

    public FaultCategory getFaultCategory() {
        return faultCategory;
    }

    public void setFaultCategory(FaultCategory faultCategory) {
        this.faultCategory = faultCategory;
    }

    public Long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(Long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
}