package com.keyboardhtml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

/**
 * Release builds must be code-shrunk (R8): the whole Gson library would
 * otherwise be dexed although only the tree-model parser is used. The
 * critical counterpart: the JS bridge methods marked
 * {@code @JavascriptInterface} must be kept, or R8 strips them and the
 * web keyboard breaks silently at runtime.
 */
public class ReleaseBuildShrinksCodeTest {

    @Test
    public void releaseBuildIsMinified() throws IOException {
        String build = ProjectFiles.readProjectFile("app/build.gradle");
        String releaseBlock = block(build, "release {", "}");
        assertTrue("release must enable R8 minification (minifyEnabled true)",
                releaseBlock.contains("minifyEnabled true"));
        assertTrue("release must shrink unused resources (shrinkResources true)",
                releaseBlock.contains("shrinkResources true"));
        int debugAt = build.indexOf("debug {");
        if (debugAt >= 0) {
            assertFalse("debug must stay unminified for fast iteration",
                    build.substring(debugAt).contains("minifyEnabled true"));
        }
    }

    @Test
    public void proguardRulesKeepTheJavascriptBridge() throws IOException {
        String rules = ProjectFiles.readProjectFile("app/proguard-rules.pro");
        assertTrue("bridge methods must survive R8 (JavascriptInterface keep)",
                rules.contains("@android.webkit.JavascriptInterface <methods>"));
    }

    /** Small brace matcher: text from the marker line until the first '}'. */
    private static String block(String text, String start, String end) {
        int from = text.indexOf(start);
        assertTrue("missing block: " + start, from >= 0);
        int to = text.indexOf(end, from + start.length());
        assertTrue("unterminated block: " + start, to >= 0);
        return text.substring(from, to);
    }
}
