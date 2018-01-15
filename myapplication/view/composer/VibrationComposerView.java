package fr.myapplication.dc.myapplication.view.composer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.contact.CurrentContactActivity;
import fr.myapplication.dc.myapplication.animation.CircleAnimationComposedVibration;
import fr.myapplication.dc.myapplication.data.model.VibrationMessage;
import fr.myapplication.dc.myapplication.gesture.OnSwipeTouchListener;
import util.Constants;
import util.LoggerHelper;

/**
 * Created by Crono on 14/02/17.
 */

public class VibrationComposerView extends View {

    private Handler handler;

    //////////////////////////////////////////////////

    private Vibrator vibrator;
    public final long[] vibration_pattern = {0,5000};

    //////////////////////////////////////////////////

    private  Paint paint;
    public float radius;
    public int final_radius;
    private float x;
    private float y;
    int strokeWidth;
    final String GREEN_COLOR = "#A4C639";

    //////////////////////////////////////////////////

    protected TranslateAnimation validateVibrationTranslateAnimation = new TranslateAnimation( x, 2000 , y, y );
    protected TranslateAnimation cancelVibrationtranslateAnimation = new TranslateAnimation( x, -2000 , y, y );
    private CircleAnimationComposedVibration circleAnimationComposedVibration;
    public int TRANSLATION_ANIMATION_DURATION = 500;

    //////////////////////////////////////////////////


    /*
    * Those timers are used to mesure how long the screen is pressed / released
    */

    ClickOperation clickOperation = new ClickOperation();
    ReleaseOperation releaseOperation = new ReleaseOperation();

    private long interval_pressed = 0;
    private long interval_released = 0;

    // max time to compose the vibration - take this value from DB
    private long MAX_TIME = 30000;
    private boolean firstTouchDown = true;
    private boolean firstTouchUp = true;

    //////////////////////////////////////////////////

    private VibrationMessage message;

    Boolean clicked = false;

    private boolean mustRun = true;

    //////////////////////////////////////////////////

    View.OnTouchListener onTouchListener;
    OnSwipeTouchListener onSwipeTouchListener;

    //////////////////////////////////////////////////

    public VibrationComposerView(Context context) {
        super(context);

      //  this.handler = activityHandler;

        //Init vibrator
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if(!vibrator.hasVibrator()){
            LoggerHelper.info("vibration is not available on the mobile");
            //Todo: what do we do if vibration is not available on the mobile ?
            //Todo: maybe displaying a graphical representation of the vibration ?
            return;
        }

        //set the circle out of screen
        x = -500;
        y = -500;

        message = new VibrationMessage().init();

        //create touch listeners
        setSendButtonListener();

        //background
        setBackground();

        //initList paint
        initPaint();

        //initList animation
        initAnimation();

        //initList translate animation
        initTranslateAnimation();
        createValidationVibrationTranslationAnimationLister();
        createCancelVibrationTranslationAnimationLister();
    }

    public void setBackground(){
        this.setBackgroundResource(R.drawable.composer_background);
    }

    public void removeBackground(){
        this.setBackground(null);
    }

    public void initPaint(){
        radius = 200;
        strokeWidth = 20;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.RED);
    }

    public void initRadius(){
        radius = 200;
        final_radius = 350;
    }

    public void initAnimation(){
        radius = 200;
        final_radius = 350;
        circleAnimationComposedVibration = new CircleAnimationComposedVibration(this,final_radius);
        circleAnimationComposedVibration.setDuration(500);
    }

    public void initTranslateAnimation(){
        // validateVibrationTranslateAnimation = new TranslateAnimation( x, 1000 , y, y );
        validateVibrationTranslateAnimation.setDuration(TRANSLATION_ANIMATION_DURATION);
        validateVibrationTranslateAnimation.setFillAfter( true );

        cancelVibrationtranslateAnimation.setDuration(TRANSLATION_ANIMATION_DURATION);
        cancelVibrationtranslateAnimation.setFillAfter( true );
    }

    public void startValidateVibrationTranslateAnimation(){
        this.startAnimation(validateVibrationTranslateAnimation);
    }
    public void startCancelVibrationTranslateAnimation(){
        this.startAnimation(cancelVibrationtranslateAnimation);
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.drawCircle(x, y, radius, paint);
    }
/*
    @Override
    public boolean onTouchEvent(final MotionEvent event){

        int action = MotionEventCompat.getActionMasked(event);

        switch (action){
            case (MotionEvent.ACTION_DOWN) :

                clicked = true;

                vibrator.vibrate(vibration_pattern,1);

                releaseOperation.cancel(true);

                clickOperation = new ClickOperation();
                clickOperation.execute("");

                x = event.getX();
                y = event.getY();

                //the logic
                if(firstTouchDown){
                    firstTouchDown = false;
                }
                else{
                    message.getPattern().add(interval_released);
                    interval_released = 0;
                }

                //animate the screen
                animateVibration(event);

                return true;

            case (MotionEvent.ACTION_MOVE) :

                return true;

            case (MotionEvent.ACTION_UP) :
                //Todo : remove the last waiting value as its useless
                clicked = false;
                clickOperation.cancel(true);
                vibrator.cancel();

                //reinit position
                x = -500;
                y = -500;

                //create Async task
                releaseOperation = new ReleaseOperation();
                releaseOperation.execute("");

                //the logic
                //push the interval
                message.getPattern().add(interval_pressed);
                LoggerHelper.warn("interval_pressed="+interval_pressed);
                interval_pressed = 0;

                //handle touch up
                if(firstTouchUp){
                    firstTouchUp = false;
                }

                //stop animation
                stopAnimateVibration();

                return true;

            case (MotionEvent.ACTION_CANCEL) :
                Log.d(LoggerHelper.DEBUG_TAG,"Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                Log.d(LoggerHelper.DEBUG_TAG,"Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            default :
                return super.onTouchEvent(event);
        }
    }
*/

    public void animateVibration(MotionEvent event){

        x = event.getX();
        y = event.getY();

        //start animate
        this.startAnimation(circleAnimationComposedVibration);
    }

    public void stopAnimateVibration(){
        //stop animation if on going
        this.clearAnimation();
        x = -500;
        y = -500;
        this.invalidate();
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    //////////////////////////////////////////////////

    public VibrationMessage getMessage() {
        return message;
    }

    public void setMessage(VibrationMessage message) {
        this.message = message;
    }

    //////////////////////////////////////////////////

    private class ClickOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            while(clicked && mustRun) {
                try {
                    Thread.sleep(1L);
                    LoggerHelper.warn("clicked && mustRun " + interval_pressed);
                    interval_pressed += 1L;
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            //TextView txt = (TextView) findViewById(R.id.output);
            //txt.setText("Executed"); // txt.setText(result);
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            LoggerHelper.debug("clicked finsihed onPostExecute");
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private class ReleaseOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            while(!clicked && mustRun) {
                try {
                    Thread.sleep(1L);
                    LoggerHelper.warn("!clicked && mustRun " + interval_released);
                    interval_released += 1L;
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            //TextView txt = (TextView) findViewById(R.id.output);
            //txt.setText("Executed"); // txt.setText(result);
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            LoggerHelper.debug("released finsihed onPostExecute");
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }


    /////////////////////////////////////////////////////////////

    private void setSendButtonListener() {

        //On touch
        onTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                removeBackground();

                boolean detectedUp = event.getAction() == MotionEvent.ACTION_UP;
                if (!onSwipeTouchListener.gestureDetector.onTouchEvent(event) && detectedUp) {
                    LoggerHelper.info("ACTION_UP");
                    clicked = false;
                    clickOperation.cancel(true);
                    vibrator.cancel();

                    //reinit position
                    x = -500;
                    y = -500;

                    //create Async task
                    releaseOperation = new ReleaseOperation();
                    releaseOperation.execute("");

                    //the logic
                    //push the interval
                    LoggerHelper.warn("interval_pressed="+interval_pressed);
                    message.getPattern().add(interval_pressed);
                    interval_pressed = 0;

                    //handle touch up
                    if(firstTouchUp){
                        firstTouchUp = false;
                    }

                    //stop animation
                    stopAnimateVibration();
                }
                return true;
            }
        };

        //On swipe
        onSwipeTouchListener = new OnSwipeTouchListener(this.getContext()) {
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                LoggerHelper.info("onSwipeRight");
                vibrator.cancel();
                clickOperation.cancel(true);
                releaseOperation.cancel(true);
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.FILL);
                startValidateVibrationTranslateAnimation();
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeRight();
                LoggerHelper.info("onSwipeRight");
                clickOperation.cancel(true);
                releaseOperation.cancel(true);
                paint.setStyle(Paint.Style.FILL);
                startCancelVibrationTranslateAnimation();
            }

            @Override
            public void onCustomDown(MotionEvent event) {
                LoggerHelper.debug("ACTION_DOWN");
                clicked = true;

                vibrator.vibrate(vibration_pattern,1);

                releaseOperation.cancel(true);

                clickOperation = new ClickOperation();
                clickOperation.execute("");

                x = event.getX();
                y = event.getY();

                //the logic
                if(firstTouchDown){
                    firstTouchDown = false;
                }
                else{
                    LoggerHelper.warn("interval_released="+interval_released);
                    message.getPattern().add(interval_released);
                    interval_released = 0;
                }

                //animate the screen
                animateVibration(event);
            }
        };

        this.setOnTouchListener(onTouchListener);
    }

    //Translation Animation Listener
    public void createValidationVibrationTranslationAnimationLister(){
        validateVibrationTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.parseColor(GREEN_COLOR));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LoggerHelper.info("validateVibrationTranslateAnimation onAnimationEnd");
                selectContactActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //Translation Animation Listener
    public void createCancelVibrationTranslationAnimationLister(){
        cancelVibrationtranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.RED);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LoggerHelper.info("validateVibrationTranslateAnimation onAnimationEnd");
                stopBackgroundTasks();
                //initList paint
                initPaint();
                //initList animation
                initAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //select contact
    public void selectContactActivity(){
        //hide current view
        this.setVisibility(View.GONE);
        //stop background takss
        stopBackgroundTasks();
        //go to CurrentContactActivity
        Intent intent = new Intent(getContext(),CurrentContactActivity.class);
        intent.putExtra(Constants.COMPOSED_MESSAGE,message);
        getContext().startActivity(intent);
    }

    public void stopBackgroundTasks(){
        clickOperation.cancel(true);
        releaseOperation.cancel(true);
        mustRun = false;
        vibrator.cancel();
    }
}
