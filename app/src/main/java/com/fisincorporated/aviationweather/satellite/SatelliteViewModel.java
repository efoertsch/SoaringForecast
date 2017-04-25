package com.fisincorporated.aviationweather.satellite;

import android.animation.ValueAnimator;
import android.databinding.DataBindingUtil;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.ViewModelLifeCycle;
import com.fisincorporated.aviationweather.databinding.SatelliteImageDisplayBinding;
import com.github.chrisbanes.photoview.PhotoView;

import org.cache2k.Cache;

import java.util.List;

import javax.inject.Inject;


public class SatelliteViewModel implements ViewModelLifeCycle {

    private SatelliteImageDisplayBinding viewDataBinding;
    private PhotoView satelliteImageView;
    private View bindingView;
    private List<String> satelliteImageNames;
    private ValueAnimator satelliteImageAnimation;

    @Inject
    public SatelliteImageDownloader satelliteImageDownloader;

    @Inject
    public Cache<String, SatelliteImage> satelliteImageCache;

    @Inject
    public SatelliteViewModel() {
    }

    public SatelliteViewModel setView(ViewGroup view) {
        bindingView = view.findViewById(R.id.satellite_image_imageView);
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
        satelliteImageAnimation = ValueAnimator.ofInt(1, 15);
        satelliteImageAnimation.setDuration(5000);
        satelliteImageAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                SatelliteImage satelliteImage;
                int index = (int )updatedAnimation.getAnimatedValue();
                if (index < satelliteImageNames.size() ) {
                    satelliteImage = satelliteImageCache.get(satelliteImageNames.get(index));
                    if (satelliteImage != null && satelliteImage.isImageLoaded()) {
                        float scale = satelliteImageView.getScale();
                        satelliteImageView.setImageBitmap(satelliteImage.getBitmap());
                        satelliteImageView.setScale(scale);
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
        satelliteImageDownloader.loadSatelliteImages(viewDataBinding.satelliteImageImageView , "alb", "vis");
        satelliteImageNames = satelliteImageDownloader.getSatelliteImageNames();
    }

}

