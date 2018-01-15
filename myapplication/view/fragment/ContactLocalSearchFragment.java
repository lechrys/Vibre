package fr.myapplication.dc.myapplication.view.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.data.adapter.SearchUser;
import fr.myapplication.dc.myapplication.data.adapter.SearchUserType;
import fr.myapplication.dc.myapplication.data.model.User;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.view.adapter.ContactLocalSearchAddUserListAdapter;
import fr.myapplication.dc.myapplication.view.adapter.ContactLocalSearchInviteUserListAdapter;
import fr.myapplication.dc.myapplication.view.adapter.ContactLocalSearchListAdapter;
import util.Constants;
import util.ContactListSingleton;
import util.LoggerHelper;
import util.PreferenceUtility;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * Created by Crono on 04/02/17.
 */

public class ContactLocalSearchFragment extends Fragment implements SearchView.OnQueryTextListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    //Views
    private LinearLayout parentLayout;

    private List<SearchUser> addUsers;
    private List<SearchUser> inviteUsers;
    private List<SearchUser> mergedUsers;

    RelativeLayout loadingLayout;

    // This is the Adapter being used to display the list's data.
    ContactLocalSearchListAdapter contactLocalSearchListAdapter;

    // If non-null, this is the current filter the user has provided.
    String mCurFilter;

    //misc datas
    String myLogin;
    int processed_child = 0;
    int potential_children;
    boolean finished_process;

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentLayout = (LinearLayout) inflater.inflate(R.layout.fragment_search_local_contact, container, false);
        return parentLayout;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        //setEmptyText("No phone numbers");

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        myLogin = PreferenceUtility.getLogin(getActivity());

        initData();

        //init views
        setViews();

        initLoader();

    }

    private boolean mayRequestContacts() {
        LoggerHelper.info(getClass().getName(),"IN mayRequestContacts");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            LoggerHelper.info(getClass().getName(),"Build.VERSION.SDK_INT < Build.VERSION_CODES.M");
            return true;
        }
        if (getActivity().checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            LoggerHelper.info(getClass().getName(),"checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED");
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            LoggerHelper.info(getClass().getName(),"shouldShowRequestPermissionRationale(READ_CONTACTS)");
            Snackbar.make(parentLayout, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            LoggerHelper.info(getClass().getName(),"requestPermissions");
            Snackbar.make(parentLayout, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }

        return false;

    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        LoggerHelper.debug(getClass().getName(),"IN onRequestPermissionsResult");
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 initLoader();
                //init
            }
        }
    }

    private void initLoader() {
        LoggerHelper.info("IN initLoader");
        if ( ! mayRequestContacts()) {
            return;
        }
        getLoaderManager().initLoader(0, null, this);
    }

    private void initData(){
        addUsers = new ArrayList<>();
        inviteUsers = new ArrayList<>();
        mergedUsers = new ArrayList<>();
    }

    private void setViews(){

        loadingLayout = (RelativeLayout) parentLayout.findViewById(R.id.loadingPanel);
        contactLocalSearchListAdapter = new ContactLocalSearchListAdapter(getContext(),mergedUsers, Glide.with(this));

        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(contactLocalSearchListAdapter);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LoggerHelper.debug(getClass().getName(),"IN onCreateOptionsMenu");
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

    @Override public boolean onQueryTextSubmit(String query) {
        LoggerHelper.info(getClass().getName(),"IN onQueryTextSubmit");
        // Don't care about this.
        return true;
    }

    // These are the Contacts rows that we will retrieve.
    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
            ContactsContract.Contacts._ID,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
    };

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LoggerHelper.debug(getClass().getName(),"IN onCreateLoader");
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        Uri baseUri;
        if (mCurFilter != null) {
            baseUri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
                    Uri.encode(mCurFilter));
        } else {
            baseUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        }

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        String select = "(("
                + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                + ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1) AND ("
                + ContactsContract.Contacts.DISPLAY_NAME + " != ''))"
                ;

        return new CursorLoader(
                getActivity(),
                baseUri,
                CONTACTS_SUMMARY_PROJECTION,
                select,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        LoggerHelper.debug(getClass().getName(),"IN onLoadFinished");

        data.moveToFirst();

        ArrayList<SearchUser> contactList = normalizeContactsData(data);

        //clear the list
        addUsers.clear();
        inviteUsers.clear();

        // load is finished now we can check if those contact exist in DB based on their phone number
        checkContactExistInDB(contactList);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        LoggerHelper.debug(getClass().getName(),"IN onLoaderReset");
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        // mAdapter.swapCursor(null);
        // addUserAdapter.clear();
        //.clear();
        mergedUsers.clear();
        finished_process = false;
        //clear the list
    }

    //////////////////////////////////////////////
    // Firebase Listeners
    //////////////////////////////////////////////

    @Override
    public boolean onQueryTextChange(String text) {

        LoggerHelper.debug(getClass().getName(),"IN onQueryTextChange");
        mCurFilter = !TextUtils.isEmpty(text) ? text : null;
        getLoaderManager().restartLoader(0, null, this);
        addUsers.clear();
        inviteUsers.clear();
        mergedUsers.clear();
        finished_process = false;
        return true;
    }

    /*
        normalizeContactsData
        add country code
    */
    private ArrayList<SearchUser> normalizeContactsData(Cursor cursor){

        ColumnIndexCache cache = new ColumnIndexCache();

        LoggerHelper.debug(getClass().getName(),"IN normalizeContactsData");
        ArrayList<SearchUser> contactList = new ArrayList<>();
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        TelephonyManager tm = (TelephonyManager)this.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String countryCodeValue = tm.getNetworkCountryIso().toUpperCase();
        int coutry_code = phoneNumberUtil.getCountryCodeForRegion(countryCodeValue);
        LoggerHelper.info("coutry_code = " + coutry_code);

        for(int i = 0; i < cursor.getCount() ; i ++){

            //phone
            final String E164_format_phone = cursor.getString(cache.getColumnIndex(cursor,ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));

          //  LoggerHelper.info("phone_tmp = " + E164_format_phone + " AND name " + cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));

            //remove spaces and hidder chars
            if( ! StringUtils.isEmpty(E164_format_phone)) {

                LoggerHelper.info("formatted_number = " + E164_format_phone);

                //login
                final String name_on_phone = cursor.getString(cache.getColumnIndex(cursor,ContactsContract.Contacts.DISPLAY_NAME));

                if ( ! StringUtils.isEmpty(name_on_phone)) {

                    //creting user object
                    SearchUser user = new SearchUser();

                    //the login
                    user.setName_on_phone(name_on_phone);
                    user.setLogin(name_on_phone);
                    user.setPhone(E164_format_phone);

                    LoggerHelper.info(getClass().getName(), "phone is " + E164_format_phone);

                    LoggerHelper.info(getClass(),"user is " + user.toString());

                    if ( ! contactList.contains(user)) {
                        //add only if user do not exists
                        contactList.add(user);
                        LoggerHelper.info(String.format("user %s do not exists with phoneNumber %s", user.getPhone(), E164_format_phone));
                    } else {
                        LoggerHelper.info(String.format("user %s already exists with phoneNumber %s ", user.getPhone(), E164_format_phone));
                    }
                }

            }
            //next phone contact
            cursor.moveToNext();
        }

        cache.clear();

        return contactList;
    }

    private void checkContactExistInDB(final ArrayList<SearchUser> userList) {

        processed_child = 0;
        potential_children = userList.size();

        LoggerHelper.info(this.getClass().getName(),"IN checkContactExistInDB with potential_children = " + potential_children);

        DatabaseReference db = DataPersistenceManager.db.child(Constants.PHONE_KEY);

        //Todo : regExp on phone number to ensure good format off the pgone

        for (int i = 0; i < potential_children; i++) {

            //remove spaces and hidder chars
            final SearchUser current_user = userList.get(i);

            final DatabaseReference numberRef = db.child(current_user.getPhone());
            LoggerHelper.info("numberkey = " + numberRef);

            if(numberRef != null){
                numberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            LoggerHelper.info(this.getClass().getName(),
                                    String.format(Locale.getDefault(),"IN checkContactExistInDB onDataChange dataSnapshot exists for phone %s and user %s ",current_user.getPhone(),current_user.getLogin()));
                            Object child = dataSnapshot.getValue();
                            if(child != null){
                                LoggerHelper.info(this.getClass().getName(),"IN checkContactExistInDB " +
                                        "searching phone number = " + current_user.getPhone()
                                        + " login = " + current_user.getLogin()
                                        + " current_user.getName_on_phone() = " + current_user.getName_on_phone());

                                final String login = child.toString();
                                LoggerHelper.info(this.getClass().getName(),"IN checkContactExistInDB contact phone with login = " + login + " and phone = " + numberRef);
                                getUserBasedOnPhone(login,current_user.getName_on_phone());
                            }
                        }
                        else{

                            LoggerHelper.info(" ! dataSnapshot.exists()");

                            current_user.setTypeUser(SearchUserType.INVITE);
                            inviteUsers.add(current_user);

                            processed_child ++;

                            if(processed_child >= potential_children && ! finished_process){
                                finished_process = true;

                                if( ! addUsers.isEmpty()){
                                    SearchUser header = new SearchUser();
                                    header.setTypeUser(SearchUserType.ADD_HEADER);
                                    addUsers.add(0,header);
                                    mergedUsers.addAll(addUsers);
                                }
                                if( ! inviteUsers.isEmpty()){
                                    SearchUser header = new SearchUser();
                                    header.setTypeUser(SearchUserType.INVITE_HEADER);
                                    inviteUsers.add(0,header);
                                    mergedUsers.addAll(inviteUsers);
                                }

                                loadingLayout.setVisibility(View.GONE);
                                contactLocalSearchListAdapter.notifyDataSetChanged();
//                                inviteUserAdapter.notifyDataSetChanged();
                            }

                            LoggerHelper.info(this.getClass().getName(),"IN checkContactExistInDB onDataChange dataSnapshot do no exists with login = " + current_user.getLogin());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        LoggerHelper.info("onCancelled " + databaseError);
//                        processed_child ++ ;
                    }
                });
            }
            else{
                processed_child ++ ;
                LoggerHelper.info(this.getClass().getName(),"IN checkContactExistInDB phone number doesnt exist = " + current_user.getPhone());
            }
        }
    }

    private void getUserBasedOnPhone(final String login, final String name_on_phone){
        LoggerHelper.info(this.getClass().getName(),String.format("IN getUserBasedOnPhone with login %s, name_on_phone = %s",login, name_on_phone));
        DatabaseReference db = DataPersistenceManager.db.child(Constants.USERS_KEY).child(login);
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot child) {
                    LoggerHelper.info(getClass(),"getUserBasedOnPhone child = " + child);
                    //add addUsers excepted the current user
                    User user = child.getValue(User.class);
                    if( user != null && ! StringUtils.isEmpty(user.getLogin()) && ! user.getLogin().equals(myLogin)){
                        LoggerHelper.info(this.getClass().getName(),"IN getUserBasedOnPhone user phone = " + user.getPhone());
                        if( ! ContactListSingleton.getInstance().getContactList().contains(user.getLogin())){
                            SearchUser searchUser = new SearchUser();
                            searchUser.setLogin(user.getLogin());
                            searchUser.setPhone(user.getPhone());
                            searchUser.setName_on_phone(name_on_phone);
                            searchUser.setTypeUser(SearchUserType.ADD);
                            addUsers.add(searchUser);
                        }
                    }

                    processed_child ++;

                    LoggerHelper.debug(this.getClass().getName(),"list is length= " + addUsers.size());
                    //redraw the list
                    if(processed_child >= potential_children && ! finished_process){
                        LoggerHelper.info(this.getClass().getName(),String.format("processed_child %d >= potential_children  %d",processed_child,potential_children));

                        finished_process = true;

                        if( ! addUsers.isEmpty()){
                            SearchUser header = new SearchUser();
                            header.setTypeUser(SearchUserType.ADD_HEADER);
                            addUsers.add(0,header);
                            mergedUsers.addAll(addUsers);
                        }

                        if( ! inviteUsers.isEmpty()){
                            SearchUser header = new SearchUser();
                            header.setTypeUser(SearchUserType.INVITE_HEADER);
                            inviteUsers.add(0,header);
                            mergedUsers.addAll(inviteUsers);
                        }

                        contactLocalSearchListAdapter.notifyDataSetChanged();
                        loadingLayout.setVisibility(View.GONE);
                    }
                    else{
                        LoggerHelper.info(this.getClass().getName(),String.format("processed_child %d <= potential_children  %d",processed_child,potential_children));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //Todo: To handle ?
                   // processed_child ++ ;
                }
            });

    }

    public class ColumnIndexCache {
        private ArrayMap<String, Integer> mMap = new ArrayMap<>();

        public int getColumnIndex(Cursor cursor, String columnName) {
            if (!mMap.containsKey(columnName))
                mMap.put(columnName, cursor.getColumnIndex(columnName));
            return mMap.get(columnName);
        }

        public void clear() {
            mMap.clear();
        }
    }
}
