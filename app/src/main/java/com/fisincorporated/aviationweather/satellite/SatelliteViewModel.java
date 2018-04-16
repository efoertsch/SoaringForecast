package com.fisincorporated.aviationweather.satellite;

import android.animation.ValueAnimator;
import android.databinding.BaseObservable;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.InverseBindingAdapter;
import android.databinding.InverseBindingListener;
import android.databinding.InverseBindingMethod;
import android.databinding.InverseBindingMethods;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.app.ViewModelLifeCycle;
import com.fisincorporated.aviationweather.common.TouchImageView;
import com.fisincorporated.aviationweather.databinding.SatelliteImageBinding;
import com.fisincorporated.aviationweather.messages.DataLoadCompleteEvent;
import com.fisincorporated.aviationweather.messages.DataLoadingEvent;
import com.fisincorporated.aviationweather.satellite.data.SatelliteImage;
import com.fisincorporated.aviationweather.satellite.data.SatelliteImageInfo;
import com.fisincorporated.aviationweather.satellite.data.SatelliteImageType;
import com.fisincorporated.aviationweather.satellite.data.SatelliteRegion;

import org.cache2k.Cache;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

@InverseBindingMethods({@InverseBindingMethod(type = Spinner.class, attribute = "android:selectedItemPosition"),})
public class SatelliteViewModel extends BaseObservable implements ViewModelLifeCycle {

    private static final String TAG = SatelliteViewModel.class.getSimpleName();

    private SatelliteImageBinding viewDataBinding;
    private TouchImageView satelliteImageView;
    private TextView utcTimeTextView;
    private TextView localTimeTextView;
    private SatelliteImageInfo satelliteImageInfo;
    private ValueAnimator satelliteImageAnimation;
    private int lastImageIndex = -1;
    private SatelliteRegion selectedSatelliteRegion;
    private SatelliteImageType selectedSatelliteImageType;

    private SatelliteImage satelliteImage;
    private boolean bypassSatelliteRegionChange = true;
    private boolean bypassSatelliteTypeChange = true;

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
    SatelliteViewModel() {
    }

    public SatelliteViewModel setView(View view) {
        View bindingView = view.findViewById(R.id.satellite_image_layout);
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
        ignoreSpinnerChanges();
        return this;
    }

    @Override
    public void onResume() {
        // Setting the initial satellite region and type will cause call update images so need
        //to bypass
        EventBus.getDefault().register(this);
        loadSatelliteImages();
    }

    private void ignoreSpinnerChanges() {
        bypassSatelliteRegionChange = true;
        bypassSatelliteTypeChange = true;
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        satelliteImageDownloader.cancelOutstandingLoads();
        stopImageAnimation();
    }

    private void setSpinnerSelectedSatelliteRegion() {
        viewDataBinding.satelliteImageRegionSpinner.setSelection(selectedSatelliteRegion != null ? selectedSatelliteRegion.getId() : 0);
    }

    private void setSpinnerSelectedSatelliteImageType() {
        viewDataBinding.satelliteImageTypeSpinner.setSelection(selectedSatelliteImageType != null ? selectedSatelliteImageType.getId() : 0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataLoadingEvent event) {
        stopImageAnimation();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataLoadCompleteEvent event) {
        startImageAnimation();
    }

    @Override
    public void onDestroy() {
        satelliteImageDownloader.shutdown();
        stopImageAnimation();

    }

    private void loadSatelliteImages() {
        stopImageAnimation();
        satelliteImageView.setImageBitmap(null);
        satelliteImageDownloader.loadSatelliteImages(selectedSatelliteRegion.getCode(), selectedSatelliteImageType.getCode());
        // Get the names of the images so you can start animating them.
        satelliteImageInfo = satelliteImageDownloader.getSatelliteImageInfo();
    }

    private void startImageAnimation() {
        // need to 'overshoot' the animation to be able to get the last image value
        satelliteImageAnimation = ValueAnimator.ofInt(0, satelliteImageInfo.getSatelliteImageNames().size());
        satelliteImageAnimation.setInterpolator(new LinearInterpolator());
        satelliteImageAnimation.setDuration(5000);
        satelliteImageAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                int index = (int) updatedAnimation.getAnimatedValue();
                // Timber.d("animation index: %d  ", index);
                if (index > satelliteImageInfo.getSatelliteImageNames().size() - 1) {
                    index = satelliteImageInfo.getSatelliteImageNames().size() - 1;
                }
                if (lastImageIndex != index) {
                    // Don't force redraw if still on same image as last time
                    satelliteImage = satelliteImageCache.get(satelliteImageInfo.getSatelliteImageNames().get(index));
                    //Bypass if jpg/bitmap does not exist or not loaded yet.
                    if (satelliteImage != null && satelliteImage.isImageLoaded()) {
                        //Timber.d("Displaying image for %s - %d", satelliteImage.getImageName(), index);
                        utcTimeTextView.setText(satelliteImageInfo.getSatelliteImageUTCTimes().get(index));
                        localTimeTextView.setText(satelliteImageInfo.getSatelliteImageLocalTimes().get(index));
                        satelliteImageView.setImageBitmap(satelliteImage.getBitmap());
                        // Timber.d("set  image for image: %d", index );
                    } else {
                       // Timber.d("Satellite image: %s, Index %d image: %s",  satelliteImageInfo.getSatelliteImageNames().get(index),index, satelliteImage == null ? " not in cache" : (satelliteImage.isImageLoaded() ? "  bitmap loaded" : " bitmap not loaded"));
                    }
                    lastImageIndex = index;
                }
            }
        });
        satelliteImageAnimation.setRepeatCount(ValueAnimator.INFINITE);
        satelliteImageAnimation.start();

    }

    private void stopImageAnimation() {
        if (satelliteImageAnimation != null) {
            satelliteImageAnimation.cancel();
        }
    }

    public List<SatelliteRegion> getSatelliteRegions() {
        return satelliteRegions;
    }

    public SatelliteRegion getSelectedSatelliteRegion() {
        return selectedSatelliteRegion;
    }

    // Called by generated databinding code - see xml
    // Bypass first call as first time setting type on UI triggers the call
    public void setSelectedSatelliteRegion(SatelliteRegion satelliteRegion) {
        if (bypassSatelliteRegionChange) {
            bypassSatelliteRegionChange = false;
            return;
        }
        selectedSatelliteRegion = satelliteRegion;
        appPreferences.setSatelliteRegion(selectedSatelliteRegion);
        loadSatelliteImages();
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
    // Bypass first call as first time setting type on UI triggers the call
    public void setSelectedSatelliteImageType(SatelliteImageType satelliteImageType) {
        if (bypassSatelliteTypeChange) {
            bypassSatelliteTypeChange = false;
            return;
        }
        selectedSatelliteImageType = satelliteImageType;
        appPreferences.setSatelliteImageType(satelliteImageType);
        loadSatelliteImages();
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

