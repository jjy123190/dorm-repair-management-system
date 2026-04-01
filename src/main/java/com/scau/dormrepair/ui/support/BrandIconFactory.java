package com.scau.dormrepair.ui.support;

import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public final class BrandIconFactory {

    private static final double ICON_SIZE = 1024.0;

    private BrandIconFactory() {
    }

    public static StackPane createLogo(double size) {
        Group iconGroup = buildIconGroup();
        double scale = size / ICON_SIZE;
        iconGroup.setScaleX(scale);
        iconGroup.setScaleY(scale);

        StackPane wrapper = new StackPane(iconGroup);
        wrapper.getStyleClass().add("login-logo-mark");
        wrapper.setMinSize(size, size);
        wrapper.setPrefSize(size, size);
        wrapper.setMaxSize(size, size);
        wrapper.setPickOnBounds(false);
        return wrapper;
    }

    public static Image createWindowIcon(double size) {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        WritableImage image = createLogo(size).snapshot(parameters, null);
        return image;
    }

    public static void attachStageIcon(Stage stage) {
        if (stage == null) {
            return;
        }
        stage.getIcons().clear();
        stage.getIcons().add(createWindowIcon(128));
    }

    private static Group buildIconGroup() {
        Rectangle background = roundedRect(0, 0, 1024, 1024, 235, Color.web("#EEEDE9"));
        Rectangle clipShape = roundedRect(0, 0, 1024, 1024, 235, Color.BLACK);

        Polygon lowerRightHalf = new Polygon(
                1024.0, 0.0,
                1024.0, 1024.0,
                0.0, 1024.0
        );
        lowerRightHalf.setFill(Color.web("#D8EEE4"));

        Line diagonal = new Line(0, 0, 1024, 1024);
        diagonal.setStroke(Color.web("#1D9E75"));
        diagonal.setStrokeWidth(7);
        diagonal.setOpacity(0.5);

        Circle upperLeftRing = new Circle(348, 348, 88);
        upperLeftRing.setFill(Color.TRANSPARENT);
        upperLeftRing.setStroke(Color.web("#C0C0BC"));
        upperLeftRing.setStrokeWidth(18);

        Polyline checkMark = new Polyline(
                604.0, 748.0,
                727.0, 870.0,
                922.0, 584.0
        );
        checkMark.setFill(Color.TRANSPARENT);
        checkMark.setStroke(Color.web("#1D9E75"));
        checkMark.setStrokeWidth(104);
        checkMark.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        checkMark.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);

        Group overlay = new Group(lowerRightHalf, diagonal, upperLeftRing, checkMark);
        overlay.setClip(clipShape);

        return new Group(background, overlay);
    }

    private static Rectangle roundedRect(double x, double y, double width, double height, double radius, Color fill) {
        Rectangle rectangle = new Rectangle(x, y, width, height);
        rectangle.setArcWidth(radius * 2);
        rectangle.setArcHeight(radius * 2);
        rectangle.setFill(fill);
        return rectangle;
    }
}
