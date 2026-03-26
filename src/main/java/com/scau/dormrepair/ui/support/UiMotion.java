package com.scau.dormrepair.ui.support;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;

/**
 * 这里只处理下拉弹层里的滚动手感。
 * 组件本体保持固定，不再给 ComboBox 本身挂任何位移、缩放类动画。
 */
public final class UiMotion {

    private static final String SMOOTH_SCROLL_BOUND = "codex.smooth.scroll.bound";
    private static final String SMOOTH_SCROLL_TARGET = "codex.smooth.scroll.target";
    private static final String SMOOTH_SCROLL_TIMELINE = "codex.smooth.scroll.timeline";

    private UiMotion() {
    }

    public static <T> void installSmoothDropdown(ComboBox<T> comboBox) {
        comboBox.showingProperty().addListener((observable, oldValue, showing) -> {
            if (Boolean.TRUE.equals(showing)) {
                Platform.runLater(() -> bindPopupScroll(comboBox));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> void bindPopupScroll(ComboBox<T> comboBox) {
        if (!(comboBox.getSkin() instanceof ComboBoxListViewSkin<?> rawSkin)) {
            return;
        }

        Node popupContent = rawSkin.getPopupContent();
        if (!(popupContent instanceof ListView<?> rawListView)) {
            return;
        }

        ListView<T> listView = (ListView<T>) rawListView;
        if (Boolean.TRUE.equals(listView.getProperties().get(SMOOTH_SCROLL_BOUND))) {
            return;
        }

        ScrollBar verticalBar = findVerticalScrollBar(listView);
        if (verticalBar == null) {
            Platform.runLater(() -> bindPopupScroll(comboBox));
            return;
        }

        listView.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() == 0) {
                return;
            }

            double baseStep = Math.max(0.045, 1.0 / Math.max(18.0, listView.getItems().size() * 2.2));
            double deltaFactor = Math.min(3.2, Math.max(0.45, Math.abs(event.getDeltaY()) / 80.0));
            double direction = event.getDeltaY() > 0 ? -1.0 : 1.0;

            double currentTarget = readTarget(verticalBar);
            double nextTarget = clamp(currentTarget + direction * baseStep * deltaFactor, 0.0, 1.0);
            writeTarget(verticalBar, nextTarget);
            playSmoothScroll(verticalBar, nextTarget);
            event.consume();
        });

        listView.getProperties().put(SMOOTH_SCROLL_BOUND, Boolean.TRUE);
    }

    private static ScrollBar findVerticalScrollBar(ListView<?> listView) {
        for (Node node : listView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar && scrollBar.getOrientation() == Orientation.VERTICAL) {
                return scrollBar;
            }
        }
        return null;
    }

    private static void playSmoothScroll(ScrollBar scrollBar, double targetValue) {
        Timeline oldTimeline = (Timeline) scrollBar.getProperties().get(SMOOTH_SCROLL_TIMELINE);
        if (oldTimeline != null) {
            oldTimeline.stop();
        }

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(110),
                        new KeyValue(scrollBar.valueProperty(), targetValue)
                )
        );
        timeline.setOnFinished(event -> scrollBar.getProperties().remove(SMOOTH_SCROLL_TIMELINE));
        scrollBar.getProperties().put(SMOOTH_SCROLL_TIMELINE, timeline);
        timeline.play();
    }

    private static double readTarget(ScrollBar scrollBar) {
        Object target = scrollBar.getProperties().get(SMOOTH_SCROLL_TARGET);
        if (target instanceof Number number) {
            return number.doubleValue();
        }
        return scrollBar.getValue();
    }

    private static void writeTarget(ScrollBar scrollBar, double targetValue) {
        scrollBar.getProperties().put(SMOOTH_SCROLL_TARGET, targetValue);
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
