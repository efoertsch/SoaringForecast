package org.soaringforecast.rasp.startup;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;

import org.soaringforecast.rasp.R;

import timber.log.Timber;

public class StartupActivity extends AppCompatActivity {

    private boolean isFirstImage = true;
    private  View view1;
    private View view2;
    private Animation rotateAnimation;
    private long duration = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        view1 = findViewById(R.id.startup_activity_glider_right_silhouette);
        view2 = findViewById(R.id.startup_activity_glider_left_silhouette);

    }

    @Override
    public void onResume(){
        super.onResume();
        view1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                isFirstImage = true;
                view1.setVisibility(View.VISIBLE);
                view2.setVisibility(View.INVISIBLE);
                if (rotateAnimation == null) {
                    //rotate2Images( 0, 90);
                    Animation rotateAnimation = rotateOneImage( view1, 0, 720, 5000);
                    final Animator translationAnimator = ObjectAnimator
                            .ofFloat(view1, View.TRANSLATION_Y, 0f, -200f)
                            .setDuration(duration);
                    AnimatorSet animatorSet = new AnimatorSet();
                    //animatorSet.playTogether(rotateAnimation, translationAnimator);

                }
            }
        });

    }


    // Create an animation instance
    private Animation rotateOneImage(View view, float start, float end, long duration) {
        final float centerX = view.getWidth() / 2.0f;
        final float centerY = view.getHeight() / 2.0f;
        Timber.d("Image1 centerX: %1$f2  centerY: %2$f2", centerX, centerY);
        rotateAnimation= new Flip3dAnimation(start, end, centerX, centerY);
        // Set the animation's parameters
        rotateAnimation.setDuration(duration);
        rotateAnimation.setFillAfter(true);
        //rotateAnimation.setRepeatCount(3);                // -1 = infinite repeated
       rotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator();
        rotateAnimation.setRepeatMode(Animation.RESTART); // reverses each repeat
        rotateAnimation.setFillAfter(true);               // keep rotation after animation
        return rotateAnimation;

    }

    private Animation rotate2Images(View view1, View view2, float start, float end, long duration) {
// Find the center of image
        final float centerX = view1.getWidth() / 2.0f;
        final float centerY = view1.getHeight() / 2.0f;
// Create a new 3D rotation with the supplied parameter
        Timber.d("Image1 centerX: %1$f2  centerY: %2$f2", centerX, centerY);

        rotateAnimation = new Flip3dAnimation(start, end, centerX, centerY);
        rotateAnimation.setDuration( duration);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setInterpolator(new AccelerateInterpolator());
        // The animation listener is used to trigger the next animation
        rotateAnimation.setAnimationListener(new DisplayNextView(isFirstImage, this.view1, this.view2));

        if (isFirstImage) {
            this.view1.startAnimation(rotateAnimation);
            isFirstImage = !isFirstImage;
        } else {

            this.view2.startAnimation(rotateAnimation);
            isFirstImage = !isFirstImage;

        }
       return rotateAnimation;

    }

}
