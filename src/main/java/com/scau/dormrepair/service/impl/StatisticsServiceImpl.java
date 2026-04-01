package com.scau.dormrepair.service.impl;

import com.scau.dormrepair.common.MyBatisExecutor;
import com.scau.dormrepair.common.RepairSlaPolicy;
import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.TimeoutLevel;
import com.scau.dormrepair.domain.view.DormBuildingFaultSummary;
import com.scau.dormrepair.domain.view.FaultCategorySummary;
import com.scau.dormrepair.domain.view.MonthlyRepairSummary;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.TimeoutStageSummary;
import com.scau.dormrepair.mapper.StatisticsMapper;
import com.scau.dormrepair.service.StatisticsService;
import java.time.LocalDate;
import java.util.List;

public class StatisticsServiceImpl implements StatisticsService {

    private final MyBatisExecutor myBatisExecutor;

    public StatisticsServiceImpl(MyBatisExecutor myBatisExecutor) {
        this.myBatisExecutor = myBatisExecutor;
    }

    @Override
    public List<MonthlyRepairSummary> listMonthlySummary(int recentMonths) {
        int safeMonths = normalizeRecentMonths(recentMonths);
        LocalDate startDate = resolveStartDate(safeMonths);
        return myBatisExecutor.executeRead(session -> session.getMapper(StatisticsMapper.class).selectMonthlySummary(startDate));
    }

    @Override
    public List<FaultCategorySummary> listFaultCategorySummary(int recentMonths) {
        int safeMonths = normalizeRecentMonths(recentMonths);
        LocalDate startDate = resolveStartDate(safeMonths);
        return myBatisExecutor.executeRead(session -> session.getMapper(StatisticsMapper.class).selectFaultCategorySummary(startDate));
    }

    @Override
    public List<DormBuildingFaultSummary> listDormBuildingFaultSummary(int recentMonths, int limit) {
        int safeMonths = normalizeRecentMonths(recentMonths);
        int safeLimit = normalizeLimit(limit, 20);
        LocalDate startDate = resolveStartDate(safeMonths);
        return myBatisExecutor.executeRead(session -> session.getMapper(StatisticsMapper.class)
                .selectDormBuildingFaultSummary(startDate, safeLimit));
    }

    @Override
    public List<TimeoutStageSummary> listTimeoutStageSummary() {
        return myBatisExecutor.executeRead(session -> session.getMapper(StatisticsMapper.class).selectTimeoutStageSummary());
    }

    @Override
    public List<RecentRepairRequestView> listDormBuildingRequestDetails(int recentMonths, String dormArea, String buildingNo, int limit) {
        int safeMonths = normalizeRecentMonths(recentMonths);
        int safeLimit = normalizeLimit(limit, 30);
        LocalDate startDate = resolveStartDate(safeMonths);
        return myBatisExecutor.executeRead(session -> decorateRows(session.getMapper(StatisticsMapper.class)
                .selectDormBuildingRequestDetails(startDate, dormArea, buildingNo, safeLimit)));
    }

    @Override
    public List<RecentRepairRequestView> listFaultCategoryRequestDetails(int recentMonths, FaultCategory faultCategory, int limit) {
        int safeMonths = normalizeRecentMonths(recentMonths);
        int safeLimit = normalizeLimit(limit, 30);
        LocalDate startDate = resolveStartDate(safeMonths);
        return myBatisExecutor.executeRead(session -> decorateRows(session.getMapper(StatisticsMapper.class)
                .selectFaultCategoryRequestDetails(startDate, faultCategory, safeLimit)));
    }

    @Override
    public List<RecentRepairRequestView> listTimeoutStageRequestDetails(String stageLabel, int limit) {
        int safeLimit = normalizeLimit(limit, 30);
        return myBatisExecutor.executeRead(session -> decorateRows(session.getMapper(StatisticsMapper.class)
                .selectTimeoutStageRequestDetails(stageLabel, safeLimit)));
    }

    private List<RecentRepairRequestView> decorateRows(List<RecentRepairRequestView> rows) {
        rows.forEach(this::applyTimeout);
        return rows;
    }

    private void applyTimeout(RecentRepairRequestView row) {
        if (row == null) {
            return;
        }
        TimeoutLevel level = RepairSlaPolicy.resolveRequestLevel(row.getStatus(), row.getSubmittedAt(), row.getAssignedAt(), row.getAcceptedAt());
        row.setTimeoutLevel(level);
        row.setTimeoutLabel(RepairSlaPolicy.resolveRequestLabel(row.getStatus(), level));
    }

    private int normalizeRecentMonths(int recentMonths) {
        return Math.min(Math.max(recentMonths, 1), 24);
    }

    private int normalizeLimit(int limit, int max) {
        return Math.min(Math.max(limit, 1), max);
    }

    private LocalDate resolveStartDate(int safeMonths) {
        return LocalDate.now().withDayOfMonth(1).minusMonths(safeMonths - 1L);
    }
}