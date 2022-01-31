package com.amber.armtp;

import android.app.Activity;
import android.app.AlertDialog;

import java.lang.annotation.Target;

public class ProgressBarLoading {
    private final Activity activity;

    private AlertDialog dialog;

    public ProgressBarLoading(Activity activity) {
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
        activity.runOnUiThread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            dialog.show();
        });
    }

    public void stop() {
        activity.runOnUiThread(() -> dialog.dismiss());
    }
}
