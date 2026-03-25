package com.scau.dormrepair.ui.support;

import javafx.scene.control.ComboBox;

/**
 * 这里保留统一入口，方便以后集中接管交互效果。
 * 当前阶段优先保证控件稳定，不再给下拉弹层加任何位移或缩放动画。
 */
public final class UiMotion {

    private UiMotion() {
    }

    public static <T> void installSmoothDropdown(ComboBox<T> comboBox) {
        // 用户明确要求下拉框打开时不能有“自己变大、自己移动”的观感。
        // 所以这里刻意不挂动画，只保留稳定入口，避免各模块各自再写一套弹层效果。
        comboBox.setOnShowing(event -> {
            comboBox.setOpacity(1);
            comboBox.setScaleX(1);
            comboBox.setScaleY(1);
        });
    }
}
