package fr.myapplication.dc.myapplication.data.model;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import util.LoggerHelper;

/**
 * Created by Crono on 04/12/16.
 */

public class VibrationMessage implements Serializable, Comparable<VibrationMessage>,Comparator<VibrationMessage>{

    //array of loag representing the successive vibrations
    public String from;
    public String to;
    protected boolean read;
    //@PropertyName("pattern")
    protected List<Long> pattern;
    protected String key;
    //protected int messageType;
    //send friend_since
    protected Long date;

    public VibrationMessage(){}

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public java.util.Map<String, String> getDate() {
         return ServerValue.TIMESTAMP;
    }

    @Exclude
    public Long getDateLong(){
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

   public String getKey() {
       return key;
   }

    public void setKey(String key) {
        this.key = key;
    }


    public List<Long> getPattern() {
        return pattern;
    }

    public void setPattern(final List<Long> pattern) {
        this.pattern = pattern;
    }

    // The first value indicates the number of millisecond to wait before turning the vibrator on
    // 0 means vibration is instantly on
    public VibrationMessage init(){
        this.setPattern(new ArrayList<Long>());
        this.getPattern().add(0L);
        return this;
    }

    @Override
    public int compare(VibrationMessage m1, VibrationMessage m2) {
        LoggerHelper.info(this.getClass().getName(),"IN compare");
        int message_compare_to = m1.date.compareTo(m2.date);

        LoggerHelper.warn(String.format("m1 date = %d --- messageList2 date = %d ---- message_compare_to = %d",
                m1.date, m2.date, message_compare_to));

        return message_compare_to;
    }

    @Override
    public int compareTo(@NonNull VibrationMessage message) {
        LoggerHelper.warn(this.getClass().getName(),"IN compareTo");
        return this.date.compareTo(message.date);
    }
}

