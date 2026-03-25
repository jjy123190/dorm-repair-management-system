package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import java.util.Arrays;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * 模块基类。
 * 统一页面标题、说明和面板样式，后面新增模块直接继承。
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

        // 页面内容居中后，两侧会自然留出呼吸空间，不会整块贴着滚动区域边界堆满。
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
        // 左右工作区统一走固定比例，避免窗口重排时两边互相抢宽度。
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
        // 表单标签和输入控件保持同一套间距，后面模块只关心字段本身。
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
        // 小型摘要卡只负责承载“当前选择/当前工单”这类上下文，不再让每个模块重复拼壳。
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
        // 顶部身份条统一成一张摘要卡，模块只传身份和说明，不再各写一套大字+说明布局。
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

    protected <T> void configureFixedTable(TableView<T> tableView, double prefHeight, double... columnWeights) {
        // 表格统一走受控缩放策略，让所有列始终收在可视区域内，避免表头和表体因为横向滚动条出现错位。
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
            // 这里用固定比例的首选宽度作为“黄金分配”基线，真正铺满宽度交给 CONSTRAINED_RESIZE_POLICY 完成。
            column.setPrefWidth(Math.max(96, 960 * (weight / weightSum)));
        }
    }

    protected void fitTableHeightToRows(TableView<?> tableView, int itemCount, int minVisibleRows, int maxVisibleRows) {
        // 数据条数不多时，直接按可见行数收表高，避免空白行和滚动条把表头表体挤得像没对齐。
        int visibleRows = Math.max(minVisibleRows, Math.min(maxVisibleRows, Math.max(itemCount, 1)));
        double headerHeight = 44;
        double totalHeight = headerHeight + visibleRows * tableView.getFixedCellSize() + 4;
        tableView.setMinHeight(totalHeight);
        tableView.setPrefHeight(totalHeight);
        tableView.setMaxHeight(totalHeight);
    }

    private Node prepareContentNode(Node node) {
        if (node instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMinWidth(0);
        }
        return node;
    }
}
