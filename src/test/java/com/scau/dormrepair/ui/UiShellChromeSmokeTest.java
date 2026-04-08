package com.scau.dormrepair.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class UiShellChromeSmokeTest {

    @Test
    void shouldKeepHeaderContractReadableInSource() throws IOException {
        String source = readSource("src/main/java/com/scau/dormrepair/ui/AppShell.java");

        assertTrue(source.contains("\\u9000\\u51fa\\u767b\\u5f55"));
        assertTrue(source.contains("new HBox(12, identityChipLabel, profileButton, logoutButton)"));
        assertTrue(source.contains("UiMotion.installSmoothScrollPane(moduleScrollPane);"));
        assertTrue(source.contains("resolveDialogWidth(owner, 440, 340)"));
        assertFalse(source.contains("moduleSummaryLabel"));
        assertFalse(source.contains("header-module-summary"));
        assertFalse(source.contains("body.setPrefWidth(440);"));
    }

    @Test
    void shouldKeepLoginFeedbackLayoutStableInSource() throws IOException {
        String source = readSource("src/main/java/com/scau/dormrepair/ui/LoginView.java");

        assertTrue(source.contains("formBody.getStyleClass().add(\"login-form-body\");"));
        assertTrue(source.contains("errorLabel.getStyleClass().add(\"login-error-label\");"));
        assertTrue(source.contains("errorLabel.setWrapText(true);"));
        assertTrue(source.contains("forgotPasswordRow.setOpacity(showForgotPassword ? 1 : 0);"));
        assertTrue(source.contains("label.setWrapText(true);"));
        assertTrue(source.contains("label.setOpacity(0);"));
        assertTrue(source.contains("errorLabel.setText(\" \");"));
        assertFalse(source.contains("formBody.setMinHeight(366);"));
        assertFalse(source.contains("errorLabel.setMinHeight(46);"));
    }

    @Test
    void shouldKeepSingleAuthoritativeShellSelectorsInCss() throws IOException {
        String css = readSource("src/main/resources/styles/app.css");

        assertEquals(1, countSelector(css, ".header-summary-inline"));
        assertEquals(1, countSelector(css, ".header-profile-action"));
        assertEquals(1, countSelector(css, ".header-profile-action:hover"));
        assertEquals(1, countSelector(css, ".header-logout-action"));
        assertEquals(1, countSelector(css, ".header-logout-action:hover"));
        assertEquals(1, countSelector(css, ".login-shell"));
        assertEquals(1, countSelector(css, ".login-hero-panel"));
        assertEquals(1, countSelector(css, ".login-card-title"));
        assertEquals(1, countSelector(css, ".login-form-body"));
        assertEquals(1, countSelector(css, ".login-footer-row"));
        assertEquals(1, countSelector(css, ".login-status-label"));
        assertEquals(1, countSelector(css, ".login-error-label"));
        assertEquals(1, countSelector(css, ".sidebar"));
        assertEquals(1, countSelector(css, ".nav-button"));
        assertEquals(1, countSelector(css, ".nav-button-active"));
        assertFalse(css.contains("-fx-min-height: 820px;"));
        assertFalse(selectorBlock(css, ".dialog-shell").contains("-fx-cursor: hand;"));
    }

    @Test
    void shouldKeepDialogsResponsiveInSource() throws IOException {
        String source = readSource("src/main/java/com/scau/dormrepair/ui/support/UiAlerts.java");

        assertTrue(source.contains("resolveDialogWidth(owner, 420, 320)"));
        assertTrue(source.contains("resolveDialogWidth(owner, 380, 300)"));
        assertFalse(source.contains("body.setPrefWidth(420);"));
        assertFalse(source.contains("body.setPrefWidth(380);"));
    }

    private static int countSelector(String css, String selector) {
        Pattern pattern = Pattern.compile("(?m)^" + Pattern.quote(selector) + "\\s*\\{");
        Matcher matcher = pattern.matcher(css);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static String selectorBlock(String css, String selector) {
        Pattern pattern = Pattern.compile("(?ms)^" + Pattern.quote(selector) + "\\s*\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(css);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String readSource(String relativePath) throws IOException {
        return Files.readString(Path.of(relativePath), StandardCharsets.UTF_8);
    }
}
