package com.keyboardhtml;

/**
 * Decides what one backspace means given the current text selection:
 * clearing a selection removes it entirely, otherwise one character to
 * the left goes away. Pure so the rule is unit tested without Android.
 */
final class DeleteResolver {

    enum Outcome {
        CLEAR_SELECTION,
        DELETE_CHAR
    }

    private DeleteResolver() {
    }

    static Outcome resolve(CharSequence selected) {
        return selected == null || selected.length() == 0
                ? Outcome.DELETE_CHAR
                : Outcome.CLEAR_SELECTION;
    }
}
