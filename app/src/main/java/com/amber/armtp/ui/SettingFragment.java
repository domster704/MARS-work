package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.amber.armtp.GlobalVars;
import com.amber.armtp.Config;
import com.amber.armtp.R;
import com.amber.armtp.ServerDetails;
import com.amber.armtp.annotations.Async;

import java.util.Objects;

/**
 * Панель настроек
 * <p>
 * Updated by domster704 on 27.09.2021
 */
public class SettingFragment extends Fragment implements View.OnClickListener {
    public static int nomenDescriptionFontSize = 14;
    public GlobalVars glbVars;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor, settingsEditor;

    private EditText fontSize;
    private EditText etFtpServer, etFtpUser, etFtpPass, etFtpPort;

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
        GlobalVars.CurFragmentContext = getActivity();
        GlobalVars.CurAc = getActivity();
    }

    /**
     * Получение элементов фрагмента и изменение их параметров
     *
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences serverSettings = getActivity().getSharedPreferences("apk_version", 0);
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

        nomenDescriptionFontSize = settings.getInt("fontSize", 14);
        fontSize = getActivity().findViewById(R.id.fontSize);
        fontSize.setText(String.valueOf(nomenDescriptionFontSize));

        getActivity().findViewById(R.id.ftpServerLayoutMain).setOnClickListener(this);
        getActivity().findViewById(R.id.fontLayoutMain).setOnClickListener(this);
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

    @Async
    private void changeServerData() {
        // Меняем данные сервера
        String host = etFtpServer.getText().toString();
        String port = etFtpPort.getText().toString();
        String user = etFtpUser.getText().toString();
        String pass = etFtpPass.getText().toString();

        editor.putString("FtpPhotoSrv", host);
        editor.putString("FtpPhotoPass", pass);
        editor.putString("FtpPhotoUser", user);
        editor.putInt("FtpPhotoPort", Integer.parseInt(port));

        editor.putString("FtpServerHost", host);
        editor.putString("FtpServerPass",  pass);
        editor.putString("FtpServerUser",  user);
        editor.putInt("FtpServerPort", Integer.parseInt(port));

        ServerDetails.updateInstance(host, port, user, pass);
        editor.commit();

        Config.sout("Настройки сохранены");
    }

    /**
     * Пока что изменяет шрифт только в поле DESCR (Наименование) во фрагменте Формирование
     */
    @Async
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

        Config.sout( "Размер шрифта изменён на " + nomenDescriptionFontSize);
    }
}
