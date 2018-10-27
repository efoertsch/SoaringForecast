package com.fisincorporated.soaringforecast.task.turnpoints.download;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.task.json.TurnpointFile;

import java.io.File;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class TurnpointsImporterViewModel extends ViewModel {

    private AppRepository appRepository;
    private MutableLiveData<List<TurnpointFile>> turnpointFiles = new MutableLiveData();
    private MutableLiveData<List<File>> cupFiles = new MutableLiveData();

    public TurnpointsImporterViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    @SuppressLint("CheckResult")
    public LiveData<List<File>> getCupFiles() {
        appRepository.getDownloadedCupFileList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(downloadedCupFileList ->
                                cupFiles.setValue(downloadedCupFileList)
                        , Timber::e);
        return cupFiles;
    }

    @SuppressLint("CheckResult")
    public LiveData<List<TurnpointFile>> getTurnpointFiles() {
        appRepository.getTurnpointFiles(Constants.NEWENGLAND_REGION)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(turnpointFileList ->
                                turnpointFiles.setValue(turnpointFileList)
                        , Timber::e);
        return turnpointFiles;
    }

    public Single<Integer> clearTurnpointDatabase() {
        return appRepository.deleteAllTurnpoints();
    }
}
