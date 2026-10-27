package com.keyboardhtml;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * TDD for the backspace rule: a selection is cleared in one step, cursor
 * deletes one character to the left.
 */
public class DeleteResolverTest {

    @Test
    public void noSelectionDeletesOneCharacter() {
        assertEquals(DeleteResolver.Outcome.DELETE_CHAR, DeleteResolver.resolve(null));
        assertEquals(DeleteResolver.Outcome.DELETE_CHAR, DeleteResolver.resolve(""));
    }

    @Test
    public void anySelectionIsClearedInOneStep() {
        assertEquals(DeleteResolver.Outcome.CLEAR_SELECTION, DeleteResolver.resolve("a"));
        assertEquals(DeleteResolver.Outcome.CLEAR_SELECTION, DeleteResolver.resolve("several words"));
    }
}
