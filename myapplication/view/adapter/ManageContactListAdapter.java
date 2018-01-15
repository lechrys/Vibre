package fr.myapplication.dc.myapplication.view.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.bumptech.glide.RequestManager;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.contact.ContactDetailActivity;
import fr.myapplication.dc.myapplication.data.model.Contact;
import util.Constants;
import util.LoggerHelper;

/**
 * Created by Crono on 04/12/16.
 * this Adapter is a bridge between rootView and contact datas
 */

public class ManageContactListAdapter extends CustomArrayAdapter<Contact>{

    LayoutInflater inflater;

    public ManageContactListAdapter(Context context, List<Contact> contactList, RequestManager glide) {
        super(context, contactList,glide);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //method used to customize rows of the list
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Holding
        ManageContactViewHolder holder;

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
            convertView = inflater.inflate(R.layout.row_manage_user_item_list, parent,false);
            holder = new ManageContactViewHolder(convertView, contact.getLogin(), position);
            convertView.setTag(holder);
        }
        else{
            holder = (ManageContactViewHolder) convertView.getTag();
        }

        //////////////////////////////////////////////////////////////
        //set specific datas
        //////////////////////////////////////////////////////////////

        //set login
        holder.setLogin(contact.getLogin());
        LoggerHelper.info(getClass().getName(),"holder.setLogin " + contact.getLogin());

        setClickListener(holder,contact);

        //////////////////////////////////////////////////////////////
        //return row
        //////////////////////////////////////////////////////////////

        return convertView;
    }


    public void setClickListener(final ManageContactViewHolder holder,final Contact contact ) {

        holder.getContactDetailsImage().setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                LoggerHelper.info(this.getClass().getName(), "IN onItemClick");

                if (contact != null ) {
                    final String login = contact.getLogin();
                    LoggerHelper.info(this.getClass().getName(), "holder != null " + login);

                    if ( ! StringUtils.isEmpty(login)) {

                        Intent intent = new Intent(getContext(), ContactDetailActivity.class);
                        Bundle b = new Bundle();
                        b.putSerializable(Constants.CONTACTS_KEY, contact);
                        intent.putExtras(b);

                        //animation
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            //Todo : check version of OS to enable or disable Material design anim
                            View sharedView = holder.getAvatar();
                            LoggerHelper.info(getClass(), "sharedView = " + sharedView);
                            String transitionName = context.getString(R.string.avatar_transition);

                            ActivityOptions transitionActivityOptions =
                                    ActivityOptions.makeSceneTransitionAnimation((Activity) context, sharedView, transitionName);

                            //start activity
                            context.startActivity(intent, transitionActivityOptions.toBundle());
                        }
                        else{
                            context.startActivity(intent);
                        }
                    }
                }
            }
        });
    }

    //////////////////////////////////////////////////////////////
    // The view Holder for each rows
    //////////////////////////////////////////////////////////////

    public class ManageContactViewHolder extends ContactBasicViewHolder{

        private Button contactDetailsImage;

        private int position;

        ManageContactViewHolder(View row,String login,int position) {
            super(row,login);
            this.position = position;
        }

        public Button getContactDetailsImage() {
            if(contactDetailsImage == null){
                contactDetailsImage = (Button) row.findViewById(R.id.contact_detail);
            }
            return contactDetailsImage;
        }

        public void setContactDetailsImage(Button contactDetailsImage) {
            this.contactDetailsImage = contactDetailsImage;
        }

        public int getPosition() {
            return position;
        }

    }


}
