package com.amber.armtp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;

/**
 * Created by filimonov on 22-08-2016.
 * Updated by domster704 on 27.09.2021
 */
public class GlobalVars extends Application {

    public static ArrayList<CheckBoxData> allOrders = new ArrayList<>();

    public Context glbContext;
    public Context frContext;
    public Activity CurAc;
    public Cursor myNom, mySgi, myGrups, Orders, OrdersDt;
    public Cursor curWC, curFSgi, curFGroup, curFocus;

    public MyCursorAdapter NomenAdapter, PreviewZakazAdapter;
    public JournalAdapter OrdersAdapter;
    public JournalDetailsAdapter OrdersDtAdapter;

    public View view;
    public DBHepler db;
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
    public Spinner spWC, spFSgi, spFGroup, spFocus;
    public File appDBFolder = new File(GetSDCardpath() + "ARMTP_DB");
    public File appPhotoFolder = new File(GetSDCardpath() + "ARM_PHOTO");
    public File appUpdatesFolder = new File(GetSDCardpath() + "ARM_UPDATES");
    public String AsyncFileName;
    public SubsamplingScaleImageView imageView;
    public AlertDialog alertPhoto = null;
    public PopupMenu nomPopupMenu;
    public boolean isSecondPhoto = false;
    public int UpdateWorking = 0;
    public String OrderID = "";
    public String DebetContr;
    public Cursor TP, CenTypes;
    public String CurrentTp, CurrentCenType, CurrentDebTP;
    public Cursor Contr;
    public Cursor Addr;
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
    public Spinner spinContr, spinAddr, TPList, spinCenTypes;
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
    public Button btFilter, btClearFilter;
    public GridView debetList;
    public Spinner spContrDeb, spTP;
    public Cursor curDebet, curDebetContr, curDebetTp;
    public SQLiteDatabase SmsDB = null;
    public int BeginPos = 0, EndPos = 0;

    public AdapterView.OnItemClickListener GridNomenClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {
            long viewId = myView.getId();

            if (viewId == R.id.ColNomPhoto) {
                if (((TextView) myView).getText().equals(""))
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
                long ID = myAdapter.getItemIdAtPosition(position);
                db.PlusQty(ID);
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
                long ID = myAdapter.getItemIdAtPosition(position);
                db.MinusQty(ID);
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
                TextView c = myView.findViewById(R.id.ColNomID);
                final String ID = c.getText().toString();

                final TextView c1 = myView.findViewById(R.id.ColNomZakaz);

                LayoutInflater layoutInflater = LayoutInflater.from(glbContext);
                View promptView;

                if (isMultiSelect) {
                    db.UpdateQty(ID, MultiQty);
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

                    alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            db.UpdateQty(ID, Integer.parseInt(input.getText().toString()));
                            c1.setText(input.getText());
                            myNom.requery();
                            String ToolBarContr = db.GetToolbarContr();
                            String OrderSum = db.getOrderSum();
                            toolbar.setSubtitle(ToolBarContr + OrderSum);
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

            nomPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
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
                }
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
            final String ID = c.getText().toString();

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
            final String Ost = db.getNomenOst(ID);
            txtOst.setText(Ost);

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

            alertD.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean wantToCloseDialog = false;
                    if (Integer.parseInt(input.getText().toString()) <= Integer.parseInt(Ost)) {
                        db.UpdateOrderQty(Zakaz_id, ID, Integer.parseInt(input.getText().toString()));
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
        }
    };
    TextView grupID, sgiID;
    android.support.v4.app.Fragment fragment = null;
    android.support.v4.app.FragmentTransaction fragmentTransaction;
    android.support.v4.app.FragmentManager fragManager;
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
            nomPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
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
                }
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

    public android.support.v7.widget.Toolbar getToolbar() {
        return toolbar;
    }

    public void LoadSgi() {
        mySgi = db.getAllSgi();
        spSgi = view.findViewById(R.id.SpinSgi);

        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.sgi_layout, mySgi, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColSgiID, R.id.ColSgiDescr});
        spSgi.setAdapter(adapter);
        spSgi.post(new Runnable() {
            public void run() {
                spSgi.setOnItemSelectedListener(SelectedSgi);
            }
        });
    }

    public void LoadGroups(String SgiID) {
        myGrups = db.getGrupBySgi(SgiID);
        spGrup = view.findViewById(R.id.SpinGrups);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.grup_layout, myGrups, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColGrupID, R.id.ColGrupDescr});
        spGrup.setAdapter(adapter);
        spGrup.setOnItemSelectedListener(SelectedGroup);
    }

    public void LoadFiltersWC(View vw) {
        curWC = dbApp.getWCs();
        spWC = vw.findViewById(R.id.spinWC);
        android.widget.SimpleCursorAdapter adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.wc_layout, curWC, new String[]{"DEMP", "DEMP"}, new int[]{R.id.ColWCID, R.id.ColWCDescr});
        spWC.setAdapter(adapter);
    }

    public void LoadFiltersFocus(View vw) {
        curFocus = db.getFocuses();
        spFocus = vw.findViewById(R.id.spinFocus);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.focus_layout, curFocus, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColFocusID, R.id.ColFocusDescr});
        spFocus.setAdapter(adapter);
    }

    public void LoadFiltersSgi(View vw) {
        curFSgi = db.getFilterSgi();
        spFSgi = vw.findViewById(R.id.spinSGI);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.sgif_layout, curFSgi, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColSgiFID, R.id.ColSgiFDescr});
        spFSgi.setAdapter(adapter);
    }

    public void LoadFiltersGroups(View vw) {
        curFGroup = db.getGroups();
        spFGroup = vw.findViewById(R.id.spinGroup);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.groupf_layout, curFGroup, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColGroupFID, R.id.ColGroupFDescr});
        spFGroup.setAdapter(adapter);
    }

    public void LoadNom(String GrupID, String SgiID) {
        FormOrderFragment.isSorted = false;
        nomenList.setSelection(0);
        if (!FormOrderFragment.isSorted) {
            FormOrderFragment.mainMenu.findItem(R.id.NomenSort).setIcon(R.drawable.to_end);
        }

        myNom = db.getNomByGroup(GrupID, SgiID);
        nomenList.setAdapter(null);
        NomenAdapter = new MyCursorAdapter(glbContext, R.layout.nomen_layout, myNom, new String[]{"_id", "KOD5", "DESCR", "OST", "PRICE", "ZAKAZ", "GRUPPA", "SGI", "FOTO"}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomPrice, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomPhoto}, 0);
        nomenList.post(new Runnable() {
            public void run() {
                nomenList.setAdapter(NomenAdapter);
            }
        });
        nomenList.setOnItemClickListener(GridNomenClick);
        nomenList.setOnItemLongClickListener(GridNomenLongClick);

    }

    public void LoadNomByFilters(String SgiID, String GrupID, String WCID, String FocusID) {
        Log.d("xd", SgiID + " " + GrupID + " " + WCID + " " + FocusID);
        myNom = db.getNomByFilters(SgiID, GrupID, WCID, FocusID);

        nomenList.setAdapter(null);
        NomenAdapter = new MyCursorAdapter(glbContext, R.layout.nomen_layout, myNom, new String[]{"_id", "KOD5", "DESCR", "OST", "PRICE", "ZAKAZ", "GRUPPA", "SGI", "FOTO"}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomPrice, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomPhoto}, 0);
        nomenList.post(new Runnable() {
            public void run() {
                nomenList.setAdapter(NomenAdapter);
            }
        });
        nomenList.setOnItemClickListener(GridNomenClick);
        nomenList.setOnItemLongClickListener(GridNomenLongClick);

    }

    public void SearchNom(String SearchStr) {
        nomenList.setAdapter(null);
        myNom = db.getSearchNom(SearchStr);
        NomenAdapter = new MyCursorAdapter(glbContext, R.layout.nomen_layout, myNom, new String[]{"ID", "COD", "DESCR", "OST", "PRICE", "ZAKAZ", "GRUPID", "SGIID", "PHOTO1", "VKOROB", "MP"}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomPrice, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomPhoto, R.id.ColNomVkorob, R.id.ColNomMP}, 0);
        nomenList.post(new Runnable() {
            public void run() {
                nomenList.setAdapter(NomenAdapter);
            }
        });
        nomenList.setOnItemClickListener(GridNomenClick);
        nomenList.setOnItemLongClickListener(GridNomenLongClick);
    }

    public void SearchNomInGroup(String SearchStr, String Group) {
        nomenList.setAdapter(null);
        myNom = db.getSearchNomInGroup(SearchStr, Group);
        NomenAdapter = new MyCursorAdapter(glbContext, R.layout.nomen_layout, myNom, new String[]{"_id", "KOD5", "DESCR", "OST", "PRICE", "ZAKAZ", "GRUPPA", "SGI", "FOTO"}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomPrice, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomPhoto}, 0);
        nomenList.post(new Runnable() {
            public void run() {
                nomenList.setAdapter(NomenAdapter);
            }
        });
        nomenList.setOnItemClickListener(GridNomenClick);
        nomenList.setOnItemLongClickListener(GridNomenLongClick);
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

    public void SetSelectedFilterSgi(String ID) {
        for (int i = 0; i < spFSgi.getCount(); i++) {
            Cursor value = (Cursor) spFSgi.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("CODE"));
            if (ID.equals(id)) {
                spFSgi.setSelection(i);
                break;
            }
        }
    }

    public void SetSelectedFilterGroup(String ID) {
        for (int i = 0; i < spFGroup.getCount(); i++) {
            Cursor value = (Cursor) spFGroup.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("CODE"));
            if (ID.equals(id)) {
                spFGroup.setSelection(i);
                break;
            }
        }
    }

    public void SetSelectedFilterWC(String ID) {
        for (int i = 0; i < spWC.getCount(); i++) {
            Cursor value = (Cursor) spWC.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("DEMP"));
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

    public String GetSDCardpath() {

        String sdpath = "";
        if (Build.BRAND == "samsung") {
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
        new Thread(new Runnable() {
            @Override
            public void run() {
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

                CurAc.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                    }
                });
            }
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
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alertPhoto = builder.create();
        alertPhoto.getWindow().setLayout(600, 400);
        alertPhoto.show();

        imageView = alertPhoto.findViewById(R.id.ivPhoto);
        imageView.setMaxScale(3f);
        imageView.setDoubleTapZoomScale(1F);

        if (imgFile.exists()) {
            imageView.setImage(ImageSource.uri(photoDir + "/" + PhotoFileName));
            imageView.setTag("Фото 1");
            imageView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
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
                }
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
        PreviewZakazAdapter = new MyCursorAdapter(glbContext, R.layout.nomen_layout, myNom, new String[]{"_id", "KOD5", "DESCR", "OST", "PRICE", "ZAKAZ", "GRUPPA", "SGI", "FOTO"}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomPrice, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomPhoto}, 0);
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
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(CurAc, R.layout.tp_layout, TP, new String[]{"_id", "CODE", "DESCR"}, new int[]{R.id.ColTP_ROWID, R.id.ColTPID, R.id.ColTPDescr});
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
        Cursor c;

        String TP, CONTR, ADDR, DOCNO, COMMENT, NOMEN;
        java.util.Date DELIVERY, DOCDATE;
        double QTY, PRICE;

        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("ddMMyyyy_hhmmss");

        String curdate = df.format(Calendar.getInstance().getTime());

        String FileName = CurAc.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/orders_" + curdate + ".dbf";
        DBF_FileName = "orders_" + curdate + ".dbf";

        c = db.getReadableDatabase().rawQuery("SELECT TORG_PRED.CODE as TP, CONTRS.CODE as CONTR, ADDRS.CODE as ADDR, ZAKAZY.DOCID as DOCID, ZAKAZY.DOC_DATE as DOC_DATE, ZAKAZY.DELIVERY_DATE as DEL_DATE, ZAKAZY.COMMENT as COMMENT, ZAKAZY_DT.NOMEN as NOMEN, ZAKAZY_DT.DESCR as DES, ZAKAZY_DT.QTY as QTY, ZAKAZY_DT.PRICE as PRICE FROM ZAKAZY JOIN TORG_PRED ON ZAKAZY.TP = TORG_PRED.CODE JOIN CONTRS ON ZAKAZY.CONTR = CONTRS.CODE JOIN ADDRS ON ZAKAZY.ADDR = ADDRS.CODE JOIN ZAKAZY_DT ON ZAKAZY.DOCID = ZAKAZY_DT.ZAKAZ_ID WHERE ZAKAZY.ROWID='" + ID + "'", null);
        if (c.getCount() == 0) {
            Toast.makeText(CurAc, "В таблице заказов нет записей для отправки", Toast.LENGTH_LONG).show();
            return "";
        }

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
        fields[index].setName("DOCNO");
        fields[index].setDataType(DBFField.FIELD_TYPE_C);
        fields[index].setFieldLength(13);
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
        fields[index].setFieldLength(12);
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
                PRICE = c.getDouble(c.getColumnIndex("PRICE"));

                c.close();

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
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 40));
            layout.addView(checkBox);

            if (status.equals("Сохранен")) {
                checkBox.setChecked(true);
            }

            GlobalVars.allOrders.add(new CheckBoxData(id, checkBox, status));
        } while (c.moveToNext());
    }

    public void LoadOrders() {
        gdOrders.setAdapter(null);
        Orders = dbOrders.getZakazy();
        if (Orders != null)
            putCheckBox(Orders);
        OrdersAdapter = new JournalAdapter(CurAc, R.layout.orders_item, Orders, new String[]{"DOCID", "STATUS", "DOC_DATE", "DELIVERY", "CONTR", "ADDR", "SUM"}, new int[]{R.id.ColOrdDocNo, R.id.ColOrdStatus, R.id.ColOrdDocDate, R.id.ColOrdDeliveryDate, R.id.ColOrdContr, R.id.ColOrdAddr, R.id.ColOrdSum}, 0);
        gdOrders.setAdapter(OrdersAdapter);
    }

    public void LoadOrdersDetails(String ZakazID) {
        orderdtList.setAdapter(null);
        OrdersDt = dbOrders.getZakazDetails(ZakazID);
        OrdersDtAdapter = new JournalDetailsAdapter(CurAc, R.layout.orderdt_item, OrdersDt, new String[]{"ZAKAZ_ID", "NOMEN", "DESCR", "QTY", "PRICE", "SUM"}, new int[]{R.id.ColOrdDtZakazID, R.id.ColOrdDtCod, R.id.ColOrdDtDescr, R.id.ColOrdDtQty, R.id.ColOrdDtPrice, R.id.ColOrdDtSum}, 0);
        orderdtList.setAdapter(OrdersDtAdapter);
        orderdtList.setOnItemClickListener(OrderDtNomenClick);
    }

    public String ReadLastUpdate() {
        SharedPreferences settings;
        settings = glbContext.getSharedPreferences("update_settings", 0);
        return settings.getString("Last_date", "");
    }

    public void SendOrders(int[] chosenOrdersID) throws DBFException {
        OrderID = "";
        Cursor c;
        String TP, CONTR, ADDR, DOCNO, COMMENT, CODE;
        java.util.Date DELIVERY, DOCDATE;
        double QTY, PRICE;
        int ID;
        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("ddMMyyyy_hhmmss");

        String curdate = df.format(Calendar.getInstance().getTime());

        String FileName = CurAc.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/orders_" + curdate + ".dbf";
        DBF_FileName = "orders_" + curdate + ".dbf";
        c = dbOrders.getReadableDatabase().rawQuery("SELECT TP, CONTR, ADDR, ZAKAZY.DOCID as DOCID," +
                " ZAKAZY.DELIVERY_DATE as DEL_DATE," +
                " ZAKAZY.DOC_DATE as DOC_DATE, ZAKAZY.COMMENT as COMMENT, ZAKAZY_DT.NOMEN as NOMEN, ZAKAZY_DT.DESCR as DES, ZAKAZY_DT.QTY as QTY, ZAKAZY_DT.PRICE as PRICE, ZAKAZY.ROWID AS ID FROM ZAKAZY JOIN ZAKAZY_DT ON ZAKAZY.DOCID = ZAKAZY_DT.ZAKAZ_ID WHERE ZAKAZY.STATUS=0", null);
        if (c.getCount() == 0) {
            Toast.makeText(CurAc, "В таблице заказов нет записей для отправки", Toast.LENGTH_LONG).show();
            return;
        }

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
        fields[index].setName("DOCNO");
        fields[index].setDataType(DBFField.FIELD_TYPE_C);
        fields[index].setFieldLength(13);
        index++;

        fields[index] = new DBFField();
        fields[index].setName("DOCDATE");
        fields[index].setDataType(DBFField.FIELD_TYPE_D);
        index++;

        fields[index] = new DBFField();
        fields[index].setName("DELIVERY");
        fields[index].setDataType(DBFField.FIELD_TYPE_D);
        index++;

        fields[index] = new DBFField();
        fields[index].setName("COMMENT");
        fields[index].setDataType(DBFField.FIELD_TYPE_C);
        fields[index].setFieldLength(255);
        index++;

        fields[index] = new DBFField();
        fields[index].setName("CODE");
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
        fields[index].setFieldLength(12);
        fields[index].setDecimalCount(2);
        Table.setFields(fields);

        // TP, CONTR, ADDR, DOCNO, DOC_DATE, DELIVERY_DATE, COMMENT, NOM_ID, DESCR, QTY, PRICE, ROWID
        try {
            while (c.moveToNext()) {
                ID = c.getInt(c.getColumnIndex("ID"));

                if (Arrays.binarySearch(chosenOrdersID, ID) < 0)
                    continue;

                TP = c.getString(c.getColumnIndex("TP"));
                CONTR = c.getString(c.getColumnIndex("CONTR"));
                ADDR = c.getString(c.getColumnIndex("ADDR"));
                DOCNO = c.getString(c.getColumnIndex("DOCID"));
                DOCDATE = StrToDbfDate(c.getString(c.getColumnIndex("DOC_DATE")));
                DELIVERY = StrToDbfDate(c.getString(c.getColumnIndex("DEL_DATE")));
                COMMENT = c.getString(c.getColumnIndex("COMMENT"));
                CODE = c.getString(c.getColumnIndex("NOMEN"));
                QTY = c.getDouble(c.getColumnIndex("QTY"));
                PRICE = c.getDouble(c.getColumnIndex("PRICE"));

                c.close();
                Object[] rowData = new Object[10];
                rowData[0] = TP;
                rowData[1] = CONTR;
                rowData[2] = ADDR;
                rowData[3] = DOCNO;
                rowData[4] = DOCDATE;
                rowData[5] = DELIVERY;
                rowData[6] = COMMENT;
                rowData[7] = CODE;
                rowData[8] = QTY;
                rowData[9] = PRICE;
                Table.addRecord(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Table.write();
        DBF_FIleForSend = FileName;
        if (isNetworkAvailable()) {
            Integer[] data = new Integer[chosenOrdersID.length];
            for (int i = 0; i < chosenOrdersID.length; i++) {
                data[i] = chosenOrdersID[i];
            }
            new UploadDBFFile().execute(data);
        } else {
            Toast.makeText(CurAc, "Нет доступного интернет соединения", Toast.LENGTH_LONG).show();
        }
    }

    public void LoadDebetByContr(String ID) {
        debetList.setAdapter(null);
        curDebet = db.getDebetByContr(ID);
        DebetAdapter adapter;
        adapter = new DebetAdapter(CurAc, R.layout.debet_layout, curDebet, new String[]{"DESCR", "STATUS", "KREDIT", "SALDO", "A7", "A14", "A21", "A28", "A35", "A42", "A49", "A56", "A63", "A64", "OTG30", "OPL30", "KOB", "FIRMA", "CRT_DATE"}, new int[]{R.id.ColDebetContr, R.id.ColDebetStatus, R.id.ColDebetCredit, R.id.ColDebetDolg, R.id.ColDebetA7, R.id.ColDebetA14, R.id.ColDebetA21, R.id.ColDebetA28, R.id.ColDebetA35, R.id.ColDebetA42, R.id.ColDebetA49, R.id.ColDebetA56, R.id.ColDebetA63, R.id.ColDebetA64, R.id.ColDebetOTG30, R.id.ColDebetOPL30, R.id.ColDebetKOB, R.id.ColDebetFirma, R.id.ColDebetDogovor}, 0);
        debetList.setAdapter(adapter);
    }

    public void LoadDebet(String TP_ID, String CONTR_ID) {
        debetList.setAdapter(null);
        curDebet = db.getDebet(TP_ID, CONTR_ID);
        if (curDebet == null) {
            Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
        }
        DebetAdapter adapter;
        adapter = new DebetAdapter(CurAc, R.layout.debet_layout, curDebet, new String[]{"DESCR", "KREDIT", "SALDO", "A7", "A14", "A21", "A28", "A35", "A42", "A49", "A56", "A63", "A64", "OTG30", "OPL30", "KOB", "FIRMA", "CRT_DATE"}, new int[]{R.id.ColDebetContr, R.id.ColDebetCredit, R.id.ColDebetDolg, R.id.ColDebetA7, R.id.ColDebetA14, R.id.ColDebetA21, R.id.ColDebetA28, R.id.ColDebetA35, R.id.ColDebetA42, R.id.ColDebetA49, R.id.ColDebetA56, R.id.ColDebetA63, R.id.ColDebetA64, R.id.ColDebetOTG30, R.id.ColDebetOPL30, R.id.ColDebetKOB, R.id.ColDebetFirma, R.id.ColDebetDogovor}, 0);
        debetList.setAdapter(adapter);
    }

    public void SearchDebet(String Contr) {
        debetList.setAdapter(null);
        curDebet = db.SearchInDebet(Contr);
        DebetAdapter adapter;
        adapter = new DebetAdapter(CurAc, R.layout.debet_layout, curDebet, new String[]{"DESCR", "STATUS", "KREDIT", "SALDO", "A7", "A14", "A21", "A28", "A35", "A42", "A49", "A56", "A63", "A64", "OTG30", "OPL30", "KOB", "FIRMA", "CRT_DATE"}, new int[]{R.id.ColDebetContr, R.id.ColDebetStatus, R.id.ColDebetCredit, R.id.ColDebetDolg, R.id.ColDebetA7, R.id.ColDebetA14, R.id.ColDebetA21, R.id.ColDebetA28, R.id.ColDebetA35, R.id.ColDebetA42, R.id.ColDebetA49, R.id.ColDebetA56, R.id.ColDebetA63, R.id.ColDebetA64, R.id.ColDebetOTG30, R.id.ColDebetOPL30, R.id.ColDebetKOB, R.id.ColDebetFirma, R.id.ColDebetDogovor}, 0);
        debetList.setAdapter(adapter);
    }

    public void LoadContrListDeb() {
        spContrDeb.setAdapter(null);
        curDebetContr = db.getContrList();
        DebetContrsAdapter adapter;
        adapter = new DebetContrsAdapter(CurAc, R.layout.contr_layout, curDebetContr, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
        spContrDeb.setAdapter(adapter);
    }

    public void LoadTpListDeb() {
        curDebetTp = db.getTpList();
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(CurAc, R.layout.tp_layout, curDebetTp, new String[]{"_id", "CODE", "DESCR"}, new int[]{R.id.ColTP_ROWID, R.id.ColTPID, R.id.ColTPDescr});
        spTP.setAdapter(adapter);

        spTP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void SetSelectedContr(String ID) {
        for (int i = 0; i < spContrDeb.getCount(); i++) {
            Cursor value = (Cursor) spContrDeb.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("ID"));
            if (ID.equals(id)) {
                spContrDeb.setSelection(i);
                break;
            }
        }
    }

    public String[] GetCurrentVersion() {
        PackageInfo pInfo = null;
        try {
            pInfo = CurAc.getPackageManager().getPackageInfo(CurAc.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return new String[]{String.valueOf(pInfo.versionCode), pInfo.versionName};
    }

    public String[] GetLastVersion() {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = CurAc.getSharedPreferences("apk_version", 0);
        editor = settings.edit();

        FTPClient ftpClient;
        ftpClient = new FTPClient();
        String FileName = CurAc.getFilesDir() + "/ver.txt";

        String version = "", versionName = "";

        try {
            ftpClient.connect(settings.getString("AppUpdateSrv", getResources().getString(R.string.ftp_update_server)));
            ftpClient.login(settings.getString("AppUpdateUser", getResources().getString(R.string.ftp_update_user)), settings.getString("AppUpdatePass", getResources().getString(R.string.ftp_update_pass)));
            ftpClient.changeWorkingDirectory("ARM_TP");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileTransferMode(FTP.ASCII_FILE_TYPE);
            ftpClient.setFileType(FTP.ASCII_FILE_TYPE);

            InputStream is = new BufferedInputStream(ftpClient.retrieveFileStream("ver.txt"));
            String[] result = convertStreamToString(is);
            version = result[0];
            versionName = result[1];

            editor.putInt("NewVersion", Integer.parseInt(result[0]));
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new String[]{version, versionName};
    }

    public String[] GetLastVersionLocal() {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = CurAc.getSharedPreferences("apk_version", 0);
        editor = settings.edit();

        FTPClient ftpClient;
        ftpClient = new FTPClient();
        String version = "", versionName = "";

        try {
            ftpClient.connect("192.168.10.24");
            ftpClient.login(settings.getString("AppUpdateUser", getResources().getString(R.string.ftp_update_user)), settings.getString("AppUpdatePass", getResources().getString(R.string.ftp_update_pass)));
            ftpClient.changeWorkingDirectory("ARM_TP");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileTransferMode(FTP.ASCII_FILE_TYPE);
            ftpClient.setFileType(FTP.ASCII_FILE_TYPE);

            InputStream is = new BufferedInputStream(ftpClient.retrieveFileStream("ver.txt"));
            String[] result = convertStreamToString(is);
            version = result[0];
            versionName = result[1];

            editor.putInt("NewVersion", Integer.parseInt(result[0]));
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new String[]{version, versionName};
    }

    private String[] convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String version = "", versionName = "";
        try {
            version = reader.readLine();
            versionName = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new String[]{version, versionName};
    }

    public void DownloadApp(String FtpServer) {
        FTPClient ftpClient;
        ftpClient = new FTPClient();
        String FileName = CurAc.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/app-debug.apk";
        File appFile = new File(CurAc.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/app-debug.apk");
        SharedPreferences settings;
        settings = CurAc.getSharedPreferences("apk_version", 0);
        int reply;

        if (appFile.exists()) {
            appFile.delete();
        }

        try {
            ftpClient.connect(FtpServer);
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                throw new Exception("Exception in connecting to FTP Server");
            }

            ftpClient.login(settings.getString("AppUpdateUser", getResources().getString(R.string.ftp_update_user)), settings.getString("AppUpdatePass", getResources().getString(R.string.ftp_update_pass)));
            ftpClient.changeWorkingDirectory("ARM_TP");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            FileOutputStream fos = new FileOutputStream(FileName);
            ftpClient.retrieveFile("app-debug.apk", fos);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if (appFile.length() == 0) {
            DownloadApp("192.168.10.24");
        }
    }

    public Integer getSMSCount() {
        String count = "";
        Cursor cur_sms = SmsDB.rawQuery("SELECT CASE WHEN COUNT(ROW_ID) IS NULL THEN '0' ELSE COUNT(ROW_ID) END  AS CNT FROM MSGS WHERE IS_NEW=1", null);
        if (cur_sms.moveToNext()) {
            count = cur_sms.getString(cur_sms.getColumnIndex("CNT"));
        }
        cur_sms.close();
        return count.equals("") ? 0 : Integer.parseInt(count);
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
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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

                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

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

    public static class DebetContrsAdapter extends SimpleCursorAdapter {
        public DebetContrsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Cursor cursor = getCursor();
            TextView tvDescr = view.findViewById(R.id.ColContrDescr);

            String resDescr = cursor.getString(cursor.getColumnIndex("DESCR"));

            if (position % 2 == 0) {
                tvDescr.setBackgroundColor(Color.rgb(201, 235, 255));
            } else {
                tvDescr.setBackgroundColor(Color.rgb(255, 255, 255));
            }

            tvDescr.setText(resDescr);
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
            int resID;

            final Cursor cursor = getCursor();
            View view = super.getView(position, convertView, parent);
            TextView tvDescr = view.findViewById(R.id.ColNomDescr);
            TextView tvPrice = view.findViewById(R.id.ColNomPrice);
            TextView tvPosition = view.findViewById(R.id.ColNomPosition);

            final TextView tvPhoto = view.findViewById(R.id.ColNomPhoto);
            final Button btPlus = view.findViewById(R.id.btPlus);
            final Button btMinus = view.findViewById(R.id.btMinus);


            tvDescr.setTextSize(SettingFragment.nomenDescriptionFontSize);

            tvPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((GridView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
                }
            });

            btPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((GridView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
                }
            });

            btMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((GridView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
                }
            });

            tvPrice.setText(String.format("%.2f", cursor.getDouble(cursor.getColumnIndex("PRICE"))));
            if (cursor.getString(cursor.getColumnIndex("FOTO")) != null) {
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


            if (position % 2 == 0) {
                view.setBackgroundColor(Color.rgb(201, 235, 255));
            } else {
                view.setBackgroundColor(Color.rgb(255, 255, 255));
            }

            tvPosition.setText(String.valueOf(position + 1));

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

    public static class JournalAdapter extends SimpleCursorAdapter {
        Cursor cursor;

        public JournalAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            cursor = c;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            if (convertView != null) {

            }

            if (position % 2 == 0) {
                view.setBackgroundColor(Color.rgb(201, 235, 255));
            } else {
                view.setBackgroundColor(Color.rgb(255, 255, 255));
            }
            return view;
        }
    }

    public static class JournalDetailsAdapter extends SimpleCursorAdapter {
        public JournalDetailsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            Cursor cursor = getCursor();
            View view = super.getView(position, convertView, parent);
            TextView tvQty = view.findViewById(R.id.ColOrdDtQty);

            if (position % 2 == 0) {
                view.setBackgroundColor(Color.rgb(201, 235, 255));
            } else {
                view.setBackgroundColor(Color.rgb(255, 255, 255));
            }

            if (cursor.getInt(8) == 1) {
                tvQty.setText(cursor.getInt(cursor.getColumnIndex("QTY")) + "(-" + cursor.getInt(9) + ")");
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
            String resDescr = cursor.getString(2);

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
                ftpClient.changeWorkingDirectory("EXCHANGE/IN/MARS");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                InputStream inputStream;

                File secondLocalFile = new File(DBF_FIleForSend);
                String secondRemoteFile = DBF_FileName;
                inputStream = new FileInputStream(secondLocalFile);

                OutputStream outputStream = ftpClient.storeFileStream(secondRemoteFile);
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
                    dbOrders.SetZakazStatus(1, 0, id);
                }

                Toast.makeText(CurAc, "Заказы успешно сформированы в файл и отправлены", Toast.LENGTH_LONG).show();
            }

            LoadOrders();
        }
    }

    public static class DebetAdapter extends SimpleCursorAdapter {

        public DebetAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Cursor cursor = getCursor();
            View view = super.getView(position, convertView, parent);
            TextView tvContr = view.findViewById(R.id.ColDebetContr);
            TextView tvStatus = view.findViewById(R.id.ColDebetStatus);
            TextView tvCredit = view.findViewById(R.id.ColDebetCredit);
            TextView tvA7 = view.findViewById(R.id.ColDebetA7);
            TextView tvA14 = view.findViewById(R.id.ColDebetA14);
            TextView tvA21 = view.findViewById(R.id.ColDebetA21);
            TextView tvA28 = view.findViewById(R.id.ColDebetA28);
            TextView tvA35 = view.findViewById(R.id.ColDebetA35);
            TextView tvA42 = view.findViewById(R.id.ColDebetA42);
            TextView tvA49 = view.findViewById(R.id.ColDebetA49);
            TextView tvA56 = view.findViewById(R.id.ColDebetA56);
            TextView tvA63 = view.findViewById(R.id.ColDebetA63);
            TextView tvA64 = view.findViewById(R.id.ColDebetA64);
            TextView tvDolg = view.findViewById(R.id.ColDebetDolg);
            if (position % 2 == 0) {
                view.setBackgroundResource(R.drawable.debet_item_border_blue);
            } else {
                view.setBackgroundResource(R.drawable.debet_item_border_white);
            }

            if (cursor.getString(2).equals("ЗАПРЕТ")) {
                tvContr.setTextColor(Color.rgb(181, 38, 29));
                tvStatus.setTextColor(Color.rgb(181, 38, 29));
                tvCredit.setTextColor(Color.rgb(181, 38, 29));
                tvA7.setTextColor(Color.rgb(181, 38, 29));
                tvA14.setTextColor(Color.rgb(181, 38, 29));
                tvA21.setTextColor(Color.rgb(181, 38, 29));
                tvA28.setTextColor(Color.rgb(181, 38, 29));

                tvA35.setTextColor(Color.rgb(181, 38, 29));
                tvA42.setTextColor(Color.rgb(181, 38, 29));
                tvA49.setTextColor(Color.rgb(181, 38, 29));
                tvA56.setTextColor(Color.rgb(181, 38, 29));
                tvA63.setTextColor(Color.rgb(181, 38, 29));
                tvA64.setTextColor(Color.rgb(181, 38, 29));

                tvDolg.setTextColor(Color.rgb(181, 38, 29));
            } else if (cursor.getString(2).equals("ДОЛЖНИК")) {
                tvContr.setTextColor(Color.rgb(83, 51, 190));
                tvStatus.setTextColor(Color.rgb(83, 51, 190));
                tvCredit.setTextColor(Color.rgb(83, 51, 190));
                tvA7.setTextColor(Color.rgb(83, 51, 190));
                tvA14.setTextColor(Color.rgb(83, 51, 190));
                tvA21.setTextColor(Color.rgb(83, 51, 190));
                tvA28.setTextColor(Color.rgb(83, 51, 190));

                tvA35.setTextColor(Color.rgb(83, 51, 190));
                tvA42.setTextColor(Color.rgb(83, 51, 190));
                tvA49.setTextColor(Color.rgb(83, 51, 190));
                tvA56.setTextColor(Color.rgb(83, 51, 190));
                tvA63.setTextColor(Color.rgb(83, 51, 190));
                tvA64.setTextColor(Color.rgb(83, 51, 190));

                tvDolg.setTextColor(Color.rgb(83, 51, 190));
            } else if (cursor.getString(2).equals("ДИНАМЩИК")) {
                tvContr.setTextColor(Color.rgb(181, 38, 29));
                tvStatus.setTextColor(Color.rgb(181, 38, 29));
                tvCredit.setTextColor(Color.rgb(181, 38, 29));
                tvA7.setTextColor(Color.rgb(181, 38, 29));
                tvA14.setTextColor(Color.rgb(181, 38, 29));
                tvA21.setTextColor(Color.rgb(181, 38, 29));
                tvA28.setTextColor(Color.rgb(181, 38, 29));

                tvA35.setTextColor(Color.rgb(181, 38, 29));
                tvA42.setTextColor(Color.rgb(181, 38, 29));
                tvA49.setTextColor(Color.rgb(181, 38, 29));
                tvA56.setTextColor(Color.rgb(181, 38, 29));
                tvA63.setTextColor(Color.rgb(181, 38, 29));
                tvA64.setTextColor(Color.rgb(181, 38, 29));

                tvDolg.setTextColor(Color.rgb(181, 38, 29));
            } else {
                tvContr.setTextColor(Color.rgb(0, 0, 0));
                tvStatus.setTextColor(Color.rgb(0, 0, 0));
                tvCredit.setTextColor(Color.rgb(0, 0, 0));
                tvA7.setTextColor(Color.rgb(0, 0, 0));
                tvA14.setTextColor(Color.rgb(0, 0, 0));
                tvA21.setTextColor(Color.rgb(0, 0, 0));
                tvA28.setTextColor(Color.rgb(0, 0, 0));

                tvA35.setTextColor(Color.rgb(0, 0, 0));
                tvA42.setTextColor(Color.rgb(0, 0, 0));
                tvA49.setTextColor(Color.rgb(0, 0, 0));
                tvA56.setTextColor(Color.rgb(0, 0, 0));
                tvA63.setTextColor(Color.rgb(0, 0, 0));
                tvA64.setTextColor(Color.rgb(0, 0, 0));

                tvDolg.setTextColor(Color.rgb(0, 0, 0));
            }

            return view;
        }
    }
}
