package com.shlee.soongsilfunnoti;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class KeywordsManager {
    private Context context;

    KeywordsManager(Context context){
        this.context = context;
    }

    public void addKeyword(String keyword){
        KeywordsDBHelper helper = new KeywordsDBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        // 중복체크
        Cursor cursor = db.rawQuery("SELECT title FROM tb_keyword WHERE title = \'" + keyword + "\'", null);
        if(!cursor.moveToNext()) return; // 중복된 키워드 입력한 경우
        db.execSQL("INSERT INTO tb_keyword (title) VALUES (\'" + keyword + "\')");

        cursor.close();
        db.close();
    }

    public void deleteKeyword(String keyword){
        KeywordsDBHelper helper = new KeywordsDBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        db.execSQL("DELETE FROM tb_keyword WHERE title = \'" + keyword + "\'");

        db.close();
    }

    public ArrayList<String> getKeywordArray(){
        KeywordsDBHelper helper = new KeywordsDBHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT title FROM tb_keyword", null);
        ArrayList<String> keywordArray = new ArrayList<>();

        while(cursor.moveToNext()){
            keywordArray.add(cursor.getString(0));
        }

        cursor.close();
        db.close();

        return keywordArray;
    }
}
