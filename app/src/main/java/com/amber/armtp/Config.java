package com.amber.armtp;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class Config {
    public static void sout(Object text) {
        GlobalVars.CurAc.runOnUiThread(() -> Toast.makeText(GlobalVars.CurAc, String.valueOf(text), Toast.LENGTH_SHORT).show());
    }

    public static void sout(Object text, int length) {
        GlobalVars.CurAc.runOnUiThread(() -> Toast.makeText(GlobalVars.CurAc, String.valueOf(text), length).show());
    }

    public static void printCursor(Cursor cursor) {
        String logName = "xd";

        StringBuilder s1 = new StringBuilder();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            s1.append(cursor.getColumnName(i)).append(" ");
        }
        Log.d(logName, String.valueOf(s1));
        while (cursor.moveToNext()) {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                s.append(cursor.getString(i)).append(" ");
            }

            Log.d(logName, String.valueOf(s));
        }
    }

    public static void hideKeyBoard() {
        InputMethodManager inputMethodManager = (InputMethodManager) GlobalVars.CurAc.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(GlobalVars.CurAc.getCurrentFocus().getWindowToken(), 0);

        SearchView searchView = GlobalVars.CurAc.findViewById(R.id.menu_search);
        if (searchView != null) {
            searchView.clearFocus();
            searchView.requestFocus();
        }
    }
}