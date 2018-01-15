package fr.myapplication.dc.myapplication.activity.user.unused;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.BasicActivity;
import fr.myapplication.dc.myapplication.activity.MainActivity;
import fr.myapplication.dc.myapplication.data.model.User;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.media.PictureStorage;
import fr.myapplication.dc.myapplication.service.GCMRegistrationIntentService;
import util.Constants;
import util.GlobalHelper;
import util.LoggerHelper;
import util.StatusCode;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */

//Todo:Be sure that the user cannot come back to the register activity by pushing return button
public class InscriptionActivity extends AppCompatActivity implements BasicActivity {

    private String token;
    private String login;
    // Set up the login form.

    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int RESULT_LOAD_IMAGE = 1;

    // UI references.
    private AutoCompleteTextView loginView;
    //private View mProgressView;
    private View mLoginFormView;
    private Button buttonPourInscription;
    private ImageButton buttonAvat;

    //InstanceID instanceID;
    //Todo:Open camera to take picture
    private byte[] avatarData;

    private SharedPreferences prefs;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    public InscriptionActivity() {
        token = null;
    }

    /*String token= null;*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoggerHelper.info("onCreate");
        try {

            prefs = this.getSharedPreferences(Constants.APP_ID, Context.MODE_PRIVATE);

            //check if token was provided
            checkToken();

            //create views
            setViews();

            //checkGooglePlayService
            checkGooglePlayService();

            //setButtonsListeners
            setButtonsListeners();
        }
        catch (Exception e){
            LoggerHelper.error("OnCreate failed with error : " + e);
        }

    }


    /*
     * check that the token exists otherwise force its regeneration
     * Todo : to move to Login Activity
     */
    private void checkGooglePlayService(){
        //Check status of Google play service in device
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if(ConnectionResult.SUCCESS != resultCode) {
            LoggerHelper.debug("The reason is " + GooglePlayServicesUtil.getErrorString(resultCode));
            //Check type of error
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Toast.makeText(getApplicationContext(), "Google Play Service is not install/enabled in this device, error code is " + resultCode, Toast.LENGTH_LONG).show();
                //So notification
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());
            } else {
                Toast.makeText(getApplicationContext(), "This device does not support for Google Play Service!, , error code is " + resultCode, Toast.LENGTH_LONG).show();
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());
            }
        } else {
            Toast.makeText(getApplicationContext(), "device supports Google Play Service :)", Toast.LENGTH_LONG).show();
            //Start service
            Intent intent = new Intent(this, GCMRegistrationIntentService.class);
            InscriptionActivity.this.startService(intent);
        }
    }


    /*
    * Views creation
    * */
    public void setViews(){
        setContentView(R.layout.activity_inscription);
        // Set up the login form.
        loginView = (AutoCompleteTextView) findViewById(R.id.pseudoInscription);
        mLoginFormView = findViewById(R.id.login_form);
        //mProgressView = findViewById(R.id.login_progress);
        buttonPourInscription = (Button) findViewById(R.id.buttonInscription);
        buttonAvat = (ImageButton) findViewById(R.id.buttonAvatar);
        //buttonAvat.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    public void setButtonsListeners(){
        final Activity currentActivity = this;
        //set Listeners
        buttonAvat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                GlobalHelper.verifyStoragePermissions(currentActivity);

                //Todo: add some progress bar
                //Todo: add upload image limit

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        buttonPourInscription.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                login = loginView.getText().toString();
                //check
                if ( ! StringUtils.isEmpty(login)) {

                    login = GlobalHelper.formatLogin(login);

                    registerUser(login,null);

                    if(avatarData != null){
                        LoggerHelper.debug("avatarData=" + avatarData + " with login="+login);
                        //upload the picture
                        PictureStorage.uploadAvatar(login, avatarData, null);
                    }
                    else{
                        LoggerHelper.debug("no picture has been uploaded");
                    }
                } else {
                    Toast.makeText(InscriptionActivity.this, "Veuillez remplir tous les champs !", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    /*
    * check that the token exists otherwise force its regeneration
    */
    //Todo : very important update token after expiration - or after reinstall of the app
    private void checkToken() throws IOException{

        String storedToken = prefs.getString(Constants.FIREBASE_REG_ID,null);

        if(storedToken != null){
            token = storedToken;
        }
        else{
            //deletes token
            FirebaseInstanceId.getInstance().deleteInstanceId();
            //regenerate token
            token = FirebaseInstanceId.getInstance().getToken();
            //save the token in prefs
            prefs.edit().putString(Constants.FIREBASE_REG_ID,token).commit();
        }
    }

    /*
    * registration process
    */
    //Todo : modify this method, we cannot iterate over all users, should be child("users").child(login)
    public void registerUser(final String login,
                             final String phoneNumber) {

        if(token != null) {

            //Login is unique : check that Login is not alreay assigned
            DatabaseReference ref = DataPersistenceManager.db.child(Constants.USERS_KEY);
            //Query queryRef = ref.orderByChild("login");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean loginExists = false;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if(snapshot.hasChildren()){
                                User user = snapshot.getValue(User.class);
                                if(user != null){
                                    if(login.equals(user.getLogin())){
                                        LoggerHelper.info("login already assigned");
                                        loginExists = true;
                                        break;
                                    }
                                }

                            }
                            else{
                                LoggerHelper.debug("user without data");
                            }
                        }
                        //check if login exists
                        if( ! loginExists){
                            try {
                                LoggerHelper.debug("about to set pref login=" + login);
                                prefs.edit().putString(Constants.LOGIN,login).commit();
                                DataPersistenceManager.persistUser(prefs, login, phoneNumber);
                            }
                            catch(Exception e){
                                LoggerHelper.error(StatusCode.USER_REGISTRATION_ERROR.getMessage());
                                Toast.makeText(
                                        InscriptionActivity.this,
                                        StatusCode.USER_REGISTRATION_ERROR.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            //start Main activity
                            LoggerHelper.debug("starting MainActivity");
                            Intent intent = new Intent (InscriptionActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        else {
                            LoggerHelper.error(StatusCode.USER_ALREADY_EXISTS.getMessage());
                            Toast.makeText(
                                    InscriptionActivity.this,
                                    StatusCode.USER_ALREADY_EXISTS.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Do something about the error
                    }
                }
            );
        }
        else{
            LoggerHelper.error("Token doesnt exists, can't register user !");
            Toast.makeText(
                    InscriptionActivity.this,
                    StatusCode.TOKEN_REGISTRATION_ERROR.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(loginView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Shows the progress UI and hides the login form.

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    */

    @Override
    protected void onResume() {
        super.onResume();
        Log.w("MainActivity", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w("MainActivity", "onPause");
    }

    /*
    *
    * The uploaded picture is encoded in base64 string
    *
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);

            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Toast.makeText(getApplicationContext(), picturePath, Toast.LENGTH_LONG).show();

            LoggerHelper.debug("picture PATH=" + picturePath);

            //avatar = Base64.encodeToString(getBytesFromBitmap(BitmapFactory.decodeFile(picturePath)), Base64.NO_WRAP);
            //LoggerHelper.debug("avatar length=" + avatar.length());

            LoggerHelper.debug("InscriptionActivity.onActivityResult");

            avatarData = getBytesFromBitmap(BitmapFactory.decodeFile(picturePath));

            buttonAvat.setImageURI(selectedImage);

            //save image locally
            File file = new File(Environment.getRootDirectory(), "/images/toro.png");
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(avatarData);
                LoggerHelper.debug("file written locally");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        LoggerHelper.debug("getBytesFromBitmap length=" + stream.toByteArray().length);
        return stream.toByteArray();
    }

    /*
    *
    * The uploaded picture is then uploaded to google storage
    * We only persist the path in google storage
    * */

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void getUser(final String login){

    }

    /**
     * A login screen that offers login via email/password.
     */
    public static class ChangeLoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
        /**
         * Id to identity READ_CONTACTS permission request.
         */
        private static final int REQUEST_READ_CONTACTS = 0;

        /**
         * A dummy authentication store containing known user names and passwords.
         * TODO: remove after connecting to a real authentication system.
         */
        private static final String[] DUMMY_CREDENTIALS = new String[]{
                "foo@example.com:hello", "bar@example.com:world",
                "foo@foo.com:hello", "bar@bar.com:world",
                "foo@bar.com:hello", "bar@foo.com:world"
        };
        /**
         * Keep track of the login task to ensure we can cancel it if requested.
         */
        private UserLoginTask mAuthTask = null;

        // UI references.
        private AutoCompleteTextView loginChange;
        private EditText mdpChange, mdp1change;
        private View mProgressView;
        private View mLoginFormView;
        private Button buttonChangeCompte;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_change_login);
            // Set up the login form.
            loginChange = (AutoCompleteTextView) findViewById(R.id.editTextChangeLogin);
            populateAutoComplete();
            mdp1change = (EditText) findViewById(R.id.editText1ChangeMdp);
            mdpChange = (EditText) findViewById(R.id.editTextChangeMdp);
            mdpChange.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == R.id.editTextChangeLogin || id == EditorInfo.IME_NULL) {
                        attemptLogin();
                        return true;
                    }
                    return false;
                }
            });

            buttonChangeCompte = (Button) findViewById(R.id.valider_changer_compte);
            buttonChangeCompte.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String Changelogin = loginChange.getText().toString();
                    String changepwd1 = mdp1change.getText().toString();
                    String changepwd2 = mdpChange.getText().toString();
                    if (!Changelogin.equals("") && !mdp1change.equals("") && !changepwd2.equals("")) {
                        if (changepwd1.equals(changepwd2)) {
                            changeProfil(Changelogin, changepwd1);

                            Toast.makeText(ChangeLoginActivity.this, "", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ChangeLoginActivity.this, "Les mots de passe sont différents !", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(ChangeLoginActivity.this, "Veuillez remplir tous les champs !", Toast.LENGTH_LONG).show();
                    }
                }
            });

            mLoginFormView = findViewById(R.id.login_form);
            mProgressView = findViewById(R.id.login_progress);

            /*//Récupération du login pour le mettre dans le menu
            //je regarde mon menu en entier (a garder)
            View header = navigationView.getHeaderView(0);
            //mon élément à mon menu
            textViewLoginInMenu = (TextView) header.findViewById(R.id.textViewNomUtilisateur);
            //Code pour recupérer l'élément et le mettre à l'endroid souhaité
            String data = getIntent().getExtras().getString("leLogin");
            textViewLoginInMenu.setText(data);
            Log.v("EditText", textViewLoginInMenu.toString());
            */
        }

        private void populateAutoComplete() {
            if (!mayRequestContacts()) {
                return;
            }

            getLoaderManager().initLoader(0, null, this);
        }

        private boolean mayRequestContacts() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return true;
            }
            if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
                Snackbar.make(loginChange, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new OnClickListener() {
                            @Override
                            @TargetApi(Build.VERSION_CODES.M)
                            public void onClick(View v) {
                                requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                            }
                        });
            } else {
                requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
            }
            return false;
        }

        public void changeProfil(String login, String password) {
            AsyncHttpClient client = new AsyncHttpClient();

            JSONObject jo = new JSONObject();
            try {
                jo.put("login", login);
                jo.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            StringEntity user = null;
            try {
                user = new StringEntity(jo.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            client.put(ChangeLoginActivity.this, "", user, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = null;
                    try {
                        response = new String(responseBody, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        Toast.makeText(getApplicationContext(), responseBody.toString(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                    try {
                        // JSON Object
                        JSONObject obj = new JSONObject(response);
                        // When the JSON response has status boolean value assigned with true
                        if (statusCode == 200) {
                            Toast.makeText(getApplicationContext(), "Changement effectué", Toast.LENGTH_LONG).show();
                            // Navigate to Home screen
                            Intent i = new Intent(ChangeLoginActivity.this, MainActivity.class);
                            startActivity(i);
                        }
                        // Else display error message
                        else {

                            Toast.makeText(getApplicationContext(), "Aucune réponse", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();

                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.v(this.getClass().getName(),"failure");
                    String response = null;
                    try {
                         response = new String(responseBody, "UTF-8");
                         Log.i(this.getClass().getName(),"failure with response : " + response);
                    } catch (UnsupportedEncodingException e) {
                        Toast.makeText(getApplicationContext(), responseBody.toString(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    // When Http response code is '404'
                    if (statusCode == 404) {
                        Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                    }
                    // When Http response code is '500'
                    else if (statusCode == 500) {
                        Toast.makeText(getApplicationContext(), "Something went wrong at server end ", Toast.LENGTH_LONG).show();
                    }
                    // When Http response code other than 404, 500
                    else {
                        Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                    }
                }
            });


        }

        /**
         * Callback received when a permissions request has been completed.
         */
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            if (requestCode == REQUEST_READ_CONTACTS) {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    populateAutoComplete();
                }
            }
        }


        /**
         * Attempts to sign in or register the account specified by the login form.
         * If there are form errors (invalid email, missing fields, etc.), the
         * errors are presented and no actual login attempt is made.
         */
        private void attemptLogin() {
            if (mAuthTask != null) {
                return;
            }

            // Reset errors.
            loginChange.setError(null);
            mdpChange.setError(null);

            // Store values at the time of the login attempt.
            String email = loginChange.getText().toString();
            String password = mdpChange.getText().toString();

            boolean cancel = false;
            View focusView = null;

            // Check for a valid password, if the user entered one.
            if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
                mdpChange.setError(getString(R.string.error_invalid_password));
                focusView = mdpChange;
                cancel = true;
            }

            // Check for a valid email address.
            if (TextUtils.isEmpty(email)) {
                loginChange.setError(getString(R.string.error_field_required));
                focusView = loginChange;
                cancel = true;
            } else if (!isEmailValid(email)) {
                loginChange.setError(getString(R.string.error_invalid_email));
                focusView = loginChange;
                cancel = true;
            }

            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            } else {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                showProgress(true);
                mAuthTask = new UserLoginTask(email, password);
                mAuthTask.execute((Void) null);
            }
        }

        private boolean isEmailValid(String email) {
            //TODO: Replace this with your own logic
            return email.contains("@");
        }

        private boolean isPasswordValid(String password) {
            //TODO: Replace this with your own logic
            return password.length() > 4;
        }

        /**
         * Shows the progress UI and hides the login form.
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        private void showProgress(final boolean show) {
            // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
            // for very easy animations. If available, use these APIs to fade-in
            // the progress spinner.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });

                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                mProgressView.animate().setDuration(shortAnimTime).alpha(
                        show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
            } else {
                // The ViewPropertyAnimator APIs are not available, so simply show
                // and hide the relevant UI components.
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return new CursorLoader(this,
                    // Retrieve data rows for the device user's 'profile' contact.
                    Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                            ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                    // Select only email addresses.
                    ContactsContract.Contacts.Data.MIMETYPE +
                            " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                    .CONTENT_ITEM_TYPE},

                    // Show primary email addresses first. Note that there won't be
                    // a primary email address if the user hasn't specified one.
                    ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            List<String> emails = new ArrayList<>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                emails.add(cursor.getString(ProfileQuery.ADDRESS));
                cursor.moveToNext();
            }

            addEmailsToAutoComplete(emails);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }

        private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
            //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(ChangeLoginActivity.this,
                            android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

            loginChange.setAdapter(adapter);
        }


        private interface ProfileQuery {
            String[] PROJECTION = {
                    ContactsContract.CommonDataKinds.Email.ADDRESS,
                    ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
            };

            int ADDRESS = 0;
            int IS_PRIMARY = 1;
        }

        /**
         * Represents an asynchronous login/registration task used to authenticate
         * the user.
         */
        public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

            private final String mEmail;
            private final String mPassword;

            UserLoginTask(String email, String password) {
                mEmail = email;
                mPassword = password;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                // TODO: attempt authentication against a network service.

                try {
                    // Simulate network access.
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return false;
                }

                for (String credential : DUMMY_CREDENTIALS) {
                    String[] pieces = credential.split(":");
                    if (pieces[0].equals(mEmail)) {
                        // Account exists, return true if the password matches.
                        return pieces[1].equals(mPassword);
                    }
                }

                // TODO: register the new account here.
                return true;
            }

            @Override
            protected void onPostExecute(final Boolean success) {
                mAuthTask = null;
                showProgress(false);

                if (success) {
                    finish();
                } else {
                    mdpChange.setError(getString(R.string.error_incorrect_password));
                    mdpChange.requestFocus();
                }
            }

            @Override
            protected void onCancelled() {
                mAuthTask = null;
                showProgress(false);
            }
        }
    }
}

