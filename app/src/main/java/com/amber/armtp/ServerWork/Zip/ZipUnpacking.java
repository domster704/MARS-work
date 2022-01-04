package com.amber.armtp.ServerWork.Zip;


import android.util.Log;

import com.amber.armtp.ServerDetails;

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

    public ZipUnpacking(ServerDetails serverDetails) {
        this.fileDir = serverDetails.filePathInAndroid;
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

    public void doUnpacking() {
        File file = new File(fileDir);
        if (!file.exists() || !file.canRead()) {
            Log.e("ftp", "File cannot be read");
            return;
        }

        try {
            unZip(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void write(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);
        out.close();
        in.close();
    }
}
