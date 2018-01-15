package fr.myapplication.dc.myapplication.view.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import java.util.List;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.data.model.IUser;
import fr.myapplication.dc.myapplication.data.model.User;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import util.Constants;
import util.LoggerHelper;

/**
 * Created by Crono on 04/12/16.
 * this Adapter is a bridge between rootView and contact datas
 */

public class ContactLocalSearchAddUserListAdapter extends CustomArrayAdapter<IUser> {

    protected Context context;

    public ContactLocalSearchAddUserListAdapter(Context context, List<IUser> users, RequestManager glide) {
        super(context,users,glide);
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
            convertView = inflater.inflate(R.layout.row_add_local_contact, parent,false);
            holder = new ContactLocalSearchListAdapterHolder(convertView, user.getLogin());
            convertView.setTag(holder);
        }
        else{
            LoggerHelper.debug("row != null");
            holder = (ContactLocalSearchListAdapterHolder) convertView.getTag();
        }

        //the login
        holder.setLogin(user.getLogin());;

        //the name on the phone
        holder.setName_on_phone(user.getName_on_phone());

        //the phone number
        holder.setPhone(user.getPhone());

        //addUserButton
        holder.getAddUserButton(user);

        return convertView;
    }

    public View.OnClickListener getAcceptClickListener(final IUser contact) {

        final ContactLocalSearchAddUserListAdapter adapter = this;
        final SharedPreferences prefs = adapter.getContext().getSharedPreferences(Constants.APP_ID, Context.MODE_PRIVATE);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggerHelper.info("ContactOnlineSearchListAdapter.getDeleteClickListener");
                ajoutContact(prefs,contact.getLogin(),contact);
            }
        };
        return listener;
    }

    //Todo : do the check before adding the contact
    public void ajoutContact(final SharedPreferences prefs, final String login, final IUser contact) {
        LoggerHelper.info(this.getClass().getName(),"IN ajoutContact");
        //add inactive contact
        DataPersistenceManager.persistContact(prefs,login,false);
        //add icon_invitation
        DataPersistenceManager.persistReceivedInvitation(prefs,login);
        //remove the contact from the list
        this.remove(contact);

        notifyDataSetChanged();
    }


    public class ContactLocalSearchListAdapterHolder extends CustomArrayAdapter.ContactBasicViewHolder {

        private TextView phoneNumber;
        private TextView name_on_phone;
        private Button addUserButton;

        public ContactLocalSearchListAdapterHolder(View row, String login){
            super(row,login);
        }

        public void setPhone(final String phoneText){
            phoneNumber = (TextView) row.findViewById(R.id.phoneNumber);
            phoneNumber.setText(phoneText);
        }

        public Button getAddUserButton(final IUser contact){
            if(addUserButton == null){
                addUserButton = (Button) row.findViewById(R.id.addUserButton);
                addUserButton.setOnClickListener(getAcceptClickListener(contact));
            }
            return addUserButton;
        }

        public void setName_on_phone(final String name) {
            name_on_phone = (TextView) row.findViewById(R.id.name_on_phone);
            name_on_phone.setText(name);
        }
    }
}
