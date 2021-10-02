package com.amber.armtp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


/**
 * Фрагмент "Шапка заказа"
 */
public class OrderHeadFragment extends Fragment {
    public GlobalVars glbVars;
    Menu mainMenu;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    android.support.v4.app.Fragment fragment = null;
    android.support.v4.app.FragmentTransaction fragmentTransaction;
    android.support.v7.widget.Toolbar toolbar;

    public OrderHeadFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.order_head_fragment, container, false);
        Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setHasOptionsMenu(true);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.order_head_menu, menu);
        mainMenu = menu;
        glbVars.setSaleIcon(mainMenu, 0, glbVars.db.CheckForSales() > 0);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.OrdSumWithSales:
                glbVars.setSaleIcon(mainMenu, 0, !glbVars.isSales);
                glbVars.db.calcSales(glbVars.db.GetContrID());
                if (glbVars.NomenAdapter != null) {
                    glbVars.myNom.requery();
                    glbVars.NomenAdapter.notifyDataSetChanged();
                }
                setContrAndSum();
                return true;
            case R.id.SaveOrder:
                try {
                    if (!glbVars.OrderID.equals("")) {
                        SaveEditOrder(glbVars.OrderID);
                    } else {
                        SaveOrder();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.ViewOrder:
                fragment = new ViewOrderFragment();
                fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame, fragment, "frag_view_order");
                fragmentTransaction.commit();
                toolbar.setTitle("Просмотр заказа");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        glbVars.toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
        toolbar = getActivity().findViewById(R.id.toolbar);
        glbVars.edContrFilter = getActivity().findViewById(R.id.txtContrFilter);
        glbVars.txtComment = getActivity().findViewById(R.id.txtComment);
        glbVars.btSave = getActivity().findViewById(R.id.btSaveHeader);
        glbVars.btClear = getActivity().findViewById(R.id.btClearOrder);

        glbVars.spinContr = getActivity().findViewById(R.id.SpinContr);
        glbVars.spinAddr = getActivity().findViewById(R.id.SpinAddr);
        glbVars.TPList = getActivity().findViewById(R.id.SpinTP);

        String OrderSum = glbVars.db.getOrderSum();
        toolbar.setSubtitle("Заказ на сумму " + OrderSum);

        glbVars.LoadTpList();
        glbVars.LoadContrList();

        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = settings.edit();

        glbVars.edContrFilter.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String ContrFilter = glbVars.edContrFilter.getText().toString();
                if (ContrFilter.length() != 0) {
                    glbVars.LoadFilteredContrList(ContrFilter);
                } else {
                    glbVars.LoadContrList();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        glbVars.DeliveryDate = Calendar.getInstance();
        glbVars.DeliveryTime = Calendar.getInstance();

        glbVars.txtDate = getActivity().findViewById(R.id.txtDelivDate);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                glbVars.DeliveryDate.set(Calendar.YEAR, year);
                glbVars.DeliveryDate.set(Calendar.MONTH, monthOfYear);
                glbVars.DeliveryDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "dd.MM.yyyy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

                glbVars.txtDate.setText(sdf.format(glbVars.DeliveryDate.getTime()));
            }

        };

        glbVars.txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(Objects.requireNonNull(getActivity()), date, glbVars.DeliveryDate.get(Calendar.YEAR), glbVars.DeliveryDate.get(Calendar.MONTH), glbVars.DeliveryDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        glbVars.btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ADDR_ID;

                glbVars.spTp = getActivity().findViewById(R.id.ColTPID);
                glbVars.spContr = getActivity().findViewById(R.id.ColContrID);
                glbVars.spAddr = getActivity().findViewById(R.id.ColContrAddrID);

                String TP_ID = glbVars.spTp.getText().toString();
                String CONTR_ID = glbVars.spContr.getText().toString();
                String CurContr = glbVars.db.GetContrID();

                if (!CurContr.equals(CONTR_ID)) {
                    glbVars.db.resetContrSales();
                    setContrAndSum();
                }

                if (glbVars.spAddr != null) {
                    ADDR_ID = glbVars.spAddr.getText().toString();
                } else {
                    ADDR_ID = "0";
                }

                String DeliveryDate = glbVars.txtDate.getText().toString();
                String Comment = glbVars.txtComment.getText().toString();

                if (TP_ID.equals("0") || CONTR_ID.equals("0") || ADDR_ID.equals("0") || DeliveryDate.equals("")) {
                    Toast.makeText(getActivity(), "Необходимо заполнить все обязательные поля шапки заказа", Toast.LENGTH_LONG).show();
                    return;
                }

                editor.putString("TP_ID", TP_ID);
                editor.commit();

                if (glbVars.db.CheckTPAccess(TP_ID) > 0) {
                    editor.putBoolean("TP_LOCK", true);
                    editor.commit();
                }

                if (glbVars.db.insertOrder(TP_ID, CONTR_ID, ADDR_ID, DeliveryDate, Comment, "", 0, 0, 0L)) {
                    glbVars.db.resetContrSales();
                    glbVars.setSaleIcon(mainMenu, 0, false);
                    setContrAndSum();
                } else {
                    if (glbVars.db.updateOrderHead(TP_ID, CONTR_ID, ADDR_ID, DeliveryDate, Comment, "", 0, 0, 0L)) {
                        glbVars.db.resetContrSales();
                        glbVars.setSaleIcon(mainMenu, 0, false);
                        setContrAndSum();
                    } else {
                        Toast.makeText(getActivity(), "Вы уже заполнили шапку заказа, либо не удалось обновить шапку заказа", Toast.LENGTH_LONG).show();
                    }
                }

                goToFormOrderFragment();
            }
        });

        glbVars.btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (glbVars.db.ClearOrderHeader()) {
                    glbVars.spinContr.setSelection(0);
                    glbVars.spinAddr.setAdapter(null);
                    glbVars.txtComment.setText("");
                    glbVars.txtDate.setText("");
                    glbVars.edContrFilter.setText("");
                    glbVars.db.resetContrSales();
                    glbVars.setSaleIcon(mainMenu, 0, false);
                    setContrAndSum();
                    Toast.makeText(getActivity(), "Шапка заказа успешно очищена", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Не удалось очистить шапку заказа", Toast.LENGTH_LONG).show();
                }
            }
        });


        int ContRowid = glbVars.db.GetContrRowID();
        SetSelectedContr(ContRowid);

        String stTP_ID = settings.getString("TP_ID", "0");

        int TPRowid = glbVars.db.GetTPRowID();
        int TPDefaultRowid = glbVars.db.GetTPByID(stTP_ID);

        if (glbVars.CheckTPLock()) {
            glbVars.TPList.setSelection(TPDefaultRowid);
        } else {
            if (stTP_ID.equals("0")) {
                glbVars.TPList.setSelection(TPRowid);
            } else {
                if (TPRowid != TPDefaultRowid && TPRowid != 0) {
                    glbVars.TPList.setSelection(TPRowid);
                } else {
                    glbVars.TPList.setSelection(TPDefaultRowid);
                }
            }
        }

        String Comment = glbVars.db.GetComment();
        String DelivDate = glbVars.db.GetDeliveryDate();
        if (!Comment.equals("")) {
            glbVars.txtComment.setText(Comment, TextView.BufferType.EDITABLE);
        }

        if (!DelivDate.equals("")) {
            glbVars.txtDate.setText(DelivDate, TextView.BufferType.EDITABLE);
        }

        setContrAndSum();

    }

    public void SetSelectedContr(int ROWID) {
        for (int i = 0; i < glbVars.spinContr.getCount(); i++) {
            Cursor value = (Cursor) glbVars.spinContr.getItemAtPosition(i);
            int id = value.getInt(value.getColumnIndexOrThrow("_id"));
            if (ROWID == id) {
                glbVars.spinContr.setSelection(i);
                break;
            }
        }
    }

    public void SaveEditOrder(final String OrderID) {
        final ProgressDialog progress;
        progress = new ProgressDialog(getActivity());
        progress.setIndeterminate(false);
        progress.setCancelable(true);
        progress.setCanceledOnTouchOutside(false);
        progress.setMessage("Идет сохранение редактируемого заказа...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cHead, c, c2;

                c = glbVars.db.getReadableDatabase().rawQuery("SELECT TORG_PRED.CODE AS TP, CONTRS.CODE AS CONTR, ADDRS.CODE AS ADDR, ORDERS.DATA, ORDERS.COMMENT, TORG_PRED.ID AS TP_ID, CONTRS.ID AS CONTR_ID, ADDRS.ID AS ADDR_ID, ORDERS.DELIV_TIME, ORDERS.GETMONEY, ORDERS.GETBACKWARD, ORDERS.BACKTYPE FROM ORDERS JOIN TORG_PRED ON ORDERS.TP_ID=TORG_PRED.ID JOIN CONTRS ON ORDERS.CONTR_ID=CONTRS.ID JOIN ADDRS ON ORDERS.ADDR_ID=ADDRS.ID", null);
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

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.show();
                    }
                });
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
                        progress.dismiss();
                        glbVars.spinContr.setSelection(0);
                        glbVars.spinAddr.setSelection(0);
                        glbVars.spinAddr.setAdapter(null);
                        glbVars.txtDate.setText("");
                        glbVars.txtComment.setText("");
                        glbVars.edContrFilter.setText("");
                        toolbar.setSubtitle("");
                    }
                });

            }
        }).start();
    }

    public void SaveOrder() throws ParseException {
//        final ProgressDialog progress;
//        progress = new ProgressDialog(getActivity());
//        progress.setIndeterminate(false);
//        progress.setCancelable(true);
//        progress.setCanceledOnTouchOutside(false);
//        progress.setMessage("Идет сохранение заказа...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor c, c2, c1;
                String TP_ID;
                String Contr_ID;
                String Addr_ID;
                String Data, Time;
                String Comment;
                String IDDOC;


                String sql;
                SQLiteStatement stmt;

                int getMoney, getBackward, getBacktype;

                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                String curdate = df.format(Calendar.getInstance().getTime());

                c = glbVars.db.getReadableDatabase().rawQuery("SELECT TORG_PRED.CODE AS TP, CONTRS.CODE AS CONTR, ADDRS.CODE AS ADDR, ORDERS.DATA, ORDERS.COMMENT, TORG_PRED.ID AS TP_ID, CONTRS.ID AS CONTR_ID, ADDRS.ID AS ADDR_ID, ORDERS.DELIV_TIME, ORDERS.GETMONEY, ORDERS.GETBACKWARD, ORDERS.BACKTYPE FROM ORDERS JOIN TORG_PRED ON ORDERS.TP_ID=TORG_PRED.ID JOIN CONTRS ON ORDERS.CONTR_ID=CONTRS.ID JOIN ADDRS ON ORDERS.ADDR_ID=ADDRS.ID", null);
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

//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        progress.show();
//                    }
//                });

                c.moveToFirst();
                Data = c.getString(3);
                Time = c.getString(8);
                getMoney = c.getInt(9);
                getBackward = c.getInt(10);
                getBacktype = c.getInt(11);

                Comment = c.getString(4);
                TP_ID = c.getString(5);
                Contr_ID = c.getString(6);
                Addr_ID = c.getString(7);

                c.close();

                int Docno = glbVars.db.GetDocNumber();
                IDDOC = Integer.toString(Docno, 36).toUpperCase();
                IDDOC += "." + TP_ID;

                sql = "INSERT INTO ZAKAZY(DOCNO, TP_ID, CONTR_ID, ADDR_ID, DOC_DATE, DELIVERY_DATE, COMMENT, DELIV_TIME, GETMONEY, GETBACKWARD, BACKTYPE)  VALUES (?,?,?,?,?,?,?,?,?,?,?);";
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
                    stmt.bindString(8, Time);
                    stmt.bindLong(9, getMoney);
                    stmt.bindLong(10, getBackward);
                    stmt.bindLong(11, getBacktype);
                    stmt.executeInsert();
                    stmt.clearBindings();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
                    glbVars.db.getWritableDatabase().endTransaction();
                }

                c1 = glbVars.db.getReadableDatabase().rawQuery("SELECT ID, COD, DESCR, ZAKAZ, ROUND(PRICE,2) AS [CENA], CODE FROM Nomen where ZAKAZ<>0", null);
                if (c1.getCount() == 0) {
                    c1.close();
                    return;
                } else {
                    glbVars.db.getWritableDatabase().beginTransaction();
                    glbVars.db.getWritableDatabase().execSQL("INSERT INTO ZAKAZY_DT (ZAKAZ_ID, NOM_ID, CODE, COD5, DESCR, QTY, PRICE) SELECT '" + IDDOC + "', ID, CODE, COD, DESCR, ZAKAZ, PRICE FROM Nomen WHERE ZAKAZ>0");
                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
                    glbVars.db.getWritableDatabase().endTransaction();
                    glbVars.db.ClearOrderHeader();
                    glbVars.db.ResetNomen();
                }


                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        progress.dismiss();
                        glbVars.spinContr.setSelection(0);
                        glbVars.spinAddr.setSelection(0);
                        glbVars.spinAddr.setAdapter(null);
                        glbVars.txtDate.setText("");
                        glbVars.txtComment.setText("");
                        glbVars.edContrFilter.setText("");
                        toolbar.setSubtitle("");
                        glbVars.setSaleIcon(mainMenu, 0, false);
                    }
                });

            }
        }).start();
    }

    public void goToFormOrderFragment() {
        Fragment fragment = new FormOrderFragment();
        fragmentTransaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment, "frag_form_order");
        fragmentTransaction.commit();
    }

    private void setContrAndSum() {
        String ToolBarContr = glbVars.db.GetToolbarContr();
        String OrderSum = glbVars.db.getOrderSum();
        toolbar.setSubtitle(ToolBarContr + OrderSum);
    }
}
