package fr.myapplication.dc.myapplication.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.OnDisconnect;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.application.AppLifeCycleApplication;
import util.ConnectionHelper;
import util.LoggerHelper;

/**
 * Created by jhamid on 16/09/2017.
 */

public class NetworkStatusReceiver extends BroadcastReceiver {

    static Snackbar snackbar;
    static boolean wasDisconnected;

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        LoggerHelper.info(getClass(),"IN onReceive with activeNetworkInfo = " + activeNetworkInfo);

        if (activeNetworkInfo == null || ! activeNetworkInfo.isConnected()) {
            snack(null, 0, "Network Connection inactive",context.getApplicationContext());
        } else{
            snack(null, 1, "Network Connection active",context.getApplicationContext());
        }

    }

    public void snack (HashMap<String,View.OnClickListener> actions, int priority, String message, Context context) {

        if(AppLifeCycleApplication.appactivity != null){
            LoggerHelper.info(getClass(),"AppLifeCycleApplication.appactivity != null");
            //hide activity content
            ViewGroup layout = (ViewGroup) AppLifeCycleApplication.appactivity.findViewById(R.id.mainLayout);

            switch (priority) {
                case 0:
                    setSnackbar(actions,message);
                    wasDisconnected = true;
                    snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
                    snackbar.getView().setBackgroundColor(context.getResources().getColor(R.color.red));
                    snackbar.show();

                    //hide activity content
                    if(layout != null){
                        layout.setVisibility(View.INVISIBLE);
                        LoggerHelper.info(getClass(),"layout.setVisibility(View.INVISIBLE)");
                    }

                    FirebaseDatabase.getInstance().goOffline();

                    break;
                case 1:
                    if(wasDisconnected){
                        wasDisconnected = false;
                        setSnackbar(actions,message);
                        snackbar.setDuration(Snackbar.LENGTH_LONG);
                        snackbar.getView().setBackgroundColor(Color.parseColor("#66ccff"));
                        snackbar.show();
                        if(layout != null){
                            layout.setVisibility(View.VISIBLE);
                            LoggerHelper.info(getClass(),"layout.setVisibility(View.VISIBLE)");
                        }

                        FirebaseDatabase.getInstance().goOnline();
                    }
                    break;
                case 2:
                    snackbar.getView().setBackgroundColor(Color.parseColor("#66ff33"));
                    break;
            }

        }
        else{
            LoggerHelper.info(getClass(),"AppLifeCycleApplication.appactivity == null");
        }
    }

    private static void hideSnackbar(){
        if(snackbar !=null && snackbar.isShown()){
            snackbar.dismiss();
        }
    }

    private void setSnackbar(HashMap<String,View.OnClickListener> actions, final String message){

        //remove eventual actions
        if (actions != null) {
            Iterator iterator = actions.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry pair = (Map.Entry) iterator.next();
                snackbar.setAction((String) pair.getKey(), (View.OnClickListener) pair.getValue());
                iterator.remove(); // avoids a ConcurrentModificationException
            }
        }

        //set View style
        snackbar = Snackbar.make(AppLifeCycleApplication.appactivity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(ContextCompat.getColor(AppLifeCycleApplication.appactivity, R.color.colorGrey));
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)view.getLayoutParams();
        params.gravity = Gravity.CENTER;
        view.setLayoutParams(params);
    }

}

