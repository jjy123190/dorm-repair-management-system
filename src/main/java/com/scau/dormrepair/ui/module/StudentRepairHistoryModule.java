package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.command.SubmitRepairFeedbackCommand;
import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.enums.WorkOrderStatus;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.StudentRepairDetailView;
import com.scau.dormrepair.domain.view.WorkOrderRecordView;
import com.scau.dormrepair.exception.BusinessException;
import com.scau.dormrepair.ui.component.AppDropdown;
import com.scau.dormrepair.ui.component.EvidenceGallery;
import com.scau.dormrepair.ui.component.StatusChip;
import com.scau.dormrepair.ui.component.TimeoutChip;
import com.scau.dormrepair.ui.support.ProjectImageStore;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.support.UiMotion;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class StudentRepairHistoryModule extends AbstractWorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StudentRepairHistoryModule(AppContext appContext) {
        super(appContext);
    }

    @Override
    public String moduleCode() {
        return "student-history";
    }

    @Override
    public String moduleName() {
        return "报修记录";
    }

    @Override
    public String moduleDescription() {
        return "";
    }

    @Override
    public Set<UserRole> supportedRoles() {
        return EnumSet.of(UserRole.STUDENT);
    }

    @Override
    public boolean cacheViewOnSwitch() {
        return true;
    }

    @Override
    public Parent createView() {
        UserAccount currentStudent = appContext.userAccountService()
                .requireCurrentAccount(appContext.appSession(), UserRole.STUDENT);

        VBox historyList = new VBox(10);
        historyList.getStyleClass().add("student-history-list");
        historyList.setFillWidth(true);

        ScrollPane historyScroll = new ScrollPane(historyList);
        historyScroll.setFitToWidth(true);
        historyScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        historyScroll.setPrefHeight(520);
        historyScroll.getStyleClass().add("student-history-scroll");
        UiMotion.installSmoothScrollPane(historyScroll);

        DetailState state = createState();
        VBox detailBox = buildDetailBox(currentStudent, state);
        ScrollPane detailScroll = new ScrollPane(detailBox);
        detailScroll.setFitToWidth(true);
        detailScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        detailScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        detailScroll.getStyleClass().add("student-detail-scroll");
        detailScroll.setPannable(true);
        UiMotion.installSmoothScrollPane(detailScroll);

        HistoryState historyState = new HistoryState(historyList);
        state.historyState = historyState;

        Button refreshButton = new Button("刷新记录");
        refreshButton.getStyleClass().add("surface-button");
        refreshButton.setOnAction(event -> refreshHistory(historyState, state, currentStudent));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(12, spacer, refreshButton);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        VBox historyBody = new VBox(12, toolbar, historyScroll);
        historyBody.setFillWidth(true);
        VBox.setVgrow(historyScroll, Priority.ALWAYS);

        GridPane workspace = createRatioWorkspace(
                34,
                66,
                wrapPanel("本人报修记录", historyBody),
                wrapPanel("记录详情", detailScroll)
        );
        workspace.setMaxHeight(Double.MAX_VALUE);

        refreshHistory(historyState, state, currentStudent);
        return createPage("学生报修记录", "待确认完成、返修原因、完工说明和完工凭证会优先显示在详情顶部。", workspace);
    }

    private DetailState createState() {
        DetailState state = new DetailState();
        state.statusContainer = new VBox();
        state.statusContainer.setFillWidth(true);
        state.requestNoValue = valueLabel("无");
        state.locationValue = valueLabel("无");
        state.phoneValue = valueLabel("无");
        state.categoryValue = valueLabel("无");
        state.submittedAtValue = valueLabel("无");
        state.completedAtValue = valueLabel("无");
        state.urgeCountValue = valueLabel("0");
        state.reworkSummaryValue = valueLabel("当前没有返修记录");
        state.descriptionValue = valueLabel("无");
        state.completionSummaryValue = valueLabel("当前暂无完工说明");
        state.feedbackSummaryValue = valueLabel("当前阶段无需评价");
        state.requestGallery = new EvidenceGallery("当前没有报修附件。");
        state.completionGallery = new EvidenceGallery("当前暂无完工凭证。");
        state.actionContainer = new VBox(10);
        state.actionContainer.setFillWidth(true);
        state.timelineContainer = new VBox(10);
        state.timelineContainer.setFillWidth(true);
        state.feedbackContainer = new VBox(10);
        state.feedbackContainer.setFillWidth(true);
        state.ratingBox = new AppDropdown<>();
        state.ratingBox.setItems(List.of(5, 4, 3, 2, 1));
        state.ratingBox.setTextMapper(score -> score == null ? "" : score + " 分");
        state.ratingBox.setPromptText("请选择 1-5 分");
        state.ratingBox.setVisibleRowCount(5);
        state.feedbackArea = new TextArea();
        state.feedbackArea.setPromptText("可以补充维修体验或处理感受。");
        state.feedbackArea.setWrapText(true);
        state.feedbackArea.setPrefRowCount(3);
        state.anonymousBox = new CheckBox("匿名评价");
        state.reworkArea = new TextArea();
        state.reworkArea.setPromptText("请填写申请返修的原因或异议说明。");
        state.reworkArea.setWrapText(true);
        state.reworkArea.setPrefRowCount(3);
        state.urgeButton = new Button("催办一次");
        state.cancelButton = new Button("取消报修");
        state.confirmButton = new Button("确认完成");
        state.reworkButton = new Button("申请返修");
        state.feedbackButton = new Button("提交评价");
        state.pendingImageFiles = new ArrayList<>();
        state.appendImageCountLabel = helperLabel("已选 0 / " + ProjectImageStore.MAX_IMAGE_COUNT + " 张");
        state.appendImagePreview = new VBox(8);
        state.appendImagePreview.getStyleClass().add("upload-preview-box");
        state.appendImagePreview.setFillWidth(true);
        state.appendImageBox = createAppendImageBox(state);
        state.urgeButton.getStyleClass().add("surface-button");
        state.cancelButton.getStyleClass().add("surface-button");
        state.confirmButton.getStyleClass().add("surface-button");
        state.reworkButton.getStyleClass().add("surface-button");
        state.feedbackButton.getStyleClass().add("surface-button");
        return state;
    }

    private VBox buildDetailBox(UserAccount currentStudent, DetailState state) {
        state.feedbackButton.setOnAction(event -> submitFeedback(currentStudent, state));
        state.urgeButton.setOnAction(event -> urge(currentStudent, state));
        state.cancelButton.setOnAction(event -> cancel(currentStudent, state));
        state.confirmButton.setOnAction(event -> confirmCompletion(currentStudent, state));
        state.reworkButton.setOnAction(event -> requestRework(currentStudent, state));

        VBox detail = new VBox(
                14,
                createFieldBlock("当前状态", state.statusContainer),
                createFieldBlock("完工说明", state.completionSummaryValue),
                createFieldBlock("完工凭证", state.completionGallery),
                createFieldBlock("返修情况", state.reworkSummaryValue),
                createFieldBlock("报修单号", state.requestNoValue),
                createFieldBlock("宿舍位置", state.locationValue),
                createFieldBlock("联系电话", state.phoneValue),
                createFieldBlock("故障类别", state.categoryValue),
                createFieldBlock("提交时间", state.submittedAtValue),
                createFieldBlock("完成时间", state.completedAtValue),
                createFieldBlock("催办次数", state.urgeCountValue),
                createFieldBlock("故障描述", state.descriptionValue),
                createFieldBlock("报修附件", state.requestGallery),
                createFieldBlock("补充图片", state.appendImageBox),
                createFieldBlock("学生操作", state.actionContainer),
                createFieldBlock("处理时间线", state.timelineContainer),
                createFieldBlock("评价", state.feedbackContainer)
        );
        detail.setFillWidth(true);
        detail.setMinWidth(0);
        detail.setMaxWidth(Double.MAX_VALUE);
        clearDetail(state);
        return detail;
    }

    private void refreshHistory(HistoryState historyState, DetailState state, UserAccount currentStudent) {
        List<RecentRepairRequestView> rows = appContext.repairRequestService()
                .listStudentSubmittedRequests(currentStudent.getId(), 20);
        historyState.container.getChildren().clear();
        historyState.selectedCard = null;
        if (rows.isEmpty()) {
            historyState.container.getChildren().add(helperLabel("暂无报修记录。"));
            historyState.selectedRequestId = null;
            clearDetail(state);
            return;
        }
        Long preferredId = historyState.selectedRequestId;
        RecentRepairRequestView preferredRow = null;
        StackPane preferredCard = null;
        for (RecentRepairRequestView row : rows) {
            StackPane card = createHistoryCard(row);
            card.setOnMouseClicked(event -> {
                applySelection(historyState, card, row.getId());
                loadDetail(state, currentStudent, row.getId());
            });
            historyState.container.getChildren().add(card);
            if (preferredId != null && preferredId.equals(row.getId())) {
                preferredRow = row;
                preferredCard = card;
            }
        }
        if (preferredRow == null) {
            preferredRow = rows.get(0);
            preferredCard = (StackPane) historyState.container.getChildren().get(0);
        }
        applySelection(historyState, preferredCard, preferredRow.getId());
        loadDetail(state, currentStudent, preferredRow.getId());
    }

    private StackPane createHistoryCard(RecentRepairRequestView row) {
        Label title = new Label(safe(row.getLocationText()).isBlank() ? "未补全位置" : safe(row.getLocationText()));
        title.getStyleClass().add("student-history-card-title");
        title.setWrapText(true);

        Label request = new Label(summarizeRequestNo(row.getRequestNo()));
        request.getStyleClass().add("student-history-card-request");
        request.setManaged(!request.getText().isBlank());
        request.setVisible(!request.getText().isBlank());

        Label status = new Label(statusText(row.getStatus()));
        status.getStyleClass().addAll("student-history-card-chip", historyStatusStyle(row.getStatus()));
        String timeout = row.getTimeoutLabel() == null || row.getTimeoutLabel().isBlank() ? "" : " / " + row.getTimeoutLabel();
        Label time = new Label(formatTime(row.getSubmittedAt()) + timeout);
        time.getStyleClass().add("student-history-card-meta");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox meta = new HBox(10, status, spacer, time);
        meta.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(8, title, request, meta);
        body.getStyleClass().add("student-history-card-body");
        StackPane card = new StackPane(body);
        card.getStyleClass().add("student-history-card");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private void applySelection(HistoryState historyState, StackPane card, Long requestId) {
        if (historyState.selectedCard != null) {
            historyState.selectedCard.getStyleClass().remove("student-history-card-active");
        }
        historyState.selectedCard = card;
        historyState.selectedRequestId = requestId;
        if (card != null && !card.getStyleClass().contains("student-history-card-active")) {
            card.getStyleClass().add("student-history-card-active");
        }
    }

    private void loadDetail(DetailState state, UserAccount currentStudent, Long requestId) {
        if (requestId == null) {
            clearDetail(state);
            return;
        }
        try {
            state.currentDetail = appContext.repairRequestService().getStudentRequestDetail(currentStudent.getId(), requestId);
            renderDetail(state, state.currentDetail);
        } catch (RuntimeException exception) {
            state.currentDetail = null;
            clearDetail(state);
            UiAlerts.error("加载失败", exception.getMessage());
        }
    }

    private void renderDetail(DetailState state, StudentRepairDetailView detail) {
        renderStatusSection(state, detail);
        state.requestNoValue.setText(placeholder(detail.getRequestNo()));
        state.locationValue.setText(placeholder(detail.getLocationText()));
        state.phoneValue.setText(placeholder(detail.getContactPhone()));
        state.categoryValue.setText(categoryText(detail.getFaultCategory()));
        state.submittedAtValue.setText(placeholder(formatTime(detail.getSubmittedAt())));
        state.completedAtValue.setText(placeholder(formatTime(detail.getCompletedAt())));
        state.urgeCountValue.setText(String.valueOf(detail.getUrgeCount() == null ? 0 : detail.getUrgeCount()));
        state.reworkSummaryValue.setText(buildReworkSummary(detail.getRecords()));
        state.descriptionValue.setText(placeholder(detail.getDescription()));
        state.completionSummaryValue.setText(buildCompletionSummary(detail));
        state.requestGallery.setImages(detail.getImageUrls(), "当前没有报修附件。");
        state.completionGallery.setImages(detail.getCompletionImageUrls(), "当前暂无完工凭证。");
        state.ratingBox.clearSelection();
        state.feedbackArea.clear();
        state.anonymousBox.setSelected(false);
        state.pendingImageFiles.clear();
        refreshPendingImagePreview(state);
        renderAppendImageSection(state, detail.getStatus());
        renderActionSection(state, detail.getStatus());
        renderTimelineSection(state, detail.getRecords());
        renderFeedbackSection(state, detail);
    }

    private void renderStatusSection(DetailState state, StudentRepairDetailView detail) {
        state.statusContainer.getChildren().clear();
        HBox row = new HBox(10,
                StatusChip.repairRequest(detail.getStatus()),
                TimeoutChip.create(detail.getTimeoutLevel(), detail.getTimeoutLabel())
        );
        row.setAlignment(Pos.CENTER_LEFT);
        state.statusContainer.getChildren().addAll(
                row,
                helperLabel(detail.getWorkerName() == null || detail.getWorkerName().isBlank()
                        ? "当前还没有明确的维修员信息。"
                        : "当前维修员：" + detail.getWorkerName() + (detail.getWorkOrderNo() == null || detail.getWorkOrderNo().isBlank() ? "" : " / 工单号 " + detail.getWorkOrderNo()))
        );
    }

    private VBox createAppendImageBox(DetailState state) {
        Label helper = helperLabel("提交后还能继续补图，最多保留 5 张。已结束报修会自动关闭这个入口。");

        Button chooseButton = new Button("选择补充图片");
        Button clearButton = new Button("清空待传");
        Button uploadButton = new Button("提交补图");
        chooseButton.getStyleClass().add("surface-button");
        clearButton.getStyleClass().add("surface-button");
        uploadButton.getStyleClass().add("surface-button");

        chooseButton.setOnAction(event -> {
            List<File> selectedFiles = chooseImageFiles(chooseButton);
            if (selectedFiles.isEmpty()) {
                return;
            }
            for (File file : selectedFiles) {
                boolean exists = state.pendingImageFiles.stream()
                        .anyMatch(existing -> existing.toPath().equals(file.toPath()));
                if (!exists) {
                    state.pendingImageFiles.add(file);
                }
            }
            try {
                ProjectImageStore.validateImageFiles(state.pendingImageFiles);
                refreshPendingImagePreview(state);
            } catch (RuntimeException exception) {
                state.pendingImageFiles.removeIf(file ->
                        selectedFiles.stream().anyMatch(selected -> selected.toPath().equals(file.toPath()))
                );
                refreshPendingImagePreview(state);
                UiAlerts.error("图片校验失败", exception.getMessage());
            }
        });

        clearButton.setOnAction(event -> {
            state.pendingImageFiles.clear();
            refreshPendingImagePreview(state);
        });

        uploadButton.setOnAction(event -> appendImages(state));

        HBox toolbar = new HBox(12, chooseButton, clearButton, uploadButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, helper, state.appendImageCountLabel, toolbar, state.appendImagePreview);
        box.setFillWidth(true);
        refreshPendingImagePreview(state);
        return box;
    }

    private void refreshPendingImagePreview(DetailState state) {
        state.appendImagePreview.getChildren().clear();
        state.appendImageCountLabel.setText("已选 " + state.pendingImageFiles.size() + " / " + ProjectImageStore.MAX_IMAGE_COUNT + " 张");
        if (state.pendingImageFiles.isEmpty()) {
            state.appendImagePreview.getChildren().add(helperLabel("当前没有待补充的图片。"));
            return;
        }
        for (int index = 0; index < state.pendingImageFiles.size(); index++) {
            Label label = new Label((index + 1) + ". " + state.pendingImageFiles.get(index).getName());
            label.getStyleClass().add("upload-preview-item");
            label.setWrapText(true);
            state.appendImagePreview.getChildren().add(label);
        }
    }

    private List<File> chooseImageFiles(Node ownerNode) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择补充图片");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.webp", "*.bmp", "*.gif")
        );
        Window ownerWindow = ownerNode.getScene() == null ? null : ownerNode.getScene().getWindow();
        List<File> selectedFiles = chooser.showOpenMultipleDialog(ownerWindow);
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return List.of();
        }
        return selectedFiles;
    }

    private void appendImages(DetailState state) {
        if (state.currentDetail == null) {
            UiAlerts.error("补图失败", "请先选择一条报修记录。");
            return;
        }
        try {
            ProjectImageStore.validateImageFiles(state.pendingImageFiles);
            int total = appContext.repairRequestService().appendStudentRequestImages(
                    state.currentDetail.getStudentId(),
                    state.currentDetail.getId(),
                    ProjectImageStore.copyImagesToProject(state.pendingImageFiles)
            );
            state.pendingImageFiles.clear();
            refreshPendingImagePreview(state);
            reloadCurrentDetail(
                    appContext.userAccountService().requireCurrentAccount(appContext.appSession(), UserRole.STUDENT),
                    state
            );
            UiAlerts.info("补图成功", "图片已补充到当前报修记录，当前共 " + total + " 张。");
        } catch (RuntimeException exception) {
            UiAlerts.error("补图失败", exception.getMessage());
        }
    }

    private void clearDetail(DetailState state) {
        state.currentDetail = null;
        state.statusContainer.getChildren().setAll(helperLabel("未选择记录"));
        state.requestNoValue.setText("无");
        state.locationValue.setText("无");
        state.phoneValue.setText("无");
        state.categoryValue.setText("无");
        state.submittedAtValue.setText("无");
        state.completedAtValue.setText("无");
        state.urgeCountValue.setText("0");
        state.reworkSummaryValue.setText("当前没有返修记录");
        state.descriptionValue.setText("无");
        state.completionSummaryValue.setText("当前暂无完工说明");
        state.feedbackSummaryValue.setText("当前阶段无需评价");
        state.requestGallery.setImages(List.of(), "当前没有报修附件。");
        state.completionGallery.setImages(List.of(), "当前暂无完工凭证。");
        state.pendingImageFiles.clear();
        refreshPendingImagePreview(state);
        renderAppendImageSection(state, null);
        state.actionContainer.getChildren().setAll(helperLabel("无"));
        renderTimelineSection(state, List.of());
        state.feedbackContainer.getChildren().setAll(state.feedbackSummaryValue);
    }

    private void renderAppendImageSection(DetailState state, RepairRequestStatus status) {
        boolean canAppend = status != null
                && status != RepairRequestStatus.COMPLETED
                && status != RepairRequestStatus.REJECTED
                && status != RepairRequestStatus.CANCELLED;
        state.appendImageBox.setDisable(!canAppend);
        state.appendImageBox.setOpacity(canAppend ? 1.0 : 0.6);
    }

    private void renderActionSection(DetailState state, RepairRequestStatus status) {
        state.actionContainer.getChildren().clear();
        boolean canUrge = status == RepairRequestStatus.SUBMITTED
                || status == RepairRequestStatus.ASSIGNED
                || status == RepairRequestStatus.IN_PROGRESS
                || status == RepairRequestStatus.REWORK_IN_PROGRESS;
        boolean canCancel = status == RepairRequestStatus.SUBMITTED || status == RepairRequestStatus.ASSIGNED;
        boolean canConfirm = status == RepairRequestStatus.PENDING_CONFIRMATION;
        if (!canUrge && !canCancel && !canConfirm) {
            state.actionContainer.getChildren().add(helperLabel("无"));
            return;
        }
        List<Node> nodes = new ArrayList<>();
        if (canUrge) {
            nodes.add(state.urgeButton);
        }
        if (canCancel) {
            nodes.add(state.cancelButton);
        }
        if (canConfirm) {
            state.reworkArea.clear();
            state.actionContainer.getChildren().add(createFieldBlock("返修说明", state.reworkArea));
            nodes.add(state.confirmButton);
            nodes.add(state.reworkButton);
        }
        HBox row = new HBox(12, nodes.toArray(Node[]::new));
        row.setAlignment(Pos.CENTER_LEFT);
        state.actionContainer.getChildren().add(row);
    }

    private void renderTimelineSection(DetailState state, List<WorkOrderRecordView> records) {
        state.timelineContainer.getChildren().setAll(
                createTimelineList(records, "暂无处理记录", "报修生成工单后，时间线会显示在这里。")
        );
    }

    private void renderFeedbackSection(DetailState state, StudentRepairDetailView detail) {
        state.feedbackContainer.getChildren().clear();
        state.feedbackSummaryValue.setText(buildFeedbackSummary(detail));
        state.feedbackContainer.getChildren().add(state.feedbackSummaryValue);
        if (detail.getStatus() == RepairRequestStatus.COMPLETED && !detail.hasFeedback()) {
            VBox editor = new VBox(
                    10,
                    createFieldBlock("评分", state.ratingBox),
                    createFieldBlock("评价内容", state.feedbackArea),
                    state.anonymousBox,
                    state.feedbackButton
            );
            editor.setFillWidth(true);
            state.feedbackContainer.getChildren().add(editor);
        }
    }

    private void submitFeedback(UserAccount currentStudent, DetailState state) {
        if (state.currentDetail == null) {
            UiAlerts.error("提交失败", "请先选择一条报修记录。");
            return;
        }
        try {
            Integer rating = state.ratingBox.getValue();
            if (rating == null) {
                throw new BusinessException("请选择评分后再提交。");
            }
            String comment = state.feedbackArea.getText() == null ? null : state.feedbackArea.getText().trim();
            appContext.repairRequestService().submitFeedback(new SubmitRepairFeedbackCommand(
                    currentStudent.getId(),
                    state.currentDetail.getId(),
                    rating,
                    comment,
                    state.anonymousBox.isSelected()
            ));
            reloadCurrentDetail(currentStudent, state);
            UiAlerts.info("评价成功", "感谢你的反馈，评价已提交。");
        } catch (BusinessException exception) {
            UiAlerts.error("提交失败", exception.getMessage());
        } catch (RuntimeException exception) {
            UiAlerts.error("提交失败", exception.getMessage());
        }
    }

    private void urge(UserAccount currentStudent, DetailState state) {
        if (state.currentDetail == null) {
            UiAlerts.error("催办失败", "请先选择一条报修记录。");
            return;
        }
        try {
            int urgeCount = appContext.repairRequestService().urgeStudentRequest(currentStudent.getId(), state.currentDetail.getId());
            reloadCurrentDetail(currentStudent, state);
            UiAlerts.info("催办成功", "已记录本次催办，当前催办次数：" + urgeCount + "。 ");
        } catch (RuntimeException exception) {
            UiAlerts.error("催办失败", exception.getMessage());
        }
    }

    private void cancel(UserAccount currentStudent, DetailState state) {
        if (state.currentDetail == null) {
            UiAlerts.error("取消失败", "请先选择一条报修记录。");
            return;
        }
        if (!UiAlerts.confirm("取消报修", "确认取消当前报修记录？取消后需要重新提交新的报修申请。", "确认取消")) {
            return;
        }
        try {
            appContext.repairRequestService().cancelStudentRequest(currentStudent.getId(), state.currentDetail.getId());
            reloadCurrentDetail(currentStudent, state);
            UiAlerts.info("取消成功", "当前报修已关闭。");
        } catch (RuntimeException exception) {
            UiAlerts.error("取消失败", exception.getMessage());
        }
    }

    private void confirmCompletion(UserAccount currentStudent, DetailState state) {
        if (state.currentDetail == null) {
            UiAlerts.error("确认失败", "请先选择一条报修记录。");
            return;
        }
        try {
            appContext.repairRequestService().confirmStudentCompletion(currentStudent.getId(), state.currentDetail.getId());
            reloadCurrentDetail(currentStudent, state);
            UiAlerts.info("确认成功", "当前报修已确认完成。");
        } catch (RuntimeException exception) {
            UiAlerts.error("确认失败", exception.getMessage());
        }
    }

    private void requestRework(UserAccount currentStudent, DetailState state) {
        if (state.currentDetail == null) {
            UiAlerts.error("返修失败", "请先选择一条报修记录。");
            return;
        }
        try {
            appContext.repairRequestService().requestStudentRework(currentStudent.getId(), state.currentDetail.getId(), state.reworkArea.getText());
            reloadCurrentDetail(currentStudent, state);
            UiAlerts.info("已提交返修", "当前报修已回到处理中链路。");
        } catch (RuntimeException exception) {
            UiAlerts.error("返修失败", exception.getMessage());
        }
    }

    private void reloadCurrentDetail(UserAccount currentStudent, DetailState state) {
        Long requestId = state.currentDetail == null ? null : state.currentDetail.getId();
        if (state.historyState == null || requestId == null) {
            clearDetail(state);
            return;
        }
        state.historyState.selectedRequestId = requestId;
        refreshHistory(state.historyState, state, currentStudent);
    }

    private String buildCompletionSummary(StudentRepairDetailView detail) {
        if (detail == null) {
            return "当前暂无完工说明";
        }
        String note = trimToNull(detail.getCompletionNote());
        int proofCount = detail.getCompletionImageUrls() == null ? 0 : detail.getCompletionImageUrls().size();
        String completedAt = formatTime(detail.getCompletedAt());
        if (note == null && proofCount == 0) {
            return "当前暂无完工说明或完工凭证。";
        }
        StringBuilder builder = new StringBuilder();
        if (completedAt != null && !completedAt.isBlank()) {
            builder.append("完工时间：").append(completedAt);
        }
        if (note != null) {
            if (builder.length() > 0) {
                builder.append(" / ");
            }
            builder.append("处理说明：").append(note);
        }
        if (proofCount > 0) {
            if (builder.length() > 0) {
                builder.append(" / ");
            }
            builder.append("完工凭证 ").append(proofCount).append(" 张");
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
        return "返修 " + reworkRecords.size() + " 次 / 最近原因：" + placeholder(latest.getRecordNote());
    }

    private String buildFeedbackSummary(StudentRepairDetailView detail) {
        if (detail.hasFeedback()) {
            StringBuilder builder = new StringBuilder("已评分 ").append(detail.getFeedbackRating()).append(" 分");
            if (Boolean.TRUE.equals(detail.getFeedbackAnonymousFlag())) {
                builder.append(" / 匿名评价");
            }
            if (detail.getFeedbackComment() != null && !detail.getFeedbackComment().isBlank()) {
                builder.append(" / ").append(detail.getFeedbackComment().trim());
            }
            return builder.toString();
        }
        return detail.getStatus() == RepairRequestStatus.COMPLETED ? "待评价" : "当前阶段无需评价";
    }

    private String statusText(RepairRequestStatus status) {
        if (status == null) {
            return "无";
        }
        return switch (status) {
            case SUBMITTED -> "已提交";
            case ASSIGNED -> "已受理";
            case IN_PROGRESS -> "处理中";
            case PENDING_CONFIRMATION -> "待确认完成";
            case REWORK_IN_PROGRESS -> "返修中";
            case COMPLETED -> "已处理";
            case REJECTED -> "已驳回";
            case CANCELLED -> "已取消";
        };
    }

    private String categoryText(FaultCategory category) {
        if (category == null) {
            return "无";
        }
        return switch (category) {
            case ELECTRICITY -> "电路电器";
            case WATER_PIPE -> "水管水龙头";
            case DOOR_WINDOW -> "门窗锁具";
            case NETWORK -> "网络故障";
            case FURNITURE -> "家具设施";
            case PUBLIC_AREA -> "公共区域";
            case OTHER -> "其他问题";
        };
    }

    private String historyStatusStyle(RepairRequestStatus status) {
        if (status == null) {
            return "student-history-card-chip-idle";
        }
        return switch (status) {
            case SUBMITTED -> "student-history-card-chip-submitted";
            case ASSIGNED -> "student-history-card-chip-assigned";
            case IN_PROGRESS, REWORK_IN_PROGRESS -> "student-history-card-chip-progress";
            case PENDING_CONFIRMATION, COMPLETED -> "student-history-card-chip-completed";
            case REJECTED, CANCELLED -> "student-history-card-chip-closed";
        };
    }

    private String summarizeRequestNo(String requestNo) {
        String value = safe(requestNo).trim();
        if (value.isBlank()) {
            return "";
        }
        return "尾号 " + (value.length() <= 6 ? value : value.substring(value.length() - 6));
    }

    private String placeholder(String value) {
        String text = safe(value).trim();
        return text.isEmpty() ? "无" : text;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? "" : TIME_FORMATTER.format(value);
    }

    private Label valueLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("plain-text");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private Label helperLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("helper-text");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private static final class HistoryState {
        private final VBox container;
        private StackPane selectedCard;
        private Long selectedRequestId;

        private HistoryState(VBox container) {
            this.container = container;
        }
    }

    private static final class DetailState {
        private HistoryState historyState;
        private StudentRepairDetailView currentDetail;
        private VBox statusContainer;
        private Label requestNoValue;
        private Label locationValue;
        private Label phoneValue;
        private Label categoryValue;
        private Label submittedAtValue;
        private Label completedAtValue;
        private Label urgeCountValue;
        private Label reworkSummaryValue;
        private Label descriptionValue;
        private Label completionSummaryValue;
        private Label feedbackSummaryValue;
        private EvidenceGallery requestGallery;
        private EvidenceGallery completionGallery;
        private VBox appendImageBox;
        private Label appendImageCountLabel;
        private VBox appendImagePreview;
        private List<File> pendingImageFiles;
        private VBox actionContainer;
        private VBox timelineContainer;
        private VBox feedbackContainer;
        private AppDropdown<Integer> ratingBox;
        private TextArea feedbackArea;
        private CheckBox anonymousBox;
        private TextArea reworkArea;
        private Button urgeButton;
        private Button cancelButton;
        private Button confirmButton;
        private Button reworkButton;
        private Button feedbackButton;
    }
}
