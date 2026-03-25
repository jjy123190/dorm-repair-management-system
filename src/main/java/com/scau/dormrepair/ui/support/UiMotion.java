package com.scau.dormrepair.ui.support;

import javafx.scene.control.ComboBox;

/**
 * 统一保留交互入口，但当前阶段默认不挂任何动画。
 * 用户明确要求组件保持固定，所以这里故意保持空实现。
 */
public final class UiMotion {

    private UiMotion() {
    }

    public static <T> void installSmoothDropdown(ComboBox<T> comboBox) {
        // 桌面端当前优先保证稳定和固定布局，这里不再注入任何显示动画。
    }
}
