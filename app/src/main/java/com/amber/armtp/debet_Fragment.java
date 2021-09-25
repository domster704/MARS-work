package com.amber.armtp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

public class debet_Fragment extends Fragment {
    public GlobalVars glbVars;

    SearchView searchView;
    MenuItem searchItem;
    Menu mainMenu;

    SharedPreferences settings;
    SharedPreferences.Editor editor;
    String DebTP_ID;

    private android.support.v7.widget.Toolbar toolbar;
    android.support.v4.app.Fragment fragment = null;
    android.support.v4.app.FragmentTransaction fragmentTransaction;

	public debet_Fragment(){}
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.debet_fragment, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setHasOptionsMenu(true);
        glbVars.view = rootView;
        return rootView;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
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
        glbVars.debetList = getActivity().findViewById(R.id.listContrs);
        glbVars.btFilter = getActivity().findViewById(R.id.btFilterDebet);
        glbVars.btClearFilter = getActivity().findViewById(R.id.btClearDebetFilter);
        glbVars.spContrDeb = getActivity().findViewById(R.id.spContr);
        glbVars.spTP = getActivity().findViewById(R.id.spTorgPred);

        glbVars.LoadContrListDeb();

        glbVars.LoadTpListDeb();

        settings = getActivity().getSharedPreferences("apk_version", 0);
        editor = settings.edit();

        DebTP_ID = settings.getString("debet_tp", "");

        int DebRPRowid = glbVars.db.GetTPByID(DebTP_ID);
        SetSelectedDebTP(DebRPRowid);

        if (glbVars.DebetContr!=null && !glbVars.DebetContr.equals("")){
            glbVars.LoadDebetByContr(glbVars.DebetContr);
            glbVars.SetSelectedContr(glbVars.DebetContr);
        }

        glbVars.btFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                glbVars.tvContr = getActivity().findViewById(R.id.ColContrID);
                glbVars.tvTP = getActivity().findViewById(R.id.ColTPID);
                String DebetContr = glbVars.tvContr.getText().toString();
                String DebetTp = glbVars.tvTP.getText().toString();
                glbVars.LoadDebet(DebetTp, DebetContr);
            }
        });

       glbVars.spTP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
                String ItemID = glbVars.curDebetTp.getString(glbVars.curDebetTp.getColumnIndex("ID"));
                settings = getActivity().getSharedPreferences("apk_version", 0);
                editor = settings.edit();
                editor.putString("debet_tp", ItemID);
                editor.commit();
                glbVars.CurrentDebTP = ItemID;
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        glbVars.btClearFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                glbVars.spContrDeb.setSelection(0);
                glbVars.spTP.setSelection(0);
                glbVars.debetList.setAdapter(null);
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.debet_menu, menu);
        mainMenu = menu;

        searchItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Поиск по дебиторке");
        searchView.setOnQueryTextListener(searchTextListner);
    }

    private final SearchView.OnQueryTextListener searchTextListner =
            new SearchView.OnQueryTextListener() {
                boolean isSearchClicked = false;
                @Override
                public boolean onQueryTextChange(String newText) {
//                    String ItemID = glbVars.myGrups.getString(glbVars.myGrups.getColumnIndex("ID"));
//                    if (newText.equals("")) {
//                        if (!isSearchClicked){
//                            glbVars.LoadNom(ItemID);
//                            searchView.clearFocus();
//                            searchView.setIconified(true);
//                        }
//                        return true;
//                    } else {
//                        if (newText.length()>=1){
//                            if (!ItemID.equals("0")){
//                                glbVars.SearchNomInGroup(newText, ItemID);
//                                return true;
//                            } else {
//                                glbVars.LoadNom(ItemID);
//                                return true;
//                            }
//                        }
//                        else {
//                            return false;
//                        }
//                    }
//
                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!query.equals("")) {
                        glbVars.SearchDebet(query);
                        isSearchClicked = true;
                        searchView.clearFocus();
                        searchView.setIconified(true);
                        return true;
                    } else {
                        return false;
                    }
                }
            };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.returnToOrderHead:
                fragment = new order_head_Fragment();
                if (fragment != null) {
                    fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fragment, "frag_view_order");
                    fragmentTransaction.commit();
                    toolbar.setTitle("Шапка заказа");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
    }

    public void SetSelectedDebTP(int ROWID){
        for (int i = 0; i < glbVars.spTP.getCount(); i++) {
            Cursor value = (Cursor) glbVars.spTP.getItemAtPosition(i);
            int id = value.getInt(value.getColumnIndexOrThrow("_id"));
            if (ROWID==id) {
                glbVars.spTP.setSelection(i);
                break;
            }
        }
    }
}
