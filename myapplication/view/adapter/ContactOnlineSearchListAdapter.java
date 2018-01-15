package fr.myapplication.dc.myapplication.view.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bumptech.glide.RequestManager;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.data.model.IUser;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import util.Constants;
import util.LoggerHelper;

/**
 * Created by Crono on 04/12/16.
 * this Adapter is a bridge between rootView and contact datas
 */

//public class CurrentContactListAdapter extends FirebaseListAdapter {
public class ContactOnlineSearchListAdapter extends CustomArrayAdapter<IUser>{

    public ContactOnlineSearchListAdapter(Context context, List<IUser> contactList,
                                          RequestManager glide) {
        super(context, contactList,glide);
    }

    //method used to customize rows of the list
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CurrentContactViewHolder holder;

        //////////////////////////////////////////////////////////////
        //
        //////////////////////////////////////////////////////////////

        final IUser contact = getItem(position);

        if(contact == null || StringUtils.isEmpty(contact.getLogin())){
            LoggerHelper.error("Contact is not supposed to be null at this point");
            return convertView;
        }

        LoggerHelper.info("IN InvitationListAdapter.getView with position=" + position + " login="+contact.getLogin());

        //////////////////////////////////////////////////////////////
        //Inflate row / Setup holder datas
        //////////////////////////////////////////////////////////////

        if(convertView == null){
            LoggerHelper.debug("row == null");
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row_search_online_contact, parent,false);
            holder = new CurrentContactViewHolder(convertView,contact.getLogin());
            convertView.setTag(holder);
        }
        else{
            LoggerHelper.debug("row != null");
            holder = (CurrentContactViewHolder) convertView.getTag();
        }

        //////////////////////////////////////////////////////////////
        //set specific datas
        //////////////////////////////////////////////////////////////

        //set login
        holder.setLogin(contact.getLogin());

        holder.getAddUserButton(contact);

        //////////////////////////////////////////////////////////////
        //return row
        //////////////////////////////////////////////////////////////

        return convertView;
    }

    public View.OnClickListener getAcceptClickListener(final IUser contact) {

        final ContactOnlineSearchListAdapter adapter = this;
        final SharedPreferences prefs = adapter.getContext().getSharedPreferences(Constants.APP_ID, Context.MODE_PRIVATE);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoggerHelper.info("ContactOnlineSearchListAdapter.getAcceptClickListener");

                ajoutContact(prefs,contact);
            }
        };
        return listener;
    }

    //Todo : do the check before adding the contact
    public void ajoutContact(final SharedPreferences prefs, final IUser contact) {
        LoggerHelper.info(this.getClass().getName(),"IN ajoutContact");
        //add inactive contact
        DataPersistenceManager.persistContact(prefs,contact.getLogin(),false);
        //add icon_invitation
        DataPersistenceManager.persistReceivedInvitation(prefs,contact.getLogin());
        //remove the contact from the list
        this.remove(contact);

        notifyDataSetChanged();
    }

    private class CurrentContactViewHolder extends ContactBasicViewHolder{

        private Button addUserButton;

        private Button getAddUserButton(final IUser contact) {
            if(addUserButton == null){
                addUserButton = (Button) row.findViewById(R.id.addUserButton);
                addUserButton.setOnClickListener(getAcceptClickListener(contact));
            }
/*            if(status.get(position)){
                LoggerHelper.info("addUserButton.setImageResource");
                addUserButton.setImageResource(R.drawable.contact_added);
                addUserButton.setEnabled(false);
            }*/
            else{
                addUserButton = (Button) row.findViewById(R.id.addUserButton);
                addUserButton.setOnClickListener(getAcceptClickListener(contact));
                addUserButton.setEnabled(true);

            }
            return addUserButton;
        }

        CurrentContactViewHolder(View row, String login) {
            super(row,login);
            LoggerHelper.info("CurrentContactViewHolder constructor");
        }
    }
}
