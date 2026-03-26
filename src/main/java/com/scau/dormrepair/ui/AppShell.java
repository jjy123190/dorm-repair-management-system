package com.scau.dormrepair.ui;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.AppSession;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.ui.module.AdminDispatchModule;
import com.scau.dormrepair.ui.module.DashboardModule;
import com.scau.dormrepair.ui.module.StatisticsModule;
import com.scau.dormrepair.ui.module.StudentRepairHistoryModule;
import com.scau.dormrepair.ui.module.StudentRepairModule;
import com.scau.dormrepair.ui.module.WorkbenchModule;
import com.scau.dormrepair.ui.module.WorkerProcessingModule;
import com.scau.dormrepair.ui.support.UiAlerts;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * 工作台壳层只保留三类稳定信息：
 * 1. 左侧系统标题
 * 2. 右上角当前身份标签
 * 3. 退出动作
 *
 * 模块说明和业务提示全部回到页面主体，避免顶部壳层跟着模块切换上下抖动。
 */
public class AppShell {

    private final AppContext appContext;
    private final AppSession appSession;
    private final List<WorkbenchModule> modules;
    private final StackPane root;
    private final Map<String, Parent> moduleViewCache;
    private final Map<String, Button> navButtons;

    private BorderPane workbenchShell;
    private StackPane moduleHost;
    private Label identityChipLabel;
    private WorkbenchModule activeModule;
    private UserRole renderedRole;

    public AppShell(AppContext appContext) {
        this.appContext = appContext;
        this.appSession = appContext.appSession();
        this.modules = List.of(
                new DashboardModule(appContext),
                new StudentRepairModule(appContext),
                new StudentRepairHistoryModule(appContext),
                new AdminDispatchModule(appContext),
                new WorkerProcessingModule(appContext),
                new StatisticsModule(appContext)
        );
        this.root = new StackPane();
        this.moduleViewCache = new HashMap<>();
        this.navButtons = new LinkedHashMap<>();
    }

    public Parent createContent() {
        appSession.authenticatedProperty().addListener((observable, oldValue, newValue) -> renderCurrentView());
        renderCurrentView();
        return root;
    }

    private void renderCurrentView() {
        if (!appSession.isAuthenticated()) {
            resetWorkbenchState();
            root.getChildren().setAll(new LoginView(appContext, this::login).createView());
            return;
        }

        ensureWorkbenchShell();
        syncWorkbench();
        root.getChildren().setAll(workbenchShell);
    }

    private void ensureWorkbenchShell() {
        if (workbenchShell != null) {
            return;
        }

        workbenchShell = new BorderPane();
        workbenchShell.getStyleClass().add("app-shell");

        moduleHost = new StackPane();
        moduleHost.getStyleClass().add("module-host");
        moduleHost.setMinWidth(0);

        workbenchShell.setTop(buildHeader());
        workbenchShell.setCenter(moduleHost);
    }

    private void syncWorkbench() {
        List<WorkbenchModule> availableModules = availableModules();
        if (availableModules.isEmpty()) {
            throw new IllegalStateException("当前角色没有可用模块，请检查模块权限配置。");
        }

        UserRole currentRole = appSession.getCurrentRole();
        boolean roleChanged = currentRole != renderedRole;
        if (roleChanged) {
            renderedRole = currentRole;
            moduleViewCache.clear();
            activeModule = null;
        }

        if (activeModule == null || !activeModule.supports(currentRole)) {
            activeModule = availableModules.get(0);
        }

        if (roleChanged || navButtons.isEmpty()) {
            workbenchShell.setLeft(buildSidebar(availableModules));
        }

        updateHeader();
        updateSidebarSelection();
        showModule(activeModule, true);
    }

    private VBox buildHeader() {
        Label titleLabel = new Label("宿舍报修与工单管理系统");
        titleLabel.getStyleClass().add("header-brand-title");

        VBox brandBox = new VBox(titleLabel);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setFillWidth(true);
        brandBox.setMaxWidth(Double.MAX_VALUE);

        identityChipLabel = new Label();
        identityChipLabel.getStyleClass().add("header-identity-chip");

        Button logoutButton = new Button("退出登录");
        logoutButton.getStyleClass().add("header-logout-action");
        logoutButton.setOnAction(event -> {
            activeModule = null;
            appSession.logout();
        });

        VBox summaryBox = new VBox(12, identityChipLabel, logoutButton);
        summaryBox.getStyleClass().add("header-summary-card");
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setMinWidth(240);
        summaryBox.setPrefWidth(240);
        summaryBox.setMaxWidth(240);
        summaryBox.setMinHeight(96);
        summaryBox.setPrefHeight(96);
        summaryBox.setMaxHeight(96);

        BorderPane headerRow = new BorderPane();
        headerRow.setLeft(brandBox);
        headerRow.setRight(summaryBox);

        VBox headerBox = new VBox(headerRow);
        headerBox.getStyleClass().add("header-shell");
        headerBox.setPadding(new Insets(16, 22, 16, 22));
        headerBox.setMinHeight(110);
        headerBox.setPrefHeight(110);
        headerBox.setMaxHeight(110);
        return headerBox;
    }

    private VBox buildSidebar(List<WorkbenchModule> availableModules) {
        VBox sidebar = new VBox(12);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(18));

        Label navTitle = new Label("工作模块");
        navTitle.getStyleClass().add("sidebar-title");
        sidebar.getChildren().add(navTitle);
        navButtons.clear();

        for (WorkbenchModule module : availableModules) {
            Button navButton = new Button(module.moduleName());
            navButton.getStyleClass().add("nav-button");
            navButton.setMaxWidth(Double.MAX_VALUE);
            navButton.setOnAction(event -> activateModule(module));
            navButtons.put(module.moduleCode(), navButton);
            sidebar.getChildren().add(navButton);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);
        return sidebar;
    }

    private void activateModule(WorkbenchModule module) {
        WorkbenchModule previousModule = activeModule;
        activeModule = module;
        try {
            updateHeader();
            updateSidebarSelection();
            showModule(module, true);
        } catch (RuntimeException exception) {
            activeModule = previousModule;
            updateHeader();
            updateSidebarSelection();
            if (previousModule != null && previousModule != module) {
                try {
                    showModule(previousModule, false);
                } catch (RuntimeException ignored) {
                    // 这里只做兜底，不覆盖真正的原始报错。
                }
            }
            UiAlerts.error("页面加载失败", exception);
        }
    }

    private void updateHeader() {
        identityChipLabel.setText(appSession.getCurrentRole().displayName() + "•" + appSession.getDisplayName());
    }

    private void updateSidebarSelection() {
        navButtons.forEach((moduleCode, button) -> {
            button.getStyleClass().remove("nav-button-active");
            if (activeModule != null && activeModule.moduleCode().equals(moduleCode)) {
                button.getStyleClass().add("nav-button-active");
            }
        });
    }

    private void showModule(WorkbenchModule module, boolean forceReload) {
        if (forceReload) {
            moduleViewCache.remove(module.moduleCode());
        }
        Parent nextContent = loadModuleView(module);
        moduleHost.getChildren().setAll(nextContent);
    }

    private Parent loadModuleView(WorkbenchModule module) {
        if (!module.cacheViewOnSwitch()) {
            return module.createView();
        }
        return moduleViewCache.computeIfAbsent(module.moduleCode(), ignored -> module.createView());
    }

    private List<WorkbenchModule> availableModules() {
        UserRole currentRole = appSession.getCurrentRole();
        return modules.stream()
                .filter(module -> module.supports(currentRole))
                .collect(Collectors.toList());
    }

    private void login(String displayName, UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("请选择登录角色");
        }
        appSession.login(displayName, role);
    }

    private void resetWorkbenchState() {
        moduleViewCache.clear();
        navButtons.clear();
        activeModule = null;
        renderedRole = null;
        workbenchShell = null;
        moduleHost = null;
        identityChipLabel = null;
    }
}
