package com.amber.armtp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.amber.armtp.annotations.AsyncUI;

public class ProgressBarLoading {
    private final Context context;
    private AlertDialog dialog;

    public ProgressBarLoading(Context context) {
        this.context = context;
        create();
    }

    public ProgressBarLoading(Context context, boolean isCreatedCancelButton, Thread thread) {
        this.context = context;
        create(isCreatedCancelButton, thread);
    }

    @AsyncUI
    private void create() {
        dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(R.layout.progress_bar_loading)
                .create();
    }

    /**
     * @param isCanceled - параметр, указывающий на необходимость создания кнопки "Отменить"
     * @param thread     - поток, который необходимо прервать
     */
    @AsyncUI
    private void create(boolean isCanceled, Thread thread) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_bar_loading);

        dialog = builder.create();
        if (isCanceled) {
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Отменить", (dialogInterface, i) -> {
                thread.interrupt();
                dialog.dismiss();
            });
        }
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
