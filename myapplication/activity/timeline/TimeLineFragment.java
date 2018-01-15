package fr.myapplication.dc.myapplication.activity.timeline;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.data.helper.ContactMessageCounter;
import fr.myapplication.dc.myapplication.data.model.BaseMessage;
import fr.myapplication.dc.myapplication.data.model.Contact;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.data.timeline.TimeLine;
import fr.myapplication.dc.myapplication.gesture.TimeLineGlobalListener;
import fr.myapplication.dc.myapplication.view.adapter.TimeLineAdapter;
import util.ConnectionHelper;
import util.Constants;
import util.ContactListSingleton;
import util.LoggerHelper;
import util.PreferenceUtility;
import util.ServerTimeSyncer;

/**
 * TimeLineFragment is the graphical representation of the TimeLine Object
 */

public class TimeLineFragment extends Fragment implements ITimeLineInfo {

    LinearLayout layout;

    ListView timeLineListView;
    TimeLineAdapter adapter;
    ArrayList<Contact> contactList;
    ArrayList<String> contactKeyList;

    //timer related
    TimeLine timeLine;
    RelativeLayout loadingLayout;

    //Firebase related
    DatabaseReference ref_contacts_load;
    DatabaseReference ref_messages_load;

    DatabaseReference ref_contacts;
    DatabaseReference ref_messages;

    Map<DatabaseReference,ChildEventListener> dbReferenceChildList;

    //datas
    boolean firstMessagesLoadOccured = false;
    int contact_read = 0;
    long total_contact_counter = 0 ;

    //Handler
    Handler handler;
    Handler handlerTest;
    HandlerThread mHandlerThread;
    Runnable scroll_runnable;

    //Time Line attributes

    int currentPosition = TimeLineAdapter.MAX_ROWS / 10 ;

    //position may have change in
    Integer saved_current_pos = currentPosition;

    //treshhold from the current position
    TimeLineGlobalListener timeLineGlobalListener;


    private boolean userScroll ;
    private boolean userTouch ;

    ////////////////////////////////////////////////////////////////////////////////////////////
    // INIT
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        LoggerHelper.info(this.getClass().toString(),"IN onCreate");

        initData();

        initHandler();

        loadContactsMessages();

        addContactMessagesListener();

        forceFirstMessagesLoadOccured();

//        initGlobalTimer();

        //miscTest();
/*
        postDelayedTest();

        ConnectionHelper.getFirebaseTimeStamp();*/

//        initHandlerTest();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LoggerHelper.warn(this.getClass().toString(),"IN onCreateView");

        if(layout == null){
            layout = (LinearLayout)  inflater.inflate(R.layout.activity_timeline,container,false);
        }

        setViews();

        initTimeLineListListeners();

        //setNotifyChangeListener();

//        initHandler();

        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LoggerHelper.warn(this.getClass().getName(),"IN onActivityCreated");
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        LoggerHelper.warn(this.getClass().toString(),"IN onSaveInstanceState");
       // bundle.add
    }

    public void initData(){
        // data initList
        contactList = new ArrayList<>();
        contactKeyList = new ArrayList<>();
        dbReferenceChildList = new HashMap<>();

        //timeLine init
        initTimeLine();
    }

    public void setViews(){

        LoggerHelper.warn(this.getActivity(),"IN setViews");

        timeLineListView = (ListView) layout.findViewById(R.id.timeline_list);
        //timeLineListView.setFadingEdgeLength(100);

        adapter.setListView(timeLineListView);
        //assign adapter
        timeLineListView.setAdapter(adapter);

        //almost infinite list view - we are in the middle of the list in order to scroll up and down
        timeLineListView.setSelection(currentPosition);

        //loading layout

        loadingLayout = (RelativeLayout) layout.findViewById(R.id.loadingPanel);
        loadingLayout.setVisibility(View.VISIBLE);

 /*       timeLineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TimeLine.TimeLineData data = (TimeLine.TimeLineData) parent.getItemAtPosition(position);
                Calendar c = data.getTime().getCalendar();
                LoggerHelper.error(String.format("########### row clicked = %d ####### time = %s",position ,c.getTime()));
            }
        });
*/
        //The toolbar
/*        final Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);*/
    }

    private void miscTest(){
        LoggerHelper.info(getClass(),"TimeZone infos = " + TimeZone.getDefault());
    }

    public void progressDialogHandler(){
    }

    public void initTimeLine(){
        LoggerHelper.info(this.getActivity(),"IN initTimeLine");
        //initList TimeLine
        timeLine = new TimeLine();

        //initList adapter
        adapter = new TimeLineAdapter(this.getActivity(),timeLine.getTimeLineDatas(), Glide.with(this),this);
        timeLine.setAdapter(adapter);
    }


    public void initTimeLineListListeners() {
        timeLineGlobalListener = new TimeLineGlobalListener(this);
    }

    /////////////////////////////////////////////////////////////
    // TimeLine lifecycle
    /////////////////////////////////////////////////////////////

    public void initHandler(){

        handler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                LoggerHelper.error(getClass(),"initHandler handleMessage received s" + msg);
            }
        };

/*        Intent intent = new Intent(this.getActivity(), ServiceTest.class);
        getActivity().startService(intent);*/
    }


/*
    public void initHandlerTest(){
        LoggerHelper.info("IN initHandler");
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        handlerTest = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                LoggerHelper.info(getClass().getName(),"initHandlerTest handleMessage " + msg);
            }
        };
        handlerTest.postDelayed(new Runnable() {
            @Override
            public void run() {
                LoggerHelper.info(getClass().getName(),"initHandlerTest RUNNABLE execution");
                handler.postDelayed(this,timer_delay);
            }
        },timer_delay);
    }
*/


    //will force loading in case of message inconsistency
    private void forceFirstMessagesLoadOccured(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LoggerHelper.info("IN forceFirstMessagesLoadOccured");
                if( ! firstMessagesLoadOccured){
                    firstMessagesLoadOccured = true;
                    adapter.notifyDataSetChanged();
                }
                loadingLayout.setVisibility(View.GONE);
            }
        },3000);
    }

    private void setNotifyChangeListener(){
        if(timeLineListView != null) {
            timeLineListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    //    timeLineListView.removeOnLayoutChangeListener(this);
                    LoggerHelper.info(getClass(), "timeLineListView got updated");
                    if (firstMessagesLoadOccured) {
                        //  adapter.animateMessageReception(currentPosition);
                    }
                }
            });
        }
    }

    /////////////////////////////////////////////////////////////
    // Read messages
    /////////////////////////////////////////////////////////////

    private void loadContactsMessages(){

        final String login = PreferenceUtility.getLogin(this.getContext());

        if(login == null) {
            LoggerHelper.error(getClass().getName(),"LOGIN should not be null at this point");
            return;
        }

        //contact request
        ref_contacts_load = DataPersistenceManager.db.
                child(Constants.USERS_KEY).
                child(login).
                child(Constants.CONTACTS_KEY);

        // Todo : retrieve data by date

        // exemple ref.child("meetups").queryOrderedByChild("endDate").queryStartingAtValue(currentDate).queryEndingAtValue(currentDate).observeEventType(.ChildAdded, withBlock: { (snapshot) -> Void in
        // that means adding date param to the message
        // future data : -KnaDn8JgrPdfnsN2FMV: date instead of -KnaDn8JgrPdfnsN2FMV: true

        ref_contacts_load.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                LoggerHelper.info(getClass().getName(),"snapshot = " + snapshot.getValue());
                LoggerHelper.info(getClass().getName(),"snapshot.getChildrenCount() = " + snapshot.getChildrenCount());

                //We read all contacts and their messages not older than 24 hours.
                total_contact_counter = snapshot.getChildrenCount();

                LoggerHelper.info(getClass().getName(),"total_contact_counter = " + total_contact_counter);

                if(total_contact_counter == 0 ){
                    firstMessagesLoadOccured = true;
                }
                else {
                    for (DataSnapshot child : snapshot.getChildren()) {

                        Contact contact = child.getValue(Contact.class);

                        if (contact != null && contact.isActive() && ! StringUtils.isEmpty(contact.getLogin())) {
                            LoggerHelper.info(getClass().toString(), "contact.isActive() " + contact.getLogin());

                            contactKeyList.add(contact.getLogin());

                            readUserMessagesOnce(login, contact);

                            ContactListSingleton.getInstance().getContactList().add(contact.getLogin());

                            LoggerHelper.info(getClass().getName(), "readUserMessagesOnce with counter = " + contact_read);

                        } else {
                            //contact is not active or inconsistant it doesnt count anymore

                            total_contact_counter--;

                            LoggerHelper.info(getClass().getName(), "total_contact_counter -- = " + total_contact_counter);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //////////////////////////////////////////////////////////////
    /////////////// Read messages once to load datas
    //////////////////////////////////////////////////////////////

    public void readUserMessagesOnce(final String login, final Contact contact){

        LoggerHelper.info(getClass().getName(),"IN readUserMessagesOnce with contact = " + contact);

        final long limitDate = ServerTimeSyncer.getInstance().getLimitDate();

        Date date = new Date();
        date.setTime(limitDate);
        LoggerHelper.info(getClass().getName(),"IN readUserMessagesOnce with limitDate = " + date);

        //The query
        ref_messages_load = DataPersistenceManager.db.
                child(Constants.USERS_KEY).
                child(login).
                child(Constants.CONTACTS_KEY).
                child(contact.getLogin()).
                child(Constants.MESSAGES_KEY).orderByChild("date").startAt(String.valueOf(limitDate)).getRef();

        //the Listener
        ref_messages_load.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final long total_message_current_contact = dataSnapshot.getChildrenCount();

                //if the contact has no message then it can be skipped
                //for exemple icon_invitation has been sent and no messages exist yets
                if(total_message_current_contact == 0 ){
                    total_contact_counter -- ;
                }

                else{

                    LoggerHelper.info(getClass().getName(),"total_message_current_contact = " + total_message_current_contact);

                    ContactMessageCounter contactMessageCounter = new ContactMessageCounter(total_message_current_contact,contact.getLogin());

                    for(DataSnapshot child : dataSnapshot.getChildren()) {
                        LoggerHelper.info(getClass().getName(),"dataSnapshot with child = " + child.getKey());
                        readSingleMessage(contact, child.getKey(), contactMessageCounter);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Todo:order messages by date and get last N messages

    public void readSingleMessage(final Contact contact, final String keyMessage,final ContactMessageCounter contactMessageCounter){

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

                    LoggerHelper.info(getClass().getName(),String.format("contact.addMessage ",message.getFrom()));

                    timeLine.addMessageToTimeline(contact,message);

                    //contact messages have been red

                    if(contactMessageCounter != null){
                        if( ! contactMessageCounter.areAllMessagesRead()){
                            LoggerHelper.info(getClass().getName(),"contactMessageCounter.incrementCounter == " + contactMessageCounter.getCurrentCounter());
                            contactMessageCounter.incrementCounter();
                        }

                        else{
                            LoggerHelper.info(getClass().getName(),"contactMessageCounter all messages are red for contact == " + contactMessageCounter.getContact());
                            contact_read ++ ;
                        }

                        LoggerHelper.info(getClass().getName(),String.format(Locale.getDefault(),
                                "contact_read == %d ///////// total_contact_counter = %d",
                                contact_read, total_contact_counter));

                        if(contact_read >= total_contact_counter - 1){
                            //when over The first read has occured, now single reads can take place
                            LoggerHelper.info(getClass().getName(),"total_contact_counter == " + total_contact_counter);

                            //adapter must update
                            adapter.notifyDataSetChanged();

                            LoggerHelper.info(getClass().getName(),"LOAD messages finished");
                            firstMessagesLoadOccured = true;
                            ///////////////////////////////////////////
                        }
                    }
                }
                else{
                    LoggerHelper.info(getClass().getName(),"message with key " + keyMessage+ " doesnt exist");
                    if(contact_read >= total_contact_counter - 1){
                        firstMessagesLoadOccured = true;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    private void addContactMessagesListener(){

        LoggerHelper.info(getClass().getName(),"IN addContactMessagesListener");

        final String login = PreferenceUtility.getLogin(this.getContext());

        if(login == null) {
            LoggerHelper.error(getClass().getName(),"LOGIN should not be null at this point");
            return;
        }

        //contact request
        ref_contacts = DataPersistenceManager.db.
                child(Constants.USERS_KEY).
                child(login).
                child(Constants.CONTACTS_KEY);

        //to constantly listen on new contact added and their messages

        ChildEventListener listener = ref_contacts.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String s) {
                LoggerHelper.info(getClass().getName(), "addContactMessagesListener.onChildAdded");
                LoggerHelper.info(getClass().getName(), String.format("addContactListListener contact added successfully with dataSnapshot=%s and param s=%s", snapshot, s));
                if (snapshot.getValue() instanceof String) {
                    LoggerHelper.info(getClass().getName(), "TimeLineFragment snapshot.getValue() instanceof String");
                } else {
                    LoggerHelper.info(getClass().getName(), "TimeLineFragment child exist with key" + snapshot.getKey());
                    Contact contact = snapshot.getValue(Contact.class);

                    if (contact != null) {

                        LoggerHelper.info(getClass().toString(), "contact.getLogin() " + contact.getLogin());

                        //contact is added even if not active
                        //must be filtered in adapters

                        if ( ! contactKeyList.contains(contact.getLogin())) {
                            contactKeyList.add(contact.getLogin());
                            ContactListSingleton.getInstance().getContactList().add(contact.getLogin());
                        }

                        if ( ! StringUtils.isEmpty(contact.getLogin())) {
                            LoggerHelper.info(getClass().getName(), "contact.isActive() addMessagesListener");
                            addMessagesListener(login, contact);
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String s) {
                LoggerHelper.info(getClass().getName(),"addContactMessagesListener.onChildChanged");
                Contact contact = snapshot.getValue(Contact.class);

                if (contact != null && contact.isActive()) {
                    LoggerHelper.info(getClass().getName(),"contact is active");
                    if( ! contactKeyList.contains(contact.getLogin())){
                        contactKeyList.add(contact.getLogin());
                        ContactListSingleton.getInstance().getContactList().add(contact.getLogin());
                        addMessagesListener(login, contact);
                    }
                }
                else{
                    LoggerHelper.info(getClass().getName(),"contact was modified differently");
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                LoggerHelper.info(getClass().getName(),"onChildRemoved");
                //Todo:Remove the listener when contact is removed, maybe it is removed automatically
                //remove contact from list
                Contact contact = dataSnapshot.getValue(Contact.class);
                if(contact != null){
                    //contactList.remove(contact);
                    ContactListSingleton.getInstance().getContactList().remove(contact.getLogin());
                    contactKeyList.remove(contact.getLogin());
                    //adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LoggerHelper.error(getClass(), "addContactMessagesListener "  + databaseError.toString());
            }
        });

        //add listener to list
        dbReferenceChildList.put(ref_contacts,listener);

    }

    //////////////////////////////////////////////////////////////////////////////

    public void addMessagesListener(final String login, final Contact contact){

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
                //Todo: not ure if using firstMessagesLoadOccured is a good idea
                if(firstMessagesLoadOccured) {
                    LoggerHelper.info(getClass().getName(), String.format("firstRead  already Occured addMessagesListener addMessagesListener.onChildAdded with dataSnapshot=%s and param s=%s", dataSnapshot, s));
                    if (dataSnapshot.getValue() != null) {
                        readSingleMessageWithoutCounter(contact, dataSnapshot.getKey());
                    } else {
                        LoggerHelper.info(getClass().getName(), "readMessages.onDataChange - there are no messages");
                    }
                }
                else{
                    LoggerHelper.info("firstMessagesLoadOccured didn't occur yet");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                LoggerHelper.info(getClass().getName(),String.format("addMessagesListener onChildChanged with dataSnapshot=%s and param s=%s",dataSnapshot,s));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                LoggerHelper.info(getClass().getName(),String.format("addMessagesListener onChildRemoved with dataSnapshot=%s",dataSnapshot));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LoggerHelper.error(getClass(), "addMessagesListener "  + databaseError.toString());
            }
        });

        //add listener
        dbReferenceChildList.put(ref_messages,listener);
    }

    public void readSingleMessageWithoutCounter(final Contact contact, final String keyMessage){

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
                    if(message != null){
                        LoggerHelper.info(getClass().getName(),String.format("contact.addMessage ",message.getFrom()));
                        //message added to timeline
                        timeLine.addMessageToTimeline(contact,message);
                        //adapter can update
                        //we can animate the reception  because it will happen on first row;
                        //no need of adapter.notifyDataSetChanged();

                        adapter.animateMessageReception(currentPosition);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LoggerHelper.error(getClass(), "readSingleMessageWithoutCounter "  + databaseError.toString());
            }
        });
    }

    /////////////////////////////////////////////////////////////
    // LifeCycle
    /////////////////////////////////////////////////////////////

    @Override
    public void onResume(){
        super.onResume();
        LoggerHelper.warn(this.getClass().toString(),"IN onResume");
        adapter.notify();
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
        saved_current_pos = currentPosition;
        detachScrollRunnableFromHandler();
    }

    @Override
    public void onStart(){
        super.onStart();
        LoggerHelper.warn(this.getClass().toString(),"IN onStart");
        refreshListView();
        timeLineGlobalListener.getEndlessScrollListener().timelineAutoScroll();
    }

    /*
    * refresh listView if timeframes were added
    * */
    private void refreshListView(){
        LoggerHelper.info(getClass(),"IN refreshListView");
        if( ! saved_current_pos.equals(currentPosition)){
            LoggerHelper.info(getClass(),"List can be refreshed");
            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        LoggerHelper.warn(this.getClass().toString(),"IN onDestroyView");
    }

    @Override
    public void onDetach(){
        super.onDetach();
        LoggerHelper.warn(this.getClass().toString(),"IN onDetach");
    }


    private void detachHandler(){
        if(handler != null){
            handler.removeCallbacks(null);
        }
    }

    private void detachScrollRunnableFromHandler(){
        timeLineGlobalListener.getEndlessScrollListener().removeCallBack();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LoggerHelper.info(getClass(),"IN Ondestroy");
        ConnectionHelper.removeChildEventListener(dbReferenceChildList);
    }

    @Override
    public int getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public void updateCurrentPosition() {
        currentPosition -- ;
    }

    @Override
    public ListView getListView() {
        return timeLineListView;
    }

    @Override
    public TimeLine getTimeLine() {
        return timeLine;
    }

    @Override
    public void setUserTouch(boolean userTouch) {
           this.userTouch = userTouch ;
    }

    @Override
    public boolean isUserTouch() {
        return userTouch;
    }

    @Override
    public void setUserScroll(boolean userScroll) {
        this.userScroll = userScroll;
    }

    @Override
    public boolean isUserScroll() {
        return userScroll;
    }
}
