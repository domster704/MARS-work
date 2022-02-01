package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.provider.DocumentFile;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amber.armtp.GlobalVars;
import com.amber.armtp.R;
import com.amber.armtp.ServerDetails;

import java.util.Objects;

/**
 * Панель настроек
 * <p>
 * Updated by domster704 on 27.09.2021
 */
public class SettingFragment extends Fragment implements View.OnClickListener {

    public static int nomenDescriptionFontSize = 14;

    public GlobalVars glbVars;
    SharedPreferences serverSettings, settings;
    SharedPreferences.Editor editor, settingPathEditor, settingsEditor;

    Button btSaveSettings;
    EditText fontSize;

    EditText etFtpServer, etFtpUser, etFtpPass, etFtpPort;
    Boolean isSelectPath = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.setting_fragment, container, false);
        glbVars.view = v;
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        glbVars = (GlobalVars) Objects.requireNonNull(getActivity()).getApplicationContext();
        glbVars.setContext(getActivity().getApplicationContext());
        glbVars.frContext = getActivity();
        GlobalVars.CurAc = getActivity();
    }

    /**
     * Получение элементов фрагмента и изменение их параметров
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        serverSettings = getActivity().getSharedPreferences("apk_version", 0);
        editor = serverSettings.edit();

        settings = getActivity().getSharedPreferences("settings", 0);
        settingsEditor = settings.edit();

        if (!settings.contains("fontSize")) {
            settingsEditor.putInt("fontSize", nomenDescriptionFontSize);
            settingsEditor.apply();
        }

        etFtpServer = getActivity().findViewById(R.id.edFtpServer);
        etFtpPass = getActivity().findViewById(R.id.edFtpPass);
        etFtpUser = getActivity().findViewById(R.id.edFtpUser);
        etFtpPort = getActivity().findViewById(R.id.edFtpPort);

        etFtpServer.setText(serverSettings.getString("FtpServerHost", getResources().getString(R.string.host)));
        etFtpPass.setText(serverSettings.getString("FtpServerPass", getResources().getString(R.string.password)));
        etFtpUser.setText(serverSettings.getString("FtpServerUser", getResources().getString(R.string.user)));
        etFtpPort.setText(String.valueOf(serverSettings.getInt("FtpServerPort", Integer.parseInt(getResources().getString(R.string.port)))));

//        btSaveSettings = getActivity().findViewById(R.id.btSaveSettings);

        nomenDescriptionFontSize = settings.getInt("fontSize", 14);
        fontSize = getActivity().findViewById(R.id.fontSize);
        fontSize.setText(String.valueOf(nomenDescriptionFontSize));

        getActivity().findViewById(R.id.ftpServerLayoutMain).setOnClickListener(this);
        getActivity().findViewById(R.id.fontLayoutMain).setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();


//        btSaveSettings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (Integer.parseInt(fontSize.getText().toString()) > 24) {
//                    Toast.makeText(getActivity(), "Слишком большой размер шрифта", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                nomenDescriptionFontSize = Integer.parseInt(fontSize.getText().toString());
//                settingsEditor.putInt("fontSize", nomenDescriptionFontSize);
//                settingsEditor.apply();
//
//                Toast.makeText(getActivity(), "Размер шрифта изменён на " + nomenDescriptionFontSize, Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        btSaveSettings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                editor.putString("FtpPhotoSrv", etFtpServer.getText().toString());
//                editor.putString("FtpPhotoPass", etFtpPass.getText().toString());
//                editor.putString("FtpPhotoUser", etFtpUser.getText().toString());
//                editor.putString("FtpPhotoPort", etFtpUser.getText().toString());
//
//                editor.putString("FtpServerHost", etFtpServer.getText().toString());
//                editor.putString("FtpServerPass", etFtpPass.getText().toString());
//                editor.putString("FtpServerUser", etFtpUser.getText().toString());
//                editor.putString("FtpServerPort", etFtpPort.getText().toString());
//
//                ServerDetails.getInstance(etFtpServer.getText().toString(), Integer.parseInt(etFtpUser.getText().toString()));
//                editor.commit();
//            }
//        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK && resultData != null) {
            DocumentFile pickedDir = DocumentFile.fromTreeUri(getActivity(), resultData.getData());
            int LOCATION_SDPATH = 1;
            int LOCATION_PHOTO_PATH = 2;
            if (requestCode == LOCATION_PHOTO_PATH) {
                settingPathEditor.putString("PhotoPath", resultData.getDataString());
                settingPathEditor.putString("PhotoPathName", pickedDir.getName());
                settingPathEditor.commit();
            } else if (requestCode == LOCATION_SDPATH) {
                settingPathEditor.putString("SDPath", resultData.getDataString());
                settingPathEditor.putString("SDPathName", pickedDir.getName());
                settingPathEditor.commit();
            }
            isSelectPath = true;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ftpServerLayoutMain:
                if (view.findViewById(R.id.ftpServerLayout).getVisibility() == View.GONE) {
                    view.findViewById(R.id.ftpServerLayout).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.ftpServerLayout).setVisibility(View.GONE);
                }
                break;
            case R.id.fontLayoutMain:
                if (view.findViewById(R.id.fontLayout).getVisibility() == View.GONE) {
                    view.findViewById(R.id.fontLayout).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.fontLayout).setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        if (item.getItemId() == R.id.SettingsSave) {
            changeServerData();
            changeFontSize();
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeServerData() {
        // Меняем данные сервера
        editor.putString("FtpPhotoSrv", etFtpServer.getText().toString());
        editor.putString("FtpPhotoPass", etFtpPass.getText().toString());
        editor.putString("FtpPhotoUser", etFtpUser.getText().toString());
        editor.putInt("FtpPhotoPort", Integer.parseInt(etFtpPort.getText().toString()));

        editor.putString("FtpServerHost", etFtpServer.getText().toString());
        editor.putString("FtpServerPass", etFtpPass.getText().toString());
        editor.putString("FtpServerUser", etFtpUser.getText().toString());
        editor.putInt("FtpServerPort", Integer.parseInt(etFtpPort.getText().toString()));

        ServerDetails.getInstance(etFtpServer.getText().toString(), etFtpPort.getText().toString());
        editor.commit();
    }

    private void changeFontSize() {
        // Меняем шрифт в приложении
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

        Toast.makeText(getActivity(), "Размер шрифта изменён на " + nomenDescriptionFontSize, Toast.LENGTH_SHORT).show();
    }
}
