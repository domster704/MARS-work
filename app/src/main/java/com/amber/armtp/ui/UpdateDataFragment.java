package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amber.armtp.extra.Config;
import com.amber.armtp.MainActivity;
import com.amber.armtp.R;
import com.amber.armtp.dbHelpers.DBAppHelper;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.dbHelpers.DBOrdersHelper;
import com.amber.armtp.extra.ExtraFunctions;
import com.amber.armtp.ftp.Downloader;
import com.amber.armtp.interfaces.ServerChecker;

/**
 * Updated by domster704 on 27.09.2021
 */
public class UpdateDataFragment extends Fragment implements View.OnClickListener, ServerChecker {
    public static UIData[] uiData;

    private final Handler handlerDB = new Handler();
    private final Handler handlerApp = new Handler();

    private TextView tvDB, tvApp;
    private ProgressBar pgDB, pgApp;
    private Downloader downloader;
    private String[] versionData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.update_data_fragment_new, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Config.hideKeyBoard(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DBHelper db = new DBHelper(getActivity().getApplicationContext());
        DBAppHelper dbAppHelper = new DBAppHelper(getActivity().getApplicationContext());
        DBOrdersHelper dbOrdersHelper = new DBOrdersHelper(getActivity().getApplicationContext());

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle("");

        Button btnDBUpdate = getActivity().findViewById(R.id.btnDBUpdate);
        btnDBUpdate.setOnClickListener(this);

        tvDB = getActivity().findViewById(R.id.chkDB);
        pgDB = getActivity().findViewById(R.id.pgDB);
        TextView tvDBPerc = getActivity().findViewById(R.id.tbDBPerc);
        TextView tvDBCount = getActivity().findViewById(R.id.tvDBCount);

        Button btnAppUpdate = getActivity().findViewById(R.id.btnAppUpdate);
        btnAppUpdate.setOnClickListener(this);

        tvApp = getActivity().findViewById(R.id.chkApp);
        pgApp = getActivity().findViewById(R.id.pgApp);
        TextView tvAppPerc = getActivity().findViewById(R.id.tvAppPerc);
        TextView tvAppCount = getActivity().findViewById(R.id.tvAppCount);

        uiData = new UIData[2];
        uiData[0] = new UIData(tvDB, pgDB, tvDBCount, tvDBPerc, handlerDB);
        uiData[1] = new UIData(tvApp, pgApp, tvAppCount, tvAppPerc, handlerApp);

        downloader = new Downloader(getActivity(), db, dbAppHelper, dbOrdersHelper);
        TextView tvAppNewVer = getActivity().findViewById(R.id.newVerApp);
        new Thread(() -> {
            try {
                versionData = downloader.isServerVersionNewer();
                getActivity().runOnUiThread(() -> {
                    if (versionData[0].equals("true")) {
                        tvAppNewVer.setVisibility(View.VISIBLE);
                        tvAppNewVer.setText(tvAppNewVer.getText().toString() + ": " + versionData[1]);
                    } else if (versionData[0].equals("false")) {
                        tvAppNewVer.setText(R.string.newVersionAvailable);
                        tvAppNewVer.setVisibility(View.GONE);
                    }
                });
            } catch (NullPointerException ignore) {
            }

        }).start();

        if (!checkIsEnoughSpaceForUpdateApp()) {
            Toast.makeText(getActivity(), "Мало свободного места, могут быть ошибки", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnDBUpdate: {
                if (!ExtraFunctions.isNetworkAvailable(getActivity())) {
                    Config.sout("Нет доступного интернет соединения. Проверьте соединение с Интернетом", getContext());
                    return;
                }

                Thread mainLogic = new Thread(() -> {
                    try {
                        getActivity().runOnUiThread(() -> {
                            view.setEnabled(false);
                            pgDB.setProgress(0);
                            tvDB.setTextColor(Color.rgb(0, 0, 0));
                        });
                        downloader.downloadDB(uiData[0], view);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                runCheckServerForAvailability(getContext(), mainLogic);
                return;
            }
            case R.id.btnAppUpdate: {
                if (!ExtraFunctions.isNetworkAvailable(getActivity())) {
                    Config.sout("Нет доступного интернет соединения. Проверьте соединение с Интернетом", getContext());
                    return;
                }

                Thread mainLogic = new Thread(() -> {
                    String status = downloader.isServerVersionNewer()[0];
                    if (status.equals("false")) {
                        Config.sout("На сервере нет новой версии", getContext());
                        return;
                    } else if (status.equals("")) {
                        return;
                    }
                    try {
                        getActivity().runOnUiThread(() -> {
                            view.setEnabled(false);
                            pgApp.setProgress(0);
                            tvApp.setTextColor(Color.rgb(0, 0, 0));
                        });

                        downloader.downloadApp(uiData[1], view, versionData[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                runCheckServerForAvailability(getContext(), mainLogic);
                break;
            }
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

    private boolean checkIsEnoughSpaceForUpdateApp() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        long availableSpace = (long) (statFs.getFreeBytes() / MainActivity.SIZE_MB);
        return availableSpace > 800;
    }
}
