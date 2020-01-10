package com.shlee.soongsilfunnoti;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private static final int RESPONSE_MSG_TO_MAIN_ACTIVITY = 3000;

    ProgramsManagementService programsManagementService = null;

    RecyclerView recyclerView;
    ProgramsAdapter adapter;
    ArrayList<Program> programArrayList;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("메인 액티비티", "-------------------------------------------------------------------메인 액티비티 onServiceConnected");
            programsManagementService = ((ProgramsManagementService.LocalBinder) service).ProgramsManagementService();


            setRecyclerView();

            //////////////////////////////////////////////////////////////////////////////////////////////새로 안가져와짐
            // 여기에서 펀시스템 프로그램 목록 불러오자

            /*// 리사이클러뷰에 표시할 데이터 리스트 생성.
            Log.i("메인 액티비티", "-------------------------------------------------------------------프로그램 배열 가져옴");
            final ArrayList<Program> programArrayList = programsManagementService.getProgramArrayList();
            // 리사이클러뷰에 LinearLayoutManager 객체 지정
            RecyclerView recyclerView = findViewById(R.id.recycler_programs);
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

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
            recyclerView.setAdapter(adapter);*/
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
        serviceIntent.putExtra("RECEIVER", resultReceiver);
        startService(serviceIntent);
        bindService(serviceIntent,connection,0);


    }

    @Override
    protected void onDestroy() {
        unbindService(connection);

        super.onDestroy();
    }

    private void setRecyclerView(){
        // 리사이클러뷰에 표시할 데이터 리스트 생성.
        Log.i("메인 액티비티", "-------------------------------------------------------------------프로그램 배열 가져옴");
        programArrayList = programsManagementService.getProgramArrayList();
        if(programArrayList == null) programArrayList = new ArrayList<>();
        // 리사이클러뷰에 LinearLayoutManager 객체 지정
        recyclerView = findViewById(R.id.recycler_programs);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        // 리사이클러뷰에 ProgramsAdapter 객체 지정.
        adapter = new ProgramsAdapter(programArrayList);
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

    private void updateRecyclerView(){
        Log.i("메인 액티비티", "-------------------------------------------------------------------프로그램 배열 가져옴");
        programArrayList.clear();
        programArrayList.addAll(programsManagementService.getProgramArrayList());
        for(Object o : programArrayList){
            Log.i("메인 액티비티", "----------------------------------------------------------------" + ((Program)o).getTitle());
        }
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

    public void onSettingButtonClick(View view){
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }
}
