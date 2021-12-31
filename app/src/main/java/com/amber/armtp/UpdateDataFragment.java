package com.amber.armtp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amber.armtp.ServerWork.DBF.DBFConvertorToSQL;
import com.amber.armtp.ServerWork.Zip.ZipDownload;
import com.amber.armtp.ServerWork.Zip.ZipUnpacking;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Objects;

/**
 * Updated by domster704 on 27.09.2021
 */
public class UpdateDataFragment extends Fragment {
    private final Handler handler = new Handler();
    private final Handler handlerSgi = new Handler();
    private final Handler handlerGroups = new Handler();
    private final Handler handlerNom = new Handler();
    private final Handler handlerPrice = new Handler();
    private final Handler handlerContr = new Handler();
    private final Handler handlerAddr = new Handler();
    private final Handler handlerTP = new Handler();
    private final Handler handlerDebet = new Handler();
    private final Handler handlerStatus = new Handler();
    private final Handler handlerOut = new Handler();
    private final Handler handlerSales = new Handler();
    private final Handler handlerFocus = new Handler();

    public GlobalVars glbVars;
    Connection conn = null;
    String sql_server;
    String sql_port;
    String sql_db;
    String sql_loging;
    String sql_pass;
    ResultSet reset;
    Thread thUpdateData;
    CheckBox chkSgi, chkGrup, chkNomen, chkContrs, chkPrice, chkAddrr, chkTp, chkDebet, chkStatus, chkOuted, chkSales, chkFocus;
    Button btUpdate;
    String TP_ID;
    Boolean TP_LOCK;
    SharedPreferences settings, PriceSettings, tp_setting;
    SharedPreferences.Editor editor;
    private String DebetIsFinished = "0";
    private final BroadcastReceiver UpdateDebetWorking = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DebetIsFinished = Objects.requireNonNull(intent.getExtras()).getString("DebetUpdateFinished");
        }
    };
    private ProgressBar pgUpSgi, pgUpGroups, pgUpNomen, pgUpContrs, pgUpAddress, pgUpTp, pgUpDebet, pgUpStatus, pgUpOuted, pgUpSales, pgUpPrice, pgUpFocus;
    private TextView tvUpSgi, tvUpGroups, tvUpNomen, tvUpContrs, tvUpAddress, tvUpTp, tvUpDebet, tvUpStatus, tvUpOuted, tvUpSales, tvUpPrice, tvUpFocus;
    private TextView tvUpSgiPerc, tvUpGroupsPerc, tvUpNomenPerc, tvUpContrsPerc, tvUpAddressPerc, tvUpTpPerc, tvUpDebetPerc, tvUpStatusPerc, tvUpOutedPerc, tvUpSalesPerc, tvUpPricePerc, tvUpFocusPerc;


//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.update_data_fragment_new, container, false);
//        v.setKeepScreenOn(true);
//        glbVars.view = v;
//        return v;
//    }

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
    public void onResume() {
        super.onResume();
        try {
            getActivity().registerReceiver(UpdateDebetWorking, new IntentFilter("DebetUpdating"));
        } catch (Exception ignored) {
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(UpdateDebetWorking);
        } catch (Exception ignored) {
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

        sql_server = settings.getString("UpdateSrv", getResources().getString(R.string.sql_server));
        sql_port = settings.getString("sqlPort", getResources().getString(R.string.sql_port));
        sql_db = settings.getString("sqlDB", getResources().getString(R.string.sql_db));
        sql_loging = settings.getString("sqlLogin", getResources().getString(R.string.sql_user));
        sql_pass = settings.getString("sqlPass", getResources().getString(R.string.sql_pass));
        glbVars.CurrentCenType = settings.getString("usr_centype", "");
        int region = PriceSettings.getInt("Region", 0);
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
        pgUpPrice = getActivity().findViewById(R.id.pgPrice);
        pgUpFocus = getActivity().findViewById(R.id.pgFocus);

        tvUpSgi = getActivity().findViewById(R.id.tvSgiCount);
        tvUpSgiPerc = getActivity().findViewById(R.id.tvSgiPerc);

        tvUpGroups = getActivity().findViewById(R.id.tvGrupCount);
        tvUpGroupsPerc = getActivity().findViewById(R.id.tvGrupPerc);

        tvUpNomen = getActivity().findViewById(R.id.tvNomenCount);
        tvUpNomenPerc = getActivity().findViewById(R.id.tvNomenPerc);

        tvUpPrice = getActivity().findViewById(R.id.tvPriceCount);
        tvUpPricePerc = getActivity().findViewById(R.id.tvPricePerc);

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

        tvUpFocus = getActivity().findViewById(R.id.tvFocusCount);
        tvUpFocusPerc = getActivity().findViewById(R.id.tvFocusPerc);

        chkSgi = getActivity().findViewById(R.id.chkSGI);
        chkGrup = getActivity().findViewById(R.id.chkGroups);
        chkNomen = getActivity().findViewById(R.id.chkNomen);
        chkPrice = getActivity().findViewById(R.id.chkPrice);
        chkContrs = getActivity().findViewById(R.id.chkContrs);
        chkAddrr = getActivity().findViewById(R.id.chkAddrs);
        chkTp = getActivity().findViewById(R.id.chkTorgPred);
        chkDebet = getActivity().findViewById(R.id.chkDebet);
        chkStatus = getActivity().findViewById(R.id.chkOrdStatus);
        chkOuted = getActivity().findViewById(R.id.chkOuted);
        chkSales = getActivity().findViewById(R.id.chkSales);
        chkFocus = getActivity().findViewById(R.id.chkFocus);

        btUpdate.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                conn = null;
                reset = null;
                if (DebetIsFinished.equals("1") || DebetIsFinished.equals("0")) {
                    if (glbVars.isNetworkAvailable()) {
                        glbVars.UpdateWorking = 1;
                        btUpdate.setEnabled(false);

                        DownloadDBFs();
//                        UpdateData();
                    } else {
                        Toast.makeText(getActivity(), "Нет доступного интернет соединения. Проверьте соединение с Интернетом", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


        try {
            getActivity().registerReceiver(UpdateDebetWorking, new IntentFilter("DebetUpdating"));
        } catch (Exception ignored) {
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void DownloadDBFs() {
        ZipDownload zipDownload;
        try {
            zipDownload = new ZipDownload(ServerDetails.getInstance());
            zipDownload.downloadZip();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ZipUnpacking zipUnpacking;
        try {
            zipUnpacking = new ZipUnpacking(ServerDetails.getInstance());
            zipUnpacking.doUnpacking();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class UIData {
        public CheckBox checkBox;
        public ProgressBar progressBar;
        public TextView tvPer;
        public TextView tvCount;
        public Handler handler;

        public UIData(CheckBox checkBox, ProgressBar progressBar, TextView tvCount, TextView tvPer, Handler handler) {
            this.checkBox = checkBox;
            this.progressBar = progressBar;
            this.tvPer = tvPer;
            this.tvCount = tvCount;
            this.handler = handler;
        }
    }

    private static class UpdateData {
        public String sqlCommand;
        public String fileName;
        public UIData uiData;

        public UpdateData(String sqlCommand, String fileName, UIData uiData) {
            this.sqlCommand = sqlCommand;
            this.fileName = fileName;
            this.uiData = uiData;
        }
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void UpdateData() {
//        thUpdateData = new Thread(new Runnable() {
//            public void run() {
//                UIData[] uiData = new UIData[]{
//                        new UIData(chkSgi, pgUpSgi, tvUpSgi, tvUpSgiPerc, handlerSgi),
//                        new UIData(chkGrup, pgUpGroups, tvUpGroups, tvUpGroupsPerc, handlerGroups),
//                        new UIData(chkNomen, pgUpNomen, tvUpNomen, tvUpNomenPerc, handlerNom),
//                        new UIData(chkPrice, pgUpPrice, tvUpPrice, tvUpPricePerc, handlerPrice),
//                        new UIData(chkContrs, pgUpContrs, tvUpContrs, tvUpContrsPerc, handlerContr),
//                        new UIData(chkAddrr, pgUpAddress, tvUpAddress, tvUpAddressPerc, handlerAddr),
//                        new UIData(chkTp, pgUpTp, tvUpTp, tvUpTpPerc, handlerTP),
//                        new UIData(chkDebet, pgUpDebet, tvUpDebet, tvUpDebetPerc, handlerDebet),
//                        new UIData(chkStatus, pgUpStatus, tvUpStatus, tvUpStatusPerc, handlerStatus),
//                        new UIData(chkOuted, pgUpOuted, tvUpOuted, tvUpOutedPerc, handlerOut),
//                        new UIData(chkSales, pgUpSales, tvUpSales, tvUpSalesPerc, handlerSales),
//                        new UIData(chkFocus, pgUpFocus, tvUpFocus, tvUpFocusPerc, handlerFocus),
//                };
//
//                UpdateData[] updateData = new UpdateData[]{
//                        new UpdateData("INSERT INTO SGI       (ID, DESCR)                                                                                                                            VALUES (?,?);", "SGI.DBF", uiData[0]),
//                        new UpdateData("INSERT INTO GRUPS     (ID, SGIID, DESCR)                                                                                                                     VALUES (?,?,?);", "GRUPPS.DBF", uiData[1]),
//                        new UpdateData("INSERT INTO Nomen     (SGIID, GRUPPA, CODE, DESCR, DEMP, FOCUSID, GOFRA, KOD5, FOTO, POSTDATA, OST, PD, ZAKAZ)                                               VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);", "GOODS.DBF", uiData[2]),
//                        new UpdateData("INSERT INTO PRICES    (NOMENID, TIPCE, PRICE)                                                                                                                VALUES (?,?,?);", "CENJ.DBF", uiData[3]),
//                        new UpdateData("INSERT INTO CONTRS    (CODE, DESCR)                                                                                                                          VALUES (?,?);", "KONTR.DBF", uiData[4]),
//                        new UpdateData("INSERT INTO ADDRS     (KONTRCODE, CODE, DESCR)                                                                                                               VALUES (?,?,?);", "ADR.DBF", uiData[5]),
//                        new UpdateData("INSERT INTO TORG_PRED (CODE, DESCR)                                                                                                                          VALUES (?,?);", "TP.DBF", uiData[6]),
//                        new UpdateData("INSERT INTO DEBET     (KONTR, CONTR_ID, SCHET, STATUS, KREDIT, DOGOVOR, A7, A14, A21, A28, A35, A42, A49, A56, A63, A64, DOLG, OTGR30, OPL30, K_OBOR, FIRMA) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);", "DEB.DBF", uiData[7]),
//                        new UpdateData("INSERT INTO STATUS    (DOCID, STATUS)                                                                                                                        VALUES(?,?)", "STATUS.DBF", uiData[8]),
//                        new UpdateData("INSERT INTO VYCHERK   (DOCID, NOM, COL)                                                                                                                      VALUES(?,?,?)", "VYCHERK.DBF", uiData[9]),
//                        new UpdateData("INSERT INTO SALES     (CONTRID, SGI, GRUPID, TIPCE, SALE)                                                                                                    VALUES(?,?,?,?,?)", "SKIDKI.DBF", uiData[10]),
//                        new UpdateData("INSERT INTO FOCUS     (CODE, DESCR, DATAN, DATAK)                                                                                                            VALUES(?,?,?,?)", "FOKUS.DBF", uiData[11]),
//                };
//
//                for (UpdateData updateDatum : updateData) {
//                    loadDBFDataToApp(updateDatum);
//                }
//
//                glbVars.UpdateWorking = 0;
//                handler.post(new Runnable() {
//                    public void run() {
//                        btUpdate.setEnabled(true);
//                        glbVars.WriteLastUpdate();
//                    }
//                });
//            }
//        });
//        thUpdateData.start();
//    }

//    private void loadDBFDataToApp(final UpdateData data) {
//        SQLiteStatement statement = glbVars.db.getWritableDatabase().compileStatement(data.sqlCommand);
//        String tableName = data.sqlCommand.split(" ")[2].split("\\(")[0];
//
//        try {
//            glbVars.db.getWritableDatabase().beginTransaction();
//            glbVars.db.getWritableDatabase().execSQL("DELETE FROM " + tableName);
//            glbVars.db.getWritableDatabase().execSQL("DELETE FROM sqlite_sequence WHERE name = '" + tableName + "'");
//
//            DBFConvertorToSQL reader = new DBFConvertorToSQL(statement);
//            runReadDBF(reader, data.fileName, data.uiData);
//
//        } finally {
//            glbVars.db.getWritableDatabase().setTransactionSuccessful();
//            glbVars.db.getWritableDatabase().endTransaction();
//        }
//    }

//    private final boolean isParallel = false;
//
//    private void runReadDBF(final DBFConvertorToSQL reader, final String fileName, final UIData uiData) {
//        // Если включить параллельное скачивание, то скорость сильно падает, так что не рекомендую
//        if (isParallel) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        reader.read(fileName, uiData);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//        } else {
//            try {
//                reader.read(fileName, uiData);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
}
