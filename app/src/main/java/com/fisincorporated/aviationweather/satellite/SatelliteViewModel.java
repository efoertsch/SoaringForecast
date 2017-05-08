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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.app.ViewModelLifeCycle;
import com.fisincorporated.aviationweather.databinding.SatelliteImageDisplayBinding;

import org.cache2k.Cache;

import java.util.List;

import javax.inject.Inject;


@InverseBindingMethods({@InverseBindingMethod(type = Spinner.class, attribute = "android:selectedItemPosition"),})
public class SatelliteViewModel extends BaseObservable implements ViewModelLifeCycle {

    private static final String TAG = SatelliteViewModel.class.getSimpleName();

    private SatelliteImageDisplayBinding viewDataBinding;
    private TouchImageView satelliteImageView;
    private TextView utcTimeTextView;
    private TextView localTimeTextView;
    private View bindingView;
    private SatelliteImageInfo satelliteImageInfo;
    private ValueAnimator satelliteImageAnimation;
    private float imageScaleFactor = 1;
    private Matrix imageMatrix = new Matrix();
    private int lastImageIndex = -1;
    private SatelliteRegion selectedSatelliteRegion;
    private SatelliteImageType selectedSatelliteImageType;

    private SatelliteImage satelliteImage;

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


        utcTimeTextView = viewDataBinding.satelliteImageUtcTime;
        localTimeTextView = viewDataBinding.satelliteImageLocalTime;
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
        loadSatelliteImages();
        startImageAnimation();
    }


    private void startImageAnimation() {
        stopImageAnimation();
        satelliteImageAnimation = ValueAnimator.ofInt(0, satelliteImageInfo.getSatelliteImageNames().size() - 1);
        satelliteImageAnimation.setInterpolator(new LinearInterpolator());
        satelliteImageAnimation.setDuration(5000);
        satelliteImageAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                int index = (int) updatedAnimation.getAnimatedValue();
                Log.d(TAG, "animation index: " + index);
                if (index < satelliteImageInfo.getSatelliteImageNames().size()  && lastImageIndex != index) {
                    satelliteImage = satelliteImageCache.get(satelliteImageInfo.getSatelliteImageNames().get(index));

                    //Bypass if jpg/bitmap does not exist or not loaded yet.
                    if (satelliteImage != null && satelliteImage.isImageLoaded()) {
//                        if (lastImageIndex != -1) {
//                            // Get scale and display matrix  but only if an image was previously displayed
//                            // TODO figure out how to keep panned image position
//                            imageScaleFactor = satelliteImageView.getScale();
//
//                            if (imageScaleFactor < satelliteImageView.getMinimumScale()) {
//                                imageScaleFactor = satelliteImageView.getMinimumScale();
//                            } else if (imageScaleFactor > satelliteImageView.getMaximumScale()) {
//                                imageScaleFactor = satelliteImageView.getMaximumScale();
//                            }
//                            Log.d(TAG, "image: " + lastImageIndex + " saving scaleFactor:" + imageScaleFactor);
//                           satelliteImageView.getDisplayMatrix(imageMatrix);
//                        }

                        // Don't force redraw if still on same image as last time
                        if (lastImageIndex != index) {
                            Log.d(TAG, "setting image for image: " + index );
                            utcTimeTextView.setText(satelliteImageInfo.getSatelliteImageUTCTimes().get(index));
                            localTimeTextView.setText(satelliteImageInfo.getSatelliteImageLocalTimes().get(index));

                            satelliteImageView.setImageBitmap(satelliteImage.getBitmap());
                            Log.d(TAG, "set  image for image: " + index );

                            Log.d(TAG, "image: " + index + " setting scaleFactor:" + imageScaleFactor);

                        }

                       // satelliteImageView.setScale(imageScaleFactor);

                        lastImageIndex = index;
                    }
                }
            }
        });
        satelliteImageAnimation.setRepeatCount(ValueAnimator.INFINITE);
        satelliteImageAnimation.start();

    }

    private void stopImageAnimation() {
        if (satelliteImageAnimation != null ) {
            satelliteImageAnimation.cancel();
        }
    }

    @Override
    public void onDestroy() {
        satelliteImageDownloader.shutdown();
        stopImageAnimation();

    }

    private void loadSatelliteImages() {
        satelliteImageDownloader.loadSatelliteImages(selectedSatelliteRegion.getCode(), selectedSatelliteImageType.getCode());
        // Get the names of the images so you can start animating them.
        satelliteImageInfo = satelliteImageDownloader.getSatelliteImageInfo();
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

