package com.amber.armtp.dbHelpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.amber.armtp.extra.ProgressBarShower;

public class DBAppHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "appData.db";

    public DBAppHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        onCreate(sqLiteDatabase);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    public void putSectionsFromDownloadedDB(Context context, SQLiteDatabase dbNomen) {
        new ProgressBarShower(context).setFunction(() -> {
            Cursor nomen = dbNomen.rawQuery("SELECT DISTINCT DEMP FROM NOMEN", null);

            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM DEMP");
            for (int i = 0; i < nomen.getCount(); i++) {
                nomen.moveToNext();
                db.execSQL("INSERT INTO DEMP(DEMP) VALUES('" + nomen.getString(nomen.getColumnIndex("DEMP")) + "')");
            }

            nomen.close();
            return null;
        }).start();
    }

    public Cursor getWCs() {
        try {
            return this.getReadableDatabase().rawQuery("SELECT 0 as _id, 'Выберите' AS DEMP UNION SELECT ROWID as _id, DEMP FROM DEMP", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getWCByID(String id) {
        if (id.equals("0"))
            return "Выберите";
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT DEMP FROM DEMP WHERE rowid ='" + id + "'", null)){
            if (c.moveToNext()) {
                return c.getString(0);
            }
        }
        return "0";
    }

    public String getIDByWC(String WC) {
        if (WC.equals("0") || WC.equals("Выберите"))
            return "0";
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT rowid FROM DEMP WHERE DEMP=?", new String[]{WC})){
            if (c.moveToNext()) {
                return c.getString(0);
            }
        }
        return "0";
    }
}
