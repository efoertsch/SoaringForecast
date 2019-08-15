package org.soaringforecast.rasp.startup;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import timber.log.Timber;


// Adapted from http://www.inter-fuser.com/2009/08/android-animations-3d-flip.html
public class Flip3dAnimation extends Animation {
    private final float mFromDegrees;
    private final float mToDegrees;
    private final float mCenterX;
    private final float mCenterY;
    private Camera mCamera;


    public Flip3dAnimation(float fromDegrees, float toDegrees,
                           float centerX, float centerY) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mCenterX = centerX;
        mCenterY = centerY;
    }

    @Override

    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
    }



    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final float fromDegrees = mFromDegrees;
        float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);
        final float centerX = mCenterX;
        final float centerY = mCenterY;
        final Camera camera = mCamera;
        final Matrix matrix = t.getMatrix();

        camera.save();
        camera.rotateY(degrees);
        camera.getMatrix(matrix);
        camera.restore();
        Timber.d("PreTranslate Degrees: %1$f   centerX: %2$f  centerY:  %3$f" , degrees, -centerX, -centerY);
        matrix.preTranslate(-centerX, -centerY);
        Timber.d("PostTranslate Degrees: %1$f   centerX: %2$f  centerY:  %3$f" , degrees, centerX, centerY);
        matrix.postTranslate(centerX, centerY);
    }

}
