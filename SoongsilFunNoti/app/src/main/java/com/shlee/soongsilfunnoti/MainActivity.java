package com.shlee.soongsilfunnoti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.SearchManager;
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

public class MainActivity extends AppCompatActivity {

    private static final int RESPONSE_MSG_TO_MAIN_ACTIVITY = 3000;

    static ProgramsManagementService programsManagementService = null;

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    ArrayList<Program> programArrayList;

    private Intent programManagementServiceIntent;

    boolean isCalledFromSwipeRefreshLayout;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("메인 액티비티", "-------------------------------------------------------------------메인 액티비티 onServiceConnected");
            programsManagementService = ((ProgramsManagementService.LocalBinder) service).ProgramsManagementService();
            programArrayList = programsManagementService.getProgramArrayList();
            //programArrayList = new ArrayList<>();
            setRecyclerView();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    requestRefreshProgramList();
//                }
//            }, 2000);

            if(isCalledFromSwipeRefreshLayout) swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            programsManagementService = null;
        }
    };

    private void setProgramManagementServiceIntent(){
        programManagementServiceIntent = new Intent(this, ProgramsManagementService.class);
        programManagementServiceIntent.putExtra("RECEIVER", resultReceiver);
        programManagementServiceIntent.putExtra("isCalledFromMainActivity", true);
        programManagementServiceIntent.putExtra("isCalledFromDeviceBootReceiveService", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setProgramManagementServiceIntent();
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

        isCalledFromSwipeRefreshLayout = false;
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_main_activity);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!isCalledFromSwipeRefreshLayout){
                    isCalledFromSwipeRefreshLayout = true;
                    requestRefreshProgramList();
                } else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            requestRefreshProgramList();
                        }
                    }, 2000);
                }
            }
        });

        try{
            programsManagementService.updateFunSystemPrograms(true, resultReceiver);
        } catch (NullPointerException e){
            e.printStackTrace();
        }

    }

//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        programsManagementService = null;
//    }

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
        //programArrayList = programsManagementService.getProgramArrayList();
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

                            startActivity(intent);
                        }
                    }
                }
        );
        recyclerView.setAdapter(adapter);
    }

    private void requestRefreshProgramList(){
        if(programsManagementService == null){
            startService(programManagementServiceIntent);
            bindService(programManagementServiceIntent,connection,0);
        } else {
            programsManagementService.updateFunSystemPrograms(true, resultReceiver);
        }
    }

    private void updateRecyclerView(){
        Log.i("메인 액티비티", "-------------------------------------------------------------------updateRecyclerView 프로그램 배열 가져옴");
        ArrayList<Program> newPrograms = programsManagementService.getProgramArrayList();
        if(newPrograms == null) return;
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if(adapter == null) return;

        programArrayList.clear();
        programArrayList.addAll(programsManagementService.getProgramArrayList());
//        for(Object o : programArrayList){
//            Log.i("메인 액티비티", "----------------------------------------------------------------" + ((Program)o).getTitle());
//        }
        adapter.notifyDataSetChanged();
    }

    private Handler handler = new Handler();
    private ResultReceiver resultReceiver = new ResultReceiver(handler){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i("메인 액티비티", "-------------------------------------------------------------------onReceiveResult");
            super.onReceiveResult(resultCode, resultData);

            if(resultCode == RESPONSE_MSG_TO_MAIN_ACTIVITY){
                Log.i("메인 액티비티", "-------------------------------------------------------------------" + resultData.getString("msg"));
                updateRecyclerView();

                if(isCalledFromSwipeRefreshLayout){
                    swipeRefreshLayout.setRefreshing(false);
                    isCalledFromSwipeRefreshLayout = false;
                }
            }
        }
    };
}
