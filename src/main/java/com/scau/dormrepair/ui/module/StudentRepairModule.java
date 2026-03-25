package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.DemoAccountDirectory;
import com.scau.dormrepair.common.DemoAccountDirectory.DemoAccount;
import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.support.UiDisplayText;
import com.scau.dormrepair.ui.support.UiMotion;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Student repair module.
 */
public class StudentRepairModule extends AbstractWorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StudentRepairModule(AppContext appContext) {
        super(appContext);
    }

    @Override
    public String moduleCode() {
        return "student-repair";
    }

    @Override
    public String moduleName() {
        return "学生报修";
    }

    @Override
    public String moduleDescription() {
        return "\u63d0\u4ea4\u81ea\u5df1\u7684\u62a5\u4fee\u7533\u8bf7\uff0c\u5e76\u67e5\u770b\u6700\u8fd1\u51e0\u6761\u5904\u7406\u8bb0\u5f55\u3002";
    }

    @Override
    public Set<UserRole> supportedRoles() {
        return EnumSet.of(UserRole.STUDENT);
    }

    @Override
    public boolean cacheViewOnSwitch() {
        return true;
    }

    @Override
    public Parent createView() {
        DemoAccount currentStudent = DemoAccountDirectory.resolveCurrent(appContext.appSession());
        if (currentStudent == null) {
            throw new IllegalStateException("\u5b66\u751f\u8eab\u4efd\u672a\u521d\u59cb\u5316\uff0c\u65e0\u6cd5\u8fdb\u5165\u62a5\u4fee\u6a21\u5757\u3002");
        }

        TableView<RecentRepairRequestView> historyTable = buildHistoryTable();
        refreshStudentHistory(historyTable, currentStudent.id());

        Node repairFormPanel = buildRepairForm(currentStudent, historyTable);
        Node historyPanel = wrapPanel("\u6700\u8fd1\u62a5\u4fee\u8bb0\u5f55", historyTable);

        GridPane contentGrid = new GridPane();
        contentGrid.setHgap(18);
        contentGrid.setMaxWidth(Double.MAX_VALUE);
        contentGrid.getColumnConstraints().addAll(percentColumn(58), percentColumn(42));
        contentGrid.add(repairFormPanel, 0, 0);
        contentGrid.add(historyPanel, 1, 0);

        VBox content = new VBox(18, buildSummaryStrip(currentStudent), contentGrid);
        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);

        return createPage(
                "\u5b66\u751f\u62a5\u4fee\u5de5\u4f5c\u533a",
                "\u5148\u628a\u5bbf\u820d\u4f4d\u7f6e\u3001\u8054\u7cfb\u65b9\u5f0f\u548c\u6545\u969c\u63cf\u8ff0\u8865\u9f50\uff0c\u518d\u63d0\u4ea4\u5230\u6570\u636e\u5e93\u91cc\uff1b\u53f3\u4fa7\u5386\u53f2\u8868\u53ea\u663e\u793a\u5f53\u524d\u5b66\u751f\u81ea\u5df1\u7684\u8bb0\u5f55\u3002",
                content
        );
    }

    private Node buildSummaryStrip(DemoAccount currentStudent) {
        Label titleLabel = new Label("\u5f53\u524d\u62a5\u4fee\u4eba");
        titleLabel.getStyleClass().add("dashboard-mini-tag");

        Label nameLabel = new Label(currentStudent.displayName());
        nameLabel.getStyleClass().add("dashboard-mini-value");

        Label helperLabel = new Label("\u6f14\u793a\u0020\u0073\u0074\u0075\u0064\u0065\u006e\u0074\u0049\u0064\u003d" + currentStudent.id() + "\uff0c\u63d0\u4ea4\u65f6\u4f1a\u7528\u8fd9\u4e2a\u7a33\u5b9a\u0020\u0049\u0044\u0020\u7ed1\u5b9a\u62a5\u4fee\u5f52\u5c5e\u3002");
        helperLabel.getStyleClass().add("dashboard-mini-description");
        helperLabel.setWrapText(true);
        helperLabel.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(8, titleLabel, nameLabel, helperLabel);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setFillWidth(true);

        return FusionUiFactory.createCard(
                content,
                0,
                104,
                "dashboard-mini-card",
                "dashboard-metric-card",
                "dashboard-metric-card-highlight"
        ).getNode();
    }

    private Node buildRepairForm(DemoAccount currentStudent, TableView<RecentRepairRequestView> historyTable) {
        TextField buildingField = new TextField();
        buildingField.setPromptText("\u4f8b\u5982\u0020\u0031\u0033\u680b");

        TextField roomField = new TextField();
        roomField.setPromptText("例如 402");

        TextField phoneField = new TextField();
        phoneField.setPromptText("联系电话");

        ComboBox<FaultCategory> faultCategoryBox = new ComboBox<>();
        faultCategoryBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(FaultCategory value) {
                return UiDisplayText.faultCategory(value);
            }

            @Override
            public FaultCategory fromString(String string) {
                return null;
            }
        });
        faultCategoryBox.getItems().addAll(FaultCategory.values());
        faultCategoryBox.setPromptText("选择故障类型");
        faultCategoryBox.setVisibleRowCount(6);
        faultCategoryBox.setMinHeight(44);
        faultCategoryBox.setPrefHeight(44);
        faultCategoryBox.setMaxHeight(44);
        faultCategoryBox.setMaxWidth(Double.MAX_VALUE);
        faultCategoryBox.setButtonCell(createFaultCategoryCell());
        faultCategoryBox.setCellFactory(ignored -> createFaultCategoryCell());
        UiMotion.installSmoothDropdown(faultCategoryBox);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("\u8bf7\u5177\u4f53\u8bf4\u660e\u6545\u969c\u73b0\u8c61\u3001\u662f\u5426\u7d27\u6025\u3001\u662f\u5426\u5f71\u54cd\u6b63\u5e38\u751f\u6d3b\u3002");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);

        TextArea imageUrlsArea = new TextArea();
        imageUrlsArea.setPromptText("\u6682\u65f6\u7528\u56fe\u7247\u5730\u5740\u4ee3\u66ff\u4e0a\u4f20\u529f\u80fd\uff0c\u4e00\u884c\u4e00\u5f20\u3002");
        imageUrlsArea.setPrefRowCount(3);
        imageUrlsArea.setWrapText(true);

        Button resetButton = new Button("清空表单");
        resetButton.getStyleClass().add("nav-button");

        Node submitButton = FusionUiFactory.createPrimaryButton("提交报修申请", 180, 40, () -> {
            try {
                CreateRepairRequestCommand command = new CreateRepairRequestCommand(
                        currentStudent.id(),
                        appContext.appSession().getDisplayName(),
                        phoneField.getText(),
                        null,
                        buildingField.getText(),
                        roomField.getText(),
                        faultCategoryBox.getValue(),
                        descriptionArea.getText(),
                        splitImageUrls(imageUrlsArea.getText())
                );

                Long repairRequestId = appContext.repairRequestService().create(command);
                refreshStudentHistory(historyTable, currentStudent.id());
                clearAfterSubmit(descriptionArea, imageUrlsArea, faultCategoryBox);
                UiAlerts.info("提交成功", "报修申请已保存，记录 ID=" + repairRequestId);
            } catch (RuntimeException exception) {
                UiAlerts.error("提交失败", exception.getMessage());
            }
        }).getNode();

        resetButton.setOnAction(event -> {
            buildingField.clear();
            roomField.clear();
            phoneField.clear();
            descriptionArea.clear();
            imageUrlsArea.clear();
            faultCategoryBox.getSelectionModel().clearSelection();
        });

        GridPane roomRow = new GridPane();
        roomRow.setHgap(12);
        roomRow.setMinWidth(0);
        roomRow.getColumnConstraints().addAll(percentColumn(50), percentColumn(50));
        roomRow.add(createFieldBlock("宿舍楼栋", buildingField), 0, 0);
        roomRow.add(createFieldBlock("\u623f\u95f4\u53f7", roomField), 1, 0);

        GridPane contactRow = new GridPane();
        contactRow.setHgap(12);
        contactRow.setMinWidth(0);
        contactRow.getColumnConstraints().addAll(percentColumn(32), percentColumn(68));
        contactRow.add(createFieldBlock("联系电话", phoneField), 0, 0);
        contactRow.add(createFieldBlock("故障类型", faultCategoryBox), 1, 0);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionRow = new HBox(12, resetButton, spacer, submitButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setMinWidth(0);

        VBox formBox = new VBox(
                12,
                roomRow,
                contactRow,
                createFieldBlock("故障描述", descriptionArea),
                createFieldBlock("图片地址", imageUrlsArea),
                actionRow
        );
        formBox.setFillWidth(true);
        formBox.setMaxWidth(Double.MAX_VALUE);

        return wrapPanel("填写报修表单", formBox);
    }

    private VBox createFieldBlock(String labelText, Region input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        input.setMaxWidth(Double.MAX_VALUE);
        input.setMinWidth(0);

        VBox block = new VBox(8, label, input);
        block.setMaxWidth(Double.MAX_VALUE);
        block.setMinWidth(0);
        HBox.setHgrow(block, Priority.ALWAYS);
        return block;
    }

    private TableView<RecentRepairRequestView> buildHistoryTable() {
        TableView<RecentRepairRequestView> historyTable = new TableView<>();
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setMinWidth(0);
        historyTable.setMaxWidth(Double.MAX_VALUE);
        historyTable.setPrefHeight(360);
        historyTable.setPlaceholder(new Label("暂无记录"));
        historyTable.getColumns().addAll(
                createTextColumn("报修单号", "requestNo", 98),
                createTextColumn("宿舍", "locationText", 88),
                createFaultColumn(),
                createStatusColumn(),
                createDateTimeColumn("提交时间")
        );
        return historyTable;
    }

    private void refreshStudentHistory(TableView<RecentRepairRequestView> historyTable, Long studentId) {
        historyTable.setItems(FXCollections.observableArrayList(
                appContext.repairRequestService().listStudentSubmittedRequests(studentId, 10)
        ));
    }

    private List<String> splitImageUrls(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return List.of();
        }
        return rawText.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    private void clearAfterSubmit(
            TextArea descriptionArea,
            TextArea imageUrlsArea,
            ComboBox<FaultCategory> faultCategoryBox
    ) {
        descriptionArea.clear();
        imageUrlsArea.clear();
        faultCategoryBox.getSelectionModel().clearSelection();
    }

    private TableColumn<RecentRepairRequestView, String> createTextColumn(String title, String property, double minWidth) {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setMinWidth(minWidth);
        return column;
    }

    private TableColumn<RecentRepairRequestView, String> createFaultColumn() {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>("故障类型");
        column.setCellValueFactory(cell ->
                Bindings.createStringBinding(() -> UiDisplayText.faultCategory(cell.getValue().getFaultCategory()))
        );
        column.setMinWidth(96);
        return column;
    }

    private TableColumn<RecentRepairRequestView, String> createStatusColumn() {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>("\u72b6\u6001");
        column.setCellValueFactory(cell ->
                Bindings.createStringBinding(() -> UiDisplayText.repairRequestStatus(cell.getValue().getStatus()))
        );
        column.setMinWidth(76);
        return column;
    }

    private TableColumn<RecentRepairRequestView, String> createDateTimeColumn(String title) {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell ->
                Bindings.createStringBinding(() -> {
                    if (cell.getValue().getSubmittedAt() == null) {
                        return "";
                    }
                    return TIME_FORMATTER.format(cell.getValue().getSubmittedAt());
                })
        );
        column.setMinWidth(136);
        return column;
    }

    private ColumnConstraints percentColumn(double percentWidth) {
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(percentWidth);
        column.setHgrow(Priority.ALWAYS);
        column.setFillWidth(true);
        column.setMinWidth(0);
        return column;
    }

    private ListCell<FaultCategory> createFaultCategoryCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(FaultCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : UiDisplayText.faultCategory(item));
            }
        };
    }
}
