package com.scau.dormrepair.ui.support;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public final class WindowResizeSupport {
    private static final double RESIZE_MARGIN = 10;

    private final Stage stage;
    private ResizeRegion activeRegion = ResizeRegion.NONE;
    private Cursor activeCursor = Cursor.DEFAULT;
    private double pressScreenX;
    private double pressScreenY;
    private double pressStageX;
    private double pressStageY;
    private double pressStageWidth;
    private double pressStageHeight;

    private WindowResizeSupport(Stage stage) {
        this.stage = stage;
    }

    public static void install(Stage stage) {
        if (stage == null || stage.getScene() == null) {
            return;
        }
        new WindowResizeSupport(stage).bind(stage.getScene());
    }

    private void bind(Scene scene) {
        scene.addEventHandler(MouseEvent.MOUSE_MOVED, this::handleMouseMoved);
        scene.addEventHandler(MouseEvent.MOUSE_EXITED, event -> updateCursor(scene, Cursor.DEFAULT));
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            activeRegion = ResizeRegion.NONE;
            updateCursor(scene, detectCursor(event));
        });
    }

    private void handleMouseMoved(MouseEvent event) {
        if (stage.isMaximized() || stage.isFullScreen()) {
            updateCursor(stage.getScene(), Cursor.DEFAULT);
            return;
        }
        updateCursor(stage.getScene(), detectCursor(event));
    }

    private void handleMousePressed(MouseEvent event) {
        if (stage.isMaximized() || stage.isFullScreen()) {
            activeRegion = ResizeRegion.NONE;
            return;
        }
        activeRegion = detectRegion(event);
        if (activeRegion == ResizeRegion.NONE) {
            return;
        }
        pressScreenX = event.getScreenX();
        pressScreenY = event.getScreenY();
        pressStageX = stage.getX();
        pressStageY = stage.getY();
        pressStageWidth = stage.getWidth();
        pressStageHeight = stage.getHeight();
        event.consume();
    }

    private void handleMouseDragged(MouseEvent event) {
        if (activeRegion == ResizeRegion.NONE || stage.isMaximized() || stage.isFullScreen()) {
            return;
        }

        double minWidth = Math.max(stage.getMinWidth(), RESIZE_MARGIN * 2);
        double minHeight = Math.max(stage.getMinHeight(), RESIZE_MARGIN * 2);
        double deltaX = event.getScreenX() - pressScreenX;
        double deltaY = event.getScreenY() - pressScreenY;

        if (activeRegion.adjustsLeft()) {
            double nextX = pressStageX + deltaX;
            double nextWidth = pressStageWidth - deltaX;
            if (nextWidth < minWidth) {
                nextX = pressStageX + (pressStageWidth - minWidth);
                nextWidth = minWidth;
            }
            stage.setX(nextX);
            stage.setWidth(nextWidth);
        } else if (activeRegion.adjustsRight()) {
            stage.setWidth(Math.max(minWidth, pressStageWidth + deltaX));
        }

        if (activeRegion.adjustsTop()) {
            double nextY = pressStageY + deltaY;
            double nextHeight = pressStageHeight - deltaY;
            if (nextHeight < minHeight) {
                nextY = pressStageY + (pressStageHeight - minHeight);
                nextHeight = minHeight;
            }
            stage.setY(nextY);
            stage.setHeight(nextHeight);
        } else if (activeRegion.adjustsBottom()) {
            stage.setHeight(Math.max(minHeight, pressStageHeight + deltaY));
        }

        event.consume();
    }

    private Cursor detectCursor(MouseEvent event) {
        return detectRegion(event).cursor();
    }

    private ResizeRegion detectRegion(MouseEvent event) {
        Scene scene = stage.getScene();
        if (scene == null) {
            return ResizeRegion.NONE;
        }

        boolean left = event.getSceneX() <= RESIZE_MARGIN;
        boolean right = event.getSceneX() >= scene.getWidth() - RESIZE_MARGIN;
        boolean top = event.getSceneY() <= RESIZE_MARGIN;
        boolean bottom = event.getSceneY() >= scene.getHeight() - RESIZE_MARGIN;

        if (left && top) {
            return ResizeRegion.TOP_LEFT;
        }
        if (right && top) {
            return ResizeRegion.TOP_RIGHT;
        }
        if (left && bottom) {
            return ResizeRegion.BOTTOM_LEFT;
        }
        if (right && bottom) {
            return ResizeRegion.BOTTOM_RIGHT;
        }
        if (left) {
            return ResizeRegion.LEFT;
        }
        if (right) {
            return ResizeRegion.RIGHT;
        }
        if (top) {
            return ResizeRegion.TOP;
        }
        if (bottom) {
            return ResizeRegion.BOTTOM;
        }
        return ResizeRegion.NONE;
    }

    private void updateCursor(Scene scene, Cursor cursor) {
        if (scene == null || activeCursor == cursor) {
            return;
        }
        activeCursor = cursor;
        scene.setCursor(cursor);
    }

    private enum ResizeRegion {
        NONE(Cursor.DEFAULT, false, false, false, false),
        LEFT(Cursor.W_RESIZE, true, false, false, false),
        RIGHT(Cursor.E_RESIZE, false, true, false, false),
        TOP(Cursor.N_RESIZE, false, false, true, false),
        BOTTOM(Cursor.S_RESIZE, false, false, false, true),
        TOP_LEFT(Cursor.NW_RESIZE, true, false, true, false),
        TOP_RIGHT(Cursor.NE_RESIZE, false, true, true, false),
        BOTTOM_LEFT(Cursor.SW_RESIZE, true, false, false, true),
        BOTTOM_RIGHT(Cursor.SE_RESIZE, false, true, false, true);

        private final Cursor cursor;
        private final boolean adjustsLeft;
        private final boolean adjustsRight;
        private final boolean adjustsTop;
        private final boolean adjustsBottom;

        ResizeRegion(Cursor cursor, boolean adjustsLeft, boolean adjustsRight, boolean adjustsTop, boolean adjustsBottom) {
            this.cursor = cursor;
            this.adjustsLeft = adjustsLeft;
            this.adjustsRight = adjustsRight;
            this.adjustsTop = adjustsTop;
            this.adjustsBottom = adjustsBottom;
        }

        private Cursor cursor() {
            return cursor;
        }

        private boolean adjustsLeft() {
            return adjustsLeft;
        }

        private boolean adjustsRight() {
            return adjustsRight;
        }

        private boolean adjustsTop() {
            return adjustsTop;
        }

        private boolean adjustsBottom() {
            return adjustsBottom;
        }
    }
}
