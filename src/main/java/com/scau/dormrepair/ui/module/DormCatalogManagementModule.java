package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.command.SaveDormBuildingCommand;
import com.scau.dormrepair.domain.command.SaveDormRoomCommand;
import com.scau.dormrepair.domain.entity.DormBuilding;
import com.scau.dormrepair.domain.entity.DormRoom;
import com.scau.dormrepair.domain.enums.UserRole;
import com.scau.dormrepair.ui.component.AppDropdown;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.support.UiAlerts;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * 管理员维护宿舍目录基础资料：宿舍区、楼栋、房间与停用状态。
 */
public class DormCatalogManagementModule extends AbstractWorkbenchModule {

    private static final String ALL_AREAS = "全部区域";
    private static final String ROOM_STATUS_ACTIVE = "ACTIVE";
    private static final String ROOM_STATUS_INACTIVE = "INACTIVE";

    public DormCatalogManagementModule(AppContext appContext) {
        super(appContext);
    }

    @Override
    public String moduleCode() {
        return "dorm-catalog";
    }

    @Override
    public String moduleName() {
        return "宿舍目录";
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
        TextField buildingKeywordField = createInput("搜索宿舍区、楼栋编号或楼栋名称");
        AppDropdown<String> areaFilterBox = createAreaFilterBox();
        VBox buildingListContainer = createListContainer();
        ObservableList<DormBuilding> buildingSource = FXCollections.observableArrayList();
        ObjectProperty<DormBuilding> selectedBuilding = new SimpleObjectProperty<>();

        TextField campusField = createInput("例如 泰山区");
        TextField buildingNoField = createInput("例如 6栋 或 6-BLD");
        TextField buildingNameField = createInput("例如 泰山区 6栋");

        TextField roomNoField = createInput("例如 402");
        TextField floorNoField = createInput("例如 4");
        TextField bedCountField = createInput("例如 6");
        AppDropdown<String> roomStatusBox = createRoomStatusBox();
        VBox roomListContainer = createListContainer();
        ObjectProperty<DormRoom> selectedRoom = new SimpleObjectProperty<>();

        Runnable resetBuildingForm = () -> {
            selectedBuilding.set(null);
            campusField.clear();
            buildingNoField.clear();
            buildingNameField.clear();
        };
        Runnable resetRoomForm = () -> {
            selectedRoom.set(null);
            roomNoField.clear();
            floorNoField.clear();
            bedCountField.clear();
            roomStatusBox.setValue(ROOM_STATUS_ACTIVE);
        };
        Runnable renderRooms = () -> renderRoomTable(roomListContainer, selectedBuilding.get(), selectedRoom);

        selectedBuilding.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                campusField.clear();
                buildingNoField.clear();
                buildingNameField.clear();
                resetRoomForm.run();
                renderRooms.run();
                return;
            }
            campusField.setText(safeText(newValue.getCampusName()));
            buildingNoField.setText(safeText(newValue.getBuildingNo()));
            buildingNameField.setText(safeText(newValue.getBuildingName()));
            resetRoomForm.run();
            renderRooms.run();
        });

        selectedRoom.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                roomNoField.clear();
                floorNoField.clear();
                bedCountField.clear();
                roomStatusBox.setValue(ROOM_STATUS_ACTIVE);
                return;
            }
            roomNoField.setText(safeText(newValue.getRoomNo()));
            floorNoField.setText(newValue.getFloorNo() == null ? "" : String.valueOf(newValue.getFloorNo()));
            bedCountField.setText(newValue.getBedCount() == null ? "" : String.valueOf(newValue.getBedCount()));
            roomStatusBox.setValue(newValue.getRoomStatus() == null ? ROOM_STATUS_ACTIVE : newValue.getRoomStatus());
        });

        Runnable refreshBuildings = () -> {
            buildingSource.setAll(appContext.dormCatalogService().listAllBuildings());
            refreshAreaFilter(areaFilterBox);
            applyBuildingFilters(buildingSource, buildingKeywordField.getText(), areaFilterBox.getValue(), buildingListContainer, selectedBuilding);
            if (selectedBuilding.get() == null) {
                renderRooms.run();
            }
        };

        buildingKeywordField.textProperty().addListener((observable, oldValue, newValue) ->
                applyBuildingFilters(buildingSource, newValue, areaFilterBox.getValue(), buildingListContainer, selectedBuilding)
        );
        areaFilterBox.valueProperty().addListener((observable, oldValue, newValue) ->
                applyBuildingFilters(buildingSource, buildingKeywordField.getText(), newValue, buildingListContainer, selectedBuilding)
        );

        Node saveBuildingButton = FusionUiFactory.createPrimaryButton("保存楼栋", 168, 40, () -> {
            try {
                Long savedId = appContext.dormCatalogService().saveBuilding(new SaveDormBuildingCommand(
                        selectedBuilding.get() == null ? null : selectedBuilding.get().getId(),
                        campusField.getText(),
                        buildingNoField.getText(),
                        buildingNameField.getText()
                ));
                refreshBuildings.run();
                selectBuilding(savedId, buildingSource, selectedBuilding, buildingListContainer, buildingKeywordField.getText(), areaFilterBox.getValue());
                UiAlerts.info("保存成功", "宿舍楼栋目录已更新。");
            } catch (RuntimeException exception) {
                UiAlerts.error("保存失败", exception.getMessage());
            }
        }).getNode();
        Node deleteBuildingButton = FusionUiFactory.createGhostButton("删除楼栋", 120, 40, () -> {
            try {
                if (selectedBuilding.get() == null) {
                    throw new IllegalArgumentException("请先选择一个楼栋。");
                }
                appContext.dormCatalogService().deleteBuilding(selectedBuilding.get().getId());
                resetBuildingForm.run();
                refreshBuildings.run();
                UiAlerts.info("删除成功", "楼栋目录已删除。");
            } catch (RuntimeException exception) {
                UiAlerts.error("删除失败", exception.getMessage());
            }
        }).getNode();
        Node resetBuildingButton = FusionUiFactory.createGhostButton("新建楼栋", 120, 40, resetBuildingForm).getNode();

        Node saveRoomButton = FusionUiFactory.createPrimaryButton("保存房间", 168, 40, () -> {
            try {
                if (selectedBuilding.get() == null) {
                    throw new IllegalArgumentException("请先选择一个楼栋，再维护房间。");
                }
                Long savedId = appContext.dormCatalogService().saveRoom(new SaveDormRoomCommand(
                        selectedRoom.get() == null ? null : selectedRoom.get().getId(),
                        selectedBuilding.get().getId(),
                        roomNoField.getText(),
                        parseInteger(floorNoField.getText(), "楼层必须是数字。"),
                        parseInteger(bedCountField.getText(), "床位数必须是数字。"),
                        roomStatusBox.getValue()
                ));
                renderRooms.run();
                selectRoom(savedId, selectedBuilding.get(), selectedRoom, roomListContainer);
                UiAlerts.info("保存成功", "宿舍房间目录已更新。");
            } catch (RuntimeException exception) {
                UiAlerts.error("保存失败", exception.getMessage());
            }
        }).getNode();
        Node deleteRoomButton = FusionUiFactory.createGhostButton("删除房间", 120, 40, () -> {
            try {
                if (selectedRoom.get() == null) {
                    throw new IllegalArgumentException("请先选择一个房间。");
                }
                appContext.dormCatalogService().deleteRoom(selectedRoom.get().getId());
                resetRoomForm.run();
                renderRooms.run();
                UiAlerts.info("删除成功", "房间目录已删除。");
            } catch (RuntimeException exception) {
                UiAlerts.error("删除失败", exception.getMessage());
            }
        }).getNode();
        Node resetRoomButton = FusionUiFactory.createGhostButton("新建房间", 120, 40, resetRoomForm).getNode();

        Node buildingPanel = buildBuildingPanel(
                buildingKeywordField,
                areaFilterBox,
                buildingListContainer,
                campusField,
                buildingNoField,
                buildingNameField,
                resetBuildingButton,
                deleteBuildingButton,
                saveBuildingButton,
                refreshBuildings
        );
        Node roomPanel = buildRoomPanel(
                selectedBuilding,
                roomListContainer,
                roomNoField,
                floorNoField,
                bedCountField,
                roomStatusBox,
                resetRoomButton,
                deleteRoomButton,
                saveRoomButton
        );

        refreshBuildings.run();
        renderRooms.run();

        return createPage("宿舍目录维护", "", createRatioWorkspace(48, 52, buildingPanel, roomPanel));
    }

    private Node buildBuildingPanel(
            TextField keywordField,
            AppDropdown<String> areaFilterBox,
            VBox buildingListContainer,
            TextField campusField,
            TextField buildingNoField,
            TextField buildingNameField,
            Node resetBuildingButton,
            Node deleteBuildingButton,
            Node saveBuildingButton,
            Runnable refreshBuildings
    ) {
        GridPane filterGrid = createFilterGrid(46, 32, 22);
        filterGrid.add(createFieldLabel("关键词筛选"), 0, 0);
        filterGrid.add(createFieldLabel("宿舍区筛选"), 1, 0);
        filterGrid.add(keywordField, 0, 1);
        filterGrid.add(areaFilterBox, 1, 1);
        filterGrid.add(createFilterActionButton("刷新楼栋", refreshBuildings), 2, 1);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(14);
        formGrid.setVgap(14);
        formGrid.getColumnConstraints().addAll(percentColumn(50), percentColumn(50));
        formGrid.add(createFieldBlock("宿舍区", campusField), 0, 0);
        formGrid.add(createFieldBlock("楼栋编号", buildingNoField), 1, 0);
        formGrid.add(createFieldBlock("楼栋名称", buildingNameField), 0, 1, 2, 1);

        HBox actionRow = new HBox(12, resetBuildingButton, deleteBuildingButton, saveBuildingButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(saveBuildingButton, Priority.ALWAYS);
        if (saveBuildingButton instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }

        VBox content = new VBox(16, filterGrid, formGrid, actionRow, buildingListContainer);
        content.setFillWidth(true);
        VBox.setVgrow(buildingListContainer, Priority.ALWAYS);
        return wrapPanel("楼栋目录", content);
    }

    private Node buildRoomPanel(
            ObjectProperty<DormBuilding> selectedBuilding,
            VBox roomListContainer,
            TextField roomNoField,
            TextField floorNoField,
            TextField bedCountField,
            AppDropdown<String> roomStatusBox,
            Node resetRoomButton,
            Node deleteRoomButton,
            Node saveRoomButton
    ) {
        Label currentBuildingTitle = new Label("未选择楼栋");
        currentBuildingTitle.getStyleClass().add("dashboard-mini-value");
        currentBuildingTitle.setWrapText(true);

        Label currentBuildingDesc = new Label("先在左侧选择一个楼栋，再维护该楼栋下的房间与停用状态。");
        currentBuildingDesc.getStyleClass().add("dashboard-mini-description");
        currentBuildingDesc.setWrapText(true);

        selectedBuilding.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                currentBuildingTitle.setText("未选择楼栋");
                currentBuildingDesc.setText("先在左侧选择一个楼栋，再维护该楼栋下的房间与停用状态。");
                return;
            }
            currentBuildingTitle.setText(newValue.getCampusName() + " / " + newValue.getBuildingNo());
            currentBuildingDesc.setText(safeText(newValue.getBuildingName()));
        });

        GridPane formGrid = new GridPane();
        formGrid.setHgap(14);
        formGrid.setVgap(14);
        formGrid.getColumnConstraints().addAll(percentColumn(50), percentColumn(50));
        formGrid.add(createFieldBlock("房间号", roomNoField), 0, 0);
        formGrid.add(createFieldBlock("房间状态", roomStatusBox), 1, 0);
        formGrid.add(createFieldBlock("楼层", floorNoField), 0, 1);
        formGrid.add(createFieldBlock("床位数", bedCountField), 1, 1);

        HBox actionRow = new HBox(12, resetRoomButton, deleteRoomButton, saveRoomButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(saveRoomButton, Priority.ALWAYS);
        if (saveRoomButton instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }

        VBox summary = new VBox(8, currentBuildingTitle, currentBuildingDesc);
        summary.setFillWidth(true);

        VBox content = new VBox(
                16,
                createInlineSummaryCard("当前楼栋", summary, "dashboard-mini-card", "dashboard-mini-soft"),
                formGrid,
                actionRow,
                roomListContainer
        );
        content.setFillWidth(true);
        VBox.setVgrow(roomListContainer, Priority.ALWAYS);
        return wrapPanel("房间目录", content);
    }

    private AppDropdown<String> createAreaFilterBox() {
        AppDropdown<String> box = new AppDropdown<>();
        box.setVisibleRowCount(8);
        box.setPromptText(ALL_AREAS);
        box.setValue(ALL_AREAS);
        return box;
    }

    private AppDropdown<String> createRoomStatusBox() {
        AppDropdown<String> box = new AppDropdown<>();
        box.setItems(List.of(ROOM_STATUS_ACTIVE, ROOM_STATUS_INACTIVE));
        box.setTextMapper(this::roomStatusLabel);
        box.setVisibleRowCount(4);
        box.setValue(ROOM_STATUS_ACTIVE);
        return box;
    }

    private TextField createInput(String promptText) {
        TextField field = new TextField();
        field.getStyleClass().addAll("text-field", "login-input");
        field.setPromptText(promptText);
        return field;
    }

    private VBox createListContainer() {
        VBox box = new VBox();
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }

    private void refreshAreaFilter(AppDropdown<String> areaFilterBox) {
        List<String> areas = new ArrayList<>();
        areas.add(ALL_AREAS);
        areas.addAll(appContext.dormCatalogService().listDormAreas());
        String current = areaFilterBox.getValue();
        areaFilterBox.setItems(areas);
        if (current != null && areas.contains(current)) {
            areaFilterBox.setValue(current);
            return;
        }
        areaFilterBox.setValue(ALL_AREAS);
    }

    private void applyBuildingFilters(
            ObservableList<DormBuilding> sourceRows,
            String keyword,
            String areaFilter,
            VBox container,
            ObjectProperty<DormBuilding> selectedBuilding
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);
        Long selectedId = selectedBuilding.get() == null ? null : selectedBuilding.get().getId();
        List<DormBuilding> visibleRows = sourceRows.stream()
                .filter(item -> ALL_AREAS.equals(areaFilter) || areaFilter == null || areaFilter.isBlank() || areaFilter.equals(item.getCampusName()))
                .filter(item -> matchesKeyword(normalizedKeyword, item.getCampusName(), item.getBuildingNo(), item.getBuildingName()))
                .toList();

        DormBuilding restored = selectedId == null
                ? null
                : visibleRows.stream().filter(item -> selectedId.equals(item.getId())).findFirst().orElse(null);
        selectedBuilding.set(restored);
        renderBuildingTable(container, visibleRows, selectedBuilding);
    }

    private void renderBuildingTable(VBox container, List<DormBuilding> rows, ObjectProperty<DormBuilding> selectedBuilding) {
        container.getChildren().clear();
        if (rows.isEmpty()) {
            container.getChildren().add(createEmptyState("当前没有楼栋目录", "你可以先在上方新增一个宿舍区楼栋，保存后会立刻出现在这里。"));
            return;
        }
        container.getChildren().add(createSelectableGrid(
                List.of("宿舍区", "楼栋编号", "楼栋名称"),
                List.of(34.0, 26.0, 40.0),
                rows.size(),
                (rowIndex, columnIndex) -> {
                    DormBuilding item = rows.get(rowIndex);
                    return switch (columnIndex) {
                        case 0 -> safeText(item.getCampusName());
                        case 1 -> safeText(item.getBuildingNo());
                        default -> safeText(item.getBuildingName());
                    };
                },
                rowIndex -> {
                    DormBuilding item = rows.get(rowIndex);
                    boolean isSelected = selectedBuilding.get() != null && selectedBuilding.get().getId().equals(item.getId());
                    return new SelectableRowConfig(isSelected, () -> selectedBuilding.set(item));
                }
        ));
    }

    private void renderRoomTable(VBox container, DormBuilding selectedBuilding, ObjectProperty<DormRoom> selectedRoom) {
        container.getChildren().clear();
        if (selectedBuilding == null) {
            container.getChildren().add(createEmptyState("先选择楼栋", "左侧选中一个楼栋后，右侧才会显示该楼栋下的房间目录。"));
            return;
        }
        List<DormRoom> rows = appContext.dormCatalogService().listRoomsByBuilding(selectedBuilding.getId());
        if (rows.isEmpty()) {
            container.getChildren().add(createEmptyState("当前楼栋还没有房间", "可以在上方补充房间号、楼层、床位数和状态。"));
            return;
        }
        container.getChildren().add(createSelectableGrid(
                List.of("房间号", "楼层", "床位数", "状态"),
                List.of(30.0, 18.0, 20.0, 32.0),
                rows.size(),
                (rowIndex, columnIndex) -> {
                    DormRoom item = rows.get(rowIndex);
                    return switch (columnIndex) {
                        case 0 -> safeText(item.getRoomNo());
                        case 1 -> item.getFloorNo() == null ? "" : String.valueOf(item.getFloorNo());
                        case 2 -> item.getBedCount() == null ? "" : String.valueOf(item.getBedCount());
                        default -> roomStatusLabel(item.getRoomStatus());
                    };
                },
                rowIndex -> {
                    DormRoom item = rows.get(rowIndex);
                    boolean isSelected = selectedRoom.get() != null && selectedRoom.get().getId().equals(item.getId());
                    return new SelectableRowConfig(isSelected, () -> selectedRoom.set(item));
                }
        ));
    }

    private void selectBuilding(
            Long buildingId,
            ObservableList<DormBuilding> sourceRows,
            ObjectProperty<DormBuilding> selectedBuilding,
            VBox container,
            String keyword,
            String areaFilter
    ) {
        DormBuilding matched = buildingId == null
                ? null
                : sourceRows.stream().filter(item -> buildingId.equals(item.getId())).findFirst().orElse(null);
        selectedBuilding.set(matched);
        applyBuildingFilters(sourceRows, keyword, areaFilter, container, selectedBuilding);
    }

    private void selectRoom(Long roomId, DormBuilding selectedBuilding, ObjectProperty<DormRoom> selectedRoom, VBox container) {
        List<DormRoom> rows = selectedBuilding == null ? List.of() : appContext.dormCatalogService().listRoomsByBuilding(selectedBuilding.getId());
        DormRoom matched = roomId == null
                ? null
                : rows.stream().filter(item -> roomId.equals(item.getId())).findFirst().orElse(null);
        selectedRoom.set(matched);
        renderRoomTable(container, selectedBuilding, selectedRoom);
    }

    private GridPane createSelectableGrid(
            List<String> headers,
            List<Double> weights,
            int rowCount,
            CellTextProvider textProvider,
            RowConfigProvider rowConfigProvider
    ) {
        GridPane table = new GridPane();
        table.getStyleClass().add("selectable-static-grid-table");
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMinWidth(0);
        for (Double weight : weights) {
            table.getColumnConstraints().add(percentColumn(weight));
        }

        for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
            addSelectableCell(table, 0, columnIndex, headers.get(columnIndex), true, false, columnIndex == 0, columnIndex == headers.size() - 1, null);
        }

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            SelectableRowConfig config = rowConfigProvider.create(rowIndex);
            for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
                addSelectableCell(
                        table,
                        rowIndex + 1,
                        columnIndex,
                        textProvider.textAt(rowIndex, columnIndex),
                        false,
                        config.selected(),
                        false,
                        columnIndex == headers.size() - 1,
                        config.clickAction()
                );
            }
        }
        return table;
    }

    private void addSelectableCell(
            GridPane table,
            int rowIndex,
            int columnIndex,
            String text,
            boolean headerCell,
            boolean selected,
            boolean firstHeaderCell,
            boolean lastCell,
            Runnable clickAction
    ) {
        Label label = new Label(text == null ? "" : text);
        label.getStyleClass().add(headerCell ? "static-table-header-cell" : "static-table-cell");
        label.setWrapText(!headerCell);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinWidth(0);

        HBox shell = new HBox(label);
        shell.setAlignment(Pos.CENTER);
        shell.setMinHeight(48);
        shell.setMaxWidth(Double.MAX_VALUE);
        shell.setMinWidth(0);
        shell.getStyleClass().add(headerCell ? "selectable-static-grid-header-shell" : "selectable-static-grid-cell-shell");
        if (firstHeaderCell) {
            shell.getStyleClass().add("selectable-static-grid-header-first");
        }
        if (lastCell) {
            shell.getStyleClass().add(headerCell ? "selectable-static-grid-header-last" : "selectable-static-grid-cell-last");
        }
        if (selected && !headerCell) {
            shell.getStyleClass().add("selectable-static-grid-cell-selected");
        }
        if (clickAction != null) {
            shell.getStyleClass().add("selectable-static-grid-clickable");
            shell.setOnMouseClicked(event -> clickAction.run());
        }
        table.add(shell, columnIndex, rowIndex);
    }

    private Integer parseInteger(String rawValue, String errorMessage) {
        try {
            return Integer.parseInt(rawValue == null ? "" : rawValue.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private String roomStatusLabel(String value) {
        if (ROOM_STATUS_INACTIVE.equalsIgnoreCase(value)) {
            return "停用中";
        }
        return "启用中";
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    @FunctionalInterface
    private interface CellTextProvider {
        String textAt(int rowIndex, int columnIndex);
    }

    @FunctionalInterface
    private interface RowConfigProvider {
        SelectableRowConfig create(int rowIndex);
    }

    private record SelectableRowConfig(boolean selected, Runnable clickAction) {
    }
}
