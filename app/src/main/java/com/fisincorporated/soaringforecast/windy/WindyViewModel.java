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
import com.fisincorporated.soaringforecast.soaring.json.Model;
import com.fisincorporated.soaringforecast.soaring.json.ModelForecastDate;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

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
    private MutableLiveData<Boolean> startUpComplete;
    private ModelForecastDate selectedModelForecastDate;
    private LatLng selectedLatLng;


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

    public MutableLiveData<Boolean> getStartUpComplete() {
        if (startUpComplete == null) {
            startUpComplete = new MutableLiveData<>();
            getSelectedModelForecastDate();
            getTask();
        }
        return startUpComplete;
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
        return selectedLatLng.latitude;
    }

    @JavascriptInterface
    public double getLong() {
        return selectedLatLng.longitude;
    }

    @JavascriptInterface
    public double getZoom() {
        return appPreferences.getWindyZoomLevel();
    }

    @JavascriptInterface
    public  String getTaskTurnpointsForMap() {
        if (taskTurnpoints.getValue() == null) {
            return null;
        }
        Gson gson = new Gson();
        String taskJson = gson.toJson(taskTurnpoints.getValue());
        return taskJson;
    }


    public void getTask(){
        long taskId = appPreferences.getSelectedTaskId();
        if (taskId != -1) {
            getTask(taskId);
        } else {
            startUpComplete.setValue(true);
        }
    }

    public void getTask(long taskId) {
        Disposable disposable = appRepository.getTaskTurnpionts(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(taskTurnpointList -> {
                            appPreferences.setSelectedTaskId(taskId);
                            taskTurnpoints.setValue(taskTurnpointList);
                            startUpComplete.setValue(true);
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

    public void setTaskId(long taskId) {
        appPreferences.setSelectedTaskId(taskId);
    }

    public void getSelectedModelForecastDate() {
        selectedModelForecastDate = appPreferences.getSelectedModelForecastDate();
        if (selectedModelForecastDate != null ) {
            Model model = selectedModelForecastDate.getModel();
            if (model != null) {
                selectedLatLng = new LatLng(model.getCenter().get(0), model.getCenter().get(1));
                return;
            }
        }
        selectedLatLng = defaultLatLng;
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }

}
