package org.soaringforecast.rasp.startup;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.soaring.forecast.ForecastDrawerActivity;

import timber.log.Timber;

public class StartupActivity extends AppCompatActivity {

    private View view1;
    private boolean isFirstTime = true;
    private long duration = 6000;
    private AnimatorSet animatorSet;
    private boolean paused = false;
    private int screenHeight;
    private int screenWidth;
    private int halfScreenWidth;
    MediaPlayer audibleVario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        view1 = findViewById(R.id.startup_activity_glider_right_silhouette);
        calculateScreenSize();

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
                    Animator circlingAnimator = getPositionAnimator(view1, duration);
                    Animator combinedAnimator = getCombinedAnimator(view1, duration, -1, 1, 200f, 400f, 720);
                    animatorSet = new AnimatorSet();
                    //animatorSet.playTogether(rotateAnimator, circlingAnimator);
                    animatorSet.playTogether(combinedAnimator);
                    view1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!paused) {
                                animatorSet.pause();
                                audibleVario.pause();

                            } else {
                                animatorSet.resume();
                                audibleVario.start();
                            }
                            paused = !paused;
                        }
                    });
                    animatorSet.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            startAudibleVario();

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            displayForacastDrawerActivity();
                            finishAudibleVario();
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

    private void startAudibleVario() {
        audibleVario = MediaPlayer.create(StartupActivity.this, R.raw.vario_sound);
        audibleVario.start();
    }

    private void finishAudibleVario() {
        if (audibleVario != null) {
            audibleVario.release();
            audibleVario = null;
        }
    }

    private void calculateScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
        halfScreenWidth = screenWidth / 2;
    }

    /**
     * @param view           - view to manipulate
     * @param duration       in milliseconds
     * @param oscillateStart - start value for oscillating image
     * @param oscillateEnd   - end value for oscillation image
     * @param xAmplitude     - distance to oscillate image on x axis
     * @param yAmplitude     - distance to move image on y axis
     * @param totalRotation  - degrees to rotate image
     * @return
     */
    private Animator getCombinedAnimator(View view, long duration, float oscillateStart, float oscillateEnd, float xAmplitude, float yAmplitude, float totalRotation) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, (float) duration);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float translationY, translationX;
                float animatedValue = (float) animator.getAnimatedValue();
                float scaleFactor = .25f;

                //move the glider onscreen in first second
                if (animatedValue <= 1000) {
                    float slideInPct = animatedValue / 1000f;
                    translationX = (slideInPct * (view1.getWidth() + halfScreenWidth - view1.getWidth() / 2));
                    Timber.d("Sliding in. Moving onto screen getX(): %1$f0  slidePct: %2$.1f  translationx: %3$f0, ",
                            view1.getX(), slideInPct, translationX);
                    view.setTranslationX(translationX);
                    // increase size as it slides in so will match size as it starts to rotate (skipping sin func here)
                    view1.setScaleY(1f + (slideInPct * scaleFactor));
                    view1.setScaleX((1f + (slideInPct * scaleFactor)));
                    return;
                } else if (animatedValue > 1000 && animatedValue < duration - 1000) {
                    // This positions the glider on x,y based on oscillateStart and oscillateEnd
                    float pctOfRotationDuration = (animatedValue - 1000) / ((float) duration - 2000f);
                    translationY = -yAmplitude * pctOfRotationDuration;
                    translationX = (view1.getWidth() + halfScreenWidth - view1.getWidth() / 2) +
                            (xAmplitude * (float) Math.sin((oscillateStart + (pctOfRotationDuration * (oscillateEnd - oscillateStart))) * Math.PI * 2));
                    Timber.d("Translation x: %1$.2f  y: %2$.2f", translationX, translationY);
                    view.setTranslationY(translationY);
                    view.setTranslationX(translationX);

                    // This rotates and scales the glider smaller/larger as it rotates
                    float rotationValue = (pctOfRotationDuration * totalRotation);
                    Float sineValue = (float) Math.sin((rotationValue + 90f) * Math.PI / 180);
                    Timber.d("Rotate and move up. Animator value: %1$.2f  pct of Duration: %2$.2f   Rotation Angle: %3$.2f   Sine of rotation value: %4$.2f"
                            , (float) animatedValue, pctOfRotationDuration, rotationValue, sineValue);
                    view.setRotationY(rotationValue);
                    Timber.d("Original Image scale: width %1$.2f    height: %2$.2f", view1.getScaleX(), view1.getScaleY());
                    view1.setScaleY(1f + (sineValue * scaleFactor));
                    view1.setScaleX((1f + (sineValue * scaleFactor)));
                    Timber.d("New Image scale: width %1$.2f    height: %2$.2f", view1.getScaleX(), view1.getScaleY());
                    return;
//
//                }
                } else {
                    // 1 second to move image from middle of screen to out of view off to right
                    float slideOutPct = (animatedValue - (duration - 1000)) / 1000f;
                    translationX = (view1.getWidth() + halfScreenWidth - view1.getWidth() / 2)
                            + (slideOutPct * (view1.getWidth() / 2 + halfScreenWidth))
                            + ((float) Math.sin(slideOutPct * 90f * Math.PI / 180) * 100);
                    Timber.d("Sliding out. Current getX(): %1$f  slideOutPct: %2$.2f  translationx: %3$f, ",
                            view1.getX(), slideOutPct, translationX);
                    view.setTranslationX(translationX);
                    view1.setScaleY(1f + scaleFactor);
                    view1.setScaleX(1f + scaleFactor);
                }
            }
        });
        return animator;
    }

    private Animator getPositionAnimator(View view, long duration) {
        final float amplitude = 200f;
        ValueAnimator animator = ValueAnimator.ofFloat(-1, 1);
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

    private Animator getRotationAnimator(View view, long duration, float... values) {
        //return ObjectAnimator.ofFloat(view, View.ROTATION_Y, values).setDuration(duration);
        ValueAnimator animator = ValueAnimator.ofFloat(values); // values from 0 to 1
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                Float sineValue = (float) Math.sin(((Float) animator.getAnimatedValue() + 90f) * Math.PI / 180);
                Timber.d("Animator value: %1$f2   Sine of value: %2$f2"
                        , (float) animator.getAnimatedValue(), sineValue);
                view.setRotationY((float) animator.getAnimatedValue());
                ViewGroup.LayoutParams params = view.getLayoutParams();
                float aspectRatio = ((float) params.height) / (float) params.width;
                params.height = params.height - ((int) ((1f - sineValue) * 75f * aspectRatio));
                params.width = params.width - ((int) ((1f - sineValue) * 75f));
                view.getParent().requestLayout();

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
