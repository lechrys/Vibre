package fr.myapplication.dc.myapplication.data.helper;

/**
 * Created by jhamid on 15/07/2017.
 * This class allow us to know if all messages for a contact, have been Firebase loaded
 */

public class ContactCounter {

    //the contact login
    private String login;

    private Long total;

    private int current;

    public ContactCounter(){
        current = 0;
        total = 0L ;
    }

    public ContactCounter(long total){
        this.total = total;
        current = 0;
    }

    public int getCurrentCounter(){
        return current;
    }

    public void incrementCounter(){
        current ++;
    }

    public boolean areAllContactRead(){
        return current >= total - 1 ;
    }

}
