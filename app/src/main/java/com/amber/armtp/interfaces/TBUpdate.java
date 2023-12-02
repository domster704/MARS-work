package com.amber.armtp.interfaces;

import android.support.v7.widget.Toolbar;

import com.amber.armtp.GlobalVars;
import com.amber.armtp.dbHelpers.DBHelper;

public interface TBUpdate {
    default void setContrAndSum(GlobalVars glbVars) {
        try {
            String OrderSum = glbVars.db.getOrderSum(glbVars.isSales);
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

    default void setContrAndSumValue(DBHelper db, Toolbar toolbar, boolean isSales) {
        try {
            String OrderSum = db.getOrderSum(isSales);
            if (!OrderSum.equals("")) {
                toolbar.setSubtitle(OrderSum + " руб.");
            } else {
                toolbar.setSubtitle("");
            }
            String ToolBarContr = db.GetToolbarContr();
            if (!ToolBarContr.trim().equals("")) {
                toolbar.setTitle(ToolBarContr);
            }
//            glbVars.toolbar.setSubtitle("Сивинский потребительский кооператив - " + "12345678910111213141516171819201234567891011121314151617181920" + " руб.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
