package com.keyboardhtml;

import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
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

    @Override
    public View onCreateInputView() {
        keyboardView = new WebView(this);
        WebSettings settings = keyboardView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        keyboardView.addJavascriptInterface(
                new KeyboardBridge(this::currentTextOps), "KeyboardBridge");
        keyboardView.setBackgroundColor(Color.TRANSPARENT);
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
        LayoutRepository repository = new LayoutRepository(layoutsRoot());
        String id = repository.currentLayoutId();
        if (id != null) {
            File file = new File(layoutsRoot(), id + "/index.html");
            if (file.isFile()) {
                return Uri.fromFile(file).toString();
            }
        }
        return DEFAULT_LAYOUT_URL;
    }

    private File layoutsRoot() {
        return new File(getFilesDir(), "layouts");
    }

    private TextOps currentTextOps() {
        InputConnection connection = getCurrentInputConnection();
        return connection == null ? null : new ImeTextOps(connection);
    }
}
