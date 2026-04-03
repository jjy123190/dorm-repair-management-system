package com.scau.dormrepair.ui.support;

import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Adds native-feeling 8-direction resize behavior for transparent custom stages.
 */
public final class WindowResizeSupport {

    private static final double DEFAULT_RESIZE_MARGIN = 8.0;

    private final Stage stage;
    private final Scene scene;
    private final double resizeMargin;

    private ResizeMode hoveredMode = ResizeMode.NONE;
    private ResizeMode activeMode = ResizeMode.NONE;
    private double dragScreenX;
    private double dragScreenY;
    private double dragStageX;
    private double dragStageY;
    private double dragStageWidth;
    private double dragStageHeight;

    private WindowResizeSupport(Stage stage, double resizeMargin) {
        this.stage = stage;
        this.scene = stage.getScene();
        this.resizeMargin = resizeMargin;
    }

    public static void install(Stage stage) {
        install(stage, DEFAULT_RESIZE_MARGIN);
    }

    public static void install(Stage stage, double resizeMargin) {
        if (stage == null || stage.getScene() == null) {
            throw new IllegalArgumentException("Stage and scene must be initialized before installing resize support.");
        }
        WindowResizeSupport support = new WindowResizeSupport(stage, resizeMargin);
        support.bind();
    }

    private void bind() {
        scene.addEventHandler(MouseEvent.MOUSE_MOVED, this::handleMouseMoved);
        scene.addEventHandler(MouseEvent.MOUSE_EXITED, this::handleMouseExited);
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
    }

    private void handleMouseMoved(MouseEvent event) {
        if (!isResizeAvailable()) {
            applyCursor(ResizeMode.NONE);
            return;
        }
        hoveredMode = resolveResizeMode(event.getSceneX(), event.getSceneY());
        applyCursor(hoveredMode);
    }

    private void handleMouseExited(MouseEvent event) {
        if (activeMode == ResizeMode.NONE) {
            applyCursor(ResizeMode.NONE);
        }
    }

    private void handleMousePressed(MouseEvent event) {
        if (!isResizeAvailable() || event.getButton() != MouseButton.PRIMARY) {
            activeMode = ResizeMode.NONE;
            return;
        }
        ResizeMode mode = resolveResizeMode(event.getSceneX(), event.getSceneY());
        if (mode == ResizeMode.NONE) {
            activeMode = ResizeMode.NONE;
            return;
        }

        activeMode = mode;
        dragScreenX = event.getScreenX();
        dragScreenY = event.getScreenY();
        dragStageX = stage.getX();
        dragStageY = stage.getY();
        dragStageWidth = stage.getWidth();
        dragStageHeight = stage.getHeight();
        applyCursor(activeMode);
        event.consume();
    }

    private void handleMouseDragged(MouseEvent event) {
        if (activeMode == ResizeMode.NONE || !isResizeAvailable()) {
            return;
        }

        double deltaX = event.getScreenX() - dragScreenX;
        double deltaY = event.getScreenY() - dragScreenY;
        double minWidth = Math.max(stage.getMinWidth(), 0.0);
        double minHeight = Math.max(stage.getMinHeight(), 0.0);

        double targetX = dragStageX;
        double targetY = dragStageY;
        double targetWidth = dragStageWidth;
        double targetHeight = dragStageHeight;

        if (activeMode.affectsLeft()) {
            double candidateWidth = dragStageWidth - deltaX;
            if (candidateWidth >= minWidth) {
                targetX = dragStageX + deltaX;
                targetWidth = candidateWidth;
            } else {
                targetX = dragStageX + (dragStageWidth - minWidth);
                targetWidth = minWidth;
            }
        } else if (activeMode.affectsRight()) {
            targetWidth = Math.max(minWidth, dragStageWidth + deltaX);
        }

        if (activeMode.affectsTop()) {
            double candidateHeight = dragStageHeight - deltaY;
            if (candidateHeight >= minHeight) {
                targetY = dragStageY + deltaY;
                targetHeight = candidateHeight;
            } else {
                targetY = dragStageY + (dragStageHeight - minHeight);
                targetHeight = minHeight;
            }
        } else if (activeMode.affectsBottom()) {
            targetHeight = Math.max(minHeight, dragStageHeight + deltaY);
        }

        Rectangle2D bounds = resolveScreenBounds();
        if (bounds != null) {
            double maxX = bounds.getMaxX();
            double maxY = bounds.getMaxY();
            targetX = Math.min(targetX, maxX - minWidth);
            targetY = Math.min(targetY, maxY - minHeight);
        }

        stage.setX(targetX);
        stage.setY(targetY);
        stage.setWidth(targetWidth);
        stage.setHeight(targetHeight);
        event.consume();
    }

    private void handleMouseReleased(MouseEvent event) {
        if (activeMode == ResizeMode.NONE) {
            return;
        }
        activeMode = ResizeMode.NONE;
        hoveredMode = resolveResizeMode(event.getSceneX(), event.getSceneY());
        applyCursor(hoveredMode);
        event.consume();
    }

    private boolean isResizeAvailable() {
        return stage.isShowing()
                && stage.isResizable()
                && !stage.isFullScreen()
                && !stage.isMaximized();
    }

    private ResizeMode resolveResizeMode(double sceneX, double sceneY) {
        boolean nearLeft = sceneX <= resizeMargin;
        boolean nearRight = sceneX >= scene.getWidth() - resizeMargin;
        boolean nearTop = sceneY <= resizeMargin;
        boolean nearBottom = sceneY >= scene.getHeight() - resizeMargin;

        if (nearTop && nearLeft) {
            return ResizeMode.NORTH_WEST;
        }
        if (nearTop && nearRight) {
            return ResizeMode.NORTH_EAST;
        }
        if (nearBottom && nearLeft) {
            return ResizeMode.SOUTH_WEST;
        }
        if (nearBottom && nearRight) {
            return ResizeMode.SOUTH_EAST;
        }
        if (nearLeft) {
            return ResizeMode.WEST;
        }
        if (nearRight) {
            return ResizeMode.EAST;
        }
        if (nearTop) {
            return ResizeMode.NORTH;
        }
        if (nearBottom) {
            return ResizeMode.SOUTH;
        }
        return ResizeMode.NONE;
    }

    private Rectangle2D resolveScreenBounds() {
        Screen currentScreen = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight())
                .stream()
                .findFirst()
                .orElse(Screen.getPrimary());
        return currentScreen == null ? null : currentScreen.getVisualBounds();
    }

    private void applyCursor(ResizeMode mode) {
        Cursor nextCursor = mode.cursor();
        if (scene.getCursor() != nextCursor) {
            scene.setCursor(nextCursor);
        }
    }

    private enum ResizeMode {
        NONE(Cursor.DEFAULT, false, false, false, false),
        NORTH(Cursor.N_RESIZE, true, false, false, false),
        SOUTH(Cursor.S_RESIZE, false, true, false, false),
        EAST(Cursor.E_RESIZE, false, false, false, true),
        WEST(Cursor.W_RESIZE, false, false, true, false),
        NORTH_EAST(Cursor.NE_RESIZE, true, false, false, true),
        NORTH_WEST(Cursor.NW_RESIZE, true, false, true, false),
        SOUTH_EAST(Cursor.SE_RESIZE, false, true, false, true),
        SOUTH_WEST(Cursor.SW_RESIZE, false, true, true, false);

        private final Cursor cursor;
        private final boolean top;
        private final boolean bottom;
        private final boolean left;
        private final boolean right;

        ResizeMode(Cursor cursor, boolean top, boolean bottom, boolean left, boolean right) {
            this.cursor = cursor;
            this.top = top;
            this.bottom = bottom;
            this.left = left;
            this.right = right;
        }

        private Cursor cursor() {
            return cursor;
        }

        private boolean affectsTop() {
            return top;
        }

        private boolean affectsBottom() {
            return bottom;
        }

        private boolean affectsLeft() {
            return left;
        }

        private boolean affectsRight() {
            return right;
        }
    }
}
