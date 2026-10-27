package com.keyboardhtml;

/**
 * A single key event coming from the HTML keyboard layer, mapped to one
 * IME operation. Parsing is kept pure so the JS bridge protocol is unit
 * tested without Android.
 */
public final class KeyAction {

    enum Kind {
        COMMIT,
        DELETE,
        ENTER,
        NOOP
    }

    /** Longest accepted text payload from the web layer. */
    private static final int MAX_TEXT_LENGTH = 32;

    private static final KeyAction NOOP = new KeyAction(Kind.NOOP, "");
    private static final KeyAction DELETE = new KeyAction(Kind.DELETE, "");
    private static final KeyAction ENTER = new KeyAction(Kind.ENTER, "");

    private final Kind kind;
    private final String text;

    private KeyAction(Kind kind, String text) {
        this.kind = kind;
        this.text = text;
    }

    /** Maps a key string from the web layer to an action; unknown input becomes NOOP. */
    public static KeyAction parse(String key) {
        if (key == null) {
            return NOOP;
        }
        String trimmed = key.trim();
        if (trimmed.isEmpty()) {
            return NOOP;
        }
        String normalized = trimmed.toLowerCase();
        switch (normalized) {
            case "backspace":
                return DELETE;
            case "enter":
                return ENTER;
            case "space":
                return new KeyAction(Kind.COMMIT, " ");
            default:
        }
        if (trimmed.length() > MAX_TEXT_LENGTH) {
            return NOOP;
        }
        return new KeyAction(Kind.COMMIT, trimmed);
    }

    /** Dispatches this action to the keyboard target; NOOP sends nothing. */
    public void apply(TextOps ops) {
        if (kind == Kind.NOOP || ops == null) {
            return;
        }
        switch (kind) {
            case COMMIT:
                ops.commitText(text);
                break;
            case DELETE:
                ops.deleteBackward();
                break;
            case ENTER:
                ops.enter();
                break;
            default:
        }
    }

    Kind kind() {
        return kind;
    }

    String text() {
        return text;
    }
}
