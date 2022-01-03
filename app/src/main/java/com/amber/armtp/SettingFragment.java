package com.amber.armtp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.provider.DocumentFile;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Objects;

/**
 * Панель настроек
 *
 * Updated by domster704 on 27.09.2021
 */
public class SettingFragment extends Fragment {

    public static int nomenDescriptionFontSize = 14;

    private final int LOCATION_SDPATH = 1;
    private final int LOCATION_PHOTO_PATH = 2;
    public GlobalVars glbVars;
    SharedPreferences serverSettings, settings;
    SharedPreferences.Editor editor, settingPathEditor, settingsEditor;

    Button btSaveSettings;
    EditText fontSize;

    EditText etFtpPhoto, etFtpPhotoUser, etFtpPhotoPass;
    EditText etFtpServer, etFtpUser, etFtpPass;
    Boolean isSelectPath = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.setting_fragment, container, false);
        glbVars.view = v;
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        glbVars = (GlobalVars) Objects.requireNonNull(getActivity()).getApplicationContext();
        glbVars.setContext(getActivity().getApplicationContext());
        glbVars.frContext = getActivity();
        glbVars.CurAc = getActivity();
    }

    /**
     * Получение элементов фрагмента и изменение их параметров
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
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

        etFtpServer.setText(serverSettings.getString("FtpServerHost", getResources().getString(R.string.host)));
        etFtpPass.setText(serverSettings.getString("FtpServerPass", getResources().getString(R.string.password)));
        etFtpUser.setText(serverSettings.getString("FtpServerUser", getResources().getString(R.string.user)));

        btSaveSettings = getActivity().findViewById(R.id.btSaveSettings);

        nomenDescriptionFontSize = settings.getInt("fontSize", 14);
        fontSize = getActivity().findViewById(R.id.fontSize);
        fontSize.setText(String.valueOf(nomenDescriptionFontSize));
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


        btSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Integer.parseInt(fontSize.getText().toString()) > 24) {
                    Toast.makeText(getActivity(), "Слишком большой размер шрифта", Toast.LENGTH_SHORT).show();
                    return;
                }

                nomenDescriptionFontSize = Integer.parseInt(fontSize.getText().toString());
                settingsEditor.putInt("fontSize", nomenDescriptionFontSize);
                settingsEditor.apply();

                Toast.makeText(getActivity(), "Размер шрифта изменён на " + nomenDescriptionFontSize, Toast.LENGTH_SHORT).show();
            }
        });

        btSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("FtpPhotoSrv", etFtpServer.getText().toString());
                editor.putString("FtpPhotoPass", etFtpPass.getText().toString());
                editor.putString("FtpPhotoUser", etFtpUser.getText().toString());

                editor.putString("FtpServerHost", etFtpServer.getText().toString());
                editor.putString("FtpServerPass", etFtpPass.getText().toString());
                editor.putString("FtpServerUser", etFtpUser.getText().toString());

                editor.commit();
            }
        });
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK && resultData != null) {
            DocumentFile pickedDir = DocumentFile.fromTreeUri(getActivity(), resultData.getData());
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
}

//<map>
//  <string name="AppUpdatePass">103343</string>
//  <string name="FtpPhotoUser">amberftp</string>
//  <string name="sqlLogin">sa</string>
//  <string name="FtpPhotoPass">201002</string>
//  <string name="FtpPhotoSrv">91.208.84.67</string>
//  <string name="UpdateSrv">91.208.84.67</string>
//  <string name="sqlPass">Yjdfz Ptkfylbz.ru</string>
//  <string name="sqlDB">IZH_2015</string>
//  <string name="AppUpdateUser">amberftp</string>
//  <string name="debet_tp">0</string>
//  <string name="sqlPort">1439</string>
//  <string name="AppUpdateSrv">185.201.89.169</string>
//</map>