package com.amber.armtp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.media.RingtoneManager;
import android.net.Uri;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import me.leolin.shortcutbadger.ShortcutBadger;


public class CheckSMS extends IntentService {
    public GlobalVars glbVars;
    Connection conn = null;

    String sql_server;
    String sql_port;
    String sql_db;
    String sql_loging;
    String sql_pass;

    public CheckSMS() {
        super("CheckSMS");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        sql_server = this.getResources().getString(R.string.sql_server);
        sql_port = this.getResources().getString(R.string.sql_port);
        sql_db = this.getResources().getString(R.string.sql_db);
        sql_loging = this.getResources().getString(R.string.sql_user);
        sql_pass = this.getResources().getString(R.string.sql_pass);
        // Don't let this service restart automatically if it has been stopped by the OS.
        return START_NOT_STICKY;
    }

    public void onCreate() {
        super.onCreate();
        glbVars = (GlobalVars) this.getApplicationContext();
        glbVars.glbContext = this.getApplicationContext();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int Rowid = 0;
        int msgRow;
        String TP_ID, TP_IDS, MSG_HEAD, MSG, MSG_DATE, MSG_TIME;
        Statement stmt;
        ResultSet reset = null;
        Cursor c;
        if (glbVars.isNetworkAvailable()) {

            if (glbVars.SmsDB == null) {
                glbVars.SmsDB = openOrCreateDatabase("armtp_msg.db", Context.MODE_ENABLE_WRITE_AHEAD_LOGGING, null);
            }

            glbVars.SmsDB.execSQL("CREATE TABLE IF NOT EXISTS MSGS (ROWID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ROW_ID INTEGER, TP_ID TEXT, TP_IDS TEXT, MSG_HEAD TEXT, MESSAGE TEXT,MSG_DATE TEXT, MSG_TIME TEXT, IS_NEW INTEGER DEFAULT 1)");
            Cursor cursor = glbVars.SmsDB.rawQuery("SELECT CASE WHEN MAX(ROW_ID) IS NULL THEN 0 ELSE MAX(ROW_ID) END AS ROW_ID FROM MSGS", null);
            if (cursor.moveToNext()) {
                Rowid = cursor.getInt(cursor.getColumnIndex("ROW_ID"));
            }

            if (conn == null) {
                ConnectToSql();
            }

            String sql_insert = "INSERT INTO MSGS(ROW_ID,TP_ID,TP_IDS,MSG_HEAD,MESSAGE,MSG_DATE,MSG_TIME) VALUES(?,?,?,?,?,?,?);";
            SQLiteStatement statement = glbVars.SmsDB.compileStatement(sql_insert);

            glbVars.SmsDB.beginTransactionNonExclusive();

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
                glbVars.SmsDB.setTransactionSuccessful();
                glbVars.SmsDB.endTransaction();
            }
            String count = "";
            Cursor cur_sms = glbVars.SmsDB.rawQuery("SELECT CASE WHEN COUNT(ROW_ID) IS NULL THEN '0' ELSE COUNT(ROW_ID) END  AS CNT FROM MSGS WHERE IS_NEW=1", null);
            if (cur_sms.moveToNext()) {
                count = cur_sms.getString(cur_sms.getColumnIndex("CNT"));
                cur_sms.close();
            }
            Intent i = new Intent("SMS_COUNT");
            i.putExtra("SmsCount", count);
            sendBroadcast(i);

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
