package com.shlee.soongsilfunnoti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SettingActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    AlertDialog addKeywordDialog;
    ArrayList<String> keywordArrayList;
    RecyclerView recyclerView;
    KeywordsManager keywordsManager;

    Switch alarmOnOffSwitch;
    boolean isAlarmON;
    SharedPreferences settingSharedPreferences;
    TextView alarmOn;
    TextView alarmOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        alarmOn = findViewById(R.id.text_notification_on);
        alarmOff = findViewById(R.id.text_notification_off);

        settingSharedPreferences = new SettingSharedPreferences(this).getSettingSharedPreferences();

        alarmOnOffSwitch = findViewById(R.id.switch_alarm_on_off);
        alarmOnOffSwitch.setOnCheckedChangeListener(this);
        isAlarmON = settingSharedPreferences.getBoolean("isAlarmON", false);
        alarmOnOffSwitch.setChecked(isAlarmON);

        getSupportActionBar().setTitle("알림 설정");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        keywordsManager = new KeywordsManager(this);
        setRecyclerView();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView == alarmOnOffSwitch){
            isAlarmON = isChecked; // 알림 ON인지 OFF인지 저장

            if(isChecked){
                alarmOn.setVisibility(View.VISIBLE);
                alarmOff.setVisibility(View.INVISIBLE);
            } else{
                alarmOn.setVisibility(View.INVISIBLE);
                alarmOff.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
        } else if(id == R.id.action_save){
            SharedPreferences.Editor editor = settingSharedPreferences.edit();
            editor.putBoolean("isAlarmON", isAlarmON);
            editor.apply();

            Log.i("SettingActivity", "----------------------------------------------------------------R.id.action_save : " + isAlarmON);
            Toast.makeText(this, "알림 설정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.setting, menu);
        return true;
    }

    public void onClickPlusButton(View view){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("키워드 추가");

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View addKeywordDialogView = inflater.inflate(R.layout.add_keyword_dialog_layout, null);
        final EditText newKeywordEditText = addKeywordDialogView.findViewById(R.id.edit_text_add_keyword_dialog_layout);
        newKeywordEditText.setTextColor(ContextCompat.getColor(this,R.color.colorText));

        dialogBuilder.setView(addKeywordDialogView);
        dialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog == addKeywordDialog && which == Dialog.BUTTON_POSITIVE){

                    String newKeyword = newKeywordEditText.getText().toString();
                    if(newKeyword.equals("")) {
                        Toast.makeText(SettingActivity.this, "저장되지 않았습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    keywordsManager.addKeyword(newKeyword);
                    resetKeywordRecyclerView();

//                    keywordArrayList.clear();
//                    keywordArrayList.addAll(keywordsManager.getKeywordArray());
//                    recyclerView.getAdapter().notifyDataSetChanged();
//                    for(Object o : keywordArrayList){
//                        Log.i("SettingActivity", "----------------------------------------------------------------" + ((String)o));
//                    }
                }
            }
        });
        dialogBuilder.setNegativeButton("취소", null);

        addKeywordDialog = dialogBuilder.create();
        addKeywordDialog.show();
    }

    private void setRecyclerView(){
        // 리사이클러뷰에 표시할 데이터 리스트 생성.
        keywordArrayList = keywordsManager.getKeywordArray();
        if(keywordArrayList == null) keywordArrayList = new ArrayList<>();
        // 리사이클러뷰에 LinearLayoutManager 객체 지정
        recyclerView = findViewById(R.id.recycler_keywords);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 리사이클러뷰에 KeywordsAdapter 객체 지정.
        KeywordsAdapter adapter = new KeywordsAdapter(keywordArrayList);
        adapter.setOnItemClickListener(
                new KeywordsAdapter.OnItemClickListner() {
                    @Override
                    public void onItemClick(View view, int position) {
                        final String keyword = keywordArrayList.get(position);

                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                        builder.setTitle("키워드 삭제");
                        builder.setMessage("\n정말 삭제하시겠습니까?");
                        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                keywordsManager.deleteKeyword(keyword);
                                resetKeywordRecyclerView();
                            }
                        });
                        builder.setNegativeButton("아니요", null);

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    }
                }
        );
        recyclerView.setAdapter(adapter);
    }

    private void resetKeywordRecyclerView(){
        keywordArrayList.clear();
        keywordArrayList.addAll(keywordsManager.getKeywordArray());
        recyclerView.getAdapter().notifyDataSetChanged();
    }
}
