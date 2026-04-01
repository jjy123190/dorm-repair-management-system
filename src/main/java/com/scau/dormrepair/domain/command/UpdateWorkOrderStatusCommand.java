package com.scau.dormrepair.domain.command;

import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import java.util.List;

/**
 * 缁翠慨鍛樻洿鏂板伐鍗曠姸鎬佹椂鐨勫懡浠ゅ璞°€?
 */
public record UpdateWorkOrderStatusCommand(
        Long workOrderId,
        Long operatorId,
        WorkOrderStatus status,
        String recordNote,
        String completionNote,
        List<String> completionImageUrls
) {

    public UpdateWorkOrderStatusCommand(
            Long workOrderId,
            Long operatorId,
            WorkOrderStatus status,
            String recordNote
    ) {
        this(workOrderId, operatorId, status, recordNote, null, List.of());
    }
}