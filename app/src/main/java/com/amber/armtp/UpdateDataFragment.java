package com.amber.armtp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amber.armtp.zip.ZipDownload;
import com.amber.armtp.zip.ZipUnpacking;

import java.io.File;
import java.util.Objects;

/**
 * Updated by domster704 on 27.09.2021
 */
public class UpdateDataFragment extends Fragment {
    public GlobalVars glbVars;
    public static UIData[] uiData;

    private String DebetIsFinished = "0";
    private final BroadcastReceiver UpdateDebetWorking = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DebetIsFinished = Objects.requireNonNull(intent.getExtras()).getString("DebetUpdateFinished");
        }
    };

    private int countOfReturning = 0;

    private final Handler handlerDB = new Handler();
    private CheckBox chkDB;
    private ProgressBar pgDB;
    private TextView tvDBPerc;
    private TextView tvDBCount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.update_data_fragment_new, container, false);
        v.setKeepScreenOn(true);
        setHasOptionsMenu(true);
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
    public void onResume() {
        super.onResume();
        try {
            getActivity().registerReceiver(UpdateDebetWorking, new IntentFilter("DebetUpdating"));
        } catch (Exception ignored) {
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(UpdateDebetWorking);
        } catch (Exception ignored) {
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);
        glbVars.toolbar.setSubtitle("");
        setRetainInstance(true);

        chkDB = getActivity().findViewById(R.id.chkDB);
        pgDB = getActivity().findViewById(R.id.pgDB);
        tvDBPerc = getActivity().findViewById(R.id.tbDBPerc);
        tvDBCount = getActivity().findViewById(R.id.tvDBCount);

        try {
            getActivity().registerReceiver(UpdateDebetWorking, new IntentFilter("DebetUpdating"));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.update_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.updateData:
                if (DebetIsFinished.equals("1") || DebetIsFinished.equals("0")) {
                    if (glbVars.isNetworkAvailable()) {
                        glbVars.UpdateWorking = 1;
                        item.setEnabled(false);

                        uiData = new UIData[2];
                        uiData[0] = new UIData(chkDB, pgDB, tvDBPerc, tvDBCount, handlerDB);

                        DownloadDB();
                        item.setEnabled(true);
                    } else {
                        Toast.makeText(getActivity(), "Нет доступного интернет соединения. Проверьте соединение с Интернетом", Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            default:
                return true;
        }
    }

    public static class UIData {
        public CheckBox checkBox;
        public ProgressBar progressBar;
        public TextView tvPer;
        public TextView tvCount;
        public Handler handler;

        public UIData(CheckBox checkBox, ProgressBar progressBar, TextView tvCount, TextView tvPer, Handler handler) {
            this.checkBox = checkBox;
            this.progressBar = progressBar;
            this.tvPer = tvPer;
            this.tvCount = tvCount;
            this.handler = handler;
        }
    }

    private void DownloadDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ZipDownload zipDownload;
                try {
                    zipDownload = new ZipDownload(ServerDetails.getInstance());
                    if (!zipDownload.downloadZip(uiData[0])) {
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                File f = new File(MainActivity.filesPath + "orders.db");

                ZipUnpacking zipUnpacking;
                try {
                    zipUnpacking = new ZipUnpacking(ServerDetails.getInstance());
                    zipUnpacking.doUnpacking(uiData[0]);

                    if (getFileSizeKiloBytes(new File(MainActivity.filesPath + "armtp3.db")) < 100 && countOfReturning < 3) {
                        countOfReturning++;
                        DownloadDB();
                    } else if (countOfReturning >= 3) {
                        return;
                    } else {
                        glbVars.dbApp.putDemp(glbVars.db.getReadableDatabase());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (f.exists()) {
                    f.delete();
                }
            }
        }).start();
    }

    public void restart() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        this.startActivity(intent);
        System.exit(0);
    }

    private static double getFileSizeKiloBytes(File file) {
        return (double) file.length() / 1024;
    }
}
