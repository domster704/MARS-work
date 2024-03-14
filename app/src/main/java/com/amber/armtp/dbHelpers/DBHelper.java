package com.amber.armtp.dbHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.amber.armtp.ServerDetails;
import com.amber.armtp.annotations.Async;
import com.amber.armtp.auxiliaryData.CounterAgentInfo;
import com.amber.armtp.ui.FormOrderFragment;
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
        try {
            return this.getReadableDatabase().rawQuery("SELECT 0 AS _id, '0' AS CODE, 'Выберите СГИ' AS DESCR UNION ALL SELECT ROWID AS _id, CODE, DESCR FROM SGI", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getFocuses() {
        try {
            return this.getReadableDatabase().rawQuery("SELECT 0 AS _id, '0' AS CODE, 'Выберите' AS DESCR UNION ALL SELECT ROWID AS _id, CODE, DESCR FROM FOKUS", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getGroupsBySgi(String KEY_GRUP_SGIID) {
        String sqlWhereSGI = "";
        if (!KEY_GRUP_SGIID.equals("0")) {
            sqlWhereSGI = " WHERE SGI='" + KEY_GRUP_SGIID + "'";
        }
        try {
            return this.getReadableDatabase().rawQuery("SELECT 0 AS _id, 0 AS CODE, 'Выберите группу' AS DESCR, 0 AS SGI UNION ALL SELECT ROWID AS _id, CODE, DESCR, SGI FROM GRUPS" + sqlWhereSGI, null);
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
            // Ошибка когда-то была в том, что cursor был объявлен вне try-catch (что странно)
            return this.getReadableDatabase().rawQuery("SELECT Nomen.ROWID AS _id, Nomen.KOD5, Nomen.DESCR, OST, PRICE, ZAKAZ, GRUPPA, NOMEN.SGI, FOTO, GRUPS.DESCR AS GRUP, PD, GOFRA, MP, POSTDATA, [" + FormOrderFragment.TypeOfPrice + "], [ACTION], " +
                    " (SELECT group_concat(act_id) as ACT_LIST" +
                    "  FROM (SELECT ACT.ID as act_id," +
                    "               SUM(" +
                    "                       CASE" +
                    "                           WHEN ACT.OFIS_NOT = TORG_PRED.OFIS THEN -10000" +
                    "                           WHEN ACT.OFIS_IN = TORG_PRED.OFIS OR ACT.OFIS_IN = '' THEN 1" +
                    "                           ELSE 0" +
                    "                           END)" +
                    "                      AS ACTION_COUNT" +
                    "        FROM NOMEN_ACT" +
                    "                 JOIN ACT ON ACT.ID = NOMEN_ACT.ACT" +
                    "                 JOIN TORG_PRED ON TORG_PRED.CODE = '" + OrderHeadFragment.TP_ID + "'" +
                    "        WHERE (ACT.OFIS_IN = TORG_PRED.OFIS OR ACT.OFIS_IN = '' OR ACT.OFIS_NOT = TORG_PRED.OFIS)" +
                    "          AND NOMEN_ACT.NOMEN = NOMEN.KOD5" +
                    "        GROUP BY act_id) act_list_table" +
                    "  WHERE ACTION_COUNT > 0) AS ACT_LIST" +
                    " FROM Nomen" +
                    " JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE" +
                    " WHERE OST>0 " + sqlMX + searchReq +
                    " ORDER BY Nomen.GRUPPA", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getOrderNom() {
        try {
            return this.getReadableDatabase().rawQuery("SELECT Nomen.ROWID AS _id, KOD5, Nomen.DESCR, OST,[" + FormOrderFragment.TypeOfPrice + "] AS PRICE, Nomen.PRICE as PRICE_FOR_VIEW_ORDER, ZAKAZ, GRUPPA, Nomen.SGI, GOFRA, MP, POSTDATA, [ACTION], FOTO, PD, " +
                    " (SELECT group_concat(act_id) as ACT_LIST" +
                    "  FROM (SELECT ACT.ID as act_id," +
                    "               SUM(" +
                    "                       CASE" +
                    "                           WHEN ACT.OFIS_NOT = TORG_PRED.OFIS THEN -10000" +
                    "                           WHEN ACT.OFIS_IN = TORG_PRED.OFIS OR ACT.OFIS_IN = '' THEN 1" +
                    "                           ELSE 0" +
                    "                           END)" +
                    "                      AS ACTION_COUNT" +
                    "        FROM NOMEN_ACT" +
                    "                 JOIN ACT ON ACT.ID = NOMEN_ACT.ACT" +
                    "                 JOIN TORG_PRED ON TORG_PRED.CODE = '" + OrderHeadFragment.TP_ID + "'" +
                    "        WHERE (ACT.OFIS_IN = TORG_PRED.OFIS OR ACT.OFIS_IN = '' OR ACT.OFIS_NOT = TORG_PRED.OFIS)" +
                    "          AND NOMEN_ACT.NOMEN = NOMEN.KOD5" +
                    "        GROUP BY act_id) act_list_table" +
                    "  WHERE ACTION_COUNT > 0) AS ACT_LIST" +
                    " FROM Nomen " +
                    " JOIN GRUPS ON Nomen.GRUPPA = GRUPS.CODE " +
                    " WHERE ZAKAZ<>0 " +
                    " ORDER BY Nomen.DESCR", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getOrderSum(boolean isSales) {
        String nomenPriceColumnName = !FormOrderFragment.TypeOfPrice.equals("") ? "[" + FormOrderFragment.TypeOfPrice + "]" : "PRICE";
        try (Cursor cursor = this.getReadableDatabase().rawQuery("SELECT " + nomenPriceColumnName + ", ZAKAZ, KOD5 FROM Nomen WHERE ZAKAZ <> 0", null)) {
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
            return curPrice == 0.0 ? "" : String.format(Locale.ROOT, "%.2f", curPrice);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public Boolean UpdateOrderHead(String TP_ID, String CONTR_ID, String ADDR_ID, String DelivDate, String Comment) {
        SQLiteDatabase db = this.getWritableDatabase();
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
        try {
            this.getWritableDatabase().execSQL("UPDATE NOMEN SET ZAKAZ = '" + Qty + "' WHERE KOD5 = '" + ID + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String[] getPhotoNames(String kod5) {
        try (Cursor cursor = this.getWritableDatabase().rawQuery("SELECT FOTO, FOTO2 FROM Nomen WHERE kod5=" + kod5, null)) {
            String[] namesOfPhotos = null;
            while (cursor.moveToNext()) {
                namesOfPhotos = new String[]{
                        cursor.getString(0),
                        cursor.getString(1)
                };
            }
            namesOfPhotos = Arrays.stream(namesOfPhotos).filter(Objects::nonNull).toArray(String[]::new);
            return namesOfPhotos;
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    public void PlusQty(String ID) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            db.execSQL("UPDATE Nomen SET ZAKAZ = ZAKAZ + 1 WHERE KOD5='" + ID + "'");
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void MinusQty(String ID) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            db.execSQL("UPDATE Nomen SET ZAKAZ = CASE WHEN (ZAKAZ-1) <= 0 THEN 0 ELSE ZAKAZ-1 END WHERE KOD5='" + ID + "'");
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean insertOrder(String TP_ID, String Contr_ID, String Addr_ID, String Data, String Comment) {
        try {
            if (getOrdersCount() != 0) {
                return false;
            }
            this.getWritableDatabase().execSQL("INSERT INTO ORDERS(TP,CONTR,ADDR,DATA,COMMENT) VALUES ('" + TP_ID + "', '" + Contr_ID + "', '" + Addr_ID + "', '" + Data + "', '" + Comment + "')");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Cursor getTpList() {
        try {
            return this.getReadableDatabase().rawQuery("SELECT 0 AS _id, '0' AS CODE, 'Выберите торгового представителя' AS DESCR UNION ALL SELECT rowid AS _id, CODE, DESCR FROM TORG_PRED", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getContrList() {
        try {
            return this.getReadableDatabase().rawQuery("SELECT 0 as _id, '0' as CODE, 'Выберите контрагента' as DESCR, '' as STATUS UNION ALL SELECT rowid AS _id, CODE, TRIM(DESCR), STATUS FROM CONTRS", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getContrFilterList(String FindStr) {
        try {
            return this.getReadableDatabase().rawQuery("SELECT ROWID AS _id, CODE, DESCR, STATUS FROM CONTRS WHERE LOWER(DESCR) LIKE '%" + FindStr + "%'", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getContrAddress(String ContrID) {
        try {
            return this.getReadableDatabase().rawQuery("SELECT '0' as _id, '0' AS CODE, 'Выберите адрес доставки' AS DESCR UNION ALL SELECT rowid as _id, CODE, DESCR FROM ADDRS WHERE KONTRCODE ='" + ContrID + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getOrdersCount() {
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT count(*) AS _id FROM ORDERS", null)) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int GetTPByID(String ID) {
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT ROWID AS _id FROM TORG_PRED WHERE CODE='" + ID + "'", null)) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getNameOfTpById(String ID) {
        if (!isTableExisted("TORG_PRED")) {
//            Config.sout("Таблица TORG_PRED не существует, обновите базу данных");
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
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT ROWID AS _id FROM CONTRS WHERE CODE='" + ID + "'", null)) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String GetContrAddr() {
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT ADDR FROM ORDERS", null)) {
            if (c.moveToFirst()) {
                return c.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }

    public String GetComment() {
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT ROWID AS _id, COMMENT FROM ORDERS", null)) {
            if (c.moveToFirst()) {
                return c.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String GetDeliveryDate() {
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT ROWID AS _id, DATA FROM ORDERS", null)) {
            if (c.moveToFirst()) {
                return c.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void ClearOrderHeader() {
        try {
            this.getWritableDatabase().execSQL("DELETE FROM ORDERS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ResetNomen() {
        try {
            this.getWritableDatabase().beginTransaction();
            this.getWritableDatabase().execSQL("UPDATE Nomen SET ZAKAZ=0 WHERE ZAKAZ>0");
            this.getWritableDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.getWritableDatabase().endTransaction();
        }
    }

    public void ResetNomenPrice(boolean isCopied) {
        SQLiteDatabase db = this.getWritableDatabase();

        // TODO: make showing progress bar
        db.beginTransaction();
        listOfUpdatedGroups.clear();

        Cursor cCheck = db.rawQuery("SELECT GRUPPA, SGI, KOD5 FROM NOMEN WHERE ZAKAZ <> 0", null);
        System.out.println(cCheck.getCount() + " rows");
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
        db.endTransaction();
    }

    public String GetToolbarContr() {
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT CONTRS.DESCR FROM CONTRS JOIN ORDERS ON ORDERS.CONTR=CONTRS.CODE", null)) {
            if (c.moveToFirst()) {
                return c.getString(0).trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public Cursor getDebet(String TP_ID) {
        String Sql = "";
        if (!TP_ID.equals("0"))
            Sql = " WHERE DEBET.TP='" + TP_ID + "'";

        try {
            return this.getReadableDatabase().rawQuery("SELECT DISTINCT " +
                    "DEBET.ROWID AS _id ," +
                    "DEBET.KONTR AS DESCR," +
                    "DEBET.STATUS AS STATUS," +
                    "DEBET.KREDIT," +
                    "DEBET.DOLG AS SALDO," +
                    "DEBET.DATASVERKI AS DATASVERKI," +
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getContrRowID() {
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT CONTRS.ROWID AS _id FROM ORDERS JOIN CONTRS ON ORDERS.CONTR = CONTRS.CODE", null)) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTPRowID() {
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT TORG_PRED.ROWID AS _id FROM ORDERS JOIN TORG_PRED ON ORDERS.TP = TORG_PRED.CODE", null)) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getLastUpdateTime() {
        if (!isTableExisted("CONFIG"))
            return "";

        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT DATA FROM CONFIG WHERE NAME='" + "TIME_UPDATE" + "'", null)) {
            if (c.moveToNext()) {
                return c.getString(c.getColumnIndex("DATA"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Async
    public void putPriceInNomen(String id, String price) {
        try {
            this.getWritableDatabase().execSQL("UPDATE Nomen SET PRICE = '" + price + "' WHERE KOD5=" + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void putPriceInNomen(long rowID, String price) {
        try {
            this.getWritableDatabase().execSQL("UPDATE Nomen SET PRICE = CASE WHEN PRICE != '0' THEN PRICE ELSE '" + price + "' END WHERE rowid=" + rowID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isTableExisted(String tableName) {
        try (Cursor ignored1 = this.getReadableDatabase().rawQuery("SELECT 1 FROM " + tableName, null)) {
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public String getPriceType(String ContrID) {
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT PRICE FROM CONTRS WHERE CODE=?", new String[]{ContrID})) {
            if (c.moveToNext()) {
                return c.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getProductKod5ByRowID(long ID) {
        try (Cursor cursor = this.getReadableDatabase().rawQuery("SELECT KOD5 FROM NOMEN WHERE rowID=" + ID + "", null)) {
            if (cursor.moveToNext()) {
                return cursor.getString(cursor.getColumnIndex("KOD5"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setBackupIp() {
        try (Cursor cursor = this.getReadableDatabase().rawQuery("SELECT DATA FROM CONFIG WHERE NAME='FTP_RESERVED'", null)) {
            if (cursor.moveToNext()) {
                ServerDetails.getInstance().setBackupIp(cursor.getString(0));
            }
//            System.out.println("Work");
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

        Cursor cursorSetPrices = null;
        while (cursor.moveToNext()) {
            cursorSetPrices = this.getWritableDatabase().rawQuery("SELECT NOMEN, CENA, TIPCEN, NOMEN.FIX FROM PRICES JOIN NOMEN on NOMEN.KOD5 = PRICES.NOMEN WHERE TIPCEN = '" + cursor.getString(cursor.getColumnIndex("TIPCEN")) + "' AND NOMEN IN (SELECT KOD5 FROM NOMEN WHERE GRUPPA = '" + GroupID + "')", null);
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
        this.getWritableDatabase().execSQL("UPDATE NOMEN SET ZAKAZ = 0 WHERE ZAKAZ <> 0");
    }

    public void clearPhoto() {
        this.getWritableDatabase().execSQL("UPDATE NOMEN SET PD = 0 WHERE PD <> 0");
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

        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT CAST((substr(data, 7, 4) || '' || substr(data, 4, 2) || '' || substr(data, 1, 2)) as INTEGER) as x, SUM(SUMMA) AS SUMMA, SUM(KOL) as KOL FROM REAL WHERE TORG_PRED=? AND (x >= ? and x <= ?)", new String[]{tpID, df, dt});
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
            String discount = c.getString(c.getColumnIndex("SKIDKA_NEW"));
            String priceWithoutDiscount = c.getString(c.getColumnIndex("CENA"));
            int fix = c.getInt(c.getColumnIndex("FIX"));
            float price;
            if (fix == 1) {
//                price = Float.parseFloat(decimalFormat.format(Float.parseFloat(c.getString(c.getColumnIndex("CONTR_PRICE")))));
                continue;
            }
            price = Float.parseFloat(decimalFormat.format(Float.parseFloat(priceWithoutDiscount) * (1 - Float.parseFloat(discount) / 100)).replace(",", "."));

            if (pricesMap.containsKey(nomen) && pricesMap.get(nomen) == price || Float.isNaN(price)) {
                continue;
            }

            if (nomen.equals("285529")) {
                System.out.println(nomen + " " + price + " " + fix);
            }
            pricesMap.put(nomen, price);
        }
        c.close();
    }

    //    @TimeLogger
    public void putAllNomenPrices(String CONTR) {
        SQLiteDatabase db = this.getWritableDatabase();
        System.out.println(CONTR);
        Cursor c = db.rawQuery("SELECT PRICES.NOMEN as NOMEN, CENA, FIX, SKIDKI.SKIDKA as SKIDKA_NEW, SKIDKI.TIPCEN as TIPCEN, NOMEN.[" + FormOrderFragment.TypeOfPrice + "] as CONTR_PRICE" +
                " FROM SKIDKI" +
                "         JOIN PRICES ON SKIDKI.TIPCEN = PRICES.TIPCEN" +
                "         JOIN NOMEN ON NOMEN.KOD5 = PRICES.NOMEN AND NOMEN.SGI = SKIDKI.SGI" +
                " WHERE SKIDKI.KONTR = ?" +
                " AND SKIDKI.GRUPPA IS NULL" +
                " AND SKIDKI.TIPCEN IS NOT NULL" +
                " AND NOT (SKIDKA_NEW = 0 AND CENA = CONTR_PRICE)", new String[]{CONTR});
        updatePrices(c);
        c.close();
        Cursor c1 = db.rawQuery("SELECT PRICES.NOMEN as NOMEN, CENA, FIX, SKIDKI.SKIDKA as SKIDKA_NEW, SKIDKI.TIPCEN as TIPCEN, NOMEN.[" + FormOrderFragment.TypeOfPrice + "] as CONTR_PRICE" +
                " FROM SKIDKI" +
                "         JOIN PRICES ON SKIDKI.TIPCEN = PRICES.TIPCEN" +
                "         JOIN NOMEN ON NOMEN.KOD5 = PRICES.NOMEN AND NOMEN.GRUPPA = SKIDKI.GRUPPA " +
                " WHERE SKIDKI.KONTR = ?" +
                " AND SKIDKI.GRUPPA IS NOT NULL" +
                " AND SKIDKI.TIPCEN IS NOT NULL" +
                " AND NOT (SKIDKA_NEW = 0 AND CENA = CONTR_PRICE)", new String[]{CONTR});
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
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT ROWID FROM TORG_PRED WHERE CODE=?", new String[]{tpID})) {
            return c.getCount() != 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getStartDate() {
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT DATA FROM CONFIG WHERE NAME='REAL_DATE_BEG'", null)) {
            if (c.moveToNext()) {
                return c.getString(c.getColumnIndex("DATA"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public int getContrFlagByID(String contrId) {
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT FLAG FROM CONTRS WHERE CODE=?", new String[]{contrId})) {
            if (c.moveToNext()) {
                return c.getInt(c.getColumnIndex("FLAG"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public CounterAgentInfo getCounterAgentInfo(String contrID) {
        try (Cursor c = this.getReadableDatabase().rawQuery("SELECT CODE, PASSWORD, EMAIL, INN FROM CONTRS WHERE CODE=?", new String[]{contrID})) {
            if (c.moveToNext()) {
                return new CounterAgentInfo(c.getString(0), c.getString(1), c.getString(2), c.getString(3));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
