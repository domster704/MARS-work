package com.amber.armtp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.linuxense.javadbf.DBFException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class JournalFragment extends Fragment {
    public GlobalVars glbVars;
    Menu mainMenu;
    SearchView searchView;
    MenuItem searchItem;
    Connection conn = null;
    Statement stmt;
    ResultSet reset;
    private int progressStatus = 0;
    private final Handler handler = new Handler();
    private android.support.v7.widget.Toolbar toolbar;
    GridView orderList, orderdtList;
    TextView tvOrder, tvContr, tvAddr, tvDocDate, tvStatus;
    EditText txtBDate, txtEDate;
    Button btOrderFilter, btUpdateStatus;
    Calendar CalBDate, CalEDate, c;
    GridView gdOrders;
    SimpleDateFormat sdf, df;
    String myFormat = "dd.MM.yyyy";
    PopupMenu nomPopupMenu;
    AlertDialog.Builder builder;
    Calendar calendar;
    Date newDate;

    ProgressDialog progress;

    public JournalFragment() {

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            int Lay = glbVars.viewFlipper.getDisplayedChild();
            if (Lay == 1) {
                glbVars.ordStatus = null;
                glbVars.viewFlipper.setDisplayedChild(0);
            } else {
                return false;
            }
        }
        return super.getActivity().onKeyDown(keyCode, event);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.journal_fragment, container, false);
        glbVars.view = rootView;
        return rootView;
    }


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.form_order_menu, menu);
        mainMenu = menu;
        searchItem = menu.findItem(R.id.menu_search);
        searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Поиск по журналу");
        searchView.setOnQueryTextListener(searchTextListner);
    }

    @Override
    public void onResume() {
        super.onResume();
        btOrderFilter.performClick();
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
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle("");
        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);
        glbVars.viewFlipper = getActivity().findViewById(R.id.viewflipper);

        c = Calendar.getInstance();
        df = new SimpleDateFormat(myFormat);
        String formattedDate = df.format(c.getTime());

        SimpleDateFormat s = new SimpleDateFormat(formattedDate);

        CalBDate = Calendar.getInstance();
        CalEDate = Calendar.getInstance();

        txtBDate = getActivity().findViewById(R.id.txtBDate);
        txtEDate = getActivity().findViewById(R.id.txtEDate);
        txtBDate.setText(GlobalVars.getCalculatedDate(myFormat, -10));
        txtEDate.setText(formattedDate);

        btOrderFilter = getActivity().findViewById(R.id.btShowOrders);
        btUpdateStatus = getActivity().findViewById(R.id.btUpdateStatus);
        glbVars.gdOrders = getActivity().findViewById(R.id.listSMS);
        glbVars.orderdtList = getActivity().findViewById(R.id.listOrdersDt);
        sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

        final DatePickerDialog.OnDateSetListener Bdate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                CalBDate.set(Calendar.YEAR, year);
                CalBDate.set(Calendar.MONTH, monthOfYear);
                CalBDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                txtBDate.setText(sdf.format(CalBDate.getTime()));
            }
        };


        txtBDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getActivity(), Bdate, CalBDate.get(Calendar.YEAR), CalBDate.get(Calendar.MONTH), CalBDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        final DatePickerDialog.OnDateSetListener Edate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                CalEDate.set(Calendar.YEAR, year);
                CalEDate.set(Calendar.MONTH, monthOfYear);
                CalEDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                txtEDate.setText(sdf.format(CalEDate.getTime()));
            }

        };

        txtEDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getActivity(), Edate, CalEDate.get(Calendar.YEAR), CalEDate.get(Calendar.MONTH), CalEDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btOrderFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String BDate = txtBDate.getText().toString(), EDate = txtEDate.getText().toString();
                if (!BDate.equals("") && !EDate.equals("")) {
                    glbVars.LoadOrders(BDate, EDate);
                    glbVars.gdOrders.setOnItemClickListener(GridOrdersClick);
                    glbVars.gdOrders.setOnItemLongClickListener(GridOrdersLongClick);
                } else {
                    Toast.makeText(getActivity(), "Обязательно необходимо указать период", Toast.LENGTH_LONG).show();
                }
            }
        });

        btUpdateStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (glbVars.isNetworkAvailable() == true) {
                    UpdateStatus();
                } else {
                    Toast.makeText(getActivity(), "Нет доступного интернет соединения. Проверьте соединение с Интернетом", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private final AdapterView.OnItemClickListener GridOrdersClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {
            tvOrder = myView.findViewById(R.id.ColOrdDocNo);
            tvContr = myView.findViewById(R.id.ColOrdContr);
            tvAddr = myView.findViewById(R.id.ColOrdAddr);
            tvDocDate = myView.findViewById(R.id.ColOrdDocDate);
            tvStatus = myView.findViewById(R.id.ColOrdStatus);
            glbVars.ordStatus = tvStatus.getText().toString();
            final String ID = tvOrder.getText().toString();
            glbVars.LoadOrdersDetails(ID);
            glbVars.viewFlipper.setDisplayedChild(1);
        }
    };

    private final AdapterView.OnItemLongClickListener GridOrdersLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            tvOrder = view.findViewById(R.id.ColOrdDocNo);
            tvStatus = view.findViewById(R.id.ColOrdStatus);
            final String ID = tvOrder.getText().toString();
            final String Status = tvStatus.getText().toString();
            nomPopupMenu = new PopupMenu(getActivity(), view);
            nomPopupMenu.getMenuInflater().inflate(R.menu.order_context_menu, nomPopupMenu.getMenu());
            nomPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    String FileName = "";
                    switch (menuItem.getItemId()) {
                        case R.id.CtxOrdSend:
                            try {
                                FileName = glbVars.FormDBFForZakaz(ID);
                            } catch (DBFException e) {
                                e.printStackTrace();
                            } finally {
                                if (!FileName.equals("")) {
                                    SendDBFFile(FileName);
                                } else {
                                    Toast.makeText(getActivity(), "Неверное имя файла для отправки", Toast.LENGTH_LONG).show();
                                }
                            }
                            return true;
                        case R.id.CtxOrdEdit:
                            glbVars.OrderID = ID;

                            // Use the Builder class for convenient dialog construction
                            builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage("При редактировании заказа текущая шапка заказа и текущий подбор товара будут полностью очищены. Вы уверены?")
                                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                        }
                                    })
                                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            EditOrder(ID);
                                        }
                                    })
                            ;
                            builder.create();
                            builder.show();

                            return true;
                        case R.id.CtxOrdDelete:
                            glbVars.db.getWritableDatabase().execSQL("UPDATE ZAKAZY SET STATUS=99 WHERE DOCNO='" + ID + "'");
                            glbVars.Orders.requery();
                            glbVars.OrdersAdapter.notifyDataSetChanged();
                            return true;
                        case R.id.CtxOrdCopy:
                            CopyOrder(ID);
                            return true;
                        default:
                    }
                    return true;
                }
            });

            if (Status.equals("Удален") || Status.equals("Отменен")) {
                nomPopupMenu.getMenu().findItem(R.id.CtxOrdSend).setEnabled(false);
                nomPopupMenu.getMenu().findItem(R.id.CtxOrdEdit).setEnabled(false);
                nomPopupMenu.getMenu().findItem(R.id.CtxOrdDelete).setEnabled(false);
            }

            if (Status.equals("Оформлен") || Status.equals("Оформлен(-)") || Status.equals("Собран(-)") || Status.equals("Собран") || Status.equals("Получен")) {
                nomPopupMenu.getMenu().findItem(R.id.CtxOrdSend).setEnabled(false);
                nomPopupMenu.getMenu().findItem(R.id.CtxOrdEdit).setEnabled(false);
                nomPopupMenu.getMenu().findItem(R.id.CtxOrdDelete).setEnabled(false);
            }

            if (Status.equals("Отправлен")) {
                nomPopupMenu.getMenu().findItem(R.id.CtxOrdEdit).setEnabled(false);
                nomPopupMenu.getMenu().findItem(R.id.CtxOrdDelete).setEnabled(false);
            }

            if (Status.equals("Сохранен")) {
                nomPopupMenu.getMenu().findItem(R.id.CtxOrdSend).setEnabled(false);
            }
            nomPopupMenu.show();
            return true;
        }

    };

    private final SearchView.OnQueryTextListener searchTextListner =
            new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    return !newText.equals("");
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }
            };

    private boolean SendDBFFile(String FileName) {
        progress = null;
        progress = new ProgressDialog(getActivity());
        progress.setIndeterminate(false);
        progress.setTitle("Загрузка файла заказов");
        progress.setMessage("Идет загрузка файлов заказа. Пожалуйста подождите...");
        progress.setCancelable(false);
        progress.setIndeterminate(false);

        progress.show();

        final String tmp_filename = FileName;

        new Thread(new Runnable() {
            @Override
            public void run() {

                String server = getResources().getString(R.string.ftp_server);
                String username = getResources().getString(R.string.ftp_user);
                String password = getResources().getString(R.string.ftp_pass);

                FTPClient ftpClient = new FTPClient();
                try {

                    ftpClient.connect(server, 21);
                    ftpClient.login(username, password);
                    ftpClient.enterLocalPassiveMode();
                    ftpClient.changeWorkingDirectory("newARM");
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                    InputStream inputStream;

//                    File secondLocalFile = new File(glbVars.GetSDCardpath()+glbVars.DBFolder+"/"+tmp_filename);
                    File secondLocalFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + tmp_filename);

                    String secondRemoteFile = tmp_filename;
                    inputStream = new FileInputStream(secondLocalFile);

                    OutputStream outputStream = ftpClient.storeFileStream(secondRemoteFile);
                    byte[] bytesIn = new byte[4096];
                    int read = 0;

                    while ((read = inputStream.read(bytesIn)) != -1) {
                        outputStream.write(bytesIn, 0, read);
                    }

                    inputStream.close();
                    outputStream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                    }
                });
            }
        }).start();
        return true;
    }

    private void EditOrder(final String OrderID) {
        progress = null;
        progress = new ProgressDialog(getActivity());
        progress.setIndeterminate(false);
        progress.setTitle("Редактирование заказа");
        progress.setMessage("Включение редактирования заказа. Пожалуйста подождите...");
        progress.setCancelable(false);
        progress.setIndeterminate(false);
        progress.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cNom, cHead;
                glbVars.db.getWritableDatabase().beginTransaction();
                cHead = glbVars.db.getWritableDatabase().rawQuery("SELECT TP_ID, CONTR_ID, ADDR_ID, DELIVERY_DATE, COMMENT, DELIV_TIME, GETMONEY, GETBACKWARD, BACKTYPE FROM ZAKAZY WHERE DOCNO='" + OrderID + "'", null);
                if (cHead.moveToNext()) {
                    try {
                        if (glbVars.db.getCount() == 0) {
                            glbVars.db.getWritableDatabase().execSQL("INSERT INTO ORDERS(TP_ID,CONTR_ID,ADDR_ID,DATA, COMMENT, DELIV_TIME, GETMONEY, GETBACKWARD, BACKTYPE) VALUES ('" + cHead.getString(0) + "', '" + cHead.getString(1) + "', '" + cHead.getString(2) + "', '" + cHead.getString(3) + "', '" + cHead.getString(4) + "', '" + cHead.getString(5) + "', " + cHead.getInt(6) + ", " + cHead.getInt(7) + ", " + cHead.getInt(8) + ")");
                        } else {
                            glbVars.db.getWritableDatabase().execSQL("UPDATE ORDERS SET TP_ID = '" + cHead.getString(0) + "', CONTR_ID = '" + cHead.getString(1) + "',ADDR_ID = '" + cHead.getString(2) + "',DATA = '" + cHead.getString(3) + "', COMMENT = '" + cHead.getString(4) + "', DELIV_TIME = '" + cHead.getString(5) + "', GETMONEY = " + cHead.getString(6) + ", GETBACKWARD = " + cHead.getString(7) + ", BACKTYPE = " + cHead.getString(8));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    cHead.close();
                }

                glbVars.db.getWritableDatabase().execSQL("UPDATE Nomen SET ZAKAZ=0 WHERE ZAKAZ>0");
                cNom = glbVars.db.getWritableDatabase().rawQuery("SELECT QTY, NOM_ID FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + OrderID + "'", null);
                try {
                    while (cNom.moveToNext()) {
                        glbVars.db.getWritableDatabase().execSQL("UPDATE Nomen SET ZAKAZ=" + cNom.getInt(0) + " WHERE ID='" + cNom.getString(1) + "'");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                cNom.close();
                glbVars.db.getWritableDatabase().setTransactionSuccessful();
                glbVars.db.getWritableDatabase().endTransaction();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                    }
                });
            }
        }).start();
    }

    private void CopyOrder(final String OrderID) {
        progress = null;
        progress = new ProgressDialog(getActivity());
        progress.setIndeterminate(false);
        progress.setTitle("Копирование заказа");
        progress.setMessage("Идет копирование. Пожалуйста подождите...");
        progress.setCancelable(false);
        progress.setIndeterminate(false);
        progress.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor;
                glbVars.db.getWritableDatabase().beginTransaction();
                glbVars.db.getWritableDatabase().execSQL("UPDATE Nomen SET ZAKAZ=0 WHERE ZAKAZ>0");
                cursor = glbVars.db.getWritableDatabase().rawQuery("SELECT QTY, NOM_ID FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + OrderID + "'", null);
                try {
                    while (cursor.moveToNext()) {
                        glbVars.db.getWritableDatabase().execSQL("UPDATE Nomen SET ZAKAZ=" + cursor.getInt(0) + " WHERE ID='" + cursor.getString(1) + "'");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                cursor.close();

                glbVars.db.getWritableDatabase().setTransactionSuccessful();
                glbVars.db.getWritableDatabase().endTransaction();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                    }
                });
            }
        }).start();
    }

    private void ConnectToSql() {
        String connString;
        String sql_server = getResources().getString(R.string.sql_server);
        String sql_port = getResources().getString(R.string.sql_port);
        String sql_db = getResources().getString(R.string.sql_db);
        String sql_loging = getResources().getString(R.string.sql_user);
        String sql_pass = getResources().getString(R.string.sql_pass);
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            connString = "jdbc:jtds:sqlserver://" + sql_server + ":" + sql_port + ";instance=MSSQLSERVER;databaseName=" + sql_db + ";user=" + sql_loging + ";password=" + sql_pass;
            conn = DriverManager.getConnection(connString, sql_loging, sql_pass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateStatus() {
        Thread thUpdateStatus;
        progress = null;
        progress = new ProgressDialog(getActivity());
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setCancelable(true);
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        if (conn == null) {
            ConnectToSql();
        }

        thUpdateStatus = new Thread(new Runnable() {
            public void run() {
                SQLiteStatement statement;

                String sql_update = "UPDATE ZAKAZY SET STATUS=? WHERE DOCNO=? AND TP_ID=? AND CONTR_ID=?";
                statement = glbVars.db.getWritableDatabase().compileStatement(sql_update);
                int cntOrders = 0;
                try {
                    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    reset = stmt.executeQuery("SELECT BASEDOC, CONTR ,TP, ORD_STATUS FROM V_ORDER_STATUS ORDER BY BASEDOC");
                    reset.last();
                    cntOrders = reset.getRow();
                    progress.setMax(cntOrders);
                    reset.beforeFirst();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    glbVars.db.getWritableDatabase().beginTransaction();
                    String ID = "", CONTR = "", TP = "";
                    int STATUS = 1;
                    Cursor c;
                    while (reset.next()) {
                        ID = reset.getString(1);
                        CONTR = reset.getString(2);
                        TP = reset.getString(3);
                        STATUS = reset.getInt(4);
                        c = glbVars.db.getWritableDatabase().rawQuery("SELECT DOCNO FROM ZAKAZY WHERE DOCNO='" + ID + "' AND TP_ID='" + TP + "' AND CONTR_ID='" + CONTR + "'", null);
                        if (c.moveToFirst()) {
                            statement.clearBindings();
                            statement.bindLong(1, STATUS);
                            statement.bindString(2, ID);
                            statement.bindString(3, TP);
                            statement.bindString(4, CONTR);
                            statement.executeUpdateDelete();
                            statement.clearBindings();
                        }
                        c.close();
                        progressStatus += 1;
                        handler.post(new Runnable() {
                            public void run() {
                                progress.setProgress(progressStatus);
                            }
                        });
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
                    glbVars.db.getWritableDatabase().endTransaction();
                }
//              Конец обновления списка номенклатуры
                handler.post(new Runnable() {
                    public void run() {
                        progress.dismiss();
                        glbVars.Orders.requery();
                        glbVars.OrdersAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        thUpdateStatus.start();
    }
}
