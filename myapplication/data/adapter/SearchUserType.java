package fr.myapplication.dc.myapplication.data.adapter;

/**
 * Created by jhamid on 08/10/2017.
 */

public enum SearchUserType {

    ADD_HEADER(1),
    ADD(2),
    INVITE_HEADER(3),
    INVITE(4);

    int type;

    SearchUserType(final int type){
        this.type = type;
    }

    public int getType(){
        return type;
    }
}
