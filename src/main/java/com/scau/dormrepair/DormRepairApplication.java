package com.scau.dormrepair;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.config.AppProperties;
import com.scau.dormrepair.ui.AppShell;
import com.scau.dormrepair.ui.support.BrandIconFactory;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.support.WindowResizeSupport;
import com.scau.dormrepair.ui.theme.DormVfxTheme;
import io.vproxy.vfx.theme.Theme;
import io.vproxy.vfx.ui.scene.VScene;
import io.vproxy.vfx.ui.scene.VSceneRole;
import io.vproxy.vfx.ui.stage.VStage;
import io.vproxy.vfx.ui.stage.VStageInitParams;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class DormRepairApplication extends Application {
    private static final double LOGIN_WIDTH_RATIO = 0.84;
    private static final double LOGIN_HEIGHT_RATIO = 0.88;
    private static final double WINDOW_MARGIN = 80;
    private static final double MIN_WINDOW_SIZE = 1;

    private AppContext appContext;

    @Override
    public void start(Stage primaryStage) {
        try {
            appContext = AppContext.bootstrap();
            AppShell appShell = new AppShell(appContext);
            Parent appRoot = appShell.createContent();
            AppProperties.UiProperties ui = appContext.properties().ui();

            VScene mainScene = new VScene(VSceneRole.MAIN);
            mainScene.enableAutoContentWidthHeight();

            VStage stage = new VStage(primaryStage, new VStageInitParams().setInitialScene(mainScene));
            stage.setTitle(appContext.appSession().isAuthenticated() ? appContext.properties().title() : "");
            stage.useLightBorder();
            BrandIconFactory.attachStageIcon(stage.getStage());

            StackPane rootContainer = new StackPane(appRoot);
            rootContainer.setMinSize(0, 0);
            rootContainer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            rootContainer.prefWidthProperty().bind(mainScene.getContentPane().widthProperty());
            rootContainer.prefHeightProperty().bind(mainScene.getContentPane().heightProperty());
            mainScene.getContentPane().getChildren().add(rootContainer);

            if (getClass().getResource("/styles/app.css") != null) {
                stage.getStage().getScene().getStylesheets().add(
                        getClass().getResource("/styles/app.css").toExternalForm()
                );
            }

            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            double maxWindowWidth = Math.max(960, visualBounds.getWidth() - WINDOW_MARGIN);
            double maxWindowHeight = Math.max(720, visualBounds.getHeight() - WINDOW_MARGIN);
            boolean authenticated = appContext.appSession().isAuthenticated();
            double[] initialSize = authenticated
                    ? fitWindowSize(ui.designWidth(), ui.designHeight(), maxWindowWidth, maxWindowHeight)
                    : fitWindowSize(
                            ui.designWidth(),
                            ui.designHeight(),
                            maxWindowWidth * LOGIN_WIDTH_RATIO,
                            maxWindowHeight * LOGIN_HEIGHT_RATIO
                    );
            double initialWidth = initialSize[0];
            double initialHeight = initialSize[1];

            stage.getStage().setFullScreen(false);
            stage.getStage().setMaximized(false);
            stage.getStage().setResizable(true);
            stage.getStage().setMinWidth(Math.min(maxWindowWidth, Math.max(ui.minWidth(), MIN_WINDOW_SIZE)));
            stage.getStage().setMinHeight(Math.min(maxWindowHeight, Math.max(ui.minHeight(), MIN_WINDOW_SIZE)));
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

    private static double[] fitWindowSize(
            double baseWidth,
            double baseHeight,
            double maxWidth,
            double maxHeight
    ) {
        double scale = Math.min(1.0, Math.min(maxWidth / baseWidth, maxHeight / baseHeight));
        return new double[] {baseWidth * scale, baseHeight * scale};
    }
}
