package com.scau.dormrepair.ui.component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class EvidenceGallery extends VBox {

    private final Label previewHint;
    private final ImageView previewImage;
    private final StackPane previewFrame;
    private final FlowPane thumbPane;
    private final List<StackPane> thumbShells = new ArrayList<>();
    private String emptyText;

    public EvidenceGallery(String emptyText) {
        super(10);
        this.emptyText = emptyText;
        this.previewHint = new Label(emptyText);
        this.previewHint.getStyleClass().add("helper-text");
        this.previewHint.setWrapText(true);

        this.previewImage = new ImageView();
        this.previewImage.setPreserveRatio(true);
        this.previewImage.setSmooth(true);
        this.previewImage.setManaged(false);
        this.previewImage.setVisible(false);

        this.previewFrame = new StackPane(previewImage);
        this.previewFrame.getStyleClass().add("evidence-preview-frame");
        this.previewFrame.setMinHeight(180);
        this.previewFrame.prefHeightProperty().bind(Bindings.max(180.0, previewFrame.widthProperty().multiply(0.46)));
        this.previewImage.fitWidthProperty().bind(Bindings.max(220.0, previewFrame.widthProperty().subtract(24)));
        this.previewImage.fitHeightProperty().bind(Bindings.max(160.0, previewFrame.widthProperty().multiply(0.58)));

        this.thumbPane = new FlowPane(10, 10);
        this.thumbPane.getStyleClass().add("evidence-thumb-pane");
        this.thumbPane.setPadding(new Insets(2, 0, 2, 0));
        this.thumbPane.prefWrapLengthProperty().bind(Bindings.max(240.0, widthProperty().subtract(12)));

        getStyleClass().add("evidence-gallery");
        setFillWidth(true);
        getChildren().addAll(previewHint, previewFrame, thumbPane);
        setImages(List.of(), emptyText);
    }

    public void setImages(List<String> storedPaths, String emptyText) {
        this.emptyText = emptyText;
        thumbPane.getChildren().clear();
        thumbShells.clear();
        clearPreview();
        int index = 0;
        if (storedPaths != null) {
            for (String storedPath : storedPaths) {
                Path imagePath = resolvePath(storedPath);
                if (imagePath == null || !Files.exists(imagePath)) {
                    continue;
                }
                Image image = new Image(imagePath.toUri().toString(), 96, 72, true, true);
                ImageView thumb = new ImageView(image);
                thumb.setFitWidth(96);
                thumb.setFitHeight(72);
                thumb.setPreserveRatio(true);

                Label tag = new Label("\u56fe" + (index + 1));
                tag.getStyleClass().add("evidence-thumb-index");

                VBox card = new VBox(6, thumb, tag);
                card.setAlignment(Pos.CENTER);

                StackPane shell = new StackPane(card);
                shell.getStyleClass().add("evidence-thumb-shell");
                thumbShells.add(shell);
                thumbPane.getChildren().add(shell);

                String fileLabel = fileLabel(imagePath, index);
                shell.setOnMouseClicked(event -> showPreview(shell, image, fileLabel));
                if (index == 0) {
                    showPreview(shell, image, fileLabel);
                }
                index++;
            }
        }
        if (index == 0) {
            thumbPane.getChildren().add(emptyLabel(this.emptyText));
        }
    }

    private void clearPreview() {
        previewImage.setImage(null);
        previewImage.setManaged(false);
        previewImage.setVisible(false);
        previewHint.setText(emptyText);
        thumbShells.forEach(node -> node.getStyleClass().remove("evidence-thumb-shell-active"));
    }

    private void showPreview(StackPane shell, Image image, String fileName) {
        previewImage.setImage(image);
        previewImage.setManaged(true);
        previewImage.setVisible(true);
        previewHint.setText(fileName);
        thumbShells.forEach(node -> node.getStyleClass().remove("evidence-thumb-shell-active"));
        shell.getStyleClass().add("evidence-thumb-shell-active");
    }

    private Label emptyLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("helper-text");
        label.setWrapText(true);
        return label;
    }

    private String fileLabel(Path path, int index) {
        String fileName = path.getFileName() == null ? null : path.getFileName().toString();
        return fileName == null || fileName.isBlank() ? "\u5df2\u52a0\u8f7d\u56fe\u7247" + (index + 1) : fileName;
    }

    private Path resolvePath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }
        Path path = Path.of(storedPath);
        return path.isAbsolute() ? path : Path.of("").toAbsolutePath().resolve(path).normalize();
    }
}
