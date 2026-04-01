package com.scau.dormrepair.mapper;

import com.scau.dormrepair.domain.view.DashboardOverview;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 棣栭〉姒傝鏌ヨ Mapper銆?
 */
public interface DashboardMapper {

    DashboardOverview selectOverview();

    DashboardOverview selectStudentOverview(@Param("studentId") Long studentId);

    DashboardOverview selectWorkerOverview(@Param("workerId") Long workerId);

    List<RecentRepairRequestView> selectRecentRepairRequests(@Param("limit") int limit);

    List<RecentRepairRequestView> selectWorkerRecentRepairRequests(
            @Param("workerId") Long workerId,
            @Param("limit") int limit
    );

    List<RecentRepairRequestView> selectStudentRecentRepairRequests(
            @Param("studentId") Long studentId,
            @Param("limit") int limit
    );
}