package org.soaringforecast.rasp.settings.regions;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.soaring.forecast.SoaringForecastDownloader;
import org.soaringforecast.rasp.soaring.json.Region;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class RegionSelectionViewModel extends AndroidViewModel {

    private SoaringForecastDownloader soaringForecastDownloader;
    private AppRepository appRepository;
    private AppPreferences appPreferences;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    // Used to signal changes to UI
    private MutableLiveData<Boolean> working = new MutableLiveData<>();
    private MutableLiveData<List<Region>> regions;

    public RegionSelectionViewModel(@NonNull Application application) {
        super(application);
    }

    public RegionSelectionViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    public RegionSelectionViewModel setAppPreferences(AppPreferences appPreferences) {
        this.appPreferences = appPreferences;
        return this;
    }

    public RegionSelectionViewModel setSoaringForecastDownloader(SoaringForecastDownloader soaringForecastDownloader) {
        this.soaringForecastDownloader = soaringForecastDownloader;
        return this;
    }

    public MutableLiveData<List<Region>> getRegions() {
        if (regions == null) {
            regions = new MutableLiveData<>();
            getRegionForecastDates();
        }
        return regions;
    }

    public String getSelectedRegion() {
        return appPreferences.getSoaringForecastRegion();
    }


    private void getRegionForecastDates() {
        working.setValue(true);
        Disposable disposable = soaringForecastDownloader.getRegionForecastDates()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(newRegionForecastDates -> {
                            regions.setValue(newRegionForecastDates.getRegions());
                            working.setValue(false);
                        },
                        throwable -> {
                            Timber.d("Error: %s ", throwable.getMessage());
                            //TODO - put error on bus
                            throwable.printStackTrace();
                            working.setValue(false);
                        });
        compositeDisposable.add(disposable);
    }

    public void setSoaringForecastRegion(String region) {
        appPreferences.setSoaringForecastRegion(region);
    }

    public LiveData<Boolean> getWorking() {
        return working;
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }

}
