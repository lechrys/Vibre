package fr.myapplication.dc.myapplication.activity.contact;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import fr.myapplication.dc.myapplication.R;

/**
 * Created by Crono on 31/07/16.
 */
public class CurrentContactActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(this.getClass().getName(), "onCreate called");

        //set views
        setContentView(R.layout.activity_current_contact);

        //initActionBar
        initActionBar();
    }

    /*
     * Set back button on bar
     */
    public void initActionBar(){
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
