package com.scau.dormrepair.web.dto;

import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 更新工单状态的请求体。
 * @param status 工单状态
 * @param operatorId 当前操作人 ID
 * @param note 本次处理说明
 */
public record UpdateWorkOrderStatusCommand(
        @NotNull(message = "工单状态不能为空")
        WorkOrderStatus status,
        @NotNull(message = "操作人ID不能为空")
        Long operatorId,
        @Size(max = 1000, message = "处理说明长度不能超过1000")
        String note
) {
}
