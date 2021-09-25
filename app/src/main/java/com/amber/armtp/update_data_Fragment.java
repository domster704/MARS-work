package com.amber.armtp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class update_data_Fragment extends Fragment{
    public GlobalVars glbVars;
    private android.support.v7.widget.Toolbar toolbar;
    Connection conn = null;
    String sql_server;
    String sql_port;
    String sql_db;
    String sql_loging;
    String sql_pass;
    Statement stmt;
    ResultSet reset;
    Thread thUpdateData;

    CheckBox chkSgi, chkGrup, chkNomen, chkContrs, chkAddrr, chkTp, chkDebet, chkStatus, chkOuted, chkSales, chkGrupAccess, chkFuncs, chkTovcat, chkBrand, chkWC, chkProd, chkFocus;
    CheckBox chkUniMx;

    private String DebetIsFinished = "0";
    SQLiteDatabase UpdateDB;

    private ProgressBar pgUpSgi, pgUpGroups, pgUpNomen, pgUpContrs, pgUpAddress, pgUpTp, pgUpDebet, pgUpStatus, pgUpOuted, pgUpSales, pgUpGrupAccess, pgUpFuncs, pgUpTovcat, pgUpBrand, pgUpWC, pgUpProd, pgUpFocus;
    private ProgressBar pgUpUniMX;
    private TextView tvUpUniMX, tvUpUniMXPerc;

    private TextView tvUpSgi, tvUpGroups, tvUpNomen, tvUpContrs, tvUpAddress, tvUpTp, tvUpDebet, tvUpStatus, tvUpOuted, tvUpSales, tvUpGrupAccess, tvUpFuncs, tvUpTovcat, tvUpBrand, tvUpWC, tvUpProd, tvUpFocus;
    private TextView tvUpSgiPerc, tvUpGroupsPerc, tvUpNomenPerc, tvUpContrsPerc, tvUpAddressPerc, tvUpTpPerc, tvUpDebetPerc, tvUpStatusPerc, tvUpOutedPerc, tvUpSalesPerc, tvUpGrupAccessPerc, tvUpFuncsPerc, tvUpTovcatPerc, tvUpBrandPerc, tvUpWCPerc, tvUpProdPerc, tvUpFocusPerc;

    Button btUpdate;
    private int progressStatus = 0;
    private int progressStatusSGI = 0;
    private int progressStatusGrup = 0;
    private int progressStatusNom = 0;
    private int progressStatusContr = 0;
    private int progressStatusAddr = 0;
    private int progressStatusTP = 0;
    private int progressStatusDeb = 0;
    private int progressStatusOrders = 0;
    private int progressStatusOuted = 0;
    private int progressStatusSales = 0;
    private int progressStatusAccess = 0;
    private int progressStatusFuncs = 0;
    private int progressStatusTovcat = 0;
    private int progressStatusBrand = 0;
    private int progressStatusWC = 0;
    private int progressStatusProd = 0;
    private int progressStatusFocus = 0;
    private int progressStatusUniMX = 0;

    private Handler handler = new Handler();
    private Handler handlerSgi = new Handler();
    private Handler handlerGroups = new Handler();
    private Handler handlerNom = new Handler();
    private Handler handlerContr = new Handler();
    private Handler handlerAddr = new Handler();
    private Handler handlerTP = new Handler();
    private Handler handlerDebet = new Handler();
    private Handler handlerStatus = new Handler();
    private Handler handlerOut = new Handler();
    private Handler handlerSales = new Handler();
    private Handler handlerGrupAccess = new Handler();
    private Handler handlerFuncs = new Handler();
    private Handler handlerTovcat = new Handler();
    private Handler handlerBrand = new Handler();
    private Handler handlerWC = new Handler();
    private Handler handlerProd = new Handler();
    private Handler handlerFocus = new Handler();
    private Handler handlerUniMX = new Handler();

    String TP_ID;
    Boolean TP_LOCK;

    String sql_update;
    String sql_insert;
    String query, sqlView;

    int finalCount = 0;
    int perc = 0;
    int cntNomen = 0;

    String ID = "", PARENTID = "", Cod5 = "", Code = "", Descr = "", SgiID = "", Photo1 = "", Photo2 = "", MP="";
    int Ost = 0, Vkorob = 0, ISNEW = 0, IS7DAY = 0, IS28DAY = 0, IS_PERM = 0;
    Float Price = 0.0f;
    Cursor c;

    String sql = "";
    SQLiteStatement statement;
    SQLiteStatement statement2;

    private int Region = 0;

    SharedPreferences settings, PriceSettings, tp_setting;
    SharedPreferences.Editor editor, tp_editor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.update_data_fragment,container,false);
        v.setKeepScreenOn(true);
        glbVars.view = v;
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        glbVars = (GlobalVars) getActivity().getApplicationContext();
        glbVars.setContext(getActivity().getApplicationContext());
        glbVars.frContext = getActivity();
        glbVars.CurAc = getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            getActivity().registerReceiver(UpdateDebetWorking, new IntentFilter("DebetUpdating"));
        } catch (Exception E) {
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(UpdateDebetWorking);
        } catch (Exception E) {
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        glbVars.toolbar = getActivity().findViewById(R.id.toolbar);
        glbVars.toolbar.setSubtitle("");
        setRetainInstance(true);
        settings = getActivity().getSharedPreferences("apk_version", 0);
        PriceSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        tp_setting = PreferenceManager.getDefaultSharedPreferences(getActivity());

        TP_ID = tp_setting.getString("TP_ID", "0");
        TP_LOCK = tp_setting.getBoolean("TP_LOCK", false);

//        System.out.println("TP_ID: " + TP_ID);
//        System.out.println("TP_LOCK: " + TP_LOCK);

//        sql_server = getResources().getString(R.string.sql_server);

        sql_server = settings.getString("UpdateSrv", getResources().getString(R.string.sql_server));
        sql_port = settings.getString("sqlPort", getResources().getString(R.string.sql_port));
        sql_db = settings.getString("sqlDB", getResources().getString(R.string.sql_db));
        sql_loging = settings.getString("sqlLogin", getResources().getString(R.string.sql_user));
        sql_pass = settings.getString("sqlPass", getResources().getString(R.string.sql_pass));
        glbVars.CurrentCenType = settings.getString("usr_centype","");
        Region = PriceSettings.getInt("Region", 0);
        editor = settings.edit();


        btUpdate = getActivity().findViewById(R.id.btUpdateData);
        pgUpStatus = getActivity().findViewById(R.id.pgOrdStatus);
        tvUpStatus = getActivity().findViewById(R.id.tvStatusCount);
        tvUpStatusPerc = getActivity().findViewById(R.id.tvStatusPerc);
        chkStatus = getActivity().findViewById(R.id.chkOrdStatus);

        pgUpSgi = getActivity().findViewById(R.id.pgSgi);
        pgUpGroups = getActivity().findViewById(R.id.pgGroups);
        pgUpNomen = getActivity().findViewById(R.id.pgNomen);
        pgUpContrs = getActivity().findViewById(R.id.pgContrs);
        pgUpAddress = getActivity().findViewById(R.id.pgAddress);
        pgUpTp = getActivity().findViewById(R.id.pgTP);
        pgUpDebet = getActivity().findViewById(R.id.pgDebet);
        pgUpStatus = getActivity().findViewById(R.id.pgOrdStatus);
        pgUpOuted = getActivity().findViewById(R.id.pgOuted);
        pgUpSales = getActivity().findViewById(R.id.pgSales);
        pgUpGrupAccess = getActivity().findViewById(R.id.pgGrupAccess);
        pgUpUniMX = getActivity().findViewById(R.id.pgUniMX);
//        pgUpFuncs = getActivity().findViewById(R.id.pgFuncs);
//        pgUpTovcat = getActivity().findViewById(R.id.pgTovcat);
//        pgUpBrand = getActivity().findViewById(R.id.pgBrand);
//        pgUpWC = getActivity().findViewById(R.id.pgWC);
//        pgUpProd = getActivity().findViewById(R.id.pgProd);
//        pgUpFocus = getActivity().findViewById(R.id.pgFocus);

        tvUpSgi = getActivity().findViewById(R.id.tvSgiCount);
        tvUpSgiPerc = getActivity().findViewById(R.id.tvSgiPerc);

        tvUpGroups = getActivity().findViewById(R.id.tvGrupCount);
        tvUpGroupsPerc = getActivity().findViewById(R.id.tvGrupPerc);

        tvUpNomen = getActivity().findViewById(R.id.tvNomenCount);
        tvUpNomenPerc = getActivity().findViewById(R.id.tvNomenPerc);

        tvUpContrs = getActivity().findViewById(R.id.tvContrCount);
        tvUpContrsPerc = getActivity().findViewById(R.id.tvContrPerc);

        tvUpAddress = getActivity().findViewById(R.id.tvAddrCount);
        tvUpAddressPerc = getActivity().findViewById(R.id.tvAddrPerc);

        tvUpTp = getActivity().findViewById(R.id.tvTpCount);
        tvUpTpPerc = getActivity().findViewById(R.id.tvTpPerc);

        tvUpDebet = getActivity().findViewById(R.id.tvDebCount);
        tvUpDebetPerc = getActivity().findViewById(R.id.tvDebPerc);

        tvUpStatus = getActivity().findViewById(R.id.tvStatusCount);
        tvUpStatusPerc = getActivity().findViewById(R.id.tvStatusPerc);

        tvUpOuted = getActivity().findViewById(R.id.tvOutedCount);
        tvUpOutedPerc = getActivity().findViewById(R.id.tvOutedPerc);

        tvUpSales = getActivity().findViewById(R.id.tvSalesCount);
        tvUpSalesPerc = getActivity().findViewById(R.id.tvSalesPerc);

        tvUpGrupAccess = getActivity().findViewById(R.id.tvGACount);
        tvUpGrupAccessPerc = getActivity().findViewById(R.id.tvGAPerc);

        tvUpUniMX = getActivity().findViewById(R.id.tvUniMXCount);
        tvUpUniMXPerc = getActivity().findViewById(R.id.tvUniMXPerc);

//        tvUpFuncs = getActivity().findViewById(R.id.tvFuncsCount);
//        tvUpFuncsPerc = getActivity().findViewById(R.id.tvFuncsPerc);
//
//        tvUpTovcat = getActivity().findViewById(R.id.tvTovcatCount);
//        tvUpTovcatPerc = getActivity().findViewById(R.id.tvTovcatPerc);
//
//        tvUpBrand = getActivity().findViewById(R.id.tvBrandCount);
//        tvUpBrandPerc = getActivity().findViewById(R.id.tvBrandPerc);
//
//        tvUpWC = getActivity().findViewById(R.id.tvWCCount);
//        tvUpWCPerc = getActivity().findViewById(R.id.tvWCPerc);
//
//        tvUpProd = getActivity().findViewById(R.id.tvProdCount);
//        tvUpProdPerc = getActivity().findViewById(R.id.tvProdPerc);
//
//        tvUpFocus = getActivity().findViewById(R.id.tvFocusCount);
//        tvUpFocusPerc = getActivity().findViewById(R.id.tvFocusPerc);

        chkSgi = getActivity().findViewById(R.id.chkSGI);
        chkGrup = getActivity().findViewById(R.id.chkGroups);
        chkNomen = getActivity().findViewById(R.id.chkNomen);
        chkContrs = getActivity().findViewById(R.id.chkContrs);
        chkAddrr = getActivity().findViewById(R.id.chkAddrs);
        chkTp = getActivity().findViewById(R.id.chkTorgPred);
        chkDebet = getActivity().findViewById(R.id.chkDebet);
        chkStatus = getActivity().findViewById(R.id.chkOrdStatus);
        chkOuted = getActivity().findViewById(R.id.chkOuted);
        chkSales = getActivity().findViewById(R.id.chkSales);
        chkGrupAccess = getActivity().findViewById(R.id.chkGrupAccess);
        chkUniMx = getActivity().findViewById(R.id.chkUniMX);
//        chkFuncs = getActivity().findViewById(R.id.chkFuncs);
//        chkTovcat = getActivity().findViewById(R.id.chkTovcat);
//        chkBrand = getActivity().findViewById(R.id.chkBrand);
//        chkWC = getActivity().findViewById(R.id.chkWC);
//        chkProd = getActivity().findViewById(R.id.chkProd);
//        chkFocus = getActivity().findViewById(R.id.chkFocus);

        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conn = null;
                reset = null;
                if (DebetIsFinished.equals("1") || DebetIsFinished.equals("0")) {
                    if (glbVars.isNetworkAvailable() == true) {
                        glbVars.UpdateWorking = 1;
                        btUpdate.setEnabled(false);
                        UpdateData();
                    } else {
                        Toast.makeText(getActivity(), "Нет доступного интернет соединения. Проверьте соединение с Интернетом", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


        try {
            getActivity().registerReceiver(UpdateDebetWorking, new IntentFilter("DebetUpdating"));
        } catch (Exception E) {
        }
    }

    private BroadcastReceiver UpdateDebetWorking= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DebetIsFinished = intent.getExtras().getString("DebetUpdateFinished");
        }
    };

    private void ConnectToSql(){
        String connString;

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
            connString = "jdbc:jtds:sqlserver://" + sql_server + ":" + sql_port + ";instance=MSSQLSERVER;databaseName=" + sql_db + ";user=" + sql_loging + ";password=" + sql_pass;
            conn = DriverManager.getConnection(connString, sql_loging, sql_pass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateData(){

        pgUpSgi.setProgress(0);
        pgUpGroups.setProgress(0);
        pgUpNomen.setProgress(0);
        pgUpContrs.setProgress(0);
        pgUpAddress.setProgress(0);
        pgUpTp.setProgress(0);
        pgUpDebet.setProgress(0);
        pgUpStatus.setProgress(0);
        pgUpOuted.setProgress(0);
        pgUpSales.setProgress(0);
        pgUpGrupAccess.setProgress(0);
        pgUpUniMX.setProgress(0);
//        pgUpFuncs.setProgress(0);
//        pgUpTovcat.setProgress(0);
//        pgUpBrand.setProgress(0);
//        pgUpWC.setProgress(0);
//        pgUpProd.setProgress(0);
//        pgUpFocus.setProgress(0);

        tvUpSgi.setText("0/0");
        tvUpSgiPerc.setText("0%");

        tvUpGroups.setText("0/0");
        tvUpGroupsPerc.setText("0%");

        tvUpNomen.setText("0/0");
        tvUpNomenPerc.setText("0%");

        tvUpContrs.setText("0/0");
        tvUpContrsPerc.setText("0%");

        tvUpAddress.setText("0/0");
        tvUpAddressPerc.setText("0%");

        tvUpTp.setText("0/0");
        tvUpTpPerc.setText("0%");

        tvUpDebet.setText("0/0");
        tvUpDebetPerc.setText("0%");

        tvUpStatus.setText("0/0");
        tvUpStatusPerc.setText("0%");

        tvUpOuted.setText("0/0");
        tvUpOutedPerc.setText("0%");

        tvUpSales.setText("0/0");
        tvUpSalesPerc.setText("0%");

        tvUpGrupAccess.setText("0/0");
        tvUpGrupAccessPerc.setText("0%");

        tvUpUniMX.setText("0/0");
        tvUpUniMXPerc.setText("0%");

//        tvUpFuncs.setText("0/0");
//        tvUpFuncsPerc.setText("0%");
//
//        tvUpTovcat.setText("0/0");
//        tvUpTovcatPerc.setText("0%");
//
//        tvUpBrand.setText("0/0");
//        tvUpBrandPerc.setText("0%");
//
//        tvUpWC.setText("0/0");
//        tvUpWCPerc.setText("0%");
//
//        tvUpProd.setText("0/0");
//        tvUpProdPerc.setText("0%");
//
//        tvUpFocus.setText("0/0");
//        tvUpFocusPerc.setText("0%");

        chkSgi.setChecked(false);
        chkGrup.setChecked(false);
        chkNomen.setChecked(false);
        chkContrs.setChecked(false);
        chkAddrr.setChecked(false);
        chkTp.setChecked(false);
        chkDebet.setChecked(false);
        chkStatus.setChecked(false);
        chkOuted.setChecked(false);
        chkSales.setChecked(false);
        chkGrupAccess.setChecked(false);
        chkUniMx.setChecked(false);
//        chkFuncs.setChecked(false);
//        chkTovcat.setChecked(false);
//        chkBrand.setChecked(false);
//        chkWC.setChecked(false);
//        chkProd.setChecked(false);
//        chkFocus.setChecked(false);

        chkSgi.setTextColor(Color.rgb(0, 0, 0));
        chkGrup.setTextColor(Color.rgb(0, 0, 0));
        chkNomen.setTextColor(Color.rgb(0, 0, 0));
        chkContrs.setTextColor(Color.rgb(0, 0, 0));
        chkAddrr.setTextColor(Color.rgb(0, 0, 0));
        chkTp.setTextColor(Color.rgb(0, 0, 0));
        chkDebet.setTextColor(Color.rgb(0, 0, 0));
        chkStatus.setTextColor(Color.rgb(0, 0, 0));
        chkOuted.setTextColor(Color.rgb(0, 0, 0));
        chkSales.setTextColor(Color.rgb(0, 0, 0));
        chkGrupAccess.setTextColor(Color.rgb(0, 0, 0));
        chkUniMx.setTextColor(Color.rgb(0, 0, 0));
//        chkFuncs.setTextColor(Color.rgb(0, 0, 0));
//        chkTovcat.setTextColor(Color.rgb(0, 0, 0));
//        chkBrand.setTextColor(Color.rgb(0, 0, 0));
//        chkWC.setTextColor(Color.rgb(0, 0, 0));
//        chkProd.setTextColor(Color.rgb(0, 0, 0));
//        chkFocus.setTextColor(Color.rgb(0, 0, 0));

        if (conn==null){
            ConnectToSql();
        }

        progressStatusSGI = 0;
        progressStatusGrup = 0;
        progressStatusNom = 0;
        progressStatusContr = 0;
        progressStatusAddr = 0;
        progressStatusTP = 0;
        progressStatusDeb = 0;
        progressStatusOrders = 0;
        progressStatusOuted = 0;
        progressStatusSales = 0;
        progressStatusAccess = 0;
        progressStatusUniMX = 0;
//        progressStatusFuncs = 0;
//        progressStatusTovcat = 0;
//        progressStatusBrand = 0;
//        progressStatusWC = 0;
//        progressStatusProd = 0;
//        progressStatusFocus = 0;


        thUpdateData = new Thread(new Runnable() {
            public void run() {
//              Обновляем список СГИ
                String sql = "";
                SQLiteStatement statement;
//                if (chkSgi.isChecked()){
                    sql = "INSERT INTO sgi(ID, DESCR, LOWDESCR)  VALUES (?,?,?);";
                    statement = glbVars.db.getWritableDatabase().compileStatement(sql);
                    int cntSgi = 0;
                    try {

                        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        String SqlSel = "";
                        if (TP_LOCK){
                            SqlSel = "SELECT DISTINCT ID ,DESCR, LOWDESCR FROM V_SGI_MOBILE_ARM JOIN V_TP_GRUP_ACCESS ON V_SGI_MOBILE_ARM.ID=V_TP_GRUP_ACCESS.SGI_ID WHERE V_TP_GRUP_ACCESS.TP_ID='"+TP_ID+"' ORDER BY 2";
                        } else {
                            SqlSel = "SELECT ID, DESCR, LOWDESCR FROM V_SGI_MOBILE_ARM ORDER BY 2";
                        }

                        reset = stmt.executeQuery(SqlSel);
                        reset.last();
                        cntSgi = reset.getRow();
                        pgUpSgi.setMax(cntSgi);
                        reset.beforeFirst();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        glbVars.db.getWritableDatabase().beginTransaction();
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM sgi; DELETE FROM sqlite_sequence WHERE name = 'sgi';");
                        String ID = "", Descr = "", LowDescr = "";
                        while (reset.next()) {
                            ID = reset.getString(1);
                            Descr = reset.getString(2);
                            Descr = Descr.replaceAll("'", "''");
                            LowDescr = reset.getString(3);
                            LowDescr = LowDescr.replaceAll("'", "''");
                            statement.clearBindings();
                            statement.bindString(1, ID);
                            statement.bindString(2, Descr);
                            statement.bindString(3, LowDescr);
                            statement.executeInsert();
                            statement.clearBindings();
                            progressStatusSGI += 1;
                            pgUpSgi.setProgress(progressStatusSGI);
                            final int finalCount = cntSgi;
                            final int perc = progressStatusSGI*100/cntSgi;
                            handlerSgi.post(new Runnable() {
                                public void run() {
                                    tvUpSgi.setText(String.valueOf(progressStatusSGI)+"/"+String.valueOf(finalCount));
                                    tvUpSgiPerc.setText(String.valueOf(perc)+"%");
                                }
                            });
                        }
                        handlerSgi.post(new Runnable() {
                            public void run() {
                                chkSgi.setChecked(true);
                                chkSgi.setTextColor(Color.rgb(3, 103, 0));
                            }
                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        glbVars.db.getWritableDatabase().setTransactionSuccessful();
                        glbVars.db.getWritableDatabase().endTransaction();
                    }
//                }
//              Конец обновления списка СГИ

//                Обновляем список типов цен
                reset = null;
//                    progressStatusContr = 0;
                sql = "INSERT INTO CEN_TYPES(ROW_ID, CEN_ID, DESCR)  VALUES (?,?,?);";
                statement = glbVars.db.getWritableDatabase().compileStatement(sql);
                try {
                    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    reset = stmt.executeQuery("SELECT [_id], [ID], [DESCR] FROM [V_CENTYPE_ARM_MOBILE] ORDER BY DESCR");
                    reset.beforeFirst();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    glbVars.db.getWritableDatabase().beginTransaction();
                    glbVars.db.getWritableDatabase().execSQL("DELETE FROM CEN_TYPES; DELETE FROM sqlite_sequence WHERE name = 'CEN_TYPES';");
                    String ID = "", Descr = "";
                    int ROW_ID = 0;
                    while (reset.next()) {
                        ROW_ID = reset.getInt(1);
                        ID = reset.getString(2);
                        Descr = reset.getString(3);

                        statement.clearBindings();
                        statement.bindLong(1, ROW_ID);
                        statement.bindString(2, ID);
                        statement.bindString(3, Descr);

                        statement.executeInsert();
                        statement.clearBindings();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
                    glbVars.db.getWritableDatabase().endTransaction();
                }
//                Конец обновления типов цен

//              Обновляем список товарных групп
//                if (chkGrup.isChecked()) {
                    reset = null;
//                    progressStatusGrup = 0;
                    sql = "INSERT INTO GRUPS(ID, SGIID, DESCR, LOWDESCR)  VALUES (?,?,?,?);";
                    statement = glbVars.db.getWritableDatabase().compileStatement(sql);
                    int cntGrups = 0;
                    try {
                        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        String SqlSel = "";
                        if (TP_LOCK){
                            SqlSel = "SELECT ID, V_GRUPS_MOBILE_ARM.SGI_ID, DESCR, CODE, LOWDESCR FROM V_GRUPS_MOBILE_ARM JOIN V_TP_GRUP_ACCESS ON V_GRUPS_MOBILE_ARM.ID=V_TP_GRUP_ACCESS.GRUP_ID WHERE V_TP_GRUP_ACCESS.TP_ID='"+TP_ID+"' ORDER BY 3";
                        } else {
                            SqlSel = "SELECT ID, SGI_ID, DESCR, CODE, LOWDESCR FROM V_GRUPS_MOBILE_ARM ORDER BY 3";
                        }

                        reset = stmt.executeQuery(SqlSel);
                        reset.last();
                        cntGrups = reset.getRow();
                        pgUpGroups.setMax(cntGrups);
                        reset.beforeFirst();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        glbVars.db.getWritableDatabase().beginTransaction();
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM GRUPS; DELETE FROM sqlite_sequence WHERE name = 'GRUPS';");
                        String ID = "", SGIID = "", Descr = "", LowDescr = "";
                        while (reset.next()) {
                            ID = reset.getString(1);
                            SGIID = reset.getString(2);
                            Descr = reset.getString(3);
                            Descr = Descr.replaceAll("'", "''");
                            LowDescr = reset.getString(5);
                            LowDescr = LowDescr.replaceAll("'", "''");
                            statement.clearBindings();
                            statement.bindString(1, ID);
                            statement.bindString(2, SGIID);
                            statement.bindString(3, Descr);
                            statement.bindString(4, LowDescr);
                            statement.executeInsert();
                            statement.clearBindings();
                            progressStatusGrup += 1;
                            pgUpGroups.setProgress(progressStatusGrup);
                            final int finalCount = cntGrups;
                            final int perc = progressStatusGrup * 100 / cntGrups;
                            handlerGroups.post(new Runnable() {
                                public void run() {
                                    tvUpGroups.setText(String.valueOf(progressStatusGrup) + "/" + String.valueOf(finalCount));
                                    tvUpGroupsPerc.setText(String.valueOf(perc) + "%");
                                }
                            });
                        }
                        handlerGroups.post(new Runnable() {
                            public void run() {
                                chkGrup.setChecked(true);
                                chkGrup.setTextColor(Color.rgb(3, 103, 0));
                            }
                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        glbVars.db.getWritableDatabase().setTransactionSuccessful();
                        glbVars.db.getWritableDatabase().endTransaction();
                    }
//                }
//              Конец обновления списка товарных групп

//              Обновляем список номенклатуры
//                if (chkNomen.isChecked()) {

                    reset = null;
//                    progressStatusNom = 0;
                    String sql_update = "UPDATE Nomen SET GRUPID=?, COD=?, DESCR=?, OST=?, PRICE=?, lowDESCR=?, SGIID=?, CODE=?, PHOTO1=?, PHOTO2=?, VKOROB=?, ISUPDATED=1, ISNEW=?, IS7DAY=?, IS28DAY=?, MP=?, IS_PERM=?, TOVCATID=?, FUNCID=?, BRANDID=?, WCID=?, PRODID=?, FOCUSID=?, MODELID=?, SIZEID=?, COLORID=? WHERE ID=?";
                    String sql_insert = "INSERT INTO Nomen(ID, GRUPID, COD, DESCR, OST, PRICE, lowDESCR, SGIID, CODE, PHOTO1, PHOTO2, VKOROB, ISUPDATED, ISNEW, IS7DAY, IS28DAY, MP, IS_PERM, TOVCATID, FUNCID, BRANDID, WCID, PRODID, FOCUSID, MODELID, SIZEID, COLORID)  VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
                    SQLiteStatement statement2 = glbVars.db.getWritableDatabase().compileStatement(sql_update);
                    statement = glbVars.db.getWritableDatabase().compileStatement(sql_insert);
                    int cntNomen = 0;
                    try {
                        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        String query, sqlView = "V_OST_MOBILE_ARM_UNI";
                        if (TP_LOCK){
                            query = "SELECT ID,PARENTID,CODE,DESCR,COD5,OST,CENA, "+sqlView+".SGI_ID, CASE WHEN PHOTO1 IS NULL THEN '' ELSE PHOTO1 END AS PHOTO1, CASE WHEN PHOTO2 IS NULL THEN '' ELSE PHOTO2 END AS PHOTO2, VKOROB, IS_NEW, IS_7DAY, IS_28DAY, MP, IN_PERM, FUNC_ID, TOVCAT_ID, BRAND_ID, WC_ID, PROD_ID, FOCUS_ID, MODEL_ID, SIZE, COLOR_ID FROM "+sqlView+" JOIN V_TP_GRUP_ACCESS ON "+sqlView+".PARENTID=V_TP_GRUP_ACCESS.GRUP_ID WHERE V_TP_GRUP_ACCESS.TP_ID='"+TP_ID+"' AND [SP327] = '"+ glbVars.CurrentCenType +"' ORDER BY 4";
                        } else {
                            query = "SELECT ID,PARENTID,CODE,DESCR,COD5,OST,CENA,SGI_ID, CASE WHEN PHOTO1 IS NULL THEN '' ELSE PHOTO1 END AS PHOTO1, CASE WHEN PHOTO2 IS NULL THEN '' ELSE PHOTO2 END AS PHOTO2, VKOROB, IS_NEW, IS_7DAY, IS_28DAY, MP, IN_PERM, FUNC_ID, TOVCAT_ID, BRAND_ID, WC_ID, PROD_ID, FOCUS_ID, MODEL_ID, SIZE, COLOR_ID FROM "+sqlView+" WHERE [SP327] = '"+ glbVars.CurrentCenType +"' ORDER BY 4";
                        }
                        reset = stmt.executeQuery(query);
                        reset.last();
                        cntNomen = reset.getRow();
                        pgUpNomen.setMax(cntNomen);
                        reset.beforeFirst();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        glbVars.db.getWritableDatabase().execSQL("UPDATE Nomen SET ISUPDATED=0, ISNEW=0, IS7DAY=0, IS28DAY=0");
                        glbVars.db.getWritableDatabase().beginTransaction();
                        String ID = "", PARENTID = "", Cod5 = "", Code = "", Descr = "", SgiID = "", Photo1 = "", Photo2 = "", MP="", TOVCAT = "", FUNC="", BRAND="", WC="", PROD="", FOCUS = "", MODEL = "", COLOR = "", SIZE = "";
                        int Ost = 0, Vkorob = 0, ISNEW = 0, IS7DAY = 0, IS28DAY = 0, IS_PERM = 0;
                        Float Price = 0.0f;
                        Cursor c;
                        while (reset.next()) {
                            ID = reset.getString(1);
                            PARENTID = reset.getString(2);
                            Cod5 = reset.getString(5);
                            Descr = reset.getString(4);
                            Descr = Descr.replaceAll("'", "''");
                            Code = reset.getString(3);
                            Ost = reset.getInt(6);
                            Price = reset.getFloat(7);
                            SgiID = reset.getString(8);
                            Photo1 = reset.getString(9);
                            Photo2 = reset.getString(10);
                            Vkorob = reset.getInt(11);
                            ISNEW  = reset.getInt(12);
                            IS7DAY  = reset.getInt(13);
                            IS28DAY  = reset.getInt(14);
                            MP = reset.getString(15);
                            IS_PERM  = reset.getInt(16);
                            FUNC = reset.getString(17);
                            TOVCAT = reset.getString(18);
                            BRAND = reset.getString(19);
                            WC = reset.getString(20);
                            PROD = reset.getString(21);
                            FOCUS = reset.getString(22);
                            MODEL = reset.getString(23);
                            SIZE = reset.getString(24);
                            COLOR = reset.getString(25);

                            c = glbVars.db.getWritableDatabase().rawQuery("SELECT 1 FROM Nomen WHERE ID='" + ID + "'", null);
                            if (c.moveToFirst()) {
                                    statement2.clearBindings();
                                    statement2.bindString(1, PARENTID);
                                    statement2.bindString(2, Cod5);
                                    statement2.bindString(3, Descr);
                                    statement2.bindLong(4, Ost);
                                    statement2.bindDouble(5, Price);
                                    statement2.bindString(6, Descr.toLowerCase());
                                    statement2.bindString(7, SgiID);
                                    statement2.bindString(8, Code);
                                    statement2.bindString(9, Photo1);
                                    statement2.bindString(10, Photo2);
                                    statement2.bindLong(11, Vkorob);
                                    statement2.bindLong(12, ISNEW);
                                    statement2.bindLong(13, IS7DAY);
                                    statement2.bindLong(14, IS28DAY);
                                    statement2.bindString(15, MP);
                                    statement2.bindLong(16, IS_PERM);
                                    statement2.bindString(17, TOVCAT);
                                    statement2.bindString(18, FUNC);
                                    statement2.bindString(19, BRAND);
                                    statement2.bindString(20, WC);
                                    statement2.bindString(21, PROD);
                                    statement2.bindString(22, FOCUS);
                                    statement2.bindString(23, MODEL);
                                    statement2.bindString(24, SIZE.toLowerCase());
                                    statement2.bindString(25, COLOR);
                                    statement2.bindString(26, ID);

                                    statement2.executeUpdateDelete();
                                    statement2.clearBindings();
                                } else {
                                    statement.clearBindings();
                                    statement.bindString(1, ID);
                                    statement.bindString(2, PARENTID);
                                    statement.bindString(3, Cod5);
                                    statement.bindString(4, Descr);
                                    statement.bindLong(5, Ost);
                                    statement.bindDouble(6, Price);
                                    statement.bindString(7, Descr.toLowerCase());
                                    statement.bindString(8, SgiID);
                                    statement.bindString(9, Code);
                                    statement.bindString(10, Photo1);
                                    statement.bindString(11, Photo2);
                                    statement.bindLong(12, Vkorob);
                                    statement.bindLong(13, 1);
                                    statement.bindLong(14, ISNEW);
                                    statement.bindLong(15, IS7DAY);
                                    statement.bindLong(16, IS28DAY);
                                    statement.bindString(17, MP);
                                    statement.bindLong(18, IS_PERM);
                                    statement.bindString(19, TOVCAT);
                                    statement.bindString(20, FUNC);
                                    statement.bindString(21, BRAND);
                                    statement.bindString(22, WC);
                                    statement.bindString(23, PROD);
                                    statement.bindString(24, FOCUS);
                                    statement.bindString(25, MODEL);
                                    statement.bindString(26, SIZE.toLowerCase());
                                    statement.bindString(27, COLOR);
                                    statement.executeInsert();
                                    statement.clearBindings();
                                }

                            c.close();
                            progressStatusNom += 1;
                            pgUpNomen.setProgress(progressStatusNom);
                            final int finalCount = cntNomen;
                            final int perc = progressStatusNom * 100 / cntNomen;
                            handlerNom.post(new Runnable() {
                                public void run() {
                                    tvUpNomen.setText(String.valueOf(progressStatusNom) + "/" + String.valueOf(finalCount));
                                    tvUpNomenPerc.setText(String.valueOf(perc) + "%");
                                }
                            });
                        }
                        handlerNom.post(new Runnable() {
                            public void run() {
                                chkNomen.setChecked(true);
                                chkNomen.setTextColor(Color.rgb(3, 103, 0));
                            }
                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        glbVars.db.getWritableDatabase().execSQL("UPDATE Nomen SET OST=0 WHERE ISUPDATED=0");
                        glbVars.db.getWritableDatabase().setTransactionSuccessful();
                        glbVars.db.getWritableDatabase().endTransaction();
                    }



//                }
//              Конец обновления списка номенклатуры

//              Обновляем список контрагентов
//                if (chkContrs.isChecked()) {
                    reset = null;
//                    progressStatusContr = 0;
                    sql = "INSERT INTO CONTRS(ID, DESCR, lowDESCR, CODE, INSTOP, DOLG, DYNAMO, TP, INFO, CRT_DATE)  VALUES (?,?,?,?,?,?,?,?,?,?);";
                    statement = glbVars.db.getWritableDatabase().compileStatement(sql);
                    int cntContrs = 0;
                    try {
                        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        reset = stmt.executeQuery("SELECT ID,DESCR,CODE, INSTOP, DOLG, DYNAMO,TP, CASE WHEN [INFO] IS NULL THEN '' ELSE INFO END AS INFO, CRT_DATE FROM V_CONTRS_MOBILE_ARM ORDER BY 2");
                        reset.last();
                        cntContrs = reset.getRow();
                        pgUpContrs.setMax(cntContrs);
                        reset.beforeFirst();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        glbVars.db.getWritableDatabase().beginTransaction();
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM CONTRS; DELETE FROM sqlite_sequence WHERE name = 'CONTRS';");
                        String ID = "", Descr = "", Code = "", TP="", contrInfo = "", Crt_date ="";
                        int Instop = 0, Dolg = 0, Dynamo = 0;
                        while (reset.next()) {
                            ID = reset.getString(1);
                            Code = reset.getString(3);
                            Descr = reset.getString(2);
                            Descr = Descr.replaceAll("'", "''");
                            Instop = reset.getInt(4);
                            Dolg = reset.getInt(5);
                            Dynamo = reset.getInt(6);
                            TP = reset.getString(7);
                            contrInfo = reset.getString(8);
                            Crt_date = reset.getString(9);

                            statement.clearBindings();
                            statement.bindString(1, ID);
                            statement.bindString(2, Descr);
                            statement.bindString(3, Descr.toLowerCase());
                            statement.bindString(4, Code);
                            statement.bindLong(5, Instop);
                            statement.bindLong(6, Dolg);
                            statement.bindLong(7, Dynamo);
                            statement.bindString(8, TP);
                            statement.bindString(9, contrInfo);
                            statement.bindString(10, Crt_date);

                            statement.executeInsert();
                            statement.clearBindings();
                            progressStatusContr += 1;
                            pgUpContrs.setProgress(progressStatusContr);
                            final int finalCount = cntContrs;
                            final int perc = progressStatusContr * 100 / cntContrs;
                            handlerContr.post(new Runnable() {
                                public void run() {
                                    tvUpContrs.setText(String.valueOf(progressStatusContr) + "/" + String.valueOf(finalCount));
                                    tvUpContrsPerc.setText(String.valueOf(perc) + "%");
                                }
                            });
                        }
                        handlerContr.post(new Runnable() {
                            public void run() {
                                chkContrs.setChecked(true);
                                chkContrs.setTextColor(Color.rgb(3, 103, 0));
                            }
                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        glbVars.db.getWritableDatabase().setTransactionSuccessful();
                        glbVars.db.getWritableDatabase().endTransaction();
                    }
//                }
//              Конец обновления списка контрагентов

//              Обновляем список адресов
//                if (chkAddrr.isChecked()) {
//                    progressStatusAddr = 0;
                    sql = "INSERT INTO ADDRS(ID, PARENTEXT, DESCR, CODE, DOP_INFO)  VALUES (?,?,?,?,?);";
                    statement = glbVars.db.getWritableDatabase().compileStatement(sql);
                    int cntAddrs = 0;
                    try {
                        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        reset = stmt.executeQuery("SELECT ID, PARENTEXT ,DESCR, CODE, DOP_INFO FROM V_ADDRS_MOBILE_ARM ORDER BY 2, 3");
                        reset.last();
                        cntAddrs = reset.getRow();
                        pgUpAddress.setMax(cntAddrs);
                        reset.beforeFirst();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        glbVars.db.getWritableDatabase().beginTransaction();
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM ADDRS; DELETE FROM sqlite_sequence WHERE name = 'ADDRS';");
                        String ID = "", PARENTID = "", Descr = "", Code = "", Dop_Info = "";
                        while (reset.next()) {
                            ID = reset.getString(1);
                            PARENTID = reset.getString(2);
                            Descr = reset.getString(3);
                            Code = reset.getString(4);
                            Dop_Info = reset.getString(5);
                            Descr = Descr.replaceAll("'", "''");
                            statement.clearBindings();
                            statement.bindString(1, ID);
                            statement.bindString(2, PARENTID);
                            statement.bindString(3, Descr);
                            statement.bindString(4, Code);
                            statement.bindString(5, Dop_Info);
                            statement.executeInsert();
                            statement.clearBindings();
                            progressStatusAddr += 1;
                            pgUpAddress.setProgress(progressStatusAddr);
                            final int finalCount = cntAddrs;
                            final int perc = progressStatusAddr * 100 / cntAddrs;
                            handlerAddr.post(new Runnable() {
                                public void run() {
                                    tvUpAddress.setText(String.valueOf(progressStatusAddr) + "/" + String.valueOf(finalCount));
                                    tvUpAddressPerc.setText(String.valueOf(perc) + "%");
                                }
                            });
                        }
                        handlerAddr.post(new Runnable() {
                            public void run() {
                                chkAddrr.setChecked(true);
                                chkAddrr.setTextColor(Color.rgb(3, 103, 0));
                            }
                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        glbVars.db.getWritableDatabase().setTransactionSuccessful();
                        glbVars.db.getWritableDatabase().endTransaction();
                    }
//                }
//              Конец обновления списка адресов

//              Обновляем список торговых представителей
//                if (chkTp.isChecked()) {
//                    progressStatusTP = 0;
                    sql = "INSERT INTO TORG_PRED(ID, DESCR, CODE, TP_PASS)  VALUES (?,?,?,?);";
                    statement = glbVars.db.getWritableDatabase().compileStatement(sql);
                    int cntTP = 0;
                    try {
                        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        reset = stmt.executeQuery("SELECT ID, DESCR, CODE, PASS FROM V_TP_MOBILE_ARM ORDER BY 2");
                        reset.last();
                        cntTP = reset.getRow();
                        pgUpTp.setMax(cntTP);
                        reset.beforeFirst();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM TORG_PRED; DELETE FROM sqlite_sequence WHERE name = 'TORG_PRED';");
                        glbVars.db.getWritableDatabase().beginTransaction();
                        String ID = "", Descr = "", Code = "", TP_PASS = "";
                        while (reset.next()) {
                            ID = reset.getString(1);
                            Descr = reset.getString(2);
                            Code = reset.getString(3);
                            TP_PASS = reset.getString(4);
                            statement.clearBindings();
                            statement.bindString(1, ID);
                            statement.bindString(2, Descr);
                            statement.bindString(3, Code);
                            statement.bindString(4, TP_PASS);
                            statement.executeInsert();
                            statement.clearBindings();
                            progressStatusTP += 1;
                            pgUpTp.setProgress(progressStatusTP);
                            final int finalCount = cntTP;
                            final int perc = progressStatusTP * 100 / cntTP;
                            handlerTP.post(new Runnable() {
                                public void run() {
                                    tvUpTp.setText(String.valueOf(progressStatusTP) + "/" + String.valueOf(finalCount));
                                    tvUpTpPerc.setText(String.valueOf(perc) + "%");
                                }
                            });
                        }
                        handlerTP.post(new Runnable() {
                            public void run() {
                                chkTp.setChecked(true);
                                chkTp.setTextColor(Color.rgb(3, 103, 0));
                            }
                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        glbVars.db.getWritableDatabase().setTransactionSuccessful();
                        glbVars.db.getWritableDatabase().endTransaction();
                    }
//                }
//              Конец обновления списка торговых представителей

//              Обновляем дебиторку
//                if (chkDebet.isChecked()) {
//                    progressStatusDeb = 0;
                    sql = "INSERT INTO DEBET(ROW, CONTR_ID, KREDIT, LIM, NEKONTR, SALDO, A7, A14, A21, A28, TP_ID, TP_IDS, A35, A42, A49, A56, A63, A64, OTG30, OPL30, FIRMA)  VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
                    statement = glbVars.db.getWritableDatabase().compileStatement(sql);
                    int cntDeb = 0;
                    try {
                        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        reset = stmt.executeQuery("SELECT ROW_ID,CONTR_ID,KREDIT,LIMIT,NEKONTR,SALDO,A7,A14,A21,A28,TPID,TPID_LIST, A35, A42, A49, A56, A63, A64, OTG30, OPL30, FIRMA FROM V_DEBET_MOBILE_ARM ORDER BY CONTR_ID");
                        reset.last();
                        cntDeb = reset.getRow();
                        pgUpDebet.setMax(cntDeb);
                        reset.beforeFirst();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        glbVars.db.getWritableDatabase().beginTransaction();
                        glbVars.db.getWritableDatabase().execSQL("DELETE FROM DEBET; DELETE FROM sqlite_sequence WHERE name = 'DEBET';");
                        String ID = "", TP_ID = "", TP_IDS = "", FIRMA = "";
                        int Rowid = 0, Kredit = 0, Limit = 0, Nekontr = 0;
                        Float Saldo = 0.0f, a7 = 0.0f, a14 = 0.0f, a21 = 0.0f, a28 = 0.0f, a35 = 0.0f, a42 = 0.0f, a49 = 0.0f, a56 = 0.0f, a63 = 0.0f, a64 = 0.0f, otg30 = 0.0f, opl30 = 0.0f;
                        while (reset.next()) {
                            Rowid = reset.getInt(1);
                            ID = reset.getString(2);
                            Kredit = reset.getInt(3);
                            Limit = reset.getInt(4);
                            Nekontr = reset.getInt(5);
                            Saldo = reset.getFloat(6);
                            a7 = reset.getFloat(7);
                            a14 = reset.getFloat(8);
                            a21 = reset.getFloat(9);
                            a28 = reset.getFloat(10);
                            TP_ID = reset.getString(11);
                            TP_IDS = reset.getString(12);
                            a35 = reset.getFloat(13);
                            a42 = reset.getFloat(14);
                            a49 = reset.getFloat(15);
                            a56 = reset.getFloat(16);
                            a63 = reset.getFloat(17);
                            a64 = reset.getFloat(18);
                            otg30 = reset.getFloat(19);
                            opl30 = reset.getFloat(20);
                            FIRMA = reset.getString(21);

                            statement.clearBindings();
                            statement.bindLong(1, Rowid);
                            statement.bindString(2, ID);
                            statement.bindLong(3, Kredit);
                            statement.bindLong(4, Limit);
                            statement.bindLong(5, Nekontr);
                            statement.bindDouble(6, Saldo);
                            statement.bindDouble(7, a7);
                            statement.bindDouble(8, a14);
                            statement.bindDouble(9, a21);
                            statement.bindDouble(10, a28);
                            statement.bindString(11, TP_ID);
                            statement.bindString(12, TP_IDS);
                            statement.bindDouble(13, a35);
                            statement.bindDouble(14, a42);
                            statement.bindDouble(15, a49);
                            statement.bindDouble(16, a56);
                            statement.bindDouble(17, a63);
                            statement.bindDouble(18, a64);
                            statement.bindDouble(19, otg30);
                            statement.bindDouble(20, opl30);
                            statement.bindString(21, FIRMA);
                            statement.executeInsert();
                            statement.clearBindings();
                            progressStatusDeb += 1;
                            pgUpDebet.setProgress(progressStatusDeb);
                            final int finalCount = cntDeb;
                            final int perc = progressStatusDeb * 100 / cntDeb;
                            handlerDebet.post(new Runnable() {
                                public void run() {
                                    tvUpDebet.setText(String.valueOf(progressStatusDeb) + "/" + String.valueOf(finalCount));
                                    tvUpDebetPerc.setText(String.valueOf(perc) + "%");
                                }
                            });
                        }
                        handlerDebet.post(new Runnable() {
                            public void run() {
                                chkDebet.setChecked(true);
                                chkDebet.setTextColor(Color.rgb(3, 103, 0));
                            }
                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        glbVars.db.getWritableDatabase().setTransactionSuccessful();
                        glbVars.db.getWritableDatabase().endTransaction();
                    }
//                }
//                Конец обновления дебиторки

//                Обновляем статусы заказов
//                progressStatusOrders = 0;
                sql = "UPDATE ZAKAZY SET STATUS=? WHERE DOCNO=? AND TP_ID=? AND CONTR_ID=?";
                statement = glbVars.db.getWritableDatabase().compileStatement(sql);
                int cntOrders = 0;
                try {
                    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    reset = stmt.executeQuery("SELECT BASEDOC, CONTR ,TP, ORD_STATUS FROM V_ORDER_STATUS ORDER BY BASEDOC");
                    reset.last();
                    cntOrders = reset.getRow();
                    pgUpStatus.setMax(cntOrders);
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
                        c = glbVars.db.getWritableDatabase().rawQuery("SELECT DOCNO FROM ZAKAZY WHERE DOCNO='" + ID + "' AND TP_ID='"+TP+"' AND CONTR_ID='"+CONTR+"'", null);
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
                        progressStatusOrders += 1;
                        pgUpStatus.setProgress(progressStatusOrders);
                        final int finalCount = cntOrders;
                        final int perc = progressStatusOrders * 100 / cntOrders;
                        handlerStatus.post(new Runnable() {
                            public void run() {
                                tvUpStatus.setText(String.valueOf(progressStatusOrders) + "/" + String.valueOf(finalCount));
                                tvUpStatusPerc.setText(String.valueOf(perc) + "%");
                            }
                        });
                    }
                    handlerStatus.post(new Runnable() {
                        public void run() {
                            chkStatus.setChecked(true);
                            chkStatus.setTextColor(Color.rgb(3, 103, 0));
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
                    glbVars.db.getWritableDatabase().endTransaction();
                }
//              Конец обновления статусов заказов

//              Обновляем вычерки
//                progressStatusOuted = 0;
                sql = "UPDATE ZAKAZY_DT SET IS_OUTED=?, OUT_QTY=? WHERE ZAKAZ_ID=? AND NOM_ID=?";
                statement = glbVars.db.getWritableDatabase().compileStatement(sql);
                int cntOuted = 0;
                try {
                    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    reset = stmt.executeQuery("SELECT ZAK_ID, NOM_ID, QTY FROM V_OUTED_NOM ORDER BY ZAK_ID");
                    reset.last();
                    cntOuted = reset.getRow();
                    pgUpOuted.setMax(cntOuted);
                    reset.beforeFirst();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    glbVars.db.getWritableDatabase().beginTransaction();
                    String NOM_ID = "", ZAK_ID = "";
                    int STATUS = 1, OUT_QTY = 0;
                    Cursor c;
                    while (reset.next()) {
                        ZAK_ID = reset.getString(1);
                        NOM_ID = reset.getString(2);
                        OUT_QTY = reset.getInt(3);

                        c = glbVars.db.getWritableDatabase().rawQuery("SELECT ZAKAZ_ID FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + ZAK_ID + "' AND NOM_ID='"+NOM_ID+"'", null);
                        if (c.moveToFirst()) {
                            statement.clearBindings();
                            statement.bindLong(1, STATUS);
                            statement.bindLong(2, OUT_QTY);
                            statement.bindString(3, ZAK_ID);
                            statement.bindString(4, NOM_ID);
                            statement.executeUpdateDelete();
                            statement.clearBindings();
                        }
                        c.close();
                        progressStatusOuted += 1;
                        pgUpOuted.setProgress(progressStatusOuted);
                        final int finalCount = cntOuted;
                        final int perc = progressStatusOuted * 100 / cntOuted;
                        handlerOut.post(new Runnable() {
                            public void run() {
                                tvUpOuted.setText(String.valueOf(progressStatusOuted) + "/" + String.valueOf(finalCount));
                                tvUpOutedPerc.setText(String.valueOf(perc) + "%");
                            }
                        });
                    }
                    handlerOut.post(new Runnable() {
                        public void run() {
                            chkOuted.setChecked(true);
                            chkOuted.setTextColor(Color.rgb(3, 103, 0));
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
                    glbVars.db.getWritableDatabase().endTransaction();
                }
//              Конец обновления вычерков

//              Обновляем скидки
//                if (chkSales.isChecked()) {
//                progressStatusSales = 0;
                sql = "INSERT INTO SALES(CONTR_ID, GRUP_ID, SALE)  VALUES (?,?,?);";
                statement = glbVars.db.getWritableDatabase().compileStatement(sql);
                int cntSales = 0;
                try {
                    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    reset = stmt.executeQuery("SELECT CONTR_ID, GRUP_ID, SALE FROM V_SALE_MOBILE_ARM ORDER BY CONTR_ID, GRUP_ID");
                    reset.last();
                    cntSales = reset.getRow();
                    pgUpSales.setMax(cntSales);
                    reset.beforeFirst();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    glbVars.db.getWritableDatabase().execSQL("DELETE FROM SALES; DELETE FROM sqlite_sequence WHERE name = 'SALES';");
                    glbVars.db.getWritableDatabase().beginTransaction();
                    String CONTR_ID = "", GRUP_ID = "";
                    Float SALE = 0.0f;
                    while (reset.next()) {
                        CONTR_ID = reset.getString(1);
                        GRUP_ID = reset.getString(2);
                        SALE = reset.getFloat(3);
                        statement.clearBindings();
                        statement.bindString(1, CONTR_ID);
                        statement.bindString(2, GRUP_ID);
                        statement.bindDouble(3, SALE);
                        statement.executeInsert();
                        statement.clearBindings();
                        progressStatusSales += 1;
                        pgUpSales.setProgress(progressStatusSales);
                        final int finalCount = cntSales;
                        final int perc = progressStatusSales * 100 / cntSales;
                        handlerSales.post(new Runnable() {
                            public void run() {
                                tvUpSales.setText(String.valueOf(progressStatusSales) + "/" + String.valueOf(finalCount));
                                tvUpSalesPerc.setText(String.valueOf(perc) + "%");
                            }
                        });
                    }
                    handlerSales.post(new Runnable() {
                        public void run() {
                            chkSales.setChecked(true);
                            chkSales.setTextColor(Color.rgb(3, 103, 0));
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
                    glbVars.db.getWritableDatabase().endTransaction();
                }
//                }
//              Конец обновления скидок

//              Обновляем ограничения товарных групп
//                if (chkSales.isChecked()) {
//                progressStatusAccess = 0;
                sql = "INSERT INTO TP_GRUP_ACCESS(TP_ID, SGI_ID, GRUP_ID)  VALUES (?,?,?);";
                statement = glbVars.db.getWritableDatabase().compileStatement(sql);
                int cntGrupAccess = 0;
                try {
                    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    reset = stmt.executeQuery("SELECT TP_ID, SGI_ID, GRUP_ID FROM V_TP_GRUP_ACCESS ORDER BY 1, 3, 2");
                    reset.last();
                    cntGrupAccess = reset.getRow();
//                    System.out.println("cntGrupAccess: " + cntGrupAccess);
                    pgUpGrupAccess.setMax(cntGrupAccess);
                    reset.beforeFirst();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    glbVars.db.getWritableDatabase().execSQL("DELETE FROM TP_GRUP_ACCESS; DELETE FROM sqlite_sequence WHERE name = 'TP_GRUP_ACCESS';");
                    glbVars.db.getWritableDatabase().beginTransaction();
                    String TP_ID = "", GRUP_ID = "", SGI_ID = "";
                    while (reset.next()) {
                        TP_ID = reset.getString(1);
                        SGI_ID = reset.getString(2);
                        GRUP_ID = reset.getString(3);
                        statement.clearBindings();
                        statement.bindString(1, TP_ID);
                        statement.bindString(2, SGI_ID);
                        statement.bindString(3, GRUP_ID);
                        statement.executeInsert();
                        statement.clearBindings();
                        progressStatusAccess += 1;
                        pgUpGrupAccess.setProgress(progressStatusAccess);
                        final int finalCount = cntGrupAccess;
                        final int perc = progressStatusAccess * 100 / cntGrupAccess;
                        handlerSales.post(new Runnable() {
                            public void run() {
                                tvUpGrupAccess.setText(String.valueOf(progressStatusAccess) + "/" + String.valueOf(finalCount));
                                tvUpGrupAccessPerc.setText(String.valueOf(perc) + "%");
                            }
                        });
                    }
                    handlerGrupAccess.post(new Runnable() {
                        public void run() {
                            chkGrupAccess.setChecked(true);
                            chkGrupAccess.setTextColor(Color.rgb(3, 103, 0));
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
                    glbVars.db.getWritableDatabase().endTransaction();
                }
//                }
//              Конец обновления ограничений товарных групп

//                Обновление универсальной матрицы 23-04-2020
                sql = "INSERT INTO UNI_MATRIX(TYPE_ID, TYPE_DESCR, ID, DESCR, LOWDESCR)  VALUES (?,?,?,?,?);";
                statement = glbVars.db.getWritableDatabase().compileStatement(sql);
                int cntUniMX = 0;
                try {

                    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    String SqlSel = "";
                    SqlSel = "SELECT [TYPE_ID], [TYPE_DESCR], [ID], [DESCR], [LOWDESCR] FROM [V_UNI_MATRIX](NOLOCK) ORDER BY [TYPE_ID], [DESCR]";

                    reset = stmt.executeQuery(SqlSel);
                    reset.last();
                    cntUniMX = reset.getRow();
                    pgUpUniMX.setMax(cntUniMX);
                    reset.beforeFirst();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    glbVars.db.getWritableDatabase().beginTransaction();
                    glbVars.db.getWritableDatabase().execSQL("DELETE FROM UNI_MATRIX; DELETE FROM sqlite_sequence WHERE name = 'UNI_MATRIX';");
                    String Type_ID = "", Type_Descr = "", ID = "", Descr = "", LowDescr = "";
                    while (reset.next()) {
                        Type_ID  = reset.getString(1);
                        Type_Descr = reset.getString(2);
                        Type_Descr = Type_Descr.replaceAll("'", "''");
                        ID = reset.getString(3);
                        Descr = reset.getString(4);
                        Descr = Descr.replaceAll("'", "''");
                        LowDescr = reset.getString(5);
                        LowDescr = LowDescr.replaceAll("'", "''");
                        statement.clearBindings();
                        statement.bindString(1, Type_ID);
                        statement.bindString(2, Type_Descr);
                        statement.bindString(3, ID);
                        statement.bindString(4, Descr);
                        statement.bindString(5, LowDescr);
                        statement.executeInsert();
                        statement.clearBindings();
                        progressStatusUniMX += 1;
                        pgUpUniMX.setProgress(progressStatusUniMX);
                        final int finalCount = cntUniMX;
                        final int perc = progressStatusUniMX*100/cntUniMX;
                        handlerUniMX.post(new Runnable() {
                            public void run() {
                                tvUpUniMX.setText(String.valueOf(progressStatusUniMX)+"/"+String.valueOf(finalCount));
                                tvUpUniMXPerc.setText(String.valueOf(perc)+"%");
                            }
                        });
                    }
                    handlerUniMX.post(new Runnable() {
                        public void run() {
                            chkUniMx.setChecked(true);
                            chkUniMx.setTextColor(Color.rgb(3, 103, 0));
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
                    glbVars.db.getWritableDatabase().endTransaction();
                }
//                Конец обновления универсальной матрицы 23-04-2020

////              Обновляем список Функциональных групп
////                if (chkSgi.isChecked()){
//                sql = "INSERT INTO FUNC(ID, DESCR, LOWDESCR)  VALUES (?,?,?);";
//                statement = glbVars.db.getWritableDatabase().compileStatement(sql);
//                int cntFunc = 0;
//                try {
//
//                    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//                    String SqlSel = "";
//                    SqlSel = "SELECT ID, DESCR, LOWDESCR FROM V_FUNC ORDER BY 2";
//
//                    reset = stmt.executeQuery(SqlSel);
//                    reset.last();
//                    cntFunc = reset.getRow();
//                    pgUpFuncs.setMax(cntFunc);
//                    reset.beforeFirst();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                try {
//                    glbVars.db.getWritableDatabase().beginTransaction();
//                    glbVars.db.getWritableDatabase().execSQL("DELETE FROM FUNC; DELETE FROM sqlite_sequence WHERE name = 'FUNC';");
//                    String ID = "", Descr = "", LowDescr = "";
//                    while (reset.next()) {
//                        ID = reset.getString(1);
//                        Descr = reset.getString(2);
//                        Descr = Descr.replaceAll("'", "''");
//                        LowDescr = reset.getString(3);
//                        LowDescr = LowDescr.replaceAll("'", "''");
//                        statement.clearBindings();
//                        statement.bindString(1, ID);
//                        statement.bindString(2, Descr);
//                        statement.bindString(3, LowDescr);
//                        statement.executeInsert();
//                        statement.clearBindings();
//                        progressStatusFuncs += 1;
//                        pgUpFuncs.setProgress(progressStatusFuncs);
//                        final int finalCount = cntFunc;
//                        final int perc = progressStatusFuncs*100/cntFunc;
//                        handlerFuncs.post(new Runnable() {
//                            public void run() {
//                                tvUpFuncs.setText(String.valueOf(progressStatusFuncs)+"/"+String.valueOf(finalCount));
//                                tvUpFuncsPerc.setText(String.valueOf(perc)+"%");
//                            }
//                        });
//                    }
//                    handlerFuncs.post(new Runnable() {
//                        public void run() {
//                            chkFuncs.setChecked(true);
//                            chkFuncs.setTextColor(Color.rgb(3, 103, 0));
//                        }
//                    });
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                } finally {
//                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
//                    glbVars.db.getWritableDatabase().endTransaction();
//                }
////                }
////              Конец обновления списка Функциональных групп
//
////              Обновляем список Товарных категорий
////                if (chkSgi.isChecked()){
//                sql = "INSERT INTO TOVCAT(ID, DESCR, LOWDESCR)  VALUES (?,?,?);";
//                statement = glbVars.db.getWritableDatabase().compileStatement(sql);
//                int cntTovcat = 0;
//                try {
//
//                    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//                    String SqlSel = "";
//                    SqlSel = "SELECT ID, DESCR, LOWDESCR FROM V_TOVCAT ORDER BY 2";
//
//                    reset = stmt.executeQuery(SqlSel);
//                    reset.last();
//                    cntTovcat = reset.getRow();
//                    pgUpTovcat.setMax(cntTovcat);
//                    reset.beforeFirst();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                try {
//                    glbVars.db.getWritableDatabase().beginTransaction();
//                    glbVars.db.getWritableDatabase().execSQL("DELETE FROM TOVCAT; DELETE FROM sqlite_sequence WHERE name = 'TOVCAT';");
//                    String ID = "", Descr = "", LowDescr = "";
//                    while (reset.next()) {
//                        ID = reset.getString(1);
//                        Descr = reset.getString(2);
//                        Descr = Descr.replaceAll("'", "''");
//                        LowDescr = reset.getString(3);
//                        LowDescr = LowDescr.replaceAll("'", "''");
//                        statement.clearBindings();
//                        statement.bindString(1, ID);
//                        statement.bindString(2, Descr);
//                        statement.bindString(3, LowDescr);
//                        statement.executeInsert();
//                        statement.clearBindings();
//                        progressStatusTovcat += 1;
//                        pgUpTovcat.setProgress(progressStatusTovcat);
//                        final int finalCount = cntTovcat;
//                        final int perc = progressStatusTovcat*100/cntTovcat;
//                        handlerTovcat.post(new Runnable() {
//                            public void run() {
//                                tvUpTovcat.setText(String.valueOf(progressStatusTovcat)+"/"+String.valueOf(finalCount));
//                                tvUpTovcatPerc.setText(String.valueOf(perc)+"%");
//                            }
//                        });
//                    }
//                    handlerTovcat.post(new Runnable() {
//                        public void run() {
//                            chkTovcat.setChecked(true);
//                            chkTovcat.setTextColor(Color.rgb(3, 103, 0));
//                        }
//                    });
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                } finally {
//                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
//                    glbVars.db.getWritableDatabase().endTransaction();
//                }
////                }
////              Конец обновления списка Товарных категорий
//
////              Обновляем список Брэндов
////                if (chkSgi.isChecked()){
//                sql = "INSERT INTO BRAND(ID, DESCR, LOWDESCR)  VALUES (?,?,?);";
//                statement = glbVars.db.getWritableDatabase().compileStatement(sql);
//                int cntBrand = 0;
//                try {
//
//                    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//                    String SqlSel = "";
//                    SqlSel = "SELECT ID, DESCR, LOWDESCR FROM V_BRAND ORDER BY 2";
//
//                    reset = stmt.executeQuery(SqlSel);
//                    reset.last();
//                    cntBrand = reset.getRow();
//                    pgUpBrand.setMax(cntBrand);
//                    reset.beforeFirst();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                try {
//                    glbVars.db.getWritableDatabase().beginTransaction();
//                    glbVars.db.getWritableDatabase().execSQL("DELETE FROM BRAND; DELETE FROM sqlite_sequence WHERE name = 'BRAND';");
//                    String ID = "", Descr = "", LowDescr = "";
//                    while (reset.next()) {
//                        ID = reset.getString(1);
//                        Descr = reset.getString(2);
//                        Descr = Descr.replaceAll("'", "''");
//                        LowDescr = reset.getString(3);
//                        LowDescr = LowDescr.replaceAll("'", "''");
//                        statement.clearBindings();
//                        statement.bindString(1, ID);
//                        statement.bindString(2, Descr);
//                        statement.bindString(3, LowDescr);
//                        statement.executeInsert();
//                        statement.clearBindings();
//                        progressStatusBrand += 1;
//                        pgUpBrand.setProgress(progressStatusBrand);
//                        final int finalCount = cntBrand;
//                        final int perc = progressStatusBrand*100/cntBrand;
//                        handlerBrand.post(new Runnable() {
//                            public void run() {
//                                tvUpBrand.setText(String.valueOf(progressStatusBrand)+"/"+String.valueOf(finalCount));
//                                tvUpBrandPerc.setText(String.valueOf(perc)+"%");
//                            }
//                        });
//                    }
//                    handlerBrand.post(new Runnable() {
//                        public void run() {
//                            chkBrand.setChecked(true);
//                            chkBrand.setTextColor(Color.rgb(3, 103, 0));
//                        }
//                    });
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                } finally {
//                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
//                    glbVars.db.getWritableDatabase().endTransaction();
//                }
////                }
////              Конец обновления списка Брэндов
//
//
////              Обновляем список демографических признаков
////                if (chkSgi.isChecked()){
//                sql = "INSERT INTO WC(ID, DESCR, LOWDESCR)  VALUES (?,?,?);";
//                statement = glbVars.db.getWritableDatabase().compileStatement(sql);
//                int cntWC = 0;
//                try {
//
//                    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//                    String SqlSel = "";
//                    SqlSel = "SELECT ID, DESCR, LOWDESCR FROM V_WC ORDER BY 2";
//
//                    reset = stmt.executeQuery(SqlSel);
//                    reset.last();
//                    cntWC = reset.getRow();
//                    pgUpWC.setMax(cntWC);
//                    reset.beforeFirst();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                try {
//                    glbVars.db.getWritableDatabase().beginTransaction();
//                    glbVars.db.getWritableDatabase().execSQL("DELETE FROM WC; DELETE FROM sqlite_sequence WHERE name = 'WC';");
//                    String ID = "", Descr = "", LowDescr = "";
//                    while (reset.next()) {
//                        ID = reset.getString(1);
//                        Descr = reset.getString(2);
//                        Descr = Descr.replaceAll("'", "''");
//                        LowDescr = reset.getString(3);
//                        LowDescr = LowDescr.replaceAll("'", "''");
//
//                        statement.clearBindings();
//                        statement.bindString(1, ID);
//                        statement.bindString(2, Descr);
//                        statement.bindString(3, LowDescr);
//                        statement.executeInsert();
//                        statement.clearBindings();
//                        progressStatusWC += 1;
//                        pgUpWC.setProgress(progressStatusWC);
//                        final int finalCount = cntWC;
//                        final int perc = progressStatusWC*100/cntWC;
//                        handlerWC.post(new Runnable() {
//                            public void run() {
//                                tvUpWC.setText(String.valueOf(progressStatusWC)+"/"+String.valueOf(finalCount));
//                                tvUpWCPerc.setText(String.valueOf(perc)+"%");
//                            }
//                        });
//                    }
//                    handlerWC.post(new Runnable() {
//                        public void run() {
//                            chkWC.setChecked(true);
//                            chkWC.setTextColor(Color.rgb(3, 103, 0));
//                        }
//                    });
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                } finally {
//                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
//                    glbVars.db.getWritableDatabase().endTransaction();
//                }
////                }
////              Конец обновления списка демографических признаков
//
//                //Обновляем список производителей/импортеров
////                if (chkSgi.isChecked()){
//                sql = "INSERT INTO PROD(ID, DESCR, LOWDESCR)  VALUES (?,?,?);";
//                statement = glbVars.db.getWritableDatabase().compileStatement(sql);
//                int cntProd = 0;
//                try {
//
//                    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//                    String SqlSel = "";
//                    SqlSel = "SELECT ID, DESCR, LOWDESCR FROM V_PROD ORDER BY 2";
//
//                    reset = stmt.executeQuery(SqlSel);
//                    reset.last();
//                    cntProd = reset.getRow();
//                    pgUpProd.setMax(cntProd);
//                    reset.beforeFirst();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                try {
//                    glbVars.db.getWritableDatabase().beginTransaction();
//                    glbVars.db.getWritableDatabase().execSQL("DELETE FROM PROD; DELETE FROM sqlite_sequence WHERE name = 'PROD';");
//                    String ID = "", Descr = "", LowDescr = "";
//                    while (reset.next()) {
//                        ID = reset.getString(1);
//                        Descr = reset.getString(2);
//                        Descr = Descr.replaceAll("'", "''");
//
//                        LowDescr = reset.getString(3);
//                        LowDescr = LowDescr.replaceAll("'", "''");
//
//                        statement.clearBindings();
//                        statement.bindString(1, ID);
//                        statement.bindString(2, Descr);
//                        statement.bindString(3, LowDescr);
//                        statement.executeInsert();
//                        statement.clearBindings();
//                        progressStatusProd += 1;
//                        pgUpProd.setProgress(progressStatusProd);
//                        final int finalCount = cntProd;
//                        final int perc = progressStatusProd*100/cntProd;
//                        handlerProd.post(new Runnable() {
//                            public void run() {
//                                tvUpProd.setText(String.valueOf(progressStatusProd)+"/"+String.valueOf(finalCount));
//                                tvUpProdPerc.setText(String.valueOf(perc)+"%");
//                            }
//                        });
//                    }
//                    handlerProd.post(new Runnable() {
//                        public void run() {
//                            chkProd.setChecked(true);
//                            chkProd.setTextColor(Color.rgb(3, 103, 0));
//                        }
//                    });
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                } finally {
//                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
//                    glbVars.db.getWritableDatabase().endTransaction();
//                }
////                }
////              Конец обновления списка производителей/импортеров
//
//                //Обновляем список Фокусов
////                if (chkSgi.isChecked()){
//                sql = "INSERT INTO FOCUS(ID, DESCR, LONG_DESCR, BDATE, EDATE, LOWDESCR)  VALUES (?,?,?,?,?,?);";
//                statement = glbVars.db.getWritableDatabase().compileStatement(sql);
//                int cntFocus = 0;
//                try {
//
//                    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//                    String SqlSel = "";
//                    SqlSel = "SELECT ID ,DESCR, LONG_DESCR, BDATE, EDATE, LOWLONG_DESCR FROM V_FOCUS ORDER BY 2";
//
//                    reset = stmt.executeQuery(SqlSel);
//                    reset.last();
//                    cntFocus = reset.getRow();
//                    pgUpFocus.setMax(cntFocus);
//                    reset.beforeFirst();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                try {
//                    glbVars.db.getWritableDatabase().beginTransaction();
//                    glbVars.db.getWritableDatabase().execSQL("DELETE FROM FOCUS; DELETE FROM sqlite_sequence WHERE name = 'FOCUS';");
//                    String ID = "", Descr = "", Long_Descr = "", BDATE = "", EDATE = "", LowDescr ="";
//                    while (reset.next()) {
//                        ID = reset.getString(1);
//                        Descr = reset.getString(2);
//
//                        Descr = Descr.replaceAll("'", "''");
//                        Long_Descr = reset.getString(3);
//                        Long_Descr = Long_Descr.replaceAll("'", "''");
//                        BDATE = reset.getString(4);
//                        EDATE = reset.getString(5);
//                        LowDescr = reset.getString(6);
//                        statement.clearBindings();
//                        statement.bindString(1, ID);
//                        statement.bindString(2, Descr);
//                        statement.bindString(3, Long_Descr);
//                        statement.bindString(4, BDATE);
//                        statement.bindString(5, EDATE);
//                        statement.bindString(6, LowDescr);
//                        statement.executeInsert();
//                        statement.clearBindings();
//                        progressStatusFocus += 1;
//                        pgUpFocus.setProgress(progressStatusFocus);
//                        final int finalCount = cntFocus;
//                        final int perc = progressStatusFocus*100/cntFocus;
//                        handlerFocus.post(new Runnable() {
//                            public void run() {
//                                tvUpFocus.setText(String.valueOf(progressStatusFocus)+"/"+String.valueOf(finalCount));
//                                tvUpFocusPerc.setText(String.valueOf(perc)+"%");
//                            }
//                        });
//                    }
//                    handlerFocus.post(new Runnable() {
//                        public void run() {
//                            chkFocus.setChecked(true);
//                            chkFocus.setTextColor(Color.rgb(3, 103, 0));
//                        }
//                    });
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                } finally {
//                    glbVars.db.getWritableDatabase().setTransactionSuccessful();
//                    glbVars.db.getWritableDatabase().endTransaction();
//                }
////                }
////              Конец обновления списка фокусов

                glbVars.UpdateWorking = 0;
                handler.post(new Runnable() {
                    public void run() {
                        btUpdate.setEnabled(true);
                        glbVars.WriteLastUpdate();
                    }
                });
            }
        });
        thUpdateData.start();
    }

}