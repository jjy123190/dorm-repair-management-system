package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.DemoAccountDirectory;
import com.scau.dormrepair.common.DemoAccountDirectory.DemoAccount;
import com.scau.dormrepair.domain.command.UpdateWorkOrderStatusCommand;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import com.scau.dormrepair.domain.view.ActiveWorkOrderView;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * 维修员处理模块。
 */
public class WorkerProcessingModule extends AbstractWorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final List<WorkOrderStatus> EDITABLE_STATUSES = List.of(
            WorkOrderStatus.ACCEPTED,
            WorkOrderStatus.IN_PROGRESS,
            WorkOrderStatus.WAITING_PARTS,
            WorkOrderStatus.COMPLETED,
            WorkOrderStatus.REJECTED,
            WorkOrderStatus.CANCELLED
    );

    public WorkerProcessingModule(AppContext appContext) {
        super(appContext);
    }

    @Override
    public String moduleCode() {
        return "worker-processing";
    }

    @Override
    public String moduleName() {
        return "维修处理";
    }

    @Override
    public String moduleDescription() {
        return "查看分配给自己的在途工单，并持续更新处理状态。";
    }

    @Override
    public Set<UserRole> supportedRoles() {
        return EnumSet.of(UserRole.WORKER);
    }

    @Override
    public boolean cacheViewOnSwitch() {
        return true;
    }

    @Override
    public Parent createView() {
        DemoAccount currentWorker = DemoAccountDirectory.resolveCurrent(appContext.appSession());
        if (currentWorker == null) {
            throw new IllegalStateException("维修员身份未初始化，无法进入维修处理模块。");
        }

        TableView<ActiveWorkOrderView> workOrderTable = buildWorkOrderTable();
        refreshWorkOrders(workOrderTable, currentWorker.id());

        GridPane contentGrid = createRatioWorkspace(
                42,
                58,
                buildStatusForm(currentWorker, workOrderTable),
                wrapPanel("我的在途工单", workOrderTable)
        );

        return createPage(
                "维修员处理工作区",
                "右侧只显示当前维修员自己的活动工单；左侧更新状态后，表格会立刻刷新，已完成工单会自动退出活动列表。",
                contentGrid
        );
    }

    private Node buildStatusForm(DemoAccount currentWorker, TableView<ActiveWorkOrderView> workOrderTable) {
        Label selectedWorkOrderLabel = new Label("未选择工单");
        selectedWorkOrderLabel.getStyleClass().add("dashboard-spotlight-value");

        Label selectedOrderInfoLabel = new Label("请先从右侧工单表中选择一条记录。");
        selectedOrderInfoLabel.getStyleClass().add("dashboard-mini-description");
        selectedOrderInfoLabel.setWrapText(true);

        AppDropdown<WorkOrderStatus> statusBox = new AppDropdown<>();
        statusBox.setItems(EDITABLE_STATUSES);
        statusBox.setTextMapper(UiDisplayText::workOrderStatus);
        statusBox.setPromptText("选择处理状态");
        statusBox.setVisibleRowCount(6);

        TextArea recordNoteArea = new TextArea();
        recordNoteArea.setPromptText("例如：已上门检测、等待配件、已更换零件并恢复使用。");
        recordNoteArea.setPrefRowCount(5);
        recordNoteArea.setWrapText(true);

        workOrderTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                selectedWorkOrderLabel.setText("未选择工单");
                selectedOrderInfoLabel.setText("请先从右侧工单表中选择一条记录。");
                statusBox.clearSelection();
                return;
            }

            selectedWorkOrderLabel.setText(newValue.getWorkOrderNo());
            selectedOrderInfoLabel.setText(
                    newValue.getRequestNo()
                            + " / 当前状态："
                            + UiDisplayText.workOrderStatus(newValue.getStatus())
                            + " / 优先级："
                            + UiDisplayText.workOrderPriority(newValue.getPriority())
            );
            statusBox.setValue(suggestNextStatus(newValue.getStatus()));
        });

        Button refreshButton = new Button("刷新工单");
        refreshButton.getStyleClass().add("surface-button");
        refreshButton.setOnAction(event -> refreshWorkOrders(workOrderTable, currentWorker.id()));

        Node updateButton = FusionUiFactory.createPrimaryButton("回填处理状态", 170, 40, () -> {
            try {
                ActiveWorkOrderView selectedWorkOrder = workOrderTable.getSelectionModel().getSelectedItem();
                if (selectedWorkOrder == null) {
                    throw new IllegalArgumentException("请选择一条工单");
                }
                if (statusBox.getValue() == null) {
                    throw new IllegalArgumentException("请选择要更新成的状态");
                }

                UpdateWorkOrderStatusCommand command = new UpdateWorkOrderStatusCommand(
                        selectedWorkOrder.getId(),
                        currentWorker.id(),
                        statusBox.getValue(),
                        recordNoteArea.getText()
                );

                appContext.workOrderService().updateStatus(command);
                refreshWorkOrders(workOrderTable, currentWorker.id());
                workOrderTable.getSelectionModel().clearSelection();
                recordNoteArea.clear();
                UiAlerts.info("更新成功", "工单状态已更新为：" + UiDisplayText.workOrderStatus(command.status()));
            } catch (RuntimeException exception) {
                UiAlerts.error("更新失败", exception.getMessage());
            }
        }).getNode();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actionRow = new HBox(12, refreshButton, spacer, updateButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        VBox summaryBox = new VBox(8, selectedWorkOrderLabel, selectedOrderInfoLabel);
        summaryBox.getStyleClass().add("dashboard-spotlight-body");

        VBox formBox = new VBox(
                14,
                createInlineSummaryCard("当前工单", summaryBox, "dashboard-mini-card", "dashboard-mini-soft"),
                createFieldBlock("更新状态", statusBox),
                createFieldBlock("处理说明", recordNoteArea),
                actionRow
        );

        return wrapPanel("填写处理结果", formBox);
    }

    private TableView<ActiveWorkOrderView> buildWorkOrderTable() {
        TableView<ActiveWorkOrderView> tableView = new TableView<>();
        tableView.getColumns().addAll(
                createTextColumn("工单号", ActiveWorkOrderView::getWorkOrderNo),
                createTextColumn("报修单号", ActiveWorkOrderView::getRequestNo),
                createTextColumn("优先级", item -> UiDisplayText.workOrderPriority(item.getPriority())),
                createTextColumn("当前状态", item -> UiDisplayText.workOrderStatus(item.getStatus())),
                createTextColumn("派单时间", item -> item.getAssignedAt() == null ? "" : TIME_FORMATTER.format(item.getAssignedAt()))
        );
        configureFixedTable(tableView, 460, 1.382, 1.236, 0.764, 1.0, 1.382);
        return tableView;
    }

    private void refreshWorkOrders(TableView<ActiveWorkOrderView> workOrderTable, Long workerId) {
        workOrderTable.setItems(FXCollections.observableArrayList(
                appContext.workOrderService().listWorkerActiveWorkOrders(workerId, 12)
        ));
    }

    private WorkOrderStatus suggestNextStatus(WorkOrderStatus currentStatus) {
        if (currentStatus == null) {
            return WorkOrderStatus.ACCEPTED;
        }
        return switch (currentStatus) {
            case ASSIGNED -> WorkOrderStatus.ACCEPTED;
            case ACCEPTED -> WorkOrderStatus.IN_PROGRESS;
            case IN_PROGRESS -> WorkOrderStatus.COMPLETED;
            case WAITING_PARTS -> WorkOrderStatus.IN_PROGRESS;
            case COMPLETED, REJECTED, CANCELLED -> currentStatus;
        };
    }

    private TableColumn<ActiveWorkOrderView, String> createTextColumn(
            String title,
            java.util.function.Function<ActiveWorkOrderView, String> getter
    ) {
        TableColumn<ActiveWorkOrderView, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell ->
                Bindings.createStringBinding(() -> getter.apply(cell.getValue()))
        );
        return column;
    }
}
