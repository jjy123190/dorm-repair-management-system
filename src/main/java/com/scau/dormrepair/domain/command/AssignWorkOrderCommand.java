package com.scau.dormrepair.domain.command;

import com.scau.dormrepair.domain.enums.WorkOrderPriority;

/**
 * 管理员派单命令。
 */
public record AssignWorkOrderCommand(
        Long repairRequestId,
        Long adminId,
        Long workerId,
        WorkOrderPriority priority,
        String assignmentNote
) {
}
