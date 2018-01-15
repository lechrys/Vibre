package fr.myapplication.dc.myapplication.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.MainActivity;
import fr.myapplication.dc.myapplication.activity.user.LoginActivity;
import fr.myapplication.dc.myapplication.application.AppLifeCycleApplication;
import util.LoggerHelper;
import util.PreferenceUtility;

import android.support.v4.app.NotificationCompat.InboxStyle;


/**
 * Created by chris on 03/05/2016.
 */
public class FireBaseNotificationService extends FirebaseMessagingService {

    int notif_counter = 0;

    /////////////////////////////////////////////////////////////////////

    //The message notified to the user
    final String CONSTANT_MESSAGE = "Someone is thinkin about you";

    /////////////////////////////////////////////////////////////////////

    @Override
    public void onMessageReceived(RemoteMessage message){
        String sender = message.getData().get("sender");
        LoggerHelper.info(LoggerHelper.DEBUG_TAG,"and with sender " + sender);
        if(AppLifeCycleApplication.appactivity.getClass().equals(MainActivity.class)){
            LoggerHelper.debug(getClass(),"AppLifeCycleApplication.appactivity.getClass().equals(MainActivity.class");
//            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//            notificationManager.cancel(notif_id);
        }
        else{
            if(PreferenceUtility.getNotificationEnabledPref(this.getApplicationContext())) {
                LoggerHelper.info(getClass(),"notif enabled");
                showNotification();
            }
            else{
                LoggerHelper.info(getClass(),"notif disabled");
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showNotification() {

        //Setup notification

        Intent toLaunch = new Intent(this, MainActivity.class);
        toLaunch.setAction("android.intent.action.MAIN");
        toLaunch.addCategory("android.intent.category.LAUNCHER");

        /////////////////////////////////////////////////////////////////////

        PendingIntent intentBack = PendingIntent.getActivity(
                getApplicationContext(), 0, toLaunch, PendingIntent.FLAG_UPDATE_CURRENT);

        //Build notification
        NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this)
                //.setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.cloud)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentText(CONSTANT_MESSAGE)
                .setAutoCancel(true)
                .setTicker("")
                .setNumber(++ notif_counter)
                .setStyle(new InboxStyle())
           ;

        if(PreferenceUtility.getSoundNotificationEnabledPref(this.getApplicationContext())){
            LoggerHelper.info(getClass(),"sound enabled");
            noBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        }
        else{
            LoggerHelper.info(getClass(),"sound disabled");
//            noBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
//            noBuilder.setSound(null);
        }
/*        else if( ! PreferenceUtility.getNotificationEnabledPref(this.getApplicationContext())
                && PreferenceUtility.getSoundNotificationEnabledPref(this.getApplicationContext())){
            noBuilder.setVisibility(Notification.VISIBILITY_SECRET);
        }
        else{
            return;
        }*/


        noBuilder.setContentIntent(intentBack);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = noBuilder.build();
        notification.defaults = 0;
        //ma notification se met à jour si l'ID est inchangé
        notificationManager.notify(0, notification); //0 = ID of notification

    }
}
