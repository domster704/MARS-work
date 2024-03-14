package com.amber.armtp.ftp;


import android.annotation.SuppressLint;
import android.util.Log;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;

public class ZipUnpacking {
    private final String fileDir;

    public ZipUnpacking(String filePathInAndroid) {
        this.fileDir = filePathInAndroid;
    }

    @SuppressLint("NewApi")
    private boolean unZip(File archiveFile) {
        final File destinationFolder = new File(archiveFile.getParent());

        String source = archiveFile.getPath();
        String destination = destinationFolder.getPath();

        try (ZipFile zipFile = new ZipFile(source)) {
            zipFile.extractAll(destination);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
//        try (InputStream stream = new FileInputStream(archiveFile)) {
//            Junrar.extract(stream, destinationFolder);
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
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
