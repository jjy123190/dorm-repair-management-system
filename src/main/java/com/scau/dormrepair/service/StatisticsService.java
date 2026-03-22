package com.scau.dormrepair.service;

import com.scau.dormrepair.web.dto.MonthlyStatisticsResponse;
import java.time.YearMonth;

public interface StatisticsService {

    MonthlyStatisticsResponse monthlySummary(YearMonth month);
}
