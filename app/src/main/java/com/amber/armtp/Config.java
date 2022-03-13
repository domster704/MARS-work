package com.amber.armtp;

import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

public class Config {
    public static void sout(Object text) {
        GlobalVars.CurAc.runOnUiThread(() -> Toast.makeText(GlobalVars.CurAc, String.valueOf(text), Toast.LENGTH_SHORT).show());
    }

    public static void sout(Object text, int length) {
        GlobalVars.CurAc.runOnUiThread(() -> Toast.makeText(GlobalVars.CurAc, String.valueOf(text), length).show());
    }

    public static void printCursor(Cursor c) {
        String logName = "xd";

        StringBuilder s1 = new StringBuilder();
        for (int i = 0; i < c.getColumnCount(); i++) {
            s1.append(c.getColumnName(i)).append(" ");
        }
        Log.d(logName, String.valueOf(s1));
        while (c.moveToNext()) {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < c.getColumnCount(); i++) {
                s.append(c.getString(i)).append(" ");
            }

            Log.d(logName, String.valueOf(s));
        }
    }
}