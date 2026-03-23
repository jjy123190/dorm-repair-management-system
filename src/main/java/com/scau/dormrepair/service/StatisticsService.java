package com.scau.dormrepair.service;

import com.scau.dormrepair.domain.view.MonthlyRepairSummary;
import java.util.List;

/**
 * 月度统计服务。
 */
public interface StatisticsService {

    List<MonthlyRepairSummary> listMonthlySummary(int recentMonths);
}
