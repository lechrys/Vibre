package fr.myapplication.dc.myapplication.animation;

import android.support.v4.view.ViewPager;
import android.view.View;

import fr.myapplication.dc.myapplication.R;
import util.LoggerHelper;

/**
 * Created by jhamid on 26/11/2017.
 */
public class BubbleTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        float pageWidthTimesPosition = pageWidth * position;
        float absPosition = Math.abs(position);

        LoggerHelper.info(getClass(),"pageWidth" + pageWidth);
        LoggerHelper.info(getClass(),"position" + position);
        LoggerHelper.info(getClass(),"absPosition" + absPosition);

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        }
        else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);

        }
        else if (position <= 1) { // (0,1]

            View cloud1 = view.findViewById(R.id.cloud_1);
            // Fade the page out.
            cloud1.setAlpha(1.0f - absPosition * 8);
            cloud1.setTranslationX( - pageWidthTimesPosition / 2f);
            cloud1.setRotation(position * (180 - 40));
/*            cloud1.setScaleX(1.5f  - absPosition);
            cloud1.setScaleY(1.5f  - absPosition);*/

            View cloud2 = view.findViewById(R.id.cloud_2);
            cloud2.setTranslationY( pageWidthTimesPosition / 2f);
            cloud2.setTranslationX( - pageWidthTimesPosition / 4f);
//            cloud2.setAlpha(1.0f - absPosition * 4);
/*            cloud2.setScaleX(1.0f  - absPosition);
            cloud2.setScaleY(1.0f - absPosition);*/

            View cloud3 = view.findViewById(R.id.cloud_3);
//            cloud3.setTranslationY(-pageWidthTimesPosition / 2f);
            cloud3.setTranslationX( - pageWidthTimesPosition);
            cloud3.setAlpha(1 - absPosition * 2);
/*            cloud3.setScaleX(1.0f  - absPosition);
            cloud3.setScaleY(1.0f  - absPosition);*/

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}

