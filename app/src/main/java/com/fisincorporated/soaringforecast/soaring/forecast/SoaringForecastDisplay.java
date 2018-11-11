package com.fisincorporated.soaringforecast.soaring.forecast;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.databinding.BaseObservable;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.app.ViewModelLifeCycle;
import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.common.Constants.FORECAST_ACTION;
import com.fisincorporated.soaringforecast.databinding.SoaringForecastImageBinding;
import com.fisincorporated.soaringforecast.messages.DataLoadingEvent;
import com.fisincorporated.soaringforecast.messages.ReadyToSelectSoaringForecastEvent;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.repository.TaskTurnpoint;
import com.fisincorporated.soaringforecast.soaring.forecast.adapters.ForecastDateRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.soaring.forecast.adapters.ForecastModelRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.soaring.forecast.adapters.SoaringForecastRecyclerViewAdapter;
import com.fisincorporated.soaringforecast.soaring.json.Forecast;
import com.fisincorporated.soaringforecast.soaring.json.Forecasts;
import com.fisincorporated.soaringforecast.soaring.json.GpsLocationAndTimes;
import com.fisincorporated.soaringforecast.soaring.json.ModelForecastDate;
import com.fisincorporated.soaringforecast.soaring.json.ModelLocationAndTimes;
import com.fisincorporated.soaringforecast.soaring.json.RegionForecastDate;
import com.fisincorporated.soaringforecast.soaring.json.RegionForecastDates;
import com.fisincorporated.soaringforecast.soaring.json.SoundingLocation;
import com.fisincorporated.soaringforecast.utils.ViewUtilities;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.cache2k.Cache;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

//@InverseBindingMethods({
//        @InverseBindingMethod(type = Spinner.class, attribute = "android:selectedItemPosition"),})
// TODO refactor - convert to correct ViewModel and simplify
public class SoaringForecastDisplay extends BaseObservable implements ViewModelLifeCycle
        , OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = SoaringForecastDisplay.class.getSimpleName();

    private SoaringForecastImageBinding viewDataBinding;
    private ValueAnimator soaringForecastImageAnimation;
    private RegionForecastDates regionForecastDates = new RegionForecastDates();
    private SoaringForecastModel selectedSoaringForecastModel;
    private ModelForecastDate selectedModelForecastDate = null;
    private View bindingView;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private HashMap<String, SoaringForecastImageSet> imageMap = new HashMap<>();
    private List<String> forecastTimes;
    private int numberForecastTimes;
    private int lastImageIndex = -1;
    private Fragment parentFragment;
    private GoogleMap googleMap;
    private LatLngBounds mapLatLngBounds;
    private GroundOverlay forecastOverlay;
    //private List<ModelForecastDate> modelForecastDates = new ArrayList<>();
    //private RecyclerViewAdapterModelForecastDate modelForecastDateRecyclerViewAdapter;
    // using generic adapter
    private ForecastDateRecyclerViewAdapter forecastDateRecyclerViewAdapter;
    private ProgressBar mapProgressBar;
    private Forecast selectedForecast;
    private int forecastOverlayOpacity;
    private List<Marker> soundingMarkers = new ArrayList<>();
    private SoaringForecastImageSet soaringForecastImageSet;
    private String forecastTime;
    private List<TaskTurnpoint> taskTurnpoints;
    private List<Marker> taskTurnpointMarkers = new ArrayList<>();
    private List<Polyline> taskTurnpointLines = new ArrayList<>();

    @Inject
    public SoaringForecastDownloader soaringForecastDownloader;

    @Inject
    public Cache<String, SoaringForecastImage> soaringForecastImageCache;

    @Inject
    public List<SoaringForecastModel> soaringForecastModels;

    @Inject
    public AppPreferences appPreferences;

    @Inject
    public AppRepository appRepository;

    private Forecasts forecasts;

    private List<SoundingLocation> soundingLocations;

    private Constants.FORECAST_SOUNDING forecastSounding;

    /**
     * Use to center task route in googleMap frame
     */
    private LatLng southwest = null;
    private double swLat = 0;
    private double swLong = 0;
    private double neLat = 0;
    private double neLong = 0;
    private boolean drawingTask;

    @Inject
    SoaringForecastDisplay(AppRepository appRepository) {
        //forecasts = appRepository.getForecasts();
    }

    public SoaringForecastDisplay setView(Fragment fragment, View view) {
        parentFragment = fragment;
        bindingView = view.findViewById(R.id.soaring_forecast_constraint_layout);
        setDefaultSoaringForecast();
        bindViewModel();
        return this;
    }

    private void setDefaultSoaringForecast() {
        // TODO cheat here to get wstar - do something more flexible
        selectedForecast = forecasts.getForecasts().get(1);
    }

    private void bindViewModel() {
        viewDataBinding = DataBindingUtil.bind(bindingView);
        if (viewDataBinding != null) {
            //viewDataBinding.setForecastDisplay(this);
            selectedSoaringForecastModel = appPreferences.getSoaringForecastModel();
            setupSoaringForecastModelsRecyclerView(soaringForecastModels);
            setupSoaringConditionRecyclerView(forecasts.getForecasts());
            mapProgressBar = viewDataBinding.soaringForecastMapProgressBar;
            setProgressBarVisibility(true);
            setInitialOverlayOpacity();
        }
    }

    private void setProgressBarVisibility(boolean visible) {
        if (mapProgressBar != null) {
            mapProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void setInitialOverlayOpacity() {
        forecastOverlayOpacity = appPreferences.getForecastOverlayOpacity();
        viewDataBinding.soaringForecastSeekbarOpacity.setProgress(forecastOverlayOpacity);
    }


    /**
     * Set up recycler view with forecast models - gfs, rap, ...
     *
     * @param soaringForecastModels
     */
    private void setupSoaringForecastModelsRecyclerView(List<SoaringForecastModel> soaringForecastModels) {
        ForecastModelRecyclerViewAdapter recyclerViewAdapter = new ForecastModelRecyclerViewAdapter(soaringForecastModels);
        setUpHorizontalRecyclerView(viewDataBinding.soaringForecastModelRecyclerView, recyclerViewAdapter);
        //TODO create preference for model to use for first display
        recyclerViewAdapter.setSelectedItem(soaringForecastModels.get(0));
    }


    private void setupModelForecastDateRecyclerView(List<ModelForecastDate> modelForecastDateList) {
        forecastDateRecyclerViewAdapter = new ForecastDateRecyclerViewAdapter(modelForecastDateList);
        setUpHorizontalRecyclerView(viewDataBinding.regionForecastDateRecyclerView, forecastDateRecyclerViewAdapter);
    }

    private void setupSoaringConditionRecyclerView(List<Forecast> forecasts) {
        SoaringForecastRecyclerViewAdapter recyclerViewAdapter = new SoaringForecastRecyclerViewAdapter(forecasts);
        setUpHorizontalRecyclerView(viewDataBinding.soaringForecastRecyclerView, recyclerViewAdapter);
        // TODO do better way to set selected
        recyclerViewAdapter.setSelectedItem(forecasts.get(1));
    }

    private void setUpHorizontalRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter recyclerViewAdapter) {
        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(viewDataBinding.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(recyclerViewAdapter);
    }


    @Override
    public void onResume() {
        EventBus.getDefault().register(this);
        //soaringForecastDownloader.loadForecastsForDay(appPreferences.getSoaringForecastRegion());
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        soaringForecastDownloader.clearOutstandingLoads();
        compositeDisposable.clear();
        stopImageAnimation();
    }

    @Override
    public void onDestroy() {
        soaringForecastDownloader.shutdown();
        compositeDisposable.dispose();
        stopImageAnimation();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataLoadingEvent event) {
        stopImageAnimation();
    }

    /**
     * Should be called after response from current.json call
     * You get a list of all dates for which some forecast model will be provided.
     *
     * @param regionForecastDates
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RegionForecastDates regionForecastDates) {
        storeRegionForecastDates(regionForecastDates);
    }

    /**
     * Should be called once all models for each date in current.json response are received and processed
     *
     * @param readyToSelectSoaringForecastEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ReadyToSelectSoaringForecastEvent readyToSelectSoaringForecastEvent) {
        // At this point for each date RegionForecastDates contains for each date the type of forecast available
        // So pull out the dates available for the forecast type selected.
        List<ModelForecastDate> modelForecastDates = createForecastDateListForSelectedModel();
        setupModelForecastDateRecyclerView(modelForecastDates);
        //modelForecastDateRecyclerViewAdapter.setSelectedModelForecastDate(0);
        // generic adapter
        forecastDateRecyclerViewAdapter.setSelectedItem(0);
        // Get whatever current date is to start
        if (modelForecastDates.size() > 0) {
            setProgressBarVisibility(false);
            selectedModelForecastDate = modelForecastDates.get(0);
            // TODO - load bitmaps for hcrit for first forecast type (e.g. gfs)
            Timber.d("Ready to load bitmaps");
            startLoadingSoaringForecastImages();
        } else {
            Snackbar.make(bindingView, R.string.model_forecast_for_date_not_available, Snackbar.LENGTH_LONG);
        }
    }

    private List<ModelForecastDate> createForecastDateListForSelectedModel() {
        ModelLocationAndTimes modelLocationAndTimes;
        List<ModelForecastDate> modelForecastDates = new ArrayList<>();
        String model = selectedSoaringForecastModel.getName();
        for (RegionForecastDate regionForecastDate : regionForecastDates.getRegionForecastDateList()) {
            modelLocationAndTimes = regionForecastDate.getModelLocationAndTimes();
            if (modelLocationAndTimes != null && modelLocationAndTimes.getGpsLocationAndTimesForModel(model) != null) {
                ModelForecastDate modelForecastDate = new ModelForecastDate(model);
                modelForecastDate.setBaseDate(regionForecastDate.getIndex(), regionForecastDate.getFormattedDate(), regionForecastDate.getYyyymmddDate());
                modelForecastDate.setGpsLocationAndTimes(modelLocationAndTimes.getGpsLocationAndTimesForModel(model));
                modelForecastDates.add(modelForecastDate);
            }
        }
        return modelForecastDates;
    }

    private void startLoadingSoaringForecastImages() {
        loadRaspImages();
        setMapBounds();

    }

    private void storeRegionForecastDates(RegionForecastDates downloadedRegionForecastDates) {
        regionForecastDates = downloadedRegionForecastDates;
        regionForecastDates.parseForecastDates();
        soaringForecastDownloader.getTypeLocationAndTimes(appPreferences.getSoaringForecastRegion(), downloadedRegionForecastDates);
    }

    @SuppressLint("CheckResult")
    private void loadRaspImages() {
        stopImageAnimation();
        compositeDisposable.clear();
        imageMap.clear();
        setProgressBarVisibility(true);
        DisposableObserver disposableObserver = soaringForecastDownloader.getSoaringForecastForTypeAndDay(
                bindingView.getContext().getString(R.string.new_england_region)
                , selectedModelForecastDate.getYyyymmddDate(), selectedSoaringForecastModel.getName()
                , selectedForecast.getForecastName()
                , selectedModelForecastDate.getGpsLocationAndTimes().getTimes())
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
                        displayMap();
                    }
                });
        compositeDisposable.add(disposableObserver);
    }

    private void storeImage(SoaringForecastImage soaringForecastImage) {
        SoaringForecastImageSet imageSet = imageMap.get(soaringForecastImage.getForecastTime());
        if (imageSet == null) {
            imageSet = new SoaringForecastImageSet();
        }
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
        ViewUtilities.displayErrorDialog(viewDataBinding.getRoot(), bindingView.getContext().getString(R.string.oops), t.toString());
    }


    /**
     * Selected forecast type gfs, nam, ...
     *
     * @param soaringForecastModel
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SoaringForecastModel soaringForecastModel) {
        if (!soaringForecastModel.equals(selectedSoaringForecastModel)) {
            selectedSoaringForecastModel = soaringForecastModel;
            appPreferences.setSoaringForecastModel(selectedSoaringForecastModel);
            List<ModelForecastDate> modelForecastDates = createForecastDateListForSelectedModel();
//            if (modelForecastDateRecyclerViewAdapter != null) {
//                modelForecastDateRecyclerViewAdapter.updateModelForecastDateList(modelForecastDates);
//            }
            if (forecastDateRecyclerViewAdapter != null) {
                forecastDateRecyclerViewAdapter.updateModelForecastDateList(modelForecastDates);
            }
            loadRaspImages();
        }
    }

    /**
     * Selected model date
     *
     * @param modelForecastDate
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ModelForecastDate modelForecastDate) {
        selectedModelForecastDate = modelForecastDate;
        loadRaspImages();
    }

    @Subscribe
    public void onMessageEvent(Forecast forecast) {
        selectedForecast = forecast;
        loadRaspImages();
    }

    private void stopImageAnimation() {
        Timber.d("Stopping Animation");
        if (soaringForecastImageAnimation != null) {
            soaringForecastImageAnimation.cancel();
            return;
        }
        Timber.e("soaringForecastImageAnimation is null so no animation to stop");
    }

    private void startImageAnimation() {
        stopImageAnimation();
        // need to 'overshoot' the animation to be able to get the last image value
        Thread thread = Thread.currentThread();
        Timber.d("Creating animation on %1$s ( %2$d )", thread.getName(), thread.getId());
        soaringForecastImageAnimation = ValueAnimator.ofInt(0, numberForecastTimes);
        soaringForecastImageAnimation.setInterpolator(new LinearInterpolator());
        soaringForecastImageAnimation.setDuration(15000);
        soaringForecastImageAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
//                Timber.d("RunnableJob is being run by %1$s ( %2$d )",  thread.getName(), thread.getId() );
                int index = (int) updatedAnimation.getAnimatedValue();
                //Timber.d("animation index: %d  ", index);
                if (index > numberForecastTimes - 1) {
                    index = numberForecastTimes - 1;
                }
                // Don't force redraw if still on same image as last time
                if (lastImageIndex != index) {
                    // Timber.d("updating image to index: %1$d", index);
                    SoaringForecastDisplay.this.displayForecastImage(index);
                }
                lastImageIndex = index;
            }
        });
        soaringForecastImageAnimation.setRepeatCount(ValueAnimator.INFINITE);
        soaringForecastImageAnimation.start();
    }

    private void getForecastTimes() {
        forecastTimes = selectedModelForecastDate.getGpsLocationAndTimes().getTimes();
        numberForecastTimes = forecastTimes.size();
    }

    public void displayForecastImage(int index) {
        soaringForecastImageSet = imageMap.get(forecastTimes.get(index));
        forecastTime = forecastTimes.get(index);
        switch (forecastSounding) {
            case FORECAST:
                displayForecastImageSet();
                break;
            case SOUNDING:
                displaySoundingImageSet();
                break;

        }
        displayForecastImageSet();
    }

    private void displayForecastImageSet() {
        viewDataBinding.soaringForecastImageLocalTime.setText(forecastTime);
        if (soaringForecastImageSet != null) {
            if (soaringForecastImageSet.getSideImage() != null
                    && soaringForecastImageSet.getBodyImage() != null) {
                setGroundOverlay(soaringForecastImageSet.getBodyImage().getBitmap());
                viewDataBinding.soaringForecastScaleImage.setImageBitmap(soaringForecastImageSet.getSideImage().getBitmap());
            }
        }
    }

    // Stepping thru forecast images
    public void onStepClick(FORECAST_ACTION forecastaction) {
        switch (forecastaction) {
            case BACKWARD:
                stopImageAnimation();
                displayForecastImage(stepImageBy(-1));
                break;
            case FORWARD:
                stopImageAnimation();
                displayForecastImage(stepImageBy(1));
                break;
            case LOOP:
                if (soaringForecastImageAnimation.isRunning()) {
                    viewDataBinding.soaringForecastLoopButton.setText(R.string.loop);
                    stopImageAnimation();
                } else {
                    viewDataBinding.soaringForecastLoopButton.setText(R.string.pause);
                    startImageAnimation();
                }
                break;
        }
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

    //------- map stuff ---------

    private void displayMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) parentFragment.getChildFragmentManager().findFragmentById(R.id.soaring_forecast_map);
        supportMapFragment.getMapAsync(this);
    }

    private void setMapBounds() {
        GpsLocationAndTimes gpsLocationAndTimes = selectedModelForecastDate.getGpsLocationAndTimes();
        mapLatLngBounds = new LatLngBounds(
                gpsLocationAndTimes.getSouthWestLatLng(), gpsLocationAndTimes.getNorthEastLatLng());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        setupMap();// do your map stuff here
        setProgressBarVisibility(false);
        displaySoundingMarkers(appPreferences.getDisplayForecastSoundings());
        forecastSounding = Constants.FORECAST_SOUNDING.FORECAST;
        startImageAnimation();
    }


    private void setupMap() {
        googleMap.setLatLngBoundsForCameraTarget(mapLatLngBounds);
        if (!drawingTask) {
            // if drawing task use the task latlng bounds for map positioning
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapLatLngBounds, 0));

        }
    }

    private void setGroundOverlay(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }

        if (forecastOverlay == null) {
            GroundOverlayOptions forecastOverlayOptions = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .positionFromBounds(mapLatLngBounds);
            forecastOverlayOptions.transparency(1.0f - forecastOverlayOpacity / 100.0f);
            forecastOverlay = googleMap.addGroundOverlay(forecastOverlayOptions);
        } else {
            forecastOverlay.setTransparency(1.0f - forecastOverlayOpacity / 100.0f);
            forecastOverlay.setImage(BitmapDescriptorFactory.fromBitmap(bitmap));
        }

    }

    public void displayOpacitySlider() {
        viewDataBinding.soaringForecastSeekbarLayout.setVisibility(viewDataBinding.soaringForecastSeekbarLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    //@BindingAdapter(value={"android:onProgressChanged"})
    public void onOpacityValueChanged(SeekBar seekBar, int newOpacity, boolean fromUser) {
        forecastOverlayOpacity = newOpacity;
        appPreferences.setForecastOverlayOpacity(newOpacity);
        stopImageAnimation();
        displayForecastImageSet();
    }

    public void toggleSoundingPoints() {
        boolean displaySoundings = !appPreferences.getDisplayForecastSoundings();
        appPreferences.setDisplayForecastSoundings(displaySoundings);
        displaySoundingMarkers(displaySoundings);
    }

    private void createSoundingMarkers() {
        if (soundingLocations == null || soundingLocations.size() == 0) {
            soundingLocations = appRepository.getLocationSoundings();
        }
        if (soundingMarkers == null || soundingMarkers.size() == 0) {
            LatLng latLng;
            Marker marker;
            for (SoundingLocation soundingLocation : soundingLocations) {
                latLng = new LatLng(soundingLocation.getLatitude(), soundingLocation.getLongitude());
                marker = googleMap.addMarker(new MarkerOptions().position(latLng)
                        .title(soundingLocation.getLocation()));
                soundingMarkers.add(marker);
                marker.setTag(soundingLocation);
                googleMap.setOnMarkerClickListener(this);

            }
        }
    }

    private void displaySoundingMarkers(boolean display) {
        if (display) {
            if (soundingMarkers == null || soundingMarkers.size() == 0) {
                createSoundingMarkers();
            }
        }
        if (soundingMarkers != null && soundingMarkers.size() > 0) {
            for (Marker marker : soundingMarkers) {
                marker.setVisible(display);

            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getTag() == null) {
            // a click on task marker causes onMarkerClick to fire, even though task marker
            // onClickListener not assigned. Go figure.
            return false;
        }

        forecastSounding = Constants.FORECAST_SOUNDING.SOUNDING;
        viewDataBinding.soaringForecastSoundingLayout.setVisibility(View.VISIBLE);
        loadForecastSoundings((SoundingLocation) marker.getTag());
        return true;
    }

    private void loadForecastSoundings(SoundingLocation soundingLocation) {
        stopImageAnimation();
        compositeDisposable.clear();
        //imageMap.clear();
        setProgressBarVisibility(true);
        DisposableObserver disposableObserver = soaringForecastDownloader.getSoaringSoundingForTypeAndDay(
                bindingView.getContext().getString(R.string.new_england_region)
                , selectedModelForecastDate.getYyyymmddDate(), selectedSoaringForecastModel.getName()
                , soundingLocation.getPosition() + ""
                , selectedModelForecastDate.getGpsLocationAndTimes().getTimes())
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
                        startImageAnimation();
                    }
                });
        compositeDisposable.add(disposableObserver);
    }

    public String getForecastTime() {
        return forecastTime;
    }


    public void displaySoundingImageSet() {
        viewDataBinding.soaringForecastSoundingLayout.bringToFront();
        viewDataBinding.soaringForecastImageLocalTime.setText(forecastTime);
        if (soaringForecastImageSet != null) {
            if (soaringForecastImageSet.getBodyImage() != null) {
                viewDataBinding.soaringForecastSoundingImage.setImageBitmap(soaringForecastImageSet.getBodyImage().getBitmap());
            }
        }
    }


    public void soundingImageCloseClick() {
        forecastSounding = Constants.FORECAST_SOUNDING.FORECAST;
        viewDataBinding.soaringForecastSoundingLayout.setVisibility(View.GONE);
        loadRaspImages();
    }

    @SuppressLint("CheckResult")
    public void displayTask(long taskId) {
        removeTaskTurnpoints();
        drawingTask = true;
        Disposable disposable = appRepository.getTaskTurnpionts(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(taskTurnpoints -> {
                            plotTurnpointsOnForecast(taskTurnpoints);

                        },
                        t -> {
                            Timber.e(t);
                        });
        compositeDisposable.add(disposable);
    }

    private void plotTurnpointsOnForecast(List<TaskTurnpoint> taskTurnpoints) {
        TaskTurnpoint taskTurnpoint;
        LatLng fromLatLng = new LatLng(0d, 0d); // to get rid of syntax checker
        LatLng toLatLng;

        if (googleMap != null) {

            if (taskTurnpoints != null && taskTurnpoints.size() > 0) {
                int numberTurnpoints = taskTurnpoints.size();
                for (int i = 0; i < taskTurnpoints.size(); ++i) {
                    taskTurnpoint = taskTurnpoints.get(i);
                    if (i == 0) {
                        fromLatLng = new LatLng(taskTurnpoint.getLatitudeDeg(), taskTurnpoint.getLongitudeDeg());
                        placeMarker(taskTurnpoint.getTitle(), "Start", fromLatLng);

                    } else {
                        toLatLng = new LatLng(taskTurnpoint.getLatitudeDeg(), taskTurnpoint.getLongitudeDeg());
                        placeMarker(taskTurnpoint.getTitle(),  (i < numberTurnpoints - 1 ?  String.format("%1$.1fkm", taskTurnpoint.getDistanceFromStartingPoint()): "Finish"), toLatLng);
                        drawLine(fromLatLng, toLatLng);
                        fromLatLng = toLatLng;
                    }
                    updateMapLatLongCorners(fromLatLng);
                }
                LatLng southwest = new LatLng(swLat, swLong);
                LatLng northeast = new LatLng(neLat, neLong);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                        southwest, northeast), 700, 700, 0));
            }

        }
    }

    /**
     * Find the most southwest and northeast task lat/long
     *
     * @param latLng
     */
    private void updateMapLatLongCorners(LatLng latLng) {
        if (southwest == null) {
            southwest = latLng;
            swLat = latLng.latitude;
            swLong = latLng.longitude;

            neLat = latLng.latitude;
            neLong = latLng.longitude;
        }
        if (latLng.latitude < swLat) {
            swLat = latLng.latitude;
        }
        if (latLng.longitude < swLong) {
            swLong = latLng.longitude;
        }
        if (latLng.latitude > neLat) {
            neLat = latLng.latitude;
        }
        if (latLng.longitude > neLong) {
            neLong = latLng.longitude;
        }

    }


    private void drawLine(LatLng fromLatLng, LatLng toLatLng) {
        Polyline polyline = googleMap.addPolyline(new PolylineOptions().add(fromLatLng, toLatLng)
                .width(5).color(Color.RED));
        taskTurnpointLines.add(polyline);
    }

    private void placeMarker(String title, String snippet, LatLng latLng) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .title(title)
                .snippet(snippet)
                .position(latLng));
        taskTurnpointMarkers.add(marker);
    }


    public void removeTaskTurnpoints() {
        drawingTask = false;
        for (Polyline polyline : taskTurnpointLines) {
            polyline.remove();
        }

        taskTurnpointLines.clear();
        for (Marker marker : taskTurnpointMarkers) {
            marker.remove();
        }
        taskTurnpointMarkers.clear();

    }
}