package com.fisincorporated.soaringforecast.soaring.forecast;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.messages.CallFailure;
import com.fisincorporated.soaringforecast.messages.SnackbarMessage;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.repository.TaskTurnpoint;
import com.fisincorporated.soaringforecast.soaring.json.Forecast;
import com.fisincorporated.soaringforecast.soaring.json.ModelForecastDate;
import com.fisincorporated.soaringforecast.soaring.json.ModelLocationAndTimes;
import com.fisincorporated.soaringforecast.soaring.json.RegionForecastDate;
import com.fisincorporated.soaringforecast.soaring.json.RegionForecastDates;
import com.fisincorporated.soaringforecast.soaring.json.SoundingLocation;
import com.fisincorporated.soaringforecast.utils.ImageAnimator;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SoaringForecastViewModel extends AndroidViewModel {

    private SoaringForecastDownloader soaringForecastDownloader;
    private AppRepository appRepository;
    private AppPreferences appPreferences;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ValueAnimator soaringForecastImageAnimation;
    private int numberForecastTimes;
    private List<String> forecastTimes;
    private int lastImageIndex = -1;


    private MutableLiveData<SoaringForecastModel> selectedSoaringForecastModel = new MutableLiveData<>();
    private MutableLiveData<List<ModelForecastDate>> modelForecastDates = new MutableLiveData<>();
    private MutableLiveData<ModelForecastDate> selectedModelForecastDate = new MutableLiveData<>();
    private MutableLiveData<Forecast> selectedForecast = new MutableLiveData<>();
    private RegionForecastDates regionForecastDates = new RegionForecastDates();
    private MutableLiveData<List<Forecast>> forecasts;
    private HashMap<String, SoaringForecastImageSet> imageMap = new HashMap<>();
    private MutableLiveData<SoaringForecastImageSet> selectedSoaringForecastImageSet = new MutableLiveData<>();
    private MutableLiveData<SoaringForecastImageSet> selectedSoundingForecastImageSet = new MutableLiveData<>();
    private MutableLiveData<List<SoaringForecastModel>> soaringForecastModels;
    private MutableLiveData<List<SoundingLocation>> soundingLocations = new MutableLiveData<>();
    private MutableLiveData<List<TaskTurnpoint>> taskTurnpoints = new MutableLiveData<>();
    private MutableLiveData<Boolean> loopRunning = new MutableLiveData<>();

    private SoundingLocation selectedSoundingLocation;

    // Used to signal changes to UI
    private MutableLiveData<Boolean> working = new MutableLiveData<>();
    private MutableLiveData<Boolean> soundingDisplay = new MutableLiveData<>();
    private Constants.FORECAST_SOUNDING forecastSounding = Constants.FORECAST_SOUNDING.FORECAST;


    public SoaringForecastViewModel(@NonNull Application application) {
        super(application);
    }

    public SoaringForecastViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    public SoaringForecastViewModel setAppPreferences(AppPreferences appPreferences) {
        this.appPreferences = appPreferences;
        return this;
    }

    public SoaringForecastViewModel setSoaringForecastDownloader(SoaringForecastDownloader soaringForecastDownloader){
        this.soaringForecastDownloader = soaringForecastDownloader;
        return this;
    }

    public void setSelectedForecastModel(SoaringForecastModel selectedForecastModel) {
        if (selectedSoaringForecastModel.getValue() == null
                || !selectedSoaringForecastModel.getValue().equals(selectedForecastModel)){
            selectedSoaringForecastModel.setValue(selectedForecastModel);
            getRegionForecastDates();
        }
        appPreferences.setSoaringForecastModel(selectedForecastModel);
    }

    public LiveData<SoaringForecastModel> getSelectedSoaringForecastModel() {
        if (selectedSoaringForecastModel.getValue() == null) {
            selectedSoaringForecastModel.setValue(appPreferences.getSoaringForecastModel());
            getRegionForecastDates();
        }
        return selectedSoaringForecastModel;
    }


    /**
     * Call when user clicked date
     * @param clickedModelForecastDate
     */
    public void setSelectedModelForecastDate(ModelForecastDate clickedModelForecastDate) {
        if (!clickedModelForecastDate.equals(getSelectedModelForecastDate())) {
            selectedModelForecastDate.setValue(clickedModelForecastDate);
            loadRaspImages();
        }

    }

    public LiveData<ModelForecastDate> getSelectedModelForecastDate() {
         return selectedModelForecastDate;

    }
    private void getRegionForecastDates() {
        Disposable disposable = soaringForecastDownloader.getRegionForecastDates()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(regionForecastDateList -> {
                            storeRegionForecastDates(regionForecastDateList);
                            getModelForecastDates();
                        },
                        throwable -> {
                            Timber.d("Error: %s ", throwable.getMessage());
                            //TODO - put error on bus
                            throwable.printStackTrace();
                        });
        compositeDisposable.add(disposable);
    }

    /**
     * Should be called after response from current.json call
     * You get a list of all dates for which some forecast model will be provided.
     *
     * @param downloadedRegionForecastDates list of forecast dates for the region
     */
    private void storeRegionForecastDates(RegionForecastDates downloadedRegionForecastDates) {
        regionForecastDates = downloadedRegionForecastDates;
        regionForecastDates.parseForecastDates();
        loadTypeLocationAndTimes(appPreferences.getSoaringForecastRegion(), downloadedRegionForecastDates);
    }

    private void loadTypeLocationAndTimes(final String region, final RegionForecastDates regionForecastDates) {
        Disposable disposable = Observable.fromIterable(regionForecastDates.getForecastDates())
                .flatMap((Function<RegionForecastDate, Observable<ModelLocationAndTimes>>)
                        (RegionForecastDate regionForecastDate) ->
                        soaringForecastDownloader.callTypeLocationAndTimes(region, regionForecastDate).toObservable()
                                .doOnNext(regionForecastDate::setModelLocationAndTimes))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ModelLocationAndTimes>() {
                    @Override
                    public void onNext(ModelLocationAndTimes typeLocationAndTimes) {
                        // TODO determine how to combine together into Single
                        //nothing
                    }

                    @Override
                    public void onError(Throwable e) {
                        //TODO - put error on bus
                    }

                    @Override
                    public void onComplete() {
                        getSoaringForecastImages();

                    }
                });
        compositeDisposable.add(disposable);
    }

    /**
     * Called once all soaring info retrieved from server and you are ready to start displaying
     * forecasts.
     */
    // TODO include options to allow customization of which forecast should be displayed first
    //  (i.e. add to Settings options for first forecast model to be displayed, time of display
    //   whether to start animation or not...)
    private void getSoaringForecastImages() {
        // At this point for each date RegionForecastDates contains for each date the type of forecast available
        // So pull out the dates available for the forecast type selected.
        modelForecastDates.setValue(createForecastDateListForSelectedModel());

        // Get whatever current date is to start
        if (modelForecastDates.getValue() != null && modelForecastDates.getValue().size() > 0) {
            setSelectedModelForecastDate(modelForecastDates.getValue().get(0));
            Timber.d("Ready to load bitmaps");
            loadRaspImages();
        } else {
            EventBus.getDefault().post(new SnackbarMessage(getApplication().getString(R.string.model_forecast_for_date_not_available), Snackbar.LENGTH_LONG));
        }
    }

    public MutableLiveData<List<ModelForecastDate>> getModelForecastDates() {
         modelForecastDates.setValue(createForecastDateListForSelectedModel());
         return  modelForecastDates;
    }

    private List<ModelForecastDate> createForecastDateListForSelectedModel() {
        ModelLocationAndTimes modelLocationAndTimes;
        List<ModelForecastDate> modelForecastDateList = new ArrayList<>();
        if (selectedSoaringForecastModel != null && selectedSoaringForecastModel.getValue() != null) {
            String model = selectedSoaringForecastModel.getValue().getName();
            for (RegionForecastDate regionForecastDate : regionForecastDates.getRegionForecastDateList()) {
                modelLocationAndTimes = regionForecastDate.getModelLocationAndTimes();
                if (modelLocationAndTimes != null && modelLocationAndTimes.getGpsLocationAndTimesForModel(model) != null) {
                    ModelForecastDate modelForecastDate = new ModelForecastDate(model,regionForecastDate.getIndex()
                            , regionForecastDate.getFormattedDate(), regionForecastDate.getYyyymmddDate());
                    modelForecastDate.setGpsLocationAndTimes(modelLocationAndTimes.getGpsLocationAndTimesForModel(model));
                    modelForecastDateList.add(modelForecastDate);
                }
            }
        }
        return modelForecastDateList;
    }

    public LiveData<List<Forecast>> getForecasts() {
        if (forecasts == null) {
            forecasts = new MutableLiveData<>();
            loadForecasts();
        }
        return forecasts;
    }

    private void loadForecasts() {
        Disposable disposable = appRepository.getForecasts()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(forecasts1 -> {
                            // rather confusing in naming of forecasts but that is the way it is
                            forecasts.setValue(forecasts1.getForecasts());
                            setDefaultSoaringForecast();
                        },
                        t -> {
                            //TODO email stack trace
                            Timber.e(t);
                        });
        compositeDisposable.add(disposable);
    }

    private void setDefaultSoaringForecast() {
        // TODO cheat here to get wstar - get from appPreferences?
        if (forecasts.getValue() != null && forecasts.getValue().size() > 1) {
            selectedForecast.setValue(forecasts.getValue().get(1));
        }
    }

    public LiveData<Forecast> getSelectedSoaringForecast(){
        return selectedForecast;
    }

    public void setSelectedSoaringForecast(Forecast forecast){
        selectedForecast.setValue(forecast);
        loadRaspImages();
    }

    /**
     * get the list of forecast models - gfs, nam, ...
     * should only need to be called once (or whenever view created)
     * @return  list of forecast models
     */
    public LiveData<List<SoaringForecastModel>> getSoaringForecastModels() {
        working.setValue(true);
        if (soaringForecastModels == null) {
            soaringForecastModels = new MutableLiveData<>();
            soaringForecastModels.setValue(new ArrayList<>());
            loadSoaringForecastModels();
        }
        return soaringForecastModels;
    }

    private void loadSoaringForecastModels() {
        Disposable disposable = appRepository.getSoaringForecastModels()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(soaringForecastModelList -> {
                    // Once the list of models loaded
                            soaringForecastModels.setValue(soaringForecastModelList);
                            // set the selected one
                            getSelectedSoaringForecastModel();
                        },
                        t -> {
                            //TODO email stack trace
                            Timber.e(t);
                        });
        compositeDisposable.add(disposable);

    }

    public void toggleSoundingPoints() {
        boolean displaySoundings = !appPreferences.getDisplayForecastSoundings();
        appPreferences.setDisplayForecastSoundings(displaySoundings);
        getSoundingsLocations(displaySoundings);
    }

    private void getSoundingsLocations(boolean display) {
        if (soundingLocations == null) {
            soundingLocations = new MutableLiveData<>();
        }
        if (display) {
            soundingLocations.setValue(appRepository.getLocationSoundings());
        } else {
            soundingLocations.setValue(null);

        }
    }

    public MutableLiveData<List<SoundingLocation>> getSoundingLocations() {
        return soundingLocations;
    }

    public void soundingImageCloseClick() {
        forecastSounding = Constants.FORECAST_SOUNDING.FORECAST;
        soundingDisplay.setValue(false);
        loadRaspImages();
    }

    public MutableLiveData<Boolean> getSoundingDisplay() {
        return soundingDisplay;
    }

    private void getForecastTimes() {
        forecastTimes = selectedModelForecastDate.getValue().getGpsLocationAndTimes().getTimes();
        numberForecastTimes = forecastTimes.size();
    }

    public void stopImageAnimation() {
        Timber.d("Stopping Animation");
        if (soaringForecastImageAnimation != null) {
            soaringForecastImageAnimation.cancel();
            return;
        }
        Timber.e("soaringForecastImageAnimation is null so no animation to stop");
    }

    // Stepping thru forecast images
    public void onStepClick(Constants.FORECAST_ACTION forecastaction) {
        switch (forecastaction) {
            case BACKWARD:
                stopImageAnimation();
                selectForecastImage(stepImageBy(-1));
                break;
            case FORWARD:
                stopImageAnimation();
                selectForecastImage(stepImageBy(1));
                break;
            case LOOP:
                if (soaringForecastImageAnimation.isRunning()) {
                    stopImageAnimation();
                    setLoopRunning();
                } else {
                    startImageAnimation();
                    setLoopRunning();
                }
                break;
        }
    }

    private void setLoopRunning() {
        loopRunning.setValue(soaringForecastImageAnimation.isRunning());
    }

    public LiveData<Boolean> getLoopRunning() {
        return loopRunning;
    }

    public LiveData<Boolean> getWorking() {
        return working;
    }

    @SuppressLint("CheckResult")
    private void loadRaspImages() {
        stopImageAnimation();
        soundingDisplay.setValue(false);
        imageMap.clear();
        working.setValue(true);
        DisposableObserver disposableObserver = soaringForecastDownloader.getSoaringForecastForTypeAndDay(
                getApplication().getString(R.string.new_england_region)
                , selectedModelForecastDate.getValue().getYyyymmddDate()
                , selectedSoaringForecastModel.getValue().getName()
                , selectedForecast.getValue().getForecastName()
                , selectedModelForecastDate.getValue().getGpsLocationAndTimes().getTimes())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<SoaringForecastImage>() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onNext(SoaringForecastImage soaringForecastImage) {
                        storeImage(soaringForecastImage);
                    }

                    @Override
                    public void onError(Throwable e) {
                        displayCallFailure(e);
                    }

                    @Override
                    public void onComplete() {
                        getForecastTimes();
                        displayRaspImages();
                    }
                });
        compositeDisposable.add(disposableObserver);
    }

    private void displayRaspImages() {
        working.setValue(false);
        forecastSounding = Constants.FORECAST_SOUNDING.FORECAST;
        startImageAnimation();
    }

    private int stepImageBy(int step) {
        lastImageIndex = lastImageIndex + step;
        if (lastImageIndex < 0) {
            lastImageIndex = numberForecastTimes - 1;
        } else if (lastImageIndex > (numberForecastTimes - 1)) {
            lastImageIndex = 0;
        }
        return lastImageIndex;
    }

    private void startImageAnimation() {
        stopImageAnimation();
        // need to 'overshoot' the animation to be able to get the last image value
        Thread thread = Thread.currentThread();
        Timber.d("Creating animation on %1$s ( %2$d )", thread.getName(), thread.getId());
        soaringForecastImageAnimation = ImageAnimator.getInitAnimator(0, numberForecastTimes
                , 15000, ValueAnimator.INFINITE);
        soaringForecastImageAnimation.addUpdateListener(updatedAnimation -> {
//                Timber.d("RunnableJob is being run by %1$s ( %2$d )",  thread.getName(), thread.getId() );
            int index = (int) updatedAnimation.getAnimatedValue();
            //Timber.d("animation index: %d  ", index);
            if (index > numberForecastTimes - 1) {
                index = numberForecastTimes - 1;
            }
            // Don't force redraw if still on same image as last time
            if (lastImageIndex != index) {
                // Timber.d("updating image to index: %1$d", index);
                selectForecastImage(index);
            }
            lastImageIndex = index;
        });
        soaringForecastImageAnimation.start();
    }

    public void selectForecastImage(int index) {
        SoaringForecastImageSet imageSet = imageMap.get(forecastTimes.get(index));
        switch (forecastSounding) {
            case FORECAST:
                displayForecastImageSet(imageSet);
                break;
            case SOUNDING:
                displaySoundingImageSet(imageSet);
                break;
        }
    }

    private void displayForecastImageSet(SoaringForecastImageSet imageSet) {
        if (imageSet != null) {
            if (imageSet.getSideImage() != null
                    && imageSet.getBodyImage() != null) {
                selectedSoaringForecastImageSet.setValue(imageSet);
            }
        }
    }

    public MutableLiveData<SoaringForecastImageSet> getSelectedSoaringForecastImageSet() {
        return selectedSoaringForecastImageSet;
    }

    public void displaySoundingImageSet(SoaringForecastImageSet imageSet) {
        if (imageSet != null) {
            if (imageSet.getBodyImage() != null) {
                selectedSoundingForecastImageSet.setValue(imageSet);
            }
        }
    }

    public MutableLiveData<SoaringForecastImageSet> getSoundingForecastImageSet(){
        return selectedSoundingForecastImageSet;
    }

    private void storeImage(SoaringForecastImage soaringForecastImage) {
        SoaringForecastImageSet imageSet = imageMap.get(soaringForecastImage.getForecastTime());
        if (imageSet == null) {
            imageSet = new SoaringForecastImageSet();
        }
        imageSet.setLocalTime(soaringForecastImage.getForecastTime());
        switch (soaringForecastImage.getBitmapType()) {
            case Constants.BODY:
                imageSet.setBodyImage(soaringForecastImage);
                break;
            case Constants.HEAD:
                imageSet.setHeaderImage(soaringForecastImage);
                break;
            case Constants.SIDE:
                imageSet.setSideImage(soaringForecastImage);
                break;
            case Constants.FOOT:
                imageSet.setFooterImage(soaringForecastImage);
                break;
            default:
                Timber.d("Unknown forecast image type: %s", soaringForecastImage.getBitmapType());
        }
        imageMap.put(soaringForecastImage.getForecastTime(), imageSet);
    }

    private void displayCallFailure(Throwable t) {
        EventBus.getDefault().post(new CallFailure(t.toString()));
    }

    //------- Soundings -------------------

    private void loadForecastSoundings(SoundingLocation soundingLocation) {
        stopImageAnimation();
        working.setValue(true);
        DisposableObserver disposableObserver = soaringForecastDownloader.getSoaringSoundingForTypeAndDay(
                getApplication().getString(R.string.new_england_region)
                , selectedModelForecastDate.getValue().getYyyymmddDate(), selectedSoaringForecastModel.getValue().getName()
                , soundingLocation.getPosition() + ""
                , selectedModelForecastDate.getValue().getGpsLocationAndTimes().getTimes())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<SoaringForecastImage>() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onNext(SoaringForecastImage soaringForecastImage) {
                        storeImage(soaringForecastImage);
                    }

                    @Override
                    public void onError(Throwable e) {
                        displayCallFailure(e);
                    }

                    @Override
                    public void onComplete() {
                        getForecastTimes();
                        displaySoundingImages();
                    }
                });
        compositeDisposable.add(disposableObserver);
    }


    private void displaySoundingImages() {
        working.setValue(false);
        soundingDisplay.setValue(true);
        forecastSounding = Constants.FORECAST_SOUNDING.SOUNDING;
        startImageAnimation();
    }


    // ------- Task display ---------------------

    public void getTask(long taskId) {
        Disposable disposable = appRepository.getTaskTurnpionts(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(taskTurnpointList -> {
                            taskTurnpoints.setValue(taskTurnpointList);
                        },
                        t -> {
                            //TODO email stack trace
                            Timber.e(t);
                        });
        compositeDisposable.add(disposable);
    }

    public MutableLiveData<List<TaskTurnpoint>> getTaskTurnpoints() {
        return taskTurnpoints;
    }

    public  SoundingLocation  getSelectedSoundingLocation() {
        return selectedSoundingLocation;
    }

    public void setSelectedSoundingLocation(SoundingLocation selectedSoundingLocation) {
        this.selectedSoundingLocation = selectedSoundingLocation;
        loadForecastSoundings(selectedSoundingLocation);
    }
}
