package fr.myapplication.dc.myapplication.data.model;

import fr.myapplication.dc.myapplication.data.adapter.InviteUserType;

/**
 * Created by Crono on 29/01/17.
 */

public interface IInvitation extends IUser {
    public InviteUserType getType();
}
