package fr.myapplication.dc.myapplication.activity.user;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.BasicActivity;
import fr.myapplication.dc.myapplication.activity.user.credentials.UpdatePasswordActivity;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.media.PictureStorage;
import util.Constants;
import util.GlobalHelper;
import util.LoggerHelper;
import util.PreferenceUtility;

/**
 * Created by jhamid on 17/09/2017.
 */

public class SettingsActivity extends AppCompatActivity implements BasicActivity {

    private ImageView avatarImage;
    private TextView loginTextView;
    private static final int RESULT_LOAD_IMAGE = 1;
    private ProgressDialog progressDialog;
    private String main_login;
    private String other_login;
    private TextView change_pass;
    private Button validate;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        prefs = this.getSharedPreferences(Constants.APP_ID, Context.MODE_PRIVATE);

        setContentView(R.layout.activity_settings);

        main_login = PreferenceUtility.getLogin(this.getApplicationContext());
        other_login = PreferenceUtility.getOtherLoginPref(this.getApplicationContext());

        setViews();

        setButtonsListeners();

        initActionBar();

        LoggerHelper.info(getClass(), "onCreate called with main_login = " + main_login);
    }

    @Override
    public void setViews() {
        //main_login
        loginTextView = (TextView) findViewById(R.id.login);
        loginTextView.setText(other_login);
        loginTextView.setFilters(new InputFilter[]{GlobalHelper.getLoginFilter()});

        //avatar
        avatarImage = (ImageView) findViewById(R.id.avatarImage);

        //creds
        change_pass = (TextView) findViewById(R.id.change_password);

        //validate
        validate = (Button) findViewById(R.id.button_login_validate);

        //load avatar
        PictureStorage.loadImageFromStorage(this, main_login, avatarImage);
    }

    @Override
    public void setButtonsListeners() {

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        change_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this,UpdatePasswordActivity.class);
                startActivity(intent);
            }
        });

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLogin();
            }
        });
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

    private void changeLogin(){
        LoggerHelper.info(getClass(),"IN changeLogin with other_login = " + other_login +
                " && loginTextView = " + loginTextView.getText());
        final String new_login = loginTextView.getText().toString();
        if( ! loginTextView.getText().equals(other_login)){
            DataPersistenceManager.updateUserLogin(prefs,new_login);
            other_login = new_login;
            PreferenceUtility.setOtherLoginPref(this.getApplicationContext(),other_login);
            Toast.makeText(this,"Login successfully updated",Toast.LENGTH_SHORT).show();
        }
    }
}
