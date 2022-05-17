package com.amber.armtp.ftp;


import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
                ZipEntry newEntry = new ZipEntry(entry);
                try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(new File(file.getParent(), newEntry.getName())));) {
                    int len;
                    while ((len = zip.read(buffer)) > 0) {
                        output.write(buffer, 0, len);
                    }
                }
            }
        }

        zip.close();
    }

//    private void unZip(File inputFile) throws IOException {
//        File outputFile = new File(inputFile.getPath().split("\\.")[0] + ".db");
//        ZipFile zip = new ZipFile(fileDir);
//        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(outputFile));
//        Enumeration entries = zip.entries();
//        byte[] buffer = new byte[2048];
//        while (entries.hasMoreElements()) {
//            ZipEntry entry = (ZipEntry) entries.nextElement();
//            outputStream.putNextEntry(entry);
//            InputStream in = zip.getInputStream(entry);
//            while (0 < in.available()) {
//                int read = in.read(buffer);
//                if (read > 0) {
//                    outputStream.write(buffer, 0, read);
//                }
//            }
//            in.close();
//            outputStream.closeEntry();
//        }
//        outputStream.close();
//    }

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
