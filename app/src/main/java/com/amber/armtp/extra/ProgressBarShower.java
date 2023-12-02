package com.amber.armtp.extra;

import com.amber.armtp.Config;
import com.amber.armtp.GlobalVars;
import com.amber.armtp.ProgressBarLoading;

public class ProgressBarShower {
    private boolean isCancelled = false;
    private Runnable function;

    public ProgressBarShower(Runnable function, boolean isCancelled) {
        this.isCancelled = isCancelled;
        this.function = function;
    }

    public ProgressBarShower(Runnable function) {
        this.isCancelled = isCancelled;
        this.function = function;
    }

    public void start() {
        ProgressBarLoading progressBarLoading = null;
        try {
//            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//            PGShowing delayedCalled = signature.getMethod().getAnnotation(PGShowing.class);
//            boolean isCanceled = delayedCalled.isCanceled();
            if (isCancelled) {
                progressBarLoading = new ProgressBarLoading(GlobalVars.CurFragmentContext, true, GlobalVars.downloadPhotoTread);
            } else {
                progressBarLoading = new ProgressBarLoading(GlobalVars.CurFragmentContext);
            }
            GlobalVars.currentPB = progressBarLoading;

            progressBarLoading.show();
            function.run();
            progressBarLoading.dismiss();
        } catch (Exception e) {
            if (progressBarLoading != null)
                progressBarLoading.dismiss();
            e.printStackTrace();
            Config.sout(e);
        }
    }
}
