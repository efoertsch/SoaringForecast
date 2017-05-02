package com.fisincorporated.aviationweather.satellite;

import android.animation.ValueAnimator;
import android.databinding.BaseObservable;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.InverseBindingAdapter;
import android.databinding.InverseBindingListener;
import android.databinding.InverseBindingMethod;
import android.databinding.InverseBindingMethods;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.app.ViewModelLifeCycle;
import com.fisincorporated.aviationweather.databinding.SatelliteImageDisplayBinding;
import com.github.chrisbanes.photoview.PhotoView;

import org.cache2k.Cache;

import java.util.List;

import javax.inject.Inject;


@InverseBindingMethods({@InverseBindingMethod(type = Spinner.class, attribute = "android:selectedItemPosition"),})
public class SatelliteViewModel extends BaseObservable implements ViewModelLifeCycle {

    private SatelliteImageDisplayBinding viewDataBinding;
    private PhotoView satelliteImageView;
    private View bindingView;
    private List<String> satelliteImageNames;
    private ValueAnimator satelliteImageAnimation;
    private float imageScaleFactor = 1;
    private Matrix imageMatrix = new Matrix();
    private int lastImageIndex = -1;
    private SatelliteRegion selectedSatelliteRegion;
    private SatelliteImageType selectedSatelliteImageType;

    @Inject
    public SatelliteImageDownloader satelliteImageDownloader;

    @Inject
    public Cache<String, SatelliteImage> satelliteImageCache;

    @Inject
    public List<SatelliteRegion> satelliteRegions;

    @Inject
    public List<SatelliteImageType> satelliteImageTypes;

    @Inject
    public AppPreferences appPreferences;

    @Inject
    public SatelliteViewModel() {
    }

    public SatelliteViewModel setView(ViewGroup view) {
        bindingView = view.findViewById(R.id.satellite_image_layout);
        selectedSatelliteRegion = appPreferences.getSatelliteRegion();
        selectedSatelliteImageType = appPreferences.getSatelliteImageType();
        viewDataBinding = DataBindingUtil.bind(bindingView);
        viewDataBinding.setViewModel(this);
        satelliteImageView = viewDataBinding.satelliteImageImageView;
        // Bind now so we can set selection to any previously stored region
        viewDataBinding.executePendingBindings();
        setSpinnerSelectedSatelliteRegion();
        setSpinnerSelectedSatelliteImageType();

        return this;
    }

    @Override
    public void onResume() {
        displaySatelliteImages();
    }

    @Override
    public void onPause() {
        satelliteImageDownloader.cancelOutstandingLoads();
        stopImageAnimation();
    }

    private void setSpinnerSelectedSatelliteRegion() {
            viewDataBinding.satelliteImageRegionSpinner.setSelection(selectedSatelliteRegion != null ? selectedSatelliteRegion.getId() : 0);
    }

    private void setSpinnerSelectedSatelliteImageType() {
        viewDataBinding.satelliteImageTypeSpinner.setSelection(selectedSatelliteImageType != null ? selectedSatelliteImageType.getId() : 0);
    }

    public void displaySatelliteImages() {
        loadCurrentVisibleSatelliteImage();
        startImageAnimation();
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
                if (index < satelliteImageNames.size() && lastImageIndex != index) {
                    satelliteImage = satelliteImageCache.get(satelliteImageNames.get(index));
                    //Bypass if jpg/bitmap does not exist or not loaded yet.
                    if (satelliteImage != null && satelliteImage.isImageLoaded()) {
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
        satelliteImageDownloader.loadSatelliteImages(selectedSatelliteRegion.getCode(), selectedSatelliteImageType.getCode());
        // Get the names of the images so you can start animating them.
        satelliteImageNames = satelliteImageDownloader.getSatelliteImageNames();
    }

    public List<SatelliteRegion> getSatelliteRegions() {
        return satelliteRegions;
    }

    public SatelliteRegion getSelectedSatelliteRegion() {
        return selectedSatelliteRegion;
    }

    // Called by generated databinding code - see xml
    public void setSelectedSatelliteRegion(SatelliteRegion satelliteRegion) {
        selectedSatelliteRegion = satelliteRegion;
        appPreferences.setSatelliteRegion(selectedSatelliteRegion);
        displaySatelliteImages();
    }


    @BindingAdapter(value = {"bind:selectedRegionValue", "bind:selectedRegionValueAttrChanged"}, requireAll = false)
    public static void bindSpinnerData(Spinner spinner, SatelliteRegion newSelectedValue, final InverseBindingListener newSatelliteAttrChanged) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newSatelliteAttrChanged.onChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (newSelectedValue != null) {
            int pos = spinner.getSelectedItemPosition();
            spinner.setSelection(pos, true);
        }
    }

    @InverseBindingAdapter(attribute = "bind:selectedRegionValue", event = "bind:selectedRegionValueAttrChanged")
    public static SatelliteRegion captureSelectedValue(Spinner spinner) {
        return (SatelliteRegion) spinner.getSelectedItem();
    }


    public List<SatelliteImageType> getSatelliteImageTypes() {
        return satelliteImageTypes;
    }

    public SatelliteImageType getSelectedSatelliteImageType() {
        return selectedSatelliteImageType;
    }

    // Called by generated databinding code - see xml
    public void setSelectedSatelliteImageType(SatelliteImageType satelliteImageType) {
        selectedSatelliteImageType = satelliteImageType;
        appPreferences.setSatelliteImageType(satelliteImageType);
        displaySatelliteImages();
    }

    @BindingAdapter(value = {"bind:selectedImageType", "bind:selectedImageTypeAttrChanged"}, requireAll = false)
    public static void bindImageTypeSpinnerData(Spinner spinner, SatelliteImageType newSelectedValue, final InverseBindingListener newSatelliteAttrChanged) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newSatelliteAttrChanged.onChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (newSelectedValue != null) {
            int pos = spinner.getSelectedItemPosition();
            spinner.setSelection(pos, true);
        }
    }

    @InverseBindingAdapter(attribute = "bind:selectedImageType", event = "bind:selectedImageTypeAttrChanged")
    public static SatelliteImageType captureSelectedImageTypeValue(Spinner spinner) {
        return (SatelliteImageType) spinner.getSelectedItem();
    }

}

