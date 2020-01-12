package com.shlee.soongsilfunnoti;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingSharedPreferences {
    private SharedPreferences settingSharedPreferences;

    SettingSharedPreferences(Context context){
        settingSharedPreferences = context.getSharedPreferences("com.shlee.soongsilfunnoti.sharedpreference", Context.MODE_PRIVATE);
    }

    public SharedPreferences getSettingSharedPreferences() {
        return settingSharedPreferences;
    }
}
