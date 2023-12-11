package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amber.armtp.Config;
import com.amber.armtp.R;

public class NomenOrderFragment extends Fragment {
    private View thisView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_order_fragment, container, false);
        setHasOptionsMenu(true);
        thisView = rootView;
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Config.hideKeyBoard();
    }

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        toolbar = getActivity().findViewById(R.id.toolbar);
//        setContrAndSum(glbVars);
//
//        glbVars.nomenList = getActivity().findViewById(R.id.listContrs);
//        PreviewOrder();
//        glbVars.fragManager = getActivity().getSupportFragmentManager();
//
//        db = new DBHelper(getActivity().getApplicationContext());
    }
}
