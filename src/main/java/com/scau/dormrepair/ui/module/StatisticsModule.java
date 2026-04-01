package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.DormBuildingFaultSummary;
import com.scau.dormrepair.domain.view.FaultCategorySummary;
import com.scau.dormrepair.domain.view.MonthlyRepairSummary;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.TimeoutStageSummary;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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
        List<FaultCategorySummary> categoryRows = appContext.statisticsService().listFaultCategorySummary(6);
        List<DormBuildingFaultSummary> dormRows = appContext.statisticsService().listDormBuildingFaultSummary(6, 8);
        List<TimeoutStageSummary> timeoutRows = appContext.statisticsService().listTimeoutStageSummary();

        ObjectProperty<DrillDownSelection> selection = new SimpleObjectProperty<>();
        Label detailTitle = new Label("点击上方任意 drill-down 行，可在这里查看对应单据。");
        detailTitle.getStyleClass().add("plain-text");
        VBox detailTableContainer = new VBox();
        detailTableContainer.setFillWidth(true);
        detailTableContainer.getChildren().setAll(createEmptyState("等待选择统计项", "从宿舍区 / 楼栋、故障类别或超时阶段中选择一行后，这里会展示对应单据。"));

        GridPane metricDeck = buildMetricDeck(monthlyRows, timeoutRows);
        Node charts = createRatioWorkspace(
                58,
                42,
                wrapPanel("近六个月趋势", buildTrendChart(monthlyRows)),
                wrapPanel("故障分类占比", buildCategoryChart(categoryRows))
        );

        Node drillDownTop = createRatioWorkspace(
                50,
                50,
                wrapPanel("宿舍区 / 楼栋故障排行", buildDormBuildingTable(dormRows, selection, detailTitle, detailTableContainer)),
                wrapPanel("故障类别 Top", buildCategoryTable(categoryRows, selection, detailTitle, detailTableContainer))
        );

        Node drillDownBottom = createRatioWorkspace(
                40,
                60,
                wrapPanel("超时阶段分析", buildTimeoutTable(timeoutRows, selection, detailTitle, detailTableContainer)),
                wrapPanel("对应单据", buildDetailPanel(detailTitle, detailTableContainer))
        );

        return createPage(
                "月度统计报表",
                "统计页继续保持页面内分析，不提供导出；drill-down 点击后可直接定位对应单据。",
                metricDeck,
                charts,
                drillDownTop,
                drillDownBottom,
                wrapPanel("近六个月明细", buildSummaryTable(monthlyRows))
        );
    }

    private GridPane buildMetricDeck(List<MonthlyRepairSummary> monthlyRows, List<TimeoutStageSummary> timeoutRows) {
        GridPane metricDeck = new GridPane();
        metricDeck.setHgap(16);
        metricDeck.setVgap(16);
        metricDeck.getColumnConstraints().addAll(percentColumn(25), percentColumn(25), percentColumn(25), percentColumn(25));
        metricDeck.add(createMetricCard("近六个月报修总数", String.valueOf(totalRequests(monthlyRows)), "按月聚合后的统计结果，包含当前可见月份。"), 0, 0);
        metricDeck.add(createMetricCard("近六个月已完成", String.valueOf(totalCompleted(monthlyRows)), "统计口径与月报表一致，只统计 COMPLETED 记录。"), 1, 0);
        metricDeck.add(createMetricCard("近六个月平均完成率", averageCompletionRate(monthlyRows), "用于快速观察近期工单闭环效率。"), 2, 0);
        metricDeck.add(createMetricCard("当前超时工单", String.valueOf(totalOverdue(timeoutRows)), "待派单、待接单、处理中三个阶段的超时合计。"), 3, 0);
        return metricDeck;
    }

    private Node createMetricCard(String title, String value, String description) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("fusion-card-title");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().addAll("fusion-card-value", "metric-value-dark");
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("fusion-card-description");
        descriptionLabel.setWrapText(true);
        VBox body = new VBox(8, titleLabel, valueLabel, descriptionLabel);
        body.getStyleClass().add("fusion-card-body");
        body.setFillWidth(true);
        var pane = FusionUiFactory.createCard(body, 0, 0, "dashboard-metric-card", "dashboard-metric-card-highlight");
        pane.getNode().setMaxWidth(Double.MAX_VALUE);
        return pane.getNode();
    }

    private Node buildTrendChart(List<MonthlyRepairSummary> monthlyRows) {
        if (monthlyRows == null || monthlyRows.isEmpty()) {
            return createEmptyState("暂无趋势数据", "近六个月内还没有可用于生成折线图的报修记录。");
        }
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("月份");
        yAxis.setLabel("工单数量");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(true);
        chart.setCreateSymbols(true);
        chart.setAnimated(false);
        chart.setMinHeight(320);
        chart.setPrefHeight(320);
        chart.setMaxWidth(Double.MAX_VALUE);
        chart.setTitle(null);

        XYChart.Series<String, Number> totalSeries = new XYChart.Series<>();
        totalSeries.setName("报修总数");
        XYChart.Series<String, Number> completedSeries = new XYChart.Series<>();
        completedSeries.setName("已处理");

        for (MonthlyRepairSummary item : ascendingMonths(monthlyRows)) {
            totalSeries.getData().add(new XYChart.Data<>(safeText(item.getMonthLabel()), safeLong(item.getTotalRequests())));
            completedSeries.getData().add(new XYChart.Data<>(safeText(item.getMonthLabel()), safeLong(item.getCompletedRequests())));
        }
        chart.getData().setAll(List.of(totalSeries, completedSeries));
        return chart;
    }

    private Node buildCategoryChart(List<FaultCategorySummary> categoryRows) {
        if (categoryRows == null || categoryRows.isEmpty()) {
            return createEmptyState("暂无分类数据", "近六个月内还没有可用于生成分类占比的报修记录。 ");
        }
        PieChart chart = new PieChart();
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        chart.setClockwise(true);
        chart.setMinHeight(320);
        chart.setPrefHeight(320);
        for (FaultCategorySummary item : categoryRows) {
            chart.getData().add(new PieChart.Data(
                    faultCategoryLabel(item.getFaultCategory()) + " " + formatRate(item.getPercentage()) + " / " + safeLong(item.getTotalRequests()),
                    safeLong(item.getTotalRequests())
            ));
        }
        return chart;
    }

    private Parent buildSummaryTable(List<MonthlyRepairSummary> monthlyRows) {
        return (Parent) createStaticDataTableOrEmpty(
                List.of(
                        staticColumn("月份", 1.08, MonthlyRepairSummary::getMonthLabel),
                        staticColumn("报修总数", 0.96, item -> String.valueOf(safeLong(item.getTotalRequests()))),
                        staticColumn("已处理", 0.92, item -> String.valueOf(safeLong(item.getCompletedRequests()))),
                        staticColumn("完成率", 1.04, item -> formatRate(item.getCompletionRate()))
                ),
                monthlyRows,
                6,
                "暂无统计数据",
                "当前月份范围内还没有可展示的统计结果。"
        );
    }

    private Node buildDormBuildingTable(List<DormBuildingFaultSummary> rows, ObjectProperty<DrillDownSelection> selection, Label detailTitle, VBox detailTableContainer) {
        return createSelectableDrillDownTable(
                List.of("宿舍区", "楼栋", "报修数"),
                rows,
                List.of(
                        item -> safeText(item.getDormArea()),
                        item -> safeText(item.getBuildingNo()),
                        item -> String.valueOf(safeLong(item.getTotalRequests()))
                ),
                item -> {
                    selection.set(new DrillDownSelection(
                            safeText(item.getDormArea()) + " / " + safeText(item.getBuildingNo()),
                            appContext.statisticsService().listDormBuildingRequestDetails(6, item.getDormArea(), item.getBuildingNo(), 20)
                    ));
                    applySelection(selection.get(), detailTitle, detailTableContainer);
                },
                "暂无楼栋维度数据",
                "近六个月内还没有可用的楼栋统计。"
        );
    }

    private Node buildCategoryTable(List<FaultCategorySummary> rows, ObjectProperty<DrillDownSelection> selection, Label detailTitle, VBox detailTableContainer) {
        return createSelectableDrillDownTable(
                List.of("故障类别", "报修数", "占比"),
                rows,
                List.of(
                        item -> faultCategoryLabel(item.getFaultCategory()),
                        item -> String.valueOf(safeLong(item.getTotalRequests())),
                        item -> formatRate(item.getPercentage())
                ),
                item -> {
                    selection.set(new DrillDownSelection(
                            faultCategoryLabel(item.getFaultCategory()),
                            appContext.statisticsService().listFaultCategoryRequestDetails(6, item.getFaultCategory(), 20)
                    ));
                    applySelection(selection.get(), detailTitle, detailTableContainer);
                },
                "暂无故障类别统计",
                "近六个月内还没有可用的故障类别数据。"
        );
    }

    private Node buildTimeoutTable(List<TimeoutStageSummary> rows, ObjectProperty<DrillDownSelection> selection, Label detailTitle, VBox detailTableContainer) {
        return createSelectableDrillDownTable(
                List.of("阶段", "总数", "超时数", "超时占比"),
                rows,
                List.of(
                        item -> timeoutStageLabel(item.getStageLabel()),
                        item -> String.valueOf(safeLong(item.getTotalCount())),
                        item -> String.valueOf(safeLong(item.getOverdueCount())),
                        item -> formatRate(item.getOverdueRate())
                ),
                item -> {
                    selection.set(new DrillDownSelection(
                            timeoutStageLabel(item.getStageLabel()),
                            appContext.statisticsService().listTimeoutStageRequestDetails(item.getStageLabel(), 20)
                    ));
                    applySelection(selection.get(), detailTitle, detailTableContainer);
                },
                "暂无超时分析数据",
                "当前没有可用的超时分析结果。"
        );
    }

    private Node buildDetailPanel(Label detailTitle, VBox detailTableContainer) {
        VBox box = new VBox(12, detailTitle, detailTableContainer);
        box.setFillWidth(true);
        VBox.setVgrow(detailTableContainer, Priority.ALWAYS);
        return box;
    }

    private void applySelection(DrillDownSelection selection, Label detailTitle, VBox detailTableContainer) {
        if (selection == null) {
            detailTitle.setText("点击上方任意 drill-down 行，可在这里查看对应单据。");
            detailTableContainer.getChildren().setAll(createEmptyState("等待选择统计项", "从宿舍区 / 楼栋、故障类别或超时阶段中选择一行后，这里会展示对应单据。"));
            return;
        }
        detailTitle.setText("对应单据：" + selection.title());
        detailTableContainer.getChildren().setAll(buildRequestDetailTable(selection.rows()));
    }

    private Parent buildRequestDetailTable(List<RecentRepairRequestView> rows) {
        return (Parent) createStaticDataTableOrEmpty(
                List.of(
                        staticColumn("报修单号", 1.1, RecentRepairRequestView::getRequestNo),
                        staticColumn("学生", 0.8, RecentRepairRequestView::getStudentName),
                        staticColumn("宿舍位置", 1.2, RecentRepairRequestView::getLocationText),
                        staticColumn("状态 / 时效", 1.2, this::requestStatusText),
                        staticColumn("提交时间", 0.9, item -> formatDateTime(item.getSubmittedAt()))
                ),
                rows,
                8,
                "暂无对应单据",
                "当前筛选口径下没有找到对应报修单。"
        );
    }

    private <T> Node createSelectableDrillDownTable(List<String> headers, List<T> rows, List<java.util.function.Function<T, String>> valueProviders, java.util.function.Consumer<T> onClick, String emptyTitle, String emptyDescription) {
        if (rows == null || rows.isEmpty()) {
            return createEmptyState(emptyTitle, emptyDescription);
        }
        GridPane table = new GridPane();
        table.getStyleClass().add("selectable-static-grid-table");
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMinWidth(0);
        double percent = 100.0 / headers.size();
        for (int index = 0; index < headers.size(); index++) {
            table.getColumnConstraints().add(percentColumn(percent));
            addSelectableCell(table, 0, index, headers.get(index), true, index == 0, index == headers.size() - 1, null);
        }
        int visibleCount = Math.max(rows.size(), 6);
        for (int rowIndex = 0; rowIndex < visibleCount; rowIndex++) {
            T rowItem = rowIndex < rows.size() ? rows.get(rowIndex) : null;
            for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
                String text = rowItem == null ? "" : safeText(valueProviders.get(columnIndex).apply(rowItem));
                T current = rowItem;
                Runnable action = current == null ? null : () -> onClick.accept(current);
                addSelectableCell(table, rowIndex + 1, columnIndex, text, false, false, columnIndex == headers.size() - 1, action);
            }
        }
        return table;
    }

    private void addSelectableCell(GridPane table, int rowIndex, int columnIndex, String text, boolean headerCell, boolean firstHeaderCell, boolean lastCell, Runnable action) {
        Label label = new Label(text == null ? "" : text);
        label.getStyleClass().add(headerCell ? "static-table-header-cell" : "static-table-cell");
        label.setWrapText(!headerCell);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinWidth(0);
        HBox shell = new HBox(label);
        shell.setMaxWidth(Double.MAX_VALUE);
        shell.setMinWidth(0);
        shell.setAlignment(javafx.geometry.Pos.CENTER);
        shell.setPrefHeight(headerCell ? 48 : 54);
        shell.getStyleClass().add(headerCell ? "selectable-static-grid-header-shell" : "selectable-static-grid-cell-shell");
        if (firstHeaderCell) {
            shell.getStyleClass().add("selectable-static-grid-header-first");
        }
        if (lastCell) {
            shell.getStyleClass().add(headerCell ? "selectable-static-grid-header-last" : "selectable-static-grid-cell-last");
        }
        if (action != null) {
            shell.getStyleClass().add("selectable-static-grid-clickable");
            shell.setOnMouseClicked(event -> action.run());
        }
        table.add(shell, columnIndex, rowIndex);
    }

    private List<MonthlyRepairSummary> ascendingMonths(List<MonthlyRepairSummary> monthlyRows) {
        List<MonthlyRepairSummary> ordered = new ArrayList<>(monthlyRows);
        java.util.Collections.reverse(ordered);
        return ordered;
    }

    private String averageCompletionRate(List<MonthlyRepairSummary> monthlyRows) {
        if (monthlyRows == null || monthlyRows.isEmpty()) {
            return "0.00%";
        }
        BigDecimal total = BigDecimal.ZERO;
        for (MonthlyRepairSummary item : monthlyRows) {
            total = total.add(item.getCompletionRate() == null ? BigDecimal.ZERO : item.getCompletionRate());
        }
        return formatRate(total.divide(BigDecimal.valueOf(monthlyRows.size()), 2, RoundingMode.HALF_UP));
    }

    private long totalRequests(List<MonthlyRepairSummary> monthlyRows) {
        return monthlyRows == null ? 0L : monthlyRows.stream().mapToLong(item -> safeLong(item.getTotalRequests())).sum();
    }

    private long totalCompleted(List<MonthlyRepairSummary> monthlyRows) {
        return monthlyRows == null ? 0L : monthlyRows.stream().mapToLong(item -> safeLong(item.getCompletedRequests())).sum();
    }

    private long totalOverdue(List<TimeoutStageSummary> timeoutRows) {
        return timeoutRows == null ? 0L : timeoutRows.stream().mapToLong(item -> safeLong(item.getOverdueCount())).sum();
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private String formatRate(BigDecimal value) {
        return value == null ? "0.00%" : value.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "--" : value.toString().replace('T', ' ');
    }

    private String requestStatusText(RecentRepairRequestView item) {
        String status = switch (item.getStatus()) {
            case SUBMITTED -> "已提交";
            case ASSIGNED -> "已受理";
            case IN_PROGRESS -> "处理中";
            case PENDING_CONFIRMATION -> "待确认完成";
            case REWORK_IN_PROGRESS -> "返修中";
            case COMPLETED -> "已处理";
            case REJECTED -> "已驳回";
            case CANCELLED -> "已取消";
        };
        if (item.getTimeoutLabel() == null || item.getTimeoutLabel().isBlank()) {
            return status;
        }
        return status + " / " + item.getTimeoutLabel();
    }

    private String faultCategoryLabel(FaultCategory category) {
        if (category == null) {
            return "其他";
        }
        return switch (category) {
            case ELECTRICITY -> "用电";
            case WATER_PIPE -> "水管";
            case DOOR_WINDOW -> "门窗";
            case NETWORK -> "网络";
            case FURNITURE -> "家具";
            case PUBLIC_AREA -> "公共区域";
            case OTHER -> "其他";
        };
    }

    private String timeoutStageLabel(String stageLabel) {
        if (stageLabel == null || stageLabel.isBlank()) {
            return "--";
        }
        return switch (stageLabel) {
            case "PENDING_ASSIGN" -> "待派单";
            case "PENDING_ACCEPT" -> "已派单未接单";
            case "PROCESSING" -> "处理中";
            default -> stageLabel;
        };
    }

    private record DrillDownSelection(String title, List<RecentRepairRequestView> rows) {
    }
}