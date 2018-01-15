package fr.myapplication.dc.myapplication.view.adapter.pager;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import fr.myapplication.dc.myapplication.view.fragment.ContactLocalSearchFragment;
import fr.myapplication.dc.myapplication.view.fragment.ContactOnlineSearchFragment;

/**
 * Created by Crono on 04/02/17.
 */

public class AddContactPagerAdapter extends FragmentPagerAdapter {

    protected Context mContext;
    protected final String fragment_titles[] = new String []{"Online","Phone"};

    public AddContactPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    // This method returns the fragment associated with
    // the specified position.
    //
    // It is called when the Adapter needs a fragment
    // and it does not exists.
    public Fragment getItem(int position) {

        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new ContactOnlineSearchFragment();
                return fragment;
            case 1:
                fragment = new ContactLocalSearchFragment();
                return fragment;
                // Create fragment object
        }

        // Attach some data to it that we'll
        // use to populate our fragment layouts
        Bundle args = new Bundle();
        args.putInt("page_position", position + 1);

        // Set the arguments on the fragment
        // that will be fetched in DemoFragment@onCreateView
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return fragment_titles[position];
    }
}
