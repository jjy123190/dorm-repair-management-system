package com.scau.dormrepair.ui.component;

import io.vproxy.vfx.theme.Theme;
import io.vproxy.vfx.ui.pane.AbstractFusionPane;
import io.vproxy.vfx.ui.pane.FusionPane;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * 对 UI 常用壳层做一层项目内封装。
 * 按钮继续保留 vfx 质感，信息卡片改回纯 JavaFX，避免两套背景系统互相打架。
 */
public final class FusionUiFactory {

    private FusionUiFactory() {
    }

    public static SurfaceCard createCard(Node content, double prefWidth, double prefHeight, String... styleClasses) {
        return createSurfaceCard(content, prefWidth, prefHeight, null, styleClasses);
    }

    public static SurfaceCard createActionCard(
            Node content,
            double prefWidth,
            double prefHeight,
            Runnable action,
            String... styleClasses
    ) {
        return createSurfaceCard(content, prefWidth, prefHeight, action, styleClasses);
    }

    public static FusionPane createPrimaryButton(String text, double prefWidth, double prefHeight, Runnable action) {
        return createButton(text, prefWidth, prefHeight, false, action);
    }

    public static FusionPane createGhostButton(String text, double prefWidth, double prefHeight, Runnable action) {
        return createButton(text, prefWidth, prefHeight, true, action);
    }

    private static SurfaceCard createSurfaceCard(
            Node content,
            double prefWidth,
            double prefHeight,
            Runnable action,
            String... styleClasses
    ) {
        if (content instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMaxHeight(Double.MAX_VALUE);
        }

        StackPane cardNode = new StackPane(content);
        cardNode.setAlignment(Pos.TOP_LEFT);
        cardNode.getStyleClass().add("fusion-node");
        cardNode.getStyleClass().addAll(styleClasses);
        cardNode.setMaxWidth(Double.MAX_VALUE);

        if (prefWidth > 0) {
            cardNode.setPrefWidth(prefWidth);
        }
        if (prefHeight > 0) {
            cardNode.setPrefHeight(prefHeight);
            cardNode.setMinHeight(Region.USE_PREF_SIZE);
        }

        if (action != null) {
            cardNode.setCursor(Cursor.HAND);

            // 展示型卡片不再做 hover/down 颜色动画，只保留点击入口，避免切页和重绘时看起来像颜色 bug。
            cardNode.setOnMouseClicked(event -> action.run());
        }

        return new SurfaceCard(cardNode);
    }

    private static FusionPane createButton(
            String text,
            double prefWidth,
            double prefHeight,
            boolean transparent,
            Runnable action
    ) {
        Label textLabel = new Label(text);
        textLabel.getStyleClass().addAll(
                "fusion-button-label",
                transparent ? "fusion-button-ghost-label" : "fusion-button-primary-label"
        );

        VBox content = new VBox(textLabel);
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().add("fusion-button-body");

        ActionButtonPane pane = new ActionButtonPane(transparent, action);
        pane.getContentPane().getChildren().add(content);
        decorateButtonRegion(
                pane.getNode(),
                prefWidth,
                prefHeight,
                "fusion-node",
                transparent ? "fusion-button-ghost" : "fusion-button-primary"
        );
        return pane;
    }

    private static void decorateButtonRegion(Region region, double prefWidth, double prefHeight, String... styleClasses) {
        if (prefWidth > 0) {
            region.setPrefWidth(prefWidth);
        }
        if (prefHeight > 0) {
            region.setPrefHeight(prefHeight);
            region.setMinHeight(Region.USE_PREF_SIZE);
        }
        region.setMaxWidth(Double.MAX_VALUE);
        region.getStyleClass().addAll(styleClasses);
    }

    /**
     * 只有按钮继续走 vfx，因为这里确实需要它的按压反馈。
     */
    private static final class ActionButtonPane extends FusionPane {

        private final boolean transparent;
        private final Runnable action;

        private ActionButtonPane(boolean transparent, Runnable action) {
            this.transparent = transparent;
            this.action = action;
            getNode().setCursor(Cursor.HAND);
        }

        @Override
        protected AbstractFusionPane buildRootNode() {
            return new FusionPaneImpl() {
                @Override
                protected Color normalColor() {
                    if (transparent) {
                        return Theme.current().transparentFusionButtonNormalBackgroundColor();
                    }
                    return Theme.current().fusionButtonNormalBackgroundColor();
                }

                @Override
                protected Color hoverColor() {
                    if (transparent) {
                        return Theme.current().transparentFusionButtonHoverBackgroundColor();
                    }
                    return Theme.current().fusionButtonHoverBackgroundColor();
                }

                @Override
                protected Color downColor() {
                    if (transparent) {
                        return Theme.current().transparentFusionButtonDownBackgroundColor();
                    }
                    return Theme.current().fusionButtonDownBackgroundColor();
                }

                @Override
                protected void onMouseClicked() {
                    if (action != null) {
                        action.run();
                    }
                }
            };
        }
    }

    public static final class SurfaceCard {

        private final StackPane node;

        private SurfaceCard(StackPane node) {
            this.node = node;
        }

        public StackPane getNode() {
            return node;
        }
    }
}
