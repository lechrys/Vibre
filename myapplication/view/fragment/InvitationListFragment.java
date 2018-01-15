package fr.myapplication.dc.myapplication.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.data.adapter.InviteUserType;
import fr.myapplication.dc.myapplication.data.model.IInvitation;
import fr.myapplication.dc.myapplication.data.model.Invitation;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.view.adapter.InvitationListAdapter;
import util.Constants;
import util.LoggerHelper;
import util.PreferenceUtility;

/**
 * Created by chris on 09/05/2016.
 * List of contact
 * Todo: Use FirebaseListAdapter instead
 */
public class InvitationListFragment extends Fragment
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    ArrayList<IInvitation> invitationReceivedList;
    ArrayList<IInvitation> invitationAcceptedList;
    ArrayList<IInvitation> invitationMergedList;
    ArrayList<String> contactKey;
    InvitationListAdapter adapter;
    DatabaseReference ref_invitations;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LoggerHelper.debug("InvitationListFragment.onCreateView");
        return inflater.inflate(R.layout.fragment_current_contact_recyclerview, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LoggerHelper.info("InvitationListFragment.onActivityCreated");
        //Data List
        invitationReceivedList = new ArrayList<>();
        invitationAcceptedList = new ArrayList<>();
        invitationMergedList = new ArrayList<>();
        contactKey = new ArrayList<>();
        //new addUserAdapter bridge between rootView and data
        adapter = new InvitationListAdapter(getActivity(), invitationMergedList, Glide.with(this));

        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(adapter);

        //listeners
        addReceivedInvitationListListener();
    }

    /*the main business logic of the application
    *notifications are readable/editable from a click on the user
    */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return true;
    }

    private void addReceivedInvitationListListener(){

        final String login = PreferenceUtility.getLogin(this.getActivity());

        if(login == null) {
            LoggerHelper.error("LOGIN should not be null at this point");
            return;
        }
        else{
            LoggerHelper.info(getClass().getName(),"LOGIN = " + login);
        }

        ref_invitations = DataPersistenceManager.db.
                child(Constants.USERS_KEY).
                child(login).
                child(Constants.INVITATION_KEY).
                child(Constants.RECEIVED_INVITATION_KEY);

        //Add listener
        ref_invitations.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot != null && snapshot.getChildrenCount() > 0) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        LoggerHelper.info(InvitationListFragment.class, "IN addReceivedInvitationListListener with child = " + child);
                        Invitation invitation = child.getValue(Invitation.class);
                        if (invitation != null) {
                            invitation.setType(InviteUserType.RECEIVED);
                            invitationReceivedList.add(invitation);
                            contactKey.add(invitation.getLogin());
                        }
                    }
                    if (snapshot.getChildrenCount() > 0) {
                        Invitation header = new Invitation();
                        header.setType(InviteUserType.RECEIVED_HEADER);
                        invitationReceivedList.add(0, header);
                        invitationMergedList.addAll(invitationReceivedList);
                        //adapter.notifyDataSetChanged();
                    }
                }
                ///////////////////////////
                //Now we can take care of accepted invitations
                ///////////////////////////

                addAcceptedInvitationListListener();

                ///////////////////////////
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addAcceptedInvitationListListener(){

        final String login = PreferenceUtility.getLogin(this.getActivity());

        if(login == null) {
            LoggerHelper.error("LOGIN should not be null at this point");
            return;
        }
        else{
            LoggerHelper.info(getClass().getName(),"LOGIN = " + login);
        }

        ref_invitations = DataPersistenceManager.db.
                child(Constants.USERS_KEY).
                child(login).
                child(Constants.INVITATION_KEY).
                child(Constants.ACCEPTED_INVITATION_KEY);

        //Add listener
        ref_invitations.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot != null && snapshot.getChildrenCount() > 0) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        LoggerHelper.info(InvitationListFragment.class, "IN addAcceptedInvitationListListener with child = " + child);
                        Invitation invitation = child.getValue(Invitation.class);
                        if (invitation != null) {
                            LoggerHelper.info(getClass(),"accepted invitation != null");
                            invitation.setType(InviteUserType.ACCEPTED);
                            invitationAcceptedList.add(invitation);
                        }
                    }
                    Invitation header = new Invitation();
                    header.setType(InviteUserType.ACCEPTED_HEADER);
                    invitationAcceptedList.add(0, header);

                    ///////////////////////////////////
                    // MERGE
                    ///////////////////////////////////
                    invitationMergedList.addAll(invitationAcceptedList);

                }
                //we can update the UI
                LoggerHelper.info("invitationMergedList size = " + invitationMergedList.size());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
