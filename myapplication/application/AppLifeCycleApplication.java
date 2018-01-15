package fr.myapplication.dc.myapplication.application;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.OnDisconnect;

import fr.myapplication.dc.myapplication.service.NetworkStatusReceiver;
import util.LoggerHelper;
import util.ServerTimeSyncer;

/**
 * Created by jhamid on 16/09/2017.
 */

public class AppLifeCycleApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

    AppLifeCycleApplication singleton;

    public static Activity appactivity;

    public AppLifeCycleApplication getSingleton (){
        return singleton;
    }

    OnDisconnect onDisconnectRef;

    //how many activities are started
    // if == 0 means activities are in background

    private int cpt_activities = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        LoggerHelper.info(getClass(),"onCreate");
        singleton = this;
        registerActivityLifecycleCallbacks(singleton);

        registerNetwordStatusReceiver();

        EnableFirebaseLogs();

        initFireBaseTimerSync();

        onDisconnectRef = FirebaseDatabase.getInstance().getReference("disconnectmessage").onDisconnect();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        LoggerHelper.info(getClass(),"onActivityCreated with activity = " + activity.getLocalClassName());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        LoggerHelper.info(getClass(),"onActivityStarted with activity = " + activity.getLocalClassName());
    }

    @Override
    public void onActivityResumed(Activity activity) {

        LoggerHelper.info(getClass(),"onActivityResumed " + activity.getLocalClassName() + " cpt_activities == " + cpt_activities);

        appactivity = activity;//here we get the activity

        Intent i = new Intent(this, NetworkStatusReceiver.class);
        sendBroadcast(i);//here we are calling the broadcastreceiver to check connection state.

        if(cpt_activities == 0){
            LoggerHelper.info(getClass(),"cpt_activities == " + cpt_activities + " --> goonline");
            FirebaseDatabase.getInstance().goOnline();
        }

        cpt_activities ++;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        LoggerHelper.info(getClass(),"onActivityPaused " + activity.getLocalClassName() + "  cpt_activities == " + cpt_activities);

        cpt_activities --;

        if(cpt_activities == 0){
            LoggerHelper.info(getClass(),"cpt_activities == 0 --> gooffline");
            //Todo check if online before going offline, otherwise it is useless
            FirebaseDatabase.getInstance().goOffline();
        }

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    private void registerNetwordStatusReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new NetworkStatusReceiver(), intentFilter);
    }

    private void initFireBaseTimerSync(){
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        ServerTimeSyncer.getInstance().startServerTimeSync();
    }

    private void EnableFirebaseLogs(){
        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
    }

    //////////////////////////////////////////////////////////////////////////////////
}
