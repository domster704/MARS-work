package com.amber.armtp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.amber.armtp.dbHelpers.DBAppHelper;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.dbHelpers.DBOrdersHelper;
import com.amber.armtp.interfaces.PGShowing;
import com.amber.armtp.ui.FormOrderFragment;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by filimonov on 22-08-2016.
 * Updated by domster704 on 27.09.2021
 */
public class GlobalVars extends Application {
    public static ArrayList<CheckBoxData> allOrders = new ArrayList<>();

    public static FragmentActivity CurAc;

    public Context glbContext;
    public Context frContext;
    public Cursor myNom, mySgi, myGrups, Orders, OrdersDt;
    public Cursor curWC, curFocus;

    public MyCursorAdapter NomenAdapter, PreviewZakazAdapter;
    public JournalAdapter OrdersAdapter;
    public JournalDetailsAdapter OrdersDtAdapter;

    public View view;
    public DBHelper db;
    public DBOrdersHelper dbOrders;
    public DBAppHelper dbApp;
    public GridView nomenList;
    public android.support.v7.widget.Toolbar toolbar;
    public String SelectGroup = null;

    public LinearLayout layout;

    public boolean isDiscount = false;
    public float Discount = 0;

    public int MultiQty = 0;
    public boolean isMultiSelect = false;

    public String frSgi;
    public String frGroup;

    public Spinner spSgi, spGrup;
    public Spinner spWC, spFocus;
    public File appDBFolder = new File(GetSDCardPath() + "ARMTP_DB");
    public File appPhotoFolder = new File(GetSDCardPath() + "ARM_PHOTO");
    public File appUpdatesFolder = new File(GetSDCardPath() + "ARM_UPDATES");
    public String AsyncFileName;
    public SubsamplingScaleImageView imageView;
    public AlertDialog alertPhoto = null;
    public PopupMenu nomPopupMenu;
    public boolean isSecondPhoto = false;
    public String OrderID = "";
    public Cursor TP;
    public String CurrentTp, CurrentDebTP;
    public Cursor Contr;
    public Cursor Addr;

    public Spinner spinContr, spinAddr, TPList;
    public Button btSave;
    public Calendar DeliveryDate, DeliveryTime;
    public EditText txtDate;
    public EditText edContrFilter;
    public EditText txtComment;
    public TextView spTp, spContr, spAddr;
    public TextView tvContr, tvTP;
    public ViewFlipper viewFlipper;
    public String ordStatus;
    public GridView gdOrders;
    public GridView orderdtList;
    public String DBF_FIleForSend;
    public String DBF_FileName;
    public Button btClearFilter;
    public GridView debetList;
    public Spinner spTP;
    public Cursor curDebet, curDebetTp;
    public int BeginPos = 0, EndPos = 0;

    private static String CurSGI = "0", CurGroup = "0", CurWCID = "0", CurFocusID = "0", CurSearchName = "";
    private boolean isNewLoaded = false;

    private final AdapterView.OnItemSelectedListener SelectedContr = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            String ItemID = Contr.getString(Contr.getColumnIndex("CODE"));
            if (!ItemID.equals("0")) {
                LoadContrAddr(ItemID);
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    public AdapterView.OnItemClickListener GridNomenClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {
            long viewId = myView.getId();

            if (viewId == R.id.ColNomPhoto) {
                if (((TextView) myView).getText() == null || ((TextView) myView).getText().toString().equals(""))
                    return;

                isSecondPhoto = false;
                String photoDir = getPhotoDir();
                long ID = myAdapter.getItemIdAtPosition(position);

                String FileName = db.GetCod(ID);
                File imgFile = new File(photoDir + "/" + FileName);
                if (!imgFile.exists() || imgFile.length() == 0) {
                    AsyncFileName = FileName;
                    if (isNetworkAvailable()) {
                        DownloadPhoto(FileName);
                    } else {
                        Toast.makeText(glbContext, "Нет доступного интернет соединения", Toast.LENGTH_LONG).show();
                    }
                } else {
                    try {
                        ShowNomenPhoto(FileName);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Не получилось открыть фото", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (viewId == R.id.btPlus) {
                View parent = (View) myView.getParent();
                TextView tvKOD5 = parent.findViewById(R.id.ColNomCod);
                TextView tvPrice = parent.findViewById(R.id.ColNomPrice);
                String price = tvPrice.getText().toString();
                String kod5 = tvKOD5.getText().toString();

                db.putPriceInNomen(kod5, price);
                db.PlusQty(kod5);

                myNom.requery();
                String ToolBarContr = db.GetToolbarContr();
                String OrderSum = db.getOrderSum();
                if (OrderSum.length() > 0) {
                    toolbar.setSubtitle(ToolBarContr + OrderSum + " руб.");
                } else {
                    toolbar.setSubtitle(ToolBarContr);
                }
                if (NomenAdapter != null) {
                    NomenAdapter.notifyDataSetChanged();
                }
                if (PreviewZakazAdapter != null) {
                    PreviewZakazAdapter.notifyDataSetChanged();
                }
            } else if (viewId == R.id.btMinus) {
                View parent = (View) myView.getParent();
                TextView tvKOD5 = parent.findViewById(R.id.ColNomCod);
                String kod5 = tvKOD5.getText().toString();
                db.MinusQty(kod5);

                myNom.requery();
                String ToolBarContr = db.GetToolbarContr();
                String OrderSum = db.getOrderSum();
                if (OrderSum.length() > 0) {
                    toolbar.setSubtitle(ToolBarContr + OrderSum + " руб.");
                } else {
                    toolbar.setSubtitle(ToolBarContr);
                }
                if (NomenAdapter != null) {
                    NomenAdapter.notifyDataSetChanged();
                }

                if (PreviewZakazAdapter != null) {
                    PreviewZakazAdapter.notifyDataSetChanged();
                }
            } else {
                TextView c = myView.findViewById(R.id.ColNomCod);
                final String ID = c.getText().toString();

                int ost = Integer.parseInt(((TextView) myView.findViewById(R.id.ColNomOst)).getText().toString());

                final TextView c1 = myView.findViewById(R.id.ColNomZakaz);

                LayoutInflater layoutInflater = LayoutInflater.from(glbContext);
                View promptView;

                if (isMultiSelect) {
                    db.UpdateQty(ID, MultiQty, ost);
                    myNom.requery();
                    NomenAdapter.notifyDataSetChanged();
                    c1.setText(String.valueOf(MultiQty));
                    String ToolBarContr = db.GetToolbarContr();
                    String OrderSum = db.getOrderSum();
                    toolbar.setSubtitle(ToolBarContr + OrderSum);
                } else {
                    // Если режим выбора нескольких позиций выключен
                    promptView = layoutInflater.inflate(R.layout.change_qty, null);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(frContext);
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
                    txtGrup.setText(myNom.getString(myNom.getColumnIndex("GRUPPA")));

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
                        TextView tvPrice = view.findViewById(R.id.ColNomPrice);
                        String price = tvPrice.getText().toString();
                        db.putPriceInNomen(ID, price);

                        db.UpdateQty(ID, Integer.parseInt(input.getText().toString()), ost);
                        c1.setText(input.getText());
                        myNom.requery();
                        String ToolBarContr = db.GetToolbarContr();
                        String OrderSum = db.getOrderSum();

                        if (OrderSum.length() > 0) {
                            toolbar.setSubtitle(ToolBarContr + OrderSum + " руб.");
                        } else {
                            toolbar.setSubtitle(ToolBarContr);
                        }
                        if (NomenAdapter != null) {
                            NomenAdapter.notifyDataSetChanged();
                        }
                        if (PreviewZakazAdapter != null) {
                            PreviewZakazAdapter.notifyDataSetChanged();
                        }
                        alertD.dismiss();
                    });
                    input.setOnEditorActionListener((v, actionId, event) -> {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            alertD.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                        }
                        return true;
                    });
                }
            }
        }
    };

    public AdapterView.OnItemLongClickListener GridNomenLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, final View myView, final int position, long arg3) {
            final String Grup;
            final String Sgi;
            final String curSgi;

            TextView txtSgi = getView().findViewById(R.id.ColSgiID);
            curSgi = txtSgi.getText().toString();

            Cursor c = myNom;
            Grup = c.getString(7);
            Sgi = c.getString(8);

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
                        if (!curSgi.equals(Sgi)) {
                            SetSelectedSgi(Sgi, Grup);
                        } else {
                            if (!curSgi.equals("0")) {
                                SetSelectedGrup(Grup);
                            } else {
                                Toast.makeText(CurAc, "Группа, к которой принадлежит данная карточка не прописана СГИ!", Toast.LENGTH_LONG).show();
                            }
                        }
                        LoadNom(Grup, curSgi);
                        return true;
                    default:
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
            String ItemID = myGrups.getString(myGrups.getColumnIndex("CODE"));
            if (!ItemID.equals("0")) {
                SharedPreferences settings = getSharedPreferences("form_order", 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.putString("ColSgiFID", "");
                editor.putString("ColGrupFID", "");
                editor.putString("ColWCID", "");
                editor.putString("ColFocusID", "");

                editor.apply();

                final String curSgi;

                TextView txtSgi = getView().findViewById(R.id.ColSgiID);
                curSgi = txtSgi.getText().toString();

                FormOrderFragment.filter.setImageResource(R.drawable.filter);
                LoadNom(ItemID, curSgi);
            } else {
                nomenList.setAdapter(null);
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };
    public AdapterView.OnItemSelectedListener SelectedSgi = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            String ItemID = mySgi.getString(mySgi.getColumnIndex("CODE"));
            if (!ItemID.equals("0")) {
                SharedPreferences settings = getSharedPreferences("form_order", 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.putString("ColSgiFID", "");
                editor.putString("ColGrupFID", "");
                editor.putString("ColWCID", "");
                editor.putString("ColFocusID", "");

                editor.apply();
                FormOrderFragment.filter.setImageResource(R.drawable.filter);
                LoadGroups(ItemID);
                if (SelectGroup != null) {
                    SetSelectedGrup(SelectGroup);
                }
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };
    public AdapterView.OnItemClickListener OrderDtNomenClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {

            if (ordStatus.equals("Отправлен") || ordStatus.equals("Удален")) {
                Toast.makeText(CurAc, "Данный заказ уже отправлен или удаен и не может быть изменен", Toast.LENGTH_LONG).show();
                return;
            }

            TextView c = myView.findViewById(R.id.ColOrdDtID);

            final TextView c1 = myView.findViewById(R.id.ColOrdDtQty);

            TextView c3 = myView.findViewById(R.id.ColOrdDtCod);

            TextView c4 = myView.findViewById(R.id.ColOrdDtDescr);

            TextView c5 = myView.findViewById(R.id.ColOrdDtPrice);

            TextView c6 = myView.findViewById(R.id.ColOrdDtZakazID);
            final String Zakaz_id = c6.getText().toString();

            LayoutInflater layoutInflater = LayoutInflater.from(CurAc);
            View promptView = layoutInflater.inflate(R.layout.change_qty, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CurAc);
            alertDialogBuilder.setView(promptView);

            final EditText input = promptView.findViewById(R.id.edPPQty);
            final TextView txtCod = promptView.findViewById(R.id.txtNomCode);
            final TextView txtDescr = promptView.findViewById(R.id.txtNomDescr);
            final TextView txtOst = promptView.findViewById(R.id.txtNomOst);

            input.setText(c1.getText());
            txtCod.setText(c3.getText());
            txtDescr.setText(c4.getText());

            String ID = txtCod.getText().toString();
            final String Ost = db.getNomenOst(ID);
            txtOst.setText(Ost);

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
            wmlp.y = 50;

            alertD.show();
            input.requestFocus();
            input.selectAll();
            input.performClick();
            input.setPressed(true);
            input.invalidate();
            InputMethodManager imm = (InputMethodManager) CurAc.getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

            alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                Boolean wantToCloseDialog = false;
                if (Integer.parseInt(input.getText().toString()) <= Integer.parseInt(Ost)) {
                    dbOrders.updateOrderQty(Zakaz_id, ID, Integer.parseInt(input.getText().toString()));
                    c1.setText(input.getText());
                    OrdersDt.requery();
                    wantToCloseDialog = true;
                } else {
                    input.selectAll();
                    input.requestFocus();
                    input.performClick();
                    input.setPressed(true);
                    input.invalidate();
                    Toast.makeText(CurAc, "На остатках всего " + Ost + "шт. Вы заказываете " + input.getText().toString() + " шт.", Toast.LENGTH_LONG).show();
                }
                if (wantToCloseDialog)
                    alertD.dismiss();
            });

            input.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    alertD.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                }
                return true;
            });
        }
    };
    TextView grupID, sgiID;
    android.support.v4.app.Fragment fragment = null;
    android.support.v4.app.FragmentTransaction fragmentTransaction;
    public android.support.v4.app.FragmentManager fragManager;
    public AdapterView.OnItemLongClickListener PreviewNomenLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, final View myView, int position, long arg3) {
            final String Grup;
            final String Sgi;

            grupID = myView.findViewById(R.id.ColNomGRUPID);
            sgiID = myView.findViewById(R.id.ColNomSGIID);

            Cursor c = myNom;
            Grup = c.getString(7);
            Sgi = c.getString(8);

            nomPopupMenu = new PopupMenu(CurAc, myView);
            nomPopupMenu.getMenuInflater().inflate(R.menu.nomen_context_menu, nomPopupMenu.getMenu());
            nomPopupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.goToGroup) {
                    fragment = new FormOrderFragment();
                    fragmentTransaction = fragManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fragment, "frag_order_header");
                    fragmentTransaction.commit();
                    toolbar.setTitle("Формирование заказа");
                    frSgi = Sgi;
                    frGroup = Grup;
                    return true;
                }
                return true;
            });
            nomPopupMenu.show();
            return true;
        }
    };

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

    public View getView() {
        return view;
    }

    public Context getContext() {
        return glbContext;
    }

    public void setContext(Context context) {
        glbContext = context;
    }

    public void LoadSgi() {
        new Thread(() -> {
            mySgi = db.getAllSgi();
            CurAc.runOnUiThread(() -> {
                spSgi = view.findViewById(R.id.SpinSgi);
                android.widget.SimpleCursorAdapter adapter;
                adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.sgi_layout, mySgi, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColSgiID, R.id.ColSgiDescr});
                spSgi.setAdapter(adapter);
                spSgi.post(() -> spSgi.setOnItemSelectedListener(SelectedSgi));
            });
        }).start();
    }

    public void LoadGroups(final String SgiID) {
        new Thread(() -> {
            myGrups = db.getGrupBySgi(SgiID);
            CurAc.runOnUiThread(() -> {
                spGrup = view.findViewById(R.id.SpinGrups);
                android.widget.SimpleCursorAdapter adapter;
                adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.grup_layout, myGrups, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColGrupID, R.id.ColGrupDescr});
                spGrup.setAdapter(adapter);
                spGrup.setOnItemSelectedListener(SelectedGroup);
            });
        }).start();
    }

    public void LoadFiltersWC(View vw) {
        curWC = dbApp.getWCs();
        spWC = vw.findViewById(R.id.spinWC);
        android.widget.SimpleCursorAdapter adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.wc_layout, curWC, new String[]{"_id", "DEMP"}, new int[]{R.id.ColWCID, R.id.ColWCDescr});
        spWC.setAdapter(adapter);
    }

    public void LoadFiltersFocus(View vw) {
        curFocus = db.getFocuses();
        spFocus = vw.findViewById(R.id.spinFocus);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.focus_layout, curFocus, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColFocusID, R.id.ColFocusDescr});
        spFocus.setAdapter(adapter);
    }

    private MyCursorAdapter getNomenAdapter(Cursor cursor) {
        return new MyCursorAdapter(glbContext, R.layout.nomen_layout, cursor, new String[]{"_id", "KOD5", "DESCR", "OST", "ZAKAZ", "GRUPPA", "SGI", "FOTO", "GOFRA", "MP", "PRICE"}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomPhoto, R.id.ColNomVkorob, R.id.ColNomMP, R.id.ColNomPrice}, 0);
    }

    public void LoadNom(final String GrupID, final String SgiID) {
        CurSGI = SgiID;
        CurGroup = GrupID;
        CurSearchName = "";

        FormOrderFragment.isSorted = false;
        nomenList.setSelection(0);
        if (!FormOrderFragment.isSorted) {
            FormOrderFragment.mainMenu.findItem(R.id.NomenSort).setIcon(R.drawable.to_end);
        }

        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                myNom = db.getNomByGroup(GrupID, SgiID);
                CurAc.runOnUiThread(() -> {
                    nomenList.setAdapter(null);
                    NomenAdapter = getNomenAdapter(myNom);
                    nomenList.post(() -> nomenList.setAdapter(NomenAdapter));
                    nomenList.setOnItemClickListener(GridNomenClick);
                    nomenList.setOnItemLongClickListener(GridNomenLongClick);
                });
            }
        }).start();
    }

    public void LoadFilterNomen(
            final String SgiID, final String GrupID,
            final String WCID, final String FocusID) {
        CurSGI = SgiID;
        CurGroup = GrupID;
        CurWCID = WCID;
        CurFocusID = FocusID;
        CurSearchName = "";

        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                myNom = db.getNextNomen(
                        SgiID, GrupID,
                        WCID, FocusID, CurSearchName, 0);
                CurAc.runOnUiThread(() -> {
                    nomenList.setAdapter(null);
                    NomenAdapter = getNomenAdapter(myNom);
                    nomenList.post(() -> nomenList.setAdapter(NomenAdapter));
                    nomenList.setOnItemClickListener(GridNomenClick);
                    nomenList.setOnItemLongClickListener(GridNomenLongClick);
                });
            }
        }).start();
    }

    public void LoadNextNomen(
            final String SgiID, final String GrupID, final String WCID,
            final String FocusID, final String search, final int positionSQL) {
        CurSGI = SgiID;
        CurGroup = GrupID;
        CurWCID = WCID;
        CurFocusID = FocusID;
        CurSearchName = search;

        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                myNom = db.getNextNomen(
                        SgiID, GrupID,
                        WCID, FocusID, search, positionSQL);
                CurAc.runOnUiThread(() -> {
                    nomenList.setAdapter(null);
                    NomenAdapter = getNomenAdapter(myNom);
                    nomenList.post(() -> nomenList.setAdapter(NomenAdapter));
                    nomenList.setOnItemClickListener(GridNomenClick);
                    nomenList.setOnItemLongClickListener(GridNomenLongClick);

                    nomenList.setSelection(positionSQL - 1);
                });
                isNewLoaded = false;
            }
        }).start();
    }

    public void SearchNom(final String SearchStr) {
        CurSGI = "0";
        CurGroup = "0";
        CurSearchName = SearchStr;

        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                myNom = db.getSearchNom(SearchStr);
                CurAc.runOnUiThread(() -> {
                    nomenList.setAdapter(null);
                    NomenAdapter = getNomenAdapter(myNom);
                    nomenList.post(() -> nomenList.setAdapter(NomenAdapter));
                    nomenList.setOnItemClickListener(GridNomenClick);
                    nomenList.setOnItemLongClickListener(GridNomenLongClick);
                });
            }
        }).start();
    }

    public void SearchNomInGroup(final String SearchStr, final String Group) {
        SQLiteDatabase sqLiteDatabase = db.getReadableDatabase();
        Cursor c = sqLiteDatabase.rawQuery("SELECT SGI FROM GRUPS WHERE CODE='" + Group + "'", null);
        c.moveToNext();

        if (c.getCount() != 0) CurSGI = c.getString(c.getColumnIndex("SGI"));
        CurGroup = Group;
        CurSearchName = SearchStr;

        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                myNom = db.getSearchNomInGroup(SearchStr, Group);
                CurAc.runOnUiThread(() -> {
                    nomenList.setAdapter(null);
                    NomenAdapter = getNomenAdapter(myNom);
                    nomenList.post(() -> nomenList.setAdapter(NomenAdapter));
                    nomenList.setOnItemClickListener(GridNomenClick);
                    nomenList.setOnItemLongClickListener(GridNomenLongClick);
                    c.close();
                });
            }
        }).start();
    }

    public void SetSelectedSgi(String SgiID, String Grup) {
        for (int i = 0; i < spSgi.getCount(); i++) {
            Cursor value = (Cursor) spSgi.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("CODE"));
            if (SgiID.equals(id)) {
                spSgi.setSelection(i);
                SelectGroup = Grup;
                break;
            }
        }
    }

    public void SetSelectedGrup(String Grup) {
        for (int i = 0; i < spGrup.getCount(); i++) {
            Cursor value = (Cursor) spGrup.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("CODE"));
            if (Grup.equals(id)) {
                spGrup.setSelection(i);
                break;
            }
        }
    }

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
        if (activeNetworkInfo == null) {
            Toast.makeText(glbContext, "Нет доступной сотовой сети", Toast.LENGTH_LONG).show();
            return false;
        }
        if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return activeNetworkInfo.isConnectedOrConnecting();
        } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            switch (activeNetworkInfo.getSubtype()) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                /*
                 * Above API level 7, make sure to set android:targetSdkVersion
                 * to appropriate level to use these
                 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public void DownloadPhoto(final String FileName) {
        new Thread(() -> {
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
                GridView gdNomen = view.findViewById(R.id.listContrs);
                myNom.requery();
                NomenAdapter.notifyDataSetChanged();
                gdNomen.invalidateViews();
            });
        }).start();
    }

    public void ShowNomenPhoto(final String PhotoFileName) {
        alertPhoto = null;
        final String photoDir = getPhotoDir();

        File imgFile = new File(photoDir + "/" + PhotoFileName);
        final File imgFile2 = new File(photoDir + "/" + FilenameUtils.removeExtension(PhotoFileName) + "_2.jpg");
        checkPhotoInDB(PhotoFileName);

        final String imFileName = PhotoFileName;
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

        if (imgFile.exists()) {
            imageView.setImage(ImageSource.uri(photoDir + "/" + PhotoFileName));
            imageView.setTag("Фото 1");
            imageView.setOnClickListener(v -> {
                checkPhotoInDB(PhotoFileName);
                String LoadingFile = imFileName;
                if (imageView.getTag().toString().equals("Фото 1")) {
                    if (imgFile2.exists() && imgFile2.length() != 0) {
                        imageView.setTag("Фото 2");
                        LoadingFile = FilenameUtils.removeExtension(imFileName) + "_2.jpg";
                        checkPhotoInDB(LoadingFile);
                        alertPhoto.setTitle(LoadingFile);
                    } else {
                        if (!myNom.getString(10).equals("")) {
                            isSecondPhoto = true;
                            if (isNetworkAvailable()) {
                                DownloadPhoto(FilenameUtils.removeExtension(imFileName) + "_2.jpg");
                                if (imgFile2.length() != 0) {
                                    LoadingFile = FilenameUtils.removeExtension(imFileName) + "_2.jpg";
                                    checkPhotoInDB(LoadingFile);
                                    imageView.setTag("Фото 2");
                                    alertPhoto.setTitle(LoadingFile);
                                }
                            }
                        }
                    }
                } else {
                    LoadingFile = imFileName;
                    imageView.setTag("Фото 1");
                }
                imageView.setImage(ImageSource.uri(photoDir + "/" + LoadingFile));
            });
        } else {
            DownloadPhoto(PhotoFileName);
            if (imgFile.length() != 0) {
                imageView.setImage(ImageSource.uri(photoDir + "/" + PhotoFileName));
                imageView.setTag("Фото 1");
            } else {
                Toast.makeText(glbContext, "Файл с именем " + imFileName + " на сервере не найден", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void PreviewZakaz() {
        nomenList.setAdapter(null);
        myNom = db.getOrderNom();
        PreviewZakazAdapter = new MyCursorAdapter(glbContext, R.layout.nomen_layout_preview, myNom, new String[]{"_id", "KOD5", "DESCR", "OST", "PRICE", "ZAKAZ", "GRUPPA", "SGI", "GOFRA", "MP"}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomPrice, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomVkorob, R.id.ColNomMP}, 0);
        nomenList.setAdapter(PreviewZakazAdapter);
        nomenList.setOnItemClickListener(GridNomenClick);
        nomenList.setOnItemLongClickListener(PreviewNomenLongClick);
    }

    public Boolean CheckTPLock() {
        SharedPreferences settings;
        settings = PreferenceManager.getDefaultSharedPreferences(frContext);
        return settings.getBoolean("TP_LOCK", false);
    }

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

    public void LoadContrList() {
        spinContr.setAdapter(null);
        Contr = db.getContrList();
        ContrsAdapter adapter;
        adapter = new ContrsAdapter(CurAc, R.layout.contr_layout, Contr, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
        spinContr.setAdapter(adapter);
        spinContr.setOnItemSelectedListener(SelectedContr);
    }

    public void LoadFilteredContrList(String FindStr) {
        spinContr.setAdapter(null);
        Contr = db.getContrFilterList(FindStr);
        ContrsAdapter adapter;
        adapter = new ContrsAdapter(CurAc, R.layout.contr_layout, Contr, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
        spinContr.setAdapter(adapter);
        spinContr.setOnItemSelectedListener(SelectedContr);
    }

    public void LoadContrAddr(String ContID) {
        Addr = db.getContrAddress(ContID);
        AddrsAdapter adapter;
        adapter = new AddrsAdapter(CurAc, R.layout.addr_layout, Addr, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrAddrID, R.id.ColContrAddrDescr}, 0);
        spinAddr.setAdapter(adapter);
        spinAddr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        String AddrID = db.GetContrAddr();
        if (!AddrID.equals("0")) {
            SetSelectedAddr(AddrID);
        }
    }

    public void SetSelectedAddr(String AddrID) {
        for (int i = 0; i < spinAddr.getCount(); i++) {
            Cursor value = (Cursor) spinAddr.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("CODE"));
            if (AddrID.equals(id)) {
                spinAddr.setSelection(i);
                break;
            }
        }
    }

    public String FormDBFForZakaz(int ID) throws DBFException {
        System.out.println(ID);
        Cursor c;

        String TP, CONTR, ADDR, DOCNO, COMMENT, NOMEN;
        java.util.Date DELIVERY, DOCDATE;
        double QTY, PRICE;

        c = dbOrders.getReadableDatabase().rawQuery("SELECT TP, CONTR, ADDR, ZAKAZY.DOCID as DOCID, ZAKAZY.DOC_DATE as DOC_DATE, ZAKAZY.DELIVERY_DATE as DEL_DATE, ZAKAZY.COMMENT as COMMENT, ZAKAZY_DT.NOMEN as NOMEN, ZAKAZY_DT.DESCR as DES, ZAKAZY_DT.QTY as QTY, ZAKAZY_DT.PRICE as PRICE FROM ZAKAZY JOIN ZAKAZY_DT ON ZAKAZY.DOCID = ZAKAZY_DT.ZAKAZ_ID WHERE ZAKAZY.ROWID='" + ID + "'", null);
        if (c.getCount() == 0) {
            Toast.makeText(CurAc, "В таблице заказов нет записей для отправки", Toast.LENGTH_LONG).show();
            return "";
        }

        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("ddMMyyyy_HHmmss");
        String curdate = df.format(Calendar.getInstance().getTimeInMillis()) + Calendar.getInstance().get(Calendar.MILLISECOND);

        Cursor cForTpId = dbOrders.getReadableDatabase().rawQuery("SELECT TP FROM ZAKAZY WHERE rowid='" + ID + "'", null);

        cForTpId.moveToNext();
        String tpID = cForTpId.getString(cForTpId.getColumnIndex("TP"));

        String FileName = CurAc.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + tpID + "_" + curdate + ".dbf";
        DBF_FileName = tpID + "_" + curdate + ".dbf";

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
        fields[index].setDataType(DBFField.FIELD_TYPE_N);
        fields[index].setFieldLength(15);
        fields[index].setDecimalCount(2);
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
                PRICE = Double.parseDouble(c.getString(c.getColumnIndex("PRICE")).replace(",", "."));

                Object[] rowData = new Object[10];
                rowData[0] = TP;
                rowData[1] = CONTR;
                rowData[2] = ADDR;
                rowData[3] = DOCNO;
                rowData[4] = DOCDATE;
                rowData[5] = DELIVERY;
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
        DBF_FIleForSend = FileName;
        if (isNetworkAvailable()) {
            Integer[] data = new Integer[]{ID};
            new UploadDBFFile().execute(data);
        } else {
            Toast.makeText(CurAc, "Нет доступного интернет соединения", Toast.LENGTH_LONG).show();
        }
        return DBF_FileName;
    }

    private void putCheckBox(Cursor c) {
        GlobalVars.allOrders.clear();
        layout.removeAllViews();

        if (c.getCount() == 0) return;

        int id;
        String status;

        c.moveToFirst();
        do {
            id = c.getInt(0);
            status = c.getString(5);

            CheckBox checkBox = new CheckBox(layout.getContext());
            float height = getResources().getDimension(R.dimen.heightOfOrdersItem);
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) height));
            layout.addView(checkBox);

            if (status.equals("Сохранен")) {
                checkBox.setChecked(true);
            }

            GlobalVars.allOrders.add(new CheckBoxData(id, checkBox, status));
        } while (c.moveToNext());
    }

    public void LoadOrders() {
        _updateOrdersStatusFromDB();

        gdOrders.setAdapter(null);
        Orders = dbOrders.getZakazy();
        if (Orders != null)
            putCheckBox(Orders);

        OrdersAdapter = new JournalAdapter(CurAc, R.layout.orders_item, Orders, new String[]{"DOCID", "STATUS", "DOC_DATE", "DELIVERY", "CONTR", "ADDR", "SUM"}, new int[]{R.id.ColOrdDocNo, R.id.ColOrdStatus, R.id.ColOrdDocDate, R.id.ColOrdDeliveryDate, R.id.ColOrdContr, R.id.ColOrdAddr, R.id.ColOrdSum}, 0);
        gdOrders.setAdapter(OrdersAdapter);
    }

    public void LoadOrdersDetails(String ZakazID) {
        _doUpdateQTYByVycherk(ZakazID);

        orderdtList.setAdapter(null);
        OrdersDt = dbOrders.getZakazDetails(ZakazID);
        OrdersDtAdapter = new JournalDetailsAdapter(CurAc, R.layout.orderdt_item, OrdersDt, new String[]{"ZAKAZ_ID", "NOMEN", "DESCR", "QTY", "PRICE", "SUM"}, new int[]{R.id.ColOrdDtZakazID, R.id.ColOrdDtCod, R.id.ColOrdDtDescr, R.id.ColOrdDtQty, R.id.ColOrdDtPrice, R.id.ColOrdDtSum}, 0);
        orderdtList.setAdapter(OrdersDtAdapter);
        orderdtList.setOnItemClickListener(OrderDtNomenClick);
    }

    public String ReadLastUpdate() {
        String timeUpdate = db.getLastUpdateTime();
        return timeUpdate != null ? timeUpdate : "";
    }

    public void LoadDebet(final String TP_ID) {
        new Thread(() -> {
            curDebet = db.getDebet(TP_ID);
            CurAc.runOnUiThread(() -> {
                debetList.setAdapter(null);
                if (curDebet == null) {
                    Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                }
                DebetAdapter adapter;
                adapter = new DebetAdapter(CurAc, R.layout.debet_layout, curDebet, new String[]{"DESCR", "STATUS", "KREDIT", "SALDO", "A7", "A14", "A21", "A28", "A35", "A42", "A49", "A56", "A63", "A64", "OTG30", "OPL30", "KOB", "FIRMA", "CRT_DATE"}, new int[]{R.id.ColDebetContr, R.id.ColDebetStatus, R.id.ColDebetCredit, R.id.ColDebetDolg, R.id.ColDebetA7, R.id.ColDebetA14, R.id.ColDebetA21, R.id.ColDebetA28, R.id.ColDebetA35, R.id.ColDebetA42, R.id.ColDebetA49, R.id.ColDebetA56, R.id.ColDebetA63, R.id.ColDebetA64, R.id.ColDebetOTG30, R.id.ColDebetOPL30, R.id.ColDebetKOB, R.id.ColDebetFirma, R.id.ColDebetDogovor}, 0);
                debetList.setAdapter(adapter);
            });
        }).start();
    }

    public void SearchDebet(final String Contr) {
        new Thread(() -> {
            curDebet = db.SearchInDebet(Contr);
            CurAc.runOnUiThread(() -> {
                debetList.setAdapter(null);
                DebetAdapter adapter;
                adapter = new DebetAdapter(CurAc, R.layout.debet_layout, curDebet, new String[]{"DESCR", "STATUS", "KREDIT", "SALDO", "A7", "A14", "A21", "A28", "A35", "A42", "A49", "A56", "A63", "A64", "OTG30", "OPL30", "KOB", "FIRMA", "CRT_DATE"}, new int[]{R.id.ColDebetContr, R.id.ColDebetStatus, R.id.ColDebetCredit, R.id.ColDebetDolg, R.id.ColDebetA7, R.id.ColDebetA14, R.id.ColDebetA21, R.id.ColDebetA28, R.id.ColDebetA35, R.id.ColDebetA42, R.id.ColDebetA49, R.id.ColDebetA56, R.id.ColDebetA63, R.id.ColDebetA64, R.id.ColDebetOTG30, R.id.ColDebetOPL30, R.id.ColDebetKOB, R.id.ColDebetFirma, R.id.ColDebetDogovor}, 0);
                debetList.setAdapter(adapter);
            });
        }).start();
    }

    public void LoadTpListDeb() {
        spTP.setAdapter(null);
        curDebetTp = db.getTpList();
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(CurAc, R.layout.tp_layout, curDebetTp, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColTPID, R.id.ColTPDescr});
        spTP.setAdapter(adapter);

        spTP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void UpdateNomenRange(int BeginRange, int EndRange, int Qty) {
        String sql_update = "UPDATE Nomen SET ZAKAZ=" + Qty + " WHERE ROWID=?";
        SQLiteStatement stmt = db.getWritableDatabase().compileStatement(sql_update);
        db.getWritableDatabase().beginTransaction();
        int tmpVal;

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
        myNom.requery();
        NomenAdapter.notifyDataSetChanged();
        String ToolBarContr = db.GetToolbarContr();
        String OrderSum = db.getOrderSum();
        toolbar.setSubtitle(ToolBarContr + OrderSum);
    }

    public void setDiscountIcon(Menu menu, int MenuItem, Boolean vis) {
        Drawable drawable = menu.getItem(MenuItem).getIcon();
        drawable.mutate();
        if (vis) {
            drawable.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
        } else {
            drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
        isDiscount = vis;
    }

    public void CalculatePercentSale(final Menu menu, final Integer WhichView) {
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

                    if (WhichView == 0) {
                        if (Discount == 0) {
                            isDiscount = false;
                            setDiscountIcon(menu, 3, false);
                        } else {
                            isDiscount = true;
                            setDiscountIcon(menu, 3, true);
                        }

                        if (NomenAdapter != null) {
                            myNom.requery();
                            NomenAdapter.notifyDataSetChanged();
                        }
                    }

                    if (WhichView == 1) {
                        if (Discount == 0) {
                            isDiscount = false;
                            setDiscountIcon(menu, 1, false);
                        } else {
                            isDiscount = true;
                            setDiscountIcon(menu, 1, true);
                        }

                        if (PreviewZakazAdapter != null) {
                            myNom.requery();
                            PreviewZakazAdapter.notifyDataSetChanged();
                        }
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
        String tmpName = isDownloaded;
        String Sql;

        if (tmpName.equals("_2")) {
            Sql = "SELECT PD From Nomen WHERE KOD5='" + isDownloaded.replace(tmpName, "") + "'";
        } else {
            Sql = "SELECT PD From Nomen WHERE KOD5='" + isDownloaded + "'";
        }
        cur = db.getWritableDatabase().rawQuery(Sql, null);

        if (cur.moveToFirst()) {
            if (cur.getInt(0) == 0) {
                if (tmpName.equals("_2")) {
                    db.getWritableDatabase().execSQL("UPDATE Nomen SET PD=1 WHERE KOD5='" + isDownloaded.replace(tmpName, "") + "'");
                } else {
                    db.getWritableDatabase().execSQL("UPDATE Nomen SET PD=1 WHERE KOD5='" + isDownloaded + "'");
                }
            }
        }
        cur.close();
        GridView gdNomen = view.findViewById(R.id.listContrs);
        myNom.requery();
        NomenAdapter.notifyDataSetChanged();
        gdNomen.invalidateViews();
    }

    public static class AddrsAdapter extends SimpleCursorAdapter {
        public AddrsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Cursor cursor = getCursor();
            TextView tvDescr = view.findViewById(R.id.ColContrAddrDescr);

            String text = cursor.getString(2);
            tvDescr.setText(text);

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Cursor cursor = getCursor();
            TextView tvDescr = view.findViewById(R.id.ColContrAddrDescr);

            if (position % 2 == 0) {
                tvDescr.setBackgroundColor(Color.rgb(201, 235, 255));
            } else {
                tvDescr.setBackgroundColor(Color.rgb(255, 255, 255));
            }
            tvDescr.setText(cursor.getString(2));

            return view;
        }
    }

    public class MyCursorAdapter extends SimpleCursorAdapter {
        public MyCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            int resID;

            final Cursor cursor = getCursor();

            TextView tvDescr = view.findViewById(R.id.ColNomDescr);
            TextView tvPrice = view.findViewById(R.id.ColNomPrice);
            TextView tvPosition = view.findViewById(R.id.ColNomPosition);
            TextView tvMP = view.findViewById(R.id.ColNomMP);
            TextView tvGofra = view.findViewById(R.id.ColNomVkorob);
            TextView tvOst = view.findViewById(R.id.ColNomOst);
            TextView tvCod = view.findViewById(R.id.ColNomCod);

            final TextView tvPhoto = view.findViewById(R.id.ColNomPhoto);
            final Button btPlus = view.findViewById(R.id.btPlus);
            final Button btMinus = view.findViewById(R.id.btMinus);

            tvDescr.setTextSize(SettingFragment.nomenDescriptionFontSize);

            if (tvPhoto != null) {
                tvPhoto.setOnClickListener(v -> {
                    ((GridView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
                });
            }

            btPlus.setOnClickListener(v -> {
                ((GridView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
            });

            btMinus.setOnClickListener(v -> {
                ((GridView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
            });

            String price = String.format("%.2f", DBHelper.pricesMap.get(cursor.getString(cursor.getColumnIndex("KOD5"))));

            if (!DBHelper.pricesMap.containsKey(cursor.getString(cursor.getColumnIndex("KOD5"))))
                price = "0";
            else if (price.equals("" + DBHelper.pricesMap.get(cursor.getString(cursor.getColumnIndex("KOD5")))))
                price = "" + DBHelper.pricesMap.get(cursor.getString(cursor.getColumnIndex("KOD5")));

            if (price.equals("0")) {
                String cPrice = cursor.getString(cursor.getColumnIndex("PRICE"));
                if (!cPrice.equals("0") && !cPrice.equals("null")) {
                    price = cPrice;
                }
            }

            tvPrice.setText(price);

            if (tvPhoto != null && cursor.getString(cursor.getColumnIndex("FOTO")) != null) {
                if (cursor.getInt(cursor.getColumnIndex("PD")) == 1) {
                    resID = glbContext.getResources().getIdentifier("photo_green", "drawable", glbContext.getPackageName());
                } else {
                    resID = glbContext.getResources().getIdentifier("photo2", "drawable", glbContext.getPackageName());
                }

                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append(" ").append(" ");
                builder.setSpan(new ImageSpan(glbContext, resID), builder.length() - 1, builder.length(), 0);
                builder.append(" ");
                tvPhoto.setText(builder);
            }

            // Начало расчёта разниы дней
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

            long daysSubstraction = (long) Math.abs((PostDataTime - CurrentTime) / (1000d * 60 * 60 * 24));
            // Конец расчёта разниы дней

            if (position % 2 == 0) {
                view.setBackgroundColor(Color.rgb(201, 235, 255));
            } else {
                view.setBackgroundColor(Color.rgb(255, 255, 255));
            }

            if (daysSubstraction <= 4) {
                int color;
                if (daysSubstraction <= 2)
                    color = getResources().getColor(R.color.postDataColorRed);
                else
                    color = getResources().getColor(R.color.postDataColorGreen);

                tvDescr.setTextColor(color);
                tvPrice.setTextColor(color);
                tvPosition.setTextColor(color);
                tvMP.setTextColor(color);
                tvGofra.setTextColor(color);
                tvOst.setTextColor(color);
                tvCod.setTextColor(color);
            }

            if (cursor.getInt(cursor.getColumnIndex("ACTION")) == 1) {
                tvDescr.setTypeface(tvDescr.getTypeface(), Typeface.BOLD_ITALIC);
                tvPrice.setTypeface(tvPrice.getTypeface(), Typeface.BOLD_ITALIC);
                tvPosition.setTypeface(tvPosition.getTypeface(), Typeface.BOLD_ITALIC);
                tvMP.setTypeface(tvMP.getTypeface(), Typeface.BOLD_ITALIC);
                tvGofra.setTypeface(tvGofra.getTypeface(), Typeface.BOLD_ITALIC);
                tvOst.setTypeface(tvOst.getTypeface(), Typeface.BOLD_ITALIC);
                tvCod.setTypeface(tvCod.getTypeface(), Typeface.BOLD_ITALIC);
            }

            tvPosition.setText(String.valueOf(position + 1));
            if (position == cursor.getCount() - 1 && CurGroup.equals("0") && !isNewLoaded && cursor.getCount() >= DBHelper.limit) {
                isNewLoaded = true;
                LoadNextNomen(CurSGI, CurGroup, CurWCID, CurFocusID, CurSearchName, cursor.getCount());
                return view;
            }

            return view;
        }
    }

    public static class CheckBoxData {
        public int id;
        public CheckBox checkBox;
        public String status;

        public CheckBoxData(int id, CheckBox checkBox, String status) {
            this.id = id;
            this.checkBox = checkBox;
            this.status = status;
        }
    }

    public class JournalAdapter extends SimpleCursorAdapter {
        public JournalAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView tvSum = view.findViewById(R.id.ColOrdSum);
            _updateTextFormatTo2DecAfterPoint(tvSum);

            if (position % 2 == 0) {
                view.setBackgroundColor(Color.rgb(201, 235, 255));
            } else {
                view.setBackgroundColor(Color.rgb(255, 255, 255));
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
            _updateTextFormatTo2DecAfterPoint(tvSum);

            if (position % 2 == 0) {
                view.setBackgroundColor(Color.rgb(201, 235, 255));
            } else {
                view.setBackgroundColor(Color.rgb(255, 255, 255));
            }

            if (cursor.getInt(cursor.getColumnIndex("IS_OUTED")) == 1) {
                tvQty.setText(cursor.getInt(cursor.getColumnIndex("QTY")) + "(-" + cursor.getInt(cursor.getColumnIndex("OUT_QTY")) + ")");
            }

            return view;
        }
    }

    public static class TPAdapter extends SimpleCursorAdapter {
        public TPAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flag) {
            super(context, layout, c, from, to, flag);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            TextView tvDescr = view.findViewById(R.id.ColTPDescr);
            if (position % 2 == 0) {
                tvDescr.setBackgroundColor(Color.rgb(201, 235, 255));
            } else {
                tvDescr.setBackgroundColor(Color.rgb(255, 255, 255));
            }

            return view;
        }
    }

    public static class ContrsAdapter extends SimpleCursorAdapter {
        public ContrsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Cursor cursor = getCursor();
            String resDescr = cursor.getString(cursor.getColumnIndex("DESCR")) + "\t\t" + cursor.getString(cursor.getColumnIndex("STATUS"));

            TextView tvDescr = view.findViewById(R.id.ColContrDescr);
            if (position % 2 == 0) {
                tvDescr.setBackgroundColor(Color.rgb(201, 235, 255));
            } else {
                tvDescr.setBackgroundColor(Color.rgb(255, 255, 255));
            }

            tvDescr.setText(resDescr);
            return view;
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class UploadDBFFile extends AsyncTask<Integer[], Integer, Integer[]> {
        boolean ret_completed;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(CurAc);
            progressDialog.setTitle("Загрузка файла заказов");
            progressDialog.setMessage("Идет загрузка файлов заказа. Пожалуйста подождите...");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(false);
            progressDialog.show();
        }

        protected Integer[] doInBackground(Integer[]... urls) {
            SharedPreferences settings;
            settings = CurAc.getSharedPreferences("apk_version", 0);

            String server = settings.getString("FtpPhotoSrv", getResources().getString(R.string.ftp_server));
            String username = settings.getString("FtpPhotoUser", getResources().getString(R.string.ftp_user));
            String password = settings.getString("FtpPhotoPass", getResources().getString(R.string.ftp_pass));

            FTPClient ftpClient = new FTPClient();

            try {
                ftpClient.connect(server, 21);
                ftpClient.login(username, password);
                ftpClient.enterLocalPassiveMode();
                ftpClient.changeWorkingDirectory("EXCHANGE/IN/MARS/");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                InputStream inputStream;

                File secondLocalFile = new File(DBF_FIleForSend);
                String secondRemoteFile = DBF_FileName;
                inputStream = new FileInputStream(secondLocalFile);

                OutputStream outputStream = ftpClient.storeFileStream(secondRemoteFile);
                if (outputStream == null)
                    return null;

                byte[] bytesIn = new byte[65536];
                int read;

                try {
                    while ((read = inputStream.read(bytesIn)) != -1) {
                        outputStream.write(bytesIn, 0, read);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                inputStream.close();
                outputStream.close();

                boolean completed = ftpClient.completePendingCommand();
                if (completed) {
                    ret_completed = true;
                }

                return urls[0];
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressDialog.setProgress(progress[0]);
        }

        protected void onPostExecute(Integer[] result) {
            progressDialog.dismiss();
            if (ret_completed) {
                for (Integer id : result) {
                    dbOrders.setZakazStatus("Отправлен", id);
                }

                Toast.makeText(CurAc, "Заказы успешно отправлены", Toast.LENGTH_LONG).show();
            }

            LoadOrders();
        }
    }

    public class DebetAdapter extends SimpleCursorAdapter {

        public DebetAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
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

            if (position % 2 == 0) {
                view.setBackgroundResource(R.drawable.debet_item_border_blue);
            } else {
                view.setBackgroundResource(R.drawable.debet_item_border_white);
            }

            for (TextView v : tvArray) {
                v.setTextColor(Color.rgb(0, 0, 0));
                _updateTextFormatTo2DecAfterPoint(v);
            }

            return view;
        }
    }

    @SuppressLint("DefaultLocale")
    private void _updateTextFormatTo2DecAfterPoint(TextView v) {
        try {
            v.setText(String.format("%.2f", Float.parseFloat(v.getText().toString())));
        } catch (Exception ignored) {
        }
    }

    public void rewritePriceToMainDB(String DOCID) {
        Cursor orderPrices;
        orderPrices = dbOrders.getReadableDatabase().rawQuery("SELECT PRICE, NOMEN FROM ZAKAZY_DT WHERE ZAKAZ_ID = '" + DOCID + "'", null);

        while (orderPrices.moveToNext()) {
            DBHelper.pricesMap.remove(orderPrices.getString(1));
            DBHelper.pricesMap.put(orderPrices.getString(1), Float.parseFloat(orderPrices.getString(0).replace(",", ".")));
        }

        orderPrices.close();
    }

    private void _updateOrdersStatusFromDB() {
        SQLiteDatabase dbApp = db.getReadableDatabase();
        SQLiteDatabase dbOrd = dbOrders.getWritableDatabase();
        Cursor statusInApp = dbOrd.rawQuery("SELECT DOCID FROM ZAKAZY", null);
        Cursor statusInDB = null;
        while (statusInApp.moveToNext()) {
            String DOCID = statusInApp.getString(statusInApp.getColumnIndex("DOCID"));
            statusInDB = dbApp.rawQuery("SELECT STATUS FROM STATUS WHERE DOCID = '" + DOCID + "'", null);
            if (statusInDB.getCount() != 0) {
                statusInDB.moveToNext();
                String Status = statusInDB.getString(statusInDB.getColumnIndex("STATUS"));
                dbOrd.execSQL("UPDATE ZAKAZY SET STATUS = '" + Status + "' WHERE DOCID='" + DOCID + "'");
            }
        }

        statusInApp.close();
        if (statusInDB != null)
            statusInDB.close();
    }

    private void _doUpdateQTYByVycherk(String DocID) {
        SQLiteDatabase dbApp = dbOrders.getWritableDatabase();
        SQLiteDatabase dbVy = db.getReadableDatabase();

        Cursor newQty = dbVy.rawQuery("SELECT NOMEN, KOL FROM VYCHERK WHERE DOCID ='" + DocID + "'", null);
        Cursor c = null;

        while (newQty.moveToNext()) {
            dbApp.execSQL("UPDATE ZAKAZY_DT SET IS_OUTED = 1, OUT_QTY = " + newQty.getInt(newQty.getColumnIndex("KOL")) + " WHERE ZAKAZ_ID = '" + DocID + "' AND NOMEN = '" + newQty.getString(0) + "'");
//            c = dbApp.rawQuery("SELECT QTY FROM ZAKAZY_DT WHERE ZAKAZ_ID = '" + DocID + "' AND NOMEN = '" + newQty.getString(0) + "'", null);
//            c.moveToNext();

//            String QTY = c.getString(0);
//            int qty;
//            if (!QTY.contains("(")) {
//                qty = Integer.parseInt(QTY);
//            } else {
//                qty = Integer.parseInt(QTY.split(" \\(")[0]);
//            }
//            dbApp.execSQL("UPDATE ZAKAZY_DT SET QTY = '" + qty + " (" + (qty - newQty.getInt(1)) + ")" + "' WHERE ZAKAZ_ID = '" + DocID + "' AND NOMEN = '" + newQty.getString(0) + "'");
        }

        newQty.close();
        if (c != null)
            c.close();
    }
}
