package com.amber.armtp;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.amber.armtp.annotations.AsyncUI;

public class ProgressBarLoading {
    private final Context context;
    private AlertDialog dialog;

    public ProgressBarLoading(Context context) {
        this.context = context;
        _create();
    }

    @AsyncUI
    private void _create() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_bar_loading);
        dialog = builder.create();
    }

    @AsyncUI
    public void show() {
        dialog.show();
    }

    @AsyncUI
    public void dismiss() {
        dialog.dismiss();
    }
}
