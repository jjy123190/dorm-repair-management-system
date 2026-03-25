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
 * 顶层壳层只负责身份、导航和模块承载。
 * 具体业务说明留在模块内部，避免头部、侧栏和正文重复说同一段话。
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

        workbenchShell.setTop(buildHeader());
        workbenchShell.setCenter(moduleHost);
    }

    private void syncWorkbench() {
        List<WorkbenchModule> availableModules = availableModules();
        if (availableModules.isEmpty()) {
            throw new IllegalStateException("\u5f53\u524d\u89d2\u8272\u6ca1\u6709\u53ef\u7528\u6a21\u5757\uff0c\u8bf7\u68c0\u67e5\u6a21\u5757\u6743\u9650\u914d\u7f6e\u3002");
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
        VBox brandBox = buildHeaderBrand();
        VBox summaryBox = buildHeaderSummary();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerRow = new HBox(20, brandBox, spacer, summaryBox);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox headerBox = new VBox(headerRow);
        headerBox.getStyleClass().add("header-shell");
        headerBox.setPadding(new Insets(16, 22, 14, 22));
        headerBox.setMinHeight(146);
        headerBox.setPrefHeight(146);
        headerBox.setMaxHeight(146);
        return headerBox;
    }

    private VBox buildHeaderBrand() {
        Label titleLabel = new Label("\u5bbf\u820d\u62a5\u4fee\u4e0e\u5de5\u5355\u7ba1\u7406\u7cfb\u7edf");
        titleLabel.getStyleClass().add("header-brand-title");

        VBox brandBox = new VBox(titleLabel);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setMaxWidth(Double.MAX_VALUE);
        return brandBox;
    }

    private VBox buildHeaderSummary() {
        roleBadgeLabel = new Label();
        roleBadgeLabel.getStyleClass().add("header-role-chip");

        userLabel = new Label();
        userLabel.getStyleClass().add("header-user-chip");
        userLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        userLabel.setMaxWidth(220);

        moduleChipLabel = new Label();
        moduleChipLabel.getStyleClass().add("header-module-chip");
        moduleChipLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        moduleChipLabel.setMaxWidth(180);

        HBox metaRow = new HBox(8, roleBadgeLabel, userLabel, moduleChipLabel);
        metaRow.getStyleClass().add("header-meta-row");
        metaRow.setAlignment(Pos.CENTER);

        Button logoutButton = new Button("\u9000\u51fa\u767b\u5f55");
        logoutButton.getStyleClass().add("header-logout-action");
        logoutButton.setOnAction(event -> {
            activeModule = null;
            appSession.logout();
        });

        VBox summaryBox = new VBox(14, metaRow, logoutButton);
        summaryBox.getStyleClass().add("header-summary-card");
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setMinWidth(320);
        summaryBox.setPrefWidth(320);
        summaryBox.setMaxWidth(320);
        summaryBox.setMinHeight(120);
        summaryBox.setPrefHeight(120);
        summaryBox.setMaxHeight(120);
        return summaryBox;
    }

    private VBox buildSidebar(List<WorkbenchModule> availableModules) {
        VBox sidebar = new VBox(12);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(18));

        Label navTitle = new Label("\u5de5\u4f5c\u6a21\u5757");
        navTitle.getStyleClass().add("sidebar-title");

        Label navHint = new Label("\u5de6\u4fa7\u53ea\u4fdd\u7559\u6a21\u5757\u5165\u53e3\uff0c\u5177\u4f53\u4e1a\u52a1\u8bf4\u660e\u56de\u5230\u9875\u9762\u4e3b\u4f53\u5185\uff0c\u907f\u514d\u91cd\u590d\u3002");
        navHint.getStyleClass().add("sidebar-hint");
        navHint.setWrapText(true);

        sidebar.getChildren().addAll(navTitle, navHint);
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
            // 就算当前按钮已经处于激活态，也强制重载主体视图，避免壳层状态和页面内容偶发串页后无法自恢复。
            showModule(module, true);
        } catch (RuntimeException exception) {
            activeModule = previousModule;
            updateHeader();
            updateSidebarSelection();
            if (previousModule != null && previousModule != module) {
                try {
                    showModule(previousModule, false);
                } catch (RuntimeException ignored) {
                    // 回退页只是兜底，不再让二次异常覆盖真正的报错原因。
                }
            }
            UiAlerts.error("\u9875\u9762\u52a0\u8f7d\u5931\u8d25", exception);
        }
    }

    private void updateHeader() {
        String moduleName = activeModule == null ? "\u9996\u9875\u6982\u89c8" : activeModule.moduleName();

        roleBadgeLabel.setText(appSession.getCurrentRole().displayName());
        userLabel.setText("\u6f14\u793a\u8eab\u4efd \u00b7 " + appSession.getDisplayName());
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
            throw new IllegalArgumentException("\u8bf7\u9009\u62e9\u767b\u5f55\u89d2\u8272");
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
