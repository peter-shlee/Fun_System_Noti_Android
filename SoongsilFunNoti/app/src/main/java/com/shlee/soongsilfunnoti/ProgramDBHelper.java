package com.shlee.soongsilfunnoti;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashSet;
import java.util.Iterator;

public class ProgramDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    @Override
    public void onCreate(SQLiteDatabase db) {
        String programSQL = "create table tb_program (" +
                "_id integer primary key autoincrement, " +
                "title, " +
                "department, " +
                "date, " +
                "d_day," +
                "url)";
        db.execSQL(programSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == DATABASE_VERSION) {
            db.execSQL("drop table tb_program");
            onCreate(db);
        }
    }

    public ProgramDBHelper(Context context){
        super(context, "programdb", null, DATABASE_VERSION);
    }
}
