package com.scau.dormrepair.ui.component;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

public class PasswordInputControl extends StackPane {

    private final PasswordField hiddenField;
    private final TextField visibleField;
    private final Button toggleButton;
    private final Group eyeOpenIcon;
    private final Group eyeClosedIcon;
    private boolean syncing;

    public PasswordInputControl() {
        hiddenField = new PasswordField();
        visibleField = new TextField();
        toggleButton = new Button();
        eyeOpenIcon = createEyeIcon(false);
        eyeClosedIcon = createEyeIcon(true);

        hiddenField.getStyleClass().addAll("text-field", "login-input", "login-password-field");
        visibleField.getStyleClass().addAll("text-field", "login-input", "login-password-field");
        toggleButton.getStyleClass().add("login-password-toggle");
        toggleButton.setGraphic(eyeOpenIcon);
        toggleButton.setFocusTraversable(false);
        toggleButton.setMnemonicParsing(false);

        visibleField.setVisible(false);
        visibleField.setManaged(false);

        ChangeListener<String> syncListener =
                (observable, oldValue, newValue) -> syncValues(observable == hiddenField.textProperty());
        hiddenField.textProperty().addListener(syncListener);
        visibleField.textProperty().addListener(syncListener);

        toggleButton.setOnAction(event -> toggleVisible());

        StackPane fieldStack = new StackPane(hiddenField, visibleField);
        getChildren().addAll(fieldStack, toggleButton);
        setAlignment(Pos.CENTER_LEFT);
        StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(toggleButton, new Insets(0, 14, 0, 0));
    }

    public void setPromptText(String promptText) {
        hiddenField.setPromptText(promptText);
        visibleField.setPromptText(promptText);
    }

    public String getText() {
        return hiddenField.getText();
    }

    public void setText(String text) {
        String safeText = text == null ? "" : text;
        hiddenField.setText(safeText);
        visibleField.setText(safeText);
    }

    public void clear() {
        setText("");
    }

    public void requestInputFocus() {
        activeField().requestFocus();
    }

    public void setOnAction(EventHandler<ActionEvent> handler) {
        hiddenField.setOnAction(handler);
        visibleField.setOnAction(handler);
    }

    public TextInputControl activeField() {
        return visibleField.isVisible() ? visibleField : hiddenField;
    }

    public StringProperty textProperty() {
        return hiddenField.textProperty();
    }

    private void toggleVisible() {
        boolean nextVisible = !visibleField.isVisible();
        visibleField.setVisible(nextVisible);
        visibleField.setManaged(nextVisible);
        hiddenField.setVisible(!nextVisible);
        hiddenField.setManaged(!nextVisible);
        toggleButton.setGraphic(nextVisible ? eyeClosedIcon : eyeOpenIcon);
        TextInputControl targetField = activeField();
        String currentText = targetField.getText();
        Platform.runLater(() -> {
            targetField.requestFocus();
            int caretPosition = currentText == null ? 0 : currentText.length();
            targetField.positionCaret(caretPosition);
            targetField.deselect();
        });
    }

    private void syncValues(boolean fromHiddenField) {
        if (syncing) {
            return;
        }
        syncing = true;
        if (fromHiddenField) {
            visibleField.setText(hiddenField.getText());
        } else {
            hiddenField.setText(visibleField.getText());
        }
        syncing = false;
    }

    private Group createEyeIcon(boolean crossed) {
        Ellipse outline = new Ellipse(0, 0, 9.5, 6.5);
        outline.setFill(Color.TRANSPARENT);
        outline.setStroke(Color.web("#5f746d"));
        outline.setStrokeWidth(1.5);

        Circle pupil = new Circle(0, 0, 2.2);
        pupil.setFill(Color.web("#5f746d"));

        Group icon = new Group(outline, pupil);
        if (crossed) {
            Line strike = new Line(-8, 7, 8, -7);
            strike.setStroke(Color.web("#5f746d"));
            strike.setStrokeWidth(1.7);
            strike.setStrokeLineCap(StrokeLineCap.ROUND);
            icon.getChildren().add(strike);
        }
        return icon;
    }
}
