package fr.myapplication.dc.myapplication.data.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.commons.lang3.StringUtils;

import fr.myapplication.dc.myapplication.data.factory.DataFactory;
import fr.myapplication.dc.myapplication.data.model.BaseMessage;
import fr.myapplication.dc.myapplication.data.model.Contact;
import fr.myapplication.dc.myapplication.data.model.Invitation;
import fr.myapplication.dc.myapplication.data.model.LightMessage;
import fr.myapplication.dc.myapplication.data.model.User;
import util.Constants;
import util.LoggerHelper;
import util.PreferenceUtility;

/**
 * Created by Crono on 22/11/16.
 * Class to manage the Datas of the app
 * Methods are static
 */
public class DataPersistenceManager {

    public static DatabaseReference db;

    static{
        db = FirebaseDatabase.getInstance().getReference();
    }

    public static void registerUserId(final String userId){
        db.child(Constants.AUTH_KEY).child(userId).setValue("");
    }

    public static void updateUserFirebaseToken(final String login, Context context){
        final String newToken = PreferenceUtility.getToken(context);
        LoggerHelper.debug("IN updateUserFirebaseToken with login="+ login + " and newToken="+newToken);
        //will be updated only when newToken is found
        if( ! StringUtils.isEmpty(login) && ! StringUtils.isEmpty(newToken)){
            LoggerHelper.debug("updateUserFirebaseToken user updated with token " + newToken);
            db.child(Constants.USERS_KEY).child(login).child(Constants.FIREBASE_REG_ID).setValue(newToken);
        }
    }

    public static void updateUserFirebaseToken(Context context){
        final String login = PreferenceUtility.getLogin(context);
        final String newToken = PreferenceUtility.getToken(context);
        LoggerHelper.debug("IN updateUserFirebaseToken with login="+ login + " and newToken="+newToken);
        //will be updated only when newToken is found
        if( ! StringUtils.isEmpty(login) && ! StringUtils.isEmpty(newToken)){
            LoggerHelper.debug("updateUserFirebaseToken user updated with token " + newToken);
            db.child(Constants.USERS_KEY).child(login).child(Constants.FIREBASE_REG_ID).setValue(newToken);
        }
    }

    public static void storePhoneNumber(final String login, final String phone){

        //add phone number to phone node
        db.child(Constants.PHONE_KEY).child(phone).setValue(login);

        //add phone to user
        db.child(Constants.USERS_KEY).child(login).child(Constants.PHONE_KEY).setValue(phone);

    }

    public static void persistUser(final SharedPreferences prefs,
                                   final String login,
                                   final String tel){

        //shared prefs
        String authId = prefs.getString(Constants.AUTH_ID,null);
        String email = prefs.getString(Constants.EMAIL,null);
        String token = prefs.getString(Constants.FIREBASE_REG_ID,null);

        LoggerHelper.debug(String.format("IN persistUser with authId=%s - email=%s - token=%s",authId,email,token));

        //check authId, shouldnt happen
        if(authId == null){
            LoggerHelper.error("authId should never be null");
            return;
        }

        //create user Object
        User user = DataFactory.createUser(login,email,token,tel);

        //set the auth/login data
        db.child(Constants.AUTH_KEY).child(authId).setValue(login);

        //persist user with login
        db.child(Constants.USERS_KEY).child(login).setValue(user);

    }

    public static void updateUserLogin(final SharedPreferences prefs, final String otherLogin){

        //shared prefs
        String login = prefs.getString(Constants.LOGIN,null);

        if (StringUtils.isEmpty(login)) {
            LoggerHelper.warn("In updateUserLogin");
            return;
        }

        //update
        db.
        child(Constants.USERS_KEY)
        .child(login)
        .child("login").
        setValue(otherLogin);

    }

    public static void persistReceivedInvitation(final SharedPreferences prefs,
                                                 final String contactLogin
    ){

        //shared prefs
        String login = prefs.getString(Constants.LOGIN,null);
        LoggerHelper.debug("IN persistReceivedInvitation with login=" + login);

        //
        if(login == null){
            LoggerHelper.error("login should never be null at this point");
            return;
        }

        //create contact
        Invitation invitation = DataFactory.createInvitation(login);

        //persist contact
        db.
        child(Constants.USERS_KEY).
        child(contactLogin).
        child(Constants.INVITATION_KEY).
        child(Constants.RECEIVED_INVITATION_KEY).
        child(login).
        setValue(invitation);

        LoggerHelper.debug("out of persistReceivedInvitation with icon_invitation="+invitation.toString());
    }

    public static void removeReceivedInvitation(final SharedPreferences prefs,
                                                final String contactLogin
    ){

        //shared prefs
        String login = prefs.getString(Constants.LOGIN,null);
        LoggerHelper.debug("IN removeReceivedInvitation with login=" + login);

        //
        if(login == null){
            LoggerHelper.error("login should never be null at this point");
            return;
        }

        //persist contact
        db.
        child(Constants.USERS_KEY).
        child(login).
        child(Constants.INVITATION_KEY).
        child(Constants.RECEIVED_INVITATION_KEY).
        child(contactLogin).
        removeValue();
        //setValue(icon_invitation);

        LoggerHelper.debug("out of removeReceivedInvitation with icon_invitation=");
    }

    public static void persistAcceptedInvitation(final SharedPreferences prefs,
                                                 final String contactLogin
    ){

        //shared prefs
        String login = prefs.getString(Constants.LOGIN,null);
        LoggerHelper.debug("IN persistAcceptedInvitation with login=" + login);

        //
        if(login == null){
            LoggerHelper.error("login should never be null at this point");
            return;
        }

        Invitation invitation = DataFactory.createInvitation(login);

        //persist contact
        db.
        child(Constants.USERS_KEY).
        child(contactLogin).
        child(Constants.INVITATION_KEY).
        child(Constants.ACCEPTED_INVITATION_KEY).
        child(login).
        setValue(invitation);

        LoggerHelper.debug("out of persistAcceptedInvitation with icon_invitation=");
    }

    /*
    * Remove all accepted invitations
    */
    public static void removeAcceptedInvitation(final SharedPreferences prefs,
                                        final String contactLogin
    ){

        //shared prefs
        String login = prefs.getString(Constants.LOGIN,null);
        LoggerHelper.debug("IN removeReceivedInvitation with login=" + login);

        //
        if(login == null){
            LoggerHelper.error("login should never be null at this point");
            return;
        }

        //persist contact
        db.
        child(Constants.USERS_KEY).
        child(login).
        child(Constants.INVITATION_KEY).
        child(Constants.ACCEPTED_INVITATION_KEY).
        removeValue();

        LoggerHelper.debug("out of removeReceivedInvitation with icon_invitation=");
    }

    public static void persistContact(final SharedPreferences prefs,
                                      final String contactLogin,
                                      final boolean active
                                  ){

        //shared prefs
        String login = prefs.getString(Constants.LOGIN, null);
        LoggerHelper.debug("IN persistContact with login=" + login);

        //
        if(login == null){
            LoggerHelper.error("login should never be null at this point");
            return;
        }

        //create contact
        Contact contact = DataFactory.createContact(contactLogin,active);

        //persist contact
        db.
        child(Constants.USERS_KEY).
        child(login).
        child(Constants.CONTACTS_KEY).
        child(contactLogin).
        setValue(contact);

        //messages key MUST be empty
        db.
        child(Constants.USERS_KEY).
        child(login).
        child(Constants.CONTACTS_KEY).
        child(contactLogin).
        child(Constants.MESSAGES_KEY).
        setValue("");

        LoggerHelper.debug("out of persistContact with contact="+contact.toString());

    }

    public static void deleteContact(final String deleteRequester,
                                      final String contactToDelete
    ){

        //delete contact
        db.
        child(Constants.USERS_KEY).
        child(deleteRequester).
        child(Constants.CONTACTS_KEY).
        child(contactToDelete).
        removeValue();

        LoggerHelper.debug("out of deleteContact with contact to delete = "+contactToDelete);

    }

    public static void activateContact(final SharedPreferences prefs,
                                      final String contactLogin
    ){

        //shared prefs
        String login = prefs.getString(Constants.LOGIN,null);
        LoggerHelper.debug("IN activateContact with login=" + login);

        //
        if(login == null){
            LoggerHelper.error("login should never be null at this point");
            return;
        }

        //activate contact
        db.
        child(Constants.USERS_KEY).
        child(contactLogin).
        child(Constants.CONTACTS_KEY).
        child(login).
        child(Constants.ACTIVE_KEY)
        .setValue(true);

        LoggerHelper.debug("out of activateContact with contact="+contactLogin);

    }

    public static void persistMessage(final SharedPreferences prefs, final String selected_contact){

        //shared prefs
        String login = prefs.getString(Constants.LOGIN,null);

        //
        if(login == null){
            LoggerHelper.error("authId should never be null");
            return;
        }


        //generate id
        String id = db.push().getKey();

        LightMessage lightMessage = new LightMessage();

        //persist Message in contact node
        db.
        child(Constants.USERS_KEY).
        child(selected_contact).
        child(Constants.CONTACTS_KEY).
        child(login).
        child(Constants.MESSAGES_KEY).
        child(id).
        setValue(lightMessage);

        //persist Message in Messages node
        BaseMessage msg = DataFactory.createMessage(login,selected_contact);

        db.child(Constants.MESSAGES_KEY).child(id).setValue(msg);

        LoggerHelper.debug("VibrationMessage persisted with value="+msg.toString());

    }

    public static void markMessageAsRed(final SharedPreferences prefs,
                                      final String keyMessage,final String selected_contact){

        LoggerHelper.debug("IN markMessageAsRed");

        //shared prefs
        String login = prefs.getString(Constants.LOGIN,null);

        //
        if(login == null){
            LoggerHelper.error("authId should never be null");
            return;
        }

        //persist Message in message node
        db.
        child(Constants.MESSAGES_KEY).
        child(keyMessage).
        child("read").
        setValue(true);

        LoggerHelper.debug("markMessageAsRed with selected contact = " + selected_contact + " and login="+login + " and keymessage="+keyMessage);
        //persist again on contact in order to trigger OnChildChangedEvent
        db.
        child(Constants.USERS_KEY).
        child(login).
        child(Constants.CONTACTS_KEY).
        child(selected_contact).
        child(Constants.MESSAGES_KEY).
        child(keyMessage).
        setValue(true);
    }

    public static User getContactByLogin(final String login){
        return null;
    }

    public static User getContactByPhoneNumber(final String phoneNumber){
        return null;
    }
}
