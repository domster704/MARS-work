package com.amber.armtp.interfaces;

import android.content.Context;

import com.amber.armtp.extra.Config;
import com.amber.armtp.extra.ProgressBarShower;
import com.amber.armtp.ftp.Ping;

public interface ServerChecker {
    default void runCheckServerForAvailability(Context context, Thread t) {
        try {
            new Thread(() -> {
                new ProgressBarShower(context).setFunction(() -> {
                    try {
                        if (!new Ping().isReachable()) {
                            Config.sout("Сервер недоступен", context);
                        } else {
                            t.start();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }).start();
            }).start();
        } catch (Exception e) {
            Config.sout(e, context);
        }
    }
}
