package com.shlee.soongsilfunnoti;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

public class DeviceBootReceiver extends BroadcastReceiver {

    Intent programManagementService;
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("DeviceBootReceiver","------------------------------------------------------------------onReceive");

        SettingSharedPreferences sp = new SettingSharedPreferences(context);
        SharedPreferences sharedPreferences = sp.getSettingSharedPreferences();

        if(sharedPreferences.getBoolean("isAlarmON", true)){
            programManagementService = new Intent(context, ServiceStarterForegroundService.class);

            this.context = context;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                context.startForegroundService(programManagementService);
            } else{
                context.startService(programManagementService);
            }
        }
    }
}
