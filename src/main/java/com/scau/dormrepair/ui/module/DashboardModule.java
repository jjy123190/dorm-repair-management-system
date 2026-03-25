package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.DashboardOverview;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.support.UiDisplayText;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * 首页只保留一屏内最关键的信息：
 * 当前角色最应该先看的主线、四个核心指标、最近报修。
 * 角色身份已经在顶部固定区展示，这里不再重复堆叠角色说明卡。
 */
public class DashboardModule extends AbstractWorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public DashboardModule(AppContext appContext) {
        super(appContext);
    }

    @Override
    public String moduleCode() {
        return "dashboard";
    }

    @Override
    public String moduleName() {
        return "首页概览";
    }

    @Override
    public String moduleDescription() {
        return "先看关键状态，再进入当前角色的主业务模块。";
    }

    @Override
    public Set<UserRole> supportedRoles() {
        return EnumSet.allOf(UserRole.class);
    }

    @Override
    public Parent createView() {
        DashboardOverview overview = appContext.dashboardService().loadOverview();

        GridPane metricGrid = new GridPane();
        metricGrid.setHgap(16);
        metricGrid.getStyleClass().add("dashboard-metric-row");
        metricGrid.getColumnConstraints().addAll(
                percentColumn(25),
                percentColumn(25),
                percentColumn(25),
                percentColumn(25)
        );
        metricGrid.add(createMetricCard("待审核", safeLong(overview.getPendingReviewRequests()), "管理员待处理", "dashboard-metric-card"), 0, 0);
        metricGrid.add(createMetricCard("处理中", safeLong(overview.getActiveWorkOrders()), "维修处理中", "dashboard-metric-card dashboard-metric-card-highlight"), 1, 0);
        metricGrid.add(createMetricCard("本月闭环", safeLong(overview.getCompletedThisMonth()), "月度完成", "dashboard-metric-card"), 2, 0);
        metricGrid.add(createMetricCard("累计工单", safeLong(overview.getTotalRequests()), "历史总量", "dashboard-metric-card"), 3, 0);

        TableView<RecentRepairRequestView> tableView = buildRepairRequestTable();
        tableView.setItems(FXCollections.observableArrayList(
                appContext.dashboardService().listRecentRepairRequests(5)
        ));
        fitTableHeightToRows(tableView, tableView.getItems().size(), 1, 5);

        VBox deck = new VBox(
                18,
                buildHeroCard(overview),
                metricGrid,
                wrapPanel("最近报修", tableView)
        );
        deck.getStyleClass().add("dashboard-overview-deck");

        return createPage("系统总览", "", deck);
    }

    private Node buildHeroCard(DashboardOverview overview) {
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(heroTitle());
        titleLabel.getStyleClass().add("dashboard-hero-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.control.Label descriptionLabel = new javafx.scene.control.Label(heroDescription());
        descriptionLabel.getStyleClass().add("dashboard-hero-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.control.Label helperLabel = new javafx.scene.control.Label(heroHelper(overview));
        helperLabel.getStyleClass().add("dashboard-hero-helper");
        helperLabel.setWrapText(true);
        helperLabel.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(12, titleLabel, descriptionLabel, helperLabel);
        content.getStyleClass().add("dashboard-hero-body");

        var pane = FusionUiFactory.createCard(content, 0, 0, "dashboard-hero-card");
        pane.getNode().setMaxWidth(Double.MAX_VALUE);
        return pane.getNode();
    }

    private Node createMetricCard(String tag, long value, String title, String styleClassNames) {
        javafx.scene.control.Label tagLabel = new javafx.scene.control.Label(tag);
        tagLabel.getStyleClass().add("dashboard-mini-tag");

        javafx.scene.control.Label valueLabel = new javafx.scene.control.Label(String.valueOf(value));
        valueLabel.getStyleClass().add("dashboard-mini-value");

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
        titleLabel.getStyleClass().add("dashboard-mini-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(10, tagLabel, valueLabel, titleLabel);
        content.getStyleClass().add("dashboard-mini-body");

        var pane = FusionUiFactory.createCard(content, 0, 126, styleClassNames.split(" "));
        pane.getNode().setMaxWidth(Double.MAX_VALUE);
        pane.getNode().setMinWidth(0);
        return pane.getNode();
    }

    private TableView<RecentRepairRequestView> buildRepairRequestTable() {
        TableView<RecentRepairRequestView> tableView = new TableView<>();

        TableColumn<RecentRepairRequestView, String> requestNoColumn = new TableColumn<>("报修单号");
        requestNoColumn.setCellValueFactory(new PropertyValueFactory<>("requestNo"));

        TableColumn<RecentRepairRequestView, String> studentColumn = new TableColumn<>("学生");
        studentColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        TableColumn<RecentRepairRequestView, String> locationColumn = new TableColumn<>("宿舍");
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("locationText"));

        TableColumn<RecentRepairRequestView, String> categoryColumn = new TableColumn<>("故障类型");
        categoryColumn.setCellValueFactory(cell ->
                Bindings.createStringBinding(() -> UiDisplayText.faultCategory(cell.getValue().getFaultCategory()))
        );

        TableColumn<RecentRepairRequestView, String> statusColumn = new TableColumn<>("状态");
        statusColumn.setCellValueFactory(cell ->
                Bindings.createStringBinding(() -> UiDisplayText.repairRequestStatus(cell.getValue().getStatus()))
        );

        TableColumn<RecentRepairRequestView, String> timeColumn = new TableColumn<>("提交时间");
        timeColumn.setCellValueFactory(cell ->
                Bindings.createStringBinding(() ->
                        cell.getValue().getSubmittedAt() == null ? "" : TIME_FORMATTER.format(cell.getValue().getSubmittedAt())
                )
        );

        tableView.getColumns().addAll(
                requestNoColumn, studentColumn, locationColumn, categoryColumn, statusColumn, timeColumn
        );
        configureFixedTable(tableView, 168, 1.618, 0.764, 1.236, 1.0, 0.764, 1.236);
        return tableView;
    }

    private String heroTitle() {
        return switch (appContext.appSession().getCurrentRole()) {
            case STUDENT -> "先看自己的报修进度";
            case ADMIN -> "先看待审和处理中工单";
            case WORKER -> "先看当前在途工单";
        };
    }

    private String heroDescription() {
        return switch (appContext.appSession().getCurrentRole()) {
            case STUDENT -> "首页只保留进度、最近记录和本月结果，不再重复身份说明。";
            case ADMIN -> "首页只保留审核、派单和处理压力，先解决主链路。";
            case WORKER -> "首页只保留在途工单、本月完成和最近处理记录。";
        };
    }

    private String heroHelper(DashboardOverview overview) {
        return switch (appContext.appSession().getCurrentRole()) {
            case STUDENT -> "你已提交 " + safeLong(overview.getTotalRequests()) + " 条报修，当前处理中 " + safeLong(overview.getActiveWorkOrders()) + " 条。";
            case ADMIN -> "待审核 " + safeLong(overview.getPendingReviewRequests()) + " 条，处理中 " + safeLong(overview.getActiveWorkOrders()) + " 条。";
            case WORKER -> "当前在途 " + safeLong(overview.getActiveWorkOrders()) + " 条，本月闭环 " + safeLong(overview.getCompletedThisMonth()) + " 条。";
        };
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}
