package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.DemoAccountDirectory;
import com.scau.dormrepair.common.DemoAccountDirectory.DemoAccount;
import com.scau.dormrepair.domain.command.UpdateWorkOrderStatusCommand;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import com.scau.dormrepair.domain.view.ActiveWorkOrderView;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Worker processing module. */
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
        return "\u67e5\u770b\u5206\u914d\u7ed9\u81ea\u5df1\u7684\u5728\u9014\u5de5\u5355\uff0c\u5e76\u6301\u7eed\u66f4\u65b0\u5904\u7406\u72b6\u6001\u3002";
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
            throw new IllegalStateException("维修员身份未初始化，无法进入维修处理模块");
        }

        TableView<ActiveWorkOrderView> workOrderTable = buildWorkOrderTable();
        refreshWorkOrders(workOrderTable, currentWorker.id());

        GridPane contentGrid = createRatioWorkspace(
                42,
                58,
                buildStatusForm(currentWorker, workOrderTable),
                wrapPanel("\u6211\u7684\u5728\u9014\u5de5\u5355", workOrderTable)
        );

        return createPage(
                "维修员处理工作区",
                "\u53f3\u4fa7\u53ea\u663e\u793a\u5f53\u524d\u7ef4\u4fee\u5458\u81ea\u5df1\u7684\u6d3b\u52a8\u5de5\u5355\uff1b\u5de6\u4fa7\u66f4\u65b0\u72b6\u6001\u540e\uff0c\u8868\u683c\u4f1a\u7acb\u523b\u5237\u65b0\uff0c\u5df2\u5b8c\u6210\u5de5\u5355\u4f1a\u81ea\u52a8\u9000\u51fa\u6d3b\u52a8\u5217\u8868\u3002",
                contentGrid
        );
    }

    private Node buildStatusForm(DemoAccount currentWorker, TableView<ActiveWorkOrderView> workOrderTable) {
        Label selectedWorkOrderLabel = new Label("未选择工单");
        selectedWorkOrderLabel.getStyleClass().add("dashboard-spotlight-value");

        Label selectedOrderInfoLabel = new Label("\u8bf7\u5148\u4ece\u53f3\u4fa7\u5de5\u5355\u8868\u4e2d\u9009\u62e9\u4e00\u6761\u8bb0\u5f55\u3002");
        selectedOrderInfoLabel.getStyleClass().add("dashboard-mini-description");
        selectedOrderInfoLabel.setWrapText(true);

        ComboBox<WorkOrderStatus> statusBox = new ComboBox<>();
        statusBox.getItems().addAll(EDITABLE_STATUSES);
        statusBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(WorkOrderStatus workOrderStatus) {
                return UiDisplayText.workOrderStatus(workOrderStatus);
            }

            @Override
            public WorkOrderStatus fromString(String string) {
                return null;
            }
        });
        statusBox.setMaxWidth(Double.MAX_VALUE);
        UiMotion.installSmoothDropdown(statusBox);

        TextArea recordNoteArea = new TextArea();
        recordNoteArea.setPromptText("\u4f8b\u5982\uff1a\u5df2\u4e0a\u95e8\u68c0\u6d4b\u3001\u7b49\u5f85\u914d\u4ef6\u3001\u5df2\u66f4\u6362\u96f6\u4ef6\u5e76\u6062\u590d\u4f7f\u7528\u3002");
        recordNoteArea.setPrefRowCount(5);
        recordNoteArea.setWrapText(true);

        workOrderTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                selectedWorkOrderLabel.setText("未选择工单");
                selectedOrderInfoLabel.setText("\u8bf7\u5148\u4ece\u53f3\u4fa7\u5de5\u5355\u8868\u4e2d\u9009\u62e9\u4e00\u6761\u8bb0\u5f55\u3002");
                statusBox.getSelectionModel().clearSelection();
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
        refreshButton.getStyleClass().add("nav-button");
        refreshButton.setOnAction(event -> refreshWorkOrders(workOrderTable, currentWorker.id()));

        Node updateButton = FusionUiFactory.createPrimaryButton("\u56de\u586b\u5904\u7406\u72b6\u6001", 170, 40, () -> {
            try {
                ActiveWorkOrderView selectedWorkOrder = workOrderTable.getSelectionModel().getSelectedItem();
                if (selectedWorkOrder == null) {
                    throw new IllegalArgumentException("\u8bf7\u9009\u62e9\u4e00\u6761\u5de5\u5355");
                }
                if (statusBox.getValue() == null) {
                    throw new IllegalArgumentException("\u8bf7\u9009\u62e9\u8981\u66f4\u65b0\u6210\u7684\u72b6\u6001");
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
                createInlineSummaryCard("\u5f53\u524d\u5de5\u5355", summaryBox, "dashboard-mini-card", "dashboard-mini-soft"),
                createFieldBlock("\u66f4\u65b0\u72b6\u6001", statusBox),
                createFieldBlock("处理说明", recordNoteArea),
                actionRow
        );

        return wrapPanel("填写处理结果", formBox);
    }

    private TableView<ActiveWorkOrderView> buildWorkOrderTable() {
        TableView<ActiveWorkOrderView> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(460);
        tableView.getColumns().addAll(
                createTextColumn("\u5de5\u5355\u53f7", item -> item.getWorkOrderNo()),
                createTextColumn("报修单号", item -> item.getRequestNo()),
                createTextColumn("\u4f18\u5148\u7ea7", item -> UiDisplayText.workOrderPriority(item.getPriority())),
                createTextColumn("\u5f53\u524d\u72b6\u6001", item -> UiDisplayText.workOrderStatus(item.getStatus())),
                createTextColumn("派单时间", item -> item.getAssignedAt() == null ? "" : TIME_FORMATTER.format(item.getAssignedAt()))
        );
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
