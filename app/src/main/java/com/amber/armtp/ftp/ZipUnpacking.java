package com.amber.armtp.ftp;


import android.util.Log;

import com.github.junrar.Junrar;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ZipUnpacking {
    private final String fileDir;

    public ZipUnpacking(String filePathInAndroid) {
        this.fileDir = filePathInAndroid;
    }

    private boolean unZip(File archiveFile) {
        final File destinationFolder = new File(archiveFile.getParent());
        try (InputStream stream = new FileInputStream(archiveFile)) {
//        try {
            Junrar.extract(stream, destinationFolder);
//            Junrar.extract(archiveFile, destinationFolder);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean doUnpacking() {
        File file = new File(fileDir);
        if (!file.exists() || !file.canRead()) {
            Log.e("ftp", "File cannot be read");
            return false;
        }

        try {
            boolean result = unZip(file);
            file.delete();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
