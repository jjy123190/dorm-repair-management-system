package com.scau.dormrepair.ui;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.AppSession;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.module.AdminDispatchModule;
import com.scau.dormrepair.ui.module.DashboardModule;
import com.scau.dormrepair.ui.module.StatisticsModule;
import com.scau.dormrepair.ui.module.StudentRepairModule;
import com.scau.dormrepair.ui.module.WorkbenchModule;
import com.scau.dormrepair.ui.module.WorkerProcessingModule;
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
 * 主壳层统一处理登录入口、模块导航和工作区切换。
 * 这里不再重建整个窗口，只替换中间模块内容。
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
    private Label headerContextTitleLabel;
    private Label headerContextCopyLabel;
    private Label moduleHintLabel;
    private WorkbenchModule activeModule;
    private UserRole renderedRole;

    public AppShell(AppContext appContext) {
        this.appContext = appContext;
        this.appSession = appContext.appSession();
        this.modules = List.of(
                new DashboardModule(appContext),
                new StudentRepairModule(appContext),
                new AdminDispatchModule(appContext),
                new WorkerProcessingModule(appContext),
                new StatisticsModule(appContext)
        );
        this.root = new StackPane();
        this.moduleViewCache = new HashMap<>();
        this.navButtons = new LinkedHashMap<>();
    }

    public Parent createContent() {
        // 只监听认证状态，避免 login() 时角色变化和认证变化把页面重绘两次。
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
            throw new IllegalStateException("当前角色没有可用模块，请检查模块权限配置。");
        }

        UserRole currentRole = appSession.getCurrentRole();
        boolean roleChanged = currentRole != renderedRole;
        if (roleChanged) {
            // 角色切换后清掉缓存，避免把上一种身份的表单状态带回来。
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
        showModule(activeModule);
    }

    private VBox buildHeader() {
        Label eyebrowLabel = new Label("DESKTOP WORKBENCH");
        eyebrowLabel.getStyleClass().add("header-brand-eyebrow");

        Label titleLabel = new Label("宿舍报修与工单管理系统");
        titleLabel.getStyleClass().add("header-brand-title");

        Label subtitleLabel = new Label("把学生报修、宿管派单和维修闭环统一到同一个桌面壳层里，顶部只保留当前演示真正需要的信息。");
        subtitleLabel.getStyleClass().add("header-brand-subtitle");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(420);

        VBox brandBox = new VBox(4, eyebrowLabel, titleLabel, subtitleLabel);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setMaxWidth(Double.MAX_VALUE);

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
        metaRow.setAlignment(Pos.CENTER_LEFT);

        headerContextTitleLabel = new Label();
        headerContextTitleLabel.getStyleClass().add("header-context-title");

        headerContextCopyLabel = new Label();
        headerContextCopyLabel.getStyleClass().add("header-context-copy");
        headerContextCopyLabel.setWrapText(true);
        headerContextCopyLabel.setMaxWidth(280);

        VBox contextCard = new VBox(4, headerContextTitleLabel, headerContextCopyLabel);
        contextCard.getStyleClass().add("header-context-card");
        contextCard.setAlignment(Pos.CENTER_LEFT);

        var logoutButton = FusionUiFactory.createGhostButton("退出登录", 120, 38, () -> {
            activeModule = null;
            appSession.logout();
        }).getNode();
        logoutButton.getStyleClass().add("header-logout-button");

        HBox actionRow = new HBox(12, contextCard, logoutButton);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        VBox summaryBox = new VBox(10, metaRow, actionRow);
        summaryBox.setAlignment(Pos.CENTER_RIGHT);
        summaryBox.setMinWidth(360);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerRow = new HBox(20, brandBox, spacer, summaryBox);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox headerBox = new VBox(headerRow);
        headerBox.getStyleClass().add("header-shell");
        headerBox.setPadding(new Insets(16, 22, 14, 22));
        return headerBox;
    }

    private VBox buildSidebar(List<WorkbenchModule> availableModules) {
        VBox sidebar = new VBox(12);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(18));

        Label navTitle = new Label("工作模块");
        navTitle.getStyleClass().add("sidebar-title");

        Label navHint = new Label("先看系统主状态，再进入对应角色的业务主链路。");
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

        moduleHintLabel = new Label();
        moduleHintLabel.getStyleClass().add("sidebar-hint");
        moduleHintLabel.setWrapText(true);
        sidebar.getChildren().add(moduleHintLabel);
        return sidebar;
    }

    private void activateModule(WorkbenchModule module) {
        if (module == activeModule) {
            return;
        }

        activeModule = module;
        updateHeader();
        updateSidebarSelection();
        showModule(module);
    }

    private void updateHeader() {
        UserRole currentRole = appSession.getCurrentRole();
        String moduleName = activeModule == null ? "首页概览" : activeModule.moduleName();
        String moduleDescription = activeModule == null
                ? currentRole.displayName() + "当前还没有模块上下文。"
                : activeModule.moduleDescription();

        roleBadgeLabel.setText(currentRole.displayName());
        userLabel.setText("演示身份 · " + appSession.getDisplayName());
        moduleChipLabel.setText(moduleName);
        headerContextTitleLabel.setText("当前聚焦 · " + moduleName);
        headerContextCopyLabel.setText(moduleDescription);
    }

    private void updateSidebarSelection() {
        navButtons.forEach((moduleCode, button) -> {
            button.getStyleClass().remove("nav-button-active");
            if (activeModule != null && activeModule.moduleCode().equals(moduleCode)) {
                button.getStyleClass().add("nav-button-active");
            }
        });

        if (moduleHintLabel != null && activeModule != null) {
            moduleHintLabel.setText(activeModule.moduleDescription());
        }
    }

    private void showModule(WorkbenchModule module) {
        Parent nextContent = loadModuleView(module);
        Parent currentContent = moduleHost.getChildren().isEmpty()
                ? null
                : (Parent) moduleHost.getChildren().get(0);

        if (currentContent == nextContent) {
            return;
        }

        switchContent(moduleHost, nextContent);
    }

    private Parent loadModuleView(WorkbenchModule module) {
        if (!module.cacheViewOnSwitch()) {
            return module.createView();
        }

        // 表单模块保留节点实例，切出去再回来时不用重建整页。
        return moduleViewCache.computeIfAbsent(module.moduleCode(), ignored -> module.createView());
    }

    private void switchContent(StackPane host, Parent nextContent) {
        host.getChildren().setAll(nextContent);
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
        headerContextTitleLabel = null;
        headerContextCopyLabel = null;
        moduleHintLabel = null;
    }
}
