package fr.myapplication.dc.myapplication.timeline;

import java.util.Calendar;

import util.LoggerHelper;

/**
 * Created by jhamid on 28/05/2017.
 */

public class TimeFrame {

    public int hour;
    public int min;
    Calendar calendar;

    public boolean is_intermediate_time;

    public TimeFrame(int hour, int min){

        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,min);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

        this.hour = hour;
        this.min = min ;

        is_intermediate_time = false;
    }

    public TimeFrame(Calendar calendar,boolean is_intermediate_time){
        this.calendar = calendar;
        this.is_intermediate_time = is_intermediate_time;
    }

    public TimeFrame(int hour, int min, boolean is_intermediate_time){
        this(hour,min);
        LoggerHelper.info("TimeFrame with calendar min " + min);
        LoggerHelper.info("TimeFrame with calendar day " + calendar.get(Calendar.DAY_OF_MONTH));
        this.is_intermediate_time = is_intermediate_time;
    }

    public int getHour(){
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public int getMin(){
        return calendar.get(Calendar.MINUTE);
    }

    public Calendar getCalendar(){
        return calendar;
    }


    @Override
    public String toString(){
        StringBuffer buf = new StringBuffer();
        buf.append(calendar.get(Calendar.HOUR_OF_DAY ) + " h ");
        buf.append(calendar.get(Calendar.MINUTE ) + " min ");
        buf.append(" is_intermediate_time=" + is_intermediate_time);
        return buf.toString();
    }
}
