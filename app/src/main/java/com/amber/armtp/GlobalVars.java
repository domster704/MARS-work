package com.amber.armtp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by filimonov on 22-08-2016.
 */
public class GlobalVars extends Application {

    public Context glbContext;
    public Context frContext;
    public Activity CurAc;
    public Cursor myNom, mySgi, myGrups, Orders, OrdersDt;
    public Cursor curTovcat, curFunc, curBrand, curWC, curProd, curFSgi, curFGroup, curFocus, curModel, curColors, curUniFilter;

    public MyCursorAdapter NomenAdapter, PreviewZakazAdapter;
    public JournalAdapter OrdersAdapter;
    public JournalDetailsAdapter OrdersDtAdapter;

    public View view;
    public DBHepler db;
    public GridView nomenList;
    public android.support.v7.widget.Toolbar toolbar;
    public String SelectGroup = null;

    public boolean isSales = false;
    public boolean isDiscount = false;
    public int Discount = 0;

    public int MultiQty = 0;
    public boolean isMultiSelect = false;

    public String frSgi;
    public String frGroup;

    public Spinner spSgi, spGrup;
    public Spinner spTovcat, spFunc, spBrand, spWC, spProd, spFSgi, spFGroup, spFocus, spModel, spColors;
    public Spinner spUniFilterResult;
    public EditText txtUniFilter;
    public File appDBFolder = new File(GetSDCardpath() + "ARMTP_DB");
    public File appPhotoFolder = new File(GetSDCardpath() + "ARM_PHOTO");
    public File appUpdatesFolder = new File(GetSDCardpath() + "ARM_UPDATES");
    public String DBFolder = "ARMTP_DB";
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
            String ItemID = Contr.getString(Contr.getColumnIndex("ID"));
            if (!ItemID.equals("0")) {
                LoadContrAddr(ItemID);
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };
    public Spinner spinContr, spinAddr, TPList, spinCenTypes;
    public Button btSave, btClear;
    public Calendar DeliveryDate, DeliveryTime;
    public EditText txtDate;
    public EditText edContrFilter;
    public EditText txtComment;
    public TextView spTp, spContr, spAddr;
    public TextView tvContr, tvTP;
    public ViewFlipper viewFlipper;
    public String ordStatus;
    public GridView gdOrders;
    public GridView orderList, orderdtList;
    public String DBF_FIleForSend;
    public String DBF_FileName;
    public Button btFilter, btClearFilter;
    public GridView debetList;
    public Spinner spContrDeb, spTP;
    public Cursor curDebet, curDebetContr, curDebetTp;
    public Cursor cur_sms;
    public SQLiteDatabase SmsDB = null;
    public SMSAdapter SMSadapter;
    public GridView smsList;
    //    public GPSTracker gps;
    public int BeginPos = 0, EndPos = 0;
    public Connection conn = null;
    //    public SQLiteDatabase geoDB = null;
    public AdapterView.OnItemClickListener GridNomenClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {
            long viewId = myView.getId();

            if (viewId == R.id.ColNomPhoto) {
                isSecondPhoto = false;
                String photoDir = getPhotoDir();
                long ID = myAdapter.getItemIdAtPosition(position);
                int cod = db.GetCod(ID);

                String FileName = cod + ".jpg";
                File imgFile = new File(photoDir + "/" + FileName);
                if (!imgFile.exists() || imgFile.length() == 0) {
                    AsyncFileName = FileName;
                    if (isNetworkAvailable()) {
                        DownloadPhoto(FileName);
                    } else {
                        Toast.makeText(glbContext, "Нет доступного интернет соединения", Toast.LENGTH_LONG).show();
                    }
                } else {
                    ShowNomenPhoto(FileName);
                }
            } else if (viewId == R.id.btPlus) {
                long ID = myAdapter.getItemIdAtPosition(position);
                db.PlusQty(ID);
                myNom.requery();
                String ToolBarContr = db.GetToolbarContr();
                String OrderSum = db.getOrderSum();
                toolbar.setSubtitle(ToolBarContr + OrderSum);
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
                toolbar.setSubtitle(ToolBarContr + OrderSum);
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
                    db.updateQty(ID, MultiQty);
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

                    input.setText(myNom.getString(6));
                    txtCod.setText(myNom.getString(2));
                    txtDescr.setText(myNom.getString(3));
                    txtOst.setText(myNom.getString(4));
                    txtGrup.setText(myNom.getString(17));

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
                            db.updateQty(ID, Integer.parseInt(input.getText().toString()));
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
            final TextView tvCod = myView.findViewById(R.id.ColNomCod);
            curSgi = txtSgi.getText().toString();

            Cursor c = myNom;
            String Photo = c.getString(9);
            Grup = c.getString(7);
            Sgi = c.getString(8);

            PopupMenu nomPopupMenu = new PopupMenu(glbContext, myView);
            nomPopupMenu.getMenuInflater().inflate(R.menu.nomen_context_menu, nomPopupMenu.getMenu());
//            nomPopupMenu.getMenu().findItem(R.id.showPhoto).setEnabled(true);
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
                            LoadNom(Grup);
                            return true;
                        default:
                    }
                    return true;
                }
            });
//            if (Photo.equals("")) {
//                nomPopupMenu.getMenu().findItem(R.id.showPhoto).setEnabled(false);
//            }
            nomPopupMenu.show();
            return true;
        }
    };
    public AdapterView.OnItemSelectedListener SelectedGroup = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            String ItemID = myGrups.getString(myGrups.getColumnIndex("ID"));
            if (!ItemID.equals("0")) {
                LoadNom(ItemID);
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
            String ItemID = mySgi.getString(mySgi.getColumnIndex("ID"));
            if (!ItemID.equals("0")) {
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
                        db.updateOrderQty(Zakaz_id, ID, Integer.parseInt(input.getText().toString()));
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
    TextView grupID, sgiID, tvCod;
    android.support.v4.app.Fragment fragment = null;
    android.support.v4.app.FragmentTransaction fragmentTransaction;
    android.support.v4.app.FragmentManager fragManager;
    public AdapterView.OnItemLongClickListener PreviewNomenLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, final View myView, int position, long arg3) {
            final String Grup;
            final String Sgi;
            final String curSgi;

            grupID = myView.findViewById(R.id.ColNomGRUPID);
            sgiID = myView.findViewById(R.id.ColNomSGIID);

            Cursor c = myNom;
            Grup = c.getString(7);
            Sgi = c.getString(8);

            String Photo = c.getString(9);

            nomPopupMenu = new PopupMenu(CurAc, myView);
            nomPopupMenu.getMenuInflater().inflate(R.menu.nomen_context_menu, nomPopupMenu.getMenu());
//            nomPopupMenu.getMenu().findItem(R.id.showPhoto).setEnabled(true);
            nomPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.goToGroup:
                            fragment = new FormOrderFragment();
                            fragmentTransaction = fragManager.beginTransaction();
                            fragmentTransaction.replace(R.id.frame, fragment, "frag_order_header");
                            fragmentTransaction.commit();
                            toolbar.setTitle("Формирование заказа");
                            frSgi = Sgi;
                            frGroup = Grup;
                            return true;
//                        case R.id.showPhoto:
//                            isSecondPhoto = false;
//                            String photoDir = getPhotoDir();
//                            tvCod = myView.findViewById(R.id.ColNomCod);
//                            String FileName = tvCod.getText().toString() + ".jpg";
//                            Toast.makeText(getContext(), FileName, Toast.LENGTH_LONG).show();
//                            File imgFile = new File(photoDir + "/" + FileName);
//
//                            if (!imgFile.exists() || imgFile.length() == 0) {
//                                AsyncFileName = FileName;
//                                if (isNetworkAvailable()) {
//                                    DownloadPhoto(FileName);
//                                } else {
//                                    Toast.makeText(CurAc, "Нет доступного интернет соединения", Toast.LENGTH_LONG).show();
//                                }
//                                return false;
//                            } else {
//                                ShowNomenPhoto(FileName);
//                            }
                        default:
                    }
                    return true;
                }
            });
//            if (Photo.equals("")) {
//                nomPopupMenu.getMenu().findItem(R.id.showPhoto).setEnabled(false);
//            }
            nomPopupMenu.show();
            return true;
        }
    };

    public static String getCalculatedDate(String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return s.format(new Date(cal.getTimeInMillis()));
    }

    public static java.util.Date StrToDbfDate(String Date) {
        java.util.Date return_date = null;
        String Year, Mon, Day;
        Year = Date.substring(6, 10);
        Mon = Date.substring(3, 5);
        Day = Date.substring(0, 2);
        String date = Year + Mon + Day;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
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
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.sgi_layout, mySgi, new String[]{"ID", "DESCR"}, new int[]{R.id.ColSgiID, R.id.ColSgiDescr});
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
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.grup_layout, myGrups, new String[]{"ID", "DESCR"}, new int[]{R.id.ColGrupID, R.id.ColGrupDescr});
        spGrup.setAdapter(adapter);
        spGrup.setOnItemSelectedListener(SelectedGroup);
    }

    public void LoadFiltersTovcat(View vw) {
        curTovcat = db.getTovcats();
        spTovcat = vw.findViewById(R.id.spinTovcat);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.tovcat_layout, curTovcat, new String[]{"ID", "DESCR"}, new int[]{R.id.ColTovcatID, R.id.ColTovcatDescr});
        spTovcat.setAdapter(adapter);
    }

    public void LoadFiltersFunc(View vw) {
        curFunc = db.getFuncs();
        spFunc = vw.findViewById(R.id.spinFunc);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.func_layout, curFunc, new String[]{"ID", "DESCR"}, new int[]{R.id.ColFuncID, R.id.ColFuncDescr});
        spFunc.setAdapter(adapter);
    }

    public void LoadFiltersBrand(View vw) {
        curBrand = db.getBrands();
        spBrand = vw.findViewById(R.id.spinBrand);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.brand_layout, curBrand, new String[]{"ID", "DESCR"}, new int[]{R.id.ColBrandID, R.id.ColBrandDescr});
        spBrand.setAdapter(adapter);
    }

    public void LoadFiltersWC(View vw) {
        curWC = db.getWCs();
        spWC = vw.findViewById(R.id.spinWC);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.wc_layout, curWC, new String[]{"ID", "DESCR"}, new int[]{R.id.ColWCID, R.id.ColWCDescr});
        spWC.setAdapter(adapter);
    }

    public void LoadFiltersProd(View vw) {
        curProd = db.getProds();
        spProd = vw.findViewById(R.id.spinProd);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.prod_layout, curProd, new String[]{"ID", "DESCR"}, new int[]{R.id.ColProdID, R.id.ColProdDescr});
        spProd.setAdapter(adapter);
    }

    public void LoadFiltersFocus(View vw) {
        curFocus = db.getFocuses();
        spFocus = vw.findViewById(R.id.spinFocus);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.focus_layout, curFocus, new String[]{"ID", "DESCR"}, new int[]{R.id.ColFocusID, R.id.ColFocusDescr});
        spFocus.setAdapter(adapter);
    }

    public void LoadFiltersModels(View vw) {
        curModel = db.getModels();
        spModel = vw.findViewById(R.id.spinModel);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.model_layout, curModel, new String[]{"ID", "DESCR"}, new int[]{R.id.ColModelID, R.id.ColModelDescr});
        spModel.setAdapter(adapter);
    }

    public void LoadFiltersColors(View vw) {
        curColors = db.getColor();
        spColors = vw.findViewById(R.id.spinColor);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.colors_layout, curColors, new String[]{"ID", "DESCR"}, new int[]{R.id.ColColorID, R.id.ColColorDescr});
        spColors.setAdapter(adapter);
    }

    public void LoadFiltersSgi(View vw) {
        curFSgi = db.getFilterSgi();
        spFSgi = vw.findViewById(R.id.spinSGI);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.sgif_layout, curFSgi, new String[]{"ID", "DESCR"}, new int[]{R.id.ColSgiFID, R.id.ColSgiFDescr});
        spFSgi.setAdapter(adapter);
    }

    public void LoadFiltersGroups(View vw) {
        curFGroup = db.getGroups();
        spFGroup = vw.findViewById(R.id.spinGroup);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.groupf_layout, curFGroup, new String[]{"ID", "DESCR"}, new int[]{R.id.ColGroupFID, R.id.ColGroupFDescr});
        spFGroup.setAdapter(adapter);
    }

    public void LoadNom(String GrupID) {
        myNom = db.getNomByGroup(GrupID);
        nomenList.setAdapter(null);
        NomenAdapter = new MyCursorAdapter(glbContext, R.layout.nomen_layout, myNom, new String[]{"ID", "COD", "DESCR", "OST", "PRICE", "ZAKAZ", "GRUPID", "SGIID", "PHOTO1", "VKOROB", "MP"}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomPrice, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomPhoto, R.id.ColNomVkorob, R.id.ColNomMP}, 0);
        nomenList.post(new Runnable() {
            public void run() {
                nomenList.setAdapter(NomenAdapter);
            }
        });
        nomenList.setOnItemClickListener(GridNomenClick);
        nomenList.setOnItemLongClickListener(GridNomenLongClick);

    }

    public void LoadNomByFilters(String SgiID, String GrupID, String TovcatID, String FuncID, String BrandID, String WCID, String ProdID, String FocusID, String ModelID, String ColorID) {
        myNom = db.getNomByFilters(SgiID, GrupID, TovcatID, FuncID, BrandID, WCID, ProdID, FocusID, ModelID, ColorID);
        nomenList.setAdapter(null);
        NomenAdapter = new MyCursorAdapter(glbContext, R.layout.nomen_layout, myNom, new String[]{"ID", "COD", "DESCR", "OST", "PRICE", "ZAKAZ", "GRUPID", "SGIID", "PHOTO1", "VKOROB", "MP"}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomPrice, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomPhoto, R.id.ColNomVkorob, R.id.ColNomMP}, 0);
        nomenList.post(new Runnable() {
            public void run() {
                nomenList.setAdapter(NomenAdapter);
            }
        });
        nomenList.setOnItemClickListener(GridNomenClick);
        nomenList.setOnItemLongClickListener(GridNomenLongClick);

    }

    public void LoadNomByUniFilters(String TypeID, String ID) {
        myNom = db.getNomByUniFilters(TypeID, ID);
        nomenList.setAdapter(null);
        NomenAdapter = new MyCursorAdapter(glbContext, R.layout.nomen_layout, myNom, new String[]{"ID", "COD", "DESCR", "OST", "PRICE", "ZAKAZ", "GRUPID", "SGIID", "PHOTO1", "VKOROB", "MP"}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomPrice, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomPhoto, R.id.ColNomVkorob, R.id.ColNomMP}, 0);
        nomenList.post(new Runnable() {
            public void run() {
                nomenList.setAdapter(NomenAdapter);
            }
        });
        nomenList.setOnItemClickListener(GridNomenClick);
        nomenList.setOnItemLongClickListener(GridNomenLongClick);

    }

    public void LoadUniFilters(View vw, String Descr) {
        curUniFilter = db.getUniFilters(Descr);
        spUniFilterResult = vw.findViewById(R.id.spinFilterResult);
        UniFilterAdatper adapter;
        adapter = new UniFilterAdatper(glbContext, R.layout.unifilter_layout, curUniFilter, new String[]{"TYPE_ID", "TYPE_DESCR", "ID", "LOWDESCR"}, new int[]{R.id.tvUniTypeID, R.id.tvUniTypeDescr, R.id.tvUniID, R.id.tvUniDescr}, 0);
        spUniFilterResult.setAdapter(adapter);

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
        NomenAdapter = new MyCursorAdapter(glbContext, R.layout.nomen_layout, myNom, new String[]{"ID", "COD", "DESCR", "OST", "PRICE", "ZAKAZ", "GRUPID", "SGIID", "PHOTO1", "VKOROB", "MP"}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomPrice, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomPhoto, R.id.ColNomVkorob, R.id.ColNomMP}, 0);
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
            String id = value.getString(value.getColumnIndexOrThrow("ID"));
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
            String id = value.getString(value.getColumnIndexOrThrow("ID"));
            if (Grup.equals(id)) {
                spGrup.setSelection(i);
                break;
            }
        }
    }

    public void SetSelectedFilterSgi(String ID) {
        for (int i = 0; i < spFSgi.getCount(); i++) {
            Cursor value = (Cursor) spFSgi.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("ID"));
            if (ID.equals(id)) {
                spFSgi.setSelection(i);
                break;
            }
        }
    }

    public void SetSelectedFilterGroup(String ID) {
        for (int i = 0; i < spFGroup.getCount(); i++) {
            Cursor value = (Cursor) spFGroup.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("ID"));
            if (ID.equals(id)) {
                spFGroup.setSelection(i);
                break;
            }
        }
    }

    public void SetSelectedFilterTovcat(String ID) {
        for (int i = 0; i < spTovcat.getCount(); i++) {
            Cursor value = (Cursor) spTovcat.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("ID"));
            if (ID.equals(id)) {
                spTovcat.setSelection(i);
                break;
            }
        }
    }

    public void SetSelectedFilterFunc(String ID) {
        for (int i = 0; i < spFunc.getCount(); i++) {
            Cursor value = (Cursor) spFunc.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("ID"));
            if (ID.equals(id)) {
                spFunc.setSelection(i);
                break;
            }
        }
    }

    public void SetSelectedFilterBrand(String ID) {
        for (int i = 0; i < spBrand.getCount(); i++) {
            Cursor value = (Cursor) spBrand.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("ID"));
            if (ID.equals(id)) {
                spBrand.setSelection(i);
                break;
            }
        }
    }

    public void SetSelectedFilterWC(String ID) {
        for (int i = 0; i < spWC.getCount(); i++) {
            Cursor value = (Cursor) spWC.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("ID"));
            if (ID.equals(id)) {
                spWC.setSelection(i);
                break;
            }
        }
    }

    public void SetSelectedFilterProd(String ID) {
        for (int i = 0; i < spProd.getCount(); i++) {
            Cursor value = (Cursor) spProd.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("ID"));
            if (ID.equals(id)) {
                spProd.setSelection(i);
                break;
            }
        }
    }

    public void SetSelectedFilterFocus(String ID) {
        for (int i = 0; i < spFocus.getCount(); i++) {
            Cursor value = (Cursor) spFocus.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("ID"));
            if (ID.equals(id)) {
                spFocus.setSelection(i);
                break;
            }
        }
    }

    public void SetSelectedFilterModel(String ID) {
        for (int i = 0; i < spModel.getCount(); i++) {
            Cursor value = (Cursor) spModel.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("ID"));
            if (ID.equals(id)) {
                spModel.setSelection(i);
                break;
            }
        }
    }

    public void SetSelectedFilterColor(String ID) {
        for (int i = 0; i < spColors.getCount(); i++) {
            Cursor value = (Cursor) spColors.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndexOrThrow("ID"));
            if (ID.equals(id)) {
                spColors.setSelection(i);
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

    public String GetStoragePath2019() {

        String sdpath;
        sdpath = Objects.requireNonNull(getExternalFilesDir(null)) + "/";

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

    public boolean DownloadPhoto(final String FileName) {
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

                    if (tmpName.equals("_2")) {
                        db.getWritableDatabase().execSQL("UPDATE Nomen SET P2D=1 WHERE COD='" + isDownloaded.replace(tmpName, "") + "'");
                    } else {
                        db.getWritableDatabase().execSQL("UPDATE Nomen SET P1D=1 WHERE COD='" + isDownloaded + "'");
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
        return true;
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
        Log.d("xd", imFileName);
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
        PreviewZakazAdapter = new MyCursorAdapter(glbContext, R.layout.nomen_layout, myNom, new String[]{"ID", "COD", "DESCR", "OST", "PRICE", "ZAKAZ", "GRUPID", "SGIID", "PHOTO1", "VKOROB", "MP"}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomPrice, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomPhoto, R.id.ColNomVkorob, R.id.ColNomMP}, 0);

        nomenList.setAdapter(PreviewZakazAdapter);
        nomenList.setOnItemClickListener(GridNomenClick);
        nomenList.setOnItemLongClickListener(PreviewNomenLongClick);
    }

    public void WriteLastUpdate() {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String curdate = df.format(Calendar.getInstance().getTime());

        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = CurAc.getSharedPreferences("update_settings", 0);
        editor = settings.edit();
        editor.putString("Last_date", curdate);
        editor.commit();
    }

    public Boolean CheckTPLock() {
        SharedPreferences settings;
        settings = PreferenceManager.getDefaultSharedPreferences(frContext);
        Boolean TP_LOCK = settings.getBoolean("TP_LOCK", false);
        return TP_LOCK;
    }

    public void LoadTpList() {
        TP = db.getTpList();
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(CurAc, R.layout.tp_layout, TP, new String[]{"_id", "ID", "DESCR"}, new int[]{R.id.ColTP_ROWID, R.id.ColTPID, R.id.ColTPDescr});
        TPList.setAdapter(adapter);

        TPList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
                String ItemID = TP.getString(TP.getColumnIndex("ID"));
                CurrentTp = ItemID;
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        if (CheckTPLock()) {
            TPList.setEnabled(false);
        }
    }

    public void LoadCenTypes() {
        CenTypes = db.getCenTypes();
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(CurAc, R.layout.centype_layout, CenTypes, new String[]{"_id", "CEN_ID", "DESCR"}, new int[]{R.id.ColCen_ROWID, R.id.ColCenID, R.id.ColCenDescr});
        spinCenTypes.setAdapter(adapter);

        spinCenTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
                String ItemID = CenTypes.getString(CenTypes.getColumnIndex("CEN_ID"));
                SharedPreferences settings;
                SharedPreferences.Editor editor;
                settings = CurAc.getSharedPreferences("apk_version", 0);
                editor = settings.edit();
                editor.putString("usr_centype", ItemID);
                editor.commit();
                CurrentCenType = ItemID;
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void LoadContrList() {
        spinContr.setAdapter(null);
        Contr = db.getContrList();
        ContrsAdapter adapter;
        adapter = new ContrsAdapter(CurAc, R.layout.contr_layout, Contr, new String[]{"ID", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
        spinContr.setAdapter(adapter);
        spinContr.setOnItemSelectedListener(SelectedContr);
    }

    public void LoadFilteredContrList(String FindStr) {
        spinContr.setAdapter(null);
        Contr = db.getContrFilterList(FindStr);
        ContrsAdapter adapter;
        adapter = new ContrsAdapter(CurAc, R.layout.contr_layout, Contr, new String[]{"ID", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
        spinContr.setAdapter(adapter);
        spinContr.setOnItemSelectedListener(SelectedContr);
    }

    public void LoadContrAddr(String ContID) {
        Addr = db.getContrAddress(ContID);
        AddrsAdapter adapter;
        adapter = new AddrsAdapter(CurAc, R.layout.addr_layout, Addr, new String[]{"ID", "DESCR"}, new int[]{R.id.ColContrAddrID, R.id.ColContrAddrDescr}, 0);
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
            String id = value.getString(value.getColumnIndexOrThrow("ID"));
            if (AddrID.equals(id)) {
                spinAddr.setSelection(i);
                break;
            }
        }
    }

    public String FormDBFForZakaz(String ID) throws DBFException {
        String DBF_FIleForSend;
        String DBF_FileName;
        Cursor c;

        String TP, CONTR, ADDR, DOCNO, COMMENT, CODE, TIME;
        java.util.Date DELIVERY, DOCDATE;
        Double QTY, PRICE;
        Double getMoney, getBackward, getBacktype;

        DateFormat df = new SimpleDateFormat("ddMMyyyy_hhmmss");

        String curdate = df.format(Calendar.getInstance().getTime());

        String FileName = CurAc.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/orders_" + curdate + ".dbf";
        DBF_FileName = "orders_" + curdate + ".dbf";

        c = db.getReadableDatabase().rawQuery("SELECT TORG_PRED.CODE AS TP, CONTRS.CODE AS CONTR, ADDRS.CODE AS ADDR, ZAKAZY.DOCNO, ZAKAZY.DELIVERY_DATE, ZAKAZY.DOC_DATE , ZAKAZY.COMMENT, ZAKAZY_DT.CODE, ZAKAZY_DT.COD5, ZAKAZY_DT.DESCR, ZAKAZY_DT.QTY, ZAKAZY_DT.PRICE, ZAKAZY.DELIV_TIME, ZAKAZY.GETMONEY, ZAKAZY.GETBACKWARD, ZAKAZY.BACKTYPE FROM ZAKAZY JOIN TORG_PRED ON ZAKAZY.TP_ID = TORG_PRED.ID JOIN CONTRS ON ZAKAZY.CONTR_ID = CONTRS.ID JOIN ADDRS ON ZAKAZY.ADDR_ID = ADDRS.ID JOIN ZAKAZY_DT ON ZAKAZY.DOCNO = ZAKAZY_DT.ZAKAZ_ID WHERE ZAKAZY.DOCNO='" + ID + "'", null);
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
        DBFField[] fields = new DBFField[14];

        fields[0] = new DBFField();
        fields[0].setName("TP");
        fields[0].setDataType(DBFField.FIELD_TYPE_C);
        fields[0].setFieldLength(13);

        fields[1] = new DBFField();
        fields[1].setName("CONTR");
        fields[1].setDataType(DBFField.FIELD_TYPE_C);
        fields[1].setFieldLength(13);

        fields[2] = new DBFField();
        fields[2].setName("ADDR");
        fields[2].setDataType(DBFField.FIELD_TYPE_C);
        fields[2].setFieldLength(13);

        fields[3] = new DBFField();
        fields[3].setName("DOCNO");
        fields[3].setDataType(DBFField.FIELD_TYPE_C);
        fields[3].setFieldLength(13);

        fields[4] = new DBFField();
        fields[4].setName("DELIVERY");
        fields[4].setDataType(DBFField.FIELD_TYPE_D);

        fields[5] = new DBFField();
        fields[5].setName("DOCDATE");
        fields[5].setDataType(DBFField.FIELD_TYPE_D);

        fields[6] = new DBFField();
        fields[6].setName("COMMENT");
        fields[6].setDataType(DBFField.FIELD_TYPE_C);
        fields[6].setFieldLength(255);

        fields[7] = new DBFField();
        fields[7].setName("CODE");
        fields[7].setDataType(DBFField.FIELD_TYPE_C);
        fields[7].setFieldLength(13);

        fields[8] = new DBFField();
        fields[8].setName("QTY");
        fields[8].setDataType(DBFField.FIELD_TYPE_N);
        fields[8].setFieldLength(13);
        fields[8].setDecimalCount(0);

        fields[9] = new DBFField();
        fields[9].setName("PRICE");
        fields[9].setDataType(DBFField.FIELD_TYPE_N);
        fields[9].setFieldLength(16);
        fields[9].setDecimalCount(2);

        fields[10] = new DBFField();
        fields[10].setName("DELIV_TIME");
        fields[10].setDataType(DBFField.FIELD_TYPE_C);
        fields[10].setFieldLength(13);

        fields[11] = new DBFField();
        fields[11].setName("GETMONEY");
        fields[11].setDataType(DBFField.FIELD_TYPE_N);
        fields[11].setFieldLength(13);
        fields[11].setDecimalCount(0);

        fields[12] = new DBFField();
        fields[12].setName("GETBACK");
        fields[12].setDataType(DBFField.FIELD_TYPE_N);
        fields[12].setFieldLength(13);
        fields[12].setDecimalCount(0);

        fields[13] = new DBFField();
        fields[13].setName("BACKTYPE");
        fields[13].setDataType(DBFField.FIELD_TYPE_N);
        fields[13].setFieldLength(13);
        fields[13].setDecimalCount(0);

        Table.setFields(fields);

        try {
            while (c.moveToNext()) {
                TP = c.getString(0);
                CONTR = c.getString(1);
                ADDR = c.getString(2);
                DOCNO = c.getString(3);
                DELIVERY = StrToDbfDate(c.getString(4));
                DOCDATE = StrToDbfDate(c.getString(5));

                COMMENT = c.getString(6);
                CODE = c.getString(7);
                QTY = c.getDouble(10);
                PRICE = c.getDouble(11);

                TIME = c.getString(12);
                getMoney = c.getDouble(13);
                getBackward = c.getDouble(14);
                getBacktype = c.getDouble(15);

                Object[] rowData = new Object[14];
                rowData[0] = TP;
                rowData[1] = CONTR;
                rowData[2] = ADDR;
                rowData[3] = DOCNO;
                rowData[4] = DELIVERY;
                rowData[5] = DOCDATE;
                rowData[6] = COMMENT;
                rowData[7] = CODE;
                rowData[8] = QTY;
                rowData[9] = PRICE;
                rowData[10] = TIME;
                rowData[11] = getMoney;
                rowData[12] = getBackward;
                rowData[13] = getBacktype;
                Table.addRecord(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Table.write();
        DBF_FIleForSend = FileName;
        if (isNetworkAvailable()) {
            new UploadDBFFile().execute();
        } else {
            Toast.makeText(CurAc, "Нет доступного интернет соединения", Toast.LENGTH_LONG).show();
        }
        return DBF_FileName;
    }

    public void LoadOrders(String Bdate, String Edate) {
        gdOrders.setAdapter(null);
        Orders = db.getZakazy(Bdate, Edate);
        OrdersAdapter = new JournalAdapter(CurAc, R.layout.orders_item, Orders, new String[]{"DOCNO", "STATUS", "DOC_DATE", "CONTR", "ADDR", "SUM", "DELIVERY_DATE"}, new int[]{R.id.ColOrdDocNo, R.id.ColOrdStatus, R.id.ColOrdDocDate, R.id.ColOrdContr, R.id.ColOrdAddr, R.id.ColOrdSum, R.id.ColOrdDeliveryDate}, 0);
        gdOrders.setAdapter(OrdersAdapter);
    }

    public void LoadOrdersDetails(String ZakazID) {
        orderdtList.setAdapter(null);
        OrdersDt = db.getZakazDetails(ZakazID);
        OrdersDtAdapter = new JournalDetailsAdapter(CurAc, R.layout.orderdt_item, OrdersDt, new String[]{"ZAKAZ_ID", "NOM_ID", "COD5", "DESCR", "QTY", "PRICE", "SUM"}, new int[]{R.id.ColOrdDtZakazID, R.id.ColOrdDtID, R.id.ColOrdDtCod, R.id.ColOrdDtDescr, R.id.ColOrdDtQty, R.id.ColOrdDtPrice, R.id.ColOrdDtSum}, 0);
        orderdtList.setAdapter(OrdersDtAdapter);
        orderdtList.setOnItemClickListener(OrderDtNomenClick);
    }

    public String ReadLastUpdate() {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = glbContext.getSharedPreferences("update_settings", 0);
        return settings.getString("Last_date", "");
    }

    public void LoadOrdersForSend() {
        orderList.setAdapter(null);
        Orders = db.getZakazyForSend();
        SendOrdersAdapter adapter;
        adapter = new SendOrdersAdapter(CurAc, R.layout.orders_item, Orders, new String[]{"DOCNO", "STATUS", "DOC_DATE", "CONTR", "ADDR", "SUM"}, new int[]{R.id.ColOrdDocNo, R.id.ColOrdStatus, R.id.ColOrdDocDate, R.id.ColOrdContr, R.id.ColOrdAddr, R.id.ColOrdSum}, 0);
        orderList.setAdapter(adapter);
    }

    public void SendOrders() throws DBFException {
        OrderID = "";
        Cursor c;
        String TP, CONTR, ADDR, DOCNO, COMMENT, CODE, TIME;
        java.util.Date DELIVERY, DOCDATE;
        Double QTY, PRICE;
        Double getMoney, getBackward, getBacktype;
        DateFormat df = new SimpleDateFormat("ddMMyyyy_hhmmss");

        String curdate = df.format(Calendar.getInstance().getTime());

        String FileName = CurAc.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/orders_" + curdate + ".dbf";
        DBF_FileName = "orders_" + curdate + ".dbf";

        c = db.getReadableDatabase().rawQuery("SELECT TORG_PRED.CODE AS TP, CONTRS.CODE AS CONTR, ADDRS.CODE AS ADDR, ZAKAZY.DOCNO, ZAKAZY.DELIVERY_DATE, ZAKAZY.DOC_DATE , ZAKAZY.COMMENT, ZAKAZY_DT.CODE, ZAKAZY_DT.COD5, ZAKAZY_DT.DESCR, ZAKAZY_DT.QTY, ZAKAZY_DT.PRICE, ZAKAZY.DELIV_TIME, ZAKAZY.GETMONEY, ZAKAZY.GETBACKWARD, ZAKAZY.BACKTYPE FROM ZAKAZY JOIN TORG_PRED ON ZAKAZY.TP_ID = TORG_PRED.ID JOIN CONTRS ON ZAKAZY.CONTR_ID = CONTRS.ID JOIN ADDRS ON ZAKAZY.ADDR_ID = ADDRS.ID JOIN ZAKAZY_DT ON ZAKAZY.DOCNO = ZAKAZY_DT.ZAKAZ_ID WHERE ZAKAZY.STATUS=0", null);
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
        DBFField[] fields = new DBFField[14];

        fields[0] = new DBFField();
        fields[0].setName("TP");
        fields[0].setDataType(DBFField.FIELD_TYPE_C);
        fields[0].setFieldLength(13);

        fields[1] = new DBFField();
        fields[1].setName("CONTR");
        fields[1].setDataType(DBFField.FIELD_TYPE_C);
        fields[1].setFieldLength(13);

        fields[2] = new DBFField();
        fields[2].setName("ADDR");
        fields[2].setDataType(DBFField.FIELD_TYPE_C);
        fields[2].setFieldLength(13);

        fields[3] = new DBFField();
        fields[3].setName("DOCNO");
        fields[3].setDataType(DBFField.FIELD_TYPE_C);
        fields[3].setFieldLength(13);

        fields[4] = new DBFField();
        fields[4].setName("DELIVERY");
        fields[4].setDataType(DBFField.FIELD_TYPE_D);

        fields[5] = new DBFField();
        fields[5].setName("DOCDATE");
        fields[5].setDataType(DBFField.FIELD_TYPE_D);

        fields[6] = new DBFField();
        fields[6].setName("COMMENT");
        fields[6].setDataType(DBFField.FIELD_TYPE_C);
        fields[6].setFieldLength(255);

        fields[7] = new DBFField();
        fields[7].setName("CODE");
        fields[7].setDataType(DBFField.FIELD_TYPE_C);
        fields[7].setFieldLength(13);

        fields[8] = new DBFField();
        fields[8].setName("QTY");
        fields[8].setDataType(DBFField.FIELD_TYPE_N);
        fields[8].setFieldLength(13);
        fields[8].setDecimalCount(0);

        fields[9] = new DBFField();
        fields[9].setName("PRICE");
        fields[9].setDataType(DBFField.FIELD_TYPE_N);
        fields[9].setFieldLength(12);
        fields[9].setDecimalCount(2);

        fields[10] = new DBFField();
        fields[10].setName("DELIV_TIME");
        fields[10].setDataType(DBFField.FIELD_TYPE_C);
        fields[10].setFieldLength(13);

        fields[11] = new DBFField();
        fields[11].setName("GETMONEY");
        fields[11].setDataType(DBFField.FIELD_TYPE_N);
        fields[11].setFieldLength(13);
        fields[11].setDecimalCount(0);

        fields[12] = new DBFField();
        fields[12].setName("GETBACK");
        fields[12].setDataType(DBFField.FIELD_TYPE_N);
        fields[12].setFieldLength(13);
        fields[12].setDecimalCount(0);

        fields[13] = new DBFField();
        fields[13].setName("BACKTYPE");
        fields[13].setDataType(DBFField.FIELD_TYPE_N);
        fields[13].setFieldLength(13);
        fields[13].setDecimalCount(0);
        Table.setFields(fields);

        try {
            while (c.moveToNext()) {
                TP = c.getString(0);
                CONTR = c.getString(1);
                ADDR = c.getString(2);
                DOCNO = c.getString(3);
                DELIVERY = StrToDbfDate(c.getString(4));
                DOCDATE = StrToDbfDate(c.getString(5));

                COMMENT = c.getString(6);
                CODE = c.getString(7);
                QTY = c.getDouble(10);
                PRICE = c.getDouble(11);
                TIME = c.getString(12);
                getMoney = c.getDouble(13);
                getBackward = c.getDouble(14);
                getBacktype = c.getDouble(15);

                Object[] rowData = new Object[14];
                rowData[0] = TP;
                rowData[1] = CONTR;
                rowData[2] = ADDR;
                rowData[3] = DOCNO;
                rowData[4] = DELIVERY;
                rowData[5] = DOCDATE;
                rowData[6] = COMMENT;
                rowData[7] = CODE;
                rowData[8] = QTY;
                rowData[9] = PRICE;
                rowData[10] = TIME;
                rowData[11] = getMoney;
                rowData[12] = getBackward;
                rowData[13] = getBacktype;
                Table.addRecord(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Table.write();
        DBF_FIleForSend = FileName;
        if (isNetworkAvailable()) {
            new UploadDBFFile().execute();
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
        DebetAdapter adapter;
        adapter = new DebetAdapter(CurAc, R.layout.debet_layout, curDebet, new String[]{"DESCR", "STATUS", "KREDIT", "SALDO", "A7", "A14", "A21", "A28", "A35", "A42", "A49", "A56", "A63", "A64", "OTG30", "OPL30", "KOB", "FIRMA", "CRT_DATE"}, new int[]{R.id.ColDebetContr, R.id.ColDebetStatus, R.id.ColDebetCredit, R.id.ColDebetDolg, R.id.ColDebetA7, R.id.ColDebetA14, R.id.ColDebetA21, R.id.ColDebetA28, R.id.ColDebetA35, R.id.ColDebetA42, R.id.ColDebetA49, R.id.ColDebetA56, R.id.ColDebetA63, R.id.ColDebetA64, R.id.ColDebetOTG30, R.id.ColDebetOPL30, R.id.ColDebetKOB, R.id.ColDebetFirma, R.id.ColDebetDogovor}, 0);
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
        adapter = new DebetContrsAdapter(CurAc, R.layout.contr_layout, curDebetContr, new String[]{"ID", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
        spContrDeb.setAdapter(adapter);
    }

    public void LoadTpListDeb() {
        curDebetTp = db.getTpList();
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(CurAc, R.layout.tp_layout, curDebetTp, new String[]{"_id", "ID", "DESCR"}, new int[]{R.id.ColTP_ROWID, R.id.ColTPID, R.id.ColTPDescr});
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

        FTPClient ftpClient = null;
        ftpClient = new FTPClient();
//        String FileName = GetSDCardpath()+UpdatesFolder+"/ver.txt";
        String FileName = CurAc.getFilesDir() + "/ver.txt";

        File appFile = new File(FileName);
        String response = "";
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

    public String[] GetLastVersionLocal() {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = CurAc.getSharedPreferences("apk_version", 0);
        editor = settings.edit();

        FTPClient ftpClient;
        ftpClient = new FTPClient();
        String FileName = CurAc.getFilesDir() + "/ver.txt";
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

    public void LoadSms(String Bdate, String Edate) {
        smsList.setAdapter(null);
        cur_sms = SmsDB.rawQuery("SELECT ROW_ID AS _id, MSG_HEAD, MESSAGE, MSG_DATE, MSG_TIME, IS_NEW FROM MSGS" +
                        " WHERE\n" +
                        " IS_NEW = 1 OR \n" +
                        "  DATE(substr(MSGS.MSG_DATE, 7, 4) || '-' || substr(MSGS.MSG_DATE, 4, 2) || '-' || substr(MSGS.MSG_DATE, 1, 2))\n" +
                        "  BETWEEN DATE(substr('" + Bdate + "', 7, 4) || '-' || substr('" + Bdate + "', 4, 2) || '-' || substr('" + Bdate + "', 1, 2)) AND DATE(substr('" + Edate + "', 7, 4) || '-' || substr('" + Edate + "', 4, 2) || '-' || substr('" + Edate + "', 1, 2)) ORDER BY ROW_ID"
                , null);
        if (cur_sms.moveToNext()) {
            SMSadapter = new SMSAdapter(CurAc, R.layout.sms_item, cur_sms, new String[]{"_id", "MSG_DATE", "MSG_TIME", "MSG_HEAD", "MESSAGE"}, new int[]{R.id.ColSMSID, R.id.ColSMSDate, R.id.ColSMSTime, R.id.ColSMSHead, R.id.ColSMSMessage}, 0);
            smsList.setAdapter(SMSadapter);
        }
    }

    public Integer getSMSCount() {
        String count = "";
        Cursor cur_sms = SmsDB.rawQuery("SELECT CASE WHEN COUNT(ROW_ID) IS NULL THEN '0' ELSE COUNT(ROW_ID) END  AS CNT FROM MSGS WHERE IS_NEW=1", null);
        if (cur_sms.moveToNext()) {
            count = cur_sms.getString(cur_sms.getColumnIndex("CNT"));
        }
        return count.equals("") ? 0 : Integer.parseInt(count);
    }

    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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

    public Integer getLastSmsId() {
        Integer lastId = 0;
        Cursor cursor = SmsDB.rawQuery("SELECT CASE WHEN MAX([ROW]) IS NULL THEN 0 ELSE MAX([ROW]) END AS [ROW] FROM MSGS", null);
        if (cursor.moveToNext()) {
            lastId = cursor.getInt(cursor.getColumnIndex("ROW"));
        }
        return lastId;
    }

    public void getNewSMS() {
        int Rowid;
        int msgRow;
        String TP_ID, TP_IDS, MSG_HEAD, MSG, MSG_DATE, MSG_TIME;
        Statement stmt;
        ResultSet reset = null;

        if (isNetworkAvailable()) {

            if (SmsDB == null) {
                SmsDB = openOrCreateDatabase(GetStoragePath2019() + DBFolder + "/armtp_msg.db", MODE_WORLD_WRITEABLE, null);
            }

            SmsDB.execSQL("CREATE TABLE IF NOT EXISTS MSGS (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, [ROW] INTEGER, TP_ID TEXT, TP_IDS TEXT, MSG_HEAD TEXT, MESSAGE TEXT,MSG_DATE TEXT, MSG_TIME TEXT, IS_NEW INTEGER DEFAULT 1)");

            Rowid = getLastSmsId();

            if (conn == null) {
                ConnectToSql();
            }

            String sql_insert = "INSERT INTO MSGS([ROW],TP_ID,TP_IDS,MSG_HEAD,MESSAGE,MSG_DATE,MSG_TIME) VALUES(?,?,?,?,?,?,?);";
            SQLiteStatement statement = SmsDB.compileStatement(sql_insert);

            SmsDB.beginTransactionNonExclusive();

            try {

                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                reset = stmt.executeQuery("SELECT [ROWID],[TP_ID],[TP_IDS],[MSG_HEAD],[MESSAGE],[MSG_DATE],[MSG_TIME] FROM [ARM_MESSAGES] WHERE ROWID>" + Rowid);
                while (reset.next()) {
                    msgRow = reset.getInt(1);
                    TP_ID = reset.getString(2);
                    TP_IDS = reset.getString(3);
                    MSG_HEAD = reset.getString(4);
                    MSG = reset.getString(5);
                    MSG_DATE = reset.getString(6);
                    MSG_TIME = reset.getString(7);
                    statement.clearBindings();
                    statement.bindLong(1, msgRow);
                    statement.bindString(2, TP_ID);
                    statement.bindString(3, TP_IDS);
                    statement.bindString(4, MSG_HEAD);
                    statement.bindString(5, MSG);
                    statement.bindString(6, MSG_DATE);
                    statement.bindString(7, MSG_TIME);
                    statement.executeInsert();
                    statement.clearBindings();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                SmsDB.setTransactionSuccessful();
                SmsDB.endTransaction();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            String currentDate = sdf.format(new Date());
            LoadSms(currentDate, currentDate);

            if (SMSadapter != null) {
                SMSadapter.notifyDataSetChanged();
                cur_sms.requery();
            }

            String count = "";
            Cursor cur_sms = SmsDB.rawQuery("SELECT CASE WHEN COUNT([ROW]) IS NULL THEN '0' ELSE COUNT([ROW]) END  AS CNT FROM MSGS WHERE IS_NEW=1", null);
            if (cur_sms.moveToNext()) {
                count = cur_sms.getString(cur_sms.getColumnIndex("CNT"));
                cur_sms.close();
            }

            // Отключено 18-08-2016 для проверки работы на Android SDK 24/6
            ShortcutBadger.applyCount(getApplicationContext(), count.equals("") ? 0 : Integer.parseInt(count));
            try {
                if (reset != null) {
                    reset.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.close();
                }
                conn = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void ConnectToSql() {
        String connString;
        String sql_server;
        String sql_port;
        String sql_db;
        String sql_loging;
        String sql_pass;

        sql_server = glbContext.getResources().getString(R.string.sql_server);
        sql_port = glbContext.getResources().getString(R.string.sql_port);
        sql_db = glbContext.getResources().getString(R.string.sql_db);
        sql_loging = glbContext.getResources().getString(R.string.sql_user);
        sql_pass = glbContext.getResources().getString(R.string.sql_pass);

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();

            connString = "jdb" + "c:jtds:sqlserver://" + sql_server + ":" + sql_port + ";instance=MSSQLSERVER;databaseName=" + sql_db + ";user=" + sql_loging + ";password=" + sql_pass;
            conn = DriverManager.getConnection(connString, sql_loging, sql_pass);
            if (conn != null) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSaleIcon(Menu menu, int MenuItem, Boolean vis) {
        Drawable drawable = menu.getItem(MenuItem).getIcon();
        drawable.mutate();
        if (vis) {
            drawable.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
        } else {
            drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
        isSales = vis;
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

        SaleDlg
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RadioGroup radiogroup = SaleMarkupView.findViewById(R.id.radioGroup);
                        int selectedId = radiogroup.getCheckedRadioButtonId();
                        RadioButton radioButton = SaleMarkupView.findViewById(selectedId);
                        String perc = "0";

                        if (radioButton.getText().equals("Скидка")) {
                            perc = edPercent.getText().toString().equals("") ? "0" : edPercent.getText().toString();
                            Discount = Integer.valueOf(perc);
                        } else if (radioButton.getText().equals("Наценка")) {
                            perc = "-" + (edPercent.getText().toString().equals("") ? "0" : edPercent.getText().toString());
                            Discount = Integer.valueOf(perc);
                            isDiscount = true;
                        } else {
                            Discount = 0;
                            isDiscount = false;
                        }

                        if (WhichView == 0) {
                            if (Integer.parseInt(perc) == 0) {
                                isDiscount = false;
                                setDiscountIcon(menu, 2, false);
                            } else {
                                isDiscount = true;
                                setDiscountIcon(menu, 2, true);
                            }

                            if (NomenAdapter != null) {
                                myNom.requery();
                                NomenAdapter.notifyDataSetChanged();
                            }
                        }

                        if (WhichView == 1) {
                            if (Integer.parseInt(perc) == 0) {
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

    public File getPhotoDirFile() {
        File photo_dir;
        File file = CurAc.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        File extPhoto, arm_photo = null;

        extPhoto = new File(file.toString());

        if (extPhoto.canWrite()) {
            arm_photo = new File(extPhoto.toString());
            if (!arm_photo.exists()) {
                arm_photo.mkdir();
            }
        }
        photo_dir = arm_photo;
        return photo_dir;
    }

    public void checkPhotoInDB(String FileName) {
        Cursor cur;
        String isDownloaded = FilenameUtils.removeExtension(FileName);
        String tmpName = isDownloaded.substring(isDownloaded.length() - 2);
        String Sql;

        if (tmpName.equals("_2")) {
            Sql = "SELECT P2D From Nomen WHERE COD='" + isDownloaded.replace(tmpName, "") + "'";
            cur = db.getWritableDatabase().rawQuery(Sql, null);
        } else {
            Sql = "SELECT P1D From Nomen WHERE COD='" + isDownloaded + "'";
            cur = db.getWritableDatabase().rawQuery(Sql, null);
        }

        if (cur.moveToFirst()) {
            if (cur.getInt(0) == 0) {
                if (tmpName.equals("_2")) {
                    db.getWritableDatabase().execSQL("UPDATE Nomen SET P2D=1 WHERE COD='" + isDownloaded.replace(tmpName, "") + "'");
                } else {
                    db.getWritableDatabase().execSQL("UPDATE Nomen SET P1D=1 WHERE COD='" + isDownloaded + "'");
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

            if (!cursor.getString(3).equals("")) {
                tvDescr.setText(cursor.getString(2) + " / " + cursor.getString(3));
            } else {
                tvDescr.setText(cursor.getString(2));
            }

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
            if (!cursor.getString(3).equals("")) {
                tvDescr.setText(cursor.getString(2) + " / " + cursor.getString(3));
            } else {
                tvDescr.setText(cursor.getString(2));
            }

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

            String contrInfo = cursor.getString(6);
            String resDescr = cursor.getString(2);

            if (position % 2 == 0) {
                tvDescr.setBackgroundColor(Color.rgb(201, 235, 255));
            } else {
                tvDescr.setBackgroundColor(Color.rgb(255, 255, 255));
            }

            if (!contrInfo.equals("")) {
                resDescr += " (" + contrInfo + ")";
            }

            tvDescr.setText(resDescr);
            return view;
        }
    }

    public class UniFilterAdatper extends SimpleCursorAdapter {
        public UniFilterAdatper(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {

            View view = super.getView(position, convertView, parent);
            return view;
        }
    }

    public class MyCursorAdapter extends SimpleCursorAdapter {
        public MyCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            int resID;

            final Cursor cursor = getCursor();
            View view = super.getView(position, convertView, parent);
            TextView tvDescr = view.findViewById(R.id.ColNomDescr);
            TextView tvMH = view.findViewById(R.id.ColNomMH);
            TextView tvPrice = view.findViewById(R.id.ColNomPrice);
            TextView tvPosition = view.findViewById(R.id.ColNomPosition);

            final TextView tvPhoto = view.findViewById(R.id.ColNomPhoto);
            final Button btPlus = view.findViewById(R.id.btPlus);
            final Button btMinus = view.findViewById(R.id.btMinus);

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

            if (isDiscount && Discount != 0) {
                tvPrice.setText(String.format("%.2f", cursor.getDouble(5) - cursor.getDouble(5) * Discount / 100));
            }

            if (!cursor.getString(9).equals("")) {
                if (cursor.getInt(18) == 1) {
                    if (!cursor.getString(10).equals("")) {
                        if (cursor.getInt(19) == 1) {
                            resID = glbContext.getResources().getIdentifier("photo_blue", "drawable", glbContext.getPackageName());
                        } else {
                            resID = glbContext.getResources().getIdentifier("photo_green_blue", "drawable", glbContext.getPackageName());
                        }
                    } else {
                        resID = glbContext.getResources().getIdentifier("photo_green", "drawable", glbContext.getPackageName());
                    }
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

            if (cursor.getInt(6) != 0) {
                view.setBackgroundColor(Color.rgb(212, 236, 187));
            }

            if (cursor.getInt(12) == 1) {
                tvDescr.setTextColor(Color.rgb(255, 0, 0));
            } else {
                if (cursor.getInt(13) == 1) {
                    tvDescr.setTextColor(Color.rgb(0, 30, 255));
                } else {
                    if (cursor.getInt(14) == 1) {
                        tvDescr.setTextColor(Color.rgb(32, 131, 34));
                    } else {
                        tvDescr.setTextColor(Color.rgb(0, 0, 0));
                    }
                }
            }

            if (cursor.getInt(16) == 1) {
                tvMH.setText("ПРМ");
            }

            tvPosition.setText(String.valueOf(position + 1));

            return view;
        }
    }

    public class JournalAdapter extends SimpleCursorAdapter {
        Cursor cursor;

        public JournalAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            cursor = c;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = super.getView(position, convertView, parent);

            if (Orders.getString(4).equals("Удален")) {
                view.setBackgroundColor(Color.rgb(253, 210, 210));
            } else {
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.rgb(201, 235, 255));
                } else {
                    view.setBackgroundColor(Color.rgb(255, 255, 255));
                }
            }
            return view;
        }
    }

    public class JournalDetailsAdapter extends SimpleCursorAdapter {
        public JournalDetailsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            int resID = glbContext.getResources().getIdentifier("photo_free", "drawable", glbContext.getPackageName());
            Cursor cursor = getCursor();
            View view = super.getView(position, convertView, parent);
            final TextView tvPhoto = view.findViewById(R.id.ColNomPhoto);
            TextView tvDescr = view.findViewById(R.id.ColNomDescr);
            TextView tvQty = view.findViewById(R.id.ColOrdDtQty);

            if (tvPhoto != null && !cursor.getString(9).equals("")) {
                if (cursor.getInt(18) == 1) {
                    if (!cursor.getString(10).equals("")) {
                        if (cursor.getInt(19) == 1) {
                            resID = glbContext.getResources().getIdentifier("photo_blue", "drawable", glbContext.getPackageName());
                        } else {
                            resID = glbContext.getResources().getIdentifier("photo_green_blue", "drawable", glbContext.getPackageName());
                        }
                    } else {
                        resID = glbContext.getResources().getIdentifier("photo_green", "drawable", glbContext.getPackageName());
                    }
                } else {
                    resID = glbContext.getResources().getIdentifier("photo2", "drawable", glbContext.getPackageName());
                }

                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append(" ").append(" ");
                builder.setSpan(new ImageSpan(glbContext, resID), builder.length() - 1, builder.length(), 0);
                builder.append(" ");
                Log.d("xd1", String.valueOf(builder));
                tvPhoto.setText(builder);
            }

            if (position % 2 == 0) {
                view.setBackgroundColor(Color.rgb(201, 235, 255));
            } else {
                view.setBackgroundColor(Color.rgb(255, 255, 255));
            }

            if (cursor.getInt(6) != 0) {
                view.setBackgroundColor(Color.rgb(212, 236, 187));
            }

            if (cursor.getInt(8) == 1) {
                tvQty.setText(cursor.getInt(5) + "(-" + cursor.getInt(9) + ")");
            }

            return view;
        }
    }

    public class ContrsAdapter extends SimpleCursorAdapter {
        public ContrsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Cursor cursor = getCursor();
            String contrInfo = cursor.getString(6);
            String resDescr = cursor.getString(2);

            TextView tvDescr = view.findViewById(R.id.ColContrDescr);
            if (position % 2 == 0) {
                tvDescr.setBackgroundColor(Color.rgb(201, 235, 255));
            } else {
                tvDescr.setBackgroundColor(Color.rgb(255, 255, 255));
            }

            if (!contrInfo.equals("")) {
                resDescr += " (" + contrInfo + ")";
            }

            tvDescr.setText(resDescr);
            return view;
        }
    }

    public class SendOrdersAdapter extends SimpleCursorAdapter {
        Cursor cursor;

        public SendOrdersAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            cursor = c;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = super.getView(position, convertView, parent);

            if (position % 2 == 0) {
                view.setBackgroundColor(Color.rgb(201, 235, 255));
            } else {
                view.setBackgroundColor(Color.rgb(255, 255, 255));
            }
            return view;
        }
    }

    public class UploadDBFFile extends AsyncTask<String, Integer, String> {
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

        protected String doInBackground(String... urls) {
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
                ftpClient.changeWorkingDirectory("newARM");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                InputStream inputStream;

                File secondLocalFile = new File(DBF_FIleForSend);
                String secondRemoteFile = DBF_FileName;
                inputStream = new FileInputStream(secondLocalFile);

                OutputStream outputStream = ftpClient.storeFileStream(secondRemoteFile);
                byte[] bytesIn = new byte[4096];
                int read = 0;

                while ((read = inputStream.read(bytesIn)) != -1) {
                    outputStream.write(bytesIn, 0, read);
                }

                inputStream.close();
                outputStream.close();

                boolean completed = ftpClient.completePendingCommand();
                if (completed) {
                    ret_completed = true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressDialog.setProgress(progress[0]);
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (ret_completed) {
                Toast.makeText(CurAc, "Заказы успешно сформированы в файл и отправлены", Toast.LENGTH_LONG).show();
                orderList.setAdapter(null);
                db.SetZakazStatus(1, 0);
            }
        }
    }

    public class DebetAdapter extends SimpleCursorAdapter {

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

    public class SMSAdapter extends SimpleCursorAdapter {
        public SMSAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            if (position % 2 == 0) {
                view.setBackgroundColor(Color.rgb(201, 235, 255));
            } else {
                view.setBackgroundColor(Color.rgb(255, 255, 255));
            }

            Cursor cursor = getCursor();

            Button btMarkAsRead = view.findViewById(R.id.btMarkAsRead);
            if (cursor.getInt(5) == 0) {
                btMarkAsRead.setVisibility(View.INVISIBLE);
            } else {
                btMarkAsRead.setVisibility(View.VISIBLE);
            }
            return view;
        }
    }
}
