package com.keyboardhtml;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Optional metadata file of a keyboard package (manifest.json). The MVP
 * reads a single field: the display name. Parsed with Gson (pure Java),
 * which unlike Android's org.json works in the unit test environment.
 */
final class KeyboardManifest {

    private final String name;

    private KeyboardManifest(String name) {
        this.name = name;
    }

    /** Display name declared in the manifest, or null when absent/invalid. */
    String name() {
        return name;
    }

    static KeyboardManifest parse(String json) {
        return new KeyboardManifest(readName(json));
    }

    /** The "name" string value of the manifest object, or null when absent/invalid. */
    private static String readName(String json) {
        if (json == null) {
            return null;
        }
        try {
            JsonElement root = JsonParser.parseString(json);
            if (!root.isJsonObject()) {
                return null;
            }
            JsonElement name = root.getAsJsonObject().get("name");
            if (name == null || !name.isJsonPrimitive() || !name.getAsJsonPrimitive().isString()) {
                return null;
            }
            String value = name.getAsString();
            return value.isEmpty() ? null : value;
        } catch (RuntimeException malformed) {
            return null;
        }
    }
}
