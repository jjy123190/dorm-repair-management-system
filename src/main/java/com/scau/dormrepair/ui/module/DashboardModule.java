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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * 首页概览只保留一屏内最有价值的信息。
 * 这里先讲清楚当前角色、处理压力和最近报修，不再堆很多重复卡片。
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

        GridPane overviewGrid = new GridPane();
        overviewGrid.setHgap(20);
        overviewGrid.getStyleClass().add("dashboard-overview-deck");
        overviewGrid.getColumnConstraints().addAll(percentColumn(68), percentColumn(32));
        overviewGrid.add(buildHeroCard(overview), 0, 0);
        overviewGrid.add(buildSpotlightColumn(overview), 1, 0);

        // 四张统计卡统一等分，避免因为文案长短不同把版面挤乱。
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

        return createPage(
                "系统总览",
                "当前角色、处理压力和最近报修都放在这一屏里。",
                overviewGrid,
                metricGrid,
                wrapPanel("最近报修", tableView)
        );
    }

    private Node buildHeroCard(DashboardOverview overview) {
        Label eyebrowLabel = new Label(roleEyebrow());
        eyebrowLabel.getStyleClass().add("dashboard-hero-eyebrow");

        Label titleLabel = new Label(heroTitle());
        titleLabel.getStyleClass().add("dashboard-hero-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label descriptionLabel = new Label(heroDescription());
        descriptionLabel.getStyleClass().add("dashboard-hero-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(Double.MAX_VALUE);

        Label helperLabel = new Label(heroHelper(overview));
        helperLabel.getStyleClass().add("dashboard-hero-helper");
        helperLabel.setWrapText(true);
        helperLabel.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(12, eyebrowLabel, titleLabel, descriptionLabel, helperLabel);
        content.getStyleClass().add("dashboard-hero-body");

        var pane = FusionUiFactory.createCard(content, 0, 0, "dashboard-hero-card");
        pane.getNode().setMaxWidth(Double.MAX_VALUE);
        return pane.getNode();
    }

    private VBox buildSpotlightColumn(DashboardOverview overview) {
        VBox column = new VBox(
                14,
                createSpotlightCard("当前角色", roleTitle(), roleSummary(), "dashboard-spotlight-card"),
                createSpotlightCard("当前重点", focusTitle(overview), focusSummary(overview), "dashboard-spotlight-card dashboard-spotlight-card-focus")
        );
        column.getStyleClass().add("dashboard-spotlight-column");
        column.setMaxWidth(Double.MAX_VALUE);
        return column;
    }

    private Node createSpotlightCard(String title, String value, String description, String styleClassNames) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dashboard-spotlight-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("dashboard-spotlight-value");
        valueLabel.setWrapText(true);
        valueLabel.setMaxWidth(Double.MAX_VALUE);

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("dashboard-spotlight-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(8, titleLabel, valueLabel, descriptionLabel);
        content.getStyleClass().add("dashboard-spotlight-body");

        var pane = FusionUiFactory.createCard(content, 0, 0, styleClassNames.split(" "));
        pane.getNode().setMaxWidth(Double.MAX_VALUE);
        return pane.getNode();
    }

    private Node createMetricCard(String tag, long value, String title, String styleClassNames) {
        Label tagLabel = new Label(tag);
        tagLabel.getStyleClass().add("dashboard-mini-tag");

        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.getStyleClass().add("dashboard-mini-value");

        Label titleLabel = new Label(title);
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
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(168);

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
        return tableView;
    }

    private String roleEyebrow() {
        return switch (appContext.appSession().getCurrentRole()) {
            case STUDENT -> "STUDENT BOARD";
            case ADMIN -> "ADMIN BOARD";
            case WORKER -> "WORKER BOARD";
        };
    }

    private String heroTitle() {
        return switch (appContext.appSession().getCurrentRole()) {
            case STUDENT -> "先看自己的报修进度";
            case ADMIN -> "把待审和派单压力看清楚";
            case WORKER -> "先处理在途工单";
        };
    }

    private String heroDescription() {
        return switch (appContext.appSession().getCurrentRole()) {
            case STUDENT -> "学生工作台先保留进度、待办和最近记录，避免一上来塞太多操作。";
            case ADMIN -> "管理员首页先聚焦审核、派单和处理节奏，不把统计和详情混成一堆。";
            case WORKER -> "维修员首页先给出接单和回填的主线，不让页面被次要信息冲淡。";
        };
    }

    private String heroHelper(DashboardOverview overview) {
        return switch (appContext.appSession().getCurrentRole()) {
            case STUDENT -> "你已提交 " + safeLong(overview.getTotalRequests()) + " 条报修，当前处理中 " + safeLong(overview.getActiveWorkOrders()) + " 条。";
            case ADMIN -> "当前待审核 " + safeLong(overview.getPendingReviewRequests()) + " 条，处理中 " + safeLong(overview.getActiveWorkOrders()) + " 条。";
            case WORKER -> "当前在途 " + safeLong(overview.getActiveWorkOrders()) + " 条，本月闭环 " + safeLong(overview.getCompletedThisMonth()) + " 条。";
        };
    }

    private String roleTitle() {
        UserRole role = appContext.appSession().getCurrentRole();
        return role == null ? "未登录角色" : role.displayName();
    }

    private String roleSummary() {
        return switch (appContext.appSession().getCurrentRole()) {
            case STUDENT -> "提交报修、查看进度、完成评价。";
            case ADMIN -> "审核、派单、催办、统计。";
            case WORKER -> "接单、处理、回填结果。";
        };
    }

    private String focusTitle(DashboardOverview overview) {
        long pendingReview = safeLong(overview.getPendingReviewRequests());
        long activeOrders = safeLong(overview.getActiveWorkOrders());

        return switch (appContext.appSession().getCurrentRole()) {
            case STUDENT -> activeOrders > 0 ? "继续跟进" : "可以报修";
            case ADMIN -> pendingReview > 0 ? "先审工单" : "审核平稳";
            case WORKER -> activeOrders > 0 ? "优先处理" : "可以接单";
        };
    }

    private String focusSummary(DashboardOverview overview) {
        long pendingReview = safeLong(overview.getPendingReviewRequests());
        long activeOrders = safeLong(overview.getActiveWorkOrders());
        long completedThisMonth = safeLong(overview.getCompletedThisMonth());

        return switch (appContext.appSession().getCurrentRole()) {
            case STUDENT -> "当前有 " + activeOrders + " 条工单处于处理中或待完成阶段。";
            case ADMIN -> "待审核 " + pendingReview + " 条，处理中 " + activeOrders + " 条。";
            case WORKER -> "当前在途 " + activeOrders + " 条，本月完成 " + completedThisMonth + " 条。";
        };
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

}
