package com.amber.armtp;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImagesFragment extends Fragment {
    private final Handler handler = new Handler();
    public SQLiteDatabase DB = null;
    public GlobalVars glbVars;
    SharedPreferences settings, pathSettings;
    SharedPreferences.Editor editor;
    Thread thDownloadPhoto = null;
    File SDCard;
    String ftp_server, ftp_user, ftp_pass;
    private android.support.v7.widget.Toolbar toolbar;
    private int progressStatus = 1;
    private ProgressBar pbFiles;
    private TextView tvCount, tvPerc;
    private FTPClient ftpClient = null;
    private Button btStart;
    private Button btStop;
    private CheckBox chkOnlyNew;

    public static float megabytesAvailable(File f) {
        StatFs stat = new StatFs(f.getPath());
        long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        return bytesAvailable / (1024.f * 1024.f);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.images_fragment, container, false);
        glbVars.view = v;
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        glbVars = (GlobalVars) getActivity().getApplicationContext();
        glbVars.setContext(getActivity().getApplicationContext());
        glbVars.frContext = getActivity();
        glbVars.CurAc = getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        DB = glbVars.db.getReadableDatabase();

        toolbar = getActivity().findViewById(R.id.toolbar);
        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);
        btStart = getActivity().findViewById(R.id.btBeginDownload);
        btStop = getActivity().findViewById(R.id.btStopDownload);
        pbFiles = getActivity().findViewById(R.id.pbFiles);
        tvCount = getActivity().findViewById(R.id.tvFilesCnt);
        tvPerc = getActivity().findViewById(R.id.tvPerc);
        chkOnlyNew = getActivity().findViewById(R.id.chkOnlyNew);

        toolbar.setSubtitle("");
        settings = getActivity().getSharedPreferences("apk_version", 0);
        editor = settings.edit();
        pathSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ftp_server = settings.getString("FtpPhotoSrv", getResources().getString(R.string.ftp_server));
        ftp_user = settings.getString("FtpPhotoUser", getResources().getString(R.string.ftp_pass));
        ftp_pass = settings.getString("FtpPhotoPass", getResources().getString(R.string.ftp_user));

        SDCard = new File(glbVars.GetSDCardpath());

        ftpClient = new FTPClient();
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvCount.setText("0 / 0");
                tvPerc.setText("0%");
                pbFiles.setProgress(0);
                pbFiles.setMax(0);
                progressStatus = 1;
                if (Build.BRAND.equals("generic_x86_64") || Build.BRAND.equals("Android")) {
                    if (thDownloadPhoto == null) {
                        btStop.setEnabled(true);
                        btStart.setEnabled(false);
                        if (megabytesAvailable(SDCard.getAbsoluteFile()) < 1000) {
                            Toast.makeText(getActivity(), "Очень мало доступного свободного места", Toast.LENGTH_LONG).show();
                            btStop.performClick();
                        } else {
                            DownloadNomenPhotos();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Уже идет загрузка файлов", Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (thDownloadPhoto == null) {
                        btStop.setEnabled(true);
                        btStart.setEnabled(false);

                        if (megabytesAvailable(glbVars.getPhotoDirFile().getAbsoluteFile()) < 500) {
                            Toast.makeText(getActivity(), "Очень мало доступного свободного места", Toast.LENGTH_LONG).show();
                            btStop.performClick();
                        } else {
                            DownloadNomenPhotos();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Уже идет загрузка файлов", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (thDownloadPhoto != null) {
                    Thread dummy = thDownloadPhoto;
                    thDownloadPhoto = null;
                    dummy.interrupt();
                    try {
                        ftpClient.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                btStop.setEnabled(false);
                btStart.setEnabled(true);
            }
        });

    }

    private void DownloadNomenPhotos() {
        final String SdPhotoDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DCIM).toString();

        try {
            ftpClient.connect(ftp_server);
            ftpClient.login(ftp_user, ftp_pass);
            ftpClient.changeWorkingDirectory("FOTO");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        thDownloadPhoto = new Thread(new Runnable() {
            @Override
            public void run() {
                final int FilesCount;
                File photo;
                String ID, ISSEC;
                String Sql;
                if (chkOnlyNew.isChecked()) {
                    Sql = "SELECT PHOTO1 AS PHOTO, ID, '' AS ISSEC FROM NOMEN WHERE PHOTO1!='' AND P1D=0 UNION  ALL SELECT PHOTO2 AS PHOTO, ID, substr(PHOTO2, -6, 6) AS ISSEC FROM NOMEN WHERE PHOTO2!='' AND P2D=0 ORDER BY ID";
                } else {
                    Sql = "SELECT PHOTO1 AS PHOTO, ID, '' AS ISSEC FROM NOMEN WHERE PHOTO1!='' UNION  ALL SELECT PHOTO2 AS PHOTO, ID, substr(PHOTO2, -6, 6) AS ISSEC FROM NOMEN WHERE PHOTO2!='' ORDER BY ID";
                }
                Cursor cur = glbVars.db.getWritableDatabase().rawQuery(Sql, null);

                if (cur.moveToNext()) {
                    FilesCount = cur.getCount();
                    pbFiles.setMax(FilesCount);
                    handler.post(new Runnable() {
                        public void run() {
                            tvCount.setText("0 / " + FilesCount);
                        }
                    });
                    int perc;
                    while (cur.moveToNext()) {
                        if (!ftpClient.isConnected()) {
                            break;
                        }
                        ID = cur.getString(1);
                        ISSEC = cur.getString(2);
                        photo = new File(SdPhotoDir + "/" + cur.getString(0));
                        if (!photo.exists() || photo.length() == 0) {
                            try {
                                FileOutputStream fos = new FileOutputStream(SdPhotoDir + "/" + cur.getString(0));
                                ftpClient.retrieveFile(cur.getString(0), fos);
                                fos.close();
                                if (ISSEC == "_2.jpg") {
                                    glbVars.db.getWritableDatabase().execSQL("UPDATE Nomen SET P2D=1 WHERE ID='" + ID + "'");
                                } else {
                                    glbVars.db.getWritableDatabase().execSQL("UPDATE Nomen SET P1D=1 WHERE ID='" + ID + "'");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            progressStatus += 1;
                            pbFiles.setProgress(progressStatus);
                            perc = progressStatus * 100 / FilesCount;
                            final int finalPerc = perc;
                            handler.post(new Runnable() {
                                public void run() {
                                    tvCount.setText(progressStatus + " / " + FilesCount);
                                    tvPerc.setText(finalPerc + "%");
                                }
                            });
                        } else {
                            if (!ftpClient.isConnected()) {
                                break;
                            }

                            try {
                                ftpClient.sendCommand("SIZE", cur.getString(0));
                                String reply = ftpClient.getReplyString();
                                File file = new File(SdPhotoDir + "/" + cur.getString(0));
                                long length = file.length();
                                if (Long.parseLong(reply.substring(4).trim()) != length) {
                                    FileOutputStream fos = new FileOutputStream(SdPhotoDir + "/" + cur.getString(0));
                                    ftpClient.retrieveFile(cur.getString(0), fos);
                                    fos.close();
                                    if (ISSEC == "_2.jpg") {
                                        glbVars.db.getWritableDatabase().execSQL("UPDATE Nomen SET P2D=1 WHERE ID='" + ID + "'");
                                    } else {
                                        glbVars.db.getWritableDatabase().execSQL("UPDATE Nomen SET P1D=1 WHERE ID='" + ID + "'");
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            progressStatus += 1;
                            pbFiles.setProgress(progressStatus);
                            perc = progressStatus * 100 / FilesCount;
                            final int finalPerc = perc;
                            handler.post(new Runnable() {
                                public void run() {
                                    tvCount.setText(progressStatus + " / " + FilesCount);
                                    tvPerc.setText(finalPerc + "%");
                                }
                            });
                        }
                    }
                }
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    public void run() {
                        btStop.performClick();
                    }
                });
            }
        });
        thDownloadPhoto.start();
    }
}