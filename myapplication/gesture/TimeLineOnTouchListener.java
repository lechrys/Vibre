package fr.myapplication.dc.myapplication.gesture;

import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;

import fr.myapplication.dc.myapplication.activity.timeline.ITimeLineInfo;
import util.LoggerHelper;

/**
 * Created by jhamid on 11/09/2017.
 */

public class TimeLineOnTouchListener implements OnTouchListener {

    float initialY, finalY;
    boolean isListScrollingUp;
    private EndlessScrollListener endlessScrollListener;
    private ITimeLineInfo timeLineInfo;
    private int row_height = 100 ;

    public TimeLineOnTouchListener(ITimeLineInfo timeLineInfo,
                                   EndlessScrollListener endlessScrollListener){

        this.timeLineInfo = timeLineInfo;
        this.endlessScrollListener = endlessScrollListener;

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        LoggerHelper.info(this.getClass().getName(),"IN onTouch with view = " + v);

        if(row_height == -1 ){
            View row = timeLineInfo.getListView().getChildAt(0);
            row_height = row.getHeight();
        }

        //action
        int action = MotionEventCompat.getActionMasked(event);
        int position = timeLineInfo.getListView().getFirstVisiblePosition();

        System.out.println("view v = " + v.getClass()) ;

        switch(action) {
            case (MotionEvent.ACTION_DOWN):
                LoggerHelper.info(getClass() + " onTouch ","MotionEvent.ACTION_DOWN");
                initialY = event.getY();
                timeLineInfo.setUserTouch(true);
                return false;

            case (MotionEvent.ACTION_UP):
                LoggerHelper.info(getClass() + " onTouch ","MotionEvent.ACTION_UP");
                if( ! timeLineInfo.isUserScroll()){
                    timeLineInfo.setUserTouch(false);
                }
                return false;

            case (MotionEvent.ACTION_MOVE):

                LoggerHelper.info(this.getClass().getName(),"MotionEvent.ACTION_MOVE");

                final int top_y2 = getPixelToFirstVisiblePosition(timeLineInfo.getListView());

                if (initialY < finalY) {
                    isListScrollingUp = true;
                    LoggerHelper.info(getClass() + " onTouch ", "ACTION_MOVE with FirstVisiblePosition = " + position + " limit_manual_scroll_position = " + (timeLineInfo.getCurrentPosition() - 1) + " top_y2 = " + top_y2);

                    if(position < timeLineInfo.getCurrentPosition() - 1
//                            || timeLineInfo.isUserScroll() && position == timeLineInfo.getCurrentPosition() - 1 && Math.abs(top_y2) >= 0
                            || position == timeLineInfo.getCurrentPosition() - 1 && Math.abs(top_y2) <= row_height * 2 / 3 ){
                        timeLineInfo.getListView().smoothScrollBy(0,0);
                        return true;
                    }
                    else {
                        endlessScrollListener.setDid_scroll_up(true);
                        endlessScrollListener.setDid_scroll_down(false);
//                        endlessScrollListener.onScrollStateChanged(timeLineInfo.getListView(), 4);
                    }
                } else if (initialY > finalY) {
                    LoggerHelper.info(getClass() + " onTouch ", " ACTION.MOVE Scrolling down with Y = " + + event.getY());
                    isListScrollingUp = false;
                    endlessScrollListener.setDid_scroll_down(true);
                    endlessScrollListener.setDid_scroll_up(false);
//                    endlessScrollListener.onScrollStateChanged(timeLineInfo.getListView(), 5);
                }
                break;

            default:
                return false;
        }

        if (isListScrollingUp) {
            // do animation for scrolling up

        } else {
            // do animation for scrolling down

        }

        // has to be false, or it will freeze the listView

        return false;
    }


    /*
    * gets number of pixels to the top of the current row
    * */
    public static int getPixelToFirstVisiblePosition(AbsListView listView){

        View row = listView.getChildAt(0);
//        int top = (row == null) ? 0 : (row.getTop() - listView.getPaddingTop());
        int y =  (row == null) ? 0 : row.getTop() ;
        LoggerHelper.info(TimeLineOnTouchListener.class,"IN getPixelToFirstVisiblePosition firstVisiblePos = " + y);

        return y;
    }


}
