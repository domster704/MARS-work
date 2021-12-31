package com.amber.armtp.ServerWork.DBF;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.util.Log;

import com.amber.armtp.MainActivity;
import com.amber.armtp.UpdateDataFragment;

import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DBFConvertorToSQL {
    private Map<String, Integer> months = new HashMap<>();
    private SQLiteStatement statement;
    private String[] columnNameList;

    {
        months.put("Jan", 1);
        months.put("Feb", 2);
        months.put("Mar", 3);
        months.put("Apr", 4);
        months.put("May", 5);
        months.put("Jun", 6);
        months.put("Jul", 7);
        months.put("Aug", 8);
        months.put("Sep", 9);
        months.put("Oct", 10);
        months.put("Nov", 11);
        months.put("Dec", 12);
    }

    public DBFConvertorToSQL(SQLiteStatement statement) {
        this.statement = statement;
    }

    /**
     * Получает список названий DBF файлов
     */
    public void getFilesList() {
        File f = new File(MainActivity.filesPath);
        String[] filesList = f.list();
    }

    // TODO: progressBar
    public void read(final String fileName, final UpdateDataFragment.UIData ui) throws InterruptedException {
        Log.d("ftp3", "---------- " + fileName + " ----------");
        try (DbfReader reader = new DbfReader(new File(MainActivity.filesPath + fileName), Charset.forName("Windows-1251"))) {
            int progressStatus = 0;
            final int finalCount = reader.getRecordCount();

            columnNameList = new String[reader.getHeader().getFieldsCount()];
            for (int i = 0; i < columnNameList.length; i++) {
                columnNameList[i] = reader.getHeader().getField(i).getName();
            }

            DbfRow row;
            while ((row = reader.nextRow()) != null) {
                ArrayList<ColData> colData = getColDataFromDBF(row);

                statement.clearBindings();
                for (int i = 0; i < colData.size(); i++) {
                    int index = i + 1;
                    switch (colData.get(i).type) {
                        case "string":
                            statement.bindString(index, (String) colData.get(i).value);
                            break;
                        case "long":
                            statement.bindLong(index, (Long) colData.get(i).value);
                            break;
                        case "double":
                            statement.bindDouble(index, (Double) colData.get(i).value);
                    }
                }
                statement.executeInsert();
                statement.clearBindings();

                progressStatus += 1;
                final int perc = progressStatus * 100 / reader.getRecordCount();
                ui.progressBar.setProgress(perc);

                final int finalProgressStatus = progressStatus;
                ui.handler.post(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    public void run() {
                        ui.tvCount.setText(finalProgressStatus + "/" + finalCount);
                        ui.tvPer.setText(perc + "%");
                    }
                });
            }

            reader.close();
            ui.handler.post(new Runnable() {
                public void run() {
                    ui.checkBox.setChecked(true);
                    ui.checkBox.setTextColor(Color.rgb(3, 103, 0));
                }
            });
        }
    }

    private static class ColData<V> {
        public V value;
        public String type;

        public ColData(V value, String type) {
            this.value = value;
            this.type = type;
        }
    }

    private String getDateFromDateType(Date date) {
        String[] data = date.toString().split(" ");
        return data[2] + "." + months.get(data[1]) + "." + data[data.length - 1];
    }

    private ArrayList<ColData> getColDataFromDBF(DbfRow row) {
        ArrayList<ColData> colData = new ArrayList<>();
        for (String i : columnNameList) {
            try {
                // if int
                colData.add(new ColData<>(row.getLong(i), "long"));
            } catch (Exception e0) {
                try {
                    // if string
                    colData.add(new ColData<>(row.getString(i), "string"));
                } catch (Exception e) {
                    try {
                        // if date
                        String date = getDateFromDateType(row.getDate(i));
                        colData.add(new ColData<>(date, "string"));
                    } catch (Exception e1) {
                        // if double
                        colData.add(new ColData<>(row.getDouble(i), "double"));
                    }
                }
            }
        }
        return colData;
    }
}
