package com.amber.armtp;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class UpdateDebet extends IntentService {
    final String sql_server = "91.208.84.67";
    final String sql_port = "1439";
    final String sql_db = "IZH_2015";
    final String sql_loging = "sa";
    final String sql_pass = "Yjdfz Ptkfylbz.ru";
    public GlobalVars glbVars;
    Connection conn = null;
    int NOTIFICATION_ID;
    SQLiteDatabase InsDB = null;
    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    NotificationCompat.Builder builder;

    public UpdateDebet() {
        super("UpdateDebet");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        // Don't let this service restart automatically if it has been stopped by the OS.
        return START_NOT_STICKY;
    }

    public void onCreate() {
        super.onCreate();
        glbVars = (GlobalVars) this.getApplicationContext();
        glbVars.glbContext = this.getApplicationContext();
        if (glbVars.UpdateWorking == 1) {
            stopSelf();
        } else {
            Intent i = new Intent("DebetUpdating");
            i.putExtra("DebetUpdateFinished", "2");
            sendBroadcast(i);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String ID, TP_ID, TP_IDS;
        int Rowid, Kredit, Limit, Nekontr;
        Float Saldo, a7, a14, a21, a28, a35, a42, a49, a56, a63, a64, otg30, opl30;
        Statement stmt;
        ResultSet reset = null;
        Cursor c;
        if (glbVars.isNetworkAvailable()) {
            if (glbVars.db == null) {
                glbVars.db = new DBHepler(this.getApplicationContext());
            }

            glbVars.db.getWritableDatabase().execSQL("DELETE FROM TMP_DEBET;");
            glbVars.db.getWritableDatabase().execSQL("DELETE FROM sqlite_sequence WHERE name = 'TMP_DEBET';");
            if (conn == null) {
                ConnectToSql();
            }
            String sql_insert = "INSERT INTO TMP_DEBET(ROW, CONTR_ID, KREDIT, LIM, NEKONTR, SALDO, A7, A14, A21, A28, TP_ID, TP_IDS, A35, A42, A49, A56, A63, A64, OTG30, OPL30)  VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
            SQLiteStatement statement2 = glbVars.db.getWritableDatabase().compileStatement(sql_insert);
            glbVars.db.getWritableDatabase().beginTransactionNonExclusive();

            try {
                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                reset = stmt.executeQuery("SELECT ROW_ID,CONTR_ID,KREDIT,LIMIT,NEKONTR,SALDO,A7,A14,A21,A28,TPID,TPID_LIST, A35, A42, A49, A56, A63, A64, OTG30, OPL30 FROM V_DEBET_MOBILE_ARM ORDER BY CONTR_ID");
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
                    statement2.clearBindings();
                    statement2.bindLong(1, Rowid);
                    statement2.bindString(2, ID);
                    statement2.bindLong(3, Kredit);
                    statement2.bindLong(4, Limit);
                    statement2.bindLong(5, Nekontr);
                    statement2.bindDouble(6, Saldo);
                    statement2.bindDouble(7, a7);
                    statement2.bindDouble(8, a14);
                    statement2.bindDouble(9, a21);
                    statement2.bindDouble(10, a28);
                    statement2.bindString(11, TP_ID);
                    statement2.bindString(12, TP_IDS);
                    statement2.bindDouble(13, a35);
                    statement2.bindDouble(14, a42);
                    statement2.bindDouble(15, a49);
                    statement2.bindDouble(16, a56);
                    statement2.bindDouble(17, a63);
                    statement2.bindDouble(18, a64);
                    statement2.bindDouble(19, otg30);
                    statement2.bindDouble(20, opl30);
                    statement2.executeInsert();
                    statement2.clearBindings();
                    ID = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            glbVars.db.getWritableDatabase().execSQL("DELETE FROM DEBET;");
            glbVars.db.getWritableDatabase().execSQL("DELETE FROM sqlite_sequence WHERE name = 'DEBET';");
            glbVars.db.getWritableDatabase().execSQL("INSERT INTO DEBET (ROW,CONTR_ID,KREDIT,LIM,NEKONTR,SALDO,A7,A14,A21,A28,TP_ID,TP_IDS, A35, A42, A49, A56, A63, A64, OTG30, OPL30) SELECT ROW,CONTR_ID,KREDIT,LIM,NEKONTR,SALDO,A7,A14,A21,A28,TP_ID,TP_IDS, A35, A42, A49, A56, A63, A64, OTG30, OPL30 FROM TMP_DEBET;");
            glbVars.db.getWritableDatabase().execSQL("DELETE FROM TMP_DEBET;");
            glbVars.db.getWritableDatabase().execSQL("DELETE FROM sqlite_sequence WHERE name = 'TMP_DEBET';");
            glbVars.db.getWritableDatabase().setTransactionSuccessful();
            glbVars.db.getWritableDatabase().endTransaction();
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

            Intent i = new Intent("DebetUpdating");
            i.putExtra("DebetUpdateFinished", "1");
            sendBroadcast(i);
        }

    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void ConnectToSql() {
        String connString;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();

            connString = "jdb" + "c:jtds:sqlserver://" + sql_server + ":" + sql_port + ";instance=MSSQLSERVER;databaseName=" + sql_db + ";user=" + sql_loging + ";password=" + sql_pass;
            conn = DriverManager.getConnection(connString, sql_loging, sql_pass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
