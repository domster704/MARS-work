package com.amber.armtp;

import android.app.AlertDialog;
import android.support.v4.app.FragmentActivity;

public class ProgressBarLoading {
    private final FragmentActivity activity;

    private AlertDialog dialog;

    public ProgressBarLoading(FragmentActivity activity) {
        this.activity = activity;
        _create();
    }

    private void _create() {
        activity.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setCancelable(false);
            builder.setView(R.layout.progress_bar_loading);
            dialog = builder.create();
        });
    }

    public void show() {
        activity.runOnUiThread(() -> dialog.show());
    }

    public void dismiss() {
        activity.runOnUiThread(() -> dialog.dismiss());
    }
}
