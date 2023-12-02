package com.amber.armtp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.amber.armtp.annotations.AsyncUI;
import com.amber.armtp.annotations.DelayedCalled;
import com.amber.armtp.annotations.PGShowing;
import com.amber.armtp.dbHelpers.DBAppHelper;
import com.amber.armtp.dbHelpers.DBHelper;
import com.amber.armtp.dbHelpers.DBOrdersHelper;
import com.amber.armtp.ftp.Ftp;
import com.amber.armtp.interfaces.BackupServerConnection;
import com.amber.armtp.interfaces.TBUpdate;
import com.amber.armtp.ui.FormOrderFragment;
import com.amber.armtp.ui.OrderHeadFragment;
import com.amber.armtp.ui.SettingFragment;
import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFWriter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

/**
 * Created by filimonov on 22-08-2016.
 * Updated by domster704 on 27.09.2021
 */
public class GlobalVars extends Application implements TBUpdate, BackupServerConnection {
    public static ArrayList<ChosenOrdersData> allOrders = new ArrayList<>();

    public static FragmentActivity CurAc;
    public static Context CurFragmentContext;
    public static String CurSGI = "0", CurGroup = "0", CurWCID = "0", CurFocusID = "0", CurSearchName = "";
    public static String CurContr = "0";
    public static String TypeOfPrice = "";

    public int CurVisiblePosition = 0;

    public Context glbContext;
    public Cursor myNom = null, mySgi, myGroup, Orders, OrdersDt;
    public Cursor myWC = null, myFocus = null;
    public Cursor myContr;
    public Cursor myAddress;
    public Cursor myTP;
    public Cursor curDebet;

    public GlobalVars.NomenAdapter NomenAdapter, PreviewZakazAdapter;
    public JournalAdapter OrdersAdapter;
    public JournalDetailsAdapter OrdersDtAdapter;

    public View CurView;

    public DBHelper db;
    public DBOrdersHelper dbOrders;
    public DBAppHelper dbApp;

    public boolean isDiscount = false;
    public boolean isMultiSelect = false;
    public boolean isSales = false;
    public boolean isNeededToBeLoadingBySgi = true;

    public float Discount = 0;
    public int MultiQty = 0;

    public GridView nomenList;
    public GridView gdOrders;
    public GridView orderDtList;
    public GridView debetList;

    //    public android.support.v7.widget.Toolbar toolbar;
    public Toolbar toolbar;
    public LinearLayout layout;

    public Spinner spSgi, spGroup;
    public Spinner spWC, spFocus;

    public AlertDialog alertPhoto = null;
    public PopupMenu nomPopupMenu;
    public String OrderID = "";
    public String CurrentTp;

    public Spinner spinContr, spinAddress, TPList;
    public Calendar DeliveryDate;
    public EditText txtDate;
    public EditText edContrFilter;
    public EditText txtComment;
    public TextView spTp, spContr, spAddress;
    public ViewFlipper viewFlipper;
    public String ordStatus;
    public int BeginPos = 0, EndPos = 0;

    public static Thread downloadPhotoTread;
    public boolean isNeededToResetSearchView = true;

    @SuppressLint("StaticFieldLeak")
    public volatile static ProgressBarLoading currentPB;

    private static boolean isNeededToSelectRowAfterGoToGroup = false;
    public static String kod5 = "";

    private final AdapterView.OnItemSelectedListener SelectedContr = new AdapterView.OnItemSelectedListener() {
        private TextView descriptionContr = null;
        private TextView descriptionContrHeader = null;
        private LinearLayout descriptionContrLayout = null;
        private ImageButton contrInfoButton;

        @Override
        public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            init();
            descriptionContr.setText("");

            String[] descriptionContrList = new String[]{"нет данных", "0.00 ₽"};
            String ItemID = myContr.getString(myContr.getColumnIndex("CODE"));
            CurContr = ItemID;
            if (!ItemID.equals("0") && !OrderHeadFragment.isOrderEditedOrCopied) {
                LoadContrAddress(ItemID);
            }

            String[] data = db.getDebetInfoByContrID(ItemID);
            String status = myContr.getString(myContr.getColumnIndex("STATUS"));
            String debt = data[0];

            if (!status.equals("")) {
                descriptionContrList[0] = status;
            }
            if (!debt.equals("")) {
                descriptionContrList[1] = String.format(Locale.ROOT, "%.2f", Float.parseFloat(debt.replace(",", "."))) + " ₽";
            }

            descriptionContr.setText(String.join(" | ", descriptionContrList));

            if (!ItemID.equals("0")) {
                descriptionContrLayout.setVisibility(View.VISIBLE);
                contrInfoButton.setVisibility(View.VISIBLE);
                changeStatusColor(descriptionContr, descriptionContrHeader, ItemID);
            } else {
                descriptionContrLayout.setVisibility(View.GONE);
                contrInfoButton.setVisibility(View.INVISIBLE);
            }
        }

        private void changeStatusColor(TextView tvStatus, TextView tvStatusHeader, String contrId) {
            switch (db.getContrFlagByID(contrId)) {
                case 0:
                    tvStatus.setTextColor(getResources().getColor(R.color.orderStatusOk));
                    tvStatusHeader.setTextColor(getResources().getColor(R.color.orderStatusOk));
                    break;
                case 1:
                    tvStatus.setTextColor(getResources().getColor(R.color.orderStatusWarn));
                    tvStatusHeader.setTextColor(getResources().getColor(R.color.orderStatusWarn));
                    break;
                case 2:
                    tvStatus.setTextColor(getResources().getColor(R.color.orderStatusBad));
                    tvStatusHeader.setTextColor(getResources().getColor(R.color.orderStatusBad));
                    break;
            }
        }

        private void init() {
            descriptionContr = CurAc.findViewById(R.id.description);
            descriptionContrHeader = CurAc.findViewById(R.id.descriptionHeader);
            descriptionContrLayout = CurAc.findViewById(R.id.layoutOfContrStatus);
            contrInfoButton = CurAc.findViewById(R.id.userCardInfoButton);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };


    public AdapterView.OnItemClickListener GridNomenClick = (myAdapter, myView, position, mylng) -> {
        try {
            long viewId = myView.getId();

            if (viewId == R.id.ColNomPhoto) {
                showPhoto(myView, position, myAdapter);
            } else if (viewId == R.id.btPlus) {
                plusQTY(myView);
            } else if (viewId == R.id.btMinus) {
                minusQTY(myView);
            } else {
                TextView tvKOD5 = myView.findViewById(R.id.ColNomCod);
                TextView tvOST = myView.findViewById(R.id.ColNomOst);

                String ID = tvKOD5.getText().toString();
                int ost = Integer.parseInt(tvOST.getText().toString());

                if (isMultiSelect) {
                    multiSelect(ID, ost);
                } else {
                    fillNomenDataFromAlertDialog(ID, ost);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Config.sout(e);
        }
    };

    public AdapterView.OnLongClickListener PhotoLongClick = new AdapterView.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            try {
                TextView tvKod5 = ((RelativeLayout) view.getParent()).findViewById(R.id.ColNomCod);
                String kod5 = tvKod5.getText().toString();

                PopupMenu nomPopupMenu = new PopupMenu(glbContext, view);
                nomPopupMenu.getMenuInflater().inflate(R.menu.photo_context_menu, nomPopupMenu.getMenu());

                nomPopupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.forceDownloadPhoto) {
                        @SuppressLint({"NewApi", "LocalSuppress"}) String[] fileNames = db.getPhotoNames(kod5);
                        downloadAndShowPhotos(fileNames, kod5, true);
                    }
                    return true;
                });
                nomPopupMenu.show();
            } catch (Exception e) {
                e.printStackTrace();
                Config.sout(e);
            }
            return true;
        }
    };

    public AdapterView.OnItemLongClickListener GridNomenLongClick = new AdapterView.OnItemLongClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, final View myView, final int position, long arg3) {
            try {
                String group;
                String sgi;

                Cursor c = myNom;
                group = c.getString(c.getColumnIndex("GRUPPA"));
                sgi = c.getString(c.getColumnIndex("SGI"));

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
                            NomenAdapter.notifyDataSetChanged();
                            return true;
                        case R.id.setEndPos:
                            EndPos = position + 1;
                            NomenAdapter.notifyDataSetChanged();
                            return true;
                        case R.id.goToGroup:
                            isNeededToSelectRowAfterGoToGroup = true;
                            kod5 = c.getString(c.getColumnIndex("KOD5"));

                            if (spWC != null) {
                                spWC.setSelection(0);
                                spFocus.setSelection(0);
                            }
                            FormOrderFragment.filter.setImageResource(R.drawable.filter);
                            FormOrderFragment.isFiltered = false;

                            System.out.println(CurSGI + " " + sgi);
                            if (!CurSGI.equals(sgi)) {
                                GlobalVars.allowUpdate = false;
                            }
//                            resetAllSpinners();
                            resetCurData();
                            resetSearchViewData();

                            setSelectionByCodeSgiAsync(sgi);
                            new Handler().postDelayed(() -> setSelectionByCodeGroupAsync(group), 500);

                            return true;
                    }
                    return true;
                });
                nomPopupMenu.show();
            } catch (Exception e) {
                e.printStackTrace();
                Config.sout(e);
            }

            return true;
        }
    };

    public static boolean allowUpdate = true;
    public AdapterView.OnItemSelectedListener SelectedGroup = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            try {
                if (!FormOrderFragment.isCleared) {
                    resetSearchViewData();
                }
                FormOrderFragment.isCleared = false;

                CurGroup = myGroup.getString(myGroup.getColumnIndex("CODE"));

                if (allowUpdate) {
                    LoadNomen(CurSGI, CurGroup, CurWCID, CurFocusID, CurSearchName);
                }
                allowUpdate = true;

                FormOrderFragment.isSorted = false;
                FormOrderFragment.mainMenu.findItem(R.id.NomenSort).setIcon(R.drawable.to_end);
            } catch (Exception e) {
                e.printStackTrace();
                Config.sout(e);
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    public AdapterView.OnItemSelectedListener SelectedSgi = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
            System.out.println(1);
            try {
                if (isNeededToResetSearchView) {
                    resetSearchViewData();
                }
                isNeededToResetSearchView = true;
                String ItemID = mySgi.getString(mySgi.getColumnIndex("CODE"));

                CurGroup = "0";
                CurSGI = ItemID;

                if (ItemID.equals("0")) {
                    nomenList.setAdapter(null);
                    spGroup.setAdapter(null);
                }
                LoadGroups(ItemID);

                if (!CurWCID.equals("0") || !CurFocusID.equals("0") || !CurSearchName.equals("")) {
                    LoadNomen(CurSGI, CurGroup, CurWCID, CurFocusID, CurSearchName);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Config.sout(e);
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    public android.support.v4.app.FragmentManager fragManager;
    TextView groupID, sgiID;
    android.support.v4.app.Fragment fragment = null;
    android.support.v4.app.FragmentTransaction fragmentTransaction;
    public AdapterView.OnItemLongClickListener PreviewNomenLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, final View myView, int position, long arg3) {
            try {
                groupID = myView.findViewById(R.id.ColNomGRUPID);
                sgiID = myView.findViewById(R.id.ColNomSGIID);

                Cursor c = myNom;
                final String group = c.getString(c.getColumnIndex("GRUPPA"));
                final String sgi = c.getString(c.getColumnIndex("SGI"));

                nomPopupMenu = new PopupMenu(CurAc, myView);
                nomPopupMenu.getMenuInflater().inflate(R.menu.nomen_context_menu, nomPopupMenu.getMenu());
                nomPopupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.goToGroup) {
                        isNeededToSelectRowAfterGoToGroup = true;
                        kod5 = c.getString(c.getColumnIndex("KOD5"));

                        fragment = new FormOrderFragment();

                        Bundle args = new Bundle();
                        args.putString("SGI", sgi);
                        args.putString("Group", group);

                        fragment.setArguments(args);
                        fragmentTransaction = fragManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame, fragment, "frag_order_header");
                        fragmentTransaction.commit();

                        toolbar.setTitle("Формирование заказа");

                        return true;
                    }
                    return true;
                });
                nomPopupMenu.show();
            } catch (Exception e) {
                e.printStackTrace();
                Config.sout(e);
            }

            return true;
        }
    };

    public Context getContext() {
        return glbContext;
    }

    public void setContext(Context context) {
        glbContext = context;
    }

    //    @AsyncUI
    public void LoadSgi() {
        if (mySgi != null) {
            mySgi.close();
        }
        mySgi = db.getAllSgi();
        spSgi = CurView.findViewById(R.id.SpinSgi);
        android.widget.SimpleCursorAdapter adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.sgi_layout, mySgi, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColSgiID, R.id.ColSgiDescr}, 0);

        spSgi.setAdapter(adapter);
//        spSgi.post(() -> spSgi.setOnItemSelectedListener(SelectedSgi));
        spSgi.setOnItemSelectedListener(SelectedSgi);
    }

    //    @AsyncUI
    public void LoadGroups(String SgiID) {
        if (myGroup != null) {
            myGroup.close();
        }
        myGroup = db.getGroupsBySgi(SgiID);
        spGroup = CurView.findViewById(R.id.SpinGrups);
        android.widget.SimpleCursorAdapter adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.grup_layout, myGroup, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColGrupID, R.id.ColGrupDescr}, 0);

        spGroup.setAdapter(adapter);
//        spGroup.post(() -> spGroup.setOnItemSelectedListener(SelectedGroup));
        spGroup.setOnItemSelectedListener(SelectedGroup);
    }

    @AsyncUI
    public void LoadFiltersWC(View vw) {
        if (myWC != null) {
            myWC.close();
        }
        myWC = dbApp.getWCs();
        spWC = vw.findViewById(R.id.spinWC);
        android.widget.SimpleCursorAdapter adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.wc_layout, myWC, new String[]{"_id", "DEMP"}, new int[]{R.id.ColWCID, R.id.ColWCDescr}, 0);
        spWC.setAdapter(adapter);
    }

    @AsyncUI
    public void LoadFiltersFocus(View vw) {
        if (myFocus != null) {
            myFocus.close();
        }
        myFocus = db.getFocuses();
        spFocus = vw.findViewById(R.id.spinFocus);
        android.widget.SimpleCursorAdapter adapter;
        adapter = new android.widget.SimpleCursorAdapter(glbContext, R.layout.focus_layout, myFocus, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColFocusID, R.id.ColFocusDescr}, 0);
        spFocus.setAdapter(adapter);
    }

    private GlobalVars.NomenAdapter getNomenAdapter(Cursor cursor) {
        return new NomenAdapter(glbContext, R.layout.nomen_layout, cursor, new String[]{"_id", "KOD5", "DESCR", "OST", "ZAKAZ", "GRUPPA", "SGI", "FOTO", "GOFRA", "MP", GlobalVars.TypeOfPrice}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomPhoto, R.id.ColNomVkorob, R.id.ColNomMP, R.id.ColNomPrice}, 0);
    }

    public void LoadNomen(String... args) {
        String[] formattedArgs = new String[5];
        System.arraycopy(args, 0, formattedArgs, 0, args.length);
        for (int i = args.length; i < formattedArgs.length; i++) {
            formattedArgs[i] = "0";
        }

        CurSGI = formattedArgs[0];
        CurGroup = formattedArgs[1];
        CurWCID = formattedArgs[2];
        CurFocusID = formattedArgs[3];
        CurSearchName = formattedArgs[4].toLowerCase(Locale.ROOT);

        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                myNom = db.getNomen(
                        CurSGI, CurGroup,
                        CurWCID, CurFocusID, CurSearchName);
//                Config.printCursor(myNom);
                NomenAdapter = getNomenAdapter(myNom);
                CurAc.runOnUiThread(() -> {
                    nomenList.setAdapter(null);
                    nomenList.setAdapter(NomenAdapter);
                    nomenList.setOnItemClickListener(GridNomenClick);
                    nomenList.setOnItemLongClickListener(GridNomenLongClick);

                    // needs to call notifyDataSetChanged in NomenAdapter class
                    NomenAdapter.notifyDataSetChanged();
                    setPositionAfterGoToGroup();
                });


            }

            private void setPositionAfterGoToGroup() {
                System.out.println(CurSGI + " " + CurGroup);
                if (isNeededToSelectRowAfterGoToGroup) {
                    int[] elementPositionData = getPositionByKod5(kod5);
                    int elementPosition = elementPositionData[0];
                    System.out.println(elementPosition + " " + kod5);
                    int visibleElementsCount = nomenList.getLastVisiblePosition() - nomenList.getFirstVisiblePosition() + 1;
                    if (elementPosition == -1) {
                        elementPosition = 0;
                    } else if (elementPositionData[1] == 1) {
                        elementPosition += visibleElementsCount;
                    }
                    nomenList.setSelection(elementPosition);
                    isNeededToSelectRowAfterGoToGroup = false;
                    kod5 = "";
                }
            }

            private int[] getPositionByKod5(String kod5) {
                int i = 0;
                while (myNom.moveToNext()) {
                    if (myNom.getString(myNom.getColumnIndex("KOD5")).equals(kod5)) {
                        return new int[]{i, 1};
                    }
                    i++;
                }

                for (int j = 0; j <= nomenList.getLastVisiblePosition(); j++) {
                    RelativeLayout layout = (RelativeLayout) nomenList.getChildAt(j);
                    String localKod5 = ((TextView) layout.findViewById(R.id.ColNomCod)).getText().toString();
                    if (localKod5.equals(kod5)) {
                        return new int[]{j, 0};
                    }
                }
                return new int[]{-1, 0};
            }
        }).start();
    }

    //    @DelayedCalled()
    public void setSelectionByCodeSgiAsync(String sgiCode) {
        for (int i = 0; i < spSgi.getCount(); i++) {
            Cursor value = (Cursor) spSgi.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndex("CODE"));
            if (sgiCode.equals(id)) {
                int finalI = i;
                spSgi.post(() -> spSgi.setSelection(finalI));
                return;
            }
        }
    }

    public void setSelectionByCodeSgi(String sgiCode) {
        for (int i = 0; i < spSgi.getCount(); i++) {
            Cursor value = (Cursor) spSgi.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndex("CODE"));
            if (sgiCode.equals(id)) {
                spSgi.setSelection(i);
                return;
            }
        }
    }

    //    @DelayedCalled(delay = 100)
    public void setSelectionByCodeGroupAsync(String groupCode) {
        for (int i = 0; i < spGroup.getCount(); i++) {
            Cursor value = (Cursor) spGroup.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndex("CODE"));
            if (groupCode.equals(id)) {
                int finalI = i;
                spGroup.post(() -> spGroup.setSelection(finalI));
                return;
            }
        }
    }

    @DelayedCalled
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

    @DelayedCalled
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

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) glbContext.getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            return true;
        }
        Config.sout("Нет доступа к интернету");
        return false;
    }

    class PhotoDownloadingRunnable implements Runnable {
        private final String[] fileNames;
        private String kod5;
        private int necessaryBytesAmountForDeletingFile = 5;
        private final boolean isForced;

        private FTPClient ftpClient = null;
        private FileOutputStream fosPhoto = null;
        private InputStream inputStream = null;

        private String ftp_user, ftp_pass;

        private void init() {
            SharedPreferences settings;
            settings = CurAc.getSharedPreferences("apk_version", 0);

            ftp_user = settings.getString("FtpPhotoUser", getResources().getString(R.string.ftp_pass));
            ftp_pass = settings.getString("FtpPhotoPass", getResources().getString(R.string.ftp_user));
        }

        public PhotoDownloadingRunnable(String[] fileNames, long ID, boolean isForced) {
            this.fileNames = fileNames;
            this.isForced = isForced;
            this.kod5 = db.getProductKod5ByRowID(ID);
        }

        public PhotoDownloadingRunnable(String[] fileNames, String kod5, boolean isForced) {
            this.fileNames = fileNames;
            this.isForced = isForced;
            this.kod5 = kod5;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        @PGShowing(isCanceled = true)
        public void run() {
            try {
                init();
                int countOfSuccessfulDownloadedPhotos = 0;
                for (int i = 0; i < fileNames.length; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    currentDownloadingPhotoName = "";
                    String fileName = fileNames[i];
                    if (!isForced && (fileName == null || fileName != null && new File(getPhotoDir() + "/" + fileName).exists())) {
//                         && !isDifferentSizeOfPhotosOnDeviceAndOnServer(fileName))
                        countOfSuccessfulDownloadedPhotos++;
                        continue;
                    }
                    currentDownloadingPhotoName = getPhotoDir() + "/" + fileName;

                    ftpClient = new FTPClient();
                    int timeout = ServerDetails.getInstance().timeout;
                    ftpClient.setDefaultTimeout(timeout);
                    ftpClient.setDataTimeout(timeout);
                    ftpClient.setConnectTimeout(timeout);
                    ftpClient.setControlKeepAliveTimeout(timeout);
                    ftpClient.setControlKeepAliveReplyTimeout(timeout);

                    if (!tryConnectToDefaultIpOtherwiseToBackupIp(ftpClient)) {
                        throw new InterruptedException();
                    }

                    final String photoDir = getPhotoDir();
                    ftpClient.login(ftp_user, ftp_pass);
                    ftpClient.changeWorkingDirectory("FOTO");
                    ftpClient.enterLocalPassiveMode();

                    fosPhoto = new FileOutputStream(photoDir + "/" + fileName);

                    ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                    inputStream = ftpClient.retrieveFileStream(fileName);
                    byte[] bytesArray = new byte[16];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                        if (Thread.currentThread().isInterrupted()) {
                            throw new InterruptedException();
                        }
                        fosPhoto.write(bytesArray, 0, bytesRead);
                    }

//                    ftpClient.retrieveFile(fileName, fosPhoto);
                    ftpClient.disconnect();
                    inputStream.close();
                    fosPhoto.close();

                    long remoteSize = getRemotePhotoSize("FOTO/" + fileName);
                    long sizeOnDevice = getDevicePhotoSize(photoDir + "/" + fileName);
                    if (Math.abs(remoteSize - sizeOnDevice) >= necessaryBytesAmountForDeletingFile) {
                        currentPB.changeText("Файл был загружен с повреждениями. Пожалуйста, подождите.");
                        File file = new File(photoDir + "/" + fileName);
                        file.delete();
                        i--;
                        continue;
                    }

                    countOfSuccessfulDownloadedPhotos++;
                    try {
                        db.getWritableDatabase().execSQL("UPDATE Nomen SET PD=1 WHERE FOTO=? or FOTO2=?", new Object[]{fileName, fileName});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (countOfSuccessfulDownloadedPhotos != 0) {
                    CurAc.runOnUiThread(() -> showProductPhoto(fileNames, kod5));
                }
                currentDownloadingPhotoName = "";
            } catch (InterruptedException interruptedException) {
                closeStreamAndDeleteFile();
            } catch (SocketTimeoutException socketTimeoutException) {
                socketTimeoutException.printStackTrace();
                Config.sout("Время ожидания вышло");
                closeStreamAndDeleteFile();
            } catch (Exception e) {
                e.printStackTrace();
                Config.sout("Сервер недоступен");
                closeStreamAndDeleteFile();
            }
        }

        private void closeStreamAndDeleteFile() {
            try {
                if (fosPhoto != null) {
                    fosPhoto.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (ftpClient != null) {
                    ftpClient.disconnect();
                }
                File f = new File(currentDownloadingPhotoName);
                f.delete();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                Config.sout("Загрузка отменена");
            }
        }
    }

    public static String currentDownloadingPhotoName = "";

    public void downloadAndShowPhotos(final String[] fileNames, long ID, boolean isForced) {
        downloadPhotoTread = new Thread(new PhotoDownloadingRunnable(fileNames, ID, isForced));
        downloadPhotoTread.start();
    }

    public void downloadAndShowPhotos(final String[] fileNames, String kod5, boolean isForced) {
        downloadPhotoTread = new Thread(new PhotoDownloadingRunnable(fileNames, kod5, isForced));
        downloadPhotoTread.start();
    }

    private long getRemotePhotoSize(String fileName) throws FTPIllegalReplyException, IOException, FTPException {
        Ftp ftp = new Ftp(ServerDetails.getInstance());
        return ftp.getFileSize(fileName);
    }

    private long getDevicePhotoSize(String fileName) {
        File file = new File(fileName);
        return file.length();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showProductPhoto(String[] photoFileName, String kod5) {
        alertPhoto = null;
        String photoDir = getPhotoDir();
        photoFileName = Arrays.stream(photoFileName).filter(Objects::nonNull).toArray(String[]::new);
        if (photoFileName.length == 0) {
            Config.sout("Нет скачанных файлов");
            return;
        }

        for (String s : photoFileName) {
            checkPhotoInDB(s);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(CurAc);
        LayoutInflater inflater = CurAc.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.image_layout, null));
        builder.setCancelable(true);
        builder.setPositiveButton("OK", (dialog, id) -> dialog.dismiss());

        alertPhoto = builder.create();
        alertPhoto.getWindow().setLayout(600, 400);
        alertPhoto.show();

        TextView productID = alertPhoto.findViewById(R.id.nomenId);
        productID.setText("Код товара:   " + kod5);

        ViewPager viewPager = alertPhoto.findViewById(R.id.photoViewPager);

        LinearLayout dots = alertPhoto.findViewById(R.id.layoutForPhotoDots);
        PhotoDotsAdapter photoDotsAdapter = new PhotoDotsAdapter(getContext(), photoFileName.length, 0);
        photoDotsAdapter.fillLayout(dots);

        ImageAdapter adapter = new ImageAdapter(getContext(), photoFileName, photoDir);

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }

            @Override
            public void onPageSelected(int i) {
                photoDotsAdapter.changePosition(i);
            }
        });
    }

    @AsyncUI
    public void PreviewOrder() {
        nomenList.setAdapter(null);
        if (myNom != null) {
            myNom.close();
        }
        myNom = db.getOrderNom();
        PreviewZakazAdapter = new NomenAdapter(glbContext, R.layout.nomen_layout_preview, myNom, new String[]{"_id", "KOD5", "DESCR", "OST", "PRICE", "ZAKAZ", "GRUPPA", "SGI", "GOFRA", "MP"}, new int[]{R.id.ColNomID, R.id.ColNomCod, R.id.ColNomDescr, R.id.ColNomOst, R.id.ColNomPrice, R.id.ColNomZakaz, R.id.ColNomGRUPID, R.id.ColNomSGIID, R.id.ColNomVkorob, R.id.ColNomMP}, 0);
        nomenList.setAdapter(PreviewZakazAdapter);
        nomenList.setOnItemClickListener(GridNomenClick);
        nomenList.setOnItemLongClickListener(PreviewNomenLongClick);
    }

    public Boolean CheckTPLock() {
        SharedPreferences settings;
        settings = PreferenceManager.getDefaultSharedPreferences(CurFragmentContext);
        return settings.getBoolean("TP_LOCK", false);
    }

    @AsyncUI
    public void LoadTpList() {
        if (myTP != null) {
            myTP.close();
        }
        myTP = db.getTpList();
        TPAdapter adapter = new TPAdapter(CurAc, R.layout.tp_layout, myTP, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColTPID, R.id.ColTPDescr}, 0);
        TPList.setAdapter(adapter);

        TPList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View selectedItemView, int position, long id) {
                CurrentTp = myTP.getString(myTP.getColumnIndex("CODE"));
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        if (CheckTPLock()) {
            TPList.setEnabled(false);
        }
    }

    @AsyncUI
    public void LoadContrList() {
        spinContr.setAdapter(null);
        if (myContr != null) {
            myContr.close();
        }
        myContr = db.getContrList();
        ContrsAdapter adapter;
        adapter = new ContrsAdapter(CurAc, R.layout.contr_layout, myContr, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
        spinContr.setAdapter(adapter);
        spinContr.setOnItemSelectedListener(SelectedContr);
    }

    @AsyncUI
    public void LoadFilteredContrList(String FindStr) {
        spinContr.setAdapter(null);
        if (myContr != null) {
            myContr.close();
        }
        myContr = db.getContrFilterList(FindStr);
        ContrsAdapter adapter;
        adapter = new ContrsAdapter(CurAc, R.layout.contr_layout, myContr, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrID, R.id.ColContrDescr}, 0);
        spinContr.setAdapter(adapter);
        spinContr.setOnItemSelectedListener(SelectedContr);
    }

    @AsyncUI
    public void LoadContrAddress(String ContID) {
        if (myAddress != null) {
            myAddress.close();
        }
        myAddress = db.getContrAddress(ContID);
        AddressAdapter adapter;
        adapter = new AddressAdapter(CurAc, R.layout.addr_layout, myAddress, new String[]{"CODE", "DESCR"}, new int[]{R.id.ColContrAddrID, R.id.ColContrAddrDescr}, 0);
        spinAddress.setAdapter(adapter);

        String AddrID = db.GetContrAddr();
        if (!AddrID.equals("0")) {
            SetSelectedAddress(AddrID);
        } else if (!OrderHeadFragment._ADDR.equals("")) {
            SetSelectedAddress(OrderHeadFragment._ADDR);
        }
    }

    @DelayedCalled
    public void SetSelectedAddress(String AddrID) {
        for (int i = 0; i < spinAddress.getCount(); i++) {
            Cursor value = (Cursor) spinAddress.getItemAtPosition(i);
            String id = value.getString(value.getColumnIndex("CODE"));
            if (AddrID.equals(id)) {
                spinAddress.setSelection(i, true);
                return;
            }
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

    public String CreateDBFForSending(int ID) throws DBFException {
        Cursor c;

        String TP, CONTR, ADDR, DOCNO = "", COMMENT, NOMEN;
        java.util.Date DELIVERY, DOCDATE;
        double QTY;
        String PRICE;

        c = dbOrders.getReadableDatabase().rawQuery("SELECT TP, CONTR, ADDR, ZAKAZY.DOCID as DOCID, ZAKAZY.DOC_DATE as DOC_DATE, ZAKAZY.DELIVERY_DATE as DEL_DATE, ZAKAZY.COMMENT as COMMENT, ZAKAZY_DT.NOMEN as NOMEN, ZAKAZY_DT.DESCR as DES, ZAKAZY_DT.QTY as QTY, ZAKAZY_DT.PRICE as PRICE FROM ZAKAZY JOIN ZAKAZY_DT ON ZAKAZY.DOCID = ZAKAZY_DT.ZAKAZ_ID WHERE ZAKAZY.ROWID='" + ID + "'", null);
        if (c.getCount() == 0) {
            Config.sout("В таблице заказов нет записей для отправки");
            return "";
        }

        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("ddMMyyyy_HHmmss");
        String curdate = df.format(Calendar.getInstance().getTimeInMillis()) + Calendar.getInstance().get(Calendar.MILLISECOND);

        Cursor cForTpId = dbOrders.getReadableDatabase().rawQuery("SELECT TP FROM ZAKAZY WHERE rowid='" + ID + "'", null);

        cForTpId.moveToNext();
        String tpID = cForTpId.getString(cForTpId.getColumnIndex("TP"));

        String fileID = tpID + "_" + curdate;
        String FileName = CurAc.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + fileID + ".temp";
        String DBF_FileName = fileID + ".temp";

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
        fields[index].setDataType(DBFField.FIELD_TYPE_C);
        fields[index].setFieldLength(15);
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
                PRICE = c.getString(c.getColumnIndex("PRICE")).replace(",", ".");

                Object[] rowData = new Object[10];
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

    @AsyncUI
    public void LoadOrders() {
        try {
            Orders = dbOrders.getZakazy();
            if (Orders != null)
                putCheckBox(Orders);
            OrdersAdapter = new JournalAdapter(CurAc, R.layout.orders_item, Orders, new String[]{"DOCID", "STATUS", "DOC_DATE", "DELIVERY", "CONTR", "ADDR", "SUM", "COMMENT"}, new int[]{R.id.ColOrdDocNo, R.id.ColOrdStatus, R.id.ColOrdDocDate, R.id.ColOrdDeliveryDate, R.id.ColOrdContr, R.id.ColOrdAddr, R.id.ColOrdSum, R.id.ColOrdComment}, 0);
            gdOrders.setAdapter(OrdersAdapter);
//            gdOrders.setOnItemClickListener(showOrderDataItemClickListener);
        } catch (Exception e) {
            Config.sout(e, Toast.LENGTH_LONG);
        }
    }

    @AsyncUI
    public void LoadOrdersDetails(String ZakazID) {
        _doUpdateQTYByOuted(ZakazID);

        GlobalVars.allOrders.clear();
        layout.removeAllViews();

        orderDtList.setAdapter(null);
        OrdersDt = dbOrders.getZakazDetails(ZakazID);
        OrdersDtAdapter = new JournalDetailsAdapter(CurAc, R.layout.orderdt_item, OrdersDt, new String[]{"ZAKAZ_ID", "NOMEN", "DESCR", "QTY", "PRICE", "SUM"}, new int[]{R.id.ColOrdDtZakazID, R.id.ColOrdDtCod, R.id.ColOrdDtDescr, R.id.ColOrdDtQty, R.id.ColOrdDtPrice, R.id.ColOrdDtSum}, 0);
        orderDtList.setAdapter(OrdersDtAdapter);
//        orderDtList.setOnItemClickListener(OrderDtNomenClick);
    }

    public String ReadLastUpdate() {
        String timeUpdate = db.getLastUpdateTime();
        return timeUpdate != null ? timeUpdate : "";
    }

    @AsyncUI
    public void LoadDebet(final String TP_ID) {
        curDebet = db.getDebet(TP_ID);
        debetList.setAdapter(null);
        DebetAdapter adapter = new DebetAdapter(CurAc, R.layout.debet_layout, curDebet, new String[]{"DESCR", "STATUS", "KREDIT", "SALDO", "A7", "A14", "A21", "A28", "A35", "A42", "A49", "A56", "A63", "A64", "OTG30", "OPL30", "KOB", "FIRMA", "CRT_DATE"}, new int[]{R.id.ColDebetContr, R.id.ColDebetStatus, R.id.ColDebetCredit, R.id.ColDebetDolg, R.id.ColDebetA7, R.id.ColDebetA14, R.id.ColDebetA21, R.id.ColDebetA28, R.id.ColDebetA35, R.id.ColDebetA42, R.id.ColDebetA49, R.id.ColDebetA56, R.id.ColDebetA63, R.id.ColDebetA64, R.id.ColDebetOTG30, R.id.ColDebetOPL30, R.id.ColDebetKOB, R.id.ColDebetFirma, R.id.ColDebetDogovor}, 0);
        debetList.setAdapter(adapter);
    }

    public void UpdateNomenRange(int beginRange, int endRange, int qty) {
        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                int EndRange = endRange;
                int BeginRange = beginRange;
//                String sql_update = "UPDATE Nomen SET ZAKAZ = CASE WHEN (OST - " + qty + ") >= 0 THEN " + qty + " ELSE OST END WHERE ROWID=?";
                String sql_update = "UPDATE Nomen SET ZAKAZ = " + qty + " WHERE ROWID=?";
                SQLiteStatement stmt = db.getWritableDatabase().compileStatement(sql_update);
                db.getWritableDatabase().beginTransaction();
                int tmpVal;

                if (EndRange > CurVisiblePosition) {
                    EndRange = CurVisiblePosition;
                }

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

                for (int i = BeginRange; i <= EndRange; i++) {
                    long pos = NomenAdapter.getItemId(i - 1);

                    SQLiteDatabase sqLiteDatabase = db.getReadableDatabase();
                    Cursor kod5 = sqLiteDatabase.rawQuery("SELECT KOD5 FROM NOMEN WHERE rowid='" + pos + "'", null);
                    kod5.moveToNext();
                    if (kod5.getCount() == 0)
                        continue;
                    db.putPriceInNomen(pos, "" + DBHelper.pricesMap.get(kod5.getString(0)));
                    kod5.close();
                }

                CurAc.runOnUiThread(() -> {
                    myNom.requery();
                    NomenAdapter.notifyDataSetChanged();
//                    setContrAndSum(GlobalVars.this);
                });
            }
        }).start();
    }

    @AsyncUI
    public void setIconColor(Menu menu, int MenuItem, Boolean vis) {
        if (menu.findItem(MenuItem) == null)
            return;
        Drawable drawable = menu.findItem(MenuItem).getIcon();
        drawable.mutate();
        if (vis) {
            drawable.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
        } else {
            drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
//        isDiscount = vis;
    }

    public void CalculatePercentSale(final Menu menu) {
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
                    if (Discount > 100) {
                        Discount = 100;
                    }

                    if (Discount == 0) {
                        isDiscount = false;
                        setIconColor(menu, R.id.NomenDiscount, false);
                    } else {
                        isDiscount = true;
                        setIconColor(menu, R.id.NomenDiscount, true);
                    }

                    if (NomenAdapter != null) {
                        myNom.requery();
                        NomenAdapter.notifyDataSetChanged();
                    }

                    if (PreviewZakazAdapter != null) {
                        myNom.requery();
                        PreviewZakazAdapter.notifyDataSetChanged();
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
        String Sql;

        if (isDownloaded.equals("_2")) {
            Sql = "SELECT PD From Nomen WHERE KOD5='" + isDownloaded.replace(isDownloaded, "") + "'";
        } else {
            Sql = "SELECT PD From Nomen WHERE KOD5='" + isDownloaded + "'";
        }
        cur = db.getWritableDatabase().rawQuery(Sql, null);

        if (cur.moveToFirst()) {
            if (cur.getInt(0) == 0) {
                if (isDownloaded.equals("_2")) {
                    db.getWritableDatabase().execSQL("UPDATE Nomen SET PD=1 WHERE KOD5='" + isDownloaded.replace(isDownloaded, "") + "'");
                } else {
                    db.getWritableDatabase().execSQL("UPDATE Nomen SET PD=1 WHERE KOD5='" + isDownloaded + "'");
                }
            }
        }
        cur.close();
        GridView gdNomen = CurView.findViewById(R.id.listContrs);
        myNom.requery();
        if (NomenAdapter != null)
            NomenAdapter.notifyDataSetChanged();
        gdNomen.invalidateViews();
    }

    public void updateOutedPositionInZakazyTable() {
        try {

            SQLiteDatabase sqLiteDatabaseOrders = dbOrders.getReadableDatabase();
            SQLiteDatabase sqLiteDatabase = db.getReadableDatabase();

            sqLiteDatabaseOrders.beginTransaction();
            Cursor ordersId = sqLiteDatabaseOrders.rawQuery("SELECT DOCID FROM ZAKAZY", null);
            while (ordersId.moveToNext()) {
                String orderID = ordersId.getString(0);
                Cursor c = sqLiteDatabase.rawQuery("SELECT NOMEN FROM VYCHERK WHERE DOCID=?", new String[]{orderID});

                if (c.getCount() != 0) {
                    sqLiteDatabaseOrders.execSQL("UPDATE ZAKAZY SET OUTED=1 WHERE DOCID='" + orderID + "'");
                } else {
                    sqLiteDatabaseOrders.execSQL("UPDATE ZAKAZY SET OUTED=0 WHERE DOCID='" + orderID + "'");
                }
                c.close();
            }
            sqLiteDatabaseOrders.setTransactionSuccessful();
            sqLiteDatabaseOrders.endTransaction();
            ordersId.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ChosenOrdersData {
        private int id;
        private boolean isChecked;
        private final String status;

        public ChosenOrdersData(int id, boolean isChecked, String status) {
            this.id = id;
            this.isChecked = isChecked;
            this.status = status;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        public String getStatus() {
            return status;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public class AddressAdapter extends SimpleCursorAdapter {
        public AddressAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Cursor cursor = getCursor();
            TextView tvDescr = view.findViewById(R.id.ColContrAddrDescr);

            if (position % 2 != 0) {
                tvDescr.setBackgroundColor(getResources().getColor(R.color.gridViewFirstColor));
            } else {
                tvDescr.setBackgroundColor(getResources().getColor(R.color.gridViewSecondColor));
            }
            tvDescr.setText(cursor.getString(2));

            return view;
        }
    }

    public class NomenAdapter extends SimpleCursorAdapter {
        public NomenAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            Cursor cursor = getCursor();
            CurVisiblePosition = cursor.getCount();
            int resID;

            String kod5 = cursor.getString(cursor.getColumnIndex("KOD5"));

            TextView tvDescr = view.findViewById(R.id.ColNomDescr);
            TextView tvPrice = view.findViewById(R.id.ColNomPrice);
            TextView tvPosition = view.findViewById(R.id.ColNomPosition);
            TextView tvMP = view.findViewById(R.id.ColNomMP);
            TextView tvGofra = view.findViewById(R.id.ColNomVkorob);
            TextView tvOst = view.findViewById(R.id.ColNomOst);
            TextView tvCod = view.findViewById(R.id.ColNomCod);
            TextView tvPhoto = view.findViewById(R.id.ColNomPhoto);
            TextView tvZakaz = view.findViewById(R.id.ColNomZakaz);

            TextView[] tvListForChange = new TextView[]{
                    tvDescr,
                    tvPrice,
                    tvPosition,
                    tvMP,
                    tvGofra,
                    tvOst,
                    tvCod,
            };

            Button btPlus = view.findViewById(R.id.btPlus);
            Button btMinus = view.findViewById(R.id.btMinus);

            tvDescr.setTextSize(SettingFragment.nomenDescriptionFontSize);
            if (tvPhoto != null) {
                tvPhoto.setOnClickListener(v -> ((GridView) parent).performItemClick(v, position, 0));
                tvPhoto.setOnLongClickListener(PhotoLongClick);
            }

            if (tvPrice != null && !tvPrice.getText().toString().equals("null")) {
                tvPrice.setText(String.format(Locale.ROOT, "%.2f", Float.parseFloat(tvPrice.getText().toString())));
            }

            btPlus.setOnClickListener(v -> ((GridView) parent).performItemClick(v, position, 0));
            btMinus.setOnClickListener(v -> ((GridView) parent).performItemClick(v, position, 0));

            if (DBHelper.pricesMap.containsKey(kod5) && isSales) {
//                System.out.println(kod5 + " " + DBHelper.pricesMap.get(kod5));
                tvPrice.setText(String.format(Locale.ROOT, "%.2f", DBHelper.pricesMap.get(kod5)));
            }

            if (isDiscount && tvPhoto != null) {
                tvPrice.setText(String.format(Locale.ROOT, "%.2f", Float.parseFloat(tvPrice.getText().toString()) * (1 - Discount / 100f)));
            }

            if (tvPhoto != null && (cursor.getString(cursor.getColumnIndex("FOTO")) != null)) {
                if (cursor.getInt(cursor.getColumnIndex("PD")) == 1) {
                    resID = glbContext.getResources().getIdentifier("photo_downloaded", "drawable", glbContext.getPackageName());
                } else {
                    resID = glbContext.getResources().getIdentifier("photo_no_downloaded", "drawable", glbContext.getPackageName());
                }
                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append(" ").append(" ");
                builder.setSpan(new ImageSpan(glbContext, resID), builder.length() - 1, builder.length(), 0);
                builder.append(" ");
                tvPhoto.setText(builder);
            }

            long daysSubtraction = _countDaySubtraction(cursor);

            int backgroundColor;
            if ((BeginPos != 0 || EndPos != 0) && position >= BeginPos - 1 && position <= EndPos - 1) {
                backgroundColor = getResources().getColor(R.color.multiSelectedNomen);
            } else if (!tvZakaz.getText().toString().equals("0")) {
                backgroundColor = getResources().getColor(R.color.selectedNomen);
            } else {
                if (position % 2 != 0) {
                    backgroundColor = getResources().getColor(R.color.gridViewFirstColor);
                } else {
                    backgroundColor = getResources().getColor(R.color.gridViewSecondColor);
                }
            }
            view.setBackgroundColor(backgroundColor);

            int color = getResources().getColor(R.color.black);
            if (daysSubtraction <= 2) {
                color = getResources().getColor(R.color.postDataColorRed);
            } else if (daysSubtraction <= 4) {
                color = getResources().getColor(R.color.postDataColorGreen);
            }
            _setTextColorOnTextView(tvListForChange, color);

            int style = Typeface.NORMAL;
            String action_list_temp = cursor.getString(cursor.getColumnIndex("ACT_LIST"));
            String[] action_list = action_list_temp == null ? new String[]{} : action_list_temp.split(",");

            if (action_list.length > 0) {
                style = Typeface.BOLD_ITALIC;
            }
            _setTypeFaceOnTextView(tvListForChange, style);

            tvPosition.setText(String.valueOf(position + 1));

            return view;
        }

        private void _setTextColorOnTextView(TextView[] tvList, int color) {
            for (TextView tv : tvList) {
                tv.setTextColor(color);
            }
        }

        private void _setTypeFaceOnTextView(TextView[] tvList, int style) {
            for (TextView tv : tvList) {
                tv.setTypeface(Typeface.defaultFromStyle(style));
            }
        }

        private long _countDaySubtraction(Cursor cursor) {
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

            return (long) Math.abs((PostDataTime - CurrentTime) / (1000d * 60 * 60 * 24));
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            setContrAndSum(GlobalVars.this);
            // check isSales and if true, then set real prices from contractor
//            if (hasBeenAlreadyNoChanged && isSales && nomenList.getCount() != 0) {
//                hasBeenAlreadyNoChanged = false;
//                FormOrderFragment.setRealPrices(GlobalVars.this);
//            }
        }
    }

    public class JournalAdapter extends SimpleCursorAdapter {
        private class ClickData {
            public int visibility;

            public ClickData(int visibility) {
                this.visibility = visibility;
            }
        }

        private HashMap<Integer, ClickData> clickDataMap;

        public JournalAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            clickDataMap = new HashMap<Integer, ClickData>() {
                @Override
                public String toString() {
                    StringBuilder s = new StringBuilder();
                    Set<Integer> a = clickDataMap.keySet();
                    for (int i : a) {
                        s.append(i).append(" ");
                    }
                    return s.toString();
                }
            };
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Cursor cursor = getCursor();
            View view = super.getView(position, convertView, parent);
            LinearLayout layout = view.findViewById(R.id.expandedData);

            if (!clickDataMap.containsKey(position)) {
                layout.setVisibility(View.GONE);
            } else {
                layout.setVisibility(clickDataMap.get(position).visibility);
            }

            ImageView showData = view.findViewById(R.id.showData);
            TextView tvSum = view.findViewById(R.id.ColOrdSum);
            updateTextFormatTo2DecAfterPoint(tvSum);

            if (cursor.getInt(cursor.getColumnIndex("OUTED")) == 1) {
                tvSum.setText(tvSum.getText().toString() + " (-)");
            }

            TextView tvStatus = view.findViewById(R.id.ColOrdStatus);
            if (tvStatus.getText().toString().equals("Сохранён"))
                tvStatus.setTypeface(Typeface.DEFAULT_BOLD);

            if (allOrders.size() != 0 && allOrders.get(position).isChecked) {
                view.setBackgroundColor(getResources().getColor(R.color.gridViewFirstColor));
            } else {
                view.setBackgroundColor(getResources().getColor(R.color.gridViewSecondColor));
            }

            ClickData clickData = new ClickData(View.GONE);
            if (!isNeededToShowEnterIcon()) {
                showData.setVisibility(View.INVISIBLE);
                clickDataMap.put(position, clickData);
            } else {
                showData.setVisibility(View.VISIBLE);
                showData.setOnClickListener(view1 -> {
                    if (layout.getVisibility() == View.GONE) {
                        layout.setVisibility(View.VISIBLE);
                        clickData.visibility = View.VISIBLE;
                    } else {
                        layout.setVisibility(View.GONE);
                    }
                    clickDataMap.put(position, clickData);
                });
            }

            return view;
        }

        private boolean isNeededToShowEnterIcon() {
            Cursor cursor = getCursor();
            String[] data = new String[]{
                    cursor.getString(cursor.getColumnIndex("COMMENT"))
            };
            for (String i : data) {
                if (!i.equals("")) {
                    return true;
                }
            }
            return false;
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
            TextView tvPrice = view.findViewById(R.id.ColOrdDtPrice);
            updateTextFormatTo2DecAfterPoint(tvSum);
            updateTextFormatTo2DecAfterPoint(tvPrice);

            if (position % 2 != 0) {
                view.setBackgroundColor(getResources().getColor(R.color.gridViewFirstColor));
            } else {
                view.setBackgroundColor(getResources().getColor(R.color.gridViewSecondColor));
            }

            if (cursor.getInt(cursor.getColumnIndex("IS_OUTED")) == 1) {
                int outQTY = cursor.getInt(cursor.getColumnIndex("OUT_QTY"));
                int QTY = cursor.getInt(cursor.getColumnIndex("QTY"));
                tvQty.setText(QTY + "(" + (QTY - outQTY) + ")");
            }

            return view;
        }
    }

    public class TPAdapter extends SimpleCursorAdapter {
        public TPAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flag) {
            super(context, layout, c, from, to, flag);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            TextView tvDescr = view.findViewById(R.id.ColTPDescr);
            if (position % 2 != 0) {
                tvDescr.setBackgroundColor(getResources().getColor(R.color.gridViewFirstColor));
            } else {
                tvDescr.setBackgroundColor(getResources().getColor(R.color.gridViewSecondColor));
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
            String resDescr = cursor.getString(cursor.getColumnIndex("DESCR"));
//            + "\t\t" + cursor.getString(cursor.getColumnIndex("STATUS"));

            TextView tvDescr = view.findViewById(R.id.ColContrDescr);
//            if (position % 2 != 0) {
//                tvDescr.setBackgroundColor(getResources().getColor(R.color.gridViewFirstColor));
//            } else {
//                tvDescr.setBackgroundColor(getResources().getColor(R.color.gridViewSecondColor));
//            }

            tvDescr.setText(resDescr);
            return view;
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

            if (position % 2 != 0) {
                view.setBackgroundColor(getResources().getColor(R.color.gridViewFirstColor));
            } else {
                view.setBackgroundColor(getResources().getColor(R.color.gridViewSecondColor));
            }

            for (TextView v : tvArray) {
                updateTextFormatTo2DecAfterPoint(v);
            }

            return view;
        }

    }

    public void resetCurData() {
        CurGroup = CurWCID = CurFocusID = CurSGI = "0";
        CurSearchName = "";
        isNeededToBeLoadingBySgi = true;

        Discount = 0f;
        Menu menu = FormOrderFragment.mainMenu;
        if (menu != null && menu.size() > 1) {
            setIconColor(menu, R.id.NomenDiscount, false);
        }
    }

    public void resetAllSpinners() {
        if (spGroup != null) {
            spGroup.post(() -> spGroup.setAdapter(null));
        }

        if (spSgi != null) {
            spSgi.post(() -> spSgi.setSelection(0));
        }

        // if spWC != null, то и spFocus и другие фильтры тоже != null
        if (spWC != null) {
            spWC.setSelection(0);
            spFocus.setSelection(0);
        }
        FormOrderFragment.filter.setImageResource(R.drawable.filter);
        FormOrderFragment.isFiltered = false;
    }

    public void resetSearchViewData() {
        CurSearchName = "";
        SearchView searchView = CurAc.findViewById(R.id.menu_search);
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.onActionViewCollapsed();
        }
    }

    public void putAllPrices() {
        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                db.putAllNomenPrices(OrderHeadFragment.CONTR_ID);
                CurAc.runOnUiThread(() -> {
                    if (NomenAdapter != null) {
                        NomenAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    public void closeCursors() {
        Cursor[] cursors = new Cursor[]{
                myNom,
                mySgi,
                myGroup,
                myWC,
                myFocus
        };
        if (myNom != null) {
            CurAc.runOnUiThread(() -> nomenList.setAdapter(null));
        }
        for (Cursor cursor : cursors) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void updateNomenPrice(boolean isCopied) {
        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                db.ResetNomenPrice(isCopied);
            }
        });
    }

    public static String[] getCurrentData() {
        return new String[]{CurSGI, CurGroup, CurWCID, CurFocusID, CurSearchName};
    }

    public void closeAllCursorsInHeadOrder() {
        if (myAddress != null) {
            myAddress.close();
        }
        if (myContr != null) {
            myContr.close();
        }
        if (myTP != null) {
            myTP.close();
        }
    }

    private void updateTextFormatTo2DecAfterPoint(TextView v) {
        try {
//            int len = v.getText().toString().split("\\.")[0].length();
//            v.setText(v.getText().toString().substring(0, len + 2));

//            DecimalFormat decimalFormat = new DecimalFormat("#.##");
//            v.setText(decimalFormat.format(v.getText().toString()));

            v.setText(String.format(Locale.ROOT, "%.2f", Float.parseFloat(v.getText().toString().replace(",", "."))));
        } catch (Exception ignored) {
        }
    }

    public void updateOrdersStatusFromDB() {
        SQLiteDatabase dbApp = db.getReadableDatabase();
        SQLiteDatabase dbOrd = dbOrders.getWritableDatabase();
//        dbOrd.beginTransaction();
        Cursor statusInApp = dbOrd.rawQuery("SELECT DOCID FROM ZAKAZY", null);
        Cursor statusInDB;
        while (statusInApp.moveToNext()) {
            String docId = statusInApp.getString(statusInApp.getColumnIndex("DOCID"));
            statusInDB = dbApp.rawQuery("SELECT STATUS FROM STATUS WHERE DOCID = '" + docId + "'", null);
            if (statusInDB.getCount() != 0) {
                statusInDB.moveToNext();
                String Status = statusInDB.getString(statusInDB.getColumnIndex("STATUS"));
                dbOrd.execSQL("UPDATE ZAKAZY SET STATUS = '" + Status + "' WHERE DOCID='" + docId + "'");
            }
            statusInDB.close();
        }

        statusInApp.close();
//        dbOrd.endTransaction();
//        if (statusInDB != null)
//            statusInDB.close();
    }

    private void _doUpdateQTYByOuted(String DocID) {
        SQLiteDatabase dbApp = dbOrders.getWritableDatabase();
        SQLiteDatabase dbVy = db.getReadableDatabase();

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

    private void putCheckBox(Cursor c) {
        GlobalVars.allOrders.clear();
        layout.removeAllViews();

        if (c.getCount() == 0) return;

        int id;
        String status;
        boolean isChecked;

        while (c.moveToNext()) {
            id = c.getInt(c.getColumnIndex("_id"));
            status = c.getString(c.getColumnIndex("STATUS"));
            isChecked = status.equals("Сохранён");

//            CheckBox checkBox = new CheckBox(layout.getContext());
//            float height = getResources().getDimension(R.dimen.heightOfOrdersItem);
//            checkBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) height));
//            layout.addView(checkBox);

            GlobalVars.allOrders.add(new ChosenOrdersData(id, isChecked, status));
        }
    }

    // Начадл обработчиков событий нажатия на NomenLayout
    private void plusQTY(View myView) {
        View parent = (View) myView.getParent();
        TextView tvKOD5 = parent.findViewById(R.id.ColNomCod);
        TextView tvPrice = parent.findViewById(R.id.ColNomPrice);

        String price = tvPrice.getText().toString();
        String kod5 = tvKOD5.getText().toString();
        db.putPriceInNomen(kod5, price);
        db.PlusQty(kod5);
        if (myNom != null)
            myNom.requery();

        if (NomenAdapter != null)
            NomenAdapter.notifyDataSetChanged();

        if (PreviewZakazAdapter != null)
            PreviewZakazAdapter.notifyDataSetChanged();

//        setContrAndSum(GlobalVars.this);
    }

    private void minusQTY(View myView) {
        View parent = (View) myView.getParent();
        TextView tvKOD5 = parent.findViewById(R.id.ColNomCod);
        String kod5 = tvKOD5.getText().toString();

        db.MinusQty(kod5);
        if (myNom != null)
            myNom.requery();

        if (NomenAdapter != null)
            NomenAdapter.notifyDataSetChanged();

        if (PreviewZakazAdapter != null)
            PreviewZakazAdapter.notifyDataSetChanged();

//        setContrAndSum(GlobalVars.this);
    }

    @AsyncUI
    private void showPhoto(View myView, int position, AdapterView<?> myAdapter) {
        if (((TextView) myView).getText() == null || ((TextView) myView).getText().toString().equals(""))
            return;

        long ID = myAdapter.getItemIdAtPosition(position);

        @SuppressLint({"NewApi", "LocalSuppress"}) String[] fileNames = db.getPhotoNames(ID);
        downloadAndShowPhotos(fileNames, ID, false);
    }

    private void multiSelect(String ID, int ost) {
        db.UpdateQty(ID, MultiQty, ost);
        if (myNom != null)
            myNom.requery();
        if (NomenAdapter != null)
            NomenAdapter.notifyDataSetChanged();

        db.putPriceInNomen(ID, "" + DBHelper.pricesMap.get(ID));
        setContrAndSum(GlobalVars.this);
    }

    @AsyncUI
    private void fillNomenDataFromAlertDialog(String ID, int ost) {
        try {
            LayoutInflater layoutInflater = LayoutInflater.from(glbContext);
            View promptView = layoutInflater.inflate(R.layout.change_qty, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CurFragmentContext);
            alertDialogBuilder.setView(promptView);

            final EditText input = promptView.findViewById(R.id.edPPQty);
            final TextView txtCod = promptView.findViewById(R.id.txtNomCode);
            final TextView txtDescr = promptView.findViewById(R.id.txtNomDescr);
            final TextView txtOst = promptView.findViewById(R.id.txtNomOst);
            final TextView txtGrup = promptView.findViewById(R.id.txtNomGroup);

            try {
                input.setText(myNom.getString(myNom.getColumnIndex("ZAKAZ")));
                txtCod.setText(myNom.getString(myNom.getColumnIndex("KOD5")));
                txtDescr.setText(myNom.getString(myNom.getColumnIndex("DESCR")));
                txtOst.setText(myNom.getString(myNom.getColumnIndex("OST")));
            } catch (Exception e1) {
                Config.sout(e1);
            }

            try {
                Cursor c = db.getReadableDatabase().rawQuery("SELECT DESCR FROM GRUPS WHERE CODE=?", new String[]{myNom.getString(myNom.getColumnIndex("GRUPPA"))});
                c.moveToNext();
                String groupDescription = c.getString(c.getColumnIndex("DESCR"));
                txtGrup.setText(groupDescription);
            } catch (Exception e2) {
                Config.sout(e2);
            }

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
                TextView tvPrice = CurView.findViewById(R.id.ColNomPrice);
                String price = tvPrice.getText().toString();
                db.putPriceInNomen(ID, price);

                db.UpdateQty(ID, Integer.parseInt(input.getText().toString()), ost);
                myNom.requery();

                setContrAndSum(GlobalVars.this);

                if (NomenAdapter != null)
                    NomenAdapter.notifyDataSetChanged();

                if (PreviewZakazAdapter != null)
                    PreviewZakazAdapter.notifyDataSetChanged();

                alertD.dismiss();
                Config.hideKeyBoard();
            });
            input.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    alertD.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                }
                return true;
            });
        } catch (Exception e) {
            e.printStackTrace();
            Config.sout(e);
        }
    }
}
