package com.amber.armtp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.FragmentActivity;

public class ProgressBarLoading {
    private final FragmentActivity activity;
    private final Context context;

    private AlertDialog dialog;

    public ProgressBarLoading(FragmentActivity activity, Context context) {
        this.activity = activity;
        this.context = context;
        _create();
    }

    private void _create() {
        activity.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
