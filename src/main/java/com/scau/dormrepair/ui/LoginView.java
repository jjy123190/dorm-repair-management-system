package com.scau.dormrepair.ui;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.DemoAccountDirectory;
import com.scau.dormrepair.common.DemoAccountDirectory.DemoAccount;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/**
 * 登录页现在直接展示可用身份入口。
 * 先按角色选择，再从该角色的稳定账号列表里进入系统，避免继续保留“手填姓名”的样板入口。
 */
public class LoginView {

    private static final Map<UserRole, String> ROLE_HINTS = Map.of(
            UserRole.STUDENT, "提交报修、查看处理进度、完成后补充评价。",
            UserRole.ADMIN, "审核报修、派发工单、跟进维修闭环。",
            UserRole.WORKER, "接收工单、更新处理状态、回填维修结果。"
    );

    private final Consumer<DemoAccount> loginAction;

    public LoginView(AppContext appContext, Consumer<DemoAccount> loginAction) {
        this.loginAction = loginAction;
    }

    public Parent createView() {
        HBox shell = new HBox(28);
        shell.getStyleClass().add("login-shell");
        shell.setAlignment(Pos.CENTER);
        shell.setPadding(new Insets(24, 28, 24, 28));
        shell.setFillHeight(true);

        VBox heroPane = buildHeroPane();
        VBox entryPane = buildEntryPane();

        HBox.setHgrow(heroPane, Priority.ALWAYS);
        heroPane.setMinWidth(0);
        entryPane.setMinWidth(480);
        entryPane.setPrefWidth(480);
        entryPane.setMaxWidth(480);

        shell.getChildren().addAll(heroPane, entryPane);
        return shell;
    }

    private VBox buildHeroPane() {
        Label chipLabel = new Label("SCAU 宿舍维修服务");
        chipLabel.getStyleClass().add("login-hero-chip");

        Label titleLabel = new Label("宿舍报修与工单管理系统");
        titleLabel.getStyleClass().add("login-hero-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label copyLabel = new Label("围绕学生报修、工单派发、维修处理和结果反馈，统一承接宿舍维修业务的完整闭环。");
        copyLabel.getStyleClass().add("login-hero-copy");
        copyLabel.setWrapText(true);
        copyLabel.setMaxWidth(Double.MAX_VALUE);

        VBox flowBox = new VBox(
                12,
                createFlowItem("01", "提交报修", "学生选择宿舍区、楼栋和房间，补充联系方式、故障类型与故障描述。"),
                createFlowItem("02", "审核派单", "宿管管理员审核报修内容，并将工单分派给对应维修人员。"),
                createFlowItem("03", "处理反馈", "维修人员更新处理状态和结果，学生在完成后查看记录并补充评价。")
        );
        flowBox.getStyleClass().add("login-flow-box");

        HBox stripRow = new HBox(
                14,
                createStripCard("3", "核心角色"),
                createStripCard("工单闭环", "处理主线"),
                createStripCard("维修协同", "当前场景")
        );
        stripRow.getStyleClass().add("login-strip-row");
        HBox.setHgrow(stripRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(stripRow.getChildren().get(1), Priority.ALWAYS);
        HBox.setHgrow(stripRow.getChildren().get(2), Priority.ALWAYS);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox heroPane = new VBox(20, chipLabel, titleLabel, copyLabel, flowBox, spacer, stripRow);
        heroPane.getStyleClass().add("login-hero-pane");
        heroPane.setPadding(new Insets(28, 30, 28, 30));
        heroPane.setMaxWidth(Double.MAX_VALUE);
        return heroPane;
    }

    private VBox buildEntryPane() {
        ObjectProperty<UserRole> selectedRole = new SimpleObjectProperty<>(UserRole.STUDENT);
        ObjectProperty<DemoAccount> selectedAccount =
                new SimpleObjectProperty<>(DemoAccountDirectory.defaultAccount(UserRole.STUDENT));
        Map<UserRole, StackPane> roleCards = new EnumMap<>(UserRole.class);
        Map<DemoAccount, StackPane> accountCards = new HashMap<>();

        Label chipLabel = new Label("身份登录");
        chipLabel.getStyleClass().add("login-entry-chip");

        Label titleLabel = new Label("进入系统");
        titleLabel.getStyleClass().add("login-entry-title");

        Label introLabel = new Label("先选择当前身份，再选择对应账号进入业务页面。");
        introLabel.getStyleClass().add("login-entry-intro");
        introLabel.setWrapText(true);
        introLabel.setMaxWidth(Double.MAX_VALUE);

        GridPane roleGrid = new GridPane();
        roleGrid.getStyleClass().add("login-role-grid");
        roleGrid.setHgap(12);
        roleGrid.setVgap(12);
        roleGrid.getColumnConstraints().addAll(thirdColumn(), thirdColumn(), thirdColumn());

        roleGrid.add(createRoleCard(UserRole.STUDENT, selectedRole, selectedAccount, roleCards), 0, 0);
        roleGrid.add(createRoleCard(UserRole.ADMIN, selectedRole, selectedAccount, roleCards), 1, 0);
        roleGrid.add(createRoleCard(UserRole.WORKER, selectedRole, selectedAccount, roleCards), 2, 0);

        Label rolePreviewLabel = new Label();
        rolePreviewLabel.getStyleClass().add("login-role-preview-title");

        Label roleHintLabel = new Label();
        roleHintLabel.getStyleClass().add("login-role-preview-copy");
        roleHintLabel.setWrapText(true);
        roleHintLabel.setMaxWidth(Double.MAX_VALUE);

        VBox previewBox = new VBox(8, rolePreviewLabel, roleHintLabel);
        previewBox.getStyleClass().add("login-role-preview");

        Label accountLabel = new Label("登录账号");
        accountLabel.getStyleClass().add("form-label");

        VBox accountListBox = new VBox(12);
        accountListBox.getStyleClass().add("login-account-list");
        accountListBox.setFillWidth(true);
        accountListBox.setMaxWidth(Double.MAX_VALUE);

        selectedRole.addListener((observable, oldRole, newRole) -> {
            updateRoleSelection(roleCards, newRole);
            selectedAccount.set(DemoAccountDirectory.defaultAccount(newRole));
            syncRolePreview(rolePreviewLabel, roleHintLabel, newRole);
            rebuildAccountCards(accountListBox, selectedAccount, accountCards, newRole);
        });
        selectedAccount.addListener((observable, oldAccount, newAccount) ->
                updateAccountSelection(accountCards, newAccount)
        );

        updateRoleSelection(roleCards, selectedRole.get());
        syncRolePreview(rolePreviewLabel, roleHintLabel, selectedRole.get());
        rebuildAccountCards(accountListBox, selectedAccount, accountCards, selectedRole.get());

        Label noteLabel = new Label("登录后将进入对应业务模块，支持随时退出并切换身份。");
        noteLabel.getStyleClass().add("login-entry-note");
        noteLabel.setWrapText(true);
        noteLabel.setMaxWidth(Double.MAX_VALUE);

        var loginButton = FusionUiFactory.createPrimaryButton("进入系统", 0, 46, () ->
                loginAction.accept(selectedAccount.get())
        );
        loginButton.getNode().setMaxWidth(Double.MAX_VALUE);

        VBox entryPane = new VBox(
                18,
                chipLabel,
                titleLabel,
                introLabel,
                roleGrid,
                previewBox,
                accountLabel,
                accountListBox,
                noteLabel,
                loginButton.getNode()
        );
        entryPane.getStyleClass().add("login-entry-card");
        entryPane.setPadding(new Insets(28, 26, 26, 26));
        entryPane.setFillWidth(true);
        return entryPane;
    }

    private Node createFlowItem(String index, String title, String text) {
        Label indexLabel = new Label(index);
        indexLabel.getStyleClass().add("login-flow-index");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("login-flow-title");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("login-flow-copy");
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(Double.MAX_VALUE);

        VBox textBox = new VBox(3, titleLabel, textLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        HBox row = new HBox(12, indexLabel, textBox);
        row.getStyleClass().add("login-flow-item");
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private Node createStripCard(String value, String caption) {
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("login-strip-value");

        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("login-strip-caption");

        VBox box = new VBox(4, valueLabel, captionLabel);
        box.getStyleClass().add("login-strip-card");
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private Node createRoleCard(
            UserRole role,
            ObjectProperty<UserRole> selectedRole,
            ObjectProperty<DemoAccount> selectedAccount,
            Map<UserRole, StackPane> roleCards
    ) {
        Label titleLabel = new Label(role.displayName());
        titleLabel.getStyleClass().add("login-role-name");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        Label subtitleLabel = new Label(shortRoleHint(role));
        subtitleLabel.getStyleClass().add("login-role-copy");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(Double.MAX_VALUE);
        subtitleLabel.setAlignment(Pos.CENTER);
        subtitleLabel.setTextAlignment(TextAlignment.CENTER);

        VBox body = new VBox(8, titleLabel, subtitleLabel);
        body.getStyleClass().add("login-role-card-body");
        body.setFillWidth(true);
        body.setAlignment(Pos.CENTER);
        body.setPadding(new Insets(16, 18, 18, 18));

        var card = FusionUiFactory.createActionCard(
                body,
                0,
                120,
                () -> {
                    selectedRole.set(role);
                    selectedAccount.set(DemoAccountDirectory.defaultAccount(role));
                },
                "login-role-card"
        );
        card.getNode().setMaxWidth(Double.MAX_VALUE);
        card.getNode().setAlignment(Pos.CENTER);
        roleCards.put(role, card.getNode());
        return card.getNode();
    }

    private void rebuildAccountCards(
            VBox accountListBox,
            ObjectProperty<DemoAccount> selectedAccount,
            Map<DemoAccount, StackPane> accountCards,
            UserRole role
    ) {
        accountListBox.getChildren().clear();
        accountCards.clear();

        List<DemoAccount> accounts = DemoAccountDirectory.accountOptions(role);
        for (DemoAccount account : accounts) {
            accountListBox.getChildren().add(createAccountCard(account, selectedAccount, accountCards));
        }
        updateAccountSelection(accountCards, selectedAccount.get());
    }

    private Node createAccountCard(
            DemoAccount account,
            ObjectProperty<DemoAccount> selectedAccount,
            Map<DemoAccount, StackPane> accountCards
    ) {
        Label titleLabel = new Label(account.displayName());
        titleLabel.getStyleClass().add("login-account-name");

        Label subtitleLabel = new Label(accountSubtitle(account));
        subtitleLabel.getStyleClass().add("login-account-copy");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(Double.MAX_VALUE);

        VBox textBox = new VBox(4, titleLabel, subtitleLabel);
        textBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label tagLabel = new Label(account.role().displayName());
        tagLabel.getStyleClass().add("login-account-tag");

        HBox body = new HBox(14, textBox, tagLabel);
        body.getStyleClass().add("login-account-body");
        body.setAlignment(Pos.CENTER_LEFT);
        body.setPadding(new Insets(16, 18, 16, 18));

        var card = FusionUiFactory.createActionCard(
                body,
                0,
                84,
                () -> selectedAccount.set(account),
                "login-account-card"
        );
        card.getNode().setMaxWidth(Double.MAX_VALUE);
        accountCards.put(account, card.getNode());
        return card.getNode();
    }

    private void syncRolePreview(Label rolePreviewLabel, Label roleHintLabel, UserRole role) {
        rolePreviewLabel.setText(role.displayName() + "业务入口");
        roleHintLabel.setText(ROLE_HINTS.get(role));
    }

    private void updateRoleSelection(Map<UserRole, StackPane> roleCards, UserRole selectedRole) {
        roleCards.forEach((role, card) -> {
            card.getStyleClass().remove("login-role-card-active");
            if (role == selectedRole) {
                card.getStyleClass().add("login-role-card-active");
            }
        });
    }

    private void updateAccountSelection(Map<DemoAccount, StackPane> accountCards, DemoAccount selectedAccount) {
        accountCards.forEach((account, card) -> {
            card.getStyleClass().remove("login-account-card-active");
            if (account.equals(selectedAccount)) {
                card.getStyleClass().add("login-account-card-active");
            }
        });
    }

    private ColumnConstraints thirdColumn() {
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(33.3333);
        constraints.setHgrow(Priority.ALWAYS);
        constraints.setFillWidth(true);
        return constraints;
    }

    private String shortRoleHint(UserRole role) {
        return switch (role) {
            case STUDENT -> "报修与进度";
            case ADMIN -> "审核与派单";
            case WORKER -> "处理与回填";
        };
    }

    private String accountSubtitle(DemoAccount account) {
        return switch (account.role()) {
            case STUDENT -> "查看自己的报修申请、处理状态和评价结果。";
            case ADMIN -> "负责审核、分类、派单和催办。";
            case WORKER -> "负责接单、维修和结果回填。";
        };
    }
}
