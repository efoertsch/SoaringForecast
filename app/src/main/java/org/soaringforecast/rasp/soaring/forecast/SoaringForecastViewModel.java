package org.soaringforecast.rasp.soaring.forecast;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Application;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.Constants;
import org.soaringforecast.rasp.common.messages.CallFailure;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.TaskTurnpoint;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.soaring.json.Forecast;
import org.soaringforecast.rasp.soaring.json.ForecastModels;
import org.soaringforecast.rasp.soaring.json.Forecasts;
import org.soaringforecast.rasp.soaring.json.Model;
import org.soaringforecast.rasp.soaring.json.ModelForecastDate;
import org.soaringforecast.rasp.soaring.json.Region;
import org.soaringforecast.rasp.soaring.json.Regions;
import org.soaringforecast.rasp.soaring.json.Sounding;
import org.soaringforecast.rasp.utils.ImageAnimator;
import org.soaringforecast.rasp.utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

public class SoaringForecastViewModel extends AndroidViewModel {

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
    private MutableLiveData<LatLngBounds> regionLatLngBounds = new MutableLiveData<>();
    private HashMap<String, SoaringForecastImageSet> imageMap = new HashMap<>();
    private MutableLiveData<SoaringForecastImageSet> selectedSoaringForecastImageSet = new MutableLiveData<>();
    private MutableLiveData<SoaringForecastImageSet> selectedSoundingForecastImageSet = new MutableLiveData<>();
    private MutableLiveData<List<Sounding>> soundings;
    private MutableLiveData<List<TaskTurnpoint>> taskTurnpoints = new MutableLiveData<>();
    private MutableLiveData<Boolean> loopRunning = new MutableLiveData<>();
    private MutableLiveData<JSONObject> suaJSONObject = new MutableLiveData<>();
    private MutableLiveData<List<Turnpoint>> regionTurnpoints = new MutableLiveData<>();
    private MutableLiveData<LatLngForecast> pointForecastText;
    // Used to signal changes to UI
    private MutableLiveData<Boolean> working = new MutableLiveData<>();
    private MutableLiveData<Boolean> soundingDisplay = new MutableLiveData<>();

    private Regions regions;
    private Region selectedRegion;
    private Sounding selectedSounding;


    private Constants.FORECAST_SOUNDING forecastSounding = Constants.FORECAST_SOUNDING.FORECAST;
    private boolean loadRasp;
    private StringUtils stringUtils;
    private HashMap<String, String> pointForecastConversion;
    private boolean displaySUA;

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


    SoaringForecastViewModel setStringUtils(StringUtils stringUtils) {
        this.stringUtils = stringUtils;
        return this;
    }

    void checkForChanges() {
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

    private void setModelNames(Region region) {
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
        Disposable disposable = appRepository.getRegionForecastDates()
                .subscribeOn(Schedulers.io())
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
            if (appPreferences.getDisplayForecastSoundings()) {
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
                            return appRepository.getForecastModels(region.getName(), regionForecastDate).toObservable()
                                    .doOnNext(region::addForecastModels);
                        })
                .subscribeOn(Schedulers.io())
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

    void setModelForecastDatePosition(int position) {
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
    private void setSelectedModelForecastDate(ModelForecastDate newModelForecastDate) {
        if (!newModelForecastDate.equals(getSelectedModelForecastDate())) {
            selectedModelForecastDate = newModelForecastDate;
            setRegionLatLngBounds(selectedModelForecastDate);
            appPreferences.setSelectedModelForecastDate(selectedModelForecastDate);
            checkToDisplaySuaForRegion(selectedRegion.getName());
            loadRaspImages();
        }
    }

    private ModelForecastDate getSelectedModelForecastDate() {
        return selectedModelForecastDate;
    }

    MutableLiveData<LatLngBounds> getRegionLatLngBounds() {
        return regionLatLngBounds;
    }

    private void setRegionLatLngBounds(ModelForecastDate modelForecastDate) {
        if (modelForecastDate != null) {
            regionLatLngBounds.setValue(new LatLngBounds(modelForecastDate.getModel().getSouthWestLatLng()
                    , modelForecastDate.getModel().getNorthEastLatLng()));
            if (appPreferences.getDisplayTurnpoints()) {
                findTurnpointsInSelectedRegion();
            }
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

    private int getLastForecastPosition() {
        return ((forecastPosition == null || forecastPosition.getValue() == null) ?
                -1 : forecastPosition.getValue());

    }


    /**
     * First try to get forecast list from appPreferences, and if nothing, get default list from appRepository
     */
    private void loadForecasts() {
        //TODO email stack trace
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
                        Timber::e);

        compositeDisposable.add(disposable);
    }

    /**
     *
     */
    private void setDefaultSoaringForecast() {
        getSelectedForecast();
        if (forecasts.getValue() != null && forecasts.getValue().size() > 0) {
            selectedForecast.setValue(forecasts.getValue().get(0));
            forecastPosition.setValue(0);
        }
        if (loadRasp) {
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
    private void setSelectedForecast(Forecast forecast) {
        selectedForecast.setValue(forecast);
        loadRaspImages();
    }

    public MutableLiveData<Integer> getForecastPosition() {
        return forecastPosition;
    }

    void setForecastPosition(int newForecastPosition) {
        forecastPosition.setValue(newForecastPosition);
        setSelectedForecast(forecasts.getValue().get(newForecastPosition));
    }

    /**
     * Get sounding locations available
     *
     * @return
     */
    MutableLiveData<List<Sounding>> getSoundings() {
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
        if (displaySoundings) {
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
        if (!okToLoadRaspImages()) {
            return;
        }
        Timber.d("Loading bitmaps");
        stopImageAnimation();
        soundingDisplay.setValue(false);
        displayForecastImageSet(null);
        imageMap.clear();
        working.setValue(true);
        DisposableObserver disposableObserver = appRepository.getSoaringForecastForTypeAndDay(
                selectedModelForecastDate.getRegionName()
                , selectedModelForecastDate.getDate()
                , selectedModelForecastDate.getModel().getName()
                , selectedForecast.getValue().getForecastName()
                , selectedModelForecastDate.getModel().getTimes())
                .subscribeOn(Schedulers.io())
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
     *
     * @return
     */
    private boolean okToLoadRaspImages() {
        if (selectedModelForecastDate != null
                && selectedModelForecastDate.getRegionName() != null
                && selectedModelForecastDate.getDate() != null
                && selectedModelForecastDate.getModel() != null
                && selectedModelForecastDate.getModel().getName() != null
                && selectedModelForecastDate.getModel().getTimes() != null) {
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
        //Timber.d("Creating animation on %1$s ( %2$d )", thread.getName(), thread.getId());
        soaringForecastImageAnimation = ImageAnimator.getInitAnimator(0, numberForecastTimes
                , 15000, ValueAnimator.INFINITE);
        soaringForecastImageAnimation.addUpdateListener(updatedAnimation -> {
            //Timber.d("RunnableJob is being run by %1$s ( %2$d )", thread.getName(), thread.getId());
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
        Timber.w("soaringForecastImageAnimation is null so no animation to stop");
    }

    private void selectForecastImage(int index) {
        SoaringForecastImageSet imageSet;
        if (imageMap != null
                && forecastTimes != null
                && forecastTimes.size() > index) {
            imageSet = imageMap.get(stringUtils.stripOldIfNeeded(forecastTimes.get(index)));
        } else {
            imageSet = null;
        }
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
                selectedSoaringForecastImageSet.setValue(null);
                postMessage(getApplication().getApplicationContext().getString(R.string.oops_body_or_side_image_null));
            }
        } else {
            selectedSoaringForecastImageSet.setValue(null);
        }
    }

    MutableLiveData<SoaringForecastImageSet> getSelectedSoaringForecastImageSet() {
        return selectedSoaringForecastImageSet;
    }

    private void displaySoundingImageSet(SoaringForecastImageSet imageSet) {
        if (imageSet != null) {
            if (imageSet.getBodyImage() != null) {
                selectedSoundingForecastImageSet.setValue(imageSet);
            }
        }
    }

    MutableLiveData<SoaringForecastImageSet> getSoundingForecastImageSet() {
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
        DisposableObserver disposableObserver = appRepository.getSoaringSoundingForTypeAndDay(
                selectedModelForecastDate.getRegionName()
                , selectedModelForecastDate.getDate()
                , selectedModelForecastDate.getModel().getName()
                , sounding.getPosition() + ""
                , selectedModelForecastDate.getModel().getTimes())
                .subscribeOn(Schedulers.io())
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
        forecastSounding = Constants.FORECAST_SOUNDING.SOUNDING;
        startImageAnimation();
        soundingDisplay.setValue(true);
    }

    void setSelectedSounding(Sounding selectedSounding) {
        this.selectedSounding = selectedSounding;
        loadForecastSoundings(selectedSounding);
    }

    // ------- Task display ---------------------

    long getTaskId(){
        return appPreferences.getSelectedTaskId();
    }

    void checkIfToDisplayTask() {
        long currentTaskId = appPreferences.getSelectedTaskId();
        if (currentTaskId != -1) {
            getTask(currentTaskId);
        }
    }

    private void getTask(long taskId) {
        Disposable disposable = appRepository.getTaskTurnpoints(taskId)
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

    public void setTaskId(int taskId) {
        appPreferences.setSelectedTaskId(taskId);
    }

    //------- Opacity of forecast overly -----------------
    void setForecastOverlayOpacity(int opacity) {
        appPreferences.setForecastOverlayOpacity(opacity);
    }

    int getForecastOverlyOpacity() {
        return appPreferences.getForecastOverlayOpacity();
    }

    // ------------------ SUA (Special Use Airspace of course!) -----------------------
    MutableLiveData<JSONObject> getSuaJSONObject() {
        return suaJSONObject;
    }

    private void setSuaJSONObject(JSONObject suaJSONObject) {
        this.suaJSONObject.setValue(suaJSONObject);
    }

    private void checkToDisplaySuaForRegion(String regionName) {
        if (shouldDisplaySUA()) {
            Observable<JSONObject> suaJSONObjectObservable = appRepository.displaySuaForRegion(regionName);
            Disposable disposable = suaJSONObjectObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(suaJSONObject -> {
                        setSuaJSONObject(suaJSONObject);
                        }
                        , t -> {
                            //TODO email stack trace
                            Timber.e(t);
                        });
            compositeDisposable.add(disposable);
        }

    }

    private boolean shouldDisplaySUA() {
        return appPreferences.getDisplaySua();
    }

    void displaySua(boolean displaySUA) {
        appPreferences.setDisplaySua(displaySUA);
        if (displaySUA) {
            checkToDisplaySuaForRegion(selectedRegion.getName());
        }
        setSuaJSONObject(null);
    }

    // ---- Region turnpoints ---------------
    void displayTurnpoints(boolean checked) {
        if (checked) {
            findTurnpointsInSelectedRegion();
        } else {
            getRegionTurnpoints();
        }

    }

    MutableLiveData<List<Turnpoint>> getRegionTurnpoints() {
        regionTurnpoints.setValue(new ArrayList<>());
        return regionTurnpoints;
    }

    private void findTurnpointsInSelectedRegion() {
        if (regionLatLngBounds != null && regionLatLngBounds.getValue() != null) {
            Disposable disposable = appRepository.getTurnpointsInRegion(regionLatLngBounds.getValue().southwest, regionLatLngBounds.getValue().northeast)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(turnpointList -> {
                                if (turnpointList != null && turnpointList.size() > 0) {
                                    regionTurnpoints.setValue(turnpointList);
                                } else {
                                    EventBus.getDefault().post(new SnackbarMessage(getApplication().getString(R.string.no_turnpoints_to_display_none_loaded)));
                                }
                            },
                            t -> {
                                //TODO email stack trace
                                Timber.e(t);
                            });
            compositeDisposable.add(disposable);
        }
    }

    // --------------- Messages for the upper management ----------------------

    private void postCallFailureMessage(Throwable t) {
        EventBus.getDefault().post(new CallFailure(t.toString()));
    }

    private void postMessage(String msg) {
        EventBus.getDefault().post(new SnackbarMessage(msg));
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        stopImageAnimation();
        super.onCleared();
    }


    MutableLiveData<LatLngForecast> getPointForecast() {
        if (pointForecastText == null) {
            pointForecastText = new MutableLiveData<>();
        }
        return pointForecastText;
    }

    /**
     * @param latLng Get point forecast based on current forecast type at latlng
     */

    void displayLatLngForecast(final LatLng latLng) {
        String latLngForecastParms;
        stopImageAnimation();
//        if (latLngForecastParms == null) {
//            latLngForecastParms = stringUtils.getHashMapFromStringRes(getApplication().getApplicationContext()
//                    , R.string.point_forecast_parm_conversion);
//        }
        if (selectedForecast.getValue() != null && selectedForecast.getValue().getForecastName() != null
                && selectedSoaringForecastImageSet != null) {
            //latLngForecastParms = pointForecastConversion.get(selectedForecast.getValue().getForecastName());
            switch (selectedForecast.getValue().getForecastCategory()) {
                case "wave":
                    latLngForecastParms = getApplication().getString(R.string.location_forecast_wave_parms);
                    break;
                default:
                    latLngForecastParms = getApplication().getString(R.string.location_forecast_standard_parms);
            }

            appRepository.getLatLngForecast(selectedRegion.getName()
                    , selectedModelForecastDate.getDate()
                    , selectedModelName.toLowerCase()
                    , selectedSoaringForecastImageSet.getValue().getLocalTime()
                    , String.valueOf(latLng.latitude)
                    , String.valueOf(latLng.longitude)
                    , latLngForecastParms)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<ResponseBody>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onSuccess(Response<ResponseBody> responseBodyResponse) {
                            if (responseBodyResponse.isSuccessful()) {
                                try {
                                    pointForecastText.setValue(new LatLngForecast(latLng,
                                            responseBodyResponse.body().string()));
                                } catch (IOException ioe) {
                                    // TODO report error
                                }
                            } else {
                                Timber.d("Error on point forecast: %s", responseBodyResponse.toString());
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            // TODO report error
                        }

                    });
        }

    }

}
