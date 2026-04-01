package com.scau.dormrepair.mapper;

import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.view.DormBuildingFaultSummary;
import com.scau.dormrepair.domain.view.FaultCategorySummary;
import com.scau.dormrepair.domain.view.MonthlyRepairSummary;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.TimeoutStageSummary;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StatisticsMapper {

    List<MonthlyRepairSummary> selectMonthlySummary(@Param("startDate") LocalDate startDate);

    List<FaultCategorySummary> selectFaultCategorySummary(@Param("startDate") LocalDate startDate);

    List<DormBuildingFaultSummary> selectDormBuildingFaultSummary(@Param("startDate") LocalDate startDate, @Param("limit") int limit);

    List<TimeoutStageSummary> selectTimeoutStageSummary();

    List<RecentRepairRequestView> selectDormBuildingRequestDetails(
            @Param("startDate") LocalDate startDate,
            @Param("dormArea") String dormArea,
            @Param("buildingNo") String buildingNo,
            @Param("limit") int limit
    );

    List<RecentRepairRequestView> selectFaultCategoryRequestDetails(
            @Param("startDate") LocalDate startDate,
            @Param("faultCategory") FaultCategory faultCategory,
            @Param("limit") int limit
    );

    List<RecentRepairRequestView> selectTimeoutStageRequestDetails(
            @Param("stageLabel") String stageLabel,
            @Param("limit") int limit
    );
}