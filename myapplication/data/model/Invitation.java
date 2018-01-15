package fr.myapplication.dc.myapplication.data.model;

import fr.myapplication.dc.myapplication.data.adapter.InviteUserType;

/**
 * Created by Crono on 29/01/17.
 */

public class Invitation implements IInvitation{

    //login of the user requesting to be in your contact
    public String login;
    public InviteUserType type;

    public Invitation(){}

    public InviteUserType getType() {
        return type;
    }

    public void setType(InviteUserType type) {
        this.type = type;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

}
