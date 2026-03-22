package com.scau.dormrepair.web.dto;

import java.math.BigDecimal;

/**
 * 月度统计响应体。
 */
public record MonthlyStatisticsResponse(
        String month,
        long totalRequests,
        long submittedCount,
        long assignedCount,
        long inProgressCount,
        long completedCount,
        long rejectedCount,
        BigDecimal averageRating
) {
}
