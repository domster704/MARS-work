package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amber.armtp.Config;
import com.amber.armtp.GlobalVars;
import com.amber.armtp.R;
import com.amber.armtp.annotations.PGShowing;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.interfaces.TBUpdate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


/**
 * Updated by domster704 on 27.09.2021
 */
public class FormOrderFragment extends Fragment implements View.OnClickListener, TBUpdate {
    public static Menu mainMenu;
    public static ImageButton filter;
    public static ImageButton spinnerClearing;
    public static boolean isSorted = false;
    public static boolean isContrIdDifferent = false;
    public GlobalVars glbVars;

    SharedPreferences settings;
    SharedPreferences.Editor editor;
    SearchView searchView;

    public static boolean isFiltered = false;

    private final SearchView.OnQueryTextListener searchTextListener =
            new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.equals("") && !GlobalVars.CurSearchName.equals("")) {
                        glbVars.LoadNomen(GlobalVars.CurSGI, GlobalVars.CurGroup, GlobalVars.CurWCID, GlobalVars.CurFocusID, newText);
                    }
                    GlobalVars.CurSearchName = newText;
                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    glbVars.LoadNomen(GlobalVars.getCurrentData());
                    Config.hideKeyBoard();
                    searchView.clearFocus();
                    return true;
                }
            };
    MenuItem searchItem;
    View thisView;
    TextView txtSgi, txtGroup, tvHeadCod, tvHeadDescr, tvHeadMP, tvHeadZakaz;
    TextView FilterWC_ID, FilterFocus_ID;
    private android.support.v7.widget.Toolbar toolbar;

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);

        glbVars.nomenList = getActivity().findViewById(R.id.listContrs);
        glbVars.spSgi = getActivity().findViewById(R.id.SpinSgi);
        glbVars.spGroup = getActivity().findViewById(R.id.SpinGrups);

        tvHeadCod = getActivity().findViewById(R.id.tvHeadCod);
        tvHeadDescr = getActivity().findViewById(R.id.tvHeadDescr);
        tvHeadMP = getActivity().findViewById(R.id.tvHeadMP);
        tvHeadZakaz = getActivity().findViewById(R.id.tvHeadZakaz);

        settings = getActivity().getSharedPreferences("form_order", 0);
        editor = settings.edit();

        glbVars.LoadSgi();

        filter = getActivity().findViewById(R.id.NomenFilters);
        filter.setOnClickListener(this);

        spinnerClearing = getActivity().findViewById(R.id.SGIClear);
        spinnerClearing.setOnClickListener(this);

        setContrAndSum(glbVars);
        if (getArguments() != null && getArguments().size() != 0 && getArguments().containsKey("SGI")) {
            String sgi = getArguments().getString("SGI");
            String group = getArguments().getString("Group");

            glbVars.resetCurData();
            glbVars.resetSearchViewData();

            glbVars.SetSelectedSgi(sgi);
            glbVars.SetSelectedGroup(group);

            getArguments().remove("SGI");
            getArguments().remove("Group");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.form_order_fragment, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setHasOptionsMenu(true);
        thisView = rootView;
        glbVars.CurView = rootView;

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        glbVars = (GlobalVars) getActivity().getApplicationContext();
        glbVars.setContext(getActivity().getApplicationContext());
        GlobalVars.CurFragmentContext = getActivity();
        GlobalVars.CurAc = getActivity();

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
        searchItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Поиск номенклатуры");
        searchView.setOnQueryTextListener(searchTextListener);
        searchView.setOnCloseListener(() -> {
            glbVars.LoadNomen(GlobalVars.CurSGI, GlobalVars.CurGroup, GlobalVars.CurWCID, GlobalVars.CurFocusID, "");
            return false;
        });

        if (glbVars.NomenAdapter != null) {
            glbVars.NomenAdapter.notifyDataSetChanged();
        }

        setContrAndSum(glbVars);
        if (glbVars.isDiscount) {
            glbVars.isDiscount = false;
            glbVars.Discount = 0;
        }
    }

    public void SaveOrder() {
        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                try {
                    //                for (int i = 0; i < 100; i++) {
                    Cursor orderHeader,
//                            nomenCountOld,
                            nomenCount;
                    String TP_ID, Contr_ID, Address_ID, Data, Comment, IDDOC = "";
                    String contrDes, addressDes;
                    String status = "Сохранён";
                    float Sum = 0f;

                    String sql;
                    SQLiteStatement stmt;

                    @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                    String curDate = df.format(Calendar.getInstance().getTime());

                    orderHeader = glbVars.db.getReadableDatabase().rawQuery("SELECT TORG_PRED.CODE as TP_ID, ORDERS.DATA as DATA, ORDERS.COMMENT as COMMENT, CONTRS.CODE AS CONTR_ID, ADDRS.CODE AS ADDR_ID, CONTRS.DESCR as C_DES, ADDRS.DESCR as A_DES FROM ORDERS JOIN TORG_PRED ON ORDERS.TP=TORG_PRED.CODE JOIN CONTRS ON ORDERS.CONTR=CONTRS.CODE JOIN ADDRS ON ORDERS.ADDR=ADDRS.CODE", null);
                    nomenCount = glbVars.db.getReadableDatabase().rawQuery("SELECT 0 AS _id, CASE WHEN COUNT(ROWID) IS NULL THEN 0 ELSE COUNT(ROWID) END AS COUNT FROM Nomen WHERE ZAKAZ<>0", null);

                    if (orderHeader.getCount() == 0) {
                        Config.sout("Не заполнена шапка заказа");
                        return;
                    }

                    nomenCount.moveToFirst();
                    if (nomenCount.getInt(1) == 0) {
                        Config.sout("Нет ни одного добавленного товара для заказа");
                        return;
                    } else {
                        nomenCount.close();
                    }

                    orderHeader.moveToNext();
                    TP_ID = orderHeader.getString(orderHeader.getColumnIndex("TP_ID"));
                    Data = orderHeader.getString(orderHeader.getColumnIndex("DATA"));
                    Comment = orderHeader.getString(orderHeader.getColumnIndex("COMMENT"));
                    Contr_ID = orderHeader.getString(orderHeader.getColumnIndex("CONTR_ID"));
                    Address_ID = orderHeader.getString(orderHeader.getColumnIndex("ADDR_ID"));
                    contrDes = orderHeader.getString(orderHeader.getColumnIndex("C_DES"));
                    addressDes = orderHeader.getString(orderHeader.getColumnIndex("A_DES"));

                    orderHeader.close();

                    @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("HHmmss");
                    String dateForIDDOC = dateFormat.format(Calendar.getInstance().getTimeInMillis()) + Calendar.getInstance().get(Calendar.MILLISECOND);

                    IDDOC += TP_ID + "_" + Data.replace(".", "") + "_" + dateForIDDOC;

//                    nomenCount = glbVars.db.getReadableDatabase().rawQuery("SELECT KOD5 FROM Nomen where ZAKAZ<>0", null);
//                    if (nomenCount.getCount() == 0) {
//                        nomenCount.close();
//                        return;
//                    } else {
                    Sum = insertIntoOrderDT(IDDOC, Sum);
//                    }

                    sql = "INSERT INTO ZAKAZY(DOCID, TP, CONTR, ADDR, DOC_DATE, DELIVERY_DATE, COMMENT, STATUS, CONTR_DES, ADDR_DES, SUM)  VALUES (?,?,?,?,?,?,?,?,?,?,?);";
                    stmt = glbVars.dbOrders.getWritableDatabase().compileStatement(sql);
                    glbVars.dbOrders.getWritableDatabase().beginTransaction();
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
                        e.printStackTrace();
                    } finally {
                        glbVars.dbOrders.getWritableDatabase().setTransactionSuccessful();
                        glbVars.dbOrders.getWritableDatabase().endTransaction();
                    }
//                }
                    glbVars.db.ClearOrderHeader();
                    glbVars.db.ResetNomen();

                    getActivity().runOnUiThread(() -> {
                        try {
                            Config.sout("Заказ сохранён");
                            glbVars.closeCursors();

                            editor.putString("ColSgiID", "0");
                            editor.commit();

                            Fragment fragment = new JournalFragment();

                            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.frame, fragment, "frag_journal");
                            fragmentTransaction.commit();
                            toolbar.setTitle(R.string.journal);
                        } catch (Exception e) {
                            Config.sout(e.getMessage());
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    Config.sout(e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void SaveEditOrder(final String OrderID) {
        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                try {
                    float Sum = 0f;
                    OrderHeadFragment.isNeededToUpdateOrderTable = false;

                    Cursor orderHeader = glbVars.db.getReadableDatabase().rawQuery("SELECT TORG_PRED.CODE as TP_ID, ORDERS.DATA as DATA, ORDERS.COMMENT as COMMENT, CONTRS.CODE AS CONTR_ID, ADDRS.CODE AS ADDR_ID, CONTRS.DESCR as C_DES, ADDRS.DESCR as A_DES FROM ORDERS JOIN TORG_PRED ON ORDERS.TP=TORG_PRED.CODE JOIN CONTRS ON ORDERS.CONTR=CONTRS.CODE JOIN ADDRS ON ORDERS.ADDR=ADDRS.CODE", null);
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

                    orderHeader.close();

                    Cursor orderCount = glbVars.db.getReadableDatabase().rawQuery("SELECT 0 AS _id, CASE WHEN COUNT(ROWID) IS NULL THEN 0 ELSE COUNT(ROWID) END AS COUNT FROM Nomen WHERE ZAKAZ<>0", null);
                    orderCount.moveToFirst();
                    if (orderCount.getInt(1) == 0) {
                        Config.sout("Нет ни одного добавленного товара для заказа", Toast.LENGTH_LONG);
                        return;
                    }
                    orderCount.close();

                    glbVars.dbOrders.getWritableDatabase().execSQL("DELETE FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + OrderID + "'");
                    Sum = insertIntoOrderDT(OrderID, Sum);

                    String sql = "UPDATE ZAKAZY SET TP=?, CONTR=?, ADDR=?, DELIVERY_DATE=?, COMMENT=?, CONTR_DES=?, ADDR_DES=?, SUM=?  WHERE DOCID='" + OrderID + "'";
                    SQLiteStatement stmt = glbVars.dbOrders.getWritableDatabase().compileStatement(sql);
                    glbVars.dbOrders.getWritableDatabase().beginTransaction();
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
                        stmt.executeInsert();
                        stmt.clearBindings();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        glbVars.dbOrders.getWritableDatabase().setTransactionSuccessful();
                        glbVars.dbOrders.getWritableDatabase().endTransaction();
                    }

                    glbVars.db.ClearOrderHeader();
                    glbVars.db.ResetNomen();

                    getActivity().runOnUiThread(() -> {
                        try {
                            Toast.makeText(getActivity(), "Заказ сохранён", Toast.LENGTH_LONG).show();
                            glbVars.closeCursors();

                            Fragment fragment = new JournalFragment();

                            Bundle args = new Bundle();
                            args.putBoolean("isStartDeletingExtraOrders", true);
                            fragment.setArguments(args);

                            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.frame, fragment, "frag_journal");
                            fragmentTransaction.commit();
                            toolbar.setTitle(R.string.journal);
                        } catch (Exception e) {
                            Config.sout(e.getMessage());
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    Config.sout(e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
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
                    Config.sout(e.getMessage());
                }
                return true;
            case R.id.NomenSave:
                try {
                    isFiltered = false;

                    glbVars.resetCurData();

                    if (glbVars.isSales) {
                        putRealPriceInPriceColumn();
                    }

                    if (OrderHeadFragment.isNeededToUpdateOrderTable) {
                        SaveEditOrder(glbVars.OrderID);
                    } else {
                        SaveOrder();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Config.sout(e.getMessage());
                }
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
                    edBeginPP.setText((glbVars.BeginPos != 0 ? String.valueOf(glbVars.BeginPos) : "0"));
                    edEndPP.setText((glbVars.EndPos != 0 ? String.valueOf(glbVars.EndPos) : "0"));
                    edPPQty.setText("0");

                    RangeDlg.setCancelable(true)
                            .setPositiveButton("OK", (dialog, id) -> {
                            })
                            .setNegativeButton("Отмена", (dialog, id) -> dialog.cancel());

                    final AlertDialog alertDlg = RangeDlg.create();
                    Objects.requireNonNull(alertDlg.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

                    alertDlg.show();

                    EditText edInput;

                    if (glbVars.BeginPos != 0 && glbVars.EndPos != 0) {
                        edInput = edPPQty;
                    } else if (glbVars.BeginPos != 0) {
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
                    InputMethodManager immPP = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
                    immPP.showSoftInput(edInput, InputMethodManager.SHOW_IMPLICIT);

                    alertDlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                        if (glbVars.NomenAdapter == null || glbVars.myNom == null || glbVars.myNom.getCount() == 0) {
                            Config.sout("Таблица товаров пуста");
                            alertDlg.dismiss();
                            return;
                        }
                        if (!edBeginPP.getText().toString().equals("") && !edEndPP.getText().toString().equals("") && !edPPQty.getText().toString().equals("")) {
                            glbVars.UpdateNomenRange(Integer.parseInt(edBeginPP.getText().toString()), Integer.parseInt(edEndPP.getText().toString()), Integer.parseInt(edPPQty.getText().toString()));
                            glbVars.BeginPos = 0;
                            glbVars.EndPos = 0;
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
                    Config.sout(e.getMessage());
                }
                return true;
            case R.id.NomenSort:
                try {
                    if (glbVars.NomenAdapter == null)
                        return true;

                    if (!isSorted) {
                        item.setIcon(R.drawable.to_top);
                        glbVars.nomenList.setAdapter(glbVars.NomenAdapter);
                        glbVars.nomenList.setSelection(glbVars.nomenList.getCount());
                        isSorted = true;
                    } else {
                        item.setIcon(R.drawable.to_end);
                        glbVars.nomenList.setAdapter(glbVars.NomenAdapter);
                        glbVars.nomenList.setSelection(0);
                        isSorted = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Config.sout(e.getMessage());
                }
                return true;
            case R.id.NomenDiscount:
                try {
                    glbVars.CalculatePercentSale(mainMenu);
                } catch (Exception e) {
                    e.printStackTrace();
                    Config.sout(e.getMessage());
                }
                return true;
            case R.id.NomenMultiSelect:
                try {
                    if (!glbVars.isMultiSelect) {
                        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                        View promptView;

                        promptView = layoutInflater.inflate(R.layout.multi_qty, null);
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setView(promptView);

                        final EditText input = promptView.findViewById(R.id.txtPercent);
                        input.setText(String.valueOf(glbVars.MultiQty));

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
                        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

                        alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                            glbVars.isMultiSelect = true;
                            glbVars.MultiQty = Integer.parseInt(input.getText().toString());
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
                        glbVars.isMultiSelect = false;
                        item.setIcon(getResources().getDrawable(R.drawable.checkbox_free));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Config.sout(e.getMessage());
                }

                return true;
            case R.id.NomenSales:
                // ИП Лужбина Н.М.
                // ИП Беляев В.В.
                try {
                    glbVars.isSales = !glbVars.isSales;
                    glbVars.setIconColor(mainMenu, R.id.NomenSales, glbVars.isSales);

                    if (isContrIdDifferent || DBHelper.pricesMap.size() == 0) {
                        isContrIdDifferent = false;
                        glbVars.putAllPrices();
                    } else if (glbVars.NomenAdapter != null) {
                        glbVars.NomenAdapter.notifyDataSetChanged();
                    } else {
                        Config.sout("Ошибка считывания таблицы заказов");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Config.sout(e.getMessage());
                }

                return true;
            case R.id.clear_whole_order:
                try {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Очистка заказа")
                            .setMessage("Вы уверены, что хотите очистить текущий заказ?")
                            .setPositiveButton("Да", (dialogInterface, i) -> {
                                glbVars.db.clearOrder();
                                if (glbVars.NomenAdapter != null) {
                                    glbVars.NomenAdapter.notifyDataSetChanged();
                                }
                                if (glbVars.myNom != null) {
                                    glbVars.myNom.requery();
                                }
                                setContrAndSum(glbVars);
                            })
                            .setNeutralButton("Нет", (dialogInterface, i) -> dialogInterface.dismiss())
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Config.sout(e.getMessage());
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
        setContrAndSum(glbVars);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        glbVars.resetCurData();

        glbVars.setIconColor(mainMenu, R.id.NomenSales, false);
        glbVars.isSales = false;
        glbVars.MultiQty = 0;
//        resetLocalData();

        try {
            isFiltered = false;
            filter.setImageResource(R.drawable.filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        glbVars.closeCursors();
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
                glbVars.LoadFiltersWC(promptView);
                glbVars.LoadFiltersFocus(promptView);

                String WCID = glbVars.dbApp.getIDByWC(GlobalVars.CurWCID);
                String FocusID = GlobalVars.CurFocusID;

                glbVars.SetSelectedFilterWC(WCID);
                glbVars.SetSelectedFilterFocus(FocusID);

                alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    isFiltered = true;
                    FilterWC_ID = promptView.findViewById(R.id.ColWCID);
                    FilterFocus_ID = promptView.findViewById(R.id.ColFocusID);

                    if (!FilterFocus_ID.getText().toString().equals("0") || !FilterWC_ID.getText().toString().equals("0")) {
                        filter.setImageResource(R.drawable.filter_activated);
                    }

                    String SgiId = "0";
                    String GroupID = "0";

                    String WC_ID = glbVars.dbApp.getWCByID(FilterWC_ID.getText().toString());

                    glbVars.LoadNomen(SgiId, GroupID,
                            WC_ID, FilterFocus_ID.getText().toString(), GlobalVars.CurSearchName);
                    glbVars.SetSelectedSgi(SgiId);
                    alertD.dismiss();
                });

                alertD.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                    isFiltered = false;
                    filter.setImageResource(R.drawable.filter);

                    glbVars.SetSelectedFilterWC("0");
                    glbVars.SetSelectedFilterFocus("0");
                    GlobalVars.CurFocusID = GlobalVars.CurWCID = "0";
                });
                break;
            case R.id.SGIClear:
                String localSearchName = GlobalVars.CurSearchName;

                glbVars.resetCurData();
                glbVars.isNeededToResetSearchView = false;
                glbVars.resetAllSpinners();

                glbVars.nomenList.setAdapter(null);
                glbVars.myNom = null;

                GlobalVars.CurSearchName = localSearchName;
                searchView.setQuery(localSearchName, true);
                break;
        }
    }

    private float insertIntoOrderDT(String docID, float SUM) {
        Cursor nomenData;
        if (glbVars.isSales) {
            nomenData = glbVars.db.getReadableDatabase().rawQuery("SELECT KOD5, DESCR, ZAKAZ, PRICE FROM Nomen WHERE ZAKAZ<>0", null);
        } else {
            nomenData = glbVars.db.getReadableDatabase().rawQuery("SELECT KOD5, DESCR, ZAKAZ, [" + GlobalVars.TypeOfPrice + "] as PRICE FROM Nomen WHERE ZAKAZ<>0", null);
        }

        glbVars.dbOrders.getWritableDatabase().beginTransaction();
        for (int i = 0; i < nomenData.getCount(); i++) {
            nomenData.moveToNext();
            String KOD5 = nomenData.getString(nomenData.getColumnIndex("KOD5"));
            String DESCR = nomenData.getString(nomenData.getColumnIndex("DESCR"));
            String ZAKAZ = nomenData.getString(nomenData.getColumnIndex("ZAKAZ"));
            String PRICE = nomenData.getString(nomenData.getColumnIndex("PRICE"));
            float sum = Float.parseFloat(PRICE.replace(",", ".")) * Integer.parseInt(ZAKAZ);
            SUM += sum;
            glbVars.dbOrders.getWritableDatabase().execSQL("INSERT INTO ZAKAZY_DT (ZAKAZ_ID, NOMEN, DESCR, QTY, PRICE, SUM) VALUES('" + docID + "','" + KOD5 + "','" + DESCR + "','" + ZAKAZ + "','" + PRICE + "','" + String.format(Locale.ROOT, "%.2f", sum) + "')");
        }

        nomenData.close();
        glbVars.dbOrders.getWritableDatabase().setTransactionSuccessful();
        glbVars.dbOrders.getWritableDatabase().endTransaction();
        return SUM;
    }

    private void putRealPriceInPriceColumn() {
        SQLiteDatabase database = glbVars.db.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT KOD5 FROM Nomen WHERE ZAKAZ <> 0", null);

        database.beginTransaction();
        while (cursor.moveToNext()) {
            String kod5 = cursor.getString(0);
            database.execSQL("UPDATE NOMEN SET PRICE=? WHERE KOD5=?", new Object[]{DBHelper.pricesMap.get(kod5), kod5});
        }
        cursor.close();
        database.setTransactionSuccessful();
        database.endTransaction();
    }
}
