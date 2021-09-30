package com.amber.armtp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class DefaultFragment extends Fragment {
    public GlobalVars glbVars;
    public WebView webview;
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
        android.support.v7.widget.Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle("");
        settings = getActivity().getSharedPreferences("apk_version", 0);
        editor = settings.edit();
        webview = getActivity().findViewById(R.id.ChangeWebView);
        webview.loadUrl("file:///android_asset/changelog/changes.html");
    }
}