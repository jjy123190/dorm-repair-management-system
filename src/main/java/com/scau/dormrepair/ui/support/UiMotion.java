package com.scau.dormrepair.ui.support;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.util.Duration;

public final class UiMotion {

    private static final Duration COMBO_POPUP_DURATION = Duration.millis(180);
    private static final Interpolator COMBO_POPUP_INTERPOLATOR = Interpolator.SPLINE(0.2, 0.82, 0.2, 1.0);

    private UiMotion() {
    }

    public static <T> void installSmoothDropdown(ComboBox<T> comboBox) {
        comboBox.setSkin(new AnimatedComboBoxSkin<>(comboBox));
    }

    private static final class AnimatedComboBoxSkin<T> extends ComboBoxListViewSkin<T> {

        private Animation currentAnimation;

        private AnimatedComboBoxSkin(ComboBox<T> comboBox) {
            super(comboBox);
        }

        @Override
        public void show() {
            super.show();
            Platform.runLater(this::playEntranceAnimation);
        }

        @Override
        public void hide() {
            if (currentAnimation != null) {
                currentAnimation.stop();
            }
            resetPopupContent();
            super.hide();
        }

        private void playEntranceAnimation() {
            Node popupContent = getPopupContent();
            if (popupContent == null) {
                return;
            }
            if (currentAnimation != null) {
                currentAnimation.stop();
            }

            popupContent.setOpacity(0);
            popupContent.setTranslateY(10);
            popupContent.setScaleY(0.96);

            FadeTransition fade = new FadeTransition(COMBO_POPUP_DURATION, popupContent);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setInterpolator(COMBO_POPUP_INTERPOLATOR);

            TranslateTransition lift = new TranslateTransition(COMBO_POPUP_DURATION, popupContent);
            lift.setFromY(10);
            lift.setToY(0);
            lift.setInterpolator(COMBO_POPUP_INTERPOLATOR);

            ScaleTransition scale = new ScaleTransition(COMBO_POPUP_DURATION, popupContent);
            scale.setFromY(0.96);
            scale.setToY(1);
            scale.setInterpolator(COMBO_POPUP_INTERPOLATOR);

            currentAnimation = new ParallelTransition(fade, lift, scale);
            currentAnimation.setOnFinished(event -> resetPopupContent());
            currentAnimation.playFromStart();
        }

        private void resetPopupContent() {
            Node popupContent = getPopupContent();
            if (popupContent == null) {
                return;
            }
            popupContent.setOpacity(1);
            popupContent.setTranslateY(0);
            popupContent.setScaleY(1);
        }
    }
}
