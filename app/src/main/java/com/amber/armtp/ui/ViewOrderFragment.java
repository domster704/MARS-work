package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupMenu;

import com.amber.armtp.extra.Config;
import com.amber.armtp.R;
import com.amber.armtp.adapters.NomenAdapterSQLite;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.interfaces.TBUpdate;

import java.util.Objects;

/**
 * Updated by domster704 on 27.09.2021
 */
public class ViewOrderFragment extends NomenOrderFragment implements TBUpdate {

    public ViewOrderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_order_fragment, container, false);
        setHasOptionsMenu(true);
        this.rootView = rootView;
        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Config.hideKeyBoard(getActivity());
    }

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbar = getActivity().findViewById(R.id.toolbar);
        db = new DBHelper(getActivity().getApplicationContext());

        nomenList = getActivity().findViewById(R.id.listContrs);
        PreviewOrder();

        setContrAndSumValue(db, toolbar, FormOrderFragment.isSales);
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
            PreviewZakazAdapter.setDbHelper(db);
            PreviewZakazAdapter.setPhotoLongClick(PhotoLongClick);
            nomenList.setAdapter(PreviewZakazAdapter);
            nomenList.setOnItemClickListener(GridNomenClick);
            nomenList.setOnItemLongClickListener(PreviewNomenLongClick);
        });
    }

    public AdapterView.OnItemLongClickListener PreviewNomenLongClick = (arg0, myView, position, arg3) -> {
        try {
            Cursor c = myNom;
            final String group = c.getString(c.getColumnIndex("GRUPPA"));
            final String sgi = c.getString(c.getColumnIndex("SGI"));

            PopupMenu nomPopupMenu = new PopupMenu(getActivity(), myView);
            nomPopupMenu.getMenuInflater().inflate(R.menu.nomen_context_menu, nomPopupMenu.getMenu());
            nomPopupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.goToGroup) {
                    isNeededToSelectRowAfterGoToGroup = true;
                    kod5 = c.getString(c.getColumnIndex("KOD5"));

                    Fragment fragment = new FormOrderFragment();

                    Bundle args = new Bundle();
                    args.putString("SGI", sgi);
                    args.putString("Group", group);

                    fragment.setArguments(args);
                    android.support.v4.app.FragmentManager fragManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fragment, "frag_order_header");
                    fragmentTransaction.commit();

                    toolbar.setTitle("Формирование заказа");

                    return true;
                }
                return true;
            });
            nomPopupMenu.show();
        } catch (Exception e) {
            e.printStackTrace();
            Config.sout(e, getContext());
        }

        return true;
    };
}
