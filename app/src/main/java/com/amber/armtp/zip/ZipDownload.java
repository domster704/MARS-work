package com.amber.armtp.zip;

import android.graphics.Color;

import com.amber.armtp.ServerDetails;
import com.amber.armtp.UpdateDataFragment;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ZipDownload {
    private FTPClient client;
    private boolean login;

    private final String user;
    private final String password;
    private final String host;
    private final String dir;
    private final int port;
    private final long dbSize;

    private final String filePathInAndroid;

    public ZipDownload(ServerDetails serverDetails) throws IOException {
        this.user = serverDetails.user;
        this.password = serverDetails.password;
        this.host = serverDetails.host;
        this.port = serverDetails.port;
        this.dir = serverDetails.dir;
        this.filePathInAndroid = serverDetails.filePathInAndroid;

        this.dbSize = getDbSize();
    }


    public long getDbSize() throws IOException {
        login = initFTPClient();
        if (login) {
            client.sendCommand("SIZE", dir);
            return Long.parseLong(client.getReplyString().split(" ")[1].trim());
        }

        try {
            client.logout();
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public boolean downloadZip(final UpdateDataFragment.UIData ui) throws IOException {
        boolean isDownload = false;

        login = initFTPClient();
        if (login) {
            File downloadFile = new File(filePathInAndroid);
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
            InputStream inputStream = client.retrieveFileStream(dir);
            byte[] bytesArray = new byte[4096];
            int bytesRead;

            int progressStatus = 0;

            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                progressStatus += bytesRead;
                outputStream.write(bytesArray, 0, bytesRead);

                changePGData(dbSize, progressStatus, ui);
            }

            isDownload = client.completePendingCommand();
            outputStream.close();
            inputStream.close();
        }

        try {
            client.logout();
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return isDownload;
    }

    private void changePGData(final long finalCount, int progressStatus, final UpdateDataFragment.UIData ui) {
        final long perc = progressStatus * 100L / finalCount;
        ui.progressBar.setProgress((int) perc);

        final int finalProgressStatus = progressStatus;
        ui.handler.post(new Runnable() {
            public void run() {
                ui.tvCount.setText(finalProgressStatus + "/" + finalCount);
                ui.tvPer.setText(perc + "%");
            }
        });

        ui.handler.post(new Runnable() {
            public void run() {
                ui.checkBox.setChecked(true);
                ui.checkBox.setTextColor(Color.rgb(3, 103, 0));
            }
        });
    }


    private boolean initFTPClient() {
        client = new FTPClient();
        try {
            client.connect(host, port);
            boolean login = client.login(user, password);

//            client.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));
            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);
            client.setFileTransferMode(FTP.BINARY_FILE_TYPE);

            return login;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
