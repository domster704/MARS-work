package com.amber.armtp.extra;

import static android.content.Context.CONNECTIVITY_SERVICE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

public class ExtraFunctions {
    public static String getPhotoDir(Context context) {
        String photo_dir;
        File file = context.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        File extPhoto, arm_photo = null;
        extPhoto = new File(file.toString());

        if (extPhoto.canWrite()) {
            arm_photo = new File(extPhoto.toString());
            if (!arm_photo.exists()) {
                arm_photo.mkdir();
            }
        }
        photo_dir = arm_photo.toString();
        return photo_dir;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            return true;
        }
        Config.sout("Нет доступа к интернету", context);
        return false;
    }

    public static void updateTextFormatTo2DecAfterPoint(TextView v) {
        new Handler().post(() -> {
            try {
                v.setText(String.format(Locale.ROOT, "%.2f", Float.parseFloat(v.getText().toString().replace(",", "."))));
            } catch (Exception ignored) {
            }
        });
    }
}
