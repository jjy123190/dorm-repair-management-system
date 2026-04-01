package com.scau.dormrepair.ui.component;

import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import com.scau.dormrepair.ui.support.UiDisplayText;
import javafx.scene.control.Label;

public final class StatusChip {

    private StatusChip() {
    }

    public static Label repairRequest(RepairRequestStatus status) {
        Label chip = baseChip(UiDisplayText.repairRequestStatus(status));
        if (status == null) {
            chip.getStyleClass().add("status-chip-neutral");
            return chip;
        }
        switch (status) {
            case SUBMITTED -> chip.getStyleClass().add("status-chip-warn");
            case ASSIGNED -> chip.getStyleClass().add("status-chip-info");
            case IN_PROGRESS, REWORK_IN_PROGRESS -> chip.getStyleClass().add("status-chip-processing");
            case PENDING_CONFIRMATION -> chip.getStyleClass().add("status-chip-focus");
            case COMPLETED -> chip.getStyleClass().add("status-chip-success");
            case REJECTED, CANCELLED -> chip.getStyleClass().add("status-chip-neutral");
        }
        return chip;
    }

    public static Label workOrder(WorkOrderStatus status) {
        Label chip = baseChip(UiDisplayText.workOrderStatus(status));
        if (status == null) {
            chip.getStyleClass().add("status-chip-neutral");
            return chip;
        }
        switch (status) {
            case ASSIGNED, WAITING_PARTS -> chip.getStyleClass().add("status-chip-warn");
            case ACCEPTED, IN_PROGRESS -> chip.getStyleClass().add("status-chip-processing");
            case WAITING_CONFIRMATION -> chip.getStyleClass().add("status-chip-focus");
            case COMPLETED -> chip.getStyleClass().add("status-chip-success");
            case REJECTED, CANCELLED -> chip.getStyleClass().add("status-chip-neutral");
        }
        return chip;
    }

    private static Label baseChip(String text) {
        Label chip = new Label(text == null || text.isBlank() ? "--" : text);
        chip.getStyleClass().add("status-chip");
        chip.setWrapText(true);
        return chip;
    }
}