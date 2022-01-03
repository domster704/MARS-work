package com.amber.armtp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;


/**
 * Updated by domster704 on 27.09.2021
 */
public class FormOrderFragment extends Fragment implements View.OnClickListener {
    public GlobalVars glbVars;
    public static Menu mainMenu;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    SearchView searchView;

    public static ImageButton filter;
    public static boolean isSorted = false;

    private boolean isSaved = false;

    private final SearchView.OnQueryTextListener searchTextListener =
            new SearchView.OnQueryTextListener() {
                boolean isSearchClicked = false;

                @Override
                public boolean onQueryTextChange(String newText) {
                    String ItemID = "";
                    if (glbVars.myGrups != null) {
                        ItemID = glbVars.myGrups.getString(glbVars.myGrups.getColumnIndex("CODE"));
                    }

                    final String curSgi = "";

//                    TextView txtSgi = getView().findViewById(R.id.ColSgiID);
//                    curSgi = txtSgi.getText().toString();

                    if (newText.equals("")) {
                        if (!isSearchClicked) {
                            glbVars.LoadNom(ItemID, curSgi);
                            searchView.clearFocus();
                            searchView.setIconified(true);
                        }
                        return true;
                    } else {
                        if (newText.length() >= 1) {
                            if (!ItemID.equals("0")) {
                                glbVars.SearchNomInGroup(newText, ItemID);
                            } else {
                                glbVars.LoadNom(ItemID, curSgi);
                            }
                            return true;
                        } else {
                            return false;
                        }
                    }
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!query.equals("")) {
                        glbVars.SearchNom(query);
                        glbVars.spSgi.setSelection(0);
                        glbVars.spGrup.setAdapter(null);
                        isSearchClicked = true;
                        searchView.clearFocus();
                        searchView.setIconified(true);
                        return true;
                    } else {
                        return false;
                    }
                }
            };
    MenuItem searchItem;
    View thisView;
    TextView txtSgi, txtGroup, tvHeadCod, tvHeadDescr, tvHeadMP, tvHeadZakaz;
    TextView FilterSgi_ID, FilterGroup_ID, FilterTovcat_ID, FilterFunc_ID, FilterBrand_ID, FilterWC_ID, FilterProd_ID, FilterFocus_ID, FilterModel_ID, FilterColor_ID;
    private android.support.v7.widget.Toolbar toolbar;

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.NomenFilters) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            final View promptView;
            promptView = layoutInflater.inflate(R.layout.nom_filters, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setView(promptView);

            alertDialogBuilder
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .setNeutralButton("Сбросить фильтры", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
            ;

            final AlertDialog alertD = alertDialogBuilder.create();
            alertD.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            alertD.show();
            glbVars.LoadFiltersSgi(promptView);
            glbVars.LoadFiltersGroups(promptView);
//            glbVars.LoadFiltersTovcat(promptView);
            glbVars.LoadFiltersFunc(promptView);
            glbVars.LoadFiltersBrand(promptView);
            glbVars.LoadFiltersWC(promptView);
            glbVars.LoadFiltersProd(promptView);
            glbVars.LoadFiltersFocus(promptView);
            glbVars.LoadFiltersModels(promptView);
            glbVars.LoadFiltersColors(promptView);

            String SgiFID = settings.getString("ColSgiFID", "0");
            String GrupFID = settings.getString("ColGrupFID", "0");
//            String TovcatID = settings.getString("ColTovcatID", "0");
            String FuncID = settings.getString("ColFuncID", "0");
            String BrandID = settings.getString("ColBrandID", "0");
            String WCID = settings.getString("ColWCID", "0");
            String ProdID = settings.getString("ColProdID", "0");
            String FocusID = settings.getString("ColFocusID", "0");
            String ModelID = settings.getString("ColModelID", "0");
            String ColorID = settings.getString("ColColorID", "0");

            if (!SgiFID.equals("0")) {
                glbVars.SetSelectedFilterSgi(SgiFID);
            }

            if (!GrupFID.equals("0")) {
                glbVars.SetSelectedFilterGroup(GrupFID);
            }

//            if (!TovcatID.equals("0")) {
//                glbVars.SetSelectedFilterTovcat(TovcatID);
//            }

            if (!FuncID.equals("0")) {
                glbVars.SetSelectedFilterFunc(FuncID);
            }

            if (!BrandID.equals("0")) {
                glbVars.SetSelectedFilterBrand(BrandID);
            }

            if (!WCID.equals("0")) {
                glbVars.SetSelectedFilterWC(WCID);
            }

            if (!ProdID.equals("0")) {
                glbVars.SetSelectedFilterProd(ProdID);
            }

            if (!FocusID.equals("0")) {
                glbVars.SetSelectedFilterFocus(FocusID);
            }

            if (!ModelID.equals("0")) {
                glbVars.SetSelectedFilterModel(ModelID);
            }

            if (!ColorID.equals("0")) {
                glbVars.SetSelectedFilterColor(ColorID);
            }

            alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filter.setImageResource(R.drawable.filter_activated);
                    glbVars.SetSelectedSgi("0", "0");
                    glbVars.SetSelectedGrup("0");

                    FilterSgi_ID = promptView.findViewById(R.id.ColSgiFID);
                    FilterGroup_ID = promptView.findViewById(R.id.ColGroupFID);
                    FilterTovcat_ID = promptView.findViewById(R.id.ColTovcatID);
                    FilterFunc_ID = promptView.findViewById(R.id.ColFuncID);
                    FilterBrand_ID = promptView.findViewById(R.id.ColBrandID);
                    FilterWC_ID = promptView.findViewById(R.id.ColWCID);
                    FilterProd_ID = promptView.findViewById(R.id.ColProdID);
                    FilterFocus_ID = promptView.findViewById(R.id.ColFocusID);
                    FilterModel_ID = promptView.findViewById(R.id.ColModelID);
                    FilterColor_ID = promptView.findViewById(R.id.ColColorID);

                    editor.putString("ColSgiFID", FilterSgi_ID.getText().toString());
                    editor.putString("ColGrupFID", FilterGroup_ID.getText().toString());
                    editor.putString("ColTovcatID", FilterTovcat_ID.getText().toString());
                    editor.putString("ColFuncID", FilterFunc_ID.getText().toString());
                    editor.putString("ColBrandID", FilterBrand_ID.getText().toString());
                    editor.putString("ColWCID", FilterWC_ID.getText().toString());
                    editor.putString("ColProdID", FilterProd_ID.getText().toString());
                    editor.putString("ColFocusID", FilterFocus_ID.getText().toString());
                    editor.putString("ColModelID", FilterModel_ID.getText().toString());
                    editor.putString("ColColorID", FilterColor_ID.getText().toString());

                    editor.commit();
                    glbVars.LoadNomByFilters(FilterSgi_ID.getText().toString(), FilterGroup_ID.getText().toString(), FilterTovcat_ID.getText().toString(), FilterFunc_ID.getText().toString(), FilterBrand_ID.getText().toString(), FilterWC_ID.getText().toString(), FilterProd_ID.getText().toString(), FilterFocus_ID.getText().toString(), FilterModel_ID.getText().toString(), FilterColor_ID.getText().toString());
                    alertD.dismiss();
                }
            });

            alertD.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    glbVars.SetSelectedFilterSgi("0");
                    glbVars.SetSelectedFilterGroup("0");
                    glbVars.SetSelectedFilterTovcat("0");
                    glbVars.SetSelectedFilterFunc("0");
                    glbVars.SetSelectedFilterBrand("0");
                    glbVars.SetSelectedFilterWC("0");
                    glbVars.SetSelectedFilterProd("0");
                    glbVars.SetSelectedFilterFocus("0");
                    glbVars.SetSelectedFilterModel("0");
                    glbVars.SetSelectedFilterColor("0");
                }
            });
        }
    }

    public FormOrderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.form_order_fragment, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setHasOptionsMenu(true);
        thisView = rootView;
        glbVars.view = rootView;

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        glbVars = (GlobalVars) getActivity().getApplicationContext();
        glbVars.setContext(getActivity().getApplicationContext());
        glbVars.frContext = getActivity();
        glbVars.CurAc = getActivity();
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

        if (glbVars.NomenAdapter != null) {
            glbVars.NomenAdapter.notifyDataSetChanged();
        }

        setContrAndSum();
        if (glbVars.isDiscount) {
            glbVars.isDiscount = false;
            glbVars.Discount = 0;
        }
    }

    private void clearChosenGroupSgi() {
        editor.clear().apply();
        glbVars.frSgi = "";
        glbVars.frGroup = "";
        txtSgi = null;
        txtGroup = null;
    }

    public void SaveOrder() throws ParseException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor c, c2, c1;
                String TP_ID;
                String Contr_ID;
                String Addr_ID;
                String Data;
                String Comment;
                String IDDOC;
                int Status = 0;

                String sql;
                SQLiteStatement stmt;

                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                String curdate = df.format(Calendar.getInstance().getTime());

                c = glbVars.db.getReadableDatabase().rawQuery("SELECT TORG_PRED.CODE AS TP_ID, ORDERS.DATA, ORDERS.COMMENT, CONTRS.CODE AS CONTR_ID, ADDRS.CODE AS ADDR_ID FROM ORDERS JOIN TORG_PRED ON ORDERS.TP=TORG_PRED.CODE JOIN CONTRS ON ORDERS.CONTR=CONTRS.CODE JOIN ADDRS ON ORDERS.ADDR=ADDRS.CODE", null);
                c2 = glbVars.db.getReadableDatabase().rawQuery("SELECT 0 AS _id, CASE WHEN COUNT(ROWID) IS NULL THEN 0 ELSE COUNT(ROWID) END AS COUNT FROM Nomen WHERE ZAKAZ<>0", null);
                if (c.getCount() == 0) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Не заполнена шапка заказа", Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }
                c2.moveToFirst();

                if (c2.getInt(1) == 0) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Нет ни одного добавленного товара для заказа", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

//               TP_ID, ORDERS.DATA, ORDERS.COMMENT, CONTR_ID, ADDR_ID
                c.moveToFirst();
                TP_ID = c.getString(0);
                Data = c.getString(1);
                Comment = c.getString(2);
                Contr_ID = c.getString(3);
                Addr_ID = c.getString(4);
                c.close();

                int Docno = glbVars.db.GetDocNumber();
                IDDOC = Integer.toString(Docno, 36).toUpperCase();
                IDDOC += "." + TP_ID;
                Log.d("xd", IDDOC);

                sql = "INSERT INTO ZAKAZY(DOCID, TP, CONTR, ADDR, DOC_DATE, DELIVERY_DATE, COMMENT, STATUS)  VALUES (?,?,?,?,?,?,?,?);";
                stmt = glbVars.db.getWritableDatabase().compileStatement(sql);
                glbVars.db.getWritableDatabase().beginTransaction();
                try {
                    stmt.clearBindings();
                    stmt.bindString(1, IDDOC);
                    stmt.bindString(2, TP_ID);
                    stmt.bindString(3, Contr_ID);
                    stmt.bindString(4, Addr_ID);
                    stmt.bindString(5, curdate);
                    stmt.bindString(6, Data);
                    stmt.bindString(7, Comment);
                    stmt.bindLong(8, Status);
                    stmt.executeInsert();
                    stmt.clearBindings();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
                    glbVars.db.getWritableDatabase().endTransaction();
                }

                c1 = glbVars.db.getReadableDatabase().rawQuery("SELECT KOD5, DESCR, ZAKAZ, PRICE FROM Nomen where ZAKAZ<>0", null);
                if (c1.getCount() == 0) {
                    c1.close();
                    return;
                } else {
                    glbVars.db.getWritableDatabase().beginTransaction();
                    glbVars.db.getWritableDatabase().execSQL("INSERT INTO ZAKAZY_DT (ZAKAZ_ID, NOMEN, DESCR, QTY, PRICE) SELECT '" + IDDOC + "', KOD5, DESCR, ZAKAZ, PRICE FROM Nomen WHERE ZAKAZ>0");
                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
                    glbVars.db.getWritableDatabase().endTransaction();
                    glbVars.db.ClearOrderHeader();
                    glbVars.db.ResetNomen();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Заказ сохранён", Toast.LENGTH_SHORT).show();
                        glbVars.spinContr.setSelection(0);
                        glbVars.spinAddr.setSelection(0);
                        glbVars.spinAddr.setAdapter(null);
                        glbVars.txtDate.setText("");
                        glbVars.txtComment.setText("");
                        glbVars.edContrFilter.setText("");
                        clearChosenGroupSgi();

                        editor.putString("ColSgiID", "0");
                        editor.commit();

                        Fragment fragment = new JournalFragment();
                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.frame, fragment, "frag_journal");
                        fragmentTransaction.commit();
                        toolbar.setTitle(R.string.journal);
                    }
                });
            }
        }).start();
    }

    public void SaveEditOrder(final String OrderID) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cHead, c, c2;

                c = glbVars.db.getReadableDatabase().rawQuery("SELECT TORG_PRED.CODE AS TP, CONTRS.CODE AS CONTR, ADDRS.CODE AS ADDR, ORDERS.DATA, ORDERS.COMMENT, TORG_PRED.CODE AS TP, CONTRS.CODE AS CONTR, ADDRS.CODE AS ADDR FROM ORDERS JOIN TORG_PRED ON ORDERS.TP=TORG_PRED.CODE JOIN CONTRS ON ORDERS.CONTR=CONTRS.CODE JOIN ADDRS ON ORDERS.ADDR=ADDRS.CODE", null);
                c2 = glbVars.db.getReadableDatabase().rawQuery("SELECT 0 AS _id, CASE WHEN COUNT(ROWID) IS NULL THEN 0 ELSE COUNT(ROWID) END AS COUNT FROM Nomen WHERE ZAKAZ<>0", null);
                if (c.getCount() == 0) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Не заполнена шапка заказа", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                c2.moveToFirst();

                if (c2.getInt(1) == 0) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Нет ни одного добавленного товара для заказа", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                glbVars.db.getWritableDatabase().beginTransaction();
                cHead = glbVars.db.getWritableDatabase().rawQuery("SELECT TP_ID,CONTR_ID,ADDR_ID,DATA, COMMENT, DELIV_TIME, GETMONEY, GETBACKWARD, BACKTYPE FROM ORDERS", null);
                if (cHead.moveToNext()) {
                    try {
                        glbVars.db.getWritableDatabase().execSQL("UPDATE ZAKAZY SET TP_ID = '" + cHead.getString(0) + "', CONTR_ID = '" + cHead.getString(1) + "',ADDR_ID = '" + cHead.getString(2) + "', DELIVERY_DATE = '" + cHead.getString(3) + "', COMMENT = '" + cHead.getString(4) + "', DELIV_TIME = '" + cHead.getString(5) + "', GETMONEY = " + cHead.getInt(6) + ", GETBACKWARD = " + cHead.getInt(7) + ", BACKTYPE = " + cHead.getInt(8) + " WHERE DOCNO='" + OrderID + "'");
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + OrderID + "'");
                        glbVars.db.getWritableDatabase().execSQL("INSERT INTO ZAKAZY_DT (ZAKAZ_ID, NOM_ID, CODE, COD5, DESCR, QTY, PRICE) SELECT '" + OrderID + "', ID, CODE, COD, DESCR, ZAKAZ, PRICE FROM Nomen WHERE ZAKAZ>0");
                        glbVars.db.getWritableDatabase().execSQL("UPDATE Nomen SET ZAKAZ=0 WHERE ZAKAZ>0");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    cHead.close();
                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
                    glbVars.db.getWritableDatabase().endTransaction();
                    glbVars.db.ClearOrderHeader();
                    glbVars.db.ResetNomen();
                    glbVars.OrderID = "";
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Заказ сохранён", Toast.LENGTH_LONG).show();
                        try {
                            glbVars.spinContr.setSelection(0);
                            glbVars.spinAddr.setSelection(0);
                            glbVars.spinAddr.setAdapter(null);
                        } catch (Exception ignored) {
                        }
                        glbVars.txtDate.setText("");
                        glbVars.txtComment.setText("");
                        glbVars.edContrFilter.setText("");

                        clearChosenGroupSgi();
                        Fragment fragment = new JournalFragment();
                        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.frame, fragment, "frag_journal");
                        fragmentTransaction.commit();
                        toolbar.setTitle(R.string.journal);
                    }
                });
            }
        }).start();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ViewOrderId:
                Fragment fragment = new ViewOrderFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame, fragment, "frag_view_order");
                fragmentTransaction.commit();
                return true;
            case R.id.NomenSave:
                try {
                    isSaved = true;
                    if (!glbVars.OrderID.equals("")) {
                        SaveEditOrder(glbVars.OrderID);
                    } else {
                        SaveOrder();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.NomenMultiPos:
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

                RangeDlg
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                final AlertDialog alertDlg = RangeDlg.create();
                alertDlg.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

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
                InputMethodManager immPP = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                immPP.showSoftInput(edInput, InputMethodManager.SHOW_IMPLICIT);

                alertDlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (edBeginPP.getText().toString() != "" && edEndPP.getText().toString() != "" && edPPQty.getText().toString() != "") {
                            glbVars.UpdateNomenRange(Integer.parseInt(edBeginPP.getText().toString()), Integer.parseInt(edEndPP.getText().toString()), Integer.parseInt(edPPQty.getText().toString()));
                            glbVars.BeginPos = 0;
                            glbVars.EndPos = 0;
                            alertDlg.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Необходимо указать начальную позицию, конечную позицию и нужное количество", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                edBeginPP.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
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
                    }
                });

                edEndPP.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
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
                    }
                });

                return true;
            case R.id.NomenSort:
                if (!isSorted) {
                    item.setIcon(R.drawable.to_top);
                    glbVars.nomenList.setSelection(glbVars.nomenList.getCount());
                    isSorted = true;
                } else {
                    item.setIcon(R.drawable.to_end);
                    glbVars.nomenList.setSelection(0);
                    isSorted = false;
                }
                return true;
            case R.id.NomenDiscount:
                glbVars.CalculatePercentSale(mainMenu, 0);
                return true;
            case R.id.NomenMultiSelect:
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
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            })
                            .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    final AlertDialog alertD = alertDialogBuilder.create();
                    alertD.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

                    alertD.show();
                    input.requestFocus();
                    input.selectAll();
                    input.performClick();
                    input.setPressed(true);
                    input.invalidate();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

                    alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            glbVars.isMultiSelect = true;
                            glbVars.MultiQty = Integer.parseInt(input.getText().toString());
                            item.setIcon(getActivity().getResources().getDrawable(R.drawable.checkbox_marked));
                            alertD.dismiss();
                        }
                    });

                    input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                alertD.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                            }
                            return true;
                        }
                    });

                } else {
                    glbVars.isMultiSelect = false;
                    item.setIcon(getResources().getDrawable(R.drawable.checkbox_free));
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        txtGroup = getActivity().findViewById(R.id.ColGrupID);
        txtSgi = getActivity().findViewById(R.id.ColSgiID);

        glbVars.isMultiSelect = false;
        glbVars.MultiQty = 0;
        if (txtSgi != null && txtGroup != null && !isSaved) {
            editor.putString("ColSgiID", txtSgi.getText().toString());
            editor.putString("ColGrupID", txtGroup.getText().toString());
            editor.putInt("ColPosition", glbVars.nomenList.getFirstVisiblePosition());
            editor.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        String SgiID = settings.getString("ColSgiID", "0");
        String GrupID = settings.getString("ColGrupID", "0");
        int VisiblePos = settings.getInt("ColPosition", 0);
        glbVars.nomenList.setSelection(VisiblePos);

        assert SgiID != null;
        if (!SgiID.equals("0")) {
            glbVars.LoadGroups(SgiID);
            glbVars.SetSelectedSgi(SgiID, GrupID);
            glbVars.SetSelectedGrup(GrupID);
        }
        setContrAndSum();
    }

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);

        toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);

        glbVars.nomenList = getActivity().findViewById(R.id.listContrs);
        glbVars.spSgi = getActivity().findViewById(R.id.SpinSgi);
        glbVars.spGrup = getActivity().findViewById(R.id.SpinGrups);

        tvHeadCod = getActivity().findViewById(R.id.tvHeadCod);
        tvHeadDescr = getActivity().findViewById(R.id.tvHeadDescr);
        tvHeadMP = getActivity().findViewById(R.id.tvHeadMP);
        tvHeadZakaz = getActivity().findViewById(R.id.tvHeadZakaz);

        settings = getActivity().getSharedPreferences("form_order", 0);
        editor = settings.edit();

        glbVars.LoadSgi();
//        if (glbVars.frSgi != null && !glbVars.frSgi.equals("")) {
//            glbVars.SetSelectedSgi(glbVars.frSgi, glbVars.frGroup);
//            glbVars.LoadGroups(glbVars.frSgi);
//            glbVars.SetSelectedGrup(glbVars.frGroup);
//        }

        filter = getActivity().findViewById(R.id.NomenFilters);
        filter.setOnClickListener(this);
        setContrAndSum();
    }

    private void setContrAndSum() {
        String ToolBarContr = glbVars.db.GetToolbarContr();
        String OrderSum = glbVars.db.getOrderSum();
        try {
            if (!OrderSum.equals("")) {
                if (ToolBarContr.trim().equals("")) {
                    glbVars.toolbar.setSubtitle("Заказ на сумму " + OrderSum + " руб.");
                } else {
                    glbVars.toolbar.setSubtitle(ToolBarContr + OrderSum + " руб.");
                }
            } else {
                glbVars.toolbar.setSubtitle("");
            }
        } catch (Exception ignored) {
        }
    }
}
