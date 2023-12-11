package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.amber.armtp.Config;
import com.amber.armtp.GlobalVars;
import com.amber.armtp.R;
import com.amber.armtp.adapters.NomenAdapterSQLite;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.interfaces.TBUpdate;

import java.util.Objects;

/**
 * Updated by domster704 on 27.09.2021
 */
public class ViewOrderFragment extends Fragment implements TBUpdate {
    public GlobalVars glbVars;
    View thisView;
    public GridView nomenList;
    public Cursor myNom;
    public NomenAdapterSQLite PreviewZakazAdapter;

    private DBHelper db;

    public ViewOrderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_order_fragment, container, false);
        setHasOptionsMenu(true);
        thisView = rootView;
        glbVars.CurView = rootView;
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        glbVars = (GlobalVars) Objects.requireNonNull(getActivity()).getApplicationContext();
        glbVars.setContext(getActivity().getApplicationContext());
        GlobalVars.CurFragmentContext = getActivity();
        GlobalVars.CurAc = getActivity();

        Config.hideKeyBoard();
    }

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        System.out.println(1);

        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);
        setContrAndSum(glbVars);

        glbVars.nomenList = getActivity().findViewById(R.id.listContrs);
        PreviewOrder();
        glbVars.fragManager = getActivity().getSupportFragmentManager();

        db = new DBHelper(getActivity().getApplicationContext());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.view_order_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.FormOrderID) {
            Fragment fragment = new FormOrderFragment();
            FragmentTransaction fragmentTransaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment, "frag_form_order");
            fragmentTransaction.commit();
//            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void PreviewOrder() {
        getActivity().runOnUiThread(() -> {
            nomenList.setAdapter(null);
            if (myNom != null) {
                myNom.close();
            }
            myNom = db.getOrderNom();
            PreviewZakazAdapter = new NomenAdapterSQLite(getContext(), R.layout.nomen_layout_preview, myNom, new String[]{"_id", "KOD5", "DESCR", "OST", "PRICE", "ZAKAZ", "GRUPPA", "SGI", "GOFRA", "MP"}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomPrice, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomVkorob, R.id.ColNomMP}, 0);
            nomenList.setAdapter(PreviewZakazAdapter);
//        nomenList.setOnItemClickListener(GridNomenClick);
//        nomenList.setOnItemLongClickListener(PreviewNomenLongClick);
        });
    }
}
