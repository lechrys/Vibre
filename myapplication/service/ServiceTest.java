package fr.myapplication.dc.myapplication.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import util.LoggerHelper;

/**
 * Created by jhamid on 17/09/2017.
 */

public class ServiceTest extends Service {

    HandlerThread handlerThread;
    Handler handler;

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        LoggerHelper.info(getClass(),"onCreate");
        handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        handler = new Handler(looper){
            @Override
            public void handleMessage(Message msg) {
                LoggerHelper.error(getClass(),"initHandler handleMessage received s" + msg);
            }
        };

/*            {
                @Override
                public void handleMessage(Message msg) {
                LoggerHelper.info(getClass(),"initHandler handleMessage received s" + msg);
            }
        };*/
        test();
    }

    public void test(){
        LoggerHelper.info(getClass(),"test");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message m = handler.obtainMessage();
                m.arg1 = 1;
                m.arg2 = 2;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ServiceTest.this,"sending message from ServiceTest.test",Toast.LENGTH_LONG).show();
                    }
                });
                handler.sendMessage(m);
//
            }
        });
        t.start();
    }
}
