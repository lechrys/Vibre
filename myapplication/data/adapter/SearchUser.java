package fr.myapplication.dc.myapplication.data.adapter;

import fr.myapplication.dc.myapplication.data.model.IUser;

/**
 * Created by jhamid on 08/10/2017.
 */

public class SearchUser implements IUser{

    private String login;
    private String name_on_phone;
    private String phone;
    private SearchUserType typeUser;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName_on_phone() {
        return name_on_phone;
    }

    public void setName_on_phone(String name_on_phone) {
        this.name_on_phone = name_on_phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public SearchUserType getTypeUser() {
        return typeUser;
    }

    public void setTypeUser(SearchUserType typeUser) {
        this.typeUser = typeUser;
    }

    @Override
    public boolean equals(Object user){

        if(user == null){
            return false;
        }

        if(user instanceof SearchUser) {

            if (((SearchUser) user).getPhone() != null  && ((SearchUser) user).getPhone().equals(this.getPhone())) {
                return true;
            }

        }

        return false;
    }

    public String toString(){

        StringBuilder builder = new StringBuilder();

        builder.append("\n login = ").append(login).append("\n");
        builder.append("telephone = ").append(phone).append("\n");
        builder.append("name_on_phone = ").append(name_on_phone).append("\n");

        return  builder.toString();
    }
}
