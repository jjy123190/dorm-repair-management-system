package com.scau.dormrepair.ui.theme;

import io.vproxy.vfx.theme.impl.DarkTheme;
import javafx.scene.paint.Color;

/**
 * 基于 vfx 的浅色项目主题。
 * 配色按当前课设主色板收敛到薄荷绿、浅黄、炭黑和暖灰。
 */
public class DormVfxTheme extends DarkTheme {

    private static final Color MINT = Color.web("#A7CDB8");
    private static final Color MINT_LIGHT = Color.web("#C5E0D0");
    private static final Color SOFT_YELLOW = Color.web("#E8DF98");
    private static final Color CHARCOAL = Color.web("#2E2E2E");
    private static final Color MUTED = Color.web("#ABABAB");
    private static final Color LIGHT_BG = Color.web("#ECECEC");
    private static final Color PANEL_BG = Color.web("#F5F2E8");
    private static final Color PANEL_BG_ALT = Color.web("#E3E3E3");

    @Override
    public Color normalTextColor() {
        return CHARCOAL;
    }

    @Override
    public Color borderColorLight() {
        return MUTED;
    }

    @Override
    public Color borderColorDark() {
        return CHARCOAL;
    }

    @Override
    public Color sceneBackgroundColor() {
        return LIGHT_BG;
    }

    @Override
    public Color subSceneBackgroundColor() {
        return PANEL_BG;
    }

    @Override
    public Color fusionButtonNormalBackgroundColor() {
        return MINT;
    }

    @Override
    public Color fusionButtonHoverBackgroundColor() {
        return MINT_LIGHT;
    }

    @Override
    public Color fusionButtonDownBackgroundColor() {
        return SOFT_YELLOW;
    }

    @Override
    public Color fusionButtonAnimatingBorderLightColor() {
        return SOFT_YELLOW;
    }

    @Override
    public Color transparentFusionButtonDownBackgroundColor() {
        return Color.color(MINT.getRed(), MINT.getGreen(), MINT.getBlue(), 0.24);
    }

    @Override
    public Color fusionPaneNormalBackgroundColor() {
        return PANEL_BG;
    }

    @Override
    public Color fusionPaneHoverBackgroundColor() {
        return Color.web("#ECF2ED");
    }

    @Override
    public Color fusionPaneBorderColor() {
        return MUTED;
    }

    @Override
    public Color transparentFusionPaneHoverBackgroundColor() {
        return Color.color(MINT.getRed(), MINT.getGreen(), MINT.getBlue(), 0.16);
    }

    @Override
    public Color scrollBarColor() {
        return MUTED;
    }

    @Override
    public Color fusionButtonTextColor() {
        return CHARCOAL;
    }

    @Override
    public Color fusionButtonDisabledTextColor() {
        return MUTED;
    }

    @Override
    public Color coverBackgroundColor() {
        return Color.color(CHARCOAL.getRed(), CHARCOAL.getGreen(), CHARCOAL.getBlue(), 0.08);
    }

    @Override
    public Color tableTextColor() {
        return CHARCOAL;
    }

    @Override
    public Color tableHeaderTextColor() {
        return CHARCOAL;
    }

    @Override
    public Color tableSortLabelColor() {
        return MUTED;
    }

    @Override
    public Color tableCellSelectedBackgroundColor() {
        return SOFT_YELLOW;
    }

    @Override
    public Color tableCellBackgroundColor2() {
        return PANEL_BG_ALT;
    }

    @Override
    public Color tableHeaderTopBackgroundColor() {
        return PANEL_BG;
    }

    @Override
    public Color tableHeaderBottomBackgroundColor() {
        return LIGHT_BG;
    }

    @Override
    public Color progressBarProgressColor() {
        return MINT;
    }

    @Override
    public Color progressBarBackgroundColor() {
        return PANEL_BG_ALT;
    }

    @Override
    public Color toggleSwitchUnselectedTrayColor() {
        return PANEL_BG_ALT;
    }

    @Override
    public Color toggleSwitchSelectedTrayColor() {
        return MINT;
    }
}
