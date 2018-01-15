package fr.myapplication.dc.myapplication.view.composer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import fr.myapplication.dc.myapplication.activity.contact.CurrentContactActivity;
import fr.myapplication.dc.myapplication.animation.CircleAnimationSingleVibration;
import fr.myapplication.dc.myapplication.data.model.VibrationMessage;
import fr.myapplication.dc.myapplication.data.model.VibrationSingleMessage;
import fr.myapplication.dc.myapplication.gesture.OnSwipeTouchListener;
import util.LoggerHelper;

/**
 * Created by Crono on 14/02/17.
 */

public class VibrationSingleView extends View {

    private Vibrator vibrator;
    public final long[] vibration_pattern = {0,5000};

    //////////////////////////////////////////////////

    private Paint paint;
    public float radius;
    public int FINAL_RADIUS = 350;
    private float x;
    private float y;
    int STROKE_WIDTH = 20;
    final String GREEN_COLOR = "#A4C639";

    //////////////////////////////////////////////////

    public float RADIUS_INIT_VALUE = 150;
    public int BASE_COLOR = Color.RED;
    public int CIRCLE_ANIMATION_DURATION = 1000;
    public int TRANSLATION_ANIMATION_DURATION = 500;

    //////////////////////////////////////////////////

    private CircleAnimationSingleVibration circleAnimation;
    protected ValueAnimator colorChangeAnim;
    protected TranslateAnimation translateAnimation = new TranslateAnimation( x, 2000 , y, y );;

    //////////////////////////////////////////////////

    WaitOperation releaseOperation = new WaitOperation();
    final long WAIT_TIME = 5000;

    //////////////////////////////////////////////////

    private VibrationMessage message;
    private boolean buttonPushed = false;
    private boolean circleCharged = false;

    //////////////////////////////////////////////////

    View.OnTouchListener onTouchListener;
    OnSwipeTouchListener onSwipeTouchListener;

    //////////////////////////////////////////////////

    public VibrationSingleView(Context context) {
        this(context,null);
    }

    public VibrationSingleView(Context context, AttributeSet attrs) {
        super(context,attrs);
      //  this.handler = activityHandler;

        //Init vibrator
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        if( ! vibrator.hasVibrator()){
            LoggerHelper.info("vibration is not available on the mobile");
            //Todo: what do we do if vibration is not available on the mobile ?
            //Todo: maybe displaying a graphical representation of the vibration ?
            return;
        }
        //initList message
        message = new VibrationSingleMessage().init();

        //initList graphics & animation
        radius = RADIUS_INIT_VALUE;

        //initList paint
        initPaint(null,null);

        //initList animation
        circleAnimation = new CircleAnimationSingleVibration(this, FINAL_RADIUS);
        initAndStartRepeatAnimation();

        //initList translate animation
        initTranslateAnimation();
        createTranslationAnimationLister();

        //
        initColorChangeAnimation();
        createColorChangeAnimationLister();

        // listener
        setSendButtonListener();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LoggerHelper.info("parentWidth"+this.getWidth());
        LoggerHelper.info("parentHeight"+this.getHeight());
        x = parentWidth / 2;
        y = parentHeight / 2;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        LoggerHelper.info("WIDTH"+this.getWidth());
        LoggerHelper.info("HEIGHT"+this.getHeight());
    }

    @Override
    public void onFinishInflate(){
        LoggerHelper.info("onFinishInflate");
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.drawCircle(x, y, radius, paint);
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public VibrationMessage getMessage() {
        return message;
    }

    public void setMessage(VibrationMessage message) {
        this.message = message;
    }


    //////////////////////////////////////////////////////
    // INIT
    //////////////////////////////////////////////////////

    public void initPaint(final Paint.Style style, Integer color){
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(STROKE_WIDTH);

        if(color != null){
            paint.setColor(color);
        }
        else{
            paint.setColor(BASE_COLOR);
        }
        if(style != null){
            paint.setStyle(style);
        }
        else{
            paint.setStyle(Paint.Style.STROKE);
        }
    }

    public void initTranslateAnimation(){
       // validateVibrationTranslateAnimation = new TranslateAnimation( x, 1000 , y, y );
        translateAnimation.setDuration(TRANSLATION_ANIMATION_DURATION);
        translateAnimation.setFillAfter( true );
    }

    public void initAndStartRepeatAnimation(){
        circleAnimation.setDuration(CIRCLE_ANIMATION_DURATION);
        circleAnimation.setRepeatMode(ValueAnimator.REVERSE);
        circleAnimation.setRepeatCount(-1);
        this.startAnimation(circleAnimation);
    }

    public void initColorChangeAnimation(){
        //animate from your current color to red
        colorChangeAnim = ValueAnimator.ofInt(paint.getColor(), Color.parseColor(GREEN_COLOR));
        colorChangeAnim.setDuration(CIRCLE_ANIMATION_DURATION);
    }

    public void startColorChangeAnimation(){
        paint.setStyle(Paint.Style.FILL);
        this.colorChangeAnim.start();
    }

    //////////////////////////////////////////////////////

    public void startTranslateAnimation(){
        this.startAnimation(translateAnimation);
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////
    // Listeners
    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    private void setSendButtonListener(){

        //On touch
        onTouchListener = new View.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event)
            {
                boolean detectedUp = event.getAction() == MotionEvent.ACTION_UP;
                if ( ! onSwipeTouchListener.gestureDetector.onTouchEvent(event) && detectedUp)
                {
                    LoggerHelper.debug("ACTION_UP ?");
                    if( ! circleCharged){
                        LoggerHelper.debug("buttonPushed = false");
                        buttonPushed=false;

                        //stop the vibration
//                        vibrator.cancel();

                        //color change anim is canceled
                        colorChangeAnim.cancel();

                        //the circle animation restarts
                        paint.setStyle(Paint.Style.STROKE);
                        circleAnimation.reset();
                        circleAnimation.setRepeatCount(-1);
                        circleAnimation.start();
                        //initAndStartRepeatAnimation();
                    }
                    else{

                        LoggerHelper.info(getClass(),"circle is charged !");
                        onSwipeTouchListener.onSwipeRight();

                    }
                }
                return true;
            }
        };

        //On swipe
        onSwipeTouchListener = new OnSwipeTouchListener(this.getContext()){
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                LoggerHelper.info("onSwipeRight");
                if(circleCharged){
                    LoggerHelper.info("startValidateVibrationTranslateAnimation");
                    startTranslateAnimation();
                }
            }
            @Override
            public void onCustomDown() {
                LoggerHelper.debug("ACTION_DOWN");
                    if( ! circleCharged) {
                    LoggerHelper.debug("buttonPushed = true");
                    buttonPushed = true;

                    //start to vibrate
//                    vibrator.vibrate(vibration_pattern,1);

                    //we need one single circle animation
                    //start
                    circleAnimation.reset();
                    circleAnimation.setRepeatCount(0);
                    circleAnimation.start();

                    //we also animate with color change
                    startColorChangeAnimation();
                }
            }
        };

        this.setOnTouchListener(onTouchListener);

        LoggerHelper.info("setSendButtonListener called");

    }


    //Translation Animation Listener
    public void createTranslationAnimationLister(){
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LoggerHelper.info("validateVibrationTranslateAnimation onAnimationEnd");
                //radius = 0;
                circleAnimation.reset();
               // circleAnimation.setRepeatCount(-1);
                circleAnimation.start();
                selectContactActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    public void createColorChangeAnimationLister() {

        colorChangeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                paint.setColor((int) animation.getAnimatedValue());
            }
        });

        colorChangeAnim.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                LoggerHelper.info("colorChangeAnim onAnimationEnd");
                if(buttonPushed){
                    LoggerHelper.info("onAnimationEnd buttonPushed");
                    circleCharged = true;
//                    vibrator.cancel();
                    //cancelAnimations();
                    releaseOperation = new WaitOperation();
                    releaseOperation.execute();
                    LoggerHelper.info("onAnimationEnd  circleCharged = true");
                }
            }
        });
    }

    //////////////////////////////////////////////////
    //This async relaunch the animation after 5 seconds
    //////////////////////////////////////////////////

    private class WaitOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                LoggerHelper.debug("released finsihed onPostExecute");
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            LoggerHelper.debug("released finsihed onPostExecute");
            circleCharged = false;
            initPaint(null,null);
            initAndStartRepeatAnimation();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    //select contact
    public void selectContactActivity(){
        //hide current view
        this.setVisibility(View.GONE);
        //clean background tasks
        stopBackgroundTasks();
        //go to CurrentContactActivity
        LoggerHelper.info("Go to CurrentContactActivity");
        Intent intent = new Intent(getContext(),CurrentContactActivity.class);
        getContext().startActivity(intent);
    }
    //////////////////////////////////////////////////////

    public void stopBackgroundTasks(){
        LoggerHelper.debug("stopBackgroundTasks");
        buttonPushed = false;
        if(releaseOperation != null)releaseOperation.cancel(true);
        if(circleAnimation != null)circleAnimation.cancel();
        if(colorChangeAnim != null)colorChangeAnim.cancel();
        if(translateAnimation != null)translateAnimation.cancel();
//        if(vibrator != null) vibrator.cancel();

        initAndStartRepeatAnimation();

    }
}
