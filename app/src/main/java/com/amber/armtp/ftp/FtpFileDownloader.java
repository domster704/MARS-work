package com.amber.armtp.ftp;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.amber.armtp.ServerDetails;
import com.amber.armtp.ui.UpdateDataFragment;

import java.io.File;
import java.io.IOException;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

public class FtpFileDownloader extends Ftp {
    private final String dir;
    private final String filePathInAndroid;
    private boolean isFailed = false;

    private final Handler handler;

    @SuppressLint("HandlerLeak")
    public FtpFileDownloader(ServerDetails serverDetails, String remoteDir, String localDir, String fileName) {
        super(serverDetails);
        this.dir = remoteDir;
        this.filePathInAndroid = localDir + fileName;

        handler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                isFailed = msg.what == 1;
            }
        };
    }

    private class TransferListener implements FTPDataTransferListener {
        private final long fileSize;
        private long progressSize = 0;
        private final UpdateDataFragment.UIData ui;

        public TransferListener(long fileSize, UpdateDataFragment.UIData ui) {
            this.fileSize = fileSize;
            this.ui = ui;
        }

        @Override
        public void started() {
        }

        @Override
        public void transferred(int i) {
            progressSize += i;
            changePGData(fileSize, progressSize, ui, false);
        }

        @Override
        public void completed() {
//            changePGData(fileSize, fileSize, ui, true);
            handler.sendEmptyMessage(0);
        }

        @Override
        public void aborted() {
            handler.sendEmptyMessage(1);
        }

        @Override
        public void failed() {
            handler.sendEmptyMessage(1);
        }
    }

    public boolean downloadWithPG(UpdateDataFragment.UIData ui) throws FTPIllegalReplyException, FTPAbortedException, FTPDataTransferException, IOException, FTPException {
//        long fileSize = ;
        boolean login = initFtpClient();
        if (login) {
            client.download(dir, new File(filePathInAndroid), new TransferListener(getFileSize(dir), ui));
            logout();
        } else {
            isFailed = true;
        }

        return isFailed;
    }

//    public boolean downloadWithPG(UpdateDataFragment.UIData ui) throws Exception {
//        int size = (int) getFileSize(dir);
//        String ftp_server, ftp_user, ftp_pass;
//        ftp_server = ServerDetails.getInstance().host;
//        ftp_user = ServerDetails.getInstance().user;
//        ftp_pass = ServerDetails.getInstance().password;
//        FTPClient ftpClient = new FTPClient();
//        ftpClient.connect(ftp_server);
//        ftpClient.login(ftp_user, ftp_pass);
//
////        ftpClient.changeWorkingDirectory("/EXCHANGE/OUT/MARS");
//        ftpClient.enterLocalPassiveMode();
//        ftpClient.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
//        ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
//
//        FileOutputStream fos = new FileOutputStream(filePathInAndroid);
//
////        File downloadFile2 = new File(filePathInAndroid);
////        OutputStream outputStream2 = new BufferedOutputStream(new FileOutputStream(downloadFile2));
////        InputStream inputStream = ftpClient.retrieveFileStream(dir);
////        byte[] bytesArray = new byte[8192];
////        int bytesRead = -1;
////        long s = 0;
////        while ((bytesRead = inputStream.read(bytesArray)) != -1) {
////            outputStream2.write(bytesArray, 0, bytesRead);
////            s += bytesRead;
////            changePGData(size, s, ui);
////        }
////
////        boolean success = ftpClient.completePendingCommand();
////        outputStream2.close();
////        inputStream.close();
////
////        return !success;
//        boolean isS = ftpClient.retrieveFile("armtp3.rar", fos);
//        fos.close();
//        return !isS;
//    }

    public void changePGData(final long finalCount, long progressStatus, final UpdateDataFragment.UIData ui, boolean isCompleted) {
        float factor = isCompleted ? 1: 0.95f;
        final long perc = progressStatus * 100L / finalCount;
        ui.progressBar.setProgress((int) (factor * perc));
    }
}
