package fr.myapplication.dc.myapplication.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.data.model.BaseMessage;
import fr.myapplication.dc.myapplication.data.model.Contact;
import fr.myapplication.dc.myapplication.media.PictureStorage;
import util.LoggerHelper;

/**
 * Created by jhamid on 20/05/2017.
 */

public class TimeLineDetailListAdapter extends RecyclerView.Adapter<TimeLineDetailListAdapter.TimeLineDetailsAdapterViewHolder>{

    Context context;
    RequestManager glide;
    LayoutInflater inflater;
    ArrayList<Contact> contactList;

    public static short MAX_MESSAGE_PER_USER_IN_TIMEFRAME = 999;

    public TimeLineDetailListAdapter(@NonNull Context context, ArrayList<Contact> contactList, RequestManager glide) {
        LoggerHelper.info(TimeLineDetailListAdapter.class.toString(),"IN TimeLineDetailListAdapter");
        this.context = context;
        this.glide = glide;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.contactList = contactList;
    }

    @Override
    public TimeLineDetailsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.row_timeline_detail_list, parent,false);
        return new TimeLineDetailsAdapterViewHolder (v);
    }

    @Override
    public void onBindViewHolder(TimeLineDetailsAdapterViewHolder holder, int position) {
        holder.load(contactList.get(position));
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    //////////////////////////////////////////////////////////////////////////////
    // The view holder
    //////////////////////////////////////////////////////////////////////////////

    protected class TimeLineDetailsAdapterViewHolder extends RecyclerView.ViewHolder {

        protected LinearLayout layout_avatar;
        private TextView login;
//        private TextView date;
        private TextView hour;
        private TextView timezone;

        TimeLineDetailsAdapterViewHolder(View row) {
            super(row);
            this.layout_avatar = (LinearLayout) row.findViewById(R.id.time_line_details_layour_avatar);
            this.login = (TextView) row.findViewById(R.id.login);
//            this.date = (TextView) row.findViewById(R.id.date_last_message);
            this.timezone = (TextView) row.findViewById(R.id.zone);
            this.hour = (TextView) row.findViewById(R.id.hour_last_message);
        }

        public void load(Contact contact){

            //loading login

            final String login = contact.getLogin();
            this.login.setText(login);

            //loading message list
            List<BaseMessage> messageList = contact.getMessageList();
            Collections.sort(messageList);

            if( ! messageList.isEmpty()){
                final int cpt_message = messageList.size();

                //get the most recent message date
                if(cpt_message > 0 ){

                    BaseMessage message = messageList.get(messageList.size() - 1);

                    if(message != null){
                        Date d = new Date(message.getDateLong());

                        //format date
                        final SimpleDateFormat parserDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        final String formatted_date = parserDate.format(d);
//                        this.date.setText(formatted_date);

                        //format hour
                        final SimpleDateFormat parserHour = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                        final String formatted_hour = parserHour.format(d);
                        this.hour.setText(formatted_hour);

                        final String timezone = message.getZone();

                        if( ! StringUtils.isEmpty(timezone)){
                            LoggerHelper.info(getClass(),"zone = " + timezone);
                            String timePrefix = context.getString(R.string.timezone_prefix);
                            LoggerHelper.info(getClass(),"timePrefix = " + timePrefix);
                            this.timezone.setText(String.format(timePrefix,timezone));
                        }
                    }
                }

                RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.layout_avatar_timeline_detail_list,null);

                if(cpt_message > 0 ){
                    LoggerHelper.info("TimeLineAdapterViewHolder setLayoutRight contact found with login=" + contact.getLogin());

                    //get avatar
                    ImageView imageView = (ImageView) relativeLayout.findViewById(R.id.avatarImageDetailList);

                    PictureStorage.loadAvatar(glide,context,contact.getLogin(),imageView);

                    //get counter
                    TextView counter_tv = (TextView) relativeLayout.findViewById(R.id.timeline_avatar_counter_for_layout_right);
                    int cpt = contact.getMessageList().size();

                    LoggerHelper.info("TimeLineAdapterViewHolder setLayoutRight cpt1 = " + cpt);

                    if(cpt > 1){
                        LoggerHelper.info("TimeLineAdapterViewHolder setLayoutRight cpt2 = " + cpt);
                        if(cpt >= MAX_MESSAGE_PER_USER_IN_TIMEFRAME){
                            counter_tv.setText(String.valueOf(MAX_MESSAGE_PER_USER_IN_TIMEFRAME));
                        }
                        else{
                            LoggerHelper.info("TimeLineAdapterViewHolder setLayoutRight cpt3 = " + cpt);
                            counter_tv.setText(String.valueOf(cpt));
                        }
                    }
                    else{
                        LoggerHelper.info("View.INVISIBLE");
                        counter_tv.setVisibility(View.INVISIBLE);
                    }

                    //layout added
                    layout_avatar.addView(relativeLayout);
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    // Listener on avatar click
    //////////////////////////////////////////////////////////////////////////////
}
