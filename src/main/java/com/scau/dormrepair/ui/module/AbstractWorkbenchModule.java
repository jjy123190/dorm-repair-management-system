package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.domain.view.WorkOrderRecordView;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import com.scau.dormrepair.ui.support.UiDisplayText;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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

public abstract class AbstractWorkbenchModule implements WorkbenchModule {

    private static final double COMPACT_WORKSPACE_BREAKPOINT = 1120;
    private static final double WORKBENCH_MAX_WIDTH = 1720;
    private static final DateTimeFormatter TIMELINE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    protected final AppContext appContext;

    protected AbstractWorkbenchModule(AppContext appContext) {
        this.appContext = appContext;
    }

    protected Parent createPage(String title, String description, Node... contentNodes) {
        VBox container = new VBox(18);
        container.getStyleClass().add("workbench-page");
        container.setFillWidth(true);
        container.setMinWidth(0);
        container.setMaxWidth(WORKBENCH_MAX_WIDTH);

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
        shell.setPadding(new Insets(18, 20, 24, 20));
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
        workspace.setHgap(20);
        workspace.setVgap(18);
        workspace.setMinWidth(0);
        workspace.setMaxWidth(Double.MAX_VALUE);

        Node preparedLeft = prepareContentNode(leftContent);
        Node preparedRight = prepareContentNode(rightContent);
        refreshRatioWorkspaceLayout(workspace, preparedLeft, preparedRight, leftPercent, rightPercent, workspace.getWidth());
        workspace.widthProperty().addListener((observable, oldValue, newValue) ->
                refreshRatioWorkspaceLayout(workspace, preparedLeft, preparedRight, leftPercent, rightPercent, newValue.doubleValue())
        );
        return workspace;
    }

    private void refreshRatioWorkspaceLayout(
            GridPane workspace,
            Node leftContent,
            Node rightContent,
            double leftPercent,
            double rightPercent,
            double availableWidth
    ) {
        workspace.getChildren().clear();
        workspace.getColumnConstraints().clear();

        if (availableWidth > 0 && availableWidth < COMPACT_WORKSPACE_BREAKPOINT) {
            workspace.getColumnConstraints().add(percentColumn(100));
            workspace.add(leftContent, 0, 0);
            workspace.add(rightContent, 0, 1);
            return;
        }

        workspace.getColumnConstraints().addAll(percentColumn(leftPercent), percentColumn(rightPercent));
        workspace.add(leftContent, 0, 0);
        workspace.add(rightContent, 1, 0);
    }

    protected VBox createFieldBlock(String labelText, Region input) {
        Label label = createFieldLabel(labelText);
        input.setMaxWidth(Double.MAX_VALUE);
        input.setMinWidth(0);

        VBox block = new VBox(8, label, input);
        block.setMaxWidth(Double.MAX_VALUE);
        block.setMinWidth(0);
        return block;
    }

    protected Label createFieldLabel(String labelText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        return label;
    }

    protected GridPane createFilterGrid(double... columnPercents) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setMinWidth(0);
        for (double percent : columnPercents) {
            grid.getColumnConstraints().add(percentColumn(percent));
        }
        return grid;
    }

    protected Button createFilterActionButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("surface-button", "filter-action-button");
        button.setMinHeight(44);
        button.setPrefHeight(44);
        button.setMaxHeight(44);
        button.setMinWidth(108);
        button.setFocusTraversable(false);
        button.setOnAction(event -> action.run());
        return button;
    }

    protected VBox createFilterActionBlock(Button button) {
        VBox block = new VBox(button);
        block.setFillWidth(true);
        block.setAlignment(Pos.BOTTOM_LEFT);
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

    protected Node createEmptyState(String title, String description) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("empty-state-title");

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("empty-state-description");
        descriptionLabel.setWrapText(true);

        VBox box = new VBox(8, titleLabel, descriptionLabel);
        box.getStyleClass().add("empty-state-box");
        box.setAlignment(Pos.CENTER_LEFT);
        box.setFillWidth(true);
        return box;
    }

    protected Node createTimelineList(List<WorkOrderRecordView> records, String emptyTitle, String emptyDescription) {
        VBox box = new VBox(10);
        box.setFillWidth(true);
        if (records == null || records.isEmpty()) {
            box.getChildren().add(createEmptyState(emptyTitle, emptyDescription));
            return box;
        }
        for (WorkOrderRecordView record : records) {
            box.getChildren().add(createTimelineCard(record));
        }
        return box;
    }

    protected <T> void installTablePlaceholder(TableView<T> tableView, String title, String description) {
        tableView.setPlaceholder(createEmptyState(title, description));
    }

    protected <T> Node createStaticDataTableOrEmpty(
            List<StaticTableColumn<T>> columns,
            List<T> rows,
            int minVisibleRows,
            String emptyTitle,
            String emptyDescription
    ) {
        if (rows == null || rows.isEmpty()) {
            return createEmptyState(emptyTitle, emptyDescription);
        }
        return createStaticDataTable(columns, rows, minVisibleRows);
    }

    protected ColumnConstraints percentColumn(double percentWidth) {
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(percentWidth);
        column.setHgrow(Priority.ALWAYS);
        column.setFillWidth(true);
        column.setMinWidth(0);
        return column;
    }

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
            column.setMinWidth(0);
            column.setMaxWidth(Double.MAX_VALUE);
            column.setPrefWidth(Math.max(72, 860 * (weight / weightSum)));
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

    protected <T> Node createStaticDataTable(List<StaticTableColumn<T>> columns, List<T> rows, int minVisibleRows) {
        GridPane table = new GridPane();
        table.getStyleClass().add("static-table");
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMinWidth(0);
        table.setHgap(0);
        table.setVgap(0);

        double weightSum = columns.stream().mapToDouble(StaticTableColumn::weight).sum();
        for (StaticTableColumn<T> column : columns) {
            table.getColumnConstraints().add(percentColumn(column.weight() * 100 / weightSum));
        }

        for (int index = 0; index < columns.size(); index++) {
            StaticTableColumn<T> column = columns.get(index);
            addStaticTableCell(table, 0, index, column.title(), true, index == 0, index == columns.size() - 1);
        }

        int visibleRows = Math.max(minVisibleRows, rows.size());
        for (int rowIndex = 0; rowIndex < visibleRows; rowIndex++) {
            T rowItem = rowIndex < rows.size() ? rows.get(rowIndex) : null;
            for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                StaticTableColumn<T> column = columns.get(columnIndex);
                String text = rowItem == null ? "" : safeCellText(column.valueProvider().apply(rowItem));
                addStaticTableCell(table, rowIndex + 1, columnIndex, text, false, false, columnIndex == columns.size() - 1);
            }
        }
        return table;
    }

    protected <T> StaticTableColumn<T> staticColumn(String title, double weight, Function<T, String> valueProvider) {
        return new StaticTableColumn<>(title, weight, valueProvider);
    }

    protected String normalizeKeyword(String rawText) {
        return rawText == null ? "" : rawText.trim().toLowerCase(Locale.ROOT);
    }

    protected boolean matchesKeyword(String normalizedKeyword, String... candidates) {
        if (normalizedKeyword == null || normalizedKeyword.isBlank()) {
            return true;
        }
        for (String candidate : candidates) {
            if (candidate != null && candidate.toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                return true;
            }
        }
        return false;
    }

    protected String placeholderText(String value) {
        return value == null || value.isBlank() ? "--" : value.trim();
    }

    protected String formatTimelineTime(LocalDateTime value) {
        return value == null ? "--" : TIMELINE_TIME_FORMATTER.format(value);
    }

    private void addStaticTableCell(
            GridPane table,
            int rowIndex,
            int columnIndex,
            String text,
            boolean headerCell,
            boolean firstHeaderCell,
            boolean lastCell
    ) {
        Label label = new Label(text == null ? "" : text);
        label.getStyleClass().add(headerCell ? "static-table-header-cell" : "static-table-cell");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinWidth(0);

        HBox cellShell = new HBox(label);
        cellShell.getStyleClass().add(headerCell ? "static-table-header-cell-shell" : "static-table-cell-shell");
        if (firstHeaderCell) {
            cellShell.getStyleClass().add("static-table-header-first-cell-shell");
        }
        if (lastCell) {
            cellShell.getStyleClass().add(headerCell ? "static-table-header-last-cell-shell" : "static-table-last-cell-shell");
        }
        cellShell.setAlignment(Pos.CENTER);
        cellShell.setMaxWidth(Double.MAX_VALUE);
        cellShell.setMinWidth(0);
        table.add(cellShell, columnIndex, rowIndex);
    }

    private Node prepareContentNode(Node node) {
        if (node instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMinWidth(0);
        }
        return node;
    }

    private Node createTimelineCard(WorkOrderRecordView record) {
        Label titleLabel = new Label(UiDisplayText.workOrderStatus(record.getStatus()));
        titleLabel.getStyleClass().add("student-timeline-title");

        Label timeLabel = new Label(formatTimelineTime(record.getRecordedAt()));
        timeLabel.getStyleClass().add("student-timeline-time");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, titleLabel, spacer, timeLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        Label copyLabel = new Label(composeTimelineCopy(record));
        copyLabel.getStyleClass().add("student-timeline-copy");
        copyLabel.setWrapText(true);
        copyLabel.setMaxWidth(Double.MAX_VALUE);

        VBox body = new VBox(8, header, copyLabel);
        body.getStyleClass().add("student-timeline-card");
        body.setFillWidth(true);
        body.setMaxWidth(Double.MAX_VALUE);
        return body;
    }

    private String composeTimelineCopy(WorkOrderRecordView record) {
        String operatorName = record.getOperatorName();
        String operatorRole = record.getOperatorRole() == null ? "" : record.getOperatorRole().displayName();
        String operatorText;
        if (operatorName == null || operatorName.isBlank()) {
            operatorText = operatorRole.isBlank() ? "\u7cfb\u7edf" : operatorRole;
        } else if (operatorRole.isBlank()) {
            operatorText = operatorName;
        } else {
            operatorText = operatorName + " / " + operatorRole;
        }
        return "\u64cd\u4f5c\u4eba\uff1a"
                + operatorText
                + System.lineSeparator()
                + "\u5907\u6ce8\uff1a"
                + placeholderText(record.getRecordNote());
    }
    private String safeCellText(String value) {
        return value == null ? "" : value;
    }

    protected record StaticTableColumn<T>(String title, double weight, Function<T, String> valueProvider) {
    }
}
