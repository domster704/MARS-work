package com.amber.armtp.ftp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Toast;

import com.amber.armtp.BuildConfig;
import com.amber.armtp.Config;
import com.amber.armtp.GlobalVars;
import com.amber.armtp.MainActivity;
import com.amber.armtp.R;
import com.amber.armtp.ServerDetails;
import com.amber.armtp.annotations.DelayedCalled;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.interfaces.BackupServerConnection;
import com.amber.armtp.ui.FormOrderFragment;
import com.amber.armtp.ui.UpdateDataFragment;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.net.SocketTimeoutException;

public class Downloader implements BackupServerConnection {
    private final GlobalVars globalVars;
    private final Activity activity;

    public Downloader(GlobalVars globalVars, Activity activity) {
        this.globalVars = globalVars;
        this.activity = activity;
    }

    public void downloadApp(final UpdateDataFragment.UIData ui, View view, String ver) {
        try {
            FtpFileDownloader ftpFileDownloader = new FtpFileDownloader(ServerDetails.getInstance(), ServerDetails.getInstance().dirAPK, MainActivity.filesPathAPK, ver + ".apk");
            if (ftpFileDownloader.downloadWithPG(ui)) {
                catchErrorInDownloadProcess(view, ui);
                return;
            }

            activity.runOnUiThread(() -> {
                view.setEnabled(true);
                ui.tvData.setTextColor(Color.rgb(3, 103, 0));
                ftpFileDownloader.changePGData(1, 1, ui, true);
            });

            _startInstallApp(ver);
        } catch (Exception e) {
            catchErrorInDownloadProcess(view, ui);
            e.printStackTrace();
        }
    }

    // TODO: есть баг, когда интернет плохой - полоса прогресса не двигается, но когда он переключается на хороший, и пользователь выходит из меню "Обновить" и заходит обратнр,
    // TODO: то всё работает так, как будто он сохраняет предыдущий результат байтов и продолжает их "догружать", в результате чего разархивирование выдаёт ошибку.
    public void downloadDB(final UpdateDataFragment.UIData ui, View view) {
        String fileName = "armtp3.rar";
        String filePath = MainActivity.filesPathDB + fileName;

        try {
            FtpFileDownloader ftpFileDownloader = new FtpFileDownloader(ServerDetails.getInstance(), ServerDetails.getInstance().dirDB, MainActivity.filesPathDB, fileName);
            if (ftpFileDownloader.downloadWithPG(ui) || !new ZipUnpacking(filePath).doUnpacking()) {
                catchErrorInDownloadProcess(view, ui);
                return;
            }

            activity.runOnUiThread(() -> {
                globalVars.db = new DBHelper(activity.getApplicationContext());

                view.setEnabled(true);
                ui.tvData.setTextColor(Color.rgb(3, 103, 0));
                Config.sout(activity.getResources().getString(R.string.successInDownloadingProcess));
                FormOrderFragment.isContrIdDifferent = true;
                ftpFileDownloader.changePGData(1, 1, ui, true);
                globalVars.dbApp.putSectionsFromDownloadedDB(activity, globalVars.db.getReadableDatabase());

                globalVars.db.setBackupIp();

                globalVars.updateOutedPositionInZakazyTable();
                globalVars.updateOrdersStatusFromDB();

                SharedPreferences serverSettings = globalVars.getSharedPreferences("apk_version", 0);
                SharedPreferences.Editor editor = serverSettings.edit();
                editor.remove("FtpBackupServerHost");

                String tradeRepresentativeID = Config.getTPId(activity);
                if (!globalVars.db.isSettingTpIDIsExistedInDB(tradeRepresentativeID)) {
                    editor.remove("ReportTPId");
                }

                editor.apply();
            });
        } catch (Exception e) {
            catchErrorInDownloadProcess(view, ui);
            e.printStackTrace();
        }
    }

    public String[] isServerVersionNewer() {
        String ver = BuildConfig.VERSION_NAME;

        FTPClient client = new FTPClient();
        int timeout = ServerDetails.getInstance().timeout;
        client.setDefaultTimeout(timeout);
        client.setDataTimeout(timeout);
        client.setConnectTimeout(timeout);
        client.setControlKeepAliveTimeout(timeout);
        client.setControlKeepAliveReplyTimeout(timeout);

        if (!tryConnectToDefaultIpOtherwiseToBackupIp(client)) {
            return new String[] {"", ""};
        }

        try {
//            client.connect(ServerDetails.getInstance().host, Integer.parseInt(ServerDetails.getInstance().port));
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

            String[] serverVersionsArray = serverVersion.split("\\.");
            String[] curVersion = ver.split("\\.");

            String newVersion = "";

            boolean isNewer = _isFirstVersionHigherThanSecond(serverVersionsArray, curVersion);
            if (isNewer)
                newVersion = serverVersion;

            return new String[]{String.valueOf(isNewer), newVersion};
        } catch (SocketTimeoutException e) {
            Config.sout("Время ожидания вышло");
            return new String[]{"error", ""};
        } catch (Exception e) {
            e.printStackTrace();
            return new String[]{"error", ""};
        }
    }

    private void catchErrorInDownloadProcess(View view, UpdateDataFragment.UIData ui) {
        activity.runOnUiThread(() -> {
            System.out.println(activity.getResources().getString(R.string.errorInDownloadingProcess));
            Config.sout(activity.getResources().getString(R.string.errorInDownloadingProcess), Toast.LENGTH_LONG);
            view.setEnabled(true);
            ui.progressBar.setProgress(0);
        });
    }

    private boolean _isFirstVersionHigherThanSecond(String[] first, String[] second) {
        boolean isNewerLocal = false;
        for (int i = 0; i < first.length; i++) {
            if (Integer.parseInt(first[i]) > Integer.parseInt(second[i])) {
                isNewerLocal = true;
                break;
            }
        }
        return isNewerLocal;
    }

    private void _startInstallApp(String name) {
        activity.runOnUiThread(new Runnable() {
            @Override
            @DelayedCalled(delay = 300)
            public void run() {
                File file1 = new File(MainActivity.filesPathAPK + "/" + name + ".apk");
                Uri uri = FileProvider.getUriForFile(activity.getApplicationContext(), activity.getApplicationContext().getPackageName() + ".provider", file1);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                activity.getApplicationContext().startActivity(intent);
            }
        });
    }

    private boolean _checkAlreadyExistedApk(String ver) {
        boolean isExisted = false;
        File file = new File(MainActivity.filesPathAPK + "/" + ver + ".apk");
        if (file.exists() && !ver.equals("") && _isFirstVersionHigherThanSecond(ver.split("\\."), BuildConfig.VERSION_NAME.split("\\."))) {
            _startInstallApp(ver);
            for (File file1 : new File(MainActivity.filesPathAPK + "/").listFiles()) {
                file1.deleteOnExit();
            }
            isExisted = true;
        }
        return isExisted;
    }
}
