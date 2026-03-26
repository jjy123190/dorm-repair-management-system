package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.DemoAccountDirectory;
import com.scau.dormrepair.common.DemoAccountDirectory.DemoAccount;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.domain.view.StudentRepairDetailView;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.support.UiDisplayText;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * 学生报修记录页单独成页，负责查看本人历史记录和单条详情。
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

        List<RecentRepairRequestView> historyRows =
                appContext.repairRequestService().listStudentSubmittedRequests(currentStudent.id(), 20);

        TableView<RecentRepairRequestView> historyTable = buildHistoryTable();
        historyTable.setItems(FXCollections.observableArrayList(historyRows));

        VBox detailPanel = buildDetailPanel(currentStudent, historyTable);
        GridPane workspace = createRatioWorkspace(
                52,
                48,
                wrapPanel("最近报修记录", historyTable),
                wrapPanel("记录详情", detailPanel)
        );

        if (!historyRows.isEmpty()) {
            historyTable.getSelectionModel().selectFirst();
        }

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
        Label imageUrlsValue = createDetailValueLabel("当前还没有图片记录。");

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
            imageUrlsValue.setText("当前还没有图片记录。");
        };

        historyTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                clearDetail.run();
                return;
            }

            try {
                StudentRepairDetailView detailView =
                        appContext.repairRequestService().getStudentRequestDetail(currentStudent.id(), newValue.getId());
                requestNoValue.setText(detailView.getRequestNo());
                statusValue.setText(UiDisplayText.repairRequestStatus(detailView.getStatus()));
                locationValue.setText(detailView.getLocationText());
                phoneValue.setText(nullToEmpty(detailView.getContactPhone()));
                categoryValue.setText(UiDisplayText.faultCategory(detailView.getFaultCategory()));
                submittedAtValue.setText(formatTime(detailView.getSubmittedAt()));
                completedAtValue.setText(formatTime(detailView.getCompletedAt()));
                imageCountValue.setText(String.valueOf(detailView.getImageUrls().size()));
                descriptionValue.setText(nullToEmpty(detailView.getDescription()));
                imageUrlsValue.setText(
                        detailView.getImageUrls().isEmpty()
                                ? "当前还没有图片记录。"
                                : String.join("\n", detailView.getImageUrls())
                );
            } catch (RuntimeException exception) {
                clearDetail.run();
                UiAlerts.error("加载详情失败", exception.getMessage());
            }
        });

        VBox detailPanel = new VBox(
                14,
                createDetailBlock("报修单号", requestNoValue),
                createDetailBlock("状态", statusValue),
                createDetailBlock("宿舍位置", locationValue),
                createDetailBlock("联系电话", phoneValue),
                createDetailBlock("故障类型", categoryValue),
                createDetailBlock("提交时间", submittedAtValue),
                createDetailBlock("完成时间", completedAtValue),
                createDetailBlock("图片数量", imageCountValue),
                createDetailBlock("故障描述", descriptionValue),
                createDetailBlock("图片地址", imageUrlsValue)
        );
        detailPanel.setFillWidth(true);
        clearDetail.run();
        return detailPanel;
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
}
