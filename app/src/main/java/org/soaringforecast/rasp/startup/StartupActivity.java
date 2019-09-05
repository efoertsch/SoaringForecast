package org.soaringforecast.rasp.startup;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.soaring.forecast.ForecastDrawerActivity;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;
import timber.log.Timber;

public class StartupActivity extends DaggerAppCompatActivity {

    private View gliderSilhoetteView;
    private boolean isFirstTime = true;
    private long duration = 4500;
    private AnimatorSet animatorSet;
    private boolean paused = false;
    private int screenWidth;
    private int halfScreenWidth;
    MediaPlayer audibleVario;
    private View cloudView;
    private TextView appNameView;

    @Inject
    public AppPreferences appPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set an exit transition
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setExitTransition(new Explode());
            getWindow().setAllowEnterTransitionOverlap(true);
        }


        gliderSilhoetteView = findViewById(R.id.startup_activity_glider_right_silhouette);
        cloudView = findViewById(R.id.startup_activity_cloud);
        appNameView = findViewById(R.id.startup_activity_app_name);
        calculateScreenSize();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!appPreferences.getShowStartupAnimation()){
            displayForecastDrawerActivity();
            return;
        }
        gliderSilhoetteView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gliderSilhoetteView.setVisibility(View.VISIBLE);
                if (isFirstTime) {
                    Animator combinedAnimator = getCombinedAnimator(gliderSilhoetteView, duration
                            , 200f, 400f, 360);
                    ObjectAnimator appTitleAnimator = ObjectAnimator.ofFloat(appNameView, "alpha", 1f)
                            .setDuration(duration - 1);
                    appTitleAnimator.setStartDelay(2000);

                    animatorSet = new AnimatorSet();
                    animatorSet.playTogether(combinedAnimator, getCloudAnimator(duration),appTitleAnimator);
                    gliderSilhoetteView.setOnClickListener(v -> {
                        if (!paused) {
                            animatorSet.pause();
                            audibleVario.pause();

                        } else {
                            animatorSet.resume();
                            audibleVario.start();
                        }
                        paused = !paused;
                    });
                    animatorSet.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            startAudibleVario();

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            displayForecastDrawerActivity();
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

    private Animator getCloudAnimator(long duration) {
        ValueAnimator fadeInCloud = ObjectAnimator.ofFloat(cloudView, "alpha", 0f, 1f)
                .setDuration(duration);
        fadeInCloud.setInterpolator(new AccelerateDecelerateInterpolator());
        return fadeInCloud;
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
        screenWidth = displayMetrics.widthPixels;
        halfScreenWidth = screenWidth / 2;
    }

    /**
     * @param view          - view to manipulate
     * @param duration      in milliseconds
     * @param xAmplitude    - distance to oscillate image on x axis
     * @param yAmplitude    - distance to move image on y axis
     * @param totalRotation - degrees to rotate image
     * @return
     */
    private Animator getCombinedAnimator(View view, long duration, float xAmplitude, float yAmplitude, float totalRotation) {
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
                    translationX = (slideInPct * (gliderSilhoetteView.getWidth() + halfScreenWidth - gliderSilhoetteView.getWidth() / 2));
                    Timber.d("Sliding in. Moving onto screen getX(): %1$.1f  slidePct: %2$.1f  translationx: %3$f0, ",
                            gliderSilhoetteView.getX(), slideInPct, translationX);
                    view.setTranslationX(translationX);
                    // increase size as it slides in so will match size as it starts to rotate (skipping sin func here)
                    gliderSilhoetteView.setScaleY(1f + (slideInPct * scaleFactor));
                    gliderSilhoetteView.setScaleX((1f + (slideInPct * scaleFactor)));
                    return;
                } else if (animatedValue > 1000 && animatedValue < duration - 1000) {
                    // This positions the glider on x,y based on oscillateStart and oscillateEnd
                    float pctOfRotationDuration = (animatedValue - 1000) / ((float) duration - 2000f);
                    float rotationValue = (pctOfRotationDuration * totalRotation);
                    Float sineValue = (float) Math.sin(Math.toRadians(rotationValue + 90f));
                    Float cosineValue = (float) Math.sin(Math.toRadians(rotationValue));
                    translationY = -yAmplitude * pctOfRotationDuration;
                    translationX = (gliderSilhoetteView.getWidth() + halfScreenWidth - gliderSilhoetteView.getWidth() / 2) +
                            (xAmplitude * cosineValue);
                    Timber.d("Translation  cosine: %1$.2f  x: %2$.2f  y: %3$.2f, ", cosineValue, translationX, translationY);
                    view.setTranslationY(translationY);
                    view.setTranslationX(translationX);

                    // This rotates and scales the glider smaller/larger as it rotates
                    Timber.d("Rotate and move up. Animator value: %1$.2f  pct of Duration: %2$.2f   Rotation Angle: %3$.2f   Sine of rotation value: %4$.2f"
                            , (float) animatedValue, pctOfRotationDuration, rotationValue, sineValue);
                    view.setRotationY(rotationValue);
                    Timber.d("Original Image scale: width %1$.2f    height: %2$.2f", gliderSilhoetteView.getScaleX(), gliderSilhoetteView.getScaleY());
                    gliderSilhoetteView.setScaleY(1f + (sineValue * scaleFactor));
                    gliderSilhoetteView.setScaleX((1f + (sineValue * scaleFactor)));
                    Timber.d("New Image scale: width %1$.2f    height: %2$.2f", gliderSilhoetteView.getScaleX(), gliderSilhoetteView.getScaleY());
                    return;
//
//                }
                } else {
                    // 1 second to move image from middle of screen to out of view off to right
                    float slideOutPct = (animatedValue - (duration - 1000)) / 1000f;
                    translationX = (gliderSilhoetteView.getWidth() + halfScreenWidth - gliderSilhoetteView.getWidth() / 2)
                            + (slideOutPct * (gliderSilhoetteView.getWidth() / 2 + halfScreenWidth))
                            + ((float) Math.sin(slideOutPct * 90f * Math.PI / 180) * 100);
                    Timber.d("Sliding out. Current getX(): %1$f  slideOutPct: %2$.2f  translationx: %3$f, ",
                            gliderSilhoetteView.getX(), slideOutPct, translationX);
                    view.setTranslationX(translationX);
                    gliderSilhoetteView.setScaleY(1f + scaleFactor);
                    gliderSilhoetteView.setScaleX(1f + scaleFactor);
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


    private void displayForecastDrawerActivity() {
        ForecastDrawerActivity.Builder builder = ForecastDrawerActivity.Builder.getBuilder();
        Intent intent = builder.build(this);
        if (Build.VERSION.SDK_INT >= 21) {
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        } else {
            startActivity(intent);
        }
        finish();
    }

}
