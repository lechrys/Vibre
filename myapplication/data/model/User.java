package fr.myapplication.dc.myapplication.data.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by Crono on 21/11/16.
 * The login is unique and can be seen as the primary key
 * The regid is unique but can be update by FCM
 * the email is unique
 */

public class User implements IUser{

    private String login;
    private String name_on_phone;
    private String phone;
    private String regid;
    private String email;
    private List<Contact> contactList;
    protected Long creationDate;

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    public User(){}

    public List<Contact> getContactList() {
        return contactList;
    }

    public void setContactList(List<Contact> contactList) {
        this.contactList = contactList;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getRegid() {
        return regid;
    }

    public void setRegid(String regid) {
        this.regid = regid;
    }

    public java.util.Map<String, String> getCreationDate() {
        return ServerValue.TIMESTAMP;
    }

    @Exclude
    public Long getCreationDateLong(){
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public boolean equals(Object user){

        if(user == null){
            return false;
        }

        if(user instanceof User) {

            if (((User) user).getPhone() == null && this.getPhone() == null) {
                return true;
            }
            if (((User) user).getPhone() != null  && ((User) user).getPhone().equals(this.getPhone())) {
                return true;
            }

        }

        return false;
    }

    @Exclude
    public String getName_on_phone() {
        return name_on_phone;
    }

    @Exclude
    public void setName_on_phone(String name_on_phone) {
        this.name_on_phone = name_on_phone;
    }

    //carries only login + activation state
    public static User getUserLight(final User srcContact){
        User light_contact = new User();
        light_contact.setLogin(srcContact.getLogin());
        light_contact.setPhone(srcContact.getPhone());
        return light_contact;
    }
}
