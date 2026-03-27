package com.scau.dormrepair.mapper;

import com.scau.dormrepair.domain.view.DashboardOverview;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 首页概览查询 Mapper。
 */
public interface DashboardMapper {

    DashboardOverview selectOverview();

    DashboardOverview selectStudentOverview(
            @Param("studentId") Long studentId,
            @Param("studentName") String studentName
    );

    List<RecentRepairRequestView> selectRecentRepairRequests(@Param("limit") int limit);

    List<RecentRepairRequestView> selectStudentRecentRepairRequests(
            @Param("studentId") Long studentId,
            @Param("studentName") String studentName,
            @Param("limit") int limit
    );
}
