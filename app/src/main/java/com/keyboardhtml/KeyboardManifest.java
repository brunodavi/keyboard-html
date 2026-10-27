package com.keyboardhtml;

/**
 * Optional metadata file of a keyboard package (manifest.json). The MVP
 * reads a single field: the display name. Parsed with a tiny literal
 * scanner so it is unit testable without Android JSON classes.
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

    /** Scans a flat json object for the string value of the "name" key. */
    private static String readName(String json) {
        if (json == null) {
            return null;
        }
        Scanner scan = new Scanner(json);
        // each iteration moves the cursor at least one character, so this
        // bound is a defense-in-depth guarantee against parser hangs
        for (int step = 0; scan.hasLiteral() && step <= json.length(); step++) {
            String literal = scan.nextLiteral();
            if (scan.nextAfter(':')) {
                String value = scan.nextLiteral();
                if ("name".equals(literal)) {
                    return value == null || value.isEmpty() ? null : value;
                }
            }
        }
        return null;
    }

    /** Char-walking scanner over json string literals, forgiving to whitespace. */
    static final class Scanner {

        private final String json;
        private int pos;

        Scanner(String json) {
            this.json = json;
        }

        boolean hasLiteral() {
            skip();
            return pos < json.length();
        }

        /** Next quoted literal; skips over any non-quote character and returns null. */
        String nextLiteral() {
            skip();
            if (pos >= json.length()) {
                return null;
            }
            if (json.charAt(pos) != '"') {
                pos++;
                return null;
            }
            pos++;
            StringBuilder out = new StringBuilder();
            while (pos < json.length()) {
                char c = json.charAt(pos++);
                if (c == '\\' && pos < json.length()) {
                    out.append(c);
                    out.append(json.charAt(pos++));
                } else if (c == '"') {
                    return out.toString();
                } else {
                    out.append(c);
                }
            }
            return null;
        }

        /** Skips whitespace and expects the given character; composed helper below. */
        boolean nextAfter(char c) {
            skip();
            if (pos < json.length() && json.charAt(pos) == c) {
                pos++;
                skip();
                return true;
            }
            return false;
        }

        private void skip() {
            while (pos < json.length()
                    && (json.charAt(pos) == ' ' || json.charAt(pos) == '\t'
                        || json.charAt(pos) == '\n' || json.charAt(pos) == '\r')) {
                pos++;
            }
        }
    }
}
