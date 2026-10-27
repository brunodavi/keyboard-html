package com.keyboardhtml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * TDD for the optional keyboard manifest.json: the parser extracts the
 * display name with Gson, staying forgiving to formatting but strict
 * about manifest content.
 */
public class KeyboardManifestTest {

    @Test
    public void readsTheNameField() {
        assertEquals("Qwerty Dark", KeyboardManifest.parse("{\"name\": \"Qwerty Dark\"}").name());
    }

    @Test
    public void ignoresWhitespaceAndFieldOrder() {
        assertEquals("Mé Keeb", KeyboardManifest.parse("{\n  \"author\":\"b\",\n \"name\" :\"Mé Keeb\"\n}").name());
    }

    @Test
    public void keysNamedNameInStringValuesDoNotConfuseTheParser() {
        assertEquals("Real Name", KeyboardManifest.parse(
                "{\"author\":\"name\",\"name\":\"Real Name\"}").name());
    }

    @Test
    public void escapesDecodePerJson() {
        assertEquals("Tabs\tHere", KeyboardManifest.parse("{\"name\":\"Tabs\\tHere\"}").name());
        assertEquals("Café", KeyboardManifest.parse("{\"name\":\"Caf\\u00e9\"}").name());
    }

    @Test
    public void missingNameFallsBackToNull() {
        assertNull(KeyboardManifest.parse("{\"author\":\"b\"}").name());
    }

    @Test
    public void nonStringValueNameFallsBackToNull() {
        assertNull(KeyboardManifest.parse("{\"name\": 5}").name());
    }

    @Test
    public void blankOrMalformedManifestFallsBackToNull() {
        assertNull(KeyboardManifest.parse("").name());
        assertNull(KeyboardManifest.parse("not json").name());
        assertNull(KeyboardManifest.parse(null).name());
    }

    @Test
    public void emptyNameFallsBackToNull() {
        assertNull(KeyboardManifest.parse("{\"name\":\"\"}").name());
    }
}
