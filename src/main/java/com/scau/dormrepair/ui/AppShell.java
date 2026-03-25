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
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * 桌面端壳层只负责三件事：
 * 1. 固定顶部身份区
 * 2. 固定左侧模块入口
 * 3. 承载中间业务页面
 * 这样模块切换时，顶部和侧栏不会因为正文内容不同而上下抖动。
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
    private Label roleBadgeLabel;
    private Label userLabel;
    private Label moduleChipLabel;
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

        roleBadgeLabel = new Label();
        roleBadgeLabel.getStyleClass().add("header-role-chip");

        userLabel = new Label();
        userLabel.getStyleClass().add("header-user-chip");
        userLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        userLabel.setMaxWidth(160);

        moduleChipLabel = new Label();
        moduleChipLabel.getStyleClass().add("header-module-chip");
        moduleChipLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        moduleChipLabel.setMaxWidth(140);

        HBox metaRow = new HBox(8, roleBadgeLabel, userLabel, moduleChipLabel);
        metaRow.getStyleClass().add("header-meta-row");
        metaRow.setAlignment(Pos.CENTER);

        Button logoutButton = new Button("退出登录");
        logoutButton.getStyleClass().add("header-logout-action");
        logoutButton.setOnAction(event -> {
            activeModule = null;
            appSession.logout();
        });

        VBox summaryBox = new VBox(12, metaRow, logoutButton);
        summaryBox.getStyleClass().add("header-summary-card");
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setMinWidth(320);
        summaryBox.setPrefWidth(320);
        summaryBox.setMaxWidth(320);
        summaryBox.setMinHeight(78);
        summaryBox.setPrefHeight(78);
        summaryBox.setMaxHeight(78);

        BorderPane headerRow = new BorderPane();
        headerRow.setLeft(brandBox);
        headerRow.setRight(summaryBox);

        VBox headerBox = new VBox(headerRow);
        headerBox.getStyleClass().add("header-shell");
        headerBox.setPadding(new Insets(14, 22, 14, 22));
        headerBox.setMinHeight(108);
        headerBox.setPrefHeight(108);
        headerBox.setMaxHeight(108);
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
            // 每次都强制刷新中间主体，避免缓存页和导航状态偶发串页。
            showModule(module, true);
        } catch (RuntimeException exception) {
            activeModule = previousModule;
            updateHeader();
            updateSidebarSelection();
            if (previousModule != null && previousModule != module) {
                try {
                    showModule(previousModule, false);
                } catch (RuntimeException ignored) {
                    // 这里只做兜底，不覆盖真正的报错原因。
                }
            }
            UiAlerts.error("页面加载失败", exception);
        }
    }

    private void updateHeader() {
        String moduleName = activeModule == null ? "首页概览" : activeModule.moduleName();
        roleBadgeLabel.setText(appSession.getCurrentRole().displayName());
        userLabel.setText("演示身份 · " + appSession.getDisplayName());
        moduleChipLabel.setText(moduleName);
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
        roleBadgeLabel = null;
        userLabel = null;
        moduleChipLabel = null;
    }
}
