package com.scau.dormrepair.ui;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.exception.BusinessException;
import com.scau.dormrepair.ui.component.PasswordInputControl;
import com.scau.dormrepair.ui.support.BrandIconFactory;
import com.scau.dormrepair.ui.support.UiAlerts;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginView {

    private static final String BRAND_NAME = "\u4fee\u4e86\u4e48";
    private static final String LAST_USERNAME_KEY = "login.lastUsername";
    private static final Pattern USERNAME_HINT_PATTERN = Pattern.compile("^[A-Za-z0-9_]{4,32}$");
    private static final Preferences LOGIN_PREFERENCES = Preferences.userNodeForPackage(LoginView.class);

    private final AppContext appContext;
    private final Consumer<UserAccount> loginAction;

    public LoginView(AppContext appContext, Consumer<UserAccount> loginAction) {
        this.appContext = appContext;
        this.loginAction = loginAction;
    }

    public Parent createView() {
        BorderPane shell = new BorderPane();
        shell.getStyleClass().add("login-shell");
        shell.setCenter(buildCenterPane());
        return shell;
    }

    private StackPane buildCenterPane() {
        ObjectProperty<LoginMode> currentMode = new SimpleObjectProperty<>();

        StackPane centerPane = new StackPane();
        centerPane.getStyleClass().add("login-center-pane");

        Region orbLeft = new Region();
        orbLeft.getStyleClass().addAll("login-orb", "login-orb-left");
        Region orbRight = new Region();
        orbRight.getStyleClass().addAll("login-orb", "login-orb-right");

        GridPane content = new GridPane();
        content.getStyleClass().add("login-content-grid");
        content.setAlignment(Pos.CENTER);
        content.setHgap(26);
        content.setPadding(new Insets(48, 24, 60, 24));
        content.maxWidthProperty().bind(Bindings.min(1060.0, centerPane.widthProperty().subtract(52)));

        ColumnConstraints heroColumn = new ColumnConstraints();
        heroColumn.setPercentWidth(58);
        heroColumn.setHgrow(Priority.ALWAYS);

        ColumnConstraints authColumn = new ColumnConstraints();
        authColumn.setPercentWidth(42);
        authColumn.setMinWidth(410);

        content.getColumnConstraints().setAll(heroColumn, authColumn);

        VBox heroPanel = buildHeroPanel();
        heroPanel.setMaxWidth(Double.MAX_VALUE);
        VBox authCard = createAuthCard(currentMode);
        authCard.setMaxWidth(Double.MAX_VALUE);
        authCard.setMinWidth(410);

        content.add(heroPanel, 0, 0);
        content.add(authCard, 1, 0);

        centerPane.getChildren().addAll(orbLeft, orbRight, content);
        StackPane.setAlignment(orbLeft, Pos.TOP_LEFT);
        StackPane.setAlignment(orbRight, Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(content, Pos.CENTER);
        return centerPane;
    }

    private VBox buildHeroPanel() {
        StackPane logo = BrandIconFactory.createLogo(142);
        logo.getStyleClass().add("login-hero-logo");

        StackPane logoShell = new StackPane(logo);
        logoShell.getStyleClass().add("login-hero-mark-shell");
        logoShell.setMinSize(198, 198);
        logoShell.setPrefSize(198, 198);
        logoShell.setMaxSize(198, 198);

        Label eyebrow = new Label("\u5bbf\u820d\u62a5\u4fee\u4e0e\u5de5\u5355");
        eyebrow.getStyleClass().add("login-hero-eyebrow");

        Label title = new Label(BRAND_NAME);
        title.getStyleClass().add("login-hero-title");

        Label subtitle = new Label("\u7edf\u4e00\u8d26\u53f7\u5165\u53e3");
        subtitle.getStyleClass().add("login-hero-subtitle");

        HBox chipRow = new HBox(10,
                createHeroChip("\u63d0\u4ea4\u62a5\u4fee"),
                createHeroChip("\u8fdb\u5ea6\u8ddf\u8fdb"),
                createHeroChip("\u8d26\u53f7\u767b\u5f55")
        );
        chipRow.getStyleClass().add("login-hero-chip-row");
        chipRow.setAlignment(Pos.CENTER_LEFT);

        VBox copyBox = new VBox(10, eyebrow, title, subtitle, chipRow);
        copyBox.getStyleClass().add("login-hero-copy");
        copyBox.setAlignment(Pos.CENTER_LEFT);

        HBox heroBody = new HBox(28, logoShell, copyBox);
        heroBody.getStyleClass().add("login-hero-panel");
        heroBody.setAlignment(Pos.CENTER_LEFT);

        HBox tileRow = new HBox(12,
                createHeroTile("\u7edf\u4e00", "\u8d26\u53f7\u5165\u53e3"),
                createHeroTile("\u5b66\u751f", "\u81ea\u52a9\u6ce8\u518c")
        );
        tileRow.getStyleClass().add("login-hero-tile-row");
        tileRow.setAlignment(Pos.CENTER_LEFT);

        VBox shell = new VBox(16, heroBody, tileRow);
        shell.getStyleClass().add("login-hero-shell");
        shell.setFillWidth(true);
        return shell;
    }

    private Label createHeroChip(String text) {
        Label chip = new Label(text);
        chip.getStyleClass().add("login-hero-chip");
        return chip;
    }

    private VBox createHeroTile(String emphasis, String text) {
        Label emphasisLabel = new Label(emphasis);
        emphasisLabel.getStyleClass().add("login-hero-tile-value");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("login-hero-tile-label");

        VBox tile = new VBox(6, emphasisLabel, textLabel);
        tile.getStyleClass().add("login-hero-tile");
        tile.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tile, Priority.ALWAYS);
        tile.setMaxWidth(Double.MAX_VALUE);
        return tile;
    }

    private VBox createAuthCard(ObjectProperty<LoginMode> currentMode) {
        BooleanProperty submitting = new SimpleBooleanProperty(false);

        TextField loginUsernameField = createInputField("\u8f93\u5165\u8d26\u53f7");
        PasswordInputControl loginPasswordField = createPasswordField("\u8f93\u5165\u5bc6\u7801");

        TextField registerUsernameField = createInputField("\u8bbe\u7f6e\u8d26\u53f7");
        TextField registerDisplayNameField = createInputField("\u8f93\u5165\u59d3\u540d");
        TextField registerPhoneField = createInputField("\u8f93\u5165\u624b\u673a\u53f7");
        PasswordInputControl registerPasswordField = createPasswordField("\u8bbe\u7f6e\u5bc6\u7801");
        PasswordInputControl registerConfirmField = createPasswordField("\u518d\u6b21\u8f93\u5165\u5bc6\u7801");

        TextField resetUsernameField = createInputField("\u8f93\u5165\u8d26\u53f7");
        TextField resetPhoneField = createInputField("\u8f93\u5165\u7ed1\u5b9a\u624b\u673a\u53f7");
        PasswordInputControl resetPasswordField = createPasswordField("\u8f93\u5165\u65b0\u5bc6\u7801");
        PasswordInputControl resetConfirmField = createPasswordField("\u518d\u6b21\u8f93\u5165\u65b0\u5bc6\u7801");

        String rememberedUsername = LOGIN_PREFERENCES.get(LAST_USERNAME_KEY, "").trim();
        if (!rememberedUsername.isEmpty()) {
            loginUsernameField.setText(rememberedUsername);
        }

        Label modeTitleLabel = new Label();
        modeTitleLabel.getStyleClass().add("login-card-title");

        Label modeHintLabel = new Label();
        modeHintLabel.getStyleClass().add("login-mode-hint");
        modeHintLabel.setWrapText(true);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("login-error-label");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        Label registerUsernameStatus = createStatusLabel();
        Label registerPasswordStatus = createStatusLabel();
        Label resetPasswordStatus = createStatusLabel();

        VBox formBody = new VBox(16);

        Button loginModeButton = createModeButton("\u767b\u5f55");
        Button registerModeButton = createModeButton("\u6ce8\u518c");
        Button forgotPasswordButton = createInlineLinkButton("\u5fd8\u8bb0\u5bc6\u7801?");
        HBox modeBar = new HBox(8, loginModeButton, registerModeButton);
        modeBar.getStyleClass().add("login-mode-bar");
        modeBar.setAlignment(Pos.CENTER_LEFT);

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        HBox forgotPasswordRow = new HBox(8, footerSpacer, forgotPasswordButton);
        forgotPasswordRow.setAlignment(Pos.CENTER_RIGHT);
        forgotPasswordRow.managedProperty().bind(forgotPasswordRow.visibleProperty());

        Button submitButton = new Button();
        submitButton.getStyleClass().add("login-primary-button");
        submitButton.setMaxWidth(Double.MAX_VALUE);

        loginModeButton.setOnAction(event -> currentMode.set(LoginMode.LOGIN));
        registerModeButton.setOnAction(event -> currentMode.set(LoginMode.REGISTER));
        forgotPasswordButton.setOnAction(event -> currentMode.set(LoginMode.RESET));

        loginModeButton.disableProperty().bind(submitting);
        registerModeButton.disableProperty().bind(submitting);
        forgotPasswordButton.disableProperty().bind(submitting);

        loginUsernameField.setOnAction(event -> loginPasswordField.requestInputFocus());
        loginPasswordField.setOnAction(event -> submitButton.fire());
        registerUsernameField.setOnAction(event -> registerDisplayNameField.requestFocus());
        registerDisplayNameField.setOnAction(event -> registerPhoneField.requestFocus());
        registerPhoneField.setOnAction(event -> registerPasswordField.requestInputFocus());
        registerPasswordField.setOnAction(event -> registerConfirmField.requestInputFocus());
        registerConfirmField.setOnAction(event -> submitButton.fire());
        resetUsernameField.setOnAction(event -> resetPhoneField.requestFocus());
        resetPhoneField.setOnAction(event -> resetPasswordField.requestInputFocus());
        resetPasswordField.setOnAction(event -> resetConfirmField.requestInputFocus());
        resetConfirmField.setOnAction(event -> submitButton.fire());

        Runnable refreshRegisterUsernameStatus = () -> updateUsernameAvailability(registerUsernameField, registerUsernameStatus);
        Runnable refreshRegisterPasswordStatus = () -> updatePasswordStatus(registerPasswordField, registerConfirmField, registerPasswordStatus);
        Runnable refreshResetPasswordStatus = () -> updatePasswordStatus(resetPasswordField, resetConfirmField, resetPasswordStatus);

        Runnable refreshSubmitState = () -> submitButton.setDisable(submitting.get() || switch (currentMode.get()) {
            case LOGIN -> loginUsernameField.getText().trim().isBlank() || loginPasswordField.getText().isBlank();
            case REGISTER -> registerUsernameField.getText().trim().isBlank()
                    || registerDisplayNameField.getText().trim().isBlank()
                    || registerPhoneField.getText().trim().isBlank()
                    || registerPasswordField.getText().isBlank()
                    || registerConfirmField.getText().isBlank();
            case RESET -> resetUsernameField.getText().trim().isBlank()
                    || resetPhoneField.getText().trim().isBlank()
                    || resetPasswordField.getText().isBlank()
                    || resetConfirmField.getText().isBlank();
        });

        Runnable clearFeedback = () -> clearError(errorLabel);

        attachInputListener(loginUsernameField, clearFeedback, refreshSubmitState);
        attachInputListener(loginPasswordField, clearFeedback, refreshSubmitState);
        attachInputListener(registerUsernameField, clearFeedback, refreshRegisterUsernameStatus, refreshSubmitState);
        attachInputListener(registerDisplayNameField, clearFeedback, refreshSubmitState);
        attachInputListener(registerPhoneField, clearFeedback, refreshSubmitState);
        attachInputListener(registerPasswordField, clearFeedback, refreshRegisterPasswordStatus, refreshSubmitState);
        attachInputListener(registerConfirmField, clearFeedback, refreshRegisterPasswordStatus, refreshSubmitState);
        attachInputListener(resetUsernameField, clearFeedback, refreshSubmitState);
        attachInputListener(resetPhoneField, clearFeedback, refreshSubmitState);
        attachInputListener(resetPasswordField, clearFeedback, refreshResetPasswordStatus, refreshSubmitState);
        attachInputListener(resetConfirmField, clearFeedback, refreshResetPasswordStatus, refreshSubmitState);

        submitting.addListener((observable, oldValue, newValue) -> {
            submitButton.setText(currentMode.get().submitText(newValue));
            refreshSubmitState.run();
        });

        currentMode.addListener((observable, oldMode, newMode) -> {
            clearError(errorLabel);
            formBody.getChildren().setAll(buildModeContent(
                    newMode,
                    modeTitleLabel,
                    modeHintLabel,
                    loginUsernameField,
                    loginPasswordField,
                    registerUsernameField,
                    registerUsernameStatus,
                    registerDisplayNameField,
                    registerPhoneField,
                    registerPasswordField,
                    registerConfirmField,
                    registerPasswordStatus,
                    resetUsernameField,
                    resetPhoneField,
                    resetPasswordField,
                    resetConfirmField,
                    resetPasswordStatus
            ));
            submitButton.setText(newMode.submitText(submitting.get()));
            setModeButtonActive(loginModeButton, newMode == LoginMode.LOGIN);
            setModeButtonActive(registerModeButton, newMode == LoginMode.REGISTER);
            forgotPasswordRow.setVisible(newMode == LoginMode.LOGIN);
            if (newMode == LoginMode.REGISTER) {
                refreshRegisterUsernameStatus.run();
                refreshRegisterPasswordStatus.run();
            }
            if (newMode == LoginMode.RESET) {
                refreshResetPasswordStatus.run();
            }
            requestModeFocus(newMode, loginUsernameField, registerUsernameField, resetUsernameField);
            refreshSubmitState.run();
        });

        submitButton.setOnAction(event -> {
            if (submitting.get()) {
                return;
            }
            clearError(errorLabel);
            submitting.set(true);
            try {
                switch (currentMode.get()) {
                    case LOGIN -> {
                        UserAccount account = appContext.userAccountService().login(
                                loginUsernameField.getText(),
                                loginPasswordField.getText()
                        );
                        rememberUsername(loginUsernameField.getText());
                        loginAction.accept(account);
                    }
                    case REGISTER -> {
                        appContext.userAccountService().registerStudent(
                                registerUsernameField.getText(),
                                registerPasswordField.getText(),
                                registerConfirmField.getText(),
                                registerDisplayNameField.getText(),
                                registerPhoneField.getText()
                        );
                        UiAlerts.info("\u6ce8\u518c\u6210\u529f", "\u8d26\u53f7\u5df2\u521b\u5efa\uff0c\u8bf7\u4f7f\u7528\u65b0\u8d26\u53f7\u767b\u5f55\u3002");
                        loginUsernameField.setText(registerUsernameField.getText().trim());
                        loginPasswordField.clear();
                        clearRegisterFields(
                                registerUsernameField,
                                registerDisplayNameField,
                                registerPhoneField,
                                registerPasswordField,
                                registerConfirmField
                        );
                        hideStatus(registerUsernameStatus);
                        hideStatus(registerPasswordStatus);
                        currentMode.set(LoginMode.LOGIN);
                    }
                    case RESET -> {
                        appContext.userAccountService().resetPasswordByPhone(
                                resetUsernameField.getText(),
                                resetPhoneField.getText(),
                                resetPasswordField.getText(),
                                resetConfirmField.getText()
                        );
                        UiAlerts.info("\u91cd\u7f6e\u6210\u529f", "\u5bc6\u7801\u5df2\u91cd\u7f6e\uff0c\u8bf7\u4f7f\u7528\u65b0\u5bc6\u7801\u91cd\u65b0\u767b\u5f55\u3002");
                        loginUsernameField.setText(resetUsernameField.getText().trim());
                        loginPasswordField.clear();
                        clearResetFields(resetUsernameField, resetPhoneField, resetPasswordField, resetConfirmField);
                        hideStatus(resetPasswordStatus);
                        currentMode.set(LoginMode.LOGIN);
                    }
                }
            } catch (BusinessException exception) {
                showInlineError(errorLabel, exception.getMessage());
            } catch (RuntimeException exception) {
                UiAlerts.error("\u64cd\u4f5c\u5931\u8d25", exception);
            } finally {
                submitting.set(false);
                refreshSubmitState.run();
            }
        });

        VBox authHeader = new VBox(6, modeTitleLabel, modeHintLabel);
        authHeader.getStyleClass().add("login-header-box");

        VBox authCard = new VBox(18, modeBar, authHeader, formBody, forgotPasswordRow, errorLabel, submitButton);
        authCard.getStyleClass().add("login-auth-card");
        authCard.setFillWidth(true);
        authCard.setMaxWidth(Double.MAX_VALUE);

        currentMode.set(LoginMode.LOGIN);
        return authCard;
    }

    private VBox[] buildModeContent(
            LoginMode mode,
            Label modeTitleLabel,
            Label modeHintLabel,
            TextField loginUsernameField,
            PasswordInputControl loginPasswordField,
            TextField registerUsernameField,
            Label registerUsernameStatus,
            TextField registerDisplayNameField,
            TextField registerPhoneField,
            PasswordInputControl registerPasswordField,
            PasswordInputControl registerConfirmField,
            Label registerPasswordStatus,
            TextField resetUsernameField,
            TextField resetPhoneField,
            PasswordInputControl resetPasswordField,
            PasswordInputControl resetConfirmField,
            Label resetPasswordStatus
    ) {
        modeTitleLabel.setText(mode.title());
        modeHintLabel.setText(mode.description());
        return switch (mode) {
            case LOGIN -> new VBox[] {
                    createFieldGroup("\u8d26\u53f7", loginUsernameField),
                    createFieldGroup("\u5bc6\u7801", loginPasswordField)
            };
            case REGISTER -> new VBox[] {
                    createFieldGroup("\u8d26\u53f7", registerUsernameField, registerUsernameStatus),
                    createFieldGroup("\u59d3\u540d", registerDisplayNameField),
                    createFieldGroup("\u624b\u673a\u53f7", registerPhoneField),
                    createFieldGroup("\u5bc6\u7801", registerPasswordField),
                    createFieldGroup("\u786e\u8ba4\u5bc6\u7801", registerConfirmField, registerPasswordStatus)
            };
            case RESET -> new VBox[] {
                    createFieldGroup("\u8d26\u53f7", resetUsernameField),
                    createFieldGroup("\u624b\u673a\u53f7", resetPhoneField),
                    createFieldGroup("\u65b0\u5bc6\u7801", resetPasswordField),
                    createFieldGroup("\u786e\u8ba4\u65b0\u5bc6\u7801", resetConfirmField, resetPasswordStatus)
            };
        };
    }

    private static TextField createInputField(String promptText) {
        TextField field = new TextField();
        field.getStyleClass().addAll("text-field", "login-input");
        field.setPromptText(promptText);
        return field;
    }

    private static PasswordInputControl createPasswordField(String promptText) {
        PasswordInputControl field = new PasswordInputControl();
        field.setPromptText(promptText);
        return field;
    }

    private static Label createStatusLabel() {
        Label label = new Label();
        label.getStyleClass().add("login-status-label");
        label.setManaged(false);
        label.setVisible(false);
        label.setWrapText(true);
        return label;
    }

    private static VBox createFieldGroup(String labelText, Node... nodes) {
        Label label = new Label(labelText);
        label.getStyleClass().add("login-input-label");
        VBox box = new VBox(8);
        box.setFillWidth(true);
        box.getChildren().add(label);
        box.getChildren().addAll(nodes);
        return box;
    }

    private static Button createModeButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("login-mode-button");
        button.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(button, Priority.ALWAYS);
        return button;
    }

    private static Button createInlineLinkButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("login-inline-link");
        return button;
    }

    private static void setModeButtonActive(Button button, boolean active) {
        if (active) {
            if (!button.getStyleClass().contains("login-mode-button-active")) {
                button.getStyleClass().add("login-mode-button-active");
            }
        } else {
            button.getStyleClass().remove("login-mode-button-active");
        }
    }

    private void updateUsernameAvailability(TextField usernameField, Label statusLabel) {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            hideStatus(statusLabel);
            return;
        }
        if (!USERNAME_HINT_PATTERN.matcher(username).matches()) {
            showStatus(statusLabel, "\u8d26\u53f7\u9700\u4e3a 4-32 \u4f4d\u5b57\u6bcd\u3001\u6570\u5b57\u6216\u4e0b\u5212\u7ebf\u3002", false);
            return;
        }
        try {
            boolean available = appContext.userAccountService().isUsernameAvailable(username);
            showStatus(statusLabel, available ? "\u8d26\u53f7\u53ef\u4f7f\u7528\u3002" : "\u8d26\u53f7\u5df2\u5b58\u5728\uff0c\u8bf7\u66f4\u6362\u540e\u518d\u8bd5\u3002", available);
        } catch (BusinessException exception) {
            showStatus(statusLabel, exception.getMessage(), false);
        } catch (RuntimeException exception) {
            showStatus(statusLabel, "\u6682\u65f6\u65e0\u6cd5\u6821\u9a8c\u8d26\u53f7\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5\u3002", false);
        }
    }

    private static void updatePasswordStatus(
            PasswordInputControl passwordField,
            PasswordInputControl confirmField,
            Label statusLabel
    ) {
        String password = passwordField.getText();
        String confirm = confirmField.getText();
        if (password.isBlank() && confirm.isBlank()) {
            hideStatus(statusLabel);
            return;
        }
        if (password.length() < 6) {
            showStatus(statusLabel, "\u5bc6\u7801\u81f3\u5c11\u4e3a 6 \u4f4d\u3002", false);
            return;
        }
        if (!confirm.isBlank() && !password.equals(confirm)) {
            showStatus(statusLabel, "\u4e24\u6b21\u8f93\u5165\u7684\u5bc6\u7801\u4e0d\u4e00\u81f4\u3002", false);
            return;
        }
        if (confirm.isBlank()) {
            showStatus(statusLabel, "\u5bc6\u7801\u53ef\u4f7f\u7528\uff0c\u8bf7\u518d\u786e\u8ba4\u4e00\u6b21\u3002", true);
            return;
        }
        showStatus(statusLabel, "\u4e24\u6b21\u5bc6\u7801\u8f93\u5165\u4e00\u81f4\u3002", true);
    }

    private static void attachInputListener(TextField field, Runnable... actions) {
        field.textProperty().addListener((observable, oldValue, newValue) -> runActions(actions));
    }

    private static void attachInputListener(PasswordInputControl field, Runnable... actions) {
        field.textProperty().addListener((observable, oldValue, newValue) -> runActions(actions));
    }

    private static void runActions(Runnable... actions) {
        for (Runnable action : actions) {
            if (action != null) {
                action.run();
            }
        }
    }

    private static void requestModeFocus(
            LoginMode mode,
            TextField loginUsernameField,
            TextField registerUsernameField,
            TextField resetUsernameField
    ) {
        switch (mode) {
            case LOGIN -> loginUsernameField.requestFocus();
            case REGISTER -> registerUsernameField.requestFocus();
            case RESET -> resetUsernameField.requestFocus();
        }
    }

    private static void clearRegisterFields(
            TextField registerUsernameField,
            TextField registerDisplayNameField,
            TextField registerPhoneField,
            PasswordInputControl registerPasswordField,
            PasswordInputControl registerConfirmField
    ) {
        registerUsernameField.clear();
        registerDisplayNameField.clear();
        registerPhoneField.clear();
        registerPasswordField.clear();
        registerConfirmField.clear();
    }

    private static void clearResetFields(
            TextField resetUsernameField,
            TextField resetPhoneField,
            PasswordInputControl resetPasswordField,
            PasswordInputControl resetConfirmField
    ) {
        resetUsernameField.clear();
        resetPhoneField.clear();
        resetPasswordField.clear();
        resetConfirmField.clear();
    }

    private static void rememberUsername(String username) {
        String normalized = username == null ? "" : username.trim();
        if (normalized.isEmpty()) {
            LOGIN_PREFERENCES.remove(LAST_USERNAME_KEY);
            return;
        }
        LOGIN_PREFERENCES.put(LAST_USERNAME_KEY, normalized);
    }

    private static void showStatus(Label label, String text, boolean success) {
        label.setText(text);
        label.setManaged(true);
        label.setVisible(true);
        label.getStyleClass().removeAll("login-status-label-success", "login-status-label-warning");
        label.getStyleClass().add(success ? "login-status-label-success" : "login-status-label-warning");
    }

    private static void hideStatus(Label label) {
        label.setText("");
        label.setManaged(false);
        label.setVisible(false);
        label.getStyleClass().removeAll("login-status-label-success", "login-status-label-warning");
    }

    private static void showInlineError(Label errorLabel, String text) {
        errorLabel.setText(text);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    private static void clearError(Label errorLabel) {
        errorLabel.setText("");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }

    private enum LoginMode {
        LOGIN(
                "\u8d26\u53f7\u767b\u5f55",
                "\u4f7f\u7528\u8d26\u53f7\u4e0e\u5bc6\u7801\u8fdb\u5165\u5de5\u4f5c\u53f0\u3002",
                "\u767b\u5f55",
                "\u6b63\u5728\u767b\u5f55..."
        ),
        REGISTER(
                "\u6ce8\u518c\u8d26\u53f7",
                "\u4ec5\u5f00\u653e\u5b66\u751f\u81ea\u52a9\u6ce8\u518c\u3002",
                "\u6ce8\u518c\u8d26\u53f7",
                "\u6b63\u5728\u6ce8\u518c..."
        ),
        RESET(
                "\u627e\u56de\u5bc6\u7801",
                "\u901a\u8fc7\u7ed1\u5b9a\u624b\u673a\u53f7\u91cd\u7f6e\u5bc6\u7801\u3002",
                "\u91cd\u7f6e\u5bc6\u7801",
                "\u6b63\u5728\u5904\u7406..."
        );

        private final String title;
        private final String description;
        private final String submitText;
        private final String busyText;

        LoginMode(String title, String description, String submitText, String busyText) {
            this.title = title;
            this.description = description;
            this.submitText = submitText;
            this.busyText = busyText;
        }

        public String title() {
            return title;
        }

        public String description() {
            return description;
        }

        public String submitText(boolean busy) {
            return busy ? busyText : submitText;
        }
    }
}