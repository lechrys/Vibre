package fr.myapplication.dc.myapplication.view.fragment;

import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.contact.CurrentContactActivity;
import fr.myapplication.dc.myapplication.gesture.OnSwipeTouchListener;
import fr.myapplication.dc.myapplication.view.composer.VibrationSingleView;
import util.LoggerHelper;

/**
 * Created by Crono on 07/04/17.
 */

public class VibrationSingleFragmentWithBubbleViews extends Fragment{

    boolean cloud_1_visible;
    boolean cloud_2_visible;
    boolean cloud_3_visible;

    ImageView cloud1;
    ImageView cloud2;
    ImageView cloud3;


    //////////////////////////////////////////////////

    RelativeLayout layout;

    //////////////////////////////////////////////////

    View.OnTouchListener onTouchListener;
    OnSwipeTouchListener onSwipeTouchListener;

    //////////////////////////////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoggerHelper.info(this.getClass().getName(),"onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LoggerHelper.info(this.getClass().getName(),"onCreateView");

        layout = (RelativeLayout) inflater.inflate(R.layout.fragment_compose_message_cloud,container,false);

        cloud1 = (ImageView) layout.findViewById(R.id.cloud_1);
        cloud2 = (ImageView) layout.findViewById(R.id.cloud_2);
        cloud3 = (ImageView) layout.findViewById(R.id.cloud_3);

        setListeners();

        return layout;
    }



    @Override
    public void onDestroy(){
        super.onDestroy();
        LoggerHelper.info(this.getClass().getName(),"onDestroy");
    }

    @Override
    public void onDestroyView(){
        super.onDestroy();
        LoggerHelper.info(this.getClass().getName(),"onDestroyView");
    }

    @Override
    public void onPause() {
        super.onPause();
        LoggerHelper.info(this.getClass().getName(),"onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        LoggerHelper.info(this.getClass().getName(),"onStop");
    }

    private class WaitOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                LoggerHelper.debug("released finsihed onPostExecute");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            LoggerHelper.debug("released finsihed onPostExecute");
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////
    // Listeners
    //////////////////////////////////////////////////
    //////////////////////////////////////////////////

    private void setListeners(){

        LoggerHelper.info("IN setListeners");

        //On touch
        onTouchListener = new View.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event)
            {
                LoggerHelper.info("onTouch called !");
                boolean detectedUp = event.getAction() == MotionEvent.ACTION_UP;
                if (!onSwipeTouchListener.gestureDetector.onTouchEvent(event) && detectedUp)
                {
                    LoggerHelper.debug("ACTION_UP ?");
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
            }
            @Override
            public void onCustomDown() {
                LoggerHelper.debug("ACTION_DOWN");
            }
        };

        layout.setOnTouchListener(onTouchListener);

        LoggerHelper.info("setSendButtonListener called");

    }

    //select contact
    public void selectContactActivity(){
        //hide current view
        //go to CurrentContactActivity
        LoggerHelper.info("Go to CurrentContactActivity");
        Intent intent = new Intent(getContext(),CurrentContactActivity.class);
        getContext().startActivity(intent);
    }
}
