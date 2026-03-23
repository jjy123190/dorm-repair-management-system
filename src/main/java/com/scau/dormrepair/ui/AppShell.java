package com.scau.dormrepair.ui;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.ui.module.AdminDispatchModule;
import com.scau.dormrepair.ui.module.DashboardModule;
import com.scau.dormrepair.ui.module.StatisticsModule;
import com.scau.dormrepair.ui.module.StudentRepairModule;
import com.scau.dormrepair.ui.module.WorkbenchModule;
import com.scau.dormrepair.ui.module.WorkerProcessingModule;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * 桌面端主工作台。
 * 左侧负责切换模块，右侧负责承载各个角色的工作页面。
 */
public class AppShell {

    private final List<WorkbenchModule> modules;

    public AppShell(AppContext appContext) {
        this.modules = List.of(
                new DashboardModule(appContext),
                new StudentRepairModule(appContext),
                new AdminDispatchModule(appContext),
                new WorkerProcessingModule(appContext),
                new StatisticsModule(appContext)
        );
    }

    public Parent createContent() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");
        root.setTop(buildHeader());
        root.setLeft(buildSidebar(root));
        root.setCenter(wrapModule(modules.get(0).createView()));
        return root;
    }

    private VBox buildHeader() {
        Label titleLabel = new Label("宿舍报修与工单管理系统");
        titleLabel.getStyleClass().add("page-title");

        Label subtitleLabel = new Label("JavaFX + MyBatis 桌面应用基础架构");
        subtitleLabel.getStyleClass().add("page-subtitle");

        VBox headerBox = new VBox(4, titleLabel, subtitleLabel);
        headerBox.getStyleClass().add("header-box");
        headerBox.setPadding(new Insets(20, 24, 16, 24));
        return headerBox;
    }

    /**
     * 侧边栏只做模块切换，不在这里掺杂业务逻辑。
     */
    private VBox buildSidebar(BorderPane root) {
        VBox sidebar = new VBox(12);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(18));

        Label navTitle = new Label("工作模块");
        navTitle.getStyleClass().add("sidebar-title");
        sidebar.getChildren().add(navTitle);

        for (WorkbenchModule module : modules) {
            Button navButton = new Button(module.moduleName());
            navButton.getStyleClass().add("nav-button");
            navButton.setMaxWidth(Double.MAX_VALUE);
            navButton.setOnAction(event -> root.setCenter(wrapModule(module.createView())));
            sidebar.getChildren().add(navButton);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        Label hintLabel = new Label("后续同学优先在各模块下补业务表单和事件处理。");
        hintLabel.setWrapText(true);
        hintLabel.getStyleClass().add("sidebar-hint");
        sidebar.getChildren().add(hintLabel);
        return sidebar;
    }

    private ScrollPane wrapModule(Parent content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("module-scroll");
        return scrollPane;
    }
}
