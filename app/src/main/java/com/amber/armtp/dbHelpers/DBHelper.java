package com.amber.armtp.dbHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.amber.armtp.Config;
import com.amber.armtp.annotations.PGShowing;
import com.amber.armtp.ui.OrderHeadFragment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Updated by domster704 on 27.09.2021
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "armtp3.db";
    private static final String TB_ORDER = "ORDERS";
    private static final String KEY_ORD_TP_ID = "TP";
    private static final String KEY_ORD_CONTR_ID = "CONTR";
    private static final String KEY_ORD_ADDR_ID = "ADDR";
    private static final String KEY_ORD_DATA = "DATA";
    private static final String KEY_ORD_COMMENT = "COMMENT";
    public static HashMap<String, Float> pricesMap = new HashMap<>();
    public static int limit = 60;
    private final HashSet<String> listOfUpdatedGroups = new HashSet<>();

    public Thread threadResetting;

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
                    _setNomenPriceWithSgi(cursor1, GroupID);
                }

                // Обновление цен по SGI
                if (!SgiID.equals("")) {
                    cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "' AND SGI = '" + SgiID + "' AND GRUPPA IS NULL", null);
                    _setNomenPriceWithSgi(cursor1, GroupID);
                }
            }

            // Инфа про Nomen
            cursor = db.rawQuery("SELECT rowid AS _id, KOD5, DESCR, OST," +
                    " PRICE," +
                    " GRUPPA, ZAKAZ, FOTO, PD, SGI, GOFRA, MP, POSTDATA, [ACTION] FROM Nomen WHERE OST>0 AND Nomen.GRUPPA='" + GroupID + "' ORDER BY Nomen.DESCR", null);
            return cursor;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getNomByGroup(String GroupID, String SgiID, String sqlCondition, String searchString) {
        Cursor cursor1;
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();

            if (!listOfUpdatedGroups.contains(GroupID)) {
                // Обновление цен по GROUP
                if (!SgiID.equals("")) {
                    cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "' AND SGI = '" + SgiID + "' AND GRUPPA = '" + GroupID + "'", null);
                    _setNomenPriceWithSgi(cursor1, GroupID);
                }

                // Обновление цен по SGI
                if (!SgiID.equals("")) {
                    cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "' AND SGI = '" + SgiID + "' AND GRUPPA IS NULL", null);
                    _setNomenPriceWithSgi(cursor1, GroupID);
                }
            }

            // Инфа про Nomen
            cursor = db.rawQuery("SELECT rowid AS _id, KOD5, DESCR, OST," +
                    " PRICE," +
                    " GRUPPA, ZAKAZ, FOTO, PD, SGI, GOFRA, MP, POSTDATA, [ACTION] FROM Nomen WHERE OST>0 " + sqlCondition + searchString + " ORDER BY Nomen.DESCR", null);
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
        if (!SearchName.equals("")) {
            String[] separated = SearchName.split(" ");
            StringBuilder Condition = new StringBuilder("%" + SearchName + "%");
            if (separated.length >= 1) {
                Condition = new StringBuilder("%");
                for (String item : separated) {
                    Condition.append(item.toLowerCase(Locale.ROOT)).append("%");
                }
            }
            searchReq = " AND (LOWER(Nomen.DESCR) LIKE '" + Condition + "' OR LOWER(Nomen.KOD5) LIKE '" + Condition + "')";
        }

        sqlMX += (!WCID.equals("0") && !WCID.equals("Выберите демографический признак") && !WCID.equals("Не использовать") && !WCID.equals("!Не определено") && !WCID.equals("Не имеет значения")) ? " AND Nomen.DEMP='" + WCID + "'" : "";
        sqlMX += (!FocusID.equals("0")) ? " AND Nomen.FOKUS='" + FocusID + "'" : "";
        sqlMX += (!SgiID.equals("0")) ? " AND Nomen.SGI='" + SgiID + "'" : "";
        sqlMX += (!GrupID.equals("0")) ? " AND Nomen.GRUPPA='" + GrupID + "'" : "";

        if (!SgiID.equals("0") && !GrupID.equals("0")) {
            return getNomByGroup(GrupID, SgiID, sqlMX, searchReq);
        }

        String limitS = String.valueOf(limit);
        if (position != 0) {
            limitS = String.valueOf(limit + position);
        }
        String sqlLimit = "LIMIT " + limitS;

        try {
            SQLiteDatabase db = this.getReadableDatabase();

            if (!listOfUpdatedGroups.contains(GrupID)) {
                cursor = db.rawQuery("SELECT Nomen.KOD5, GRUPPA, NOMEN.SGI as SGI FROM Nomen JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE WHERE OST>0 " + sqlMX + searchReq + " ORDER BY Nomen.GRUPPA " + sqlLimit, null);
                _setNomenPriceWithoutSgi(cursor);
            }

            cursor = db.rawQuery("SELECT Nomen.ROWID AS _id, Nomen.KOD5, Nomen.DESCR, OST, PRICE, ZAKAZ, GRUPPA, NOMEN.SGI, FOTO, GRUPS.DESCR AS GRUP, PD, GOFRA, MP, POSTDATA, [ACTION] FROM Nomen JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE WHERE OST>0 " + sqlMX + searchReq + " ORDER BY Nomen.GRUPPA " + sqlLimit, null);
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
            cursor = db.rawQuery("SELECT Nomen.ROWID AS _id, KOD5, Nomen.DESCR, OST, PRICE, ZAKAZ, GRUPPA, Nomen.SGI, GOFRA, MP, POSTDATA, [ACTION], FOTO, PD FROM Nomen JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE WHERE ZAKAZ<>0 ORDER BY Nomen.DESCR", null);
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
            cursor = db.rawQuery("SELECT PRICE, ZAKAZ FROM Nomen WHERE ZAKAZ <> 0", null);
            float curPrice = 0;
            while (cursor.moveToNext()) {
                curPrice += (Float.parseFloat(cursor.getString(0).replace(",", ".")) * cursor.getInt(1));
            }

            res = curPrice == 0.0 ? "" : " - " + String.format(Locale.ROOT, "%.2f", curPrice);

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

    public void UpdateQty(String ID, int Qty, int OST) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        try {
            if (Qty > OST) Qty = OST;
            db.execSQL("UPDATE NOMEN SET ZAKAZ = '" + Qty + "' WHERE KOD5 = '" + ID + "'");
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

    public void PlusQty(String ID) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET ZAKAZ = CASE WHEN (OST - ZAKAZ) <= 0 THEN ZAKAZ ELSE ZAKAZ + 1 END WHERE KOD5=" + ID);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void MinusQty(String ID) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET ZAKAZ = CASE WHEN (ZAKAZ-1) <= 0 THEN 0 ELSE ZAKAZ-1 END WHERE KOD5=" + ID);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public boolean insertOrder(String TP_ID, String Contr_ID, String Addr_ID, String Data, String Comment) {
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

    public int GetContrByID(String ID) {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT ROWID AS _id FROM CONTRS WHERE CODE='" + ID + "'", null);
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
        db = this.getWritableDatabase();
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
        db = this.getWritableDatabase();
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

    public void ResetNomenPrice(boolean isCopied) throws InterruptedException {
        SQLiteDatabase db = this.getWritableDatabase();

        // TODO: make showing progress bar
        threadResetting = new Thread(new Runnable() {
            @Override
            @PGShowing
            public void run() {
                try {
                    db.beginTransaction();
                    pricesMap.clear();
                    listOfUpdatedGroups.clear();

                    Cursor cCheck = db.rawQuery("SELECT GRUPPA, SGI, KOD5 FROM NOMEN WHERE ZAKAZ <> 0", null);

                    if (!isCopied) {
                        db.execSQL("UPDATE Nomen SET ZAKAZ=0 WHERE ZAKAZ<>0");
                    }

                    if (isCopied || cCheck.getCount() != 0) {
                        Cursor c = db.rawQuery("SELECT GRUPPA, SGI, KOD5 FROM NOMEN WHERE PRICE <> 0 ORDER BY GRUPPA", null);
                        _setNomenPriceWithoutSgi(c);

                        Set<String> keys = pricesMap.keySet();
                        for (String key : keys) {
                            float price = pricesMap.get(key);
                            db.execSQL("UPDATE NOMEN SET PRICE = '" + price + "' WHERE KOD5 = '" + key + "'");
                        }
                    } else {
                        db.execSQL("UPDATE Nomen SET PRICE=0 WHERE PRICE<>0");
                    }

                    cCheck.close();
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                }
            }
        });

        threadResetting.start();
        threadResetting.join();
    }

    public String GetToolbarContr() {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
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
        String Sql;
        Sql = " WHERE DEBET.TP='" + TP_ID + "'";

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
                    "DEBET.K_OBOR AS KOB, \n" +
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
                    "DEBET.K_OROB AS KOB, \n" +
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
        if (!isTableExisted("CONFIG"))
            return "";

        Cursor c = db.rawQuery("SELECT DATA FROM CONFIG WHERE NAME='" + "TIME_UPDATE" + "'", null);
        if (c.moveToNext()) {
            return c.getString(c.getColumnIndex("DATA"));
        } else {
            c.close();
            return null;
        }
    }

    public void putPriceInNomen(String id, String price) {
        SQLiteDatabase db;
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET PRICE = CASE WHEN PRICE != '0' THEN PRICE ELSE '" + price + "' END WHERE KOD5=" + id);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void putPriceInNomen(long rowid, String price) {
        SQLiteDatabase db;
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET PRICE = CASE WHEN PRICE != '0' THEN PRICE ELSE '" + price + "' END WHERE rowid=" + rowid);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public boolean isTableExisted(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor ignored1 = db.rawQuery("SELECT 1 FROM " + tableName, null)) {
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

//    public void putAllNomenPrices(String CONTR) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        Cursor allSgiAndGroup = db.rawQuery("SELECT SGI, GRUPS.CODE AS [GROUP] FROM SGI JOIN GRUPS ON GRUPS.SGI = SGI.CODE", null);
//        int i = 1;
//        while (allSgiAndGroup.moveToNext()) {
//            String SGI = allSgiAndGroup.getString(0);
//            String GROUP = allSgiAndGroup.getString(1);
//            System.out.println(SGI + " " + GROUP + " " + i);
//            i += 1;
//            Cursor c = db.rawQuery("SELECT NOMEN, CENA * (1 - SKIDKI.SKIDKA / 100) as PRICE, SKIDKI.TIPCEN FROM PRICES JOIN SKIDKI ON PRICES.TIPCEN=SKIDKI.TIPCEN AND SKIDKI.SGI=? AND SKIDKI.KONTR=? AND PRICES.NOMEN IN (SELECT KOD5 FROM NOMEN WHERE GRUPPA=? AND SGI=?)", new String[]{SGI, CONTR, GROUP, SGI});
//            while (c.moveToNext()) {
//                pricesMap.put(c.getString(0), c.getFloat(1));
//            }
//        }
//    }

    private void _setNomenPriceWithoutSgi(Cursor cursor) {
        if (cursor.getCount() == 0)
            return;
//        SQLiteDatabase db = this.getWritableDatabase();

        while (cursor.moveToNext()) {
            if (pricesMap.containsKey(cursor.getString(cursor.getColumnIndex("KOD5"))))
                continue;
//            String SGI = cursor.getString(cursor.getColumnIndex("SGI"));
//            String GROUP = cursor.getString(cursor.getColumnIndex("GRUPPA"));
//
//            System.out.println(SGI + " " + GROUP);
//
//            if (listOfUpdatedGroups.contains(GROUP))
//                continue;
//
//            Cursor c = db.rawQuery("SELECT NOMEN, CENA * (1 - SKIDKI.SKIDKA / 100) as PRICE, SKIDKI.TIPCEN FROM PRICES JOIN SKIDKI ON PRICES.TIPCEN=SKIDKI.TIPCEN AND SKIDKI.SGI=? AND SKIDKI.KONTR=? AND PRICES.NOMEN IN (SELECT KOD5 FROM NOMEN WHERE GRUPPA=? AND SGI=?)", new String[]{SGI, OrderHeadFragment.CONTR_ID, GROUP, SGI});
//            while (c.moveToNext()) {
//                pricesMap.put(c.getString(0), c.getFloat(1));
//            }
//            listOfUpdatedGroups.add(GROUP);
            String groupID = cursor.getString(cursor.getColumnIndex("GRUPPA"));
            String sgiID = cursor.getString(cursor.getColumnIndex("SGI"));
            if (!listOfUpdatedGroups.contains(groupID))
                _setNomPriceBySgiAndGroup(sgiID, groupID);
        }

        cursor.close();
    }

    private void _setNomPriceBySgiAndGroup(String SgiID, String GroupID) {
        Cursor cursor1 = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();

            // Обновление цен по GROUP
            if (!SgiID.equals("")) {
                cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "' AND SGI = '" + SgiID + "' AND GRUPPA = '" + GroupID + "'", null);
                _setNomenPriceWithSgi(cursor1, GroupID);
            }

            // Обновление цен по SGI
            if (!SgiID.equals("")) {
                cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "' AND SGI = '" + SgiID + "' AND GRUPPA IS NULL", null);
                _setNomenPriceWithSgi(cursor1, GroupID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor1 != null)
                cursor1.close();
        }
    }

    private void _setNomenPriceWithSgi(Cursor cursor, String GroupID) {
        listOfUpdatedGroups.add(GroupID);

        boolean isPulledFields = false;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursorSetPrices = null;
        while (cursor.moveToNext()) {
//            System.out.println(cursor.getString(cursor.getColumnIndex("TIPCEN")));
            cursorSetPrices = db.rawQuery("SELECT NOMEN, CENA, TIPCEN FROM PRICES WHERE TIPCEN = '" + cursor.getString(cursor.getColumnIndex("TIPCEN")) + "' AND NOMEN IN (SELECT KOD5 FROM NOMEN WHERE GRUPPA = '" + GroupID + "')", null);
            for (int j = 0; j < cursorSetPrices.getCount(); j++) {
                cursorSetPrices.moveToNext();
                if (pricesMap.containsKey(cursorSetPrices.getString(cursorSetPrices.getColumnIndex("NOMEN"))))
                    continue;

                float price = Float.parseFloat(cursorSetPrices.getString(cursorSetPrices.getColumnIndex("CENA"))) * (1 - Float.parseFloat(cursor.getString(cursor.getColumnIndex("SKIDKA"))) / 100);
                pricesMap.remove(cursorSetPrices.getString(cursorSetPrices.getColumnIndex("NOMEN")));

                pricesMap.put(cursorSetPrices.getString(cursorSetPrices.getColumnIndex("NOMEN")), price);
                isPulledFields = true;
            }
        }
        if (cursorSetPrices != null)
            cursorSetPrices.close();

        if (isPulledFields)
            listOfUpdatedGroups.add(GroupID);

        cursor.close();
    }
}
