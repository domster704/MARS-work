package com.amber.armtp.ui;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amber.armtp.Config;
import com.amber.armtp.GlobalVars;
import com.amber.armtp.ImageAdapter;
import com.amber.armtp.PhotoDotsAdapter;
import com.amber.armtp.R;
import com.amber.armtp.adapters.NomenAdapterSQLite;
import com.amber.armtp.annotations.DelayedCalled;
import com.amber.armtp.dbHelpers.DBAppHelper;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.dbHelpers.DBOrdersHelper;
import com.amber.armtp.extra.ExtraFunctions;
import com.amber.armtp.extra.PhotoDownloadingRunnable;
import com.amber.armtp.extra.ProgressBarShower;
import com.amber.armtp.interfaces.TBUpdate;

import org.apache.commons.io.FilenameUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

/**
 * Updated by domster704 on 02.12.2023
 */
public class FormOrderFragment extends Fragment implements View.OnClickListener, TBUpdate {
    public boolean isSorted = false;
    public static boolean isContrIdDifferent = false;
    public boolean isFiltered = false;
    public boolean isCleared = false;
    public static boolean isSales;
    public boolean isMultiSelect = false;
    private boolean isNeededToSelectRowAfterGoToGroup = false;
    private boolean isNeededToResetSearchView = true;
    private boolean isDiscount = false;
    private DBHelper db;
    public DBAppHelper dbApp;
    public DBOrdersHelper dbOrders;
    public Cursor myNom = null, mySgi, myGroup;
    public Cursor myWC = null, myFocus = null;
    private SharedPreferences.Editor editor;
    private SearchView searchView;
    public GridView nomenList;
    public ImageButton filter;
    public ImageButton spinnerClearing;
    public static Menu mainMenu;
    private View rootView;
    private TextView FilterWC_ID, FilterFocus_ID;
    private android.support.v7.widget.Toolbar toolbar;

    private Spinner spSgi, spGroup;
    private Spinner spWC, spFocus;
    private Handler photoThreadhandler;
    public static Thread downloadPhotoTread;
    private NomenAdapterSQLite NomenAdapter, PreviewZakazAdapter;
    private String CurSGI = "0", CurGroup = "0", CurWCID = "0", CurFocusID = "0", CurSearchName = "";
    public static String TypeOfPrice = "";
    public static String kod5 = "";
    public int BeginPos = 0, EndPos = 0;
    public int MultiQty = 0;
    public float Discount = 0;

    private final SearchView.OnQueryTextListener searchTextListener =
            new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.equals("") && !GlobalVars.CurSearchName.equals("")) {
                        LoadNomen(GlobalVars.CurSGI, GlobalVars.CurGroup, GlobalVars.CurWCID, GlobalVars.CurFocusID, newText);
                    }
                    GlobalVars.CurSearchName = newText;
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    LoadNomen(GlobalVars.getCurrentData());
                    Config.hideKeyBoard();
                    searchView.clearFocus();
                    return true;
                }
            };

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);

        db = new DBHelper(getActivity().getApplicationContext());
        dbApp = new DBAppHelper(getActivity().getApplicationContext());
        dbOrders = new DBOrdersHelper(getActivity().getApplicationContext());

        toolbar = getActivity().findViewById(R.id.toolbar);

        nomenList = getActivity().findViewById(R.id.listContrs);
        spSgi = getActivity().findViewById(R.id.SpinSgi);
        spGroup = getActivity().findViewById(R.id.SpinGrups);

        TextView tvHeadCod = getActivity().findViewById(R.id.tvHeadCod);
        TextView tvHeadDescr = getActivity().findViewById(R.id.tvHeadDescr);
        TextView tvHeadMP = getActivity().findViewById(R.id.tvHeadMP);
        TextView tvHeadZakaz = getActivity().findViewById(R.id.tvHeadZakaz);

        SharedPreferences settings = getActivity().getSharedPreferences("form_order", 0);
        editor = settings.edit();

        LoadSgi();

        filter = getActivity().findViewById(R.id.NomenFilters);
        filter.setOnClickListener(this);

        spinnerClearing = getActivity().findViewById(R.id.SGIClear);
        spinnerClearing.setOnClickListener(this);

        setContrAndSumValue(db, toolbar, isSales);
        if (getArguments() != null && getArguments().size() != 0 && getArguments().containsKey("SGI")) {
            String sgi = getArguments().getString("SGI");
            String group = getArguments().getString("Group");

            resetCurData();
            resetSearchViewData();

            allowUpdate = false;
            setSelectionByCodeSgi(sgi);
            new Handler().postDelayed(() -> setSelectionByCodeGroupAsync(group), 500);

            getArguments().remove("SGI");
            getArguments().remove("Group");
        } else {
            LoadGroups("0");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.form_order_fragment, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setHasOptionsMenu(true);
        this.rootView = rootView;

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        GlobalVars.CurFragmentContext = getActivity();
        GlobalVars.CurAc = getActivity();

        resetCurData();

        photoThreadhandler = new Handler(new Handler.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean handleMessage(Message message) {
                Bundle bundle = message.getData();
                switch (message.what) {
                    case PhotoDownloadingRunnable.MESSAGE_UPDATE_DB:
                        String fileName = bundle.getString("fileName");
                        db.getWritableDatabase().execSQL("UPDATE Nomen SET PD=1 WHERE FOTO=? or FOTO2=?", new Object[]{fileName, fileName});
                        break;
                    case PhotoDownloadingRunnable.MESSAGE_SHOW_PRODUCTS:
                        String[] fileNames = bundle.getStringArray("fileNames");
                        String kod5 = bundle.getString("kod5");
                        getActivity().runOnUiThread(() -> showProductPhoto(fileNames, kod5));
                        break;
                }
                return false;
            }
        });

        Config.hideKeyBoard();
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.form_order_menu, menu);
        mainMenu = menu;

        // Включение учёта скидки торгового представителя (значок "%" станет зелёным (Этой иконки уже нет, но думаю вы поймёте))
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Поиск номенклатуры");
        searchView.setOnQueryTextListener(searchTextListener);
        searchView.setOnCloseListener(() -> {
            LoadNomen(GlobalVars.CurSGI, GlobalVars.CurGroup, GlobalVars.CurWCID, GlobalVars.CurFocusID, "");
            return false;
        });

        if (NomenAdapter != null) {
            NomenAdapter.notifyDataSetChanged();
        }

        setContrAndSumValue(db, toolbar, isSales);
        if (isDiscount) {
            isDiscount = false;
            Discount = 0;
        }
    }

    public void SaveOrder() {
        Cursor c = null, c1 = null, c2 = null;
        try {
            //                for (int i = 0; i < 100; i++) {
            String TP_ID, Contr_ID, Address_ID, Data, Comment, IDDOC = "";
            String contrDes, addressDes;
            String status = "Сохранён";
            float Sum = 0f;

            String sql;
            SQLiteStatement stmt;

            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            String curDate = df.format(Calendar.getInstance().getTime());

            c = db.getReadableDatabase().rawQuery("SELECT TORG_PRED.CODE as TP_ID, ORDERS.DATA as DATA, ORDERS.COMMENT as COMMENT, CONTRS.CODE AS CONTR_ID, ADDRS.CODE AS ADDR_ID, CONTRS.DESCR as C_DES, ADDRS.DESCR as A_DES FROM ORDERS JOIN TORG_PRED ON ORDERS.TP=TORG_PRED.CODE JOIN CONTRS ON ORDERS.CONTR=CONTRS.CODE JOIN ADDRS ON ORDERS.ADDR=ADDRS.CODE", null);
            c2 = db.getReadableDatabase().rawQuery("SELECT 0 AS _id, CASE WHEN COUNT(ROWID) IS NULL THEN 0 ELSE COUNT(ROWID) END AS COUNT FROM Nomen WHERE ZAKAZ<>0", null);
            if (c.getCount() == 0) {
                Config.sout("Не заполнена шапка заказа");
                return;
            }

            if (c2.getCount() == 0) {
                Config.sout("Нет ни одного добавленного товара для заказа");
                return;
            } else {
                c2.close();
            }

            c.moveToNext();
            TP_ID = c.getString(c.getColumnIndex("TP_ID"));
            Data = c.getString(c.getColumnIndex("DATA"));
            Comment = c.getString(c.getColumnIndex("COMMENT"));
            Contr_ID = c.getString(c.getColumnIndex("CONTR_ID"));
            Address_ID = c.getString(c.getColumnIndex("ADDR_ID"));
            contrDes = c.getString(c.getColumnIndex("C_DES"));
            addressDes = c.getString(c.getColumnIndex("A_DES"));

            c.close();

            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("HHmmss");
            String dateForIDDOC = dateFormat.format(Calendar.getInstance().getTimeInMillis()) + Calendar.getInstance().get(Calendar.MILLISECOND);

            IDDOC += TP_ID + "_" + Data.replace(".", "") + "_" + dateForIDDOC;

            c1 = db.getReadableDatabase().rawQuery("SELECT KOD5 FROM Nomen where ZAKAZ<>0", null);
            if (c1.getCount() == 0) {
                c1.close();
                return;
            } else {
                Sum = insertIntoOrderDT(IDDOC, Sum);
            }

            sql = "INSERT INTO ZAKAZY(DOCID, TP, CONTR, ADDR, DOC_DATE, DELIVERY_DATE, COMMENT, STATUS, CONTR_DES, ADDR_DES, SUM)  VALUES (?,?,?,?,?,?,?,?,?,?,?);";
            stmt = dbOrders.getWritableDatabase().compileStatement(sql);
            dbOrders.getWritableDatabase().beginTransaction();
            try {
                stmt.clearBindings();
                stmt.bindString(1, IDDOC);
                stmt.bindString(2, TP_ID);
                stmt.bindString(3, Contr_ID);
                stmt.bindString(4, Address_ID);
                stmt.bindString(5, curDate);
                stmt.bindString(6, Data);
                stmt.bindString(7, Comment);
                stmt.bindString(8, status);
                stmt.bindString(9, contrDes);
                stmt.bindString(10, addressDes);
                stmt.bindString(11, String.format(Locale.ROOT, "%.2f", Sum));
                stmt.executeInsert();
                stmt.clearBindings();
            } catch (Exception e) {
                Config.sout(e);
                e.printStackTrace();
                throw new Exception(e);
            } finally {
                dbOrders.getWritableDatabase().setTransactionSuccessful();
                dbOrders.getWritableDatabase().endTransaction();
                stmt.close();
            }
//                }
            db.ClearOrderHeader();
            db.ResetNomen();

            getActivity().runOnUiThread(() -> {
                try {
                    Config.sout("Заказ сохранён");
                    closeCursors();

                    editor.putString("ColSgiID", "0");
                    editor.commit();

                    Fragment fragment = new JournalFragment();

                    FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fragment, "frag_journal");
                    fragmentTransaction.commit();
                    toolbar.setTitle(R.string.journal);
                } catch (Exception e) {
                    Config.sout(e);
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            Config.sout(e);
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
            if (c1 != null) {
                c1.close();
            }
            if (c2 != null) {
                c2.close();
            }
        }
    }

    public void SaveEditOrder(final String OrderID) {
//        new Thread(new Runnable() {
//            @Override
//            @PGShowing
//            public void run() {
        try {
            float Sum = 0f;
            OrderHeadFragment.isNeededToUpdateOrderTable = false;

            Cursor orderHeader = db.getReadableDatabase().rawQuery("SELECT TORG_PRED.CODE as TP_ID, ORDERS.DATA as DATA, ORDERS.COMMENT as COMMENT, CONTRS.CODE AS CONTR_ID, ADDRS.CODE AS ADDR_ID, CONTRS.DESCR as C_DES, ADDRS.DESCR as A_DES FROM ORDERS JOIN TORG_PRED ON ORDERS.TP=TORG_PRED.CODE JOIN CONTRS ON ORDERS.CONTR=CONTRS.CODE JOIN ADDRS ON ORDERS.ADDR=ADDRS.CODE", null);
            if (orderHeader.getCount() == 0) {
                Config.sout("Не заполнена шапка заказа", Toast.LENGTH_LONG);
                return;
            }
            orderHeader.moveToFirst();
            String TP_ID = orderHeader.getString(orderHeader.getColumnIndex("TP_ID")),
                    Data = orderHeader.getString(orderHeader.getColumnIndex("DATA")),
                    Comment = orderHeader.getString(orderHeader.getColumnIndex("COMMENT")),
                    ContrID = orderHeader.getString(orderHeader.getColumnIndex("CONTR_ID")),
                    AddressID = orderHeader.getString(orderHeader.getColumnIndex("ADDR_ID")),
                    ContrDes = orderHeader.getString(orderHeader.getColumnIndex("C_DES")),
                    AddressDes = orderHeader.getString(orderHeader.getColumnIndex("A_DES"));


            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("HHmmss");
            String dateForIDDOC = dateFormat.format(Calendar.getInstance().getTimeInMillis()) + Calendar.getInstance().get(Calendar.MILLISECOND);

            String IDDOC = TP_ID + "_" + Data.replace(".", "") + "_" + dateForIDDOC;
//            String IDDOC = TP_ID;

            orderHeader.close();

            Cursor orderCount = db.getReadableDatabase().rawQuery("SELECT 0 AS _id, CASE WHEN COUNT(ROWID) IS NULL THEN 0 ELSE COUNT(ROWID) END AS COUNT FROM Nomen WHERE ZAKAZ<>0", null);
            orderCount.moveToFirst();
            if (orderCount.getInt(1) == 0) {
                Config.sout("Нет ни одного добавленного товара для заказа", Toast.LENGTH_LONG);
                return;
            }
            orderCount.close();

            dbOrders.getWritableDatabase().execSQL("DELETE FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + OrderID + "'");
            Sum = insertIntoOrderDT(IDDOC, Sum);

            String sql = "UPDATE ZAKAZY SET TP=?, CONTR=?, ADDR=?, DELIVERY_DATE=?, COMMENT=?, CONTR_DES=?, ADDR_DES=?, SUM=?, DOCID=? WHERE DOCID='" + OrderID + "'";
            SQLiteStatement stmt = dbOrders.getWritableDatabase().compileStatement(sql);
            dbOrders.getWritableDatabase().beginTransaction();
            try {
                stmt.clearBindings();
                stmt.bindString(1, TP_ID);
                stmt.bindString(2, ContrID);
                stmt.bindString(3, AddressID);
                stmt.bindString(4, Data);
                stmt.bindString(5, Comment);
                stmt.bindString(6, ContrDes);
                stmt.bindString(7, AddressDes);
                stmt.bindString(8, String.format(Locale.ROOT, "%.2f", Sum));
                stmt.bindString(9, IDDOC);
                stmt.executeInsert();
                stmt.clearBindings();
            } catch (Exception e) {
                Config.sout(e);
                throw new Exception(e);
            } finally {
                dbOrders.getWritableDatabase().setTransactionSuccessful();
                dbOrders.getWritableDatabase().endTransaction();
                stmt.close();
            }

            db.ClearOrderHeader();
            db.ResetNomen();

            getActivity().runOnUiThread(() -> {
                try {
                    Toast.makeText(getActivity(), "Заказ сохранён", Toast.LENGTH_LONG).show();
                    closeCursors();

                    Fragment fragment = new JournalFragment();

                    Bundle args = new Bundle();
                    args.putBoolean("isStartDeletingExtraOrders", true);
                    fragment.setArguments(args);

                    FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fragment, "frag_journal");
                    fragmentTransaction.commit();
                    toolbar.setTitle(R.string.journal);
                } catch (Exception e) {
                    Config.sout(e);
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            Config.sout(e);
            e.printStackTrace();
        }
//            }
//        }).start();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ViewOrderId:
                try {
                    Fragment fragment = new ViewOrderFragment();
                    FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fragment);
                    fragmentTransaction.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    Config.sout(e);
                }
                return true;
            case R.id.NomenSave:
                new Thread(() -> {
                    new ProgressBarShower(getContext()).setFunction(() -> {
                        try {
                            isFiltered = false;
                            resetCurData();

                            if (isSales) {
                                putRealPriceInPriceColumn();
                            }

                            if (OrderHeadFragment.isNeededToUpdateOrderTable) {
                                SaveEditOrder(JournalFragment.OrderID);
                            } else {
                                SaveOrder();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Config.sout(e);
                        }
                        return null;
                    }).start();
                }).start();

                return true;
            case R.id.NomenMultiPos:
                try {
                    LayoutInflater lInf = LayoutInflater.from(getActivity());
                    View RangeDlgView;

                    RangeDlgView = lInf.inflate(R.layout.change_range_qty, null);
                    AlertDialog.Builder RangeDlg = new AlertDialog.Builder(getActivity());
                    RangeDlg.setView(RangeDlgView);

                    final EditText edBeginPP = RangeDlgView.findViewById(R.id.edBeginPP);
                    final EditText edEndPP = RangeDlgView.findViewById(R.id.edEndPP);
                    final EditText edPPQty = RangeDlgView.findViewById(R.id.edPPQty);
                    edBeginPP.setText((BeginPos != 0 ? String.valueOf(BeginPos) : "0"));
                    edEndPP.setText((EndPos != 0 ? String.valueOf(EndPos) : "0"));
                    edPPQty.setText("0");

                    RangeDlg.setCancelable(true)
                            .setPositiveButton("OK", (dialog, id) -> {
                            })
                            .setNegativeButton("Отмена", (dialog, id) -> dialog.cancel());

                    final AlertDialog alertDlg = RangeDlg.create();
                    Objects.requireNonNull(alertDlg.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

                    alertDlg.show();

                    EditText edInput;

                    if (BeginPos != 0 && EndPos != 0) {
                        edInput = edPPQty;
                    } else if (BeginPos != 0) {
                        edInput = edEndPP;
                    } else {
                        edInput = edBeginPP;
                    }

                    edInput.requestFocus();
                    edInput.selectAll();
                    edInput.performClick();
                    edInput.setPressed(true);
                    edInput.invalidate();
                    getActivity();
                    InputMethodManager immPP = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(INPUT_METHOD_SERVICE);
                    immPP.showSoftInput(edInput, InputMethodManager.SHOW_IMPLICIT);

                    alertDlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                        if (nomenList == null || NomenAdapter == null || myNom == null || myNom.getCount() == 0) {
                            Config.sout("Таблица товаров пуста");
                            alertDlg.dismiss();
                            return;
                        }
                        if (!edBeginPP.getText().toString().equals("") && !edEndPP.getText().toString().equals("") && !edPPQty.getText().toString().equals("")) {
                            UpdateNomenRange(Integer.parseInt(edBeginPP.getText().toString()), Integer.parseInt(edEndPP.getText().toString()), Integer.parseInt(edPPQty.getText().toString()));
                            BeginPos = 0;
                            EndPos = 0;
                            alertDlg.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Необходимо указать начальную позицию, конечную позицию и нужное количество", Toast.LENGTH_LONG).show();
                        }
                    });

                    edBeginPP.setOnEditorActionListener((v, actionId, event) -> {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            if (!edBeginPP.getText().toString().equals("")) {
                                edEndPP.requestFocus();
                                edEndPP.selectAll();
                                edEndPP.performClick();
                                edEndPP.setPressed(true);
                                edEndPP.invalidate();
                            }
                        }
                        return true;
                    });

                    edEndPP.setOnEditorActionListener((v, actionId, event) -> {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            if (!edEndPP.getText().toString().equals("")) {
                                edPPQty.requestFocus();
                                edPPQty.selectAll();
                                edPPQty.performClick();
                                edPPQty.setPressed(true);
                                edPPQty.invalidate();
                            }
                        }
                        return true;
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Config.sout(e);
                }
                return true;
            case R.id.NomenSort:
                try {
                    if (NomenAdapter == null)
                        return true;

                    if (!isSorted) {
                        item.setIcon(R.drawable.to_top);
                        nomenList.setAdapter(NomenAdapter);
                        nomenList.setSelection(nomenList.getCount());
                        isSorted = true;
                    } else {
                        item.setIcon(R.drawable.to_end);
                        nomenList.setAdapter(NomenAdapter);
                        nomenList.setSelection(0);
                        isSorted = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Config.sout(e);
                }
                return true;
            case R.id.NomenDiscount:
                try {
                    CalculatePercentSale(mainMenu);
                } catch (Exception e) {
                    e.printStackTrace();
                    Config.sout(e);
                }
                return true;
            case R.id.NomenMultiSelect:
                try {
                    if (!isMultiSelect) {
                        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                        View promptView;

                        promptView = layoutInflater.inflate(R.layout.multi_qty, null);
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setView(promptView);

                        final EditText input = promptView.findViewById(R.id.txtPercent);
                        input.setText(String.valueOf(MultiQty));

                        alertDialogBuilder
                                .setCancelable(true)
                                .setPositiveButton("OK", (dialog, id) -> {
                                })
                                .setNegativeButton("Отмена", (dialog, id) -> dialog.cancel());

                        final AlertDialog alertD = alertDialogBuilder.create();
                        Objects.requireNonNull(alertD.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

                        alertD.show();
                        input.requestFocus();
                        input.selectAll();
                        input.performClick();
                        input.setPressed(true);
                        input.invalidate();
                        getActivity();
                        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(INPUT_METHOD_SERVICE);
                        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

                        alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                            isMultiSelect = true;
                            MultiQty = Integer.parseInt(input.getText().toString());
                            item.setIcon(Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.checkbox_marked));
                            alertD.dismiss();
                        });

                        input.setOnEditorActionListener((v, actionId, event) -> {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                alertD.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                            }
                            return true;
                        });

                    } else {
                        isMultiSelect = false;
                        item.setIcon(getResources().getDrawable(R.drawable.checkbox_free));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Config.sout(e);
                }

                return true;
            case R.id.NomenSales:
                // ИП Лужбина Н.М.
                // ИП Беляев В.В.
                // ИП Трушникова А.А. I09109
                try {
                    isSales = !isSales;
                    setIconColor(mainMenu, R.id.NomenSales, isSales);
                    if (isContrIdDifferent || DBHelper.pricesMap.size() == 0) {
                        isContrIdDifferent = false;
                        putAllPrices();
                    } else if (NomenAdapter != null) {
                        NomenAdapter.notifyDataSetChanged();
                    } else {
                        Config.sout("Ошибка считывания таблицы заказов");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Config.sout(e);
                }
                return true;
            case R.id.clear_whole_order:
                try {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Очистка заказа")
                            .setMessage("Вы уверены, что хотите очистить текущий заказ?")
                            .setPositiveButton("Да", (dialogInterface, i) -> {
                                db.clearOrder();
                                if (NomenAdapter != null) {
                                    NomenAdapter.notifyDataSetChanged();
                                }
                                if (myNom != null) {
                                    myNom.requery();
                                }
                                setContrAndSumValue(db, toolbar, isSales);
                            })
                            .setNeutralButton("Нет", (dialogInterface, i) -> dialogInterface.dismiss())
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Config.sout(e);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        setContrAndSumValue(db, toolbar, isSales);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        resetCurData();

        setIconColor(mainMenu, R.id.NomenSales, false);
        isSales = false;
        MultiQty = 0;

        try {
            isFiltered = false;
            filter.setImageResource(R.drawable.filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        closeCursors();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.NomenFilters:
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                View promptView;
                promptView = layoutInflater.inflate(R.layout.nom_filters, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setView(promptView);

                alertDialogBuilder
                        .setCancelable(true)
                        .setPositiveButton("OK", (dialog, id) -> {
                        })
                        .setNegativeButton("Отмена", (dialog, id) -> dialog.cancel())
                        .setNeutralButton("Сбросить фильтры", (dialog, id) -> {
                        });

                AlertDialog alertD = alertDialogBuilder.create();
                alertD.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                alertD.show();
                LoadFiltersWC(promptView);
                LoadFiltersFocus(promptView);

                String WCID = dbApp.getIDByWC(GlobalVars.CurWCID);
                String FocusID = GlobalVars.CurFocusID;

                SetSelectedFilterWC(WCID);
                SetSelectedFilterFocus(FocusID);

                alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    isFiltered = true;
                    FilterWC_ID = promptView.findViewById(R.id.ColWCID);
                    FilterFocus_ID = promptView.findViewById(R.id.ColFocusID);

                    if (!FilterFocus_ID.getText().toString().equals("0") || !FilterWC_ID.getText().toString().equals("0")) {
                        filter.setImageResource(R.drawable.filter_activated);
                    }

                    String SgiId = "0";
                    String GroupID = "0";

                    String WC_ID = dbApp.getWCByID(FilterWC_ID.getText().toString());

                    LoadNomen(SgiId, GroupID,
                            WC_ID, FilterFocus_ID.getText().toString(), GlobalVars.CurSearchName);
                    setSelectionByCodeSgiAsync(SgiId);
                    alertD.dismiss();
                });

                alertD.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                    isFiltered = false;
                    filter.setImageResource(R.drawable.filter);

                    SetSelectedFilterWC("0");
                    SetSelectedFilterFocus("0");
                    GlobalVars.CurFocusID = GlobalVars.CurWCID = "0";
                });
                break;
            case R.id.SGIClear:
                String localSearchName = GlobalVars.CurSearchName;
                isCleared = true;

                resetCurData();
                isNeededToResetSearchView = false;
                resetAllSpinners();

                nomenList.setAdapter(null);
                myNom = null;

                GlobalVars.CurSearchName = localSearchName;
                spSgi.post(() -> {
                    spSgi.setAdapter(spSgi.getAdapter());
                    spSgi.setSelection(0);
                    searchView.setQuery(localSearchName, true);
                });
                break;
        }
    }

    private float insertIntoOrderDT(String docID, float SUM) {
        Cursor nomenData;
        if (isSales) {
            nomenData = db.getReadableDatabase().rawQuery("SELECT KOD5, DESCR, ZAKAZ, PRICE FROM Nomen WHERE ZAKAZ<>0", null);
        } else {
            nomenData = db.getReadableDatabase().rawQuery("SELECT KOD5, DESCR, ZAKAZ, [" + TypeOfPrice + "] as PRICE FROM Nomen WHERE ZAKAZ<>0", null);
        }

        dbOrders.getWritableDatabase().beginTransaction();
        for (int i = 0; i < nomenData.getCount(); i++) {
            nomenData.moveToNext();
            String PRICE = nomenData.getString(nomenData.getColumnIndex("PRICE"));
            String KOD5 = nomenData.getString(nomenData.getColumnIndex("KOD5"));
            if (PRICE == null) {
                System.out.println(KOD5 + " проблема с ценой в виде " + PRICE);
                PRICE = "0.0";
            }
            String DESCR = nomenData.getString(nomenData.getColumnIndex("DESCR"));
            String ZAKAZ = nomenData.getString(nomenData.getColumnIndex("ZAKAZ"));

            float sum = Float.parseFloat(PRICE.replace(",", ".")) * Integer.parseInt(ZAKAZ);
            SUM += sum;
            dbOrders.getWritableDatabase().execSQL("INSERT INTO ZAKAZY_DT (ZAKAZ_ID, NOMEN, DESCR, QTY, PRICE, SUM) VALUES('" + docID + "','" + KOD5 + "','" + DESCR + "','" + ZAKAZ + "','" + PRICE + "','" + String.format(Locale.ROOT, "%.2f", sum) + "')");
        }

        nomenData.close();
        dbOrders.getWritableDatabase().setTransactionSuccessful();
        dbOrders.getWritableDatabase().endTransaction();
        return SUM;
    }

    private void putRealPriceInPriceColumn() {
        SQLiteDatabase database = db.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT KOD5 FROM Nomen WHERE ZAKAZ <> 0", null);

        database.beginTransaction();
        while (cursor.moveToNext()) {
            String kod5 = cursor.getString(0);
            if (!DBHelper.pricesMap.containsKey(kod5)) {
                continue;
            }
            database.execSQL("UPDATE NOMEN SET PRICE=? WHERE KOD5=?", new Object[]{DBHelper.pricesMap.get(kod5), kod5});
        }
        cursor.close();
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public void downloadAndShowPhotos(final String[] fileNames, long ID, boolean isForced) {
        downloadPhotoTread = new Thread(new PhotoDownloadingRunnable(fileNames, db.getProductKod5ByRowID(ID), isForced, getActivity(), photoThreadhandler));
        downloadPhotoTread.start();
    }

    public void downloadAndShowPhotos(final String[] fileNames, String kod5, boolean isForced) {
        downloadPhotoTread = new Thread(new PhotoDownloadingRunnable(fileNames, kod5, isForced, getActivity(), photoThreadhandler));
        downloadPhotoTread.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showProductPhoto(String[] photoFileName, String kod5) {
        AlertDialog alertPhoto = null;
        String photoDir = ExtraFunctions.getPhotoDir(getContext());
        photoFileName = Arrays.stream(photoFileName).filter(Objects::nonNull).toArray(String[]::new);
        if (photoFileName.length == 0) {
            Config.sout("Нет скачанных файлов");
            return;
        }

        for (String s : photoFileName) {
            checkPhotoInDB(s);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.image_layout, null));
        builder.setCancelable(true);
        builder.setPositiveButton("OK", (dialog, id) -> dialog.dismiss());

        alertPhoto = builder.create();
        alertPhoto.getWindow().setLayout(600, 400);
        alertPhoto.show();

        TextView productID = alertPhoto.findViewById(R.id.nomenId);
        productID.setText("Код товара:   " + kod5);

        ViewPager viewPager = alertPhoto.findViewById(R.id.photoViewPager);

        LinearLayout dots = alertPhoto.findViewById(R.id.layoutForPhotoDots);
        PhotoDotsAdapter photoDotsAdapter = new PhotoDotsAdapter(getContext(), photoFileName.length, 0);
        photoDotsAdapter.fillLayout(dots);

        ImageAdapter adapter = new ImageAdapter(getContext(), photoFileName, photoDir);

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }

            @Override
            public void onPageSelected(int i) {
                photoDotsAdapter.changePosition(i);
            }
        });
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
        GridView gdNomen = getActivity().getWindow().getDecorView().findViewById(R.id.listContrs);
        myNom.requery();
        if (NomenAdapter != null)
            NomenAdapter.notifyDataSetChanged();
        gdNomen.invalidateViews();
    }

    private NomenAdapterSQLite getNomenAdapter(Cursor cursor) {
        return new NomenAdapterSQLite(getContext(), R.layout.nomen_layout, cursor, new String[]{"_id", "KOD5", "DESCR", "OST", "ZAKAZ", "GRUPPA", "SGI", "FOTO", "GOFRA", "MP", TypeOfPrice}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomPhoto, R.id.ColNomVkorob, R.id.ColNomMP, R.id.ColNomPrice}, 0);
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
            nomenList.setOnItemClickListener(GridNomenClick);
            nomenList.setOnItemLongClickListener(PreviewNomenLongClick);
        });
    }

    public void LoadNomen(String... args) {
        String[] formattedArgs = new String[5];
        System.arraycopy(args, 0, formattedArgs, 0, args.length);
        for (int i = args.length; i < formattedArgs.length; i++) {
            formattedArgs[i] = "0";
        }

        CurSGI = formattedArgs[0];
        CurGroup = formattedArgs[1];
        CurWCID = formattedArgs[2];
        CurFocusID = formattedArgs[3];
        CurSearchName = formattedArgs[4].toLowerCase(Locale.ROOT);

        new Thread(new Runnable() {
            @Override
            public void run() {
                new ProgressBarShower(getContext()).setFunction(() -> {
                    myNom = db.getNomen(
                            CurSGI, CurGroup,
                            CurWCID, CurFocusID, CurSearchName);
                    NomenAdapter = getNomenAdapter(myNom);
                    NomenAdapter.setToolbar(toolbar);
                    NomenAdapter.setDbHelper(db);
                    getActivity().runOnUiThread(() -> {
                        nomenList.setAdapter(null);
                        nomenList.setAdapter(NomenAdapter);
                        nomenList.setOnItemClickListener(GridNomenClick);
                        nomenList.setOnItemLongClickListener(GridNomenLongClick);

                        NomenAdapter.notifyDataSetChanged();
                        setPositionAfterGoToGroup();
                    });
                    return null;
                }).start();
            }

            private void setPositionAfterGoToGroup() {
                System.out.println(CurSGI + " " + CurGroup);
                if (isNeededToSelectRowAfterGoToGroup) {
                    int[] elementPositionData = getPositionByKod5(kod5);
                    int elementPosition = elementPositionData[0];
                    System.out.println(elementPosition + " " + kod5);
                    int visibleElementsCount = nomenList.getLastVisiblePosition() - nomenList.getFirstVisiblePosition() + 1;
                    if (elementPosition == -1) {
                        elementPosition = 0;
                    } else if (elementPositionData[1] == 1) {
                        elementPosition += visibleElementsCount;
                    }
                    nomenList.setSelection(elementPosition);
                    isNeededToSelectRowAfterGoToGroup = false;
                    kod5 = "";
                }
            }

            private int[] getPositionByKod5(String kod5) {
                int i = 0;
                while (myNom.moveToNext()) {
                    if (myNom.getString(myNom.getColumnIndex("KOD5")).equals(kod5)) {
                        return new int[]{i, 1};
                    }
                    i++;
                }

                for (int j = 0; j <= nomenList.getLastVisiblePosition(); j++) {
                    RelativeLayout layout = (RelativeLayout) nomenList.getChildAt(j);
                    String localKod5 = ((TextView) layout.findViewById(R.id.ColNomCod)).getText().toString();
                    if (localKod5.equals(kod5)) {
                        return new int[]{j, 0};
                    }
                }
                return new int[]{-1, 0};
            }
        }).start();
    }

    public AdapterView.OnItemClickListener GridNomenClick = (myAdapter, myView, position, mylng) -> {
        try {
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

                String ID = tvKOD5.getText().toString();
                int ost = Integer.parseInt(tvOST.getText().toString());

                if (isMultiSelect) {
                    multiSelect(ID, ost);
                } else {
                    fillNomenDataFromAlertDialog(ID, ost);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Config.sout(e);
        }
    };

    public AdapterView.OnLongClickListener PhotoLongClick = new AdapterView.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            try {
                TextView tvKod5 = ((RelativeLayout) view.getParent()).findViewById(R.id.ColNomCod);
                String kod5 = tvKod5.getText().toString();

                PopupMenu nomPopupMenu = new PopupMenu(getContext(), view);
                nomPopupMenu.getMenuInflater().inflate(R.menu.photo_context_menu, nomPopupMenu.getMenu());

                nomPopupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.forceDownloadPhoto) {
                        @SuppressLint({"NewApi", "LocalSuppress"}) String[] fileNames = db.getPhotoNames(kod5);
                        downloadAndShowPhotos(fileNames, kod5, true);
                    }
                    return true;
                });
                nomPopupMenu.show();
            } catch (Exception e) {
                e.printStackTrace();
                Config.sout(e);
            }
            return true;
        }
    };

    public AdapterView.OnItemLongClickListener GridNomenLongClick = new AdapterView.OnItemLongClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, final View myView, final int position, long arg3) {
            try {
                String group;
                String sgi;

                Cursor c = myNom;
                group = c.getString(c.getColumnIndex("GRUPPA"));
                sgi = c.getString(c.getColumnIndex("SGI"));

                PopupMenu nomPopupMenu = new PopupMenu(getContext(), myView);
                nomPopupMenu.getMenuInflater().inflate(R.menu.nomen_context_menu, nomPopupMenu.getMenu());
                if (NomenAdapter.beginPos != 0) {
                    nomPopupMenu.getMenu().findItem(R.id.setBeginPos).setTitle("Установить как начальную позицию. (сейчас установлена " + BeginPos + ")");
                }

                if (NomenAdapter.endPos != 0) {
                    nomPopupMenu.getMenu().findItem(R.id.setEndPos).setTitle("Установить как конечную позицию. (сейчас установлена " + EndPos + ")");
                }

                nomPopupMenu.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.resetBeginEndPos:
                            NomenAdapter.beginPos = 0;
                            NomenAdapter.endPos = 0;
                            return true;
                        case R.id.setBeginPos:
                            NomenAdapter.beginPos = position + 1;
                            NomenAdapter.notifyDataSetChanged();
                            return true;
                        case R.id.setEndPos:
                            NomenAdapter.endPos = position + 1;
                            NomenAdapter.notifyDataSetChanged();
                            return true;
                        case R.id.goToGroup:
                            isNeededToSelectRowAfterGoToGroup = true;
                            kod5 = c.getString(c.getColumnIndex("KOD5"));

                            if (spWC != null) {
                                spWC.setSelection(0);
                                spFocus.setSelection(0);
                            }
                            filter.setImageResource(R.drawable.filter);
                            isFiltered = false;

                            System.out.println(CurSGI + " " + sgi);
                            if (!CurSGI.equals(sgi)) {
                                allowUpdate = false;
                            }
                            resetCurData();
                            resetSearchViewData();

                            setSelectionByCodeSgiAsync(sgi);
                            new Handler().postDelayed(() -> setSelectionByCodeGroupAsync(group), 500);

                            return true;
                    }
                    return true;
                });
                nomPopupMenu.show();
            } catch (Exception e) {
                e.printStackTrace();
                Config.sout(e);
            }

            return true;
        }
    };


    public AdapterView.OnItemLongClickListener PreviewNomenLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, final View myView, int position, long arg3) {
            try {
                TextView groupID = myView.findViewById(R.id.ColNomGRUPID);
                TextView sgiID = myView.findViewById(R.id.ColNomSGIID);

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
                Config.sout(e);
            }

            return true;
        }
    };

    private void plusQTY(View myView) {
        View parent = (View) myView.getParent();
        TextView tvKOD5 = parent.findViewById(R.id.ColNomCod);
        TextView tvPrice = parent.findViewById(R.id.ColNomPrice);

        String price = tvPrice.getText().toString();
        String kod5 = tvKOD5.getText().toString();
        db.putPriceInNomen(kod5, price);
        db.PlusQty(kod5);
        if (myNom != null)
            myNom.requery();

        if (NomenAdapter != null)
            NomenAdapter.notifyDataSetChanged();

        if (PreviewZakazAdapter != null)
            PreviewZakazAdapter.notifyDataSetChanged();
    }

    private void minusQTY(View myView) {
        View parent = (View) myView.getParent();
        TextView tvKOD5 = parent.findViewById(R.id.ColNomCod);
        String kod5 = tvKOD5.getText().toString();

        db.MinusQty(kod5);
        if (myNom != null)
            myNom.requery();

        if (NomenAdapter != null)
            NomenAdapter.notifyDataSetChanged();

        if (PreviewZakazAdapter != null)
            PreviewZakazAdapter.notifyDataSetChanged();
    }

    private void showPhoto(View myView, int position, AdapterView<?> myAdapter) {
        getActivity().runOnUiThread(() -> {
            if (((TextView) myView).getText() == null || ((TextView) myView).getText().toString().equals(""))
                return;

            long ID = myAdapter.getItemIdAtPosition(position);

            @SuppressLint({"NewApi", "LocalSuppress"}) String[] fileNames = db.getPhotoNames(ID);
            downloadAndShowPhotos(fileNames, ID, false);
        });
    }

    private void multiSelect(String ID, int ost) {
        db.UpdateQty(ID, MultiQty, ost);
        if (myNom != null)
            myNom.requery();
        if (NomenAdapter != null)
            NomenAdapter.notifyDataSetChanged();

        db.putPriceInNomen(ID, "" + DBHelper.pricesMap.get(ID));
        setContrAndSumValue(db, toolbar, isSales);
    }

    private void fillNomenDataFromAlertDialog(String ID, int ost) {
        getActivity().runOnUiThread(() -> {
            try {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                View promptView = layoutInflater.inflate(R.layout.change_qty, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setView(promptView);

                final EditText input = promptView.findViewById(R.id.edPPQty);
                final TextView txtCod = promptView.findViewById(R.id.txtNomCode);
                final TextView txtDescr = promptView.findViewById(R.id.txtNomDescr);
                final TextView txtOst = promptView.findViewById(R.id.txtNomOst);
                final TextView txtGrup = promptView.findViewById(R.id.txtNomGroup);

                try {
                    input.setText(myNom.getString(myNom.getColumnIndex("ZAKAZ")));
                    txtCod.setText(myNom.getString(myNom.getColumnIndex("KOD5")));
                    txtDescr.setText(myNom.getString(myNom.getColumnIndex("DESCR")));
                    txtOst.setText(myNom.getString(myNom.getColumnIndex("OST")));
                } catch (Exception e1) {
                    Config.sout(e1);
                }

                try {
                    Cursor c = db.getReadableDatabase().rawQuery("SELECT DESCR FROM GRUPS WHERE CODE=?", new String[]{myNom.getString(myNom.getColumnIndex("GRUPPA"))});
                    c.moveToNext();
                    String groupDescription = c.getString(c.getColumnIndex("DESCR"));
                    txtGrup.setText(groupDescription);
                } catch (Exception e2) {
                    Config.sout(e2);
                }

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
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

                alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    TextView tvPrice = rootView.findViewById(R.id.ColNomPrice);
                    String price = tvPrice.getText().toString();
                    db.putPriceInNomen(ID, price);

                    db.UpdateQty(ID, Integer.parseInt(input.getText().toString()), ost);
                    myNom.requery();

                    setContrAndSumValue(db, toolbar, isSales);

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
            } catch (Exception e) {
                e.printStackTrace();
                Config.sout(e);
            }
        });
    }

    public void resetCurData() {
        CurGroup = CurWCID = CurFocusID = CurSGI = "0";
        CurSearchName = "";
//        isNeededToBeLoadingBySgi = true;

        Discount = 0f;
        Menu menu = FormOrderFragment.mainMenu;
        if (menu != null && menu.size() > 1) {
            setIconColor(menu, R.id.NomenDiscount, false);
        }
    }

    public void resetAllSpinners() {
        if (spGroup != null) {
            spGroup.post(() -> spGroup.setAdapter(null));
        }

        if (spSgi != null) {
            spSgi.post(() -> spSgi.setSelection(0));
        }

        // if spWC != null, то и spFocus и другие фильтры тоже != null
        if (spWC != null) {
            spWC.setSelection(0);
            spFocus.setSelection(0);
        }
        filter.setImageResource(R.drawable.filter);
        isFiltered = false;
    }

    public void resetSearchViewData() {
        CurSearchName = "";
        SearchView searchView = getActivity().findViewById(R.id.menu_search);
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.onActionViewCollapsed();
        }
    }

    public void setSelectionByCodeSgiAsync(String sgiCode) {
        for (int i = 0; i < spSgi.getCount(); i++) {
            Cursor value = (Cursor) spSgi.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndex("CODE"));
            if (sgiCode.equals(id)) {
                int finalI = i;
                spSgi.post(() -> spSgi.setSelection(finalI));
                return;
            }
        }
    }

    //    @DelayedCalled(delay = 100)
    public void setSelectionByCodeGroupAsync(String groupCode) {
        for (int i = 0; i < spGroup.getCount(); i++) {
            Cursor value = (Cursor) spGroup.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndex("CODE"));
            if (groupCode.equals(id)) {
                int finalI = i;
                spGroup.post(() -> spGroup.setSelection(finalI));
                return;
            }
        }
    }

    public void setIconColor(Menu menu, int MenuItem, Boolean vis) {
        getActivity().runOnUiThread(() -> {
            if (menu.findItem(MenuItem) == null)
                return;
            Drawable drawable = menu.findItem(MenuItem).getIcon();
            drawable.mutate();
            if (vis) {
                drawable.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
            } else {
                drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
        });
//        isDiscount = vis;
    }

    public static boolean allowUpdate = true;
    public AdapterView.OnItemSelectedListener SelectedGroup = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            try {
                if (!isCleared) {
                    resetSearchViewData();
                }
                isCleared = false;

                CurGroup = myGroup.getString(myGroup.getColumnIndex("CODE"));

                if (allowUpdate) {
                    LoadNomen(CurSGI, CurGroup, CurWCID, CurFocusID, CurSearchName);
                }
                allowUpdate = true;

                isSorted = false;
                FormOrderFragment.mainMenu.findItem(R.id.NomenSort).setIcon(R.drawable.to_end);
            } catch (Exception e) {
                e.printStackTrace();
                Config.sout(e);
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    public AdapterView.OnItemSelectedListener SelectedSgi = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            try {
                if (isNeededToResetSearchView) {
                    resetSearchViewData();
                }
                isNeededToResetSearchView = true;
                String ItemID = mySgi.getString(mySgi.getColumnIndex("CODE"));

                CurGroup = "0";
                CurSGI = ItemID;

                if (ItemID.equals("0")) {
                    nomenList.setAdapter(null);
                    spGroup.setAdapter(null);
                }
                LoadGroups(ItemID);

                if (!CurWCID.equals("0") || !CurFocusID.equals("0") || !CurSearchName.equals("")) {
                    LoadNomen(CurSGI, CurGroup, CurWCID, CurFocusID, CurSearchName);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Config.sout(e);
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    //    @AsyncUI
    public void LoadSgi() {
        if (mySgi != null) {
            mySgi.close();
        }
        mySgi = db.getAllSgi();
        spSgi = getActivity().findViewById(R.id.SpinSgi);
        android.widget.SimpleCursorAdapter adapter = new android.widget.SimpleCursorAdapter(getContext(), R.layout.sgi_layout, mySgi, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColSgiID, R.id.ColSgiDescr}, 0);

        spSgi.setAdapter(adapter);
//        spSgi.post(() -> spSgi.setOnItemSelectedListener(SelectedSgi));
        spSgi.setOnItemSelectedListener(SelectedSgi);
    }

    //    @AsyncUI
    public void LoadGroups(String SgiID) {
        if (myGroup != null) {
            myGroup.close();
        }
        myGroup = db.getGroupsBySgi(SgiID);
        spGroup = getActivity().findViewById(R.id.SpinGrups);
        android.widget.SimpleCursorAdapter adapter = new android.widget.SimpleCursorAdapter(getContext(), R.layout.grup_layout, myGroup, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColGrupID, R.id.ColGrupDescr}, 0);

        spGroup.setAdapter(adapter);
//        spGroup.post(() -> spGroup.setOnItemSelectedListener(SelectedGroup));
        spGroup.setOnItemSelectedListener(SelectedGroup);
    }

    public void LoadFiltersWC(View vw) {
        getActivity().runOnUiThread(() -> {
            if (myWC != null) {
                myWC.close();
            }
            myWC = dbApp.getWCs();
            spWC = vw.findViewById(R.id.spinWC);
            android.widget.SimpleCursorAdapter adapter = new android.widget.SimpleCursorAdapter(getContext(), R.layout.wc_layout, myWC, new String[]{"_id", "DEMP"}, new int[]{R.id.ColWCID, R.id.ColWCDescr}, 0);
            spWC.setAdapter(adapter);
        });
    }

    public void LoadFiltersFocus(View vw) {
        getActivity().runOnUiThread(() -> {
            if (myFocus != null) {
                myFocus.close();
            }
            myFocus = db.getFocuses();
            spFocus = vw.findViewById(R.id.spinFocus);
            android.widget.SimpleCursorAdapter adapter;
            adapter = new android.widget.SimpleCursorAdapter(getContext(), R.layout.focus_layout, myFocus, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColFocusID, R.id.ColFocusDescr}, 0);
            spFocus.setAdapter(adapter);
        });
    }

    public void setSelectionByCodeSgi(String sgiCode) {
        for (int i = 0; i < spSgi.getCount(); i++) {
            Cursor value = (Cursor) spSgi.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndex("CODE"));
            if (sgiCode.equals(id)) {
                spSgi.setSelection(i);
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

    public void UpdateNomenRange(int beginRange, int endRange, int qty) {

        new Thread(() -> {
            new ProgressBarShower(getContext()).setFunction(() -> {
                int EndRange = endRange;
                int BeginRange = beginRange;
                String sql_update = "UPDATE Nomen SET ZAKAZ = " + qty + " WHERE ROWID=?";
                SQLiteStatement stmt = db.getWritableDatabase().compileStatement(sql_update);
                db.getWritableDatabase().beginTransaction();
                int tmpVal;

                if (EndRange > NomenAdapterSQLite.CurVisiblePosition) {
                    EndRange = NomenAdapterSQLite.CurVisiblePosition;
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

                getActivity().runOnUiThread(() -> {
                    myNom.requery();
                    NomenAdapter.notifyDataSetChanged();
//                    setContrAndSum(GlobalVars.this);
                });
                return null;
            }).start();
        }).start();
    }

    public void CalculatePercentSale(final Menu menu) {
        LayoutInflater SaleInf = LayoutInflater.from(getActivity());
        final View SaleMarkupView;

        SaleMarkupView = SaleInf.inflate(R.layout.discount_dlg, null);
        AlertDialog.Builder SaleDlg = new AlertDialog.Builder(getActivity());
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

    public void putAllPrices() {
        new Thread(() -> {
           new ProgressBarShower(getContext()).setFunction(() -> {
                db.putAllNomenPrices(OrderHeadFragment.CONTR_ID);
                getActivity().runOnUiThread(() -> {
                    if (NomenAdapter != null) {
                        NomenAdapter.notifyDataSetChanged();
                    }
                });
                return null;
            }).start();
        }).start();
    }

    public void closeCursors() {
        Cursor[] cursors = new Cursor[]{
                myNom,
                mySgi,
                myGroup,
                myWC,
                myFocus
        };
        if (myNom != null) {
            getActivity().runOnUiThread(() -> nomenList.setAdapter(null));
        }
        for (Cursor cursor : cursors) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
