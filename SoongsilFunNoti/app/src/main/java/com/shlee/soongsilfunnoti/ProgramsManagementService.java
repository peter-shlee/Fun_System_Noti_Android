package com.shlee.soongsilfunnoti;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class ProgramsManagementService extends Service {

    private static final int MINIMUM_UPDATE_INTERVAL_TIME = 1700000;
    private static final int ADDITIONAL_UPDATE_INTERVAL_TIME = 300000;
    private static final int REQUEST_MSG_PROGRAM_LIST = 1000;
    private static final int RESPONSE_MSG_PROGRAM_LIST = 2000;
    private static final int RESPONSE_MSG_TO_MAIN_ACTIVITY = 3000;
    private static final int RESPONSE_MSG_TO_DEVICE_BOOT_RECEIVER = 4000;

    ResultReceiver resultReceiver;

    private Messenger parsingFunSystemServiceMessenger = null;
    private Messenger messengerForResponseFromParsingFunSystemService = null;

    private HashSet<Program> programHashSet;
    private HashSet<Program> addedProgramHashSet;

    private final Binder binder = new LocalBinder();

    static private Intent startingIntent;

    private int serviceCount = 0;

    public class LocalBinder extends Binder {
        ProgramsManagementService ProgramsManagementService() {return ProgramsManagementService.this;}
        //MainActivity.this.setProgramsManagementService(ProgramsManagementService.this);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("ProgramsManagementService", "------------------------------------------------onServiceConnected");
            parsingFunSystemServiceMessenger = new Messenger(service);
            boolean isAnImmediateUpdateRequest = startingIntent.getBooleanExtra("isCalledFromMainActivity", false);
            updateFunSystemPrograms(isAnImmediateUpdateRequest, resultReceiver);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            parsingFunSystemServiceMessenger = null;
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ProgramsManagementService", "------------------------------------------------ProgramDBHelper onStartCommand");
        startingIntent = intent;
        resultReceiver = startingIntent.getParcelableExtra("RECEIVER");

        if(startingIntent.getBooleanExtra("isCalledFromDeviceBootReceiveService", false)){
            sendStopMessageToDeviceBootReceiveService();

//            try{
//                ResultReceiver resultReceiver =  startingIntent.getParcelableExtra("RECEIVER");
//                Bundle bundle = new Bundle();
//                bundle.putString("msg", "starting ProgramManagementService complete!");
//                resultReceiver.send(RESPONSE_MSG_TO_DEVICE_BOOT_RECEIVER, bundle);
//            } catch (NullPointerException e){
//                e.printStackTrace();
//            }
        }



        return START_STICKY;
    }

    private void sendStopMessageToDeviceBootReceiveService(){
        try{
            Bundle bundle = new Bundle();
            bundle.putString("msg", "starting ProgramManagementService complete!");
            resultReceiver.send(RESPONSE_MSG_TO_DEVICE_BOOT_RECEIVER, bundle);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        programHashSet = new HashSet<>();
        addedProgramHashSet = new HashSet<>();

        Intent serviceIntent = new Intent(this, ParsingFunSystemService.class);
        bindService(serviceIntent, connection, BIND_AUTO_CREATE);
        Log.i("ProgramsManagementService", "------------------------------------------------bindService");

        HandlerForResponseFromParsingFunSystemService msgResponseHandler = new HandlerForResponseFromParsingFunSystemService(this);
        messengerForResponseFromParsingFunSystemService = new Messenger(msgResponseHandler);

        getProgramsFromDB();
    }

    protected void setAlarmTimer(){
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        Intent intent = new Intent(this, DeviceBootReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }

    @Override
    public void onDestroy() {
        try{
            this.unbindService(connection);
        } catch (Exception e){
            e.printStackTrace();
        }

        SettingSharedPreferences sp = new SettingSharedPreferences(this);
        SharedPreferences sharedPreferences = sp.getSettingSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();

        startingIntent = null;
        setAlarmTimer();
        Thread.currentThread().interrupt();

        super.onDestroy();
    }

    private static class HandlerForResponseFromParsingFunSystemService extends Handler{
        private final WeakReference<ProgramsManagementService> ref;

        private HandlerForResponseFromParsingFunSystemService(ProgramsManagementService service){
            ref = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.i("ProgramsManagementService", "------------------------------------------------MsgResponseHandler handleMessage");
            switch (msg.what){
                case RESPONSE_MSG_PROGRAM_LIST: // 여기로 펀시스템의 프로그램 목록 요청(updateFunSystemPrograms())에 대한 응답이 온다
                {
                    Log.i("ProgramsManagementService", "------------------------------------------------MsgResponseHandler handleMessage RESPONSE_MSG_PROGRAM_LIST");
                    final HashSet<Program> programs = (HashSet<Program>) msg.obj;

                    ref.get().getProgramsFromDB();
                    ref.get().updateProgramHashSet(programs);
                    ref.get().saveProgramsAtDB();

                    List list = new ArrayList<>(ref.get().getProgramHashSet());
                    Collections.sort(list);
                    for(Object o : list){
                        Log.i("ProgramsManagementService", "----------------------------------------------------------------" + ((Program)o).getD_day() + ((Program)o).getTitle());
                    }

                    Log.i("ProgramsManagementService", "----------------------------------------------------------------새로 등록된 프로그램들");
                    List list2 = new ArrayList<>(ref.get().getAddedProgramHashSet());
                    Collections.sort(list2);
                    for(Object o : list2){
                        Log.i("ProgramsManagementService", "----------------------------------------------------------------" + ((Program)o).getTitle());
                    }

                    //새로운 프로그램들 중 키워드 포함된 프로그램 있는지 검사
                    if(!ref.get().addedProgramHashSet.isEmpty())ref.get().checkNewAddedProgramsIncludeKeywords();

                    // 메인 액티비티 화면 갱신 요구
                    ref.get().refreshRecyclerViewAtMainActivity();

                    break;
                }
            }
        }
    }

    public void refreshRecyclerViewAtMainActivity(){
        Log.i("ProgramsManagementService","------------------------------------------------------------------refreshRecyclerViewAtMainActivity");
        if(startingIntent != null){
            Bundle  bundle = new Bundle();

            try{ // 메인 액티비티에서 아래 요청이 수행됨
                Log.i("ProgramsManagementService","------------------------------------------------------------------refreshRecyclerViewAtMainActivity***************");
                bundle.putString("msg", "Parsing Complete!");
                resultReceiver.send(RESPONSE_MSG_TO_MAIN_ACTIVITY, bundle);
            } catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }

    public void updateFunSystemPrograms(boolean isAnImmediateUpdateRequest, final ResultReceiver resultReceiverFromMainActivity){
        resultReceiver = resultReceiverFromMainActivity;
        Log.i("ProgramsManagementService", "---------------------------------------------updateFunSystemPrograms() " + (serviceCount));
        SharedPreferences sharedPreferences = new SettingSharedPreferences(this).getSettingSharedPreferences();
        long lastUpdateTime = sharedPreferences.getLong("lastUpdateTime", 0);
        long currentTime = System.currentTimeMillis();

        if(currentTime - lastUpdateTime >= MINIMUM_UPDATE_INTERVAL_TIME || isAnImmediateUpdateRequest){
            Log.i("ProgramsManagementService", "---------------------------------------------updateFunSystemPrograms()****************************** " + (serviceCount++));
            Message msg = Message.obtain(null, REQUEST_MSG_PROGRAM_LIST);
            msg.replyTo = messengerForResponseFromParsingFunSystemService;
            try{
                parsingFunSystemServiceMessenger.send(msg);
            } catch (Exception e){
                e.printStackTrace();
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("lastUpdateTime",currentTime);
            editor.apply();

            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {//OREO API 26 미만에서
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //////////////////////////////////////////////////////////////////////////////////////////!!!!!!!!!!!!!!!!!!!!!!
                        updateFunSystemPrograms(false, resultReceiverFromMainActivity);
                    }
                }, MINIMUM_UPDATE_INTERVAL_TIME + ADDITIONAL_UPDATE_INTERVAL_TIME);
            }

        }

    }

    private void getProgramsFromDB(){

        ProgramDBHelper helper = new ProgramDBHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT title, department, date, d_day, url FROM tb_program", null);

        if(programHashSet == null) programHashSet = new HashSet<>();
        if(!programHashSet.isEmpty()) programHashSet.clear();

        while (cursor.moveToNext()){
            Program program = new Program(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            programHashSet.add(program);
        }

        cursor.close();
        db.close();

        // print programs from DB
        if(programHashSet != null){
            Iterator<Program> iterator = programHashSet.iterator();
            while(iterator.hasNext()){
                Program program = iterator.next();
                Log.i("ProgramsManagementService", program.getD_day() + program.getURL() + program.getDate() + program.getDepartment() + program.getTitle());
            }
        }

    }

    private void saveProgramsAtDB(){
        if (programHashSet == null) return;

        ProgramDBHelper helper = new ProgramDBHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM tb_program");

        Iterator<Program> iterator = programHashSet.iterator();
        while(iterator.hasNext()){
            Program program = iterator.next();
            db.execSQL("INSERT INTO tb_program (title, department, date, d_day, url) VALUES ( \'" + program.getTitle() + "\', \'" + program.getDepartment() + "\', \'"+ program.getDate() + "\', \'"+ program.getD_day() + "\', \'"+ program.getURL() + "\')");
        }

        db.close();
    }

    private void setProgramHashSet(HashSet<Program> programHashSet){
        if (programHashSet == null) programHashSet = new HashSet<>();
        this.programHashSet = programHashSet;
        Log.i("ProgramsManagementService", "------------------------------------------------프로그램 목록 업데이트됨");
    }

    private void updateProgramHashSet(HashSet<Program> newProgramHashSet){
        if(newProgramHashSet == null) {
            addedProgramHashSet = new HashSet<>();
            return;
        }
        if(newProgramHashSet.isEmpty()) {
            addedProgramHashSet = new HashSet<>();
            return;
        }

        HashSet<Program> tmpAddedProgramHashSet = new HashSet<>();

        Iterator<Program> newProgramHashSetIterator = newProgramHashSet.iterator();

        while(newProgramHashSetIterator.hasNext() && programHashSet != null){
            Program program = newProgramHashSetIterator.next();
            if(!programHashSet.contains(program)){
                Log.i("ProgramsManagementService", "------------------------------------------------------------!programHashSet.contains(program)");
                tmpAddedProgramHashSet.add(program);
            }
        }

        if(tmpAddedProgramHashSet.isEmpty()) {
            addedProgramHashSet = new HashSet<>();
            return;
        }

        if(addedProgramHashSet == null) addedProgramHashSet = new HashSet<>();
        if(!addedProgramHashSet.isEmpty()) addedProgramHashSet.clear();
        addedProgramHashSet.addAll(tmpAddedProgramHashSet);


        setProgramHashSet(newProgramHashSet);
    }

    private HashSet<Program> getAddedProgramHashSet() {
        return addedProgramHashSet;
    }

    public HashSet<Program> getProgramHashSet(){
        return programHashSet;
    }

    public ArrayList<Program> getProgramArrayList(){
        if(programHashSet == null) return new ArrayList<>();

        List list = new ArrayList<>(getProgramHashSet());
        Collections.sort(list);

        return new ArrayList<Program>(list);
    }

    private void checkNewAddedProgramsIncludeKeywords(){
        if(addedProgramHashSet == null) return;
        if(addedProgramHashSet.isEmpty()) return;

        ArrayList<String> keywords = new KeywordsManager(this).getKeywordArray();
        HashSet<String> containedKeywords = new HashSet<>();

        for(Program program : addedProgramHashSet){
            String title = program.getTitle().toLowerCase();
            String department = program.getDepartment().toLowerCase();
            for(String keyword : keywords){
                keyword = keyword.toLowerCase();
                if(title.contains(keyword) || department.contains(keyword) ){
                    Log.i("ProgramsManagementService", "------------------------------------------------------------" + title + department);
                    program.setHighlight(true);
                    containedKeywords.add(keyword);
                }
            }
        }
        Log.i("ProgramsManagementService", "------------------------------------------------------------call newProgramAddedNotification");
        newProgramAddedNotification(containedKeywords);

        keywords = null;
        containedKeywords = null;
        addedProgramHashSet.clear();
    }

    private NotificationManager registerNotiChannel(String channelName, String channelDescription, int importance){
        Log.i("ProgramsManagementService", "------------------------------------------------------------registerNotiChannel");

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {//OREO API 26 이상에서

            NotificationChannel channel = new NotificationChannel("new program notification channel", channelName, importance);
            channel.setDescription(channelDescription);

            if (notificationManager != null) {
                // 노티피케이션 채널을 시스템에 등록
                notificationManager.createNotificationChannel(channel);
            }
        }

        return notificationManager;
    }


    private void newProgramAddedNotification(HashSet<String> containedKeywords){
        if(!(new SettingSharedPreferences(this).getSettingSharedPreferences().getBoolean("isAlarmON", true))) return;
        if(addedProgramHashSet.size() == 0) return;
        if(containedKeywords.size() == 0) return;

        Log.i("ProgramsManagementService", "------------------------------------------------------------newProgramAddedNotification");

        NotificationManager notificationManager = registerNotiChannel("새 프로그램 알림", "특정 키워드가 포함된 새로운 프로그램이 숭실대 펀시스템에 등록되었을 때 알림 합니다.", NotificationManager.IMPORTANCE_HIGH);

        Intent activityIntent = new Intent(this, MainActivity.class); // 알림창 클릭시 호출할 액티비티
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "new program notification channel");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {//OREO API 26 이상에서
            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
        } else {
            builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남
        }

        ArrayList<String> containedKeywordsList = new ArrayList<>(containedKeywords);
        StringBuilder keywordsString = new StringBuilder("\'" + containedKeywordsList.get(0) + "\'");
        for(String keyword : containedKeywordsList.subList(1,containedKeywordsList.size())){
            keywordsString.append(", \'").append(keyword).append("\'");
        }
        builder.setContentTitle("펀시스템에 새로운 프로그램이 등록되었습니다.")
                .setContentText(keywordsString + " 키워드에 대한 새로운 프로그램이 등록되었습니다.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        Log.i("ProgramsManagementService", "------------------------------------------------------------newProgramAddedNotification show notification 111");
        if (notificationManager != null) {
            // 노티피케이션 동작시킴
            notificationManager.notify(20160548, builder.build());
            Log.i("ProgramsManagementService", "------------------------------------------------------------newProgramAddedNotification show notification");
        }
        Log.i("ProgramsManagementService", "------------------------------------------------------------newProgramAddedNotification show notification 222");
    }
}
