package com.amber.armtp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.linuxense.javadbf.DBFException;

import java.util.Objects;

public class SendOrdersFragment extends Fragment {
    public SendOrdersFragment() {
    }

    public GlobalVars glbVars;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.send_orders_fragment, container, false);
        setHasOptionsMenu(true);
        glbVars.view = rootView;
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.send_orders_menu, menu);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionSendOrders) {
            if (glbVars.isNetworkAvailable()) {
                try {
                    glbVars.SendOrders();
                } catch (DBFException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), "Нет доступного инетрнет соединения. Проверьте соединение с Интернетом", Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);
        android.support.v7.widget.Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle("");
        glbVars.orderList = getActivity().findViewById(R.id.listSMS);
        glbVars.LoadOrdersForSend();
    }

}
