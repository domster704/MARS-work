package com.amber.armtp.interfaces;

import com.amber.armtp.Config;
import com.amber.armtp.ServerDetails;
import com.amber.armtp.annotations.PGShowing;
import com.amber.armtp.ftp.Ping;

public interface ServerChecker {
    default void runCheckServerForAvailability(Thread t) {
        try {
            new Thread(new Runnable() {
                @Override
                @PGShowing
                public void run() {
                    try {
                        if (!new Ping(ServerDetails.getInstance()).isReachable()) {
                            Config.sout("Сервер недоступен");
                        } else {
                            t.start();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            Config.sout(e);
        }
    }
}
