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
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * 主壳层统一处理登录入口、模块导航和工作区切换。
 * 这里不再重建整个窗口，只替换中间模块内容。
 */
public class AppShell {

    private static final Duration MODULE_SWITCH_DURATION = Duration.millis(90);

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
        showModule(activeModule, false);
    }

    private VBox buildHeader() {
        Label titleLabel = new Label("宿舍报修与工单管理系统");
        titleLabel.getStyleClass().add("page-title");

        Label subtitleLabel = new Label("JavaFX + MyBatis 桌面工作台");
        subtitleLabel.getStyleClass().add("page-subtitle");

        roleBadgeLabel = new Label();
        roleBadgeLabel.getStyleClass().add("role-badge");

        userLabel = new Label();
        userLabel.getStyleClass().add("plain-text");
        userLabel.setMaxWidth(Double.MAX_VALUE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        var logoutButton = FusionUiFactory.createGhostButton("退出登录", 132, 40, () -> {
            activeModule = null;
            appSession.logout();
        }).getNode();
        logoutButton.getStyleClass().add("header-logout-button");

        HBox statusRow = new HBox(12, roleBadgeLabel, userLabel, spacer, logoutButton);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        VBox headerBox = new VBox(6, titleLabel, subtitleLabel, statusRow);
        headerBox.getStyleClass().add("header-box");
        headerBox.setPadding(new Insets(14, 22, 12, 22));
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
        updateSidebarSelection();
        showModule(module, true);
    }

    private void updateHeader() {
        roleBadgeLabel.setText(appSession.getCurrentRole().displayName());
        userLabel.setText("当前演示身份: " + appSession.getDisplayName());
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

    private void showModule(WorkbenchModule module, boolean animate) {
        Parent nextContent = loadModuleView(module);
        Parent currentContent = moduleHost.getChildren().isEmpty()
                ? null
                : (Parent) moduleHost.getChildren().get(0);

        if (currentContent == nextContent) {
            return;
        }

        switchContent(moduleHost, nextContent, animate);
    }

    private Parent loadModuleView(WorkbenchModule module) {
        if (!module.cacheViewOnSwitch()) {
            return module.createView();
        }

        // 表单模块保留节点实例，切出去再回来时不用重建整页。
        return moduleViewCache.computeIfAbsent(module.moduleCode(), ignored -> module.createView());
    }

    private void switchContent(StackPane host, Parent nextContent, boolean animate) {
        if (!animate || host.getChildren().isEmpty()) {
            host.getChildren().setAll(nextContent);
            return;
        }

        nextContent.setOpacity(0);
        host.getChildren().setAll(nextContent);

        // 只留短淡入，不做位移动画，避免“卡片自己在动”的观感。
        FadeTransition fadeTransition = new FadeTransition(MODULE_SWITCH_DURATION, nextContent);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
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
        moduleHintLabel = null;
    }
}
