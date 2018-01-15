package fr.myapplication.dc.myapplication.activity.registration;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.MainActivity;
import fr.myapplication.dc.myapplication.activity.user.AccountActivity;
import fr.myapplication.dc.myapplication.activity.user.LoginActivity;
import fr.myapplication.dc.myapplication.activity.user.SettingsActivity;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import util.Constants;
import util.GlobalHelper;
import util.LoggerHelper;
import util.PreferenceUtility;

/**
 * Created by jhamid on 26/08/2017.
 */

public class RegisterPhoneNumberActivity extends AppCompatActivity {

    //UI
    EditText phone;
    Button validate_button;
    Button skip_button;
    Button skip_button_hidden;
    CountryCodePicker ccp;
    private ProgressDialog mProgressDialog;
    private Button verifyButton;
    private TextView verifyText;

    private RelativeLayout validateCodeLayout;
    private LinearLayout getCodeLayout;

    //firebase
    private FirebaseAuth mAuth;

    //callBacks
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    //data
    boolean isVerificationInProgress;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    String mVerificationId;
    String phoneNumber;
    int selected_country_code;

    //caller activity
    String parent_activity;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        Bundle b = getIntent().getExtras();

        if(b != null){
            parent_activity = b.getString(Constants.PARENT_ACTIVITY);
            //changing theme before setCotnentView
            setTheme(R.style.AppTheme);
            initActionBar();
        }

        if( ! StringUtils.isEmpty(parent_activity)){
            LoggerHelper.info(getClass(),"found parent_activity property with value = " + parent_activity);
        }

        setContentView(R.layout.activity_signup_phone);

        phone = (EditText) findViewById(R.id.phone_text);
        //phone.addTextChangedListener(mPhoneWatcher);
        validate_button = (Button) findViewById(R.id.validateButton);
        skip_button = (Button) findViewById(R.id.skipButton);
        skip_button_hidden = (Button) findViewById(R.id.skipButtonHidden);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        selected_country_code = ccp.getDefaultCountryCodeAsInt();
        validateCodeLayout = (RelativeLayout) findViewById(R.id.validateCodeLayout);
        getCodeLayout = (LinearLayout) findViewById(R.id.getCodeLayout);
        verifyButton = (Button) findViewById(R.id.verifyButton);
        verifyText = (TextView) findViewById(R.id.verifyText);

        LoggerHelper.info(getClass().getName(),"selected_country_code = " + selected_country_code);

        //listeners
        setlisteners();

        //
        setVerifyButtonListener();

        //set callbacks
        setmCallBacks();
    }

    public void initActionBar(){
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

/*    @Override
    public void onBackPressed() {
        LoggerHelper.info(getClass(),"IN onBackPressed");
        if( ! StringUtils.isEmpty(parent_activity)){
          Intent intent = new Intent(RegisterPhoneNumberActivity.this, AccountActivity.class);
           startActivity(intent);
        }
    }*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        LoggerHelper.info(getClass(),"IN onKeyDown with event " + event + " and keycode = " + keyCode);
/*        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }*/
        super.onKeyDown(keyCode,event);
        return true;
    }

    //to resume the phone number sign in process if your app closes before the user can sign in
    @Override
    public void onStart(){
        super.onStart();
        LoggerHelper.info(this.getClass().getName(),"onStart");
        if(isVerificationInProgress){
            verify();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("isVerificationInProgress",isVerificationInProgress);
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle){
        super.onRestoreInstanceState(bundle);
        isVerificationInProgress = bundle.getBoolean("isVerificationInProgress");
    }

    private void setlisteners(){
        validate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggerHelper.info("validate_button clicked");
                verify();
            }
        });

        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                //Toast.makeText(this,"Updated ", Toast.LENGTH_SHORT).show();
                selected_country_code = ccp.getSelectedCountryCodeAsInt();
                LoggerHelper.info(getClass().getName(),"updated selected_country_code = " + selected_country_code);
            }
        });

        if( ! StringUtils.isEmpty(parent_activity)){
            skip_button.setVisibility(View.INVISIBLE);
        }
        else {
            skip_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoggerHelper.info("skip_button clicked");
                    nextActivity();
                }
            });

            skip_button_hidden.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoggerHelper.info("skip_button clicked");
                    nextActivity();
                }
            });
        }
    }

    private void setmCallBacks(){

        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verificaiton without
            //     user action.
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                LoggerHelper.info(this.getClass().getName(),"onVerificationCompleted");
                LoggerHelper.info(this.getClass().getName(),
                        String.format("phoneAuthCredential.getSmsCode = %s ////// phoneAuthCredential.getProvider = %s" ,
                                phoneAuthCredential.getSmsCode(),phoneAuthCredential.getProvider()));

                //flag is cleared
                isVerificationInProgress = false;

                //signIn
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                LoggerHelper.info(this.getClass().getName(),"onVerificationFailed");
                //flag is cleared
                isVerificationInProgress = false;

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    LoggerHelper.info(getClass().getName(),"Invalid request " + e);
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                    LoggerHelper.info(getClass().getName(),"The SMS quota for the project has been exceeded " + e);
                }

                Toast.makeText(RegisterPhoneNumberActivity.this,"phone verification failed " + e,Toast.LENGTH_SHORT).show();

                hideProgressDialog();
            }

            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId,token);
                hideProgressDialog();
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                LoggerHelper.info(this.getClass().getName(), "IN onCodeSent with verificationId = " + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                //Prompt the user for received code
                //hide
                getCodeLayout.setVisibility(View.GONE);

                //show
                validateCodeLayout.setVisibility(View.VISIBLE);

                if( ! StringUtils.isEmpty(parent_activity)){
                    skip_button_hidden.setVisibility(View.INVISIBLE);
                }
               // signInWithPhoneAuthCredential(credential);
            }

        };
    }

    private void setVerifyButtonListener(){
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggerHelper.info(getClass().getName(),"setVerifyButtonListener onClick");
                // build PhoneAuthCredential object
                PhoneAuthCredential credential = new PhoneAuthCredential(mVerificationId,verifyText.getText().toString());
                signInWithPhoneAuthCredential(credential);
            }
        });
    }


    private void verify(){

        //showProgressDialog();

        phoneNumber = phone.getText().toString();
        phoneNumber = GlobalHelper.formatE164Number(ccp.getSelectedCountryNameCode(),phoneNumber);

        LoggerHelper.info("IN verify with phoneNumber = " + phoneNumber);

        if( ! StringUtils.isEmpty(phoneNumber)){

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,        // Phone number to verify
                    30,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    mCallbacks);        // OnVerificationStateChangedCallbacks

            isVerificationInProgress = true;

        }
        else{
            Toast.makeText(this,"phone format error",Toast.LENGTH_SHORT).show();

        }
    }

    private void showError(final String error){
        Toast.makeText(this,error,Toast.LENGTH_SHORT).show();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            LoggerHelper.info(this.getClass().getName(), "signInWithCredential:success ");
                            FirebaseUser user = task.getResult().getUser();
                            LoggerHelper.info(String.format("user info email %s /// displyaName = %s", user.getEmail(),user.getDisplayName()));

                            //phone is stored
                            storeUserPhone();

                        } else {
                            // Sign in failed, display a message and update the UI
                            LoggerHelper.warn(this.getClass().getName(), "signInWithCredential:failure - " + task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                showError("Verification code error");
                            }
                            if (task.getException() instanceof FirebaseNetworkException) {
                                showError("Network error");
                            }

                        }
                        hideProgressDialog();
                    }
                });
    }


    //Store user phone in order to look it up by phone number
    private void storeUserPhone(){
        LoggerHelper.info(this.getClass().getName(),"IN storeUserPhone");

        final String login = PreferenceUtility.getLogin(this);

        if( ! StringUtils.isEmpty(login) && ! StringUtils.isEmpty(phoneNumber)){

            LoggerHelper.info(this.getClass().getName(),"login = " + login);

            //Todo : parse the phone number to have + indicativ mandatory
            DataPersistenceManager.storePhoneNumber(login,phoneNumber);

            PreferenceUtility.setPhonePref(getApplication(),phoneNumber);

            //link to email account
            linkToEmailAccount();

            //go to Main Activity
            nextActivity();
        }
        else{
            LoggerHelper.warn(this.getClass().getName(),"login & phone number shouldnt be empty at this point");
            //we can disconnect
            signOut();
        }
    }

    //Todo : link to email
    private void linkToEmailAccount(){
/*        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        mAuth.getCurrentUser().linkWithCredential(credential);*/
    }

    private void nextActivity(){
        LoggerHelper.info("IN nextActivity");
        if( ! StringUtils.isEmpty(parent_activity)){
            Intent intent = new Intent(RegisterPhoneNumberActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(RegisterPhoneNumberActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Loading...");
        }
        mProgressDialog.show();
    }
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    ////////////////////////////////////////////////

    @Override
    public void onDestroy(){
        super.onDestroy();
        FirebaseAuth.getInstance().signOut();
    }

    private void signOut(){
        finishAffinity();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
}
