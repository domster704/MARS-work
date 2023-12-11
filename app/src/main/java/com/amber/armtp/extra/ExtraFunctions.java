package com.amber.armtp.extra;

import android.content.Context;
import android.os.Environment;

import java.io.File;

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
}
