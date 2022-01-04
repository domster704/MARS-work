package com.amber.armtp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
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
import android.widget.TextView;
import android.widget.Toast;

import com.linuxense.javadbf.DBFException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Updated by domster704 on 27.09.2021
 */
public class JournalFragment extends Fragment {
    private static final int ID_GOBACK = 101;

    private final ArrayList<GlobalVars.CheckBoxData> chosenOrders = new ArrayList<>();

    private int[] itemsList;

    Menu mainMenu;
    public GlobalVars glbVars;
    TextView tvOrder, tvContr, tvAddr, tvDocDate, tvStatus;
    private final AdapterView.OnItemClickListener GridOrdersClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {
            glbVars.layout.setVisibility(View.GONE);
            ClearAllMenuItems();
            mainMenu.add(Menu.NONE, ID_GOBACK, Menu.NONE, "Вернуться назад")
                    .setIcon(R.drawable.back_arrow)
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

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
    PopupMenu nomPopupMenu;
    AlertDialog.Builder builder;

    ProgressDialog progress;
    private final AdapterView.OnItemLongClickListener GridOrdersLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, final View view, int position, long id) {
            tvOrder = view.findViewById(R.id.ColOrdDocNo);
            tvStatus = view.findViewById(R.id.ColOrdStatus);
            final String ID = tvOrder.getText().toString();
            final String Status = tvStatus.getText().toString();
            nomPopupMenu = new PopupMenu(getActivity(), view);
            nomPopupMenu.getMenuInflater().inflate(R.menu.order_context_menu, nomPopupMenu.getMenu());
            nomPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.CtxOrdEdit:
                            glbVars.OrderID = ID;
                            // Use the Builder class for convenient dialog construction
                            builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                            builder.setMessage("Вы уверены?")
                                    .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                        }
                                    })
                                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            EditOrder(ID);
                                            Fragment fragment = new FormOrderFragment();
                                            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                                            fragmentTransaction.replace(R.id.frame, fragment, "frag_form_order");
                                            fragmentTransaction.commit();
                                        }
                                    });
                            builder.create();
                            builder.show();
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
                nomPopupMenu.getMenu().findItem(R.id.CtxOrdEdit).setEnabled(false);
            }

            if (Status.equals("Оформлен") || Status.equals("Оформлен(-)") || Status.equals("Собран(-)") || Status.equals("Собран") || Status.equals("Получен")) {
                nomPopupMenu.getMenu().findItem(R.id.CtxOrdEdit).setEnabled(false);
            }

            if (Status.equals("Отправлен")) {
                nomPopupMenu.getMenu().findItem(R.id.CtxOrdEdit).setEnabled(false);
            }

            nomPopupMenu.show();
            return true;
        }

    };
    private android.support.v7.widget.Toolbar toolbar;

    public JournalFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.journal_fragment, container, false);
        glbVars.view = rootView;
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
        deleteExtraOrders();
    }

    @Override
    public void onPause() {
        super.onPause();
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

    @SuppressLint("CutPasteId")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        glbVars.gdOrders = Objects.requireNonNull(getActivity()).findViewById(R.id.listSMS);
        glbVars.orderdtList = getActivity().findViewById(R.id.listOrdersDt);

        glbVars.layout = getActivity().findViewById(R.id.checkboxLayout);
        glbVars.LoadOrders();

        glbVars.gdOrders.setOnItemClickListener(GridOrdersClick);
        glbVars.gdOrders.setOnItemLongClickListener(GridOrdersLongClick);

        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);
        glbVars.toolbar.setSubtitle("Всего заказов: " + glbVars.gdOrders.getCount());
        glbVars.toolbar.setTitle("Журнал");

        glbVars.viewFlipper = getActivity().findViewById(R.id.viewflipper);
    }

    /**
     * Если кол-во заказов > 100, то удаляем самые старые заказы, которые выходят за рамки 100 заказов
     */
    public void deleteExtraOrders() {
        if (glbVars.gdOrders.getCount() > 100) {
            for (int i = 0; i < glbVars.gdOrders.getCount() - 100; i++) {
                int id = GlobalVars.allOrders.get(i).id;
                glbVars.dbOrders.DeleteOrderByID(id);
            }
            GlobalVars.allOrders.subList(0, glbVars.gdOrders.getCount() - 100).clear();
            glbVars.LoadOrders();

            toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
            toolbar.setSubtitle("Всего заказов: " + glbVars.gdOrders.getCount() + " из возможных 100");
        }
    }

    private void SendDBFFile(String FileName) {
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

                    inputStream.close();
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
                cHead = glbVars.db.getWritableDatabase().rawQuery("SELECT TP, CONTR, ADDR, DOC_DATA, COMMENT FROM ZAKAZY WHERE DOCID='" + OrderID + "'", null);
                if (cHead.moveToNext()) {
                    try {
                        if (glbVars.db.getCount() == 0) {
                            glbVars.db.getWritableDatabase().execSQL("INSERT INTO ORDERS(TP,CONTR,ADDR,DOC_DATA,COMMENT) VALUES ('" + cHead.getString(0) + "', '" + cHead.getString(1) + "', '" + cHead.getString(2) + "', '" + cHead.getString(3) + "', '" + cHead.getString(4) + ")");
                        } else {
                            glbVars.db.getWritableDatabase().execSQL("UPDATE ORDERS SET TP = '" + cHead.getString(0) + "', CONTR = '" + cHead.getString(1) + "',ADDR = '" + cHead.getString(2) + "', DOC_DATA = '" + cHead.getString(3) + "', COMMENT = '" + cHead.getString(4));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    cHead.close();
                }

                glbVars.db.getWritableDatabase().execSQL("UPDATE Nomen SET ZAKAZ=0 WHERE ZAKAZ>0");
                cNom = glbVars.db.getWritableDatabase().rawQuery("SELECT QTY, NOMEN FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + OrderID + "'", null);
                try {
                    while (cNom.moveToNext()) {
                        glbVars.db.getWritableDatabase().execSQL("UPDATE Nomen SET ZAKAZ=" + cNom.getInt(0) + " WHERE KOD5='" + cNom.getString(1) + "'");
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
                cursor = glbVars.db.getWritableDatabase().rawQuery("SELECT QTY, NOMEN FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + OrderID + "'", null);
                try {
                    while (cursor.moveToNext()) {
                        glbVars.db.getWritableDatabase().execSQL("UPDATE Nomen SET ZAKAZ=" + cursor.getInt(0) + " WHERE KOD5='" + cursor.getString(1) + "'");
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.journal_menu, menu);
        mainMenu = menu;

        itemsList = new int[mainMenu.size()];
        for (int i = 0; i < mainMenu.size(); i++) {
            itemsList[i] = mainMenu.getItem(i).getItemId();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.SelectAllOrders:
                selectAllOrders();
                return true;
            case R.id.SendOrders:
                if (glbVars.isNetworkAvailable()) {
                    if (isAtLeastOneSelectedOrder())
                        return true;

                    int countOfSentOrders = 0;
                    for (GlobalVars.CheckBoxData i : chosenOrders) {
                        if (i.status.equals("Отправлен")) {
                            resendOrder(i.id);
                            countOfSentOrders++;
                        }
                    }

                    if (chosenOrders.size() - countOfSentOrders == 0) {
                        glbVars.LoadOrders();
                        return true;
                    }

                    int[] chosenOrdersForSending = new int[chosenOrders.size() - countOfSentOrders];

                    int index = 0;
                    for (int i = 0; i < chosenOrders.size(); i++) {
                        if (!chosenOrders.get(i).status.equals("Отправлен")) {
                            chosenOrdersForSending[index] = chosenOrders.get(i).id;
                            index++;
                        }
                    }

                    try {
                        glbVars.SendOrders(chosenOrdersForSending);
                    } catch (DBFException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getActivity(), "Нет доступного инетрнет соединения. Проверьте соединение с Интернетом", Toast.LENGTH_LONG).show();
                }
                return true;
            case ID_GOBACK:
                Fragment fragment = new JournalFragment();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame, fragment, "frag_journal");
                fragmentTransaction.commit();
                glbVars.layout.setVisibility(View.VISIBLE);
                return true;
            case R.id.DeleteOrders:
                if (isAtLeastOneSelectedOrder())
                    return true;

                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                builder.setMessage("Удалить заказы?")
                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                for (GlobalVars.CheckBoxData data : chosenOrders) {
                                    glbVars.dbOrders.DeleteOrderByID(data.id);
                                }
                                chosenOrders.clear();

                                glbVars.LoadOrders();
                                toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
                                toolbar.setSubtitle("Всего заказов: " + glbVars.gdOrders.getCount());
                            }
                        })
                        .setNeutralButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                for (GlobalVars.CheckBoxData data : chosenOrders) {
                                    if (data.checkBox.isChecked()) {
                                        data.checkBox.setChecked(false);
                                    }
                                }
                                chosenOrders.clear();
                            }
                        });

                AlertDialog alertDlg = builder.create();
                alertDlg.show();

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
        for (GlobalVars.CheckBoxData i : GlobalVars.allOrders) {
            if (i.checkBox.isChecked()) {
                chosenOrders.add(new GlobalVars.CheckBoxData(i.id, i.checkBox, i.status));
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
            return true;
        } else {
            return false;
        }
    }

    /**
     * Повторно отправляет заказ со статутосм "Отправлено" (логично)
     *
     * @param id - поле ROWID в таблице ZAKAZY
     */
    private void resendOrder(int id) {
        String FileName = "";

        try {
            FileName = glbVars.FormDBFForZakaz(id);
        } catch (DBFException e) {
            e.printStackTrace();
        } finally {
            if (!FileName.equals("")) {
                SendDBFFile(FileName);
            } else {
                Toast.makeText(getActivity(), "Неверное имя файла для отправки", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isChecked = false;

    private void selectAllOrders() {
        chosenOrders.clear();
        for (int i = 0; i < GlobalVars.allOrders.size(); i++) {
            GlobalVars.allOrders.get(i).checkBox.setChecked(!isChecked);
            chosenOrders.add(GlobalVars.allOrders.get(i));
        }
        isChecked = !isChecked;
    }
}
