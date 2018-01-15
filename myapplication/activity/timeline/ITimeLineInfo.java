package fr.myapplication.dc.myapplication.activity.timeline;

import android.widget.ListView;

import fr.myapplication.dc.myapplication.data.timeline.TimeLine;

/**
 * Created by jhamid on 14/10/2017.
 */

public interface ITimeLineInfo {
    int getCurrentPosition ();
    void updateCurrentPosition();
    ListView getListView();
    TimeLine getTimeLine();
    void setUserTouch(boolean userScroll);
    boolean isUserTouch();
    void setUserScroll(boolean userScroll);
    boolean isUserScroll();
}
