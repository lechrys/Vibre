package fr.myapplication.dc.myapplication.activity.contact;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.data.model.Contact;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.media.PictureStorage;
import util.Constants;
import util.LoggerHelper;
import util.PreferenceUtility;

/**
 * Created by jhamid on 17/09/2017.
 */

public class ContactDetailActivity extends AppCompatActivity {

    Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail_contact);
        //initActionBar
        initActionBar();

        Bundle b = this.getIntent().getExtras();

        contact = null;

        if (b != null) {
            contact = (Contact) b.getSerializable(Constants.CONTACTS_KEY);
            createContent(contact);
        }

        LoggerHelper.info(this.getClass().getName(), "onCreate called with contact = " + contact);

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

    private void createContent(Contact contact){
        LoggerHelper.info(this.getClass().getName(), "login = " + contact.getLogin());

        //set Layout content
        //get the avatar
        ImageView avatarImage = (ImageView) findViewById(R.id.avatarImage);
        PictureStorage.loadAvatar(Glide.with(this), this, contact.getLogin(), avatarImage);

        //login
        TextView loginTextView = (TextView) findViewById(R.id.login);
        loginTextView.setText(contact.getLogin());

        //get the date since friend
        TextView friend_since_text = (TextView) findViewById(R.id.friend_since_text);
        StringBuilder stringBuilder = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyy MMM d", Locale.getDefault());
        final String formatted_date = dateFormat.format(contact.getCreationDateLong());
        stringBuilder.append(formatted_date);
        friend_since_text.setText(stringBuilder);

        //get the stats
        TextView statistics = (TextView) findViewById(R.id.statistics_text);
        StringBuilder string_builder = new StringBuilder();
        LoggerHelper.info("nb messages = " + contact.getMessageList().size());
        string_builder.append(contact.getMessageList().size());
        statistics.setText(string_builder);

        ImageView imageDelete = (ImageView) findViewById(R.id.deleteUser);
        imageDelete.setOnClickListener(getDeleteClickListener());
    }

    //////////////////////////////////////////////////////////////
    // Handle onClick events to delete
    //////////////////////////////////////////////////////////////

    public View.OnClickListener getDeleteClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askDelete().show();
            }
        };
        return listener;
    }

    private AlertDialog askDelete()
    {

        String deleteText = "";

        if(contact != null){
            deleteText = contact.getLogin() + " from your contact list ?";
        }
        else{
            deleteText = "your contact";
        }

        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                //set message, title, and icon

                .setTitle("Delete")
                .setMessage(String.format("Do you want to remove %s ", deleteText))
                .setIcon(R.drawable.icon_contact_delete)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        deleteContact();
                        dialog.dismiss();
                        goToContactListActivity();
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        return myQuittingDialogBox;
    }

    private void deleteContact(){


        final String login = PreferenceUtility.getLogin(this);

        if(login == null) {
            LoggerHelper.error("LOGIN should not be null at this point");
            return;
        }

        if(contact == null || StringUtils.isEmpty(contact.getLogin())){
            LoggerHelper.error("contact LOGIN should not be null at this point");
            return;
        }

        //remove contact
        DataPersistenceManager.deleteContact(login,contact.getLogin());

        //im remove from his contact
        DataPersistenceManager.deleteContact(contact.getLogin(),login);

        //notify that the data has changed to update the list view
    }

    private void goToContactListActivity(){
        Intent intent = new Intent(this,ManageContactActivity.class);
        startActivity(intent);
    }
}
