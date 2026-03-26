package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.MonthlyRepairSummary;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.scene.Parent;

/**
 * 月度统计页先稳定展示核心汇总，不把交互复杂度堆上去。
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
        return "";
    }

    @Override
    public Set<UserRole> supportedRoles() {
        return EnumSet.of(UserRole.ADMIN);
    }

    @Override
    public Parent createView() {
        List<MonthlyRepairSummary> monthlyRows = appContext.statisticsService().listMonthlySummary(6);
        return createPage(
                "月度统计报表",
                "",
                wrapPanel("月度汇总", buildSummaryTable(monthlyRows))
        );
    }

    private Parent buildSummaryTable(List<MonthlyRepairSummary> monthlyRows) {
        return (Parent) createStaticDataTable(
                List.of(
                        staticColumn("月份", 1.618, MonthlyRepairSummary::getMonthLabel),
                        staticColumn("报修总数", 1.0, item -> String.valueOf(safeLong(item.getTotalRequests()))),
                        staticColumn("已完成", 1.0, item -> String.valueOf(safeLong(item.getCompletedRequests()))),
                        staticColumn("完成率(%)", 1.382, item ->
                                item.getCompletionRate() == null ? "0.00" : item.getCompletionRate().toPlainString())
                ),
                monthlyRows,
                4
        );
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}
