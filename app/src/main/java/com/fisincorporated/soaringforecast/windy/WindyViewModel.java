package com.fisincorporated.soaringforecast.windy;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.webkit.JavascriptInterface;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.repository.TaskTurnpoint;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class WindyViewModel extends AndroidViewModel {

    Context context;
    MutableLiveData<String> command;

    // TODO put zoom in appPreferences
    private int zoom = 7;
    private LatLng defaultLatLng = new LatLng(43.1393051, -72.076004);
    private AppPreferences appPreferences;
    private AppRepository appRepository;
    private MutableLiveData<List<TaskTurnpoint>> taskTurnpoints = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    public WindyViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
    }

     WindyViewModel setAppPreferences(AppPreferences appPreferences){
        this.appPreferences =  appPreferences;
        return this;
    }

    WindyViewModel setAppRepository(AppRepository appRepository){
        this.appRepository =  appRepository;
        return this;
    }


    public MutableLiveData<String> getCommand() {
        if (command == null) {
            command = new MutableLiveData<>();
        }
        return command;
    }

    @JavascriptInterface
    public void resetHeight() {
        command.setValue("javascript:setHeight('200px')");
    }

    public void drawLine(double fromLat, double fromLong, double toLat, double toLong) {
        command.setValue("javascript:drawLine( " + fromLat + "," + fromLong
                + "," + toLat + "," + toLong + ")");
    }

    @JavascriptInterface
    public String getWindyKey() {
        return context.getString(R.string.WindyKey);
    }

    @JavascriptInterface
    public double getLat() {
        return defaultLatLng.latitude;
    }

    @JavascriptInterface
    public double getLong() {
        return defaultLatLng.longitude;
    }

    @JavascriptInterface
    public double getZoom() {
        return appPreferences.getWindyZoomLevel();
    }

    public void getTask(long taskId) {
        Disposable disposable = appRepository.getTaskTurnpionts(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(taskTurnpointList -> {
                            appPreferences.setSelectedTaskId(taskId);
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

    public void getTask(){
        getTask(appPreferences.getSelectedTaskId());
    }

    public void setTaskId(long taskId) {
        appPreferences.setSelectedTaskId(taskId);
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }
}
