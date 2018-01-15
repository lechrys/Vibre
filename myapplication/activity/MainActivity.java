package fr.myapplication.dc.myapplication.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.contact.InvitationContactActivity;
import fr.myapplication.dc.myapplication.activity.contact.InviteForAppActivity;
import fr.myapplication.dc.myapplication.activity.contact.ManageContactActivity;
import fr.myapplication.dc.myapplication.activity.contact.SearchContactActivity;
import fr.myapplication.dc.myapplication.activity.timeline.TimeLineFragment;
import fr.myapplication.dc.myapplication.activity.tutorial.IntroPageTuto;
import fr.myapplication.dc.myapplication.view.adapter.pager.MainActivityPagerAdapter;
import fr.myapplication.dc.myapplication.activity.user.LoginActivity;
import fr.myapplication.dc.myapplication.activity.user.AccountActivity;
import fr.myapplication.dc.myapplication.animation.BubbleTransformer;
import fr.myapplication.dc.myapplication.data.model.User;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.gesture.OnSwipeTouchListener;
import fr.myapplication.dc.myapplication.media.PictureStorage;
import fr.myapplication.dc.myapplication.view.fragment.VibrationSingleFragment;
import fr.myapplication.dc.myapplication.view.fragment.VibrationSingleFragmentWithBubbleViews;
import util.Constants;
import util.GlobalHelper;
import util.ImageUtil;
import util.LoggerHelper;
import util.PreferenceUtility;

import static fr.myapplication.dc.myapplication.media.PictureStorage.STORAGE_AVATAR_REF;
import static fr.myapplication.dc.myapplication.media.PictureStorage.getPictureTargetPath;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,   BasicActivity {

    private TextView textViewLoginInMenu;
    private ImageView avatarImageView;
    private FloatingActionButton fabReceivedVibrations;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private View header;
    private RelativeLayout invitationBadgeLayout;
    private ProgressDialog progressDialog;
    private MainActivityPagerAdapter mCustomPagerAdapter;
    private ViewPager viewPager;
    private AlertDialog avatar_dialog;

    ///////////////////////////////////

    private String authId;
    public String login;
    private int nb_invitations_received;
    private int nb_invitations_accepted;
    boolean allowCommit;

    //////////////////////////////////////////////

    DatabaseReference invitationRef;
    private HashMap<DatabaseReference, ChildEventListener> childEventListenerHashMap ;

    ///////////////////////////////////

    View.OnTouchListener onTouchListener;
    OnSwipeTouchListener onSwipeTouchListener;

    ///////////////////////////////////

    //Todo: To handle Firebase logout

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_TAKE_PICTURE = 2;

    //////////////////////////////////////////////

    ValueEventListener isFirebaseConnectionUp;

    // Fragments

    TimeLineFragment timelineFragment;
    VibrationSingleFragment vibrationSingleFragment;
    VibrationSingleFragmentWithBubbleViews vibrationSingleFragmentWithBubbleViews;

    //////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoggerHelper.info(getClass(),"IN onCreate");
        allowCommit = true;

        //hide la barre noir en haut
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //prepare
        prepareActivity();

        //retrieve authId
        authId = PreferenceUtility.getAuthPref(this);

        //get Login value followed by setView method
        saveLoginInContext();

        //set the user creation date if not exist in local prefs
    }

    private void prepareActivity(){
        timelineFragment = new TimeLineFragment();
        vibrationSingleFragment = new VibrationSingleFragment();
        vibrationSingleFragmentWithBubbleViews = new VibrationSingleFragmentWithBubbleViews();
        childEventListenerHashMap = new HashMap<>();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        LoggerHelper.info(getClass(),"IN onStart");
    }

    @Override
    public void setViews() {

        //content
        setContentView(R.layout.activity_main);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //je regarde mon menu en entier (a garder)
        header = navigationView.getHeaderView(0);

        //toolbar setup
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back_button));
        setSupportActionBar(toolbar);

        //fabReceivedVibrations setup
/*       fabReceivedVibrations = (FloatingActionButton) findViewById(R.id.fab);
        fabReceivedVibrations.setImageResource(R.drawable.cloud);
        fabReceivedVibrations.setTransitionName("reveal");*/
        //fabReceivedVibrations.getBackground();
        //fabReceivedVibrations.setBackgroundColor(Color.TRANSPARENT);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        //get the user login
        textViewLoginInMenu = (TextView) header.findViewById(R.id.textViewNomUtilisateur);
        avatarImageView = (ImageView) header.findViewById(R.id.imageAvatar);

        //get Login
        login = PreferenceUtility.getLogin(this.getApplicationContext());
        textViewLoginInMenu.setText(login);

        //load avatar
        PictureStorage.loadImageFromStorage(this,login, avatarImageView);

        //initActionBar
        initActionBar();
        //
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setPageTransformer(true, new BubbleTransformer());
        setPager();

    }

    public void setPager(){
        mCustomPagerAdapter = new MainActivityPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(mCustomPagerAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle saveInstance){
//        super.onResume();
        LoggerHelper.info(this.getClass().toString(),"IN onSaveInstanceState");
        allowCommit = false;
    }

    @Override
    protected void onResumeFragments() {
        LoggerHelper.info(this.getClass().toString(),"IN onResumeFragments");
        allowCommit = true;
    }

    @Override
    public void onResume(){
        super.onResume();

        LoggerHelper.debug(getClass(),"IN onResume");

        if (viewPager != null) {
            viewPager.setCurrentItem(0, true);
        }
    }

    /*
     * Set back button on bar
     */
    public void initActionBar(){
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            toolbar.setNavigationIcon(R.drawable.back_button);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            //For Toolbar (Action bar) end
        }
    }

    //Fragments are managed programmatically

    @Override
    public void setButtonsListeners() {

        //fab

/*        fabReceivedVibrations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(this.getClass().getSimpleName(), "fabReceivedVibrations clicked");

                switchVibrationMode();

            }
        });*/

        //////////////////////////////////////////////

        //nav

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //////////////////////////////////////////////

        avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(avatar_dialog == null){
                    dialog();
                }
                avatar_dialog.show();
            }
        });
    }

    private AlertDialog dialog(){

        LoggerHelper.info(getClass(),"IN dialog()");

        final Activity currentActivity = this;


        avatar_dialog = new AlertDialog.Builder(this)
                //set message, title, and icon
                .create();

        if(avatar_dialog.getWindow() != null) avatar_dialog.getWindow().setLayout(600, 400);

        View layout = getLayoutInflater().inflate(R.layout.layout_activity_account_avatar,null);
        ImageView avatar_select = (ImageView) layout.findViewById(R.id.avatar_select);
        avatar_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalHelper.verifyStoragePermissions(currentActivity);

                //Todo: add some progress bar
                //Todo: add upload image limit

                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        ImageView avatar_take_photo = (ImageView) layout.findViewById(R.id.avatar_photo);
        avatar_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalHelper.verifyStoragePermissions(currentActivity);

                //Todo: add some progress bar
                //Todo: add upload image limit

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, RESULT_TAKE_PICTURE);
                }
            }
        });

        avatar_dialog.setView(layout);

        return avatar_dialog;
    }

    /*
   *
   * The uploaded picture is encoded in base64 string
   *
   */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LoggerHelper.info(getClass(),String.format("onActivityResult with requestCode = %d and requestCode = %d",requestCode,resultCode));

        if(avatar_dialog != null) avatar_dialog.dismiss();

        Bitmap bitmapImage = null;

        if(requestCode == RESULT_TAKE_PICTURE &&  resultCode == RESULT_OK && null != data){
            Bundle extras = data.getExtras();
            bitmapImage = (Bitmap) extras.get("data");
        }

        else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            if (progressDialog == null) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Compressing picture...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(true);
                progressDialog.show();
            }

            //get the image
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);

            if (cursor != null) {

                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                //get bitmap
                bitmapImage = BitmapFactory.decodeFile(picturePath);
            }
        }

        else if(resultCode == RESULT_CANCELED){
            //do nothing
        }
        else{
            Toast.makeText(this,"Error loading picture",Toast.LENGTH_SHORT).show();
        }

        if(bitmapImage != null) {
            //resize pic
            Bitmap scaledBitmap = ImageUtil.scaleDown(bitmapImage, 256, true);

            //save image locally
            PictureStorage.saveToInternalStorage(this, login, scaledBitmap);

            //load image from local storage
            PictureStorage.loadImageFromStorage(this, login, avatarImageView);

            //upload compressed image to firebase
            byte[] avatarData = ImageUtil.compressBitmap(scaledBitmap);

            //preparing Dialog
            if(progressDialog != null) progressDialog.dismiss();

            progressDialog = new ProgressDialog(this);
            progressDialog.setMax(100);
            progressDialog.setMessage("Uploading...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
            progressDialog.setCancelable(true);

            //save in Firebase
            uploadAvatarOnFirebase(login, avatarData);
        }
    }

    //////////////////////////////////////////////
    // Avatar management
    //////////////////////////////////////////////

    public void uploadAvatarOnFirebase(final String login, final byte[] pictureData) {

        //point to the file to upload
        StorageReference mountainsRef = STORAGE_AVATAR_REF.child(getPictureTargetPath(login));

        com.google.firebase.storage.UploadTask uploadTask = mountainsRef.putBytes(pictureData);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Todo: Handle unsuccessful uploads
                Toast.makeText(MainActivity.this,"Error loading picture",Toast.LENGTH_SHORT).show();
                LoggerHelper.debug(getClass().getName(),"failure uploading picture");
                progressDialog.dismiss();

            }
        }).addOnSuccessListener(new OnSuccessListener<com.google.firebase.storage.UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(com.google.firebase.storage.UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
    /*            DataPersistenceManager.persistPicture(login, getPictureTargetPath(login));
                progressDialog.dismiss();*/
                progressDialog.dismiss();
                //show picture
            }
        });

        if (progressDialog != null) {
            uploadTask.addOnProgressListener(new OnProgressListener<com.google.firebase.storage.UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(com.google.firebase.storage.UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests")
                    long div = (long)((float) taskSnapshot.getTotalByteCount() / taskSnapshot.getBytesTransferred());
                    int progress = (int) (100.0 / div);

                    //updatin the UI
                    progressDialog.incrementProgressBy((int) progress);
                }
            });
        }
    }

    //////////////////////////////////////////////////
    // swipe listner
    //////////////////////////////////////////////////

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LoggerHelper.info("IN onTouchEvent");
        if(onSwipeTouchListener != null) {
            onSwipeTouchListener.gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    public void initOnTouchListener() {

        LoggerHelper.info("IN initOnTouchListener");

        onSwipeTouchListener = new OnSwipeTouchListener(this)

        {
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                LoggerHelper.info("onSwipeRight");
            }

            @Override
            public void onSwipeLeft() {
                LoggerHelper.info("onSwipeLeft");
            }
        };

        onTouchListener = new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LoggerHelper.info(this.getClass().getName(),"initOnTouchListener onTouch");
                onSwipeTouchListener.gestureDetector.onTouchEvent(event);
                return true;
            }
        };

    }

    ///////////////////////////////////////////////////

    public void setUpFragments() {

        if(allowCommit) {
            FragmentManager fragmentManager = getSupportFragmentManager();

            fragmentManager
            .beginTransaction()
            //.addSharedElement(fabReceivedVibrations, "reveal")
            .replace(R.id.vibrationManagerLayout, timelineFragment, "timelineFragment")
            .commit();
        }
        else{
            //commit with loss because onSaveInstance has been call just before and doesnt allow commit.
            FragmentManager fragmentManager = getSupportFragmentManager();

            fragmentManager
                    .beginTransaction()
                    //.addSharedElement(fabReceivedVibrations, "reveal")
                    .replace(R.id.vibrationManagerLayout, timelineFragment, "timelineFragment")
                    .commitAllowingStateLoss();
        }


    // Note that we need the API version check here because the actual transition classes (e.g. Fade)
    // are not in the support library and are only available in API 21+. The methods we are calling on the Fragment
    // ARE available in the support library (though they don't do anything on API < 21)
/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            vibrationSingleFragment.setSharedElementEnterTransition(new DetailsTransition());
            vibrationSingleFragment.setEnterTransition(new Fade());
            vibrationSingleFragment.setExitTransition(new Fade());
            vibrationSingleFragment.setSharedElementReturnTransition(new DetailsTransition());
        }*/
    }

    public void switchVibrationMode() {
        LoggerHelper.info(this.getClass().getName(),"MainActivity.switchVibrationMode");

        FragmentManager fragmentManager = getSupportFragmentManager();

        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();

        LoggerHelper.info(this.getClass().getName(),"backStackEntryCount = " + backStackEntryCount);

        mCustomPagerAdapter.getItem(0);
/*
        if ( ! timelineFragment.isVisible()) {
            LoggerHelper.info("timelineFragment.isNotVisible()");

            //cancel last transaction (meaning reversing the replace vibrationSingleFragment action bellow)
            fragmentManager.popBackStack();

        }
        else{

            LoggerHelper.info(" timelineFragment.isVisible()");

            if (vibrationSingleFragment == null || ! vibrationSingleFragment.isVisible()) {
                vibrationSingleFragment = new VibrationSingleFragment();
                LoggerHelper.info(" vibrationSingleFragment.isNotVisible()");
                fragmentManager.
                    beginTransaction().
                    replace(R.id.vibrationManagerLayout, vibrationSingleFragment, "vibrationSingleFragment")
                    //.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack("vibrationSingleFragment")
                    .commit();

            }
          if (vibrationSingleFragmentWithBubbleViews == null || ! vibrationSingleFragmentWithBubbleViews.isVisible()) {
                fragmentManager.
                        beginTransaction().
                        replace(R.id.vibrationManagerLayout, vibrationSingleFragmentWithBubbleViews, "vibrationSingleFragment").
                        addToBackStack("vibrationSingleFragment").
                        commit();
            }
        }
        */
    }

    @Override
    public void onBackPressed() {
        LoggerHelper.info("IN onBackPressed with getFragmentManager().getBackStackEntryCount() = " + getFragmentManager().getBackStackEntryCount());
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            LoggerHelper.debug("onKeyDown");
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LoggerHelper.info(getClass().getName(),"IN onCreateOptionsMenu");

        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.invitationContactButton);
        MenuItemCompat.setActionView(item, R.layout.layout_menu_invitations);

        invitationBadgeLayout = (RelativeLayout) MenuItemCompat.getActionView(item);
        TextView invitations_received_tv = (TextView) invitationBadgeLayout.findViewById(R.id.actionbar_notification_received_textview);
        TextView invitations_accepted_tv = (TextView) invitationBadgeLayout.findViewById(R.id.actionbar_notification_accepted_textview);


        invitationBadgeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, InvitationContactActivity.class);
                startActivity(i);
            }
        });


        if(nb_invitations_accepted > 0){
            invitationBadgeLayout.setVisibility(View.VISIBLE);
            invitations_accepted_tv.setVisibility(View.VISIBLE);
            invitations_accepted_tv.setText(String.valueOf(nb_invitations_accepted));
            item.setVisible(true);
        }
        else{
            invitations_accepted_tv.setVisibility(View.INVISIBLE);
        }
        if(nb_invitations_received > 0){
            invitationBadgeLayout.setVisibility(View.VISIBLE);
            invitations_received_tv.setVisibility(View.VISIBLE);
            invitations_received_tv.setText(String.valueOf(nb_invitations_received));
            item.setVisible(true);
        }
        else{
            invitations_received_tv.setVisibility(View.INVISIBLE);
        }

        if(nb_invitations_received <= 0 && nb_invitations_accepted <= 0 ){
            invitationBadgeLayout.setVisibility(View.INVISIBLE);
            item.setVisible(false);
        }
        else{
            invitationBadgeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MainActivity.this, InvitationContactActivity.class);
                    startActivity(i);
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LoggerHelper.debug("onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.searchContactButton:
                Log.i(this.getClass().getSimpleName(), "searchContactButton clicked");
                Intent i = new Intent(MainActivity.this, SearchContactActivity.class);
                startActivity(i);
                return true;

            case android.R.id.home:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        LoggerHelper.debug("onNavigationItemSelected");
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_view) {
            // Handle the camera action
        } else if (id == R.id.nav_contacts) {
            Intent i = new Intent(this, ManageContactActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_account) {
            Intent i = new Intent(this, AccountActivity.class);
            startActivity(i);
        }
        else if (id == R.id.nav_about) {
            Intent i = new Intent(this, IntroPageTuto.class);
            startActivity(i);
        }
        else if (id == R.id.nav_invite) {
            Intent i = new Intent(this, InviteForAppActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_deconnexion) {
            signOut();
            finishAffinity();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void saveLoginInContext() {

        DatabaseReference ref = DataPersistenceManager.db.
                child(Constants.AUTH_KEY).child(authId);

        LoggerHelper.info(getClass(),"IN saveLoginInContext with authId = " + authId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                LoggerHelper.info(getClass(),"IN saveLoginInContext onDataChange = " + dataSnapshot);

                String login = null;

                if (dataSnapshot != null) {
                    login = (String) dataSnapshot.getValue();
                    LoggerHelper.debug(getClass(),"Login retrieved is " + login);
                    PreferenceUtility.setLoginPref(getApplication(), login);
                }

                if(StringUtils.isEmpty(login)){
                    LoggerHelper.error(getClass(),"dataSnapshot is null ");
                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(intent);
                }

                //getUserInfos to fill de prefs
                getUserInfos(login);

                //When Done we can set the listeners
                //we got the login we can show the views now

                setViews();

                //listeners
                setButtonsListeners();

                //setUpFragments
               // setUpFragments();

                //swipeListener
                initOnTouchListener();

                if(childEventListenerHashMap.get(invitationRef) == null) {
                    LoggerHelper.debug("childEventListenerHashMap.get(invitationRef) == null");
                    addInvitationReceivedListener();
                    addInvitationAcceptedListener();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LoggerHelper.error(getClass(),"onCancelled - databaseError = " + databaseError);
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getUserInfos(final String login){
        DatabaseReference ref = DataPersistenceManager.db.
                child(Constants.USERS_KEY).child(login);

        LoggerHelper.info(getClass(),"IN getUserInfos with login = " + login);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                LoggerHelper.info(getClass(), "IN saveLoginInContext onDataChange = " + dataSnapshot);

                if (dataSnapshot != null) {
                    final User user = dataSnapshot.getValue(User.class);
                    LoggerHelper.debug(getClass(), "Login retrieved is " + login);
                    if(user != null) {
                        PreferenceUtility.setPhonePref(getApplication(), user.getPhone());
                        PreferenceUtility.setOtherLoginPref(getApplication(), user.getLogin());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LoggerHelper.error(getClass(),"onCancelled - databaseError = " + databaseError);
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////
    // Listeners for invitations
    //////////////////////////////////////////////////////////////////////////

    private void addInvitationReceivedListener() {

        nb_invitations_received = 0;

        final String login = PreferenceUtility.getLogin(this);

        if (login == null) {
            LoggerHelper.error("addInvitationReceivedListener MainActivity LOGIN should not be null at this point");
            signOut();
            finishAffinity();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);

        } else {

            invitationRef = DataPersistenceManager.db.
                    child(Constants.USERS_KEY).
                    child(login).
                    child(Constants.INVITATION_KEY).
                    child(Constants.RECEIVED_INVITATION_KEY);

            ChildEventListener childEventListener = invitationRef.addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot snapshot, String s) {
                    LoggerHelper.info("MainActivity addInvitationReceivedListener contact exist with value" + snapshot.getValue());
                    nb_invitations_received++;
                    invalidateOptionsMenu();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    LoggerHelper.info("onChildChanged");
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    LoggerHelper.info("onChildRemoved");
                    nb_invitations_received--;
                    invalidateOptionsMenu();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            childEventListenerHashMap.put(invitationRef,childEventListener);
        }
    }

    private void addInvitationAcceptedListener() {

        nb_invitations_accepted = 0;

        final String login = PreferenceUtility.getLogin(this);

        if (login == null) {
            LoggerHelper.error("addInvitationReceivedListener MainActivity LOGIN should not be null at this point");
            signOut();
            finishAffinity();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);

        } else {

            invitationRef = DataPersistenceManager.db.
                    child(Constants.USERS_KEY).
                    child(login).
                    child(Constants.INVITATION_KEY).
                    child(Constants.ACCEPTED_INVITATION_KEY);

            ChildEventListener childEventListener = invitationRef.addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot snapshot, String s) {
                    LoggerHelper.info("MainActivity addInvitationReceivedListener contact exist with value" + snapshot.getValue());
                    nb_invitations_accepted++;
                    invalidateOptionsMenu();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    LoggerHelper.info("onChildChanged");
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    LoggerHelper.info("onChildRemoved");
                    nb_invitations_accepted--;
                    invalidateOptionsMenu();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            childEventListenerHashMap.put(invitationRef,childEventListener);
        }
    }

    private void addFirebaseConnectionListener(){
        //isFirebaseConnectionUp = ConnectionHelper.getFirebaseIsConnectedListener();
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        isFirebaseConnectionUp = connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected) {
                    LoggerHelper.info(getClass(),"addFirebaseConnectedListener Firebase connected");
                } else {
                    LoggerHelper.info(getClass(),"addFirebaseConnectedListener Firebase not connected");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                LoggerHelper.error(getClass(),"Listener was cancelled");
            }
        });
    }

    ///////////////////////////////////////////
    // Activity LifeCycle
    ///////////////////////////////////////////

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        PreferenceUtility.clearLogin(this);
    }

    public void onStop()
    {
        super.onStop();
        LoggerHelper.info(getClass(), "IN onStop");
        //Todo : handle Invitation Listener
       // ConnectionHelper.removeChildEventListener(childEventListenerHashMap);
    }

    public void onRestart()
    {
        super.onRestart();
        LoggerHelper.info(getClass(), "IN onRestart");
    }

    public void onPause()
    {
        super.onPause();
        LoggerHelper.info(getClass(), "IN onPause");
    }

    public void onDestroy()
    {
        super.onDestroy();
        LoggerHelper.info(getClass(), "IN onDestroy");
    }

}

