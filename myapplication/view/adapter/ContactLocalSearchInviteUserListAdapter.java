package fr.myapplication.dc.myapplication.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.data.model.IContact;
import fr.myapplication.dc.myapplication.data.model.IUser;
import fr.myapplication.dc.myapplication.data.model.User;
import util.Constants;
import util.LoggerHelper;

/**
 * Created by Crono on 04/12/16.
 * this Adapter is a bridge between rootView and contact datas
 */

public class ContactLocalSearchInviteUserListAdapter extends CustomArrayAdapter<IUser> {

    protected Context context;

    public ContactLocalSearchInviteUserListAdapter(Context context, List<IUser> users) {
        super(context,users);
        this.context = context;
    }

    //method used to customize rows of the list
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ContactLocalSearchListAdapterHolder holder;

        //////////////////////////////////////////////////////////////
        //
        //////////////////////////////////////////////////////////////

        final User user = (User) getItem(position);

        if(user == null || user.getLogin().isEmpty()){
            LoggerHelper.error("Contact is not supposed to be null at this point");
            return convertView;
        }

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_invite_local_contact, parent,false);
            holder = new ContactLocalSearchListAdapterHolder(convertView,user.getLogin(),user.getPhone());
            convertView.setTag(holder);
        }
        else{
            holder = (ContactLocalSearchListAdapterHolder) convertView.getTag();
        }

        //move cursor
        //cursor.moveToPosition(position);

        //the login
        LoggerHelper.info("login is " + user.getLogin());
        holder.setLogin(user.getLogin());

        //the phone number

        LoggerHelper.info("phone is " + user.getPhone());
        holder.setPhone(user.getPhone());

        //addUserButton
        holder.getInviteUserButton();

        //LoggerHelper.info("contact is " + contact.toString() );
        //textView.setText(contact.getLogin());

        return convertView;
    }

    private  View.OnClickListener getAcceptClickListener() {

        final ContactLocalSearchInviteUserListAdapter adapter = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggerHelper.info("ContactOnlineSearchListAdapter.getDeleteClickListener");
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "http://www.vibre.com" );
                sendIntent.setType("text/plain");
                context.startActivity(sendIntent);
            }
        };
        return listener;
    }


    private class ContactLocalSearchListAdapterHolder {

        private TextView login;
        private TextView phoneNumber;
        private Button inviteUserButton;
        View row;

        private ContactLocalSearchListAdapterHolder(View row, String login, String phone){
            this.row = row;
            setLogin(login);
            setPhone(phone);
        }

        public TextView getLogin() {
            if(login == null){
                login = (TextView) row.findViewById(R.id.loginTextView);
            }
            return login;
        }

        public TextView getPhone() {
            if(phoneNumber == null){
                phoneNumber = (TextView) row.findViewById(R.id.phoneNumber);
            }
            return phoneNumber;
        }

        public Button getInviteUserButton(){
            if(inviteUserButton == null){
                inviteUserButton = (Button) row.findViewById(R.id.inviteUserButton);
                inviteUserButton.setOnClickListener(getAcceptClickListener());
            }
            return inviteUserButton;
        }

        public void setLogin(final String phoneText){
            this.getLogin().setText(phoneText);
        }

        public void setPhone(final String phoneText){
            this.getPhone().setText(phoneText);
        }

    }
}
