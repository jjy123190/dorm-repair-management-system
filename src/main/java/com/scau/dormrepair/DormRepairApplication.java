package com.scau.dormrepair;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.ui.AppShell;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * 桌面端启动入口。
 * 这里负责装配应用上下文，并把主工作台挂到 JavaFX 窗口上。
 */
public class DormRepairApplication extends Application {

    private AppContext appContext;

    @Override
    public void start(Stage primaryStage) {
        try {
            appContext = AppContext.bootstrap();
            AppShell appShell = new AppShell(appContext);
            Scene scene = new Scene(
                    appShell.createContent(),
                    appContext.properties().ui().width(),
                    appContext.properties().ui().height()
            );
            if (getClass().getResource("/styles/app.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
            }

            primaryStage.setTitle(appContext.properties().title());
            primaryStage.setMinWidth(appContext.properties().ui().minWidth());
            primaryStage.setMinHeight(appContext.properties().ui().minHeight());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception exception) {
            showStartupError(exception);
        }
    }

    @Override
    public void stop() {
        if (appContext != null) {
            appContext.close();
        }
    }

    /**
     * 入口 main 方法仍然保留，方便命令行和 IDE 直接运行。
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * 启动失败时给出明确提示，避免同学只看到一串栈追踪。
     */
    private void showStartupError(Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("启动失败");
        alert.setHeaderText("宿舍报修与工单管理系统未能启动");
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
}
