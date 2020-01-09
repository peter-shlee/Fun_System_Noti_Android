package com.shlee.soongsilfunnoti;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    ProgramsManagementService programsManagementService = null;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("메인 액티비티", "-------------------------------------------------------------------메인 액티비티 onServiceConnected");
            programsManagementService = ((ProgramsManagementService.LocalBinder) service).ProgramsManagementService();

            //programsManagementService.updateFunSystemPrograms();
            // 여기에서 펀시스템 프로그램 목록 불러오자
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent serviceIntent = new Intent(this, ProgramsManagementService.class);
        startService(serviceIntent);
        bindService(serviceIntent,connection,0);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent serviceIntent = new Intent(this, ProgramsManagementService.class);
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);

        super.onDestroy();
    }
}
