package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.DemoAccountDirectory;
import com.scau.dormrepair.common.DemoAccountDirectory.DemoAccount;
import com.scau.dormrepair.domain.command.AssignWorkOrderCommand;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.enums.WorkOrderPriority;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.support.UiDisplayText;
import com.scau.dormrepair.ui.support.UiMotion;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Admin dispatch module. */
public class AdminDispatchModule extends AbstractWorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AdminDispatchModule(AppContext appContext) {
        super(appContext);
    }

    @Override
    public String moduleCode() {
        return "admin-dispatch";
    }

    @Override
    public String moduleName() {
        return "\u7ba1\u7406\u5458\u6d3e\u5355";
    }

    @Override
    public String moduleDescription() {
        return "\u5ba1\u6838\u5b66\u751f\u62a5\u4fee\uff0c\u5e76\u628a\u5408\u9002\u7684\u5de5\u5355\u6d3e\u7ed9\u6307\u5b9a\u7ef4\u4fee\u5458\u3002";
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
        DemoAccount currentAdmin = DemoAccountDirectory.resolveCurrent(appContext.appSession());
        if (currentAdmin == null) {
            throw new IllegalStateException("管理员身份未初始化，无法进入派单模块");
        }

        TableView<RecentRepairRequestView> pendingTable = buildPendingTable();
        refreshPendingRequests(pendingTable);

        GridPane contentGrid = new GridPane();
        contentGrid.setHgap(18);
        contentGrid.setMinWidth(0);
        contentGrid.setMaxWidth(Double.MAX_VALUE);
        contentGrid.getColumnConstraints().addAll(percentColumn(44), percentColumn(56));
        contentGrid.add(buildDispatchForm(currentAdmin, pendingTable), 0, 0);
        contentGrid.add(wrapPanel("\u5f85\u6d3e\u5355\u5217\u8868", pendingTable), 1, 0);

        return createPage(
                "管理员派单工作区",
                "\u5148\u4ece\u53f3\u4fa7\u6311\u4e00\u6761\u5f85\u6d3e\u5355\u62a5\u4fee\uff0c\u518d\u9009\u7ef4\u4fee\u5458\u548c\u4f18\u5148\u7ea7\uff0c\u628a\u62a5\u4fee\u6b63\u5f0f\u6d41\u8f6c\u6210\u5de5\u5355\u3002",
                contentGrid
        );
    }

    private Node buildDispatchForm(DemoAccount currentAdmin, TableView<RecentRepairRequestView> pendingTable) {
        Label selectedRequestNoLabel = new Label("\u672a\u9009\u62e9\u62a5\u4fee\u5355");
        selectedRequestNoLabel.getStyleClass().add("dashboard-spotlight-value");

        Label selectedRequestInfoLabel = new Label("\u8bf7\u5148\u5728\u53f3\u4fa7\u8868\u683c\u91cc\u9009\u62e9\u4e00\u6761\u5f85\u6d3e\u5355\u62a5\u4fee\u3002");
        selectedRequestInfoLabel.getStyleClass().add("dashboard-mini-description");
        selectedRequestInfoLabel.setWrapText(true);

        ComboBox<DemoAccount> workerBox = new ComboBox<>();
        workerBox.getItems().addAll(DemoAccountDirectory.workerOptions());
        workerBox.setMaxWidth(Double.MAX_VALUE);
        UiMotion.installSmoothDropdown(workerBox);

        ComboBox<WorkOrderPriority> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll(WorkOrderPriority.values());
        priorityBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(WorkOrderPriority workOrderPriority) {
                return UiDisplayText.workOrderPriority(workOrderPriority);
            }

            @Override
            public WorkOrderPriority fromString(String string) {
                return null;
            }
        });
        priorityBox.setMaxWidth(Double.MAX_VALUE);
        priorityBox.setValue(WorkOrderPriority.NORMAL);
        UiMotion.installSmoothDropdown(priorityBox);

        TextArea assignmentNoteArea = new TextArea();
        assignmentNoteArea.setPromptText("\u5199\u6e05\u695a\u6d3e\u5355\u8bf4\u660e\u3001\u6ce8\u610f\u4e8b\u9879\u548c\u662f\u5426\u9700\u8981\u4f18\u5148\u5904\u7406\u3002");
        assignmentNoteArea.setPrefRowCount(5);
        assignmentNoteArea.setWrapText(true);

        pendingTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                selectedRequestNoLabel.setText("\u672a\u9009\u62e9\u62a5\u4fee\u5355");
                selectedRequestInfoLabel.setText("\u8bf7\u5148\u5728\u53f3\u4fa7\u8868\u683c\u91cc\u9009\u62e9\u4e00\u6761\u5f85\u6d3e\u5355\u62a5\u4fee\u3002");
                return;
            }

            selectedRequestNoLabel.setText(newValue.getRequestNo());
            selectedRequestInfoLabel.setText(
                    newValue.getStudentName()
                            + " / "
                            + newValue.getLocationText()
                            + " / "
                            + UiDisplayText.faultCategory(newValue.getFaultCategory())
            );
        });

        Button refreshButton = new Button("\u5237\u65b0\u5f85\u6d3e\u5355");
        refreshButton.getStyleClass().add("nav-button");
        refreshButton.setOnAction(event -> refreshPendingRequests(pendingTable));

        Node assignButton = FusionUiFactory.createPrimaryButton("\u751f\u6210\u5de5\u5355\u5e76\u6d3e\u5355", 190, 40, () -> {
            try {
                RecentRepairRequestView selectedRequest = pendingTable.getSelectionModel().getSelectedItem();
                if (selectedRequest == null) {
                    throw new IllegalArgumentException("请先选择一条待派单报修");
                }
                if (workerBox.getValue() == null) {
                    throw new IllegalArgumentException("\u8bf7\u9009\u62e9\u7ef4\u4fee\u5458");
                }
                if (priorityBox.getValue() == null) {
                    throw new IllegalArgumentException("\u8bf7\u9009\u62e9\u4f18\u5148\u7ea7");
                }

                AssignWorkOrderCommand command = new AssignWorkOrderCommand(
                        selectedRequest.getId(),
                        currentAdmin.id(),
                        workerBox.getValue().id(),
                        priorityBox.getValue(),
                        assignmentNoteArea.getText()
                );

                Long workOrderId = appContext.workOrderService().assign(command);
                refreshPendingRequests(pendingTable);
                pendingTable.getSelectionModel().clearSelection();
                assignmentNoteArea.clear();
                UiAlerts.info("派单成功", "工单已创建，工单ID=" + workOrderId);
            } catch (RuntimeException exception) {
                UiAlerts.error("派单失败", exception.getMessage());
            }
        }).getNode();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionRow = new HBox(12, refreshButton, spacer, assignButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        VBox summaryBox = new VBox(8, selectedRequestNoLabel, selectedRequestInfoLabel);
        summaryBox.getStyleClass().add("dashboard-spotlight-body");

        VBox formBox = new VBox(
                14,
                wrapInlinePanel("\u5f53\u524d\u9009\u62e9", summaryBox),
                createFieldBlock("\u7ef4\u4fee\u5458", workerBox),
                createFieldBlock("\u4f18\u5148\u7ea7", priorityBox),
                createFieldBlock("派单说明", assignmentNoteArea),
                actionRow
        );

        return wrapPanel("填写派单信息", formBox);
    }

    private Node wrapInlinePanel(String title, Node content) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dashboard-mini-tag");

        VBox box = new VBox(10, titleLabel, content);
        return FusionUiFactory.createCard(box, 0, 0, "dashboard-mini-card", "dashboard-mini-soft").getNode();
    }

    private VBox createFieldBlock(String labelText, Region input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        input.setMaxWidth(Double.MAX_VALUE);

        VBox block = new VBox(8, label, input);
        block.setMaxWidth(Double.MAX_VALUE);
        return block;
    }

    private TableView<RecentRepairRequestView> buildPendingTable() {
        TableView<RecentRepairRequestView> pendingTable = new TableView<>();
        pendingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        pendingTable.setPrefHeight(460);
        pendingTable.getColumns().addAll(
                createTextColumn("报修单号", "requestNo"),
                createTextColumn("学生", "studentName"),
                createTextColumn("宿舍", "locationText"),
                createFaultColumn(),
                createStatusColumn(),
                createDateTimeColumn("提交时间")
        );
        return pendingTable;
    }

    private void refreshPendingRequests(TableView<RecentRepairRequestView> pendingTable) {
        pendingTable.setItems(FXCollections.observableArrayList(
                appContext.repairRequestService().listPendingAssignmentRequests(12)
        ));
    }

    private TableColumn<RecentRepairRequestView, String> createTextColumn(String title, String property) {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    private TableColumn<RecentRepairRequestView, String> createFaultColumn() {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>("故障类型");
        column.setCellValueFactory(cell ->
                Bindings.createStringBinding(() -> UiDisplayText.faultCategory(cell.getValue().getFaultCategory()))
        );
        return column;
    }

    private TableColumn<RecentRepairRequestView, String> createStatusColumn() {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>("\u72b6\u6001");
        column.setCellValueFactory(cell ->
                Bindings.createStringBinding(() -> UiDisplayText.repairRequestStatus(cell.getValue().getStatus()))
        );
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
        return column;
    }

    private ColumnConstraints percentColumn(double percentWidth) {
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(percentWidth);
        constraints.setFillWidth(true);
        constraints.setHgrow(Priority.ALWAYS);
        return constraints;
    }
}
