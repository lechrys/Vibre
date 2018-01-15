package fr.myapplication.dc.myapplication.activity.contact;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;

import fr.myapplication.dc.myapplication.R;

public class InvitationContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(this.getClass().getName(), "InvitationContactActivity onCreate called");
        setContentView(R.layout.activity_invitation_contact);
    }
}
