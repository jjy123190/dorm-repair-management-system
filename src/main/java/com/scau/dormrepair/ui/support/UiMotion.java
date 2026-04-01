package com.scau.dormrepair.ui.support;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Transition;
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
    private static final String SMOOTH_SCROLL_TRANSITION = "codex.smooth.scroll.transition";

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
            if (event.getDeltaY() == 0) {
                return;
            }

            double normalizedDelta = resolveNormalizedDelta(scrollPane, event.getDeltaY());
            if (Math.abs(normalizedDelta) < 0.000001) {
                return;
            }

            SmoothScrollTransition oldTransition = (SmoothScrollTransition) scrollPane.getProperties().get(SMOOTH_SCROLL_TRANSITION);
            SmoothScrollTransition transition = new SmoothScrollTransition(scrollPane, oldTransition, normalizedDelta);
            if (oldTransition != null) {
                oldTransition.stop();
            }
            scrollPane.getProperties().put(SMOOTH_SCROLL_TRANSITION, transition);
            transition.play();
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
        return clamp(ratio * 1.35, -0.35, 0.35);
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

    private static boolean sameSign(double left, double right) {
        return (left > 0.0 && right > 0.0) || (left < 0.0 && right < 0.0);
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

    private static final class SmoothScrollTransition extends Transition {

        private static final Duration DURATION = Duration.millis(170);
        private static final double BASE_MODIFIER = 1.0;
        private static final double MAX_MODIFIER = 4.0;

        private final ScrollPane scrollPane;
        private final double startValue;
        private final double targetValue;
        private final double delta;
        private final double modifier;

        private SmoothScrollTransition(ScrollPane scrollPane, SmoothScrollTransition oldTransition, double delta) {
            this.scrollPane = scrollPane;
            this.startValue = scrollPane.getVvalue();
            this.delta = delta;
            this.modifier = resolveModifier(oldTransition, delta);
            this.targetValue = clamp(startValue + delta * modifier * BASE_MODIFIER, 0.0, 1.0);
            setCycleDuration(DURATION);
            setInterpolator(Interpolator.SPLINE(0.20, 0.82, 0.22, 1.0));
            setOnFinished(event -> {
                scrollPane.setVvalue(targetValue);
                scrollPane.getProperties().remove(SMOOTH_SCROLL_TRANSITION);
            });
        }

        @Override
        protected void interpolate(double frac) {
            double value = getInterpolator().interpolate(startValue, targetValue, frac);
            scrollPane.setVvalue(value);
        }

        @Override
        public void play() {
            super.play();
            if (modifier > 1.0) {
                jumpTo(getCycleDuration().multiply(0.10));
            }
        }

        private static double resolveModifier(SmoothScrollTransition oldTransition, double delta) {
            if (oldTransition == null || oldTransition.getStatus() != Animation.Status.RUNNING) {
                return 1.0;
            }
            if (!sameSign(delta, oldTransition.delta)) {
                return 1.0;
            }
            return Math.min(MAX_MODIFIER, oldTransition.modifier + 0.65);
        }
    }
}