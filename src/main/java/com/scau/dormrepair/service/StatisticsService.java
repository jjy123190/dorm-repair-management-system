package com.scau.dormrepair.service;

import com.scau.dormrepair.web.dto.MonthlyStatisticsResponse;
import java.time.YearMonth;

/**
 * 报表统计服务接口。
 */
public interface StatisticsService {

    /**
     * 统计指定月份的报修汇总。
     * @param month 统计月份，为空时默认当前月
     * @return 月度汇总结果
     */
    MonthlyStatisticsResponse monthlySummary(YearMonth month);
}
