package com.amber.armtp.ftp;


import android.util.Log;

import org.apache.commons.io.FileSystemUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZipUnpacking {
    private final String fileDir;

    public ZipUnpacking(String filePathInAndroid) {
        this.fileDir = filePathInAndroid;
    }

    private void unZip(File file) throws IOException {
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(fileDir)));
        byte[] buffer = new byte[2048];
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                new File(file.getParent(), entry.getName()).mkdirs();
            } else {
                System.out.println(entry.getName() + " " + file.getPath());
                try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(new File(file.getParent(), entry.getName())));) {
                    int len;
                    while ((len = zip.read(buffer)) > 0) {
                        output.write(buffer, 0, len);
                    }
                }
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
