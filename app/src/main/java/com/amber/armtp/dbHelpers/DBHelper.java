package com.amber.armtp.dbHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.amber.armtp.Config;
import com.amber.armtp.GlobalVars;
import com.amber.armtp.ServerDetails;
import com.amber.armtp.annotations.Async;
import com.amber.armtp.auxiliaryData.CounterAgentInfo;
import com.amber.armtp.ui.OrderHeadFragment;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
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
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
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
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS CODE, 'Выберите' AS DESCR UNION ALL SELECT ROWID AS _id, CODE, DESCR FROM FOKUS", null);

            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getGroupsBySgi(String KEY_GRUP_SGIID) {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            if (KEY_GRUP_SGIID.equals("0")) {
                cursor = db.rawQuery("SELECT 0 AS _id, 0 AS CODE, 'Выберите группу' AS DESCR, 0 AS SGI UNION ALL SELECT ROWID AS _id, CODE, DESCR, SGI FROM GRUPS", null);
            } else {
                cursor = db.rawQuery("SELECT 0 AS _id, 0 AS CODE, 'Выберите группу' AS DESCR, 0 AS SGI UNION ALL SELECT ROWID AS _id, CODE, DESCR, SGI FROM GRUPS WHERE SGI='" + KEY_GRUP_SGIID + "'", null);
            }
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getNomen(String sgiID, String groupID,
                           String wcID, String focusID, String searchName) {
        String sqlMX = "", searchReq = "";
        if (!searchName.equals("")) {
            String[] separated = searchName.split(" ");
            StringBuilder Condition = new StringBuilder("%" + searchName + "%");
            if (separated.length >= 1) {
                Condition = new StringBuilder("%");
                for (String item : separated) {
                    Condition.append(item.toLowerCase(Locale.ROOT)).append("%");
                }
            }
            searchReq = " AND (LOWER(Nomen.DESCR) LIKE '" + Condition + "' OR LOWER(Nomen.KOD5) LIKE '" + Condition + "')";
        }

        sqlMX += (!wcID.equals("0") && !wcID.equals("Выберите") && !wcID.equals("Не использовать") && !wcID.equals("!Не определено") && !wcID.equals("Не имеет значения")) ? " AND Nomen.DEMP='" + wcID + "'" : "";
        sqlMX += (!focusID.equals("0")) ? " AND Nomen.FOKUS='" + focusID + "'" : "";
        sqlMX += (!sgiID.equals("0")) ? " AND Nomen.SGI='" + sgiID + "'" : "";
        sqlMX += (!groupID.equals("0")) ? " AND Nomen.GRUPPA='" + groupID + "'" : "";

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            // Ошибка была в том, что cursor был объявлен вне try-catch (что странно)
            return db.rawQuery("SELECT Nomen.ROWID AS _id, Nomen.KOD5, Nomen.DESCR, OST, PRICE, ZAKAZ, GRUPPA, NOMEN.SGI, FOTO, GRUPS.DESCR AS GRUP, PD, GOFRA, MP, POSTDATA, [" + GlobalVars.TypeOfPrice + "], [ACTION] FROM Nomen JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE WHERE OST>0 " + sqlMX + searchReq + " ORDER BY Nomen.GRUPPA", null);
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
            cursor = db.rawQuery("SELECT Nomen.ROWID AS _id, KOD5, Nomen.DESCR, OST,[" + GlobalVars.TypeOfPrice + "] AS PRICE, ZAKAZ, GRUPPA, Nomen.SGI, GOFRA, MP, POSTDATA, [ACTION], FOTO, PD FROM Nomen JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE WHERE ZAKAZ<>0 ORDER BY Nomen.DESCR", null);
            return cursor;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getOrderSum(boolean isSales) {
        String res = "";
        Cursor cursor = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT [" + GlobalVars.TypeOfPrice + "], ZAKAZ, KOD5 FROM Nomen WHERE ZAKAZ <> 0", null);
            float curPrice = 0;
            if (!isSales) {
                while (cursor.moveToNext()) {
                    curPrice += (Float.parseFloat(cursor.getString(0).replace(",", ".")) * cursor.getInt(1));
                }
            } else {
                while (cursor.moveToNext()) {
                    String kod5 = cursor.getString(cursor.getColumnIndex("KOD5"));
                    if (!pricesMap.containsKey(kod5)) {
                        curPrice += Float.parseFloat(cursor.getString(0).replace(",", ".")) * cursor.getInt(1);
                    } else {
                        curPrice += pricesMap.get(kod5) * cursor.getInt(1);
                    }
                }
            }

            res = curPrice == 0.0 ? "" : String.format(Locale.ROOT, "%.2f", curPrice);

            return res;
        } catch (Exception e) {
//            e.printStackTrace();
            return res;
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
//            if (Qty > OST) Qty = OST;
            db.execSQL("UPDATE NOMEN SET ZAKAZ = '" + Qty + "' WHERE KOD5 = '" + ID + "'");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String[] getPhotoNames(Long RowID) {
        SQLiteDatabase db;
        Cursor cursor;
        db = this.getWritableDatabase();
        try {
            cursor = db.rawQuery("SELECT FOTO, FOTO2 FROM Nomen WHERE rowid=" + RowID, null);
            String[] namesOfPhotos = null;
            while (cursor.moveToNext()) {
                namesOfPhotos = new String[]{
                        cursor.getString(0),
                        cursor.getString(1)
                };
            }
            cursor.close();
            namesOfPhotos = Arrays.stream(namesOfPhotos).filter(Objects::nonNull).toArray(String[]::new);
            return namesOfPhotos;
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String[] getPhotoNames(String kod5) {
        SQLiteDatabase db;
        Cursor cursor;
        db = this.getWritableDatabase();
        try {
            cursor = db.rawQuery("SELECT FOTO, FOTO2 FROM Nomen WHERE kod5=" + kod5, null);
            String[] namesOfPhotos = null;
            while (cursor.moveToNext()) {
                namesOfPhotos = new String[]{
                        cursor.getString(0),
                        cursor.getString(1)
                };
            }
            cursor.close();
            namesOfPhotos = Arrays.stream(namesOfPhotos).filter(Objects::nonNull).toArray(String[]::new);
            return namesOfPhotos;
        } catch (SQLiteException e) {
            Config.sout(e);
            e.printStackTrace();
        }
        return new String[0];
    }

    public void PlusQty(String ID) {
        SQLiteDatabase db;
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
//            db.execSQL("UPDATE Nomen SET ZAKAZ = CASE WHEN (OST - ZAKAZ) <= 0 THEN ZAKAZ ELSE ZAKAZ + 1 END WHERE KOD5=" + ID);
            db.execSQL("UPDATE Nomen SET ZAKAZ = ZAKAZ + 1 WHERE KOD5='" + ID + "'");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Config.sout(e);
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
//            db.execSQL("UPDATE Nomen SET ZAKAZ = CASE WHEN (ZAKAZ-1) <= 0 THEN 0 ELSE ZAKAZ-1 END WHERE KOD5=" + ID);
            db.execSQL("UPDATE Nomen SET ZAKAZ = CASE WHEN (ZAKAZ-1) <= 0 THEN 0 ELSE ZAKAZ-1 END WHERE KOD5='" + ID + "'");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Config.sout(e);
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
            cursor = db.rawQuery("SELECT 0 as _id, '0' as CODE, 'Выберите контрагента' as DESCR, '' as STATUS UNION ALL SELECT rowid AS _id, CODE, DESCR, STATUS FROM CONTRS", null);
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

    public String getNameOfTpById(String ID) {
        if (!isTableExisted("TORG_PRED")) {
            Config.sout("Таблица TORG_PRED не существует, обновите базу данных");
            return "";
        }
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT DESCR FROM TORG_PRED WHERE CODE='" + ID + "'", null)) {
            if (c.moveToFirst()) {
                return c.getString(0);
            }
            return "";
        } catch (Exception e) {
            return "";
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
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            db.execSQL("UPDATE Nomen SET ZAKAZ=0 WHERE ZAKAZ>0");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void ResetNomenPrice(boolean isCopied) {
        SQLiteDatabase db = this.getWritableDatabase();

        // TODO: make showing progress bar
        db.beginTransaction();
        listOfUpdatedGroups.clear();

        Cursor cCheck = db.rawQuery("SELECT GRUPPA, SGI, KOD5 FROM NOMEN WHERE ZAKAZ <> 0", null);

        if (!isCopied) {
            db.execSQL("UPDATE Nomen SET ZAKAZ=0 WHERE ZAKAZ<>0");
        }

        if (isCopied || cCheck.getCount() != 0) {
            Cursor c = db.rawQuery("SELECT GRUPPA, SGI, KOD5 FROM NOMEN WHERE PRICE <> 0 ORDER BY GRUPPA", null);
            setNomenPriceWithoutSgi(c);

            Set<String> keys = pricesMap.keySet();
            for (String key : keys) {
                float price = pricesMap.get(key);
                db.execSQL("UPDATE NOMEN SET PRICE = '" + price + "' WHERE KOD5 = '" + key + "'");
            }
        } else {
            db.execSQL("UPDATE Nomen SET PRICE=0 WHERE PRICE<>0");
        }
        pricesMap.clear();

        cCheck.close();
        db.setTransactionSuccessful();
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
        String Sql = "";
        if (!TP_ID.equals("0"))
            Sql = " WHERE DEBET.TP='" + TP_ID + "'";

        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT DISTINCT " +
                    "DEBET.ROWID AS _id ," +
                    "DEBET.KONTR AS DESCR," +
                    "DEBET.STATUS AS STATUS," +
                    "DEBET.KREDIT," +
                    "DEBET.DOLG AS SALDO," +
                    "DEBET.A7 AS A7," +
                    "DEBET.A14 AS A14," +
                    "DEBET.A21 AS A21," +
                    "DEBET.A28 AS A28," +
                    "DEBET.A35 AS A35," +
                    "DEBET.A42 AS A42," +
                    "DEBET.A49 AS A49," +
                    "DEBET.A56 AS A56," +
                    "DEBET.A63 AS A63," +
                    "DEBET.A64 AS A64," +
                    "DEBET.OTGR30 AS OTG30," +
                    "DEBET.OPL30 AS OPL30," +
                    "DEBET.K_OBOR AS KOB," +
                    "DEBET.SCHET AS FIRMA," +
                    "DEBET.DOGOVOR AS CRT_DATE " +
                    "FROM DEBET " + Sql + " ORDER BY DEBET.KONTR", null);
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getContrRowID() {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT CONTRS.ROWID AS _id FROM ORDERS JOIN CONTRS ON ORDERS.CONTR = CONTRS.CODE", null);
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public int getTPRowID() {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT TORG_PRED.ROWID AS _id FROM ORDERS JOIN TORG_PRED ON ORDERS.TP = TORG_PRED.CODE", null);
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
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
        String res = null;
        if (c.moveToNext()) {
            res = c.getString(c.getColumnIndex("DATA"));
        }
        c.close();
        return res;
    }

    @Async
    public void putPriceInNomen(String id, String price) {
        SQLiteDatabase db;
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET PRICE = '" + price + "' WHERE KOD5=" + id);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void putPriceInNomen(long rowID, String price) {
        SQLiteDatabase db;
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET PRICE = CASE WHEN PRICE != '0' THEN PRICE ELSE '" + price + "' END WHERE rowid=" + rowID);
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

    public String getPriceType(String ContrID) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT PRICE FROM CONTRS WHERE CODE=?", new String[]{ContrID})) {
            c.moveToNext();
            return c.getString(0);
        } catch (Exception e) {
            return "";
        }
    }

    public String getProductKod5ByRowID(long ID) {
        SQLiteDatabase database = this.getReadableDatabase();
        String kod5 = "";
        try {
            Cursor cursor = database.rawQuery("SELECT KOD5 FROM NOMEN WHERE rowID=" + ID + "", null);
            cursor.moveToNext();
            kod5 = cursor.getString(cursor.getColumnIndex("KOD5"));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return kod5;
    }

    public void setBackupIp() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT DATA FROM CONFIG WHERE NAME='FTP_RESERVED'", null);
            cursor.moveToNext();
            ServerDetails.getInstance().setBackupIp(cursor.getString(0));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setNomenPriceWithoutSgi(Cursor cursor) {
        if (cursor.getCount() == 0)
            return;

        while (cursor.moveToNext()) {
            if (pricesMap.containsKey(cursor.getString(cursor.getColumnIndex("KOD5"))))
                continue;

            String groupID = cursor.getString(cursor.getColumnIndex("GRUPPA"));
            String sgiID = cursor.getString(cursor.getColumnIndex("SGI"));
            if (!listOfUpdatedGroups.contains(groupID))
                setNomPriceBySgiAndGroup(sgiID, groupID);
        }

        cursor.close();
    }

    private void setNomPriceBySgiAndGroup(String SgiID, String GroupID) {
        Cursor cursor1 = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();

            // Обновление цен по GROUP
            if (!SgiID.equals("")) {
                cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "' AND SGI = '" + SgiID + "' AND GRUPPA = '" + GroupID + "'", null);
                setNomenPriceWithSgi(cursor1, GroupID);
            }

            // Обновление цен по SGI
            if (!SgiID.equals("")) {
                cursor1 = db.rawQuery("SELECT SKIDKA, TIPCEN, SGI FROM SKIDKI WHERE KONTR = '" + OrderHeadFragment.CONTR_ID + "' AND SGI = '" + SgiID + "' AND GRUPPA IS NULL", null);
                setNomenPriceWithSgi(cursor1, GroupID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor1 != null)
                cursor1.close();
        }
    }

    private void setNomenPriceWithSgi(Cursor cursor, String GroupID) {
        listOfUpdatedGroups.add(GroupID);

        boolean isPulledFields = false;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursorSetPrices = null;
        while (cursor.moveToNext()) {
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

    public void clearOrder() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        db.execSQL("UPDATE NOMEN SET ZAKAZ = 0 WHERE ZAKAZ <> 0");
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void clearPhoto() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        db.execSQL("UPDATE NOMEN SET PD = 0 WHERE PD <> 0");
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public String[] countSaleDataInRealTableById(String tpID, String[] dateData) {
        String[] dateFrom = dateData[0].split("\\.");
        ArrayUtils.swap(dateFrom, 0, 2);
        dateFrom[0] = dateFrom[0].substring(2);
        String df = StringUtils.join(dateFrom, "");

        String[] dateTo = dateData[1].split("\\.");
        ArrayUtils.swap(dateTo, 0, 2);
        dateTo[0] = dateTo[0].substring(2);
        String dt = StringUtils.join(dateTo, "");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT CAST((substr(data, 7, 4) || '' || substr(data, 4, 2) || '' || substr(data, 1, 2)) as INTEGER) as x, SUM(SUMMA) AS SUMMA, SUM(KOL) as KOL FROM REAL WHERE TORG_PRED=? AND (x >= ? and x <= ?)", new String[]{tpID, df, dt});
        cursor.moveToNext();
        Double res = cursor.getDouble(1);
        Integer count = cursor.getInt(2);

        cursor.close();
        return new String[]{
                res == null || res == 0 ? "0.00" : String.format(Locale.ROOT, "%.2f", res),
                count == null || count == 0 ? "0" : String.valueOf(count)
        };
    }

    private void updatePrices(Cursor c) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        while (c.moveToNext()) {
            String nomen = c.getString(c.getColumnIndex("NOMEN"));
            float price = Float.parseFloat(decimalFormat.format(Float.parseFloat(c.getString(c.getColumnIndex("CENA"))) * (1 - Float.parseFloat(c.getString(c.getColumnIndex("SKIDKA"))) / 100)).replace(",", "."));
            if (pricesMap.containsKey(nomen) && pricesMap.get(nomen) == price || Float.isNaN(price)) {
                continue;
            }
//            Log.d("xd", nomen + " " + price + " " + c.getString(c.getColumnIndex("TIPCEN")) + " " + c.getString(c.getColumnIndex("SKIDKA")));
            pricesMap.put(nomen, price);
        }
        c.close();
    }

    public void putAllNomenPrices(String CONTR) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT PRICES.NOMEN as NOMEN, CENA, SKIDKI.SKIDKA as SKIDKA, SKIDKI.TIPCEN as TIPCEN" +
                " FROM SKIDKI JOIN PRICES ON SKIDKI.TIPCEN=PRICES.TIPCEN JOIN NOMEN ON NOMEN.KOD5=PRICES.NOMEN AND NOMEN.SGI=SKIDKI.SGI AND SKIDKI.GRUPPA IS NULL AND SKIDKI.TIPCEN IS NOT NULL WHERE SKIDKI.KONTR=?", new String[]{CONTR});
        updatePrices(c);
        c.close();
        Cursor c1 = db.rawQuery("SELECT PRICES.NOMEN as NOMEN, CENA, SKIDKI.SKIDKA as SKIDKA, SKIDKI.TIPCEN as TIPCEN" +
                " FROM SKIDKI JOIN PRICES ON SKIDKI.TIPCEN=PRICES.TIPCEN JOIN NOMEN ON NOMEN.KOD5=PRICES.NOMEN AND NOMEN.GRUPPA=SKIDKI.GRUPPA AND SKIDKI.GRUPPA IS NOT NULL AND SKIDKI.TIPCEN IS NOT NULL WHERE SKIDKI.KONTR=?", new String[]{CONTR});
        updatePrices(c1);
        c1.close();
    }

    /**
     * Getting KREDIT, DOGOVOR, DOLG from DEBET table
     *
     * @param itemID - contrs code
     * @return string array with KREDIT, DOGOVOR, DOLG accordingly
     */
    public String[] getDebetInfoByContrID(String itemID) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT DOLG FROM DEBET WHERE CODE=?", new String[]{itemID})) {
            c.moveToNext();
            return new String[]{
//                    c.getString(c.getColumnIndex("KREDIT")),
//                    c.getString(c.getColumnIndex("DOGOVOR")),
                    c.getString(c.getColumnIndex("DOLG")),
            };
        } catch (Exception e) {
            return new String[]{"", "", ""};
        }
    }

    public boolean isSettingTpIDIsExistedInDB(String tpID) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT ROWID FROM TORG_PRED WHERE CODE=?", new String[]{tpID})) {
            return c.getCount() != 0;
        } catch (Exception e) {
            Config.sout(e);
            e.printStackTrace();
            return false;
        }
    }

    public String getStartDate() {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT DATA FROM CONFIG WHERE NAME='REAL_DATE_BEG'", null)) {
            c.moveToNext();
            return c.getString(c.getColumnIndex("DATA"));
        } catch (Exception e) {
            Config.sout(e);
            e.printStackTrace();
            return "";
        }
    }

    public int getContrFlagByID(String contrId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT FLAG FROM CONTRS WHERE CODE=?", new String[]{contrId})) {
            c.moveToNext();
            return c.getInt(c.getColumnIndex("FLAG"));
        } catch (Exception e) {
            Config.sout(e);
            e.printStackTrace();
            return 0;
        }
    }

    public CounterAgentInfo getCounterAgentInfo(String contrID) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT CODE, PASSWORD, EMAIL FROM CONTRS WHERE CODE=?", new String[]{contrID})) {
            c.moveToNext();
            return new CounterAgentInfo(c.getString(0), c.getString(1), c.getString(2));
        } catch (Exception e) {
            Config.sout(e);
            e.printStackTrace();
            return null;
        }
    }

//    public void deleteTP(String tpID) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        db.beginTransaction();
//        db.execSQL("DELETE FROM TORG_PRED WHERE CODE=?", new String[] {tpID});
//        db.setTransactionSuccessful();
//        db.endTransaction();
//    }
}
