package fr.myapplication.dc.myapplication.activity.user.credentials;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.apache.commons.lang3.StringUtils;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.BasicActivity;
import util.LoggerHelper;
import util.PreferenceUtility;

/**
 * Created by jhamid on 25/11/2017.
 */

public class NewPasswordFragment extends Fragment implements BasicActivity {

    LinearLayout parentLayout;
    TextView new_pass;
    TextView new_pass_verif;
    Button validate;
    ProgressDialog progressDialog;

    //////////////////////////

    Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentLayout = (LinearLayout) inflater.inflate(R.layout.fragment_new_password, container, false);
        return parentLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        new_pass = (TextView) parentLayout.findViewById(R.id.new_pass);
        new_pass_verif = (TextView) parentLayout.findViewById(R.id.new_pass_verif);
        validate = (Button) parentLayout.findViewById(R.id.validate);
        validate.setVisibility(View.INVISIBLE);

        setButtonsListeners();

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                LoggerHelper.error(getClass(), "handleMessage received s" + msg);
                getActivity().finish();
            }
        };
    }

    @Override
    public void setViews() {

    }


    //Todo : add logic for password length verification etc...
    @Override
    public void setButtonsListeners() {

        new_pass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    LoggerHelper.info(getClass(), "hasFocus");
                    new_pass_verif.setText("");
                }
            }
        });

        new_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                LoggerHelper.info(getClass(), "new_pass afterTextChanged");
                if (TextUtils.isEmpty(s)) {
                    validate.setVisibility(View.INVISIBLE);
                }
            }
        });

        new_pass_verif.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                LoggerHelper.info(getClass(), "newpassverif afterTextChanged");
                if ( ! StringUtils.isEmpty(s.toString()) && s.toString().equals(new_pass.getText().toString())) {
                    validate.setVisibility(View.VISIBLE);
                    validate.setClickable(true);
                } else {
                    validate.setVisibility(View.INVISIBLE);
                }
            }
        });

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggerHelper.info(getClass(), "IN setbuttonListener");
                validate.setClickable(false);
                changePassword();
            }
        });

    }

    private void changePassword() {


        String email = PreferenceUtility.getEmailPref(getActivity());

        LoggerHelper.info(getClass(), "email = " + email);
        final String new_pass_txt = new_pass.getText().toString();
        final String new_pass_verif_txt = new_pass.getText().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && !StringUtils.isEmpty(email) && !StringUtils.isEmpty(new_pass_txt) && !StringUtils.isEmpty(new_pass_verif_txt)) {

            dialog();

            user.updatePassword(new_pass_txt).addOnCompleteListener
                    (getActivity(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            LoggerHelper.info("signInWithEmail:onComplete:" + task.isSuccessful());
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if ( ! task.isSuccessful()) {
                                LoggerHelper.warn("password update failed" + task.getException());
                                Toast.makeText(getActivity(), "Password update failed.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                LoggerHelper.info(getClass(), "password successfully updated");

                                //password changed successfully
 /*                               dialog = new AlertDialog.Builder(NewPasswordFragment.this.getContext())
                                        //set message, title, and icon
                                        .create();

                                dialog.setTitle("password successfully updated");

                                if(dialog.getWindow() != null) dialog.getWindow().setLayout(600, 400);

                                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {


                                    }
                                });*/

                                Toast.makeText(NewPasswordFragment.this.getContext(),
                                        "password successfully updated", Toast.LENGTH_SHORT).show();

                                Message message = handler.obtainMessage();

                                //handler.sendMessageDelayed(message, 3000);


                            }
                            progressDialog.dismiss();
                        }
                    });
        } else {
            LoggerHelper.error(getClass(), "email & pass shouldnt be null at this point");
        }
    }

    private void dialog() {

        progressDialog = new ProgressDialog(NewPasswordFragment.this.getContext());
        progressDialog.setMessage("updating password...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(true);
        progressDialog.show();

        if ( progressDialog.getWindow() != null) progressDialog.getWindow().setLayout(600, 400);

        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                LoggerHelper.info(getClass(),"dialog dismissed");
                getActivity().finish();
            }
        });
    }
}
