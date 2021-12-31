package com.amber.armtp.ServerWork.Zip;

import com.amber.armtp.ServerDetails;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ZipDownload {
    private final String host;
    private final int port;
    private final String dir;

    private final String filePathInAndroid;

    public ZipDownload(ServerDetails serverDetails) {
        this.host = serverDetails.host;
        this.port = serverDetails.port;
        this.dir = serverDetails.dir;
        this.filePathInAndroid = serverDetails.filePathInAndroid;
    }

    public void downloadZip() {
        FTPClient client = new FTPClient();

        try {
            client.connect(host, port);
            boolean login = client.login("amberftp", "201002");

            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);
            client.setFileTransferMode(FTP.BINARY_FILE_TYPE);

            if (login) {
                //  Download file from FTP server.
                File f = new File(filePathInAndroid);
                FileOutputStream fos = new FileOutputStream(f);
                client.retrieveFile(dir, fos);
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.logout();
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
