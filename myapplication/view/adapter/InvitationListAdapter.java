package fr.myapplication.dc.myapplication.view.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import java.util.ArrayList;
import java.util.List;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.data.adapter.InviteUserType;
import fr.myapplication.dc.myapplication.data.model.IInvitation;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.media.PictureStorage;
import fr.myapplication.dc.myapplication.view.adapter.holder.BasicRecyclerViewHolder;
import util.Constants;
import util.LoggerHelper;
import util.PreferenceUtility;

/**
 * Created by Crono on 04/12/16.
 * this Adapter is a bridge between rootView and contact datas
 */

public class InvitationListAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder>{

    Context context;
    RequestManager glide;
    LayoutInflater inflater;
    List<IInvitation> invitationList;

    public InvitationListAdapter(Context context, ArrayList<IInvitation> invitationList, RequestManager glide) {
        this.context = context;
        this.glide = glide;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.invitationList = invitationList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LoggerHelper.info(getClass(),"IN onCreateViewHolder");

        View inflatedView;
        RecyclerView.ViewHolder holder = null;

        switch(viewType){
            case 1 :
                inflatedView = inflater.inflate(R.layout.row_invitation_header_received,parent,false);
                holder = new InvitationReceivedHeaderViewHolder(inflatedView);
                break;

            case 2 :
                inflatedView = inflater.inflate(R.layout.row_invitation_received,parent,false);
                holder = new InvitationReceivedViewHolder(inflatedView);
                break;

            case 3 :
                inflatedView = inflater.inflate(R.layout.row_invitation_header_accepted,parent,false);
                holder = new InvitationAcceptedHeaderViewHolder(inflatedView);
                break;

            case 4 :
                inflatedView = inflater.inflate(R.layout.row_invitation_accepted,parent,false);
                holder = new InvitationAcceptedViewHolder(inflatedView);
                break;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        LoggerHelper.info(getClass(),"IN onBindViewHolder with position = " + position);

        IInvitation iInvitation = invitationList.get(position);

        if(iInvitation.getType().equals(InviteUserType.RECEIVED)){
            ((InvitationReceivedViewHolder)holder).load(iInvitation);
        }

        if(iInvitation.getType().equals(InviteUserType.ACCEPTED)){
            ((InvitationAcceptedViewHolder)holder).load(iInvitation);
        }
    }

    @Override
    public int getItemCount() {
        return invitationList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return invitationList.get(position).getType().getType();
    }

    ////////////////////////////////////////////////////
    //HOLDERS
    ////////////////////////////////////////////////////


    public class InvitationReceivedHeaderViewHolder extends RecyclerView.ViewHolder  {

        TextView textView;

        public InvitationReceivedHeaderViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
        }

    }

    public class InvitationReceivedViewHolder extends BasicRecyclerViewHolder {

        private Button acceptInvitation;
        private Button refuseInvitation;

        InvitationReceivedViewHolder(View row) {
            super(row);
            acceptInvitation = (Button) row.findViewById(R.id.acceptInvitation);
            refuseInvitation = (Button) row.findViewById(R.id.refuseInvitation);
        }

        public void load(IInvitation user) {
            login.setText(user.getLogin());
            PictureStorage.loadAvatar(glide, context, user.getLogin(), avatar);
            acceptInvitation.setOnClickListener(getAcceptClickListener());
            refuseInvitation.setOnClickListener(getRefuseClickListener());
        }

        private View.OnClickListener getAcceptClickListener() {

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String loginText = login.getText().toString();

                    final String myLogin = PreferenceUtility.getLogin(context);

                    LoggerHelper.info("IN InvitationListAdapter.addInvitationListListener login=" + myLogin);

                    if(login == null) {
                        LoggerHelper.error("LOGIN should not be null at this point");
                        return;
                    }

                    SharedPreferences prefs = context.getSharedPreferences(Constants.APP_ID, Context.MODE_PRIVATE);

                    //activate contact for the requester

                    DataPersistenceManager.activateContact(prefs,loginText);

                    //persist contact for the current user

                    DataPersistenceManager.persistContact(prefs,loginText,true);

                    //remove contact demand
                    DataPersistenceManager.removeReceivedInvitation(prefs,loginText);

                    //notify demand is accepted
                    DataPersistenceManager.persistAcceptedInvitation(prefs,loginText);

                    //notify that the data has changed to update the list view
                    removeAt(getAdapterPosition());
                }
            };
            return listener;
        }

        private void removeAt(final int position){

            invitationList.remove(position);
            notifyItemRemoved(position);

            //we remove the header if no more contacts to add
            if(position == 1 && getItemViewType() == InviteUserType.RECEIVED.getType()
                    &&  invitationList.get(1) == null || ! invitationList.get(1).getType().equals(InviteUserType.RECEIVED)){

                invitationList.remove(0);
                notifyItemRemoved(0);
            }
        }

        public View.OnClickListener getRefuseClickListener() {

            final SharedPreferences prefs = context.getSharedPreferences(Constants.APP_ID, Context.MODE_PRIVATE);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //remove icon_invitation from DB
                    DataPersistenceManager.removeReceivedInvitation(prefs,login.getText().toString());
                    //update list
                    removeAt(getAdapterPosition());
                }
            };
            return listener;
        }
    }

    public class InvitationAcceptedHeaderViewHolder extends RecyclerView.ViewHolder  {

        TextView textView;

        public InvitationAcceptedHeaderViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
        }

    }

    public class InvitationAcceptedViewHolder extends BasicRecyclerViewHolder {

        InvitationAcceptedViewHolder(View row) {
            super(row);
        }

        public void load(IInvitation user) {
            login.setText(user.getLogin());
            PictureStorage.loadAvatar(glide, context, user.getLogin(), avatar);
        }
    }

}
