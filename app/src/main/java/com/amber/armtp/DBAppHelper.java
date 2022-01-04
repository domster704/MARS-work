package com.amber.armtp;

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

    public void putDemp(SQLiteDatabase dbNomen) {
        Cursor nomen = dbNomen.rawQuery("SELECT DISTINCT DEMP FROM NOMEN", null);

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM DEMP");
        for (int i = 0; i < nomen.getCount(); i++) {
            nomen.moveToNext();
            db.execSQL("INSERT INTO DEMP(DEMP) VALUES('" + nomen.getString(nomen.getColumnIndex("DEMP")) +"')");
        }
    }

    public Cursor getWCs() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT '0' as _id, 'Выберите Демографический признак' AS DEMP UNION SELECT ROWID as _id, DEMP FROM DEMP", null);

            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
