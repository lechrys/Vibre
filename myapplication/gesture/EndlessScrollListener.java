package fr.myapplication.dc.myapplication.gesture;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AbsListView;

import fr.myapplication.dc.myapplication.activity.timeline.ITimeLineInfo;
import fr.myapplication.dc.myapplication.data.timeline.TimeLine;
import util.LoggerHelper;

public abstract class EndlessScrollListener implements AbsListView.OnScrollListener {

    ITimeLineInfo timeLineInfo;

    int animation_treshold = 4 ;

    //is limit reached ?
    private boolean limit_up_reached;
    private boolean limit_down_reached;

    //have we scrolled down after limit was reached ?
    private boolean did_scroll_down;
    private boolean did_scroll_up;

    //last position before list was scrolled
    private int lastVisiblePosition = -1;

    //LIMITS are dfferent if scroll from user or programmatical

    private int saved_top_y;
    private int total_scroll_limit;
    private long time_left_till_next_timeframe = 0;

    //when touch / scrolled by user

    public boolean user_touch = false;
    private boolean is_list_being_scrolled = false;
    private Handler handler;

    //scroll params to print
    public ScrollParameters scrollParameters;

    /////////////////////////////

    Runnable scroll_runnable;

    /////////////////////////////

    public EndlessScrollListener(ITimeLineInfo timeLineInfo) {
        this.timeLineInfo = timeLineInfo ;
        handler = new Handler(Looper.getMainLooper());
    }

    public abstract void loadMore(int page, int totalItemsCount);

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {

        if (totalItemCount > visibleItemCount) {

            if(timeLineInfo.isUserTouch()){
                LoggerHelper.info("timeLineInfo.isUserTouch()");
                timeLineInfo.setUserScroll(true);
            }

            else{
                LoggerHelper.info(" ! timeLineInfo.isUserTouch()");
            }

            //the list is scrollable

            scrollParameters = new ScrollParameters(view, firstVisibleItem);

            scrollParameters.print("ONSCROLL");

            //////////////////////////////////////////////
            //save position
            //////////////////////////////////////////////

            if(firstVisibleItem > lastVisiblePosition){
                LoggerHelper.debug(getClass() + " onScroll ","SCROLLING DOWN");
                setDid_scroll_up(false);
                setDid_scroll_down(true);
            }

            if(firstVisibleItem < lastVisiblePosition){
                LoggerHelper.debug(getClass() + " onScroll ","SCROLLING UP with RowLimitTop = " +
                        getRowLimitTop() + ", timeLineInfo.isUserScroll() = " + timeLineInfo.isUserScroll());
                setDid_scroll_down(false);
                setDid_scroll_up(true);
            }

                //////////////////////////////////////////////
                //check limit up
                //////////////////////////////////////////////

                if(firstVisibleItem < getRowLimitTop()
                    //by user scrolling we stop when on the limit timeframe
                    || timeLineInfo.isUserScroll() &&  firstVisibleItem == getRowLimitTop() && Math.abs(scrollParameters.getFirstVisibleItemTop()) >= 0
                    //by autoscrolling we stop when at half of limit timeframe
                    || firstVisibleItem == getRowLimitTop() && Math.abs(scrollParameters.getFirstVisibleItemTop()) <= scrollParameters.getFirstVisibleItemHeight() / 2 ){
    //            if(firstVisibleItem <= limit_item_top){
                    LoggerHelper.debug(getClass() + " onScroll ","limit_up_reached = true with did_scroll_down = " + did_scroll_down);
                    if( ! did_scroll_down) {
                        limit_up_reached = true;
                        LoggerHelper.debug(getClass() + " onScroll ","onScroll STOP SCROLLING");
                        stopScrollingUp(view);
                    }
                    //this.onScrollStateChanged(view,AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                }
                else{
                    LoggerHelper.debug(getClass() + " onScroll ","limit_up_reached = false");
                    limit_up_reached = false;
                }



            //////////////////////////////////////////////
            //check limit down
            //////////////////////////////////////////////

            if(firstVisibleItem + visibleItemCount >= timeLineInfo.getCurrentPosition() + 1 +  TimeLine.TOTAL_TIME_FRAMES){
                limit_down_reached = true;
                if( ! did_scroll_up) {
                    LoggerHelper.debug(getClass() + " onScroll ","STOP SCROLLING");
                    stopScrollingDown(view);
                }
            }
            else{
                LoggerHelper.debug(getClass() + " onScroll ", "limit_down_reached = false");
                limit_down_reached = false;
            }

            lastVisiblePosition = firstVisibleItem;

        }
        else{
            //list is not scrollable
            LoggerHelper.debug(getClass() + " onScroll"," totalItemCount < visibleItemCount");
        }
    }

    protected void stopScrollingUp(AbsListView view){
        view.smoothScrollBy(0,0);
        //limit_up_reached = true;
        is_list_being_scrolled = false;
    }

    protected void stopScrollingDown(AbsListView view){
        view.smoothScrollBy(0,0);
//        limit_down_reached = true;
        is_list_being_scrolled = false;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        int firstVisibleItem = view.getFirstVisiblePosition();

        scrollParameters = new ScrollParameters(view, firstVisibleItem);
        scrollParameters.print("ONSCROLL STATE CHANGED");

        //////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////
        //important to save the position again when scroll state is changed for exemple from fling to scroll touch
        //////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////

        //////////////////////////////////////////////
        //check limit up
        //////////////////////////////////////////////

        if(firstVisibleItem < getRowLimitTop() && !did_scroll_down
                || firstVisibleItem <= getRowLimitTop() && Math.abs(scrollParameters.getFirstVisibleItemTop()) <= scrollParameters.getFirstVisibleItemHeight() / 2  && !did_scroll_down){
            LoggerHelper.debug(getClass() + " onScrollStateChanged","firstVisibleItem <= limit_item_top && top_y >= limit_top_y");
            stopScrollingUp(view);
        }

/*        if(limit_up_reached && scrollState != 5 && ! did_scroll_down){
            LoggerHelper.debug(getClass(), "onScrollStateChanged STOP SCROLLING");
            stopScrollingUp(view);
        }
        if(limit_down_reached && scrollState != 4 && !did_scroll_up){
            LoggerHelper.debug(getClass()," onScrollStateChanged STOP SCROLLING");
            stopScrollingDown(view);
        }*/

        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                LoggerHelper.debug(getClass() + " onScrollStateChanged","SCROLL_STATE_IDLE");
                is_list_being_scrolled = false;
//                did_scroll_down = false;
//                did_scroll_up = false;

                if( ! timeLineInfo.isUserTouch()){
                    timeLineInfo.setUserScroll(false);
                }
                else {
                    timeLineInfo.setUserTouch(false);
                    timeLineInfo.setUserScroll(false);
                }

                //limit_top_y = saved_top_y;
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                LoggerHelper.debug(getClass() + " onScrollStateChanged","SCROLL_STATE_TOUCH_SCROLL");
                //List is scrolling under the direct touch of the user
                is_list_being_scrolled = true;

                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                LoggerHelper.debug(getClass() + " onScrollStateChanged","SCROLL_STATE_FLING");
                //The user did a 'fling' on the list and it's still scrolling
                is_list_being_scrolled = true;
                break;
            case 4:
                LoggerHelper.debug(getClass() + " onScrollStateChanged","case 4 SCROLL UP");
                did_scroll_down = false;
                did_scroll_up = true;
               // onScrollStateChanged(view,10);
                break;
            case 5:
                LoggerHelper.debug(getClass() + " onScrollStateChanged","case 5 SCROLL DOWN");
                did_scroll_down = true;
                did_scroll_up = false;
                break;
        }
    }


    public class ScrollParameters{

        int firstVisibleItem;
        int firstVisibleItemHeight;
        int firstVisibleItemTop;
        int limit_top_y;

        public ScrollParameters(AbsListView  view, int firstVisibleItem){
            this.firstVisibleItem = firstVisibleItem;
            View v = view.getChildAt(0);
            if(v != null) {
                this.firstVisibleItemHeight = v.getHeight();
                this.firstVisibleItemTop = v.getTop();
            }
        }

        public ScrollParameters(AbsListView  view, int firstVisibleItem, int limit_top_y){
            this.firstVisibleItem = firstVisibleItem;
            View v = view.getChildAt(0);
            if(v != null) {
                this.firstVisibleItemHeight = v.getHeight();
                this.firstVisibleItemTop = v.getTop();
            }
            this.limit_top_y = limit_top_y;
        }

        public void print(String origin){

            StringBuffer buffer = new StringBuffer();
            buffer.append(" firstVisibleItem = " + firstVisibleItem);
            buffer.append(" timeLineInfo.getCurrentPosition() = " + timeLineInfo.getCurrentPosition());
            buffer.append(" firstVisibleItemTop = " + firstVisibleItemTop);

            LoggerHelper.debug(getClass() + " " +  origin, buffer.toString());

        }

        public int getFirstVisibleItem() {
            return firstVisibleItem;
        }

        public void setFirstVisibleItem(int firstVisibleItem) {
            this.firstVisibleItem = firstVisibleItem;
        }

        public int getFirstVisibleItemHeight() {
            return firstVisibleItemHeight;
        }

        public void setFirstVisibleItemHeight(int firstVisibleItemHeight) {
            this.firstVisibleItemHeight = firstVisibleItemHeight;
        }

        public int getFirstVisibleItemTop() {
            return firstVisibleItemTop;
        }

        public void setFirstVisibleItemTop(int firstVisibleItemTop) {
            this.firstVisibleItemTop = firstVisibleItemTop;
        }

    }

    // because the list is almost not being scrolled + this is taking cpu for nothing
    // maybe wait if we are at least at NEXT_TIMEFRAME / 2
    /*
    * A new row is visible every time_left_till_next_timeframe
    * When a row becomes visible it pushes the rest of the rows suddenly and we want to avoid that
    * We want to scroll but we want to stop before reaching the top of the listView thats the reason
    * why we stop at row_height / 2
    */
    public void timelineAutoScroll() {

        scroll_runnable = new Runnable() {

            @Override
            public void run() {

                LoggerHelper.info(getClass(),"timelineAutoScroll.RUN");

                //to run only if user is not scrollig the screen
                if( ! timeLineInfo.isUserScroll()) {

                    View v = timeLineInfo.getListView().getChildAt(0);

                    if (v != null) {

                        int row_height = v.getHeight();

                        boolean canScroll = false;
                        int firstVisiblePosition = timeLineInfo.getListView().getFirstVisiblePosition();

                        //////////////////// PRINT ///////////////////////////

                        StringBuffer buffer = new StringBuffer();
                        buffer.append("  ########### ");
                        buffer.append(" currentPosition = ").append(timeLineInfo.getCurrentPosition());
                        buffer.append("  ########### ");
                        buffer.append(" getFirstVisiblePosition() = ").append(firstVisiblePosition);
                        buffer.append("  ########### ");
                        buffer.append(" row_height = ").append(row_height);
                        buffer.append("  ########### ");

                        LoggerHelper.info(getClass().getName(), "timelineAutoScroll" + buffer.toString());

                        //////////////////// PRINT ///////////////////////////

                        //save position

                        int rows_height_to_scroll = 0;
                        int distanceToFistVisiblePositionTop = 0;

                        if (firstVisiblePosition == timeLineInfo.getCurrentPosition()) {
                            LoggerHelper.info(getClass(), "firstRowPosition == currentPosition");
                            canScroll = true;
                            rows_height_to_scroll = row_height;
                            distanceToFistVisiblePositionTop = TimeLineOnTouchListener.getPixelToFirstVisiblePosition(timeLineInfo.getListView());
                        }

                        //otherwise check if we are on a position near the current one
                        else if (firstVisiblePosition - timeLineInfo.getCurrentPosition() <= animation_treshold) {
                            LoggerHelper.info("firstRowPosition - currentPosition  <= animation_treshold ");
                            canScroll = true;
                            rows_height_to_scroll = (firstVisiblePosition - timeLineInfo.getCurrentPosition() + 1) * row_height;
                            distanceToFistVisiblePositionTop = TimeLineOnTouchListener.getPixelToFirstVisiblePosition(timeLineInfo.getListView());
                        } else {
                            LoggerHelper.info("CANNOT SCROLL");
                        }

                        //we do not scroll until the top of the first row but we stop in the middle
                        //the we should never go to the top as rws above are hidden and when
                        // need to put negative  value on rows height in order to scroll backward
                        // scrollingup mean going backward
                        //total_scroll_limit = - rows_height_to_scroll + distanceToFistVisiblePositionTop - (row_height / 2);
                        //the distance to the top of the current row

                        total_scroll_limit = -rows_height_to_scroll + distanceToFistVisiblePositionTop;

                        //////////////////// PRINT ///////////////////////////

                        buffer = new StringBuffer();
                        buffer.append(" distanceToFistVisiblePoisitionTop = ").append(distanceToFistVisiblePositionTop);
                        buffer.append(" rows_height_to_scroll = ").append(rows_height_to_scroll);
                        buffer.append(" total_scroll_limit = ").append(total_scroll_limit);
                        buffer.append(" currentPosition = ").append(timeLineInfo.getCurrentPosition());
                        buffer.append(" firstRowPosition = ").append(firstVisiblePosition);

                        LoggerHelper.info("timelineAutoScroll", buffer.toString());

                        //////////////////// PRINT ///////////////////////////

                        time_left_till_next_timeframe = timeLineInfo.getTimeLine().calculateTimeTillNextTimeFrame();

                        //////////////////// PRINT ///////////////////////////

                        buffer.append(" time_left_till_next_timeframe = ").append(time_left_till_next_timeframe);
                        buffer.append(" total_scroll_limit = ").append(total_scroll_limit);
                        buffer.append(" canScroll = ").append(canScroll);
                        buffer.append(" is_list_being_scrolled = ").append(is_list_being_scrolled()
                        );

                        LoggerHelper.info("@@@@@@@@@@@ timelineAutoScroll", buffer.toString());


                        // decide to scroll or not
                        if (time_left_till_next_timeframe >= 0 && canScroll && ! is_list_being_scrolled()) {

                            LoggerHelper.info(EndlessScrollListener.this.getClass(), "########## timelineAutoScroll method #######  list ISN'T being scrolled");

                            //how much to scroll depending on time left
                        /*
                        *
                        *
                        * The closer we are to the end of the timeframe the faster
                        * */

                            if (time_left_till_next_timeframe < 1000 * 60 && time_left_till_next_timeframe > 1000 * 20) {
                                LoggerHelper.info(EndlessScrollListener.this.getClass(), "time_left_till_next_timeframe < 1000 * 60");
                                timeLineInfo.getListView().smoothScrollBy(total_scroll_limit, (int) time_left_till_next_timeframe);
                            } else if (time_left_till_next_timeframe > 1000 * 60 * TimeLine.TIMESCALE / 2) {
                                LoggerHelper.info(EndlessScrollListener.this.getClass(), "time_left_till_next_timeframe > 1000 * 60 * TimeLine.TIMESCALE / 2");
                                timeLineInfo.getListView().smoothScrollBy(total_scroll_limit / 4, (int) time_left_till_next_timeframe / 4);
                            } else if (time_left_till_next_timeframe < 1000 * 60 * TimeLine.TIMESCALE / 2) {
                                LoggerHelper.info(EndlessScrollListener.this.getClass(), "time_left_till_next_timeframe < 1000 * 60 * TimeLine.TIMESCALE / 2");
                                timeLineInfo.getListView().smoothScrollBy(total_scroll_limit / 2, (int) time_left_till_next_timeframe / 2);
                            }
                            else{
                                LoggerHelper.debug(getClass(),"Not going to scroll");
                            }
                        } else {
                            LoggerHelper.debug(getClass(), "########## timelineAutoScroll method #######  not allowed to scroll");
                        }
                    } else {
                        LoggerHelper.debug(getClass().getName(), "can't get child's height");
                    }
                }
                //every min
                handler.postDelayed(this, 10000L);
            }
        };
        //event is posted for the first time
        handler.postDelayed(scroll_runnable,5000L);
    }

    public Runnable getScroll_runnable() {
        return scroll_runnable;
    }

    public void removeCallBack(){
        if(handler != null){
            handler.removeCallbacks(scroll_runnable);
        }
    }
    ///////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////

    public boolean isUser_touch() {
        return user_touch;
    }

    public void setUser_touch(boolean user_touch) {
        this.user_touch = user_touch;
    }

    public boolean is_list_being_scrolled() {
        return is_list_being_scrolled;
    }

    public void setIs_list_being_scrolled(boolean is_list_being_scrolled) {
        this.is_list_being_scrolled = is_list_being_scrolled;
    }

    public boolean isDid_scroll_down() {
        return did_scroll_down;
    }

    public void setDid_scroll_down(boolean did_scroll_down) {
        this.did_scroll_down = did_scroll_down;
    }

    public boolean isDid_scroll_up() {
        return did_scroll_up;
    }

    public void setDid_scroll_up(boolean did_scroll_up) {
        this.did_scroll_up = did_scroll_up;
    }

    public int getSaved_top_y() {
        return saved_top_y;
    }

    public void setSaved_top_y(int saved_top_y) {
        this.saved_top_y = saved_top_y;
    }

    private int getRowLimitTop(){
        return timeLineInfo.getCurrentPosition() - 1;
    }
}