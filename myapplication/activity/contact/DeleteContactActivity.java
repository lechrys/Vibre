package fr.myapplication.dc.myapplication.activity.contact;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import fr.myapplication.dc.myapplication.R;

/**
 * Created by Crono on 31/07/16.
 */
public class DeleteContactActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(this.getClass().getName(), "onCreate called");
        setContentView(R.layout.activity_delete_contact);
    }
}
