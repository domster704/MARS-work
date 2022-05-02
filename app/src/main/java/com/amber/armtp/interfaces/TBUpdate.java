package com.amber.armtp.interfaces;

import com.amber.armtp.GlobalVars;

public interface TBUpdate {
    default void setContrAndSum(GlobalVars glbVars) {
        String OrderSum = glbVars.db.getOrderSum();
        try {
            if (!OrderSum.equals("")) {
                glbVars.toolbar.setSubtitle("Заказ на сумму " + OrderSum + " руб.");
            } else {
                glbVars.toolbar.setSubtitle("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
