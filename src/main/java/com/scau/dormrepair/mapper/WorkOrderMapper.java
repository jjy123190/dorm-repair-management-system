package com.scau.dormrepair.mapper;

import com.scau.dormrepair.domain.entity.WorkOrder;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import com.scau.dormrepair.domain.view.ActiveWorkOrderView;
import com.scau.dormrepair.domain.view.WorkOrderDetailView;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * Work-order mapper.
 */
public interface WorkOrderMapper {

    WorkOrder selectById(Long id);

    WorkOrder selectByRepairRequestId(Long repairRequestId);

    int insert(WorkOrder workOrder);

    int updateStatus(
            @Param("id") Long id,
            @Param("status") WorkOrderStatus status,
            @Param("acceptedAt") LocalDateTime acceptedAt,
            @Param("completedAt") LocalDateTime completedAt,
            @Param("completionNote") String completionNote
    );

    List<ActiveWorkOrderView> selectActiveWorkOrders(@Param("limit") int limit);

    List<ActiveWorkOrderView> selectWorkerActiveWorkOrders(
            @Param("workerId") Long workerId,
            @Param("limit") int limit
    );

    List<WorkOrderDetailView> selectTrackedWorkOrders(@Param("limit") int limit);

    List<WorkOrderDetailView> selectWorkerTrackedWorkOrders(
            @Param("workerId") Long workerId,
            @Param("limit") int limit
    );

    WorkOrderDetailView selectTrackedWorkOrderDetail(@Param("workOrderId") Long workOrderId);

    WorkOrderDetailView selectWorkerTrackedWorkOrderDetail(
            @Param("workerId") Long workerId,
            @Param("workOrderId") Long workOrderId
    );
}