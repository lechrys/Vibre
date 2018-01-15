package fr.myapplication.dc.myapplication.animation;

import android.view.animation.Animation;
import android.view.animation.Transformation;

import fr.myapplication.dc.myapplication.view.composer.VibrationComposerView;
import fr.myapplication.dc.myapplication.view.composer.VibrationSingleView;
import util.LoggerHelper;

/**
 * Created by Crono on 14/02/17.
 */

public class CircleAnimationSingleVibration extends Animation {

    private VibrationSingleView circle;

    private float oldRadius;
    private float newAngle;

    public CircleAnimationSingleVibration(VibrationSingleView circle, int newAngle) {
        LoggerHelper.info("circle.getRadius();" + circle.getRadius());
        this.oldRadius = circle.getRadius();
        this.newAngle = newAngle;
        this.circle = circle;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation) {
        float angle = oldRadius + ((newAngle - oldRadius) * interpolatedTime);
        //LoggerHelper.info("CircleAnimationSingleVibration.applyTransformation with angle="+angle);
        circle.setRadius(angle);
        circle.invalidate();
    }


}
