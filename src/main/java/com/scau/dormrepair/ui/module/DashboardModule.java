package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.enums.TimeoutLevel;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.DashboardOverview;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.component.TimeoutChip;
import com.scau.dormrepair.ui.support.UiDisplayText;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DashboardModule extends AbstractWorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Consumer<String> moduleOpener;

    public DashboardModule(AppContext appContext, Consumer<String> moduleOpener) {
        super(appContext);
        this.moduleOpener = moduleOpener;
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
        return "在这里优先看当前最该处理的风险、待办和闭环状态。";
    }

    @Override
    public Set<UserRole> supportedRoles() {
        return EnumSet.allOf(UserRole.class);
    }

    @Override
    public Parent createView() {
        UserRole currentRole = appContext.appSession().getCurrentRole();
        Long currentAccountId = appContext.appSession().getCurrentAccountId();

        DashboardOverview overview = appContext.dashboardService().loadOverview(currentRole, currentAccountId);
        List<RecentRepairRequestView> recentRequests = prioritizeRecentRows(
                currentRole,
                appContext.dashboardService().listRecentRepairRequests(currentRole, currentAccountId, 8)
        );

        GridPane topWorkspace = createRatioWorkspace(
                64,
                36,
                wrapPanel(summaryPanelTitle(), buildSummaryPanel(currentRole, overview)),
                wrapPanel("常用操作", buildQuickActions(currentRole))
        );

        GridPane metricGrid = buildMetricGrid(currentRole, overview);

        GridPane lowerWorkspace = createRatioWorkspace(
                68,
                32,
                wrapPanel("风险优先列表", buildRecentRepairsTable(currentRole, recentRequests)),
                wrapPanel("处理提醒", buildReminderPanel(currentRole, overview, recentRequests))
        );

        VBox deck = new VBox(18, topWorkspace, metricGrid, lowerWorkspace);
        deck.getStyleClass().add("dashboard-overview-deck");
        return createPage(moduleName(), moduleLead(), deck);
    }

    private GridPane buildMetricGrid(UserRole role, DashboardOverview overview) {
        List<MetricItem> items = switch (role) {
            case STUDENT -> List.of(
                    new MetricItem("处理中", safeLong(overview.getActiveWorkOrders()), "当前仍在推进中的报修。", "dashboard-metric-card dashboard-metric-card-highlight"),
                    new MetricItem("待确认完成", safeLong(overview.getPendingConfirmationCount()), "维修员已完工，等待你确认。", "dashboard-metric-card"),
                    new MetricItem("返修中", safeLong(overview.getReworkInProgressCount()), "已经申请返修并重新进入处理链路。", "dashboard-metric-card"),
                    new MetricItem("即将超时", safeLong(overview.getTimeoutWarningCount()), "已接近时效阈值，建议优先查看。", "dashboard-metric-card"),
                    new MetricItem("已超时", safeLong(overview.getTimeoutOverdueCount()), "已超过默认时效规则。", "dashboard-metric-card dashboard-metric-card-highlight"),
                    new MetricItem("累计报修", safeLong(overview.getTotalRequests()), "你的历史报修总数。", "dashboard-metric-card")
            );
            case ADMIN -> List.of(
                    new MetricItem("待派单", safeLong(overview.getPendingReviewRequests()), "还未完成派单的报修。", "dashboard-metric-card dashboard-metric-card-highlight"),
                    new MetricItem("处理中", safeLong(overview.getActiveWorkOrders()), "已经进入工单推进链路。", "dashboard-metric-card"),
                    new MetricItem("待学生确认", safeLong(overview.getPendingConfirmationCount()), "已完工但还未由学生确认。", "dashboard-metric-card"),
                    new MetricItem("返修中", safeLong(overview.getReworkInProgressCount()), "学生提出异议后重新进入处理。", "dashboard-metric-card"),
                    new MetricItem("即将超时", safeLong(overview.getTimeoutWarningCount()), "接近时效阈值，需提前干预。", "dashboard-metric-card"),
                    new MetricItem("已超时", safeLong(overview.getTimeoutOverdueCount()), "演示时优先展示这部分风险。", "dashboard-metric-card dashboard-metric-card-highlight")
            );
            case WORKER -> List.of(
                    new MetricItem("待接单", safeLong(overview.getPendingReviewRequests()), "已派单但还未接单。", "dashboard-metric-card"),
                    new MetricItem("处理中", safeLong(overview.getActiveWorkOrders()), "仍在推进中的工单。", "dashboard-metric-card dashboard-metric-card-highlight"),
                    new MetricItem("待学生确认", safeLong(overview.getPendingConfirmationCount()), "你已提交完工，等待学生确认。", "dashboard-metric-card"),
                    new MetricItem("返修中", safeLong(overview.getReworkInProgressCount()), "学生申请返修后重新回到你手上的工单。", "dashboard-metric-card"),
                    new MetricItem("即将超时", safeLong(overview.getTimeoutWarningCount()), "需要优先处理的边缘工单。", "dashboard-metric-card"),
                    new MetricItem("已超时", safeLong(overview.getTimeoutOverdueCount()), "必须优先清掉的超时工单。", "dashboard-metric-card dashboard-metric-card-highlight")
            );
        };

        GridPane metricGrid = new GridPane();
        metricGrid.setHgap(16);
        metricGrid.setVgap(16);
        metricGrid.getStyleClass().add("dashboard-metric-row");
        metricGrid.getColumnConstraints().addAll(percentColumn(33.33), percentColumn(33.33), percentColumn(33.34));
        for (int index = 0; index < items.size(); index++) {
            MetricItem item = items.get(index);
            metricGrid.add(createMetricCard(item.tag(), item.value(), item.description(), item.styleClassNames()), index % 3, index / 3);
        }
        return metricGrid;
    }

    private Node buildSummaryPanel(UserRole role, DashboardOverview overview) {
        Label kickerLabel = new Label(summaryKicker());
        kickerLabel.getStyleClass().add("dashboard-kicker");

        Label titleLabel = new Label(summaryHeadline());
        titleLabel.getStyleClass().add("dashboard-summary-title");
        titleLabel.setWrapText(true);

        Label textLabel = new Label(summaryBody(role, overview));
        textLabel.getStyleClass().add("dashboard-summary-text");
        textLabel.setWrapText(true);

        HBox summaryRow = new HBox(
                10,
                createSummaryChip("在途", safeLong(overview.getActiveWorkOrders())),
                createSummaryChip("待确认", safeLong(overview.getPendingConfirmationCount())),
                createSummaryChip("返修中", safeLong(overview.getReworkInProgressCount()))
        );
        summaryRow.getStyleClass().add("dashboard-chip-row");

        HBox timeoutRow = new HBox(
                10,
                buildRiskChip("即将超时", TimeoutChip.create(TimeoutLevel.WARNING, "即将超时"), safeLong(overview.getTimeoutWarningCount())),
                buildRiskChip("已超时", TimeoutChip.create(TimeoutLevel.OVERDUE, "已超时"), safeLong(overview.getTimeoutOverdueCount()))
        );
        timeoutRow.getStyleClass().add("dashboard-chip-row");

        VBox content = new VBox(14, kickerLabel, titleLabel, textLabel, summaryRow, timeoutRow);
        content.getStyleClass().add("dashboard-summary-body");
        return content;
    }

    private Node buildRiskChip(String title, Label chip, long count) {
        Label countLabel = new Label(title + " " + count);
        countLabel.getStyleClass().add("dashboard-risk-count");
        VBox box = new VBox(6, countLabel, chip);
        box.getStyleClass().add("dashboard-risk-box");
        return box;
    }

    private Label createSummaryChip(String label, long value) {
        Label chip = new Label(label + "  " + value);
        chip.getStyleClass().add("dashboard-summary-chip");
        return chip;
    }

    private Node buildQuickActions(UserRole role) {
        VBox content = new VBox(10);
        content.getStyleClass().add("dashboard-action-stack");
        for (DashboardAction action : actionsFor(role)) {
            content.getChildren().add(createActionButton(action));
        }
        return content;
    }

    private Node createActionButton(DashboardAction action) {
        var pane = action.primary()
                ? FusionUiFactory.createPrimaryButton(action.label(), 0, 42, () -> moduleOpener.accept(action.moduleCode()))
                : FusionUiFactory.createGhostButton(action.label(), 0, 42, () -> moduleOpener.accept(action.moduleCode()));
        pane.getNode().getStyleClass().add("dashboard-action-button");
        pane.getNode().setMaxWidth(Double.MAX_VALUE);
        return pane.getNode();
    }

    private List<DashboardAction> actionsFor(UserRole role) {
        return switch (role) {
            case STUDENT -> List.of(
                    new DashboardAction("student-repair", "提交报修", true),
                    new DashboardAction("student-history", "查看进度", false)
            );
            case ADMIN -> List.of(
                    new DashboardAction("admin-dispatch", "去派单", true),
                    new DashboardAction("audit-log", "查看审计", false),
                    new DashboardAction("statistics", "查看统计", false)
            );
            case WORKER -> List.of(
                    new DashboardAction("worker-processing", "开始处理", true)
            );
        };
    }

    private Node buildRecentRepairsTable(UserRole currentRole, List<RecentRepairRequestView> recentRequests) {
        if (currentRole == UserRole.STUDENT) {
            return createStaticDataTableOrEmpty(
                    List.of(
                            staticColumn("报修单号", 1.15, RecentRepairRequestView::getRequestNo),
                            staticColumn("宿舍位置", 1.15, RecentRepairRequestView::getLocationText),
                            staticColumn("当前状态", 1.05, this::composeStatusText),
                            staticColumn("最近更新", 0.95, item -> formatTime(item.getSubmittedAt()))
                    ),
                    recentRequests,
                    6,
                    "暂无记录",
                    "当前还没有可展示的最近报修记录。"
            );
        }
        return createStaticDataTableOrEmpty(
                List.of(
                        staticColumn("报修单号", 1.0, RecentRepairRequestView::getRequestNo),
                        staticColumn("发起人", 0.8, RecentRepairRequestView::getStudentName),
                        staticColumn("宿舍位置", 1.0, RecentRepairRequestView::getLocationText),
                        staticColumn("状态 / 时效", 1.1, this::composeStatusText),
                        staticColumn("最近更新", 0.8, item -> formatTime(item.getSubmittedAt()))
                ),
                recentRequests,
                6,
                "暂无记录",
                "当前还没有可展示的最近动态。"
        );
    }

    private Node buildReminderPanel(UserRole role, DashboardOverview overview, List<RecentRepairRequestView> recentRows) {
        VBox list = new VBox(12,
                createReminderLine(reminderTitlePrimary(role), reminderBodyPrimary(role, overview)),
                createReminderLine(reminderTitleSecondary(role), reminderBodySecondary(role, overview)),
                createReminderLine("最近更新", recentRows.isEmpty()
                        ? "暂无最近动态，可从左侧工作模块直接进入相应操作。"
                        : formatTime(recentRows.get(0).getSubmittedAt()) + " 有一条更高优先级的记录进入视图。")
        );
        list.getStyleClass().add("dashboard-note-list");
        return list;
    }

    private Node createReminderLine(String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dashboard-note-title");

        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("dashboard-note-body");
        bodyLabel.setWrapText(true);

        VBox box = new VBox(4, titleLabel, bodyLabel);
        box.getStyleClass().add("dashboard-note-item");
        return box;
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

    private List<RecentRepairRequestView> prioritizeRecentRows(UserRole role, List<RecentRepairRequestView> rows) {
        List<RecentRepairRequestView> ordered = new ArrayList<>(rows);
        Comparator<RecentRepairRequestView> comparator = Comparator
                .comparingInt((RecentRepairRequestView item) -> timeoutRank(item.getTimeoutLevel()))
                .thenComparingInt(item -> pendingConfirmationRank(role, item))
                .thenComparing(item -> item.getSubmittedAt() == null ? LocalDateTime.MIN : item.getSubmittedAt(), Comparator.reverseOrder())
                .thenComparing(RecentRepairRequestView::getId, Comparator.nullsLast(Comparator.reverseOrder()));
        ordered.sort(comparator);
        return ordered;
    }

    private int timeoutRank(TimeoutLevel level) {
        if (level == TimeoutLevel.OVERDUE) {
            return 0;
        }
        if (level == TimeoutLevel.WARNING) {
            return 1;
        }
        return 2;
    }

    private int pendingConfirmationRank(UserRole role, RecentRepairRequestView item) {
        if (item == null || item.getStatus() == null) {
            return 3;
        }
        if (item.getStatus().name().equals("PENDING_CONFIRMATION")) {
            return 0;
        }
        if (item.getStatus().name().equals("REWORK_IN_PROGRESS")) {
            return 1;
        }
        return role == UserRole.STUDENT ? 2 : 3;
    }

    private String composeStatusText(RecentRepairRequestView item) {
        String status = UiDisplayText.repairRequestStatus(item.getStatus());
        String timeout = UiDisplayText.timeoutLabel(item.getTimeoutLevel(), item.getTimeoutLabel());
        if (timeout == null || timeout.isBlank()) {
            return status;
        }
        return status + " / " + timeout;
    }

    private String moduleLead() {
        return switch (appContext.appSession().getCurrentRole()) {
            case STUDENT -> "首页优先展示待确认完成、返修和超时风险，不再把关键闭环信息埋在详情里。";
            case ADMIN -> "首页优先展示待派单、待学生确认和超时风险，先把最影响演示判断的单据顶上来。";
            case WORKER -> "首页优先展示待接单、返修和超时工单，避免重要单据被普通记录淹没。";
        };
    }

    private String summaryPanelTitle() {
        return switch (appContext.appSession().getCurrentRole()) {
            case STUDENT -> "我的报修概览";
            case ADMIN -> "当前处理摘要";
            case WORKER -> "手上工单概览";
        };
    }

    private String summaryKicker() {
        return switch (appContext.appSession().getCurrentRole()) {
            case STUDENT -> "学生视角";
            case ADMIN -> "管理视角";
            case WORKER -> "维修视角";
        };
    }

    private String summaryHeadline() {
        return switch (appContext.appSession().getCurrentRole()) {
            case STUDENT -> "先看还有哪些报修没有真正闭环";
            case ADMIN -> "先判断今天最该处理的是哪一类风险";
            case WORKER -> "先把超时单、返修单和待确认单挑出来";
        };
    }

    private String summaryBody(UserRole role, DashboardOverview overview) {
        return switch (role) {
            case STUDENT -> "你累计提交了 " + safeLong(overview.getTotalRequests()) + " 条报修，目前处理中 " + safeLong(overview.getActiveWorkOrders())
                    + " 条，待你确认完成 " + safeLong(overview.getPendingConfirmationCount()) + " 条。";
            case ADMIN -> "当前待派单 " + safeLong(overview.getPendingReviewRequests()) + " 条，待学生确认 " + safeLong(overview.getPendingConfirmationCount())
                    + " 条，已超时 " + safeLong(overview.getTimeoutOverdueCount()) + " 条。";
            case WORKER -> "你当前还有 " + safeLong(overview.getActiveWorkOrders()) + " 条在途工单，其中返修中 "
                    + safeLong(overview.getReworkInProgressCount()) + " 条，待学生确认 " + safeLong(overview.getPendingConfirmationCount()) + " 条。";
        };
    }

    private String reminderTitlePrimary(UserRole role) {
        return switch (role) {
            case STUDENT -> "闭环重点";
            case ADMIN -> "派单重点";
            case WORKER -> "处理重点";
        };
    }

    private String reminderBodyPrimary(UserRole role, DashboardOverview overview) {
        return switch (role) {
            case STUDENT -> safeLong(overview.getPendingConfirmationCount()) == 0
                    ? "当前没有待你确认完成的报修，可优先关注仍在处理中或返修中的记录。"
                    : "当前有 " + safeLong(overview.getPendingConfirmationCount()) + " 条报修等待你确认完成，建议先查看完工说明和凭证。";
            case ADMIN -> safeLong(overview.getTimeoutOverdueCount()) == 0
                    ? "当前没有超时工单，但仍有 " + safeLong(overview.getPendingReviewRequests()) + " 条待派单记录需要继续清理。"
                    : "当前已有 " + safeLong(overview.getTimeoutOverdueCount()) + " 条超时记录，演示时应优先从这些单据切入。";
            case WORKER -> safeLong(overview.getTimeoutOverdueCount()) == 0
                    ? "当前没有超时工单，可继续优先推进待学生确认和返修中的单据。"
                    : "当前已有 " + safeLong(overview.getTimeoutOverdueCount()) + " 条超时工单，建议先从维修处理页顶部列表进入。";
        };
    }

    private String reminderTitleSecondary(UserRole role) {
        return switch (role) {
            case STUDENT -> "返修提醒";
            case ADMIN -> "闭环提醒";
            case WORKER -> "完工提醒";
        };
    }

    private String reminderBodySecondary(UserRole role, DashboardOverview overview) {
        return switch (role) {
            case STUDENT -> safeLong(overview.getReworkInProgressCount()) == 0
                    ? "当前没有返修中的记录。"
                    : "当前有 " + safeLong(overview.getReworkInProgressCount()) + " 条返修中的记录，历史链路会继续保留在原单内。";
            case ADMIN -> safeLong(overview.getPendingConfirmationCount()) == 0
                    ? "当前没有待学生确认的记录，可继续查看审计或统计定位问题。"
                    : "当前有 " + safeLong(overview.getPendingConfirmationCount()) + " 条记录停在待学生确认阶段，这部分最适合演示闭环。";
            case WORKER -> safeLong(overview.getPendingConfirmationCount()) == 0
                    ? "当前没有待学生确认的工单。"
                    : "当前有 " + safeLong(overview.getPendingConfirmationCount()) + " 条工单已提交完工，可回看处理说明和完工凭证。";
        };
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? "--" : TIME_FORMATTER.format(value);
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private record DashboardAction(String moduleCode, String label, boolean primary) {
    }

    private record MetricItem(String tag, long value, String description, String styleClassNames) {
    }
}