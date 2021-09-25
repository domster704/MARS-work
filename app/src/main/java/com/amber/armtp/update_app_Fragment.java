package com.amber.armtp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class update_app_Fragment extends Fragment{
    private android.support.v7.widget.Toolbar toolbar;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    Button btCheckUpdates, btUpdateApp;
    TextView tvCurVer, tvLastVer, txtNotify;
    public GlobalVars glbVars;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.update_app_fragment,container,false);
        glbVars.view = v;
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        glbVars = (GlobalVars) getActivity().getApplicationContext();
        glbVars.setContext(getActivity().getApplicationContext());
        glbVars.frContext = getActivity();
        glbVars.CurAc = getActivity();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        toolbar =  getActivity().findViewById(R.id.toolbar);
        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);
        btCheckUpdates = getActivity().findViewById(R.id.btCheckUpdates);
        btUpdateApp = getActivity().findViewById(R.id.btUpdateApp);
        tvCurVer = getActivity().findViewById(R.id.txtCurVer);
        tvLastVer = getActivity().findViewById(R.id.txtLastVer);
        txtNotify = getActivity().findViewById(R.id.txtNotification);

        toolbar.setSubtitle("");
        settings = getActivity().getSharedPreferences("apk_version", 0);
        editor = settings.edit();

        final String[] CurVer = glbVars.GetCurrentVersion();
        tvCurVer.setText(String.valueOf(CurVer[0]) + " ( сборка "+CurVer[1]+")");

        btCheckUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (glbVars.isNetworkAvailable()==true){
                    String result[];

                    result = glbVars.GetLastVersion();

                    Integer LastVersion = 0;
                    Integer CurVersion = 0;

                    if (result[0] !=""){
                        LastVersion = Integer.parseInt(result[0]);
                        CurVersion = Integer.parseInt(CurVer[0]);
                    } else {
                        result = glbVars.GetLastVersionLocal();
                        LastVersion = Integer.parseInt(result[0]);
                        CurVersion = Integer.parseInt(CurVer[0]);
                    }

                    Float LastBuild = result[1]==null? 0:Float.parseFloat(result[1]);
                    Float CurBuild =  Float.parseFloat(CurVer[1]);

                    tvLastVer.setText(String.valueOf(LastVersion)+ " ( сборка "+(LastBuild==0? "?":LastBuild)+")");
//                    if (Integer.parseInt(result[0])>=Integer.parseInt(CurVer[0]) && Float.parseFloat(result[1]) >= Float.parseFloat(CurVer[1])) {
                    if (LastVersion>=CurVersion && LastBuild >= CurBuild) {
                        txtNotify.setText("Доступна новая версия программы НЬЮ АРМ v." + LastVersion +" ( сборка "+(LastBuild==0? "?":LastBuild)+").");
                        btUpdateApp.setVisibility(View.VISIBLE);
                    }

                } else {
                    Toast.makeText(getActivity(), "Нет доступного интернет соединения. Проверьте соединение с Интернетом", Toast.LENGTH_LONG).show();
                }
            }
        });

        btUpdateApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (glbVars.db.CheckForUpdates()==0) {
                    if (glbVars.isNetworkAvailable()==true){
                        glbVars.DownloadApp(settings.getString("AppUpdateSrv", getResources().getString(R.string.ftp_update_server)));

                        Intent intent = new Intent(Intent.ACTION_VIEW);
//                        intent.setDataAndType(Uri.fromFile(new File(glbVars.GetSDCardpath() + glbVars.UpdatesFolder + "/app-debug.apk")), "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        intent.setDataAndType(Uri.fromFile(new File(getActivity().getFilesDir()+"/app-debug.apk")), "application/vnd.android.package-archive");
                        intent.setDataAndType(Uri.fromFile(new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+"/app-debug.apk")), "application/vnd.android.package-archive");

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivityForResult(intent,1);

                    } else {
                        Toast.makeText(getActivity(), "Нет доступного инетрнет соединения. Проверьте соединение с Интернетом", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getActivity(), "У вас имеются неотправленные заказы. Перед обновлением их необходимо отправить.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}