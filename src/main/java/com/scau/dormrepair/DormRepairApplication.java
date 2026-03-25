package com.scau.dormrepair;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.ui.AppShell;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.theme.DormVfxTheme;
import io.vproxy.vfx.theme.Theme;
import io.vproxy.vfx.ui.scene.VScene;
import io.vproxy.vfx.ui.scene.VSceneRole;
import io.vproxy.vfx.ui.stage.VStage;
import io.vproxy.vfx.ui.stage.VStageInitParams;
import io.vproxy.vfx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class DormRepairApplication extends Application {

    private AppContext appContext;

    @Override
    public void start(Stage primaryStage) {
        try {
            appContext = AppContext.bootstrap();
            AppShell appShell = new AppShell(appContext);
            Parent appRoot = appShell.createContent();

            // 顶层窗口壳必须回到 vfx，用户不接受系统默认标题栏。
            VScene mainScene = new VScene(VSceneRole.MAIN);
            mainScene.enableAutoContentWidthHeight();

            VStage stage = new VStage(primaryStage, new VStageInitParams().setInitialScene(mainScene));
            stage.setTitle(appContext.properties().title());
            stage.useLightBorder();

            if (appRoot instanceof Region region) {
                FXUtils.observeWidthHeight(mainScene.getContentPane(), region);
            }
            mainScene.getContentPane().getChildren().add(appRoot);

            if (getClass().getResource("/styles/app.css") != null) {
                stage.getStage().getScene().getStylesheets().add(
                        getClass().getResource("/styles/app.css").toExternalForm()
                );
            }

            stage.getStage().setMinWidth(appContext.properties().ui().minWidth());
            stage.getStage().setMinHeight(appContext.properties().ui().minHeight());
            stage.getStage().setWidth(appContext.properties().ui().width());
            stage.getStage().setHeight(appContext.properties().ui().height());
            stage.getStage().centerOnScreen();
            stage.show();
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

    public static void main(String[] args) {
        installTheme();
        launch(args);
    }

    private static void installTheme() {
        try {
            Theme.setTheme(new DormVfxTheme());
        } catch (IllegalStateException ignored) {
            // 主题只能初始化一次，重复运行时沿用现有主题即可。
        }
    }

    private void showStartupError(Exception exception) {
        // 启动失败也统一走项目自己的弹窗风格，避免回退到系统默认 Alert。
        UiAlerts.error("启动失败", exception);
        Platform.exit();
    }
}
