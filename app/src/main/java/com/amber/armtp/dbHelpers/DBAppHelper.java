package com.amber.armtp.dbHelpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.amber.armtp.annotations.PGShowing;

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

    @PGShowing
    public void putDemp(SQLiteDatabase dbNomen) {
        Cursor nomen = dbNomen.rawQuery("SELECT DISTINCT DEMP FROM NOMEN", null);

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM DEMP");
        for (int i = 0; i < nomen.getCount(); i++) {
            nomen.moveToNext();
            db.execSQL("INSERT INTO DEMP(DEMP) VALUES('" + nomen.getString(nomen.getColumnIndex("DEMP")) + "')");
        }

        nomen.close();
    }

    public Cursor getWCs() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 as _id, 'Выберите' AS DEMP UNION SELECT ROWID as _id, DEMP FROM DEMP", null);

            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getWCByID(String id) {
        if (id.equals("0"))
            return "Выберите";
        Cursor c = this.getReadableDatabase().rawQuery("SELECT DEMP FROM DEMP WHERE rowid ='" + id + "'", null);
        c.moveToNext();
        return c.getString(0);
    }

    public String getIDByWC(String WC) {
        if (WC.equals("0") || WC.equals("Выберите"))
            return "0";
        Cursor c = this.getReadableDatabase().rawQuery("SELECT rowid FROM DEMP WHERE DEMP=?", new String[] {WC});
        c.moveToNext();
        return c.getString(0);
    }
}
