package org.soaringforecast.rasp.soaring.forecast;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import timber.log.Timber;

/**
 * Animate the display of the sounding view from the sounding marker location up to the
 * size of the containing view. Or vica-versa.
 * Note this just animates the view. What is displayed in the view is handled elsewhere
 */
//TODO Generalize the logic
public class SoundingZoomer {

    private Animator currentAnimator;
    private int shortAnimationDuration = 500;
    private Point startingPoint;
    private RelativeLayout imageLayout;
    private ImageView expandedImageView;
    private View closeButton;
    private Rect finalBounds = new Rect();
    private Point globalOffset = new Point();
    private float startScale;
    private Rect originalBounds;


    SoundingZoomer(Point startingPoint, RelativeLayout imageLayout, ImageView expandedImageView, View closeButton) {
        this.startingPoint = startingPoint;
        Timber.d("Starting point x: %1$d, y: %2$d", startingPoint.x, startingPoint.y);
        this.imageLayout = imageLayout;
        this.expandedImageView = expandedImageView;
        this.closeButton = closeButton;
    }


    public void zoomUpViewToDisplay() {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        imageLayout.setVisibility(View.VISIBLE);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.

        // The start bounds are the x,y location of the sounding marker
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).

        expandedImageView.getGlobalVisibleRect(finalBounds, globalOffset);
        Timber.d("finalBounds: left: %1$d, top: %2$d, right: %3$d, bottom: %4$d", finalBounds.left, finalBounds.top, finalBounds.right, finalBounds.bottom);
        Timber.d("globalOffsets: x: %1$d, y: %2$d", globalOffset.x, globalOffset.y);

        // Adjust starting point
        Rect startBounds = new Rect(startingPoint.x, startingPoint.y + finalBounds.top, startingPoint.x + 1, startingPoint.y + finalBounds.top + 1);


        originalBounds = new Rect(finalBounds.left, finalBounds.top, finalBounds.right, finalBounds.bottom);

        Timber.d("originalBounds: left: %1$d, top: %2$d, right: %3$d, bottom: %4$d", originalBounds.left, originalBounds.top,
                originalBounds.right, originalBounds.bottom);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        Timber.d("startBounds: left: %1$d, top: %2$d, right: %3$d, bottom: %4$d", startBounds.left, startBounds.top,
                startBounds.right, startBounds.bottom);

        Timber.d("finalBounds: left: %1$d, top: %2$d, right: %3$d, bottom: %4$d", finalBounds.left, finalBounds.top,
                finalBounds.right, finalBounds.bottom);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).

        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }
        Timber.d("Adjusted startBounds: left: %1$d, top: %2$d, right: %3$d, bottom: %4$d", startBounds.left, startBounds.top,
                startBounds.right, startBounds.bottom);

        // Show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view at the startingPoint

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        Timber.d("Opening");
        Timber.d("Animating View.X property from %1$d to %2$d", startBounds.left, finalBounds.left);
        Timber.d("Animating View.Y property from %1$d to %2$d", startBounds.top, finalBounds.top);
        Timber.d("Animating View.SCALE_X property from %1$f to %2$f", startScale, 1f);
        Timber.d("Animating View.SCALE_Y property from %1$f to %2$f", startScale, 1f);

        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
                closeButton.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams layoutParams = expandedImageView.getLayoutParams();
                Timber.d("expandedImageView.getLayoutParams() width: %1$d, height: %2$d", layoutParams.width,
                        layoutParams.height);
                Timber.d("expandedImageView.getScaleX(): %1$f", expandedImageView.getScaleX());

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;
    }


    public void zoomDownToHide() {

        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        expandedImageView.getLocalVisibleRect(finalBounds);

        final float startScaleFinal = startScale;
        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set = new AnimatorSet();
        Timber.d("Closing");
        Timber.d("Animating View.X property from %1$f to %2$d", expandedImageView.getX(), startingPoint.x);
        Timber.d("Animating View.Y property from %1$f to %2$d", expandedImageView.getY(), startingPoint.y);
        Timber.d("Original width: %1$d   height: %2$d", expandedImageView.getWidth(), expandedImageView.getHeight());
        Timber.d("Animating View.SCALE_X property from %1$f to %2$f", expandedImageView.getScaleX(), 0f);
        Timber.d("Animating View.SCALE_Y property from %1$f to %2$f", expandedImageView.getScaleY(), 0f);
        set.play(ObjectAnimator
                .ofFloat(expandedImageView, View.X, startingPoint.x))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.Y, startingPoint.y))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.SCALE_X, 0f))
                .with(ObjectAnimator
                        .ofFloat(expandedImageView,
                                View.SCALE_Y, 0f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                closeButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                imageLayout.setVisibility(View.INVISIBLE);
                expandedImageView.setImageBitmap(null);
                expandedImageView.getLayoutParams().width = originalBounds.right - originalBounds.left;
                expandedImageView.getLayoutParams().height = originalBounds.bottom - originalBounds.top;
                expandedImageView.setX(0);
                expandedImageView.setY(0);
                expandedImageView.setScaleX(1f);
                expandedImageView.setScaleY(1f);
                Timber.d("After closing - resetting to original");
                Timber.d("x: %1$f, y: %2$f ", expandedImageView.getX(), expandedImageView.getY());
                Timber.d("width: %1$d, height: %2$d ", expandedImageView.getWidth(), expandedImageView.getHeight());
                Timber.d("View.SCALE_X: %1$f  View.SCALE_Y: %2$f",
                        expandedImageView.getScaleX(), expandedImageView.getScaleY());

                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                imageLayout.setVisibility(View.INVISIBLE);
                expandedImageView.getLayoutParams().width = imageLayout.getWidth();
                expandedImageView.getLayoutParams().height = imageLayout.getHeight();
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;
    }


    public void cancelZooming() {
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
    }

}
