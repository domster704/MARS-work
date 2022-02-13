package com.amber.armtp.interfaces;

import com.amber.armtp.GlobalVars;

public interface TBUpdate {
    default void setContrAndSum(GlobalVars glbVars) {
        String ToolBarContr = glbVars.db.GetToolbarContr();
        String OrderSum = glbVars.db.getOrderSum();
        try {
            if (!OrderSum.equals("")) {
                if (ToolBarContr.trim().equals("")) {
                    glbVars.toolbar.setSubtitle("Заказ на сумму " + OrderSum + " руб.");
                } else {
                    glbVars.toolbar.setSubtitle(ToolBarContr + OrderSum + " руб.");
                }
            } else {
                glbVars.toolbar.setSubtitle("");
            }
        } catch (Exception ignored) {
        }
    }
}
