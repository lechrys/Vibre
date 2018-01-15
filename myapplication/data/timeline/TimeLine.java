package fr.myapplication.dc.myapplication.data.timeline;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.myapplication.dc.myapplication.data.model.BaseMessage;
import fr.myapplication.dc.myapplication.data.model.Contact;
import fr.myapplication.dc.myapplication.timeline.TimeFrame;
import fr.myapplication.dc.myapplication.view.adapter.CustomArrayAdapter;
import util.LoggerHelper;


/**
 * The timeline represents the events received on the last 24 hours
 * The timeline represents the events received on the last 24 hours
 * Row are initially created and fixed
 * Number of rows is : 24 * 60 / TIMEFRAME
 * 24 hours
 * 60 min
 * TIMEFRAME = nb rows in an hours
 */

//Todo: store datas of this class in context in order not to load it all the time
public class TimeLine {

    private static TimeLine timeLine;

    public static boolean initOccured;

    ////////////////////////////////////////////////////////////////////////////////////////////

    //Very important value which will cut the hour into (60 / TIMESCALE) rows

    public final static int TIMESCALE = 15 ; // in min
    public static short TOTAL_TIME_FRAMES = 24 * 60 / TimeLine.TIMESCALE;

    short added_time_frames = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////

    public List<TimeFrame> timeFrameList;
    public ArrayList<TimeLineData> timeLineDataList;

    ////////////////////////////////////////////////////////////////////////////////////////////

    CustomArrayAdapter adapter;

    public TimeLine(){
        init();
    }

    public static TimeLine getInstance(){
        if(timeLine == null){
            timeLine = new TimeLine();
        }
        return timeLine;
    }

    public void init(){
//        if( ! initOccured){
            timeFrameList = new ArrayList<>();
            timeLineDataList = new ArrayList<>();
            initTimeFrames();
            populateDataList();
//        }
/*        else{
            LoggerHelper.info(getClass(),"TimeLine init already occured");
        }*/
        //printTimeLineMessage();
    }

    public static boolean isInitOccured() {
        return initOccured;
    }

    public static void setInitOccured(boolean initOccured) {
        TimeLine.initOccured = initOccured;
    }

    public void setAdapter(CustomArrayAdapter adapter){
        this.adapter = adapter;
    }

    public long calculateTimeTillNextTimeFrame(){

        int added = added_time_frames;

        LoggerHelper.debug("IN calculateTimeTillNextTimeFrame with added_time_frames = "  + added_time_frames + " added_time_frames = " + added);

        if(added_time_frames == TOTAL_TIME_FRAMES - 1){
            added = 0  ;
        }

        int position;

        position = timeLineDataList.size() - 1 - added;

        LoggerHelper.debug("calculateTimeTillNextTimeFrame position = "  + position + " added_time_frames = " + added);

        Calendar most_recent_timeframe = timeLineDataList.get(position).getTime().getCalendar();
        Calendar nextTimeFrame = (Calendar) most_recent_timeframe.clone();
        nextTimeFrame.add(Calendar.DAY_OF_YEAR,1);

        //Todo: get the date from Firebase platform
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        LoggerHelper.debug(getClass(),"nextTimeFrame = " + nextTimeFrame.getTime() + " now = " + now);

        LoggerHelper.debug(getClass(),"calculateTimeTillNextTimeFrame return " + (nextTimeFrame.getTime().getTime() - now.getTime()));

        long result = nextTimeFrame.getTime().getTime() - now.getTime();

        if(result >= 0){
            return result;
        }

        return 0;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Set Timeline Rows
    ////////////////////////////////////////////////////////////////////////////////////////////

    public void initTimeFrames(){
        //actual time
        Date now = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);

        LoggerHelper.debug(TimeLine.class.getName(),String.format(Locale.getDefault(),"initTimeFrames method - hour is %d - min is %d",hour,min));

        populateTimeLine(hour,min);
    }

    protected void populateTimeLine(final int hour, final int min){

        //creating frames for the previous 23 hours

        TimeFrame timeFrame;

        ////////////////////////
        //Current Hour
        ////////////////////////

        List<TimeFrame> tmp = new ArrayList<>();

        int cpt_timeframe_for_current_hour = 0;

        for(int i = 1 ; i <= 60 / TIMESCALE ; i++){
            if((min - ( i * TIMESCALE )) >= 0){
                timeFrame = new TimeFrame(hour, i * TIMESCALE, true);
                tmp.add(timeFrame);
                LoggerHelper.debug(String.format("populateTimeLine hour = %d /  min = %d",timeFrame.getHour(), timeFrame.getMin()));
                cpt_timeframe_for_current_hour ++;
            }
            else{
                break;
            }
        }

        //list is reversed and datas are added
        for(int i = tmp.size() - 1  ; i >= 0; i --){
            timeFrameList.add(tmp.get(i));
        }

        //////////////////////////////////////////////////////////
        //Rest of the time line
        //////////////////////////////////////////////////////////

        for (int i = 0 ; i < ( 24 * 60 / TIMESCALE) - cpt_timeframe_for_current_hour ; i++){

            if(((i * TIMESCALE) % 60 ) == 0){
                timeFrame = new TimeFrame(hour, - i * TIMESCALE);
            }
            else{
                timeFrame = new TimeFrame(hour, - i * TIMESCALE,true);
            }

            LoggerHelper.debug(getClass().toString(),String.format("60 modulo i * TIMESCALE = %s creating frames timeframe = %s" ,(i * TIMESCALE) % 60, timeFrame.toString()));

            timeFrameList.add(timeFrame);
        }
    }

    /*
    *  Because the list is circular / infinite
    *  The last row is replaced filled with next timeframe
    * */
    public void populateDataListForNextTimeFrame(){

        //add data in second list

        LoggerHelper.warn(String.format("IN populateDataListForNextTimeFrame with added_time_frames" +
                " = %d / timeLineDataList.size() = %d ",added_time_frames,timeLineDataList.size()));

        //add in beginning of list

        int position = (timeLineDataList.size() - 1) - added_time_frames ;

        Calendar currentTimeFrameCalendar = timeLineDataList.get(position).getTime().getCalendar();
        currentTimeFrameCalendar.add(Calendar.DAY_OF_YEAR,1);

        LoggerHelper.warn(String.format("IN populateDataListForNextTimeFrame with calendar = %s / position = %d", currentTimeFrameCalendar.getTime(),position));

        TimeFrame timeFrame;

        if(((currentTimeFrameCalendar.get(Calendar.MINUTE) * TIMESCALE) % 60 ) == 0){
            timeFrame = new TimeFrame(currentTimeFrameCalendar,false);
        }
        else{
            timeFrame = new TimeFrame(currentTimeFrameCalendar,true);
        }

        if(added_time_frames == TOTAL_TIME_FRAMES - 1){
            added_time_frames = 0  ;
        }
        else {
            added_time_frames ++;
        }


        //change time
        timeLineDataList.get(position).setTime(timeFrame);

        //remove datas
        timeLineDataList.get(position).emptyContactList();

        //timeLineDataList.add(position,new TimeLineData(timeFrame,null));

        LoggerHelper.warn("timeFrame to be replaced by = " + timeFrame);
        LoggerHelper.warn("timeLineDataList.get(position).getTime = " + timeLineDataList.get(position).getTime().getCalendar().getTime());

        printTimeLineRows();

    }

    public void populateDataList(){
        //add data in second list
        for(TimeFrame timeFrame : timeFrameList){
            LoggerHelper.warn("IN populateDataList with timeframe hour = " + timeFrame.getHour() + " min = " + timeFrame.getMin());
            timeLineDataList.add(new TimeLineData(timeFrame,new ArrayList<Contact>()));
        }
    }

    public void printTimeLineRows(){
        //add data in second list
        for(TimeLineData row : timeLineDataList){
         //   LoggerHelper.error(String.format("##### row data date = %s - hour = %d ###### /m row Min = %d ", row.getTime().getCalendar().getTime().toString(),row.getTime().getHour(),row.getTime().getMin()));
        }

        LoggerHelper.warn("#### timeLine total rows = " + timeLineDataList.size());
    }

    public void printTimeLineMessage(){
        //add data in second list
        for(TimeLineData row : timeLineDataList){
            LoggerHelper.error(getClass().getName(),String.format("##### row data date = %s - hour = %d ###### /m row Min = %d ", row.getTime().getCalendar().getTime().toString(),row.getTime().getHour(),row.getTime().getMin()));
            for(Contact contact : row.getContactList()){
                LoggerHelper.error(getClass().getName(),"contact found for row : " + contact.getLogin());
            }
        }

        LoggerHelper.warn("#### timeLine total rows = " + timeLineDataList.size());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //                   Manage Messages on Timeline
    ////////////////////////////////////////////////////////////////////////////////////////////

    public void addMessageToTimeline(Contact contact, BaseMessage message){
        LoggerHelper.info("IN addMessageToTimeline");
        if(contact != null){
            LoggerHelper.debug("contact="+contact);
            Long creationDateLong;
            if(message != null && (creationDateLong = message.getDateLong()) != null){
                LoggerHelper.debug("message.friend_since=" + message.getDateLong());

                //messages older than now - 24h are discarded
                //set time to now
   /*             Map<String,String> dateMap = ServerValue.TIMESTAMP;
                Long dateLong = Long.valueOf(dateMap.get("date"));*/

                Date now = new Date();

                LoggerHelper.debug("IN addMessageToTimeline date now = " + now);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(now);

                LoggerHelper.debug(String.format("IN addMessageToTimeline calendar hour = %d #### min = %d",
                        calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE)));

                //one day in ms
                long day_time_long = 24L * 60L * 60L * 1000;
                long yesterday_time = now.getTime() - day_time_long ;

                //today - 24h
                Date yesterday_date = new Date(yesterday_time);
                LoggerHelper.debug("yesterday_date = " + yesterday_date);

                //message date
                Date message_creation_date = new Date(creationDateLong);
                LoggerHelper.debug("message.date = " + message_creation_date);

                if(message_creation_date.before(yesterday_date)){
                    LoggerHelper.debug("message is older than yesterday / message is discarded");
                }
                else{
                    //LoggerHelper.debug("message is recent / message from " + message.getFrom());

                    //message is added to timeLine
                    resolveMessageTimeFrame(message,contact.getLogin());
                }
            }
        }
    }

    //based on last 24 hours
    /*
    * take the reference time from firebase db ?
    */

    public void resolveMessageTimeFrame(BaseMessage message,final String login){

        Date date = new Date(message.getDateLong());
        LoggerHelper.debug("IN resolveMessageTimeFrame with message friend_since = " + date.toString());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);

        LoggerHelper.debug(TimeLine.class.getName(),String.format(Locale.getDefault(),"hour is %d - min is %d",hour,min));

        boolean timeFrameFound = false;

        // we start from the end of the list because more recent timeframes could be present since
        // they are added at the end of the list
        for(int i = 0 ; i + 1 < timeLineDataList.size() ; i ++){

//            LoggerHelper.debug(getClass().getName(),"IN resolveMessageTimeFrame with i = " + i);

            Calendar c1 = timeLineDataList.get(i).getTime().getCalendar();
            Calendar c2 = timeLineDataList.get(i + 1).getTime().getCalendar();

         /*   LoggerHelper.debug("IN resolveMessageTimeFrame with calendar_to_resolve = " + calendar.getTime());
            LoggerHelper.debug("IN resolveMessageTimeFrame with c1 = " + c1.getTime());
            LoggerHelper.debug("IN resolveMessageTimeFrame with c2 = " + c2.getTime());*/

            //usual case : older messages
            Calendar resolved_calendar;
            if((resolved_calendar = timeBetween(calendar,c1,c2)) != null){
                //do the job
                timeFrameFound = true;
                if(resolved_calendar == c1){
//                    LoggerHelper.warn("resolved_calendar == c1");
                    timeLineDataList.get(i).addData(message,login);
                    break;
                }
                else{
//                    LoggerHelper.warn("resolved_calendar == c2");
                    timeLineDataList.get(i + 1).addData(message,login);
                    break;
                }
                //out of loop
            }
        }

        //special case where the message is more recent than first timeframe
        if( ! timeFrameFound){
            Calendar c1;
            int current_pos = 0;
            if(added_time_frames > 0){
                current_pos = timeLineDataList.size() - added_time_frames;
                c1 = timeLineDataList.get(current_pos).getTime().getCalendar();
                LoggerHelper.debug(getClass().getName(),"added_time_frames > 0 with value = " + added_time_frames );
            }
            else {
                LoggerHelper.debug(getClass().getName(),"c1 = timeLineDataList.get(0).getTime().getCalendar()");
                c1 = timeLineDataList.get(0).getTime().getCalendar();
            }

            LoggerHelper.debug(getClass().getName(),"current_pos = " + current_pos);

            if(date.after(c1.getTime())){
                LoggerHelper.debug(getClass().getName(),"message is more recent than first time frame / will be part of first time frame ");
                TimeLineData timeLineData = timeLineDataList.get(current_pos);
                timeLineData.addData(message,login);
               // notifyAdapter();
            }
            else{
                LoggerHelper.error(getClass().getName(),"don't know where to put the message in timeline");
            }
        }
    }

    public Calendar timeBetween(final Calendar timeToResolve, final Calendar c1, final Calendar c2){
        Date dateToResolve = timeToResolve.getTime();

        if(dateToResolve.equals(c1.getTime())){
//            LoggerHelper.debug(getClass().getName(),"dateToResolve.equals(c1.getTime())");
            return c1;
        }

        if(dateToResolve.equals(c2.getTime())){
//            LoggerHelper.debug(getClass().getName(),"dateToResolve.equals(c2.getTime())");
            return c2;
        }

        if(timeToResolve.after(c2) && timeToResolve.before(c1)){
//            LoggerHelper.debug(getClass().getName(),"c1.getTime().getTime() - dateToResolve.getTime() = " + (c1.getTime().getTime() - dateToResolve.getTime()));
//            LoggerHelper.debug(getClass().getName(),"dateToResolve.getTime() - c2.getTime().getTime() = " + (dateToResolve.getTime() - c2.getTime().getTime()));
            if(c1.getTime().getTime() - dateToResolve.getTime()  <=  dateToResolve.getTime() - c2.getTime().getTime()){
//                LoggerHelper.debug(getClass().getName(),"return c1");
                return c1;
            }
//            LoggerHelper.debug(getClass().getName(),"return c2");
            return c2;
        }

        return null;
    }

    private void notifyAdapter(){
        adapter.notifyDataSetChanged();
    }


    public ArrayList<TimeLineData> getTimeLineDatas(){
        return timeLineDataList;
    }



    //////////////////////////////////////////////////////////
    // Data Holded by the timeline
    //////////////////////////////////////////////////////////

    public class TimeLineData {
        //time frame
        TimeFrame timeFrame;

        //contact List corresponding to that timeframe
        List<Contact> contactList;

        //faster to use a MAP objet to seach for contact
        Map<String,Integer> contactMessageList;

        TimeLineData(TimeFrame time){
            this.timeFrame = time;
            this.contactList = new ArrayList<>();
            contactMessageList = new HashMap<>();
        }

        TimeLineData(TimeFrame time, List<Contact> contactList){
            this.timeFrame = time;

            if(contactList == null){
                this.contactList = new ArrayList<>();
            }
            else {
                this.contactList = contactList;
            }
            contactMessageList = new HashMap<>();
        }

        public List<Contact> getContactList(){
            return contactList;
        }

        public TimeFrame getTime(){
            return timeFrame;
        }

        public void setTime(TimeFrame timeFrame){
            this.timeFrame = timeFrame;
            emptyContactList();
        }

        public void emptyContactList(){
            this.contactList = new ArrayList<>();
        }

        //Todo : have a HashMap <Login,Message> in order to avoid reading all the list
        private void addData(BaseMessage message,final String login){

            LoggerHelper.debug(getClass().getName(),"IN addData with TimeFrame = " + timeFrame.toString());

            if(contactMessageList.get(login) == null){
                LoggerHelper.debug(getClass().getName(),"contactMessageList.get(login) == null");
                contactMessageList.put(login,1);
            }

            else{
                LoggerHelper.debug(getClass().getName()," ! contactMessageList.get(login) == null");
                //get actual counter
                int counter = contactMessageList.get(login);
                //increment message counter
                counter++;
                //assign it
                contactMessageList.put(login,counter);
            }

            boolean already_exist_contact = false;

            if(contactList != null) {
                for (Contact contactElement : contactList) {
                    LoggerHelper.debug(getClass().getName(),"addData searching contact " + login + " found contact " + contactElement.getLogin());
                    if (contactElement.getLogin().equals(login)) {
                        LoggerHelper.debug(getClass().getName(),"contactElement.getLogin().equals(contact.getLogin()");
                        contactElement.getMessageList().add(message);
                        already_exist_contact = true;
                        break;
                    }
                }
            }

            if( ! already_exist_contact){
                LoggerHelper.debug(getClass().getName(),"!already_exist_contact");
                Contact new_contact = new Contact(login);
                new_contact.addMessage(message);
                contactList.add(new_contact);
            }

            if(timeLineDataList != null &&
                    timeLineDataList.get(0) != null &&
                    ! timeLineDataList.get(0).getContactList().isEmpty() &&
                    ! timeLineDataList.get(0).getContactList().get(0).getMessageList().isEmpty()) {
                LoggerHelper.debug(getClass().getName(), "OUT addData timeLineDataList.get(0).getContactList().size() " + timeLineDataList.get(0).getContactList().get(0).getMessageList().size());
            }
        }
    }
}
