package fr.myapplication.dc.myapplication.activity.user;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.BasicActivity;
import fr.myapplication.dc.myapplication.activity.registration.RegisterPhoneNumberActivity;
import fr.myapplication.dc.myapplication.media.PictureStorage;
import util.Constants;
import util.LoggerHelper;
import util.PreferenceUtility;

/**
 * Created by Crono on 05/03/17.
 */

public class AccountActivity extends AppCompatActivity implements BasicActivity{

    private ImageView avatarImage;
    private TextView loginTextView;
    String login;
    private LinearLayout verify_phone_layout;
    private TextView phone;
    private LinearLayout settings;
    private LinearLayout notifications;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(this.getClass().getName(), "onCreate called");

        login = PreferenceUtility.getLogin(this.getApplicationContext());

        setContentView(R.layout.activity_account);

        setViews();

        setButtonsListeners();
    }

    @Override
    public void setViews() {

        avatarImage = (ImageView) findViewById(R.id.avatarImage);
        loginTextView = (TextView) findViewById(R.id.loginTextView);
        loginTextView.setText(login);
        verify_phone_layout = (LinearLayout) findViewById(R.id.verify_phone);
        phone = (TextView) findViewById(R.id.phone);
        phone.setText(PreferenceUtility.getPhonePref(this));
        settings = (LinearLayout) findViewById(R.id.settings);
        notifications = (LinearLayout) findViewById(R.id.notifications);

        //load avatar
        PictureStorage.loadImageFromStorage(this,login, avatarImage);

    }

    @Override
    public void setButtonsListeners() {

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AccountActivity.this, SettingsActivity.class);

                //animation

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    //Todo : check version of OS to enable or disable Material design anim
                    String transitionName = AccountActivity.this.getString(R.string.avatar_transition);

                    ActivityOptions transitionActivityOptions =
                            ActivityOptions.makeSceneTransitionAnimation(AccountActivity.this, avatarImage, transitionName);

                    //start activity
                    startActivity(intent, transitionActivityOptions.toBundle());
                }
                else{
                    startActivity(intent);
                }
            }
        });

        verify_phone_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggerHelper.debug(getClass(),"verify_phone_image onClick");
                Intent intent = new Intent(AccountActivity.this, RegisterPhoneNumberActivity.class);
                intent.putExtra(Constants.PARENT_ACTIVITY,"boeuf");
                startActivity(intent);
            }
        });

        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggerHelper.debug(getClass(),"verify_phone_image onClick");
                Intent intent = new Intent(AccountActivity.this, FireBaseNotificationActivity.class);
                startActivity(intent);
            }
        });

    }

}
