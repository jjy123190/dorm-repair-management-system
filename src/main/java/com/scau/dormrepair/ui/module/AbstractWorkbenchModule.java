package com.scau.dormrepair.ui.module;

import com.scau.dormrepair.common.AppContext;
import com.scau.dormrepair.ui.component.FusionUiFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
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

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("plain-text");
        descriptionLabel.setWrapText(true);

        container.getChildren().addAll(titleLabel, descriptionLabel);
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
}
