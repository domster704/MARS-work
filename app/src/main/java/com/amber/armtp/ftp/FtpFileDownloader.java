package com.amber.armtp.ftp;

import com.amber.armtp.ServerDetails;
import com.amber.armtp.ui.UpdateDataFragment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FtpFileDownloader extends Ftp {
    private final String dir;
    private final String filePathInAndroid;
    private long fileSize;

    public FtpFileDownloader(ServerDetails serverDetails, String remoteDir, String localDir, String fileName) {
        super(serverDetails);
        this.dir = remoteDir;
        this.filePathInAndroid = localDir + fileName;
    }

    public boolean downloadWithPG(final UpdateDataFragment.UIData ui) throws Exception {
        fileSize = getFileSize(dir);

        boolean isDownload = false;
        boolean login = initFtpClient();
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

                if (ui != null)
                    changePGData(fileSize, progressStatus, ui);
            }

            outputStream.close();
            inputStream.close();
            isDownload = client.completePendingCommand();
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
    }
}
