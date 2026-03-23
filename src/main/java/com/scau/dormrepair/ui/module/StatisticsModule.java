package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.view.MonthlyRepairSummary;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

/**
 * 月度统计模块。
 */
public class StatisticsModule implements WorkbenchModule {

    private final AppContext appContext;

    public StatisticsModule(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String moduleName() {
        return "月度统计";
    }

    @Override
    public Parent createView() {
        VBox container = new VBox(18);
        container.setPadding(new Insets(24));

        Label titleLabel = new Label("月度统计报表");
        titleLabel.getStyleClass().add("section-title");

        Label hintLabel = new Label("当前先用表格展示月度汇总，后续如果需要再补折线图、柱状图和导出功能。");
        hintLabel.getStyleClass().add("plain-text");
        hintLabel.setWrapText(true);

        TableView<MonthlyRepairSummary> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getColumns().addAll(
                createColumn("月份", "monthLabel"),
                createColumn("报修总数", "totalRequests"),
                createColumn("已完成", "completedRequests"),
                createColumn("完成率(%)", "completionRate")
        );
        tableView.setItems(FXCollections.observableArrayList(
                appContext.statisticsService().listMonthlySummary(6)
        ));

        container.getChildren().addAll(titleLabel, hintLabel, tableView);
        return container;
    }

    private TableColumn<MonthlyRepairSummary, Object> createColumn(String title, String property) {
        TableColumn<MonthlyRepairSummary, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }
}
