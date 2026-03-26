package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.DemoAccountDirectory;
import com.scau.dormrepair.common.DemoAccountDirectory.DemoAccount;
import com.scau.dormrepair.domain.command.SubmitRepairFeedbackCommand;
import com.scau.dormrepair.domain.enums.RepairRequestStatus;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.StudentRepairDetailView;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.support.UiDisplayText;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import javafx.scene.control.ComboBox;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * 学生报修记录页，负责查看本人历史记录、图片预览和评价提交。
 */
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
            throw new IllegalStateException("学生身份未初始化，无法进入报修记录模块。");
        }

        TableView<RecentRepairRequestView> historyTable = buildHistoryTable();
        VBox detailPanel = buildDetailPanel(currentStudent, historyTable);

        Button refreshButton = new Button("刷新记录");
        refreshButton.getStyleClass().add("nav-button");
        refreshButton.setOnAction(event -> refreshHistory(historyTable, currentStudent.id()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(12, spacer, refreshButton);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        GridPane workspace = createRatioWorkspace(
                50,
                50,
                wrapPanel("我的报修记录", new VBox(12, toolbar, historyTable)),
                wrapPanel("当前记录详情", detailPanel)
        );

        refreshHistory(historyTable, currentStudent.id());

        VBox content = new VBox(18, workspace);
        content.setFillWidth(true);
        return createPage("学生报修记录", "", content);
    }

    private TableView<RecentRepairRequestView> buildHistoryTable() {
        TableView<RecentRepairRequestView> historyTable = new TableView<>();
        historyTable.getColumns().addAll(
                createTextColumn("报修单号", "requestNo"),
                createTextColumn("宿舍", "locationText"),
                createFaultColumn(),
                createStatusColumn(),
                createDateTimeColumn()
        );
        configureFixedTable(historyTable, 460, 1.618, 1.382, 1.236, 0.764, 1.382);
        return historyTable;
    }

    private void refreshHistory(TableView<RecentRepairRequestView> historyTable, Long studentId) {
        List<RecentRepairRequestView> historyRows =
                appContext.repairRequestService().listStudentSubmittedRequests(studentId, 20);
        historyTable.setItems(FXCollections.observableArrayList(historyRows));
        if (!historyRows.isEmpty()) {
            historyTable.getSelectionModel().selectFirst();
        } else {
            historyTable.getSelectionModel().clearSelection();
        }
    }

    private VBox buildDetailPanel(DemoAccount currentStudent, TableView<RecentRepairRequestView> historyTable) {
        Label requestNoValue = createDetailValueLabel("请先在左侧表格里选择一条记录。");
        Label statusValue = createDetailValueLabel("");
        Label locationValue = createDetailValueLabel("");
        Label phoneValue = createDetailValueLabel("");
        Label categoryValue = createDetailValueLabel("");
        Label submittedAtValue = createDetailValueLabel("");
        Label completedAtValue = createDetailValueLabel("");
        Label imageCountValue = createDetailValueLabel("");
        Label descriptionValue = createDetailValueLabel("");
        Label feedbackStatusValue = createDetailValueLabel("当前还没有评价记录。");
        HBox progressTrack = new HBox(10);
        progressTrack.setAlignment(Pos.CENTER_LEFT);
        progressTrack.getChildren().addAll(
                createProgressStep("1", "已提交", "学生已成功发起报修"),
                createProgressStep("2", "已派单", "管理员已接入处理流程"),
                createProgressStep("3", "处理中", "维修人员正在处理问题"),
                createProgressStep("4", "已完成", "本次报修处理结束")
        );
        Label progressSummaryValue = createDetailValueLabel("请选择一条记录查看当前进度。");

        FlowPane imageThumbPane = new FlowPane(10, 10);
        imageThumbPane.setPrefWrapLength(260);
        imageThumbPane.setPadding(new Insets(4, 0, 4, 0));

        ImageView previewImage = new ImageView();
        previewImage.setFitWidth(260);
        previewImage.setFitHeight(180);
        previewImage.setPreserveRatio(true);
        previewImage.setSmooth(true);
        previewImage.setVisible(false);
        previewImage.setManaged(false);

        Label previewHint = createDetailValueLabel("当前没有可预览图片。");

        ComboBox<Integer> ratingBox = new ComboBox<>();
        ratingBox.getItems().addAll(5, 4, 3, 2, 1);
        ratingBox.setPromptText("选择评分");
        ratingBox.setMaxWidth(Double.MAX_VALUE);

        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("填写处理结果评价，已完成后可提交。");
        feedbackArea.setWrapText(true);
        feedbackArea.setPrefRowCount(3);

        CheckBox anonymousBox = new CheckBox("匿名评价");
        Button feedbackButton = new Button("提交评价");
        feedbackButton.getStyleClass().add("nav-button");
        feedbackButton.setDisable(true);

        VBox feedbackBox = new VBox(
                10,
                createDetailBlock("评价状态", feedbackStatusValue),
                createFieldBlock("评分", ratingBox),
                createFieldBlock("评价内容", feedbackArea),
                anonymousBox,
                feedbackButton
        );

        DetailState detailState = new DetailState(
                null,
                requestNoValue,
                statusValue,
                locationValue,
                phoneValue,
                categoryValue,
                submittedAtValue,
                completedAtValue,
                imageCountValue,
                descriptionValue,
                feedbackStatusValue,
                progressTrack,
                progressSummaryValue,
                imageThumbPane,
                previewImage,
                previewHint,
                ratingBox,
                feedbackArea,
                anonymousBox,
                feedbackButton
        );

        Runnable clearDetail = () -> {
            requestNoValue.setText("请先在左侧表格里选择一条记录。");
            statusValue.setText("");
            locationValue.setText("");
            phoneValue.setText("");
            categoryValue.setText("");
            submittedAtValue.setText("");
            completedAtValue.setText("");
            imageCountValue.setText("");
            descriptionValue.setText("");
            feedbackStatusValue.setText("当前还没有评价记录。");
            progressSummaryValue.setText("请选择一条记录查看当前进度。");
            imageThumbPane.getChildren().clear();
            previewImage.setImage(null);
            previewImage.setVisible(false);
            previewImage.setManaged(false);
            previewHint.setText("当前没有可预览图片。");
            updateProgressTrack(progressTrack, null);
            ratingBox.getSelectionModel().clearSelection();
            feedbackArea.clear();
            anonymousBox.setSelected(false);
            feedbackButton.setDisable(true);
        };

        historyTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                clearDetail.run();
                return;
            }

            try {
                StudentRepairDetailView detailView =
                        appContext.repairRequestService().getStudentRequestDetail(currentStudent.id(), newValue.getId());
                detailState.currentDetail = detailView;
                renderDetail(detailState, detailView);
            } catch (RuntimeException exception) {
                detailState.currentDetail = null;
                clearDetail.run();
                UiAlerts.error("加载详情失败", exception.getMessage());
            }
        });

        feedbackButton.setOnAction(event -> {
            if (detailState.currentDetail == null) {
                UiAlerts.error("提交失败", "请先选择一条已完成的报修记录。");
                return;
            }

            try {
                SubmitRepairFeedbackCommand command = new SubmitRepairFeedbackCommand(
                        detailState.currentDetail.getId(),
                        ratingBox.getValue(),
                        feedbackArea.getText(),
                        anonymousBox.isSelected()
                );
                appContext.repairRequestService().submitFeedback(command);
                StudentRepairDetailView refreshed =
                        appContext.repairRequestService().getStudentRequestDetail(currentStudent.id(), detailState.currentDetail.getId());
                detailState.currentDetail = refreshed;
                renderDetail(detailState, refreshed);
                UiAlerts.info("评价成功", "本条报修的评价已保存。");
            } catch (RuntimeException exception) {
                UiAlerts.error("评价失败", exception.getMessage());
            }
        });

        ScrollPane imageScrollPane = new ScrollPane(imageThumbPane);
        imageScrollPane.setFitToWidth(true);
        imageScrollPane.setPrefHeight(120);
        imageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox detailPanel = new VBox(
                14,
                createDetailBlock("报修单号", requestNoValue),
                createDetailBlock("状态", statusValue),
                createDetailBlock("宿舍位置", locationValue),
                createDetailBlock("联系电话", phoneValue),
                createDetailBlock("故障类型", categoryValue),
                createFieldBlock("处理进度", new VBox(10, progressSummaryValue, progressTrack)),
                createDetailBlock("提交时间", submittedAtValue),
                createDetailBlock("完成时间", completedAtValue),
                createDetailBlock("图片数量", imageCountValue),
                createDetailBlock("故障描述", descriptionValue),
                createFieldBlock("图片缩略图", imageScrollPane),
                createFieldBlock("当前预览", new VBox(8, previewHint, previewImage)),
                feedbackBox
        );
        detailPanel.setFillWidth(true);
        clearDetail.run();
        return detailPanel;
    }

    private void renderDetail(DetailState detailState, StudentRepairDetailView detailView) {
        detailState.requestNoValue.setText(detailView.getRequestNo());
        detailState.statusValue.setText(UiDisplayText.repairRequestStatus(detailView.getStatus()));
        detailState.locationValue.setText(detailView.getLocationText());
        detailState.phoneValue.setText(nullToEmpty(detailView.getContactPhone()));
        detailState.categoryValue.setText(UiDisplayText.faultCategory(detailView.getFaultCategory()));
        detailState.submittedAtValue.setText(formatTime(detailView.getSubmittedAt()));
        detailState.completedAtValue.setText(formatTime(detailView.getCompletedAt()));
        detailState.imageCountValue.setText(String.valueOf(detailView.getImageUrls().size()));
        detailState.descriptionValue.setText(nullToEmpty(detailView.getDescription()));
        detailState.progressSummaryValue.setText(buildProgressSummary(detailView));
        updateProgressTrack(detailState.progressTrack, detailView.getStatus());

        renderImagePreview(detailState, detailView.getImageUrls());

        if (detailView.hasFeedback()) {
            detailState.feedbackStatusValue.setText(
                    "已评价："
                            + detailView.getFeedbackRating()
                            + " 星"
                            + (Boolean.TRUE.equals(detailView.getFeedbackAnonymousFlag()) ? "（匿名）" : "")
                            + (detailView.getFeedbackComment() == null || detailView.getFeedbackComment().isBlank()
                            ? ""
                            : " / " + detailView.getFeedbackComment().trim())
            );
            detailState.ratingBox.setValue(detailView.getFeedbackRating());
            detailState.feedbackArea.setText(nullToEmpty(detailView.getFeedbackComment()));
            detailState.anonymousBox.setSelected(Boolean.TRUE.equals(detailView.getFeedbackAnonymousFlag()));
            detailState.feedbackButton.setDisable(true);
        } else {
            boolean canFeedback = detailView.getStatus() == RepairRequestStatus.COMPLETED;
            detailState.feedbackStatusValue.setText(
                    canFeedback ? "本条报修已完成，可以提交评价。" : "只有已完成的报修记录才可以评价。"
            );
            detailState.ratingBox.getSelectionModel().clearSelection();
            detailState.feedbackArea.clear();
            detailState.anonymousBox.setSelected(false);
            detailState.feedbackButton.setDisable(!canFeedback);
        }
    }

    private void renderImagePreview(DetailState detailState, List<String> imageUrls) {
        detailState.imageThumbPane.getChildren().clear();
        detailState.previewImage.setImage(null);
        detailState.previewImage.setVisible(false);
        detailState.previewImage.setManaged(false);

        if (imageUrls == null || imageUrls.isEmpty()) {
            detailState.previewHint.setText("当前没有可预览图片。");
            return;
        }

        detailState.previewHint.setText("点击下方缩略图切换预览。");
        boolean firstImageLoaded = false;
        for (String imageUrl : imageUrls) {
            Path imagePath = resolveProjectPath(imageUrl);
            if (imagePath == null || !Files.exists(imagePath)) {
                Label missingLabel = new Label("图片缺失：" + imageUrl);
                missingLabel.getStyleClass().add("helper-text");
                detailState.imageThumbPane.getChildren().add(missingLabel);
                continue;
            }

            Image image = new Image(imagePath.toUri().toString(), 90, 70, true, true);
            ImageView thumb = new ImageView(image);
            thumb.setFitWidth(90);
            thumb.setFitHeight(70);
            thumb.setPreserveRatio(true);
            thumb.getStyleClass().add("student-thumb");
            thumb.setOnMouseClicked(event -> showPreview(detailState.previewImage, detailState.previewHint, image));
            detailState.imageThumbPane.getChildren().add(thumb);

            if (!firstImageLoaded) {
                showPreview(detailState.previewImage, detailState.previewHint, image);
                firstImageLoaded = true;
            }
        }
    }

    private void showPreview(ImageView previewImage, Label previewHint, Image image) {
        previewImage.setImage(image);
        previewImage.setVisible(true);
        previewImage.setManaged(true);
        previewHint.setText("正在预览已上传图片。");
    }

    private String buildProgressSummary(StudentRepairDetailView detailView) {
        if (detailView.getStatus() == null) {
            return "当前状态暂不可用。";
        }
        return switch (detailView.getStatus()) {
            case SUBMITTED -> "当前处于“已提交”，等待管理员审核并派单。";
            case ASSIGNED -> "当前处于“已派单”，说明管理员已经把工单分配给维修人员。";
            case IN_PROGRESS -> "当前处于“处理中”，维修人员正在跟进本次报修。";
            case COMPLETED -> "当前处于“已完成”，如果确认处理结果，可在下方提交评价。";
            case REJECTED -> "当前报修已被驳回，建议根据说明重新提交。";
            case CANCELLED -> "当前报修已取消，本次流程已经结束。";
        };
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
        return shell;
    }

    private void updateProgressTrack(HBox progressTrack, RepairRequestStatus status) {
        int completedIndex;
        if (status == null) {
            completedIndex = -1;
        } else {
            completedIndex = switch (status) {
                case SUBMITTED -> 0;
                case ASSIGNED -> 1;
                case IN_PROGRESS -> 2;
                case COMPLETED -> 3;
                case REJECTED, CANCELLED -> -1;
            };
        }

        for (int index = 0; index < progressTrack.getChildren().size(); index++) {
            StackPane shell = (StackPane) progressTrack.getChildren().get(index);
            VBox card = (VBox) shell.getChildren().get(0);
            card.getStyleClass().removeAll("student-progress-step-active", "student-progress-step-done", "student-progress-step-closed");

            if (completedIndex >= 0) {
                if (index < completedIndex) {
                    card.getStyleClass().add("student-progress-step-done");
                } else if (index == completedIndex) {
                    card.getStyleClass().add("student-progress-step-active");
                }
            } else if (status == RepairRequestStatus.REJECTED || status == RepairRequestStatus.CANCELLED) {
                card.getStyleClass().add("student-progress-step-closed");
            }
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

    private TableColumn<RecentRepairRequestView, String> createTextColumn(String title, String property) {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    private TableColumn<RecentRepairRequestView, String> createFaultColumn() {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>("故障类型");
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

    private static final class DetailState {
        private StudentRepairDetailView currentDetail;
        private final Label requestNoValue;
        private final Label statusValue;
        private final Label locationValue;
        private final Label phoneValue;
        private final Label categoryValue;
        private final Label submittedAtValue;
        private final Label completedAtValue;
        private final Label imageCountValue;
        private final Label descriptionValue;
        private final Label feedbackStatusValue;
        private final HBox progressTrack;
        private final Label progressSummaryValue;
        private final FlowPane imageThumbPane;
        private final ImageView previewImage;
        private final Label previewHint;
        private final ComboBox<Integer> ratingBox;
        private final TextArea feedbackArea;
        private final CheckBox anonymousBox;
        private final Button feedbackButton;

        private DetailState(
                StudentRepairDetailView currentDetail,
                Label requestNoValue,
                Label statusValue,
                Label locationValue,
                Label phoneValue,
                Label categoryValue,
                Label submittedAtValue,
                Label completedAtValue,
                Label imageCountValue,
                Label descriptionValue,
                Label feedbackStatusValue,
                HBox progressTrack,
                Label progressSummaryValue,
                FlowPane imageThumbPane,
                ImageView previewImage,
                Label previewHint,
                ComboBox<Integer> ratingBox,
                TextArea feedbackArea,
                CheckBox anonymousBox,
                Button feedbackButton
        ) {
            this.currentDetail = currentDetail;
            this.requestNoValue = requestNoValue;
            this.statusValue = statusValue;
            this.locationValue = locationValue;
            this.phoneValue = phoneValue;
            this.categoryValue = categoryValue;
            this.submittedAtValue = submittedAtValue;
            this.completedAtValue = completedAtValue;
            this.imageCountValue = imageCountValue;
            this.descriptionValue = descriptionValue;
            this.feedbackStatusValue = feedbackStatusValue;
            this.progressTrack = progressTrack;
            this.progressSummaryValue = progressSummaryValue;
            this.imageThumbPane = imageThumbPane;
            this.previewImage = previewImage;
            this.previewHint = previewHint;
            this.ratingBox = ratingBox;
            this.feedbackArea = feedbackArea;
            this.anonymousBox = anonymousBox;
            this.feedbackButton = feedbackButton;
        }
    }
}
