package com.amber.armtp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.amber.armtp.annotations.AsyncUI;
import com.amber.armtp.annotations.DelayedCalled;
import com.amber.armtp.annotations.PGShowing;
import com.amber.armtp.dbHelpers.DBAppHelper;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.dbHelpers.DBOrdersHelper;
import com.amber.armtp.interfaces.TBUpdate;
import com.amber.armtp.ui.FormOrderFragment;
import com.amber.armtp.ui.OrderHeadFragment;
import com.amber.armtp.ui.SettingFragment;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;

/**
 * Created by filimonov on 22-08-2016.
 * Updated by domster704 on 27.09.2021
 */
public class GlobalVars extends Application implements TBUpdate {
    public static ArrayList<ChosenOrdersData> allOrders = new ArrayList<>();

    public static FragmentActivity CurAc;
    public static Context CurFragmentContext;
    public static String CurSGI = "0", CurGroup = "0", CurWCID = "0", CurFocusID = "0", CurSearchName = "";
    public static String TypeOfPrice = "";

    public int CurVisiblePosition = 0;

    public Context glbContext;
    public Cursor myNom = null, mySgi, myGrups, Orders, OrdersDt;
    public Cursor curWC = null, curFocus = null;
    public Cursor Contr;
    public Cursor Addr;
    public Cursor TP;
    public Cursor curDebet, curDebetTp;

    public GlobalVars.NomenAdapter NomenAdapter, PreviewZakazAdapter;
    public JournalAdapter OrdersAdapter;
    public JournalDetailsAdapter OrdersDtAdapter;

    public View CurView;

    public DBHelper db;
    public DBOrdersHelper dbOrders;
    public DBAppHelper dbApp;

    public boolean isDiscount = false;
    public boolean isMultiSelect = false;
    public boolean isSecondPhoto = false;
    public boolean isSales = false;
    public boolean isNeededToBeLoadingBySgi = true;

    public float Discount = 0;
    public int MultiQty = 0;

    public GridView nomenList;
    public GridView gdOrders;
    public GridView orderDtList;
    public GridView debetList;

    //    public android.support.v7.widget.Toolbar toolbar;
    public android.support.v7.widget.Toolbar toolbar;
    public LinearLayout layout;

    public Spinner spSgi, spGrup;
    public Spinner spWC, spFocus;

    public File appDBFolder = new File(GetSDCardPath() + "ARMTP_DB");
    public File appPhotoFolder = new File(GetSDCardPath() + "ARM_PHOTO");
    public File appUpdatesFolder = new File(GetSDCardPath() + "ARM_UPDATES");

    public String AsyncFileName;
    public SubsamplingScaleImageView imageView;
    public AlertDialog alertPhoto = null;
    public PopupMenu nomPopupMenu;
    public String OrderID = "";
    public String CurrentTp, CurrentDebTP;

    public Spinner spinContr, spinAddr, TPList;
    public Calendar DeliveryDate;
    public EditText txtDate;
    public EditText edContrFilter;
    public EditText txtComment;
    public TextView spTp, spContr, spAddr;
    public TextView tvContr, tvTP;
    public ViewFlipper viewFlipper;
    public String ordStatus;

    public Spinner spTP;
    public int BeginPos = 0, EndPos = 0;

    public static Thread downloadPhotoTread;
    public boolean isNeededToResetSearchView = true;

    private HashSet<String> listOfOutedDocId;

    private final AdapterView.OnItemSelectedListener SelectedContr = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            String ItemID = Contr.getString(Contr.getColumnIndex("CODE"));
            if (!ItemID.equals("0") && !OrderHeadFragment.isOrderEditedOrCopied) {
                LoadContrAddr(ItemID);
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    public AdapterView.OnItemClickListener GridNomenClick = (myAdapter, myView, position, mylng) -> {
        long viewId = myView.getId();

        if (viewId == R.id.ColNomPhoto) {
            showPhoto(myView, position, myAdapter);
        } else if (viewId == R.id.btPlus) {
            plusQTY(myView);
        } else if (viewId == R.id.btMinus) {
            minusQTY(myView);
        } else {
            TextView tvKOD5 = myView.findViewById(R.id.ColNomCod);
            TextView tvOST = myView.findViewById(R.id.ColNomOst);
            TextView tvNomenCount = myView.findViewById(R.id.ColNomZakaz);

            String ID = tvKOD5.getText().toString();
            int ost = Integer.parseInt(tvOST.getText().toString());
            int count = Integer.parseInt(tvNomenCount.getText().toString());

            if (isMultiSelect) {
                multiSelect(ID, ost);
            } else {
                fillNomenDataFromAlertDialog(ID, ost);
            }
        }
    };

    //    public static boolean isGoneInGroup = false;
    public AdapterView.OnItemLongClickListener GridNomenLongClick = new AdapterView.OnItemLongClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, final View myView, final int position, long arg3) {
            String Grup;
            String Sgi;
            String curSgi = "";
            String curGroup = "";

            TextView txtSgi = getCurView().findViewById(R.id.ColSgiID);
            if (txtSgi != null)
                curSgi = txtSgi.getText().toString();

            TextView txtGroup = getCurView().findViewById(R.id.ColGrupID);
            if (txtGroup != null)
                curGroup = txtGroup.getText().toString();

            Cursor c = myNom;
            Grup = c.getString(c.getColumnIndex("GRUPPA"));
            Sgi = c.getString(c.getColumnIndex("SGI"));

            PopupMenu nomPopupMenu = new PopupMenu(glbContext, myView);
            nomPopupMenu.getMenuInflater().inflate(R.menu.nomen_context_menu, nomPopupMenu.getMenu());
            if (BeginPos != 0) {
                nomPopupMenu.getMenu().findItem(R.id.setBeginPos).setTitle("Установить как начальную позицию. (сейчас установлена " + BeginPos + ")");
            }

            if (EndPos != 0) {
                nomPopupMenu.getMenu().findItem(R.id.setEndPos).setTitle("Установить как конечную позицию. (сейчас установлена " + EndPos + ")");
            }

            nomPopupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.resetBeginEndPos:
                        BeginPos = 0;
                        EndPos = 0;
                        return true;
                    case R.id.setBeginPos:
                        BeginPos = position + 1;
                        return true;
                    case R.id.setEndPos:
                        EndPos = position + 1;
                        return true;
                    case R.id.goToGroup:
                        resetAllSpinners();
                        resetCurData();
                        resetSearchViewData();

                        SetSelectedSgi(Sgi);
                        SetSelectedGrup(Grup);
                        return true;
                }
                return true;
            });
            nomPopupMenu.show();
            return true;
        }
    };
    public AdapterView.OnItemSelectedListener SelectedGroup = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            resetSearchViewData();
            String ItemID = myGrups.getString(myGrups.getColumnIndex("CODE"));
            CurGroup = ItemID;

            if (!ItemID.equals("0")) {
                TextView txtSgi = getCurView().findViewById(R.id.ColSgiID);
                CurSGI = txtSgi.getText().toString();
            }
            if (CurSGI.equals("0"))
                return;

            System.out.println("Group & SGI: " + CurGroup + " " + CurSGI);
            LoadNomen(CurSGI, CurGroup, CurWCID, CurFocusID, CurSearchName);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    public AdapterView.OnItemSelectedListener SelectedSgi = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            if (isNeededToResetSearchView) {
                resetSearchViewData();
            }
            isNeededToResetSearchView = true;
            String ItemID = mySgi.getString(mySgi.getColumnIndex("CODE"));

            CurGroup = "0";
            CurSGI = ItemID;

            if (ItemID.equals("0")) {
                nomenList.setAdapter(null);
                spGrup.setAdapter(null);
            } else {
                LoadGroups(ItemID);
            }

            if (!CurWCID.equals("0") || !CurFocusID.equals("0") || !CurSearchName.equals("")) {
                LoadNomen(CurSGI, CurGroup, CurWCID, CurFocusID, CurSearchName);
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

//    public AdapterView.OnItemClickListener OrderDtNomenClick = new AdapterView.OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {
//
//            if (ordStatus.equals("Отправлен") || ordStatus.equals("Удален")) {
//                Toast.makeText(CurAc, "Данный заказ уже отправлен или удаен и не может быть изменен", Toast.LENGTH_LONG).show();
//                return;
//            }
//
//            TextView c1 = myView.findViewById(R.id.ColOrdDtQty);
//            TextView c3 = myView.findViewById(R.id.ColOrdDtCod);
//            TextView c4 = myView.findViewById(R.id.ColOrdDtDescr);
//            TextView c6 = myView.findViewById(R.id.ColOrdDtZakazID);
//
//            String Zakaz_id = c6.getText().toString();
//
//            LayoutInflater layoutInflater = LayoutInflater.from(CurAc);
//            View promptView = layoutInflater.inflate(R.layout.change_qty, null);
//            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CurAc);
//            alertDialogBuilder.setView(promptView);
//
//            EditText input = promptView.findViewById(R.id.edPPQty);
//            TextView txtCod = promptView.findViewById(R.id.txtNomCode);
//            TextView txtDescr = promptView.findViewById(R.id.txtNomDescr);
//            TextView txtOst = promptView.findViewById(R.id.txtNomOst);
//
//            input.setText(c1.getText());
//            txtCod.setText(c3.getText());
//            txtDescr.setText(c4.getText());
//
//            String ID = txtCod.getText().toString();
//            final String Ost = db.getNomenOst(ID);
//            txtOst.setText(Ost);
//
//            alertDialogBuilder
//                    .setCancelable(true)
//                    .setPositiveButton("OK", (dialog, id) -> {
//                    })
//                    .setNegativeButton("Отмена", (dialog, id) -> dialog.cancel());
//
//            final AlertDialog alertD = alertDialogBuilder.create();
//            alertD.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
//            WindowManager.LayoutParams wmlp = alertD.getWindow().getAttributes();
//            wmlp.gravity = Gravity.TOP | Gravity.START;
//            wmlp.x = 50;
//            wmlp.y = 50;
//
//            alertD.show();
//            input.requestFocus();
//            input.selectAll();
//            input.performClick();
//            input.setPressed(true);
//            input.invalidate();
//            InputMethodManager imm = (InputMethodManager) CurAc.getSystemService(INPUT_METHOD_SERVICE);
//            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
//
//            alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
//                boolean wantToCloseDialog = false;
//                if (Integer.parseInt(input.getText().toString()) <= Integer.parseInt(Ost)) {
//                    int newQuantityOfOrder = Integer.parseInt(input.getText().toString());
//                    dbOrders.updateOrderQty(Zakaz_id, ID, newQuantityOfOrder);
//                    c1.setText(newQuantityOfOrder);
//                    OrdersDt.requery();
//                    wantToCloseDialog = true;
//                } else {
//                    input.selectAll();
//                    input.requestFocus();
//                    input.performClick();
//                    input.setPressed(true);
//                    input.invalidate();
//                    Toast.makeText(CurAc, "На остатках всего " + Ost + "шт. Вы заказываете " + input.getText().toString() + " шт.", Toast.LENGTH_LONG).show();
//                }
//                if (wantToCloseDialog)
//                    alertD.dismiss();
//            });
//
//            input.setOnEditorActionListener((v, actionId, event) -> {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    alertD.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
//                }
//                return true;
//            });
//        }
//    };

    public android.support.v4.app.FragmentManager fragManager;
    TextView grupID, sgiID;
    android.support.v4.app.Fragment fragment = null;
    android.support.v4.app.FragmentTransaction fragmentTransaction;
    public AdapterView.OnItemLongClickListener PreviewNomenLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, final View myView, int position, long arg3) {
            grupID = myView.findViewById(R.id.ColNomGRUPID);
            sgiID = myView.findViewById(R.id.ColNomSGIID);

            Cursor c = myNom;
            final String Grup = c.getString(c.getColumnIndex("GRUPPA"));
            final String Sgi = c.getString(c.getColumnIndex("SGI"));

            nomPopupMenu = new PopupMenu(CurAc, myView);
            nomPopupMenu.getMenuInflater().inflate(R.menu.nomen_context_menu, nomPopupMenu.getMenu());
            nomPopupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.goToGroup) {
                    fragment = new FormOrderFragment();

                    Bundle args = new Bundle();
                    System.out.println(Sgi + " " + Grup);
                    args.putString("SGI", Sgi);
                    args.putString("Group", Grup);

                    fragment.setArguments(args);
                    fragmentTransaction = fragManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fragment, "frag_order_header");
                    fragmentTransaction.commit();

                    toolbar.setTitle("Формирование заказа");

                    return true;
                }
                return true;
            });
            nomPopupMenu.show();
            return true;
        }
    };
    private boolean isNewLoaded = false;

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

    public View getCurView() {
        return CurView;
    }

    public Context getContext() {
        return glbContext;
    }

    public void setContext(Context context) {
        glbContext = context;
    }

    //    @AsyncUI
    public void LoadSgi() {
        mySgi = db.getAllSgi();
        spSgi = CurView.findViewById(R.id.SpinSgi);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.sgi_layout, mySgi, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColSgiID, R.id.ColSgiDescr}, 0);
        spSgi.setAdapter(adapter);
        spSgi.post(() -> spSgi.setOnItemSelectedListener(SelectedSgi));
    }

    //    private boolean isJustSettingAdapter = false;
//    @AsyncUI
    public void LoadGroups(final String SgiID) {
        myGrups = db.getGrupBySgi(SgiID);
        spGrup = CurView.findViewById(R.id.SpinGrups);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.grup_layout, myGrups, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColGrupID, R.id.ColGrupDescr}, 0);

//        isJustSettingAdapter = true;
        spGrup.setAdapter(adapter);
        spGrup.setOnItemSelectedListener(SelectedGroup);
    }

    @AsyncUI
    public void LoadFiltersWC(View vw) {
        curWC = dbApp.getWCs();
        spWC = vw.findViewById(R.id.spinWC);
        android.widget.SimpleCursorAdapter adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.wc_layout, curWC, new String[]{"_id", "DEMP"}, new int[]{R.id.ColWCID, R.id.ColWCDescr}, 0);
        spWC.setAdapter(adapter);
    }

    @AsyncUI
    public void LoadFiltersFocus(View vw) {
        curFocus = db.getFocuses();
        spFocus = vw.findViewById(R.id.spinFocus);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.focus_layout, curFocus, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColFocusID, R.id.ColFocusDescr}, 0);
        spFocus.setAdapter(adapter);
    }

    private GlobalVars.NomenAdapter getNomenAdapter(Cursor cursor) {
        return new NomenAdapter(glbContext, R.layout.nomen_layout, cursor, new String[]{"_id", "KOD5", "DESCR", "OST", "ZAKAZ", "GRUPPA", "SGI", "FOTO", "GOFRA", "MP", GlobalVars.TypeOfPrice}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomPhoto, R.id.ColNomVkorob, R.id.ColNomMP, R.id.ColNomPrice}, 0);
    }

    public void LoadNomen(String... args) {
        String[] formatedArgs = new String[5];
        System.arraycopy(args, 0, formatedArgs, 0, args.length);
        for (int i = args.length; i < formatedArgs.length; i++) {
            formatedArgs[i] = "0";
        }

        CurSGI = formatedArgs[0];
        CurGroup = formatedArgs[1];
        CurWCID = formatedArgs[2];
        CurFocusID = formatedArgs[3];
        CurSearchName = formatedArgs[4].toLowerCase(Locale.ROOT);

        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
//                myNom = db.getNomen(
//                        CurSGI, CurGroup,
//                        CurWCID, CurFocusID, CurSearchName);
//                Config.printCursor(myNom);
                myNom = db.getNomen(
                        CurSGI, CurGroup,
                        CurWCID, CurFocusID, CurSearchName);
                NomenAdapter = getNomenAdapter(myNom);
                CurAc.runOnUiThread(() -> {
                    nomenList.setAdapter(null);
                    nomenList.setAdapter(NomenAdapter);
                    nomenList.setOnItemClickListener(GridNomenClick);
                    nomenList.setOnItemLongClickListener(GridNomenLongClick);

                    // needs to call notifyDataSetChanged in NomenAdapter class
                    NomenAdapter.notifyDataSetChanged();
                });
                isNewLoaded = false;
            }
        }).start();
    }

    @DelayedCalled()
    public void SetSelectedSgi(String SgiID) {
        for (int i = 0; i < spSgi.getCount(); i++) {
            Cursor value = (Cursor) spSgi.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndex("CODE"));
            if (SgiID.equals(id)) {
                spSgi.setSelection(i);
                return;
            }
        }
    }

    @DelayedCalled(delay = 300)
    public void SetSelectedGrup(String Grup) {
        for (int i = 0; i < spGrup.getCount(); i++) {
            Cursor value = (Cursor) spGrup.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndex("CODE"));
            if (Grup.equals(id)) {
                spGrup.setSelection(i);
                return;
            }
        }
    }

    @DelayedCalled
    public void SetSelectedFilterWC(String ID) {
        for (int i = 0; i < spWC.getCount(); i++) {
            Cursor value = (Cursor) spWC.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("_id"));
            if (ID.equals(id)) {
                spWC.setSelection(i);
                break;
            }
        }
    }

    @DelayedCalled
    public void SetSelectedFilterFocus(String ID) {
        for (int i = 0; i < spFocus.getCount(); i++) {
            Cursor value = (Cursor) spFocus.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("CODE"));
            if (ID.equals(id)) {
                spFocus.setSelection(i);
                break;
            }
        }
    }

    public String GetSDCardPath() {
        String sdpath = "";
        if (Build.BRAND.equals("samsung")) {
            sdpath = "/storage/extSdCard/";
        } else {
            if (new File("/storage/extSdCard/").exists()) {
                sdpath = "/storage/extSdCard/";

            }
            if (new File("/storage/sdcard1/").exists()) {
                sdpath = "/storage/sdcard1/";
            }
            if (new File("/storage/usbcard1/").exists()) {
                sdpath = "/storage/usbcard1/";
            }
            if (new File("/storage/sdcard0/").exists()) {
                sdpath = "/storage/sdcard0/";
            }
            if (new File("/storage/sdcard/").exists()) {
                sdpath = "/storage/sdcard/";
            }
            if (new File("/storage/emulated/0/").exists()) {
                sdpath = "/storage/emulated/0/";
            }
        }

        return sdpath;

    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) glbContext.getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            return true;
        }
        Config.sout("Нет доступа к интернету");
        return false;
//        if (activeNetworkInfo == null) {
//            Toast.makeText(glbContext, "Нет доступной сотовой сети", Toast.LENGTH_LONG).show();
//            return false;
//        }
//        if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
//            return activeNetworkInfo.isConnectedOrConnecting();
//        } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
//            switch (activeNetworkInfo.getSubtype()) {
//                case TelephonyManager.NETWORK_TYPE_1xRTT:
//                case TelephonyManager.NETWORK_TYPE_EDGE:
//                    return false; // ~ 50-100 kbps
//                case TelephonyManager.NETWORK_TYPE_CDMA:
//                    return false; // ~ 14-64 kbps
//                case TelephonyManager.NETWORK_TYPE_EVDO_0:
//                    return true; // ~ 400-1000 kbps
//                case TelephonyManager.NETWORK_TYPE_EVDO_A:
//                    return true; // ~ 600-1400 kbps
//                case TelephonyManager.NETWORK_TYPE_GPRS:
//                    return false; // ~ 100 kbps
//                case TelephonyManager.NETWORK_TYPE_HSDPA:
//                    return true; // ~ 2-14 Mbps
//                case TelephonyManager.NETWORK_TYPE_HSPA:
//                    return true; // ~ 700-1700 kbps
//                case TelephonyManager.NETWORK_TYPE_HSUPA:
//                    return true; // ~ 1-23 Mbps
//                case TelephonyManager.NETWORK_TYPE_UMTS:
//                    return true; // ~ 400-7000 kbps
//                /*
//                 * Above API level 7, make sure to set android:targetSdkVersion
//                 * to appropriate level to use these
//                 */
//                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
//                    return true; // ~ 1-2 Mbps
//                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
//                    return true; // ~ 5 Mbps
//                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
//                    return true; // ~ 10-20 Mbps
//                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
//                    return false; // ~25 kbps
//                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
//                    return true; // ~ 10+ Mbps
//                // Unknown
//                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
//                default:
//                    return false;
//            }
//        } else {
//            return false;
//        }
    }

    public void DownloadPhoto(final String FileName) {
        downloadPhotoTread = new Thread(new Runnable() {
            @Override
            @PGShowing(isCanceled = true)
            public void run() {
                SharedPreferences settings;
                String ftp_server, ftp_user, ftp_pass;
                settings = CurAc.getSharedPreferences("apk_version", 0);

                ftp_server = settings.getString("FtpPhotoSrv", getResources().getString(R.string.ftp_server));
                ftp_user = settings.getString("FtpPhotoUser", getResources().getString(R.string.ftp_pass));
                ftp_pass = settings.getString("FtpPhotoPass", getResources().getString(R.string.ftp_user));

                FTPClient ftpClient;
                ftpClient = new FTPClient();
                final String photoDir = getPhotoDir();
                try {
                    ftpClient.connect(ftp_server);

                    ftpClient.login(ftp_user, ftp_pass);

                    ftpClient.changeWorkingDirectory("FOTO");
                    ftpClient.enterLocalPassiveMode();
                    ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                    FileOutputStream fos = new FileOutputStream(photoDir + "/" + FileName);

                    ftpClient.retrieveFile(FileName, fos);
                    fos.close();
                    String isDownloaded = FilenameUtils.removeExtension(FileName);
                    String tmpName = isDownloaded.substring(isDownloaded.length() - 2);

                    try {
                        if (tmpName.equals("_2")) {
                            db.getWritableDatabase().execSQL("UPDATE Nomen SET PD=1 WHERE KOD5='" + isDownloaded.replace(tmpName, "") + "'");
                        } else {
                            db.getWritableDatabase().execSQL("UPDATE Nomen SET PD=1 WHERE KOD5='" + isDownloaded + "'");
                        }
                    } catch (Exception ignored) {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        ftpClient.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (!Thread.currentThread().isInterrupted()) {
                    CurAc.runOnUiThread(() -> {
                        if (!isSecondPhoto) {
                            ShowNomenPhoto(FileName);
                            imageView.invalidate();
                        } else {
                            if (alertPhoto != null) {
                                imageView.setTag("Фото 2");
                                alertPhoto.setTitle(FileName);
                                imageView.setImage(ImageSource.uri(photoDir + "/" + FileName));

                                imageView.invalidate();
                            }
                        }
                        GridView gdNomen = CurView.findViewById(R.id.listContrs);
                        myNom.requery();
                        if (NomenAdapter != null)
                            NomenAdapter.notifyDataSetChanged();
                        gdNomen.invalidateViews();
                    });
                }
            }
        });
        downloadPhotoTread.start();
    }

    // TODO
    public void ShowNomenPhoto(final String PhotoFileName) {
        alertPhoto = null;
        String photoDir = getPhotoDir();

        File imgFile = new File(photoDir + "/" + PhotoFileName);
        File imgFile2 = new File(photoDir + "/" + FilenameUtils.removeExtension(PhotoFileName) + "_2.jpg");
        checkPhotoInDB(PhotoFileName);

        AlertDialog.Builder builder = new AlertDialog.Builder(CurAc);
        LayoutInflater inflater = CurAc.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.image_layout, null));
        builder.setCancelable(true);
        builder.setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        alertPhoto = builder.create();
        alertPhoto.getWindow().setLayout(600, 400);
        alertPhoto.show();

        imageView = alertPhoto.findViewById(R.id.ivPhoto);
        imageView.setMaxScale(3f);
        imageView.setDoubleTapZoomScale(1F);

        imageView.setImage(ImageSource.uri(photoDir + "/" + PhotoFileName));
        imageView.setTag("Фото 1");

        imageView.setOnClickListener(v -> {
            checkPhotoInDB(PhotoFileName);
            String LoadingFile = PhotoFileName;
            if (imageView.getTag().toString().equals("Фото 1")) {
                if (imgFile2.exists() && imgFile2.length() != 0) {
                    imageView.setTag("Фото 2");
                    LoadingFile = FilenameUtils.removeExtension(PhotoFileName) + "_2.jpg";
                    checkPhotoInDB(LoadingFile);
                    alertPhoto.setTitle(LoadingFile);
                }
            } else {
                LoadingFile = PhotoFileName;
                imageView.setTag("Фото 1");
            }
            imageView.setImage(ImageSource.uri(photoDir + "/" + LoadingFile));
        });
    }

    @AsyncUI
    public void PreviewZakaz() {
        nomenList.setAdapter(null);
        myNom = db.getOrderNom();
        PreviewZakazAdapter = new NomenAdapter(glbContext, R.layout.nomen_layout_preview, myNom, new String[]{"_id", "KOD5", "DESCR", "OST", "PRICE", "ZAKAZ", "GRUPPA", "SGI", "GOFRA", "MP"}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomPrice, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomVkorob, R.id.ColNomMP}, 0);
        nomenList.setAdapter(PreviewZakazAdapter);
        nomenList.setOnItemClickListener(GridNomenClick);
        nomenList.setOnItemLongClickListener(PreviewNomenLongClick);
    }

    public Boolean CheckTPLock() {
        SharedPreferences settings;
        settings = PreferenceManager.getDefaultSharedPreferences(CurFragmentContext);
        return settings.getBoolean("TP_LOCK", false);
    }

    @AsyncUI
    public void LoadTpList() {
        TP = db.getTpList();
        TPAdapter adapter = new TPAdapter(CurAc, R.layout.tp_layout, TP, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColTPID, R.id.ColTPDescr}, 0);
        TPList.setAdapter(adapter);

        TPList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
                CurrentTp = TP.getString(TP.getColumnIndex("CODE"));
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
        Contr = db.getContrList();
        ContrsAdapter adapter;
        adapter = new ContrsAdapter(CurAc, R.layout.contr_layout, Contr, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
        spinContr.setAdapter(adapter);
        spinContr.setOnItemSelectedListener(SelectedContr);
    }

    @AsyncUI
    public void LoadFilteredContrList(String FindStr) {
        spinContr.setAdapter(null);
        Contr = db.getContrFilterList(FindStr);
        ContrsAdapter adapter;
        adapter = new ContrsAdapter(CurAc, R.layout.contr_layout, Contr, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
        spinContr.setAdapter(adapter);
        spinContr.setOnItemSelectedListener(SelectedContr);
    }

    @AsyncUI
    public void LoadContrAddr(String ContID) {
        Addr = db.getContrAddress(ContID);
        AddressAdapter adapter;
        adapter = new AddressAdapter(CurAc, R.layout.addr_layout, Addr, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrAddrID, R.id.ColContrAddrDescr}, 0);
        spinAddr.setAdapter(adapter);

        String AddrID = db.GetContrAddr();
        if (!AddrID.equals("0")) {
            SetSelectedAddr(AddrID);
        } else if (!OrderHeadFragment._ADDR.equals("")) {
            SetSelectedAddr(OrderHeadFragment._ADDR);
        }
    }

    @DelayedCalled
    public void SetSelectedAddr(String AddrID) {
        for (int i = 0; i < spinAddr.getCount(); i++) {
            Cursor value = (Cursor) spinAddr.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndex("CODE"));
            if (AddrID.equals(id)) {
                spinAddr.setSelection(i, true);
                return;
            }
        }
    }

    public String CreateDBFForSending(int ID) throws DBFException {
        Cursor c;

        String TP, CONTR, ADDR, DOCNO, COMMENT, NOMEN;
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

        String FileName = CurAc.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + tpID + "_" + curdate + ".temp";
        String DBF_FileName = tpID + "_" + curdate + ".temp";

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
        Table.write();

        return DBF_FileName;
    }

    @AsyncUI
    public void LoadOrders() {
        try {
            Orders = dbOrders.getZakazy();
            if (Orders != null)
                putCheckBox(Orders);
            OrdersAdapter = new JournalAdapter(CurAc, R.layout.orders_item, Orders, new String[]{"DOCID", "STATUS", "DOC_DATE", "DELIVERY", "CONTR", "ADDR", "SUM"}, new int[]{R.id.ColOrdDocNo, R.id.ColOrdStatus, R.id.ColOrdDocDate, R.id.ColOrdDeliveryDate, R.id.ColOrdContr, R.id.ColOrdAddr, R.id.ColOrdSum}, 0);
            gdOrders.setAdapter(OrdersAdapter);
        } catch (Exception e) {
            Config.sout(e.getMessage(), Toast.LENGTH_LONG);
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

    public void LoadTpListDeb() {
        spTP.setAdapter(null);
        curDebetTp = db.getTpList();
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(CurAc, R.layout.tp_layout, curDebetTp, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColTPID, R.id.ColTPDescr}, 0);
        spTP.setAdapter(adapter);
    }

    public void UpdateNomenRange(int beginRange, int endRange, int qty) {
        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                int EndRange = endRange;
                int BeginRange = beginRange;
//                String sql_update = "UPDATE Nomen SET ZAKAZ = CASE WHEN (OST - " + qty + ") >= 0 THEN " + qty + " ELSE OST END WHERE ROWID=?";
                String sql_update = "UPDATE Nomen SET ZAKAZ = " + qty + " WHERE ROWID=?";
                SQLiteStatement stmt = db.getWritableDatabase().compileStatement(sql_update);
                db.getWritableDatabase().beginTransaction();
                int tmpVal;

                if (EndRange > CurVisiblePosition) {
                    EndRange = CurVisiblePosition;
                }

                if (BeginRange > EndRange) {
                    tmpVal = BeginRange;
                    BeginRange = EndRange;
                    EndRange = tmpVal;
                }

                for (int i = BeginRange - 1; i <= EndRange - 1; i++) {
                    stmt.clearBindings();
                    stmt.bindLong(1, NomenAdapter.getItemId(i));
                    stmt.executeUpdateDelete();
                    stmt.clearBindings();
                }

                db.getWritableDatabase().setTransactionSuccessful();
                db.getWritableDatabase().endTransaction();

                for (int i = BeginRange; i <= EndRange; i++) {
                    long pos = NomenAdapter.getItemId(i - 1);

                    SQLiteDatabase sqLiteDatabase = db.getReadableDatabase();
                    Cursor kod5 = sqLiteDatabase.rawQuery("SELECT KOD5 FROM NOMEN WHERE rowid='" + pos + "'", null);
                    kod5.moveToNext();
                    if (kod5.getCount() == 0)
                        continue;
                    db.putPriceInNomen(pos, "" + DBHelper.pricesMap.get(kod5.getString(0)));
                    kod5.close();
                }

                CurAc.runOnUiThread(() -> {
                    myNom.requery();
                    NomenAdapter.notifyDataSetChanged();
                    setContrAndSum(GlobalVars.this);
                });
            }
        }).start();
    }

    @AsyncUI
    public void setIconColor(Menu menu, int MenuItem, Boolean vis) {
        if (menu.findItem(MenuItem) == null)
            return;
        Drawable drawable = menu.findItem(MenuItem).getIcon();
        drawable.mutate();
        if (vis) {
            drawable.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
        } else {
            drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
//        isDiscount = vis;
    }

    public void CalculatePercentSale(final Menu menu) {
        LayoutInflater SaleInf = LayoutInflater.from(CurAc);
        final View SaleMarkupView;

        SaleMarkupView = SaleInf.inflate(R.layout.discount_dlg, null);
        AlertDialog.Builder SaleDlg = new AlertDialog.Builder(CurAc);
        SaleDlg.setView(SaleMarkupView);

        final EditText edPercent = SaleMarkupView.findViewById(R.id.txtPercent);
        edPercent.setText(String.valueOf(Discount));

        SaleDlg.setCancelable(true)
                .setPositiveButton("OK", (dialog, id) -> {
                    String perc;
                    perc = edPercent.getText().toString().equals("") ? "0" : edPercent.getText().toString();
                    Discount = Float.parseFloat(perc);
                    if (Discount > 100) {
                        Discount = 100;
                    }

                    if (Discount == 0) {
                        isDiscount = false;
                        setIconColor(menu, R.id.NomenDiscount, false);
                    } else {
                        isDiscount = true;
                        setIconColor(menu, R.id.NomenDiscount, true);
                    }

                    if (NomenAdapter != null) {
                        myNom.requery();
                        NomenAdapter.notifyDataSetChanged();
                    }

                    if (PreviewZakazAdapter != null) {
                        myNom.requery();
                        PreviewZakazAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Отмена", (dialog, id) -> dialog.cancel());

        final AlertDialog discountDlg = SaleDlg.create();
        discountDlg.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        discountDlg.show();
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

    public void checkPhotoInDB(String FileName) {
        Cursor cur;
        String isDownloaded = FilenameUtils.removeExtension(FileName);
        String Sql;

        if (isDownloaded.equals("_2")) {
            Sql = "SELECT PD From Nomen WHERE KOD5='" + isDownloaded.replace(isDownloaded, "") + "'";
        } else {
            Sql = "SELECT PD From Nomen WHERE KOD5='" + isDownloaded + "'";
        }
        cur = db.getWritableDatabase().rawQuery(Sql, null);

        if (cur.moveToFirst()) {
            if (cur.getInt(0) == 0) {
                if (isDownloaded.equals("_2")) {
                    db.getWritableDatabase().execSQL("UPDATE Nomen SET PD=1 WHERE KOD5='" + isDownloaded.replace(isDownloaded, "") + "'");
                } else {
                    db.getWritableDatabase().execSQL("UPDATE Nomen SET PD=1 WHERE KOD5='" + isDownloaded + "'");
                }
            }
        }
        cur.close();
        GridView gdNomen = CurView.findViewById(R.id.listContrs);
        myNom.requery();
        if (NomenAdapter != null)
            NomenAdapter.notifyDataSetChanged();
        gdNomen.invalidateViews();
    }

    public void rewritePriceToMainDB(String docId) {
        Cursor orderPrices;
        orderPrices = dbOrders.getReadableDatabase().rawQuery("SELECT PRICE, NOMEN FROM ZAKAZY_DT WHERE ZAKAZ_ID = '" + docId + "'", null);

        while (orderPrices.moveToNext()) {
            DBHelper.pricesMap.remove(orderPrices.getString(1));
            DBHelper.pricesMap.put(orderPrices.getString(1), Float.parseFloat(orderPrices.getString(0).replace(",", ".")));
        }

        orderPrices.close();
    }

    public void calculatePricesByContrDiscount(String data) {
        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                db.calcSales(db.GetContrID(), data);
                CurAc.runOnUiThread(() -> {
                    if (NomenAdapter != null) {
                        myNom.requery();
                        NomenAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    public void updateOutedPositionInZakazyTable() {
        try {
            listOfOutedDocId = new HashSet<>();

            SQLiteDatabase sqLiteDatabaseOrders = dbOrders.getReadableDatabase();
            SQLiteDatabase sqLiteDatabase = db.getReadableDatabase();

//        Cursor c = sqLiteDatabaseOrders.rawQuery("SELECT DISTINCT ZAKAZ_ID FROM ZAKAZY_DT WHERE IS_OUTED=1", null);
//        while (c.moveToNext()) {
//            listOfOutedDocId.add(c.getString(0));
//        }
//        c.close();
            sqLiteDatabaseOrders.beginTransaction();
            Cursor ordersId = sqLiteDatabaseOrders.rawQuery("SELECT DOCID FROM ZAKAZY", null);
            while (ordersId.moveToNext()) {
                String orderID = ordersId.getString(0);
                Cursor c = sqLiteDatabase.rawQuery("SELECT NOMEN FROM VYCHERK WHERE DOCID=?", new String[]{orderID});

//                String orderID = ordersId.getString(0);
//                Cursor allOrdersDetails = sqLiteDatabaseOrders.rawQuery("SELECT NOMEN FROM ZAKAZY_DT WHERE ZAKAZ_ID=?", new String[]{orderID});
//                if (allOrdersDetails.getCount() == 0)
//                    continue;

//                StringBuilder s = new StringBuilder();
//                while (allOrdersDetails.moveToNext()) {
//                    s.append(allOrdersDetails.getString(0)).append(",");
//                }
//                String s2 = s.substring(0, s.length() - 1);

//                Cursor c = sqLiteDatabase.rawQuery("SELECT NOMEN FROM VYCHERK WHERE DOCID = ? AND NOMEN in (" + s2 + ")", new String[]{orderID});

                if (c.getCount() != 0) {
                    sqLiteDatabaseOrders.execSQL("UPDATE ZAKAZY SET OUTED=1 WHERE DOCID='" + orderID + "'");
                } else {
                    sqLiteDatabaseOrders.execSQL("UPDATE ZAKAZY SET OUTED=0 WHERE DOCID='" + orderID + "'");
                }
//                allOrdersDetails.close();
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

    public class NomenAdapter extends SimpleCursorAdapter {
        private boolean hasBeenAlreadyNoChanged = true;

        public NomenAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            Cursor cursor = getCursor();
            CurVisiblePosition = cursor.getCount();
            int resID;

            String kod5 = cursor.getString(cursor.getColumnIndex("KOD5"));

            TextView tvDescr = view.findViewById(R.id.ColNomDescr);
            TextView tvPrice = view.findViewById(R.id.ColNomPrice);
            TextView tvPosition = view.findViewById(R.id.ColNomPosition);
            TextView tvMP = view.findViewById(R.id.ColNomMP);
            TextView tvGofra = view.findViewById(R.id.ColNomVkorob);
            TextView tvOst = view.findViewById(R.id.ColNomOst);
            TextView tvCod = view.findViewById(R.id.ColNomCod);
            TextView tvPhoto = view.findViewById(R.id.ColNomPhoto);
            TextView tvZakaz = view.findViewById(R.id.ColNomZakaz);

            TextView[] tvListForChange = new TextView[]{
                    tvDescr,
                    tvPrice,
                    tvPosition,
                    tvMP,
                    tvGofra,
                    tvOst,
                    tvCod,
            };

            Button btPlus = view.findViewById(R.id.btPlus);
            Button btMinus = view.findViewById(R.id.btMinus);

            tvDescr.setTextSize(SettingFragment.nomenDescriptionFontSize);
            if (tvPhoto != null) {
                tvPhoto.setOnClickListener(v -> ((GridView) parent).performItemClick(v, position, 0));
            }

            if (tvPrice != null && !tvPrice.getText().toString().equals("null")) {
                tvPrice.setText(String.format(Locale.ROOT, "%.2f", Float.parseFloat(tvPrice.getText().toString())));
            }

            btPlus.setOnClickListener(v -> ((GridView) parent).performItemClick(v, position, 0));
            btMinus.setOnClickListener(v -> ((GridView) parent).performItemClick(v, position, 0));

            if (DBHelper.pricesMap.containsKey(kod5) && isSales) {
                tvPrice.setText(String.format(Locale.ROOT, "%.2f", DBHelper.pricesMap.get(kod5)));
            }

            if (isDiscount && tvPhoto != null) {
                tvPrice.setText(String.format(Locale.ROOT, "%.2f", Float.parseFloat(tvPrice.getText().toString()) * (1 - Discount / 100f)));
            }

            if (tvPhoto != null && cursor.getString(cursor.getColumnIndex("FOTO")) != null) {
                if (cursor.getInt(cursor.getColumnIndex("PD")) == 1)
                    resID = glbContext.getResources().getIdentifier("photo_green", "drawable", glbContext.getPackageName());
                else
                    resID = glbContext.getResources().getIdentifier("photo2", "drawable", glbContext.getPackageName());

                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append(" ").append(" ");
                builder.setSpan(new ImageSpan(glbContext, resID), builder.length() - 1, builder.length(), 0);
                builder.append(" ");
                tvPhoto.setText(builder);
            }

            long daysSubtraction = _countDaySubtraction(cursor);

            int backgroundColor;
            if (!tvZakaz.getText().toString().equals("0")) {
                backgroundColor = getResources().getColor(R.color.selectedNomen);
            } else {
                if (position % 2 != 0) {
                    backgroundColor = getResources().getColor(R.color.gridViewFirstColor);
                } else {
                    backgroundColor = getResources().getColor(R.color.gridViewSecondColor);
                }
            }
            view.setBackgroundColor(backgroundColor);

            int color = getResources().getColor(R.color.black);
            if (daysSubtraction <= 2) {
                color = getResources().getColor(R.color.postDataColorRed);
            } else if (daysSubtraction <= 4) {
                color = getResources().getColor(R.color.postDataColorGreen);
            }
            _setTextColorOnTextView(tvListForChange, color);

            int style = cursor.getInt(cursor.getColumnIndex("ACTION")) == 1 ? Typeface.BOLD_ITALIC : Typeface.NORMAL;
            _setTypeFaceOnTextView(tvListForChange, style);

            tvPosition.setText(String.valueOf(position + 1));

            return view;
        }

        private void _setTextColorOnTextView(TextView[] tvList, int color) {
            for (TextView tv : tvList) {
                tv.setTextColor(color);
            }
        }

        private void _setTypeFaceOnTextView(TextView[] tvList, int style) {
            for (TextView tv : tvList) {
                tv.setTypeface(Typeface.defaultFromStyle(style));
            }
        }

        private long _countDaySubtraction(Cursor cursor) {
            long CurrentTime = Calendar.getInstance().getTimeInMillis();
            String PostData = cursor.getString(cursor.getColumnIndex("POSTDATA"));

            String[] data = PostData.split("\\.");
            data[2] = "20" + data[2];

            PostData = data[0] + "." + data[1] + "." + data[2];

            DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            long PostDataTime = 0;
            try {
                PostDataTime = format.parse(PostData).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return (long) Math.abs((PostDataTime - CurrentTime) / (1000d * 60 * 60 * 24));
        }

        // TODO: fix it
        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();

            // check isSales and if true, then set real prices from contractor
//            if (hasBeenAlreadyNoChanged && isSales && nomenList.getCount() != 0) {
//                hasBeenAlreadyNoChanged = false;
//                FormOrderFragment.setRealPrices(GlobalVars.this);
//            }
        }
    }

    public class JournalAdapter extends SimpleCursorAdapter {
        public JournalAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Cursor cursor = getCursor();
            View view = super.getView(position, convertView, parent);
            TextView tvSum = view.findViewById(R.id.ColOrdSum);
            updateTextFormatTo2DecAfterPoint(tvSum);

            if (cursor.getInt(cursor.getColumnIndex("OUTED")) == 1) {
                tvSum.setText(tvSum.getText().toString() + " (-)");
            }
//            try {
//                if (listOfOutedDocId != null && listOfOutedDocId.contains(cursor.getString(cursor.getColumnIndex("DOCID")))) {
//                }
//            } catch (Exception ignore) {
//            }

            TextView tvStatus = view.findViewById(R.id.ColOrdStatus);
            if (tvStatus.getText().toString().equals("Сохранён"))
                tvStatus.setTypeface(Typeface.DEFAULT_BOLD);

//            String status = cursor.getString(cursor.getColumnIndex("STATUS"));
//            boolean isChecked = status.equals("Сохранён");
//            System.out.println(position);
            if (allOrders.size() != 0 && allOrders.get(position).isChecked) {
                view.setBackgroundColor(getResources().getColor(R.color.gridViewFirstColor));
            } else {
                view.setBackgroundColor(getResources().getColor(R.color.gridViewSecondColor));
            }
            return view;
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
            String resDescr = cursor.getString(cursor.getColumnIndex("DESCR")) + "\t\t" + cursor.getString(cursor.getColumnIndex("STATUS"));

            TextView tvDescr = view.findViewById(R.id.ColContrDescr);
            if (position % 2 != 0) {
                tvDescr.setBackgroundColor(getResources().getColor(R.color.gridViewFirstColor));
            } else {
                tvDescr.setBackgroundColor(getResources().getColor(R.color.gridViewSecondColor));
            }

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
            Cursor cursor = getCursor();
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

    public void resetCurData() {
        CurGroup = CurWCID = CurFocusID = CurSGI = "0";
        CurSearchName = "";
        isNeededToBeLoadingBySgi = true;

        Discount = 0f;
        Menu menu = FormOrderFragment.mainMenu;
        if (menu != null && menu.size() > 1) {
            setIconColor(menu, R.id.NomenDiscount, false);
        }
    }

    public void resetAllSpinners() {
        spSgi.setSelection(0);

        // if spWC != null, то и spFocus и другие фильтры тоже != null
        if (spWC != null) {
            spWC.setSelection(0);
            spFocus.setSelection(0);
        }
        FormOrderFragment.filter.setImageResource(R.drawable.filter);
        FormOrderFragment.isFiltered = false;
    }

    public void resetSearchViewData() {
        CurSearchName = "";
        SearchView searchView = CurAc.findViewById(R.id.menu_search);
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.onActionViewCollapsed();
        }
    }

    public void putAllPrices() {
        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                db.putAllNomenPrices(OrderHeadFragment.CONTR_ID);
                CurAc.runOnUiThread(() -> {
                    if (NomenAdapter != null) {
                        NomenAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    public void closeCursors() {
        Cursor[] cursors = new Cursor[]{
                myNom,
                mySgi,
                myGrups,
                curWC,
                curFocus
        };
        for (Cursor cursor : cursors) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void updateNomenPrice(boolean isCopied) {
        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                db.ResetNomenPrice(isCopied);
            }
        });
    }

    public static String[] getCurrentData() {
        return new String[]{CurSGI, CurGroup, CurWCID, CurFocusID, CurSearchName};
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
    private void plusQTY(View myView) {
        View parent = (View) myView.getParent();
        TextView tvKOD5 = parent.findViewById(R.id.ColNomCod);
        TextView tvPrice = parent.findViewById(R.id.ColNomPrice);

        String price = tvPrice.getText().toString();
        String kod5 = tvKOD5.getText().toString();

        db.putPriceInNomen(kod5, price);
        db.PlusQty(kod5);
        myNom.requery();

        if (NomenAdapter != null)
            NomenAdapter.notifyDataSetChanged();

        if (PreviewZakazAdapter != null)
            PreviewZakazAdapter.notifyDataSetChanged();

        setContrAndSum(GlobalVars.this);
    }

    private void minusQTY(View myView) {
        View parent = (View) myView.getParent();
        TextView tvKOD5 = parent.findViewById(R.id.ColNomCod);
        String kod5 = tvKOD5.getText().toString();

        db.MinusQty(kod5);
        myNom.requery();

        if (NomenAdapter != null)
            NomenAdapter.notifyDataSetChanged();

        if (PreviewZakazAdapter != null)
            PreviewZakazAdapter.notifyDataSetChanged();

        setContrAndSum(GlobalVars.this);
    }

    @AsyncUI
    private void showPhoto(View myView, int position, AdapterView<?> myAdapter) {
        if (((TextView) myView).getText() == null || ((TextView) myView).getText().toString().equals(""))
            return;

        isSecondPhoto = false;
        String photoDir = getPhotoDir();
        long ID = myAdapter.getItemIdAtPosition(position);

        String FileName = db.GetCod(ID);
        File imgFile = new File(photoDir + "/" + FileName);
//        DownloadPhoto(FileName);
        if (!imgFile.exists() || imgFile.length() == 0) {
            AsyncFileName = FileName;
            if (isNetworkAvailable()) {
                DownloadPhoto(FileName);
            } else {
                Toast.makeText(glbContext, "Нет доступного интернет соединения", Toast.LENGTH_LONG).show();
            }
        } else {
            //TODO: сюда заходит
            try {
                ShowNomenPhoto(FileName);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Не получилось открыть фото", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void multiSelect(String ID, int ost) {
        db.UpdateQty(ID, MultiQty, ost);
        myNom.requery();
        NomenAdapter.notifyDataSetChanged();

        db.putPriceInNomen(ID, "" + DBHelper.pricesMap.get(ID));
        setContrAndSum(GlobalVars.this);
    }

    @AsyncUI
    private void fillNomenDataFromAlertDialog(String ID, int ost) {
        LayoutInflater layoutInflater = LayoutInflater.from(glbContext);
        View promptView = layoutInflater.inflate(R.layout.change_qty, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CurFragmentContext);
        alertDialogBuilder.setView(promptView);

        final EditText input = promptView.findViewById(R.id.edPPQty);
        final TextView txtCod = promptView.findViewById(R.id.txtNomCode);
        final TextView txtDescr = promptView.findViewById(R.id.txtNomDescr);
        final TextView txtOst = promptView.findViewById(R.id.txtNomOst);
        final TextView txtGrup = promptView.findViewById(R.id.txtNomGroup);

        input.setText(myNom.getString(myNom.getColumnIndex("ZAKAZ")));
        txtCod.setText(myNom.getString(myNom.getColumnIndex("KOD5")));
        txtDescr.setText(myNom.getString(myNom.getColumnIndex("DESCR")));
        txtOst.setText(myNom.getString(myNom.getColumnIndex("OST")));

        Cursor c = db.getReadableDatabase().rawQuery("SELECT DESCR FROM GRUPS WHERE CODE=?", new String[]{myNom.getString(myNom.getColumnIndex("GRUPPA"))});
        c.moveToNext();
        String groupDescription = c.getString(c.getColumnIndex("DESCR"));
        txtGrup.setText(groupDescription);

        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("OK", (dialog, id) -> {
                })
                .setNegativeButton("Отмена", (dialog, id) -> dialog.cancel());

        final AlertDialog alertD = alertDialogBuilder.create();
        alertD.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        WindowManager.LayoutParams wmlp = alertD.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        wmlp.x = 50;
        wmlp.y = 15;

        alertD.show();
        input.requestFocus();
        input.selectAll();
        input.performClick();
        input.setPressed(true);
        input.invalidate();
        InputMethodManager imm = (InputMethodManager) glbContext.getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

        alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            TextView tvPrice = CurView.findViewById(R.id.ColNomPrice);
            String price = tvPrice.getText().toString();
            db.putPriceInNomen(ID, price);

            db.UpdateQty(ID, Integer.parseInt(input.getText().toString()), ost);
            myNom.requery();

            setContrAndSum(GlobalVars.this);

            if (NomenAdapter != null)
                NomenAdapter.notifyDataSetChanged();

            if (PreviewZakazAdapter != null)
                PreviewZakazAdapter.notifyDataSetChanged();

            alertD.dismiss();
            Config.hideKeyBoard();
        });
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                alertD.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
            }
            return true;
        });
    }
}
