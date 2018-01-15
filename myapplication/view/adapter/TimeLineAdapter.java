package fr.myapplication.dc.myapplication.view.adapter;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.timeline.ITimeLineInfo;
import fr.myapplication.dc.myapplication.data.model.Contact;
import fr.myapplication.dc.myapplication.data.timeline.TimeLine;
import fr.myapplication.dc.myapplication.media.PictureStorage;
import util.LoggerHelper;

/**
 * Created by jhamid on 20/05/2017.
 */

//Todo : use RecyclerView.Adapter instead
public class TimeLineAdapter extends CustomArrayAdapter {

    protected static final int TYPE_COUNT = 5;
    protected static final int TYPE_INTERMEDIATE = 0;
    protected static final int TYPE_HOUR = 1;
    protected static final int TYPE_FIRST_ROW = 2;
    protected static final int TYPE_HIDDEN_ROW = 3;
    protected static final int TYPE_PAST_ROW = 4;

    public final static int MAX_ROWS = TimeLine.TOTAL_TIME_FRAMES * 10;
    public final static short MAX_MESSAGE_PER_USER_IN_TIMEFRAME = 999;

    SparseBooleanArray mSelectedItemsIds;
    Map<Integer,String> dataPosition;
    final String RIGHT_KEY = "RIGHT";
    final String LEFT_KEY = "LEFT";
    String LAST_VISIBLE_KEY = "";

    Context context;
    RequestManager glide;
    LayoutInflater inflater;

    ListView lv;

    TimeLineAdapter timeLineAdapter;
    ITimeLineInfo timeLineInfo;

    //
    final int nb_item_to_hide = 0;

    public TimeLineAdapter(@NonNull Context context, ArrayList<TimeLine.TimeLineData> timeLineDatas, RequestManager glide, ListView lv, ITimeLineInfo timeLineInfo) {
        super(context,timeLineDatas,glide);
        this.timeLineInfo = timeLineInfo;
        this.lv = timeLineInfo.getListView();
        this.context = context;
        this.glide = glide;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mSelectedItemsIds = new SparseBooleanArray();

        //the list to dispatch datas between left and right layouts
        dataPosition = new HashMap<>();

        //keep reference to the adapter
        timeLineAdapter = this;

    }

    public TimeLineAdapter(@NonNull Context context, ArrayList<TimeLine.TimeLineData> timeLineDatas, RequestManager glide,ITimeLineInfo timeLineInfo) {

        super(context,timeLineDatas,glide);
        this.timeLineInfo = timeLineInfo;

        this.context = context;
        this.glide = glide;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mSelectedItemsIds = new SparseBooleanArray();

        //the list to dispatch datas between left and right layouts
        dataPosition = new HashMap<>();

        //keep reference to the adapter
        timeLineAdapter = this;
    }

    public void printTimeLineMessage(){
        //add data in second list
        for(Object row : dataList){
            LoggerHelper.error(getClass().getName(),String.format("##### row data date = %s - hour = %d ###### /m row Min = %d ",((TimeLine.TimeLineData)row).getTime().getCalendar().getTime().toString(),((TimeLine.TimeLineData)row).getTime().getHour(),((TimeLine.TimeLineData)row).getTime().getMin()));
            for(Contact contact : ((TimeLine.TimeLineData)row).getContactList()){
                LoggerHelper.error(getClass().getName(),"contact found for row : " + contact.getLogin());
            }
        }

        LoggerHelper.warn(getClass().getName(),"#### timeLine total rows = " + dataList.size());
    }


    public void setListView(ListView lv){
        this.lv = lv;
    }

    //method used to customize rows of the list
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Holding
        TimeLineAdapter.TimeLineAdapterViewHolder holder;

        //////////////////////////////////////////////////////////////
        //
        //////////////////////////////////////////////////////////////

        final TimeLine.TimeLineData data = (TimeLine.TimeLineData) getItem(position);

        //////////////////////////////////////////////////////////////
        //Inflate row / Setup holder datas
        //////////////////////////////////////////////////////////////

        holder = null;

        if(convertView == null){

                int type = getItemViewType(position);

                switch (type) {
                    case TYPE_FIRST_ROW:
//                        LoggerHelper.info("FIRST_ROW getItemViewType return_value = " + type + " and time = " + ((TimeLine.TimeLineData) getItem(position)).getTime().toString());
                        convertView = inflater.inflate(R.layout.row_timeline_first_row, parent, false);
                        break;
                    case TYPE_INTERMEDIATE:
//                        LoggerHelper.info("TYPE_INTERMEDIATE getItemViewType return_value = " + type + " and time = " + ((TimeLine.TimeLineData) getItem(position)).getTime().toString());
                        convertView = inflater.inflate(R.layout.row_timeline_intermediate, parent, false);
                        break;
                    case TYPE_HOUR:
//                        LoggerHelper.info("TYPE_HOUR getItemViewType return_value = " + type + " and time = " + ((TimeLine.TimeLineData) getItem(position)).getTime().toString());
                        convertView = inflater.inflate(R.layout.row_timeline_hour, parent, false);
                        break;
                    case TYPE_HIDDEN_ROW:
//                        LoggerHelper.info("TYPE_HIDDEN_ROW getItemViewType return_value = " + type + " and time = " + ((TimeLine.TimeLineData) getItem(position)).getTime().toString());
                        convertView = inflater.inflate(R.layout.row_timeline_null, parent, false);
                        break;
            }

            if(type != TYPE_HIDDEN_ROW){

                holder = new TimeLineAdapter.TimeLineAdapterViewHolder(convertView);

                if(convertView != null)
                    convertView.setTag(holder);
                }
            }

        else{
            holder = (TimeLineAdapter.TimeLineAdapterViewHolder) convertView.getTag();
        }

        //////////////////////////////////////////////////////
        // SET DATAS
        //////////////////////////////////////////////////////

        if(holder != null){

            //////////////////////////////////////////////////////
            // setting Hour
            holder.setHour(data);

            //////////////////////////////////////////////////////
            //setting avatar images on timeline

            //Map is filled
            distributeDataBetweenLayouts(position);

            //getting real position in datalist using modulo
            int position_in_list = position % dataList.size();

            //checking if data exist for this position
            String pos = dataPosition.get(position_in_list);

            if(pos == null){
                holder.clearLayouts();
            }
            else{

                //
                //Todo : hide layout left and right if data visible on the first row (meanng from yesterday because the first row is considered as future but can be filled with past datas)
                if(position == timeLineInfo.getCurrentPosition() - 1){
                    LoggerHelper.info(getClass(),"hiding layouts");
                    holder.clearLayoutLeft();
                    holder.clearLayoutRight();
                    dataPosition.remove(timeLineInfo.getCurrentPosition() - 1);
                }
                else
                    if(pos.equals(RIGHT_KEY)){
                        holder.clearLayouts();
                        holder.setLayoutRight(position);
                 }
                else{
                    holder.clearLayouts();
                    holder.setLayoutLeft(position);
                }
            }

            //////////////////////////////////////////////////////


            //////////////////////////////////////////////////////
            //ANIMATION
            //////////////////////////////////////////////////////

            // This tells the view where to start based on the direction of the scroll.
            // If the last position to be loaded is <= the current position, we want
            // the views to start below their ending point (500f further down).
            // Otherwise, we start above the ending point.

    /*        float initialTranslation = (mLastPosition <= position ? 500f : -500f);

            convertView.setTranslationY(initialTranslation);
            convertView.animate()
                    .setInterpolator(new DecelerateInterpolator(1.0f))
                    .translationY(0f)
                    .setDuration(300l)
                    .setListener(null);

            // Keep track of the last position we loaded
            mLastPosition = position;

     */
        }

        return convertView;
    }


    @Override
    public int getItemViewType(int position) {

        if(position < timeLineInfo.getCurrentPosition() - 1 || position > timeLineInfo.getCurrentPosition() - 1  + TimeLine.TOTAL_TIME_FRAMES){
            return TYPE_HIDDEN_ROW;
        }

/*        if(position == timeLineInfo.getCurrentPosition() ){
            return TYPE_FIRST_ROW;
        }*/

        final TimeLine.TimeLineData data = (TimeLine.TimeLineData) getItem(position);

        boolean is_intermediate_time = data != null && data.getTime().is_intermediate_time;

        return ((is_intermediate_time ? TYPE_INTERMEDIATE : TYPE_HOUR ));
    }

    @Override
    public int getViewTypeCount(){
        return TYPE_COUNT;
    }


    @Override
    public int getCount() {
        return MAX_ROWS - nb_item_to_hide;
    }

    @Override
    public long getItemId(int position) {
        // you can do your own tricks here. to let it display the right item in your array.
        return position;
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position % dataList.size());
    }

    //////////////////////////////////////////////////////////////////////////////
    // Misc
    //////////////////////////////////////////////////////////////////////////////

    //data is shared between layout left and layout right by filling dataPosition Map
    //the idea is to have them successively presented right on first row then left on second etc..
    /*
    * 3 cases :
    * currentPosition % dataList.size() == 0 ---> no timeframe was added yet, in this case we must look into the past ()
    * currentPosition % dataList.size()
    * */
    public void distributeDataBetweenLayouts(int position_in_list){

        int position_modulo = position_in_list % dataList.size();

        //get the first row which contains datas
        LoggerHelper.debug(this.getClass().getName(),
                "IN initShareDataBetweenLayouts with "
                        + "dataList.size() = " + dataList.size()
                        + " and position_modulo = " + position_modulo
                        + "---------- position_in_list = " + position_in_list
                        +  " ----- currentPosition = " + timeLineInfo.getCurrentPosition());

        String current_key = "" ;
        String previous_key;
        boolean previous_found = false;
        final int currentPositionModulo = timeLineInfo.getCurrentPosition() % dataList.size();

        if( ! StringUtils.isEmpty(dataPosition.get(position_modulo))){
            //data already exist for this position
            LoggerHelper.debug(this.getClass().getName(),"dataPosition already exist for position = " + position_modulo);
        }
        else{
            LoggerHelper.debug(this.getClass().getName(),"dataPosition do not exist for position = " + position_modulo);
            // we get positions

            TimeLine.TimeLineData data = (TimeLine.TimeLineData) dataList.get(position_modulo);

            if(data.getContactList().isEmpty()){
               LoggerHelper.debug(this.getClass().getName(),"no contact exist for position " + position_modulo + " searched can be skipped");
            }
            else {

                if(position_modulo == dataList.size() - 1){
                    LoggerHelper.debug("position == dataList.size() - 1");
                }

                ///////////////////////////////////////////////////////////////////////////////////
                //CASE 1 no timeframe added yet (or return to initial position after  24h)
                //currentPos = 0
                // check data bellow in the list
                ///////////////////////////////////////////////////////////////////////////////////

                if(position_modulo == currentPositionModulo && currentPositionModulo == 0){
                    //otherwise we need to check if datas are present in from 0 to position - 1
                    LoggerHelper.info(getClass(),"CASE 1");

                    for (int i = 0; i <= dataList.size() - 1 ; i++) {
                        LoggerHelper.info(getClass(),"position_modulo == currentPositionModulo && currentPositionModulo == 0");
                        previous_key = dataPosition.get(i);

                        //check if a key already exist
                        if (StringUtils.isEmpty(previous_key)) {
                            LoggerHelper.debug(this.getClass().getName(), "previous_key is empty");
                            //previous_key = "";
                            continue;
                        }

                        data = (TimeLine.TimeLineData) dataList.get(i);

                        if ( ! data.getContactList().isEmpty()) {
                            LoggerHelper.info(this.getClass().getName(), String.format("!data.getContactList().isEmpty() with i = %d and dataposition = %s ", i, dataPosition.get(i)));
                            if (previous_key.equals(RIGHT_KEY)) {
                                current_key = LEFT_KEY;
                            } else {
                                current_key = RIGHT_KEY;
                            }

                            //previous position is putted in the list
                            dataPosition.put(position_modulo, current_key);
                            previous_found = true;
                            break;
                        }
                    }
                }

                ///////////////////////////////////////////////////////////////////////////////////
                //CASE 2 first added timeframe
                // ex currentPos = 95 with list size = 96
                ///////////////////////////////////////////////////////////////////////////////////

                if( ! previous_found && position_modulo == currentPositionModulo && currentPositionModulo == dataList.size() - 1) {

                    LoggerHelper.info(getClass(),"CASE 2");

                    previous_key = dataPosition.get(0);

                    //check if a key already exist
                    if ( ! StringUtils.isEmpty(previous_key)) {

                        data = (TimeLine.TimeLineData) dataList.get(0);

                        if ( ! data.getContactList().isEmpty()) {
                            LoggerHelper.debug(String.format("!data.getContactList().isEmpty() with i = %d and dataposition = %s ", 0, dataPosition.get(0)));
                            if (previous_key.equals(RIGHT_KEY)) {
                                current_key = LEFT_KEY;
                            } else {
                                current_key = RIGHT_KEY;
                            }

                            LoggerHelper.debug("dataPosition.put(position, current_key);");
                            //previous position is putted in the list
                            dataPosition.put(position_modulo, current_key);
                            previous_found = true;
                        }
                    }
                }

                ///////////////////////////////////////////////////////////////////////////////////
                //CASE 3 added timeframes
                //set currentPosition compared to position added between currentPos and app launch
                // exemple : currentPos = 93 ---> will check position 94 & 95 (if 94 not found)
                ///////////////////////////////////////////////////////////////////////////////////

                if( ! previous_found && position_modulo == currentPositionModulo && currentPositionModulo != 0) {
                    LoggerHelper.info(getClass(),"CASE 3");
                    //we need to check if datas are present in from position + 1 to datalist.size - 1
                    LoggerHelper.info("position >= currentPosition % dataList.size()");

                    //set currentPosition compared to position added between currentPos and app launch
                    for (int i = position_modulo + 1 ; i <= dataList.size() - 1 ; i++) {

                        LoggerHelper.debug(String.format(" i = %d",i));
                        previous_key = dataPosition.get(i);

                        //check if a key already exist
                        if(StringUtils.isEmpty(previous_key)){
                            LoggerHelper.error("previous_key is empty");
                            //previous_key = "";
                            continue;
                        }

                        data = (TimeLine.TimeLineData) dataList.get(i);

                        if ( ! data.getContactList().isEmpty()) {
                            LoggerHelper.debug(String.format("!data.getContactList().isEmpty() with i = %d and dataposition = %s ", i, dataPosition.get(i)));
                            if (previous_key.equals(RIGHT_KEY)) {
                                current_key = LEFT_KEY;
                            } else {
                                current_key = RIGHT_KEY;
                            }

                            LoggerHelper.info("dataPosition.put(position, current_key);");
                            //previous position is putted in the list
                            dataPosition.put(position_modulo, current_key);
                            previous_found = true;
                            break;
                        } else {
                            LoggerHelper.info(String.format("!data.getMessageList().isEmpty()"));
                        }
                    }
                    if( ! previous_found) {
                        //if nothing found before check ahead if data between 0 and end of list
                        for (int i = 0; i <= dataList.size() - 1; i++) {
                            LoggerHelper.info(getClass(), "position_modulo == currentPositionModulo && currentPositionModulo == 0");
                            previous_key = dataPosition.get(i);

                            //check if a key already exist
                            if (StringUtils.isEmpty(previous_key)) {
                                LoggerHelper.debug(this.getClass().getName(), "previous_key is empty");
                                //previous_key = "";
                                continue;
                            }

                            data = (TimeLine.TimeLineData) dataList.get(i);

                            if (!data.getContactList().isEmpty()) {
                                LoggerHelper.info(this.getClass().getName(), String.format("!data.getContactList().isEmpty() with i = %d and dataposition = %s ", i, dataPosition.get(i)));
                                if (previous_key.equals(RIGHT_KEY)) {
                                    current_key = LEFT_KEY;
                                } else {
                                    current_key = RIGHT_KEY;
                                }

                                //previous position is putted in the list
                                dataPosition.put(position_modulo, current_key);
                                previous_found = true;
                                break;
                            }
                        }
                    }
                }

                ////////////////////////////////////////////////////////////////////////////////////
                // CASE 4
                // the rest of the timeline
                // position_modulo > 0
                // the timeframes with position_modulo > 0 for exemple from position 963 to 961
                ////////////////////////////////////////////////////////////////////////////////////

                if( ! previous_found){
                    LoggerHelper.info(getClass(),"CASE 4");
                        LoggerHelper.debug(this.getClass().getName(), "NOT position >= currentPosition % dataList.size()");
                        //check if data between 0 to current post
                        for (int i = position_modulo - 1; i >= 0; i--) {

                            previous_key = dataPosition.get(i);

                            //check if a key already exist
                            if (StringUtils.isEmpty(previous_key)) {
                                LoggerHelper.info(this.getClass().getName(), "previous_key is empty");
                                //previous_key = "";
                                continue;
                            }

                            data = (TimeLine.TimeLineData) dataList.get(i);

                            if ( ! data.getContactList().isEmpty()) {
                                LoggerHelper.info(this.getClass().getName(), String.format("!data.getContactList().isEmpty() with i = %d and dataposition = %s ", i, dataPosition.get(i)));
                                if (previous_key.equals(RIGHT_KEY)) {
                                    current_key = LEFT_KEY;
                                } else {
                                    current_key = RIGHT_KEY;
                                }

                                LoggerHelper.info(this.getClass().getName(), "dataPosition.put(position, current_key);");
                                //previous position is putted in the list
                                dataPosition.put(position_modulo, current_key);
                                previous_found = true;
                                break;
                            } else {
                                LoggerHelper.info(this.getClass().getName(), String.format("!data.getMessageList().isEmpty()"));
                                // LoggerHelper.error("empty data");
                            }
                        }

                    //if nothing found before check ahead if data between currentPos and end of list
                    if( ! previous_found){
                        for (int i = position_modulo + 1 ; i <= dataList.size() - 1 ; i++) {

                            LoggerHelper.debug(String.format(" i = %d",i));
                            previous_key = dataPosition.get(i);

                            //check if a key already exist
                            if(StringUtils.isEmpty(previous_key)){
                                LoggerHelper.error("previous_key is empty");
                                //previous_key = "";
                                continue;
                            }

                            data = (TimeLine.TimeLineData) dataList.get(i);

                            if ( ! data.getContactList().isEmpty()) {
                                LoggerHelper.debug(String.format("!data.getContactList().isEmpty() with i = %d and dataposition = %s ", i, dataPosition.get(i)));
                                if (previous_key.equals(RIGHT_KEY)) {
                                    current_key = LEFT_KEY;
                                } else {
                                    current_key = RIGHT_KEY;
                                }

                                LoggerHelper.info("dataPosition.put(position, current_key);");
                                //previous position is putted in the list
                                dataPosition.put(position_modulo, current_key);
                                previous_found = true;
                                break;
                            } else {
                                LoggerHelper.info(String.format("!data.getMessageList().isEmpty()"));
                            }
                        }
                    }
                }

                //////////////////////////////////////////////////////
                // CASE 5
                // default
                //////////////////////////////////////////////////////

                if( ! previous_found){
                    LoggerHelper.info(getClass(),"CASE 5");
                    LoggerHelper.info(this.getClass().getName(),"i == position");
                    dataPosition.put(position_modulo,RIGHT_KEY);
                }

                LAST_VISIBLE_KEY = current_key;
            }
        }
    }

    //listener on avatar click

    protected void setListener(View imageView, final ArrayList<Contact> contactList){
        LoggerHelper.info("IN setListener");
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                View view = inflater.inflate(R.layout.layout_avatar_timeline_detail_list_recycler,null);
                RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);

                TimeLineDetailListAdapter timeLineDetailListAdapter = new TimeLineDetailListAdapter(context,contactList,glide);

                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                recyclerView.setAdapter(timeLineDetailListAdapter);

                //refresh datas in case recen messages received
                timeLineDetailListAdapter.notifyDataSetChanged();

                builder.setView(view);

                // dialog.setContentView(dialog.findViewById(R.id.row_timeline_detail));
                //  dialog.getWindow().setLayout(600, 400); //Controlling width and height.

/*                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;*/


/*                dialog.show();
                dialog.getWindow().setAttributes(lp);*/

                Dialog dialog = builder.create();
                dialog.show();

            }
        });
    }


    //////////////////////////////////////////////////////////////////////////////
    // The view holder
    //////////////////////////////////////////////////////////////////////////////

    protected class TimeLineAdapterViewHolder {

        protected View row;
        private TextView hour;
        private LinearLayout layoutRight;
        private LinearLayout layoutLeft;
        public int position;

        TimeLineAdapterViewHolder(View row) {
            this.row = row;
            this.hour = (TextView) row.findViewById(R.id.time_line_hour);
            layoutRight = (LinearLayout) row.findViewById(R.id.time_line_layout_right);
            layoutLeft = (LinearLayout) row.findViewById(R.id.time_line_layout_left);
        }

        public LinearLayout setLayoutRight(int position) {

            //clean the layout
            layoutRight.removeAllViews();

            final TimeLine.TimeLineData data = (TimeLine.TimeLineData) getItem(position);

            if(data != null) {
                ArrayList<Contact> contactList = (ArrayList<Contact>) data.getContactList();

//                LoggerHelper.info(this.getClass().getName(), "about to sort invitationList");

                //Contacts are sorted in descending order
                Collections.sort(contactList);

                if ( ! contactList.isEmpty()) {
                    LoggerHelper.info("TimeLineAdapterViewHolder setLayoutRight contact found with login=" + contactList.get(0).getLogin());

                    //get the avatar layout
                    RelativeLayout layout_avatar = (RelativeLayout) inflater.inflate(R.layout.layout_avatar_timeline, null);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                    layout_avatar.setLayoutParams(params);

                    //get avatar 1
                    ImageView imageView = (ImageView) layout_avatar.findViewById(R.id.avatarImage_1);
                    PictureStorage.loadAvatar(glide, context, contactList.get(0).getLogin(), imageView);

                    //image2
                    ImageView imageView2 = (ImageView) layout_avatar.findViewById(R.id.avatarImage_2);

                    //counter
                    TextView counter_tv = (TextView) layout_avatar.findViewById(R.id.timeline_avatar_counter_for_layout_right);

                    //hide the other counter
                    TextView counter_tv_hidden = (TextView) layout_avatar.findViewById(R.id.timeline_avatar_counter_for_layout_left);
                    counter_tv_hidden.setVisibility(View.INVISIBLE);

                    if (contactList.size() > 1) {
                        //get avatar 1
                        imageView2 = (ImageView) layout_avatar.findViewById(R.id.avatarImage_2);
                        PictureStorage.loadAvatar(glide, context, contactList.get(1).getLogin(), imageView2);

                        //get counter
                        counter_tv = (TextView) layout_avatar.findViewById(R.id.timeline_avatar_counter_for_layout_right);
                        int cpt = contactList.size();
                        LoggerHelper.info("TimeLineDetailsAdapterViewHolder setLayoutRight cpt1 = " + cpt);
                        if (cpt > 1) {
                            LoggerHelper.info("TimeLineDetailsAdapterViewHolder setLayoutRight cpt2 = " + cpt);
                            if (cpt >= MAX_MESSAGE_PER_USER_IN_TIMEFRAME) {
                                counter_tv.setText(String.valueOf(MAX_MESSAGE_PER_USER_IN_TIMEFRAME));
                            } else {
                                LoggerHelper.info("TimeLineDetailsAdapterViewHolder setLayoutRight cpt3 = " + cpt);
                                counter_tv.setText(String.valueOf(cpt));
                            }
                        } else {
                            LoggerHelper.info("View.INVISIBLE");
                            counter_tv.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        imageView2.setVisibility(View.INVISIBLE);
                        counter_tv.setVisibility(View.INVISIBLE);
                    }

                    //layout added
                    layoutRight.addView(layout_avatar);

                    //set click Listener on ImageView
                    setListener(imageView, contactList);

                    //animate infinite
            /*        if(position == timeLineInfo.getCurrentPosition()){
                        if(imageView2.getVisibility() == View.VISIBLE){
                            startFabAnimation(imageView2);
                        }
                        else{
                            startFabAnimation(imageView);
                        }
                    }*/
                }
            }

            return layoutRight;
        }


        public void clearLayoutRight(){
            layoutRight.removeAllViews();
        }

        public void clearLayoutLeft(){
            layoutLeft.removeAllViews();
        }

        public void clearLayouts(){

            clearLayoutRight();
            clearLayoutLeft();

            layoutLeft.setVisibility(View.VISIBLE);
            layoutRight.setVisibility(View.VISIBLE);
        }



        public LinearLayout setLayoutLeft(final int position) {

            //clean the layout
            layoutLeft.removeAllViews();
            layoutLeft.setVisibility(View.VISIBLE);

            final TimeLine.TimeLineData data = (TimeLine.TimeLineData) getItem(position);

            if(data != null) {
                ArrayList<Contact> contactList = (ArrayList<Contact>) data.getContactList();

//                LoggerHelper.info(this.getClass().getName(), "about to sort invitationList");

                //Contacts are sorted in descending order
                Collections.sort(contactList);

 /*               LoggerHelper.info("TimeLineAdapterViewHolder setLayoutRight contact found with login =" + invitationList.get(0).getLogin());
                ImageView imageView_tmp = new ImageView(context);
                PictureStorage.loadAvatar(glide,context,invitationList.get(0).getLogin(),imageView_tmp);
                layoutLeft.addView(imageView_tmp,120,120);*/

                //get the avatar layout
                RelativeLayout layout_avatar = (RelativeLayout) inflater.inflate(R.layout.layout_avatar_timeline,null);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                layout_avatar.setLayoutParams(params);

                //get avatar 1
                ImageView imageView = (ImageView) layout_avatar.findViewById(R.id.avatarImage_1);
                PictureStorage.loadAvatar(glide,context,contactList.get(0).getLogin(),imageView);

                //image2
                ImageView imageView2 = (ImageView) layout_avatar.findViewById(R.id.avatarImage_2);

                //counter
                TextView counter_tv = (TextView) layout_avatar.findViewById(R.id.timeline_avatar_counter_for_layout_left);

                //hide the other counter
                TextView counter_tv_hidden = (TextView) layout_avatar.findViewById(R.id.timeline_avatar_counter_for_layout_right);
                counter_tv_hidden.setVisibility(View.INVISIBLE);

                if(contactList.size() > 1){

                    //get avatar 1
                    imageView2 = (ImageView) layout_avatar.findViewById(R.id.avatarImage_2);
                    PictureStorage.loadAvatar(glide,context,contactList.get(1).getLogin(),imageView2);

                    //get counter
                    counter_tv = (TextView) layout_avatar.findViewById(R.id.timeline_avatar_counter_for_layout_left);
                    int cpt = contactList.size();
                    LoggerHelper.info("TimeLineDetailsAdapterViewHolder setLayoutRight cpt1 = " + cpt);
                    if(cpt > 1){
                        LoggerHelper.info("TimeLineDetailsAdapterViewHolder setLayoutRight cpt2 = " + cpt);
                        if(cpt >= MAX_MESSAGE_PER_USER_IN_TIMEFRAME){
                            counter_tv.setText(String.valueOf(MAX_MESSAGE_PER_USER_IN_TIMEFRAME));
                        }
                        else{
                            LoggerHelper.info("TimeLineDetailsAdapterViewHolder setLayoutRight cpt3 = " + cpt);
                            counter_tv.setText(String.valueOf(cpt));
                        }
                    }
                    else{
                        LoggerHelper.info("View.INVISIBLE");
                        counter_tv.setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    imageView2.setVisibility(View.INVISIBLE);
                    counter_tv.setVisibility(View.INVISIBLE);
                }


                //layout added
                layoutLeft.addView(layout_avatar);

                //set click Listener on ImageView
                setListener(imageView,contactList);

                //animate infinite
               /* if(position == timeLineInfo.getCurrentPosition()){
                    if(imageView2.getVisibility() == View.VISIBLE){
                        startFabAnimation(imageView2);
                    }
                    else{
                        startFabAnimation(imageView);
                    }
                }*/
            }

            return layoutLeft;
        }

        public void setHour(TimeLine.TimeLineData data) {
            if(data.getTime().is_intermediate_time){
                this.hour.setText("--");
               // this.hour.setBackground(null);
            }
            else {
                this.hour.setText(String.valueOf(data.getTime().getHour()));
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Animations
    ////////////////////////////////////////////////////////////////////////////////////////////

    public void animateMessageReception(int position){
        animateMessageReception(position,false);
    }

    public void animateMessageReception(int position, boolean isFound){

        LoggerHelper.info(getClass().getName(),"IN animateMessageReception with position = " + position);

        boolean found = false;
        //check if the currentPosition is visible otherwise no need to animate
        if(lv != null){

            LoggerHelper.info(getClass().getName(),"IN animateMessageReception with lv.getFirstVisiblePosition() = " + lv.getFirstVisiblePosition());;

            LoggerHelper.info(getClass().getName(),"lv != null & currentPosition = " + position);

            View v = lv.getChildAt(position);

            //try to find image view in the right layout
            if(v != null){

                LinearLayout ll = (LinearLayout) v.findViewById(R.id.time_line_layout_right);

                if(ll != null) {
                    int childCount = ll.getChildCount();
                    LoggerHelper.info(getClass().getName(),"childCount = " + childCount);

                    if(childCount == 0){
                        LoggerHelper.info(getClass().getName(),"nothing in the right layout, search on left layout");
                        ll = (LinearLayout) v.findViewById(R.id.time_line_layout_left);
                        childCount = ll.getChildCount();
                    }

                    for (int i = 0; i < childCount; i++) {
                        LoggerHelper.info(getClass().getName(),"i = " + i);
                        RelativeLayout relativeLayout = (RelativeLayout) ll.getChildAt(i);
                        //RelativeLayout relativeLayout = (RelativeLayout) child.getChildAt(i);
                        final int childCount2 = relativeLayout.getChildCount();
                        LoggerHelper.info(getClass().getName(),"childCount2 = " + childCount2);
                        for (int j = 0; j < childCount2; j++) {
                            View child2 = relativeLayout.getChildAt(j);
                            if (child2 != null && child2.getId() == (R.id.avatarImage_1)) {
                                LoggerHelper.info(getClass().getName(),"image found !");
                                startFabAnimation((ImageView) child2);
                                found = true;
                            }
                        }
                    }
                }
                else{
                    LoggerHelper.info(getClass().getName(),"ll == null");
                }
            }
            else{
                LoggerHelper.info(getClass().getName(),"v == null");
                //if the current row was not visible we need to update the UI list
            }
        }
        else{

            LoggerHelper.info(getClass().getName(),"lv == null");
            //if the current row was not visible we need to update the UI list
        }
        if( ! found && ! isFound) {
            LoggerHelper.info(getClass().getName(),"otherwise notifyDataSetChanged");
            notifyDataSetChanged();
        }
    }

    public void startFabAnimation(final ImageView avatarImage){

        LoggerHelper.info(getClass().getName(),"startFabAnimation with avatarImage = " + avatarImage);

        //cancel current animations if necessary
        //stopUnreadVibrationsAnimation();

        //start animation

        final long animation1_duration = 300;
        final long animation2_duration = 300;
        final long animation3_duration = 500;
        final long animation4_duration = 400;
        final long animation5_duration = 400;

        final ScaleAnimation animation1 =  new ScaleAnimation(0.8f, 0.9f, 0.8f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation1.setDuration(animation1_duration);

        final ScaleAnimation animation2 =  new ScaleAnimation(0.9f, 0.7f, 0.9f, 0.7f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation2.setDuration(animation2_duration);

        final ScaleAnimation animation3 =  new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation3.setDuration(animation3_duration);

        final ScaleAnimation animation4 =  new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation4.setDuration(animation4_duration);

        final ScaleAnimation animation5 =  new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        animation5.setDuration(animation5_duration);

        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                LoggerHelper.info("animation1 onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LoggerHelper.info("animation1 onAnimationEnd");
                avatarImage.startAnimation(animation2);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                LoggerHelper.info("onAnimation2 Start");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                avatarImage.startAnimation(animation3);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation3.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                LoggerHelper.info("animation3 started");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                avatarImage.startAnimation(animation4);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation4.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                LoggerHelper.info("animation4 started");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                avatarImage.startAnimation(animation5);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation5.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                LoggerHelper.info("animation5 started");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LoggerHelper.info("animation5 ended");
                notifyDataSetChanged();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        avatarImage.startAnimation(animation1);
    }

}
