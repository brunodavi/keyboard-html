package com.keyboardhtml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Spec of the built-in dark QWERTY keyboard asset (Gboard-style): three
 * sheets — letters, symbols 1/2 and symbols 2/2 — with the classic key
 * arrangement and no extra toolbars. Special keys are handled locally by
 * the web layer (data-ui) and must never travel through the JS bridge as
 * "text" (data-key), otherwise e.g. "shift" would be committed literally.
 */
public class DefaultKeyboardAssetTest {

    private static final String ASSET =
            "app/src/main/assets/keyboard/default/index.html";

    /** data-key on a static <button> tag. */
    private static final Pattern BUTTON_KEY =
            Pattern.compile("<button[^>]*?data-key=\"([^\"]*)\"");
    /** data-ui on a static <button> tag. */
    private static final Pattern BUTTON_UI =
            Pattern.compile("<button[^>]*?data-ui=\"([^\"]*)\"");

    private static String asset() throws IOException {
        return ProjectFiles.readProjectFile(ASSET);
    }

    private static Set<String> values(String html, Pattern pattern) {
        Set<String> found = new HashSet<>();
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            found.add(decodeEntities(matcher.group(1)));
        }
        return found;
    }

    /** Attribute escapes used by the asset (order matters). */
    private static String decodeEntities(String value) {
        return value.replace("&amp;", "&").replace("&lt;", "<")
                .replace("&gt;", ">").replace("&quot;", "\"");
    }

    private static int occurrences(String html, Pattern pattern) {
        Matcher matcher = pattern.matcher(html);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    @Test
    public void hasThreeSheets() throws IOException {
        String html = asset();
        assertTrue(html.contains("data-sheet=\"letters\""));
        assertTrue(html.contains("data-sheet=\"symbols-1\""));
        assertTrue(html.contains("data-sheet=\"symbols-2\""));
    }

    @Test
    public void lettersSheetHasTheClassicQwertyRows() throws IOException {
        Set<String> keys = values(asset(), BUTTON_KEY);
        String letters = "qwertyuiopasdfghjklzxcvbnm";
        for (char letter : letters.toCharArray()) {
            assertTrue("missing letter " + letter, keys.contains(String.valueOf(letter)));
        }
        assertFalse("nested letter rows collapse the middle letters (old bug)",
                asset().contains("data-keys="));
    }

    @Test
    public void lettersSheetIsAGrid() throws IOException {
        String html = asset();
        assertTrue("letters rows use a shared 20-column grid",
                html.contains("grid-template-columns: repeat(20, 1fr);"));
        assertTrue("letters span two columns",
                html.contains(".lg > button { grid-column: span 2; }"));
        assertTrue("shift/backspace span three columns",
                html.contains(".lg > button.fn.side { grid-column: span 3; }"));
    }

    @Test
    public void symbolsFirstPageHasTheExpectedKeys() throws IOException {
        Set<String> keys = values(asset(), BUTTON_KEY);
        String expected = "1234567890" + "!@#$%€&*()" + "-'\":;,?";
        for (char key : expected.toCharArray()) {
            assertTrue("missing symbol " + key, keys.contains(String.valueOf(key)));
        }
    }

    @Test
    public void symbolsSecondPageHasTheExpectedKeys() throws IOException {
        Set<String> keys = values(asset(), BUTTON_KEY);
        String expected = "+×÷=<>{}[]€£₩/~`•°❤_\\|¬¿¡";
        for (char key : expected.toCharArray()) {
            assertTrue("missing symbol " + key, keys.contains(String.valueOf(key)));
        }
    }

    @Test
    public void everySheetKeepsTheBottomActionRow() throws IOException {
        Set<String> keys = values(asset(), BUTTON_KEY);
        for (String action : new String[]{",", ".", "space", "backspace", "enter"}) {
            assertTrue("missing action key " + action, keys.contains(action));
        }
    }

    @Test
    public void specialKeysAreUiOnlyAndNeverBridgeText() throws IOException {
        Set<String> bridged = values(asset(), BUTTON_KEY);
        for (String special : new String[]{"shift", "!#1", "abc", "1/2", "2/2"}) {
            assertFalse("special key must not bridge: " + special, bridged.contains(special));
        }
        Set<String> ui = values(asset(), BUTTON_UI);
        assertTrue(ui.contains("shift"));
        assertTrue(ui.contains("symbols"));
        assertTrue(ui.contains("letters"));
        assertTrue(ui.contains("page-toggle"));
    }

    @Test
    public void everyButtonIsEitherABridgeKeyOrALocalUiControl() throws IOException {
        String html = asset();
        int buttons = occurrences(html, Pattern.compile("<button"));
        int wired = occurrences(html, BUTTON_KEY) + occurrences(html, BUTTON_UI);
        assertEquals("buttons without data-key/data-ui leak clicks", buttons, wired);
    }

    @Test
    public void everyBridgedKeyIsWithinTheBridgePayloadLimitOrAnAction() throws IOException {
        for (String key : values(asset(), BUTTON_KEY)) {
            boolean action = key.equals("space") || key.equals("backspace")
                    || key.equals("enter");
            assertTrue("data-key payload too long: " + key,
                    action || key.length() <= 32);
        }
    }

    @Test
    public void shiftAndBackspaceMatchTheLetterWidthOnTheLettersRow() throws IOException {
        String html = asset();
        assertTrue(html.matches("(?s).*<button class=\"fn side\" data-ui=\"shift\".*"));
        assertTrue(html.matches("(?s).*<button class=\"fn side\" data-key=\"backspace\".*"));
        assertFalse("old compact flex class must be gone (grid owns the sizes)",
                html.contains("button.compact"));
    }

    @Test
    public void shiftDoubleTapSimulatesCapsLock() throws IOException {
        assertTrue("caps lock state marker missing",
                asset().contains("shift-caps"));
    }

    @Test
    public void pageToggleLabelReflectsTheCurrentPage() throws IOException {
        String html = asset();
        assertTrue("symbols-1 shows 1/2",
                html.matches("(?s).*data-sheet=\"symbols-1\"(?:.(?!data-sheet))+?"
                        + "data-ui=\"page-toggle\">1/2.*"));
        assertTrue("symbols-2 shows 2/2",
                html.matches("(?s).*data-sheet=\"symbols-2\"(?:.(?!data-sheet))+?"
                        + "data-ui=\"page-toggle\">2/2.*"));
    }

    @Test
    public void theKeyboardNeverScrolls() throws IOException {
        String html = asset();
        assertTrue("page must lock overflow (no scrollbars)",
                html.matches("(?s).*html, body \\{[^}]*overflow: hidden;[^}]*overscroll-behavior: none;.*"));
        assertFalse("height 100% breaks the web view intrinsic sizing (invisible keyboard)",
                html.contains("height: 100%"));
    }
}
