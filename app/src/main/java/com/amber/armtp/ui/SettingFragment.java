package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amber.armtp.extra.Config;
import com.amber.armtp.R;
import com.amber.armtp.ServerDetails;
import com.amber.armtp.dbHelpers.DBHelper;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Панель настроек
 * <p>
 * Updated by domster704 on 27.09.2021
 */
public class SettingFragment extends Fragment implements View.OnClickListener {
    public static int nomenDescriptionFontSize = 14;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor, settingsEditor;

    private EditText fontSize;
    private EditText etFtpServer, etFtpUser, etFtpPass, etFtpPort, etFtpBackupServer;
    private EditText etTpSetting;
    private EditText etTimeout;

    private DBHelper db;

    public SettingFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.setting_fragment, container, false);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * Получение элементов фрагмента и изменение их параметров
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        db = new DBHelper(getActivity().getApplicationContext());

        SharedPreferences serverSettings = getActivity().getSharedPreferences("apk_version", 0);
        editor = serverSettings.edit();

        settings = getActivity().getSharedPreferences("settings", 0);
        settingsEditor = settings.edit();

        if (!settings.contains("fontSize")) {
            settingsEditor.putInt("fontSize", nomenDescriptionFontSize);
            settingsEditor.apply();
        }

        etFtpServer = getActivity().findViewById(R.id.etFtpServer);
        etFtpPass = getActivity().findViewById(R.id.edFtpPass);
        etFtpUser = getActivity().findViewById(R.id.edFtpUser);
        etFtpPort = getActivity().findViewById(R.id.edFtpPort);
        etFtpBackupServer = getActivity().findViewById(R.id.edFtpBackupHost);
        etTimeout = getActivity().findViewById(R.id.etTimeout);

        etFtpServer.setText(serverSettings.getString("FtpServerHost", getResources().getString(R.string.host)));
        etFtpPass.setText(serverSettings.getString("FtpServerPass", getResources().getString(R.string.password)));
        etFtpUser.setText(serverSettings.getString("FtpServerUser", getResources().getString(R.string.user)));
        etFtpPort.setText(String.valueOf(serverSettings.getInt("FtpServerPort", Integer.parseInt(getResources().getString(R.string.port)))));
        etFtpBackupServer.setText(serverSettings.getString("FtpBackupServerHost", ServerDetails.getInstance().getBackUpIp()));
        etTimeout.setText(String.valueOf(serverSettings.getInt("timeout", ServerDetails.getInstance().timeout)));

        nomenDescriptionFontSize = settings.getInt("fontSize", 14);
        fontSize = getActivity().findViewById(R.id.fontSize);
        fontSize.setText(String.valueOf(nomenDescriptionFontSize));

        etTpSetting = getActivity().findViewById(R.id.etTpSetting);
        etTpSetting.setText(serverSettings.getString("ReportTPId", ""));

        getActivity().findViewById(R.id.ftpServerLayoutMain).setOnClickListener(this);
        getActivity().findViewById(R.id.fontLayoutMain).setOnClickListener(this);
        getActivity().findViewById(R.id.photoLayoutsMain).setOnClickListener(this);
        getActivity().findViewById(R.id.reportLayoutsMain).setOnClickListener(this);

        getActivity().findViewById(R.id.buttonClearAllPhoto).setOnClickListener(this);

        android.support.v7.widget.Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Настройки");
        toolbar.setSubtitle("");

        if (getArguments() == null)
            return;

        int[] layoutsNeededToOpen = getArguments().getIntArray("Layouts");
        for (int id : layoutsNeededToOpen) {
            LinearLayout layout = getActivity().findViewById(id);
            for (int i = 0; i < layout.getChildCount(); i++) {
                if (layout.getChildAt(i) instanceof LinearLayout) {
                    layout.getChildAt(i).setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reportLayoutsMain:
            case R.id.photoLayoutsMain:
            case R.id.fontLayoutMain:
            case R.id.ftpServerLayoutMain:
                LinearLayout layout = (LinearLayout) view;
                for (int i = 0; i < layout.getChildCount(); i++) {
                    if (layout.getChildAt(i) instanceof LinearLayout) {
                        LinearLayout layoutInside = (LinearLayout) layout.getChildAt(i);
                        if (layoutInside.getVisibility() == View.GONE) {
                            layoutInside.setVisibility(View.VISIBLE);
                        } else {
                            layoutInside.setVisibility(View.GONE);
                        }
                        break;
                    }
                }
                break;
            case R.id.buttonClearAllPhoto:
                new AlertDialog.Builder(getActivity())
                        .setTitle("Удалить все фотографии")
                        .setMessage("Вы уверены, что хотите удалить все фотографии товаров с этого устройства?")
                        .setPositiveButton("Да", (dialogInterface, i) -> getActivity().runOnUiThread(() -> {
                            File file = new File(getPhotoDir());
                            int countOfFiles = file.listFiles().length;
                            for (File elem : file.listFiles()) {
                                boolean isDeleted = elem.delete();
                                if (!isDeleted) {
                                    try {
                                        throw new FileNotFoundException("Файл не найден");
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            if (countOfFiles != 0) {
                                Config.sout("Фотографии успешно удалены", getContext());
                                db.clearPhoto();
                            } else {
                                Config.sout("Отсутствуют фотографии на устройстве", getContext());
                            }
                        }))
                        .setNeutralButton("Нет", (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.SettingsSave) {
            Config.hideKeyBoard(getActivity());
            changeServerData();
            changeFontSize();
            setTpInSalesFragment();
            changeTimeout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeServerData() {
        String host = etFtpServer.getText().toString();
        String port = etFtpPort.getText().toString();
        String user = etFtpUser.getText().toString();
        String pass = etFtpPass.getText().toString();
        String backupHost = etFtpBackupServer.getText().toString();

        editor.putString("FtpPhotoSrv", host);
        editor.putString("FtpPhotoPass", pass);
        editor.putString("FtpPhotoUser", user);
        editor.putInt("FtpPhotoPort", Integer.parseInt(port));

        editor.putString("FtpServerHost", host);
        editor.putString("FtpServerPass", pass);
        editor.putString("FtpServerUser", user);
        editor.putInt("FtpServerPort", Integer.parseInt(port));
        editor.putString("FtpBackupServerHost", backupHost);

        ServerDetails.updateInstance(host, port, user, pass);
        ServerDetails.getInstance().setBackupIp(backupHost);
        editor.commit();

        Config.sout("Настройки сохранены", getContext());
    }

    /**
     * Пока что изменяет шрифт только в поле DESCR (Наименование) во фрагменте Формирование
     */
    private void changeFontSize() {
        if (Integer.parseInt(fontSize.getText().toString()) == settings.getInt("fontSize", -1)) {
            return;
        }

        if (Integer.parseInt(fontSize.getText().toString()) > 24) {
            Toast.makeText(getActivity(), "Слишком большой размер шрифта", Toast.LENGTH_SHORT).show();
            return;
        }

        nomenDescriptionFontSize = Integer.parseInt(fontSize.getText().toString());
        settingsEditor.putInt("fontSize", nomenDescriptionFontSize);
        settingsEditor.apply();

        Config.sout("Размер шрифта изменён на " + nomenDescriptionFontSize, getContext());
    }

    private void setTpInSalesFragment() {
        editor.putString("ReportTPId", etTpSetting.getText().toString().trim());
        editor.apply();
    }

    private void changeTimeout() {
        if (ServerDetails.getInstance().timeout == Integer.parseInt(etTimeout.getText().toString())) {
            return;
        }
        ServerDetails.getInstance().timeout = Integer.parseInt(etTimeout.getText().toString());
        editor.putInt("timeout", ServerDetails.getInstance().timeout);
        editor.apply();
        Config.sout("Таймаут изменён на " + ServerDetails.getInstance().timeout, getContext());
    }

    public String getPhotoDir() {
        String photo_dir;
        File file = getActivity().getExternalFilesDir(Environment.DIRECTORY_DCIM);
        File extPhoto, arm_photo = null;
        extPhoto = new File(file.toString());

        if (extPhoto.canWrite()) {
            arm_photo = new File(extPhoto.toString());
            if (!arm_photo.exists()) {
                arm_photo.mkdir();
            }
        }
        photo_dir = arm_photo.toString();
        return photo_dir;
    }
}
