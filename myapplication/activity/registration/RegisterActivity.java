package fr.myapplication.dc.myapplication.activity.registration;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.BasicActivity;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import util.ConnectionHelper;
import util.Constants;
import util.GlobalHelper;
import util.LoggerHelper;
import util.PreferenceUtility;
import util.StatusCode;

/**
 * Firebase Authenticatiob Activity via mail/pass
 */
public class RegisterActivity extends AppCompatActivity implements BasicActivity {

    //shared firebase objects

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String login;
    private String email;
    private String pwd1;

    // UI references.
    private AutoCompleteTextView loginView;
    private EditText editTextEmail, editTextPass, editTexPassConfirm;
    private ProgressDialog progressDialog;

    private SharedPreferences prefs;

    public RegisterActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoggerHelper.debug("IN onCreate");

        try {

            prefs = this.getSharedPreferences(Constants.APP_ID, Context.MODE_PRIVATE);

            //create views
            setViews();

            //setButtonsListeners
            setButtonsListeners();

            //create the authentication listener in order to check the state of the authentication
            setAuthListener();

            //
            initActionBar();

        }
        catch (Exception e){
            LoggerHelper.error("OnCreate failed with error : " + e);
        }

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

    //////////////////////////////////////////////////////////////

    /*
    * Views creation
    * */
    public void setViews(){
        LoggerHelper.debug("IN setViews");
        setContentView(R.layout.activity_signup);
        // Set up the login form.
        editTextEmail = (EditText) findViewById(R.id.emailAuth);
        editTextPass = (EditText) findViewById(R.id.passAuth);
        editTexPassConfirm = (EditText) findViewById(R.id.passAuthConfirm);
        loginView = (AutoCompleteTextView) findViewById(R.id.pseudoInscription);
        loginView.setFilters(new InputFilter[]{GlobalHelper.getLoginFilter()});

        if( progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Registering...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
        }
    }

    public void setButtonsListeners(){
        LoggerHelper.debug("IN setButtonsListeners");
        //set Listeners

        Button authButton = (Button) findViewById(R.id.authButton);
        assert authButton != null;

        authButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ConnectionHelper.isGooglePlayServicesAvailable(RegisterActivity.this)) {

                    email = editTextEmail.getText().toString();
                    pwd1 = editTextPass.getText().toString();
                    String pwd2 = editTexPassConfirm.getText().toString();
                    login = loginView.getText().toString();
                    login = GlobalHelper.formatLogin(login);
                    //check
                    if (!login.isEmpty() && !email.isEmpty() && !pwd1.isEmpty() && !pwd2.isEmpty()) {
                        if (login.length() < Constants.LOGIN_MIN_LENGTH) {
                            Toast.makeText(RegisterActivity.this, "login too short", Toast.LENGTH_LONG).show();
                        } else if (login.length() > Constants.LOGIN_MAX_LENGTH) {
                            Toast.makeText(RegisterActivity.this, "login too long", Toast.LENGTH_LONG).show();
                        } else {
                            if (pwd1.equals(pwd2)) {
                                registerUser();
                            } else {
                                Toast.makeText(RegisterActivity.this, "passwords do not math", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "please fill all fields", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    AlertDialog alertDialog =
                            new AlertDialog.Builder(RegisterActivity.this).
                                    setMessage(
                                    "You need to download Google Play Services in order to use the app")
                                    .create();
                    alertDialog.show();
                }

            }

        });
    }

    public void createUserWithEmailAndPassword(){
        LoggerHelper.debug("IN createUserWithEmailAndPassword");
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, pwd1)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        LoggerHelper.debug("createUserWithEmail:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if ( ! task.isSuccessful()) {

                            progressDialog.hide();

                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(RegisterActivity.this, "email already assigned.", Toast.LENGTH_SHORT).show();
                                LoggerHelper.error("User with this email already exist" + task.toString());
                            }
                            if (task.getException() instanceof FirebaseAuthEmailException) {
                                Toast.makeText(RegisterActivity.this, "email is badly formatted", Toast.LENGTH_SHORT).show();
                                LoggerHelper.error("User with this email already exist" + task.toString());
                            }
                            else{
                                Toast.makeText(RegisterActivity.this, "authentication failed " , Toast.LENGTH_SHORT).show();
                                LoggerHelper.error("authentication failed" + task.getException());
                            }
                        }
                        else {

                        }
                    }
                });
    }

    public void setAuthListener(){
        LoggerHelper.debug("IN setAuthListener");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    final String uid = user.getUid();
                    LoggerHelper.debug("onAuthStateChanged:signed_in:" + uid);

                    //clean stored prefs
                    PreferenceUtility.clear(RegisterActivity.this);

                    //saving auth preference : unique id used for user / pass auh
                    PreferenceUtility.setAuthPref(getApplication(),uid);

                    try {
                        //check that firebase token exists
                        checkToken();

                        LoggerHelper.debug(String.format("about to set pref login=%s email=%s", login,email));

                        PreferenceUtility.setLoginPref(RegisterActivity.this,login);
                        PreferenceUtility.setOtherLoginPref(RegisterActivity.this,login);

                        PreferenceUtility.setEmailPref(RegisterActivity.this,email);

                        DataPersistenceManager.persistUser(prefs, login, null);

                        //Phone number registration
                        Intent intent = new Intent (RegisterActivity.this, RegisterPhoneNumberActivity.class);
                        startActivity(intent);

                    } catch (Exception e) {
                        LoggerHelper.error("authentication error " + e);
                        Toast.makeText(RegisterActivity.this, "authentication failed ",
                                Toast.LENGTH_SHORT).show();
                        LoggerHelper.error("auth failed in checkToken method " + e);

                        return;
                    }
                    finally {
                        progressDialog.hide();
                    }
                } else {
                    // User is signed out
                    LoggerHelper.debug("RegisterActivity onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    //Todo : very important update token after expiration - or after reinstall of the app
    // Firebase Instance ID provides a unique identifier for each app instance and a mechanism
    // to authenticate and authorize actions (example: sending FCM messages).
    private void checkToken() throws IOException {

        //token is always deleted when new registration occur
        //if someone was login before it would be reusing the same RegID and we don't want that
//        FirebaseInstanceId.getInstance().deleteInstanceId();

        String storedToken = prefs.getString(Constants.FIREBASE_REG_ID, null);
        LoggerHelper.debug("IN checkToken with token=" + storedToken);

        if(storedToken == null){
            //deletes token
            if(FirebaseInstanceId.getInstance() != null) {
                storedToken = FirebaseInstanceId.getInstance().getToken();
                //save the token in prefs
                prefs.edit().putString(Constants.FIREBASE_REG_ID, storedToken).commit();
            }
        }
    }

    /*
   * registration process
   */
    //Todo : modify this method, we cannot iterate over all users, should be child("users").child(login)
    public void registerUser() {
        LoggerHelper.info(getClass().getName(),"registerUser");
        //Login is unique : check that Login is not alreay assigned
        DatabaseReference ref = DataPersistenceManager.db.child("users/"+login);
        //Query queryRef = ref.orderByChild("login");
        //Todo:replqce with   DatabaseReference ref = DataPersistenceManager.db.child("users/" + login);
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                       @Override
                       public void onDataChange(DataSnapshot dataSnapshot) {
                           LoggerHelper.info(this.getClass().getName(),"IN registerUser onDataChange with dataSnapshot = " +  dataSnapshot);

                           boolean loginExists = false;

                           if(dataSnapshot.hasChildren()){
                               loginExists = true;
                               LoggerHelper.info(getClass().getName(),"dataSnapshot != null + " + dataSnapshot);
                           }
                           else{
                               LoggerHelper.info(getClass().getName(),"dataSnapshot == null");
                           }

                           //check if login exists
                           if( ! loginExists){
                               try {
                                   createUserWithEmailAndPassword();
                               }
                               catch(Exception e){
                                   LoggerHelper.error(StatusCode.USER_REGISTRATION_ERROR.getMessage());
                                   Toast.makeText(
                                           RegisterActivity.this,
                                           StatusCode.USER_REGISTRATION_ERROR.getMessage(),
                                           Toast.LENGTH_LONG).show();
                                   progressDialog.hide();
                                   return;
                               }
                           }
                           else {
                               LoggerHelper.error(StatusCode.USER_ALREADY_EXISTS.getMessage());
                               Toast.makeText(
                                       RegisterActivity.this,
                                       StatusCode.USER_ALREADY_EXISTS.getMessage(),
                                       Toast.LENGTH_LONG).show();

                               progressDialog.hide();
                           }
                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {
                           // Do something about the error
                           LoggerHelper.error(StatusCode.TECHNICAL_ERROR.getMessage());
                           Toast.makeText(
                                   RegisterActivity.this,
                                   StatusCode.TECHNICAL_ERROR.getMessage(),
                                   Toast.LENGTH_LONG).show();

                           progressDialog.hide();
                       }
                   }
        );
    }

    //////////////////////////////////////////////////////////////

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

