package com.scau.dormrepair.ui.module;

import javafx.scene.Parent;

/**
 * 所有工作台模块都统一实现这个接口。
 */
public interface WorkbenchModule {

    String moduleName();

    Parent createView();
}
