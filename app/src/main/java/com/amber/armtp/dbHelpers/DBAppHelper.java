package com.amber.armtp.dbHelpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    public synchronized SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    public void putDemp(SQLiteDatabase dbNomen) {
        Cursor nomen = dbNomen.rawQuery("SELECT DISTINCT DEMP FROM NOMEN", null);

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM DEMP");
        for (int i = 0; i < nomen.getCount(); i++) {
            nomen.moveToNext();
            db.execSQL("INSERT INTO DEMP(DEMP) VALUES('" + nomen.getString(nomen.getColumnIndex("DEMP")) + "')");
        }
    }

    public Cursor getWCs() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
//                                    SELECT 0 AS _id, '0' AS CODE, 'Выберите Фокус' AS DESCR UNION ALL SELECT ROWID AS _id, CODE, DESCR FROM FOKUS
            cursor = db.rawQuery("SELECT 0 as _id, 'Выберите демографический признак' AS DEMP UNION SELECT ROWID as _id, DEMP FROM DEMP", null);

            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
