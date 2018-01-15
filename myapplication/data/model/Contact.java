package fr.myapplication.dc.myapplication.data.model;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import util.LoggerHelper;

/**
 * Created by Crono on 21/11/16.
 * The login is unique and can be seen as the primary key
 * The regid is unique but can be update by FCM
 * the emqil is unique
 */

public class Contact implements Serializable,IContact,Cloneable, Comparable<Contact>{

    //private String reg_id;
    public String login;
    public String telephone;
    public boolean active;
    protected Long creationDate;
    public List<BaseMessage> messageList;
    public List<String> messageKeyList;

    @SuppressWarnings("unused")
    public Contact(){
    }

    public Contact(String login){
        this.login = login;
    }

    //used in TimeLineAdapter
    //do not use in other adapters
    public Contact (final Contact contact){
        this.login = contact.getLogin();
        this.messageList = contact.getMessageList();
    }

    public String getLogin() {
        return login;
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

    public void setLogin(String login) {
        this.login = login;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String toString(){

        StringBuilder builder = new StringBuilder();

        builder.append("\n login = ").append(login).append("\n");
        builder.append("telephone = ").append(telephone).append("\n");
        builder.append("active = ").append(active).append("\n");
        builder.append("date = ").append(creationDate).append("\n");

        return  builder.toString();
    }
    public List<BaseMessage> getMessageList() {
        if(messageList == null) {
            messageList = new ArrayList<>();
        }
        return messageList;
    }

    public void initMessageList(){
        messageList = new ArrayList<>();
    }

    public void setMessageList(List<BaseMessage>  message) {
        this.messageList = message;
    }

    public void addMessage(final BaseMessage msg){
        if(messageList == null)
            messageList = new ArrayList<>();
        //add accordingly to date
        messageList.add(msg);
    }

    public List<String> getMessageKeyList() {
        return messageKeyList;
    }

    public void setMessageKeyList(List<String> messageKeyList) {
        this.messageKeyList = messageKeyList;
    }

    public void addMessageKey(final String key){
        if(messageKeyList == null)
            messageKeyList = new ArrayList<String>();

        messageKeyList.add(key);
    }

    //carries only login + activation state
    public static Contact getContactLight(final Contact srcContact){

        Contact light_contact = new Contact();
        light_contact.setLogin(srcContact.getLogin());
        light_contact.setActive(srcContact.isActive());

        return light_contact;
    }

    //following methods ares excluded when object is being transformed in JSON / written in Firebase
    // used to sort the Collection
    @Override
    public int compareTo(@NonNull Contact contact) {

        LoggerHelper.info(this.getClass().getName(),"IN compareTo");

        //sorting datas first
        List<BaseMessage> messageList1 = this.getMessageList();
        List<BaseMessage> messageList2 = contact.getMessageList();

        if(messageList2 == null || messageList2.isEmpty()){
            return -1;
        }
        if(messageList1 == null || messageList1.isEmpty()){
            return 1;
        }

        Collections.sort(messageList1);
        Collections.sort(messageList2);

        int contact_compare_to = messageList1.get(messageList1.size() - 1).compare
                (messageList1.get(messageList1.size() - 1), messageList2.get(messageList2.size() - 1 ));

        LoggerHelper.debug(String.format("messageList1 date = %d --- messageList2 date = %d ---- contact_comparer_to = %d",
                messageList1.get(messageList1.size() - 1).date,messageList2.get(messageList2.size() - 1).date, contact_compare_to));

        if(contact_compare_to < 0){
            return 1;
        }
        if(contact_compare_to > 0){
            return -1;
        }

        return 0;
    }

}
