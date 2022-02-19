package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.amber.armtp.GlobalVars;
import com.amber.armtp.R;

import java.util.Objects;

/**
 * Updated by domster704 on 27.09.2021
 */
public class DebetFragment extends Fragment {
    public GlobalVars glbVars;

    Menu mainMenu;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    String DebTP_ID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.debet_fragment, container, false);
        Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setHasOptionsMenu(true);
        glbVars.view = rootView;
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glbVars = (GlobalVars) Objects.requireNonNull(getActivity()).getApplicationContext();
        glbVars.setContext(getActivity().getApplicationContext());
        GlobalVars.CurFragmentContext = getActivity();
        GlobalVars.CurAc = getActivity();
    }

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        glbVars.toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
        android.support.v7.widget.Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle("");
        glbVars.debetList = getActivity().findViewById(R.id.listContrs);

        glbVars.debetList = getActivity().findViewById(R.id.listContrs);
        glbVars.spTP = getActivity().findViewById(R.id.spTorgPred);

        glbVars.LoadTpListDeb();

        settings = getActivity().getSharedPreferences("apk_version", 0);
        editor = settings.edit();

        DebTP_ID = settings.getString("debet_tp", "");

        int DebRPRowid = glbVars.db.GetTPByID(DebTP_ID);
        SetSelectedDebTP(DebRPRowid);

        settings = getActivity().getSharedPreferences("apk_version", 0);
        editor = settings.edit();

        glbVars.spTP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
                String ItemID = glbVars.curDebetTp.getString(glbVars.curDebetTp.getColumnIndex("CODE"));
                settings = Objects.requireNonNull(getActivity()).getSharedPreferences("apk_version", 0);
                editor = settings.edit();
                editor.putString("debet_tp", ItemID);
                editor.commit();
                glbVars.CurrentDebTP = ItemID;

                glbVars.tvContr = Objects.requireNonNull(getActivity()).findViewById(R.id.ColContrID);
                glbVars.tvTP = getActivity().findViewById(R.id.ColTPID);
                String DebetTp = glbVars.tvTP.getText().toString();
                glbVars.LoadDebet(DebetTp);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.debet_menu, menu);
        mainMenu = menu;
    }

    public void SetSelectedDebTP(int ROWID) {
        for (int i = 0; i < glbVars.spTP.getCount(); i++) {
            Cursor value = (Cursor) glbVars.spTP.getItemAtPosition(i);
            int id = value.getInt(value.getColumnIndexOrThrow("_id"));
            if (ROWID == id) {
                glbVars.spTP.setSelection(i);
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.clearTP) {
            glbVars.spTP.setSelection(0);
            glbVars.debetList.setAdapter(null);
            return true;
        }
        return true;
    }
}
