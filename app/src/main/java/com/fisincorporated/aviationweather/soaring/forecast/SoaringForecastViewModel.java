package com.fisincorporated.aviationweather.soaring.forecast;

import android.animation.ValueAnimator;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.InverseBindingAdapter;
import android.databinding.InverseBindingListener;
import android.graphics.Color;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.app.ViewModelLifeCycle;
import com.fisincorporated.aviationweather.common.TouchImageView;
import com.fisincorporated.aviationweather.databinding.SoaringForecastImageBinding;
import com.fisincorporated.aviationweather.messages.DataLoadCompleteEvent;
import com.fisincorporated.aviationweather.messages.DataLoadingEvent;
import com.fisincorporated.aviationweather.messages.ReadyToSelectSoaringForecastEvent;
import com.fisincorporated.aviationweather.retrofit.SoaringForecastApi;
import com.fisincorporated.aviationweather.soaring.json.RegionForecastDate;
import com.fisincorporated.aviationweather.soaring.json.RegionForecastDates;
import com.fisincorporated.aviationweather.soaring.json.TypeLocationAndTimes;

import org.cache2k.Cache;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import timber.log.Timber;

public class SoaringForecastViewModel extends BaseObservable implements ViewModelLifeCycle {

    private static final String TAG = SoaringForecastViewModel.class.getSimpleName();

    private SoaringForecastImageBinding viewDataBinding;
    private TouchImageView soaringForecastImageView;
    private TextView localTimeTextView;
    private SoaringForecastDate soaringForecastImageInfo;
    private ValueAnimator soaringForecastImageAnimation;
    private int lastImageIndex = -1;


    private Call<RegionForecastDates> regionForecastDatesCall;
    private RegionForecastDates regionForecastDates = new RegionForecastDates();

    private Call<TypeLocationAndTimes> typeLocationAndTimesCall;
    private TypeLocationAndTimes typeLocationAndTimes;

    private SoaringForecastType selectedSoaringForecastType;
    private RegionForecastDate selectedRegionForecastDate = new RegionForecastDate();

    private List<SoaringForecastDate> soaringForecastDates;

    private boolean bypassSoaringForecastTypeChange = true;
    private boolean bypassForecastDateChange = true;

    private View bindingView;

    @Inject
    public SoaringForecastDownloader soaringForecastDownloader;

    @Inject
    public Cache<String, SoaringForecastImage> soaringForecastImageCache;

    @Inject
    public List<SoaringForecastType> soaringForecastTypes;

    @Inject
    public AppPreferences appPreferences;

    @Inject
    public SoaringForecastApi soaringForecastApi;

    @Inject
    SoaringForecastViewModel() {
    }

    public SoaringForecastViewModel setView(View view) {
        fireLoadStarted();
        bindingView = view.findViewById(R.id.soaring_forecast_layout);
        viewDataBinding = DataBindingUtil.bind(bindingView);
        // not doing binding until forecast dates obtained
        soaringForecastDownloader.loadForecastsForDay(appPreferences.getSoaringForecastRegion());

        return this;
    }

    public void bindViewModel() {
        viewDataBinding.setViewModel(this);
        selectedSoaringForecastType = appPreferences.getSoaringForecastType();
        setSpinnerSelectedSoaringForecastType();
    }

    @Override
    public void onResume() {
        // Setting the initial satellite region and type will cause call update images so need
        //to bypass
        EventBus.getDefault().register(this);

    }

    private void ignoreSpinnerChanges() {
        bypassSoaringForecastTypeChange = true;
        bypassForecastDateChange = true;

    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        soaringForecastDownloader.cancelOutstandingLoads();
        stopImageAnimation();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataLoadingEvent event) {
        stopImageAnimation();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DataLoadCompleteEvent event) {
        startImageAnimation();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RegionForecastDates regionForecastDates){
        this.regionForecastDates = regionForecastDates;
        selectedRegionForecastDate.copy(regionForecastDates.getForecastDates().get(0));
        setSpinnerSelectedForecastDate();

    }

    @Subscribe(threadMode =  ThreadMode.MAIN)
    public void onMessageEvent(ReadyToSelectSoaringForecastEvent readyToSelectSoaringForecastEvent){
        // TODO - load bitmaps for hcrit for first forecast type (e.g. gfs)
        xxxx
    }
    @Override
    public void onDestroy() {
        soaringForecastDownloader.shutdown();
        stopImageAnimation();
    }

    private void loadSoaringForecastImages() {
        stopImageAnimation();
        //soaringForecastImageView.setImageBitmap(null);
        soaringForecastDownloader.loadSoaringForcecastImages(selectedSoaringForecastType.getName(), selectedRegionForecastDate.getYyyymmddDate(), "hwcrit" );
    }

    private void stopImageAnimation() {
        if (soaringForecastImageAnimation != null) {
            soaringForecastImageAnimation.cancel();
        }
    }

    public List<SoaringForecastType> getSoaringForecastTypes() {
        return soaringForecastTypes;
    }

    public SoaringForecastType getSelectedSoaringForecastType() {
        return selectedSoaringForecastType;
    }

    // Called by generated databinding code - see xml
    // Bypass first call as first time setting type on UI triggers the call
    public void setSelectedSoaringForecastType(SoaringForecastType soaringForecastType) {
        if (bypassSoaringForecastTypeChange) {
            bypassSoaringForecastTypeChange = false;
            return;
        }
        selectedSoaringForecastType = soaringForecastType;
        appPreferences.setSoaringForecastType(selectedSoaringForecastType);
        loadSoaringForecastImages();
    }


    @BindingAdapter(value = {"bind:selectedForecastType", "bind:selectedForecastTypeAttrChanged"}, requireAll = false)
    public static void bindSpinnerData(AppCompatSpinner spinner, SoaringForecastType newSelectedValue, final InverseBindingListener newSoaringForecastTypeAttrChanged) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newSoaringForecastTypeAttrChanged.onChange();
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

    @InverseBindingAdapter(attribute = "bind:selectedForecastType", event = "bind:selectedForecastTypeAttrChanged")
    public static SoaringForecastType getSelectedSoaringForecastType(AppCompatSpinner spinner) {
        return (SoaringForecastType) spinner.getSelectedItem();
    }

    private void setSpinnerSelectedSoaringForecastType() {
        viewDataBinding.soaringForecastTypeSpinner.setSelection(selectedSoaringForecastType != null ? selectedSoaringForecastType.getId() : 0);
    }


    public List<RegionForecastDate> getRegionForecastDates() {
        return regionForecastDates.getForecastDates();
    }

    @Bindable
    public RegionForecastDate getSelectedRegionForecastDate() {
        return selectedRegionForecastDate;
    }

    // Called by generated databinding code - see xml
    // Bypass first call as first time setting type on UI triggers the call
    public void setSelectedRegionForecastDate(RegionForecastDate forecastDate) {
        if (bypassForecastDateChange) {
            bypassForecastDateChange = false;
            return;
        }
        selectedRegionForecastDate = forecastDate;
        loadSoaringForecastImages();
    }

    @BindingAdapter(value = {"selectedRegionForecastDate", "selectedRegionForecastDateAttrChanged"}, requireAll = false)
    public static void bindForecastDateSpinnerData(AppCompatSpinner spinner, RegionForecastDate newSelectedValue
            , final InverseBindingListener newRegionForecastDateAttrChanged) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newRegionForecastDateAttrChanged.onChange();

                ((TextView) view).setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Timber.d("Nothing selected");
            }
        });
        if (newSelectedValue != null) {
            int pos = spinner.getSelectedItemPosition();
            spinner.setSelection(pos, true);
        }
    }

    @InverseBindingAdapter(attribute = "selectedRegionForecastDate", event = "selectedRegionForecastDateAttrChanged")
    public  static RegionForecastDate captureSelectedDate(AppCompatSpinner spinner) {
        return (RegionForecastDate) spinner.getSelectedItem();
    }


//    private void loadRegionForecastDatesAndTypes() {
//        soaringForecastDownloader.loadForecastsForDay();
//    }

    // Get forecasted dates
//    @SuppressLint("CheckResult")
//    private void callForecastCurrentJson() {
//        soaringForecastApi.getForecastDates("current.json?" + new Date().getTime())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//        .subscribe(this::storeRegionForecastDates);
//    }

//    private void storeRegionForecastDates(RegionForecastDates regionForecastDates) {
//        regionForecastDates.parseForecastDates();
//        selectedRegionForecastDate.copy(regionForecastDates.getForecastDates().get(0));
//        setSpinnerSelectedForecastDate();
//        soaringForecastDownloader.loadTypeLocationAndTimes(appPreferences.getSoaringForecastRegion(), regionForecastDates);
//
//    }

    private void setSpinnerSelectedForecastDate() {
        viewDataBinding.soaringForecastDateSpinner.setSelection(selectedRegionForecastDate != null ? selectedRegionForecastDate.getIndex() : 0);
    }

//    // Call to get info on various forecast models - note currently just one region - NewEngland
//    private void callStatusJson(String region, String yyyymmddDate) {
//        //"NewEngland/2018-03-30/status.json"
//        typeLocationAndTimesCall = soaringForecastApi.getTypeLocationAndTimes(region + "/" + yyyymmddDate + "/status.json");
//        typeLocationAndTimesCall.enqueue(new Callback<TypeLocationAndTimes>() {
//            @Override
//            public void onResponse(Call<TypeLocationAndTimes> call, Response<TypeLocationAndTimes> response) {
//                typeLocationAndTimes = response.body();
//                bindViewModel();
//                loadSoaringForecastImages();
//            }
//
//            @Override
//            public void onFailure(Call<TypeLocationAndTimes> call, Throwable t) {
//                displayCallFailure(t);
//            }
//        });
//    }

//    private void displayCallFailure(Throwable t) {
//        ViewUtilities.displayErrorDialog(viewDataBinding.getRoot(), bindingView.getContext().getString(R.string.oops), t.toString());
//    }


    private void startImageAnimation() {
        // need to 'overshoot' the animation to be able to get the last image value
//        soaringForecastImageAnimation = ValueAnimator.ofInt(0, soaringForecastImageInfo.getSoaringForecastImageNames().size());
//        soaringForecastImageAnimation.setInterpolator(new LinearInterpolator());
//        soaringForecastImageAnimation.setDuration(5000);
//        soaringForecastImageAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
//                int index = (int) updatedAnimation.getAnimatedValue();
//                // Timber.d("animation index: %d  ", index);
//                if (index > soaringForecastImageInfo.getSoaringForecastImageNames().size() - 1) {
//                    index = soaringForecastImageInfo.getSoaringForecastImageNames().size() - 1;
//                }
//                if (lastImageIndex != index) {
//                    // Don't force redraw if still on same image as last time
//                    soaringForecastImage = soaringForecastImageCache.get(soaringForecastImageInfo.getSoaringForecastImageNames().get(index));
//                    //Bypass if jpg/bitmap does not exist or not loaded yet.
//                    if (soaringForecastImage != null && soaringForecastImage.isImageLoaded()) {
//                        //Timber.d("Displaying image for %s - %d", satelliteImage.getImageName(), index);
//                        localTimeTextView.setText(soaringForecastImageInfo.getSoaringForecastImageLocalTimes().get(index));
//                        soaringForecastImage.setImageBitmap(satelliteImage.getBitmap());
//                        // Timber.d("set  image for image: %d", index );
//                    } else {
//                        // Timber.d("Satellite image: %s, Index %d image: %s",  satelliteImageInfo.getSatelliteImageNames().get(index),index, satelliteImage == null ? " not in cache" : (satelliteImage.isImageLoaded() ? "  bitmap loaded" : " bitmap not loaded"));
//                    }
//                    lastImageIndex = index;
//                }
//            }
//        });
//        soaringForecastImageAnimation.setRepeatCount(ValueAnimator.INFINITE);
//        soaringForecastImageAnimation.start();

    }

    private void fireLoadStarted() {
        EventBus.getDefault().post(new DataLoadingEvent());
    }

    private void fireLoadComplete() {
        EventBus.getDefault().post(new DataLoadCompleteEvent());
    }
}