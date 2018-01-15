package fr.myapplication.dc.myapplication.statistics;

import fr.myapplication.dc.myapplication.data.model.Contact;
import fr.myapplication.dc.myapplication.data.timeline.TimeLine;

/**
 * Created by jhamid on 18/09/2017.
 */


public class StatisticsHelper {

    public static Statistics getContactStats(final Contact contact, TimeLine timeLine){
        Statistics statistics = new Statistics();
        return statistics;
    }


    public static class Statistics{

        int messageLastHour;
        int messageLastweek;
        int messageLastMonth;

        public Statistics(){

        }
    }
}
