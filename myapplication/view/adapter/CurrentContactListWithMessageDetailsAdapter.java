package fr.myapplication.dc.myapplication.view.adapter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.data.model.Contact;
import util.LoggerHelper;

/**
 * Created by Crono on 04/12/16.
 * this Adapter is a bridge between rootView and contact datas
 */

public class CurrentContactListWithMessageDetailsAdapter extends CustomArrayAdapter<Contact>{

    Context context;

    public CurrentContactListWithMessageDetailsAdapter(Context context, List<Contact> contactList,
                                                       RequestManager glide) {
        super(context, contactList,glide);
        this.context = context;
    }

    //method used to customize rows of the list
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Holding
        CurrentContactListWithMessageViewHolder holder;

        //////////////////////////////////////////////////////////////
        //
        //////////////////////////////////////////////////////////////

        final Contact contact = getItem(position);

        if(contact == null || StringUtils.isEmpty(contact.getLogin())){
            LoggerHelper.error("Contact is not supposed to be null at this point");
            return convertView;
        }

        //////////////////////////////////////////////////////////////
        //Inflate row / Setup holder datas
        //////////////////////////////////////////////////////////////

        if(convertView == null){
            LoggerHelper.debug("row == null");
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_contact_with_message_counter_list, parent,false);
            holder = new CurrentContactListWithMessageViewHolder(convertView,contact.getLogin());
            convertView.setTag(holder);
        }
        else{
            holder = (CurrentContactListWithMessageViewHolder) convertView.getTag();
        }

        //////////////////////////////////////////////////////////////
        //set specific datas
        //////////////////////////////////////////////////////////////

     /*   if(contact.getMessageList().isEmpty()){
            LoggerHelper.debug("contact.getMessageList().isEmpty");
            convertView.setVisibility(View.INVISIBLE);
            //convertView.getLayoutParams().height = 1;
        }
        else {*/

            //set login
            holder.setLogin(contact.getLogin());

            //set number of unread message
            int nb_unread_messages = 0;

        /*    for (VibrationMessage message : contact.getMessageList()) {
                if (!message.isRead()) nb_unread_messages++;
            }*/

            holder.getUnreadMessages().setText(String.valueOf(nb_unread_messages));

            //start avatar animation
            if (nb_unread_messages > 0) {
                ScaleAnimation animation = new ScaleAnimation(0.7F, 1.0F, 0.7F, 1.0F,
                        holder.getUnreadMessages().getPivotX(), holder.getUnreadMessages().getPivotY());
                animation.setDuration(500);
                animation.setRepeatCount(ValueAnimator.INFINITE);
                animation.setRepeatMode(ValueAnimator.REVERSE);
                holder.getUnreadMessages().setAnimation(animation);
                animation.start();
            }
       // }
        //////////////////////////////////////////////////////////////
        //return row
        //////////////////////////////////////////////////////////////

        return convertView;
    }

    public class CurrentContactListWithMessageViewHolder extends ContactBasicViewHolder{

        private TextView unreadMessages;

        public TextView getUnreadMessages() {
            if(unreadMessages == null){
                LoggerHelper.info("unreadMessages == null");
                unreadMessages = (TextView) row.findViewById(R.id.unred_messages);
            }
            else{
                LoggerHelper.info("unreadMessages != null");
            }
            return unreadMessages;
        }

        CurrentContactListWithMessageViewHolder(View row,final String login) {
            super(row,login);
        }
    }
}
