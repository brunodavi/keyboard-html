package com.keyboardhtml;

import android.webkit.JavascriptInterface;

/**
 * JS bridge exposed to the HTML keyboard layer as window.KeyboardBridge.
 * The provider resolves the current input connection at call time so the
 * bridge survives focus changes between fields.
 */
class KeyboardBridge {

    interface TextOpsProvider {
        TextOps get();
    }

    private final TextOpsProvider opsProvider;

    KeyboardBridge(TextOpsProvider opsProvider) {
        this.opsProvider = opsProvider;
    }

    @JavascriptInterface
    public void onKey(String key) {
        TextOps ops = opsProvider.get();
        if (ops != null) {
            KeyAction.parse(key).apply(ops);
        }
    }
}
