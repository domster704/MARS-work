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
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amber.armtp.Config;
import com.amber.armtp.GlobalVars;
import com.amber.armtp.R;
import com.amber.armtp.ServerDetails;
import com.amber.armtp.annotations.PGShowing;
import com.amber.armtp.ftp.Ftp;
import com.amber.armtp.interfaces.BackupServerConnection;
import com.amber.armtp.interfaces.ServerChecker;
import com.linuxense.javadbf.DBFException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Updated by domster704 on 27.09.2021
 */
public class JournalFragment extends Fragment implements ServerChecker, BackupServerConnection {
    private static final int ID_GOBACK = 101;

    private final ArrayList<GlobalVars.ChosenOrdersData> chosenOrders = new ArrayList<>();
    public GlobalVars glbVars;
    Menu mainMenu;
    TextView tvOrder, tvContr, tvAddr, tvDocDate, tvStatus;
    PopupMenu nomPopupMenu;
    AlertDialog.Builder builder;

    private final AdapterView.OnItemLongClickListener GridOrdersLongClick = new AdapterView.OnItemLongClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, final View view, int position, long id) {
            view.setElevation(10f);

            tvOrder = view.findViewById(R.id.ColOrdDocNo);
            tvStatus = view.findViewById(R.id.ColOrdStatus);

            final String ID = tvOrder.getText().toString();
            final String Status = tvStatus.getText().toString();

            nomPopupMenu = new PopupMenu(getActivity(), view);
            nomPopupMenu.getMenuInflater().inflate(R.menu.order_context_menu, nomPopupMenu.getMenu());
            nomPopupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.CtxOrdEdit:
                        glbVars.OrderID = ID;
                        builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                        builder.setMessage("Вы уверены?")
                                .setNegativeButton("Нет", (dialog, id1) -> {
                                })
                                .setPositiveButton("Да", (dialog, id1) -> {
                                    modifyOrder(ID, false);
//                                    Fragment fragment = new FormOrderFragment();
//                                    FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//                                    fragmentTransaction.replace(R.id.frame, fragment, "frag_form_order");
//                                    fragmentTransaction.commit();
                                });
                        builder.create();
                        builder.show();
                        return true;
                    case R.id.CtxOrdCopy:
                        modifyOrder(ID, true);
                        return true;
                    case R.id.CtxOrdShow:
                        glbVars.layout.setVisibility(View.GONE);
                        ClearAllMenuItems();
                        mainMenu.add(Menu.NONE, ID_GOBACK, Menu.NONE, "Вернуться назад")
                                .setIcon(R.drawable.back_arrow)
                                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

                        tvOrder = view.findViewById(R.id.ColOrdDocNo);
                        tvContr = view.findViewById(R.id.ColOrdContr);
                        tvAddr = view.findViewById(R.id.ColOrdAddr);
                        tvDocDate = view.findViewById(R.id.ColOrdDocDate);
                        tvStatus = view.findViewById(R.id.ColOrdStatus);

                        glbVars.ordStatus = tvStatus.getText().toString();
//                        String ID = tvOrder.getText().toString();

                        glbVars.LoadOrdersDetails(ID);
                        glbVars.viewFlipper.setDisplayedChild(1);
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

    private int[] itemsList;
    private final AdapterView.OnItemClickListener GridOrdersClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> myAdapter, View view, int position, long mylng) {
            boolean isCheckedNow = GlobalVars.allOrders.get(position).isChecked();
            GlobalVars.allOrders.get(position).setChecked(!isCheckedNow);
            glbVars.OrdersAdapter.notifyDataSetChanged();
        }
    };
    private android.support.v7.widget.Toolbar toolbar;
    private boolean isChecked = false;

    public JournalFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.journal_fragment, container, false);
        glbVars.CurView = rootView;
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
        glbVars = (GlobalVars) getActivity().getApplicationContext();
        glbVars.setContext(getActivity().getApplicationContext());
        GlobalVars.CurFragmentContext = getActivity();
        GlobalVars.CurAc = getActivity();
    }

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        glbVars.gdOrders = Objects.requireNonNull(getActivity()).findViewById(R.id.listSMS);
        glbVars.orderDtList = getActivity().findViewById(R.id.listOrdersDt);

        glbVars.layout = getActivity().findViewById(R.id.checkboxLayout);
        glbVars.LoadOrders();

        glbVars.gdOrders.setOnItemClickListener(GridOrdersClick);
        glbVars.gdOrders.setOnItemLongClickListener(GridOrdersLongClick);

        glbVars.viewFlipper = getActivity().findViewById(R.id.viewflipper);

        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);
        glbVars.toolbar.setSubtitle("Всего заказов: " + glbVars.gdOrders.getCount() + " из 100");
        getActivity().setTitle("Журнал");

        ScrollView view = getActivity().findViewById(R.id.scrollViewJ);
        view.fullScroll(View.FOCUS_DOWN);

        deleteExtraOrders();
    }

    /**
     * Если кол-во заказов > 100, то удаляем самые старые заказы, которые выходят за рамки 100 заказов
     */
    public void deleteExtraOrders() {
        if (glbVars.gdOrders.getCount() > 100) {
            for (int i = 100; i < glbVars.gdOrders.getCount(); i++) {
                glbVars.dbOrders.deleteOrderByID(GlobalVars.allOrders.get(i).getId());
            }
            GlobalVars.allOrders.subList(100, glbVars.gdOrders.getCount()).clear();

            glbVars.Orders.requery();
            glbVars.OrdersAdapter.notifyDataSetChanged();

            GlobalVars.CurAc.runOnUiThread(() -> {
                toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
                toolbar.setSubtitle("Всего заказов: " + glbVars.gdOrders.getCount() + " из 100");
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
        itemsList[itemsList.length - 1] = ID_GOBACK;
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
                            if (!glbVars.isNetworkAvailable()) {
                                Config.sout("Нет доступного инетрнет соединения. Проверьте соединение с Интернетом");
                                return;
                            }

                            if (!isAtLeastOneSelectedOrder())
                                return;

                            Thread mainLogic = new Thread(new Runnable() {
                                @Override
                                @PGShowing
                                public void run() {
                                    isChecked = false;

                                    for (GlobalVars.ChosenOrdersData i : chosenOrders) {
                                        if (i.getStatus().equals("Сохранён") || i.getStatus().equals("Отправлен")) {
                                            sendOrders(i.getId());
                                        }
                                    }
                                    Config.sout("Заказы отправлены");
                                }
                            });

                            runCheckServerForAvailability(mainLogic);
                        });
                AlertDialog alertDlgSend = builderSend.create();
                alertDlgSend.show();

                return true;
            case ID_GOBACK:
                Fragment fragment = new JournalFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame, fragment, "frag_journal");
                fragmentTransaction.commit();
                glbVars.layout.setVisibility(View.VISIBLE);
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

                            for (GlobalVars.ChosenOrdersData data : chosenOrders) {
                                glbVars.dbOrders.deleteOrderByID(data.getId());
                            }
                            chosenOrders.clear();

                            glbVars.LoadOrders();
                            toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
                            toolbar.setSubtitle("Всего заказов: " + glbVars.gdOrders.getCount() + " из 100");
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
        for (GlobalVars.ChosenOrdersData i : GlobalVars.allOrders) {
            if (i.isChecked()) {
                chosenOrders.add(new GlobalVars.ChosenOrdersData(i.getId(), true, i.getStatus()));
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
     * Повторно отправляет заказ со статутосм "Отправлено" (логично)
     *
     * @param id - поле ROWID в таблице ZAKAZY
     */
    private void sendOrders(int id) {
        String FileName = "";

        try {
            FileName = glbVars.CreateDBFForSending(id);
        } catch (DBFException e) {
            e.printStackTrace();
        } finally {
            if (!FileName.equals("")) {
                SendDBFFile(FileName, id);
            } else {
                Config.sout("Ошибка при отправке");
            }
        }
    }

    private void selectAllOrders() {
        chosenOrders.clear();
        for (int i = 0; i < GlobalVars.allOrders.size(); i++) {
            GlobalVars.allOrders.get(i).setChecked(!isChecked);
            chosenOrders.add(GlobalVars.allOrders.get(i));
        }
        isChecked = !isChecked;
        glbVars.OrdersAdapter.notifyDataSetChanged();
    }

    private void SendDBFFile(String FileName, int id) {
        final String tmp_filename = FileName;

        new Thread(() -> {
            try {
                FTPClient ftpClient = new FTPClient();
                int timeout = 10 * 1000;
                ftpClient.setDefaultTimeout(timeout);
                ftpClient.setDataTimeout(timeout);
                ftpClient.setConnectTimeout(timeout);
                ftpClient.setControlKeepAliveTimeout(timeout);
                ftpClient.setControlKeepAliveReplyTimeout(timeout);

                if (!tryConnectToDefaultIpOtherwiseToBackupIp(ftpClient)) {
                    return;
                }

//                ftpClient.connect(ServerDetails.getInstance().host, 21);
                ftpClient.login(ServerDetails.getInstance().user, ServerDetails.getInstance().password);
                ftpClient.enterLocalPassiveMode();
                ftpClient.changeWorkingDirectory("EXCHANGE/IN/MARS");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                InputStream inputStream;
                File secondLocalFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + tmp_filename);

                inputStream = new FileInputStream(secondLocalFile);

                OutputStream outputStream = ftpClient.storeFileStream(tmp_filename);
                byte[] bytesIn = new byte[4096];
                int read;

                try {
                    while ((read = inputStream.read(bytesIn)) != -1) {
                        outputStream.write(bytesIn, 0, read);
                    }
                    outputStream.close();
                } catch (Exception ignored) {
                }

                if (checkFileIntegrityOnServer(FileName, secondLocalFile.length())) {
                    if (ftpClient.completePendingCommand()) {
                        secondLocalFile.delete();
                        changeIntegralFile(FileName);
                        glbVars.dbOrders.setZakazStatus("Отправлен", id);
                        glbVars.LoadOrders();
                    }
                } else {
                    Config.sout("Сбой отправки");
                }

                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean checkFileIntegrityOnServer(String FileName, long fileInAppSize) throws Exception {
        Ftp ftp = new Ftp(ServerDetails.getInstance());
        long fileSize = ftp.getFileSize("EXCHANGE/IN/MARS/" + FileName);
        return fileSize == fileInAppSize;
    }

    private void changeIntegralFile(String FileName) throws Exception {
        Ftp ftp = new Ftp(ServerDetails.getInstance());
        ftp.renameFile("EXCHANGE/IN/MARS/" + FileName);
    }

    private void modifyOrder(String OrderID, boolean isCopied) {
        String messageName = isCopied ? "копирования" : "редоктирования";
        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
//                try {
                try (Cursor cHead = glbVars.dbOrders.getWritableDatabase().rawQuery("SELECT ROWID FROM ZAKAZY WHERE DOCID='" + OrderID + "'", null)) {
                    cHead.moveToNext();
                    if (cHead.getCount() == 0) {
                        Config.sout("Отсутсвует информация о загаловке заказа");
                        return;
                    }
                } catch (Exception e) {
                    Config.sout("Ошибка во время копирования " + messageName);
                    e.printStackTrace();
                    return;
                }

                SQLiteDatabase db = glbVars.db.getWritableDatabase();
                try (Cursor cNom = glbVars.dbOrders.getReadableDatabase().rawQuery("SELECT QTY, NOMEN FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + OrderID + "'", null)) {
                    glbVars.db.ResetNomen();
                    db.beginTransaction();
                    while (cNom.moveToNext()) {
                        db.execSQL("UPDATE Nomen SET ZAKAZ=" + cNom.getInt(cNom.getColumnIndex("QTY")) + " WHERE KOD5='" + cNom.getString(cNom.getColumnIndex("NOMEN")) + "'");
                    }
//                        db.execSQL("DELETE FROM ORDERS");
                } catch (Exception e) {
                    e.printStackTrace();
                    Config.sout("Ошибка во время попытки " + messageName);
                    glbVars.db.ResetNomen();
                    return;
                } finally {
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }

                if (!isCopied) {
                    OrderHeadFragment.isNeededToUpdateOrderTable = true;
                }

                HashMap<String, String> orderData = glbVars.dbOrders.getOrderData(OrderID);
                if (orderData.size() == 0) {
                    Config.sout("Отсутсвуют данные о заказе");
                    return;
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
//                } catch (Exception e) {
//                    Config.sout(e.getMessage(), Toast.LENGTH_LONG);
//                }
            }
        }).start();
    }

//    private void copyOrder(final String OrderID) {
//        new Thread(new Runnable() {
//            @Override
//            @PGShowing
//            public void run() {
//                try (Cursor cursor = glbVars.dbOrders.getWritableDatabase().rawQuery("SELECT QTY, NOMEN, PRICE FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + OrderID + "'", null);){
//                    glbVars.db.ResetNomen();
//                    glbVars.db.getWritableDatabase().beginTransaction();
//                    while (cursor.moveToNext()) {
//                        glbVars.db.getWritableDatabase().execSQL("UPDATE Nomen SET ZAKAZ=" + cursor.getInt(cursor.getColumnIndex("QTY")) + " WHERE KOD5='" + cursor.getString(cursor.getColumnIndex("NOMEN")) + "'");
//                    }
//                    glbVars.db.getWritableDatabase().execSQL("DELETE FROM ORDERS");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Config.sout("Ошибка во время попытки копирования");
//                    glbVars.db.ResetNomen();
//                    return;
//                } finally {
//                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
//                    glbVars.db.getWritableDatabase().endTransaction();
//                }
//
//                HashMap<String, String> orderData = glbVars.dbOrders.getOrderData(OrderID);
//                if (orderData.size() == 0) {
//                    return;
//                }
//
//                Fragment fragment = new OrderHeadFragment();
//
//                Bundle args = new Bundle();
//                args.putBoolean("isOrderEditedOrCopied", true);
//                args.putString("TP", orderData.get("TP"));
//                args.putString("CONTR", orderData.get("CONTR"));
//                args.putString("ADDR", orderData.get("ADDR"));
//                args.putString("DELIVERY_DATE", orderData.get("DELIVERY_DATE"));
//                args.putString("COMMENT", orderData.get("COMMENT"));
//                fragment.setArguments(args);
//
//                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
//                fragmentTransaction.replace(R.id.frame, fragment, "frag_order_header");
//                fragmentTransaction.commit();
//            }
//        }).start();
//    }
}
