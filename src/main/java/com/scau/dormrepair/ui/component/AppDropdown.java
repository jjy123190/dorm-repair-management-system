package com.scau.dormrepair.ui.component;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;

/**
 * 项目统一下拉组件。
 * 这里不用 JavaFX 默认 ComboBox，而是自己控制本体高度、弹层样式和滚轮缓动，
 * 这样可以避免默认皮肤带来的尺寸跳动和下拉弹层观感不稳定。
 */
public class AppDropdown<T> extends VBox {

    private static final double CONTROL_HEIGHT = 44.0;
    private static final double OPTION_HEIGHT = 42.0;
    private static final double POPUP_PADDING = 12.0;
    private static final double MIN_WHEEL_STEP = 6.0;
    private static final double MAX_WHEEL_STEP = 28.0;
    private static final double WHEEL_STEP_FACTOR = 0.48;
    private static final Duration SCROLL_DURATION = Duration.millis(220);

    private final ObservableList<T> items;
    private final ObjectProperty<T> value;
    private final StringProperty promptText;
    private final Label valueLabel;
    private final Label arrowLabel;
    private final VBox optionList;
    private final ScrollPane scrollPane;
    private final Popup popup;

    private Timeline scrollTimeline;
    private double scrollTargetOffset;
    private Function<T, String> textMapper;
    private int visibleRowCount;

    public AppDropdown() {
        this.items = FXCollections.observableArrayList();
        this.value = new SimpleObjectProperty<>();
        this.promptText = new SimpleStringProperty("");
        this.valueLabel = new Label();
        this.arrowLabel = new Label("▼");
        this.optionList = new VBox(2);
        this.scrollPane = new ScrollPane(optionList);
        this.popup = new Popup();
        this.textMapper = item -> item == null ? "" : String.valueOf(item);
        this.visibleRowCount = 5;

        getStyleClass().add("app-dropdown");
        setAlignment(Pos.CENTER_LEFT);
        setFillWidth(true);
        setMinHeight(CONTROL_HEIGHT);
        setPrefHeight(CONTROL_HEIGHT);
        setMaxHeight(CONTROL_HEIGHT);
        setMaxWidth(Double.MAX_VALUE);

        valueLabel.getStyleClass().add("app-dropdown-value");
        valueLabel.setMaxWidth(Double.MAX_VALUE);

        arrowLabel.getStyleClass().add("app-dropdown-arrow");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox trigger = new HBox(10, valueLabel, spacer, arrowLabel);
        trigger.getStyleClass().add("app-dropdown-trigger");
        trigger.setAlignment(Pos.CENTER_LEFT);
        trigger.setPadding(new Insets(0, 14, 0, 16));
        trigger.setMinHeight(CONTROL_HEIGHT);
        trigger.setPrefHeight(CONTROL_HEIGHT);
        trigger.setMaxHeight(CONTROL_HEIGHT);
        trigger.setMaxWidth(Double.MAX_VALUE);
        trigger.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (!isDisabled()) {
                togglePopup();
            }
        });

        optionList.getStyleClass().add("app-dropdown-option-list");
        optionList.setFillWidth(true);
        optionList.setPadding(new Insets(4));

        scrollPane.getStyleClass().add("app-dropdown-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPannable(false);
        scrollPane.setFocusTraversable(false);
        scrollPane.addEventFilter(ScrollEvent.SCROLL, this::handlePopupScroll);

        StackPane popupShell = new StackPane(scrollPane);
        popupShell.getStyleClass().add("app-dropdown-popup");
        popupShell.setMinWidth(180);
        popupShell.setMaxWidth(Double.MAX_VALUE);

        popup.getContent().add(popupShell);
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.setHideOnEscape(true);
        popup.setOnHidden(event -> stopScrollTimeline());

        value.addListener((observable, oldValue, newValue) -> {
            refreshValueLabel();
            rebuildOptions();
        });
        promptText.addListener((observable, oldValue, newValue) -> refreshValueLabel());
        items.addListener((javafx.collections.ListChangeListener<T>) change -> rebuildOptions());
        disabledProperty().addListener((observable, oldValue, newValue) -> {
            if (Boolean.TRUE.equals(newValue)) {
                popup.hide();
            }
            pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("disabled"), Boolean.TRUE.equals(newValue));
        });

        getChildren().add(trigger);
        rebuildOptions();
        refreshValueLabel();
    }

    public ObservableList<T> getItems() {
        return items;
    }

    public void setItems(List<T> newItems) {
        items.setAll(newItems);
    }

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public T getValue() {
        return value.get();
    }

    public void setValue(T newValue) {
        value.set(newValue);
    }

    public void clearSelection() {
        value.set(null);
    }

    public StringProperty promptTextProperty() {
        return promptText;
    }

    public void setPromptText(String prompt) {
        promptText.set(prompt);
    }

    public void setTextMapper(Function<T, String> textMapper) {
        this.textMapper = Objects.requireNonNull(textMapper);
        rebuildOptions();
        refreshValueLabel();
    }

    public void setVisibleRowCount(int visibleRowCount) {
        this.visibleRowCount = Math.max(3, visibleRowCount);
        updatePopupHeight();
    }

    private void togglePopup() {
        if (popup.isShowing()) {
            popup.hide();
            return;
        }
        showPopup();
    }

    private void showPopup() {
        if (getScene() == null || getScene().getWindow() == null) {
            return;
        }

        rebuildOptions();

        Bounds bounds = localToScreen(getBoundsInLocal());
        if (bounds == null) {
            return;
        }

        Node popupRoot = popup.getContent().get(0);
        if (popupRoot instanceof Region region) {
            region.setPrefWidth(getWidth());
            region.setMinWidth(getWidth());
            region.setMaxWidth(getWidth());
        }

        Point2D popupPoint = new Point2D(bounds.getMinX(), bounds.getMaxY() + 4);
        scrollToCurrentSelection();
        popup.show(getScene().getWindow(), popupPoint.getX(), popupPoint.getY());
    }

    private void rebuildOptions() {
        optionList.getChildren().clear();
        for (T item : items) {
            optionList.getChildren().add(createOptionNode(item));
        }
        updatePopupHeight();
    }

    private Node createOptionNode(T item) {
        Label optionLabel = new Label(displayText(item));
        optionLabel.setMaxWidth(Double.MAX_VALUE);

        HBox optionRow = new HBox(optionLabel);
        optionRow.getStyleClass().add("app-dropdown-option");
        optionRow.setAlignment(Pos.CENTER_LEFT);
        optionRow.setPadding(new Insets(10, 14, 10, 14));
        optionRow.setMinHeight(OPTION_HEIGHT);
        optionRow.setPrefHeight(OPTION_HEIGHT);
        optionRow.setMaxWidth(Double.MAX_VALUE);
        optionRow.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            value.set(item);
            popup.hide();
        });

        if (Objects.equals(item, value.get())) {
            optionRow.getStyleClass().add("app-dropdown-option-selected");
        }
        return optionRow;
    }

    private void refreshValueLabel() {
        T selected = value.get();
        boolean hasValue = selected != null;
        valueLabel.setText(hasValue ? displayText(selected) : promptText.get());
        valueLabel.getStyleClass().remove("app-dropdown-value-placeholder");
        if (!hasValue) {
            valueLabel.getStyleClass().add("app-dropdown-value-placeholder");
        }
    }

    private String displayText(T item) {
        String text = textMapper.apply(item);
        return text == null ? "" : text;
    }

    private void handlePopupScroll(ScrollEvent event) {
        if (event.getDeltaY() == 0) {
            return;
        }

        double maxOffset = maxScrollableOffset();
        if (maxOffset <= 0) {
            return;
        }

        double currentOffset = currentScrollableOffset();
        double nextOffset = clamp(
                currentOffset - wheelOffsetDelta(event.getDeltaY()),
                0,
                maxOffset
        );
        playSmoothScroll(currentOffset, nextOffset);
        event.consume();
    }

    private void updatePopupHeight() {
        int visibleCount = Math.max(1, Math.min(visibleRowCount, Math.max(items.size(), 1)));
        double viewportHeight = visibleCount * OPTION_HEIGHT + 8;
        scrollPane.setPrefViewportHeight(viewportHeight);
        scrollPane.setMinViewportHeight(viewportHeight);
        scrollPane.setPrefHeight(viewportHeight + POPUP_PADDING);
        scrollPane.setMinHeight(viewportHeight + POPUP_PADDING);
    }

    private void scrollToCurrentSelection() {
        double targetOffset = 0;
        int selectedIndex = items.indexOf(value.get());
        if (selectedIndex >= 0) {
            double preferredOffset = selectedIndex * OPTION_HEIGHT - OPTION_HEIGHT * 0.5;
            targetOffset = clamp(preferredOffset, 0, maxScrollableOffset());
        }
        scrollTargetOffset = targetOffset;
        scrollPane.setVvalue(offsetToVvalue(targetOffset));
    }

    private void playSmoothScroll(double currentOffset, double targetOffset) {
        stopScrollTimeline();

        scrollTargetOffset = targetOffset;
        scrollTimeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(scrollPane.vvalueProperty(), offsetToVvalue(currentOffset), Interpolator.EASE_OUT)
                ),
                new KeyFrame(
                        SCROLL_DURATION,
                        new KeyValue(
                                scrollPane.vvalueProperty(),
                                offsetToVvalue(targetOffset),
                                Interpolator.SPLINE(0.18, 0.82, 0.18, 1.0)
                        )
                )
        );
        scrollTimeline.play();
    }

    private void stopScrollTimeline() {
        if (scrollTimeline != null) {
            scrollTimeline.stop();
            scrollTimeline = null;
        }
    }

    private double maxScrollableOffset() {
        double contentHeight = Math.max(optionList.getBoundsInLocal().getHeight(), 1);
        double viewportHeight = Math.max(scrollPane.getViewportBounds().getHeight(), 1);
        return Math.max(contentHeight - viewportHeight, 0);
    }

    private double currentScrollableOffset() {
        return maxScrollableOffset() * clamp(scrollPane.getVvalue(), 0, 1);
    }

    private double offsetToVvalue(double offset) {
        double maxOffset = maxScrollableOffset();
        if (maxOffset <= 0) {
            return 0;
        }
        return clamp(offset / maxOffset, 0, 1);
    }

    private double wheelOffsetDelta(double deltaY) {
        double scaledDelta = Math.abs(deltaY) * WHEEL_STEP_FACTOR;
        double offsetDelta = clamp(scaledDelta, MIN_WHEEL_STEP, MAX_WHEEL_STEP);
        return Math.signum(deltaY) * offsetDelta;
    }

    private double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
