package com.scau.dormrepair.ui.support;

import com.scau.dormrepair.ui.component.FusionUiFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * 统一项目内的轻提示弹窗。
 * 这里只保留一层圆角内容卡片，不再额外套标题栏或外层壳。
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

    public static void error(String title, Throwable throwable) {
        show(AlertTone.ERROR, title, UiErrorMessages.resolve(throwable));
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

        Stage[] stageRef = new Stage[1];
        var okButton = FusionUiFactory.createPrimaryButton("\u786e\u5b9a", 132, 38, () -> {
            if (stageRef[0] != null) {
                stageRef[0].close();
            }
        });
        okButton.getNode().getStyleClass().add("dialog-confirm-button");

        HBox actionRow = new HBox(okButton.getNode());
        actionRow.setAlignment(Pos.CENTER);

        VBox body = new VBox(18, chipLabel, titleLabel, contentLabel, actionRow);
        body.getStyleClass().addAll("dialog-shell", tone.shellStyleClass);
        body.setPadding(new Insets(24, 26, 24, 26));
        body.setPrefWidth(380);
        body.setMaxWidth(380);

        Scene scene = new Scene(body);
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
        INFO("\u64cd\u4f5c\u63d0\u793a", "dialog-chip-info", "dialog-shell-info"),
        ERROR("\u5931\u8d25\u63d0\u793a", "dialog-chip-error", "dialog-shell-error");

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
