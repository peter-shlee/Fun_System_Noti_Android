package com.shlee.soongsilfunnoti;

import java.util.Objects;

import androidx.annotation.Nullable;

public class Program implements Comparable {
    private String url;
    private String d_day;
    private String department;
    private String title;
    private String date;
    private int remainingDate;
    private boolean highlight;

    public Program( String title, String department,String date, String d_day, String programURL){
        setTitle(title);
        setD_day(d_day);
        setDepartment(department);
        setDate(date);
        setURL(programURL);
        setHighlight(false);
    }

    public Program(){
        setHighlight(false);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        //Log.i("Program", "--------------------------------------------------equals");
        return (title.equals(((Program)obj).getTitle()) && date.equals(((Program)obj).getDate()));
    }

    @Override
    public int compareTo(Object obj) {
        //프로그램 시작 날짜를 이용해 비교
        //return date.compareTo(((Program)obj).getDate());

        int result = 0;
        if(remainingDate < ((Program)obj).getRemainingDate()){
            result = -1;
        } else if(remainingDate == ((Program)obj).getRemainingDate()){
            ;
        } else {
            result = 1;
        }

        return result;
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
    boolean isHighlight() { return highlight;}
    int getRemainingDate() {return remainingDate;}

    void setD_day(String d_day){
        this.d_day = d_day;
        int remainingDate = 987654321;

        if(d_day.contains("D-")){
            remainingDate = Integer.parseInt(d_day.substring(2));
        } else if (d_day.equals("임박")){
            remainingDate = 0;
        } else {
            ;
        }
        setRemainingDate(remainingDate);
    }
    void setDate(String date){ this.date = date; }
    void setDepartment(String department){ this.department = department; }
    void setURL(String programURL){ this.url = programURL; }
    void setTitle(String title){ this.title = title; }
    void setHighlight(boolean highlight){this.highlight = highlight;}
    private void setRemainingDate(int remainingDate){ this.remainingDate = remainingDate; }

}
