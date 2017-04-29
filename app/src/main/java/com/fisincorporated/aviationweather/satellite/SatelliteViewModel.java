package com.fisincorporated.aviationweather.satellite;

import android.animation.ValueAnimator;
import android.databinding.DataBindingUtil;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.ViewModelLifeCycle;
import com.fisincorporated.aviationweather.databinding.SatelliteImageDisplayBinding;
import com.github.chrisbanes.photoview.PhotoView;

import org.cache2k.Cache;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class SatelliteViewModel implements ViewModelLifeCycle {

    private SatelliteImageDisplayBinding viewDataBinding;
    private PhotoView satelliteImageView;
    private View bindingView;
    private List<String> satelliteImageNames;
    private ValueAnimator satelliteImageAnimation;
    private float imageScaleFactor = 1;
    private float imageXPosition;
    private float imageYPosition;
    private Matrix imageMatrix = new Matrix();
    private boolean displayedImage = false;
    private int lastImageIndex = -1;
    private ViewGroup.LayoutParams imageLayoutParams ;
    private RectF imageRectf;


    private int regionIdx;

    @Inject
    public SatelliteImageDownloader satelliteImageDownloader;

    @Inject
    public Cache<String, SatelliteImage> satelliteImageCache;

    //@Inject
    public List<SatelliteRegion> satelliteRegions = new ArrayList<>();

    @Inject
    public SatelliteViewModel() {
    }

    public SatelliteViewModel setView(ViewGroup view) {
        bindingView = view.findViewById(R.id.satellite_image_layout);
        viewDataBinding = DataBindingUtil.bind(bindingView);
        satelliteImageView = viewDataBinding.satelliteImageImageView;
        return this;
    }

    @Override
    public void onResume() {
        loadCurrentVisibleSatelliteImage();
        startImageAnimation();
    }

    @Override
    public void onPause() {
        satelliteImageDownloader.cancelOutstandingLoads();
        stopImageAnimation();
    }

    private void startImageAnimation() {

        satelliteImageAnimation = ValueAnimator.ofInt(0, satelliteImageNames.size() - 1);
        satelliteImageAnimation.setInterpolator(new LinearInterpolator());
        satelliteImageAnimation.setDuration(5000);
        satelliteImageAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                SatelliteImage satelliteImage;
                int index = (int) updatedAnimation.getAnimatedValue();
                if (index < satelliteImageNames.size() && lastImageIndex != index ) {
                    satelliteImage = satelliteImageCache.get(satelliteImageNames.get(index));
                    //Bypass if jpg/bitmap does not exist or not loaded yet.
                    if (satelliteImage != null && satelliteImage.isImageLoaded() ) {
                        // Get scale and display matrix  but only if an image was previously displayed
                        if (lastImageIndex != -1 && lastImageIndex != index) {
                            imageScaleFactor = satelliteImageView.getScale();
                            if (imageScaleFactor < satelliteImageView.getMinimumScale()) {
                                imageScaleFactor = satelliteImageView.getMinimumScale();
                            } else if (imageScaleFactor > satelliteImageView.getMaximumScale()) {
                                imageScaleFactor = satelliteImageView.getMaximumScale();
                            }
                            satelliteImageView.getDisplayMatrix(imageMatrix);
                        }

                        satelliteImageView.setImageBitmap(satelliteImage.getBitmap());

                        if (lastImageIndex != -1) {
                            // set scale and position to last image
                            satelliteImageView.setScale(imageScaleFactor);
                            //satelliteImageView.setDisplayMatrix(imageMatrix);
                        }
                        lastImageIndex = index;
                    }
                }
            }
        });
        satelliteImageAnimation.setRepeatCount(ValueAnimator.INFINITE);
        satelliteImageAnimation.start();

    }

    private void stopImageAnimation() {
        if (satelliteImageAnimation != null && satelliteImageAnimation.isRunning()) {
            satelliteImageAnimation.cancel();
        }
    }

    @Override
    public void onDestroy() {
        satelliteImageDownloader.shutdown();
        stopImageAnimation();

    }

    private void loadCurrentVisibleSatelliteImage() {
        satelliteImageDownloader.loadSatelliteImages("alb", "vis");
        satelliteImageNames = satelliteImageDownloader.getSatelliteImageNames();
    }

    public List<SatelliteRegion> getSatelliteRegions() {
        return satelliteRegions;
    }

    public int getRegionIdx() {
        return regionIdx;
    }

    public void setRegionIdx(int regionIdx) {
        this.regionIdx = regionIdx;
    }

}

