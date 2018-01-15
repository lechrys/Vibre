package fr.myapplication.dc.myapplication.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.user.LoginActivity;
import util.ConnectionHelper;
import util.LoggerHelper;


public class SplashScreen extends Activity {
    private Vibrator vibrator;
    private Animation shake;
    private ImageView image;
    private AudioManager mAudioManager;

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

//        mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);

        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && !notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            startActivity(intent);
        }

//        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

        shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        image = (ImageView)findViewById(R.id.Logo);

        ///////////////////////////////////////////////////////
        //vibration au demarrage pendant 3secande
        /*vibrator.vibrate(3000);*/
        onShakeImage();

        if(ConnectionHelper.isConnectionActive(this)) {
            LoggerHelper.debug(getClass().getName(),"connection is active : ");
        }else {
            //no connection
            Toast toast = Toast.makeText(SplashScreen.this, "please verify your internet connection", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER , 0, 0);
            toast.show();
        }

        new Handler().postDelayed(new Runnable() {

        /*
         * Showing splash screen with a timer. This will be useful when you
         * want to show case your app logo / company
         */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }//onCreate

    public void onShakeImage() {
        image.setAnimation(shake);
    }//onShakeImage

    }//class
