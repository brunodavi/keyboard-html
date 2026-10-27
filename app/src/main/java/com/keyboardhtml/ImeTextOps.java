package com.keyboardhtml;

import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

/** TextOps implementation backed by the current IME input connection. */
final class ImeTextOps implements TextOps {

    private final InputConnection connection;

    ImeTextOps(InputConnection connection) {
        this.connection = connection;
    }

    @Override
    public void commitText(String text) {
        connection.commitText(text, 1);
    }

    @Override
    public void deleteBackward() {
        if (DeleteResolver.resolve(connection.getSelectedText(0)) == DeleteResolver.Outcome.CLEAR_SELECTION) {
            connection.commitText("", 1);
        } else {
            connection.deleteSurroundingText(1, 0);
        }
    }

    @Override
    public void enter() {
        connection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        connection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
    }
}
