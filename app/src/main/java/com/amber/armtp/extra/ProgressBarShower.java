package com.amber.armtp.extra;

import android.content.Context;

import com.amber.armtp.ProgressBarLoading;

import java.util.concurrent.Callable;

public class ProgressBarShower {
    private boolean isCancelled = false;
    private Callable function;
    private ProgressBarLoading progressBarLoading;
    private final Context context;

    public ProgressBarShower(Context context, boolean isCancelled) {
        this.context = context;
        this.isCancelled = isCancelled;
    }

    public ProgressBarShower(Context context) {
        this.context = context;
    }

    public ProgressBarShower setFunction(Callable function) {
        this.function = function;
        return this;
    }

    public void start() {
        try {
            if (isCancelled) {
                progressBarLoading = new ProgressBarLoading(context, true);
            } else {
                progressBarLoading = new ProgressBarLoading(context);
            }

            progressBarLoading.show();
            function.call();
            progressBarLoading.dismiss();
        } catch (Exception e) {
            if (progressBarLoading != null) {
                progressBarLoading.dismiss();
            }
            e.printStackTrace();
            Config.sout(e, context);
        }
    }

    public ProgressBarLoading getProgressBarLoading() {
        return progressBarLoading;
    }
}
