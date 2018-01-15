package fr.myapplication.dc.myapplication.activity.contact;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.activity.BasicActivity;
import fr.myapplication.dc.myapplication.view.adapter.pager.AddContactPagerAdapter;

public class SearchContactActivity extends AppCompatActivity implements BasicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //setViews
        setViews();

        //initActionBar
        initActionBar();
    }

    /*
     * Set back button on bar
     */
    public void initActionBar(){
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public void setViews() {
        setContentView(R.layout.activity_add_contact);
    }

    @Override
    public void setButtonsListeners() {

    }

}
