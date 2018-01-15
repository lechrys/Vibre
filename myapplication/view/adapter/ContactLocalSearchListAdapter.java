package fr.myapplication.dc.myapplication.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import java.util.List;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.data.adapter.SearchUser;
import fr.myapplication.dc.myapplication.data.adapter.SearchUserType;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.media.PictureStorage;
import fr.myapplication.dc.myapplication.view.adapter.holder.BasicRecyclerViewHolder;
import util.Constants;
import util.LoggerHelper;

/**
 * Created by Crono on 04/12/16.
 * this Adapter is a bridge between rootView and contact datas
 */

public class ContactLocalSearchListAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    RequestManager glide;
    LayoutInflater inflater;
    List<SearchUser> contactList;

    public ContactLocalSearchListAdapter(@NonNull Context context, List<SearchUser> contactList, RequestManager glide) {
        LoggerHelper.info(TimeLineDetailListAdapter.class.toString(),"IN ContactLocalSearchListAdapter");
        this.context = context;
        this.glide = glide;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.contactList = contactList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View convertView;
        RecyclerView.ViewHolder holder = null;

        switch (viewType) {

            case 1:
                convertView = inflater.inflate(R.layout.row_add_header_local_contact, parent, false);
                holder = new ContactLocalSearchAddHeaderListAdapterHolder(convertView);
                break;

            case 2:
                convertView = inflater.inflate(R.layout.row_add_local_contact, parent, false);
                holder = new ContactLocalSearchAddListAdapterHolder(convertView);
                break;

            case 3:
                convertView = inflater.inflate(R.layout.row_invite_header_local_contact, parent, false);
                holder = new ContactLocalSearchInviteHeaderListAdapterHolder(convertView);
                break;

            case 4:
                convertView = inflater.inflate(R.layout.row_invite_local_contact, parent, false);
                holder = new ContactLocalSearchInviteListAdapterHolder(convertView);
                break;

        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        SearchUser item = contactList.get(position);

        if(item.getTypeUser().equals(SearchUserType.ADD)){
            ((ContactLocalSearchAddListAdapterHolder) holder).load(contactList.get(position));
        }

        else if (item.getTypeUser().equals(SearchUserType.INVITE)){
            ((ContactLocalSearchInviteListAdapterHolder) holder).load(contactList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return contactList.get(position).getTypeUser().getType();
    }

    public class ContactLocalSearchAddListAdapterHolder extends BasicRecyclerViewHolder {

        private TextView phoneNumber;
        private TextView name_on_phone;
        private Button addUserButton;

        public ContactLocalSearchAddListAdapterHolder(View row){
            super(row);
            this.phoneNumber = (TextView) row.findViewById(R.id.phoneNumber);
            this.addUserButton = (Button) row.findViewById(R.id.addUserButton);
            this.name_on_phone = (TextView) row.findViewById(R.id.name_on_phone);
        }

        public void load(SearchUser user){
            login.setText(user.getLogin());
            PictureStorage.loadAvatar(glide,context,user.getLogin(),avatar);
            phoneNumber.setText(user.getPhone());
            name_on_phone.setText(user.getName_on_phone());
            addUserButton.setOnClickListener(getAcceptClickListener());
        }

        public View.OnClickListener getAcceptClickListener() {

            final SharedPreferences prefs = context.getSharedPreferences(Constants.APP_ID, Context.MODE_PRIVATE);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ajoutContact(prefs,login.getText().toString());
                }
            };
            return listener;
        }

        //Todo : do the check before adding the contact
        public void ajoutContact(final SharedPreferences prefs, final String login) {

            DataPersistenceManager.persistContact(prefs,login,false);

            DataPersistenceManager.persistReceivedInvitation(prefs,login);
            //remove the contact from the list

            removeAt(getAdapterPosition());
        }

        private void removeAt(final int position){

            LoggerHelper.info(getClass(),"removeAt with position = " + position);
            contactList.remove(position);

            //we remove the header if no more contacts to add
            if(position == 1 && getItemViewType() == SearchUserType.ADD.getType()
                    //check that the next element is not of type ADD, otherwise we are not allowed to remove the header
                    && contactList.get(1) == null || ! contactList.get(1).getTypeUser().equals(SearchUserType.ADD)){
                LoggerHelper.info(getClass(),"removeAt3 with invitationList.get(2).getType() = " + contactList.get(1).getTypeUser());
                contactList.remove(0);
            }

            notifyItemRemoved(position);
        }
    }

    public class ContactLocalSearchAddHeaderListAdapterHolder extends RecyclerView.ViewHolder  {

        TextView textView;

        public ContactLocalSearchAddHeaderListAdapterHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
        }

    }

    public class ContactLocalSearchInviteHeaderListAdapterHolder extends RecyclerView.ViewHolder  {

        TextView textView;

        public ContactLocalSearchInviteHeaderListAdapterHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
        }

    }

    public class ContactLocalSearchInviteListAdapterHolder extends BasicRecyclerViewHolder {

        private TextView phoneNumber;
        private Button inviteUserButton;

        public ContactLocalSearchInviteListAdapterHolder(View row){
            super(row);
            this.phoneNumber = (TextView) row.findViewById(R.id.phoneNumber);
            this.inviteUserButton = (Button) row.findViewById(R.id.inviteUserButton);
        }

        public void load(SearchUser user){
            login.setText(user.getLogin());
            phoneNumber.setText(user.getPhone());
            inviteUserButton.setOnClickListener(getAcceptClickListener());
        }

        private  View.OnClickListener getAcceptClickListener() {

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "http://www.vibre.com" );
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                }
            };

            return listener;
        }
    }
}
