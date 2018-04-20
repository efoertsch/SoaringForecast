package com.fisincorporated.aviationweather.soaring.forecast;

import android.animation.ValueAnimator;
import android.databinding.BaseObservable;
import android.databinding.DataBindingUtil;
import android.databinding.InverseBindingMethod;
import android.databinding.InverseBindingMethods;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Spinner;
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
import com.fisincorporated.aviationweather.soaring.json.GpsLocationAndTimes;
import com.fisincorporated.aviationweather.soaring.json.RegionForecastDate;
import com.fisincorporated.aviationweather.soaring.json.RegionForecastDates;
import com.fisincorporated.aviationweather.utils.ViewUtilities;

import org.cache2k.Cache;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@InverseBindingMethods({
        @InverseBindingMethod(type = Spinner.class, attribute = "android:selectedItemPosition"),})

public class SoaringForecastViewModel extends BaseObservable implements ViewModelLifeCycle, SoaringForecastTypeClickListener, RegionForecastDateClickListener {

    private static final String TAG = SoaringForecastViewModel.class.getSimpleName();

    private SoaringForecastImageBinding viewDataBinding;
    private TouchImageView soaringForecastImageView;
    private TextView localTimeTextView;
    private SoaringForecastDate soaringForecastImageInfo;
    private ValueAnimator soaringForecastImageAnimation;
    private int lastImageIndex = -1;

    private RegionForecastDates regionForecastDates = new RegionForecastDates();

    private SoaringForecastType selectedSoaringForecastType;
    private RegionForecastDate selectedRegionForecastDate = null;

    private View bindingView;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();


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
        bindViewModel();

        return this;
    }

    public void bindViewModel() {
        viewDataBinding = DataBindingUtil.bind(bindingView);
        if (viewDataBinding != null) {
            viewDataBinding.setViewModel(this);
            selectedSoaringForecastType = appPreferences.getSoaringForecastType();
            setupSoaringForecastTypeRecyclerView(soaringForecastTypes);
        }
    }

    private void setupSoaringForecastTypeRecyclerView(List<SoaringForecastType> soaringForecastTypes) {
        viewDataBinding.soaringForecastTypeRecyclerView.setHasFixedSize(true);
        viewDataBinding.soaringForecastTypeRecyclerView.setLayoutManager(
                new LinearLayoutManager(viewDataBinding.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false));
        // TODO add selectedSoaringForecastType
        RecyclerViewAdapterSoaringForecastType recyclerViewAdapter = new RecyclerViewAdapterSoaringForecastType(this, soaringForecastTypes);
        viewDataBinding.soaringForecastTypeRecyclerView.setAdapter(recyclerViewAdapter);
    }

    private void setupRegionForecastDateRecyclerView(List<RegionForecastDate> regionForecastDateList) {
        viewDataBinding.regionForecastDateRecyclerView.setHasFixedSize(true);
        viewDataBinding.regionForecastDateRecyclerView.setLayoutManager(
                new LinearLayoutManager(viewDataBinding.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false));
        // TODO add selectedSoaringForecastType
        RecyclerViewAdapterRegionForecastDate recyclerViewAdapter = new RecyclerViewAdapterRegionForecastDate(this, regionForecastDateList);
        viewDataBinding.regionForecastDateRecyclerView.setAdapter(recyclerViewAdapter);
    }



    @Override
    public void onResume() {
        // Setting the initial satellite region and type will cause call update images so need
        //to bypass
        EventBus.getDefault().register(this);
        soaringForecastDownloader.loadForecastsForDay(appPreferences.getSoaringForecastRegion());
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        soaringForecastDownloader.cancelOutstandingLoads();
        stopImageAnimation();
    }

    @Override
    public void onDestroy() {
        soaringForecastDownloader.shutdown();
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
    public void onMessageEvent(RegionForecastDates regionForecastDates) {
        storeRegionForecastDates(regionForecastDates);
        setupRegionForecastDateRecyclerView(regionForecastDates.getRegionForecastDateList());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ReadyToSelectSoaringForecastEvent readyToSelectSoaringForecastEvent) {
        // TODO - load bitmaps for hcrit for first forecast type (e.g. gfs)
        Timber.d("Ready to load bitmaps");
        startLoadingSoaringForecastImages();
    }

    private void startLoadingSoaringForecastImages() {
        new Thread(() -> loadSoaringForecastImages()).start();

    }

    private void storeRegionForecastDates(RegionForecastDates downloadedRegionForecastDates) {
        regionForecastDates = downloadedRegionForecastDates;
        regionForecastDates.parseForecastDates();
        selectedRegionForecastDate = regionForecastDates.getForecastDates().get(0);
        soaringForecastDownloader.loadTypeLocationAndTimes(appPreferences.getSoaringForecastRegion(), downloadedRegionForecastDates);
    }

    private void loadSoaringForecastImages() {
        stopImageAnimation();
        Single<SoaringForecastImage> soaringForecastImageSingle =  soaringForecastDownloader.getSoaringForecastImageObservable(bindingView.getContext().getString(R.string.new_england_region)
                , selectedRegionForecastDate.getYyyymmddDate()
                , selectedSoaringForecastType.getName()
                , "hwcrit"
                , getGpsLocationAndTimesForType(selectedSoaringForecastType.getName()).getTimes().get(0)
                , "body");
        soaringForecastImageSingle.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<SoaringForecastImage>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onSuccess(SoaringForecastImage soaringForecastImage) {
                displaySoaringForecastImage(soaringForecastImage);
            }

            @Override
            public void onError(Throwable e) {
                displayCallFailure(e);
            }
        });
    }

    private void displaySoaringForecastImage(SoaringForecastImage soaringForecastImage) {
        viewDataBinding.soaringForecastImageLocalTime.setText(soaringForecastImage.getForecastTime());
        viewDataBinding.soaringForecastImageView.setImageBitmap(soaringForecastImage.getBitmap());
    }

    private void stopImageAnimation() {
        if (soaringForecastImageAnimation != null) {
            soaringForecastImageAnimation.cancel();
        }
    }

    private void displayCallFailure(Throwable t) {
        ViewUtilities.displayErrorDialog(viewDataBinding.getRoot(), bindingView.getContext().getString(R.string.oops), t.toString());
    }




    private void fireLoadStarted() {
        EventBus.getDefault().post(new DataLoadingEvent());
    }

    private void fireLoadComplete() {
        EventBus.getDefault().post(new DataLoadCompleteEvent());
    }


    @Override
    public void setSoaringForecastType(SoaringForecastType soaringForecastType) {
        selectedSoaringForecastType = soaringForecastType;
        appPreferences.setSoaringForecastType(selectedSoaringForecastType);
    }

    @Override
    public void setRegionForecastDate(RegionForecastDate regionForecastDate) {
        selectedRegionForecastDate = regionForecastDate;
    }


    private GpsLocationAndTimes getGpsLocationAndTimesForType(String type) {
        switch (type) {
            case "GFS":
                return selectedRegionForecastDate.getTypeLocationAndTimes().getGfs();
            case "NAM":
                return selectedRegionForecastDate.getTypeLocationAndTimes().getNam();
            case "RAP":
                return selectedRegionForecastDate.getTypeLocationAndTimes().getRap();
            default:
                return null;

        }
    }

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
}