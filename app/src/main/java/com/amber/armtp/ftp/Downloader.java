package com.amber.armtp.ftp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Toast;

import com.amber.armtp.BuildConfig;
import com.amber.armtp.GlobalVars;
import com.amber.armtp.ServerDetails;
import com.amber.armtp.MainActivity;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.ui.UpdateDataFragment;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;

public class Downloader {
    public static String isServerVerNewer;

    private final GlobalVars globalVars;
    private final Activity activity;

    static {
        new Thread(() -> {
            try {
                isServerVerNewer = isServerVersionNewer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public Downloader(GlobalVars globalVars, Activity activity) {
        this.globalVars = globalVars;
        this.activity = activity;
    }

    public void downloadApp(final UpdateDataFragment.UIData ui, View view) {
        if (globalVars.isNetworkAvailable()) {
            if (isServerVerNewer.equals("true")) {
                new Thread(() -> {
                    FtpFileDownloader ftpFileDownloader;
                    try {
                        ftpFileDownloader = new FtpFileDownloader(ServerDetails.getInstance(), ServerDetails.getInstance().dirAPK, MainActivity.filesPathAPK, "app.apk");
                        if (ftpFileDownloader.download(ui)) {
                            if (activity == null || activity.getApplicationContext() == null)
                                return;

                            activity.runOnUiThread(() -> {
                                File file1 = new File(MainActivity.filesPathAPK + "/app.apk");
                                view.setEnabled(true);
                                Toast.makeText(GlobalVars.CurAc, "Приложение успешно скачано", Toast.LENGTH_SHORT).show();

                                Uri uri = FileProvider.getUriForFile(activity.getApplicationContext(), activity.getApplicationContext().getPackageName() + ".provider", file1);
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                activity.getApplicationContext().startActivity(intent);
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } else {
            GlobalVars.CurAc.runOnUiThread(() -> view.setEnabled(true));
        }
    }

    // Кол-во попыток, затраченных на скачивание файла
    private int countOfTrying = 0;
    public void downloadDB(final UpdateDataFragment.UIData ui, View view, GlobalVars globalVariable) {
        new Thread(() -> {
            String filePath = MainActivity.filesPathDB + "armtp3.zip";

            FtpFileDownloader ftpFileDownloader;
            try {
                ftpFileDownloader = new FtpFileDownloader(ServerDetails.getInstance(), ServerDetails.getInstance().dirDB, MainActivity.filesPathDB, "armtp3.zip");
                if (!ftpFileDownloader.download(ui)) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            ZipUnpacking zipUnpacking;
            try {
                zipUnpacking = new ZipUnpacking(filePath);
                if (!zipUnpacking.doUnpacking() && countOfTrying < 3) {
                    countOfTrying++;
                    downloadDB(ui, view, globalVars);
                } else if (countOfTrying >= 3) {
                    return;
                }

                activity.runOnUiThread(() -> {
                    globalVariable.db = new DBHelper(activity.getApplicationContext());

                    view.setEnabled(true);
                    Toast.makeText(GlobalVars.CurAc, "База данных успешно обновлена", Toast.LENGTH_SHORT).show();
                });
                globalVars.dbApp.putDemp(globalVars.db.getReadableDatabase());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static String isServerVersionNewer() {
        String ver = BuildConfig.VERSION_NAME;

        FTPClient client = new FTPClient();

        try {
            client.connect(ServerDetails.getInstance().host, Integer.parseInt(ServerDetails.getInstance().port));
            client.login(ServerDetails.getInstance().user, ServerDetails.getInstance().password);

            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);
            client.setFileTransferMode(FTP.BINARY_FILE_TYPE);

            String path = ServerDetails.getInstance().dirAPK;
            path = path.substring(0, path.lastIndexOf("/") + 1);

            FTPFile[] f = client.listFiles(path);
            String serverVersion = "";

            for (FTPFile file : f) {
                if (file.getName().contains(".txt")) {
                    serverVersion = file.getName().substring(0, file.getName().length() - 4);
                }
            }

            String[] serverVer = serverVersion.split("\\.");
            String[] curVer = ver.split("\\.");

            boolean isNewer = false;
            for (int i = 0; i < serverVer.length; i++) {
                if (Integer.parseInt(serverVer[i]) > Integer.parseInt(curVer[i])) {
                    isNewer = true;
                    break;
                }
            }

            return String.valueOf(isNewer);
        } catch (Exception e) {
            GlobalVars.CurAc.runOnUiThread(() -> Toast.makeText(GlobalVars.CurAc, "Сервер недоступен", Toast.LENGTH_LONG).show());
            e.printStackTrace();
            return "error";
        }
    }
}
