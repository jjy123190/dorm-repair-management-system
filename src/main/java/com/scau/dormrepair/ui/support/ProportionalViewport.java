package com.scau.dormrepair.ui.support;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;

public class ProportionalViewport extends Pane {

    private final Node content;
    private final double designWidth;
    private final double designHeight;
    private final Scale scaleTransform;

    public ProportionalViewport(Node content, double designWidth, double designHeight) {
        this.content = content;
        this.designWidth = designWidth;
        this.designHeight = designHeight;
        this.scaleTransform = new Scale(1, 1, 0, 0);

        content.setManaged(false);
        content.getTransforms().add(scaleTransform);
        getChildren().add(content);
    }

    @Override
    protected void layoutChildren() {
        double viewportWidth = getWidth();
        double viewportHeight = getHeight();
        if (viewportWidth <= 0 || viewportHeight <= 0 || designWidth <= 0 || designHeight <= 0) {
            return;
        }

        double scaleX = viewportWidth / designWidth;
        double scaleY = viewportHeight / designHeight;

        scaleTransform.setX(scaleX);
        scaleTransform.setY(scaleY);

        if (content instanceof Region region) {
            region.resizeRelocate(0, 0, designWidth, designHeight);
        } else {
            content.relocate(0, 0);
        }
    }

    @Override
    protected double computePrefWidth(double height) {
        return designWidth;
    }

    @Override
    protected double computePrefHeight(double width) {
        return designHeight;
    }
}
