package com.amber.armtp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

/**
 * Updated by domster704 on 27.09.2021
 */
public class DefaultFragment extends Fragment {
    public GlobalVars glbVars;
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.default_fragment, container, false);
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

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);
        android.support.v7.widget.Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle("");
        settings = getActivity().getSharedPreferences("apk_version", 0);
        editor = settings.edit();
    }
}