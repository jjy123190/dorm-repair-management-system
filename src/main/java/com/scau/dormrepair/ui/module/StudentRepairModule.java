package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.common.DemoAccountDirectory;
import com.scau.dormrepair.common.DemoAccountDirectory.DemoAccount;
import com.scau.dormrepair.domain.command.CreateRepairRequestCommand;
import com.scau.dormrepair.domain.entity.DormBuilding;
import com.scau.dormrepair.domain.enums.FaultCategory;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.support.UiAlerts;
import com.scau.dormrepair.ui.support.UiDisplayText;
import com.scau.dormrepair.ui.support.UiMotion;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * 学生报修页只负责提交新的报修单。
 */
public class StudentRepairModule extends AbstractWorkbenchModule {

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
        return "\u5148\u9009\u5bbf\u820d\u533a\u548c\u697c\u680b\uff0c\u518d\u8865\u5168\u8054\u7cfb\u65b9\u5f0f\u548c\u6545\u969c\u63cf\u8ff0\uff0c\u63d0\u4ea4\u65b0\u7684\u62a5\u4fee\u7533\u8bf7\u3002";
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
            throw new IllegalStateException("\u5b66\u751f\u8eab\u4efd\u672a\u521d\u59cb\u5316\uff0c\u65e0\u6cd5\u8fdb\u5165\u62a5\u4fee\u6a21\u5757\u3002");
        }

        VBox content = new VBox(18, buildRepairForm(currentStudent));
        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);

        return createPage(
                "\u5b66\u751f\u62a5\u4fee\u5de5\u4f5c\u533a",
                "",
                content
        );
    }

    private Node buildRepairForm(DemoAccount currentStudent) {
        ComboBox<String> dormAreaBox = createDormAreaBox();
        ComboBox<DormBuilding> buildingBox = createDormBuildingBox();

        TextField roomField = new TextField();
        roomField.setPromptText("\u4f8b\u5982 402");

        TextField phoneField = new TextField();
        phoneField.setPromptText("\u8054\u7cfb\u7535\u8bdd");

        ComboBox<FaultCategory> faultCategoryBox = createFaultCategoryBox();

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("\u8bf7\u5177\u4f53\u8bf4\u660e\u6545\u969c\u73b0\u8c61\u3001\u662f\u5426\u7d27\u6025\u3001\u662f\u5426\u5f71\u54cd\u6b63\u5e38\u751f\u6d3b\u3002");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);

        TextArea imageUrlsArea = new TextArea();
        imageUrlsArea.setPromptText("\u6682\u65f6\u7528\u56fe\u7247\u5730\u5740\u4ee3\u66ff\u4e0a\u4f20\u529f\u80fd\uff0c\u4e00\u884c\u4e00\u5f20\u3002");
        imageUrlsArea.setPrefRowCount(3);
        imageUrlsArea.setWrapText(true);

        dormAreaBox.valueProperty().addListener((observable, oldValue, newValue) ->
                reloadBuildingsByArea(newValue, buildingBox)
        );

        Button resetButton = new Button("\u6e05\u7a7a\u8868\u5355");
        resetButton.getStyleClass().add("nav-button");

        Node submitButton = FusionUiFactory.createPrimaryButton("\u63d0\u4ea4\u62a5\u4fee\u7533\u8bf7", 180, 40, () -> {
            try {
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
                        splitImageUrls(imageUrlsArea.getText())
                );

                Long repairRequestId = appContext.repairRequestService().create(command);
                clearAfterSubmit(
                        dormAreaBox,
                        buildingBox,
                        roomField,
                        phoneField,
                        descriptionArea,
                        imageUrlsArea,
                        faultCategoryBox
                );
                UiAlerts.info(
                        "\u63d0\u4ea4\u6210\u529f",
                        "\u62a5\u4fee\u7533\u8bf7\u5df2\u4fdd\u5b58\uff0c\u8bb0\u5f55 ID=" + repairRequestId + "\u3002\u6700\u8fd1\u8bb0\u5f55\u8bf7\u53bb\u300c\u62a5\u4fee\u8bb0\u5f55\u300d\u9875\u9762\u67e5\u770b\u3002"
                );
            } catch (RuntimeException exception) {
                UiAlerts.error("\u63d0\u4ea4\u5931\u8d25", exception.getMessage());
            }
        }).getNode();

        resetButton.setOnAction(event ->
                clearAfterSubmit(
                        dormAreaBox,
                        buildingBox,
                        roomField,
                        phoneField,
                        descriptionArea,
                        imageUrlsArea,
                        faultCategoryBox
                )
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
        contactRow.add(createFieldBlock("\u623f\u95f4\u53f7", roomField), 0, 0);
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
                createFieldBlock("\u6545\u969c\u63cf\u8ff0", descriptionArea),
                createFieldBlock("\u56fe\u7247\u5730\u5740", imageUrlsArea),
                actionRow
        );
        formBox.setFillWidth(true);
        formBox.setMaxWidth(Double.MAX_VALUE);

        return wrapPanel("\u586b\u5199\u62a5\u4fee\u8868\u5355", formBox);
    }

    private ComboBox<String> createDormAreaBox() {
        ComboBox<String> dormAreaBox = new ComboBox<>();
        dormAreaBox.getItems().setAll(appContext.dormCatalogService().listDormAreas());
        dormAreaBox.setPromptText("\u9009\u62e9\u5bbf\u820d\u533a");
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
        buildingBox.setPromptText("\u5148\u9009\u5bbf\u820d\u533a\u518d\u9009\u5bbf\u820d\u697c");
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
        faultCategoryBox.setPromptText("\u9009\u62e9\u6545\u969c\u7c7b\u578b");
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

    private void reloadBuildingsByArea(String dormArea, ComboBox<DormBuilding> buildingBox) {
        buildingBox.getItems().clear();
        buildingBox.getSelectionModel().clearSelection();

        if (dormArea == null || dormArea.isBlank()) {
            buildingBox.setDisable(true);
            buildingBox.setPromptText("\u5148\u9009\u5bbf\u820d\u533a\u518d\u9009\u5bbf\u820d\u697c");
            return;
        }

        buildingBox.getItems().setAll(appContext.dormCatalogService().listBuildingsByArea(dormArea));
        buildingBox.setDisable(false);
        buildingBox.setPromptText("\u9009\u62e9" + dormArea + "\u7684\u697c\u680b");
    }

    private List<String> splitImageUrls(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return List.of();
        }
        return rawText.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    private void clearAfterSubmit(
            ComboBox<String> dormAreaBox,
            ComboBox<DormBuilding> buildingBox,
            TextField roomField,
            TextField phoneField,
            TextArea descriptionArea,
            TextArea imageUrlsArea,
            ComboBox<FaultCategory> faultCategoryBox
    ) {
        dormAreaBox.getSelectionModel().clearSelection();
        buildingBox.getItems().clear();
        buildingBox.getSelectionModel().clearSelection();
        buildingBox.setDisable(true);
        buildingBox.setPromptText("\u5148\u9009\u5bbf\u820d\u533a\u518d\u9009\u5bbf\u820d\u697c");
        roomField.clear();
        phoneField.clear();
        descriptionArea.clear();
        imageUrlsArea.clear();
        faultCategoryBox.getSelectionModel().clearSelection();
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
}
