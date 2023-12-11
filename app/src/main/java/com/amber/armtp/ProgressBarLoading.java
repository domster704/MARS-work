package com.amber.armtp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.widget.TextView;

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

    private void create() {
        new Handler(context.getMainLooper()).post(() -> {
            dialog = new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setView(R.layout.progress_bar_loading)
                    .create();
        });
    }

    /**
     * @param isCanceled - параметр, указывающий на необходимость создания кнопки "Отменить"
     * @param thread     - поток, который необходимо прервать
     */
    private void create(boolean isCanceled, Thread thread) {
        new Handler(context.getMainLooper()).post(() -> {
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
        });
    }

    public void changeText(String text) {
        new Handler(context.getMainLooper()).post(() -> {
            TextView tv = dialog.findViewById(R.id.pgBarText);
            tv.setText(text);
        });
    }

    public void show() {
        new Handler(context.getMainLooper()).post(() -> {
            dialog.show();
        });
    }

    public void dismiss() {
        new Handler(context.getMainLooper()).post(() -> {
            dialog.dismiss();
        });
    }
}
