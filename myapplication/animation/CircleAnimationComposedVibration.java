package fr.myapplication.dc.myapplication.animation;

import android.view.animation.Animation;
import android.view.animation.Transformation;

import fr.myapplication.dc.myapplication.view.composer.VibrationComposerView;

/**
 * Created by Crono on 14/02/17.
 */

public class CircleAnimationComposedVibration extends Animation {

    private VibrationComposerView circle;

    private float oldRadius;
    private float newAngle;

    public CircleAnimationComposedVibration(VibrationComposerView circle, int newAngle) {
        this.oldRadius = circle.getRadius();
        this.newAngle = newAngle;
        this.circle = circle;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation) {
        float angle = oldRadius + ((newAngle - oldRadius) * interpolatedTime);
        circle.setRadius(angle);
        circle.invalidate();
    }
}
