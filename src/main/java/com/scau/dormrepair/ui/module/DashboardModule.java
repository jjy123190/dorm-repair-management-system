package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.view.DashboardOverview;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * 首页模块，先把最关键的概览和最近报修列表展示出来。
 */
public class DashboardModule implements WorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AppContext appContext;

    public DashboardModule(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String moduleName() {
        return "首页概览";
    }

    @Override
    public Parent createView() {
        DashboardOverview overview = appContext.dashboardService().loadOverview();

        VBox container = new VBox(18);
        container.setPadding(new Insets(24));

        Label titleLabel = new Label("系统概览");
        titleLabel.getStyleClass().add("section-title");

        HBox cardRow = new HBox(16,
                createMetricCard("报修总数", safeLong(overview.getTotalRequests()), "所有学生报修申请"),
                createMetricCard("待审核", safeLong(overview.getPendingReviewRequests()), "等待管理员派单"),
                createMetricCard("进行中工单", safeLong(overview.getActiveWorkOrders()), "维修员正在处理"),
                createMetricCard("本月已完成", safeLong(overview.getCompletedThisMonth()), "用于答辩展示月报")
        );

        Label tableTitle = new Label("最近报修");
        tableTitle.getStyleClass().add("section-title");

        TableView<RecentRepairRequestView> tableView = buildRepairRequestTable();
        tableView.setItems(FXCollections.observableArrayList(
                appContext.dashboardService().listRecentRepairRequests(8)
        ));

        container.getChildren().addAll(titleLabel, cardRow, tableTitle, tableView);
        return container;
    }

    private VBox createMetricCard(String title, long value, String description) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-title");

        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.getStyleClass().add("metric-value");

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("metric-description");
        descriptionLabel.setWrapText(true);

        VBox box = new VBox(10, titleLabel, valueLabel, descriptionLabel);
        box.getStyleClass().add("metric-card");
        box.setPadding(new Insets(18));
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private TableView<RecentRepairRequestView> buildRepairRequestTable() {
        TableView<RecentRepairRequestView> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(320);

        TableColumn<RecentRepairRequestView, String> requestNoColumn = new TableColumn<>("报修单号");
        requestNoColumn.setCellValueFactory(new PropertyValueFactory<>("requestNo"));

        TableColumn<RecentRepairRequestView, String> studentColumn = new TableColumn<>("学生");
        studentColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        TableColumn<RecentRepairRequestView, String> locationColumn = new TableColumn<>("宿舍");
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("locationText"));

        TableColumn<RecentRepairRequestView, String> categoryColumn = new TableColumn<>("故障类型");
        categoryColumn.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(() ->
                        cell.getValue().getFaultCategory() == null ? "" : cell.getValue().getFaultCategory().name()
                ));

        TableColumn<RecentRepairRequestView, String> statusColumn = new TableColumn<>("状态");
        statusColumn.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(() ->
                        cell.getValue().getStatus() == null ? "" : cell.getValue().getStatus().name()
                ));

        TableColumn<RecentRepairRequestView, String> timeColumn = new TableColumn<>("提交时间");
        timeColumn.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(() ->
                        cell.getValue().getSubmittedAt() == null ? "" : TIME_FORMATTER.format(cell.getValue().getSubmittedAt())
                ));

        tableView.getColumns().addAll(
                requestNoColumn, studentColumn, locationColumn, categoryColumn, statusColumn, timeColumn
        );
        return tableView;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}
