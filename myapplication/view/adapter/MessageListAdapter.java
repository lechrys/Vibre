package fr.myapplication.dc.myapplication.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.data.model.VibrationMessage;
import fr.myapplication.dc.myapplication.media.PictureStorage;
import util.LoggerHelper;

/**
 * Created by Crono on 20/02/17.
 */

public class MessageListAdapter extends CustomArrayAdapter <VibrationMessage>{

    public MessageListAdapter(Context context, List dataList) {
        super(context, dataList);
        LoggerHelper.warn("dataList=" + dataList);
    }

    //method used to customize rows of the list
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Holding
        MessageAllViewHolder holder;

        //////////////////////////////////////////////////////////////
        //Inflate row / Setup holder datas
        //////////////////////////////////////////////////////////////

        if(convertView == null){
            LoggerHelper.debug("row == null");
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_message_list, parent,false);
            holder = new MessageAllViewHolder(convertView);
            convertView.setTag(holder);
        }
        else{
            holder = (MessageAllViewHolder) convertView.getTag();
        }

        //////////////////////////////////////////////////////////////
        //
        //////////////////////////////////////////////////////////////

        final VibrationMessage message = getItem(position);

        if(message == null){
            LoggerHelper.error("Contact is not supposed to be null at this point");
            return convertView;
        }

        //////////////////////////////////////////////////////////////
        //set specific datas
        //////////////////////////////////////////////////////////////

        //set number of unread message
        if(message.getPattern() != null){
            holder.getVibrationContent().setText(String.valueOf(message.getPattern().toString()));

        }

        //check background color if message was not yet red

        LoggerHelper.info("MessageListAdapter message.isRead=" + message.isRead());

        if(!message.isRead()){
            convertView.setBackgroundColor(Color.GRAY);
        }
        else{
            convertView.setBackgroundColor(Color.WHITE);
        }
        //////////////////////////////////////////////////////////////
        //return row
        //////////////////////////////////////////////////////////////

        return convertView;
    }

    public class MessageAllViewHolder extends ContactBasicViewHolder{

        private ImageView iconVibration;
        private TextView vibrationContent;

        MessageAllViewHolder(View row) {
            super(row,null);
        }

        public ImageView getIconVibration(){
            if(iconVibration == null){
                iconVibration = (ImageView) row.findViewById(R.id.icon_vibration);
            }
            return iconVibration;
        }

        public TextView getVibrationContent() {
            if(vibrationContent == null){
                vibrationContent = (TextView) row.findViewById(R.id.vibration_content);
            }
            return vibrationContent;
        }
    }
}
