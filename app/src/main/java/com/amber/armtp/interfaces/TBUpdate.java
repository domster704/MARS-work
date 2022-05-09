package com.amber.armtp.interfaces;

import com.amber.armtp.GlobalVars;

public interface TBUpdate {
    default void setContrAndSum(GlobalVars glbVars) {
        try {
            String OrderSum = glbVars.db.getOrderSum();
            if (!OrderSum.equals("")) {
                glbVars.toolbar.setSubtitle(OrderSum + " руб.");
            } else {
                glbVars.toolbar.setSubtitle("");
            }
            String ToolBarContr = glbVars.db.GetToolbarContr();
            if (!ToolBarContr.trim().equals("")) {
                glbVars.toolbar.setTitle(ToolBarContr);
            }
//            glbVars.toolbar.setSubtitle("Сивинский потребительский кооператив - " + "12345678910111213141516171819201234567891011121314151617181920" + " руб.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
