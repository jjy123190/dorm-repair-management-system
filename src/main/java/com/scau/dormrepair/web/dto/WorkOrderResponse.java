package com.scau.dormrepair.web.dto;

import com.scau.dormrepair.domain.enums.WorkOrderPriority;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import java.time.LocalDateTime;

public record WorkOrderResponse(
        Long id,
        String workOrderNo,
        Long repairRequestId,
        Long adminId,
        Long workerId,
        WorkOrderStatus status,
        WorkOrderPriority priority,
        String assignmentNote,
        LocalDateTime assignedAt,
        LocalDateTime acceptedAt,
        LocalDateTime completedAt
) {
}
