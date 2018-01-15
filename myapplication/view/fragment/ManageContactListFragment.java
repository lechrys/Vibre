package fr.myapplication.dc.myapplication.view.fragment;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.data.model.Contact;
import fr.myapplication.dc.myapplication.data.model.User;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.view.adapter.ManageContactListAdapter;
import util.ConnectionHelper;
import util.Constants;
import util.ContactListSingleton;
import util.LoggerHelper;
import util.PreferenceUtility;

/**
 * Created by chris on 09/05/2016.
 * List of contact
 * Todo: Use FirebaseListAdapter instead
 */
public class ManageContactListFragment extends ListFragment {

    ArrayList<Contact> contactList;
    ArrayList<String> contactKey;
    ManageContactListAdapter adapter;
    ChildEventListener childEventListener;

    private HashMap<DatabaseReference, ChildEventListener> childEventListenerHashMap = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LoggerHelper.debug("CurrentContactListFragment.onCreateView");
        return inflater.inflate(R.layout.fragment_current_contact_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LoggerHelper.info("CurrentContactListFragment.onActivityCreated");
        //Data List
        contactList = new ArrayList<>();
        contactKey = new ArrayList<>();
        //new adapter bridge between rootView and data
        adapter = new ManageContactListAdapter(getActivity(), contactList, Glide.with(this));

        setListAdapter(adapter);

        addContactListListener();
    }

    //will need to add some date on the message

    private void addContactListListener(){

        LoggerHelper.error("IN addContactListListener");

        final String login = PreferenceUtility.getLogin(this.getActivity());

        if(login == null) {
            LoggerHelper.error("LOGIN should not be null at this point");
            return;
        }

        if(childEventListener == null) {

            DatabaseReference ref = DataPersistenceManager.db.
                    child(Constants.USERS_KEY).child(login).child(Constants.CONTACTS_KEY);

            childEventListener = ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String s) {
                    LoggerHelper.info("CurrentContactListFragment contact added successfully with dataSnapshot=" + snapshot);
                    if (snapshot.getValue() instanceof String) {
                        LoggerHelper.info("instance of string with key " + snapshot.getKey());
                        LoggerHelper.info("instance of string with val " + snapshot.getValue());
                    } else {
                        LoggerHelper.info("CurrentContactListFragment child exist with key" + snapshot.getKey());
                        Contact contact = snapshot.getValue(Contact.class);
                        if (contact != null) {
                            if (contact.isActive() && !StringUtils.isEmpty(contact.getLogin())) {
                                //now check if the user still exist in DB and didnt delete his account
                                LoggerHelper.info("contact added = " + contact);
                                LoggerHelper.info("contact messages = " + snapshot.child(Constants.MESSAGES_KEY).getChildrenCount());
                                checkUserExist(contact);
                            } else {
                                LoggerHelper.info("contact not active" + contact.getLogin());
                            }
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    LoggerHelper.info("onChildChanged");
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    LoggerHelper.info("onChildRemoved dataSnapshot = " + dataSnapshot);
                    Contact contact = dataSnapshot.getValue(Contact.class);

                    if (contact != null && !StringUtils.isEmpty(contact.getLogin())) {
                        contactList.remove(contact);
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

            childEventListenerHashMap.put(ref, childEventListener);
        }
    }

    //check user exists

    private void checkUserExist(final Contact searchedContact){
        //clear the users list

        //prepare query
        DatabaseReference db = DataPersistenceManager.db.child(Constants.USERS_KEY).child(searchedContact.getLogin());

        //listen
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //clear the users list
                //add users excepted the current user

                LoggerHelper.info(getClass().getName(),"checkUserExist with child=" + dataSnapshot.getKey());

                User user = dataSnapshot.getValue(User.class);

                if( user != null && ! StringUtils.isEmpty(user.getLogin())){
                    //It is possible that the user was deleted then recreated
                    //since we do not remove the user from our contacts when deleted
                    // we need to check that his creation date isnt occuring after our friendship date
                    if( ! (user.getCreationDateLong() > searchedContact.getCreationDateLong())){
                        LoggerHelper.info(getClass().getName(),"user exist with login=" + user.getLogin());
                        contactList.add(searchedContact);
                        contactKey.add(searchedContact.getLogin());
                        //addUserAdapter must update
                        // Todo : count the number of contact before in order to reduce the calls to notifyDataSetChanged
                        adapter.notifyDataSetChanged();
                    }
                    else{
                        LoggerHelper.info(String.format("contact %s has deleted his account ", user.getLogin()));
                    }

                }
                else{
                    LoggerHelper.info(getClass().getName(),"contact do not exist anymore " + searchedContact.getLogin());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Todo: To handle ?
            }
        });
    }


/*
    public Contact contactByNickname(String nickname) {
        ContentResolver content = this.getContext().getContentResolver();
        String[] projection = { ContactsContract.Data.MIMETYPE,
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Email.ADDRESS };

        // Defines the selection clause
        String selection = ContactsContract.Data.RAW_CONTACT_ID + "=?";

        // Defines the sort order
        String sortOrder = ContactsContract.Data.LOOKUP_KEY;

        String[] args = { id+"" };

        Cursor cursor = content.query(ContactsContract.Data.CONTENT_URI, projection, selection,
                args, sortOrder);


    }
*/

    /////////////////////////////////////////////////////////////
    // LifeCycle
    /////////////////////////////////////////////////////////////

    @Override
    public void onResume(){
        super.onResume();
        LoggerHelper.warn(this.getClass().toString(),"IN onResume");

    }

    @Override
    public void onPause(){
        super.onPause();
        LoggerHelper.warn(this.getClass().toString(),"IN onPause");
    }

    @Override
    public void onStop(){
        super.onStop();
        LoggerHelper.warn(this.getClass().toString(),"IN onStop");

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //Todo : remove Firebase listeners
        ConnectionHelper.removeChildEventListener(childEventListenerHashMap);
        childEventListener = null;
        contactList.clear();
    }
}
