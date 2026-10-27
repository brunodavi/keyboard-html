package com.keyboardhtml;

import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * The keyboard itself: an InputMethodService whose input view is a WebView
 * rendering an HTML keyboard layout. Key events travel from the web layer
 * through the JS bridge back into the current input connection.
 */
public class KeyboardService extends InputMethodService {

    private static final String DEFAULT_LAYOUT_URL =
            "file:///android_asset/keyboard/default/index.html";

    private WebView keyboardView;

    @Override
    public View onCreateInputView() {
        keyboardView = new WebView(this);
        WebSettings settings = keyboardView.getSettings();
        settings.setJavaScriptEnabled(true);
        keyboardView.addJavascriptInterface(
                new KeyboardBridge(this::currentTextOps), "KeyboardBridge");
        keyboardView.setBackgroundColor(Color.TRANSPARENT);
        keyboardView.loadUrl(DEFAULT_LAYOUT_URL);
        return keyboardView;
    }

    /** Fullscreen extract mode off: the WebView keyboard must stay visible over fields. */
    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    private TextOps currentTextOps() {
        InputConnection connection = getCurrentInputConnection();
        return connection == null ? null : new ImeTextOps(connection);
    }
}
