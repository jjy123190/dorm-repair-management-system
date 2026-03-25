package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.MonthlyRepairSummary;
import java.util.EnumSet;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * 月度统计模块。
 */
public class StatisticsModule extends AbstractWorkbenchModule {

    public StatisticsModule(AppContext appContext) {
        super(appContext);
    }

    @Override
    public String moduleCode() {
        return "statistics";
    }

    @Override
    public String moduleName() {
        return "月度统计";
    }

    @Override
    public String moduleDescription() {
        return "查看月度汇总、完成率和后续报表扩展入口。";
    }

    @Override
    public Set<UserRole> supportedRoles() {
        return EnumSet.of(UserRole.ADMIN);
    }

    @Override
    public Parent createView() {
        Label hintLabel = new Label("当前先用表格展示月度汇总，后续如果需要，再补折线图、柱状图和导出功能。");
        hintLabel.getStyleClass().add("plain-text");
        hintLabel.setWrapText(true);

        TableView<MonthlyRepairSummary> tableView = new TableView<>();
        tableView.getColumns().addAll(
                createColumn("月份", "monthLabel"),
                createColumn("报修总数", "totalRequests"),
                createColumn("已完成", "completedRequests"),
                createColumn("完成率(%)", "completionRate")
        );
        configureFixedTable(tableView, 420, 1.618, 1.0, 1.0, 1.382);
        tableView.setItems(FXCollections.observableArrayList(
                appContext.statisticsService().listMonthlySummary(6)
        ));

        return createPage(
                "月度统计报表",
                "统计页先保证核心汇总能展示，等数据库和前端同学后续补图表、筛选条件和导出。",
                hintLabel,
                wrapPanel("月度汇总", tableView)
        );
    }

    private TableColumn<MonthlyRepairSummary, Object> createColumn(String title, String property) {
        TableColumn<MonthlyRepairSummary, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }
}
