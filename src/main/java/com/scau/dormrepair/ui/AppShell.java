package com.scau.dormrepair.ui;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.AppSession;
import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.exception.BusinessException;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.component.PasswordInputControl;
import com.scau.dormrepair.ui.module.AccountManagementModule;
import com.scau.dormrepair.ui.module.AdminDispatchModule;
import com.scau.dormrepair.ui.module.AuditLogModule;
import com.scau.dormrepair.ui.module.DashboardModule;
import com.scau.dormrepair.ui.module.DormCatalogManagementModule;
import com.scau.dormrepair.ui.module.ProfileCenterModule;
import com.scau.dormrepair.ui.module.StatisticsModule;
import com.scau.dormrepair.ui.module.StudentRepairHistoryModule;
import com.scau.dormrepair.ui.module.StudentRepairModule;
import com.scau.dormrepair.ui.module.WorkbenchModule;
import com.scau.dormrepair.ui.module.WorkerProcessingModule;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.support.UiMotion;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class AppShell {

    private final AppContext appContext;
    private final AppSession appSession;
    private final List<WorkbenchModule> modules;
    private final StackPane root;
    private final Map<String, Parent> moduleViewCache;
    private final Map<String, Button> navButtons;

    private BorderPane workbenchShell;
    private StackPane moduleHost;
    private ScrollPane moduleScrollPane;
    private Label identityChipLabel;
    private WorkbenchModule activeModule;
    private UserRole renderedRole;
    private boolean windowListenerInstalled;

    public AppShell(AppContext appContext) {
        this.appContext = appContext;
        this.appSession = appContext.appSession();
        this.modules = List.of(
                new DashboardModule(appContext, this::openModuleByCode),
                new StudentRepairModule(appContext),
                new StudentRepairHistoryModule(appContext),
                new AdminDispatchModule(appContext),
                new AccountManagementModule(appContext),
                new DormCatalogManagementModule(appContext),
                new WorkerProcessingModule(appContext),
                new StatisticsModule(appContext),
                new AuditLogModule(appContext),
                new ProfileCenterModule(appContext, this::openChangePasswordDialog, this::logoutCurrentAccount)
        );
        this.root = new StackPane();
        this.moduleViewCache = new HashMap<>();
        this.navButtons = new LinkedHashMap<>();
    }

    public Parent createContent() {
        appSession.authenticatedProperty().addListener((observable, oldValue, newValue) -> renderCurrentView());
        appSession.displayNameProperty().addListener((observable, oldValue, newValue) -> updateHeader());
        appSession.currentRoleProperty().addListener((observable, oldValue, newValue) -> {
            if (appSession.isAuthenticated()) {
                renderCurrentView();
            }
        });
        root.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null || windowListenerInstalled) {
                return;
            }
            newScene.windowProperty().addListener((sceneObservable, oldWindow, newWindow) -> updateWindowTitle());
            windowListenerInstalled = true;
            updateWindowTitle();
        });
        renderCurrentView();
        return root;
    }

    private void renderCurrentView() {
        if (!appSession.isAuthenticated()) {
            resetWorkbenchState();
            root.getChildren().setAll(new LoginView(appContext, this::login).createView());
            updateWindowTitle();
            return;
        }

        ensureWorkbenchShell();
        syncWorkbench();
        root.getChildren().setAll(workbenchShell);
        updateWindowTitle();
    }

    private void ensureWorkbenchShell() {
        if (workbenchShell != null) {
            return;
        }

        workbenchShell = new BorderPane();
        workbenchShell.getStyleClass().add("app-shell");

        moduleHost = new StackPane();
        moduleHost.getStyleClass().add("module-host");
        moduleHost.setAlignment(Pos.TOP_CENTER);
        moduleHost.setMinWidth(0);
        moduleHost.setMaxWidth(Double.MAX_VALUE);

        moduleScrollPane = new ScrollPane(moduleHost);
        moduleScrollPane.getStyleClass().add("module-scroll-pane");
        moduleScrollPane.setFitToWidth(true);
        moduleScrollPane.setFitToHeight(false);
        moduleScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        moduleScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        moduleScrollPane.setPannable(true);
        moduleScrollPane.setFocusTraversable(false);
        UiMotion.installSmoothScrollPane(moduleScrollPane);

        workbenchShell.setTop(buildHeader());
        workbenchShell.setCenter(moduleScrollPane);
    }

    private void syncWorkbench() {
        List<WorkbenchModule> availableModules = availableModules();
        if (availableModules.isEmpty()) {
            throw new IllegalStateException("\u5f53\u524d\u8eab\u4efd\u6ca1\u6709\u53ef\u7528\u6a21\u5757\uff0c\u8bf7\u68c0\u67e5\u6743\u9650\u914d\u7f6e\u3002");
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
        showModule(activeModule, roleChanged);
    }

    private VBox buildHeader() {
        Label titleLabel = new Label("\u5bbf\u820d\u62a5\u4fee\u4e0e\u5de5\u5355\u7ba1\u7406\u7cfb\u7edf");
        titleLabel.getStyleClass().add("header-brand-title");

        Label roleLabel = new Label(appSession.getCurrentRole().displayName() + "\u5de5\u4f5c\u53f0");
        roleLabel.getStyleClass().add("header-role-summary");

        VBox brandBox = new VBox(6, titleLabel, roleLabel);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setFillWidth(true);
        brandBox.setMaxWidth(Double.MAX_VALUE);

        identityChipLabel = new Label();
        identityChipLabel.getStyleClass().add("header-identity-chip");

        Button profileButton = new Button("\u4e2a\u4eba\u4e2d\u5fc3");
        profileButton.getStyleClass().add("header-profile-action");
        profileButton.setOnAction(event -> openModuleByCode("profile"));

        Button logoutButton = new Button("\u9000\u51fa\u767b\u5f55");
        logoutButton.getStyleClass().add("header-logout-action");
        logoutButton.setOnAction(event -> logoutCurrentAccount());

        HBox summaryBox = new HBox(12, identityChipLabel, profileButton, logoutButton);
        summaryBox.getStyleClass().add("header-summary-inline");
        summaryBox.setAlignment(Pos.CENTER_RIGHT);

        HBox headerRow = new HBox(18, brandBox, summaryBox);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(brandBox, Priority.ALWAYS);

        VBox headerBox = new VBox(headerRow);
        headerBox.getStyleClass().add("header-shell");
        headerBox.setPadding(new Insets(10, 16, 10, 16));
        return headerBox;
    }
    private VBox buildSidebar(List<WorkbenchModule> availableModules) {
        VBox sidebar = new VBox(12);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20));
        sidebar.setMinWidth(176);
        sidebar.setPrefWidth(216);
        sidebar.setMaxWidth(260);

        Label navTitle = new Label("\u5de5\u4f5c\u6a21\u5757");
        navTitle.getStyleClass().add("sidebar-title");
        sidebar.getChildren().add(navTitle);
        navButtons.clear();

        for (WorkbenchModule module : availableModules.stream().filter(item -> !"profile".equals(item.moduleCode())).toList()) {
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
            showModule(module, false);
        } catch (RuntimeException exception) {
            activeModule = previousModule;
            updateHeader();
            updateSidebarSelection();
            if (previousModule != null && previousModule != module) {
                try {
                    showModule(previousModule, false);
                } catch (RuntimeException ignored) {
                    // fallback only
                }
            }
            UiAlerts.error("\u9875\u9762\u52a0\u8f7d\u5931\u8d25", exception);
        }
    }

    private void openModuleByCode(String moduleCode) {
        if (moduleCode == null || moduleCode.isBlank()) {
            return;
        }
        modules.stream()
                .filter(module -> moduleCode.equals(module.moduleCode()))
                .filter(module -> module.supports(appSession.getCurrentRole()))
                .findFirst()
                .ifPresent(this::activateModule);
    }

    private void logoutCurrentAccount() {
        activeModule = null;
        appSession.logout();
    }

    private void updateHeader() {
        if (identityChipLabel != null) {
            identityChipLabel.setText(appSession.getCurrentRole().displayName() + "\u00b7" + appSession.getDisplayName());
        }
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
        if (nextContent instanceof Region region) {
            region.setMinWidth(0);
            region.setMaxWidth(Double.MAX_VALUE);
        }
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

    private void login(UserAccount account) {
        appSession.login(account);
    }

    private void openChangePasswordDialog() {
        if (appSession.getCurrentAccountId() == null) {
            UiAlerts.error("\u4fee\u6539\u5931\u8d25", "\u5f53\u524d\u672a\u767b\u5f55\uff0c\u65e0\u6cd5\u4fee\u6539\u5bc6\u7801\u3002");
            return;
        }

        PasswordInputControl oldPasswordField = new PasswordInputControl();
        oldPasswordField.setPromptText("\u8f93\u5165\u65e7\u5bc6\u7801");
        PasswordInputControl newPasswordField = new PasswordInputControl();
        newPasswordField.setPromptText("\u8f93\u5165\u65b0\u5bc6\u7801");
        PasswordInputControl confirmPasswordField = new PasswordInputControl();
        confirmPasswordField.setPromptText("\u518d\u6b21\u8f93\u5165\u65b0\u5bc6\u7801");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("login-error-label");
        errorLabel.setWrapText(true);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        clearInlineError(errorLabel);

        VBox formBox = new VBox(
                12,
                createDialogField("\u65e7\u5bc6\u7801", oldPasswordField),
                createDialogField("\u65b0\u5bc6\u7801", newPasswordField),
                createDialogField("\u786e\u8ba4\u65b0\u5bc6\u7801", confirmPasswordField),
                errorLabel
        );
        formBox.getStyleClass().add("dialog-form-box");

        Stage[] stageRef = new Stage[1];
        var cancelButton = FusionUiFactory.createGhostButton("\u53d6\u6d88", 132, 38, () -> {
            if (stageRef[0] != null) {
                stageRef[0].close();
            }
        });
        var confirmButton = FusionUiFactory.createPrimaryButton("\u786e\u8ba4\u4fee\u6539", 132, 38, () -> {
            try {
                clearInlineError(errorLabel);
                appContext.userAccountService().changePassword(
                        appSession.getCurrentAccountId(),
                        oldPasswordField.getText(),
                        newPasswordField.getText(),
                        confirmPasswordField.getText()
                );
                if (stageRef[0] != null) {
                    stageRef[0].close();
                }
                UiAlerts.info("\u4fee\u6539\u6210\u529f", "\u5bc6\u7801\u5df2\u66f4\u65b0\uff0c\u540e\u7eed\u767b\u5f55\u8bf7\u4f7f\u7528\u65b0\u5bc6\u7801\u3002");
            } catch (BusinessException exception) {
                showInlineError(errorLabel, exception.getMessage());
            } catch (RuntimeException exception) {
                UiAlerts.error("\u4fee\u6539\u5931\u8d25", exception);
            }
        });

        cancelButton.getNode().getStyleClass().add("dialog-confirm-button");
        confirmButton.getNode().getStyleClass().add("dialog-confirm-button");
        HBox actionRow = new HBox(12, cancelButton.getNode(), confirmButton.getNode());
        actionRow.setAlignment(Pos.CENTER);

        Label chipLabel = new Label("\u5b89\u5168\u8bbe\u7f6e");
        chipLabel.getStyleClass().addAll("dialog-chip", "dialog-chip-info");
        Label titleLabel = new Label("\u4fee\u6539\u5bc6\u7801");
        titleLabel.getStyleClass().add("dialog-title");

        VBox body = new VBox(18, chipLabel, titleLabel, formBox, actionRow);
        body.getStyleClass().addAll("dialog-shell", "dialog-shell-info");
        body.setPadding(new Insets(24, 26, 24, 26));
        Window owner = resolveOwner();
        double dialogWidth = resolveDialogWidth(owner, 440, 340);
        body.setPrefWidth(dialogWidth);
        body.setMaxWidth(dialogWidth);

        Scene scene = new Scene(body);
        scene.setFill(Color.TRANSPARENT);
        if (AppShell.class.getResource("/styles/app.css") != null) {
            scene.getStylesheets().add(AppShell.class.getResource("/styles/app.css").toExternalForm());
        }
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (stageRef[0] != null) {
                    stageRef[0].close();
                }
                event.consume();
            }
        });

        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        stageRef[0] = dialog;
        dialog.setResizable(false);
        dialog.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.setScene(scene);
        dialog.setOnShown(event -> {
            oldPasswordField.requestInputFocus();
            if (owner == null) {
                dialog.centerOnScreen();
                return;
            }
            double x = owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2;
            double y = owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2;
            dialog.setX(Math.max(0, x));
            dialog.setY(Math.max(0, y));
        });
        dialog.sizeToScene();
        dialog.showAndWait();
    }

    private static VBox createDialogField(String labelText, Region field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("login-input-label");
        VBox box = new VBox(8, label, field);
        box.setFillWidth(true);
        return box;
    }

    private static void showInlineError(Label errorLabel, String text) {
        errorLabel.setText(text == null ? "" : text);
        errorLabel.setOpacity(1);
    }

    private static void clearInlineError(Label errorLabel) {
        errorLabel.setText(" ");
        errorLabel.setOpacity(0);
    }

    private static Window resolveOwner() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .filter(Window::isFocused)
                .findFirst()
                .orElseGet(() -> Window.getWindows().stream()
                        .filter(Window::isShowing)
                        .findFirst()
                        .orElse(null));
    }

    private static double resolveDialogWidth(Window owner, double preferredWidth, double minWidth) {
        if (owner == null) {
            return preferredWidth;
        }
        double availableWidth = Math.max(minWidth, owner.getWidth() - 72);
        return Math.max(minWidth, Math.min(preferredWidth, availableWidth));
    }

    private void updateWindowTitle() {
        if (root.getScene() == null || root.getScene().getWindow() == null) {
            return;
        }
        if (root.getScene().getWindow() instanceof Stage stage) {
            stage.setTitle(appSession.isAuthenticated() ? appContext.properties().title() : "");
        }
    }

    private void resetWorkbenchState() {
        moduleViewCache.clear();
        navButtons.clear();
        activeModule = null;
        renderedRole = null;
        workbenchShell = null;
        moduleHost = null;
        moduleScrollPane = null;
        identityChipLabel = null;
    }
}
