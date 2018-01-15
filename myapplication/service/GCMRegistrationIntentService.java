package fr.myapplication.dc.myapplication.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import util.Constants;
import util.LoggerHelper;

/**
 * Created by chris on 03/05/2016.
 * Since google FCM (replacing GCM), onTokenRefresh is triggered only :
 *      * when first installing the app
 *      * when key rotation is needed on FCM side; thus token needs to be refreshed
 */
public class GCMRegistrationIntentService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        LoggerHelper.debug(getClass(),"IN onTokenRefresh");

        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        SharedPreferences prefs = this.getSharedPreferences(Constants.APP_ID, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.FIREBASE_REG_ID,refreshedToken).commit();

        LoggerHelper.debug(getClass(),"Refreshed token: " + refreshedToken);
        LoggerHelper.debug(getClass(),"Refreshed token: " + prefs.getString(Constants.FIREBASE_REG_ID,null));

        DataPersistenceManager.updateUserFirebaseToken(getApplicationContext());

    }
}
