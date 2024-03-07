package com.amber.armtp.dbHelpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

public class DBOrdersHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "order.db";

    public DBOrdersHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        onCreate(sqLiteDatabase);
        try {
            if (newVersion > oldVersion) {
                sqLiteDatabase.execSQL("ALTER TABLE ZAKAZY ADD COLUMN OUTED INTEGER DEFAULT 0");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cursor getZakazy() {
        try {
            return this.getReadableDatabase().rawQuery("SELECT" +
                    " ZAKAZY.ROWID AS _id," +
                    " ZAKAZY.DOC_DATE," +
                    " CONTR_DES AS CONTR," +
                    " ADDR_DES AS ADDR," +
                    " ZAKAZY.DELIVERY_DATE AS DELIVERY," +
                    " ZAKAZY.STATUS AS STATUS," +
                    " ZAKAZY.SUM as SUM," +
                    " ZAKAZY.DOCID AS DOCID," +
                    " ZAKAZY.OUTED AS OUTED," +
                    " ZAKAZY.COMMENT AS COMMENT" +
                    " FROM ZAKAZY" +
                    " WHERE" +
                    " DATE(substr(ZAKAZY.DOC_DATE, 7, 4) || '-' || substr(ZAKAZY.DOC_DATE, 4, 2) || '-' || substr(ZAKAZY.DOC_DATE, 1, 2))" +
                    " ORDER BY _id DESC", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getZakazDetails(String ZakazID) {
        try {
            return this.getReadableDatabase().rawQuery("SELECT ROWID AS _id, ZAKAZ_ID, NOMEN, DESCR, QTY, PRICE, IS_OUTED, OUT_QTY, SUM FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + ZakazID + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setZakazStatus(String Status, int id) {
        try {
            this.getWritableDatabase().execSQL("UPDATE ZAKAZY SET STATUS='" + Status + "' WHERE ROWID=" + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteOrderByID(long id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT DOCID FROM ZAKAZY WHERE ROWID=" + id, null);
            String data = "0";
            if (cursor.moveToFirst()) {
                data = cursor.getString(0);
            }

            db.execSQL("DELETE FROM ZAKAZY_DT WHERE ZAKAZ_ID=\"" + data + "\"");
            db.execSQL("DELETE FROM ZAKAZY WHERE ROWID=" + id);

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getOrderData(String id) {
        HashMap<String, String> data = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT TP, CONTR, ADDR, DELIVERY_DATE, COMMENT FROM ZAKAZY WHERE DOCID='" + id + "'", null)) {
            c.moveToNext();

            data.put("TP", c.getString(c.getColumnIndex("TP")));
            data.put("CONTR", c.getString(c.getColumnIndex("CONTR")));
            data.put("ADDR", c.getString(c.getColumnIndex("ADDR")));
            data.put("DELIVERY_DATE", c.getString(c.getColumnIndex("DELIVERY_DATE")));
            data.put("COMMENT", c.getString(c.getColumnIndex("COMMENT")));
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return data;
    }
}
