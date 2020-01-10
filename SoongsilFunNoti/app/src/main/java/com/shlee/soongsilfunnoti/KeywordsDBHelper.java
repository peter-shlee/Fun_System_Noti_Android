package com.shlee.soongsilfunnoti;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class KeywordsDBHelper  extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;

    @Override
    public void onCreate(SQLiteDatabase db) {
        String keywordSQL = "create table tb_keyword (" +
                "_id integer primary key autoincrement, " +
                "keyword)";
        db.execSQL(keywordSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == DATABASE_VERSION) {
            db.execSQL("drop table tb_keyword");
            onCreate(db);
        }
    }

    public KeywordsDBHelper(Context context){
        super(context, "keyworddb", null, DATABASE_VERSION);
    }
}
