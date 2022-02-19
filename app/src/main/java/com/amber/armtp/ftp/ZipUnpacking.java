package com.amber.armtp.ftp;


import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUnpacking {
    private final String fileDir;

    public ZipUnpacking(String filePathInAndroid) {
        this.fileDir = filePathInAndroid;
    }

    private static void write(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);
        out.close();
        in.close();
    }

    private void unZip(File file) throws IOException {
        ZipFile zip = new ZipFile(fileDir);
        Enumeration entries = zip.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();

            if (entry.isDirectory()) {
                new File(file.getParent(), entry.getName()).mkdirs();
            } else {
                write(zip.getInputStream(entry),
                        new BufferedOutputStream(new FileOutputStream(
                                new File(file.getParent(), entry.getName()))));
            }
        }

        zip.close();
    }

    public boolean doUnpacking() {
        File file = new File(fileDir);
        if (!file.exists() || !file.canRead()) {
            Log.e("ftp", "File cannot be read");
            return true;
        }

        try {
            unZip(file);
            file.delete();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
