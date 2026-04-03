package com.scau.dormrepair;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.config.AppProperties;
import com.scau.dormrepair.ui.AppShell;
import com.scau.dormrepair.ui.support.BrandIconFactory;
import com.scau.dormrepair.ui.support.ProportionalViewport;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.support.WindowResizeSupport;
import com.scau.dormrepair.ui.theme.DormVfxTheme;
import io.vproxy.vfx.theme.Theme;
import io.vproxy.vfx.ui.scene.VScene;
import io.vproxy.vfx.ui.scene.VSceneRole;
import io.vproxy.vfx.ui.stage.VStage;
import io.vproxy.vfx.ui.stage.VStageInitParams;
import io.vproxy.vfx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class DormRepairApplication extends Application {
    private static final double LOGIN_WIDTH_RATIO = 0.84;
    private static final double LOGIN_HEIGHT_RATIO = 0.90;

    private static final double WINDOW_MARGIN = 80;

    private AppContext appContext;

    @Override
    public void start(Stage primaryStage) {
        try {
            appContext = AppContext.bootstrap();
            AppShell appShell = new AppShell(appContext);
            Parent appRoot = appShell.createContent();
            AppProperties.UiProperties ui = appContext.properties().ui();

            ProportionalViewport viewport = new ProportionalViewport(appRoot, ui.designWidth(), ui.designHeight());

            VScene mainScene = new VScene(VSceneRole.MAIN);
            mainScene.enableAutoContentWidthHeight();

            VStage stage = new VStage(primaryStage, new VStageInitParams().setInitialScene(mainScene));
            stage.setTitle(appContext.appSession().isAuthenticated() ? appContext.properties().title() : "");
            stage.useLightBorder();
            BrandIconFactory.attachStageIcon(stage.getStage());

            FXUtils.observeWidthHeight(mainScene.getContentPane(), viewport);
            mainScene.getContentPane().getChildren().add(viewport);

            if (getClass().getResource("/styles/app.css") != null) {
                stage.getStage().getScene().getStylesheets().add(
                        getClass().getResource("/styles/app.css").toExternalForm()
                );
            }

            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            double maxWindowWidth = Math.max(960, visualBounds.getWidth() - WINDOW_MARGIN);
            double maxWindowHeight = Math.max(720, visualBounds.getHeight() - WINDOW_MARGIN);
            boolean authenticated = appContext.appSession().isAuthenticated();
            double loginLaunchWidth = Math.min(ui.designWidth(), maxWindowWidth * LOGIN_WIDTH_RATIO);
            double loginLaunchHeight = Math.min(ui.designHeight(), maxWindowHeight * LOGIN_HEIGHT_RATIO);
            double launchBaseWidth = authenticated ? ui.designWidth() : loginLaunchWidth;
            double launchBaseHeight = authenticated ? ui.designHeight() : loginLaunchHeight;
            double designScale = Math.min(1.0, Math.min(
                    maxWindowWidth / launchBaseWidth,
                    maxWindowHeight / launchBaseHeight
            ));
            double initialWidth = launchBaseWidth * designScale;
            double initialHeight = launchBaseHeight * designScale;

            stage.getStage().setFullScreen(false);
            stage.getStage().setMaximized(false);
            stage.getStage().setResizable(true);
            stage.getStage().setMinWidth(Math.min(ui.minWidth(), initialWidth));
            stage.getStage().setMinHeight(Math.min(ui.minHeight(), initialHeight));
            stage.getStage().setWidth(initialWidth);
            stage.getStage().setHeight(initialHeight);
            stage.getStage().centerOnScreen();
            WindowResizeSupport.install(stage.getStage());
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
            // Theme can only be initialized once per JVM.
        }
    }

    private void showStartupError(Exception exception) {
        UiAlerts.error("\u542f\u52a8\u5931\u8d25", exception);
        Platform.exit();
    }
}
