package fr.myapplication.dc.myapplication.data.model;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.Comparator;

import util.LoggerHelper;

/**
 * Created by jhamid on 16/09/2017.
 */

public abstract class Message <T extends Message> implements Serializable,Comparable<T>,Comparator<T> {

    protected Long date;

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

    @Override
    public int compare(T m1, T m2) {
        LoggerHelper.info(this.getClass().getName(),"IN compare");
        int message_compare_to = m1.date.compareTo(m2.date);

        LoggerHelper.warn(String.format("m1 date = %d --- messageList2 date = %d ---- message_compare_to = %d",
                m1.date, m2.date, message_compare_to));

        return message_compare_to;
    }

    @Override
    public int compareTo(@NonNull T message) {
        LoggerHelper.warn(this.getClass().getName(),"IN compareTo");
        return this.date.compareTo(message.date);
    }

}
