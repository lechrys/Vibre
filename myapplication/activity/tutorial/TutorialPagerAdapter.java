package fr.myapplication.dc.myapplication.activity.tutorial;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import fr.myapplication.dc.myapplication.activity.timeline.TimeLineFragment;
import fr.myapplication.dc.myapplication.view.fragment.VibrationSingleFragmentWithBubbleViews;

/**
 * Created by chris on 13/12/17.
 */

public class TutorialPagerAdapter extends FragmentPagerAdapter {



        public TutorialPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return TutoPage1Fragment.newInstance(Color.parseColor("#03A9F4"), position); // blue
                default:
                    return TutoPage1Fragment.newInstance(Color.parseColor("#4CAF50"), position); // green
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

    }

