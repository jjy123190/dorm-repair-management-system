package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * 学生报修模块先把表单字段和列表工作区预留好。
 */
public class StudentRepairModule implements WorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AppContext appContext;

    public StudentRepairModule(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String moduleName() {
        return "学生报修";
    }

    @Override
    public Parent createView() {
        VBox container = new VBox(18);
        container.setPadding(new Insets(24));

        Label titleLabel = new Label("学生报修工作区");
        titleLabel.getStyleClass().add("section-title");

        Label descriptionLabel = new Label("这个页面先固定表单字段、图片上传位和历史报修列表，后续同学只需要补按钮事件和校验。");
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("plain-text");

        ListView<String> fieldList = new ListView<>();
        fieldList.setItems(FXCollections.observableArrayList(
                "宿舍楼栋 buildingNoSnapshot",
                "房间号 roomNoSnapshot",
                "联系电话 contactPhone",
                "故障类型 faultCategory",
                "故障描述 description",
                "图片列表 imageUrls"
        ));
        fieldList.setPrefHeight(180);

        TableView<RecentRepairRequestView> historyTable = new TableView<>();
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setItems(FXCollections.observableArrayList(
                appContext.repairRequestService().listLatestSubmittedRequests(8)
        ));
        historyTable.getColumns().addAll(
                createTextColumn("报修单号", "requestNo"),
                createTextColumn("学生", "studentName"),
                createTextColumn("宿舍", "locationText"),
                createDateTimeColumn("提交时间")
        );

        HBox contentRow = new HBox(18, wrapSection("计划表单字段", fieldList), wrapSection("最近报修记录", historyTable));
        HBox.setHgrow(contentRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(contentRow.getChildren().get(1), Priority.ALWAYS);

        container.getChildren().addAll(titleLabel, descriptionLabel, contentRow);
        return container;
    }

    private VBox wrapSection(String title, javafx.scene.Node content) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("subsection-title");

        VBox box = new VBox(12, titleLabel, content);
        box.getStyleClass().add("panel-box");
        box.setPadding(new Insets(18));
        VBox.setVgrow(content, Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private TableColumn<RecentRepairRequestView, String> createTextColumn(String title, String property) {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    private TableColumn<RecentRepairRequestView, String> createDateTimeColumn(String title) {
        TableColumn<RecentRepairRequestView, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell ->
                javafx.beans.binding.Bindings.createStringBinding(() -> {
                    if (cell.getValue().getSubmittedAt() == null) {
                        return "";
                    }
                    return TIME_FORMATTER.format(cell.getValue().getSubmittedAt());
                })
        );
        return column;
    }
}
