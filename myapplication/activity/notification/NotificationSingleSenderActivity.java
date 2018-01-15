package fr.myapplication.dc.myapplication.activity.notification;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.BasicActivity;
import fr.myapplication.dc.myapplication.activity.MainActivity;
import fr.myapplication.dc.myapplication.data.model.VibrationSingleMessage;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.view.composer.VibrationSingleView;
import util.Constants;
import util.LoggerHelper;

/**
 * Created by Crono on 10/08/16.
 * This class sends a Notif notification after clicking on the contact
 * The notification is first send to our application server which will forward it to GCM
 */
public class NotificationSingleSenderActivity extends AppCompatActivity implements BasicActivity{

    SharedPreferences prefs;
    VibrationSingleView vibrationComposer;
    LinearLayout ll;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //retrieve prefs
        prefs = this.getSharedPreferences(Constants.APP_ID, Context.MODE_PRIVATE);

        //Handler
        setUpHandler();

        //views setup
        setViews();

        //initList listeners
        setButtonsListeners();

    }

    @Override
    public void setViews() {
        //set views
        setContentView(R.layout.activity_send_single_vibration);
      /*  ll = (LinearLayout)findViewById(R.id.sendSingleVibration);
        vibrationComposer = new VibrationSingleView(this.getApplicationContext(),handler);
        ll.addView(vibrationComposer);
        */
    }

    @Override
    public void setButtonsListeners() {

    }

    /*
    * Sends The notification to our app server
    * the notification is forwarded to the mobile via firebase notification
    */
    protected void sendRequest(){

    }

    public void setUpHandler(){
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                LoggerHelper.info("message received");
                persistMessage();
                backToMainActivity();
            }
        };
    }

    public void backToMainActivity(){
        LoggerHelper.info("choseContact");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    protected void persistMessage(){
        Bundle extras = getIntent().getExtras();
        String selected = (String) extras.get(Constants.SELECTED_CONTACT_KEY);
        LoggerHelper.info("persistMessage selected contact is " + selected);
        DataPersistenceManager.persistMessage(prefs,selected);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoggerHelper.warn("NotificationSenderActivity.onDestroy");
        getDelegate().onDestroy();
        vibrationComposer.stopBackgroundTasks();
    }

    @Override
    public void onPause(){
        super.onPause();
        LoggerHelper.warn("NotificationSenderActivity.onPause");
        vibrationComposer.stopBackgroundTasks();
    }
}
