package fr.myapplication.dc.myapplication.activity.tutorial;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;

import fr.myapplication.dc.myapplication.R;

/**
 * Created by chris on 13/12/2017.
 */


    public class IntroPageTuto  extends AppCompatActivity {


        private ViewPager mViewPager;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.fragment_tutorial_intro);

            mViewPager = (ViewPager) findViewById(R.id.viewpager);

            // Set an Adapter on the ViewPager
            mViewPager.setAdapter(new TutorialPagerAdapter(getSupportFragmentManager()));

            // Set a PageTransformer
            mViewPager.setPageTransformer(false, new TutoPage2Fragment());
        }

    }
