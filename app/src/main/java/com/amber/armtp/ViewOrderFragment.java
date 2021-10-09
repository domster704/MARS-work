package com.amber.armtp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

public class ViewOrderFragment extends Fragment {
    public GlobalVars glbVars;
    View thisView;

    public ViewOrderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_order_fragment, container, false);
        setHasOptionsMenu(true);
        thisView = rootView;
        glbVars.view = rootView;
        return rootView;
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
        android.support.v7.widget.Toolbar toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);

        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);
        String ToolBarContr = glbVars.db.GetToolbarContr();
        String OrderSum = glbVars.db.getOrderSum();
        try {
            toolbar.setSubtitle(ToolBarContr + OrderSum.substring(2) + " руб.");
        } catch (Exception ignored) {
        }
        glbVars.nomenList = getActivity().findViewById(R.id.listContrs);
        glbVars.PreviewZakaz();
        glbVars.fragManager = getActivity().getSupportFragmentManager();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.view_order_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        if (item.getItemId() == R.id.FormOrderID) {
            Fragment fragment = new FormOrderFragment();
            FragmentTransaction fragmentTransaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment, "frag_form_order");
            fragmentTransaction.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
