package com.scau.dormrepair.domain.command;

import com.scau.dormrepair.domain.enums.WorkOrderStatus;

/**
 * 维修员更新工单状态时的命令对象。
 */
public record UpdateWorkOrderStatusCommand(
        Long workOrderId,
        Long operatorId,
        WorkOrderStatus status,
        String recordNote
) {
}
