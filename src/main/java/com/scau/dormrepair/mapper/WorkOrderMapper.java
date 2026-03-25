package com.scau.dormrepair.mapper;

import com.scau.dormrepair.domain.entity.WorkOrder;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import com.scau.dormrepair.domain.view.ActiveWorkOrderView;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 工单 Mapper。
 */
public interface WorkOrderMapper {

    WorkOrder selectById(Long id);

    WorkOrder selectByRepairRequestId(Long repairRequestId);

    int insert(WorkOrder workOrder);

    int updateStatus(
            @Param("id") Long id,
            @Param("status") WorkOrderStatus status,
            @Param("acceptedAt") LocalDateTime acceptedAt,
            @Param("completedAt") LocalDateTime completedAt
    );

    List<ActiveWorkOrderView> selectActiveWorkOrders(@Param("limit") int limit);

    List<ActiveWorkOrderView> selectWorkerActiveWorkOrders(
            @Param("workerId") Long workerId,
            @Param("limit") int limit
    );
}
