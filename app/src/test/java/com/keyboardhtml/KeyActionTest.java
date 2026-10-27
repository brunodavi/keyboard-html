package com.keyboardhtml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/** TDD for the JS bridge key protocol: a key string from the HTML layer maps to exactly one IME operation. */
public class KeyActionTest {

    @Test
    public void singleCharacterCommitsItsText() {
        assertEquals(KeyAction.Kind.COMMIT, KeyAction.parse("a").kind());
        assertEquals("a", KeyAction.parse("a").text());
    }

    @Test
    public void spaceCommitsASingleSpaceCharacter() {
        assertEquals(KeyAction.Kind.COMMIT, KeyAction.parse("space").kind());
        assertEquals(" ", KeyAction.parse("space").text());
    }

    @Test
    public void enterTriggersTheEnterOperation() {
        assertEquals(KeyAction.Kind.ENTER, KeyAction.parse("enter").kind());
    }

    @Test
    public void backspaceTriggersTheDeleteOperation() {
        assertEquals(KeyAction.Kind.DELETE, KeyAction.parse("backspace").kind());
    }

    @Test
    public void actionNamesAreCaseInsensitive() {
        assertEquals(KeyAction.Kind.ENTER, KeyAction.parse("Enter").kind());
        assertEquals(KeyAction.Kind.DELETE, KeyAction.parse("BACKSPACE").kind());
    }

    @Test
    public void nullKeyIsIgnored() {
        assertEquals(KeyAction.Kind.NOOP, KeyAction.parse(null).kind());
    }

    @Test
    public void emptyKeyIsIgnored() {
        assertEquals(KeyAction.Kind.NOOP, KeyAction.parse("").kind());
        assertEquals(KeyAction.Kind.NOOP, KeyAction.parse("   ").kind());
    }

    @Test
    public void overlyLongInputIsRejected() {
        StringBuilder longKey = new StringBuilder();
        for (int i = 0; i < 33; i++) {
            longKey.append('x');
        }
        assertEquals(KeyAction.Kind.NOOP, KeyAction.parse(longKey.toString()).kind());
        assertEquals(KeyAction.Kind.COMMIT, KeyAction.parse("abcdefghijklmno").kind());
    }

    @Test
    public void applyingCommitSendsTextToTheKeyboardTarget() {
        RecordingTextOps ops = new RecordingTextOps();
        KeyAction.parse("h").apply(ops);
        assertEquals(TextOps.Op.COMMIT, ops.seen.get(0).kind);
        assertEquals("h", ops.seen.get(0).text);
        assertEquals(1, ops.seen.size());
    }

    @Test
    public void applyingDeleteSendsDeleteToTheKeyboardTarget() {
        RecordingTextOps ops = new RecordingTextOps();
        KeyAction.parse("backspace").apply(ops);
        assertEquals(1, ops.seen.size());
        assertEquals(TextOps.Op.DELETE, ops.seen.get(0).kind);
    }

    @Test
    public void applyingEnterSendsEnterToTheKeyboardTarget() {
        RecordingTextOps ops = new RecordingTextOps();
        KeyAction.parse("enter").apply(ops);
        assertEquals(1, ops.seen.size());
        assertEquals(TextOps.Op.ENTER, ops.seen.get(0).kind);
    }

    @Test
    public void applyingNoopSendsNothingToTheKeyboardTarget() {
        RecordingTextOps ops = new RecordingTextOps();
        KeyAction.parse(null).apply(ops);
        assertTrue(ops.seen.isEmpty());
    }

    /** Recording fake for TextOps used to verify which operations were dispatched. */
    private static final class RecordingTextOps implements TextOps {
        final List<Call> seen = new ArrayList<>();

        @Override
        public void commitText(String text) {
            seen.add(new Call(TextOps.Op.COMMIT, text));
        }

        @Override
        public void deleteBackward() {
            seen.add(new Call(TextOps.Op.DELETE, null));
        }

        @Override
        public void enter() {
            seen.add(new Call(TextOps.Op.ENTER, null));
        }

        private static final class Call {
            final TextOps.Op kind;
            final String text;

            Call(TextOps.Op kind, String text) {
                this.kind = kind;
                this.text = text;
            }
        }
    }
}
