package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * 工作台模块共享底座。
 * 这里统一页面标题、面板样式、左右布局比例和静态表格结构，避免每个模块各写一套。
 */
public abstract class AbstractWorkbenchModule implements WorkbenchModule {

    protected final AppContext appContext;

    protected AbstractWorkbenchModule(AppContext appContext) {
        this.appContext = appContext;
    }

    protected Parent createPage(String title, String description, Node... contentNodes) {
        VBox container = new VBox(18);
        container.getStyleClass().add("workbench-page");
        container.setFillWidth(true);
        container.setMinWidth(0);
        container.setMaxWidth(1360);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");
        container.getChildren().add(titleLabel);

        if (description != null && !description.isBlank()) {
            Label descriptionLabel = new Label(description);
            descriptionLabel.getStyleClass().add("plain-text");
            descriptionLabel.setWrapText(true);
            container.getChildren().add(descriptionLabel);
        }

        for (Node contentNode : contentNodes) {
            if (contentNode instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMinWidth(0);
            }
            container.getChildren().add(contentNode);
        }

        StackPane shell = new StackPane(container);
        shell.getStyleClass().add("workbench-page-shell");
        shell.setAlignment(Pos.TOP_CENTER);
        shell.setPadding(new Insets(18, 24, 24, 24));
        return shell;
    }

    protected Node wrapPanel(String title, Node content) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("subsection-title");

        VBox box = new VBox(12, titleLabel, content);
        box.getStyleClass().add("fusion-panel-body");
        box.setPadding(new Insets(18, 20, 20, 20));
        box.setMaxWidth(Double.MAX_VALUE);
        box.setMinWidth(0);

        if (content instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMinWidth(0);
            VBox.setVgrow(region, Priority.ALWAYS);
        }

        var pane = FusionUiFactory.createCard(box, 0, 0, "fusion-card-shell", "fusion-panel-box");
        pane.getNode().setMaxWidth(Double.MAX_VALUE);
        pane.getNode().setMinWidth(0);
        VBox.setVgrow(pane.getNode(), Priority.ALWAYS);
        return pane.getNode();
    }

    protected GridPane createRatioWorkspace(double leftPercent, double rightPercent, Node leftContent, Node rightContent) {
        GridPane workspace = new GridPane();
        workspace.setHgap(18);
        workspace.setVgap(18);
        workspace.setMinWidth(0);
        workspace.setMaxWidth(Double.MAX_VALUE);
        workspace.getColumnConstraints().addAll(percentColumn(leftPercent), percentColumn(rightPercent));
        workspace.add(prepareContentNode(leftContent), 0, 0);
        workspace.add(prepareContentNode(rightContent), 1, 0);
        return workspace;
    }

    protected VBox createFieldBlock(String labelText, Region input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        input.setMaxWidth(Double.MAX_VALUE);
        input.setMinWidth(0);

        VBox block = new VBox(8, label, input);
        block.setMaxWidth(Double.MAX_VALUE);
        block.setMinWidth(0);
        return block;
    }

    protected Node createInlineSummaryCard(String tagText, Node content, String... styleClasses) {
        Label tagLabel = new Label(tagText);
        tagLabel.getStyleClass().add("dashboard-mini-tag");

        VBox box = new VBox(10, tagLabel, content);
        box.getStyleClass().add("workbench-inline-card-body");
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);

        var pane = FusionUiFactory.createCard(box, 0, 0, styleClasses);
        pane.getNode().setMaxWidth(Double.MAX_VALUE);
        pane.getNode().setMinWidth(0);
        return pane.getNode();
    }

    protected Node createIdentityBanner(String tagText, String titleText, String descriptionText, String... styleClasses) {
        Label tagLabel = new Label(tagText);
        tagLabel.getStyleClass().add("dashboard-mini-tag");

        Label titleLabel = new Label(titleText);
        titleLabel.getStyleClass().add("dashboard-mini-value");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label descriptionLabel = new Label(descriptionText);
        descriptionLabel.getStyleClass().add("dashboard-mini-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(Double.MAX_VALUE);

        VBox box = new VBox(8, tagLabel, titleLabel, descriptionLabel);
        box.getStyleClass().add("workbench-banner-body");
        box.setFillWidth(true);
        box.setMaxWidth(Double.MAX_VALUE);

        var pane = FusionUiFactory.createCard(box, 0, 0, styleClasses);
        pane.getNode().setMaxWidth(Double.MAX_VALUE);
        pane.getNode().setMinWidth(0);
        return pane.getNode();
    }

    protected ColumnConstraints percentColumn(double percentWidth) {
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(percentWidth);
        column.setHgrow(Priority.ALWAYS);
        column.setFillWidth(true);
        column.setMinWidth(0);
        return column;
    }

    /**
     * 管理员和维修员的业务表仍然需要选择态，所以保留受控 TableView 版本。
     */
    protected <T> void configureFixedTable(TableView<T> tableView, double prefHeight, double... columnWeights) {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setTableMenuButtonVisible(false);
        tableView.setEditable(false);
        tableView.setFixedCellSize(38);
        tableView.setMinWidth(0);
        tableView.setMaxWidth(Double.MAX_VALUE);
        tableView.setPrefHeight(prefHeight);

        double weightSum = Arrays.stream(columnWeights).sum();
        for (int index = 0; index < tableView.getColumns().size() && index < columnWeights.length; index++) {
            TableColumn<?, ?> column = tableView.getColumns().get(index);
            double weight = columnWeights[index];
            column.setSortable(false);
            column.setReorderable(false);
            column.setResizable(false);
            column.setStyle("-fx-alignment: CENTER;");
            column.setPrefWidth(Math.max(96, 960 * (weight / weightSum)));
        }
    }

    protected void fitTableHeightToRows(TableView<?> tableView, int itemCount, int minVisibleRows, int maxVisibleRows) {
        int visibleRows = Math.max(minVisibleRows, Math.min(maxVisibleRows, Math.max(itemCount, 1)));
        double headerHeight = 44;
        double totalHeight = headerHeight + visibleRows * tableView.getFixedCellSize() + 4;
        tableView.setMinHeight(totalHeight);
        tableView.setPrefHeight(totalHeight);
        tableView.setMaxHeight(totalHeight);
    }

    /**
     * 只读展示型表格直接用静态网格，列宽、边界线、空白行完全受控，不再受 TableView 皮肤影响。
     */
    protected <T> Node createStaticDataTable(
            List<StaticTableColumn<T>> columns,
            List<T> rows,
            int minVisibleRows
    ) {
        VBox tableBox = new VBox();
        tableBox.getStyleClass().add("static-table");
        tableBox.setFillWidth(true);
        tableBox.setMaxWidth(Double.MAX_VALUE);
        tableBox.getChildren().add(createStaticTableRow(columns, null, true));

        int visibleRows = Math.max(minVisibleRows, rows.size());
        for (int index = 0; index < visibleRows; index++) {
            T rowItem = index < rows.size() ? rows.get(index) : null;
            tableBox.getChildren().add(createStaticTableRow(columns, rowItem, false));
        }
        return tableBox;
    }

    protected <T> StaticTableColumn<T> staticColumn(
            String title,
            double weight,
            Function<T, String> valueProvider
    ) {
        return new StaticTableColumn<>(title, weight, valueProvider);
    }

    private <T> GridPane createStaticTableRow(List<StaticTableColumn<T>> columns, T rowItem, boolean headerRow) {
        GridPane row = new GridPane();
        row.getStyleClass().add(headerRow ? "static-table-header-row" : "static-table-body-row");
        row.setMaxWidth(Double.MAX_VALUE);
        row.setMinWidth(0);

        double weightSum = columns.stream().mapToDouble(StaticTableColumn::weight).sum();
        for (StaticTableColumn<T> column : columns) {
            row.getColumnConstraints().add(percentColumn(column.weight() * 100 / weightSum));
        }

        for (int index = 0; index < columns.size(); index++) {
            StaticTableColumn<T> column = columns.get(index);
            String text = headerRow ? column.title() : safeCellText(rowItem == null ? "" : column.valueProvider().apply(rowItem));

            Label label = new Label(text);
            label.getStyleClass().add(headerRow ? "static-table-header-cell" : "static-table-cell");
            label.setWrapText(false);
            label.setAlignment(Pos.CENTER);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setMinWidth(0);

            HBox cellShell = new HBox(label);
            cellShell.getStyleClass().add(headerRow ? "static-table-header-cell-shell" : "static-table-cell-shell");
            if (index == columns.size() - 1) {
                cellShell.getStyleClass().add("static-table-last-cell-shell");
            }
            cellShell.setAlignment(Pos.CENTER);
            cellShell.setMaxWidth(Double.MAX_VALUE);
            cellShell.setMinWidth(0);
            row.add(cellShell, index, 0);
        }
        return row;
    }

    private Node prepareContentNode(Node node) {
        if (node instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMinWidth(0);
        }
        return node;
    }

    private String safeCellText(String value) {
        return value == null ? "" : value;
    }

    protected record StaticTableColumn<T>(
            String title,
            double weight,
            Function<T, String> valueProvider
    ) {
    }
}
