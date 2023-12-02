//package com.amber.armtp.extra;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.Build;
//import android.os.Environment;
//import android.support.annotation.RequiresApi;
//import android.support.v4.app.FragmentActivity;
//
//import com.amber.armtp.Config;
//import com.amber.armtp.R;
//import com.amber.armtp.ServerDetails;
//import com.amber.armtp.annotations.PGShowing;
//import com.amber.armtp.interfaces.BackupServerConnection;
//
//import org.apache.commons.net.ftp.FTP;
//import org.apache.commons.net.ftp.FTPClient;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.net.SocketTimeoutException;
//
//public class PhotoDownloadingRunnable implements Runnable, BackupServerConnection {
//    private final String[] fileNames;
//    private String kod5;
//    private int necessaryBytesAmountForDeletingFile = 5;
//    private final boolean isForced;
//
//    private FTPClient ftpClient = null;
//    private FileOutputStream fosPhoto = null;
//    private InputStream inputStream = null;
//
//    private String ftp_user, ftp_pass;
//
//    private FragmentActivity activity;
//    private Context context;
//
//    private void init() {
//        SharedPreferences settings;
//        settings = context.getSharedPreferences("apk_version", 0);
//
//        ftp_user = settings.getString("FtpPhotoUser", context.getResources().getString(R.string.ftp_pass));
//        ftp_pass = settings.getString("FtpPhotoPass", context.getResources().getString(R.string.ftp_user));
//    }
//
////    public PhotoDownloadingRunnable(String[] fileNames, long ID, boolean isForced, FragmentActivity activity) {
////        this.fileNames = fileNames;
////        this.isForced = isForced;
////        this.kod5 = db.getProductKod5ByRowID(ID);
////        this.activity = activity;
////        this.context = activity.getApplicationContext();
////    }
//
//    public PhotoDownloadingRunnable(String[] fileNames, String kod5, boolean isForced, FragmentActivity activity) {
//        this.fileNames = fileNames;
//        this.isForced = isForced;
//        this.kod5 = kod5;
//        this.activity = activity;
//        this.context = activity.getApplicationContext();
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    @Override
//    @PGShowing(isCanceled = true)
//    public void run() {
//        try {
//            init();
//            int countOfSuccessfulDownloadedPhotos = 0;
//            for (int i = 0; i < fileNames.length; i++) {
//                if (Thread.currentThread().isInterrupted()) {
//                    throw new InterruptedException();
//                }
//                currentDownloadingPhotoName = "";
//                String fileName = fileNames[i];
//                if (!isForced && (fileName == null || fileName != null && new File(getPhotoDir() + "/" + fileName).exists())) {
////                         && !isDifferentSizeOfPhotosOnDeviceAndOnServer(fileName))
//                    countOfSuccessfulDownloadedPhotos++;
//                    continue;
//                }
//                currentDownloadingPhotoName = getPhotoDir() + "/" + fileName;
//
//                ftpClient = new FTPClient();
//                int timeout = ServerDetails.getInstance().timeout;
//                ftpClient.setDefaultTimeout(timeout);
//                ftpClient.setDataTimeout(timeout);
//                ftpClient.setConnectTimeout(timeout);
//                ftpClient.setControlKeepAliveTimeout(timeout);
//                ftpClient.setControlKeepAliveReplyTimeout(timeout);
//
//                if (!tryConnectToDefaultIpOtherwiseToBackupIp(ftpClient)) {
//                    throw new InterruptedException();
//                }
//
//                final String photoDir = getPhotoDir();
//                ftpClient.login(ftp_user, ftp_pass);
//                ftpClient.changeWorkingDirectory("FOTO");
//                ftpClient.enterLocalPassiveMode();
//
//                fosPhoto = new FileOutputStream(photoDir + "/" + fileName);
//
//                ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
//                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//
//                inputStream = ftpClient.retrieveFileStream(fileName);
//                byte[] bytesArray = new byte[16];
//                int bytesRead;
//                while ((bytesRead = inputStream.read(bytesArray)) != -1) {
//                    if (Thread.currentThread().isInterrupted()) {
//                        throw new InterruptedException();
//                    }
//                    fosPhoto.write(bytesArray, 0, bytesRead);
//                }
//
////                    ftpClient.retrieveFile(fileName, fosPhoto);
//                ftpClient.disconnect();
//                inputStream.close();
//                fosPhoto.close();
//
//                long remoteSize = getRemotePhotoSize("FOTO/" + fileName);
//                long sizeOnDevice = getDevicePhotoSize(photoDir + "/" + fileName);
//                if (Math.abs(remoteSize - sizeOnDevice) >= necessaryBytesAmountForDeletingFile) {
//                    currentPB.changeText("Файл был загружен с повреждениями. Пожалуйста, подождите.");
//                    File file = new File(photoDir + "/" + fileName);
//                    file.delete();
//                    i--;
//                    continue;
//                }
//
//                countOfSuccessfulDownloadedPhotos++;
//                try {
//                    db.getWritableDatabase().execSQL("UPDATE Nomen SET PD=1 WHERE FOTO=? or FOTO2=?", new Object[]{fileName, fileName});
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            if (countOfSuccessfulDownloadedPhotos != 0) {
//                context.runOnUiThread(() -> showProductPhoto(fileNames, kod5));
//            }
//            currentDownloadingPhotoName = "";
//        } catch (InterruptedException interruptedException) {
//            closeStreamAndDeleteFile();
//        } catch (SocketTimeoutException socketTimeoutException) {
//            socketTimeoutException.printStackTrace();
//            Config.sout("Время ожидания вышло");
//            closeStreamAndDeleteFile();
//        } catch (Exception e) {
//            e.printStackTrace();
//            Config.sout("Сервер недоступен");
//            closeStreamAndDeleteFile();
//        }
//    }
//
//    private void closeStreamAndDeleteFile() {
//        try {
//            if (fosPhoto != null) {
//                fosPhoto.close();
//            }
//            if (inputStream != null) {
//                inputStream.close();
//            }
//            if (ftpClient != null) {
//                ftpClient.disconnect();
//            }
//            File f = new File(currentDownloadingPhotoName);
//            f.delete();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        } finally {
//            Config.sout("Загрузка отменена");
//        }
//    }
//
//    public String getPhotoDir() {
//        String photo_dir;
//        File file = context.getExternalFilesDir(Environment.DIRECTORY_DCIM);
//        File extPhoto, arm_photo = null;
//        extPhoto = new File(file.toString());
//
//        if (extPhoto.canWrite()) {
//            arm_photo = new File(extPhoto.toString());
//            if (!arm_photo.exists()) {
//                arm_photo.mkdir();
//            }
//        }
//        photo_dir = arm_photo.toString();
//        return photo_dir;
//    }
//}
