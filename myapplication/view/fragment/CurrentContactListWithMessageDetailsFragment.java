package fr.myapplication.dc.myapplication.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.notification.NotificationReaderListActivity;
import fr.myapplication.dc.myapplication.activity.user.LoginActivity;
import fr.myapplication.dc.myapplication.data.model.BaseMessage;
import fr.myapplication.dc.myapplication.data.model.Contact;
import fr.myapplication.dc.myapplication.data.model.VibrationMessage;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.view.adapter.CurrentContactListWithMessageDetailsAdapter;
import util.Constants;
import util.ContactListSingleton;
import util.LoggerHelper;
import util.PreferenceUtility;

/**
 * Created by chris on 09/05/2016.
 * List of contact
 * Todo: Use FirebaseListAdapter instead
 */
public class CurrentContactListWithMessageDetailsFragment extends ListFragment
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{

    ArrayList<Contact> contactList;
    ArrayList<String> contactKeyList;
    CurrentContactListWithMessageDetailsAdapter adapter;
    Map<DatabaseReference,List<ChildEventListener>> dbReferenceChildList;
    DatabaseReference ref_contacts;
    DatabaseReference ref_messages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_contact_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final String login = PreferenceUtility.getLogin(getActivity());
        LoggerHelper.debug(this.getContext(),"onActivityCreated with login=" + login);

        //prevent any crash
        if(StringUtils.isEmpty(login)){
            Intent i = new Intent(getActivity(),LoginActivity.class);
            startActivity(i);
        }
        //Data List
        contactList = new ArrayList<>();
        contactKeyList = new ArrayList<>();
        dbReferenceChildList = new HashMap<>();
        //new addUserAdapter bridge between rootView and data
        //Todo:pass glide param to other Adapters
        adapter = new CurrentContactListWithMessageDetailsAdapter(getActivity(), contactList,Glide.with(this));

        setListAdapter(adapter);

        //clear singleton data
        ContactListSingleton.getInstance().getContactList().clear();

        //listeners
        getListView().setOnItemClickListener(this);

        getListView().setOnItemLongClickListener(this);

        addContactListListener();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        LoggerHelper.debug(getClass().getName(),"onDestroy");

        List<ChildEventListener> listenerList = dbReferenceChildList.get(ref_contacts);
        for(ChildEventListener listener : listenerList){
            ref_contacts.removeEventListener(listener);
            LoggerHelper.debug(getClass().getName(),"ref_contacts.removeEventListener " + listener);
        }

        listenerList = dbReferenceChildList.get(ref_messages);
        for(ChildEventListener listener : listenerList){
            ref_messages.removeEventListener(listener);
            LoggerHelper.debug(getClass().getName(),"ref_contacts.removeEventListener " + listener);
        }
    }


    /*
    @Override
    public void onResume() {
        super.onResume();
        LoggerHelper.debug("CurrentContactListWithMessageDetailsFragment.onResume");
        for(Contact contact : contactList){
            for(VibrationMessage message : contact.getMessageList()){
                LoggerHelper.debug(String.format("contact=%s - message read=%s",contact.getLogin(), message.isRead()));
            }
        }
        //addContactListListener();
        addUserAdapter.notifyDataSetChanged();
    }
    */

    /*the main business logic of the application
    *notifications are readable/editable from a click on the user
    */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //if we have a wizz waiting to be red then we open NotificationReaderActivity
        Intent i = new Intent(getActivity().getApplicationContext(),NotificationReaderListActivity.class);

        i.putExtra(Constants.SELECTED_CONTACT,contactList.get(position).getLogin());
        i.putExtra(Constants.SELECTED_CONTACT_KEY, contactKeyList.get(position));

        startActivity(i);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return true;
    }

    private void addContactListListener(){

        final String login = PreferenceUtility.getLogin(this.getActivity());

        if(login == null) {
            LoggerHelper.error("LOGIN should not be null at this point");
            return;
        }

        //contact request
        ref_contacts = DataPersistenceManager.db.
                child(Constants.USERS_KEY).child(login).child(Constants.CONTACTS_KEY);

        ChildEventListener listener = ref_contacts.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String s) {
                LoggerHelper.info(getContext(),String.format("addContactListListener contact added successfully with dataSnapshot=%s and param s=%s",snapshot,s));
                if(snapshot.getValue() instanceof String){
                }
                else {
                    LoggerHelper.info("CurrentContactListFragment child exist with key" + snapshot.getKey());
                    Contact contact = snapshot.getValue(Contact.class);
                    if(contact != null ) {
                        if (contact.isActive()) {
                            //retrieve the messages
                            contactList.add(contact);
                            contactKeyList.add(contact.getLogin());
                            readUserMessages(login, contact);
                        }
                        ContactListSingleton.getInstance().getContactList().add(contact.getLogin());
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //Todo:Remove the listener when contact is removed, maybe it is removed automatically
                //remove contact from list
                Contact contact = dataSnapshot.getValue(Contact.class);
                if(contact != null ) {
                    contactList.remove(contact);
                    ContactListSingleton.getInstance().getContactList().remove(contact.getLogin());
                    contactKeyList.remove(contact.getLogin());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //add listener to list
        List listenerList = new ArrayList<ChildEventListener>();
        listenerList.add(listener);
        dbReferenceChildList.put(ref_contacts,listenerList);
    }

    public void readUserMessages(final String login, final Contact contact){
        //The query
        ref_messages = DataPersistenceManager.db.
                child(Constants.USERS_KEY).
                child(login).
                child(Constants.CONTACTS_KEY).
                child(contact.getLogin()).
                child(Constants.MESSAGES_KEY);

        //the Listener
        ChildEventListener listener = ref_messages.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                LoggerHelper.info(getClass().getName(),String.format(" addMessagesListener onChildAdded with dataSnapshot=%s and param s=%s",dataSnapshot,s));
                if(dataSnapshot.getValue() != null){
                    readSingleMessage(contact,dataSnapshot.getKey());
                }
                else{
                    LoggerHelper.info("readMessages.onDataChange - there are no messages");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                LoggerHelper.info(getContext(),String.format("addMessagesListener onChildChanged with dataSnapshot=%s and param s=%s",dataSnapshot,s));
                updateSingleMessage(contact,dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                LoggerHelper.info(getContext(),String.format("CurrentContactListWithMessageDetailsFragment addMessagesListener onChildRemoved with dataSnapshot=%s",dataSnapshot));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //add listener
        //add listener to list
        List listenerList = new ArrayList<>();
        listenerList.add(listener);
        dbReferenceChildList.put(ref_messages,listenerList);
    }

    public void readSingleMessage(final Contact contact, final String keyMessage){

        //The query
        DatabaseReference ref = DataPersistenceManager.db.
                child(Constants.MESSAGES_KEY).
                child(keyMessage);

        //the Listener
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LoggerHelper.info(getClass().getName(),String.format("readSingleMessage onDataChange with dataSnapshot=%s",dataSnapshot));
                final BaseMessage message;
                if(dataSnapshot.getValue() != null){
                    message = dataSnapshot.getValue(BaseMessage.class);
                    contact.addMessage(message);
                    contact.addMessageKey(keyMessage);
                    //addUserAdapter must update
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

    public void updateSingleMessage(final Contact contact, final String keyMessage){
        //The query

        DatabaseReference ref = DataPersistenceManager.db.
                child(Constants.MESSAGES_KEY).
                child(keyMessage);

        //the Listener
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LoggerHelper.info(getContext(),String.format("CurrentContactListWithMessageDetailsFragment updateSingleMessage onDataChange with dataSnapshot=%s",dataSnapshot));
                final VibrationMessage message;
                if(dataSnapshot.getValue() != null){
                    message = dataSnapshot.getValue(VibrationMessage.class);
                    int index = contact.getMessageKeyList().indexOf(keyMessage);
       /*             contact.getMessageList().set(index,message);
                    //addUserAdapter must update
                    addUserAdapter.notifyDataSetChanged();*/
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
