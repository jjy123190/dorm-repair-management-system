package com.scau.dormrepair.ui.component;

import com.scau.dormrepair.domain.enums.TimeoutLevel;
import com.scau.dormrepair.ui.support.UiDisplayText;
import javafx.scene.control.Label;

public final class TimeoutChip {

    private TimeoutChip() {
    }

    public static Label create(TimeoutLevel level, String text) {
        String display = UiDisplayText.timeoutLabel(level, text);
        Label chip = new Label(display == null || display.isBlank() ? "时效正常" : display);
        chip.getStyleClass().add("timeout-chip");
        if (level == null || level == TimeoutLevel.NORMAL) {
            chip.getStyleClass().add("timeout-chip-normal");
        } else if (level == TimeoutLevel.WARNING) {
            chip.getStyleClass().add("timeout-chip-warning");
        } else {
            chip.getStyleClass().add("timeout-chip-overdue");
        }
        chip.setWrapText(true);
        return chip;
    }
}