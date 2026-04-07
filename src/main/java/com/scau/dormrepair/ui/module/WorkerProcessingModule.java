package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.command.UpdateWorkOrderStatusCommand;
import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.enums.TimeoutLevel;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import com.scau.dormrepair.domain.view.WorkOrderDetailView;
import com.scau.dormrepair.ui.component.AppDropdown;
import com.scau.dormrepair.ui.component.EvidenceGallery;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.component.StatusChip;
import com.scau.dormrepair.ui.component.TimeoutChip;
import com.scau.dormrepair.ui.support.ProjectImageStore;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.support.UiDisplayText;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.beans.binding.Bindings;
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
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class WorkerProcessingModule extends AbstractWorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public WorkerProcessingModule(AppContext appContext) {
        super(appContext);
    }

    @Override
    public String moduleCode() {
        return "worker-processing";
    }

    @Override
    public String moduleName() {
        return "维修处理";
    }

    @Override
    public String moduleDescription() {
        return "";
    }

    @Override
    public Set<UserRole> supportedRoles() {
        return EnumSet.of(UserRole.WORKER);
    }

    @Override
    public boolean cacheViewOnSwitch() {
        return true;
    }

    @Override
    public Parent createView() {
        UserAccount currentWorker = appContext.userAccountService().requireCurrentAccount(appContext.appSession(), UserRole.WORKER);

        TextField keywordField = createSearchField("搜索工单号、报修单号、报修人或宿舍位置");
        AppDropdown<StatusFilterOption> statusFilterBox = new AppDropdown<>();
        statusFilterBox.setItems(List.of(StatusFilterOption.values()));
        statusFilterBox.setTextMapper(StatusFilterOption::label);
        statusFilterBox.setPromptText("筛选工单状态");
        statusFilterBox.setVisibleRowCount(8);
        statusFilterBox.setValue(StatusFilterOption.ALL);

        AppDropdown<WorkOrderStatus> statusBox = new AppDropdown<>();
        statusBox.setTextMapper(UiDisplayText::workOrderStatus);
        statusBox.setPromptText("选择下一状态");
        statusBox.setVisibleRowCount(5);

        TextArea recordNoteArea = new TextArea();
        recordNoteArea.setPromptText("填写处理说明；如准备提交待学生确认，请写清完工说明。");
        recordNoteArea.setPrefRowCount(4);
        recordNoteArea.setWrapText(true);

        List<File> completionImageFiles = new ArrayList<>();
        Label completionCountLabel = new Label("已选 0 / 3");
        completionCountLabel.getStyleClass().add("plain-text");
        VBox completionPreviewBox = new VBox(8);
        completionPreviewBox.getStyleClass().add("upload-preview-box");
        completionPreviewBox.setFillWidth(true);
        refreshCompletionPreview(completionPreviewBox, completionImageFiles, completionCountLabel);

        ObservableList<WorkOrderDetailView> sourceRows = FXCollections.observableArrayList();
        ObjectProperty<WorkOrderDetailView> trackedDetail = new SimpleObjectProperty<>();
        VBox trackedTableContainer = buildTableContainer();

        Runnable applyFilters = () -> applyTrackedFilters(sourceRows, keywordField.getText(), statusFilterBox.getValue(), trackedTableContainer, trackedDetail, currentWorker.getId());
        Runnable refreshTracked = () -> reloadTrackedOrders(sourceRows, keywordField.getText(), statusFilterBox.getValue(), trackedTableContainer, trackedDetail, currentWorker.getId());

        keywordField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters.run());
        statusFilterBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters.run());
        refreshTracked.run();

        GridPane contentGrid = createRatioWorkspace(
                40,
                60,
                buildStatusForm(currentWorker, trackedDetail, statusBox, recordNoteArea, completionImageFiles, completionCountLabel, completionPreviewBox, refreshTracked),
                buildTrackedListPanel(keywordField, statusFilterBox, trackedTableContainer, refreshTracked)
        );
        return createPage(moduleName(), "超时、待学生确认和当前处理中工单会优先显示在这里。", contentGrid);
    }

    private Node buildStatusForm(UserAccount currentWorker, ObjectProperty<WorkOrderDetailView> trackedDetail, AppDropdown<WorkOrderStatus> statusBox, TextArea recordNoteArea, List<File> completionImageFiles, Label completionCountLabel, VBox completionPreviewBox, Runnable refreshTracked) {
        Label workOrderNoLabel = new Label("未选择工单");
        workOrderNoLabel.getStyleClass().add("dashboard-spotlight-value");

        VBox statusContainer = new VBox();
        statusContainer.setFillWidth(true);

        Label requestInfoLabel = new Label("请先从右侧列表中选择一条工单。");
        requestInfoLabel.getStyleClass().add("dashboard-mini-title");
        requestInfoLabel.setWrapText(true);

        Label locationLabel = new Label("选中后会在这里显示宿舍位置、故障类别和派单说明。");
        locationLabel.getStyleClass().add("dashboard-mini-description");
        locationLabel.setWrapText(true);

        Label noteLabel = plainLabel("--");
        Label completionSummaryLabel = plainLabel("当前暂无完工说明或凭证。");
        Label feedbackLabel = plainLabel("当前暂无学生评价。");

        EvidenceGallery existingCompletionGallery = new EvidenceGallery("当前暂无已存完工凭证。");
        VBox timelineContainer = new VBox(10);
        timelineContainer.setFillWidth(true);
        timelineContainer.getChildren().setAll(createTimelineList(List.of(), "暂无处理记录", "工单产生处理记录后，会显示在这里。"));

        trackedDetail.addListener((observable, oldValue, detail) -> {
            recordNoteArea.clear();
            completionImageFiles.clear();
            refreshCompletionPreview(completionPreviewBox, completionImageFiles, completionCountLabel);
            if (detail == null) {
                workOrderNoLabel.setText("未选择工单");
                statusContainer.getChildren().setAll(helperLabel("请先从右侧列表中选择一条工单。"));
                requestInfoLabel.setText("请先从右侧列表中选择一条工单。");
                locationLabel.setText("选中后会在这里显示宿舍位置、故障类别和派单说明。");
                noteLabel.setText("--");
                completionSummaryLabel.setText("当前暂无完工说明或凭证。");
                feedbackLabel.setText("当前暂无学生评价。");
                existingCompletionGallery.setImages(List.of(), "当前暂无已存完工凭证。");
                timelineContainer.getChildren().setAll(createTimelineList(List.of(), "暂无处理记录", "工单产生处理记录后，会显示在这里。"));
                refreshStatusChoices(statusBox, null);
                return;
            }
            workOrderNoLabel.setText(placeholderText(detail.getWorkOrderNo()));
            statusContainer.getChildren().setAll(buildStatusRows(detail));
            requestInfoLabel.setText(placeholderText(detail.getStudentName()) + " / " + placeholderText(detail.getRequestNo()));
            locationLabel.setText(placeholderText(detail.getLocationText()) + " / " + UiDisplayText.faultCategory(detail.getFaultCategory()));
            noteLabel.setText(placeholderText(detail.getAssignmentNote()));
            completionSummaryLabel.setText(composeCompletionSummary(detail));
            feedbackLabel.setText(buildFeedbackSummary(detail));
            existingCompletionGallery.setImages(detail.getCompletionImageUrls(), "当前暂无已存完工凭证。");
            timelineContainer.getChildren().setAll(createTimelineList(detail.getRecords(), "暂无处理记录", "工单产生处理记录后，会显示在这里。"));
            refreshStatusChoices(statusBox, detail.getStatus());
        });

        statusBox.disableProperty().bind(Bindings.createBooleanBinding(() -> trackedDetail.get() == null || statusBox.getItems().isEmpty(), trackedDetail, statusBox.valueProperty()));
        recordNoteArea.disableProperty().bind(trackedDetail.isNull());

        Button chooseProofButton = new Button("选择完工凭证");
        chooseProofButton.getStyleClass().add("surface-button");
        chooseProofButton.disableProperty().bind(Bindings.createBooleanBinding(() -> trackedDetail.get() == null || statusBox.getValue() != WorkOrderStatus.WAITING_CONFIRMATION, trackedDetail, statusBox.valueProperty()));
        chooseProofButton.setOnAction(event -> chooseProofImages(chooseProofButton, completionImageFiles, completionPreviewBox, completionCountLabel));

        Button clearProofButton = new Button("清空待上传");
        clearProofButton.getStyleClass().add("surface-button");
        clearProofButton.disableProperty().bind(trackedDetail.isNull());
        clearProofButton.setOnAction(event -> {
            completionImageFiles.clear();
            refreshCompletionPreview(completionPreviewBox, completionImageFiles, completionCountLabel);
        });

        Button refreshButton = createFilterActionButton("刷新列表", refreshTracked);

        Node updateButton = FusionUiFactory.createPrimaryButton("更新处理状态", 170, 40, () -> {
            try {
                WorkOrderDetailView selected = trackedDetail.get();
                if (selected == null) {
                    throw new IllegalArgumentException("请先选择一条工单。");
                }
                if (statusBox.getValue() == null) {
                    throw new IllegalArgumentException("请选择下一状态。");
                }
                List<String> storedProofs = List.of();
                String completionNote = null;
                if (statusBox.getValue() == WorkOrderStatus.WAITING_CONFIRMATION) {
                    ProjectImageStore.validateImageFiles(completionImageFiles);
                    if (completionImageFiles.size() > 3) {
                        throw new IllegalArgumentException("完工凭证最多只能上传 3 张。");
                    }
                    storedProofs = ProjectImageStore.copyImagesToProject(completionImageFiles);
                    completionNote = recordNoteArea.getText();
                }
                UpdateWorkOrderStatusCommand command = new UpdateWorkOrderStatusCommand(
                        selected.getId(),
                        currentWorker.getId(),
                        statusBox.getValue(),
                        recordNoteArea.getText(),
                        completionNote,
                        storedProofs
                );
                appContext.workOrderService().updateStatus(command);
                recordNoteArea.clear();
                completionImageFiles.clear();
                refreshCompletionPreview(completionPreviewBox, completionImageFiles, completionCountLabel);
                refreshTracked.run();
                UiAlerts.info("更新成功", "工单状态已更新。");
            } catch (RuntimeException exception) {
                UiAlerts.error("更新失败", exception.getMessage());
            }
        }).getNode();
        updateButton.disableProperty().bind(Bindings.createBooleanBinding(() -> trackedDetail.get() == null || statusBox.getItems().isEmpty(), trackedDetail, statusBox.valueProperty()));

        VBox summaryBox = new VBox(8, workOrderNoLabel, requestInfoLabel, locationLabel);
        summaryBox.getStyleClass().add("dashboard-spotlight-body");

        HBox proofToolbar = new HBox(12, chooseProofButton, clearProofButton);
        proofToolbar.setAlignment(Pos.CENTER_LEFT);

        HBox actionRow = new HBox(12, refreshButton, updateButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        if (updateButton instanceof Region updateRegion) {
            updateRegion.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(updateRegion, Priority.ALWAYS);
        }

        VBox formBody = new VBox(
                18,
                createInlineSummaryCard("当前选择", summaryBox, "dashboard-mini-card", "dashboard-mini-soft"),
                statusContainer,
                createFieldBlock("派单说明", noteLabel),
                createFieldBlock("完工说明", completionSummaryLabel),
                createFieldBlock("学生评价", feedbackLabel),
                createFieldBlock("已存凭证", existingCompletionGallery),
                createFieldBlock("下一状态", statusBox),
                createFieldBlock("处理说明", recordNoteArea),
                createFieldBlock("待上传凭证", completionCountLabel),
                proofToolbar,
                completionPreviewBox,
                actionRow,
                createFieldBlock("处理时间线", timelineContainer)
        );
        formBody.setFillWidth(true);
        return wrapPanel("处理操作", formBody);
    }

    private void chooseProofImages(Node ownerNode, List<File> completionImageFiles, VBox completionPreviewBox, Label completionCountLabel) {
        List<File> chosenFiles = chooseImageFiles(ownerNode);
        if (chosenFiles.isEmpty()) {
            return;
        }
        for (File chosenFile : chosenFiles) {
            boolean exists = completionImageFiles.stream().anyMatch(existingFile -> existingFile.toPath().equals(chosenFile.toPath()));
            if (!exists) {
                completionImageFiles.add(chosenFile);
            }
        }
        try {
            ProjectImageStore.validateImageFiles(completionImageFiles);
            if (completionImageFiles.size() > 3) {
                throw new IllegalStateException("完工凭证最多只能上传 3 张。");
            }
            refreshCompletionPreview(completionPreviewBox, completionImageFiles, completionCountLabel);
        } catch (RuntimeException exception) {
            completionImageFiles.removeIf(file -> chosenFiles.stream().anyMatch(chosen -> chosen.toPath().equals(file.toPath())));
            refreshCompletionPreview(completionPreviewBox, completionImageFiles, completionCountLabel);
            UiAlerts.error("选择凭证失败", exception.getMessage());
        }
    }

    private Node buildTrackedListPanel(TextField keywordField, AppDropdown<StatusFilterOption> statusFilterBox, VBox trackedTableContainer, Runnable refreshTracked) {
        Button refreshButton = createFilterActionButton("刷新列表", refreshTracked);
        keywordField.setMaxWidth(Double.MAX_VALUE);
        keywordField.setMinWidth(0);
        statusFilterBox.setMaxWidth(Double.MAX_VALUE);
        statusFilterBox.setMinWidth(0);

        GridPane filterGrid = createFilterGrid(58, 24, 18);
        filterGrid.add(createFieldLabel("关键词筛选"), 0, 0);
        filterGrid.add(createFieldLabel("状态筛选"), 1, 0);
        filterGrid.add(keywordField, 0, 1);
        filterGrid.add(statusFilterBox, 1, 1);
        filterGrid.add(refreshButton, 2, 1);
        GridPane.setValignment(refreshButton, VPos.TOP);
        GridPane.setHgrow(keywordField, Priority.ALWAYS);
        GridPane.setHgrow(statusFilterBox, Priority.ALWAYS);

        VBox content = new VBox(14, filterGrid, trackedTableContainer);
        content.setFillWidth(true);
        VBox.setVgrow(trackedTableContainer, Priority.ALWAYS);
        return wrapPanel("我的工单", content);
    }

    private VBox buildTableContainer() {
        VBox container = new VBox();
        container.setFillWidth(true);
        container.setMaxWidth(Double.MAX_VALUE);
        container.setMinWidth(0);
        VBox.setVgrow(container, Priority.ALWAYS);
        return container;
    }

    private void reloadTrackedOrders(ObservableList<WorkOrderDetailView> sourceRows, String keyword, StatusFilterOption statusFilter, VBox trackedTableContainer, ObjectProperty<WorkOrderDetailView> trackedDetail, Long workerId) {
        List<WorkOrderDetailView> rows = appContext.workOrderService().listWorkerTrackedWorkOrders(workerId, 30);
        sourceRows.setAll(rows);
        applyTrackedFilters(sourceRows, keyword, statusFilter, trackedTableContainer, trackedDetail, workerId);
    }

    private void applyTrackedFilters(ObservableList<WorkOrderDetailView> sourceRows, String keyword, StatusFilterOption statusFilter, VBox trackedTableContainer, ObjectProperty<WorkOrderDetailView> trackedDetail, Long workerId) {
        String normalizedKeyword = normalizeKeyword(keyword);
        WorkOrderStatus targetStatus = statusFilter == null ? null : statusFilter.status();
        Long selectedId = trackedDetail.get() == null ? null : trackedDetail.get().getId();
        List<WorkOrderDetailView> visibleRows = sourceRows.stream()
                .filter(item -> matchesKeyword(normalizedKeyword, item.getWorkOrderNo(), item.getRequestNo(), item.getStudentName(), item.getLocationText(), UiDisplayText.faultCategory(item.getFaultCategory()), UiDisplayText.workOrderStatus(item.getStatus()), item.getAssignmentNote(), item.getTimeoutLabel(), item.getCompletionNote()))
                .filter(item -> targetStatus == null || targetStatus == item.getStatus())
                .sorted(Comparator
                        .comparingInt((WorkOrderDetailView item) -> timeoutRank(item.getTimeoutLevel()))
                        .thenComparingInt(item -> item.getStatus() == WorkOrderStatus.WAITING_CONFIRMATION ? 0 : item.getStatus() == WorkOrderStatus.IN_PROGRESS ? 1 : 2)
                        .thenComparing(item -> item.getAssignedAt() == null ? LocalDateTime.MIN : item.getAssignedAt(), Comparator.reverseOrder()))
                .toList();
        WorkOrderDetailView restoredSelection = null;
        if (selectedId != null) {
            WorkOrderDetailView restoredSummary = visibleRows.stream().filter(item -> selectedId.equals(item.getId())).findFirst().orElse(null);
            if (restoredSummary != null) {
                restoredSelection = appContext.workOrderService().getWorkerWorkOrderDetail(workerId, restoredSummary.getId());
            }
        }
        trackedDetail.set(restoredSelection);
        renderTrackedTable(trackedTableContainer, visibleRows, trackedDetail, workerId);
    }

    private void renderTrackedTable(VBox trackedTableContainer, List<WorkOrderDetailView> visibleRows, ObjectProperty<WorkOrderDetailView> trackedDetail, Long workerId) {
        trackedTableContainer.getChildren().clear();
        if (visibleRows.isEmpty()) {
            trackedTableContainer.getChildren().add(createEmptyState("当前没有工单", "已派给你的工单和历史工单会显示在这里。"));
            return;
        }
        GridPane table = new GridPane();
        table.getStyleClass().add("selectable-static-grid-table");
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMinWidth(0);
        table.getColumnConstraints().addAll(percentColumn(20), percentColumn(16), percentColumn(16), percentColumn(20), percentColumn(28));

        addCell(table, 0, 0, "工单号", true, false, true, false, null);
        addCell(table, 0, 1, "状态", true, false, false, false, null);
        addCell(table, 0, 2, "学生", true, false, false, false, null);
        addCell(table, 0, 3, "宿舍位置", true, false, false, false, null);
        addCell(table, 0, 4, "故障 / 时效", true, false, false, true, null);

        Long selectedId = trackedDetail.get() == null ? null : trackedDetail.get().getId();
        int visibleCount = Math.max(6, visibleRows.size());
        for (int index = 0; index < visibleCount; index++) {
            int rowIndex = index + 1;
            WorkOrderDetailView rowItem = index < visibleRows.size() ? visibleRows.get(index) : null;
            boolean selected = rowItem != null && selectedId != null && selectedId.equals(rowItem.getId());
            Runnable clickAction = rowItem == null ? null : () -> {
                trackedDetail.set(appContext.workOrderService().getWorkerWorkOrderDetail(workerId, rowItem.getId()));
                renderTrackedTable(trackedTableContainer, visibleRows, trackedDetail, workerId);
            };
            addCell(table, rowIndex, 0, rowItem == null ? "" : safeText(rowItem.getWorkOrderNo()), false, selected, false, false, clickAction);
            addCell(table, rowIndex, 1, rowItem == null ? "" : UiDisplayText.workOrderStatus(rowItem.getStatus()), false, selected, false, false, clickAction);
            addCell(table, rowIndex, 2, rowItem == null ? "" : safeText(rowItem.getStudentName()), false, selected, false, false, clickAction);
            addCell(table, rowIndex, 3, rowItem == null ? "" : placeholderText(rowItem.getLocationText()), false, selected, false, false, clickAction);
            addCell(table, rowIndex, 4, rowItem == null ? "" : composeFaultSummary(rowItem), false, selected, false, true, clickAction);
        }
        trackedTableContainer.getChildren().add(table);
    }

    private VBox buildStatusRows(WorkOrderDetailView detail) {
        HBox chipRow = new HBox(10, StatusChip.workOrder(detail.getStatus()), TimeoutChip.create(detail.getTimeoutLevel(), detail.getTimeoutLabel()));
        chipRow.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(8, chipRow, helperLabel(UiDisplayText.workOrderPriority(detail.getPriority()) + " / 派单时间：" + formatTime(detail.getAssignedAt())));
        box.setFillWidth(true);
        return box;
    }

    private void addCell(GridPane table, int rowIndex, int columnIndex, String text, boolean headerCell, boolean selected, boolean firstHeaderCell, boolean lastCell, Runnable clickAction) {
        Label label = new Label(text == null ? "" : text);
        label.getStyleClass().add(headerCell ? "static-table-header-cell" : "static-table-cell");
        label.setWrapText(!headerCell && columnIndex == 4);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinWidth(0);

        HBox cellShell = new HBox(label);
        cellShell.setAlignment(Pos.CENTER);
        cellShell.setMaxWidth(Double.MAX_VALUE);
        cellShell.setMinWidth(0);
        cellShell.setPrefHeight(columnIndex == 4 && !headerCell ? 58 : 48);
        cellShell.setMinHeight(columnIndex == 4 && !headerCell ? 58 : 48);
        cellShell.setMaxHeight(columnIndex == 4 && !headerCell ? 58 : 48);
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

    private void refreshStatusChoices(AppDropdown<WorkOrderStatus> statusBox, WorkOrderStatus currentStatus) {
        List<WorkOrderStatus> items = allowedEditableStatuses(currentStatus);
        statusBox.setItems(items);
        if (items.isEmpty()) {
            statusBox.clearSelection();
            return;
        }
        statusBox.setValue(items.get(0));
    }

    private List<WorkOrderStatus> allowedEditableStatuses(WorkOrderStatus currentStatus) {
        if (currentStatus == null) {
            return List.of();
        }
        return switch (currentStatus) {
            case ASSIGNED -> List.of(WorkOrderStatus.ACCEPTED, WorkOrderStatus.REJECTED, WorkOrderStatus.CANCELLED);
            case ACCEPTED -> List.of(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.WAITING_PARTS, WorkOrderStatus.CANCELLED);
            case IN_PROGRESS -> List.of(WorkOrderStatus.WAITING_PARTS, WorkOrderStatus.WAITING_CONFIRMATION, WorkOrderStatus.CANCELLED);
            case WAITING_PARTS -> List.of(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.WAITING_CONFIRMATION, WorkOrderStatus.CANCELLED);
            case WAITING_CONFIRMATION, COMPLETED, REJECTED, CANCELLED -> List.of();
        };
    }

    private TextField createSearchField(String promptText) {
        TextField field = new TextField();
        field.getStyleClass().addAll("text-field", "login-input");
        field.setPromptText(promptText);
        return field;
    }

    private List<File> chooseImageFiles(Node ownerNode) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择完工凭证图片");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.webp", "*.bmp", "*.gif"));
        Window ownerWindow = ownerNode.getScene() == null ? null : ownerNode.getScene().getWindow();
        List<File> selectedFiles = chooser.showOpenMultipleDialog(ownerWindow);
        return selectedFiles == null || selectedFiles.isEmpty() ? List.of() : selectedFiles;
    }

    private void refreshCompletionPreview(VBox previewBox, List<File> completionImageFiles, Label completionCountLabel) {
        previewBox.getChildren().clear();
        completionCountLabel.setText("已选 " + completionImageFiles.size() + " / 3");
        if (completionImageFiles.isEmpty()) {
            Label emptyLabel = new Label("进入待学生确认前，请先上传 1 到 3 张完工凭证图片。");
            emptyLabel.getStyleClass().add("helper-text");
            emptyLabel.setWrapText(true);
            previewBox.getChildren().add(emptyLabel);
            return;
        }
        for (int index = 0; index < completionImageFiles.size(); index++) {
            Label itemLabel = new Label((index + 1) + ". " + completionImageFiles.get(index).getName());
            itemLabel.getStyleClass().add("upload-preview-item");
            itemLabel.setWrapText(true);
            previewBox.getChildren().add(itemLabel);
        }
    }

    private String composeFaultSummary(WorkOrderDetailView workOrder) {
        String timeout = workOrder.getTimeoutLabel() == null || workOrder.getTimeoutLabel().isBlank() ? "时效正常" : workOrder.getTimeoutLabel();
        return UiDisplayText.faultCategory(workOrder.getFaultCategory()) + " / " + timeout;
    }

    private String composeCompletionSummary(WorkOrderDetailView detail) {
        if (detail == null) {
            return "当前暂无完工说明或凭证。";
        }
        String note = detail.getCompletionNote();
        int imageCount = detail.getCompletionImageUrls() == null ? 0 : detail.getCompletionImageUrls().size();
        if ((note == null || note.isBlank()) && imageCount == 0) {
            return "当前暂无完工说明或凭证。";
        }
        StringBuilder builder = new StringBuilder();
        if (detail.getCompletedAt() != null) {
            builder.append("完工时间：").append(formatTime(detail.getCompletedAt()));
        }
        if (note != null && !note.isBlank()) {
            if (builder.length() > 0) {
                builder.append(" / ");
            }
            builder.append("处理说明：").append(note.trim());
        }
        if (imageCount > 0) {
            if (builder.length() > 0) {
                builder.append(" / ");
            }
            builder.append("完工凭证 ").append(imageCount).append(" 张");
        }
        return builder.toString();
    }

    private String buildFeedbackSummary(WorkOrderDetailView detail) {
        if (detail == null || !detail.hasFeedback()) {
            return "当前暂无学生评价。";
        }
        StringBuilder builder = new StringBuilder("已评分 ").append(detail.getFeedbackRating());
        if (Boolean.TRUE.equals(detail.getFeedbackAnonymousFlag())) {
            builder.append(" / 匿名评价");
        }
        if (detail.getFeedbackComment() != null && !detail.getFeedbackComment().isBlank()) {
            builder.append(" / ").append(detail.getFeedbackComment().trim());
        }
        return builder.toString();
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

    private Label plainLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("plain-text");
        label.setWrapText(true);
        return label;
    }

    private String safeText(String value) {
        return value == null ? "" : value;
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

    private enum StatusFilterOption {
        ALL("全部状态", null),
        ASSIGNED("已派单", WorkOrderStatus.ASSIGNED),
        ACCEPTED("已受理", WorkOrderStatus.ACCEPTED),
        IN_PROGRESS("处理中", WorkOrderStatus.IN_PROGRESS),
        WAITING_PARTS("待配件", WorkOrderStatus.WAITING_PARTS),
        WAITING_CONFIRMATION("待学生确认", WorkOrderStatus.WAITING_CONFIRMATION),
        COMPLETED("已处理", WorkOrderStatus.COMPLETED),
        REJECTED("已驳回", WorkOrderStatus.REJECTED),
        CANCELLED("已取消", WorkOrderStatus.CANCELLED);

        private final String label;
        private final WorkOrderStatus status;

        StatusFilterOption(String label, WorkOrderStatus status) {
            this.label = label;
            this.status = status;
        }

        public String label() {
            return label;
        }

        public WorkOrderStatus status() {
            return status;
        }
    }
}
