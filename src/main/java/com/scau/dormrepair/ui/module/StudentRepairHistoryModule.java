package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.DemoAccountDirectory;
import com.scau.dormrepair.common.DemoAccountDirectory.DemoAccount;
import com.scau.dormrepair.domain.command.SubmitRepairFeedbackCommand;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.StudentRepairDetailView;
import com.scau.dormrepair.ui.component.AppDropdown;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.support.UiDisplayText;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

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
        DemoAccount currentStudent = DemoAccountDirectory.resolveCurrent(appContext.appSession());
        if (currentStudent == null) {
            throw new IllegalStateException("当前未找到学生会话，无法查看报修记录。");
        }

        TableView<RecentRepairRequestView> historyTable = buildHistoryTable();
        VBox detailPanel = buildDetailPanel(currentStudent, historyTable);

        Button refreshButton = new Button("刷新记录");
        refreshButton.getStyleClass().add("surface-button");
        refreshButton.setOnAction(event -> refreshHistory(historyTable, currentStudent.id(), currentStudent.displayName()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(12, spacer, refreshButton);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        GridPane workspace = createRatioWorkspace(
                47,
                53,
                wrapPanel("本人报修记录", new VBox(12, toolbar, historyTable)),
                wrapPanel("记录详情", detailPanel)
        );

        refreshHistory(historyTable, currentStudent.id(), currentStudent.displayName());

        VBox content = new VBox(18, workspace);
        content.setFillWidth(true);
        return createPage("学生报修记录", "", content);
    }

    private TableView<RecentRepairRequestView> buildHistoryTable() {
        TableView<RecentRepairRequestView> historyTable = new TableView<>();
        historyTable.getColumns().addAll(
                createTextColumn("报修单号", "requestNo"),
                createTextColumn("位置", "locationText"),
                createFaultColumn(),
                createStatusColumn(),
                createDateTimeColumn()
        );
        configureFixedTable(historyTable, 460, 1.58, 1.42, 1.16, 0.86, 1.18);
        return historyTable;
    }

    private void refreshHistory(TableView<RecentRepairRequestView> historyTable, Long studentId, String studentName) {
        List<RecentRepairRequestView> historyRows =
                appContext.repairRequestService().listStudentSubmittedRequests(studentId, studentName, 20);
        historyTable.setItems(FXCollections.observableArrayList(historyRows));
        fitTableHeightToRows(historyTable, historyRows.size(), 6, 11);
        if (!historyRows.isEmpty()) {
            historyTable.getSelectionModel().selectFirst();
        } else {
            historyTable.getSelectionModel().clearSelection();
        }
    }

    private VBox buildDetailPanel(DemoAccount currentStudent, TableView<RecentRepairRequestView> historyTable) {
        DetailState state = createDetailState();
        Runnable clearDetail = () -> clearDetail(state);

        historyTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                clearDetail.run();
                return;
            }

            try {
                StudentRepairDetailView detailView =
                        appContext.repairRequestService().getStudentRequestDetail(
                                currentStudent.id(),
                                currentStudent.displayName(),
                                newValue.getId()
                        );
                state.currentDetail = detailView;
                renderDetail(state, detailView);
            } catch (RuntimeException exception) {
                state.currentDetail = null;
                clearDetail.run();
                UiAlerts.error("详情加载失败", exception.getMessage());
            }
        });

        state.feedbackButton.setOnAction(event -> submitFeedback(currentStudent, state, historyTable));
        state.urgeButton.setOnAction(event -> urgeRequest(currentStudent, state, historyTable));
        state.cancelButton.setOnAction(event -> cancelRequest(currentStudent, state, historyTable));

        ScrollPane imageScrollPane = new ScrollPane(state.imageThumbPane);
        imageScrollPane.setFitToWidth(true);
        imageScrollPane.setPrefHeight(132);
        imageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox previewBox = new VBox(10, state.previewHint, state.previewFrame);
        previewBox.getStyleClass().add("student-preview-box");

        HBox actionRow = new HBox(12, state.urgeButton, state.cancelButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        VBox feedbackBox = new VBox(
                10,
                createFieldBlock("评价状态", state.feedbackBanner),
                createFieldBlock("评分", state.ratingBox),
                createFieldBlock("评价内容", state.feedbackArea),
                state.anonymousBox,
                state.feedbackButton
        );
        feedbackBox.getStyleClass().add("student-feedback-box");

        VBox detailPanel = new VBox(
                14,
                createStatusSummaryRow(state),
                createFieldBlock("学生侧操作", new VBox(10, state.actionBanner, actionRow)),
                createFieldBlock("处理进度", new VBox(10, state.progressSummaryValue, state.progressTrack)),
                createDetailBlock("报修单号", state.requestNoValue),
                createDetailBlock("宿舍位置", state.locationValue),
                createDetailBlock("联系电话", state.phoneValue),
                createDetailBlock("故障类别", state.categoryValue),
                createDetailBlock("提交时间", state.submittedAtValue),
                createDetailBlock("完成时间", state.completedAtValue),
                createDetailBlock("催办次数", state.urgeCountValue),
                createDetailBlock("图片数量", state.imageCountValue),
                createDetailBlock("报修描述", state.descriptionValue),
                createFieldBlock("图片缩略图", imageScrollPane),
                createFieldBlock("大图预览", previewBox),
                createDetailBlock("å·¥å•ç¼–å·", state.workOrderNoValue),
                createDetailBlock("æ´¾å•æ—¶é—´", state.assignedAtValue),
                createDetailBlock("æŽ¥å•æ—¶é—´", state.acceptedAtValue),
                createDetailBlock("ç»´ä¿®äººå‘˜", state.workerNameValue),
                createDetailBlock("æ´¾å•å¤‡æ³¨", state.assignmentNoteValue),
                createFieldBlock("å¤„ç†æ—¶é—´çº¿", state.timelineBox),
                feedbackBox
        );
        detailPanel.setFillWidth(true);
        clearDetail.run();
        return detailPanel;
    }

    private DetailState createDetailState() {
        Label statusValue = createBadgeLabel("未选择记录");
        Label statusHintValue = createBadgeLabel("请先在左侧选择一条记录");
        Label requestNoValue = createDetailValueLabel("请先在左侧选择一条记录");
        Label locationValue = createDetailValueLabel("");
        Label phoneValue = createDetailValueLabel("");
        Label categoryValue = createDetailValueLabel("");
        Label submittedAtValue = createDetailValueLabel("");
        Label assignedAtValue = createDetailValueLabel("");
        Label acceptedAtValue = createDetailValueLabel("");
        Label completedAtValue = createDetailValueLabel("");
        Label urgeCountValue = createDetailValueLabel("0");
        Label workOrderNoValue = createDetailValueLabel("");
        Label workerNameValue = createDetailValueLabel("");
        Label assignmentNoteValue = createDetailValueLabel("");
        Label imageCountValue = createDetailValueLabel("");
        Label descriptionValue = createDetailValueLabel("");
        Label progressSummaryValue = createDetailValueLabel("当前未加载工单状态。");
        VBox timelineBox = new VBox(10);
        timelineBox.setFillWidth(true);

        HBox progressTrack = new HBox(10);
        progressTrack.setAlignment(Pos.CENTER_LEFT);
        progressTrack.getChildren().addAll(
                createProgressStep("1", "已提交", "学生已成功提交报修申请。"),
                createProgressStep("2", "已派单", "管理员已进入派单环节。"),
                createProgressStep("3", "处理中", "维修人员正在处理本次报修。"),
                createProgressStep("4", "已完成", "本次报修已处理完成，可提交评价。")
        );

        FlowPane imageThumbPane = new FlowPane(10, 10);
        imageThumbPane.setPadding(new Insets(4, 0, 4, 0));
        imageThumbPane.setPrefWrapLength(300);

        ImageView previewImage = new ImageView();
        previewImage.setFitWidth(300);
        previewImage.setFitHeight(200);
        previewImage.setPreserveRatio(true);
        previewImage.setSmooth(true);
        previewImage.setVisible(false);
        previewImage.setManaged(false);

        StackPane previewFrame = new StackPane(previewImage);
        previewFrame.getStyleClass().add("student-preview-frame");
        previewFrame.setMinHeight(220);
        previewFrame.setPrefHeight(220);

        Label previewHint = createDetailValueLabel("当前记录暂无可预览图片。");
        Label actionBanner = createBadgeLabel("催办和取消会根据工单状态自动开放。");
        Label feedbackBanner = createBadgeLabel("评价入口会在工单完成后开放。");

        AppDropdown<Integer> ratingBox = new AppDropdown<>();
        ratingBox.setItems(List.of(5, 4, 3, 2, 1));
        ratingBox.setPromptText("请选择 1-5 分");
        ratingBox.setVisibleRowCount(5);

        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("可以补充维修体验、响应速度、处理质量等反馈。");
        feedbackArea.setWrapText(true);
        feedbackArea.setPrefRowCount(3);

        CheckBox anonymousBox = new CheckBox("匿名评价");
        Button urgeButton = new Button("催办一次");
        urgeButton.getStyleClass().add("surface-button");
        Button cancelButton = new Button("取消报修");
        cancelButton.getStyleClass().add("surface-button");
        Button feedbackButton = new Button("提交评价");
        feedbackButton.getStyleClass().add("surface-button");

        return new DetailState(
                null,
                requestNoValue,
                statusValue,
                statusHintValue,
                locationValue,
                phoneValue,
                categoryValue,
                submittedAtValue,
                assignedAtValue,
                acceptedAtValue,
                completedAtValue,
                urgeCountValue,
                workOrderNoValue,
                workerNameValue,
                assignmentNoteValue,
                imageCountValue,
                descriptionValue,
                progressTrack,
                progressSummaryValue,
                timelineBox,
                imageThumbPane,
                previewImage,
                previewFrame,
                previewHint,
                actionBanner,
                feedbackBanner,
                ratingBox,
                feedbackArea,
                anonymousBox,
                urgeButton,
                cancelButton,
                feedbackButton,
                new ArrayList<>()
        );
    }

    private HBox createStatusSummaryRow(DetailState state) {
        VBox primaryBox = new VBox(
                8,
                createFieldBlock("当前状态", state.statusValue),
                createFieldBlock("状态说明", state.statusHintValue)
        );
        primaryBox.setFillWidth(true);
        HBox.setHgrow(primaryBox, Priority.ALWAYS);
        return new HBox(14, primaryBox);
    }

    private void clearDetail(DetailState state) {
        state.currentDetail = null;
        state.requestNoValue.setText("请先在左侧选择一条记录");
        state.statusValue.setText("未选择记录");
        applyStatusBadge(state.statusValue, null);
        state.statusHintValue.setText("加载后可查看处理进度、图片和评价信息。");
        state.locationValue.setText("");
        state.phoneValue.setText("");
        state.categoryValue.setText("");
        state.submittedAtValue.setText("");
        state.assignedAtValue.setText("");
        state.acceptedAtValue.setText("");
        state.completedAtValue.setText("");
        state.urgeCountValue.setText("0");
        state.workOrderNoValue.setText("");
        state.workerNameValue.setText("");
        state.assignmentNoteValue.setText("");
        state.imageCountValue.setText("0");
        state.descriptionValue.setText("");
        state.progressSummaryValue.setText("当前未加载工单状态。");
        updateProgressTrack(state.progressTrack, null);
        renderTimeline(state, null);
        clearPreview(state);
        state.actionBanner.setText("催办和取消会根据工单状态自动开放。");
        applyActionBannerStyle(state.actionBanner, ActionBannerState.IDLE);
        state.feedbackBanner.setText("评价入口会在工单完成后开放。");
        applyFeedbackBannerStyle(state.feedbackBanner, FeedbackBannerState.IDLE);
                state.ratingBox.clearSelection();
        state.ratingBox.setDisable(true);
        state.feedbackArea.clear();
        state.feedbackArea.setDisable(true);
        state.anonymousBox.setSelected(false);
        state.anonymousBox.setDisable(true);
        state.urgeButton.setDisable(true);
        state.cancelButton.setDisable(true);
        state.feedbackButton.setText("提交评价");
        state.feedbackButton.setDisable(true);
    }

    private void renderDetail(DetailState state, StudentRepairDetailView detailView) {
        state.requestNoValue.setText(nullToEmpty(detailView.getRequestNo()));
        state.statusValue.setText(UiDisplayText.repairRequestStatus(detailView.getStatus()));
        applyStatusBadge(state.statusValue, detailView.getStatus());
        state.statusHintValue.setText(buildStatusHint(detailView));
        state.locationValue.setText(nullToEmpty(detailView.getLocationText()));
        state.phoneValue.setText(nullToEmpty(detailView.getContactPhone()));
        state.categoryValue.setText(UiDisplayText.faultCategory(detailView.getFaultCategory()));
        state.submittedAtValue.setText(formatTime(detailView.getSubmittedAt()));
        state.assignedAtValue.setText(formatTime(detailView.getAssignedAt()));
        state.acceptedAtValue.setText(formatTime(detailView.getAcceptedAt()));
        state.completedAtValue.setText(formatTime(detailView.getCompletedAt()));
        state.urgeCountValue.setText(String.valueOf(detailView.getUrgeCount()));
        state.workOrderNoValue.setText(nullToEmpty(detailView.getWorkOrderNo()));
        state.workerNameValue.setText(nullToEmpty(detailView.getWorkerName()));
        state.assignmentNoteValue.setText(nullToEmpty(detailView.getAssignmentNote()));
        state.imageCountValue.setText(String.valueOf(detailView.getImageUrls().size()));
        state.descriptionValue.setText(nullToEmpty(detailView.getDescription()));
        state.progressSummaryValue.setText(buildProgressSummary(detailView));
        updateProgressTrack(state.progressTrack, detailView.getStatus());
        renderTimeline(state, detailView);
        renderActionState(state, detailView);
        renderImagePreview(state, detailView.getImageUrls());
        renderFeedbackState(state, detailView);
    }

    private void renderTimeline(DetailState state, StudentRepairDetailView detailView) {
        state.timelineBox.getChildren().clear();
        if (detailView == null) {
            state.timelineBox.getChildren().add(createTimelineCard("等待选择记录", "", "选中左侧报修后，这里会展示派单和处理节点。"));
            return;
        }

        state.timelineBox.getChildren().add(createTimelineCard(
                "学生提交报修",
                formatTime(detailView.getSubmittedAt()),
                "报修申请已经进入系统。"
        ));

        if (detailView.getAssignedAt() != null) {
            String workerText = nullToEmpty(detailView.getWorkerName());
            String noteText = nullToEmpty(detailView.getAssignmentNote());
            String copy = workerText.isBlank() ? "管理员已完成派单。" : "已派给 " + workerText + " 处理。";
            if (!noteText.isBlank()) {
                copy = copy + " 派单备注：" + noteText;
            }
            state.timelineBox.getChildren().add(createTimelineCard(
                    "管理员派单",
                    formatTime(detailView.getAssignedAt()),
                    copy
            ));
        }

        if (detailView.getAcceptedAt() != null) {
            String workerText = nullToEmpty(detailView.getWorkerName());
            String copy = workerText.isBlank() ? "维修人员已接单，正在跟进。" : workerText + " 已接单，开始处理。";
            state.timelineBox.getChildren().add(createTimelineCard(
                    "维修人员接单",
                    formatTime(detailView.getAcceptedAt()),
                    copy
            ));
        }

        if (detailView.getCompletedAt() != null) {
            state.timelineBox.getChildren().add(createTimelineCard(
                    "工单处理完成",
                    formatTime(detailView.getCompletedAt()),
                    "当前报修已经完结，可在下方补充评价。"
            ));
        } else if (detailView.getStatus() == RepairRequestStatus.CANCELLED) {
            state.timelineBox.getChildren().add(createTimelineCard(
                    "学生取消报修",
                    "",
                    "该工单已在学生侧关闭，后续不会再进入完成节点。"
            ));
        } else if (detailView.getStatus() == RepairRequestStatus.REJECTED) {
            state.timelineBox.getChildren().add(createTimelineCard(
                    "工单已关闭",
                    "",
                    "该工单已被关闭，处理流程在此结束。"
            ));
        }
    }

    private VBox createTimelineCard(String title, String timeText, String description) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("student-timeline-title");

        Label timeLabel = new Label(timeText == null || timeText.isBlank() ? "时间待更新" : timeText);
        timeLabel.getStyleClass().add("student-timeline-time");

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("student-timeline-copy");
        descriptionLabel.setWrapText(true);

        VBox box = new VBox(6, titleLabel, timeLabel, descriptionLabel);
        box.getStyleClass().add("student-timeline-card");
        box.setFillWidth(true);
        return box;
    }

    private void renderFeedbackState(DetailState state, StudentRepairDetailView detailView) {
        if (detailView.hasFeedback()) {
            state.feedbackBanner.setText(buildFeedbackSummary(detailView));
            applyFeedbackBannerStyle(state.feedbackBanner, FeedbackBannerState.DONE);
            state.ratingBox.setValue(detailView.getFeedbackRating());
            state.ratingBox.setDisable(true);
            state.feedbackArea.setText(nullToEmpty(detailView.getFeedbackComment()));
            state.feedbackArea.setDisable(true);
            state.anonymousBox.setSelected(Boolean.TRUE.equals(detailView.getFeedbackAnonymousFlag()));
            state.anonymousBox.setDisable(true);
            state.feedbackButton.setText("已提交评价");
            state.feedbackButton.setDisable(true);
            return;
        }

        boolean canFeedback = detailView.getStatus() == RepairRequestStatus.COMPLETED;
                state.ratingBox.clearSelection();
        state.ratingBox.setDisable(!canFeedback);
        state.feedbackArea.clear();
        state.feedbackArea.setDisable(!canFeedback);
        state.anonymousBox.setSelected(false);
        state.anonymousBox.setDisable(!canFeedback);
        state.feedbackButton.setText("提交评价");
        state.feedbackButton.setDisable(!canFeedback);

        if (canFeedback) {
            state.feedbackBanner.setText("本次报修已完成，欢迎补充评分和处理体验。");
            applyFeedbackBannerStyle(state.feedbackBanner, FeedbackBannerState.READY);
        } else {
            state.feedbackBanner.setText("工单完成后才会开放评价入口。");
            applyFeedbackBannerStyle(state.feedbackBanner, FeedbackBannerState.IDLE);
        }
    }

    private void renderActionState(DetailState state, StudentRepairDetailView detailView) {
        RepairRequestStatus status = detailView.getStatus();
        boolean canUrge = status == RepairRequestStatus.SUBMITTED
                || status == RepairRequestStatus.ASSIGNED
                || status == RepairRequestStatus.IN_PROGRESS;
        boolean canCancel = status == RepairRequestStatus.SUBMITTED
                || status == RepairRequestStatus.ASSIGNED;

        state.urgeButton.setDisable(!canUrge);
        state.cancelButton.setDisable(!canCancel);

        if (status == RepairRequestStatus.CANCELLED) {
            state.actionBanner.setText("该工单已由学生侧取消，后续不会再进入处理流程。");
            applyActionBannerStyle(state.actionBanner, ActionBannerState.CLOSED);
            return;
        }
        if (status == RepairRequestStatus.REJECTED) {
            state.actionBanner.setText("该工单已关闭，当前不支持继续催办或取消。");
            applyActionBannerStyle(state.actionBanner, ActionBannerState.CLOSED);
            return;
        }
        if (status == RepairRequestStatus.COMPLETED) {
            state.actionBanner.setText("该工单已完成，催办和取消入口已自动关闭。");
            applyActionBannerStyle(state.actionBanner, ActionBannerState.CLOSED);
            return;
        }
        if (status == RepairRequestStatus.IN_PROGRESS) {
            state.actionBanner.setText("维修人员已开始处理，可催办同步进度，但不再支持取消。");
            applyActionBannerStyle(state.actionBanner, ActionBannerState.OPEN);
            return;
        }
        if (status == RepairRequestStatus.ASSIGNED) {
            state.actionBanner.setText("工单已派单，可继续催办，也可在正式处理前取消。");
            applyActionBannerStyle(state.actionBanner, ActionBannerState.OPEN);
            return;
        }

        state.actionBanner.setText("工单刚提交成功，支持催办提醒，也支持主动取消。");
        applyActionBannerStyle(state.actionBanner, ActionBannerState.OPEN);
    }

    private void submitFeedback(DemoAccount currentStudent, DetailState state, TableView<RecentRepairRequestView> historyTable) {
        if (state.currentDetail == null) {
            UiAlerts.error("提交失败", "请先选择一条已完成的报修记录。");
            return;
        }

        try {
            SubmitRepairFeedbackCommand command = new SubmitRepairFeedbackCommand(
                    state.currentDetail.getId(),
                    state.ratingBox.getValue(),
                    state.feedbackArea.getText(),
                    state.anonymousBox.isSelected()
            );
            appContext.repairRequestService().submitFeedback(command);
            reloadCurrentDetail(currentStudent, state, historyTable);
            UiAlerts.info("评价已提交", "感谢反馈，评价结果已经同步到当前工单。");
        } catch (RuntimeException exception) {
            UiAlerts.error("提交失败", exception.getMessage());
        }
    }

    private void urgeRequest(DemoAccount currentStudent, DetailState state, TableView<RecentRepairRequestView> historyTable) {
        if (state.currentDetail == null) {
            UiAlerts.error("催办失败", "请先选择一条需要催办的报修记录。");
            return;
        }

        try {
            int urgeCount = appContext.repairRequestService()
                    .urgeStudentRequest(currentStudent.id(), currentStudent.displayName(), state.currentDetail.getId());
            reloadCurrentDetail(currentStudent, state, historyTable);
            UiAlerts.info("催办已提交", "本次催办已经记录，当前累计催办 " + urgeCount + " 次。");
        } catch (RuntimeException exception) {
            UiAlerts.error("催办失败", exception.getMessage());
        }
    }

    private void cancelRequest(DemoAccount currentStudent, DetailState state, TableView<RecentRepairRequestView> historyTable) {
        if (state.currentDetail == null) {
            UiAlerts.error("取消失败", "请先选择一条需要取消的报修记录。");
            return;
        }

        boolean confirmed = UiAlerts.confirm(
                "确认取消报修",
                "取消后，当前工单将立即关闭，且不再继续进入维修流程。",
                "确认取消"
        );
        if (!confirmed) {
            return;
        }

        try {
            appContext.repairRequestService().cancelStudentRequest(
                    currentStudent.id(),
                    currentStudent.displayName(),
                    state.currentDetail.getId()
            );
            reloadCurrentDetail(currentStudent, state, historyTable);
            UiAlerts.info("报修已取消", "当前工单已从学生侧取消，状态已同步刷新。");
        } catch (RuntimeException exception) {
            UiAlerts.error("取消失败", exception.getMessage());
        }
    }

    private void reloadCurrentDetail(DemoAccount currentStudent, DetailState state, TableView<RecentRepairRequestView> historyTable) {
        Long currentRequestId = state.currentDetail == null ? null : state.currentDetail.getId();
        refreshHistory(historyTable, currentStudent.id(), currentStudent.displayName());
        if (currentRequestId == null) {
            clearDetail(state);
            return;
        }

        for (RecentRepairRequestView item : historyTable.getItems()) {
            if (currentRequestId.equals(item.getId())) {
                historyTable.getSelectionModel().select(item);
                return;
            }
        }

        historyTable.getSelectionModel().clearSelection();
        clearDetail(state);
    }

    private void renderImagePreview(DetailState state, List<String> imageUrls) {
        state.imageThumbPane.getChildren().clear();
        state.thumbShells.clear();
        clearPreview(state);

        if (imageUrls == null || imageUrls.isEmpty()) {
            state.previewHint.setText("当前记录暂无可预览图片。");
            return;
        }

        int visibleIndex = 0;
        for (String imageUrl : imageUrls) {
            Path imagePath = resolveProjectPath(imageUrl);
            if (imagePath == null || !Files.exists(imagePath)) {
                Label missingLabel = new Label("图片文件不存在: " + imageUrl);
                missingLabel.getStyleClass().add("helper-text");
                state.imageThumbPane.getChildren().add(missingLabel);
                continue;
            }

            Image image = new Image(imagePath.toUri().toString(), 96, 72, true, true);
            ImageView thumb = new ImageView(image);
            thumb.setFitWidth(96);
            thumb.setFitHeight(72);
            thumb.setPreserveRatio(true);
            thumb.getStyleClass().add("student-thumb");

            Label indexLabel = new Label("图 " + (visibleIndex + 1));
            indexLabel.getStyleClass().add("student-thumb-index");

            VBox card = new VBox(6, thumb, indexLabel);
            card.setAlignment(Pos.CENTER);

            StackPane shell = new StackPane(card);
            shell.getStyleClass().add("student-thumb-shell");
            shell.setOnMouseClicked(event -> showPreview(state, shell, image, imagePath.getFileName().toString()));

            state.thumbShells.add(shell);
            state.imageThumbPane.getChildren().add(shell);

            if (visibleIndex == 0) {
                showPreview(state, shell, image, imagePath.getFileName().toString());
            }
            visibleIndex++;
        }

        if (visibleIndex == 0) {
            state.previewHint.setText("当前图片路径存在，但本地文件未找到。");
        }
    }

    private void clearPreview(DetailState state) {
        state.previewImage.setImage(null);
        state.previewImage.setVisible(false);
        state.previewImage.setManaged(false);
        state.previewHint.setText("当前记录暂无可预览图片。");
        state.thumbShells.forEach(shell -> shell.getStyleClass().remove("student-thumb-shell-active"));
    }

    private void showPreview(DetailState state, StackPane activeShell, Image image, String fileName) {
        state.previewImage.setImage(image);
        state.previewImage.setVisible(true);
        state.previewImage.setManaged(true);
        state.previewHint.setText("当前预览: " + fileName + "，点击其他缩略图可切换。");
        state.thumbShells.forEach(shell -> shell.getStyleClass().remove("student-thumb-shell-active"));
        activeShell.getStyleClass().add("student-thumb-shell-active");
    }

    private String buildStatusHint(StudentRepairDetailView detailView) {
        if (detailView.getStatus() == null) {
            return "当前状态未知。";
        }
        return switch (detailView.getStatus()) {
            case SUBMITTED -> "工单已提交，等待管理员查看并派单。";
            case ASSIGNED -> "工单已进入派单环节，正在安排维修人员。";
            case IN_PROGRESS -> "维修人员正在处理中，请留意后续进度。";
            case COMPLETED -> "工单已完成，如体验允许可直接在下方评价。";
            case REJECTED -> "工单已被驳回，建议查看原因后重新提交。";
            case CANCELLED -> "工单已取消，本次流程已关闭。";
        };
    }

    private String buildProgressSummary(StudentRepairDetailView detailView) {
        if (detailView.getStatus() == null) {
            return "当前未加载工单状态。";
        }
        return switch (detailView.getStatus()) {
            case SUBMITTED -> "报修已进入系统，正在等待管理员处理。";
            case ASSIGNED -> "管理员已介入，当前正在安排维修人员。";
            case IN_PROGRESS -> "本次工单已进入维修处理阶段。";
            case COMPLETED -> "工单已完成，学生侧链路已经进入评价阶段。";
            case REJECTED -> "工单已关闭，当前状态为驳回。";
            case CANCELLED -> "工单已关闭，当前状态为取消。";
        };
    }

    private String buildFeedbackSummary(StudentRepairDetailView detailView) {
        StringBuilder builder = new StringBuilder();
        builder.append("已评价 ").append(detailView.getFeedbackRating()).append(" 分");
        if (Boolean.TRUE.equals(detailView.getFeedbackAnonymousFlag())) {
            builder.append(" / 匿名");
        }
        if (detailView.getFeedbackComment() != null && !detailView.getFeedbackComment().isBlank()) {
            builder.append(" / ").append(detailView.getFeedbackComment().trim());
        }
        return builder.toString();
    }

    private HBox createProgressStep(String indexText, String titleText, String descriptionText) {
        Label indexLabel = new Label(indexText);
        indexLabel.getStyleClass().add("student-progress-index");

        Label titleLabel = new Label(titleText);
        titleLabel.getStyleClass().add("student-progress-title");

        Label descriptionLabel = new Label(descriptionText);
        descriptionLabel.getStyleClass().add("student-progress-copy");
        descriptionLabel.setWrapText(true);

        VBox card = new VBox(6, indexLabel, titleLabel, descriptionLabel);
        card.getStyleClass().add("student-progress-step");
        card.setFillWidth(true);
        card.setMaxWidth(Double.MAX_VALUE);

        StackPane shell = new StackPane(card);
        shell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(shell, Priority.ALWAYS);
        return new HBox(shell);
    }

    private void updateProgressTrack(HBox progressTrack, RepairRequestStatus status) {
        int activeIndex;
        if (status == null) {
            activeIndex = -1;
        } else {
            activeIndex = switch (status) {
                case SUBMITTED -> 0;
                case ASSIGNED -> 1;
                case IN_PROGRESS -> 2;
                case COMPLETED -> 3;
                case REJECTED, CANCELLED -> -1;
            };
        }

        for (int index = 0; index < progressTrack.getChildren().size(); index++) {
            HBox row = (HBox) progressTrack.getChildren().get(index);
            StackPane shell = (StackPane) row.getChildren().get(0);
            VBox card = (VBox) shell.getChildren().get(0);
            card.getStyleClass().removeAll(
                    "student-progress-step-active",
                    "student-progress-step-done",
                    "student-progress-step-closed"
            );

            if (activeIndex >= 0) {
                if (index < activeIndex) {
                    card.getStyleClass().add("student-progress-step-done");
                } else if (index == activeIndex) {
                    card.getStyleClass().add("student-progress-step-active");
                }
            } else if (status == RepairRequestStatus.REJECTED || status == RepairRequestStatus.CANCELLED) {
                card.getStyleClass().add("student-progress-step-closed");
            }
        }
    }

    private void applyStatusBadge(Label label, RepairRequestStatus status) {
        label.getStyleClass().removeAll(
                "student-status-chip-submitted",
                "student-status-chip-assigned",
                "student-status-chip-progress",
                "student-status-chip-completed",
                "student-status-chip-closed"
        );
        if (status == null) {
            return;
        }
        switch (status) {
            case SUBMITTED -> label.getStyleClass().add("student-status-chip-submitted");
            case ASSIGNED -> label.getStyleClass().add("student-status-chip-assigned");
            case IN_PROGRESS -> label.getStyleClass().add("student-status-chip-progress");
            case COMPLETED -> label.getStyleClass().add("student-status-chip-completed");
            case REJECTED, CANCELLED -> label.getStyleClass().add("student-status-chip-closed");
        }
    }

    private void applyFeedbackBannerStyle(Label label, FeedbackBannerState state) {
        label.getStyleClass().removeAll(
                "student-feedback-banner-idle",
                "student-feedback-banner-ready",
                "student-feedback-banner-done"
        );
        switch (state) {
            case IDLE -> label.getStyleClass().add("student-feedback-banner-idle");
            case READY -> label.getStyleClass().add("student-feedback-banner-ready");
            case DONE -> label.getStyleClass().add("student-feedback-banner-done");
        }
    }

    private void applyActionBannerStyle(Label label, ActionBannerState state) {
        label.getStyleClass().removeAll(
                "student-action-banner-idle",
                "student-action-banner-open",
                "student-action-banner-closed"
        );
        switch (state) {
            case IDLE -> label.getStyleClass().add("student-action-banner-idle");
            case OPEN -> label.getStyleClass().add("student-action-banner-open");
            case CLOSED -> label.getStyleClass().add("student-action-banner-closed");
        }
    }

    private Path resolveProjectPath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }
        Path path = Path.of(storedPath);
        if (path.isAbsolute()) {
            return path;
        }
        return Path.of("").toAbsolutePath().resolve(storedPath).normalize();
    }

    private VBox createDetailBlock(String labelText, Label valueLabel) {
        valueLabel.getStyleClass().add("plain-text");
        return createFieldBlock(labelText, valueLabel);
    }

    private Label createDetailValueLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private Label createBadgeLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.getStyleClass().add("student-detail-badge");
        return label;
    }

    private TableColumn<RecentRepairRequestView, String> createTextColumn(String title, String property) {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    private TableColumn<RecentRepairRequestView, String> createFaultColumn() {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>("故障类别");
        column.setCellValueFactory(cell ->
                Bindings.createStringBinding(() -> UiDisplayText.faultCategory(cell.getValue().getFaultCategory()))
        );
        return column;
    }

    private TableColumn<RecentRepairRequestView, String> createStatusColumn() {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>("状态");
        column.setCellValueFactory(cell ->
                Bindings.createStringBinding(() -> UiDisplayText.repairRequestStatus(cell.getValue().getStatus()))
        );
        return column;
    }

    private TableColumn<RecentRepairRequestView, String> createDateTimeColumn() {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>("提交时间");
        column.setCellValueFactory(cell ->
                Bindings.createStringBinding(() -> formatTime(cell.getValue().getSubmittedAt()))
        );
        return column;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : TIME_FORMATTER.format(time);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private enum FeedbackBannerState {
        IDLE,
        READY,
        DONE
    }

    private enum ActionBannerState {
        IDLE,
        OPEN,
        CLOSED
    }

    private static final class DetailState {
        private StudentRepairDetailView currentDetail;
        private final Label requestNoValue;
        private final Label statusValue;
        private final Label statusHintValue;
        private final Label locationValue;
        private final Label phoneValue;
        private final Label categoryValue;
        private final Label submittedAtValue;
        private final Label assignedAtValue;
        private final Label acceptedAtValue;
        private final Label completedAtValue;
        private final Label urgeCountValue;
        private final Label workOrderNoValue;
        private final Label workerNameValue;
        private final Label assignmentNoteValue;
        private final Label imageCountValue;
        private final Label descriptionValue;
        private final HBox progressTrack;
        private final Label progressSummaryValue;
        private final VBox timelineBox;
        private final FlowPane imageThumbPane;
        private final ImageView previewImage;
        private final StackPane previewFrame;
        private final Label previewHint;
        private final Label actionBanner;
        private final Label feedbackBanner;
        private final AppDropdown<Integer> ratingBox;
        private final TextArea feedbackArea;
        private final CheckBox anonymousBox;
        private final Button urgeButton;
        private final Button cancelButton;
        private final Button feedbackButton;
        private final List<StackPane> thumbShells;

        private DetailState(
                StudentRepairDetailView currentDetail,
                Label requestNoValue,
                Label statusValue,
                Label statusHintValue,
                Label locationValue,
                Label phoneValue,
                Label categoryValue,
                Label submittedAtValue,
                Label assignedAtValue,
                Label acceptedAtValue,
                Label completedAtValue,
                Label urgeCountValue,
                Label workOrderNoValue,
                Label workerNameValue,
                Label assignmentNoteValue,
                Label imageCountValue,
                Label descriptionValue,
                HBox progressTrack,
                Label progressSummaryValue,
                VBox timelineBox,
                FlowPane imageThumbPane,
                ImageView previewImage,
                StackPane previewFrame,
                Label previewHint,
                Label actionBanner,
                Label feedbackBanner,
                AppDropdown<Integer> ratingBox,
                TextArea feedbackArea,
                CheckBox anonymousBox,
                Button urgeButton,
                Button cancelButton,
                Button feedbackButton,
                List<StackPane> thumbShells
        ) {
            this.currentDetail = currentDetail;
            this.requestNoValue = requestNoValue;
            this.statusValue = statusValue;
            this.statusHintValue = statusHintValue;
            this.locationValue = locationValue;
            this.phoneValue = phoneValue;
            this.categoryValue = categoryValue;
            this.submittedAtValue = submittedAtValue;
            this.assignedAtValue = assignedAtValue;
            this.acceptedAtValue = acceptedAtValue;
            this.completedAtValue = completedAtValue;
            this.urgeCountValue = urgeCountValue;
            this.workOrderNoValue = workOrderNoValue;
            this.workerNameValue = workerNameValue;
            this.assignmentNoteValue = assignmentNoteValue;
            this.imageCountValue = imageCountValue;
            this.descriptionValue = descriptionValue;
            this.progressTrack = progressTrack;
            this.progressSummaryValue = progressSummaryValue;
            this.timelineBox = timelineBox;
            this.imageThumbPane = imageThumbPane;
            this.previewImage = previewImage;
            this.previewFrame = previewFrame;
            this.previewHint = previewHint;
            this.actionBanner = actionBanner;
            this.feedbackBanner = feedbackBanner;
            this.ratingBox = ratingBox;
            this.feedbackArea = feedbackArea;
            this.anonymousBox = anonymousBox;
            this.urgeButton = urgeButton;
            this.cancelButton = cancelButton;
            this.feedbackButton = feedbackButton;
            this.thumbShells = thumbShells;
        }
    }
}
