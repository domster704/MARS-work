package com.amber.armtp.extra;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.amber.armtp.R;

public class Config {
    public static void sout(Object text, Context context) {
        try {
            new Handler().post(() -> Toast.makeText(context, String.valueOf(text), Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sout(Object text, Context context, int length) {
        try {
            new Handler().post(() -> Toast.makeText(context, String.valueOf(text), length).show());
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static void hideKeyBoard(FragmentActivity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);

        SearchView searchView = activity.findViewById(R.id.menu_search);
        if (searchView != null) {
            searchView.clearFocus();
        }
    }

    public static String getTPId(Activity activity) {
        SharedPreferences settings = activity.getSharedPreferences("apk_version", 0);
        return settings.getString("ReportTPId", ""); // IXXX26 I09601
    }
}