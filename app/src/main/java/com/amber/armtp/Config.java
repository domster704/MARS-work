package com.amber.armtp;

import android.widget.Toast;

public class Config {
    public static void sout(Object text) {
        GlobalVars.CurAc.runOnUiThread(() -> Toast.makeText(GlobalVars.CurAc, String.valueOf(text), Toast.LENGTH_SHORT).show());
    }

    public static void sout(Object text, int length) {
        GlobalVars.CurAc.runOnUiThread(() -> Toast.makeText(GlobalVars.CurAc, String.valueOf(text), length).show());
    }
}