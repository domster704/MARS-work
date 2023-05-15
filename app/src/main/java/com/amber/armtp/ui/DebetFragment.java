package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amber.armtp.Config;
import com.amber.armtp.GlobalVars;
import com.amber.armtp.R;
import com.amber.armtp.dbHelpers.DBHelper;

import java.util.Objects;

/**
 * Updated by domster704 on 27.09.2021
 */
public class DebetFragment extends Fragment {
    public GlobalVars glbVars;

    Menu mainMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.debet_fragment, container, false);
        Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setHasOptionsMenu(true);
        glbVars.CurView = rootView;
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

        String tradeRepresentativeID = Config.getTPId(getActivity());
        String tpName = new DBHelper(getActivity()).getNameOfTpById(tradeRepresentativeID);

        if (tradeRepresentativeID.equals("") || tpName.equals("")) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Неправильный идентификатор")
                    .setMessage("Неправильно введен или отсутсвует ID торгового представителя. Попробуйте ввести правильное ID или обновить базу данных")
                    .setCancelable(false)
                    .setPositiveButton("Ввести ID", (dialogInterface, i) -> {
                        SettingFragment fragment = new SettingFragment();
                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();

                        Bundle bundle = new Bundle();
                        bundle.putIntArray("Layouts", new int[]{R.id.reportLayoutsMain});

                        fragment.setArguments(bundle);
                        fragmentTransaction.replace(R.id.frame, fragment);
                        fragmentTransaction.commit();
                    })
                    .setNegativeButton("Обновить БД", ((dialogInterface, i) ->
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.frame, new UpdateDataFragment())
                                    .commit()))
                    .show();
            return;
        }

        toolbar.setSubtitle(tpName);
        glbVars.debetList = getActivity().findViewById(R.id.listContrs);
        glbVars.LoadDebet(tradeRepresentativeID);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.debet_menu, menu);
        mainMenu = menu;
    }
}
