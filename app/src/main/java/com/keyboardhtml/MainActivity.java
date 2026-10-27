package com.keyboardhtml;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Single configuration screen (XML layout): shortcuts to enable and
 * select the keyboard, a text field to try it, layout import, status.
 */
public class MainActivity extends Activity {

    private static final int IMPORT_ZIP_REQUEST = 1001;

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.status_text);
        findViewById(R.id.btn_enable)
                .setOnClickListener(v -> startActivity(
                        new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        findViewById(R.id.btn_select)
                .setOnClickListener(v -> inputMethodManager().showInputMethodPicker());
        findViewById(R.id.btn_import)
                .setOnClickListener(v -> launchZipImport());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void refreshStatus() {
        status.setText(getText(isThisKeyboardEnabled() && isThisKeyboardSelected()
                ? R.string.status_selected
                : isThisKeyboardEnabled()
                        ? R.string.status_enabled
                        : R.string.status_disabled));
    }

    private boolean isThisKeyboardEnabled() {
        for (InputMethodInfo info : inputMethodManager().getEnabledInputMethodList()) {
            if (getPackageName().equals(info.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isThisKeyboardSelected() {
        String current = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        return current != null && current.contains(getPackageName() + "/");
    }

    private InputMethodManager inputMethodManager() {
        return (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    // -------------------------------------------------------------- zip import

    private void launchZipImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        startActivityForResult(intent, IMPORT_ZIP_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != IMPORT_ZIP_REQUEST || resultCode != RESULT_OK || data == null) {
            return;
        }
        try {
            String imported = importLayout(data.getData());
            refreshStatus();
            Toast.makeText(this, getString(R.string.imported_toast, imported), Toast.LENGTH_LONG).show();
        } catch (IOException | ZipLayoutExtractor.InvalidLayout e) {
            Toast.makeText(this,
                    getString(R.string.import_failed_toast, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    /** Copies the picked document into cache, then extracts it into layouts/. */
    private String importLayout(Uri source) throws IOException {
        File cache = new File(getCacheDir(), "import.zip");
        copyTo(source, cache);
        return ZipLayoutExtractor.extract(cache, layoutsRoot()).layoutId();
    }

    private File layoutsRoot() {
        return new File(getFilesDir(), "layouts");
    }

    private void copyTo(Uri source, File target) throws IOException {
        try (InputStream in = getContentResolver().openInputStream(source);
             OutputStream out = new FileOutputStream(target)) {
            if (in == null) {
                throw new IOException("cannot open the selected zip");
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
}
