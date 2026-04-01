package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.command.AssignWorkOrderCommand;
import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.TimeoutLevel;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.enums.WorkOrderPriority;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.WorkOrderDetailView;
import com.scau.dormrepair.domain.view.WorkOrderRecordView;
import com.scau.dormrepair.ui.component.AppDropdown;
import com.scau.dormrepair.ui.component.EvidenceGallery;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.component.StatusChip;
import com.scau.dormrepair.ui.component.TimeoutChip;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.support.UiDisplayText;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AdminDispatchModule extends AbstractWorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AdminDispatchModule(AppContext appContext) {
        super(appContext);
    }

    @Override
    public String moduleCode() {
        return "admin-dispatch";
    }

    @Override
    public String moduleName() {
        return "管理员派单";
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
    public boolean cacheViewOnSwitch() {
        return true;
    }

    @Override
    public Parent createView() {
        UserAccount currentAdmin = appContext.userAccountService().requireCurrentAccount(appContext.appSession(), UserRole.ADMIN);

        TextField keywordField = createSearchField("搜索报修单号、学生或宿舍位置");

        AppDropdown<FaultFilterOption> faultFilterBox = new AppDropdown<>();
        faultFilterBox.setItems(List.of(FaultFilterOption.values()));
        faultFilterBox.setTextMapper(FaultFilterOption::label);
        faultFilterBox.setPromptText("筛选故障类别");
        faultFilterBox.setVisibleRowCount(8);
        faultFilterBox.setValue(FaultFilterOption.ALL);

        ObservableList<RecentRepairRequestView> pendingRows = FXCollections.observableArrayList();
        ObjectProperty<RecentRepairRequestView> selectedRequest = new SimpleObjectProperty<>();
        VBox pendingTableContainer = buildTableContainer();

        ObjectProperty<WorkOrderDetailView> trackedDetail = new SimpleObjectProperty<>();
        ObservableList<WorkOrderDetailView> trackedRows = FXCollections.observableArrayList();
        VBox trackedTableContainer = buildTableContainer();

        Runnable applyPendingFilters = () -> applyPendingFilters(
                pendingRows,
                keywordField.getText(),
                faultFilterBox.getValue(),
                pendingTableContainer,
                selectedRequest
        );
        Runnable refreshPending = () -> reloadPendingRequests(
                pendingRows,
                keywordField.getText(),
                faultFilterBox.getValue(),
                pendingTableContainer,
                selectedRequest
        );
        Runnable refreshTracked = () -> reloadTrackedOrders(trackedRows, trackedTableContainer, trackedDetail);
        Runnable refreshAll = () -> {
            refreshPending.run();
            refreshTracked.run();
        };

        keywordField.textProperty().addListener((observable, oldValue, newValue) -> applyPendingFilters.run());
        faultFilterBox.valueProperty().addListener((observable, oldValue, newValue) -> applyPendingFilters.run());
        refreshAll.run();

        VBox leftContent = new VBox(
                18,
                buildDispatchForm(currentAdmin, selectedRequest, refreshAll),
                buildTrackedDetailPanel(trackedDetail)
        );
        leftContent.setFillWidth(true);

        VBox rightContent = new VBox(
                18,
                buildPendingListPanel(keywordField, faultFilterBox, pendingTableContainer, refreshPending),
                buildTrackedListPanel(trackedTableContainer, refreshTracked)
        );
        rightContent.setFillWidth(true);

        GridPane contentGrid = createRatioWorkspace(40, 60, leftContent, rightContent);
        return createPage(moduleName(), "待派单、超时风险、待学生确认和完工凭证都会集中显示在这里。", contentGrid);
    }

    private Node buildDispatchForm(UserAccount currentAdmin, ObjectProperty<RecentRepairRequestView> selectedRequest, Runnable refreshAll) {
        Label selectedRequestNoLabel = new Label("未选择报修单");
        selectedRequestNoLabel.getStyleClass().add("dashboard-spotlight-value");

        Label selectedLocationLabel = new Label("请先从右侧列表中选择一条待派单报修。");
        selectedLocationLabel.getStyleClass().add("dashboard-mini-title");
        selectedLocationLabel.setWrapText(true);

        Label selectedRequestInfoLabel = new Label("选中后会在这里显示报修人、故障类别和时效提示。");
        selectedRequestInfoLabel.getStyleClass().add("dashboard-mini-description");
        selectedRequestInfoLabel.setWrapText(true);

        AppDropdown<UserAccount> workerBox = new AppDropdown<>();
        workerBox.setItems(appContext.userAccountService().listEnabledAccountsByRole(UserRole.WORKER));
        workerBox.setTextMapper(UserAccount::getDisplayName);
        workerBox.setPromptText("选择维修员");
        workerBox.setVisibleRowCount(8);

        AppDropdown<WorkOrderPriority> priorityBox = new AppDropdown<>();
        priorityBox.setItems(List.of(WorkOrderPriority.values()));
        priorityBox.setTextMapper(UiDisplayText::workOrderPriority);
        priorityBox.setPromptText("选择优先级");
        priorityBox.setVisibleRowCount(5);
        priorityBox.setValue(WorkOrderPriority.NORMAL);

        TextArea assignmentNoteArea = new TextArea();
        assignmentNoteArea.setPromptText("写清派单说明、现场注意事项和优先处理要求。");
        assignmentNoteArea.setPrefRowCount(5);
        assignmentNoteArea.setWrapText(true);

        selectedRequest.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                selectedRequestNoLabel.setText("未选择报修单");
                selectedLocationLabel.setText("请先从右侧列表中选择一条待派单报修。");
                selectedRequestInfoLabel.setText("选中后会在这里显示报修人、故障类别和时效提示。");
                return;
            }
            selectedRequestNoLabel.setText(newValue.getRequestNo());
            selectedLocationLabel.setText(newValue.getLocationText());
            selectedRequestInfoLabel.setText(
                    newValue.getStudentName()
                            + " / "
                            + UiDisplayText.faultCategory(newValue.getFaultCategory())
                            + (newValue.getTimeoutLabel() == null || newValue.getTimeoutLabel().isBlank() ? "" : " / " + newValue.getTimeoutLabel())
            );
        });

        Button refreshButton = createFilterActionButton("刷新待派单", refreshAll);

        Node assignButton = FusionUiFactory.createPrimaryButton("创建工单", 170, 40, () -> {
            try {
                RecentRepairRequestView request = selectedRequest.get();
                if (request == null) {
                    throw new IllegalArgumentException("请先选择一条待派单报修");
                }
                if (workerBox.getValue() == null) {
                    throw new IllegalArgumentException("请选择维修员");
                }
                if (priorityBox.getValue() == null) {
                    throw new IllegalArgumentException("请选择优先级");
                }
                AssignWorkOrderCommand command = new AssignWorkOrderCommand(
                        request.getId(),
                        currentAdmin.getId(),
                        workerBox.getValue().getId(),
                        priorityBox.getValue(),
                        assignmentNoteArea.getText()
                );
                appContext.workOrderService().assign(command);
                assignmentNoteArea.clear();
                workerBox.clearSelection();
                priorityBox.setValue(WorkOrderPriority.NORMAL);
                selectedRequest.set(null);
                refreshAll.run();
                UiAlerts.info("派单成功", "工单已创建，并进入工单追踪区。");
            } catch (RuntimeException exception) {
                UiAlerts.error("派单失败", exception.getMessage());
            }
        }).getNode();

        VBox summaryBox = new VBox(8, selectedRequestNoLabel, selectedLocationLabel, selectedRequestInfoLabel);
        summaryBox.getStyleClass().add("dashboard-spotlight-body");

        HBox actionRow = new HBox(12, refreshButton, assignButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        if (assignButton instanceof Region assignRegion) {
            assignRegion.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(assignRegion, Priority.ALWAYS);
        }

        VBox formBody = new VBox(
                18,
                createInlineSummaryCard("当前选择", summaryBox, "dashboard-mini-card", "dashboard-mini-soft"),
                createFieldBlock("维修员", workerBox),
                createFieldBlock("优先级", priorityBox),
                createFieldBlock("派单说明", assignmentNoteArea),
                actionRow
        );
        formBody.setFillWidth(true);
        return wrapPanel("派单操作", formBody);
    }

    private Node buildTrackedDetailPanel(ObjectProperty<WorkOrderDetailView> trackedDetail) {
        Label workOrderNoLabel = new Label("未选择工单");
        workOrderNoLabel.getStyleClass().add("dashboard-mini-value");
        workOrderNoLabel.setWrapText(true);

        VBox statusBox = new VBox();
        statusBox.setFillWidth(true);

        Label requestInfoLabel = new Label("从右下方工单追踪区选择一条工单后，这里会显示闭环详情、完工凭证和返修原因。");
        requestInfoLabel.getStyleClass().add("dashboard-mini-description");
        requestInfoLabel.setWrapText(true);

        Label workerLabel = new Label("--");
        workerLabel.getStyleClass().add("plain-text");
        workerLabel.setWrapText(true);

        Label locationLabel = new Label("--");
        locationLabel.getStyleClass().add("plain-text");
        locationLabel.setWrapText(true);

        Label noteLabel = new Label("--");
        noteLabel.getStyleClass().add("plain-text");
        noteLabel.setWrapText(true);

        Label completionLabel = new Label("当前暂无完工说明或凭证");
        completionLabel.getStyleClass().add("plain-text");
        completionLabel.setWrapText(true);

        Label reworkLabel = new Label("当前没有返修记录");
        reworkLabel.getStyleClass().add("plain-text");
        reworkLabel.setWrapText(true);

        Label feedbackLabel = new Label("当前暂无评价");
        feedbackLabel.getStyleClass().add("plain-text");
        feedbackLabel.setWrapText(true);

        EvidenceGallery completionGallery = new EvidenceGallery("当前暂无完工凭证。");
        VBox timelineContainer = new VBox(10);
        timelineContainer.setFillWidth(true);
        timelineContainer.getChildren().setAll(
                createTimelineList(List.of(), "暂无处理记录", "工单产生处理记录后，会显示在这里。")
        );

        trackedDetail.addListener((observable, oldValue, detail) -> {
            if (detail == null) {
                workOrderNoLabel.setText("未选择工单");
                statusBox.getChildren().setAll(helperLabel("请选择一条工单查看详情。"));
                requestInfoLabel.setText("从右下方工单追踪区选择一条工单后，这里会显示闭环详情、完工凭证和返修原因。");
                workerLabel.setText("--");
                locationLabel.setText("--");
                noteLabel.setText("--");
                completionLabel.setText("当前暂无完工说明或凭证");
                reworkLabel.setText("当前没有返修记录");
                feedbackLabel.setText("当前暂无评价");
                completionGallery.setImages(List.of(), "当前暂无完工凭证。");
                timelineContainer.getChildren().setAll(createTimelineList(List.of(), "暂无处理记录", "工单产生处理记录后，会显示在这里。"));
                return;
            }
            workOrderNoLabel.setText(detail.getWorkOrderNo());
            statusBox.getChildren().setAll(buildStatusRows(detail));
            requestInfoLabel.setText(placeholderText(detail.getRequestNo()) + " / " + placeholderText(detail.getStudentName()));
            workerLabel.setText(placeholderText(detail.getWorkerName()));
            locationLabel.setText(placeholderText(detail.getLocationText()));
            noteLabel.setText(placeholderText(detail.getAssignmentNote()));
            completionLabel.setText(buildCompletionSummary(detail));
            reworkLabel.setText(buildReworkSummary(detail.getRecords()));
            feedbackLabel.setText(buildFeedbackSummary(detail));
            completionGallery.setImages(detail.getCompletionImageUrls(), "当前暂无完工凭证。");
            timelineContainer.getChildren().setAll(createTimelineList(detail.getRecords(), "暂无处理记录", "工单产生处理记录后，会显示在这里。"));
        });

        VBox content = new VBox(
                14,
                workOrderNoLabel,
                statusBox,
                requestInfoLabel,
                createFieldBlock("维修员", workerLabel),
                createFieldBlock("宿舍位置", locationLabel),
                createFieldBlock("派单说明", noteLabel),
                createFieldBlock("完工说明", completionLabel),
                createFieldBlock("完工凭证", completionGallery),
                createFieldBlock("返修情况", reworkLabel),
                createFieldBlock("评价回看", feedbackLabel),
                createFieldBlock("处理时间线", timelineContainer)
        );
        content.setFillWidth(true);
        return wrapPanel("工单详情回看", content);
    }

    private Node buildPendingListPanel(TextField keywordField, AppDropdown<FaultFilterOption> faultFilterBox, VBox pendingTableContainer, Runnable refreshPending) {
        Button refreshButton = createFilterActionButton("刷新列表", refreshPending);
        keywordField.setMaxWidth(Double.MAX_VALUE);
        keywordField.setMinWidth(0);
        faultFilterBox.setMaxWidth(Double.MAX_VALUE);
        faultFilterBox.setMinWidth(0);

        GridPane filterGrid = createFilterGrid(45, 35, 20);
        filterGrid.add(createFieldLabel("关键词筛选"), 0, 0);
        filterGrid.add(createFieldLabel("故障筛选"), 1, 0);
        filterGrid.add(keywordField, 0, 1);
        filterGrid.add(faultFilterBox, 1, 1);
        filterGrid.add(refreshButton, 2, 1);
        GridPane.setValignment(refreshButton, VPos.TOP);
        GridPane.setHgrow(keywordField, Priority.ALWAYS);
        GridPane.setHgrow(faultFilterBox, Priority.ALWAYS);

        VBox content = new VBox(14, filterGrid, pendingTableContainer);
        content.setFillWidth(true);
        VBox.setVgrow(pendingTableContainer, Priority.ALWAYS);
        return wrapPanel("待派单列表", content);
    }

    private Node buildTrackedListPanel(VBox trackedTableContainer, Runnable refreshTracked) {
        Button refreshButton = createFilterActionButton("刷新追踪", refreshTracked);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(12, spacer, refreshButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(12, toolbar, trackedTableContainer);
        content.setFillWidth(true);
        VBox.setVgrow(trackedTableContainer, Priority.ALWAYS);
        return wrapPanel("工单追踪", content);
    }

    private VBox buildTableContainer() {
        VBox container = new VBox();
        container.setFillWidth(true);
        container.setMaxWidth(Double.MAX_VALUE);
        container.setMinWidth(0);
        VBox.setVgrow(container, Priority.ALWAYS);
        return container;
    }

    private void reloadPendingRequests(ObservableList<RecentRepairRequestView> sourceRows, String keyword, FaultFilterOption faultFilter, VBox pendingTableContainer, ObjectProperty<RecentRepairRequestView> selectedRequest) {
        List<RecentRepairRequestView> rows = appContext.repairRequestService().listPendingAssignmentRequests(20);
        sourceRows.setAll(rows);
        applyPendingFilters(sourceRows, keyword, faultFilter, pendingTableContainer, selectedRequest);
    }

    private void applyPendingFilters(ObservableList<RecentRepairRequestView> sourceRows, String keyword, FaultFilterOption faultFilter, VBox pendingTableContainer, ObjectProperty<RecentRepairRequestView> selectedRequest) {
        String normalizedKeyword = normalizeKeyword(keyword);
        FaultCategory targetCategory = faultFilter == null ? null : faultFilter.category();
        Long selectedId = selectedRequest.get() == null ? null : selectedRequest.get().getId();
        List<RecentRepairRequestView> visibleRows = sourceRows.stream()
                .filter(item -> matchesKeyword(normalizedKeyword, item.getRequestNo(), item.getStudentName(), item.getLocationText(), UiDisplayText.faultCategory(item.getFaultCategory()), item.getTimeoutLabel()))
                .filter(item -> targetCategory == null || targetCategory == item.getFaultCategory())
                .sorted(Comparator
                        .comparingInt((RecentRepairRequestView item) -> timeoutRank(item.getTimeoutLevel()))
                        .thenComparing(item -> item.getSubmittedAt() == null ? LocalDateTime.MIN : item.getSubmittedAt(), Comparator.reverseOrder()))
                .toList();
        RecentRepairRequestView restoredSelection = selectedId == null ? null : visibleRows.stream().filter(item -> selectedId.equals(item.getId())).findFirst().orElse(null);
        selectedRequest.set(restoredSelection);
        renderPendingTable(pendingTableContainer, visibleRows, selectedRequest);
    }

    private void reloadTrackedOrders(ObservableList<WorkOrderDetailView> sourceRows, VBox trackedTableContainer, ObjectProperty<WorkOrderDetailView> trackedDetail) {
        Long selectedId = trackedDetail.get() == null ? null : trackedDetail.get().getId();
        List<WorkOrderDetailView> rows = appContext.workOrderService().listAdminTrackedWorkOrders(30);
        sourceRows.setAll(rows);
        WorkOrderDetailView restored = null;
        if (selectedId != null) {
            restored = rows.stream().filter(item -> selectedId.equals(item.getId())).findFirst().orElse(null);
        }
        trackedDetail.set(restored == null ? null : appContext.workOrderService().getAdminWorkOrderDetail(restored.getId()));
        renderTrackedTable(trackedTableContainer, rows, trackedDetail);
    }

    private void renderPendingTable(VBox pendingTableContainer, List<RecentRepairRequestView> visibleRows, ObjectProperty<RecentRepairRequestView> selectedRequest) {
        pendingTableContainer.getChildren().clear();
        if (visibleRows.isEmpty()) {
            pendingTableContainer.getChildren().add(createEmptyState("当前没有待派单报修", "有新的待处理报修后，会显示在这里。"));
            return;
        }
        GridPane table = new GridPane();
        table.getStyleClass().add("selectable-static-grid-table");
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMinWidth(0);
        table.getColumnConstraints().addAll(percentColumn(24), percentColumn(18), percentColumn(28), percentColumn(30));

        addCell(table, 0, 0, "报修单号", true, false, true, false, null);
        addCell(table, 0, 1, "学生", true, false, false, false, null);
        addCell(table, 0, 2, "宿舍位置", true, false, false, false, null);
        addCell(table, 0, 3, "故障 / 时效", true, false, false, true, null);

        int visibleCount = Math.max(6, visibleRows.size());
        for (int index = 0; index < visibleCount; index++) {
            int rowIndex = index + 1;
            RecentRepairRequestView rowItem = index < visibleRows.size() ? visibleRows.get(index) : null;
            boolean isSelected = rowItem != null && selectedRequest.get() != null && selectedRequest.get().getId().equals(rowItem.getId());
            Runnable clickAction = rowItem == null ? null : () -> {
                selectedRequest.set(rowItem);
                renderPendingTable(pendingTableContainer, visibleRows, selectedRequest);
            };
            addCell(table, rowIndex, 0, rowItem == null ? "" : safeText(rowItem.getRequestNo()), false, isSelected, false, false, clickAction);
            addCell(table, rowIndex, 1, rowItem == null ? "" : safeText(rowItem.getStudentName()), false, isSelected, false, false, clickAction);
            addCell(table, rowIndex, 2, rowItem == null ? "" : safeText(rowItem.getLocationText()), false, isSelected, false, false, clickAction);
            addCell(table, rowIndex, 3, rowItem == null ? "" : composePendingSummary(rowItem), false, isSelected, false, true, clickAction);
        }
        pendingTableContainer.getChildren().add(table);
    }

    private void renderTrackedTable(VBox trackedTableContainer, List<WorkOrderDetailView> visibleRows, ObjectProperty<WorkOrderDetailView> trackedDetail) {
        trackedTableContainer.getChildren().clear();
        if (visibleRows.isEmpty()) {
            trackedTableContainer.getChildren().add(createEmptyState("当前没有工单追踪记录", "创建工单后，最近工单会显示在这里。"));
            return;
        }
        List<WorkOrderDetailView> orderedRows = visibleRows.stream()
                .sorted(Comparator
                        .comparingInt((WorkOrderDetailView item) -> timeoutRank(item.getTimeoutLevel()))
                        .thenComparingInt(item -> item.getStatus() == WorkOrderStatus.WAITING_CONFIRMATION ? 0 : item.getStatus() == WorkOrderStatus.IN_PROGRESS ? 1 : 2)
                        .thenComparing(item -> item.getAssignedAt() == null ? LocalDateTime.MIN : item.getAssignedAt(), Comparator.reverseOrder()))
                .toList();

        GridPane table = new GridPane();
        table.getStyleClass().add("selectable-static-grid-table");
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMinWidth(0);
        table.getColumnConstraints().addAll(percentColumn(20), percentColumn(16), percentColumn(16), percentColumn(20), percentColumn(28));

        addCell(table, 0, 0, "工单号", true, false, true, false, null);
        addCell(table, 0, 1, "维修员", true, false, false, false, null);
        addCell(table, 0, 2, "状态", true, false, false, false, null);
        addCell(table, 0, 3, "学生", true, false, false, false, null);
        addCell(table, 0, 4, "位置 / 时效", true, false, false, true, null);

        Long selectedId = trackedDetail.get() == null ? null : trackedDetail.get().getId();
        int visibleCount = Math.max(6, orderedRows.size());
        for (int index = 0; index < visibleCount; index++) {
            int rowIndex = index + 1;
            WorkOrderDetailView rowItem = index < orderedRows.size() ? orderedRows.get(index) : null;
            boolean isSelected = rowItem != null && selectedId != null && selectedId.equals(rowItem.getId());
            Runnable clickAction = rowItem == null ? null : () -> {
                trackedDetail.set(appContext.workOrderService().getAdminWorkOrderDetail(rowItem.getId()));
                renderTrackedTable(trackedTableContainer, orderedRows, trackedDetail);
            };
            addCell(table, rowIndex, 0, rowItem == null ? "" : safeText(rowItem.getWorkOrderNo()), false, isSelected, false, false, clickAction);
            addCell(table, rowIndex, 1, rowItem == null ? "" : safeText(rowItem.getWorkerName()), false, isSelected, false, false, clickAction);
            addCell(table, rowIndex, 2, rowItem == null ? "" : UiDisplayText.workOrderStatus(rowItem.getStatus()), false, isSelected, false, false, clickAction);
            addCell(table, rowIndex, 3, rowItem == null ? "" : safeText(rowItem.getStudentName()), false, isSelected, false, false, clickAction);
            addCell(table, rowIndex, 4, rowItem == null ? "" : composeTrackedSummary(rowItem), false, isSelected, false, true, clickAction);
        }
        trackedTableContainer.getChildren().add(table);
    }

    private VBox buildStatusRows(WorkOrderDetailView detail) {
        HBox chipRow = new HBox(10,
                StatusChip.workOrder(detail.getStatus()),
                TimeoutChip.create(detail.getTimeoutLevel(), detail.getTimeoutLabel())
        );
        chipRow.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(8,
                chipRow,
                helperLabel(UiDisplayText.workOrderPriority(detail.getPriority()) + " / 派单时间：" + formatTime(detail.getAssignedAt()))
        );
        box.setFillWidth(true);
        return box;
    }

    private String composePendingSummary(RecentRepairRequestView item) {
        String timeout = item.getTimeoutLabel() == null || item.getTimeoutLabel().isBlank() ? "时效正常" : item.getTimeoutLabel();
        return UiDisplayText.faultCategory(item.getFaultCategory()) + " / " + timeout;
    }

    private String composeTrackedSummary(WorkOrderDetailView item) {
        String timeout = item.getTimeoutLabel() == null || item.getTimeoutLabel().isBlank() ? "时效正常" : item.getTimeoutLabel();
        return placeholderText(item.getLocationText()) + " / " + timeout;
    }

    private String buildCompletionSummary(WorkOrderDetailView detail) {
        if (detail == null) {
            return "当前暂无完工说明或凭证";
        }
        String note = trimToNull(detail.getCompletionNote());
        int imageCount = detail.getCompletionImageUrls() == null ? 0 : detail.getCompletionImageUrls().size();
        if (note == null && imageCount == 0) {
            return "当前暂无完工说明或凭证";
        }
        StringBuilder builder = new StringBuilder();
        if (detail.getCompletedAt() != null) {
            builder.append("完工时间：").append(formatTime(detail.getCompletedAt()));
        }
        if (note != null) {
            if (builder.length() > 0) {
                builder.append(" / ");
            }
            builder.append("处理说明：").append(note);
        }
        if (imageCount > 0) {
            if (builder.length() > 0) {
                builder.append(" / ");
            }
            builder.append("完工凭证 ").append(imageCount).append(" 张");
        }
        return builder.toString();
    }

    private String buildReworkSummary(List<WorkOrderRecordView> records) {
        List<WorkOrderRecordView> reworkRecords = records == null ? List.of() : records.stream()
                .filter(item -> item.getStatus() == WorkOrderStatus.IN_PROGRESS)
                .filter(item -> item.getRecordNote() != null && item.getRecordNote().contains("返修"))
                .toList();
        if (reworkRecords.isEmpty()) {
            return "当前没有返修记录";
        }
        WorkOrderRecordView latest = reworkRecords.get(reworkRecords.size() - 1);
        return "返修 " + reworkRecords.size() + " 次 / 最近原因：" + placeholderText(latest.getRecordNote());
    }

    private String buildFeedbackSummary(WorkOrderDetailView detail) {
        if (detail == null || !detail.hasFeedback()) {
            return "当前暂无评价";
        }
        StringBuilder builder = new StringBuilder("已评分 ")
                .append(detail.getFeedbackRating())
                .append(" 分");
        if (Boolean.TRUE.equals(detail.getFeedbackAnonymousFlag())) {
            builder.append(" / 匿名评价");
        }
        if (detail.getFeedbackComment() != null && !detail.getFeedbackComment().isBlank()) {
            builder.append(" / ").append(detail.getFeedbackComment().trim());
        }
        return builder.toString();
    }

    private void addCell(GridPane table, int rowIndex, int columnIndex, String text, boolean headerCell, boolean selected, boolean firstHeaderCell, boolean lastCell, Runnable clickAction) {
        Label label = new Label(text == null ? "" : text);
        label.getStyleClass().add(headerCell ? "static-table-header-cell" : "static-table-cell");
        label.setWrapText(!headerCell && columnIndex >= 3);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinWidth(0);

        HBox cellShell = new HBox(label);
        cellShell.setAlignment(Pos.CENTER);
        cellShell.setMaxWidth(Double.MAX_VALUE);
        cellShell.setMinWidth(0);
        cellShell.setPrefHeight(columnIndex >= 3 && !headerCell ? 58 : 48);
        cellShell.setMinHeight(columnIndex >= 3 && !headerCell ? 58 : 48);
        cellShell.setMaxHeight(columnIndex >= 3 && !headerCell ? 58 : 48);
        cellShell.getStyleClass().add(headerCell ? "selectable-static-grid-header-shell" : "selectable-static-grid-cell-shell");
        if (firstHeaderCell) {
            cellShell.getStyleClass().add("selectable-static-grid-header-first");
        }
        if (lastCell) {
            cellShell.getStyleClass().add(headerCell ? "selectable-static-grid-header-last" : "selectable-static-grid-cell-last");
        }
        if (selected && !headerCell) {
            cellShell.getStyleClass().add("selectable-static-grid-cell-selected");
        }
        if (clickAction != null) {
            cellShell.getStyleClass().add("selectable-static-grid-clickable");
            cellShell.setOnMouseClicked(event -> clickAction.run());
        }
        table.add(cellShell, columnIndex, rowIndex);
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

    private TextField createSearchField(String promptText) {
        TextField field = new TextField();
        field.getStyleClass().addAll("text-field", "login-input");
        field.setPromptText(promptText);
        return field;
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? "--" : TIME_FORMATTER.format(value);
    }

    private Label helperLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("helper-text");
        label.setWrapText(true);
        return label;
    }

    private enum FaultFilterOption {
        ALL("全部类别", null),
        ELECTRICITY("电路电器", FaultCategory.ELECTRICITY),
        WATER_PIPE("水管水龙头", FaultCategory.WATER_PIPE),
        DOOR_WINDOW("门窗锁具", FaultCategory.DOOR_WINDOW),
        NETWORK("网络故障", FaultCategory.NETWORK),
        FURNITURE("家具设施", FaultCategory.FURNITURE),
        PUBLIC_AREA("公共区域", FaultCategory.PUBLIC_AREA),
        OTHER("其他问题", FaultCategory.OTHER);

        private final String label;
        private final FaultCategory category;

        FaultFilterOption(String label, FaultCategory category) {
            this.label = label;
            this.category = category;
        }

        public String label() {
            return label;
        }

        public FaultCategory category() {
            return category;
        }
    }
}