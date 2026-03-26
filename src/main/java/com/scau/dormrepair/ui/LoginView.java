package com.scau.dormrepair.ui;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/**
 * 登录页只负责选择当前登录角色并进入工作台。
 * 当前阶段先走本地登录占位，界面口径按正式系统展示，不再出现演示措辞。
 */
public class LoginView {

    private static final Map<UserRole, String> ROLE_HINTS = Map.of(
            UserRole.STUDENT, "提交报修、查看进度、完成评价",
            UserRole.ADMIN, "审核分类、派单催办、查看统计",
            UserRole.WORKER, "接单处理、更新状态、回填结果"
    );

    private static final Map<UserRole, String> ROLE_LABELS = Map.of(
            UserRole.STUDENT, "学生入口",
            UserRole.ADMIN, "宿管管理员入口",
            UserRole.WORKER, "维修人员入口"
    );

    private final AppContext appContext;
    private final BiConsumer<String, UserRole> loginAction;

    public LoginView(AppContext appContext, BiConsumer<String, UserRole> loginAction) {
        this.appContext = appContext;
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
        entryPane.setMinWidth(460);
        entryPane.setPrefWidth(460);
        entryPane.setMaxWidth(460);

        shell.getChildren().addAll(heroPane, entryPane);
        return shell;
    }

    private VBox buildHeroPane() {
        Label chipLabel = new Label("SCAU DORM REPAIR");
        chipLabel.getStyleClass().add("login-hero-chip");

        Label titleLabel = new Label("宿舍报修与工单管理系统");
        titleLabel.getStyleClass().add("login-hero-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label copyLabel = new Label("围绕学生报修、管理员派单、维修处理和结果反馈，统一承接宿舍维修业务的完整闭环。");
        copyLabel.getStyleClass().add("login-hero-copy");
        copyLabel.setWrapText(true);
        copyLabel.setMaxWidth(Double.MAX_VALUE);

        VBox flowBox = new VBox(
                12,
                createFlowItem("01", "提交报修", "学生填写宿舍区、楼栋、房间、故障类型和故障描述。"),
                createFlowItem("02", "审核派单", "管理员审核报修内容，分类后分派给对应维修人员。"),
                createFlowItem("03", "处理反馈", "维修人员更新状态并回填处理结果，形成完整记录。")
        );
        flowBox.getStyleClass().add("login-flow-box");

        HBox stripRow = new HBox(
                14,
                createStripCard("3", "核心角色"),
                createStripCard("工单闭环", "核心流程"),
                createStripCard("MyBatis", "数据访问")
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
        Map<UserRole, StackPane> roleCards = new EnumMap<>(UserRole.class);

        Label chipLabel = new Label("身份登录");
        chipLabel.getStyleClass().add("login-entry-chip");

        Label titleLabel = new Label("进入工作台");
        titleLabel.getStyleClass().add("login-entry-title");

        Label introLabel = new Label("请选择当前登录身份并填写显示名称，进入对应业务工作台。");
        introLabel.getStyleClass().add("login-entry-intro");
        introLabel.setWrapText(true);
        introLabel.setMaxWidth(Double.MAX_VALUE);

        TextField nameField = new TextField(defaultName(UserRole.STUDENT));
        nameField.setPromptText("输入当前登录姓名");

        Label rolePreviewLabel = new Label();
        rolePreviewLabel.getStyleClass().add("login-role-preview-title");

        Label roleHintLabel = new Label();
        roleHintLabel.getStyleClass().add("login-role-preview-copy");
        roleHintLabel.setWrapText(true);
        roleHintLabel.setMaxWidth(Double.MAX_VALUE);

        GridPane roleGrid = new GridPane();
        roleGrid.getStyleClass().add("login-role-grid");
        roleGrid.setHgap(12);
        roleGrid.setVgap(12);
        roleGrid.getColumnConstraints().addAll(thirdColumn(), thirdColumn(), thirdColumn());

        roleGrid.add(createRoleCard(UserRole.STUDENT, selectedRole, nameField, roleCards), 0, 0);
        roleGrid.add(createRoleCard(UserRole.ADMIN, selectedRole, nameField, roleCards), 1, 0);
        roleGrid.add(createRoleCard(UserRole.WORKER, selectedRole, nameField, roleCards), 2, 0);

        VBox previewBox = new VBox(8, rolePreviewLabel, roleHintLabel);
        previewBox.getStyleClass().add("login-role-preview");

        selectedRole.addListener((observable, oldRole, newRole) -> {
            updateRoleSelection(roleCards, newRole);
            rolePreviewLabel.setText(ROLE_LABELS.get(newRole));
            roleHintLabel.setText(ROLE_HINTS.get(newRole));
        });
        updateRoleSelection(roleCards, selectedRole.get());
        rolePreviewLabel.setText(ROLE_LABELS.get(selectedRole.get()));
        roleHintLabel.setText(ROLE_HINTS.get(selectedRole.get()));

        Label fieldLabel = new Label("显示名称");
        fieldLabel.getStyleClass().add("form-label");

        Label noteLabel = new Label("当前先使用本地登录占位，后续接入真实用户表后可直接替换登录校验逻辑。");
        noteLabel.getStyleClass().add("login-entry-note");
        noteLabel.setWrapText(true);
        noteLabel.setMaxWidth(Double.MAX_VALUE);

        var loginButton = FusionUiFactory.createPrimaryButton("进入桌面工作台", 0, 46, () ->
                loginAction.accept(nameField.getText(), selectedRole.get())
        );
        loginButton.getNode().setMaxWidth(Double.MAX_VALUE);

        VBox entryPane = new VBox(
                18,
                chipLabel,
                titleLabel,
                introLabel,
                roleGrid,
                previewBox,
                fieldLabel,
                nameField,
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
            TextField nameField,
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
                    nameField.setText(defaultName(role));
                },
                "login-role-card"
        );
        card.getNode().setMaxWidth(Double.MAX_VALUE);
        card.getNode().setAlignment(Pos.CENTER);
        roleCards.put(role, card.getNode());
        return card.getNode();
    }

    private void updateRoleSelection(Map<UserRole, StackPane> roleCards, UserRole selectedRole) {
        roleCards.forEach((role, card) -> {
            card.getStyleClass().remove("login-role-card-active");
            if (role == selectedRole) {
                card.getStyleClass().add("login-role-card-active");
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

    private String defaultName(UserRole role) {
        return switch (role) {
            case STUDENT -> "张三";
            case ADMIN -> "李老师";
            case WORKER -> "王师傅";
        };
    }

    private String shortRoleHint(UserRole role) {
        return switch (role) {
            case STUDENT -> "报修与进度";
            case ADMIN -> "审核与派单";
            case WORKER -> "处理与回填";
        };
    }
}
