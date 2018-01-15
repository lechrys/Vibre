package fr.myapplication.dc.myapplication.view.adapter.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import fr.myapplication.dc.myapplication.R;

/**
 * Created by jhamid on 08/10/2017.
 */

public class BasicRecyclerViewHolder extends RecyclerView.ViewHolder {

    protected TextView login;
    protected ImageView avatar;

    public BasicRecyclerViewHolder(View row) {
        super(row);
        this.login = (TextView) row.findViewById(R.id.loginTextView);
        this.avatar = (ImageView) row.findViewById(R.id.avatarImage);
    }
}
