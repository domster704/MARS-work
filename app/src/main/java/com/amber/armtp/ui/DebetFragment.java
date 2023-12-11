package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.amber.armtp.extra.Config;
import com.amber.armtp.R;
import com.amber.armtp.adapters.DebetAdapterSQLite;
import com.amber.armtp.dbHelpers.DBHelper;

import java.util.Objects;

/**
 * Updated by domster704 on 27.09.2021
 */
public class DebetFragment extends Fragment {
    private GridView debetList;
    private Menu mainMenu;
    private DBHelper db;
    public Cursor curDebet;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.debet_fragment, container, false);
        Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        db = new DBHelper(getActivity().getApplicationContext());

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
        debetList = getActivity().findViewById(R.id.listContrs);
        LoadDebet(tradeRepresentativeID);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.debet_menu, menu);
        mainMenu = menu;
    }

    public void LoadDebet(final String TP_ID) {
        getActivity().runOnUiThread(() -> {
            curDebet = db.getDebet(TP_ID);
            debetList.setAdapter(null);
            DebetAdapterSQLite adapter = new DebetAdapterSQLite(getActivity(), R.layout.debet_layout, curDebet, new String[]{"DESCR", "STATUS", "KREDIT", "SALDO", "A7", "A14", "A21", "A28", "A35", "A42", "A49", "A56", "A63", "A64", "OTG30", "OPL30", "KOB", "FIRMA", "CRT_DATE"}, new int[]{R.id.ColDebetContr, R.id.ColDebetStatus, R.id.ColDebetCredit, R.id.ColDebetDolg, R.id.ColDebetA7, R.id.ColDebetA14, R.id.ColDebetA21, R.id.ColDebetA28, R.id.ColDebetA35, R.id.ColDebetA42, R.id.ColDebetA49, R.id.ColDebetA56, R.id.ColDebetA63, R.id.ColDebetA64, R.id.ColDebetOTG30, R.id.ColDebetOPL30, R.id.ColDebetKOB, R.id.ColDebetFirma, R.id.ColDebetDogovor}, 0);
            debetList.setAdapter(adapter);
        });
    }
}
