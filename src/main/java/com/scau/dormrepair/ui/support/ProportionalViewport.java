package com.scau.dormrepair.ui.support;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

public class ProportionalViewport extends Pane {

    private final Node content;
    private final double designWidth;
    private final double designHeight;
    private final Scale scaleTransform;
    private final Rectangle viewportClip;

    public ProportionalViewport(Node content, double designWidth, double designHeight) {
        this.content = content;
        this.designWidth = designWidth;
        this.designHeight = designHeight;
        this.scaleTransform = new Scale(1, 1, 0, 0);
        this.viewportClip = new Rectangle();

        content.setManaged(false);
        content.getTransforms().add(scaleTransform);
        setClip(viewportClip);
        getChildren().add(content);
    }

    @Override
    protected void layoutChildren() {
        viewportClip.setWidth(getWidth());
        viewportClip.setHeight(getHeight());

        LayoutMetrics metrics = resolveLayout(getWidth(), getHeight(), designWidth, designHeight);
        if (metrics == null) {
            return;
        }

        scaleTransform.setX(metrics.scale());
        scaleTransform.setY(metrics.scale());

        if (content instanceof Region region) {
            region.resize(designWidth, designHeight);
            region.relocate(metrics.offsetX(), metrics.offsetY());
        } else {
            content.relocate(metrics.offsetX(), metrics.offsetY());
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

    static LayoutMetrics resolveLayout(
            double viewportWidth,
            double viewportHeight,
            double designWidth,
            double designHeight
    ) {
        if (viewportWidth <= 0 || viewportHeight <= 0 || designWidth <= 0 || designHeight <= 0) {
            return null;
        }

        // Keep the full UI visible and scale it proportionally when the viewport is smaller.
        double scale = Math.min(viewportWidth / designWidth, viewportHeight / designHeight);
        double scaledWidth = designWidth * scale;
        double scaledHeight = designHeight * scale;
        double offsetX = (viewportWidth - scaledWidth) / 2;
        double offsetY = (viewportHeight - scaledHeight) / 2;
        return new LayoutMetrics(scale, offsetX, offsetY);
    }

    record LayoutMetrics(double scale, double offsetX, double offsetY) {
    }
}
