package com.fisincorporated.soaringforecast.satellite.noaa;

import android.animation.ValueAnimator;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.databinding.InverseBindingListener;
import android.databinding.InverseBindingMethod;
import android.databinding.InverseBindingMethods;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.satellite.SatelliteImageDownloader;
import com.fisincorporated.soaringforecast.satellite.data.SatelliteImage;
import com.fisincorporated.soaringforecast.satellite.data.SatelliteImageInfo;
import com.fisincorporated.soaringforecast.satellite.data.SatelliteImageType;
import com.fisincorporated.soaringforecast.satellite.data.SatelliteRegion;
import com.fisincorporated.soaringforecast.utils.ImageAnimator;
import com.fisincorporated.soaringforecast.utils.TimeUtils;

import org.cache2k.Cache;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@InverseBindingMethods({@InverseBindingMethod(type = Spinner.class, attribute = "android:selectedItemPosition"),})
public class NoaaSatelliteViewModel extends AndroidViewModel {

    private AppPreferences appPreferences;
    private AppRepository appRepository;

    private MutableLiveData<SatelliteRegion> selectedSatelliteRegion = new MutableLiveData<>();
    private MutableLiveData<SatelliteImageType> selectedSatelliteImageType = new MutableLiveData<>();
    private MutableLiveData<List<SatelliteRegion>> satelliteRegions = new MutableLiveData<>();
    private MutableLiveData<List<SatelliteImageType>> satelliteImageTypes = new MutableLiveData<>();
    private MutableLiveData<SatelliteImage> selectedSatelliteImage = new MutableLiveData<>();
    private MutableLiveData<String> localTimeDisplay = new MutableLiveData<>();

    private MutableLiveData<Bitmap> satelliteBitmap = new MutableLiveData<>();
    private MutableLiveData<Boolean> working = new MutableLiveData<>();
    private MutableLiveData<Boolean> loopRunning = new MutableLiveData<>();

    private SatelliteImageInfo satelliteImageInfo;

    public SatelliteImageDownloader satelliteImageDownloader;
    public Cache<String, SatelliteImage> satelliteImageCache;

    private SatelliteImage satelliteImage;
    private boolean bypassSatelliteRegionChange = true;
    private boolean bypassSatelliteTypeChange = true;

    private ValueAnimator satelliteImageAnimation;
    private int numberImages = 0;
    private int lastImageIndex = -1;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public NoaaSatelliteViewModel(@NonNull Application application) {
        super(application);
    }

    //TODO ?create viewmodel injector and replace?
    public NoaaSatelliteViewModel setAppPreferences(AppPreferences appPreferences) {
        this.appPreferences = appPreferences;
        return this;
    }

    public NoaaSatelliteViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    public NoaaSatelliteViewModel setSatelliteImageDownloader(SatelliteImageDownloader satelliteImageDownloader) {
        this.satelliteImageDownloader = satelliteImageDownloader;
        return this;
    }

    public NoaaSatelliteViewModel setSatelliteImageCache(Cache<String, SatelliteImage> satelliteImageCache) {
        this.satelliteImageCache = satelliteImageCache;
        return this;
    }

    public MutableLiveData<Bitmap> getSatelliteBitmap() {
        return satelliteBitmap;
    }

    public void setup() {
        satelliteRegions.setValue(appRepository.getSatelliteRegions());
        satelliteImageTypes.setValue(appRepository.getSatelliteImageTypes());
        selectedSatelliteRegion.setValue(appPreferences.getSatelliteRegion());
        selectedSatelliteImageType.setValue(appPreferences.getSatelliteImageType());
        localTimeDisplay.setValue("");
        loadSatelliteImages();
    }

    private void loadSatelliteImages() {
        working.setValue(true);
        stopImageAnimation();
        satelliteImageInfo = SatelliteImageDownloader.createSatelliteImageInfo(TimeUtils.getUtcRightNow()
                , selectedSatelliteRegion.getValue().getCode()
                , selectedSatelliteImageType.getValue().getCode());
        selectedSatelliteImage.setValue(null);

        Disposable disposable = satelliteImageDownloader.getImageDownloaderObservable(satelliteImageInfo.getSatelliteImageNames())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Void>() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        confirmLoad();
                        startImageAnimation();

                    }
                });
        compositeDisposable.add(disposable);
    }

    private void confirmLoad() {
        Timber.d("Confirming load in cache");
        SatelliteImage satelliteImage;
        for (String satelliteImageName : satelliteImageInfo.getSatelliteImageNames()) {
            satelliteImage = satelliteImageCache.get(satelliteImageName);
            Timber.d("Satellite image: %s -  %s", satelliteImageName, satelliteImage != null ? " cached" : " not cached");
        }
    }

    public MutableLiveData<List<SatelliteRegion>> getSatelliteRegions() {
        return satelliteRegions;
    }

    public MutableLiveData<List<SatelliteImageType>> getSatelliteImageTypes() {
        return satelliteImageTypes;
    }

    public MutableLiveData<String> getLocalTimeDisplay() {
        return localTimeDisplay;
    }

    // Stepping thru forecast images
    public void onStepClick(Constants.STEP_ACTION forecastaction) {
        switch (forecastaction) {
            case BACKWARD:
                stopImageAnimation();
                selectSatelliteImage(stepImageBy(-1));
                break;
            case FORWARD:
                stopImageAnimation();
                selectSatelliteImage(stepImageBy(1));
                break;
            case LOOP:
                if (satelliteImageAnimation.isRunning()) {
                    stopImageAnimation();
                    setLoopRunning();
                } else {
                    startImageAnimation();
                    setLoopRunning();
                }
                break;
        }
    }

    private void selectSatelliteImage(int i) {
        getSatelliteImageByIndex(i);
    }

    private int stepImageBy(int step) {
        lastImageIndex = lastImageIndex + step;
        if (lastImageIndex < 0) {
            lastImageIndex = satelliteImageInfo.getSatelliteImageNames().size() - 1;
        } else if (lastImageIndex > (satelliteImageInfo.getSatelliteImageNames().size() - 1)) {
            lastImageIndex = 0;
        }
        return lastImageIndex;
    }

    private void startImageAnimation() {
        working.setValue(false);
        numberImages = satelliteImageInfo.getSatelliteImageNames().size();
        // need to 'overshoot' the animation to be able to get the last image value
        satelliteImageAnimation = ImageAnimator.getInitAnimator(0, satelliteImageInfo.getSatelliteImageNames().size(), 5000, ValueAnimator.INFINITE);
        satelliteImageAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                int index = (int) updatedAnimation.getAnimatedValue();
                // Timber.d("animation index: %d  ", index);
                if (index > numberImages - 1) {
                    index = numberImages - 1;
                }
                if (lastImageIndex != index) {
                    // Don't force redraw if still on same image as last time
                    getSatelliteImageByIndex(index);
                    lastImageIndex = index;
                }
            }
        });
        satelliteImageAnimation.setRepeatCount(ValueAnimator.INFINITE);
        satelliteImageAnimation.start();
        setLoopRunning();

    }

    private void getSatelliteImageByIndex(int index) {
        satelliteImage = satelliteImageCache.get(satelliteImageInfo.getSatelliteImageNames().get(index));
        //Bypass if jpg/bitmap does not exist or not loaded yet.
        if (satelliteImage != null && satelliteImage.isImageLoaded()) {
            //Timber.d("Displaying image for %s - %d", satelliteImage.getImageName(), index);
            localTimeDisplay.setValue(satelliteImageInfo.getSatelliteImageLocalTimes().get(index));
            satelliteBitmap.setValue(satelliteImage.getBitmap());
            // Timber.d("set  image for image: %d", index );
        } else {
            // Timber.d("Satellite image: %s, Index %d image: %s",  satelliteImageInfo.getSatelliteImageNames().get(index),index, satelliteImage == null ? " not in cache" : (satelliteImage.isImageLoaded() ? "  bitmap loaded" : " bitmap not loaded"));
        }
    }

    private void stopImageAnimation() {
        if (satelliteImageAnimation != null) {
            satelliteImageAnimation.cancel();
        }
        setLoopRunning();
    }

    private void setLoopRunning() {
        loopRunning.setValue(satelliteImageAnimation == null || satelliteImageAnimation.isRunning());
    }

    public LiveData<Boolean> getLoopRunning() {
        return loopRunning;
    }

    public LiveData<Boolean> getWorking() {
        return working;
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        if (satelliteImageCache != null) {
            satelliteImageCache.clearAndClose();
        }
        super.onCleared();

    }


    public MutableLiveData<SatelliteRegion> getSelectedSatelliteRegion() {
        return selectedSatelliteRegion;
    }


    public void setSelectedSatelliteRegion(SatelliteRegion satelliteRegion) {
        if (!selectedSatelliteRegion.getValue().equals(satelliteRegion)) {
            selectedSatelliteRegion.setValue(satelliteRegion);
            appPreferences.setSatelliteRegion(satelliteRegion);
            loadSatelliteImages();
        }
    }

    public void selectedSatelliteRegion(int pos) {
        selectedSatelliteRegion.setValue(satelliteRegions.getValue().get(pos));
    }

    //-------- Binding for regions (CONUS, Albany, ..
    @BindingAdapter(value = {"selectedRegionValue", "selectedRegionValueAttrChanged"}, requireAll = false)
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

    @InverseBindingAdapter(attribute = "selectedRegionValue", event = "selectedRegionValueAttrChanged")
    public static SatelliteRegion captureSelectedValue(Spinner spinner) {
        return (SatelliteRegion) spinner.getSelectedItem();
    }

    // ---- Binding for satellite image type - Visible, Water Vapor,...

    public MutableLiveData<SatelliteImageType> getSelectedSatelliteImageType() {
        return selectedSatelliteImageType;
    }

    public void setSelectedSatelliteImageType(SatelliteImageType satelliteImageType) {
        if (selectedSatelliteImageType.getValue().equals(satelliteImageType)) {
            selectedSatelliteImageType.setValue(satelliteImageType);
            appPreferences.setSatelliteImageType(satelliteImageType);
            loadSatelliteImages();
        }
    }

    @BindingAdapter(value = {"selectedImageType", "selectedImageTypeAttrChanged"}, requireAll = false)
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

    @InverseBindingAdapter(attribute = "selectedImageType", event = "selectedImageTypeAttrChanged")
    public static SatelliteImageType captureSelectedImageTypeValue(Spinner spinner) {
        return (SatelliteImageType) spinner.getSelectedItem();
    }

}

