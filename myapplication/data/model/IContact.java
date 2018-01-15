package fr.myapplication.dc.myapplication.data.model;

import java.util.List;

/**
 * Created by Crono on 29/01/17.
 */

public interface IContact <T extends Message> extends IUser {
    public List<T> getMessageList();
}
