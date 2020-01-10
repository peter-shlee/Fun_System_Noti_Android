package com.shlee.soongsilfunnoti;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;

public class ParsingFunSystemService extends Service {
    private Messenger messengerService = null;
    private MsgRequestHandler msgRequestHandler = null;

    private String funSystemURL = "https://fun.ssu.ac.kr";
    private String funSystemProgramListPath = funSystemURL + "/ko/program/all/list/all/";

    static final int REQUEST_MSG_PROGRAM_LIST = 1000;
    static final int RESPONSE_MSG_PROGRAM_LIST = 2000;

    private class MsgRequestHandler extends Handler {
        MsgRequestHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case REQUEST_MSG_PROGRAM_LIST:
                {
                    HashSet<Program> programHashSet = getFunSystemProgramHashSet();

                    Message replyMsg = Message.obtain(null,RESPONSE_MSG_PROGRAM_LIST,programHashSet);
                    try{
                        msg.replyTo.send(replyMsg);
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                    break;
                }
            }


        }
    }

    private HashSet<Program> getFunSystemProgramHashSet(){
        boolean parsingEndFlag = false;
        int funSystemProgramListPageNumber = 0;
        HashSet<Program> programHashSet = new HashSet<>();

        while(!parsingEndFlag){
            Document funSystemHTML = null;
            try{
                funSystemHTML = Jsoup.connect(funSystemProgramListPath + (++funSystemProgramListPageNumber)).get();
            } catch (IOException e){
                e.printStackTrace();
                break;
            }
            Elements funSystemProgramsListHTML = funSystemHTML.select(".columns-4").select("a");

            for(Element funSystemProgramHTML : funSystemProgramsListHTML){
                if(!(funSystemProgramHTML.select("label.CLOSED").isEmpty())) {
                    parsingEndFlag = true;
                    break;
                }

                Program program = new Program();
                program.setD_day(funSystemProgramHTML.select("label").select("b").text().split(" ")[0]);
                program.setDate(funSystemProgramHTML.select(".detail").select(".content").select("small").text());
                program.setDepartment(funSystemProgramHTML.select(".detail").select(".content").select("label").text());
                program.setURL(funSystemURL + funSystemProgramHTML.attr("href"));
                program.setTitle(funSystemProgramHTML.select(".detail").select(".content").select(".title").text());

                programHashSet.add(program);
            }
        }

        return programHashSet;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread("");
        thread.start();

        msgRequestHandler = new MsgRequestHandler(thread.getLooper());
        messengerService = new Messenger(msgRequestHandler);
    }

    @Override
    public void onDestroy() {
        msgRequestHandler.getLooper().quit();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messengerService.getBinder();
    }
}
