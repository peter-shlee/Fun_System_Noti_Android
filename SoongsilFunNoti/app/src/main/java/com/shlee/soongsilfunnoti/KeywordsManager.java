package com.shlee.soongsilfunnoti;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class KeywordsManager {
    private Context context;

    KeywordsManager(Context context){
        this.context = context;
    }

    synchronized public void addKeyword(String keyword){
        KeywordsDBHelper helper = new KeywordsDBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        // 중복체크
        Cursor cursor = db.rawQuery("SELECT keyword FROM tb_keyword WHERE keyword = \'" + keyword + "\'", null);
        if(cursor.moveToNext()) return; // 중복된 키워드 입력한 경우
        db.execSQL("INSERT INTO tb_keyword (keyword) VALUES (\'" + keyword + "\')");

        cursor.close();
        db.close();
    }

    synchronized public void deleteKeyword(String keyword){
        KeywordsDBHelper helper = new KeywordsDBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        db.execSQL("DELETE FROM tb_keyword WHERE keyword = \'" + keyword + "\'");

        db.close();
    }

    synchronized public ArrayList<String> getKeywordArray(){
        Log.i("KeywordsManager","------------------------------------------getKeywordArray");
        KeywordsDBHelper helper = new KeywordsDBHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT keyword FROM tb_keyword", null);
        ArrayList<String> keywordArray = new ArrayList<>();

        while(cursor.moveToNext()){
            keywordArray.add(cursor.getString(0));
            //Log.i("KeywordsManager","------------------------------------------keywordArray");
        }

        Collections.sort(keywordArray);

        cursor.close();
        db.close();

        return keywordArray;
    }
}
