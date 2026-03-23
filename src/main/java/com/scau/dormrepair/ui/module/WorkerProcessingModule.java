package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.view.ActiveWorkOrderView;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
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
 * 维修员处理模块。
 */
public class WorkerProcessingModule implements WorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AppContext appContext;

    public WorkerProcessingModule(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String moduleName() {
        return "维修处理";
    }

    @Override
    public Parent createView() {
        VBox container = new VBox(18);
        container.setPadding(new Insets(24));

        Label titleLabel = new Label("维修员处理工作区");
        titleLabel.getStyleClass().add("section-title");

        ListView<String> checklist = new ListView<>();
        checklist.setItems(FXCollections.observableArrayList(
                "1. 查看已派单工单",
                "2. 维修员接单后更新为 ACCEPTED",
                "3. 开始维修时更新为 IN_PROGRESS",
                "4. 缺少配件可更新为 WAITING_PARTS",
                "5. 完成后写说明并更新为 COMPLETED"
        ));
        checklist.setPrefHeight(200);

        TableView<ActiveWorkOrderView> workOrderTable = buildTable();
        workOrderTable.setItems(FXCollections.observableArrayList(
                appContext.workOrderService().listActiveWorkOrders(10)
        ));

        HBox contentRow = new HBox(18, wrapPanel("处理步骤", checklist), wrapPanel("活动工单", workOrderTable));
        HBox.setHgrow(contentRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(contentRow.getChildren().get(1), Priority.ALWAYS);

        container.getChildren().addAll(titleLabel, contentRow);
        return container;
    }

    private TableView<ActiveWorkOrderView> buildTable() {
        TableView<ActiveWorkOrderView> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getColumns().addAll(
                createTextColumn("工单号", item -> valueOf(item.getWorkOrderNo())),
                createTextColumn("报修单号", item -> valueOf(item.getRequestNo())),
                createTextColumn("维修员 ID", item -> valueOf(item.getWorkerId())),
                createTextColumn("优先级", item -> valueOf(item.getPriority())),
                createTextColumn("状态", item -> valueOf(item.getStatus())),
                createTextColumn("派单时间", item -> item.getAssignedAt() == null ? "" : TIME_FORMATTER.format(item.getAssignedAt()))
        );
        return tableView;
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

    private TableColumn<ActiveWorkOrderView, String> createTextColumn(
            String title,
            Function<ActiveWorkOrderView, String> getter
    ) {
        TableColumn<ActiveWorkOrderView, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(() -> getter.apply(cell.getValue()))
        );
        return column;
    }

    private String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
