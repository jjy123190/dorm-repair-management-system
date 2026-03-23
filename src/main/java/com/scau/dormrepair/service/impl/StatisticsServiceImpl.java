package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.common.MyBatisExecutor;
import com.scau.dormrepair.domain.view.MonthlyRepairSummary;
import com.scau.dormrepair.mapper.StatisticsMapper;
import com.scau.dormrepair.service.StatisticsService;
import java.util.List;

/**
 * 统计报表服务实现。
 */
public class StatisticsServiceImpl implements StatisticsService {

    private final MyBatisExecutor myBatisExecutor;

    public StatisticsServiceImpl(MyBatisExecutor myBatisExecutor) {
        this.myBatisExecutor = myBatisExecutor;
    }

    @Override
    public List<MonthlyRepairSummary> listMonthlySummary(int recentMonths) {
        int safeMonths = Math.min(Math.max(recentMonths, 1), 24);
        return myBatisExecutor.executeRead(
                session -> session.getMapper(StatisticsMapper.class).selectMonthlySummary(safeMonths)
        );
    }
}
