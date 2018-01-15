package fr.myapplication.dc.myapplication.activity.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.apache.commons.lang3.StringUtils;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.BasicActivity;
import fr.myapplication.dc.myapplication.activity.MainActivity;
import fr.myapplication.dc.myapplication.activity.registration.RegisterActivity;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import util.ConnectionHelper;
import util.LoggerHelper;
import util.PreferenceUtility;
import util.ValidationHelper;

/**
 * A login screen that offers login via email/password.
 */
//Todo: progressbar or some indicator durign the login process
public class LoginActivity extends AppCompatActivity implements BasicActivity
//        implements LoaderCallbacks<Cursor>, BasicActivity
{
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    private AutoCompleteTextView emailTextView;
    private EditText PasswordView;
    private View mLoginFormView;
    private Button connect;
    private TextView signup;
    private TextView forgot_pass;
    private ProgressDialog progressDialog;

    //Authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LoggerHelper.info(getClass(),"IN onCreate");

        setViews();

        setButtonsListeners();

        setAuthListener();
    }

    @Override
    public void setViews() {
        setContentView(R.layout.activity_login);
        // Set up the login form.
        emailTextView = (AutoCompleteTextView) findViewById(R.id.emailText);
        signup = (TextView) findViewById(R.id.link_signup);
        PasswordView = (EditText) findViewById(R.id.editTextCoPassword);
  //      mProgressView.setMax(100);
        connect = (Button) findViewById(R.id.email_sign_in_button);
        mLoginFormView = findViewById(R.id.login_form);
        forgot_pass = (TextView) findViewById(R.id.forgot_password);

        if( progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Login...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
        }
    }

    @Override
    public void setButtonsListeners() {

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });


        PasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.emailText || id == EditorInfo.IME_NULL) {
                    return true;
                }
                return false;
            }
        });


        connect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ConnectionHelper.isGooglePlayServicesAvailable(LoginActivity.this)) {
                    if (!StringUtils.isEmpty(emailTextView.getText().toString()) || !StringUtils.isEmpty(PasswordView.getText().toString())) {
                        LoggerHelper.info(getClass(), "connect.setOnClickListener");

                        attemptLogin();
                    } else {
                        Toast.makeText(LoginActivity.this, "Veuillez entrer votre login et votre mot de passe !", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    AlertDialog alertDialog =
                            new AlertDialog.Builder(LoginActivity.this).
                                    setMessage(
                                            "You need to download Google Play Services in order to use the app")
                                    .create();
                    alertDialog.show();
                }
            }
        });

        forgot_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggerHelper.info(getClass(),"onClick");
                if( ! StringUtils.isEmpty(emailTextView.getText().toString())){
                    LoggerHelper.info(getClass(),"email is not empty");
                    reAuthenticate();
                }
                else{
                    LoggerHelper.info(getClass(),"email is empty");
                    Toast.makeText(LoginActivity.this, "please verify your email", Toast.LENGTH_LONG).show();
                    emailTextView.requestFocus();
                }
            }
        });

    }

    private void reAuthenticate(){
        LoggerHelper.info("IN reAuthenticate");
        mAuth.sendPasswordResetEmail(emailTextView.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "We have sent you instructions to reset your password", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Failed to send reset email, please verify the email", Toast.LENGTH_SHORT).show();
                    emailTextView.requestFocus();
                }
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        LoggerHelper.debug("IN attemptLogin");
        // Reset errors.
        emailTextView.setError(null);
        PasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = emailTextView.getText().toString();
        String password = PasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if ( ! TextUtils.isEmpty(password) && ! ValidationHelper.isPasswordValid(password)) {
            PasswordView.setError(getString(R.string.error_invalid_password));
            focusView = PasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailTextView.setError(getString(R.string.error_field_required));
            focusView = emailTextView;
            cancel = true;
        } else if (!ValidationHelper.isEmailValid(email)) {
            emailTextView.setError(getString(R.string.error_invalid_email));
            focusView = emailTextView;
            cancel = true;
        }

        // Check for valid pass
        if (TextUtils.isEmpty(password)) {
            PasswordView.setError(getString(R.string.error_field_required));
            focusView = emailTextView;
            cancel = true;
        } else if (!ValidationHelper.isPasswordValid(password)) {
            PasswordView.setError(getString(R.string.error_invalid_email));
            focusView = emailTextView;
            cancel = true;
        }
        
        if (cancel) {
            LoggerHelper.info("cancel");
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            LoggerHelper.info("ok");
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);
            //Firebase Authentication
            signIn(email,password);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
/*    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
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
    }*/



    public void signIn(final String email, final String pass){
        LoggerHelper.debug("IN signIn");
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener
            (this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    LoggerHelper.info("signInWithEmail:onComplete:" + task.isSuccessful());
                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if ( ! task.isSuccessful()) {
                        LoggerHelper.warn("signInWithEmail" + task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }

                    progressDialog.dismiss();
                }
            });
    }

    private void setAuthListener(){
        // responds to changes in the user's sign-in state
        LoggerHelper.debug("IN setAuthListener");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    final String uid = user.getUid();
                    LoggerHelper.info("onAuthStateChanged:signed_in:" + uid);

                    //saving auth preference
                    PreferenceUtility.setAuthPref(getApplication(), uid);

                    //Todo : get the login from DB if empty using auth node
                    final String login = PreferenceUtility.getLogin(getApplication());
                    LoggerHelper.info("login = " + login);

                    //Todo: check for any login problems
                 /*   if(StringUtils.isEmpty(login)){
                        //login was not provisionned on previous inscription
                        //login is requested
                        Intent intent = new Intent(LoginActivity.this, InscriptionActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else {
                    */

                    //register token

                    //TODO : update only if necessary
                    //update token if necessary
                    DataPersistenceManager.updateUserFirebaseToken(login,LoginActivity.this);

                    PreferenceUtility.setEmailPref(getApplication(), firebaseAuth.getCurrentUser().getEmail());

                    // Authenticated successfully with authData
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                   // }
                } else {
                    // User is signed out
                    LoggerHelper.info("LoginActivity onAuthStateChanged:signed_out");
                }
            }
        };
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

        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}

