package com.keyboardhtml;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Single configuration screen: hardware-free shortcuts to enable and
 * select the keyboard, a text field to try it, and status feedback.
 */
public class MainActivity extends Activity {

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildContentView());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private View buildContentView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int margin = dp(16);
        root.setPadding(margin, margin, margin, margin);

        TextView title = new TextView(this);
        title.setText(R.string.app_name);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        root.addView(title, rowParams(margin));

        TextView intro = new TextView(this);
        intro.setText(getText(R.string.setup_intro));
        intro.setTextSize(14);
        root.addView(intro, rowParams(margin));

        status = new TextView(this);
        status.setText(getText(R.string.status_checking));
        status.setTextSize(14);
        root.addView(status, rowParams(margin));

        Button enable = new Button(this);
        enable.setText(R.string.action_enable);
        enable.setOnClickListener(v -> startActivity(new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS)));
        root.addView(enable, rowParams(margin));

        Button select = new Button(this);
        select.setText(R.string.action_select);
        select.setOnClickListener(v -> inputMethodManager().showInputMethodPicker());
        root.addView(select, rowParams(margin));

        TextView testLabel = new TextView(this);
        testLabel.setText(getText(R.string.test_field_label));
        root.addView(testLabel, rowParams(0));

        EditText testField = new EditText(this);
        testField.setHint(getText(R.string.test_field_hint));
        root.addView(testField);

        TextView footer = new TextView(this);
        footer.setText(getText(R.string.zip_hint));
        footer.setTextSize(13);
        footer.setMovementMethod(LinkMovementMethod.getInstance());
        root.addView(footer, rowParams(margin));

        return root;
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
        String current = android.provider.Settings.Secure.getString(
                getContentResolver(), android.provider.Settings.Secure.DEFAULT_INPUT_METHOD);
        return current != null && current.contains(getPackageName() + "/");
    }

    private InputMethodManager inputMethodManager() {
        return (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    private LinearLayout.LayoutParams rowParams(int verticalMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, verticalMargin, 0, verticalMargin);
        return params;
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
