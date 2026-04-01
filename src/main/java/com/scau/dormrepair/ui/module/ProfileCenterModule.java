package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.support.UiAlerts;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ProfileCenterModule extends AbstractWorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Runnable changePasswordAction;
    private final Runnable logoutAction;

    public ProfileCenterModule(
            AppContext appContext,
            Runnable changePasswordAction,
            Runnable logoutAction
    ) {
        super(appContext);
        this.changePasswordAction = changePasswordAction;
        this.logoutAction = logoutAction;
    }

    @Override
    public String moduleCode() {
        return "profile";
    }

    @Override
    public String moduleName() {
        return "\u4e2a\u4eba\u4e2d\u5fc3";
    }

    @Override
    public String moduleDescription() {
        return "";
    }

    @Override
    public Set<UserRole> supportedRoles() {
        return EnumSet.allOf(UserRole.class);
    }

    @Override
    public Parent createView() {
        Long accountId = appContext.appSession().getCurrentAccountId();
        String displayName = appContext.appSession().getDisplayName();
        UserRole currentRole = appContext.appSession().getCurrentRole();
        ObjectProperty<UserAccount> accountState = new SimpleObjectProperty<>(resolveAccount(accountId, displayName, currentRole));

        VBox deck = new VBox(
                20,
                buildBasicInfoCard(accountState),
                createRatioWorkspace(56, 44,
                        wrapPanel("\u8d44\u6599\u7ef4\u62a4", buildProfileEditorPanel(accountState)),
                        wrapPanel("\u5b89\u5168\u4e0e\u767b\u5f55", buildSecurityPanel())
                )
        );
        deck.setFillWidth(true);
        return createPage("\u4e2a\u4eba\u4e2d\u5fc3", "", deck);
    }

    private UserAccount resolveAccount(Long accountId, String displayName, UserRole role) {
        UserAccount account = accountId == null ? null : appContext.userAccountService().getById(accountId);
        if (account != null) {
            return account;
        }
        UserAccount fallback = new UserAccount();
        fallback.setId(accountId);
        fallback.setUsername(appContext.appSession().getUsername());
        fallback.setDisplayName(displayName);
        fallback.setPhone("");
        fallback.setRoleCode(role);
        fallback.setEnabled(Boolean.TRUE);
        return fallback;
    }

    private Node buildBasicInfoCard(ObjectProperty<UserAccount> accountState) {
        Label sectionLabel = new Label("\u57fa\u7840\u8d44\u6599");
        sectionLabel.getStyleClass().add("profile-section-title");

        Label tagLabel = new Label();
        tagLabel.getStyleClass().add("dashboard-mini-tag");
        tagLabel.textProperty().bind(Bindings.createStringBinding(
                () -> safeText(roleDisplay(accountState.get()), "\u6682\u65e0"),
                accountState
        ));

        Label nameLabel = new Label();
        nameLabel.getStyleClass().add("profile-hero-name");
        nameLabel.textProperty().bind(Bindings.createStringBinding(
                () -> safeText(accountState.get() == null ? null : accountState.get().getDisplayName(), "\u6682\u65e0"),
                accountState
        ));

        Label metaLabel = new Label();
        metaLabel.getStyleClass().add("profile-hero-meta");
        metaLabel.setWrapText(true);
        metaLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            UserAccount account = accountState.get();
            if (account == null) {
                return "\u4ec5\u7ef4\u62a4\u59d3\u540d\u548c\u624b\u673a\u53f7\uff0c\u5176\u4ed6\u5de5\u4f5c\u53f0\u4e1a\u52a1\u4ecd\u5728\u5bf9\u5e94\u6a21\u5757\u5904\u7406\u3002";
            }
            return account.getRoleCode() == UserRole.STUDENT
                    ? "\u5b66\u751f\u89c6\u89d2\u53ef\u4ee5\u7ef4\u62a4\u81ea\u5df1\u7684\u59d3\u540d\u548c\u624b\u673a\u53f7\uff0c\u4fbf\u4e8e\u627e\u56de\u5bc6\u7801\u548c\u8054\u7cfb\u786e\u8ba4\u3002"
                    : "\u5185\u90e8\u8d26\u53f7\u53ea\u5728\u8fd9\u91cc\u7ef4\u62a4\u672c\u4eba\u59d3\u540d\u4e0e\u624b\u673a\u53f7\uff0c\u89d2\u8272\u4e0e\u542f\u505c\u7528\u4ecd\u7531\u7ba1\u7406\u7aef\u5904\u7406\u3002";
        }, accountState));

        Label accountChip = createIdentityChip();
        accountChip.textProperty().bind(Bindings.createStringBinding(() -> {
            UserAccount account = accountState.get();
            return identityLabel(account == null ? null : account.getRoleCode())
                    + "  "
                    + safeText(account == null ? null : account.getUsername(), "\u6682\u65e0");
        }, accountState));

        Label roleChip = createIdentityChip();
        roleChip.textProperty().bind(Bindings.createStringBinding(
                () -> "\u89d2\u8272  " + safeText(roleDisplay(accountState.get()), "\u6682\u65e0"),
                accountState
        ));

        HBox chipRow = new HBox(8, accountChip, roleChip);
        chipRow.getStyleClass().add("profile-chip-row");

        VBox leadBox = new VBox(8, sectionLabel, tagLabel, nameLabel, metaLabel, chipRow);
        leadBox.getStyleClass().add("profile-basic-lead");
        leadBox.setFillWidth(true);
        leadBox.setMaxWidth(Double.MAX_VALUE);
        leadBox.setMinWidth(0);

        GridPane infoGrid = new GridPane();
        infoGrid.getStyleClass().add("profile-basic-grid");
        infoGrid.setHgap(12);
        infoGrid.setVgap(12);
        infoGrid.getColumnConstraints().addAll(percentColumn(50), percentColumn(50));
        infoGrid.add(createBoundFactCard("\u59d3\u540d", Bindings.createStringBinding(
                () -> safeText(accountState.get() == null ? null : accountState.get().getDisplayName(), "\u6682\u65e0"),
                accountState
        )), 0, 0);
        infoGrid.add(createBoundFactCard(Bindings.createStringBinding(
                () -> identityLabel(accountState.get() == null ? null : accountState.get().getRoleCode()),
                accountState
        ), Bindings.createStringBinding(
                () -> safeText(accountState.get() == null ? null : accountState.get().getUsername(), "\u6682\u65e0"),
                accountState
        )), 1, 0);
        infoGrid.add(createBoundFactCard("\u89d2\u8272", Bindings.createStringBinding(
                () -> safeText(roleDisplay(accountState.get()), "\u6682\u65e0"),
                accountState
        )), 0, 1);
        infoGrid.add(createBoundFactCard("\u624b\u673a\u53f7", Bindings.createStringBinding(
                () -> safeText(accountState.get() == null ? null : accountState.get().getPhone(), "\u672a\u7ed1\u5b9a"),
                accountState
        )), 1, 1);
        infoGrid.add(createBoundFactCard("\u521b\u5efa\u65f6\u95f4", Bindings.createStringBinding(
                () -> formatTime(accountState.get() == null ? null : accountState.get().getCreatedAt(), "\u6682\u65e0"),
                accountState
        )), 0, 2, 2, 1);
        infoGrid.setMaxWidth(Double.MAX_VALUE);
        infoGrid.setMinWidth(0);

        HBox layout = new HBox(18, leadBox, infoGrid);
        layout.getStyleClass().add("profile-basic-layout");
        layout.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leadBox, Priority.ALWAYS);
        HBox.setHgrow(infoGrid, Priority.ALWAYS);

        var pane = FusionUiFactory.createCard(layout, 0, 0, "profile-hero-card");
        pane.getNode().setMaxWidth(Double.MAX_VALUE);
        return pane.getNode();
    }

    private Node buildProfileEditorPanel(ObjectProperty<UserAccount> accountState) {
        TextField displayNameField = createProfileField("\u8f93\u5165\u59d3\u540d");
        TextField phoneField = createProfileField("\u8f93\u5165\u624b\u673a\u53f7");

        UserAccount initial = accountState.get();
        displayNameField.setText(initial == null ? "" : safeText(initial.getDisplayName(), ""));
        phoneField.setText(initial == null ? "" : safeText(initial.getPhone(), ""));

        accountState.addListener((observable, oldValue, newValue) -> {
            displayNameField.setText(newValue == null ? "" : safeText(newValue.getDisplayName(), ""));
            phoneField.setText(newValue == null ? "" : safeText(newValue.getPhone(), ""));
        });

        Label introLabel = new Label();
        introLabel.getStyleClass().add("profile-section-copy");
        introLabel.setWrapText(true);
        introLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            UserAccount account = accountState.get();
            if (account == null) {
                return "\u53ea\u5141\u8bb8\u7ef4\u62a4\u59d3\u540d\u548c\u624b\u673a\u53f7\u3002";
            }
            return account.getRoleCode() == UserRole.STUDENT
                    ? "\u5b66\u751f\u89c6\u89d2\u53ea\u7ef4\u62a4\u59d3\u540d\u548c\u624b\u673a\u53f7\uff0c\u4e0d\u5728\u8fd9\u91cc\u6539\u5b66\u53f7\u6216\u4e1a\u52a1\u6743\u9650\u3002"
                    : "\u5185\u90e8\u8d26\u53f7\u53ea\u5728\u8fd9\u91cc\u7ef4\u62a4\u4e2a\u4eba\u59d3\u540d\u4e0e\u624b\u673a\u53f7\uff0c\u89d2\u8272\u53ca\u8d26\u53f7\u72b6\u6001\u7531\u7ba1\u7406\u7aef\u63a7\u5236\u3002";
        }, accountState));

        Node saveButton = FusionUiFactory.createPrimaryButton("\u4fdd\u5b58\u57fa\u7840\u8d44\u6599", 0, 42, () -> {
            UserAccount current = accountState.get();
            if (current == null || current.getId() == null) {
                UiAlerts.error("\u4fdd\u5b58\u5931\u8d25", "\u5f53\u524d\u8d26\u53f7\u4e0d\u5b58\u5728\uff0c\u65e0\u6cd5\u4fdd\u5b58\u8d44\u6599\u3002");
                return;
            }
            try {
                UserAccount updated = appContext.userAccountService().updateOwnProfile(
                        current.getId(),
                        displayNameField.getText(),
                        phoneField.getText()
                );
                accountState.set(updated);
                appContext.appSession().login(updated);
                UiAlerts.info("\u4fdd\u5b58\u6210\u529f", "\u4e2a\u4eba\u8d44\u6599\u5df2\u66f4\u65b0\u3002");
            } catch (RuntimeException exception) {
                UiAlerts.error("\u4fdd\u5b58\u5931\u8d25", exception);
            }
        }).getNode();

        VBox content = new VBox(
                12,
                introLabel,
                createFieldRow("\u59d3\u540d", displayNameField, "\u624b\u673a\u53f7", phoneField),
                createActionItem(
                        "\u8d44\u6599\u540c\u6b65",
                        "\u4fdd\u5b58\u540e\u4f1a\u7acb\u5373\u540c\u6b65\u5230\u9875\u9762\u5934\u90e8\u8eab\u4efd\u6807\u8bc6\u548c\u4e2a\u4eba\u5361\u7247\u3002",
                        saveButton
                )
        );
        content.setFillWidth(true);
        return content;
    }

    private HBox createFieldRow(String leftLabel, TextField leftInput, String rightLabel, TextField rightInput) {
        VBox leftBlock = createFieldBlock(leftLabel, leftInput);
        VBox rightBlock = createFieldBlock(rightLabel, rightInput);
        HBox row = new HBox(12, leftBlock, rightBlock);
        row.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(leftBlock, Priority.ALWAYS);
        HBox.setHgrow(rightBlock, Priority.ALWAYS);
        return row;
    }

    private TextField createProfileField(String promptText) {
        TextField field = new TextField();
        field.getStyleClass().addAll("text-field", "login-input");
        field.setPromptText(promptText);
        return field;
    }

    private Label createIdentityChip() {
        Label chip = new Label();
        chip.getStyleClass().add("profile-chip");
        return chip;
    }

    private Node createBoundFactCard(String labelText, ObservableValue<String> valueBinding) {
        Label label = new Label(labelText);
        label.getStyleClass().add("profile-fact-label");
        return createBoundFactCard(label, valueBinding);
    }

    private Node createBoundFactCard(ObservableValue<String> labelBinding, ObservableValue<String> valueBinding) {
        Label label = new Label();
        label.getStyleClass().add("profile-fact-label");
        label.textProperty().bind(labelBinding);
        return createBoundFactCard(label, valueBinding);
    }

    private Node createBoundFactCard(Label label, ObservableValue<String> valueBinding) {
        Label value = new Label();
        value.getStyleClass().add("profile-fact-value");
        value.textProperty().bind(valueBinding);
        value.setWrapText(true);
        value.setMaxWidth(Double.MAX_VALUE);

        VBox box = new VBox(6, label, value);
        box.getStyleClass().add("profile-fact-card");
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setMinWidth(0);
        return box;
    }

    private Node buildSecurityPanel() {
        Label introLabel = new Label("\u8fd9\u91cc\u53ea\u653e\u4e0e\u4f1a\u8bdd\u548c\u5bc6\u7801\u76f4\u63a5\u76f8\u5173\u7684\u52a8\u4f5c\uff0c\u4e0d\u4ece\u4e2a\u4eba\u9875\u62c9\u5165\u5176\u4ed6\u4e1a\u52a1\u64cd\u4f5c\u3002");
        introLabel.getStyleClass().add("profile-section-copy");
        introLabel.setWrapText(true);

        VBox content = new VBox(
                12,
                introLabel,
                createActionItem("\u4fee\u6539\u5bc6\u7801", "\u901a\u8fc7\u65e7\u5bc6\u7801\u6821\u9a8c\u540e\u66f4\u65b0\u5f53\u524d\u8d26\u53f7\u5bc6\u7801\u3002", createActionButton("\u4fee\u6539\u5bc6\u7801", true, changePasswordAction)),
                createActionItem("\u9000\u51fa\u767b\u5f55", "\u7ed3\u675f\u5f53\u524d\u4f1a\u8bdd\uff0c\u56de\u5230\u767b\u5f55\u5165\u53e3\u3002", createActionButton("\u9000\u51fa\u767b\u5f55", false, logoutAction))
        );
        content.getStyleClass().add("profile-security-stack");
        return content;
    }

    private Node createActionItem(String title, String body, Node button) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("profile-action-heading");

        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("profile-action-copy");
        bodyLabel.setWrapText(true);

        VBox box = new VBox(10, titleLabel, bodyLabel, button);
        box.getStyleClass().add("profile-action-card");
        box.setFillWidth(true);
        return box;
    }

    private Node createActionButton(String text, boolean primary, Runnable action) {
        var pane = primary
                ? FusionUiFactory.createPrimaryButton(text, 0, 42, action)
                : FusionUiFactory.createGhostButton(text, 0, 42, action);
        pane.getNode().setMaxWidth(Double.MAX_VALUE);
        return pane.getNode();
    }

    private String roleDisplay(UserAccount account) {
        return account == null || account.getRoleCode() == null ? "" : account.getRoleCode().displayName();
    }

    private String identityLabel(UserRole role) {
        return role == UserRole.STUDENT ? "\u5b66\u53f7" : "\u5de5\u53f7";
    }

    private String formatTime(LocalDateTime value, String fallback) {
        return value == null ? fallback : TIME_FORMATTER.format(value);
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}