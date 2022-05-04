package org.soaringforecast.rasp.satellite.noaa;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import org.cache2k.Cache;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.Constants;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.satellite.SatelliteImageDownloader;
import org.soaringforecast.rasp.satellite.data.SatelliteImage;
import org.soaringforecast.rasp.satellite.data.SatelliteImageInfo;
import org.soaringforecast.rasp.satellite.data.SatelliteImageType;
import org.soaringforecast.rasp.satellite.data.SatelliteRegion;
import org.soaringforecast.rasp.utils.ImageAnimator;

import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class NoaaSatelliteViewModel extends AndroidViewModel {

    private AppPreferences appPreferences;
    private AppRepository appRepository;

    private MutableLiveData<List<SatelliteRegion>> satelliteRegions = new MutableLiveData<>();
    private SatelliteRegion selectedSatelliteRegion;
    private MutableLiveData<Integer> regionPosition = new MutableLiveData<>();

    private MutableLiveData<List<SatelliteImageType>> satelliteImageTypes = new MutableLiveData<>();
    private SatelliteImageType selectedSatelliteImageType;
    private MutableLiveData<Integer> imageTypePosition = new MutableLiveData<>();

    private MutableLiveData<SatelliteImage> selectedSatelliteImage = new MutableLiveData<>();
    private MutableLiveData<String> localTimeDisplay = new MutableLiveData<>();

    private MutableLiveData<Bitmap> satelliteBitmap = new MutableLiveData<>();
    private MutableLiveData<Boolean> working = new MutableLiveData<>();
    private MutableLiveData<Boolean> loopRunning = new MutableLiveData<>();

    private MutableLiveData<Boolean> steppingEnabled = new MutableLiveData<>();

    private SatelliteImageInfo satelliteImageInfo;

    public SatelliteImageDownloader satelliteImageDownloader;
    public Cache<String, SatelliteImage> satelliteImageCache;

    private SatelliteImage satelliteImage;

    private ValueAnimator satelliteImageAnimation;
    private int numberImages = 0;
    private int lastImageIndex = -1;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public NoaaSatelliteViewModel(@NonNull Application application) {
        super(application);
    }

    //TODO ?create viewmodel factor or injector and replace?
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
        selectedSatelliteRegion = appPreferences.getSatelliteRegion();
        if (selectedSatelliteRegion != null) {
            regionPosition.setValue(satelliteRegions.getValue().indexOf(selectedSatelliteRegion));
        }

        satelliteImageTypes.setValue(appRepository.getSatelliteImageTypes());
        selectedSatelliteImageType = appPreferences.getSatelliteImageType();
        if (selectedSatelliteImageType != null) {
            imageTypePosition.setValue(satelliteImageTypes.getValue().indexOf(selectedSatelliteImageType));
        }
        localTimeDisplay.setValue("");
        findMostRecentSatelliteImage();
    }

    @SuppressLint("CheckResult")
    protected void findMostRecentSatelliteImage(){
        working.setValue(true);
        stopImageAnimation();
        selectedSatelliteImage.setValue(null);

        satelliteImageDownloader.getSatelliteImageInfoSingle(selectedSatelliteRegion.getCode()
                , selectedSatelliteImageType.getCode())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new SingleObserver<SatelliteImageInfo>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(SatelliteImageInfo satelliteImageInfo) {
                        saveSatelliteImageInfo(satelliteImageInfo);
                        loadSatelliteImages();
                        startImageAnimation();
                    }

                    @Override
                    public void onError(Throwable e) {
                        // TODO report
                        Timber.e(e);
                    }
                });

    }

    private void saveSatelliteImageInfo(SatelliteImageInfo satelliteImageInfo) {
        this.satelliteImageInfo = satelliteImageInfo;
    }

    protected void loadSatelliteImages() {
        working.setValue(true);
        steppingEnabled.setValue(false);
       Disposable disposable = satelliteImageDownloader.getImageDownloaderObservable(satelliteImageInfo.getSatelliteImageNames())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Void>() {
                    @Override
                    public void onNext(Void aVoid) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

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
                if (satelliteImageAnimation != null && satelliteImageAnimation.isRunning()) {
                    stopImageAnimation();
                    setLoopRunning();
                } else {
                    startImageAnimation();
                    setLoopRunning();
                }
                break;
        }
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
        steppingEnabled.setValue(true);
        stopImageAnimation();
        numberImages = satelliteImageInfo.getSatelliteImageNames().size();
        // need to 'overshoot' the animation to be able to get the last image value
        satelliteImageAnimation = ImageAnimator.getInitAnimator(0, satelliteImageInfo.getSatelliteImageNames().size(), 5000, ValueAnimator.INFINITE);
        satelliteImageAnimation.addUpdateListener(updatedAnimation -> {
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
        });
        satelliteImageAnimation.setRepeatCount(ValueAnimator.INFINITE);
        satelliteImageAnimation.start();
        setLoopRunning();

    }

    private void selectSatelliteImage(int i) {
        getSatelliteImageByIndex(i);
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


    public LiveData<Boolean> isSteppingEnabled() {
        return steppingEnabled;
    }



    //-------- Satellite Regions (CONUS, Albany, ..
    public MutableLiveData<List<SatelliteRegion>> getSatelliteRegions() {
        return satelliteRegions;
    }

    public MutableLiveData<Integer> getRegionPosition() {
        return regionPosition;
    }

    // Note this never gets called even though 2way databinding specified in xml.
    // The value is updated via databinding directly, not via this method. So you need to
    // check observer for updates and act accordingly
    public void setRegionPosition(MutableLiveData<Integer> regionPosition) {
        this.regionPosition = regionPosition;
    }

    public SatelliteRegion  getSelectedSatelliteRegion() {
        return selectedSatelliteRegion;
    }

    public void setSelectedSatelliteRegion(int pos) {
        setSelectedSatelliteRegion(satelliteRegions.getValue().get(pos));
    }

    public void setSelectedSatelliteRegion(SatelliteRegion satelliteRegion) {
        if (!selectedSatelliteRegion.equals(satelliteRegion)) {
            selectedSatelliteRegion = satelliteRegion;
            appPreferences.setSatelliteRegion(satelliteRegion);
            findMostRecentSatelliteImage();
        }
    }

    // ---- Satellite image type - Visible, Water Vapor,...

    public MutableLiveData<List<SatelliteImageType>> getSatelliteImageTypes() {
        return satelliteImageTypes;
    }

    public MutableLiveData<Integer> getImageTypePosition() {
        return imageTypePosition;
    }

    // Note this never gets called even though 2way databinding specified in xml.
    // The value is updated via databinding directly, not via this method. So you need to
    // check observer for updates and act accordingly
    public void setImageTypePosition(MutableLiveData<Integer> imageTypePosition) {
        this.imageTypePosition = imageTypePosition;

    }

    public SatelliteImageType  getSelectedSatelliteImageType() {
        return selectedSatelliteImageType;
    }

    public void setSelectedSatelliteImageType(int pos) {
        setSelectedSatelliteImageType(satelliteImageTypes.getValue().get(pos));
    }

    public void setSelectedSatelliteImageType(SatelliteImageType satelliteImageType) {
        if (!selectedSatelliteImageType.equals(satelliteImageType)) {
            selectedSatelliteImageType = satelliteImageType;
            appPreferences.setSatelliteImageType(satelliteImageType);
            findMostRecentSatelliteImage();
        }
    }

    public MutableLiveData<String> getLocalTimeDisplay() {
        return localTimeDisplay;
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        stopImageAnimation();
        super.onCleared();
    }

}



