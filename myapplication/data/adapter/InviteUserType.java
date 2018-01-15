package fr.myapplication.dc.myapplication.data.adapter;

/**
 * Created by jhamid on 08/10/2017.
 */

public enum InviteUserType {

    RECEIVED_HEADER(1),
    RECEIVED(2),
    ACCEPTED_HEADER(3),
    ACCEPTED(4);

    int type;

    InviteUserType(final int type){
        this.type = type;
    }

    public int getType(){
        return type;
    }
}
