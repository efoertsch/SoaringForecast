package com.fisincorporated.soaringforecast.soaring.forecast;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.messages.CallFailure;
import com.fisincorporated.soaringforecast.messages.SnackbarMessage;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.repository.TaskTurnpoint;
import com.fisincorporated.soaringforecast.soaring.json.Forecast;
import com.fisincorporated.soaringforecast.soaring.json.ForecastModels;
import com.fisincorporated.soaringforecast.soaring.json.Forecasts;
import com.fisincorporated.soaringforecast.soaring.json.Model;
import com.fisincorporated.soaringforecast.soaring.json.ModelForecastDate;
import com.fisincorporated.soaringforecast.soaring.json.Region;
import com.fisincorporated.soaringforecast.soaring.json.Regions;
import com.fisincorporated.soaringforecast.soaring.json.Sounding;
import com.fisincorporated.soaringforecast.utils.ImageAnimator;
import com.google.android.gms.maps.model.LatLngBounds;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
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

    // List of modelNames (GFS, NAM, ...)
    private MutableLiveData<List<String>> modelNames = new MutableLiveData<>();
    private MutableLiveData<Integer> modelPosition = new MutableLiveData<>();
    private String selectedModelName;

    // List of dates for the selected model name
    private MutableLiveData<List<ModelForecastDate>> modelForecastDates = new MutableLiveData<>();
    private MutableLiveData<Integer> modelForecastDatePosition = new MutableLiveData<>();
    private ModelForecastDate selectedModelForecastDate;

    private MutableLiveData<List<Forecast>> forecasts = new MutableLiveData<>();
    private MutableLiveData<Integer> forecastPosition = new MutableLiveData<>();
    private MutableLiveData<Forecast> selectedForecast = new MutableLiveData<>();

    private Regions regions;
    private Region selectedRegion;
    private MutableLiveData<LatLngBounds> regionLatLngBounds = new MutableLiveData<>();

    private HashMap<String, SoaringForecastImageSet> imageMap = new HashMap<>();
    private MutableLiveData<SoaringForecastImageSet> selectedSoaringForecastImageSet = new MutableLiveData<>();
    private MutableLiveData<SoaringForecastImageSet> selectedSoundingForecastImageSet = new MutableLiveData<>();

    private MutableLiveData<List<Sounding>> soundings;
    private MutableLiveData<List<TaskTurnpoint>> taskTurnpoints = new MutableLiveData<>();
    private MutableLiveData<Boolean> loopRunning = new MutableLiveData<>();

    private Sounding selectedSounding;

    // Used to signal changes to UI
    private MutableLiveData<Boolean> working = new MutableLiveData<>();
    private MutableLiveData<Boolean> soundingDisplay = new MutableLiveData<>();
    private Constants.FORECAST_SOUNDING forecastSounding = Constants.FORECAST_SOUNDING.FORECAST;

    private boolean displaySoundings;
    private long lastTaskId;
    private boolean loadRasp;

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

    public SoaringForecastViewModel setSoaringForecastDownloader(SoaringForecastDownloader soaringForecastDownloader) {
        this.soaringForecastDownloader = soaringForecastDownloader;
        return this;
    }

    public void checkForChanges() {
        // if region changed then
        // get list of models/dates for that region
        // refresh display
        if (selectedRegion != null && !selectedRegion.getName().equals(appPreferences.getSoaringForecastRegion())) {
            if (regions == null) {
                getRegionForecastDates();
            } else {
                assignSelectedRegion();
            }
            setTaskId(-1);
            taskTurnpoints.setValue(new ArrayList<>());
            soundings.setValue(new ArrayList<>());

        } else {
            checkIfToDisplayTask();
        }
    }

    // The order of API calls are
    // 1. current.json - gets all regions and dates for each region for which some model forecasts have been created
    //
    // 2. status.json - for selected region and date, call status.json to provide list of models (gfs, nam, rap,..), times,
    //    and gps lat/longs for generated forecasts
    //
    // 3. Based on selected model and date, retrieve corresponding forecast bitmaps.
    //

    /**
     * Get list of all modelNames (GFS, NAM, RAP,...) that have a forecast
     */

    public MutableLiveData<List<String>> getModelNames() {
        if (modelNames.getValue() == null) {
            getRegionForecastDates();
        }
        return modelNames;
    }

    public void setModelNames(Region region) {
        int position;
        List<String> modelNameList = new ArrayList<>();
        List<ForecastModels> forecastModelsList = region.getForecastModels();
        for (ForecastModels forecastModels : forecastModelsList) {
            for (Model model : forecastModels.getModels()) {
                if (!modelNameList.contains(model.getName().toUpperCase())) {
                    modelNameList.add(model.getName().toUpperCase());
                }
            }
        }
        Collections.sort(modelNameList);
        if (modelNameList.size() > 0) {
            modelNames.setValue(modelNameList);
            selectedModelName = appPreferences.getForecastModel();
            position = modelNames.getValue().indexOf(selectedModelName);
            if (position >= 0) {
                modelPosition.setValue(position);
            } else {
                selectedModelName = modelNames.getValue().get(0);
                appPreferences.setForecastModel(selectedModelName);
                modelPosition.setValue(0);
            }
        }
    }

    public MutableLiveData<Integer> getModelPosition() {
        return modelPosition;
    }

    public void setModelPosition(int forecastModelPosition) {
        modelPosition.setValue(forecastModelPosition);
        selectedModelName = modelNames.getValue().get(forecastModelPosition);
        appPreferences.setForecastModel(selectedModelName);
        createDatesAndModels(selectedModelName);
    }

    /**
     * Get all regions and dates for which forecasts are available by region
     * Currently returns for NewEngland region 7 dates (current, plus next 6 days)
     * May include other regions/dates
     * Note this does not include list of forecast models (that comes next)
     */
    private void getRegionForecastDates() {
        Disposable disposable = soaringForecastDownloader.getRegionForecastDates()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(regionList -> {
                            regions = regionList;
                            assignSelectedRegion();
                        },
                        throwable -> {
                            Timber.d("Error: %s ", throwable.getMessage());
                            //TODO - put error on bus
                            postMessage(getApplication().getApplicationContext().getString(R.string.oops_error_getting_regions));
                            throwable.printStackTrace();
                        });
        compositeDisposable.add(disposable);
    }

    private void assignSelectedRegion() {
        selectedRegion = getDefaultRegion();
        if (selectedRegion != null) {
            loadForecastModels(selectedRegion);
            // see if soundings should be displayed
            if (displaySoundings = appPreferences.getDisplayForecastSoundings()) {
                loadSoundings();
            }
        } else {
            // TODO display alert dialog on fragment and go to fragment to select region from available regions.
            postMessage(getApplication().getApplicationContext().getString(R.string.default_region_not_in_available_forecast_regions
                    , appPreferences.getSoaringForecastRegion()));
        }
    }


    private Region getDefaultRegion() {
        for (Region region : regions.getRegions()) {
            if (region.getName().equals(appPreferences.getSoaringForecastRegion())) {
                return region;
            }
        }
        return null;
    }

    /**
     * For the selected region (e.g. "New England") and for each date in list
     * get the list of modelNames, lat/lng coordinates and times that a forecast exists
     *
     * @param region
     */
    private void loadForecastModels(Region region) {
        Disposable disposable = Observable.fromIterable(region.getDates())
                .flatMap((Function<String, Observable<ForecastModels>>)
                        (String regionForecastDate) -> {
                            return soaringForecastDownloader.getForecastModels(region.getName(), regionForecastDate).toObservable()
                                    .doOnNext(region::addForecastModels);
                        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ForecastModels>() {
                    @Override
                    public void onNext(ForecastModels forecastModels) {
                        // TODO determine how to combine together into Single
                        Timber.d(forecastModels.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d(e);
                    }

                    @Override
                    public void onComplete() {
                        // Now have modelNames for each date at least one model forecast has been generated
                        //Create arrays needed for display
                        createDatesAndModels(appPreferences.getForecastModel());

                    }
                });
        compositeDisposable.add(disposable);
    }

    /**
     * For selected model, get list of dates that forecasts are available
     *
     * @param selectedModelName
     */
    private void createDatesAndModels(final String selectedModelName) {
        List<ModelForecastDate> modelForecastDateList = new ArrayList<>();
        // for the selected model get the dates and model info
        for (int i = 0; i < selectedRegion.getDates().size(); ++i) {
            if (selectedRegion.getForecastModel(i) != null && selectedRegion.getForecastModel(i).getSelectedModel(selectedModelName) != null) {
                ModelForecastDate modelForecastDate = new ModelForecastDate(i, selectedRegion.getName()
                        , selectedRegion.getForecastModel(i).getSelectedModel(selectedModelName)
                        , selectedRegion.getPrintDate(i), selectedRegion.getDate(i));
                modelForecastDateList.add(modelForecastDate);
            }
        }
        setModelNames(selectedRegion);

        // Save new list of dates
        modelForecastDates.setValue(modelForecastDateList);

        // Set selected date to either first in list or if previous model date is also in current model date
        // use that one (so models can be compared by date)
        if (modelForecastDatePosition.getValue() == null || modelForecastDatePosition.getValue() >= modelForecastDateList.size()) {
            modelForecastDatePosition.setValue(0);
        } else {
            int position = modelForecastDatePosition.getValue();
            if (position >= modelForecastDates.getValue().size()) {
                modelForecastDatePosition.setValue(0);
            }
        }

        if (modelForecastDateList.size() > 0
                && modelForecastDatePosition.getValue() != null
                && modelForecastDatePosition.getValue() < modelForecastDateList.size()) {
            setSelectedModelForecastDate(modelForecastDateList.get(modelForecastDatePosition.getValue()));
        }
    }

    public MutableLiveData<Integer> getModelForecastDatePosition() {
        return modelForecastDatePosition;
    }

    public void setModelForecastDatePosition(int position) {
        modelForecastDatePosition.setValue(position);
        setSelectedModelForecastDate(modelForecastDates.getValue().get(position));
    }

    public MutableLiveData<List<ModelForecastDate>> getModelForecastDates() {
        return modelForecastDates;
    }

    /**
     * Call when user clicked date
     *
     * @param newModelForecastDate
     */
    public void setSelectedModelForecastDate(ModelForecastDate newModelForecastDate) {
        if (!newModelForecastDate.equals(getSelectedModelForecastDate())) {
            selectedModelForecastDate = newModelForecastDate;
            setRegionLatLngBounds(selectedModelForecastDate);
            appPreferences.setSelectedModelForecastDate(selectedModelForecastDate);
            loadRaspImages();
        }
    }

    public ModelForecastDate getSelectedModelForecastDate() {
        return selectedModelForecastDate;
    }

    public MutableLiveData<LatLngBounds> getRegionLatLngBounds() {
        return regionLatLngBounds;
    }

    public void setRegionLatLngBounds(ModelForecastDate modelForecastDate) {
        if (modelForecastDate != null) {
            regionLatLngBounds.setValue(new LatLngBounds(modelForecastDate.getModel().getSouthWestLatLng()
                    , modelForecastDate.getModel().getNorthEastLatLng()));
        }
    }

    /**
     * Get the list of forecasts (wstar,...) that can be selected
     *
     * @return
     */
    public LiveData<List<Forecast>> getForecasts() {
        if (forecasts.getValue() == null) {
            loadForecasts();
        }
        return forecasts;
    }

    public void reloadForecasts() {
        // If forecast prior to reorder was 0 and now setting new forecast
        // position to 0, this won't cause reload of forecast bitmaps via
        // observer logic, to put in boolean to force reload
        int lastForecastPosition = getLastForecastPosition();
        if (lastForecastPosition == 0) {
            loadRasp = true;  // TODO got to be better way
        }
        loadForecasts();
    }

    public int getLastForecastPosition() {
        return ((forecastPosition == null || forecastPosition.getValue() == null) ?
                -1 : forecastPosition.getValue());

    }


    /**
     * First try to get forecast list from appPreferences, and if nothing, get default list from appRepository
     */
    private void loadForecasts() {
        Disposable disposable = appPreferences.getOrderedForecastList()
                .flatMap((Function<Forecasts, Observable<Forecasts>>) orderedForecasts -> {
                    if (orderedForecasts != null && orderedForecasts.getForecasts() != null && orderedForecasts.getForecasts().size() > 0) {
                        return Observable.just(orderedForecasts);
                    } else {
                        return appRepository.getForecasts().toObservable();
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderedForecasts -> {
                            forecasts.setValue(orderedForecasts.getForecasts());
                            setDefaultSoaringForecast();
                        },
                        t -> {
                            //TODO email stack trace
                            Timber.e(t);
                        });

        compositeDisposable.add(disposable);
    }

    /**
     *
     */
    public void setDefaultSoaringForecast() {
        getSelectedForecast();
        if (forecasts.getValue() != null && forecasts.getValue().size() > 0) {
            selectedForecast.setValue(forecasts.getValue().get(0));
            forecastPosition.setValue(0);
        }
        if (loadRasp){
            loadRaspImages();
        }
        loadRasp = false;
    }

    public MutableLiveData<Forecast> getSelectedForecast() {
        if (selectedForecast == null) {
            selectedForecast = new MutableLiveData<>();
            selectedForecast.setValue(new Forecast());
        }
        return selectedForecast;

    }

    /**
     * Set user selected forecast
     *
     * @param forecast
     */
    public void setSelected(Forecast forecast) {
        selectedForecast.setValue(forecast);
        loadRaspImages();
    }

    public MutableLiveData<Integer> getForecastPosition() {
        return forecastPosition;
    }

    public void setForecastPosition(int newForecastPosition) {
        forecastPosition.setValue(newForecastPosition);
        setSelected(forecasts.getValue().get(newForecastPosition));
    }

    /**
     * Get sounding locations available
     *
     * @return
     */
    public MutableLiveData<List<Sounding>> getSoundings() {
        if (soundings == null) {
            soundings = new MutableLiveData<>();
        }
        return soundings;
    }

    private boolean loadSoundings() {
        if (selectedRegion == null || selectedRegion.getSoundings() == null) {
            postMessage(getApplication().getString(R.string.no_soundings_available));
            return false;
        }
        List<Sounding> soundingList = selectedRegion.getSoundings();
        soundings.setValue(soundingList);
        if (soundingList == null || soundingList.size() == 0) {
            postMessage(getApplication().getString(R.string.no_soundings_available));
            return false;
        }
        return true;
    }

    public boolean displaySoundings(boolean displaySoundings) {
        if (this.displaySoundings = displaySoundings) {
            return loadSoundings();
        } else {
            soundings.setValue(null);
            return false;
        }
    }

    public void soundingImageCloseClick() {
        forecastSounding = Constants.FORECAST_SOUNDING.FORECAST;
        soundingDisplay.setValue(false);
        loadRaspImages();
    }

    /**
     * Should a skew-t sounding chart be displayed.
     *
     * @return
     */
    public MutableLiveData<Boolean> getSoundingDisplay() {
        return soundingDisplay;
    }

    /**
     * Set the times and number of forecast periods (hours of day) for selected forecast
     * 0900, 1000, 1100, ...
     */
    private void getForecastTimes() {
        forecastTimes = selectedModelForecastDate.getModel().getTimes();
        numberForecastTimes = forecastTimes.size();
    }

    // Stepping thru forecast images
    public void onStepClick(Constants.STEP_ACTION forecastaction) {
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
                if (soaringForecastImageAnimation != null && soaringForecastImageAnimation.isRunning()) {
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
        loopRunning.setValue(soaringForecastImageAnimation == null || soaringForecastImageAnimation.isRunning());
    }

    public LiveData<Boolean> getLoopRunning() {
        return loopRunning;
    }

    public LiveData<Boolean> getWorking() {
        return working;
    }

    @SuppressLint("CheckResult")
    private void loadRaspImages() {
        if (!okToLoadRaspImages()){
            return;
        }
        Timber.d("Loading bitmaps");
        stopImageAnimation();
        soundingDisplay.setValue(false);
        displayForecastImageSet(null);
        imageMap.clear();
        working.setValue(true);
        DisposableObserver disposableObserver = soaringForecastDownloader.getSoaringForecastForTypeAndDay(
                selectedModelForecastDate.getRegionName()
                , selectedModelForecastDate.getDate()
                , selectedModelForecastDate.getModel().getName()
                , selectedForecast.getValue().getForecastName()
                , selectedModelForecastDate.getModel().getTimes())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<SoaringForecastImage>() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onNext(SoaringForecastImage soaringForecastImage) {
                        Timber.d("got SoaringForecastImage");
                        storeImage(soaringForecastImage);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        postCallFailureMessage(e);
                    }

                    @Override
                    public void onComplete() {
                        getForecastTimes();
                        displayRaspImages();
                    }
                });
        compositeDisposable.add(disposableObserver);
    }

    /**
     * Make sure all info available before calling to load rasp images
     * About the only reason(?) this should be really needed is if bad/missing internet service and not
     * all info loaded.
     * @return
     */
    private boolean okToLoadRaspImages(){
        if (selectedModelForecastDate.getRegionName() != null
                && selectedModelForecastDate.getDate() != null
                && selectedModelForecastDate.getModel() != null
                && selectedModelForecastDate.getModel().getName() != null
                && selectedModelForecastDate.getModel().getTimes() != null ) {
                return true;
            } else {
                postMessage(getApplication().getString(R.string.missing_data_check_internet_cell_service));
                return false;
        }
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
        setLoopRunning();
    }

    public void stopImageAnimation() {
        Timber.d("Stopping Animation");
        if (soaringForecastImageAnimation != null) {
            soaringForecastImageAnimation.cancel();
        }
        setLoopRunning();
        Timber.e("soaringForecastImageAnimation is null so no animation to stop");
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
            } else {
                Timber.d("imageSet side and or bodyImage is null");
            }
        } else {
            selectedSoaringForecastImageSet.setValue(null);
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

    public MutableLiveData<SoaringForecastImageSet> getSoundingForecastImageSet() {
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

    //------- Soundings -------------------

    private void loadForecastSoundings(Sounding sounding) {
        stopImageAnimation();
        working.setValue(true);
        DisposableObserver disposableObserver = soaringForecastDownloader.getSoaringSoundingForTypeAndDay(
                selectedModelForecastDate.getRegionName()
                , selectedModelForecastDate.getDate()
                , selectedModelForecastDate.getModel().getName()
                , sounding.getPosition() + ""
                , selectedModelForecastDate.getModel().getTimes())
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
                        postCallFailureMessage(e);
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

    public void checkIfToDisplayTask() {
        long currentTaskId = appPreferences.getSelectedTaskId();
        if (lastTaskId != currentTaskId) {
            lastTaskId = currentTaskId;
            getTask(lastTaskId);
        }
    }

    private void getTask(long taskId) {
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
        if (taskTurnpoints.getValue() == null || taskTurnpoints.getValue().size() == 0) {
            long taskId = appPreferences.getSelectedTaskId();
            if (taskId != -1L) {
                getTask(taskId);
            }
        }
        return taskTurnpoints;
    }

    public Sounding getSelectedSounding() {
        return selectedSounding;
    }

    public void setSelectedSounding(Sounding selectedSounding) {
        this.selectedSounding = selectedSounding;
        loadForecastSoundings(selectedSounding);
    }

    //------- Opacity of forecast overly -----------------
    public void setForecastOverlayOpacity(int opacity){
        appPreferences.setForecastOverlayOpacity(opacity);
    }

    public int getForecastOverlyOpacity() {
        return appPreferences.getForecastOverlayOpacity();
    }

    public void setTaskId(int taskId) {
        appPreferences.setSelectedTaskId(taskId);
    }

    public void postCallFailureMessage(Throwable t){
        EventBus.getDefault().post(new CallFailure(t.toString()));
    }

    public void postMessage(String msg){
        EventBus.getDefault().post(new SnackbarMessage(msg));
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        stopImageAnimation();
        super.onCleared();
    }

}
