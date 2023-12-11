package com.amber.armtp;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.amber.armtp.adapters.NomenAdapterSQLite;
import com.amber.armtp.annotations.AsyncUI;
import com.amber.armtp.annotations.DelayedCalled;
import com.amber.armtp.dbHelpers.DBAppHelper;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.dbHelpers.DBOrdersHelper;
import com.amber.armtp.extra.ProgressBarShower;
import com.amber.armtp.interfaces.BackupServerConnection;
import com.amber.armtp.interfaces.TBUpdate;
import com.amber.armtp.ui.OrderHeadFragment;
import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

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
    public Cursor Orders, OrdersDt;
    public Cursor myContr;
    public Cursor myAddress;
    public Cursor myTP;
    public Cursor curDebet;

    public NomenAdapterSQLite PreviewZakazAdapter;
    public JournalAdapter OrdersAdapter;
    public JournalDetailsAdapter OrdersDtAdapter;

    public View CurView;

    public DBHelper db;
    public DBOrdersHelper dbOrders;
    public DBAppHelper dbApp;

    public boolean isMultiSelect = false;
    public boolean isSales = false;

    public GridView nomenList;
    public GridView gdOrders;
    public GridView orderDtList;
    public GridView debetList;

    //    public android.support.v7.widget.Toolbar toolbar;
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
    public ViewFlipper viewFlipper;
    public String ordStatus;

    public static Thread downloadPhotoTread;

    private static boolean isNeededToSelectRowAfterGoToGroup = false;
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

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) glbContext.getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            return true;
        }
        Config.sout("Нет доступа к интернету");
        return false;
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

    public static java.util.Date StrToDbfDate(String Date) {
        java.util.Date return_date = null;
        String Year, Mon, Day;
        Year = Date.substring(6, 10);
        Mon = Date.substring(3, 5);
        Day = Date.substring(0, 2);

        String date = Year + Mon + Day;
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            return_date = formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return return_date;
    }

    public String CreateDBFForSending(int ID) throws DBFException {
        Cursor c;

        String TP, CONTR, ADDR, DOCNO = "", COMMENT, NOMEN;
        java.util.Date DELIVERY, DOCDATE;
        double QTY;
        String PRICE;

        c = dbOrders.getReadableDatabase().rawQuery("SELECT TP, CONTR, ADDR, ZAKAZY.DOCID as DOCID, ZAKAZY.DOC_DATE as DOC_DATE, ZAKAZY.DELIVERY_DATE as DEL_DATE, ZAKAZY.COMMENT as COMMENT, ZAKAZY_DT.NOMEN as NOMEN, ZAKAZY_DT.DESCR as DES, ZAKAZY_DT.QTY as QTY, ZAKAZY_DT.PRICE as PRICE FROM ZAKAZY JOIN ZAKAZY_DT ON ZAKAZY.DOCID = ZAKAZY_DT.ZAKAZ_ID WHERE ZAKAZY.ROWID='" + ID + "'", null);
        if (c.getCount() == 0) {
            Config.sout("В таблице заказов нет записей для отправки");
            return "";
        }

        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("ddMMyyyy_HHmmss");
        String curdate = df.format(Calendar.getInstance().getTimeInMillis()) + Calendar.getInstance().get(Calendar.MILLISECOND);

        Cursor cForTpId = dbOrders.getReadableDatabase().rawQuery("SELECT TP FROM ZAKAZY WHERE rowid='" + ID + "'", null);

        cForTpId.moveToNext();
        String tpID = cForTpId.getString(cForTpId.getColumnIndex("TP"));

        String fileID = tpID + "_" + curdate;
        String FileName = CurAc.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + fileID + ".temp";
        String DBF_FileName = fileID + ".temp";

        File DBFFile = new File(FileName);
        if (DBFFile.exists()) {
            DBFFile.delete();
        }

        DBFWriter Table = new DBFWriter(DBFFile);
        Table.setCharactersetName("cp866");
        DBFField[] fields = new DBFField[10];

        int index = 0;

        fields[index] = new DBFField();
        fields[index].setName("TP");
        fields[index].setDataType(DBFField.FIELD_TYPE_C);
        fields[index].setFieldLength(13);
        index++;

        fields[index] = new DBFField();
        fields[index].setName("CONTR");
        fields[index].setDataType(DBFField.FIELD_TYPE_C);
        fields[index].setFieldLength(13);
        index++;

        fields[index] = new DBFField();
        fields[index].setName("ADDR");
        fields[index].setDataType(DBFField.FIELD_TYPE_C);
        fields[index].setFieldLength(13);
        index++;

        fields[index] = new DBFField();
        fields[index].setName("DOCID");
        fields[index].setDataType(DBFField.FIELD_TYPE_C);
        fields[index].setFieldLength(30);
        index++;

        fields[index] = new DBFField();
        fields[index].setName("DELIVERY");
        fields[index].setDataType(DBFField.FIELD_TYPE_D);
        index++;

        fields[index] = new DBFField();
        fields[index].setName("DOCDATE");
        fields[index].setDataType(DBFField.FIELD_TYPE_D);
        index++;

        fields[index] = new DBFField();
        fields[index].setName("COMMENT");
        fields[index].setDataType(DBFField.FIELD_TYPE_C);
        fields[index].setFieldLength(255);
        index++;

        fields[index] = new DBFField();
        fields[index].setName("NOMEN");
        fields[index].setDataType(DBFField.FIELD_TYPE_C);
        fields[index].setFieldLength(13);
        index++;

        fields[index] = new DBFField();
        fields[index].setName("QTY");
        fields[index].setDataType(DBFField.FIELD_TYPE_N);
        fields[index].setFieldLength(13);
        fields[index].setDecimalCount(0);
        index++;

        fields[index] = new DBFField();
        fields[index].setName("PRICE");
        fields[index].setDataType(DBFField.FIELD_TYPE_C);
        fields[index].setFieldLength(15);
        Table.setFields(fields);

        try {
            while (c.moveToNext()) {
//                TP, CONTR, ADDR, ZAKAZY.DOCID, ZAKAZY.DOC_DATE, ZAKAZY.COMMENT, ZAKAZY_DT.NOMEN, ZAKAZY_DT.DESCR, ZAKAZY_DT.QTY, ZAKAZY_DT.PRICE
                TP = c.getString(c.getColumnIndex("TP"));
                CONTR = c.getString(c.getColumnIndex("CONTR"));
                ADDR = c.getString(c.getColumnIndex("ADDR"));
                DOCNO = c.getString(c.getColumnIndex("DOCID"));
                DOCDATE = StrToDbfDate(c.getString(c.getColumnIndex("DOC_DATE")));
                DELIVERY = StrToDbfDate(c.getString(c.getColumnIndex("DEL_DATE")));
                COMMENT = c.getString(c.getColumnIndex("COMMENT"));
                NOMEN = c.getString(c.getColumnIndex("NOMEN"));
                QTY = c.getDouble(c.getColumnIndex("QTY"));
                PRICE = c.getString(c.getColumnIndex("PRICE")).replace(",", ".");

                Object[] rowData = new Object[10];
                rowData[0] = TP;
                rowData[1] = CONTR;
                rowData[2] = ADDR;
                rowData[3] = DOCNO;
                rowData[4] = DELIVERY;
                rowData[5] = DOCDATE;
                rowData[6] = COMMENT;
                rowData[7] = NOMEN;
                rowData[8] = QTY;
                rowData[9] = PRICE;
                Table.addRecord(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
        System.out.println(DOCNO);
        Table.write();

        return DBF_FileName;
    }

    @AsyncUI
    public void LoadOrders() {
        try {
            Orders = dbOrders.getZakazy();
            if (Orders != null)
                putCheckBox(Orders);
            OrdersAdapter = new JournalAdapter(CurAc, R.layout.orders_item, Orders, new String[]{"DOCID", "STATUS", "DOC_DATE", "DELIVERY", "CONTR", "ADDR", "SUM", "COMMENT"}, new int[]{R.id.ColOrdDocNo, R.id.ColOrdStatus, R.id.ColOrdDocDate, R.id.ColOrdDeliveryDate, R.id.ColOrdContr, R.id.ColOrdAddr, R.id.ColOrdSum, R.id.ColOrdComment}, 0);
            gdOrders.setAdapter(OrdersAdapter);
//            gdOrders.setOnItemClickListener(showOrderDataItemClickListener);
        } catch (Exception e) {
            Config.sout(e, Toast.LENGTH_LONG);
        }
    }

    @AsyncUI
    public void LoadOrdersDetails(String ZakazID) {
        _doUpdateQTYByOuted(ZakazID);

        GlobalVars.allOrders.clear();
        layout.removeAllViews();

        orderDtList.setAdapter(null);
        OrdersDt = dbOrders.getZakazDetails(ZakazID);
        OrdersDtAdapter = new JournalDetailsAdapter(CurAc, R.layout.orderdt_item, OrdersDt, new String[]{"ZAKAZ_ID", "NOMEN", "DESCR", "QTY", "PRICE", "SUM"}, new int[]{R.id.ColOrdDtZakazID, R.id.ColOrdDtCod, R.id.ColOrdDtDescr, R.id.ColOrdDtQty, R.id.ColOrdDtPrice, R.id.ColOrdDtSum}, 0);
        orderDtList.setAdapter(OrdersDtAdapter);
//        orderDtList.setOnItemClickListener(OrderDtNomenClick);
    }

    public String ReadLastUpdate() {
        String timeUpdate = db.getLastUpdateTime();
        return timeUpdate != null ? timeUpdate : "";
    }

    @AsyncUI
    public void LoadDebet(final String TP_ID) {
        curDebet = db.getDebet(TP_ID);
        debetList.setAdapter(null);
        DebetAdapter adapter = new DebetAdapter(CurAc, R.layout.debet_layout, curDebet, new String[]{"DESCR", "STATUS", "KREDIT", "SALDO", "A7", "A14", "A21", "A28", "A35", "A42", "A49", "A56", "A63", "A64", "OTG30", "OPL30", "KOB", "FIRMA", "CRT_DATE"}, new int[]{R.id.ColDebetContr, R.id.ColDebetStatus, R.id.ColDebetCredit, R.id.ColDebetDolg, R.id.ColDebetA7, R.id.ColDebetA14, R.id.ColDebetA21, R.id.ColDebetA28, R.id.ColDebetA35, R.id.ColDebetA42, R.id.ColDebetA49, R.id.ColDebetA56, R.id.ColDebetA63, R.id.ColDebetA64, R.id.ColDebetOTG30, R.id.ColDebetOPL30, R.id.ColDebetKOB, R.id.ColDebetFirma, R.id.ColDebetDogovor}, 0);
        debetList.setAdapter(adapter);
    }

    public String getPhotoDir() {
        String photo_dir;
        File file = CurAc.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        File extPhoto, arm_photo = null;
        extPhoto = new File(file.toString());

        if (extPhoto.canWrite()) {
            arm_photo = new File(extPhoto.toString());
            if (!arm_photo.exists()) {
                arm_photo.mkdir();
            }
        }
        photo_dir = arm_photo.toString();
        return photo_dir;
    }

    public void updateOutedPositionInZakazyTable() {
        try {

            SQLiteDatabase sqLiteDatabaseOrders = dbOrders.getReadableDatabase();
            SQLiteDatabase sqLiteDatabase = db.getReadableDatabase();

            sqLiteDatabaseOrders.beginTransaction();
            Cursor ordersId = sqLiteDatabaseOrders.rawQuery("SELECT DOCID FROM ZAKAZY", null);
            while (ordersId.moveToNext()) {
                String orderID = ordersId.getString(0);
                Cursor c = sqLiteDatabase.rawQuery("SELECT NOMEN FROM VYCHERK WHERE DOCID=?", new String[]{orderID});

                if (c.getCount() != 0) {
                    sqLiteDatabaseOrders.execSQL("UPDATE ZAKAZY SET OUTED=1 WHERE DOCID='" + orderID + "'");
                } else {
                    sqLiteDatabaseOrders.execSQL("UPDATE ZAKAZY SET OUTED=0 WHERE DOCID='" + orderID + "'");
                }
                c.close();
            }
            sqLiteDatabaseOrders.setTransactionSuccessful();
            sqLiteDatabaseOrders.endTransaction();
            ordersId.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ChosenOrdersData {
        private int id;
        private boolean isChecked;
        private final String status;

        public ChosenOrdersData(int id, boolean isChecked, String status) {
            this.id = id;
            this.isChecked = isChecked;
            this.status = status;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        public String getStatus() {
            return status;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
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

    public class JournalAdapter extends SimpleCursorAdapter {
        private class ClickData {
            public int visibility;

            public ClickData(int visibility) {
                this.visibility = visibility;
            }
        }

        private HashMap<Integer, ClickData> clickDataMap;

        public JournalAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            clickDataMap = new HashMap<Integer, ClickData>() {
                @Override
                public String toString() {
                    StringBuilder s = new StringBuilder();
                    Set<Integer> a = clickDataMap.keySet();
                    for (int i : a) {
                        s.append(i).append(" ");
                    }
                    return s.toString();
                }
            };
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Cursor cursor = getCursor();
            View view = super.getView(position, convertView, parent);
            LinearLayout layout = view.findViewById(R.id.expandedData);

            if (!clickDataMap.containsKey(position)) {
                layout.setVisibility(View.GONE);
            } else {
                layout.setVisibility(clickDataMap.get(position).visibility);
            }

            ImageView showData = view.findViewById(R.id.showData);
            TextView tvSum = view.findViewById(R.id.ColOrdSum);
            updateTextFormatTo2DecAfterPoint(tvSum);

            if (cursor.getInt(cursor.getColumnIndex("OUTED")) == 1) {
                tvSum.setText(tvSum.getText().toString() + " (-)");
            }

            TextView tvStatus = view.findViewById(R.id.ColOrdStatus);
            if (tvStatus.getText().toString().equals("Сохранён"))
                tvStatus.setTypeface(Typeface.DEFAULT_BOLD);

            if (allOrders.size() != 0 && allOrders.get(position).isChecked) {
                view.setBackgroundColor(getResources().getColor(R.color.gridViewFirstColor));
            } else {
                view.setBackgroundColor(getResources().getColor(R.color.gridViewSecondColor));
            }

            ClickData clickData = new ClickData(View.GONE);
            if (!isNeededToShowEnterIcon()) {
                showData.setVisibility(View.INVISIBLE);
                clickDataMap.put(position, clickData);
            } else {
                showData.setVisibility(View.VISIBLE);
                showData.setOnClickListener(view1 -> {
                    if (layout.getVisibility() == View.GONE) {
                        layout.setVisibility(View.VISIBLE);
                        clickData.visibility = View.VISIBLE;
                    } else {
                        layout.setVisibility(View.GONE);
                    }
                    clickDataMap.put(position, clickData);
                });
            }

            return view;
        }

        private boolean isNeededToShowEnterIcon() {
            Cursor cursor = getCursor();
            String[] data = new String[]{
                    cursor.getString(cursor.getColumnIndex("COMMENT"))
            };
            for (String i : data) {
                if (!i.equals("")) {
                    return true;
                }
            }
            return false;
        }
    }

    public class JournalDetailsAdapter extends SimpleCursorAdapter {
        public JournalDetailsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            Cursor cursor = getCursor();
            View view = super.getView(position, convertView, parent);
            TextView tvQty = view.findViewById(R.id.ColOrdDtQty);

            TextView tvSum = view.findViewById(R.id.ColOrdDtSum);
            TextView tvPrice = view.findViewById(R.id.ColOrdDtPrice);
            updateTextFormatTo2DecAfterPoint(tvSum);
            updateTextFormatTo2DecAfterPoint(tvPrice);

            if (position % 2 != 0) {
                view.setBackgroundColor(getResources().getColor(R.color.gridViewFirstColor));
            } else {
                view.setBackgroundColor(getResources().getColor(R.color.gridViewSecondColor));
            }

            if (cursor.getInt(cursor.getColumnIndex("IS_OUTED")) == 1) {
                int outQTY = cursor.getInt(cursor.getColumnIndex("OUT_QTY"));
                int QTY = cursor.getInt(cursor.getColumnIndex("QTY"));
                tvQty.setText(QTY + "(" + (QTY - outQTY) + ")");
            }

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

    public class DebetAdapter extends SimpleCursorAdapter {
        public DebetAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView[] tvArray = new TextView[]{
                    view.findViewById(R.id.ColDebetContr),
                    view.findViewById(R.id.ColDebetStatus),
                    view.findViewById(R.id.ColDebetCredit),
                    view.findViewById(R.id.ColDebetA7),
                    view.findViewById(R.id.ColDebetA14),
                    view.findViewById(R.id.ColDebetA21),
                    view.findViewById(R.id.ColDebetA28),
                    view.findViewById(R.id.ColDebetA35),
                    view.findViewById(R.id.ColDebetA42),
                    view.findViewById(R.id.ColDebetA49),
                    view.findViewById(R.id.ColDebetA56),
                    view.findViewById(R.id.ColDebetA63),
                    view.findViewById(R.id.ColDebetA64),
                    view.findViewById(R.id.ColDebetDolg),
                    view.findViewById(R.id.ColDebetOTG30),
                    view.findViewById(R.id.ColDebetOPL30),
                    view.findViewById(R.id.ColDebetKOB),
            };

            if (position % 2 != 0) {
                view.setBackgroundColor(getResources().getColor(R.color.gridViewFirstColor));
            } else {
                view.setBackgroundColor(getResources().getColor(R.color.gridViewSecondColor));
            }

            for (TextView v : tvArray) {
                updateTextFormatTo2DecAfterPoint(v);
            }

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

    private void updateTextFormatTo2DecAfterPoint(TextView v) {
        try {
//            int len = v.getText().toString().split("\\.")[0].length();
//            v.setText(v.getText().toString().substring(0, len + 2));

//            DecimalFormat decimalFormat = new DecimalFormat("#.##");
//            v.setText(decimalFormat.format(v.getText().toString()));

            v.setText(String.format(Locale.ROOT, "%.2f", Float.parseFloat(v.getText().toString().replace(",", "."))));
        } catch (Exception ignored) {
        }
    }

    public void updateOrdersStatusFromDB() {
        SQLiteDatabase dbApp = db.getReadableDatabase();
        SQLiteDatabase dbOrd = dbOrders.getWritableDatabase();
//        dbOrd.beginTransaction();
        Cursor statusInApp = dbOrd.rawQuery("SELECT DOCID FROM ZAKAZY", null);
        Cursor statusInDB;
        while (statusInApp.moveToNext()) {
            String docId = statusInApp.getString(statusInApp.getColumnIndex("DOCID"));
            statusInDB = dbApp.rawQuery("SELECT STATUS FROM STATUS WHERE DOCID = '" + docId + "'", null);
            if (statusInDB.getCount() != 0) {
                statusInDB.moveToNext();
                String Status = statusInDB.getString(statusInDB.getColumnIndex("STATUS"));
                dbOrd.execSQL("UPDATE ZAKAZY SET STATUS = '" + Status + "' WHERE DOCID='" + docId + "'");
            }
            statusInDB.close();
        }

        statusInApp.close();
//        dbOrd.endTransaction();
//        if (statusInDB != null)
//            statusInDB.close();
    }

    private void _doUpdateQTYByOuted(String DocID) {
        SQLiteDatabase dbApp = dbOrders.getWritableDatabase();
        SQLiteDatabase dbVy = db.getReadableDatabase();

        Cursor newQty = dbVy.rawQuery("SELECT NOMEN, KOL FROM VYCHERK WHERE DOCID ='" + DocID + "'", null);
        Cursor cursor = dbApp.rawQuery("SELECT NOMEN, OUT_QTY, IS_OUTED FROM ZAKAZY_DT WHERE ZAKAZ_ID ='" + DocID + "'", null);
        while (cursor.moveToNext()) {
            dbApp.execSQL("UPDATE ZAKAZY_DT SET IS_OUTED = 0, OUT_QTY = 0 WHERE ZAKAZ_ID = '" + DocID + "' AND NOMEN = '" + cursor.getString(0) + "'");
        }

        while (newQty.moveToNext()) {
            int qty = newQty.getInt(newQty.getColumnIndex("KOL"));
            int IS_OUTED = qty != 0 ? 1 : 0;
            dbApp.execSQL("UPDATE ZAKAZY_DT SET IS_OUTED = " + IS_OUTED + ", OUT_QTY = " + qty + " WHERE ZAKAZ_ID = '" + DocID + "' AND NOMEN = '" + newQty.getString(0) + "'");
        }

        newQty.close();
        cursor.close();
    }

    private void putCheckBox(Cursor c) {
        GlobalVars.allOrders.clear();
        layout.removeAllViews();

        if (c.getCount() == 0) return;

        int id;
        String status;
        boolean isChecked;

        while (c.moveToNext()) {
            id = c.getInt(c.getColumnIndex("_id"));
            status = c.getString(c.getColumnIndex("STATUS"));
            isChecked = status.equals("Сохранён");

//            CheckBox checkBox = new CheckBox(layout.getContext());
//            float height = getResources().getDimension(R.dimen.heightOfOrdersItem);
//            checkBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) height));
//            layout.addView(checkBox);

            GlobalVars.allOrders.add(new ChosenOrdersData(id, isChecked, status));
        }
    }

    // Начадл обработчиков событий нажатия на NomenLayout

}
