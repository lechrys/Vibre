package fr.myapplication.dc.myapplication.activity.notification;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Random;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.BasicActivity;
import fr.myapplication.dc.myapplication.data.model.VibrationMessage;
import util.Constants;
import util.LoggerHelper;

/**
 * Created by Crono on 10/08/16.
 * This class performs a vibration when notification is received
 * will eventually display some graphical representation of the vibrations
 */

//Todo: This activity can be changed to a fragment
public class NotificationReaderActivity extends AppCompatActivity implements BasicActivity {

    private Vibrator vibrator;
    VibrationMessage message;
    LinearLayout main_layout;
    Integer currentItem = 1;//first value is skipped, always 0 to start immediately

    //Animators
    //ObjectAnimator fakeColorAnimator;
    //ObjectAnimator backgroundColorAnimator;

    AnimateOperation animateOperation = new AnimateOperation();
    ClearOperation clearOperation = new ClearOperation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setViews();

        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        if(!vibrator.hasVibrator()){
            Log.d(this.getClass().getName(),"vibration is not available on the mobile");
            //Todo: what do we do if vibration is not available on the mobile ?
            //Todo: maybe displaying a graphical representation of the vibration ?
            return;
        }

        //We get the selected message
        message = (VibrationMessage) this.getIntent().getExtras().
                getSerializable(Constants.SELECTED_MESSAGE);

        // check that the message is consistent
        if(message != null && message.getPattern() != null && message.getPattern().size() > 0){
            LoggerHelper.info(String.format("NotificationReaderActivity message=%s",message.getPattern()));

            //animate
           // animate();
            animateOperation.execute();

            //play the vibration
            vibrate(message.getPattern());

        }
        else{
            LoggerHelper.warn("NotificationReaderActivity message is null");
        }
    }

    ////////////////////////////////////////////////////////////

    @Override
    public void setViews() {
        setContentView(R.layout.activity_read_vibration);
        main_layout = (LinearLayout) findViewById(R.id.activity_read_vibration_layout);

    }

    @Override
    public void setButtonsListeners() {

    }

    ////////////////////////////////////////////////////////////

    //Todo:add animation when reading vibration
    public void vibrate(final List<Long> pattern){
        Long[] pattern_array = new Long[pattern.size()];
        pattern_array = pattern.toArray(pattern_array);
        // -1 means no repeat
        vibrator.vibrate(ArrayUtils.toPrimitive(pattern_array),-1);
    }

    ////////////////////////////////////////////////

    public void setBackground(){
        Random rand = new Random();
        main_layout.setBackgroundColor(Color.argb(255, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256) ));
    }

    public void setOriginalBackground(){
        //white color
        main_layout.setBackgroundColor(Color.argb(255, 255, 255, 255));
    }


    private class AnimateOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                LoggerHelper.debug("AnimateOperation doInBackground with currentItem="+message.getPattern().get(currentItem));
                Thread.sleep(message.getPattern().get(currentItem));
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            currentItem ++;
            if(currentItem < message.getPattern().size()) {
                animateOperation = new AnimateOperation();
                clearOperation.execute();
            }
            else{
                setOriginalBackground();
                //goToMessageListActivity();
            }
        }

        @Override
        protected void onPreExecute() {
            setBackground();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private class ClearOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                LoggerHelper.debug("ClearOperation doInBackground with currentItem="+message.getPattern().get(currentItem));
                Thread.sleep(message.getPattern().get(currentItem));
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            currentItem ++;
            if(currentItem < message.getPattern().size()) {
                clearOperation = new ClearOperation();
                animateOperation.execute();
            }
            else{
                //goToMessageListActivity();
            }
        }

        @Override
        protected void onPreExecute() {
            setOriginalBackground();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    ////////////////////////////////////////////////////////////
    // activity stop behaviour
    ////////////////////////////////////////////////////////////

    public void goToMessageListActivity(){
        LoggerHelper.info("goToMessageListActivity");
        Intent intent = new Intent(NotificationReaderActivity.this,NotificationReaderListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPause(){
        super.onPause();
        vibrator.cancel();
        animateOperation.cancel(true);
        clearOperation.cancel(true);
        //to stop the running async tasks
        if(message.getPattern() != null) message.getPattern().clear();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        vibrator.cancel();
        currentItem = -1;
        animateOperation.cancel(true);
        clearOperation.cancel(true);
        //to stop the running async tasks
        if(message.getPattern() != null) message.getPattern().clear();
    }


    /////////////////////////////////////////////////////////
/*
    public void initAndStartRepeatAnimation(){

        /////////////////////////////////////

        //The real colored animation
        backgroundColorAnimator = ObjectAnimator.ofObject(main_layout,
                "backgroundColor",
                new ArgbEvaluator(),
                0xFFFFFFFF,
                0xff78c5f9);

        //the fake animator
        fakeColorAnimator = ObjectAnimator.ofObject(main_layout,
                "backgroundColor",
                new ArgbEvaluator(),
                0xFFFFFFFF,
                0xFFFFFFFF);

        fakeColorAnimator.setDuration(message.getPattern().get(currentItem + 1));

        /////////////////////////////////////
        //Listeners
        /////////////////////////////////////

        //listen for end of animation to launch fake animation
        backgroundColorAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                LoggerHelper.debug("backgroundColorAnimator onAnimationStart with duration="+backgroundColorAnimator.getDuration());
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                LoggerHelper.debug("onAnimationEnd ColorAnimator");
                currentItem ++;
                if (currentItem < message.getPattern().size()) {
                    fakeColorAnimator.setDuration(message.getPattern().get(currentItem));
                    fakeColorAnimator.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        fakeColorAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                LoggerHelper.debug("fakeColorAnimator onAnimationStart with duration="+fakeColorAnimator.getDuration());
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                currentItem ++;
                if (currentItem < message.getPattern().size()) {
                    backgroundColorAnimator.setDuration(message.getPattern().get(currentItem));
                    backgroundColorAnimator.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void configureDuration(){
        backgroundColorAnimator.setDuration(message.getPattern().get(currentItem));
        fakeColorAnimator.setDuration(message.getPattern().get(currentItem + 1));
    }

    public void animate() {
        //we expecte
        if (message.getPattern().size() > 1) {

            //Init animation
            initAndStartRepeatAnimation();

            //start the animation
            backgroundColorAnimator.start();

        }
    }
*/
}
