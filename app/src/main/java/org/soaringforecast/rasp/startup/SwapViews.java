package org.soaringforecast.rasp.startup;

import android.view.View;
import android.view.animation.DecelerateInterpolator;

import timber.log.Timber;

// from http://www.inter-fuser.com/2009/08/android-animations-3d-flip.html
public final class SwapViews implements Runnable {

    private boolean mIsFirstView;
    View image1;
    View image2;
    private long duration;

    public SwapViews(boolean isFirstView, View view1, View view2, long duration) {
        mIsFirstView = isFirstView;
        this.image1 = view1;
        this.image2 = view2;
        this.duration = duration;
    }

    public void run() {
        final float centerX = image1.getWidth() / 2.0f;
        final float centerY = image1.getHeight() / 2.0f;
        Flip3dAnimation rotation;

        if (mIsFirstView) {
            image1.setVisibility(View.INVISIBLE);
            image2.setVisibility(View.VISIBLE);
            image2.requestFocus();
            rotation = new Flip3dAnimation(-90, 0, centerX, centerY);
        } else {
            image2.setVisibility(View.INVISIBLE);
            image1.setVisibility(View.VISIBLE);
            image1.requestFocus();
            rotation = new Flip3dAnimation(90, 0, centerX, centerY);

            Timber.d("Swapped views: image1: %1$s  image2: %2$s", (mIsFirstView ? "Visible" : "Invisible"),
                    (!mIsFirstView ? "Invisible" : "Visble"));

        }
        rotation.setDuration(duration);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new DecelerateInterpolator());
        if (mIsFirstView) {
            Timber.d("Starting image2 animation");
            image2.startAnimation(rotation);
        } else {
            Timber.d("Starting image1 animation");
            image1.startAnimation(rotation);
        }

    }
}


