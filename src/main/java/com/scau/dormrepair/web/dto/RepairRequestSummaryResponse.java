package com.scau.dormrepair.web.dto;

import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import java.time.LocalDateTime;

/**
 * 报修单列表响应体。
 */
public record RepairRequestSummaryResponse(
        Long id,
        String requestNo,
        String studentName,
        String buildingNo,
        String roomNo,
        FaultCategory faultCategory,
        RepairRequestStatus status,
        LocalDateTime submittedAt
) {
}
