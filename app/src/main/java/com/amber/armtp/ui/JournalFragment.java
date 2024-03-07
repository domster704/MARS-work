package com.amber.armtp.ui;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.amber.armtp.BuildConfig;
import com.amber.armtp.R;
import com.amber.armtp.ServerDetails;
import com.amber.armtp.adapters.JournalAdapterSQLite;
import com.amber.armtp.adapters.JournalDetailsAdapterSQLite;
import com.amber.armtp.auxiliaryData.ChosenOrdersData;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.dbHelpers.DBOrdersHelper;
import com.amber.armtp.extra.Config;
import com.amber.armtp.extra.ExtraFunctions;
import com.amber.armtp.extra.ProgressBarShower;
import com.amber.armtp.ftp.Ftp;
import com.amber.armtp.interfaces.BackupServerConnection;
import com.amber.armtp.interfaces.ServerChecker;
import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

/**
 * Updated by domster704 on 27.09.2021
 */
public class JournalFragment extends Fragment implements ServerChecker, BackupServerConnection {
    private static final int ID_GO_BACK = 101;
    private int[] itemsList;
    public static String OrderID;
    public String ordStatus;
    private boolean isChecked = false;
    private final ArrayList<ChosenOrdersData> chosenOrders = new ArrayList<>();
    private Menu mainMenu;
    private TextView tvOrder, tvStatus;
    private LinearLayout layout;
    private AlertDialog.Builder builder;
    public GridView gdOrders;
    public GridView orderDtList;
    private ViewFlipper viewFlipper;
    public static ArrayList<ChosenOrdersData> allOrders = new ArrayList<>();
    private DBHelper dbHelper;
    private DBOrdersHelper dbOrders;
    private Cursor Orders;
    private JournalAdapterSQLite OrdersAdapter;
    private android.support.v7.widget.Toolbar toolbar;

    private final AdapterView.OnItemLongClickListener GridOrdersLongClick = new AdapterView.OnItemLongClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, final View view, int position, long id) {
            view.setElevation(10f);

            tvOrder = view.findViewById(R.id.ColOrdDocNo);
            tvStatus = view.findViewById(R.id.ColOrdStatus);

            final String ID = tvOrder.getText().toString();
            final String Status = tvStatus.getText().toString();

            PopupMenu nomPopupMenu = new PopupMenu(getActivity(), view);
            nomPopupMenu.getMenuInflater().inflate(R.menu.order_context_menu, nomPopupMenu.getMenu());
            nomPopupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.CtxOrdEdit:
                        OrderID = ID;
                        builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                        builder.setMessage("Вы уверены?")
                                .setNegativeButton("Нет", (dialog, id1) -> {
                                })
                                .setPositiveButton("Да", (dialog, id1) -> modifyOrder(ID, false));
                        builder.create();
                        builder.show();
                        return true;
                    case R.id.CtxOrdCopy:
                        modifyOrder(ID, true);
                        return true;
                    case R.id.CtxOrdShow:
                        layout.setVisibility(View.GONE);
                        ClearAllMenuItems();
                        mainMenu.add(Menu.NONE, ID_GO_BACK, Menu.NONE, "Вернуться назад")
                                .setIcon(R.drawable.back_arrow)
                                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

                        tvOrder = view.findViewById(R.id.ColOrdDocNo);
                        tvStatus = view.findViewById(R.id.ColOrdStatus);

                        ordStatus = tvStatus.getText().toString();

                        LoadOrdersDetails(ID);
                        viewFlipper.setDisplayedChild(1);
                    default:
                        return true;
                }
            });

            nomPopupMenu.setOnDismissListener(popupMenu -> {
                popupMenu.dismiss();
                view.setElevation(0f);
            });

            if (!Status.equals("Сохранён")) {
                nomPopupMenu.getMenu().findItem(R.id.CtxOrdEdit).setEnabled(false);
            }

            nomPopupMenu.show();
            return true;
        }

    };

    private final AdapterView.OnItemClickListener GridOrdersClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> myAdapter, View view, int position, long mylng) {
            boolean isCheckedNow = allOrders.get(position).isChecked();
            allOrders.get(position).setChecked(!isCheckedNow);
            OrdersAdapter.notifyDataSetChanged();
        }
    };

    public JournalFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.journal_fragment, container, false);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dbHelper = new DBHelper(getActivity().getApplicationContext());
        dbOrders = new DBOrdersHelper(getActivity().getApplicationContext());

        toolbar = getActivity().findViewById(R.id.toolbar);

        gdOrders = Objects.requireNonNull(getActivity()).findViewById(R.id.listSMS);
        orderDtList = getActivity().findViewById(R.id.listOrdersDt);

        layout = getActivity().findViewById(R.id.checkboxLayout);
        LoadOrders();

        gdOrders.setOnItemClickListener(GridOrdersClick);
        gdOrders.setOnItemLongClickListener(GridOrdersLongClick);

        viewFlipper = getActivity().findViewById(R.id.viewflipper);

        getActivity().setTitle("Журнал");

        ScrollView view = getActivity().findViewById(R.id.scrollViewJ);
        view.fullScroll(View.FOCUS_DOWN);
    }


    /**
     * Если кол-во заказов > 100, то удаляем самые старые заказы, которые выходят за рамки 100 заказов
     */
    public void deleteExtraOrders() {
        if (gdOrders.getCount() > 100) {
            getActivity().runOnUiThread(() -> {
                for (int i = 100; i < gdOrders.getCount(); i++) {
                    dbOrders.deleteOrderByID(allOrders.get(i).getId());
                }
                allOrders.subList(100, gdOrders.getCount()).clear();

                Orders.requery();
                OrdersAdapter.notifyDataSetChanged();

                toolbar.setSubtitle("Всего заказов: " + gdOrders.getCount() + " из 100");
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.journal_menu, menu);
        mainMenu = menu;

        itemsList = new int[mainMenu.size() + 1];
        for (int i = 0; i < mainMenu.size(); i++) {
            itemsList[i] = mainMenu.getItem(i).getItemId();
        }
        itemsList[itemsList.length - 1] = ID_GO_BACK;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.SelectAllOrders:
                selectAllOrders();
                return true;
            case R.id.SendOrders:
                AlertDialog.Builder builderSend = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                builderSend.setMessage("Отправить заказы?")
                        .setNeutralButton("Нет", (dialogInterface, i) -> dialogInterface.cancel())
                        .setNegativeButton("Да", (dialogInterface, ii) -> {
                            if (!ExtraFunctions.isNetworkAvailable(getActivity())) {
                                Config.sout("Нет доступного интернет соединения. Проверьте соединение с интернетом", getContext());
                                return;
                            }

                            if (!isAtLeastOneSelectedOrder())
                                return;

                            Thread mainLogic = new Thread(() -> new ProgressBarShower(getContext()).setFunction(() -> {
                                isChecked = false;

                                boolean isForEntered = false;
                                for (ChosenOrdersData i : chosenOrders) {
                                    if (i.getStatus().equals("Сохранён") || i.getStatus().equals("Отправлен")) {
                                        sendOrders(i.getId());
                                        isForEntered = true;
                                    }
                                }
                                if (!isForEntered)
                                    return null;

                                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Заказы отправлены", Toast.LENGTH_SHORT).show());
                                return null;
                            }).start());

                            runCheckServerForAvailability(getContext(), mainLogic);
                        });
                AlertDialog alertDlgSend = builderSend.create();
                alertDlgSend.show();

                return true;
            case ID_GO_BACK:
                Fragment fragment = new JournalFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame, fragment, "frag_journal");
                fragmentTransaction.commit();
                layout.setVisibility(View.VISIBLE);
                return true;
            case R.id.DeleteOrders:
                if (!isAtLeastOneSelectedOrder())
                    return true;

                AlertDialog.Builder builderDel = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                builderDel.setMessage("Удалить заказы?")
                        .setNegativeButton("Нет", (dialogInterface, i) -> dialogInterface.cancel())
                        // Поставил NeutralButton, чтобы кнопка была слева, а не рядом с кнопкой "Нет"
                        .setNeutralButton("Да", (dialogInterface, i) -> {
                            isChecked = false;

                            for (ChosenOrdersData data : chosenOrders) {
                                dbOrders.deleteOrderByID(data.getId());
                            }
                            chosenOrders.clear();

                            LoadOrders();
                            toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
                            toolbar.setSubtitle("Всего заказов: " + gdOrders.getCount() + " из 100");
                        });

                AlertDialog alertDlgDel = builderDel.create();
                alertDlgDel.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void ClearAllMenuItems() {
        for (int i : itemsList) {
            mainMenu.removeItem(i);
        }
    }

    /**
     * Записывает в chosenOrders какие заказы были выбраны
     */
    private void checkCB() {
        chosenOrders.clear();
        for (ChosenOrdersData i : allOrders) {
            if (i.isChecked()) {
                chosenOrders.add(new ChosenOrdersData(i.getId(), true, i.getStatus()));
            }
        }
    }

    /**
     * Проверяет наличие хотя бы одного выбранного заказа
     *
     * @return true  - если не выбран
     * false - если выбран
     */
    private boolean isAtLeastOneSelectedOrder() {
        checkCB();
        if (chosenOrders.size() == 0) {
            Toast.makeText(getActivity(), "Вы не выбрали заказ", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Повторно отправляет заказ со статусом "Отправлено" (логично)
     *
     * @param id - поле ROWID в таблице ZAKAZY
     */
    private void sendOrders(int id) {
        String FileName = "";

        try {
            FileName = CreateDBFForSending(id);
        } catch (DBFException e) {
            e.printStackTrace();
        } finally {
            if (!FileName.equals("")) {
                SendDBFFile(FileName, id);
            } else {
                Config.sout("Ошибка при отправке", getContext());
            }
        }
    }

    private void selectAllOrders() {
        chosenOrders.clear();
        for (int i = 0; i < allOrders.size(); i++) {
            allOrders.get(i).setChecked(!isChecked);
            chosenOrders.add(allOrders.get(i));
        }
        isChecked = !isChecked;
        OrdersAdapter.notifyDataSetChanged();
    }

    private void SendDBFFile(String FileName, int id) {
        final String tmp_filename = FileName;

        new Thread(() -> {
            try {
                int timeout = ServerDetails.getInstance().timeout;

                FTPClient ftpClient = new FTPClient();
                ftpClient.setDefaultTimeout(timeout);
                ftpClient.setDataTimeout(timeout);
                ftpClient.setConnectTimeout(timeout);
                ftpClient.setControlKeepAliveTimeout(timeout);
                ftpClient.setControlKeepAliveReplyTimeout(timeout);

                if (!tryConnectToDefaultIpOtherwiseToBackupIp(ftpClient)) {
                    return;
                }

                ftpClient.login(ServerDetails.getInstance().user, ServerDetails.getInstance().password);
                ftpClient.enterLocalPassiveMode();
                ftpClient.changeWorkingDirectory("EXCHANGE/IN/MARS");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                File secondLocalFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + tmp_filename);

                byte[] bytesIn = new byte[4096];
                int read;

                try (InputStream inputStream = new FileInputStream(secondLocalFile); OutputStream outputStream = ftpClient.storeFileStream(tmp_filename)) {
                    while ((read = inputStream.read(bytesIn)) != -1) {
                        outputStream.write(bytesIn, 0, read);
                    }
                } catch (Exception ignored) {
                }

                if (checkFileIntegrityOnServer(FileName, secondLocalFile.length())) {
                    if (ftpClient.completePendingCommand()) {
//                        secondLocalFile.delete();
                        changeIntegralFile(FileName);
                        dbOrders.setZakazStatus("Отправлен", id);
                        LoadOrders();
                    }
                } else {
                    Config.sout("Сбой отправки", getContext());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean checkFileIntegrityOnServer(String FileName, long fileInAppSize) throws Exception {
        Ftp ftp = new Ftp(ServerDetails.getInstance());
        long fileSize = ftp.getFileSize("EXCHANGE/IN/MARS/" + FileName);
        System.out.println(fileSize + " " + fileInAppSize);
        return fileSize == fileInAppSize;
    }

    private void changeIntegralFile(String FileName) throws Exception {
        Ftp ftp = new Ftp(ServerDetails.getInstance());
        ftp.renameFile("EXCHANGE/IN/MARS/" + FileName);
    }

    private void modifyOrder(String OrderID, boolean isCopied) {
        String messageName = isCopied ? "копирования" : "редактирования";
        new Thread(() -> new ProgressBarShower(getContext()).setFunction(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try (Cursor cHead = dbOrders.getReadableDatabase().rawQuery("SELECT ROWID FROM ZAKAZY WHERE DOCID='" + OrderID + "'", null)) {
                cHead.moveToNext();
                db.setTransactionSuccessful();
                if (cHead.getCount() == 0) {
                    Config.sout("Отсутствует информация о заголовке заказа", getContext());
                    return null;
                }
            } catch (Exception e) {
                Config.sout("Ошибка во время " + messageName, getContext());
                e.printStackTrace();
                return null;
            } finally {
                db.endTransaction();
            }

            db = dbHelper.getWritableDatabase();
            try (Cursor cNom = dbOrders.getReadableDatabase().rawQuery("SELECT QTY, NOMEN FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + OrderID + "'", null)) {
                dbHelper.ResetNomen();
                db.beginTransaction();
                while (cNom.moveToNext()) {
                    db.execSQL("UPDATE Nomen SET ZAKAZ=" + cNom.getInt(cNom.getColumnIndex("QTY")) + " WHERE KOD5='" + cNom.getString(cNom.getColumnIndex("NOMEN")) + "'");
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
                Config.sout("Ошибка во время попытки " + messageName, getContext());
                dbHelper.ResetNomen();
                return null;
            } finally {
                db.endTransaction();
            }

            if (!isCopied) {
                OrderHeadFragment.isNeededToUpdateOrderTable = true;
            }

            HashMap<String, String> orderData = dbOrders.getOrderData(OrderID);
            if (orderData.size() == 0) {
                Config.sout("Отсутствуют данные о заказе", getContext());
                return null;
            }

            Fragment fragment = new OrderHeadFragment();

            Bundle args = new Bundle();
            args.putBoolean("isOrderEditedOrCopied", true);
            args.putString("TP", orderData.get("TP"));
            args.putString("CONTR", orderData.get("CONTR"));
            args.putString("ADDR", orderData.get("ADDR"));
            args.putString("DELIVERY_DATE", orderData.get("DELIVERY_DATE"));
            args.putString("COMMENT", orderData.get("COMMENT"));
            fragment.setArguments(args);

            getActivity().runOnUiThread(() -> {
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame, fragment, "frag_order_header");
                fragmentTransaction.commit();
            });
            return null;
        }).start()).start();
    }

    public void LoadOrdersDetails(String ZakazID) {
        getActivity().runOnUiThread(() -> {
            _doUpdateQTYByOuted(ZakazID);

            allOrders.clear();
            layout.removeAllViews();

            orderDtList.setAdapter(null);
            Cursor ordersDt = dbOrders.getZakazDetails(ZakazID);
            JournalDetailsAdapterSQLite ordersDtAdapter = new JournalDetailsAdapterSQLite(getActivity(),
                    R.layout.orderdt_item,
                    ordersDt,
                    new String[]{"ZAKAZ_ID", "NOMEN", "DESCR", "QTY", "PRICE", "SUM"},
                    new int[]{R.id.ColOrdDtZakazID, R.id.ColOrdDtCod, R.id.ColOrdDtDescr, R.id.ColOrdDtQty, R.id.ColOrdDtPrice, R.id.ColOrdDtSum},
                    0);
            orderDtList.setAdapter(ordersDtAdapter);
        });
    }

    public void LoadOrders() {
        getActivity().runOnUiThread(() -> {
            try {
                Orders = dbOrders.getZakazy();
                if (Orders != null)
                    putCheckBox(Orders);

                OrdersAdapter = new JournalAdapterSQLite(getActivity(), R.layout.orders_item, Orders, new String[]{"DOCID", "STATUS", "DOC_DATE", "DELIVERY", "CONTR", "ADDR", "SUM", "COMMENT"}, new int[]{R.id.ColOrdDocNo, R.id.ColOrdStatus, R.id.ColOrdDocDate, R.id.ColOrdDeliveryDate, R.id.ColOrdContr, R.id.ColOrdAddr, R.id.ColOrdSum, R.id.ColOrdComment}, 0);
                gdOrders.setAdapter(OrdersAdapter);
            } catch (Exception e) {
                Config.sout(e, getContext(), Toast.LENGTH_LONG);
            } finally {
                toolbar.setSubtitle("Всего заказов: " + gdOrders.getCount() + " из 100");
                deleteExtraOrders();
            }
        });
    }

    private void _doUpdateQTYByOuted(String DocID) {
        SQLiteDatabase dbApp = dbOrders.getWritableDatabase();
        SQLiteDatabase dbVy = dbHelper.getReadableDatabase();

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

    public String CreateDBFForSending(int ID) throws DBFException {
        Cursor c;

        String TP, CONTR, ADDR, DOCNO = "", COMMENT, NOMEN;
        java.util.Date DELIVERY, DOCDATE;
        double QTY;
        String PRICE;

        c = dbOrders.getReadableDatabase().rawQuery("SELECT TP, CONTR, ADDR, ZAKAZY.DOCID as DOCID, ZAKAZY.DOC_DATE as DOC_DATE, ZAKAZY.DELIVERY_DATE as DEL_DATE, ZAKAZY.COMMENT as COMMENT, ZAKAZY_DT.NOMEN as NOMEN, ZAKAZY_DT.DESCR as DES, ZAKAZY_DT.QTY as QTY, ZAKAZY_DT.PRICE as PRICE FROM ZAKAZY JOIN ZAKAZY_DT ON ZAKAZY.DOCID = ZAKAZY_DT.ZAKAZ_ID WHERE ZAKAZY.ROWID='" + ID + "'", null);
        if (c.getCount() == 0) {
            Config.sout("В таблице заказов нет записей для отправки", getContext());
            return "";
        }

        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("ddMMyyyy_HHmmss");
        String dateNow = df.format(Calendar.getInstance().getTimeInMillis()) + Calendar.getInstance().get(Calendar.MILLISECOND);

        Cursor cForTpId = dbOrders.getReadableDatabase().rawQuery("SELECT TP FROM ZAKAZY WHERE rowid='" + ID + "'", null);

        cForTpId.moveToNext();
        String tpID = cForTpId.getString(cForTpId.getColumnIndex("TP"));
        cForTpId.close();

        String fileID = tpID + "_" + dateNow;
        String FileName = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + fileID + ".temp";
        String DBF_FileName = fileID + ".temp";

        File DBFFile = new File(FileName);
        if (DBFFile.exists()) {
            DBFFile.delete();
        }

        DBFWriter Table = new DBFWriter(DBFFile);
        Table.setCharactersetName("cp866");
        DBFField[] fields = new DBFField[11];

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
        index++;

        fields[index] = new DBFField();
        fields[index].setName("VERSION");
        fields[index].setDataType(DBFField.FIELD_TYPE_C);
        fields[index].setFieldLength(10);
        Table.setFields(fields);

        try {
            while (c.moveToNext()) {
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

                Object[] rowData = new Object[11];
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
                rowData[10] = BuildConfig.VERSION_NAME;
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

    private void putCheckBox(Cursor c) {
        allOrders.clear();
        layout.removeAllViews();

        if (c.getCount() == 0) return;

        int id;
        String status;
        boolean isChecked;

        while (c.moveToNext()) {
            id = c.getInt(c.getColumnIndex("_id"));
            status = c.getString(c.getColumnIndex("STATUS"));
            isChecked = status.equals("Сохранён");

            allOrders.add(new ChosenOrdersData(id, isChecked, status));
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
}
