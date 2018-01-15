package fr.myapplication.dc.myapplication.view.fragment;

import android.app.ListFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import fr.myapplication.dc.myapplication.activity.notification.NotificationReaderActivity;
import fr.myapplication.dc.myapplication.data.model.VibrationMessage;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.view.adapter.MessageListAdapter;
import util.Constants;
import util.LoggerHelper;
import util.PreferenceUtility;

/**
 * Created by chris on 09/05/2016.
 * List of contact
 * Todo: Use FirebaseListAdapter instead
 */
public class MessageLisFragment extends ListFragment
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    List<VibrationMessage> messageList;
    List<String> messageKey;
    MessageListAdapter adapter;
    String selected_contact;
    DatabaseReference ref;
    List<ChildEventListener> childList;

    /*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lis, container, false);
    }
    */

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LoggerHelper.debug("MessageLisFragment.onActivityCreated");

        selected_contact = this.getActivity().getIntent().getExtras().
                getString(Constants.SELECTED_CONTACT_KEY);

        //setup the tool bar with icon of the selected user
        //((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        /*ImageView avatarToolbarImageView = (ImageView) getActivity().findViewById(R.id.avatarToolbar);
        if(avatarToolbarImageView != null){
            LoggerHelper.info("avatarToolbarImageView exists");
        }
        */
        //ImageUtil.getAvatar(getActivity(),null,selected_contact,avatarToolbarImageView);
        //((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.icon_default_user
        //);

        //Data List
        messageList = new ArrayList<>();
        messageKey = new ArrayList<>();
        childList = new ArrayList<>();
        //new addUserAdapter bridge between rootView and data
        adapter = new MessageListAdapter(getActivity(), messageList);

        setListAdapter(adapter);

        //listeners
        getListView().setOnItemClickListener(this);

        addMessageListListener();
    }

    /*the main business logic of the application
    *notifications are readable/editable from a click on the user
    */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //mark the message as read
        SharedPreferences preferences = PreferenceUtility.getPreference(getActivity());
        DataPersistenceManager.markMessageAsRed(preferences,messageKey.get(position),selected_contact);
        messageList.get(position).setRead(true);
        adapter.notifyDataSetChanged();

        //if we have a wizz waiting to be red then we open NotificationReaderActivity
        Intent i = new Intent(getActivity().getApplicationContext(),NotificationReaderActivity.class);
        //The selected message
        i.putExtra(Constants.SELECTED_MESSAGE, messageList.get(position));
        //start activity
        startActivity(i);
    }

    @Override
    public void onResume(){
        super.onResume();
        LoggerHelper.debug("MessageLisFragment.onResume");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        LoggerHelper.debug(getClass().getName(),"MessageLisFragment.onDestroy");
        for(ChildEventListener listener : childList){
            ref.removeEventListener(listener);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    public void addMessageListListener(){

        //login
        final String login = PreferenceUtility.getLogin(this.getActivity());

        //selected
        Bundle extras = this.getActivity().getIntent().getExtras();
        String selected = (String) extras.get(Constants.SELECTED_CONTACT);

        if(StringUtils.isEmpty(login)){
            LoggerHelper.error("login shouldnt be null at this point");
            return;
        }

        if(StringUtils.isEmpty(selected)){
            LoggerHelper.error("selected contact shouldnt be null at this point");
            return;
        }

        //The query
         ref = DataPersistenceManager.db.
                child(Constants.USERS_KEY).
                child(login).
                child(Constants.CONTACTS_KEY).
                child(selected).
                child(Constants.MESSAGES_KEY);

        //the Listener
        ChildEventListener listener = ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                LoggerHelper.info(String.format("MessageLisFragment addMessageListListener onChildAdded with s=%s - dataSnapshot=%s",s,dataSnapshot));
                if(dataSnapshot.getValue() != null){
                    //get the message
                    readSingleMessage(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //listener is saved to me removed later on
        childList.add(listener);
    }

    public void readSingleMessage(final String keyMessage){

        //The query
        DatabaseReference ref = DataPersistenceManager.db.
                child(Constants.MESSAGES_KEY).
                child(keyMessage);

        //the Listener
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LoggerHelper.info("MessageLisFragment addMessageListListener readSingleMessage.onDataChange dataSnapshot="+dataSnapshot.getValue());
                final VibrationMessage message;
                if(dataSnapshot.getValue() != null){
                    //Todo : gestion des exceptions la ligne ci dessous peut faire planter l'appli
                    message = dataSnapshot.getValue(VibrationMessage.class);
                    //add message to list
                    messageList.add(message);
                    messageKey.add(keyMessage);
                    adapter.notifyDataSetChanged();
                }
                else{
                    LoggerHelper.info("message with key " + keyMessage+ " doesnt exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
