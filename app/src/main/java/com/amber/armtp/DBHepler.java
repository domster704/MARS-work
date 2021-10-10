package com.amber.armtp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Updated by Linker4 on 27.09.2021
 */
public class DBHepler extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "armtp.db";
    private static final String TB_NOMEN = "Nomen";
    private static final String TB_ORDER = "ORDERS";
    private static final String KEY_ZAKAZ = "ZAKAZ";
    private static final String KEY_ORD_TP_ID = "TP_ID";
    private static final String KEY_ORD_CONTR_ID = "CONTR_ID";
    private static final String KEY_ORD_ADDR_ID = "ADDR_ID";
    private static final String KEY_ORD_DATA = "DATA";
    private static final String KEY_ORD_COMMENT = "COMMENT";
    private static final String KEY_ORD_DELIVTIME = "DELIV_TIME";
    private static final String KEY_ORD_GETMONEY = "GETMONEY";
    private static final String KEY_ORD_GETBACKWARD = "GETBACKWARD";
    private static final String KEY_ORD_BACKWARD_TYPE = "BACKTYPE";

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
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, '---- Выберите СГИ ----' AS DESCR UNION ALL SELECT ROWID AS _id, ID, DESCR FROM sgi", null);
            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getTovcats() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, '---- Выберите товарную категорию ----' AS DESCR UNION ALL SELECT ROWID AS _id, ID, DESCR FROM UNI_MATRIX WHERE TYPE_ID=3", null);
            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getFuncs() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, '---- Выберите функциональную группу ----' AS DESCR UNION ALL SELECT ROWID AS _id, ID, DESCR FROM UNI_MATRIX WHERE TYPE_ID=4", null);
            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getBrands() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, '---- Выберите Брэнд ----' AS DESCR UNION ALL SELECT ROWID AS _id, ID, DESCR FROM UNI_MATRIX WHERE TYPE_ID=5", null);
            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getWCs() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, '---- Выберите Демографический признак ----' AS DESCR UNION ALL SELECT ROWID AS _id, ID, DESCR FROM UNI_MATRIX WHERE TYPE_ID=6", null);

            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getProds() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, '---- Выберите производителя/импортера ----' AS DESCR UNION ALL SELECT ROWID AS _id, ID, DESCR FROM UNI_MATRIX WHERE TYPE_ID=7", null);

            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getFocuses() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, '---- Выберите Фокус ----' AS DESCR UNION ALL SELECT ROWID AS _id, ID, DESCR FROM UNI_MATRIX WHERE TYPE_ID=8", null);

            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getModels() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, '---- Выберите модель ----' AS DESCR UNION ALL SELECT ROWID AS _id, ID, DESCR FROM UNI_MATRIX WHERE TYPE_ID=9 AND DESCR<>''", null);

            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getColor() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, '---- Выберите цвет ----' AS DESCR UNION ALL SELECT ROWID AS _id, ID, DESCR FROM UNI_MATRIX WHERE TYPE_ID=10 AND DESCR<>''", null);

            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getGroups() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, '---- Выберите группу ----' AS DESCR UNION ALL SELECT ROWID AS _id, ID, DESCR FROM UNI_MATRIX WHERE TYPE_ID=2", null);

            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getFilterSgi() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, '---- Выберите СГИ ----' AS DESCR UNION ALL SELECT ROWID AS _id, ID, DESCR FROM UNI_MATRIX WHERE TYPE_ID=1", null);
            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getUniFilters(String Descr) {
        Cursor cursor;

        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT rowid as _id, [TYPE_ID], [TYPE_DESCR], [ID], [LOWDESCR] FROM [UNI_MATRIX] WHERE LOWDESCR LIKE '%" + Descr + "%'", null);
            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getGrupBySgi(String KEY_GRUP_SGIID) {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, '---- Выберите группу ----' AS DESCR UNION ALL SELECT ROWID AS _id, ID, DESCR FROM GRUPS WHERE SGIID='" + KEY_GRUP_SGIID + "'", null);
            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getNomByGroup(String KEY_NOM_GROUPID) {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT Nomen.ROWID AS _id, Nomen.ID, Nomen.COD, Nomen.DESCR, OST, printf('%.2f', CASE WHEN SALE_PRICE>0 THEN SALE_PRICE ELSE PRICE END) AS PRICE,  ZAKAZ,  GRUPID, Nomen.SGIID, CASE WHEN PHOTO1 IS NULL THEN '' ELSE PHOTO1 END AS PHOTO1, CASE WHEN PHOTO2 IS NULL THEN '' ELSE PHOTO2 END AS PHOTO2, VKOROB, ISNEW, IS7DAY, IS28DAY, MP, IS_PERM, GRUPS.DESCR AS GRUP, P1D, P2D FROM Nomen JOIN GRUPS ON Nomen.GRUPID =  GRUPS.ID WHERE OST>0 AND Nomen.GRUPID='" + KEY_NOM_GROUPID + "' ORDER BY Nomen.DESCR"
                    , null);
            return cursor;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getNomByFilters(String SgiID, String GrupID, String TovcatID, String FuncID, String BrandID, String WCID, String ProdID, String FocusID, String ModelID, String ColorID) {
        Cursor cursor;
        String sqlMX = "";
        sqlMX += (!SgiID.equals("0")) ? " AND Nomen.SGIID='" + SgiID + "'" : "";
        sqlMX += (!GrupID.equals("0")) ? " AND Nomen.GRUPID='" + GrupID + "'" : "";
        sqlMX += (!TovcatID.equals("0")) ? " AND Nomen.TOVCATID='" + TovcatID + "'" : "";
        sqlMX += (!FuncID.equals("0")) ? " AND Nomen.FUNCID='" + FuncID + "'" : "";
        sqlMX += (!BrandID.equals("0")) ? " AND Nomen.BRANDID='" + BrandID + "'" : "";
        sqlMX += (!WCID.equals("0")) ? " AND Nomen.WCID='" + WCID + "'" : "";
        sqlMX += (!ProdID.equals("0")) ? " AND Nomen.PRODID='" + ProdID + "'" : "";
        sqlMX += (!FocusID.equals("0")) ? " AND Nomen.FOCUSID='" + FocusID + "'" : "";
        sqlMX += (!ModelID.equals("0")) ? " AND Nomen.MODELID='" + ModelID + "'" : "";
        sqlMX += (!ColorID.equals("0")) ? " AND Nomen.COLORID='" + ColorID + "'" : "";
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT Nomen.ROWID AS _id, Nomen.ID, Nomen.COD, Nomen.DESCR, OST, printf('%.2f', CASE WHEN SALE_PRICE>0 THEN SALE_PRICE ELSE PRICE END) AS PRICE,  ZAKAZ,  GRUPID, Nomen.SGIID, CASE WHEN PHOTO1 IS NULL THEN '' ELSE PHOTO1 END AS PHOTO1, CASE WHEN PHOTO2 IS NULL THEN '' ELSE PHOTO2 END AS PHOTO2, VKOROB, ISNEW, IS7DAY, IS28DAY, MP, IS_PERM, GRUPS.DESCR AS GRUP, P1D, P2D FROM Nomen JOIN GRUPS ON Nomen.GRUPID =  GRUPS.ID WHERE OST>0 " + sqlMX + " ORDER BY Nomen.DESCR", null);
            return cursor;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Cursor getNomByUniFilters(String TypeID, String ID) {
        Cursor cursor;
        String sqlMX = "";
        switch (TypeID) {
            case "1":
                sqlMX += (!ID.equals("0")) ? " AND Nomen.SGIID='" + ID + "'" : "";
                break;
            case "2":
                sqlMX += (!ID.equals("0")) ? " AND Nomen.GRUPID='" + ID + "'" : "";
                break;
            case "3":
                sqlMX += (!ID.equals("0")) ? " AND Nomen.TOVCATID='" + ID + "'" : "";
                break;
            case "4":
                sqlMX += (!ID.equals("0")) ? " AND Nomen.FUNCID='" + ID + "'" : "";
                break;
            case "5":
                sqlMX += (!ID.equals("0")) ? " AND Nomen.BRANDID='" + ID + "'" : "";
                break;
            case "6":
                sqlMX += (!ID.equals("0")) ? " AND Nomen.WCID='" + ID + "'" : "";
                break;
            case "7":
                sqlMX += (!ID.equals("0")) ? " AND Nomen.PRODID='" + ID + "'" : "";
                break;
            case "8":
                sqlMX += (!ID.equals("0")) ? " AND Nomen.FOCUSID='" + ID + "'" : "";
                break;
            case "9":
                sqlMX += (!ID.equals("0")) ? " AND Nomen.MODELID='" + ID + "'" : "";
                break;
            case "10":
                sqlMX += (!ID.equals("0")) ? " AND Nomen.COLORID='" + ID + "'" : "";
                break;
        }
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT Nomen.ROWID AS _id, Nomen.ID, Nomen.COD, Nomen.DESCR, OST, printf('%.2f', CASE WHEN SALE_PRICE>0 THEN SALE_PRICE ELSE PRICE END) AS PRICE,  ZAKAZ,  GRUPID, Nomen.SGIID, CASE WHEN PHOTO1 IS NULL THEN '' ELSE PHOTO1 END AS PHOTO1, CASE WHEN PHOTO2 IS NULL THEN '' ELSE PHOTO2 END AS PHOTO2, VKOROB, ISNEW, IS7DAY, IS28DAY, MP, IS_PERM, GRUPS.DESCR AS GRUP, P1D, P2D FROM Nomen JOIN GRUPS ON Nomen.GRUPID =  GRUPS.ID WHERE OST>0 " + sqlMX + " ORDER BY Nomen.DESCR", null);
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
            cursor = db.rawQuery("SELECT Nomen.ROWID AS _id, Nomen.ID, Nomen.COD, Nomen.DESCR, OST, printf('%.2f', CASE WHEN SALE_PRICE>0 THEN SALE_PRICE ELSE PRICE END) AS PRICE,  ZAKAZ,  GRUPID, Nomen.SGIID, CASE WHEN PHOTO1 IS NULL THEN '' ELSE PHOTO1 END AS PHOTO1, CASE WHEN PHOTO2 IS NULL THEN '' ELSE PHOTO2 END AS PHOTO2, VKOROB, ISNEW, IS7DAY, IS28DAY, MP, IS_PERM, GRUPS.DESCR AS GRUP, P1D, P2D FROM Nomen JOIN GRUPS ON Nomen.GRUPID =  GRUPS.ID WHERE  OST>0 AND (Nomen.lowDESCR LIKE '" + Condition + "' OR Nomen.COD LIKE '" + Condition + "') ORDER BY Nomen.DESCR", null);
            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getSearchNomInGroup(String SearchStr, String Group) {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT Nomen.ROWID AS _id, Nomen.ID, Nomen.COD, Nomen.DESCR, OST, printf('%.2f', CASE WHEN SALE_PRICE>0 THEN SALE_PRICE ELSE PRICE END) AS PRICE,  ZAKAZ,  GRUPID, Nomen.SGIID, CASE WHEN PHOTO1 IS NULL THEN '' ELSE PHOTO1 END AS PHOTO1, CASE WHEN PHOTO2 IS NULL THEN '' ELSE PHOTO2 END AS PHOTO2, VKOROB, ISNEW, IS7DAY, IS28DAY, MP, IS_PERM, GRUPS.DESCR AS GRUP, P1D, P2D FROM Nomen JOIN GRUPS ON Nomen.GRUPID =  GRUPS.ID WHERE  OST>0 AND Nomen.GRUPID='" + Group + "' AND (Nomen.lowDESCR LIKE '%" + SearchStr + "%' OR Nomen.COD LIKE '%" + SearchStr + "%')  ORDER BY Nomen.DESCR", null);
            return cursor;

        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getOrderNom() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT Nomen.ROWID AS _id, Nomen.ID, Nomen.COD, Nomen.DESCR, OST, printf('%.2f', CASE WHEN SALE_PRICE>0 THEN SALE_PRICE ELSE PRICE END) AS PRICE,  ZAKAZ,  GRUPID, Nomen.SGIID, CASE WHEN PHOTO1 IS NULL THEN '' ELSE PHOTO1 END AS PHOTO1, CASE WHEN PHOTO2 IS NULL THEN '' ELSE PHOTO2 END AS PHOTO2, VKOROB, ISNEW, IS7DAY, IS28DAY, MP, IS_PERM, GRUPS.DESCR AS GRUP, P1D, P2D FROM Nomen JOIN GRUPS ON Nomen.GRUPID =  GRUPS.ID WHERE ZAKAZ<>0  ORDER BY Nomen.DESCR", null);
            return cursor;

        } catch (Exception e) {
            return null;
        }
    }

    public String getOrderSum() {
        String Sum = "";
        Cursor cursor = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT CASE WHEN CAST(SUM((CASE WHEN SALE_PRICE>0 THEN SALE_PRICE ELSE PRICE END)*ZAKAZ) AS NUMERIC) IS NULL THEN 0 ELSE printf('%.2f', SUM((CASE WHEN SALE_PRICE>0 THEN SALE_PRICE ELSE PRICE END)*ZAKAZ)) END AS SUM FROM Nomen WHERE ZAKAZ<>0", null);
            if (cursor.moveToNext()) {
                Sum = cursor.getString(cursor.getColumnIndex("SUM")).equals("0") ? "" : " - " + cursor.getString(cursor.getColumnIndex("SUM"));
            }
            return Sum;
        } catch (Exception e) {
            return Sum;
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
            cursor = db.rawQuery("SELECT OST FROM Nomen WHERE ID='" + ID + "'", null);
            if (cursor.moveToNext()) {
                Ost = cursor.getString(cursor.getColumnIndex("OST"));
            }
            return Ost;
        } catch (Exception e) {
            return Ost;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Boolean updateOrderHead(String TP_ID, String CONTR_ID, String ADDR_ID, String DelivDate, String Comment, String DelivTime, Integer GetMoney, Integer GetBackward, Long BackwardType) {
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
            updatedValues.put(KEY_ORD_DELIVTIME, DelivTime);
            updatedValues.put(KEY_ORD_GETMONEY, GetMoney);
            updatedValues.put(KEY_ORD_GETBACKWARD, GetBackward);
            updatedValues.put(KEY_ORD_BACKWARD_TYPE, BackwardType);
            db.update(TB_ORDER, updatedValues, null, null);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public Boolean updateQty(String ID, int Qty) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        try {
            ContentValues updatedValues = new ContentValues();
            updatedValues.put(KEY_ZAKAZ, Qty);
            db.update(TB_NOMEN, updatedValues, "ID='" + ID + "'", null);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public int GetCod(Long RowID) {
        SQLiteDatabase db;
        Cursor cursor;
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            cursor = db.rawQuery("SELECT COD FROM Nomen WHERE rowid=" + RowID, null);
            int x = 0;
            if (cursor.moveToNext()) {
                x = cursor.getInt(cursor.getColumnIndex("COD"));
                cursor.close();
            }
            return x;
        } catch (SQLiteException e) {
            Log.d("xd", String.valueOf(e));
        } finally {
            db.endTransaction();
        }

        return -1;
    }

    public Boolean PlusQty(Long RowID) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET ZAKAZ = ZAKAZ+1 WHERE rowid=" + RowID);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public Boolean MinusQty(Long RowID) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET ZAKAZ = CASE WHEN (ZAKAZ-1)<=0 THEN 0 ELSE ZAKAZ-1 END WHERE rowid=" + RowID);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public Boolean updateOrderQty(String ZakID, String ID, int Qty) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        try {
            ContentValues updatedValues = new ContentValues();
            updatedValues.put("QTY", Qty);
            db.update("ZAKAZY_DT", updatedValues, "NOM_ID='" + ID + "' AND ZAKAZ_ID='" + ZakID + "'", null);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public Boolean insertOrder(String TP_ID, String Contr_ID, String Addr_ID, String Data, String Comment, String DelivTime, Integer getMoney, Integer getBackward, Long BackwardType) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        try {
            if (getCount() == 0) {
                db.execSQL("INSERT INTO ORDERS(TP_ID,CONTR_ID,ADDR_ID,DATA, COMMENT, DELIV_TIME, GETMONEY, GETBACKWARD, BACKTYPE) VALUES ('" + TP_ID + "', '" + Contr_ID + "', '" + Addr_ID + "', '" + Data + "', '" + Comment + "', '" + DelivTime + "', " + getMoney + ", " + getBackward + ", " + BackwardType + ")");
                db.setTransactionSuccessful();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
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
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, '---- Выберите торгового представителя ----' AS DESCR UNION ALL SELECT ROWID AS _id, ID , DESCR FROM TORG_PRED", null);
            return cursor;

        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getCenTypes() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT ROW_ID AS _id, CEN_ID , DESCR FROM CEN_TYPES", null);
            return cursor;

        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getContrList() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, '---- Выберите контрагента ----' AS DESCR, 0 AS INSTOP, 0 AS DOLG, 0 AS DYNAMO, '' AS INFO UNION ALL SELECT ROWID AS [_id], ID, DESCR, INSTOP, DOLG, DYNAMO, INFO FROM CONTRS", null);
            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getContrFilterList(String FindStr) {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT ROWID AS _id, ID, DESCR, INSTOP, DOLG, DYNAMO, INFO  FROM CONTRS WHERE lowDESCR LIKE '%" + FindStr + "%'", null);
            return cursor;

        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getContrAddress(String ContrID) {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 0 AS _id, '0' AS ID, '---- Выберите адрес доставки ----' AS DESCR, '' AS DOP_INFO UNION ALL SELECT rowid AS _id, ID, DESCR, DOP_INFO FROM ADDRS WHERE PARENTEXT ='" + ContrID + "'", null);
            return cursor;

        } catch (Exception e) {
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

    public int GetTPRowID() {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT [TORG_PRED].[ROWID] AS [_id] FROM [ORDERS] JOIN [TORG_PRED] ON [ORDERS].[TP_ID]=[TORG_PRED].[ID]", null);
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
            c = db.rawQuery("SELECT [ROWID] AS [_id] FROM [TORG_PRED] WHERE [ID]='" + ID + "'", null);
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

    public int GetContrRowID() {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT [CONTRS].[ROWID] AS [_id] FROM [ORDERS] JOIN [CONTRS] ON [ORDERS].[CONTR_ID]=[CONTRS].[ID]", null);
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

    public int GetCenTypeRowID(String ID) {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT [ROW_ID] AS [_id] FROM [CEN_TYPES] WHERE [CEN_ID]='" + ID + "'", null);
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
            c = db.rawQuery("SELECT CONTR_ID FROM [ORDERS]", null);
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
            c = db.rawQuery("SELECT [ADDR_ID] FROM [ORDERS]", null);
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

    public void DeleteOrderByID(long id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();

            Log.d("xd", String.valueOf(id));
            Cursor cursor = db.rawQuery("SELECT DOCNO FROM ZAKAZY WHERE ROWID=" + id, null);
            String data = "0";
            if (cursor.moveToFirst()) {
                data = cursor.getString(0);
            }
            Log.d("xd", data);

            db.execSQL("DELETE FROM ZAKAZY_DT WHERE ZAKAZ_ID=\"" + data + "\"");
            db.execSQL("DELETE FROM ZAKAZY WHERE ROWID=" + id);
        } catch (Exception ignored) {
        }

    }

    public Boolean ClearOrderHeader() {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.setLockingEnabled(false);
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM ORDERS");
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
            return true;
        }
    }

    public int GetDocNumber() {
        Cursor c = null;
        int DocNo = 0;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            db.setLockingEnabled(false);
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

    public Boolean ResetNomen() {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.setLockingEnabled(false);
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET ZAKAZ=0, SALE_PRICE=0 WHERE ZAKAZ>0");
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
            return true;
        }
    }

    public Boolean SetZakazStatus(int Status, int State) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.setLockingEnabled(false);
        db.beginTransaction();
        try {
            db.execSQL("UPDATE ZAKAZY SET STATUS=" + Status + " WHERE STATUS=" + State);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
            return true;
        }
    }

    public Cursor getZakazy() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            db.setLockingEnabled(false);
            cursor = db.rawQuery("SELECT\n" +
                    "  ZAKAZY.ROWID AS _id,\n" +
                    "  ZAKAZY.DOC_DATE,\n" +
                    "  CONTRS.DESCR AS CONTR,\n" +
                    "  ADDRS.DESCR AS ADDR,\n" +
                    "  CASE ZAKAZY.STATUS WHEN 0 THEN 'Сохранен' WHEN 1 THEN 'Отправлен' WHEN 2 THEN 'Получен' WHEN 3 THEN 'Оформлен' WHEN 4 THEN 'Оформлен(-)' WHEN 6 THEN 'Собран' WHEN 7 THEN 'Собран(-)' WHEN 5 THEN 'Удален' WHEN 99 THEN 'Отменен' END AS STATUS,\n" +
                    "  (SELECT printf('%.2f', ROUND(SUM(QTY*PRICE),2)) FROM ZAKAZY_DT WHERE ZAKAZ_ID=ZAKAZY.DOCNO) AS SUM,\n" +
                    "  ZAKAZY.DOCNO,\n" +
                    "  ZAKAZY.DELIVERY_DATE\n" +
                    "FROM ZAKAZY\n" +
                    "  JOIN CONTRS ON ZAKAZY.CONTR_ID=CONTRS.ID\n" +
                    "  JOIN ADDRS ON ZAKAZY.ADDR_ID=ADDRS.ID\n" +
                    "WHERE\n" +
                    "  DATE(substr(ZAKAZY.DOC_DATE, 7, 4) || '-' || substr(ZAKAZY.DOC_DATE, 4, 2) || '-' || substr(ZAKAZY.DOC_DATE, 1, 2))\n", null);
            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getZakazyForSend() {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            db.setLockingEnabled(false);
            cursor = db.rawQuery("SELECT\n" +
                    "  ZAKAZY.ROWID AS _id,\n" +
                    "  ZAKAZY.DOC_DATE,\n" +
                    "  CONTRS.DESCR AS CONTR,\n" +
                    "  ADDRS.DESCR AS ADDR,\n" +
                    "  CASE ZAKAZY.STATUS WHEN 0 THEN 'Сохранен' WHEN 1 THEN 'Отправлен' WHEN 2 THEN 'Обработан' END AS STATUS,\n" +
                    "  (SELECT printf('%.2f', ROUND(SUM(QTY*PRICE),2)) FROM ZAKAZY_DT WHERE ZAKAZ_ID=ZAKAZY.DOCNO) AS SUM,\n" +
                    "  ZAKAZY.DOCNO\n" +
                    "FROM ZAKAZY\n" +
                    "  JOIN CONTRS ON ZAKAZY.CONTR_ID=CONTRS.ID\n" +
                    "  JOIN ADDRS ON ZAKAZY.ADDR_ID=ADDRS.ID WHERE ZAKAZY.STATUS = 0 ORDER BY ZAKAZY.ROWID", null);
            return cursor;

        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getZakazDetails(String ZakazID) {
        Cursor cursor;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            db.setLockingEnabled(false);
            cursor = db.rawQuery("SELECT ROWID AS _id, ZAKAZ_ID, NOM_ID, COD5, DESCR, QTY, printf('%.2f', PRICE) AS PRICE, printf('%.2f', ROUND(QTY*PRICE,2)) AS SUM, IS_OUTED, OUT_QTY FROM ZAKAZY_DT WHERE ZAKAZ_ID='" + ZakazID + "'", null);
            return cursor;

        } catch (Exception e) {
            return null;
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
            c = db.rawQuery("SELECT CONTRS.DESCR FROM ORDERS JOIN CONTRS ON ORDERS.CONTR_ID=CONTRS.ID", null);
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

    public void ClearOrderTb() {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.setLockingEnabled(false);
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET ZAKAZ=0 WHERE ZAKAZ<>0");
            db.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            db.endTransaction();
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
            return null;
        }
    }

    public Cursor getDebet(String TP_ID, String Contr_ID) {
        Cursor cursor;
        String Sql = "";
        if (!TP_ID.equals("0") && !Contr_ID.equals("0")) {
            Sql = " WHERE CONTRS.ID='" + Contr_ID + "' AND (DEBET.TP_ID='" + TP_ID + "' AND DEBET.TP_IDS LIKE '%" + TP_ID + "%')";
        } else {
            if (!Contr_ID.equals("0")) {
                Sql = " WHERE CONTRS.ID='" + Contr_ID + "'";
            }

            if (!TP_ID.equals("0")) {
                Sql = " WHERE DEBET.TP_ID='" + TP_ID + "' AND DEBET.TP_IDS LIKE '%" + TP_ID + "%'";
            }
        }

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
                    " FROM DEBET JOIN CONTRS ON RTRIM(DEBET.CONTR_ID)=CONTRS.ID" + Sql + " ORDER BY CONTRS.DESCR", null);
            System.out.println("Sql query:" + "SELECT DISTINCT\n" +
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
                    " FROM DEBET JOIN CONTRS ON RTRIM(DEBET.CONTR_ID)=CONTRS.ID" + Sql + " ORDER BY CONTRS.DESCR");
            return cursor;
        } catch (Exception e) {
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
            return null;
        }
    }

    public void resetContrSales() {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        try {
            db.execSQL("UPDATE Nomen SET SALE_PRICE=0 WHERE SALE_PRICE>0");
            db.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            db.endTransaction();
        }
    }

    public void calcSales(String Contr_ID) {
        SQLiteDatabase db;
        db = this.getWritableDatabase(); // Read Data
        db.beginTransaction();
        Cursor c;
        try {
            c = db.rawQuery("SELECT count(ID) AS CNT FROM Nomen WHERE SALE_PRICE>0", null);
            if (c.moveToFirst()) {
                if (c.getInt(0) > 0) {
                    resetContrSales();
                    if (c != null) {
                        c.close();
                    }
                } else {
                    c = db.rawQuery("SELECT NOMEN.ID, printf('%.2f',NOMEN.PRICE-NOMEN.PRICE*SALES.SALE)  AS SALEPRICE FROM SALES JOIN Nomen ON SALES.GRUP_ID=Nomen.GRUPID WHERE SALES.CONTR_ID='" + Contr_ID + "'", null);
                    while (c.moveToNext()) {
                        db.execSQL("UPDATE Nomen SET SALE_PRICE=" + c.getDouble(1) + " WHERE ID='" + c.getString(0) + "'");
                    }
                    c.close();
                }
            }
        } catch (Exception ignored) {
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public int CheckForSales() {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT count(ID) AS CNT FROM Nomen WHERE SALE_PRICE>0", null);
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

    public int CheckTPAccess(String TP_ID) {
        Cursor c = null;
        try {
            SQLiteDatabase db;
            db = this.getReadableDatabase();
            c = db.rawQuery("SELECT count(TP_ID) AS CNT FROM TP_GRUP_ACCESS WHERE TP_ID='" + TP_ID + "'", null);
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
