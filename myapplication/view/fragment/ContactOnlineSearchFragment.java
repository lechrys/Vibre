package fr.myapplication.dc.myapplication.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.BasicActivity;
import fr.myapplication.dc.myapplication.data.model.IUser;
import fr.myapplication.dc.myapplication.data.model.User;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.view.adapter.ContactOnlineSearchListAdapter;
import util.Constants;
import util.ContactListSingleton;
import util.LoggerHelper;
import util.PreferenceUtility;

public class ContactOnlineSearchFragment extends Fragment implements BasicActivity,
        SearchView.OnQueryTextListener {

    private ListView usersListView;
    private LinearLayout parentLayout;
    private List<IUser> users;
    private ContactOnlineSearchListAdapter adapter;
    private String myLogin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentLayout = (LinearLayout) inflater.inflate(R.layout.fragment_search_online_contact, container, false);
        return parentLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //for the search bar
        setHasOptionsMenu(true);

        //setup
        myLogin = PreferenceUtility.getLogin(getActivity());
        users = new ArrayList<>();
        adapter = new ContactOnlineSearchListAdapter(getContext(), users, Glide.with(this));

        for(String contact : ContactListSingleton.getInstance().getContactList()){
            LoggerHelper.info("ContactListSingleton contact=" + contact);
        }

        //Views setup
        setViews();

        //Listeners setup
        setButtonsListeners();
    }


    @Override
    public void setViews() {
        usersListView = (ListView) parentLayout.findViewById(R.id.searchUserList);
        usersListView.setAdapter(adapter);
    }

    @Override
    public void setButtonsListeners(){

    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Place an action bar item for searching.
        //MenuItem
        MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        //search view
        SearchView sv = new SearchView(getActivity());
        sv.setOnQueryTextListener(this);
        item.setActionView(sv);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String text) {

        if (!text.isEmpty()) {

            //prepare query
            DatabaseReference db = DataPersistenceManager.db.child(Constants.USERS_KEY);
            Query usersQuery = db.orderByKey().startAt(text).endAt(text + "\uf8ff").
                    limitToFirst(Constants.CONTACT_SEARCH_ONLINE_LIMIT);

            //listen
            usersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //clear the users list
                    users.clear();
                    //add users excepted the current user
                    for (DataSnapshot child : dataSnapshot.getChildren()) {

                        LoggerHelper.info("onQueryTextChange with child=" + child.getKey());

                        User user = child.getValue(User.class);

                        if( user != null && ! StringUtils.isEmpty(user.getLogin()) && ! user.getLogin().equals(myLogin)){

                            if( ! ContactListSingleton.getInstance().getContactList().contains(user.getLogin())){
                                LoggerHelper.info("ContactListSingleton.getInstance().getMessageList() do not contains " + user.getLogin());
                                user = User.getUserLight(user);
                                users.add(user);
                            }
                            else {
                                LoggerHelper.info("ContactListSingleton.getInstance().getMessageList().contains " + user.getLogin());
                            }
                            LoggerHelper.debug("list is length= " + users.size());
                        }
                    }
                    //redraw the list
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //Todo: To handle ?
                }
            });
        }
        else{
            //when no text we clear the list
            LoggerHelper.info("clear the lists");
            users.clear();
            adapter.notifyDataSetChanged();
        }

        return true;
    }
}
