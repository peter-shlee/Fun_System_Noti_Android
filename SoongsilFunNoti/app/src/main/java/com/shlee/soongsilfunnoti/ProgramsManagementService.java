package com.shlee.soongsilfunnoti;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class ProgramsManagementService extends Service {

    private static final int REQUEST_MSG_PROGRAM_LIST = 1000;
    private static final int RESPONSE_MSG_PROGRAM_LIST = 2000;
    private static final int RESPONSE_MSG_TO_MAIN_ACTIVITY = 3000;

    private Messenger messengerService;
    private MsgResponseHandler messengerResponseHandler = null;
    private Messenger messengerResponse;

    private HashSet<Program> programHashSet;
    private HashSet<Program> addedProgramHashSet;

    private final Binder binder = new LocalBinder();

    private Intent startingIntent;

    public class LocalBinder extends Binder {
        ProgramsManagementService ProgramsManagementService() {return ProgramsManagementService.this;}
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("ProgramsManagementService", "------------------------------------------------onServiceConnected");
            messengerService = new Messenger(service);
            updateFunSystemPrograms();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            messengerService = null;
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ProgramsManagementService", "------------------------------------------------ProgramDBHelper onStartCommand");
        startingIntent = intent;
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Intent serviceIntent = new Intent(this, ParsingFunSystemService.class);
        this.bindService(serviceIntent, connection, BIND_AUTO_CREATE);
        Log.i("ProgramsManagementService", "------------------------------------------------bindService");

        messengerResponseHandler = new MsgResponseHandler(this);
        messengerResponse = new Messenger(messengerResponseHandler);

        getProgramsFromDB();
    }

    @Override
    public void onDestroy() {
        this.unbindService(connection);

        super.onDestroy();
    }

    private static class MsgResponseHandler extends Handler{
        private final WeakReference<ProgramsManagementService> ref;

        public MsgResponseHandler(ProgramsManagementService service){
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

                    ref.get().updateProgramHashSet(programs);

                    List list = new ArrayList<>(ref.get().getProgramHashSet());
                    Collections.sort(list);
                    for(Object o : list){
                        Log.i("ProgramsManagementService", "----------------------------------------------------------------" + ((Program)o).getD_day() + ((Program)o).getTitle());
                    }

                    Log.i("ProgramsManagementService", "----------------------------------------------------------------새로 등록된 프로그램들");
                    List list2 = new ArrayList<>(ref.get().getAddedProgramHashSet());
                    Collections.sort(list);
                    for(Object o : list2){
                        Log.i("ProgramsManagementService", "----------------------------------------------------------------" + ((Program)o).getTitle());
                    }

                    // 메인 액티비티 화면 갱신 요구
                    if(ref.get().startingIntent != null){
                        Bundle  bundle = new Bundle();
                        final ResultReceiver receiver = ref.get().startingIntent.getParcelableExtra("RECEIVER");
                        bundle.putString("msg", "Parsing Complete!");
                        receiver.send(RESPONSE_MSG_TO_MAIN_ACTIVITY, bundle);
                    }

                    break;
                }
            }
        }
    }

    private void updateFunSystemPrograms(){
        Log.i("ProgramsManagementService", "------------------------------------------------updateFunSystemPrograms");
        Message msg = Message.obtain(null, REQUEST_MSG_PROGRAM_LIST);
        msg.replyTo = messengerResponse;
        try{
            Log.i("ProgramsManagementService", "------------------------------------------------send");
            messengerService.send(msg);
        } catch (RemoteException e){
            e.printStackTrace();
        }
    }

    private void getProgramsFromDB(){
        if(programHashSet == null) programHashSet = new HashSet<>();
        if(!programHashSet.isEmpty()) programHashSet.clear();

        ProgramDBHelper helper = new ProgramDBHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT title, department, date, d_day, url FROM tb_program", null);

        while(cursor.moveToNext()){
            Program program = new Program(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            programHashSet.add(program);
        }

        cursor.close();
        db.close();


        Iterator<Program> iterator = programHashSet.iterator();
        while(iterator.hasNext()){
            Program program = iterator.next();

            Log.i("ProgramsManagementService", program.getD_day() + program.getURL() + program.getDate() + program.getDepartment() + program.getTitle());
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
        this.programHashSet = programHashSet;
        saveProgramsAtDB();
        Log.i("ProgramsManagementService", "------------------------------------------------프로그램 목록 업데이트됨");
    }

    private void updateProgramHashSet(HashSet<Program> newProgramHashSet){
        if(addedProgramHashSet == null) addedProgramHashSet = new HashSet<>();
        if(!addedProgramHashSet.isEmpty()) addedProgramHashSet.clear();

        Iterator<Program> newProgramHashSetIterator = newProgramHashSet.iterator();

        while(newProgramHashSetIterator.hasNext()){
            Program program = newProgramHashSetIterator.next();
            if(!programHashSet.contains(program)){
                Log.i("ProgramsManagementService", "------------------------------------------------------------!programHashSet.contains(program)");
                addedProgramHashSet.add(program);
            }
        }

        setProgramHashSet(newProgramHashSet);
    }

    private HashSet<Program> getAddedProgramHashSet() {
        return addedProgramHashSet;
    }

    public HashSet<Program> getProgramHashSet(){
        return programHashSet;
    }

    public ArrayList<Program> getProgramArrayList(){
        List list = new ArrayList<>(getProgramHashSet());
        Collections.sort(list);

        return new ArrayList<>(list);
    }
}
