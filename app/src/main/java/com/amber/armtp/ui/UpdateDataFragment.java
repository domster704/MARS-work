package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amber.armtp.GlobalVars;
import com.amber.armtp.R;
import com.amber.armtp.ftp.Downloader;

import java.util.Objects;

/**
 * Updated by domster704 on 27.09.2021
 */
public class UpdateDataFragment extends Fragment implements View.OnClickListener {
    public GlobalVars glbVars;
    public static UIData[] uiData;

    private String DebetIsFinished = "0";
    private final BroadcastReceiver UpdateDebetWorking = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DebetIsFinished = Objects.requireNonNull(intent.getExtras()).getString("DebetUpdateFinished");
        }
    };

    private final Handler handlerDB = new Handler();
    private final Handler handlerApp = new Handler();
    private TextView tvDB, tvApp;
    private ProgressBar pgDB, pgApp;
    private TextView tvDBPerc, tvAppPerc;
    private TextView tvDBCount, tvAppCount;

    Button btnDBUpdate, btnAppUpdate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.update_data_fragment_new, container, false);
        v.setKeepScreenOn(true);
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
        GlobalVars.CurAc = getActivity();
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

        btnDBUpdate = getActivity().findViewById(R.id.btnDBUpdate);
        btnDBUpdate.setOnClickListener(this);

        tvDB = getActivity().findViewById(R.id.chkDB);
        pgDB = getActivity().findViewById(R.id.pgDB);
        tvDBPerc = getActivity().findViewById(R.id.tbDBPerc);
        tvDBCount = getActivity().findViewById(R.id.tvDBCount);

        btnAppUpdate = getActivity().findViewById(R.id.btnAppUpdate);
        btnAppUpdate.setOnClickListener(this);

        tvApp = getActivity().findViewById(R.id.chkApp);
        pgApp = getActivity().findViewById(R.id.pgApp);
        tvAppPerc = getActivity().findViewById(R.id.tvAppPerc);
        tvAppCount = getActivity().findViewById(R.id.tvAppCount);

        uiData = new UIData[2];
        uiData[0] = new UIData(tvDB, pgDB, tvDBCount, tvDBPerc, handlerDB);
        uiData[1] = new UIData(tvApp, pgApp, tvAppCount, tvAppPerc, handlerApp);

        try {
            getActivity().registerReceiver(UpdateDebetWorking, new IntentFilter("DebetUpdating"));
        } catch (Exception ignored) {
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnDBUpdate:
                if (DebetIsFinished.equals("1") || DebetIsFinished.equals("0")) {
                    if (glbVars.isNetworkAvailable()) {
                        Downloader downloader = new Downloader(glbVars, getActivity());

                        view.setEnabled(false);

                        pgDB.setProgress(0);
                        tvDB.setTextColor(Color.rgb(0, 0, 0));

                        downloader.downloadDB(uiData[0], view);
                    } else {
                        Toast.makeText(getActivity(), "Нет доступного интернет соединения. Проверьте соединение с Интернетом", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.btnAppUpdate:
                Downloader downloader = new Downloader(glbVars, getActivity());

                if (!Downloader.isServerVerNewer) {
                    Toast.makeText(getContext(), "На сервере нет новой версии", Toast.LENGTH_LONG).show();
                    break;
                }

                view.setEnabled(false);

                pgApp.setProgress(0);
                tvApp.setTextColor(Color.rgb(0, 0, 0));

                try {
                    downloader.downloadApp(uiData[1], view);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public static class UIData {
        public TextView tvData;
        public ProgressBar progressBar;
        public TextView tvPer;
        public TextView tvCount;
        public Handler handler;

        public UIData(TextView tvData, ProgressBar progressBar, TextView tvCount, TextView tvPer, Handler handler) {
            this.tvData = tvData;
            this.progressBar = progressBar;
            this.tvPer = tvPer;
            this.tvCount = tvCount;
            this.handler = handler;
        }
    }
}
