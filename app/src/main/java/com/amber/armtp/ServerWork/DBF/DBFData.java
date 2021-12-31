package com.amber.armtp.ServerWork.DBF;

import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfField;

import java.io.File;

public class DBFData {
    String dbfDir;
    DbfField[] fields;

    public void setFields() {
        try (DbfReader reader = new DbfReader(new File(dbfDir))) {
            fields = new DbfField[reader.getHeader().getFieldsCount()];
            for (int i = 0; i < fields.length; i++) {
                fields[i] = reader.getHeader().getField(i);
            }
        }
    }
//
//    public DBFData(DbfField dbfField){
//        this.dbfDir = fileName;
//    }
}
