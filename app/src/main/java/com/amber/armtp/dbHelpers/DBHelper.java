package com.amber.armtp.dbHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.amber.armtp.interfaces.Async;
import com.amber.armtp.interfaces.PGShowing;
import com.amber.armtp.ui.OrderHeadFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

/**
 * Updated by domster704 on 27.09.2021
 */
public class DBHelper extends SQLiteOpenHelper {
    public static HashMap<String, Float> pricesMap = new HashMap<>();

    public static final String DATABASE_NAME = "armtp3.db";

    private static final String TB_NOMEN = "Nomen";
    private static final String TB_ORDER = "ORDERS";
    private static final String KEY_ZAKAZ = "ZAKAZ";
    private static final String KEY_ORD_TP_ID = "TP";
    private static final String KEY_ORD_CONTR_ID = "CONTR";
    private static final String KEY_ORD_ADDR_ID = "ADDR";
    private static final String KEY_ORD_DATA = "DATA";
    private static final String KEY_ORD_COMMENT = "COMMENT";

    private final HashSet<String> listOfUpdatedGroups = new HashSet<>();

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    public Cursor getAllSgi() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS CODE, 'Выберите СГИ' AS DESCR UNION ALL SELECT ROWID AS _id, CODE, DESCR FROM SGI", null);
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getFocuses() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS CODE, 'Выберите Фокус' AS DESCR UNION ALL SELECT ROWID AS _id, CODE, DESCR FROM FOKUS", null);

            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getColor() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, 'Выберите цвет' AS DESCR UNION ALL SELECT ROWID AS _id, ID, DESCR FROM UNI_MATRIX WHERE TYPE_ID=10 AND DESCR<>''", null);

            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getGrupBySgi(String KEY_GRUP_SGIID) {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, 0 AS CODE, 'Выберите группу' AS DESCR, 0 AS SGI UNION ALL SELECT ROWID AS _id, CODE, DESCR, SGI FROM GRUPS WHERE SGI='" + KEY_GRUP_SGIID + "'", null);
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getNomByGroup(String GroupID, String SgiID) {
        Cursor cursor1;
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();

            if (!listOfUpdatedGroups.contains(GroupID)) {
                // Обновление цен по GROUP
                if (!SgiID.equals("")) {
                    cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "' AND SGI = '" + SgiID + "' AND GRUPPA = '" + GroupID + "'", null);
                } else {
                    cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "'", null);
                }
                _setNomenPriceWithSgi(cursor1, GroupID);

                // Обновление цен по SGI
                if (!SgiID.equals("")) {
                    cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "' AND SGI = '" + SgiID + "' AND GRUPPA IS NULL", null);
                } else {
                    cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "'", null);
                }
                _setNomenPriceWithSgi(cursor1, GroupID);
            }

            // Инфа про Nomen
            cursor = db.rawQuery("SELECT rowid AS _id, KOD5, DESCR, OST," +
                    " PRICE," +
                    " GRUPPA, ZAKAZ, FOTO, PD, SGI, GOFRA, MP, POSTDATA FROM Nomen WHERE OST>0 AND Nomen.GRUPPA='" + GroupID + "' ORDER BY Nomen.DESCR", null);
            return cursor;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getNomByGroup(String GroupID, String SgiID, String sqlCondition) {
        Cursor cursor1;
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();

            if (!listOfUpdatedGroups.contains(GroupID)) {
                // Обновление цен по GROUP
                if (!SgiID.equals("")) {
                    cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "' AND SGI = '" + SgiID + "' AND GRUPPA = '" + GroupID + "'", null);
                } else {
                    cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "'", null);
                }
                _setNomenPriceWithSgi(cursor1, GroupID);

                // Обновление цен по SGI
                if (!SgiID.equals("")) {
                    cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "' AND SGI = '" + SgiID + "' AND GRUPPA IS NULL", null);
                } else {
                    cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "'", null);
                }
                _setNomenPriceWithSgi(cursor1, GroupID);
            }

            // Инфа про Nomen
            cursor = db.rawQuery("SELECT rowid AS _id, KOD5, DESCR, OST," +
                    " PRICE," +
                    " GRUPPA, ZAKAZ, FOTO, PD, SGI, GOFRA, MP, POSTDATA FROM Nomen WHERE OST>0 " + sqlCondition + " ORDER BY Nomen.DESCR", null);
            return cursor;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public Cursor getNextNomen(
            String SgiID, String GrupID,
            String WCID, String FocusID, String SearchName, int position) {
        Cursor cursor;
        String sqlMX = "", searchReq = "";
        Log.d("xd", SgiID);
        if (!SearchName.equals("")) {
            String[] separated = SearchName.split(" ");
            StringBuilder Condition = new StringBuilder("%" + SearchName + "%");
            if (separated.length > 1) {
                Condition = new StringBuilder("%");
                for (String item : separated) {
                    Condition.append(item).append("%");
                }
            }
            searchReq = " AND (LOWER(Nomen.DESCR) LIKE '" + Condition + "' OR LOWER(Nomen.KOD5) LIKE '" + Condition + "')";
        }

        if (SgiID != null)
            sqlMX += (!SgiID.equals("0")) ? " AND Nomen.SGI='" + SgiID + "'" : "";
        if (GrupID != null)
            sqlMX += (!GrupID.equals("0")) ? " AND Nomen.GRUPPA='" + GrupID + "'" : "";
        if (WCID != null)
            sqlMX += (!WCID.equals("0") && !WCID.equals("Не использовать") && !WCID.equals("!Не определено") && !WCID.equals("Не имеет значения")) ? " AND Nomen.DEMP='" + WCID + "'" : "";
        if (FocusID != null)
            sqlMX += (!FocusID.equals("0")) ? " AND Nomen.FOKUS='" + FocusID + "'" : "";

        String limit = "40";
        if (position != 0) {
            limit = String.valueOf(40 + position);
        }

        String sqlLimit = "LIMIT " + limit;

        if (!SgiID.equals("0") && !GrupID.equals("0")) {
            return getNomByGroup(SgiID, GrupID, sqlMX);
        }

        try {
            SQLiteDatabase db = this.getReadableDatabase();

            if (!listOfUpdatedGroups.contains(GrupID)) {
                cursor = db.rawQuery("SELECT Nomen.ROWID AS _id, Nomen.KOD5, Nomen.DESCR, OST, ZAKAZ, GRUPPA, NOMEN.SGI, FOTO, GRUPS.DESCR AS GRUP, PD, PRICE FROM Nomen JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE WHERE OST>0 " + sqlMX + searchReq + " ORDER BY Nomen.DESCR " + sqlLimit, null);
                _setNomenPriceWithoutSgi(cursor);
            }

            cursor = db.rawQuery("SELECT Nomen.ROWID AS _id, Nomen.KOD5, Nomen.DESCR, OST, PRICE, ZAKAZ, GRUPPA, NOMEN.SGI, FOTO, GRUPS.DESCR AS GRUP, PD, GOFRA, MP, POSTDATA FROM Nomen JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE WHERE OST>0 " + sqlMX + searchReq + " ORDER BY Nomen.DESCR " + sqlLimit, null);
            return cursor;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getSearchNom(String SearchStr) {
        Cursor cursor;
        String[] separated = SearchStr.split(" ");
        StringBuilder Condition = new StringBuilder("%" + SearchStr + "%");
        if (separated.length > 1) {
            Condition = new StringBuilder("%");
            for (String item : separated) {
                Condition.append(item).append("%");
            }
        }

        try {
            SQLiteDatabase db = this.getReadableDatabase();

            cursor = db.rawQuery("SELECT Nomen.ROWID as _id, Nomen.KOD5 as KOD5, GRUPPA, Nomen.SGI as SGI, PRICE FROM Nomen JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE WHERE OST>0 AND (LOWER(Nomen.DESCR) LIKE '" + Condition + "' OR LOWER(Nomen.KOD5) LIKE '" + Condition + "') ORDER BY Nomen.DESCR LIMIT 40", null);
            _setNomenPriceWithoutSgi(cursor);

            cursor = db.rawQuery("SELECT Nomen.ROWID AS _id, Nomen.KOD5, Nomen.DESCR, OST, PRICE, ZAKAZ, GRUPPA, Nomen.SGI, PD, FOTO, GOFRA, MP, POSTDATA FROM Nomen JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE WHERE OST>0 AND (LOWER(Nomen.DESCR) LIKE '" + Condition + "' OR LOWER(Nomen.KOD5) LIKE '" + Condition + "') ORDER BY Nomen.DESCR LIMIT 40", null);
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getSearchNomInGroup(String SearchStr, String Group) {
        Cursor cursor;
        String[] separated = SearchStr.split(" ");
        StringBuilder Condition = new StringBuilder("%" + SearchStr + "%");
        if (separated.length > 1) {
            Condition = new StringBuilder("%");
            for (String item : separated) {
                Condition.append(item).append("%");
            }
        }

        try {
            SQLiteDatabase db = this.getReadableDatabase();

            cursor = db.rawQuery("SELECT Nomen.ROWID as _id, Nomen.KOD5 as KOD5, GRUPPA, Nomen.SGI as SGI, PRICE FROM Nomen JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE WHERE Nomen.GRUPPA='" + Group + "' AND OST>0 AND (LOWER(Nomen.DESCR) LIKE '" + Condition + "' OR LOWER(Nomen.KOD5) LIKE '" + Condition + "') ORDER BY Nomen.DESCR LIMIT 50", null);
            _setNomenPriceWithoutSgi(cursor);

            cursor = db.rawQuery("SELECT Nomen.ROWID as _id, Nomen.KOD5 as KOD5, Nomen.DESCR, OST, ZAKAZ, PRICE, GRUPPA, Nomen.SGI as SGI, PD, FOTO, GOFRA, MP, POSTDATA FROM Nomen JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE WHERE Nomen.GRUPPA='" + Group + "' AND OST>0 AND (LOWER(Nomen.DESCR) LIKE '" + Condition + "' OR LOWER(Nomen.KOD5) LIKE '" + Condition + "') ORDER BY Nomen.DESCR LIMIT 50", null);
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getOrderNom() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT Nomen.ROWID AS _id, KOD5, Nomen.DESCR, OST, PRICE, ZAKAZ, GRUPPA, Nomen.SGI, GOFRA, MP, POSTDATA FROM Nomen JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE WHERE ZAKAZ<>0 ORDER BY Nomen.DESCR", null);
            return cursor;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getOrderSum() {
        String res = "";
        Cursor cursor = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT SUM(ROUND(PRICE * ZAKAZ, 2)) AS SUM, PRICE, ZAKAZ FROM Nomen WHERE ZAKAZ <> 0", null);

            if (cursor.getCount() == 0)
                return res;

            if (cursor.moveToNext()) {
                res = cursor.getFloat(0) == 0.0 ? "" : " - " + cursor.getFloat(0);
            }

            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return res;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public String getNomenOst(String ID) {
        String Ost = "0";
        Cursor cursor = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT OST FROM Nomen WHERE KOD5='" + ID + "'", null);
            if (cursor.moveToNext()) {
                Ost = cursor.getString(cursor.getColumnIndex("OST"));
            }
            return Ost;
        } catch (Exception e) {
            e.printStackTrace();
            return Ost;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Boolean UpdateOrderHead(String TP_ID, String CONTR_ID, String ADDR_ID, String DelivDate, String Comment) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        try {
            ContentValues updatedValues = new ContentValues();
            updatedValues.put(KEY_ORD_COMMENT, Comment);
            updatedValues.put(KEY_ORD_TP_ID, TP_ID);
            updatedValues.put(KEY_ORD_CONTR_ID, CONTR_ID);
            updatedValues.put(KEY_ORD_ADDR_ID, ADDR_ID);
            updatedValues.put(KEY_ORD_DATA, DelivDate);
            db.update(TB_ORDER, updatedValues, null, null);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public void UpdateQty(String ID, int Qty) {
        Log.d("xd", ID + " " + Qty);
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        try {
            ContentValues updatedValues = new ContentValues();
            updatedValues.put(KEY_ZAKAZ, Qty);
            db.update(TB_NOMEN, updatedValues, "KOD5='" + ID + "'", null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public String GetCod(Long RowID) {
        SQLiteDatabase db;
        Cursor cursor;
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            cursor = db.rawQuery("SELECT FOTO FROM Nomen WHERE rowid=" + RowID, null);
            String x = "";
            if (cursor.moveToNext()) {
                x = cursor.getString(cursor.getColumnIndex("FOTO"));
                cursor.close();
            }
            return x;
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return "";
    }

    public void PlusQty(Long RowID) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET ZAKAZ = CASE WHEN (OST - ZAKAZ) <= 0 THEN ZAKAZ ELSE ZAKAZ + 1 END WHERE rowid=" + RowID);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void MinusQty(Long RowID) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET ZAKAZ = CASE WHEN (ZAKAZ-1)<=0 THEN 0 ELSE ZAKAZ-1 END WHERE rowid=" + RowID);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public Boolean insertOrder(String TP_ID, String Contr_ID, String Addr_ID, String Data, String Comment) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        try {
            if (getCount() == 0) {
                db.execSQL("INSERT INTO ORDERS(TP,CONTR,ADDR,DATA,COMMENT) VALUES ('" + TP_ID + "', '" + Contr_ID + "', '" + Addr_ID + "', '" + Data + "', '" + Comment + "')");
                db.setTransactionSuccessful();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getTpList() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS CODE, 'Выберите торгового представителя' AS DESCR UNION ALL SELECT rowid AS _id, CODE, DESCR FROM TORG_PRED", null);
            return cursor;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getContrList() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 as _id, '0' as CODE, 'Выберете контрагента' as DESCR, '' as STATUS UNION ALL SELECT rowid AS _id, CODE, DESCR, STATUS FROM CONTRS", null);
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getContrFilterList(String FindStr) {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT ROWID AS _id, CODE, DESCR, STATUS FROM CONTRS WHERE LOWER(DESCR) LIKE '%" + FindStr + "%'", null);
            return cursor;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getContrAddress(String ContrID) {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT '0' as _id, '0' AS CODE, 'Выберите адрес доставки' AS DESCR UNION ALL SELECT rowid as _id, CODE, DESCR FROM ADDRS WHERE KONTRCODE ='" + ContrID + "'", null);
            return cursor;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getCount() {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT count(*) AS _id FROM ORDERS", null);
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public int GetTPByID(String ID) {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT ROWID AS _id FROM TORG_PRED WHERE CODE='" + ID + "'", null);
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public String GetContrID() {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT CONTR FROM ORDERS", null);
            if (c.moveToFirst()) {
                return c.getString(0);
            }
            return "";
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public String GetContrAddr() {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT ADDR FROM ORDERS", null);
            if (c.moveToFirst()) {
                return c.getString(0);
            }
            return "0";
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public String GetComment() {
        Cursor c = null;
        String Comment = "";
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT ROWID AS _id, COMMENT FROM ORDERS", null);
            if (c.moveToFirst()) {
                Comment = c.getString(1);
            }
            return Comment;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public String GetDeliveryDate() {
        Cursor c = null;
        String DeliveryDate = "";
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            db.setLockingEnabled(false);
            c = db.rawQuery("SELECT ROWID AS _id, DATA FROM ORDERS", null);
            if (c.moveToFirst()) {
                DeliveryDate = c.getString(1);
            }
            return DeliveryDate;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public void ClearOrderHeader() {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.setLockingEnabled(false);
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM ORDERS");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void ResetNomen() {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.setLockingEnabled(false);
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET ZAKAZ=0 WHERE ZAKAZ>0");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void ResetNomenPrice() {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.setLockingEnabled(false);
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET PRICE=0 WHERE PRICE<>0");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public String GetToolbarContr() {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            db.setLockingEnabled(false);
            c = db.rawQuery("SELECT CONTRS.DESCR FROM CONTRS JOIN ORDERS ON ORDERS.CONTR=CONTRS.CODE", null);
            if (c.moveToFirst()) {
                return c.getString(0).trim();
            }
            return "";
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public Cursor getDebet(String TP_ID) {
        Cursor cursor;
        String Sql = "";
        if (!TP_ID.equals("0")) {
            Sql = " WHERE DEBET.TP='" + TP_ID + "'";
        }

        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT DISTINCT\n" +
                    "DEBET.ROWID AS _id ,\n" +
                    "DEBET.KONTR AS DESCR, \n" +
                    "DEBET.STATUS AS STATUS, \n" +
                    "DEBET.KREDIT, \n" +
                    "DEBET.DOLG AS SALDO,\n" +
                    "DEBET.A7 AS A7, \n" +
                    "DEBET.A14 AS A14, \n" +
                    "DEBET.A21 AS A21, \n" +
                    "DEBET.A28 AS A28, \n" +
                    "DEBET.A35 AS A35, \n" +
                    "DEBET.A42 AS A42, \n" +
                    "DEBET.A49 AS A49, \n" +
                    "DEBET.A56 AS A56, \n" +
                    "DEBET.A63 AS A63, \n" +
                    "DEBET.A64 AS A64, \n" +
                    "DEBET.OTGR30 AS OTG30, \n" +
                    "DEBET.OPL30 AS OPL30, \n" +
                    "DEBET.OTGR30/DEBET.DOLG AS KOB, \n" +
                    "DEBET.SCHET AS FIRMA, \n" +
                    "DEBET.DOGOVOR AS CRT_DATE \n" +
                    "FROM DEBET " + Sql + " ORDER BY DEBET.KONTR", null);
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor SearchInDebet(String Contr) {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT DISTINCT\n" +
                    "DEBET.ROWID AS _id ,\n" +
                    "DEBET.KONTR AS DESCR, \n" +
                    "DEBET.STATUS AS STATUS, \n" +
                    "DEBET.KREDIT, \n" +
                    "DEBET.DOLG AS SALDO,\n" +
                    "DEBET.A7 AS A7, \n" +
                    "DEBET.A14 AS A14, \n" +
                    "DEBET.A21 AS A21, \n" +
                    "DEBET.A28 AS A28, \n" +
                    "DEBET.A35 AS A35, \n" +
                    "DEBET.A42 AS A42, \n" +
                    "DEBET.A49 AS A49, \n" +
                    "DEBET.A56 AS A56, \n" +
                    "DEBET.A63 AS A63, \n" +
                    "DEBET.A64 AS A64, \n" +
                    "DEBET.OTGR30 AS OTG30, \n" +
                    "DEBET.OPL30 AS OPL30, \n" +
                    "DEBET.OTGR30/DEBET.DOLG AS KOB, \n" +
                    "DEBET.SCHET AS FIRMA, \n" +
                    "DEBET.DOGOVOR AS CRT_DATE \n" +
                    "FROM DEBET WHERE LOWER(DESCR) LIKE '%" + Contr.toLowerCase() + "%' ORDER BY DESCR", null);
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int GetContrRowID() {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT CONTRS.ROWID AS _id FROM ORDERS JOIN CONTRS ON ORDERS.CONTR = CONTRS.CODE", null);
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public int GetTPRowID() {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT TORG_PRED.ROWID AS _id FROM ORDERS JOIN TORG_PRED ON ORDERS.TP = TORG_PRED.CODE", null);
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public String getLastUpdateTime() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT DATA FROM CONFIG WHERE NAME='" + "TIME_UPDATE" + "'", null);
            if (c.moveToNext()) {
                return c.getString(c.getColumnIndex("DATA"));
            } else {
                c.close();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (c != null)
                c.close();
        }
    }

    // TODO: сделать нормально с помощью стека
    public void fillAllNomenPrice() {
        SQLiteDatabase db = this.getWritableDatabase();
        new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                final Cursor temp;
                temp = db.rawQuery("SELECT GRUPS.CODE as GROUPID, SGI.CODE as SGIID FROM GRUPS JOIN SGI ON SGI.CODE = GRUPS.SGI", null);

                while (temp.moveToNext()) {
                    String Group = temp.getString(temp.getColumnIndex("GROUPID"));
                    String SGI = temp.getString(temp.getColumnIndex("SGIID"));
                    _setNomPriceByGroup(SGI, Group);
                }
                temp.close();
            }
        }).start();
    }

    private void _setNomenPriceWithoutSgi(Cursor cursor) {
        if (cursor.getCount() == 0)
            return;

        while (cursor.moveToNext()) {
            if (pricesMap.containsKey(cursor.getString(cursor.getColumnIndex("KOD5"))))
                continue;

            String groupID = cursor.getString(cursor.getColumnIndex("GRUPPA"));
            if (!listOfUpdatedGroups.contains(groupID))
                _setNomPriceByGroup(cursor.getString(cursor.getColumnIndex("SGI")), groupID);
        }

        cursor.close();
    }

    private void _setNomPriceByGroup(String SgiID, String GroupID) {
        System.out.println(SgiID + " " + GroupID);
        Cursor cursor1 = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();

            // Обновление цен по GROUP
            if (!SgiID.equals("")) {
                cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "' AND SGI = '" + SgiID + "' AND GRUPPA = '" + GroupID + "'", null);
            } else {
                cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "'", null);
            }
            _setNomenPriceWithSgi(cursor1, GroupID);

            // Обновление цен по SGI
            if (!SgiID.equals("")) {
                cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "' AND SGI = '" + SgiID + "' AND GRUPPA IS NULL", null);
            } else {
                cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "'", null);
            }
            _setNomenPriceWithSgi(cursor1, GroupID);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor1 != null)
                cursor1.close();
        }
    }

    private void _setNomenPriceWithSgi(Cursor cursor, String GroupID) {
        System.out.println(listOfUpdatedGroups);

        listOfUpdatedGroups.add(GroupID);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursorSetPrices = null;
        while (cursor.moveToNext()) {
            cursorSetPrices = db.rawQuery("SELECT NOMEN, CENA FROM PRICES WHERE TIPCEN = '" + cursor.getString(cursor.getColumnIndex("TIPCEN")) + "' AND NOMEN IN (SELECT KOD5 FROM NOMEN WHERE GRUPPA = '" + GroupID + "')", null);

            for (int j = 0; j < cursorSetPrices.getCount(); j++) {
                cursorSetPrices.moveToNext();

                if (pricesMap.containsKey(cursorSetPrices.getString(cursorSetPrices.getColumnIndex("NOMEN"))))
                    continue;

                float price = Float.parseFloat(cursorSetPrices.getString(cursorSetPrices.getColumnIndex("CENA"))) * (1 - Float.parseFloat(cursor.getString(cursor.getColumnIndex("SKIDKA"))) / 100);
//                db.execSQL("UPDATE NOMEN SET PRICE = CASE WHEN PRICE != 0 THEN PRICE ELSE'" + price + "' END WHERE KOD5 = '" + cursorSetPrices.getString(cursorSetPrices.getColumnIndex("NOMEN")) + "'");
                pricesMap.put(cursorSetPrices.getString(cursorSetPrices.getColumnIndex("NOMEN")), price);
            }
        }
        if (cursorSetPrices != null)
            cursorSetPrices.close();

        cursor.close();
    }

    public void putPriceInNomen(long id, String price) {
        SQLiteDatabase db;
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET PRICE = CASE WHEN PRICE != '0' THEN PRICE ELSE '" + price + "' END WHERE rowid=" + id);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }
}
