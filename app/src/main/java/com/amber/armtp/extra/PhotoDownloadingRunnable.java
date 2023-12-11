package com.amber.armtp.extra;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;

import com.amber.armtp.R;
import com.amber.armtp.ServerDetails;
import com.amber.armtp.ftp.Ftp;
import com.amber.armtp.interfaces.BackupServerConnection;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;

import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

public class PhotoDownloadingRunnable implements Runnable, BackupServerConnection {
    public final static int MESSAGE_UPDATE_DB = 1;
    public final static int MESSAGE_SHOW_PRODUCTS = 2;

    private final String[] fileNames;
    private String kod5;
    private int necessaryBytesAmountForDeletingFile = 5;
    private final boolean isForced;

    private FTPClient ftpClient = null;
    private FileOutputStream fosPhoto = null;
    private InputStream inputStream = null;

    private String ftp_user, ftp_pass;

    private Context context;
    public String currentDownloadingPhotoName = "";

    private final Handler handler;

    private void init() {
        SharedPreferences settings;
        settings = context.getSharedPreferences("apk_version", 0);

        ftp_user = settings.getString("FtpPhotoUser", context.getResources().getString(R.string.ftp_pass));
        ftp_pass = settings.getString("FtpPhotoPass", context.getResources().getString(R.string.ftp_user));
    }

//    public PhotoDownloadingRunnable(String[] fileNames, long ID, boolean isForced, FragmentActivity activity) {
//        this.fileNames = fileNames;
//        this.isForced = isForced;
//        this.kod5 = db.getProductKod5ByRowID(ID);
//        this.activity = activity;
//        this.context = activity.getApplicationContext();
//    }

    public PhotoDownloadingRunnable(String[] fileNames, String kod5, boolean isForced, Context context, Handler handler) {
        this.fileNames = fileNames;
        this.isForced = isForced;
        this.kod5 = kod5;
        this.context = context;
        this.handler = handler;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void run() {
        ProgressBarShower progressBarShower = new ProgressBarShower(context, true);
        progressBarShower.setFunction(() -> {
            try {
                init();
                int countOfSuccessfulDownloadedPhotos = 0;
                for (int i = 0; i < fileNames.length; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    currentDownloadingPhotoName = "";
                    String fileName = fileNames[i];
                    if (!isForced && (fileName == null || fileName != null && new File(ExtraFunctions.getPhotoDir(context) + "/" + fileName).exists())) {
                        countOfSuccessfulDownloadedPhotos++;
                        continue;
                    }
                    currentDownloadingPhotoName = ExtraFunctions.getPhotoDir(context) + "/" + fileName;

                    ftpClient = new FTPClient();
                    int timeout = ServerDetails.getInstance().timeout;
                    ftpClient.setDefaultTimeout(timeout);
                    ftpClient.setDataTimeout(timeout);
                    ftpClient.setConnectTimeout(timeout);
                    ftpClient.setControlKeepAliveTimeout(timeout);
                    ftpClient.setControlKeepAliveReplyTimeout(timeout);

                    if (!tryConnectToDefaultIpOtherwiseToBackupIp(ftpClient)) {
                        throw new InterruptedException();
                    }

                    final String photoDir = ExtraFunctions.getPhotoDir(context);
                    ftpClient.login(ftp_user, ftp_pass);
                    ftpClient.changeWorkingDirectory("FOTO");
                    ftpClient.enterLocalPassiveMode();

                    fosPhoto = new FileOutputStream(photoDir + "/" + fileName);

                    ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                    inputStream = ftpClient.retrieveFileStream(fileName);
                    byte[] bytesArray = new byte[16];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                        if (Thread.currentThread().isInterrupted()) {
                            throw new InterruptedException();
                        }
                        fosPhoto.write(bytesArray, 0, bytesRead);
                    }

//                    ftpClient.retrieveFile(fileName, fosPhoto);
                    ftpClient.disconnect();
                    inputStream.close();
                    fosPhoto.close();

                    long remoteSize = getRemotePhotoSize("FOTO/" + fileName);
                    long sizeOnDevice = getDevicePhotoSize(photoDir + "/" + fileName);
                    if (Math.abs(remoteSize - sizeOnDevice) >= necessaryBytesAmountForDeletingFile) {
                        progressBarShower.getProgressBarLoading().changeText("Файл был загружен с повреждениями. Пожалуйста, подождите.");
                        File file = new File(photoDir + "/" + fileName);
                        file.delete();
                        i--;
                        continue;
                    }

                    countOfSuccessfulDownloadedPhotos++;
                    try {
                        Message message = handler.obtainMessage(MESSAGE_UPDATE_DB);
                        Bundle bundle = new Bundle();
                        bundle.putString("fileName", fileName);
                        message.setData(bundle);
                        handler.sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (countOfSuccessfulDownloadedPhotos != 0) {
                    Message message = handler.obtainMessage(MESSAGE_SHOW_PRODUCTS);
                    Bundle bundle = new Bundle();
                    bundle.putStringArray("fileNames", fileNames);
                    bundle.putString("kod5", kod5);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
                currentDownloadingPhotoName = "";
            } catch (InterruptedException interruptedException) {
                closeStreamAndDeleteFile();
            } catch (SocketTimeoutException socketTimeoutException) {
                socketTimeoutException.printStackTrace();
                Config.sout("Время ожидания вышло", context);
                closeStreamAndDeleteFile();
            } catch (Exception e) {
                e.printStackTrace();
                Config.sout("Сервер недоступен", context);
                closeStreamAndDeleteFile();
            }
            return null;
        });
        progressBarShower.start();
    }

    private void closeStreamAndDeleteFile() {
        try {
            if (fosPhoto != null) {
                fosPhoto.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (ftpClient != null) {
                ftpClient.disconnect();
            }
            File f = new File(currentDownloadingPhotoName);
            f.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            Config.sout("Загрузка отменена", context);
        }
    }

    private long getRemotePhotoSize(String fileName) throws FTPIllegalReplyException, IOException, FTPException {
        Ftp ftp = new Ftp(ServerDetails.getInstance());
        return ftp.getFileSize(fileName);
    }

    private long getDevicePhotoSize(String fileName) {
        File file = new File(fileName);
        return file.length();
    }
}
