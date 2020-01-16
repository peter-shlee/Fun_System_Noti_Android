package com.shlee.soongsilfunnoti;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ServiceStarterForegroundService extends Service {

    private static final int RESPONSE_MSG_TO_DEVICE_BOOT_RECEIVER = 4000;

    private String channelID = "start service on device boot channel";

    private NotificationManager registerNotiChannel(String channelName, String channelDescription, int importance){
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {//OREO API 26 이상에서

            NotificationChannel channel = new NotificationChannel(channelID, channelName, importance);
            channel.setDescription(channelDescription);

            if (notificationManager != null) {
                // 노티피케이션 채널을 시스템에 등록
                notificationManager.createNotificationChannel(channel);
            }
        }

        return notificationManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT >= 26){
            registerNotiChannel("서비스 시작", "서비스를 시작할 때 표시되는 알림입니다", NotificationManager.IMPORTANCE_LOW);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,channelID);

            startForeground(20160548,builder.build());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ServiceStarterForegroundService", "--------------------------------------onStartCommand();");
        //Intent serviceIntent = new Intent(this, ProgramsManagementService.class);
        Intent serviceIntent = new Intent(this, ProgramsManagementService.class);
        serviceIntent.putExtra("isCalledFromMainActivity", false);
        serviceIntent.putExtra("isCalledFromDeviceBootReceiveService", true);
        serviceIntent.putExtra("RECEIVER", resultReceiver);
        startService(serviceIntent);

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private Handler handler = new Handler();
    private ResultReceiver resultReceiver = new ResultReceiver(handler){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            if(resultCode == RESPONSE_MSG_TO_DEVICE_BOOT_RECEIVER){
                Log.i("ServiceStarterForegroundService", "--------------------------------------stopSelf();");
                stopSelf();
            }
        }
    };
}
