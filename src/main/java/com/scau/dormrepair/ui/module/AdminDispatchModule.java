package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * 管理员派单模块。
 * 这里先把待派单列表和标准流程放稳，后续同学可以直接接事件处理。
 */
public class AdminDispatchModule implements WorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AppContext appContext;

    public AdminDispatchModule(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String moduleName() {
        return "管理员派单";
    }

    @Override
    public Parent createView() {
        VBox container = new VBox(18);
        container.setPadding(new Insets(24));

        Label titleLabel = new Label("管理员派单工作区");
        titleLabel.getStyleClass().add("section-title");

        ListView<String> workflowList = new ListView<>();
        workflowList.setItems(FXCollections.observableArrayList(
                "1. 审核学生报修信息是否完整",
                "2. 判断故障类型和优先级",
                "3. 选择维修人员并填写 assignmentNote",
                "4. 调用 WorkOrderService.assign 完成派单",
                "5. 进入维修流转和催办管理"
        ));
        workflowList.setPrefHeight(200);

        TableView<RecentRepairRequestView> pendingTable = new TableView<>();
        pendingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        pendingTable.setItems(FXCollections.observableArrayList(
                appContext.repairRequestService().listPendingAssignmentRequests(10)
        ));
        pendingTable.getColumns().addAll(
                createTextColumn("报修单号", cell -> cell.getRequestNo()),
                createTextColumn("学生", cell -> cell.getStudentName()),
                createTextColumn("宿舍", cell -> cell.getLocationText()),
                createTextColumn("故障类型", cell -> cell.getFaultCategory() == null ? "" : cell.getFaultCategory().name()),
                createTextColumn("提交时间", cell -> cell.getSubmittedAt() == null ? "" : TIME_FORMATTER.format(cell.getSubmittedAt()))
        );

        HBox contentRow = new HBox(18, wrapPanel("派单流程", workflowList), wrapPanel("待派单列表", pendingTable));
        HBox.setHgrow(contentRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(contentRow.getChildren().get(1), Priority.ALWAYS);

        container.getChildren().addAll(titleLabel, contentRow);
        return container;
    }

    private VBox wrapPanel(String title, javafx.scene.Node content) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("subsection-title");

        VBox box = new VBox(12, titleLabel, content);
        box.getStyleClass().add("panel-box");
        box.setPadding(new Insets(18));
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private TableColumn<RecentRepairRequestView, String> createTextColumn(
            String title,
            java.util.function.Function<RecentRepairRequestView, String> getter
    ) {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(() -> getter.apply(cell.getValue()))
        );
        return column;
    }
}
