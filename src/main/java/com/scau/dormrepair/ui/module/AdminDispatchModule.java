package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.DemoAccountDirectory;
import com.scau.dormrepair.common.DemoAccountDirectory.DemoAccount;
import com.scau.dormrepair.domain.command.AssignWorkOrderCommand;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.enums.WorkOrderPriority;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.ui.component.AppDropdown;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.support.UiDisplayText;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * 管理员派单模块。
 */
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
        return "管理员派单";
    }

    @Override
    public String moduleDescription() {
        return "审核学生报修，并把合适的工单派给指定维修员。";
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
            throw new IllegalStateException("管理员身份未初始化，无法进入派单模块。");
        }

        TableView<RecentRepairRequestView> pendingTable = buildPendingTable();
        refreshPendingRequests(pendingTable);

        GridPane contentGrid = createRatioWorkspace(
                44,
                56,
                buildDispatchForm(currentAdmin, pendingTable),
                wrapPanel("待派单列表", pendingTable)
        );

        return createPage(
                "管理员派单工作区",
                "先从右侧挑一条待派单报修，再选维修员和优先级，把报修正式流转成工单。",
                contentGrid
        );
    }

    private Node buildDispatchForm(DemoAccount currentAdmin, TableView<RecentRepairRequestView> pendingTable) {
        Label selectedRequestNoLabel = new Label("未选择报修单");
        selectedRequestNoLabel.getStyleClass().add("dashboard-spotlight-value");

        Label selectedRequestInfoLabel = new Label("请先在右侧表格里选择一条待派单报修。");
        selectedRequestInfoLabel.getStyleClass().add("dashboard-mini-description");
        selectedRequestInfoLabel.setWrapText(true);

        AppDropdown<DemoAccount> workerBox = new AppDropdown<>();
        workerBox.setItems(DemoAccountDirectory.workerOptions());
        workerBox.setTextMapper(DemoAccount::displayName);
        workerBox.setPromptText("选择维修员");
        workerBox.setVisibleRowCount(6);

        AppDropdown<WorkOrderPriority> priorityBox = new AppDropdown<>();
        priorityBox.setItems(List.of(WorkOrderPriority.values()));
        priorityBox.setTextMapper(UiDisplayText::workOrderPriority);
        priorityBox.setPromptText("选择优先级");
        priorityBox.setVisibleRowCount(5);
        priorityBox.setValue(WorkOrderPriority.NORMAL);

        TextArea assignmentNoteArea = new TextArea();
        assignmentNoteArea.setPromptText("写清楚派单说明、注意事项和是否需要优先处理。");
        assignmentNoteArea.setPrefRowCount(5);
        assignmentNoteArea.setWrapText(true);

        pendingTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                selectedRequestNoLabel.setText("未选择报修单");
                selectedRequestInfoLabel.setText("请先在右侧表格里选择一条待派单报修。");
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

        Button refreshButton = new Button("刷新待派单");
        refreshButton.getStyleClass().add("surface-button");
        refreshButton.setOnAction(event -> refreshPendingRequests(pendingTable));

        Node assignButton = FusionUiFactory.createPrimaryButton("生成工单并派单", 190, 40, () -> {
            try {
                RecentRepairRequestView selectedRequest = pendingTable.getSelectionModel().getSelectedItem();
                if (selectedRequest == null) {
                    throw new IllegalArgumentException("请先选择一条待派单报修");
                }
                if (workerBox.getValue() == null) {
                    throw new IllegalArgumentException("请选择维修员");
                }
                if (priorityBox.getValue() == null) {
                    throw new IllegalArgumentException("请选择优先级");
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
                UiAlerts.info("派单成功", "工单已创建，工单 ID=" + workOrderId);
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
                createInlineSummaryCard("当前选择", summaryBox, "dashboard-mini-card", "dashboard-mini-soft"),
                createFieldBlock("维修员", workerBox),
                createFieldBlock("优先级", priorityBox),
                createFieldBlock("派单说明", assignmentNoteArea),
                actionRow
        );

        return wrapPanel("填写派单信息", formBox);
    }

    private TableView<RecentRepairRequestView> buildPendingTable() {
        TableView<RecentRepairRequestView> pendingTable = new TableView<>();
        pendingTable.getColumns().addAll(
                createTextColumn("报修单号", "requestNo"),
                createTextColumn("学生", "studentName"),
                createTextColumn("宿舍", "locationText"),
                createFaultColumn(),
                createStatusColumn(),
                createDateTimeColumn("提交时间")
        );
        configureFixedTable(pendingTable, 460, 1.618, 1.0, 1.382, 1.236, 0.764, 1.382);
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
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>("状态");
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
}
