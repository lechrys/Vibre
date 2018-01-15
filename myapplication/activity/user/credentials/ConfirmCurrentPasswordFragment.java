package fr.myapplication.dc.myapplication.activity.user.credentials;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.apache.commons.lang3.StringUtils;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.BasicActivity;
import util.LoggerHelper;
import util.PreferenceUtility;

/**
 * Created by jhamid on 25/11/2017.
 */

public class ConfirmCurrentPasswordFragment extends Fragment implements BasicActivity{

    LinearLayout parentLayout;
    TextView old_pass;
    Button validate;
    PasswordChange mCallback;

    //////////////////////////

    private FirebaseAuth mAuth;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            Activity a =(Activity) context;

            // This makes sure that the container activity has implemented
            // the callback interface. If not, it throws an exception
            try {
                mCallback  = (UpdatePasswordActivity) a;
            } catch (ClassCastException e) {
                throw new ClassCastException(a.toString()
                        + " must implement OnHeadlineSelectedListener");
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentLayout = (LinearLayout) inflater.inflate(R.layout.fragment_confirm_password, container, false);
        mAuth = FirebaseAuth.getInstance();
        return parentLayout;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        old_pass = (TextView) parentLayout.findViewById(R.id.pass);
        validate = (Button) parentLayout.findViewById(R.id.validate);
        validate.setVisibility(View.INVISIBLE);

        setButtonsListeners();
    }


    @Override
    public void setViews() {

    }

    @Override
    public void setButtonsListeners() {


        old_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(TextUtils.isEmpty(s)){
                    validate.setVisibility(View.INVISIBLE);
                }
                else{
                    validate.setVisibility(View.VISIBLE);
                }
            }
        });

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggerHelper.info(getClass(),"IN setbuttonListener");
                verifyPassword();
            }
        });
    }

    private void verifyPassword(){

        String email = PreferenceUtility.getEmailPref(getActivity());

        LoggerHelper.info(getClass(),"email = " + email);
        final String pass = old_pass.getText().toString();

        if( ! StringUtils.isEmpty(email) && ! StringUtils.isEmpty(pass)){

        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener
                (getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        LoggerHelper.info("signInWithEmail:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if ( ! task.isSuccessful()) {
                            LoggerHelper.warn("signInWithEmail" + task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            mCallback.swipeFragment();
                        }
                    }
                });
        }
        else{
            LoggerHelper.error(getClass(),"email & pass shouldnt be null at this point");
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    ////////////////////////////////////////////////////
    //The interface to be implemented by the Activity
    ////////////////////////////////////////////////////

    protected interface PasswordChange {
         void swipeFragment();
    }
}
