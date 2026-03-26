package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.DemoAccountDirectory;
import com.scau.dormrepair.common.DemoAccountDirectory.DemoAccount;
import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
import com.scau.dormrepair.domain.entity.DormBuilding;
import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.domain.view.RecentRepairRequestView;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.support.ProjectImageStore;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.support.UiDisplayText;
import com.scau.dormrepair.ui.support.UiMotion;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;

/**
 * 学生报修页，负责提交新报修并查看最近提交概况。
 */
public class StudentRepairModule extends AbstractWorkbenchModule {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StudentRepairModule(AppContext appContext) {
        super(appContext);
    }

    @Override
    public String moduleCode() {
        return "student-repair";
    }

    @Override
    public String moduleName() {
        return "提交报修";
    }

    @Override
    public String moduleDescription() {
        return "按宿舍区、楼栋、房间、故障类型和图片完成报修提交，并在右侧实时查看当前填写概况。";
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
            throw new IllegalStateException("学生身份未初始化，无法进入报修模块。");
        }

        TableView<RecentRepairRequestView> recentTable = buildRecentTable();
        refreshRecentRequests(recentTable, currentStudent.id());

        GridPane workspace = buildRepairWorkspace(currentStudent, recentTable);
        VBox content = new VBox(18, workspace);
        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);

        return createPage("学生报修工作区", "", content);
    }

    private GridPane buildRepairWorkspace(DemoAccount currentStudent, TableView<RecentRepairRequestView> recentTable) {
        ComboBox<String> dormAreaBox = createDormAreaBox();
        ComboBox<DormBuilding> buildingBox = createDormBuildingBox();

        TextField roomField = new TextField();
        roomField.setPromptText("例如 402");

        TextField phoneField = new TextField();
        phoneField.setPromptText("联系电话");

        ComboBox<FaultCategory> faultCategoryBox = createFaultCategoryBox();

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("请具体说明故障现象、是否紧急、是否影响正常生活。");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);

        List<File> selectedImageFiles = new ArrayList<>();
        Label draftLocationValue = createDraftValueLabel("待补全");
        Label draftFaultValue = createDraftValueLabel("待选择");
        Label draftImageCountValue = createDraftValueLabel("0 张");
        Label draftRecentHintValue = createDraftValueLabel("提交后可在“报修记录”模块查看完整详情。");
        VBox imageUploadBox = createImageUploadBox(selectedImageFiles, draftImageCountValue);

        dormAreaBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            reloadBuildingsByArea(newValue, buildingBox);
            refreshDraftLocation(dormAreaBox, buildingBox, roomField, draftLocationValue);
        });
        buildingBox.valueProperty().addListener((observable, oldValue, newValue) ->
                refreshDraftLocation(dormAreaBox, buildingBox, roomField, draftLocationValue)
        );
        roomField.textProperty().addListener((observable, oldValue, newValue) ->
                refreshDraftLocation(dormAreaBox, buildingBox, roomField, draftLocationValue)
        );
        faultCategoryBox.valueProperty().addListener((observable, oldValue, newValue) ->
                draftFaultValue.setText(newValue == null ? "待选择" : UiDisplayText.faultCategory(newValue))
        );

        Button resetButton = new Button("清空表单");
        resetButton.getStyleClass().add("nav-button");

        Node submitButton = FusionUiFactory.createPrimaryButton("提交报修申请", 180, 40, () -> {
            try {
                ProjectImageStore.validateImageFiles(selectedImageFiles);

                DormBuilding selectedBuilding = buildingBox.getValue();
                CreateRepairRequestCommand command = new CreateRepairRequestCommand(
                        currentStudent.id(),
                        appContext.appSession().getDisplayName(),
                        phoneField.getText(),
                        null,
                        dormAreaBox.getValue(),
                        selectedBuilding == null ? null : selectedBuilding.getBuildingNo(),
                        roomField.getText(),
                        faultCategoryBox.getValue(),
                        descriptionArea.getText(),
                        ProjectImageStore.copyImagesToProject(selectedImageFiles)
                );

                Long repairRequestId = appContext.repairRequestService().create(command);
                clearAfterSubmit(
                        dormAreaBox,
                        buildingBox,
                        roomField,
                        phoneField,
                        descriptionArea,
                        selectedImageFiles,
                        imageUploadBox,
                        faultCategoryBox
                );
                draftFaultValue.setText("待选择");
                draftImageCountValue.setText("0 张");
                draftRecentHintValue.setText("本次报修已提交，右侧最近记录已自动刷新。");
                refreshDraftLocation(dormAreaBox, buildingBox, roomField, draftLocationValue);
                refreshRecentRequests(recentTable, currentStudent.id());
                UiAlerts.info(
                        "提交成功",
                        "报修申请已保存，记录 ID=" + repairRequestId + "。最近记录已同步刷新。"
                );
            } catch (RuntimeException exception) {
                UiAlerts.error("提交失败", exception.getMessage());
            }
        }).getNode();

        resetButton.setOnAction(event -> {
            clearAfterSubmit(
                    dormAreaBox,
                    buildingBox,
                    roomField,
                    phoneField,
                    descriptionArea,
                    selectedImageFiles,
                    imageUploadBox,
                    faultCategoryBox
            );
            draftLocationValue.setText("待补全");
            draftFaultValue.setText("待选择");
            draftImageCountValue.setText("0 张");
            draftRecentHintValue.setText("提交后可在“报修记录”模块查看完整详情。");
        });

        GridPane dormRow = new GridPane();
        dormRow.setHgap(16);
        dormRow.setMinWidth(0);
        dormRow.getColumnConstraints().addAll(percentColumn(50), percentColumn(50));
        dormRow.add(createFieldBlock("宿舍区", dormAreaBox), 0, 0);
        dormRow.add(createFieldBlock("宿舍楼", buildingBox), 1, 0);

        GridPane contactRow = new GridPane();
        contactRow.setHgap(16);
        contactRow.setMinWidth(0);
        contactRow.getColumnConstraints().addAll(percentColumn(50), percentColumn(50));
        contactRow.add(createFieldBlock("房间号", roomField), 0, 0);
        contactRow.add(createFieldBlock("联系电话", phoneField), 1, 0);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionRow = new HBox(12, resetButton, spacer, submitButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setMinWidth(0);

        VBox formBox = new VBox(
                16,
                dormRow,
                contactRow,
                createFieldBlock("故障类型", faultCategoryBox),
                createFieldBlock("故障描述", descriptionArea),
                createFieldBlock("图片上传", imageUploadBox),
                actionRow
        );
        formBox.setFillWidth(true);
        formBox.setMaxWidth(Double.MAX_VALUE);

        VBox summaryBox = new VBox(
                14,
                createDetailBlock("当前学生", createDraftValueLabel(currentStudent.displayName())),
                createDetailBlock("报修位置", draftLocationValue),
                createDetailBlock("故障类型", draftFaultValue),
                createDetailBlock("已选图片", draftImageCountValue),
                createDetailBlock("提交提示", draftRecentHintValue)
        );

        VBox sideBox = new VBox(
                16,
                wrapPanel("填写概况", summaryBox),
                wrapPanel("最近提交记录", recentTable)
        );
        sideBox.setFillWidth(true);

        return createRatioWorkspace(
                58,
                42,
                wrapPanel("填写报修表单", formBox),
                sideBox
        );
    }

    private ComboBox<String> createDormAreaBox() {
        ComboBox<String> dormAreaBox = new ComboBox<>();
        dormAreaBox.getItems().setAll(appContext.dormCatalogService().listDormAreas());
        dormAreaBox.setPromptText("选择宿舍区");
        dormAreaBox.setVisibleRowCount(5);
        dormAreaBox.setMinHeight(44);
        dormAreaBox.setPrefHeight(44);
        dormAreaBox.setMaxHeight(44);
        dormAreaBox.setMaxWidth(Double.MAX_VALUE);
        UiMotion.installSmoothDropdown(dormAreaBox);
        return dormAreaBox;
    }

    private ComboBox<DormBuilding> createDormBuildingBox() {
        ComboBox<DormBuilding> buildingBox = new ComboBox<>();
        buildingBox.setPromptText("先选宿舍区再选宿舍楼");
        buildingBox.setDisable(true);
        buildingBox.setVisibleRowCount(5);
        buildingBox.setMinHeight(44);
        buildingBox.setPrefHeight(44);
        buildingBox.setMaxHeight(44);
        buildingBox.setMaxWidth(Double.MAX_VALUE);
        buildingBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(DormBuilding building) {
                return building == null ? "" : building.getDisplayName();
            }

            @Override
            public DormBuilding fromString(String string) {
                return null;
            }
        });
        buildingBox.setButtonCell(createDormBuildingCell());
        buildingBox.setCellFactory(ignored -> createDormBuildingCell());
        UiMotion.installSmoothDropdown(buildingBox);
        return buildingBox;
    }

    private ComboBox<FaultCategory> createFaultCategoryBox() {
        ComboBox<FaultCategory> faultCategoryBox = new ComboBox<>();
        faultCategoryBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(FaultCategory value) {
                return UiDisplayText.faultCategory(value);
            }

            @Override
            public FaultCategory fromString(String string) {
                return null;
            }
        });
        faultCategoryBox.getItems().addAll(FaultCategory.values());
        faultCategoryBox.setPromptText("选择故障类型");
        faultCategoryBox.setVisibleRowCount(7);
        faultCategoryBox.setMinHeight(44);
        faultCategoryBox.setPrefHeight(44);
        faultCategoryBox.setMaxHeight(44);
        faultCategoryBox.setMaxWidth(Double.MAX_VALUE);
        faultCategoryBox.setButtonCell(createFaultCategoryCell());
        faultCategoryBox.setCellFactory(ignored -> createFaultCategoryCell());
        UiMotion.installSmoothDropdown(faultCategoryBox);
        return faultCategoryBox;
    }

    private TableView<RecentRepairRequestView> buildRecentTable() {
        TableView<RecentRepairRequestView> recentTable = new TableView<>();
        recentTable.getColumns().addAll(
                createTextColumn("报修单号", "requestNo"),
                createTextColumn("宿舍", "locationText"),
                createStatusColumn(),
                createDateTimeColumn()
        );
        configureFixedTable(recentTable, 268, 1.382, 1.382, 0.764, 1.236);
        return recentTable;
    }

    private void refreshRecentRequests(TableView<RecentRepairRequestView> recentTable, Long studentId) {
        recentTable.setItems(FXCollections.observableArrayList(
                appContext.repairRequestService().listStudentSubmittedRequests(studentId, 6)
        ));
    }

    private void reloadBuildingsByArea(String dormArea, ComboBox<DormBuilding> buildingBox) {
        buildingBox.getItems().clear();
        buildingBox.getSelectionModel().clearSelection();

        if (dormArea == null || dormArea.isBlank()) {
            buildingBox.setDisable(true);
            buildingBox.setPromptText("先选宿舍区再选宿舍楼");
            return;
        }

        buildingBox.getItems().setAll(appContext.dormCatalogService().listBuildingsByArea(dormArea));
        buildingBox.setDisable(false);
        buildingBox.setPromptText("选择 " + dormArea + " 的楼栋");
    }

    private void clearAfterSubmit(
            ComboBox<String> dormAreaBox,
            ComboBox<DormBuilding> buildingBox,
            TextField roomField,
            TextField phoneField,
            TextArea descriptionArea,
            List<File> selectedImageFiles,
            VBox imageUploadBox,
            ComboBox<FaultCategory> faultCategoryBox
    ) {
        dormAreaBox.getSelectionModel().clearSelection();
        buildingBox.getItems().clear();
        buildingBox.getSelectionModel().clearSelection();
        buildingBox.setDisable(true);
        buildingBox.setPromptText("先选宿舍区再选宿舍楼");
        roomField.clear();
        phoneField.clear();
        descriptionArea.clear();
        selectedImageFiles.clear();
        ImageUploadState uploadState = (ImageUploadState) imageUploadBox.getUserData();
        if (uploadState != null) {
            refreshImagePreview(uploadState.previewBox(), selectedImageFiles, uploadState.uploadCountLabel(), uploadState.draftCountLabel());
        }
        faultCategoryBox.getSelectionModel().clearSelection();
    }

    private VBox createImageUploadBox(List<File> selectedImageFiles, Label draftImageCountLabel) {
        Label imageCountLabel = createDraftValueLabel("已选 0 / " + ProjectImageStore.MAX_IMAGE_COUNT + " 张");

        Button chooseButton = new Button("选择图片");
        chooseButton.getStyleClass().add("nav-button");

        Button clearButton = new Button("清空图片");
        clearButton.getStyleClass().add("nav-button");

        Label helperLabel = new Label(
                "支持 png、jpg、jpeg、webp、bmp、gif，最多 "
                        + ProjectImageStore.MAX_IMAGE_COUNT
                        + " 张，单张不超过 8MB。提交时会自动复制到项目 pics/ 目录。"
        );
        helperLabel.getStyleClass().add("helper-text");
        helperLabel.setWrapText(true);

        VBox previewBox = new VBox(8);
        previewBox.getStyleClass().add("upload-preview-box");
        previewBox.setFillWidth(true);
        previewBox.setMaxWidth(Double.MAX_VALUE);
        refreshImagePreview(previewBox, selectedImageFiles, imageCountLabel, draftImageCountLabel);

        chooseButton.setOnAction(event -> {
            List<File> chosenFiles = chooseImageFiles(chooseButton);
            if (chosenFiles.isEmpty()) {
                return;
            }

            for (File chosenFile : chosenFiles) {
                boolean exists = selectedImageFiles.stream()
                        .anyMatch(existingFile -> existingFile.toPath().equals(chosenFile.toPath()));
                if (!exists) {
                    selectedImageFiles.add(chosenFile);
                }
            }

            try {
                ProjectImageStore.validateImageFiles(selectedImageFiles);
                refreshImagePreview(previewBox, selectedImageFiles, imageCountLabel, draftImageCountLabel);
            } catch (RuntimeException exception) {
                selectedImageFiles.removeIf(file -> chosenFiles.stream()
                        .anyMatch(chosen -> chosen.toPath().equals(file.toPath())));
                refreshImagePreview(previewBox, selectedImageFiles, imageCountLabel, draftImageCountLabel);
                UiAlerts.error("图片校验失败", exception.getMessage());
            }
        });

        clearButton.setOnAction(event -> {
            selectedImageFiles.clear();
            refreshImagePreview(previewBox, selectedImageFiles, imageCountLabel, draftImageCountLabel);
        });

        HBox toolbar = new HBox(12, chooseButton, clearButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        VBox uploadBox = new VBox(10, helperLabel, imageCountLabel, toolbar, previewBox);
        uploadBox.setUserData(new ImageUploadState(previewBox, imageCountLabel, draftImageCountLabel));
        return uploadBox;
    }

    private List<File> chooseImageFiles(Node ownerNode) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择报修图片");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "图片文件",
                        "*.png",
                        "*.jpg",
                        "*.jpeg",
                        "*.webp",
                        "*.bmp",
                        "*.gif"
                )
        );

        Window ownerWindow = ownerNode.getScene() == null ? null : ownerNode.getScene().getWindow();
        List<File> selectedFiles = chooser.showOpenMultipleDialog(ownerWindow);
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return List.of();
        }
        return selectedFiles;
    }

    private void refreshImagePreview(
            VBox previewBox,
            List<File> selectedImageFiles,
            Label imageCountLabel,
            Label draftImageCountLabel
    ) {
        previewBox.getChildren().clear();
        String countText = "已选 " + selectedImageFiles.size() + " / " + ProjectImageStore.MAX_IMAGE_COUNT + " 张";
        imageCountLabel.setText(countText);
        draftImageCountLabel.setText(selectedImageFiles.size() + " 张");

        if (selectedImageFiles.isEmpty()) {
            Label emptyLabel = new Label("暂未选择图片。");
            emptyLabel.getStyleClass().add("helper-text");
            previewBox.getChildren().add(emptyLabel);
            return;
        }

        for (int index = 0; index < selectedImageFiles.size(); index++) {
            File selectedFile = selectedImageFiles.get(index);
            Label imageLabel = new Label((index + 1) + ". " + selectedFile.getName());
            imageLabel.getStyleClass().add("upload-preview-item");
            imageLabel.setWrapText(true);
            previewBox.getChildren().add(imageLabel);
        }
    }

    private void refreshDraftLocation(
            ComboBox<String> dormAreaBox,
            ComboBox<DormBuilding> buildingBox,
            TextField roomField,
            Label targetLabel
    ) {
        String area = dormAreaBox.getValue();
        DormBuilding building = buildingBox.getValue();
        String room = roomField.getText() == null ? "" : roomField.getText().trim();

        if (area == null || area.isBlank() || building == null || room.isBlank()) {
            targetLabel.setText("待补全");
            return;
        }
        targetLabel.setText(area + " " + building.getBuildingNo() + "-" + room.toUpperCase());
    }

    private VBox createDetailBlock(String labelText, Label valueLabel) {
        valueLabel.getStyleClass().add("plain-text");
        return createFieldBlock(labelText, valueLabel);
    }

    private Label createDraftValueLabel(String text) {
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
                Bindings.createStringBinding(() -> {
                    if (cell.getValue().getSubmittedAt() == null) {
                        return "";
                    }
                    return TIME_FORMATTER.format(cell.getValue().getSubmittedAt());
                })
        );
        return column;
    }

    private ListCell<DormBuilding> createDormBuildingCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(DormBuilding item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        };
    }

    private ListCell<FaultCategory> createFaultCategoryCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(FaultCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : UiDisplayText.faultCategory(item));
            }
        };
    }

    private record ImageUploadState(
            VBox previewBox,
            Label uploadCountLabel,
            Label draftCountLabel
    ) {
    }
}
