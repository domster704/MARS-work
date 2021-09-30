package com.amber.armtp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

/**
 * Класс для поддержания обратной связи;
 * отправка сообщений/комментарий на сервер
 */
public class BackSMSFragment extends Fragment {
    public GlobalVars glbVars;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    private android.support.v7.widget.Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.backsms_fragment, container, false);
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
        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle("");
        settings = getActivity().getSharedPreferences("apk_version", 0);
        editor = settings.edit();
    }
}