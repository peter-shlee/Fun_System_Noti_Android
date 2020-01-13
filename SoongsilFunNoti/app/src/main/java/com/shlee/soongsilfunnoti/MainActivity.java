package com.shlee.soongsilfunnoti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int RESPONSE_MSG_TO_MAIN_ACTIVITY = 3000;

    ProgramsManagementService programsManagementService = null;

    RecyclerView recyclerView;
    ArrayList<Program> programArrayList;

    private Intent programManagementServiceIntent;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("메인 액티비티", "-------------------------------------------------------------------메인 액티비티 onServiceConnected");
            programsManagementService = ((ProgramsManagementService.LocalBinder) service).ProgramsManagementService();

            setRecyclerView();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        programManagementServiceIntent = new Intent(this, ProgramsManagementService.class);
        programManagementServiceIntent.putExtra("RECEIVER", resultReceiver);
        programManagementServiceIntent.putExtra("isCalledFromMainActivity", true);
        programManagementServiceIntent.putExtra("isCalledFromDeviceBootReceiveService", false);
        startService(programManagementServiceIntent);
        bindService(programManagementServiceIntent,connection,0);

        getSupportActionBar().setTitle("숭실대 펀시스템");

        // 초기 설정 어플 처음 실행 시에만 실행
        SharedPreferences settingPref = new SettingSharedPreferences(this).getSettingSharedPreferences();
        if(settingPref.getBoolean("isFirstExecute", true)){
            SharedPreferences.Editor editor = settingPref.edit();
            editor.putBoolean("isFirstExecute", false);
            editor.putBoolean("isAlarmON", true);
            //editor.
            editor.commit();
        }

        try{
            programsManagementService.updateFunSystemPrograms(true);
        } catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_setting){
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        if(programManagementServiceIntent != null){
            stopService(programManagementServiceIntent);
            programManagementServiceIntent = null;
        }

        super.onDestroy();
    }

    private void setRecyclerView(){
        // 리사이클러뷰에 표시할 데이터 리스트 생성.
        Log.i("메인 액티비티", "-------------------------------------------------------------------프로그램 배열 가져옴");
        programArrayList = programsManagementService.getProgramArrayList();
        if(programArrayList == null) programArrayList = new ArrayList<>();
        // 리사이클러뷰에 LinearLayoutManager 객체 지정
        recyclerView = findViewById(R.id.recycler_programs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 리사이클러뷰에 ProgramsAdapter 객체 지정.
        ProgramsAdapter adapter = new ProgramsAdapter(programArrayList);
        adapter.setOnItemClickListener(
                new ProgramsAdapter.OnItemClickListner() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Program program = programArrayList.get(position);

                        String url = program.getURL();
                        if(url != null){
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            //intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

                            startActivity(intent);
                        }
                    }
                }
        );
        recyclerView.setAdapter(adapter);
    }

    private void updateRecyclerView(){
        Log.i("메인 액티비티", "-------------------------------------------------------------------updateRecyclerView 프로그램 배열 가져옴");
        programArrayList.clear();
        programArrayList.addAll(programsManagementService.getProgramArrayList());
//        for(Object o : programArrayList){
//            Log.i("메인 액티비티", "----------------------------------------------------------------" + ((Program)o).getTitle());
//        }
        //recyclerView.invalidate();
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private Handler handler = new Handler();
    private ResultReceiver resultReceiver = new ResultReceiver(handler){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            if(resultCode == RESPONSE_MSG_TO_MAIN_ACTIVITY){
                Log.i("메인 액티비티", "-------------------------------------------------------------------" + resultData.getString("msg"));
                updateRecyclerView();
            }
        }
    };
}
