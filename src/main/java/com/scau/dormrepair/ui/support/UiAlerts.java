package com.scau.dormrepair.ui.support;

import com.scau.dormrepair.ui.component.FusionUiFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * 业务提示统一走项目自己的轻量弹窗。
 * 这里不再使用带标题栏的 VStage 外壳，只保留中间那张圆角卡片。
 */
public final class UiAlerts {

    private UiAlerts() {
    }

    public static void info(String title, String content) {
        show(AlertTone.INFO, title, content);
    }

    public static void error(String title, String content) {
        show(AlertTone.ERROR, title, UiErrorMessages.resolve(content));
    }

    private static void show(AlertTone tone, String title, String content) {
        Window owner = resolveOwner();

        Label chipLabel = new Label(tone.chipText);
        chipLabel.getStyleClass().addAll("dialog-chip", tone.chipStyleClass);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dialog-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("dialog-content");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Stage[] stageRef = new Stage[1];
        var okButton = FusionUiFactory.createPrimaryButton("确定", 108, 36, () -> {
            if (stageRef[0] != null) {
                stageRef[0].close();
            }
        });

        HBox actionRow = new HBox(12, spacer, okButton.getNode());
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        VBox body = new VBox(16, chipLabel, titleLabel, contentLabel, actionRow);
        body.getStyleClass().addAll("dialog-shell", tone.shellStyleClass);
        body.setPadding(new Insets(20));
        body.setPrefWidth(420);
        body.setMaxWidth(420);

        // 透明舞台只负责承载阴影和居中，视觉上只剩里层圆角卡片。
        StackPane root = new StackPane(body);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(18));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        if (UiAlerts.class.getResource("/styles/app.css") != null) {
            scene.getStylesheets().add(UiAlerts.class.getResource("/styles/app.css").toExternalForm());
        }

        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        stageRef[0] = dialog;
        dialog.setResizable(false);
        dialog.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.setScene(scene);

        // 不再显示外层标题栏，只在显示后按主窗口中心定位这张卡片。
        dialog.setOnShown(event -> {
            if (owner == null) {
                dialog.centerOnScreen();
                return;
            }
            double x = owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2;
            double y = owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2;
            dialog.setX(Math.max(0, x));
            dialog.setY(Math.max(0, y));
        });

        dialog.sizeToScene();
        dialog.showAndWait();
    }

    private static Window resolveOwner() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .filter(Window::isFocused)
                .findFirst()
                .orElseGet(() -> Window.getWindows().stream()
                        .filter(Window::isShowing)
                        .findFirst()
                        .orElse(null));
    }

    private enum AlertTone {
        INFO("操作提示", "dialog-chip-info", "dialog-shell-info"),
        ERROR("失败提示", "dialog-chip-error", "dialog-shell-error");

        private final String chipText;
        private final String chipStyleClass;
        private final String shellStyleClass;

        AlertTone(String chipText, String chipStyleClass, String shellStyleClass) {
            this.chipText = chipText;
            this.chipStyleClass = chipStyleClass;
            this.shellStyleClass = shellStyleClass;
        }
    }
}
