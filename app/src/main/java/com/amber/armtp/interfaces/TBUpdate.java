package com.amber.armtp.interfaces;

import android.support.v7.widget.Toolbar;

import com.amber.armtp.dbHelpers.DBHelper;

public interface TBUpdate {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
