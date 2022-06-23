package com.amber.armtp.ftp;


import android.util.Log;

import com.github.junrar.Junrar;
import com.github.junrar.exception.RarException;

import java.io.File;
import java.io.IOException;

public class ZipUnpacking {
    private final String fileDir;

    public ZipUnpacking(String filePathInAndroid) {
        this.fileDir = filePathInAndroid;
    }

    private boolean unZip(File archiveFile) {
        final File destinationFolder = new File(archiveFile.getParent());
        try {
            Junrar.extract(archiveFile, destinationFolder);
            return true;
        } catch (RarException | IOException ignored) {
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
//            File fileDB = new File(MainActivity.filesPathDB + "armtp3.db");
//            fileDB.delete();
            boolean result = unZip(file);
            file.delete();

//            fileDB = new File(MainActivity.filesPathDB + "armtp3.db");
//            return result && fileDB.exists();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
