package com.keyboardhtml;

import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.File;

/**
 * The keyboard itself: an InputMethodService whose input view is a WebView
 * rendering an HTML keyboard layout. Key events travel from the web layer
 * through the JS bridge back into the current input connection.
 */
public class KeyboardService extends InputMethodService {

    private static final String DEFAULT_LAYOUT_URL =
            "file:///android_asset/keyboard/default/index.html";

    private WebView keyboardView;
    private String loadedLayoutUrl;

    /** Sent after a layout change so an active keyboard picks it up at once. */
    public static final String ACTION_RELOAD_LAYOUT =
            "com.keyboardhtml.action.RELOAD_LAYOUT";

    @Override
    public View onCreateInputView() {
        keyboardView = new WebView(this);
        WebSettings settings = keyboardView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        keyboardView.addJavascriptInterface(
                new KeyboardBridge(this::currentTextOps), "KeyboardBridge");
        keyboardView.setBackgroundColor(Color.TRANSPARENT);
        // the keyboard page must never scroll: overflow and overscroll are
        // locked in CSS, and the web view scrollbars/glow are disabled here
        keyboardView.setVerticalScrollBarEnabled(false);
        keyboardView.setHorizontalScrollBarEnabled(false);
        keyboardView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        loadedLayoutUrl = null;
        return keyboardView;
    }

    /** Load the selected layout when the keyboard shows up on a field. */
    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        if (keyboardView != null) {
            loadCurrentLayout();
        }
    }

    /**
     * Layout picked in the config screen while this keyboard is active:
     * reload the web view right away (cache dropped, the page may also have
     * new content after re-importing the same zip name).
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_RELOAD_LAYOUT.equals(intent.getAction())
                && keyboardView != null) {
            loadedLayoutUrl = null;
            keyboardView.post(this::loadCurrentLayout);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /** Fullscreen extract mode off: the WebView keyboard must stay visible over fields. */
    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    private void loadCurrentLayout() {
        String layoutUrl = selectedLayoutUrl();
        if (!layoutUrl.equals(loadedLayoutUrl)) {
            loadedLayoutUrl = layoutUrl;
            keyboardView.loadUrl(layoutUrl);
        }
    }

    /** Selected installed layout (internal storage), or the bundled default. */
    private String selectedLayoutUrl() {
        String uri = new LayoutRepository(layoutsRoot()).currentLayoutUri();
        return uri == null ? DEFAULT_LAYOUT_URL : uri;
    }

    private File layoutsRoot() {
        return new File(getFilesDir(), "layouts");
    }

    private TextOps currentTextOps() {
        InputConnection connection = getCurrentInputConnection();
        return connection == null ? null : new ImeTextOps(connection);
    }
}
