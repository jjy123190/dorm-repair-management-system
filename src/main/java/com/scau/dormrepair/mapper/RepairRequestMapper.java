package com.scau.dormrepair.mapper;

import com.scau.dormrepair.domain.entity.RepairRequest;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.StudentRepairDetailView;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 报修单 Mapper。
 */
public interface RepairRequestMapper {

    RepairRequest selectById(Long id);

    int insert(RepairRequest repairRequest);

    int updateAssignmentInfo(
            @Param("id") Long id,
            @Param("status") RepairRequestStatus status,
            @Param("reviewerId") Long reviewerId,
            @Param("workerId") Long workerId
    );

    int updateStatus(
            @Param("id") Long id,
            @Param("status") RepairRequestStatus status,
            @Param("completedAt") LocalDateTime completedAt
    );

    List<RecentRepairRequestView> selectLatestSubmittedRequests(@Param("limit") int limit);

    List<RecentRepairRequestView> selectStudentSubmittedRequests(
            @Param("studentId") Long studentId,
            @Param("limit") int limit
    );

    StudentRepairDetailView selectStudentRequestDetail(
            @Param("studentId") Long studentId,
            @Param("requestId") Long requestId
    );

    List<RecentRepairRequestView> selectPendingAssignmentRequests(@Param("limit") int limit);
}
