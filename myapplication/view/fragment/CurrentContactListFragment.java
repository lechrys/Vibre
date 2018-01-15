package fr.myapplication.dc.myapplication.view.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.data.model.Contact;
import fr.myapplication.dc.myapplication.data.model.IContact;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.view.adapter.CurrentContactListAdapter;
import util.Constants;
import util.LoggerHelper;
import util.PreferenceUtility;

/**
 * Created by chris on 09/05/2016.
 * List of contact
 * Todo: Use FirebaseListAdapter instead
 */
public class CurrentContactListFragment extends ListFragment
        implements AdapterView.OnItemClickListener {

    Short MAX_USERS_SELECTED = 5;

    ArrayList<IContact> contactList;
    ArrayList<String> contactKey;
    CurrentContactListAdapter adapter;

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
        //new addUserAdapter bridge between rootView and data
        adapter = new CurrentContactListAdapter(getActivity(), contactList, Glide.with(this));
        setListAdapter(adapter);

        //list init
        initList();

        //listeners
        getListView().setOnItemClickListener(this);
        addContactListListener();

    }

    public void initList(){

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            private int nr = 0;

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                adapter.clearSelection();
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub
                //addUserAdapter.clearSelection();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                nr = 0;
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.contextual_menu, menu);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // TODO Auto-generated method stub
                switch (item.getItemId()) {
                    case R.id.item_send:
                        LoggerHelper.info("item clicked");
                        if(nr > 0){
                            adapter.sendMessages();
                        }
                        nr = 0;
                        mode.finish();
                }
                return true;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                // TODO Auto-generated method stub
                if (checked) {
                    nr++;
                    adapter.setNewSelection(position, checked);
                } else {
                    nr--;
                    adapter.removeSelection(position);
                }
                mode.setTitle(nr + " selected");
            }
        });
    }

    /*the main business logic of the application
    *notifications are readable/editable from a click on the user
    */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LoggerHelper.info("onItemLongClick");
        getListView().setItemChecked(position, !adapter.isPositionChecked(position));
        //view.setSelected(true);
    }

    private void addContactListListener(){

        final String login = PreferenceUtility.getLogin(this.getActivity());

        if(login == null) {
            LoggerHelper.error("LOGIN should not be null at this point");
            return;
        }

        DatabaseReference ref = DataPersistenceManager.db.
                child(Constants.USERS_KEY).child(login).child(Constants.CONTACTS_KEY);

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String s) {
                LoggerHelper.info("CurrentContactListFragment contact added successfully with dataSnapshot="+snapshot);
                if(snapshot.getValue() instanceof String){
                    LoggerHelper.info("instance of string with key " + snapshot.getKey());
                    LoggerHelper.info("instance of string with val " + snapshot.getValue());
                }
                else {
                    LoggerHelper.info("CurrentContactListFragment child exist with key" + snapshot.getKey());
                    Contact contact = snapshot.getValue(Contact.class);
                    if(contact != null && contact.isActive()){
                        LoggerHelper.info(getClass().getName(),"contact added with login=" + contact.getLogin());
                        contactList.add(contact);
                        contactKey.add(contact.getLogin());
                    }
                    else{
                        LoggerHelper.info("contact not active" + contact.getLogin());
                    }
                }
                //addUserAdapter must update
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String s) {
                LoggerHelper.info("onChildChanged");
                if( ! (snapshot.getValue() instanceof String)) {
                    Contact contact = snapshot.getValue(Contact.class);
                    if(contact != null){
                        LoggerHelper.info("contact added with login=" + contact.getLogin());
                        if( ! contactKey.contains(contact.getLogin())){
                            contactList.add(contact);
                            contactKey.add(contact.getLogin());
                            //addUserAdapter must update
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                LoggerHelper.info("onChildRemoved");
                LoggerHelper.info("CurrentContactListFragment contact added successfully with dataSnapshot="+snapshot);
                if(snapshot.getValue() instanceof String){
                    LoggerHelper.info("instance of string with key " + snapshot.getKey());
                    LoggerHelper.info("instance of string with val " + snapshot.getValue());
                }
                else {
                    LoggerHelper.info("CurrentContactListFragment child exist with key" + snapshot.getKey());
                    Contact contact = snapshot.getValue(Contact.class);
                    LoggerHelper.info("contact added with login=" + contact.getLogin());
                    contactList.remove(contact);
                    contactKey.remove(contact.getLogin());
                }
                //addUserAdapter must update
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
