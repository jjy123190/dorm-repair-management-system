package com.scau.dormrepair.service;

import com.scau.dormrepair.web.dto.MonthlyStatisticsResponse;
import java.time.YearMonth;

/**
 * 报表统计服务接口。
 */
public interface StatisticsService {

    /**
     * 统计指定月份的报修汇总。
     */
    MonthlyStatisticsResponse monthlySummary(YearMonth month);
}
