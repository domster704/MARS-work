package com.amber.armtp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.amber.armtp.annotations.AsyncUI;
import com.amber.armtp.annotations.DelayedCalled;
import com.amber.armtp.auxiliaryData.ChosenOrdersData;
import com.amber.armtp.dbHelpers.DBAppHelper;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.dbHelpers.DBOrdersHelper;
import com.amber.armtp.extra.ProgressBarShower;
import com.amber.armtp.interfaces.BackupServerConnection;
import com.amber.armtp.interfaces.TBUpdate;
import com.amber.armtp.ui.OrderHeadFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by filimonov on 22-08-2016.
 * Updated by domster704 on 27.09.2021
 */
public class GlobalVars extends Application implements TBUpdate, BackupServerConnection {
    public static ArrayList<ChosenOrdersData> allOrders = new ArrayList<>();

    public static FragmentActivity CurAc;
    public static Context CurFragmentContext;
    public static String CurSGI = "0", CurGroup = "0", CurWCID = "0", CurFocusID = "0", CurSearchName = "";
    public static String CurContr = "0";


    public Context glbContext;
    public Cursor myContr;
    public Cursor myAddress;
    public Cursor myTP;
    public View CurView;

    public DBHelper db;
    public DBOrdersHelper dbOrders;
    public DBAppHelper dbApp;

    public boolean isMultiSelect = false;
    public boolean isSales = false;

    public GridView nomenList;
    public GridView gdOrders;
    public GridView orderDtList;

    public Toolbar toolbar;
    public LinearLayout layout;
    public PopupMenu nomPopupMenu;
    public String CurrentTp;

    public Spinner spinContr, spinAddress, TPList;
    public Calendar DeliveryDate;
    public EditText txtDate;
    public EditText edContrFilter;
    public EditText txtComment;
    public TextView spTp, spContr, spAddress;

    public static Thread downloadPhotoTread;
    public static String kod5 = "";

    private final AdapterView.OnItemSelectedListener SelectedContr = new AdapterView.OnItemSelectedListener() {
        private TextView descriptionContr = null;
        private TextView descriptionContrHeader = null;
        private LinearLayout descriptionContrLayout = null;
        private ImageButton contrInfoButton;

        @Override
        public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            init();
            descriptionContr.setText("");

            String[] descriptionContrList = new String[]{"нет данных", "0.00 ₽"};
            String ItemID = myContr.getString(myContr.getColumnIndex("CODE"));
            CurContr = ItemID;
            if (!ItemID.equals("0") && !OrderHeadFragment.isOrderEditedOrCopied) {
                LoadContrAddress(ItemID);
            }

            String[] data = db.getDebetInfoByContrID(ItemID);
            String status = myContr.getString(myContr.getColumnIndex("STATUS"));
            String debt = data[0];

            if (!status.equals("")) {
                descriptionContrList[0] = status;
            }
            if (!debt.equals("")) {
                descriptionContrList[1] = String.format(Locale.ROOT, "%.2f", Float.parseFloat(debt.replace(",", "."))) + " ₽";
            }

            descriptionContr.setText(String.join(" | ", descriptionContrList));

            if (!ItemID.equals("0")) {
                descriptionContrLayout.setVisibility(View.VISIBLE);
                contrInfoButton.setVisibility(View.VISIBLE);
                changeStatusColor(descriptionContr, descriptionContrHeader, ItemID);
            } else {
                descriptionContrLayout.setVisibility(View.GONE);
                contrInfoButton.setVisibility(View.INVISIBLE);
            }
        }

        private void changeStatusColor(TextView tvStatus, TextView tvStatusHeader, String contrId) {
            switch (db.getContrFlagByID(contrId)) {
                case 0:
                    tvStatus.setTextColor(getResources().getColor(R.color.orderStatusOk));
                    tvStatusHeader.setTextColor(getResources().getColor(R.color.orderStatusOk));
                    break;
                case 1:
                    tvStatus.setTextColor(getResources().getColor(R.color.orderStatusWarn));
                    tvStatusHeader.setTextColor(getResources().getColor(R.color.orderStatusWarn));
                    break;
                case 2:
                    tvStatus.setTextColor(getResources().getColor(R.color.orderStatusBad));
                    tvStatusHeader.setTextColor(getResources().getColor(R.color.orderStatusBad));
                    break;
            }
        }

        private void init() {
            descriptionContr = CurAc.findViewById(R.id.description);
            descriptionContrHeader = CurAc.findViewById(R.id.descriptionHeader);
            descriptionContrLayout = CurAc.findViewById(R.id.layoutOfContrStatus);
            contrInfoButton = CurAc.findViewById(R.id.userCardInfoButton);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    public android.support.v4.app.FragmentManager fragManager;
    android.support.v4.app.Fragment fragment = null;
    android.support.v4.app.FragmentTransaction fragmentTransaction;

    public Context getContext() {
        return glbContext;
    }

    public void setContext(Context context) {
        glbContext = context;
    }

    public Boolean CheckTPLock() {
        SharedPreferences settings;
        settings = PreferenceManager.getDefaultSharedPreferences(CurFragmentContext);
        return settings.getBoolean("TP_LOCK", false);
    }

    @AsyncUI
    public void LoadTpList() {
        if (myTP != null) {
            myTP.close();
        }
        myTP = db.getTpList();
        TPAdapter adapter = new TPAdapter(CurAc, R.layout.tp_layout, myTP, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColTPID, R.id.ColTPDescr}, 0);
        TPList.setAdapter(adapter);

        TPList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
                CurrentTp = myTP.getString(myTP.getColumnIndex("CODE"));
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        if (CheckTPLock()) {
            TPList.setEnabled(false);
        }
    }

    @AsyncUI
    public void LoadContrList() {
        spinContr.setAdapter(null);
        if (myContr != null) {
            myContr.close();
        }
        myContr = db.getContrList();
        ContrsAdapter adapter;
        adapter = new ContrsAdapter(CurAc, R.layout.contr_layout, myContr, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
        spinContr.setAdapter(adapter);
        spinContr.setOnItemSelectedListener(SelectedContr);
    }

    @AsyncUI
    public void LoadFilteredContrList(String FindStr) {
        spinContr.setAdapter(null);
        if (myContr != null) {
            myContr.close();
        }
        myContr = db.getContrFilterList(FindStr);
        ContrsAdapter adapter;
        adapter = new ContrsAdapter(CurAc, R.layout.contr_layout, myContr, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
        spinContr.setAdapter(adapter);
        spinContr.setOnItemSelectedListener(SelectedContr);
    }

    @AsyncUI
    public void LoadContrAddress(String ContID) {
        if (myAddress != null) {
            myAddress.close();
        }
        myAddress = db.getContrAddress(ContID);
        AddressAdapter adapter;
        adapter = new AddressAdapter(CurAc, R.layout.addr_layout, myAddress, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrAddrID, R.id.ColContrAddrDescr}, 0);
        spinAddress.setAdapter(adapter);

        String AddrID = db.GetContrAddr();
        if (!AddrID.equals("0")) {
            SetSelectedAddress(AddrID);
        } else if (!OrderHeadFragment._ADDR.equals("")) {
            SetSelectedAddress(OrderHeadFragment._ADDR);
        }
    }

    @DelayedCalled
    public void SetSelectedAddress(String AddrID) {
        for (int i = 0; i < spinAddress.getCount(); i++) {
            Cursor value = (Cursor) spinAddress.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndex("CODE"));
            if (AddrID.equals(id)) {
                spinAddress.setSelection(i, true);
                return;
            }
        }
    }

    public String ReadLastUpdate() {
        String timeUpdate = db.getLastUpdateTime();
        return timeUpdate != null ? timeUpdate : "";
    }

    public class AddressAdapter extends SimpleCursorAdapter {
        public AddressAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Cursor cursor = getCursor();
            TextView tvDescr = view.findViewById(R.id.ColContrAddrDescr);

            if (position % 2 != 0) {
                tvDescr.setBackgroundColor(getResources().getColor(R.color.gridViewFirstColor));
            } else {
                tvDescr.setBackgroundColor(getResources().getColor(R.color.gridViewSecondColor));
            }
            tvDescr.setText(cursor.getString(2));

            return view;
        }
    }

    public class TPAdapter extends SimpleCursorAdapter {
        public TPAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flag) {
            super(context, layout, c, from, to, flag);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            TextView tvDescr = view.findViewById(R.id.ColTPDescr);
            if (position % 2 != 0) {
                tvDescr.setBackgroundColor(getResources().getColor(R.color.gridViewFirstColor));
            } else {
                tvDescr.setBackgroundColor(getResources().getColor(R.color.gridViewSecondColor));
            }

            return view;
        }
    }

    public class ContrsAdapter extends SimpleCursorAdapter {
        public ContrsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Cursor cursor = getCursor();
            String resDescr = cursor.getString(cursor.getColumnIndex("DESCR"));
//            + "\t\t" + cursor.getString(cursor.getColumnIndex("STATUS"));

            TextView tvDescr = view.findViewById(R.id.ColContrDescr);
//            if (position % 2 != 0) {
//                tvDescr.setBackgroundColor(getResources().getColor(R.color.gridViewFirstColor));
//            } else {
//                tvDescr.setBackgroundColor(getResources().getColor(R.color.gridViewSecondColor));
//            }

            tvDescr.setText(resDescr);
            return view;
        }
    }

    public void updateNomenPrice(boolean isCopied) {
        new Thread(() -> {
            new ProgressBarShower(getContext()).setFunction(() -> {
                db.ResetNomenPrice(isCopied);
                return null;
            }).start();
        });
    }

    public static String[] getCurrentData() {
        return new String[]{CurSGI, CurGroup, CurWCID, CurFocusID, CurSearchName};
    }

    public void closeAllCursorsInHeadOrder() {
        if (myAddress != null) {
            myAddress.close();
        }
        if (myContr != null) {
            myContr.close();
        }
        if (myTP != null) {
            myTP.close();
        }
    }

}
