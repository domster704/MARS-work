package com.amber.armtp;

import android.widget.Toast;

public class Mess {
    public static void sout(Object text) {
        GlobalVars.CurAc.runOnUiThread(() -> {
            Toast.makeText(GlobalVars.CurAc, String.valueOf(text), Toast.LENGTH_SHORT).show();
        });
    }
}
