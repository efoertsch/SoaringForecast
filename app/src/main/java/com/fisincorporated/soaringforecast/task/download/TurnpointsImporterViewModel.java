package com.fisincorporated.soaringforecast.task.download;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.task.TurnpointsImporter;
import com.fisincorporated.soaringforecast.task.json.TurnpointFile;

import java.io.File;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class TurnpointsImporterViewModel extends ViewModel {

    private TurnpointsImporter turnpointsImporter;
    private AppRepository appRepository;
    private MutableLiveData<List<TurnpointFile>> turnpointFiles =  new MutableLiveData();

    public TurnpointsImporterViewModel setTurnpointsImporter(TurnpointsImporter turnpointsImporter) {
        this.turnpointsImporter = turnpointsImporter;
        return this;
    }

    public TurnpointsImporterViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    public List<File> getCupFiles() {
        return turnpointsImporter.getDownloadedCupFileList();
    }

    @SuppressLint("CheckResult")
    public LiveData<List<TurnpointFile>> getTurnpointFiles() {
       appRepository.getTurnpointFiles(Constants.NEWENGLAND_REGION)
               .subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe(turnpointFileList ->
                       turnpointFiles.setValue(turnpointFileList)
               ,t -> {
                 Timber.e(t);
                });
       return turnpointFiles;
    }


}
