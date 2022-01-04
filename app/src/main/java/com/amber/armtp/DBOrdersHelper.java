package com.amber.armtp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOrdersHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "order.db";

    public DBOrdersHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        onCreate(sqLiteDatabase);
    }

    public Cursor getZakazy() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            db.setLockingEnabled(false);
            cursor = db.rawQuery("SELECT" +
                    "  ZAKAZY.ROWID AS _id," +
                    "  ZAKAZY.DOC_DATE," +
                    "  CONTR_DES AS CONTR," +
                    "  ADDR_DES AS ADDR," +
                    "  ZAKAZY.DELIVERY_DATE AS DELIVERY," +
                    "  CASE ZAKAZY.STATUS WHEN 0 THEN 'Сохранен' WHEN 1 THEN 'Отправлен' WHEN 2 THEN 'Получен' WHEN 3 THEN 'Оформлен' WHEN 4 THEN 'Оформлен(-)' WHEN 6 THEN 'Собран' WHEN 7 THEN 'Собран(-)' WHEN 5 THEN 'Удален' WHEN 99 THEN 'Отменен' END AS STATUS," +
                    "  (SELECT printf('%.2f', ROUND(SUM(QTY * PRICE),2)) FROM ZAKAZY_DT WHERE ZAKAZ_ID=ZAKAZY.DOCID) AS SUM," +
                    "  ZAKAZY.DOCID AS DOCID" +
                    "  FROM ZAKAZY" +
                    "  WHERE" +
                    "  DATE(substr(ZAKAZY.DOC_DATE, 7, 4) || '-' || substr(ZAKAZY.DOC_DATE, 4, 2) || '-' || substr(ZAKAZY.DOC_DATE, 1, 2))", null);
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
            db.setLockingEnabled(false);
            cursor = db.rawQuery("SELECT ROWID AS _id, ZAKAZ_ID, NOMEN, DESCR, QTY, printf('%.2f', PRICE) AS PRICE, printf('%.2f', ROUND(QTY*PRICE,2)) AS SUM, IS_OUTED, OUT_QTY FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + ZakazID + "'", null);
            return cursor;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void SetZakazStatus(int Status, int State, int id) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.setLockingEnabled(false);
        db.beginTransaction();
        try {
            db.execSQL("UPDATE ZAKAZY SET STATUS=" + Status + " WHERE STATUS=" + State + " AND ROWID=" + id);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void DeleteOrderByID(long id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT DOCID FROM ZAKAZY WHERE ROWID=" + id, null);
            String data = "0";
            if (cursor.moveToFirst()) {
                data = cursor.getString(0);
            }

            db.execSQL("DELETE FROM ZAKAZY_DT WHERE ZAKAZ_ID=\"" + data + "\"");
            db.execSQL("DELETE FROM ZAKAZY WHERE ROWID=" + id);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
