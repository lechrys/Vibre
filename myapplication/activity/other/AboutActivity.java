package fr.myapplication.dc.myapplication.activity.other;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import fr.myapplication.dc.myapplication.R;

/**
 * Created by Crono on 05/03/17.
 */

public class AboutActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(this.getClass().getName(), "onCreate called");
        setContentView(R.layout.activity_about);
    }
}
