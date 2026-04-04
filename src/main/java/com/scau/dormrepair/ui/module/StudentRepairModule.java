package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
import com.scau.dormrepair.domain.entity.DormBuilding;
import com.scau.dormrepair.domain.entity.DormRoom;
import com.scau.dormrepair.domain.entity.UserAccount;
import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.ui.component.AppDropdown;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.support.ProjectImageStore;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.support.UiDisplayText;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.geometry.Pos;
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
import java.util.regex.Pattern;

public class StudentRepairModule extends AbstractWorkbenchModule {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s]{6,32}$");
    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    public StudentRepairModule(AppContext appContext) {
        super(appContext);
    }

    @Override
    public String moduleCode() {
        return "student-repair";
    }

    @Override
    public String moduleName() {
        return "\u63d0\u4ea4\u62a5\u4fee";
    }

    @Override
    public String moduleDescription() {
        return "\u6309\u5bbf\u820d\u533a\u3001\u697c\u680b\u3001\u623f\u95f4\u3001\u6545\u969c\u7c7b\u578b\u548c\u56fe\u7247\u5b8c\u6210\u62a5\u4fee\u63d0\u4ea4\uff0c\u5e76\u5728\u53f3\u4fa7\u5b9e\u65f6\u67e5\u770b\u5f53\u524d\u586b\u5199\u6982\u51b5\u3002";
    }

    @Override
    public Set<UserRole> supportedRoles() {
        return Set.of(UserRole.STUDENT);
    }

    @Override
    public boolean cacheViewOnSwitch() {
        return true;
    }

    @Override
    public Parent createView() {
        UserAccount currentStudent = appContext.userAccountService()
                .requireCurrentAccount(appContext.appSession(), UserRole.STUDENT);

        GridPane workspace = buildRepairWorkspace(currentStudent);
        VBox content = new VBox(18, workspace);
        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);
        return createPage("\u5b66\u751f\u62a5\u4fee\u5de5\u4f5c\u533a", "", content);
    }

    private GridPane buildRepairWorkspace(UserAccount currentStudent) {
        AppDropdown<String> dormAreaBox = createDormAreaBox();
        AppDropdown<DormBuilding> buildingBox = createDormBuildingBox();
        AppDropdown<DormRoom> roomBox = createDormRoomBox();

        TextField phoneField = new TextField();
        phoneField.setPromptText("\u8054\u7cfb\u7535\u8bdd");
        phoneField.setText(currentStudent.getPhone() == null ? "" : currentStudent.getPhone());
        Label phoneHintLabel = createDraftValueLabel("");
        phoneHintLabel.getStyleClass().add("helper-text");

        AppDropdown<FaultCategory> faultCategoryBox = createFaultCategoryBox();

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("\u8bf7\u5177\u4f53\u8bf4\u660e\u6545\u969c\u73b0\u8c61\u3001\u662f\u5426\u7d27\u6025\u3001\u662f\u5426\u5f71\u54cd\u6b63\u5e38\u751f\u6d3b\u3002");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);
        Label descriptionHintLabel = createDraftValueLabel("");
        descriptionHintLabel.getStyleClass().add("helper-text");

        List<File> selectedImageFiles = new ArrayList<>();
        Label draftLocationValue = createDraftValueLabel("\u5f85\u8865\u5145");
        Label draftFaultValue = createDraftValueLabel("\u5f85\u9009\u62e9");
        Label draftImageCountValue = createDraftValueLabel("0 \u5f20");
        Label draftRecentHintValue = createDraftValueLabel("\u63d0\u4ea4\u540e\u53ef\u5728\u201c\u62a5\u4fee\u8bb0\u5f55\u201d\u6a21\u5757\u67e5\u770b\u5b8c\u6574\u8be6\u60c5\u3002");
        Label submitReadinessLabel = createDraftValueLabel("");
        submitReadinessLabel.getStyleClass().add("helper-text");
        VBox imageUploadBox = createImageUploadBox(selectedImageFiles, draftImageCountValue);

        dormAreaBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            reloadBuildingsByArea(newValue, buildingBox);
            reloadRoomsByBuilding(null, roomBox);
            refreshDraftLocation(dormAreaBox, buildingBox, roomBox, draftLocationValue);
        });
        buildingBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            reloadRoomsByBuilding(newValue, roomBox);
            refreshDraftLocation(dormAreaBox, buildingBox, roomBox, draftLocationValue);
        });
        roomBox.valueProperty().addListener((observable, oldValue, newValue) ->
                refreshDraftLocation(dormAreaBox, buildingBox, roomBox, draftLocationValue)
        );
        faultCategoryBox.valueProperty().addListener((observable, oldValue, newValue) ->
                draftFaultValue.setText(newValue == null ? "\u5f85\u9009\u62e9" : UiDisplayText.faultCategory(newValue))
        );
        phoneField.textProperty().addListener((observable, oldValue, newValue) ->
                refreshPhoneHint(phoneField, phoneHintLabel)
        );
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) ->
                refreshDescriptionHint(descriptionArea, descriptionHintLabel)
        );
        refreshPhoneHint(phoneField, phoneHintLabel);
        refreshDescriptionHint(descriptionArea, descriptionHintLabel);

        Button resetButton = new Button("\u6e05\u7a7a\u8868\u5355");
        resetButton.getStyleClass().add("surface-button");

        Node[] submitButtonHolder = new Node[1];
        Node submitButton = FusionUiFactory.createPrimaryButton("\u63d0\u4ea4\u62a5\u4fee\u7533\u8bf7", 180, 40, () -> {
            try {
                ProjectImageStore.validateImageFiles(selectedImageFiles);

                DormBuilding selectedBuilding = buildingBox.getValue();
                DormRoom selectedRoom = roomBox.getValue();
                CreateRepairRequestCommand command = new CreateRepairRequestCommand(
                        currentStudent.getId(),
                        currentStudent.getDisplayName(),
                        phoneField.getText(),
                        selectedRoom == null ? null : selectedRoom.getId(),
                        dormAreaBox.getValue(),
                        selectedBuilding == null ? null : selectedBuilding.getBuildingNo(),
                        selectedRoom == null ? null : selectedRoom.getRoomNo(),
                        faultCategoryBox.getValue(),
                        descriptionArea.getText(),
                        ProjectImageStore.copyImagesToProject(selectedImageFiles)
                );

                appContext.repairRequestService().create(command);
                clearAfterSubmit(
                        dormAreaBox,
                        buildingBox,
                        roomBox,
                        phoneField,
                        currentStudent.getPhone(),
                        descriptionArea,
                        selectedImageFiles,
                        imageUploadBox,
                        faultCategoryBox
                );
                draftFaultValue.setText("\u5f85\u9009\u62e9");
                draftImageCountValue.setText("0 \u5f20");
                draftRecentHintValue.setText("\u672c\u6b21\u62a5\u4fee\u5df2\u63d0\u4ea4\uff0c\u53ef\u5728\u201c\u62a5\u4fee\u8bb0\u5f55\u201d\u6a21\u5757\u67e5\u770b\u5904\u7406\u8fdb\u5ea6\u548c\u5b8c\u6574\u8be6\u60c5\u3002");
                refreshDraftLocation(dormAreaBox, buildingBox, roomBox, draftLocationValue);
                refreshSubmitReadiness(
                        dormAreaBox,
                        buildingBox,
                        roomBox,
                        phoneField,
                        descriptionArea,
                        faultCategoryBox,
                        submitButtonHolder[0],
                        submitReadinessLabel
                );
                UiAlerts.info("\u63d0\u4ea4\u6210\u529f", "\u62a5\u4fee\u7533\u8bf7\u5df2\u63d0\u4ea4\uff0c\u53ef\u5728\u201c\u62a5\u4fee\u8bb0\u5f55\u201d\u6a21\u5757\u67e5\u770b\u5904\u7406\u8fdb\u5ea6\u548c\u5b8c\u6574\u8be6\u60c5\u3002");
            } catch (RuntimeException exception) {
                UiAlerts.error("\u63d0\u4ea4\u5931\u8d25", exception.getMessage());
            }
        }).getNode();
        submitButtonHolder[0] = submitButton;

        resetButton.setOnAction(event -> {
            clearAfterSubmit(
                    dormAreaBox,
                    buildingBox,
                    roomBox,
                    phoneField,
                    currentStudent.getPhone(),
                    descriptionArea,
                    selectedImageFiles,
                    imageUploadBox,
                    faultCategoryBox
            );
            draftLocationValue.setText("\u5f85\u8865\u5145");
            draftFaultValue.setText("\u5f85\u9009\u62e9");
            draftImageCountValue.setText("0 \u5f20");
            draftRecentHintValue.setText("\u63d0\u4ea4\u540e\u53ef\u5728\u201c\u62a5\u4fee\u8bb0\u5f55\u201d\u6a21\u5757\u67e5\u770b\u5b8c\u6574\u8be6\u60c5\u3002");
            refreshSubmitReadiness(
                    dormAreaBox,
                    buildingBox,
                    roomBox,
                    phoneField,
                    descriptionArea,
                    faultCategoryBox,
                    submitButton,
                    submitReadinessLabel
            );
        });

        dormAreaBox.valueProperty().addListener((observable, oldValue, newValue) ->
                refreshSubmitReadiness(
                        dormAreaBox,
                        buildingBox,
                        roomBox,
                        phoneField,
                        descriptionArea,
                        faultCategoryBox,
                        submitButton,
                        submitReadinessLabel
                )
        );
        buildingBox.valueProperty().addListener((observable, oldValue, newValue) ->
                refreshSubmitReadiness(
                        dormAreaBox,
                        buildingBox,
                        roomBox,
                        phoneField,
                        descriptionArea,
                        faultCategoryBox,
                        submitButton,
                        submitReadinessLabel
                )
        );
        roomBox.valueProperty().addListener((observable, oldValue, newValue) ->
                refreshSubmitReadiness(
                        dormAreaBox,
                        buildingBox,
                        roomBox,
                        phoneField,
                        descriptionArea,
                        faultCategoryBox,
                        submitButton,
                        submitReadinessLabel
                )
        );
        faultCategoryBox.valueProperty().addListener((observable, oldValue, newValue) ->
                refreshSubmitReadiness(
                        dormAreaBox,
                        buildingBox,
                        roomBox,
                        phoneField,
                        descriptionArea,
                        faultCategoryBox,
                        submitButton,
                        submitReadinessLabel
                )
        );
        phoneField.textProperty().addListener((observable, oldValue, newValue) ->
                refreshSubmitReadiness(
                        dormAreaBox,
                        buildingBox,
                        roomBox,
                        phoneField,
                        descriptionArea,
                        faultCategoryBox,
                        submitButton,
                        submitReadinessLabel
                )
        );
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) ->
                refreshSubmitReadiness(
                        dormAreaBox,
                        buildingBox,
                        roomBox,
                        phoneField,
                        descriptionArea,
                        faultCategoryBox,
                        submitButton,
                        submitReadinessLabel
                )
        );
        refreshSubmitReadiness(
                dormAreaBox,
                buildingBox,
                roomBox,
                phoneField,
                descriptionArea,
                faultCategoryBox,
                submitButton,
                submitReadinessLabel
        );

        GridPane dormRow = new GridPane();
        dormRow.setHgap(16);
        dormRow.setMinWidth(0);
        dormRow.getColumnConstraints().addAll(percentColumn(50), percentColumn(50));
        dormRow.add(createFieldBlock("\u5bbf\u820d\u533a", dormAreaBox), 0, 0);
        dormRow.add(createFieldBlock("\u5bbf\u820d\u697c", buildingBox), 1, 0);

        GridPane contactRow = new GridPane();
        contactRow.setHgap(16);
        contactRow.setMinWidth(0);
        contactRow.getColumnConstraints().addAll(percentColumn(50), percentColumn(50));
        contactRow.add(createFieldBlock("\u623f\u95f4\u53f7", roomBox), 0, 0);
        contactRow.add(createFieldBlock("\u8054\u7cfb\u7535\u8bdd", phoneField), 1, 0);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionRow = new HBox(12, resetButton, spacer, submitButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setMinWidth(0);

        VBox formBox = new VBox(
                16,
                dormRow,
                contactRow,
                createFieldBlock("\u6545\u969c\u7c7b\u578b", faultCategoryBox),
                createFieldBlock("\u8054\u7cfb\u7535\u8bdd\u63d0\u793a", phoneHintLabel),
                createFieldBlock("\u6545\u969c\u63cf\u8ff0", descriptionArea),
                createFieldBlock("\u63cf\u8ff0\u5b57\u6570", descriptionHintLabel),
                createFieldBlock("\u63d0\u4ea4\u6761\u4ef6", submitReadinessLabel),
                createFieldBlock("\u56fe\u7247\u4e0a\u4f20", imageUploadBox),
                actionRow
        );
        formBox.setFillWidth(true);
        formBox.setMaxWidth(Double.MAX_VALUE);

        VBox summaryBox = new VBox(
                14,
                createDetailBlock("\u5f53\u524d\u5b66\u751f", createDraftValueLabel(currentStudent.getDisplayName())),
                createDetailBlock("\u62a5\u4fee\u4f4d\u7f6e", draftLocationValue),
                createDetailBlock("\u6545\u969c\u7c7b\u578b", draftFaultValue),
                createDetailBlock("\u5df2\u9009\u56fe\u7247", draftImageCountValue),
                createDetailBlock("\u63d0\u4ea4\u63d0\u793a", draftRecentHintValue)
        );

        return createRatioWorkspace(
                64,
                36,
                wrapPanel("\u586b\u5199\u62a5\u4fee\u8868\u5355", formBox),
                wrapPanel("\u586b\u5199\u6982\u51b5", summaryBox)
        );
    }

    private AppDropdown<String> createDormAreaBox() {
        AppDropdown<String> dormAreaBox = new AppDropdown<>();
        dormAreaBox.setItems(appContext.dormCatalogService().listDormAreas());
        dormAreaBox.setPromptText("\u9009\u62e9\u5bbf\u820d\u533a");
        dormAreaBox.setVisibleRowCount(5);
        return dormAreaBox;
    }

    private AppDropdown<DormBuilding> createDormBuildingBox() {
        AppDropdown<DormBuilding> buildingBox = new AppDropdown<>();
        buildingBox.setPromptText("\u5148\u9009\u5bbf\u820d\u533a\u518d\u9009\u5bbf\u820d\u697c");
        buildingBox.setDisable(true);
        buildingBox.setVisibleRowCount(5);
        buildingBox.setTextMapper(building -> building == null ? "" : building.getDisplayName());
        return buildingBox;
    }

    private AppDropdown<DormRoom> createDormRoomBox() {
        AppDropdown<DormRoom> roomBox = new AppDropdown<>();
        roomBox.setPromptText("\u5148\u9009\u5bbf\u820d\u697c\u518d\u9009\u623f\u95f4");
        roomBox.setDisable(true);
        roomBox.setVisibleRowCount(6);
        roomBox.setTextMapper(room -> room == null ? "" : room.getRoomNo());
        return roomBox;
    }

    private AppDropdown<FaultCategory> createFaultCategoryBox() {
        AppDropdown<FaultCategory> faultCategoryBox = new AppDropdown<>();
        faultCategoryBox.setItems(List.of(FaultCategory.values()));
        faultCategoryBox.setTextMapper(UiDisplayText::faultCategory);
        faultCategoryBox.setPromptText("\u9009\u62e9\u6545\u969c\u7c7b\u578b");
        faultCategoryBox.setVisibleRowCount(7);
        return faultCategoryBox;
    }

    private void reloadBuildingsByArea(String dormArea, AppDropdown<DormBuilding> buildingBox) {
        buildingBox.getItems().clear();
        buildingBox.clearSelection();

        if (dormArea == null || dormArea.isBlank()) {
            buildingBox.setDisable(true);
            buildingBox.setPromptText("\u5148\u9009\u5bbf\u820d\u533a\u518d\u9009\u5bbf\u820d\u697c");
            return;
        }

        buildingBox.setItems(appContext.dormCatalogService().listBuildingsByArea(dormArea));
        buildingBox.setDisable(false);
        buildingBox.setPromptText("\u9009\u62e9 " + dormArea + " \u7684\u697c\u680b");
    }

    private void reloadRoomsByBuilding(DormBuilding building, AppDropdown<DormRoom> roomBox) {
        roomBox.getItems().clear();
        roomBox.clearSelection();

        if (building == null || building.getId() == null) {
            roomBox.setDisable(true);
            roomBox.setPromptText("\u5148\u9009\u5bbf\u820d\u697c\u518d\u9009\u623f\u95f4");
            return;
        }

        List<DormRoom> activeRooms = appContext.dormCatalogService().listActiveRoomsByBuilding(building.getId());
        roomBox.setItems(activeRooms);
        roomBox.setDisable(activeRooms.isEmpty());
        roomBox.setPromptText(activeRooms.isEmpty()
                ? "\u5f53\u524d\u697c\u680b\u6682\u65e0\u53ef\u62a5\u4fee\u623f\u95f4"
                : "\u9009\u62e9 " + building.getBuildingNo() + " \u7684\u623f\u95f4");
    }

    private void clearAfterSubmit(
            AppDropdown<String> dormAreaBox,
            AppDropdown<DormBuilding> buildingBox,
            AppDropdown<DormRoom> roomBox,
            TextField phoneField,
            String defaultPhone,
            TextArea descriptionArea,
            List<File> selectedImageFiles,
            VBox imageUploadBox,
            AppDropdown<FaultCategory> faultCategoryBox
    ) {
        dormAreaBox.clearSelection();
        buildingBox.getItems().clear();
        buildingBox.clearSelection();
        buildingBox.setDisable(true);
        buildingBox.setPromptText("\u5148\u9009\u5bbf\u820d\u533a\u518d\u9009\u5bbf\u820d\u697c");
        roomBox.getItems().clear();
        roomBox.clearSelection();
        roomBox.setDisable(true);
        roomBox.setPromptText("\u5148\u9009\u5bbf\u820d\u697c\u518d\u9009\u623f\u95f4");
        phoneField.setText(defaultPhone == null ? "" : defaultPhone);
        descriptionArea.clear();
        selectedImageFiles.clear();
        ImageUploadState uploadState = (ImageUploadState) imageUploadBox.getUserData();
        if (uploadState != null) {
            refreshImagePreview(
                    uploadState.previewBox(),
                    selectedImageFiles,
                    uploadState.uploadCountLabel(),
                    uploadState.draftCountLabel()
            );
        }
        faultCategoryBox.clearSelection();
    }

    private void refreshPhoneHint(TextField phoneField, Label phoneHintLabel) {
        String value = phoneField.getText() == null ? "" : phoneField.getText().trim();
        if (value.isEmpty()) {
            phoneHintLabel.setText("请填写可联系到你的手机号或常用电话。");
            return;
        }
        if (PHONE_PATTERN.matcher(value).matches()) {
            phoneHintLabel.setText("联系电话格式看起来正常，提交后维修员会按这个号码联系你。");
            return;
        }
        phoneHintLabel.setText("当前电话格式可能有误，建议检查是否包含空号、错号或异常字符。");
    }

    private void refreshDescriptionHint(TextArea descriptionArea, Label descriptionHintLabel) {
        String value = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
        int used = value.length();
        int remaining = MAX_DESCRIPTION_LENGTH - used;
        if (used == 0) {
            descriptionHintLabel.setText("建议写清故障现象、影响范围和是否紧急，至少 5 个字。");
            return;
        }
        if (remaining < 0) {
            descriptionHintLabel.setText("已超出 " + MAX_DESCRIPTION_LENGTH + " 字上限 " + (-remaining) + " 个字，请精简后再提交。");
            return;
        }
        if (used < 5) {
            descriptionHintLabel.setText("当前已写 " + used + " 个字，还需要至少 " + (5 - used) + " 个字。");
            return;
        }
        descriptionHintLabel.setText("当前已写 " + used + " 个字，还可填写 " + remaining + " 个字。");
    }

    private void refreshSubmitReadiness(
            AppDropdown<String> dormAreaBox,
            AppDropdown<DormBuilding> buildingBox,
            AppDropdown<DormRoom> roomBox,
            TextField phoneField,
            TextArea descriptionArea,
            AppDropdown<FaultCategory> faultCategoryBox,
            Node submitButton,
            Label submitReadinessLabel
    ) {
        List<String> issues = new ArrayList<>();
        if (dormAreaBox.getValue() == null || dormAreaBox.getValue().isBlank()) {
            issues.add("宿舍区");
        }
        if (buildingBox.getValue() == null) {
            issues.add("宿舍楼");
        }
        if (roomBox.getValue() == null) {
            issues.add("房间号");
        }
        if (faultCategoryBox.getValue() == null) {
            issues.add("故障类型");
        }

        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        if (phone.isEmpty()) {
            issues.add("联系电话");
        } else if (!PHONE_PATTERN.matcher(phone).matches()) {
            issues.add("联系电话格式");
        }

        String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
        if (description.length() < 5) {
            issues.add("故障描述至少 5 个字");
        } else if (description.length() > MAX_DESCRIPTION_LENGTH) {
            issues.add("故障描述超出上限");
        }

        boolean ready = issues.isEmpty();
        submitButton.setDisable(!ready);
        submitButton.setOpacity(ready ? 1.0 : 0.6);
        submitReadinessLabel.setText(ready
                ? "当前信息已经完整，可以直接提交报修申请。"
                : "当前还需完善：" + String.join("、", issues) + "。");
    }

    private VBox createImageUploadBox(List<File> selectedImageFiles, Label draftImageCountLabel) {
        Label imageCountLabel = createDraftValueLabel("\u5df2\u9009 0 / " + ProjectImageStore.MAX_IMAGE_COUNT + " \u5f20");

        Button chooseButton = new Button("\u9009\u62e9\u56fe\u7247");
        chooseButton.getStyleClass().add("surface-button");

        Button clearButton = new Button("\u6e05\u7a7a\u56fe\u7247");
        clearButton.getStyleClass().add("surface-button");

        Label helperLabel = new Label(
                "\u652f\u6301 png\u3001jpg\u3001jpeg\u3001webp\u3001bmp\u3001gif\uff0c\u6700\u591a "
                        + ProjectImageStore.MAX_IMAGE_COUNT
                        + " \u5f20\uff0c\u5355\u5f20\u4e0d\u8d85\u8fc7 8MB\u3002\u63d0\u4ea4\u65f6\u4f1a\u81ea\u52a8\u590d\u5236\u5230\u9879\u76ee pics/ \u76ee\u5f55\u3002"
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
                UiAlerts.error("\u56fe\u7247\u6821\u9a8c\u5931\u8d25", exception.getMessage());
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
        chooser.setTitle("\u9009\u62e9\u62a5\u4fee\u56fe\u7247");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "\u56fe\u7247\u6587\u4ef6",
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
        String countText = "\u5df2\u9009 " + selectedImageFiles.size() + " / " + ProjectImageStore.MAX_IMAGE_COUNT + " \u5f20";
        imageCountLabel.setText(countText);
        draftImageCountLabel.setText(selectedImageFiles.size() + " \u5f20");

        if (selectedImageFiles.isEmpty()) {
            Label emptyLabel = new Label("\u6682\u672a\u9009\u62e9\u56fe\u7247\u3002");
            emptyLabel.getStyleClass().add("helper-text");
            previewBox.getChildren().add(emptyLabel);
            return;
        }

        for (int index = 0; index < selectedImageFiles.size(); index++) {
            File selectedFile = selectedImageFiles.get(index);
            Label imageLabel = new Label((index + 1) + ". " + selectedFile.getName());
            imageLabel.getStyleClass().add("upload-preview-item");
            imageLabel.setWrapText(true);
            Button removeButton = new Button("\u79fb\u9664");
            removeButton.getStyleClass().add("surface-button");
            removeButton.setOnAction(event -> {
                selectedImageFiles.remove(selectedFile);
                refreshImagePreview(previewBox, selectedImageFiles, imageCountLabel, draftImageCountLabel);
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox row = new HBox(10, imageLabel, spacer, removeButton);
            row.setAlignment(Pos.CENTER_LEFT);
            previewBox.getChildren().add(row);
        }
    }

    private void refreshDraftLocation(
            AppDropdown<String> dormAreaBox,
            AppDropdown<DormBuilding> buildingBox,
            AppDropdown<DormRoom> roomBox,
            Label targetLabel
    ) {
        String area = dormAreaBox.getValue();
        DormBuilding building = buildingBox.getValue();
        DormRoom room = roomBox.getValue();

        if (area == null || area.isBlank() || building == null || room == null || room.getRoomNo() == null || room.getRoomNo().isBlank()) {
            targetLabel.setText("\u5f85\u8865\u5145");
            return;
        }
        targetLabel.setText(area + " " + building.getBuildingNo() + "-" + room.getRoomNo().toUpperCase());
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

    private record ImageUploadState(
            VBox previewBox,
            Label uploadCountLabel,
            Label draftCountLabel
    ) {
    }
}
