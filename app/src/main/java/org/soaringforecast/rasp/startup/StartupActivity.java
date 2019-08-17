package org.soaringforecast.rasp.startup;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.soaring.forecast.ForecastDrawerActivity;

import timber.log.Timber;

public class StartupActivity extends AppCompatActivity {

    private View view1;
    private boolean isFirstTime = true;
    private long duration = 5000;
    private AnimatorSet animatorSet;
    private boolean paused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        view1 = findViewById(R.id.startup_activity_glider_right_silhouette);

    }

    @Override
    public void onResume() {
        super.onResume();
        view1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view1.setVisibility(View.VISIBLE);
                if (isFirstTime) {
                    Animator rotateAnimator = getRotationAnimator(view1, duration, 0, 720);
                    // Animator translationXAnimator = getTranslationXAnimator(view1, duration,
                    //        0f, 200f, 200f, 0, 0, -200f, -200, 0, 0f, 200f, 200f, 0, 0, -200f, -200, 0);
                    //Animator translationYAnimator = getTranslationYAnimator(view1, duration, 0f, -400f);
                    Animator circlingAnimator = getCirclingAnimator(view1, duration);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(rotateAnimator, circlingAnimator);
                    view1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!paused) {
                                animatorSet.pause();

                            } else {
                                animatorSet.resume();
                            }
                            paused = !paused;
                        }
                    });
                    animatorSet.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            displayForacastDrawerActivity();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });
                    animatorSet.start();

                    isFirstTime = false;

                }
            }
        });

    }

    private Animator getRotationAnimator(View view, long duration, float... values) {
        //return ObjectAnimator.ofFloat(view, View.ROTATION_Y, values).setDuration(duration);
        ValueAnimator animator = ValueAnimator.ofFloat(values); // values from 0 to 1
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
//                float adjustedAngle;
//                float sign;
//                float value = (float) animator.getAnimatedValue();
//                if ((value <=.5) || (value >= 1.5 && value < 2.5) || value >= 3.5)  {
//                    adjustedAngle = 0;
//                    sign = 0f;
//                } else {
//                    adjustedAngle = -180;
//                    sign = -1f;
//                }
//                float rotationValue = adjustedAngle + (90f * (float) Math.sin((Float) animator.getAnimatedValue() * Math.PI * 2f));
//                Timber.d("Animator value: %1$f2   Rotation degrees y: %2$f2   Angle: %3$f2"
//                        , (float) animator.getAnimatedValue(), rotationValue, ((float) Math.sin((Float) animator.getAnimatedValue() * Math.PI * 2f)));
                //               view.setRotationY(roationValue);
                Float sineValue = (float) Math.sin(((Float) animator.getAnimatedValue() + 90f)* Math.PI/ 180);
                Timber.d("Animator value: %1$f2   Sine of value: %2$f2"
                        , (float) animator.getAnimatedValue(), sineValue);
                view.setRotationY((float) animator.getAnimatedValue());
                ViewGroup.LayoutParams params = view.getLayoutParams();
                float aspectRatio = ((float) params.height) / (float) params.width;
                params.height =  params.height - ((int) ((1f - sineValue) * 50f * aspectRatio));
                params.width = params.width - ((int) ((1f - sineValue) * 50f));
               view.getParent().requestLayout();


            }
        });
        return animator;

    }

    private Animator getTranslationXAnimator(View view, long duration, float... values) {
        Animator translationXAnimator = ObjectAnimator
                .ofFloat(view, View.TRANSLATION_X, values)
                .setDuration(duration);
        translationXAnimator.setInterpolator(new AccelerateInterpolator());
        return translationXAnimator;
    }

    private Animator getTranslationYAnimator(View view, long duration, float... values) {
        Animator translationYAnimator = ObjectAnimator
                .ofFloat(view1, View.TRANSLATION_Y, values)
                .setDuration(duration);
        return translationYAnimator;
    }

    private Animator getCirclingAnimator(View view, long duration) {
        final float amplitude = 200f;
        ValueAnimator animator = ValueAnimator.ofFloat(-1, 1); // values from 0 to 1
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float translationY, translationX;
                translationY = -amplitude * (float) animator.getAnimatedValue();
                translationX = amplitude * (float) Math.sin((Float) animator.getAnimatedValue() * Math.PI * 2);
                //Timber.d("Translation x: %1$f2  y: %2$f2", translationX, translationY);
                view.setTranslationY(translationY);
                view.setTranslationX(translationX);
            }
        });
        return animator;
    }

    private void displayForacastDrawerActivity() {
        ForecastDrawerActivity.Builder builder = ForecastDrawerActivity.Builder.getBuilder();
        startActivity(builder.build(this));
        finish();
    }

}
