package com.scau.dormrepair.mapper;

import com.scau.dormrepair.domain.view.MonthlyRepairSummary;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 统计报表 Mapper。
 */
public interface StatisticsMapper {

    List<MonthlyRepairSummary> selectMonthlySummary(@Param("recentMonths") int recentMonths);
}
