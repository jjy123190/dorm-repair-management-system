package com.scau.dormrepair.ui.support;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;

/**
 * Keep popup and panel scrolling smooth without moving the control body itself.
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

    public static void installSmoothScrollPane(ScrollPane scrollPane) {
        scrollPane.viewportBoundsProperty().addListener((observable, oldBounds, newBounds) -> Platform.runLater(() -> bindScrollPaneScroll(scrollPane)));
        scrollPane.contentProperty().addListener((observable, oldContent, newContent) -> Platform.runLater(() -> bindScrollPaneScroll(scrollPane)));
        Platform.runLater(() -> bindScrollPaneScroll(scrollPane));
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

            double currentTarget = readTarget(verticalBar);
            double deltaRatio = -event.getDeltaY() / Math.max(520.0, listView.getItems().size() * 36.0);
            double nextTarget = clamp(currentTarget + deltaRatio, 0.0, 1.0);
            writeTarget(verticalBar, nextTarget);
            playSmoothScroll(verticalBar, nextTarget, 180);
            event.consume();
        });

        listView.getProperties().put(SMOOTH_SCROLL_BOUND, Boolean.TRUE);
    }

    private static void bindScrollPaneScroll(ScrollPane scrollPane) {
        if (Boolean.TRUE.equals(scrollPane.getProperties().get(SMOOTH_SCROLL_BOUND))) {
            return;
        }
        if (scrollPane.getContent() == null) {
            return;
        }

        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() == 0 || Math.abs(event.getDeltaY()) <= Math.abs(event.getDeltaX())) {
                return;
            }

            double currentTarget = readTarget(scrollPane);
            double normalizedDelta = resolveNormalizedDelta(scrollPane, event.getDeltaY());
            if (Math.abs(normalizedDelta) < 0.000001) {
                return;
            }

            double nextTarget = clamp(currentTarget + normalizedDelta, 0.0, 1.0);
            if (Math.abs(nextTarget - currentTarget) < 0.000001) {
                return;
            }

            writeTarget(scrollPane, nextTarget);
            playSmoothScroll(scrollPane, nextTarget, event.isInertia() ? 110 : 170);
            event.consume();
        });

        scrollPane.getProperties().put(SMOOTH_SCROLL_BOUND, Boolean.TRUE);
    }

    private static double resolveNormalizedDelta(ScrollPane scrollPane, double deltaY) {
        Node content = scrollPane.getContent();
        if (content == null) {
            return 0.0;
        }

        Bounds contentBounds = content.getLayoutBounds();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double scrollableHeight = contentBounds.getHeight() - viewportHeight;
        if (scrollableHeight <= 0.0) {
            return 0.0;
        }

        double ratio = -deltaY / scrollableHeight;
        return clamp(ratio * 1.55, -0.22, 0.22);
    }

    private static ScrollBar findVerticalScrollBar(Node scrollHost) {
        for (Node node : scrollHost.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar && scrollBar.getOrientation() == Orientation.VERTICAL) {
                return scrollBar;
            }
        }
        return null;
    }

    private static void playSmoothScroll(ScrollBar scrollBar, double targetValue, double durationMillis) {
        Timeline oldTimeline = (Timeline) scrollBar.getProperties().get(SMOOTH_SCROLL_TIMELINE);
        if (oldTimeline != null) {
            oldTimeline.stop();
        }

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(scrollBar.valueProperty(), scrollBar.getValue(), Interpolator.EASE_OUT)
                ),
                new KeyFrame(
                        Duration.millis(durationMillis),
                        new KeyValue(scrollBar.valueProperty(), targetValue, Interpolator.SPLINE(0.22, 0.78, 0.18, 1.0))
                )
        );
        timeline.setOnFinished(event -> {
            scrollBar.getProperties().remove(SMOOTH_SCROLL_TIMELINE);
            writeTarget(scrollBar, scrollBar.getValue());
        });
        scrollBar.getProperties().put(SMOOTH_SCROLL_TIMELINE, timeline);
        timeline.play();
    }

    private static void playSmoothScroll(ScrollPane scrollPane, double targetValue, double durationMillis) {
        Timeline oldTimeline = (Timeline) scrollPane.getProperties().get(SMOOTH_SCROLL_TIMELINE);
        if (oldTimeline != null) {
            oldTimeline.stop();
        }

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(scrollPane.vvalueProperty(), scrollPane.getVvalue(), Interpolator.LINEAR)
                ),
                new KeyFrame(
                        Duration.millis(durationMillis),
                        new KeyValue(scrollPane.vvalueProperty(), targetValue, Interpolator.SPLINE(0.18, 0.88, 0.22, 1.0))
                )
        );
        timeline.setOnFinished(event -> {
            scrollPane.getProperties().remove(SMOOTH_SCROLL_TIMELINE);
            scrollPane.setVvalue(targetValue);
            writeTarget(scrollPane, targetValue);
        });
        scrollPane.getProperties().put(SMOOTH_SCROLL_TIMELINE, timeline);
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

    private static double readTarget(ScrollPane scrollPane) {
        Timeline timeline = (Timeline) scrollPane.getProperties().get(SMOOTH_SCROLL_TIMELINE);
        Object target = scrollPane.getProperties().get(SMOOTH_SCROLL_TARGET);
        if (timeline != null && timeline.getStatus() == Animation.Status.RUNNING && target instanceof Number number) {
            return number.doubleValue();
        }
        return scrollPane.getVvalue();
    }

    private static void writeTarget(ScrollPane scrollPane, double targetValue) {
        scrollPane.getProperties().put(SMOOTH_SCROLL_TARGET, targetValue);
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
