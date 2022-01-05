package com.amber.armtp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Updated by domster704 on 27.09.2021
 */
public class DBHepler extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "armtp3.db";

    private static final String TB_NOMEN = "Nomen";
    private static final String TB_ORDER = "ORDERS";
    private static final String KEY_ZAKAZ = "ZAKAZ";
    private static final String KEY_ORD_TP_ID = "TP";
    private static final String KEY_ORD_CONTR_ID = "CONTR";
    private static final String KEY_ORD_ADDR_ID = "ADDR";
    private static final String KEY_ORD_DATA = "DATA";
    private static final String KEY_ORD_COMMENT = "COMMENT";

    public DBHepler(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        onCreate(db);
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

    public Cursor getGroups() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS CODE, 'Выберите группу' AS DESCR UNION ALL SELECT ROWID AS _id, CODE, DESCR FROM GRUPS", null);

            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getFilterSgi() {
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

            // Обновление цен по GROUP
            if (!SgiID.equals("")) {
                cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "' AND SGI = '" + SgiID + "' AND GRUPPA = '" + GroupID + "'", null);
            } else {
                cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "'", null);
            }
            updatePrices(cursor1, SgiID, GroupID);

            // Обновление цен по SGI
            if (!SgiID.equals("")) {
                cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "' AND SGI = '" + SgiID + "' AND GRUPPA IS NULL", null);
            } else {
                cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "'", null);
            }
            updatePrices(cursor1, SgiID, GroupID);

            // Инфа про Nomen
            cursor = db.rawQuery("SELECT rowid AS _id, KOD5, DESCR, OST," +
                    " PRICE," +
                    " GRUPPA, ZAKAZ, FOTO, PD, SGI FROM Nomen WHERE OST>0 AND Nomen.GRUPPA='" + GroupID + "' ORDER BY Nomen.DESCR", null);
            return cursor;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updatePrices(Cursor cursor, String SgiID, String GroupID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursorSetPrices = null;
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            cursorSetPrices = db.rawQuery("SELECT NOMEN, CENA FROM PRICES WHERE TIPCEN = '" + cursor.getString(1) + "' AND NOMEN IN (SELECT KOD5 FROM NOMEN WHERE SGI = '" + SgiID + "' AND GRUPPA = '" + GroupID + "')", null);

            for (int j = 0; j < cursorSetPrices.getCount(); j++) {
                cursorSetPrices.moveToNext();
                float price = Float.parseFloat(cursorSetPrices.getString(1)) * (1 - Float.parseFloat(cursor.getString(0)) / 100);
                db.execSQL("UPDATE NOMEN SET PRICE = '" + price + "' WHERE KOD5 = '" + cursorSetPrices.getString(0) + "'");
            }
        }
        if (cursorSetPrices != null)
            cursorSetPrices.close();
    }

    public Cursor getNomByFilters(String SgiID, String GrupID, String WCID, String FocusID) {
        Cursor cursor;
        String sqlMX = "";
        sqlMX += (!SgiID.equals("0")) ? " AND Nomen.SGI='" + SgiID + "'" : "";
        sqlMX += (!GrupID.equals("0")) ? " AND Nomen.GRUPPA='" + GrupID + "'" : "";
        sqlMX += (!WCID.equals("0") && !WCID.equals("Не использовать          ") && !WCID.equals("!Не определено           ") && !WCID.equals("Не имеет значения        ")) ? " AND Nomen.DEMP='" + WCID + "'" : "";
        sqlMX += (!FocusID.equals("0")) ? " AND Nomen.FOKUS='" + FocusID + "'" : "";
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT Nomen.ROWID AS _id, Nomen.KOD5, Nomen.DESCR, OST, PRICE, ZAKAZ, GRUPPA, NOMEN.SGI, FOTO, GRUPS.DESCR AS GRUP, PD FROM Nomen JOIN GRUPS ON Nomen.GRUPPA =  GRUPS.CODE WHERE OST>0 " + sqlMX + " ORDER BY Nomen.DESCR", null);
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
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT Nomen.ROWID AS _id, Nomen.KOD5, Nomen.DESCR, OST, printf('%.2f', (SELECT CENA FROM PRICES WHERE NOMEN = NOMEN.KOD5)) AS PRICE,  ZAKAZ,  GRUPPA, Nomen.SGI, PD FROM Nomen JOIN GRUPS ON Nomen.GRUPID = GRUPS.CODE WHERE OST>0 AND (Nomen.DESCR LIKE '" + Condition + "' OR Nomen.KOD5 LIKE '" + Condition + "') ORDER BY Nomen.DESCR", null);
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getSearchNomInGroup(String SearchStr, String Group) {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT Nomen.ROWID AS _id, Nomen.KOD5, Nomen.DESCR, OST, printf('%.2f', (SELECT CENA FROM PRICES WHERE NOMEN = NOMEN.KOD5)) AS PRICE, ZAKAZ, GRUPPA, Nomen.SGI, PD, FOTO FROM Nomen JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE WHERE  OST>0 AND Nomen.GRUPPA='" + Group + "' AND (Nomen.DESCR LIKE '%" + SearchStr + "%' OR Nomen.KOD5 LIKE '%" + SearchStr + "%')  ORDER BY Nomen.DESCR", null);
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
            cursor = db.rawQuery("SELECT Nomen.ROWID AS _id, KOD5, Nomen.DESCR, OST, PRICE, ZAKAZ, GRUPPA, Nomen.SGI, FOTO, PD FROM Nomen JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE WHERE ZAKAZ<>0 ORDER BY Nomen.DESCR", null);
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

    public Boolean updateOrderHead(String TP_ID, String CONTR_ID, String ADDR_ID, String DelivDate, String Comment) {
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
            db.execSQL("UPDATE Nomen SET ZAKAZ = ZAKAZ+1 WHERE rowid=" + RowID);
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

    public void UpdateOrderQty(String ZakID, String ID, int Qty) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        try {
            ContentValues updatedValues = new ContentValues();
            updatedValues.put("QTY", Qty);
            db.update("ZAKAZY_DT", updatedValues, "NOM_ID='" + ID + "' AND ZAKAZ_ID='" + ZakID + "'", null);
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
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS CODE, 'Выберете контрагента' as DESCR UNION ALL SELECT rowid AS _id, CODE, DESCR FROM CONTRS", null);
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
            cursor = db.rawQuery("SELECT ROWID AS _id, CODE, DESCR FROM CONTRS WHERE DESCR LIKE '%" + FindStr + "%'", null);
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

    public int CheckForUpdates() {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            db.setLockingEnabled(false);
            c = db.rawQuery("SELECT count(ROWID) AS CNT FROM ZAKAZY WHERE STATUS=0", null);
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

    public String GetToolbarContr() {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            db.setLockingEnabled(false);
            c = db.rawQuery("SELECT CONTRS.DESCR FROM ORDERS JOIN CONTRS ON ORDERS.CONTR=CONTRS.CODE", null);
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

    public Cursor getDebetByContr(String ID) {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            db.setLockingEnabled(false);
            cursor = db.rawQuery("SELECT\n" +
                    "DEBET.ROWID AS _id ,\n" +
                    "CONTRS.DESCR, \n" +
//                    "CASE WHEN CONTRS.INSTOP=1 THEN 'ЗАПРЕТ' WHEN CONTRS.DOLG=1 THEN 'ДОЛЖНИК'  WHEN CONTRS.DYNAMO THEN 'ДИНАМЩИК' ELSE '' END  AS STATUS,\n" +
                    "CONTRS.INFO AS STATUS, \n" +
                    "DEBET.KREDIT|| CASE WHEN NEKONTR =0 THEN '/x' ELSE '/' END || DEBET.LIM AS KREDIT,  \n" +
                    "printf('%.2f', DEBET.SALDO) AS SALDO,\n" +
                    " printf('%.2f', DEBET.A7) AS A7, \n" +
                    " printf('%.2f', DEBET.A14) AS A14, \n" +
                    " printf('%.2f', DEBET.A21) AS A21, \n" +
                    " printf('%.2f', DEBET.A28) AS A28, \n" +
                    " printf('%.2f', DEBET.A35) AS A35, \n" +
                    " printf('%.2f', DEBET.A42) AS A42, \n" +
                    " printf('%.2f', DEBET.A49) AS A49, \n" +
                    " printf('%.2f', DEBET.A56) AS A56, \n" +
                    " printf('%.2f', DEBET.A63) AS A63, \n" +
                    " printf('%.2f', DEBET.A64) AS A64, \n" +

                    " printf('%.2f', DEBET.OTG30) AS OTG30, \n" +
                    " printf('%.2f', DEBET.OPL30) AS OPL30, \n" +
                    " printf('%.2f', DEBET.OTG30/DEBET.SALDO) AS KOB, \n" +
                    "DEBET.FIRMA, \n" +
                    "CONTRS.CRT_DATE \n" +
                    " FROM DEBET JOIN CONTRS ON RTRIM(DEBET.CONTR_ID)=CONTRS.ID WHERE CONTRS.ID='" + ID + "'", null);
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getDebet(String TP_ID, String Contr_ID) {
        Cursor cursor;
        String Sql = "";
        if (!TP_ID.equals("0") && !Contr_ID.equals("0")) {
            Sql = " WHERE CONTRS.CODE='" + Contr_ID + "' AND (DEBET.TP='" + TP_ID + "')";
        } else {
            if (!Contr_ID.equals("0")) {
                Sql = " WHERE CONTRS.CODE='" + Contr_ID + "'";
            }

            if (!TP_ID.equals("0")) {
                Sql = " WHERE DEBET.TP='" + TP_ID + "'";
            }
        }

        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT DISTINCT\n" +
                    "DEBET.ROWID AS _id ,\n" +
                    "CONTRS.DESCR, \n" +
                    "DEBET.KREDIT, \n" +
                    "printf('%.2f', DEBET.DOLG) AS SALDO,\n" +
                    " printf('%.2f', DEBET.A7) AS A7, \n" +
                    " printf('%.2f', DEBET.A14) AS A14, \n" +
                    " printf('%.2f', DEBET.A21) AS A21, \n" +
                    " printf('%.2f', DEBET.A28) AS A28, \n" +
                    " printf('%.2f', DEBET.A35) AS A35, \n" +
                    " printf('%.2f', DEBET.A42) AS A42, \n" +
                    " printf('%.2f', DEBET.A49) AS A49, \n" +
                    " printf('%.2f', DEBET.A56) AS A56, \n" +
                    " printf('%.2f', DEBET.A63) AS A63, \n" +
                    " printf('%.2f', DEBET.A64) AS A64, \n" +

                    " printf('%.2f', DEBET.OTGR30) AS OTG30, \n" +
                    " printf('%.2f', DEBET.OPL30) AS OPL30, \n" +
                    " printf('%.2f', DEBET.OTGR30/DEBET.DOLG) AS KOB, \n" +
                    " DEBET.SCHET AS FIRMA, \n" +
                    " DEBET.DOGOVOR AS CRT_DATE \n" +
                    " FROM DEBET JOIN CONTRS" + Sql + " ORDER BY CONTRS.DESCR LIMIT 100", null);
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
                    "CONTRS.DESCR, \n" +
//                    "CASE WHEN CONTRS.INSTOP=1 THEN 'ЗАПРЕТ' WHEN CONTRS.DOLG=1 THEN 'ДОЛЖНИК'  WHEN CONTRS.DYNAMO THEN 'ДИНАМЩИК' ELSE '' END  AS STATUS,\n" +
                    "CONTRS.INFO AS STATUS, \n" +
                    "DEBET.KREDIT|| CASE WHEN NEKONTR =0 THEN '/x' ELSE '/' END || DEBET.LIM AS KREDIT,  \n" +
                    "printf('%.2f', DEBET.SALDO) AS SALDO,\n" +
                    " printf('%.2f', DEBET.A7) AS A7, \n" +
                    " printf('%.2f', DEBET.A14) AS A14, \n" +
                    " printf('%.2f', DEBET.A21) AS A21, \n" +
                    " printf('%.2f', DEBET.A28) AS A28, \n" +
                    " printf('%.2f', DEBET.A35) AS A35, \n" +
                    " printf('%.2f', DEBET.A42) AS A42, \n" +
                    " printf('%.2f', DEBET.A49) AS A49, \n" +
                    " printf('%.2f', DEBET.A56) AS A56, \n" +
                    " printf('%.2f', DEBET.A63) AS A63, \n" +
                    " printf('%.2f', DEBET.A64) AS A64, \n" +

                    " printf('%.2f', DEBET.OTG30) AS OTG30, \n" +
                    " printf('%.2f', DEBET.OPL30) AS OPL30, \n" +
                    " printf('%.2f', DEBET.OTG30/DEBET.SALDO) AS KOB, \n" +
                    "DEBET.FIRMA, \n" +
                    "CONTRS.CRT_DATE \n" +
                    " FROM DEBET JOIN CONTRS ON RTRIM(DEBET.CONTR_ID)=CONTRS.ID WHERE CONTRS.lowDESCR LIKE '%" + Contr + "%' ORDER BY CONTRS.DESCR", null);
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
}
