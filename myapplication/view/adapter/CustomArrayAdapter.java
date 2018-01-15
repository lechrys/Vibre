package fr.myapplication.dc.myapplication.view.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.media.PictureStorage;
import util.LoggerHelper;

/**
 * Created by Crono on 04/12/16.
 */

public class CustomArrayAdapter<T> extends ArrayAdapter<T> {

    protected RequestManager glide;
    public Context context;
    protected final List<T> dataList;

    public CustomArrayAdapter(Context context, RequestManager glide) {
        super(context, -1);
        this.context = context;
        this.dataList = null;
        this.glide = glide;
    }


    public CustomArrayAdapter(Context context, List<T> dataList) {
        super(context, -1 , dataList);
        this.context = context;
        this.dataList = dataList;
    }

    public CustomArrayAdapter(Context context, List<T> dataList, RequestManager glide) {
        super(context, -1 , dataList);
        this.context = context;
        this.dataList = dataList;
        this.glide = glide;
    }

    /*
    * This class holds the Views of the row
    */
    protected class ContactBasicViewHolder {

        protected View row;

        private TextView login;
        private ImageView avatar;

        ContactBasicViewHolder(View row) {
            this.row = row;
        }

        ContactBasicViewHolder(View row,String login) {
            this.row = row;
        }

        public TextView getLogin() {
            if(login == null){
                login = (TextView) row.findViewById(R.id.loginTextView);
            }
            return login;
        }

        public void setLogin(final String login) {
            if( ! StringUtils.isEmpty(login)) {
                this.getLogin().setText(login);
                PictureStorage.loadAvatar(glide,context,login,getAvatar());
                //loadAvatar();
            }
        }

        public ImageView getAvatar() {
            if(avatar == null){
                avatar = (ImageView) row.findViewById(R.id.avatarImage);
            }
            return avatar;
        }
    }

    protected class CustomGlideDrawableImageViewTarget extends GlideDrawableImageViewTarget {

        CustomGlideDrawableImageViewTarget(ImageView view) {
            super(view);
        }

        //When image load fails we set back the default image
        @Override
        public void onLoadFailed(Exception e, Drawable errorDrawable) {
            super.onLoadFailed(e, errorDrawable);
            //never called
            LoggerHelper.debug("onLoadFailed");
            //avatar.invalidate();
            view.setImageResource(R.drawable.icon_default_user);
        }
    }
}
