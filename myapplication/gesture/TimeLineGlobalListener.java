package fr.myapplication.dc.myapplication.gesture;

import android.os.Handler;
import android.os.Looper;

import fr.myapplication.dc.myapplication.activity.timeline.ITimeLineInfo;
import util.LoggerHelper;

/**
 * Created by jhamid on 14/10/2017.
 */

public class TimeLineGlobalListener {

    ITimeLineInfo timeLineInfo;
    EndlessScrollListener endlessScrollListener;
    TimeLineOnTouchListener timelineOnTouchListener;

    Handler handler;
    Runnable timerTask;

    //data
    long time_left_till_next_timeframe;

    public TimeLineGlobalListener(ITimeLineInfo timeLineInfo){

        this.timeLineInfo = timeLineInfo;

        this.endlessScrollListener = new EndlessScrollListener(this.timeLineInfo) {
            @Override
            public void loadMore(int page, int totalItemsCount) {
                LoggerHelper.info("onLoadMore");
            }
        };

        timelineOnTouchListener = new TimeLineOnTouchListener(this.timeLineInfo,endlessScrollListener);

        timeLineInfo.getListView().setOnTouchListener(timelineOnTouchListener);

        timeLineInfo.getListView().setOnScrollListener(endlessScrollListener);

        handler = new Handler(Looper.getMainLooper());

        initGlobalTimer();
    }

    /*
* actual time is needed
* @timescale : timescale diving each row of the list
*/
    private void initGlobalTimer(){
        time_left_till_next_timeframe = timeLineInfo.getTimeLine().calculateTimeTillNextTimeFrame();
        LoggerHelper.error(getClass(),"time_left_till_next_timeframe = " + time_left_till_next_timeframe);

        timerTask = new Runnable() {
            @Override
            public void run() {
                //add next time frame
                LoggerHelper.info(this.getClass(),"initGlobalTimer run");
                timeLineInfo.getTimeLine().populateDataListForNextTimeFrame();

                //we can change the current position
                timeLineInfo.updateCurrentPosition();
                LoggerHelper.info(this.getClass(),"currentPosition change to " + timeLineInfo.getCurrentPosition());

                //relaunch timer
                time_left_till_next_timeframe = timeLineInfo.getTimeLine().calculateTimeTillNextTimeFrame();
                handler.postDelayed(this,time_left_till_next_timeframe);
                LoggerHelper.info(this.getClass(),"time_left_till_next_timeframe to be called again with " + time_left_till_next_timeframe);
            }
        };

        //first post
        handler.postDelayed(timerTask,time_left_till_next_timeframe);
    }

    public EndlessScrollListener getEndlessScrollListener() {
        return endlessScrollListener;
    }

    public TimeLineOnTouchListener getTimelineOnTouchListener() {
        return timelineOnTouchListener;
    }

    public Runnable getTimerTask() {
        return timerTask;
    }


}
