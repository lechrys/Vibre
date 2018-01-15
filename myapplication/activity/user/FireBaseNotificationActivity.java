package fr.myapplication.dc.myapplication.activity.user;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.BasicActivity;
import util.LoggerHelper;
import util.PreferenceUtility;

/**
 * Created by jhamid on 26/11/2017.
 */

public class FireBaseNotificationActivity extends AppCompatActivity implements BasicActivity {

    SwitchCompat sound_switch;
    SwitchCompat notification_switch;
    TextView text_explanation;

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);

        setContentView(R.layout.activity_account_notification);

        setViews();

        setButtonsListeners();
    }

    @Override
    public void setViews() {

        text_explanation = (TextView) findViewById(R.id.text_explanation);

        LoggerHelper.info(getClass(),"PreferenceUtility.getSoundNotificationEnabledPref(this.getApplicationContext()) = " + PreferenceUtility.getNotificationEnabledPref(this.getApplicationContext()));

        notification_switch = (SwitchCompat) findViewById(R.id.notification_switch);
        if(PreferenceUtility.getNotificationEnabledPref(this.getApplicationContext())){
            notification_switch.setChecked(true);
            text_explanation.setVisibility(View.INVISIBLE);
        }
        else{
            text_explanation.setVisibility(View.VISIBLE);
        }

        LoggerHelper.info(getClass(),"PreferenceUtility.getSoundNotificationEnabledPref(this.getApplicationContext()) = " + PreferenceUtility.getSoundNotificationEnabledPref(this.getApplicationContext()));

        sound_switch = (SwitchCompat) findViewById(R.id.sound_switch);
        if(PreferenceUtility.getSoundNotificationEnabledPref(this.getApplicationContext())){
            sound_switch.setChecked(true);
        }
    }

    @Override
    public void setButtonsListeners() {
        sound_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceUtility.setSoundNotificationEnabledPref
                        (FireBaseNotificationActivity.this.getApplicationContext(),isChecked);
            }
        });

        notification_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceUtility.setNotificationEnabledPref
                        (FireBaseNotificationActivity.this.getApplicationContext(),isChecked);
                if(isChecked){
                    text_explanation.setVisibility(View.INVISIBLE);
                }
                else{
                    text_explanation.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
