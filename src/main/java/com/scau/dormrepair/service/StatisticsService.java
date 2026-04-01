package com.scau.dormrepair.service;

import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.view.DormBuildingFaultSummary;
import com.scau.dormrepair.domain.view.FaultCategorySummary;
import com.scau.dormrepair.domain.view.MonthlyRepairSummary;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.TimeoutStageSummary;
import java.util.List;

public interface StatisticsService {

    List<MonthlyRepairSummary> listMonthlySummary(int recentMonths);

    List<FaultCategorySummary> listFaultCategorySummary(int recentMonths);

    List<DormBuildingFaultSummary> listDormBuildingFaultSummary(int recentMonths, int limit);

    List<TimeoutStageSummary> listTimeoutStageSummary();

    List<RecentRepairRequestView> listDormBuildingRequestDetails(int recentMonths, String dormArea, String buildingNo, int limit);

    List<RecentRepairRequestView> listFaultCategoryRequestDetails(int recentMonths, FaultCategory faultCategory, int limit);

    List<RecentRepairRequestView> listTimeoutStageRequestDetails(String stageLabel, int limit);
}