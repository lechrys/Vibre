package fr.myapplication.dc.myapplication.view.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.MainActivity;
import fr.myapplication.dc.myapplication.data.model.IContact;
import fr.myapplication.dc.myapplication.media.PictureStorage;
import util.ImageUtil;
import util.LoggerHelper;
import util.MessageUtil;

/**
 * Created by Crono on 04/12/16.
 * this Adapter is a bridge between rootView and contact datas
 */

public class CurrentContactListAdapter extends CustomArrayAdapter<IContact>{

    private HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();
    private LayoutInflater inflater;
    private Context context;
    Dialog dialog;
    View view;

    public CurrentContactListAdapter(Context context, List<IContact> contactList,RequestManager glide) {
        super(context, contactList,glide);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    //method used to customize rows of the list
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Holding
        ContactBasicViewHolder holder;

        //////////////////////////////////////////////////////////////
        //
        //////////////////////////////////////////////////////////////

        final IContact contact = getItem(position);

        if(contact == null || StringUtils.isEmpty(contact.getLogin())){
            LoggerHelper.error("Contact is not supposed to be null at this point");
            return convertView;
        }

        //////////////////////////////////////////////////////////////
        //Inflate row / Setup holder datas
        //////////////////////////////////////////////////////////////

        if(convertView == null){
            LoggerHelper.info("row == null");
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_user_item_list, parent,false);
            holder = new ContactBasicViewHolder(convertView,contact.getLogin());
            convertView.setTag(holder);
        }
        else{
            holder = (ContactBasicViewHolder) convertView.getTag();
        }

        //////////////////////////////////////////////////////////////
        //set specific datas
        //////////////////////////////////////////////////////////////

        //set login
        holder.setLogin(contact.getLogin());

        //////////////////////////////////////////////////////////////
        //return row
        //////////////////////////////////////////////////////////////

        return convertView;
    }

    public void sendMessages(){
        LoggerHelper.info("IN sendMessages");

        ArrayList<String> selectedContacts = getSelectedItems();

        for(String contact : selectedContacts){
            LoggerHelper.info("CurrentContactListFragment.onItemClick vibrationMessage == null" );
            try {
                MessageUtil.persistVibrationMessage(getContext(), contact);
            }
            catch (Exception e){
                LoggerHelper.info(this.getClass().toString(),"firebase exception ??");
            }
        }

        showSendResultDialog();
        //showSuccessResult();
    }

    private void showSendResultDialog(){
        //show dialog View

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        view = inflater.inflate(R.layout.layout_message_sent_animation,null);

        //create builder

        builder.setView(view);
        dialog = builder.create();
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_slide;
        dialog.setCancelable(true);

        //dismiss Listener

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                LoggerHelper.info(getClass(),"onCancel Dialog");
                gotoTimeLine();
            }
        });

        dialog.show();

        //Animate

        ImageView imageView = (ImageView) view.findViewById(R.id.message_sent_image_animation);
        Animation fadeIn = new AlphaAnimation(0, 1);
        Animation fadeOut = new AlphaAnimation(1,0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(3000);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(3000);

        //listener

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                LoggerHelper.info("onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LoggerHelper.info("onAnimationEnd");
                showSuccessResult();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        imageView.startAnimation(fadeIn);
    }

    private void gotoTimeLine(){

/*        //clean
        if(dialog != null) {
            dialog.dismiss();
        }*/
        //go to activity

        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    public void showSuccessResult(){

        view = inflater.inflate(R.layout.layout_message_sent,null);

        //Show result

        ImageView avatarImage1 = (ImageView) view.findViewById(R.id.avatarImage_1);
        ImageView avatarImage2 = (ImageView) view.findViewById(R.id.avatarImage_2);
        TextView counter_tv = (TextView) view.findViewById(R.id.timeline_avatar_counter_for_layout_right);

        //close button

        ImageView close = (ImageView) view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //adapt content
        ArrayList<String> contactList = getSelectedItems();

        LoggerHelper.info("invitationList size " + contactList.size());

        if(contactList.size() > 0 ){
            PictureStorage.loadAvatar(glide,context,contactList.get(0),avatarImage1);
            if(contactList.size() > 1){
                LoggerHelper.info("selected > 1 ");
                PictureStorage.loadAvatar(glide,context,contactList.get(1),avatarImage2);
                counter_tv.setText(String.valueOf(contactList.size()));
            }
            else{
                LoggerHelper.info("selected == 1");
                avatarImage2.setVisibility(View.INVISIBLE);
                counter_tv.setVisibility(View.INVISIBLE);
            }

            //if error or success adapt text

            StringBuilder textBuilder = new StringBuilder();
            TextView targetTextView = (TextView) view.findViewById(R.id.status_target_text);
            textBuilder.append(contactList.get(0));

            if(contactList.size() > 1 ){
                textBuilder.append(String.format(Locale.getDefault()," and %d more contacts",contactList.size() - 1));
            }

            targetTextView.setText(textBuilder.toString());
        }

        // show dialog

        dialog.dismiss();
        dialog.setContentView(view);

        if(dialog.getWindow() != null){
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_slide;
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                LoggerHelper.info(getClass(),"onDismiss Dialog");
                gotoTimeLine();
            }
        });


        dialog.show();
        this.clearSelection();
    }

    public void setNewSelection(int position, boolean value) {
        mSelection.put(position, value);
        notifyDataSetChanged();
    }

    public boolean isPositionChecked(int position) {
        Boolean result = mSelection.get(position);
        return result == null ? false : result;
    }

    private Set<Integer> getCurrentCheckedPosition() {
        return mSelection.keySet();
    }

    public void removeSelection(int position) {
        mSelection.remove(position);
        notifyDataSetChanged();
    }

    @SuppressWarnings("unchecked")
    public void clearSelection() {
        mSelection = new HashMap();
        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectedItems(){

        ArrayList<String> selectedContacts = new ArrayList<>();

        if(dataList != null && dataList.size() > 0){
            for(Integer key : getCurrentCheckedPosition()){
                selectedContacts.add(dataList.get(key).getLogin());
            }
        }

        return selectedContacts;
    }
}
