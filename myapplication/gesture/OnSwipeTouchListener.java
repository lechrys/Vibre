package fr.myapplication.dc.myapplication.gesture;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import util.LoggerHelper;

/**
 * Created by Crono on 19/02/17.
 */


public class OnSwipeTouchListener implements View.OnTouchListener {

    public final GestureDetector gestureDetector;

    public OnSwipeTouchListener (Context ctx){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        LoggerHelper.debug("OnSwipeTouchListener onTouch");
        return gestureDetector.onTouchEvent(event);
    }

    protected final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            onCustomDown();
            onCustomDown(e);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            LoggerHelper.debug("GestureListener onFling");
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                    result = true;
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeRight() {
        LoggerHelper.debug("onSwipe right called");
    }

    public void onSwipeLeft() {
        LoggerHelper.debug("onSwipeLeft called");
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }

    public void onCustomDown() {
    }
    public void onCustomDown(MotionEvent e) {
    }
}

