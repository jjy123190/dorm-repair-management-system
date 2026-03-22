package com.scau.dormrepair.web.dto;

import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import java.time.LocalDateTime;
import java.util.List;

public record RepairRequestDetailResponse(
        Long id,
        String requestNo,
        String studentName,
        String contactPhone,
        String buildingNo,
        String roomNo,
        FaultCategory faultCategory,
        String description,
        List<String> imageUrls,
        RepairRequestStatus status,
        Long reviewerId,
        Long workerId,
        Integer urgeCount,
        LocalDateTime submittedAt,
        LocalDateTime completedAt
) {
}
