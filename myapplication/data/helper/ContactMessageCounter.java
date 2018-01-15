package fr.myapplication.dc.myapplication.data.helper;

/**
 * Created by jhamid on 15/07/2017.
 * This class allow us to know if all messages for a contact, have been Firebase loaded
 */

public class ContactMessageCounter {

    //the contact login
    private String login;

    private Long total;

    private int current;

    public ContactMessageCounter(final String login){
        this.login = login;
        current = 0;
        total = 0L ;
    }

    public ContactMessageCounter(long total, final String login){
        this.total = total;
        this.login = login;
        current = 0;
    }

    public int getCurrentCounter(){
        return current;
    }

    public synchronized void incrementCounter(){
        current ++;
    }

    public boolean areAllMessagesRead(){
        return current >= total - 1 ;
    }

    public String getContact(){
        return login;
    }

}
