package com.keyboardhtml;

/**
 * Target of IME text operations delivered by the JS bridge. Implemented
 * on the Android side on top of InputConnection; faked in unit tests.
 */
public interface TextOps {

    enum Op {
        COMMIT,
        DELETE,
        ENTER
    }

    void commitText(String text);

    void deleteBackward();

    void enter();
}
