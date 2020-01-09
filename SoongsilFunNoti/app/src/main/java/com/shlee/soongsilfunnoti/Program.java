package com.shlee.soongsilfunnoti;

import android.util.Log;
import java.util.Objects;

import androidx.annotation.Nullable;

public class Program implements Comparable {
    private String url;
    private String d_day;
    private String department;
    private String title;
    private String date;

    public Program( String title, String department,String date, String d_day, String programURL){
        this.title = title;
        this.department = department;
        this.date = date;
        this.d_day = d_day;
        this.url = programURL;
    }

    public Program(){

    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Log.i("Program", "--------------------------------------------------equals");
        return (title.equals(((Program)obj).getTitle()) && date.equals(((Program)obj).getDate()));
    }

    @Override
    public int compareTo(Object obj) {
        //프로그램 시작 날짜를 이용해 비교
        return date.compareTo(((Program)obj).getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, date);
    }

    String getD_day() { return d_day; }
    String getDate() { return date; }
    String getDepartment() { return department; }
    String getURL() { return url; }
    String getTitle() { return title; }

    void setD_day(String d_day){ this.d_day = d_day; }
    void setDate(String date){ this.date = date; }
    void setDepartment(String department){ this.department = department; }
    void setURL(String programURL){ this.url = programURL; }
    void setTitle(String title){ this.title = title; }

}
