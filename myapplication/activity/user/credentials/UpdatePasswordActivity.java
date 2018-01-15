package fr.myapplication.dc.myapplication.activity.user.credentials;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.BasicActivity;

/**
 * Created by jhamid on 25/11/2017.
 */

public class UpdatePasswordActivity extends AppCompatActivity
        implements BasicActivity, ConfirmCurrentPasswordFragment.PasswordChange {

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_update_pass);

        if (findViewById(R.id.frame) != null) {

            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            ConfirmCurrentPasswordFragment firstFragment = new ConfirmCurrentPasswordFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.frame, firstFragment)
                    .commit();
            setButtonsListeners();
        }


    }

    @Override
    public void setViews() {
    }

    @Override
    public void setButtonsListeners() {

    }

/*
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
*/

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
    public void swipeFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        NewPasswordFragment fragment = new NewPasswordFragment();

        transaction
        .replace(R.id.frame, fragment)
        .addToBackStack(null)
        .commit();
    }
}
