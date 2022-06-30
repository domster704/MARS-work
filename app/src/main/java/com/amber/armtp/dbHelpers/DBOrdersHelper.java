package com.amber.armtp.dbHelpers;

import android.content.ContentValues;
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
//        System.out.println(oldVersion + " " + newVersion);
        if (newVersion > oldVersion) {
            sqLiteDatabase.execSQL("ALTER TABLE ZAKAZY ADD COLUMN OUTED INTEGER DEFAULT 0");
        }
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    public Cursor getZakazy() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT" +
                    " ZAKAZY.ROWID AS _id," +
                    " ZAKAZY.DOC_DATE," +
                    " CONTR_DES AS CONTR," +
                    " ADDR_DES AS ADDR," +
                    " ZAKAZY.DELIVERY_DATE AS DELIVERY," +
                    " ZAKAZY.STATUS AS STATUS," +
                    " ZAKAZY.SUM as SUM," +
                    " ZAKAZY.DOCID AS DOCID," +
                    " ZAKAZY.OUTED AS OUTED" +
                    " FROM ZAKAZY" +
                    " WHERE" +
                    " DATE(substr(ZAKAZY.DOC_DATE, 7, 4) || '-' || substr(ZAKAZY.DOC_DATE, 4, 2) || '-' || substr(ZAKAZY.DOC_DATE, 1, 2))" +
                    " ORDER BY _id DESC", null);
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getZakazDetails(String ZakazID) {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT ROWID AS _id, ZAKAZ_ID, NOMEN, DESCR, QTY, PRICE, IS_OUTED, OUT_QTY, SUM FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + ZakazID + "'", null);
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setZakazStatus(String Status, int id) {
        SQLiteDatabase db;
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("UPDATE ZAKAZY SET STATUS='" + Status + "' WHERE ROWID=" + id);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
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

    public int getDocNumber() {
        Cursor c = null;
        int DocNo = 0;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT CASE WHEN MAX(ROWID)+1 IS NULL THEN 1 ELSE MAX(ROWID)+1 END AS [DOCNO] FROM ZAKAZY;", null);
            if (c.moveToFirst()) {
                DocNo = c.getInt(0);
            }
            return DocNo;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public void updateOrderQty(String ZakID, String ID, int Qty) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues updatedValues = new ContentValues();
            updatedValues.put("QTY", Qty);
            db.update("ZAKAZY_DT", updatedValues, "NOMEN='" + ID + "' AND ZAKAZ_ID='" + ZakID + "'", null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public HashMap<String, String> getOrderData(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT TP, CONTR, ADDR, DELIVERY_DATE, COMMENT FROM ZAKAZY WHERE DOCID='" + id + "'", null);
        c.moveToNext();

        HashMap<String, String> data = new HashMap<>();
        data.put("TP", c.getString(c.getColumnIndex("TP")));
        data.put("CONTR", c.getString(c.getColumnIndex("CONTR")));
        data.put("ADDR", c.getString(c.getColumnIndex("ADDR")));
        data.put("DELIVERY_DATE", c.getString(c.getColumnIndex("DELIVERY_DATE")));
        data.put("COMMENT", c.getString(c.getColumnIndex("COMMENT")));
        return data;
    }
}
