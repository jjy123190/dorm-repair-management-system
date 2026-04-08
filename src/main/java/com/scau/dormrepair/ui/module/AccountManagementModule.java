package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.command.AccountFilter;
import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.ui.component.AppDropdown;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.component.PasswordInputControl;
import com.scau.dormrepair.ui.support.UiAlerts;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AccountManagementModule extends AbstractWorkbenchModule {

    public AccountManagementModule(AppContext appContext) {
        super(appContext);
    }

    @Override
    public String moduleCode() {
        return "account-management";
    }

    @Override
    public String moduleName() {
        return "\u8d26\u53f7\u7ba1\u7406";
    }

    @Override
    public String moduleDescription() {
        return "";
    }

    @Override
    public Set<UserRole> supportedRoles() {
        return EnumSet.of(UserRole.ADMIN);
    }

    @Override
    public boolean cacheViewOnSwitch() {
        return true;
    }

    @Override
    public Parent createView() {
        UserAccount currentAdmin = appContext.userAccountService()
                .requireCurrentAccount(appContext.appSession(), UserRole.ADMIN);

        AppDropdown<RoleFilterOption> roleFilterBox = new AppDropdown<>();
        roleFilterBox.setItems(List.of(RoleFilterOption.ALL, RoleFilterOption.ADMIN, RoleFilterOption.WORKER));
        roleFilterBox.setTextMapper(RoleFilterOption::label);
        roleFilterBox.setPromptText("\u7b5b\u9009\u89d2\u8272");
        roleFilterBox.setVisibleRowCount(4);
        roleFilterBox.setValue(RoleFilterOption.ALL);

        AppDropdown<AccountEnabledOption> enabledFilterBox = new AppDropdown<>();
        enabledFilterBox.setItems(List.of(AccountEnabledOption.ALL, AccountEnabledOption.ENABLED, AccountEnabledOption.DISABLED));
        enabledFilterBox.setTextMapper(AccountEnabledOption::label);
        enabledFilterBox.setPromptText("\u7b5b\u9009\u72b6\u6001");
        enabledFilterBox.setVisibleRowCount(4);
        enabledFilterBox.setValue(AccountEnabledOption.ALL);

        TextField keywordField = createSearchField("\u641c\u7d22\u8d26\u53f7\u3001\u59d3\u540d\u6216\u624b\u673a\u53f7");
        ObservableList<UserAccount> sourceRows = FXCollections.observableArrayList();
        ObjectProperty<UserAccount> selectedAccount = new SimpleObjectProperty<>();
        VBox accountTableContainer = buildAccountTableContainer();

        Runnable applySearch = () -> applyAccountFilter(sourceRows, keywordField.getText(), accountTableContainer, selectedAccount);
        Runnable refreshAccounts = () -> reloadAccounts(
                sourceRows,
                roleFilterBox.getValue(),
                enabledFilterBox.getValue(),
                keywordField.getText(),
                accountTableContainer,
                selectedAccount
        );

        roleFilterBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshAccounts.run());
        enabledFilterBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshAccounts.run());
        keywordField.textProperty().addListener((observable, oldValue, newValue) -> applySearch.run());
        refreshAccounts.run();

        Node workspace = createRatioWorkspace(
                34,
                66,
                buildManageForm(currentAdmin, selectedAccount, refreshAccounts),
                buildAccountListPanel(keywordField, roleFilterBox, enabledFilterBox, accountTableContainer, refreshAccounts)
        );

        return createPage("\u5185\u90e8\u7528\u6237\u7ba1\u7406", "", workspace);
    }

    private Node buildManageForm(
            UserAccount currentAdmin,
            ObjectProperty<UserAccount> selectedAccount,
            Runnable refreshAccounts
    ) {
        TextField usernameField = createSearchField("\u4f8b\u5982 admin03 \u6216 worker04");
        TextField displayNameField = createSearchField("\u8f93\u5165\u59d3\u540d");
        TextField phoneField = createSearchField("\u8f93\u5165\u624b\u673a\u53f7");

        AppDropdown<UserRole> roleBox = createInternalRoleDropdown();
        roleBox.setValue(UserRole.WORKER);

        PasswordInputControl passwordField = new PasswordInputControl();
        passwordField.setPromptText("\u8bbe\u7f6e\u521d\u59cb\u5bc6\u7801");
        PasswordInputControl confirmPasswordField = new PasswordInputControl();
        confirmPasswordField.setPromptText("\u518d\u6b21\u8f93\u5165\u521d\u59cb\u5bc6\u7801");

        TextField editDisplayNameField = createSearchField("\u8f93\u5165\u59d3\u540d");
        TextField editPhoneField = createSearchField("\u8f93\u5165\u624b\u673a\u53f7");
        AppDropdown<UserRole> editRoleBox = createInternalRoleDropdown();
        TextField statusField = createReadOnlyField("\u672a\u9009\u4e2d");

        editDisplayNameField.disableProperty().bind(Bindings.isNull(selectedAccount));
        editPhoneField.disableProperty().bind(Bindings.isNull(selectedAccount));
        editRoleBox.disableProperty().bind(Bindings.isNull(selectedAccount));

        PasswordInputControl resetPasswordField = new PasswordInputControl();
        resetPasswordField.setPromptText("\u8f93\u5165\u65b0\u5bc6\u7801");
        PasswordInputControl resetConfirmField = new PasswordInputControl();
        resetConfirmField.setPromptText("\u518d\u6b21\u8f93\u5165\u65b0\u5bc6\u7801");
        resetPasswordField.disableProperty().bind(Bindings.isNull(selectedAccount));
        resetConfirmField.disableProperty().bind(Bindings.isNull(selectedAccount));

        Label selectedTitleLabel = new Label("\u672a\u9009\u62e9\u8d26\u53f7");
        selectedTitleLabel.getStyleClass().add("dashboard-mini-value");
        selectedTitleLabel.setWrapText(true);
        selectedTitleLabel.setMaxWidth(Double.MAX_VALUE);

        Label selectedMetaLabel = new Label("\u5148\u4ece\u53f3\u4fa7\u9009\u4e2d\u4e00\u6761\u5185\u90e8\u8d26\u53f7\uff0c\u518d\u7ef4\u62a4\u59d3\u540d\u3001\u624b\u673a\u3001\u89d2\u8272\u3001\u5bc6\u7801\u6216\u8d26\u53f7\u72b6\u6001\u3002");
        selectedMetaLabel.getStyleClass().add("dashboard-mini-description");
        selectedMetaLabel.setWrapText(true);
        selectedMetaLabel.setMaxWidth(Double.MAX_VALUE);

        Label selectedStatusTag = new Label("\u672a\u9009\u4e2d");
        selectedStatusTag.getStyleClass().add("dashboard-mini-tag");

        VBox selectedSummaryBox = new VBox(8, selectedTitleLabel, selectedMetaLabel, selectedStatusTag);
        selectedSummaryBox.setFillWidth(true);
        Node selectedSummaryCard = createInlineSummaryCard(
                "\u5f53\u524d\u9009\u4e2d",
                selectedSummaryBox,
                "dashboard-metric-card",
                "dashboard-metric-card-highlight"
        );

        selectedAccount.addListener((observable, oldValue, newValue) -> {
            resetPasswordField.clear();
            resetConfirmField.clear();
            if (newValue == null) {
                selectedTitleLabel.setText("\u672a\u9009\u62e9\u8d26\u53f7");
                selectedMetaLabel.setText("\u5148\u4ece\u53f3\u4fa7\u9009\u4e2d\u4e00\u6761\u5185\u90e8\u8d26\u53f7\uff0c\u518d\u7ef4\u62a4\u59d3\u540d\u3001\u624b\u673a\u3001\u89d2\u8272\u3001\u5bc6\u7801\u6216\u8d26\u53f7\u72b6\u6001\u3002");
                selectedStatusTag.setText("\u672a\u9009\u4e2d");
                editDisplayNameField.clear();
                editPhoneField.clear();
                editRoleBox.setValue(UserRole.WORKER);
                statusField.setText("\u672a\u9009\u4e2d");
                return;
            }
            selectedTitleLabel.setText(newValue.getUsername() + "  /  " + safeText(newValue.getDisplayName()));
            selectedMetaLabel.setText(
                    "\u89d2\u8272\uff1a" + newValue.getRoleCode().displayName()
                            + "    \u624b\u673a\uff1a" + safeText(newValue.getPhone())
            );
            selectedStatusTag.setText(accountStatusText(newValue));
            editDisplayNameField.setText(safeText(newValue.getDisplayName()));
            editPhoneField.setText(safeText(newValue.getPhone()));
            editRoleBox.setValue(newValue.getRoleCode());
            statusField.setText(accountStatusText(newValue));
        });

        Node createButton = FusionUiFactory.createPrimaryButton("\u521b\u5efa\u5185\u90e8\u8d26\u53f7", 188, 40, () -> {
            try {
                appContext.userAccountService().createInternalAccount(
                        usernameField.getText(),
                        passwordField.getText(),
                        confirmPasswordField.getText(),
                        displayNameField.getText(),
                        phoneField.getText(),
                        roleBox.getValue()
                );
                usernameField.clear();
                displayNameField.clear();
                phoneField.clear();
                passwordField.clear();
                confirmPasswordField.clear();
                roleBox.setValue(UserRole.WORKER);
                refreshAccounts.run();
                UiAlerts.info("\u521b\u5efa\u6210\u529f", "\u5185\u90e8\u8d26\u53f7\u5df2\u521b\u5efa\uff0c\u53ef\u4ee5\u7acb\u5373\u767b\u5f55\u3002");
            } catch (RuntimeException exception) {
                UiAlerts.error("\u521b\u5efa\u5931\u8d25", exception);
            }
        }).getNode();

        Node saveProfileButton = FusionUiFactory.createPrimaryButton("\u4fdd\u5b58\u8d44\u6599", 160, 40, () -> {
            UserAccount selected = selectedAccount.get();
            if (selected == null) {
                UiAlerts.error("\u4fdd\u5b58\u5931\u8d25", "\u8bf7\u5148\u9009\u62e9\u4e00\u4e2a\u5185\u90e8\u8d26\u53f7\u3002");
                return;
            }
            if (currentAdmin.getId().equals(selected.getId()) && editRoleBox.getValue() != UserRole.ADMIN) {
                UiAlerts.error("\u4fdd\u5b58\u5931\u8d25", "\u5f53\u524d\u767b\u5f55\u7684\u7ba1\u7406\u5458\u4e0d\u80fd\u628a\u81ea\u5df1\u7684\u89d2\u8272\u6539\u6210\u975e\u7ba1\u7406\u5458\u3002");
                return;
            }
            try {
                UserAccount updated = appContext.userAccountService().updateInternalAccountProfile(
                        selected.getId(),
                        editDisplayNameField.getText(),
                        editPhoneField.getText(),
                        editRoleBox.getValue()
                );
                if (appContext.appSession().getCurrentAccountId() != null
                        && appContext.appSession().getCurrentAccountId().equals(updated.getId())) {
                    appContext.appSession().login(updated);
                }
                refreshAccounts.run();
                UiAlerts.info("\u4fdd\u5b58\u6210\u529f", "\u8d26\u53f7\u8d44\u6599\u5df2\u66f4\u65b0\u3002");
            } catch (RuntimeException exception) {
                UiAlerts.error("\u4fdd\u5b58\u5931\u8d25", exception);
            }
        }).getNode();
        saveProfileButton.disableProperty().bind(Bindings.isNull(selectedAccount));

        Button toggleEnabledButton = new Button();
        toggleEnabledButton.getStyleClass().add("surface-button");
        toggleEnabledButton.setMaxWidth(Double.MAX_VALUE);
        toggleEnabledButton.textProperty().bind(Bindings.createStringBinding(() -> {
            UserAccount selected = selectedAccount.get();
            if (selected == null || Boolean.TRUE.equals(selected.getEnabled())) {
                return "\u505c\u7528\u8d26\u53f7";
            }
            return "\u542f\u7528\u8d26\u53f7";
        }, selectedAccount));
        toggleEnabledButton.disableProperty().bind(Bindings.isNull(selectedAccount));
        toggleEnabledButton.setOnAction(event -> {
            UserAccount selected = selectedAccount.get();
            if (selected == null) {
                UiAlerts.error("\u64cd\u4f5c\u5931\u8d25", "\u8bf7\u5148\u9009\u62e9\u4e00\u4e2a\u5185\u90e8\u8d26\u53f7\u3002");
                return;
            }
            if (currentAdmin.getId().equals(selected.getId()) && Boolean.TRUE.equals(selected.getEnabled())) {
                UiAlerts.error("\u64cd\u4f5c\u5931\u8d25", "\u4e0d\u80fd\u505c\u7528\u5f53\u524d\u767b\u5f55\u4e2d\u7684\u7ba1\u7406\u5458\u8d26\u53f7\u3002");
                return;
            }
            boolean nextEnabled = !Boolean.TRUE.equals(selected.getEnabled());
            String actionText = nextEnabled ? "\u542f\u7528" : "\u505c\u7528";
            boolean confirmed = UiAlerts.confirm(
                    "\u786e\u8ba4\u64cd\u4f5c",
                    "\u786e\u5b9a\u8981" + actionText + "\u8d26\u53f7 " + selected.getUsername() + " \u5417\uff1f",
                    actionText + "\u8d26\u53f7"
            );
            if (!confirmed) {
                return;
            }
            try {
                appContext.userAccountService().setAccountEnabled(selected.getId(), nextEnabled);
                refreshAccounts.run();
                UiAlerts.info("\u64cd\u4f5c\u6210\u529f", "\u8d26\u53f7\u72b6\u6001\u5df2\u66f4\u65b0\u3002");
            } catch (RuntimeException exception) {
                UiAlerts.error("\u64cd\u4f5c\u5931\u8d25", exception);
            }
        });

        Node resetButton = FusionUiFactory.createPrimaryButton("\u91cd\u7f6e\u5bc6\u7801", 160, 40, () -> {
            UserAccount selected = selectedAccount.get();
            if (selected == null) {
                UiAlerts.error("\u91cd\u7f6e\u5931\u8d25", "\u8bf7\u5148\u9009\u62e9\u4e00\u4e2a\u5185\u90e8\u8d26\u53f7\u3002");
                return;
            }
            try {
                appContext.userAccountService().adminResetPassword(
                        selected.getId(),
                        resetPasswordField.getText(),
                        resetConfirmField.getText()
                );
                resetPasswordField.clear();
                resetConfirmField.clear();
                UiAlerts.info("\u91cd\u7f6e\u6210\u529f", "\u5bc6\u7801\u5df2\u66f4\u65b0\u3002");
            } catch (RuntimeException exception) {
                UiAlerts.error("\u91cd\u7f6e\u5931\u8d25", exception);
            }
        }).getNode();
        resetButton.disableProperty().bind(Bindings.isNull(selectedAccount));

        HBox selectedActionRow = new HBox(12, toggleEnabledButton, resetButton);
        selectedActionRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(toggleEnabledButton, Priority.ALWAYS);
        if (resetButton instanceof Region resetRegion) {
            resetRegion.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(resetRegion, Priority.ALWAYS);
        }

        VBox createForm = new VBox(
                12,
                createFieldRow("\u8d26\u53f7", usernameField, "\u89d2\u8272", roleBox),
                createFieldRow("\u59d3\u540d", displayNameField, "\u624b\u673a\u53f7", phoneField),
                createFieldRow("\u521d\u59cb\u5bc6\u7801", passwordField, "\u786e\u8ba4\u521d\u59cb\u5bc6\u7801", confirmPasswordField),
                createButtonRow(createButton)
        );

        VBox selectedForm = new VBox(
                12,
                selectedSummaryCard,
                createFieldRow("\u59d3\u540d", editDisplayNameField, "\u624b\u673a\u53f7", editPhoneField),
                createFieldRow("\u89d2\u8272", editRoleBox, "\u8d26\u53f7\u72b6\u6001", statusField),
                createButtonRow(saveProfileButton),
                createFieldRow("\u65b0\u5bc6\u7801", resetPasswordField, "\u786e\u8ba4\u65b0\u5bc6\u7801", resetConfirmField),
                selectedActionRow
        );

        VBox body = new VBox(
                18,
                wrapPanel("\u65b0\u5efa\u5185\u90e8\u8d26\u53f7", createForm),
                wrapPanel("\u5df2\u9009\u8d26\u53f7\u7ef4\u62a4", selectedForm)
        );
        body.setFillWidth(true);
        return body;
    }

    private Node buildAccountListPanel(
            TextField keywordField,
            AppDropdown<RoleFilterOption> roleFilterBox,
            AppDropdown<AccountEnabledOption> enabledFilterBox,
            VBox accountTableContainer,
            Runnable refreshAccounts
    ) {
        Button refreshButton = createFilterActionButton("\u5237\u65b0\u5217\u8868", refreshAccounts);

        keywordField.setMaxWidth(Double.MAX_VALUE);
        keywordField.setMinWidth(0);
        roleFilterBox.setMaxWidth(Double.MAX_VALUE);
        roleFilterBox.setMinWidth(0);
        enabledFilterBox.setMaxWidth(Double.MAX_VALUE);
        enabledFilterBox.setMinWidth(0);

        GridPane filterGrid = createFilterGrid(34, 20, 20, 14);
        filterGrid.add(createFieldLabel("\u5173\u952e\u8bcd\u7b5b\u9009"), 0, 0);
        filterGrid.add(createFieldLabel("\u89d2\u8272\u7b5b\u9009"), 1, 0);
        filterGrid.add(createFieldLabel("\u72b6\u6001\u7b5b\u9009"), 2, 0);
        filterGrid.add(keywordField, 0, 1);
        filterGrid.add(roleFilterBox, 1, 1);
        filterGrid.add(enabledFilterBox, 2, 1);
        filterGrid.add(refreshButton, 3, 1);
        GridPane.setValignment(refreshButton, VPos.TOP);
        GridPane.setHgrow(keywordField, Priority.ALWAYS);
        GridPane.setHgrow(roleFilterBox, Priority.ALWAYS);
        GridPane.setHgrow(enabledFilterBox, Priority.ALWAYS);

        VBox content = new VBox(14, filterGrid, accountTableContainer);
        content.setFillWidth(true);
        VBox.setVgrow(accountTableContainer, Priority.ALWAYS);
        return wrapPanel("\u5185\u90e8\u8d26\u53f7\u5217\u8868", content);
    }

    private HBox createButtonRow(Node primaryButton) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(12, spacer, primaryButton);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox createFieldRow(String leftLabel, Region leftInput, String rightLabel, Region rightInput) {
        VBox leftBlock = createFieldBlock(leftLabel, leftInput);
        VBox rightBlock = createFieldBlock(rightLabel, rightInput);
        HBox row = new HBox(12, leftBlock, rightBlock);
        row.setAlignment(Pos.TOP_LEFT);
        row.setFillHeight(true);
        HBox.setHgrow(leftBlock, Priority.ALWAYS);
        HBox.setHgrow(rightBlock, Priority.ALWAYS);
        return row;
    }

    private AppDropdown<UserRole> createInternalRoleDropdown() {
        AppDropdown<UserRole> roleBox = new AppDropdown<>();
        roleBox.setItems(List.of(UserRole.WORKER, UserRole.ADMIN));
        roleBox.setTextMapper(UserRole::displayName);
        roleBox.setPromptText("\u9009\u62e9\u89d2\u8272");
        roleBox.setVisibleRowCount(4);
        return roleBox;
    }

    private VBox buildAccountTableContainer() {
        VBox container = new VBox();
        container.setFillWidth(true);
        container.setMaxWidth(Double.MAX_VALUE);
        container.setMinWidth(0);
        VBox.setVgrow(container, Priority.ALWAYS);
        return container;
    }

    private void reloadAccounts(
            ObservableList<UserAccount> sourceRows,
            RoleFilterOption roleOption,
            AccountEnabledOption enabledOption,
            String keyword,
            VBox accountTableContainer,
            ObjectProperty<UserAccount> selectedAccount
    ) {
        AccountFilter filter = new AccountFilter(
                roleOption == null ? null : roleOption.role(),
                enabledOption == null ? null : enabledOption.enabled()
        );
        List<UserAccount> rows = appContext.userAccountService().listAccounts(filter).stream()
                .filter(account -> account.getRoleCode() != UserRole.STUDENT)
                .toList();
        sourceRows.setAll(rows);
        applyAccountFilter(sourceRows, keyword, accountTableContainer, selectedAccount);
    }

    private void applyAccountFilter(
            ObservableList<UserAccount> sourceRows,
            String keyword,
            VBox accountTableContainer,
            ObjectProperty<UserAccount> selectedAccount
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);
        Long selectedId = selectedAccount.get() == null ? null : selectedAccount.get().getId();
        List<UserAccount> visibleRows = sourceRows.stream()
                .filter(account -> matchesKeyword(
                        normalizedKeyword,
                        account.getUsername(),
                        account.getDisplayName(),
                        account.getPhone()
                ))
                .toList();
        UserAccount restoredSelection = selectedId == null
                ? null
                : visibleRows.stream().filter(account -> selectedId.equals(account.getId())).findFirst().orElse(null);
        selectedAccount.set(restoredSelection);
        renderAccountTable(accountTableContainer, visibleRows, selectedAccount);
    }

    private void renderAccountTable(
            VBox accountTableContainer,
            List<UserAccount> visibleRows,
            ObjectProperty<UserAccount> selectedAccount
    ) {
        accountTableContainer.getChildren().clear();
        if (visibleRows.isEmpty()) {
            accountTableContainer.getChildren().add(
                    createEmptyState(
                            "\u5f53\u524d\u6ca1\u6709\u5185\u90e8\u8d26\u53f7",
                            "\u65b0\u589e\u5bbf\u7ba1\u7ba1\u7406\u5458\u6216\u7ef4\u4fee\u5458\u8d26\u53f7\u540e\uff0c\u4f1a\u663e\u793a\u5728\u8fd9\u91cc\u3002"
                    )
            );
            return;
        }

        GridPane table = new GridPane();
        table.getStyleClass().add("selectable-static-grid-table");
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMinWidth(0);
        table.getColumnConstraints().addAll(
                percentColumn(18),
                percentColumn(19),
                percentColumn(20),
                percentColumn(23),
                percentColumn(20)
        );

        addAccountTableCell(table, 0, 0, "\u89d2\u8272", true, false, true, false, null);
        addAccountTableCell(table, 0, 1, "\u8d26\u53f7", true, false, false, false, null);
        addAccountTableCell(table, 0, 2, "\u59d3\u540d", true, false, false, false, null);
        addAccountTableCell(table, 0, 3, "\u624b\u673a\u53f7", true, false, false, false, null);
        addAccountTableCell(table, 0, 4, "\u72b6\u6001", true, false, false, true, null);

        int visibleCount = Math.max(7, visibleRows.size());
        for (int index = 0; index < visibleCount; index++) {
            int rowIndex = index + 1;
            UserAccount rowItem = index < visibleRows.size() ? visibleRows.get(index) : null;
            boolean isSelected = rowItem != null
                    && selectedAccount.get() != null
                    && selectedAccount.get().getId().equals(rowItem.getId());
            Runnable clickAction = rowItem == null ? null : () -> {
                selectedAccount.set(rowItem);
                renderAccountTable(accountTableContainer, visibleRows, selectedAccount);
            };
            addAccountTableCell(table, rowIndex, 0, rowItem == null ? "" : rowItem.getRoleCode().displayName(), false, isSelected, false, false, clickAction);
            addAccountTableCell(table, rowIndex, 1, rowItem == null ? "" : safeText(rowItem.getUsername()), false, isSelected, false, false, clickAction);
            addAccountTableCell(table, rowIndex, 2, rowItem == null ? "" : safeText(rowItem.getDisplayName()), false, isSelected, false, false, clickAction);
            addAccountTableCell(table, rowIndex, 3, rowItem == null ? "" : safeText(rowItem.getPhone()), false, isSelected, false, false, clickAction);
            addAccountTableCell(table, rowIndex, 4, rowItem == null ? "" : accountStatusText(rowItem), false, isSelected, false, true, clickAction);
        }

        accountTableContainer.getChildren().add(table);
    }

    private void addAccountTableCell(
            GridPane table,
            int rowIndex,
            int columnIndex,
            String text,
            boolean headerCell,
            boolean selected,
            boolean firstHeaderCell,
            boolean lastCell,
            Runnable clickAction
    ) {
        Label label = new Label(text == null ? "" : text);
        label.getStyleClass().add(headerCell ? "static-table-header-cell" : "static-table-cell");
        label.setWrapText(!headerCell);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinWidth(0);

        HBox cellShell = new HBox(label);
        cellShell.setAlignment(Pos.CENTER);
        cellShell.setMaxWidth(Double.MAX_VALUE);
        cellShell.setMinWidth(0);
        cellShell.setMinHeight(48);
        cellShell.getStyleClass().add(headerCell ? "selectable-static-grid-header-shell" : "selectable-static-grid-cell-shell");
        if (firstHeaderCell) {
            cellShell.getStyleClass().add("selectable-static-grid-header-first");
        }
        if (lastCell) {
            cellShell.getStyleClass().add(headerCell ? "selectable-static-grid-header-last" : "selectable-static-grid-cell-last");
        }
        if (selected && !headerCell) {
            cellShell.getStyleClass().add("selectable-static-grid-cell-selected");
        }
        if (clickAction != null) {
            cellShell.getStyleClass().add("selectable-static-grid-clickable");
            cellShell.setOnMouseClicked(event -> clickAction.run());
        }

        table.add(cellShell, columnIndex, rowIndex);
    }

    private TextField createSearchField(String promptText) {
        TextField field = new TextField();
        field.getStyleClass().addAll("text-field", "login-input");
        field.setPromptText(promptText);
        return field;
    }

    private TextField createReadOnlyField(String text) {
        TextField field = createSearchField("");
        field.setText(text);
        field.setEditable(false);
        field.setFocusTraversable(false);
        return field;
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private static String accountStatusText(UserAccount account) {
        return Boolean.TRUE.equals(account.getEnabled()) ? "\u542f\u7528\u4e2d" : "\u5df2\u505c\u7528";
    }

    private enum RoleFilterOption {
        ALL("\u5168\u90e8\u89d2\u8272", null),
        ADMIN("\u7ba1\u7406\u5458", UserRole.ADMIN),
        WORKER("\u7ef4\u4fee\u5458", UserRole.WORKER);

        private final String label;
        private final UserRole role;

        RoleFilterOption(String label, UserRole role) {
            this.label = label;
            this.role = role;
        }

        public String label() {
            return label;
        }

        public UserRole role() {
            return role;
        }
    }

    private enum AccountEnabledOption {
        ALL("\u5168\u90e8\u72b6\u6001", null),
        ENABLED("\u542f\u7528\u4e2d", Boolean.TRUE),
        DISABLED("\u5df2\u505c\u7528", Boolean.FALSE);

        private final String label;
        private final Boolean enabled;

        AccountEnabledOption(String label, Boolean enabled) {
            this.label = label;
            this.enabled = enabled;
        }

        public String label() {
            return label;
        }

        public Boolean enabled() {
            return enabled;
        }
    }
}
