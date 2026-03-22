package com.scau.dormrepair.web.dto;

import com.scau.dormrepair.domain.enums.WorkOrderPriority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建工单并派单的请求体。
 * @param repairRequestId 报修单 ID
 * @param adminId 管理员 ID
 * @param workerId 维修人员 ID
 * @param priority 工单优先级
 * @param assignmentNote 派单备注
 */
public record AssignWorkOrderCommand(
        @NotNull(message = "报修单ID不能为空")
        Long repairRequestId,
        @NotNull(message = "管理员ID不能为空")
        Long adminId,
        @NotNull(message = "维修人员ID不能为空")
        Long workerId,
        @NotNull(message = "优先级不能为空")
        WorkOrderPriority priority,
        @Size(max = 1000, message = "派单备注长度不能超过1000")
        String assignmentNote
) {
}
