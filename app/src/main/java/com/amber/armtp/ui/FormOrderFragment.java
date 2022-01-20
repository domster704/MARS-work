package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
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

import com.amber.armtp.GlobalVars;
import com.amber.armtp.R;

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

                    String curSgi;
                    TextView txtSgi = Objects.requireNonNull(getView()).findViewById(R.id.ColSgiID);
                    curSgi = txtSgi.getText().toString();

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
    TextView FilterWC_ID, FilterFocus_ID;
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
                    .setPositiveButton("OK", (dialog, id) -> {
                    })
                    .setNegativeButton("Отмена", (dialog, id) -> dialog.cancel())
                    .setNeutralButton("Сбросить фильтры", (dialog, id) -> {
                    })
            ;

            final AlertDialog alertD = alertDialogBuilder.create();
            alertD.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            alertD.show();
            glbVars.LoadFiltersWC(promptView);
            glbVars.LoadFiltersFocus(promptView);

            String WCID = settings.getString("ColWCID", "0");
            String FocusID = settings.getString("ColFocusID", "0");

            if (WCID != null && !WCID.equals("0")) {
                glbVars.SetSelectedFilterWC(WCID);
            }

            if (FocusID != null && !FocusID.equals("0")) {
                glbVars.SetSelectedFilterFocus(FocusID);
            }

            alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                filter.setImageResource(R.drawable.filter_activated);
                FilterWC_ID = promptView.findViewById(R.id.ColWCID);
                FilterFocus_ID = promptView.findViewById(R.id.ColFocusID);

                editor.putString("ColWCID", FilterWC_ID.getText().toString());
                editor.putString("ColFocusID", FilterFocus_ID.getText().toString());

                editor.commit();

                String SgiId = "0";
                String GroupID = "0";
                try {
                    SgiId = ((TextView) getActivity().findViewById(R.id.ColSgiID)).getText().toString();
                    GroupID = ((TextView) getActivity().findViewById(R.id.ColGrupID)).getText().toString();
                } catch (Exception ignored) {}

                glbVars.LoadFilterNomen(SgiId, GroupID,
                        FilterWC_ID.getText().toString(), FilterFocus_ID.getText().toString());
                alertD.dismiss();
            });

            alertD.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                glbVars.SetSelectedFilterWC("0");
                glbVars.SetSelectedFilterFocus("0");
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
        GlobalVars.CurAc = getActivity();
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
        new Thread(() -> {
            Cursor c, c2, c1;
            String TP_ID, Contr_ID, Addr_ID, Data, Comment, IDDOC;
            String ContrDes, AddrDes;
            int Status = 0;

            String sql;
            SQLiteStatement stmt;

            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            String curdate = df.format(Calendar.getInstance().getTime());

            c = glbVars.db.getReadableDatabase().rawQuery("SELECT TORG_PRED.CODE as TP_ID, ORDERS.DATA as DATA, ORDERS.COMMENT as COMMENT, CONTRS.CODE AS CONTR_ID, ADDRS.CODE AS ADDR_ID, CONTRS.DESCR as C_DES, ADDRS.DESCR as A_DES FROM ORDERS JOIN TORG_PRED ON ORDERS.TP=TORG_PRED.CODE JOIN CONTRS ON ORDERS.CONTR=CONTRS.CODE JOIN ADDRS ON ORDERS.ADDR=ADDRS.CODE", null);
            c2 = glbVars.db.getReadableDatabase().rawQuery("SELECT 0 AS _id, CASE WHEN COUNT(ROWID) IS NULL THEN 0 ELSE COUNT(ROWID) END AS COUNT FROM Nomen WHERE ZAKAZ<>0", null);
            if (c.getCount() == 0) {
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Не заполнена шапка заказа", Toast.LENGTH_LONG).show());

                return;
            }
            c2.moveToFirst();

            if (c2.getInt(1) == 0) {
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Нет ни одного добавленного товара для заказа", Toast.LENGTH_LONG).show());
                return;
            }

            c.moveToFirst();
            TP_ID = c.getString(c.getColumnIndex("TP_ID"));
            Data = c.getString(c.getColumnIndex("DATA"));
            Comment = c.getString(c.getColumnIndex("COMMENT"));
            Contr_ID = c.getString(c.getColumnIndex("CONTR_ID"));
            Addr_ID = c.getString(c.getColumnIndex("ADDR_ID"));
            ContrDes = c.getString(c.getColumnIndex("C_DES"));
            AddrDes = c.getString(c.getColumnIndex("A_DES"));

            c.close();
            c2.close();

            int Docno = glbVars.dbOrders.GetDocNumber();
            IDDOC = Integer.toString(Docno, 36).toUpperCase();
            IDDOC += "." + TP_ID + "." + Data.replace(".", "");

            sql = "INSERT INTO ZAKAZY(DOCID, TP, CONTR, ADDR, DOC_DATE, DELIVERY_DATE, COMMENT, STATUS, CONTR_DES, ADDR_DES)  VALUES (?,?,?,?,?,?,?,?,?,?);";
            stmt = glbVars.dbOrders.getWritableDatabase().compileStatement(sql);
            glbVars.dbOrders.getWritableDatabase().beginTransaction();
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
                stmt.bindString(9, ContrDes);
                stmt.bindString(10, AddrDes);
                stmt.executeInsert();
                stmt.clearBindings();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                glbVars.dbOrders.getWritableDatabase().setTransactionSuccessful();
                glbVars.dbOrders.getWritableDatabase().endTransaction();
            }

            c1 = glbVars.db.getReadableDatabase().rawQuery("SELECT KOD5, DESCR, ZAKAZ, PRICE FROM Nomen where ZAKAZ<>0", null);
            if (c1.getCount() == 0) {
                c1.close();
                return;
            } else {
                _insertIntoZakazyDT(IDDOC);
            }

            getActivity().runOnUiThread(() -> {
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
            });
        }).start();
    }

    public void SaveEditOrder(final String OrderID) {
        new Thread(() -> {
            Cursor cHead, c, c2;

            c = glbVars.db.getReadableDatabase().rawQuery("SELECT TORG_PRED.CODE AS TP, CONTRS.CODE AS CONTR, ADDRS.CODE AS ADDR, ORDERS.DATA, ORDERS.COMMENT, TORG_PRED.CODE AS TP, CONTRS.CODE AS CONTR, ADDRS.CODE AS ADDR FROM ORDERS JOIN TORG_PRED ON ORDERS.TP=TORG_PRED.CODE JOIN CONTRS ON ORDERS.CONTR=CONTRS.CODE JOIN ADDRS ON ORDERS.ADDR=ADDRS.CODE", null);
            c2 = glbVars.db.getReadableDatabase().rawQuery("SELECT 0 AS _id, CASE WHEN COUNT(ROWID) IS NULL THEN 0 ELSE COUNT(ROWID) END AS COUNT FROM Nomen WHERE ZAKAZ<>0", null);
            if (c.getCount() == 0) {
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Не заполнена шапка заказа", Toast.LENGTH_LONG).show());
                return;
            }

            c2.moveToFirst();

            if (c2.getInt(1) == 0) {
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Нет ни одного добавленного товара для заказа", Toast.LENGTH_LONG).show());
                return;
            }

            c.close();
            c2.close();

            glbVars.db.getWritableDatabase().beginTransaction();
            cHead = glbVars.db.getWritableDatabase().rawQuery("SELECT TP, CONTR, ADDR, DATA, COMMENT FROM ORDERS", null);
            if (cHead.moveToNext()) {
                try {
                    glbVars.dbOrders.getWritableDatabase().execSQL("UPDATE ZAKAZY SET TP = '" + cHead.getString(cHead.getColumnIndex("TP")) + "'," +
                            " CONTR = '" + cHead.getString(cHead.getColumnIndex("CONTR")) + "'," +
                            " ADDR = '" + cHead.getString(cHead.getColumnIndex("ADDR")) + "'," +
                            " DELIVERY_DATE = '" + cHead.getString(cHead.getColumnIndex("DATA")) + "'," +
                            " COMMENT = '" + cHead.getString(cHead.getColumnIndex("COMMENT")) + "' WHERE DOCID='" + OrderID + "'");
                    glbVars.dbOrders.getWritableDatabase().execSQL("DELETE FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + OrderID + "'");

                    _insertIntoZakazyDT(OrderID);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                cHead.close();
                glbVars.db.getWritableDatabase().setTransactionSuccessful();
                glbVars.db.getWritableDatabase().endTransaction();
                glbVars.db.ClearOrderHeader();
                glbVars.OrderID = "";
            }

            getActivity().runOnUiThread(() -> {
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
            });
        }).start();
    }

    private void _insertIntoZakazyDT(String docid) {
        Cursor nomenData = glbVars.db.getReadableDatabase().rawQuery("SELECT KOD5, DESCR, ZAKAZ, PRICE FROM Nomen WHERE ZAKAZ>0", null);

        glbVars.dbOrders.getWritableDatabase().beginTransaction();
        for (int i = 0; i < nomenData.getCount(); i++) {
            nomenData.moveToNext();
            String KOD5 = nomenData.getString(nomenData.getColumnIndex("KOD5"));
            String DESCR = nomenData.getString(nomenData.getColumnIndex("DESCR"));
            String ZAKAZ = nomenData.getString(nomenData.getColumnIndex("ZAKAZ"));
            String PRICE = nomenData.getString(nomenData.getColumnIndex("PRICE"));
            glbVars.dbOrders.getWritableDatabase().execSQL("INSERT INTO ZAKAZY_DT (ZAKAZ_ID, NOMEN, DESCR, QTY, PRICE) VALUES('" + docid + "','" + KOD5 + "','" + DESCR + "','" + ZAKAZ + "','" + PRICE + "')");
        }

        glbVars.dbOrders.getWritableDatabase().setTransactionSuccessful();
        glbVars.dbOrders.getWritableDatabase().endTransaction();
        glbVars.db.ClearOrderHeader();
        glbVars.db.ResetNomen();
        nomenData.close();
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
