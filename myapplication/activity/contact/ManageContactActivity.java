package fr.myapplication.dc.myapplication.activity.contact;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import fr.myapplication.dc.myapplication.R;
import util.LoggerHelper;

/**
 * Created by Crono on 31/07/16.
 */
public class ManageContactActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(this.getClass().getName(), "onCreate called");
        setContentView(R.layout.activity_manage_contact);
        //initActionBar
        initActionBar();
    }

    /*
     * Set back button on ActionBar
     */
    public void initActionBar(){
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        LoggerHelper.info(getClass(),"onBackPressed");
    }
}
