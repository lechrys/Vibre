package fr.myapplication.dc.myapplication.activity.contact;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.google.android.gms.appinvite.AppInviteInvitation;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.BasicActivity;
import fr.myapplication.dc.myapplication.activity.MainActivity;
import fr.myapplication.dc.myapplication.activity.other.AboutActivity;
import util.LoggerHelper;

public class InviteForAppActivity extends AppCompatActivity {

    private Button inviteButton;

    private static final String DYNAMIC_LINK = "https://kayg4.app.goo.gl/";

    public static final short REQUEST_INVITE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //setViews
        setViews();

        //Listeners
//        setButtonsListeners();

        //initActionBar
        initActionBar();

        onInviteClicked();
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


    public void setViews() {
        setContentView(R.layout.activity_invite);
//        inviteButton = (Button) findViewById(R.id.invite_cloud_image);
    }

/*    @Override
    public void setButtonsListeners() {
        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }*/

    private void onInviteClicked() {

/*        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();*/

        Intent intent = new AppInviteInvitation.IntentBuilder("app invite")
                .setMessage("link to donwlaod the app")
                .setDeepLink(Uri.parse(DYNAMIC_LINK))
                .setCustomImage(Uri.parse("http://wikiclipart.com/wp-content/uploads/2017/03/Cloud-black-and-white-clouds-clipart-black-and-white-free-images.png"))
                .setCallToActionText("actionText")
                .build();

        startActivityForResult(intent, REQUEST_INVITE);
    }

    ////////////////////////////////////////////////////
    // Handle FireBase Invite for the app
    ////////////////////////////////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        LoggerHelper.debug(this.getClass().getName(), "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    LoggerHelper.debug(this.getClass().getName(), "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // ...
            }
        }
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

}
