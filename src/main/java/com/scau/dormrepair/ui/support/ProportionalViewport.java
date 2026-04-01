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

        double scale = Math.min(1.0, Math.min(viewportWidth / designWidth, viewportHeight / designHeight));
        double scaledWidth = designWidth * scale;
        double scaledHeight = designHeight * scale;
        double offsetX = (viewportWidth - scaledWidth) / 2;
        double offsetY = (viewportHeight - scaledHeight) / 2;

        scaleTransform.setX(scale);
        scaleTransform.setY(scale);

        if (content instanceof Region region) {
            region.resizeRelocate(offsetX, offsetY, designWidth, designHeight);
        } else {
            content.relocate(offsetX, offsetY);
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