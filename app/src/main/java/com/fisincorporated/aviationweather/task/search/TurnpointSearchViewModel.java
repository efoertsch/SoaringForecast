package com.fisincorporated.aviationweather.task.search;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.repository.Turnpoint;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class TurnpointSearchViewModel extends ViewModel {

    private MutableLiveData<List<Turnpoint>> turnpoints = new MutableLiveData<>();
    private AppRepository appRepository;

    public TurnpointSearchViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        turnpoints = new MutableLiveData<>();
        turnpoints.setValue(new ArrayList<>());
        return this;
    }

    @SuppressLint("CheckResult")
    public LiveData<List<Turnpoint>> searchTurnpoints(String search) {
        appRepository.findTurnpoints("%" + search + "%")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(turnpointList -> {
                            turnpoints.setValue(turnpointList);
                        },
                        t -> {
                            Timber.e(t);
                        });
        return turnpoints;
    }
}
